package org.supremica.automata.algorithms.scheduling;

import java.util.*;
import java.io.*;

import org.supremica.automata.*;
import org.supremica.automata.algorithms.*;
import org.supremica.gui.ActionMan;
import org.supremica.gui.Gui;
import org.supremica.gui.ScheduleDialog;
import org.supremica.log.*;
import org.supremica.util.ActionTimer;
import org.supremica.util.BDD.solvers.TSPSolver;

//TODO: Strukturera upp koden. Kommentera. 
public class Milp
        implements Scheduler
{
    /****************************************************************************************/
    /*                                 VARIABLE SECTION                                     */
    /****************************************************************************************/
    
    /** The logger */
    private static Logger logger = LoggerFactory.createLogger(Milp.class);
    
    private static final int NO_PATH_SPLIT_INDEX = -1;
    
    /** A big enough value used by the MILP-solver (should be greater than any time-variable). */
    private static final int BIG_M_VALUE = 120000;
    
    /** The involved automata, plants, zone specifications */
    private Automata theAutomata, plants, zones, externalSpecs;
    
    // prec_temp
    // private Automata specs;
    
    /** The project used for return result */
    // NOT USED YET...
    // private Project theProject;
    
    /** int[zone_nr][plant_nr][state_nr] - stores the states that fire booking/unbooking events */
    private int[][][] bookingTics, unbookingTics;
    
    /** The optimal cycle time (makespan) */
    private double makespan;
    
    /** The *.mod file that serves as an input to the Glpk-solver */
    private File modelFile;
    
    /** The *.sol file that stores the solution, i.e. the output of the Glpk-solver */
    private File solutionFile;
    
    /** The optimal times (for each plantstate) that the MILP solver returns */
    private double[][] optimalTimes = null;
    
    /** The path choice variables (booleans) for each [plant][start_state][end_state] */
    private boolean[][][] pathChoices = null;
    
    /** Needed to kill the MILP-solver if necessary */
    private Process milpProcess;
    
    /** The map that assigns an index to each automaton/state/event */
    private AutomataIndexMap indexMap;
    
    /** The automaton representing the (resulting) optimal schedule */
    private Automaton schedule = null;
    
    /**
     * Contains the boolean expressions that control the path alternatives,
     * indicated by the first states of each alt. path. It is needed to ensure
     * that all alternative times but one are set close to -INF by the
     * MILP solver, even if they would interfere with the mutex constraints.
     */
    private Hashtable<State,String> pathCutTable = null;
    
    /** The timer */
    private ActionTimer timer = new ActionTimer();
    
    /** This boolean is true if the scheduler-thread should be (is) running */
    private volatile boolean isRunning = false;
    
    /** The dialog box that launched this scheduler */
    private ScheduleDialog scheduleDialog;
    
    /** Decides if the schedule should be built */
    private boolean buildSchedule;
    
    /** The output string */
    private String outputStr = "";
    
    /** The constrainst represented by external specifications, in string form. */
    private String externalConstraints = "";
    
    /**
     *  The declaration of variables, used in internal ordering of precedence constraints.
     **/
    private String internalPrecVarDecl = "";
    
//TODO: Remove "reverse"-variables when the other thing works. @Deprecated    
//    /** 
//     *  The declaration of variables, used to invalidate (reverse) multi-plant 
//     *  precedence constraints. 
//     */
//    private String precReversalVarDecl = "";
    
    /*
     * The thread that performs the search for the optimal solution
     */
    private Thread milpThread;
    
    /**
     *  The safety buffer between unbooking and booking, used in MILP. To use the 
     *  automatic deduction of epsilon from the optmal time values in  
     *  @see{buildScheduleAutomaton}, it should be a power of 10. For correct 
     *  functioning, this variable should be strictly smaller than 10^(-x), where
     *  x is the total number of (individual) plant states. 
     */
    private final double EPSILON = 0.001;
    
    /**
     *  Used to round off the optimal times, as returned by MILP, thus removing
     *  the added epsilons.
     */
    private double roundOffCoeff = -1;
    
    /****************************************************************************************/
    /*                                 CONSTUCTORS                                          */
    /****************************************************************************************/
    
    public Milp(Automata theAutomata, boolean buildSchedule, ScheduleDialog scheduleDialog)
    throws Exception
    {
        this.theAutomata = theAutomata;
        //  this.theProject = theProject;
        this.buildSchedule = buildSchedule;
        this.scheduleDialog = scheduleDialog;
    }
    
    public void startSearchThread()
    {
        milpThread = new Thread(this);
        isRunning = true;
        milpThread.start();
    }
    
    public void run()
    {
        try
        {
            schedule();
        }
        catch (Exception ex)
        {
            if (milpProcess != null)
            {
                milpProcess.destroy();
                milpProcess = null;
            }
            if (modelFile != null)
            {
                //modelFile.delete();
            }
            if (solutionFile != null)
            {
                solutionFile.delete();
            }
            if (scheduleDialog != null)
            {
                scheduleDialog.close();
            }
            
            logger.error("Milp::schedule() -> " + ex);
            logger.debug(ex.getStackTrace());
        }
    }
    
    /****************************************************************************************/
    /*                                 "SCHEDULER"-INTERFACE METHODS                        */
    /****************************************************************************************/
    
    /**
     * This method converts the selected automata to a MILP-representation, calls the MILP-solver,
     * processes and stores the resulting information.
     */
    public void schedule()
        throws Exception
    {
        ActionTimer totalTimer = new ActionTimer();
        
        if (isRunning)
        {
            totalTimer.restart();
            initialize();
        }
        
        // Converts automata to constraints that the MILP-solver takes as input (*.mod file)
        if (isRunning)
        {
            convertAutomataToMilp();
        }
        
        // Calls the MILP-solver
        if (isRunning)
        {
            callMilpSolver();
        }
        
        // Processes the output from the MILP-solver (*.sol file) and stores the optimal times for each state
        if (isRunning)
        {
            processSolutionFile();
            
            String totalTimeStr = "Total optimization time = " + totalTimer.elapsedTime() + "ms";
            logger.info(totalTimeStr);
        }
        
        // Builds the optimal schedule (if solicited)
        if (isRunning && buildSchedule)
        {
            buildScheduleAutomaton();
        }
        
        if (isRunning)
        {
            requestStop(true);
        }
        else
        {
            logger.warn("Scheduling interrupted");
            
            {
                scheduleDialog.reset();
            }
        }
    }
    
    /**
     * This method constructs an automaton representing the optimal schedule, using the
     * sequence of times (one time value for every state of the involved plant automata)
     * that the MILP-solver has generated.
     *
     * Does not return anything? The automaton representing the optimal schedule, as given
     * by the MILP-solver can be accessed by the getSchedule-method
     */
    public void buildScheduleAutomaton()
        throws Exception
    {                
        //TODO: temp (fulhack) - fixa bättre schemabygge.
        SynthesizerOptions synthesizerOptions = new SynthesizerOptions();
        synthesizerOptions.setSynthesisType(SynthesisType.NONBLOCKINGCONTROLLABLE);
        synthesizerOptions.setSynthesisAlgorithm(SynthesisAlgorithm.MONOLITHIC);
        synthesizerOptions.setPurge(true);
        synthesizerOptions.setMaximallyPermissive(true);
        synthesizerOptions.setMaximallyPermissiveIncremental(true);
        
        AutomataSynthesizer synthesizer = new AutomataSynthesizer(theAutomata, SynchronizationOptions.getDefaultSynthesisOptions(), synthesizerOptions);
        
        Automaton synthAll = synthesizer.execute().getFirstAutomaton();
        synthAll.setName("SYNTH_OVER_ALL");
        synthAll.setType(AutomatonType.PLANT);
        State synthState = synthAll.getInitialState();
        
        
        // Create the automaton with a chosen name
        String scheduleName = "";
        while (scheduleName != null && scheduleName.trim() == "")
        {
            scheduleName = ActionMan.getGui().getNewAutomatonName("Enter a name for the schedule", "Schedule");
        }       
        if (scheduleName == null)
        {
            return;
        }
        schedule = new Automaton(scheduleName);
        
        // Start the clock
        timer.restart();
        
        SynchronizationStepper stepper = new SynchronizationStepper(theAutomata);
        
        // The current synchronized state indices, consisting of as well plant
        // as specification indices and used to step through the final graph following the
        // time schedule (which is needed to build the schedule automaton)
        int[] currComposedStateIndices = stepper.getInitialStateIndices();
        
        // The first set of state indices correspond to the initial state of the composed automaton.
        // They are thus used to construct the initial state of the schedule automaton.
        State currScheduledState = makeScheduleState(currComposedStateIndices, true);
        
        // This alphabet is needed to check which events are common to the plants
        // If several transitions with the same event are enabled simultaneously, naturally
        // the transition having the greatest time value should be fired
        Alphabet commonPlantEventsAlphabet = new Alphabet();
        for (int i=0; i<plants.size() - 1; i++)
        {
            Alphabet firstAlphabet = plants.getAutomatonAt(i).getAlphabet();
            
            for (int j=i+1; j<plants.size(); j++)
            {
                Alphabet secondAlphabet = plants.getAutomatonAt(j).getAlphabet();
                
                commonPlantEventsAlphabet.addEvents(AlphabetHelpers.intersect(firstAlphabet, secondAlphabet));
            }
        }
        
        // Walk from the initial state until an accepting (synchronized) state is found
        // In every step, the cheapest allowed transition is chosen
        // This is done until an accepting state is found for our schedule
        while (! currScheduledState.isAccepting())
        {
            // Every plant is checked for possible transitions and the one with smallest time value
            // is chosen.
            double smallestTime = Double.MAX_VALUE;
            
            // The event that is next to be fired in the schedule (i.e. the event corresponding to the smallest allowed time value) is stored
            LabeledEvent currOptimalEvent = null;
            
            // The index of a plant in the "plants"-variable. Is increased whenever a plant is found
            int plantIndex = -1;
            
            // Stores the highest firing times for each active synchronizing event
            Hashtable<LabeledEvent, Double> synchArcsInfo = new Hashtable<LabeledEvent, Double>();
     
            // Which automaton fires the "cheapest" transition...
            for (Iterator<Automaton> autIt = theAutomata.iterator(); autIt.hasNext(); )
            {
//                 Automaton currPlant = indexMap.getAutomatonAt(i);
                Automaton currPlant = autIt.next();
                
                // Since the plants are supposed to fire events, the check for the "smallest time event"
                // is only done for the plants
                if (currPlant.isPlant())
                {
//                     plantIndex++;
                    plantIndex = indexMap.getAutomatonIndex(currPlant);
                    
                    State currState = indexMap.getStateAt(currPlant, currComposedStateIndices[plantIndex]);
                    double currTime = optimalTimes[plantIndex][currComposedStateIndices[plantIndex]];
                   
                    // Choose the smallest time (as long as it is not smaller than the previously scheduled time)...
                    if (currTime <= smallestTime)
                    {
                        for (Iterator<Arc> arcs = currState.outgoingArcsIterator(); arcs.hasNext(); )
                        {
                            Arc currArc = arcs.next();
                            LabeledEvent currEvent = currArc.getEvent();
                            
                            // ... that correspoinds to an enabled transition
//                             if (stepper.isEnabled(currEvent))
                            // temp (fulhack)
                            if (synthState.nextState(currEvent) != null)
                            {
                                int currStateIndex = indexMap.getStateIndex(currPlant, currState);
                                int nextStateIndex = indexMap.getStateIndex(currPlant, currArc.getToState());
                                
                                // If the next node has lower time value, then it cannot belong
                                // to the optimal path, since precedence constraints are not fulfilled
                                // Otherwise, this could be the path
//                                 if (optimalTimes[plantIndex][nextStateIndex] >= currTime + indexMap.getStateAt(currPlant, nextStateIndex).getCost())
                                if (pathChoices[plantIndex][currStateIndex][nextStateIndex])
                                {
                                    // But! Care should be taken with the events common to any pair of plants.
                                    // When synched, the slowest plant should fire the synchronizing event.
                                    // If this event is unique for some plant...
                                    if (! commonPlantEventsAlphabet.contains(currEvent))
                                    {
                                        currOptimalEvent = currEvent;
                                        smallestTime = currTime;
                                    }
                                    // ... and if not, the current time is put away to be processed when all
                                    // the plants' outgoing events have been processed (for this state).
                                    // The highest time value (so far) is stored for every synchronizing event.
                                    else
                                    {
                                        Double currSynchTime = synchArcsInfo.get(currEvent);
                                        
                                        if (currSynchTime == null)
                                        {
                                            synchArcsInfo.put(currEvent, new Double(currTime));
                                        }
                                        else if (currSynchTime.intValue() < currTime)
                                        {
                                            synchArcsInfo.put(currEvent, new Double(currTime));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // Here is the check to see if there have been any synchronizing events. If so, and if the
            // time of the slowest synchronizing event is smaller than the current optimal time, obviously
            // an update is needed.
            if (synchArcsInfo.size() > 0)
            {
                for (Iterator<LabeledEvent> synchEvents = synchArcsInfo.keySet().iterator(); synchEvents.hasNext(); )
                {
                    LabeledEvent currSynchEvent = synchEvents.next();
                    double currSynchTime = synchArcsInfo.get(currSynchEvent).doubleValue();
                    
                    if (currSynchTime <= smallestTime)
                    {
                        currOptimalEvent = currSynchEvent;
                        smallestTime = currSynchTime;
                    }
                }
            }
            
            // Remove the added epsilons from the smallest time, thus obtaining the true smallest time
            smallestTime = removeEpsilons(smallestTime);
            
            // Add the transition time to the name of the state-to-be-in-the-schedule
            currScheduledState.setName(currScheduledState.getName() + ";  firing_time = " + smallestTime);
            //			currScheduledState.setCost(smallestTime);
            
            // Make a transition (in the synchronizer) to the state that is reachable in one step at cheapest cost
            // This state will be the next parting point in our walk
            currComposedStateIndices = stepper.step(currComposedStateIndices, currOptimalEvent);

            // Update the schedule automaton
            State nextScheduledState = makeScheduleState(currComposedStateIndices);
            
            if (! schedule.getAlphabet().contains(currOptimalEvent))
            {
                schedule.getAlphabet().addEvent(currOptimalEvent);
            }
            schedule.addArc(new Arc(currScheduledState, nextScheduledState, currOptimalEvent));
            
            //temp (fulhack)
            synthState = synthState.nextState(currOptimalEvent);

            currScheduledState = nextScheduledState;
            
            // If all the states that build up the current state are accepting, make the composed state accepting too
            boolean isAccepting = true;
            for (int i=0; i<theAutomata.size(); i++)
            {
                if (! indexMap.getStateAt(indexMap.getAutomatonAt(i), currComposedStateIndices[i]).isAccepting())
                {
                    isAccepting = false;
                }
            }
            
            if (isAccepting)
            {
                if (smallestTime != makespan)
                {
                    logger.info("curroptimalevent = " + currOptimalEvent.getName());
                    throw new Exception("Makespan value does NOT correspond to the cost of the final state of the schedule (sched_time = " + smallestTime + "; makespan = " + makespan + "). Something went wrong...");
                }
                
                currScheduledState.setAccepting(true);
                currScheduledState.setName(currScheduledState.getName() + ";  makespan = " + makespan);
            }
        }
        
        // A dummy reset-event that returns the schedule automaton from its accepting
        // to its initial state. Needed to describe repetitive working cycles...
        String resetEventName = "reset";
        while (theAutomata.getPlantAutomata().getUnionAlphabet().contains(resetEventName))
        {
            resetEventName += "1";
        }
        LabeledEvent resetEvent = new LabeledEvent(resetEventName);
        
        // The reset event brings the schedule to its initial state...
        schedule.getAlphabet().addEvent(resetEvent);
        schedule.addArc(new Arc(currScheduledState, schedule.getInitialState(), resetEvent));
        
        // ...But then also the participating plants should have this event in their initial states
        for (Automaton plantAuto : theAutomata.getPlantAutomata())
        {
            plantAuto.addArc(new Arc(plantAuto.getInitialState(), plantAuto.getInitialState(), resetEvent));
        }
        
        String str = "Time to build the schedule: " + timer.elapsedTime() + "ms ";
        logger.info(str);
        outputStr += "\t" + str;
        
        addAutomatonToGui(schedule);
    }
    
    
    /****************************************************************************************/
    /*                                 INIT METHODS                                         */
    /****************************************************************************************/
    
    /**
     * Creates the (temporary) *.mod- and *.sol- files that are used to communicate
     * with the MILP-solver (GLPK). The automata are preprocessed (syntes and purge)
     * while the information about the location of booking/unbooking events is collected.
     */
    private void initialize()
        throws Exception
    {
        modelFile = File.createTempFile("milp", ".mod");
        modelFile.deleteOnExit();

        solutionFile = File.createTempFile("milp", ".sol");
        solutionFile.deleteOnExit();
        
        logger.info("model: " + modelFile.getPath());
        logger.info("solution: " + solutionFile.getPath());        
        
        indexMap = new AutomataIndexMap(theAutomata);
        pathCutTable = new Hashtable<State,String>();
        
        initAutomata();
        initMutexStates();
    }
    
    
    /**
     * Goes through the supplied automata and synchronizes all specifications
     * that do not represent zones with corresponding plant automata.
     * This assumes that plants and the specifications regulating their behavior
     * have similar roots. For example if plant.name = "PLANT_A", the specification.name
     * should include "PLANT_A". The resulting plant and zone automata are stored globally.
     */
    private void initAutomata()
        throws Exception
    {
        plants = theAutomata.getPlantAutomata();
        zones = new Automata();
        externalSpecs = new Automata();
        Automata allSpecs = theAutomata.getSpecificationAutomata();
        
        Hashtable<Automaton, Automata> toBeSynthesizedSet = new Hashtable<Automaton, Automata>(plants.size());
        for (Iterator<Automaton> specIt = allSpecs.iterator(); specIt.hasNext(); )
        {
            Automaton spec = specIt.next();
            Alphabet specAlphabet = spec.getAlphabet();
            int counter = 0;
            Automaton latestPlant = null;
            
            for (Iterator<Automaton> plantIt = plants.iterator(); plantIt.hasNext(); )
            {
                Automaton plant = plantIt.next();
                
                if (specAlphabet.overlap(plant.getAlphabet()))
                {
                    counter++;
                    latestPlant = plant;
                }
            }
            
            if (counter == 0)
            {
                logger.warn("Specification " + spec.getName() + " has no common events with any of the plants");
            }
            else if (counter == 1)
            {
                Automata toBeSynthesized = toBeSynthesizedSet.get(latestPlant);
                if (toBeSynthesized == null)
                {
                    toBeSynthesized = new Automata(latestPlant);
                }
                toBeSynthesized.addAutomaton(spec);
                toBeSynthesizedSet.put(latestPlant, toBeSynthesized);
            }
        }
        
        for (Enumeration<Automaton> keysEnum = toBeSynthesizedSet.keys(); keysEnum.hasMoreElements(); )
        {
            Automaton plant = keysEnum.nextElement();
            Automata toBeSynthesized = toBeSynthesizedSet.get(plant);
            
            if (toBeSynthesized != null)
            {
                // Store the costs of each of the plants states
                double[] costs = new double[plant.nbrOfStates()];
                for (Iterator<State> stateIter = plant.stateIterator(); stateIter.hasNext(); )
                {
                    State currState = stateIter.next();
                    int stateIndex = indexMap.getStateIndex(plant, currState);
                    costs[stateIndex] = currState.getCost();
                }
                
                // If there are several automata with similar names (one is a plant the other are
                // restricting specification), then perform a synthesis
                SynthesizerOptions synthesizerOptions = new SynthesizerOptions();
                synthesizerOptions.setSynthesisType(SynthesisType.NONBLOCKINGCONTROLLABLE);
                synthesizerOptions.setSynthesisAlgorithm(SynthesisAlgorithm.MONOLITHIC);
                synthesizerOptions.setPurge(true);
                synthesizerOptions.setMaximallyPermissive(true);
                synthesizerOptions.setMaximallyPermissiveIncremental(true);
                
                AutomataSynthesizer synthesizer = new AutomataSynthesizer(toBeSynthesized, SynchronizationOptions.getDefaultSynthesisOptions(), synthesizerOptions);
                
                Automaton restrictedPlant = synthesizer.execute().getFirstAutomaton();
                restrictedPlant.setName(plant.getName());
                restrictedPlant.setType(AutomatonType.PLANT);
                
                // Set the state costs for the resulting synthesized automaton in an appropriate way
                for (Iterator<State> stateIter = restrictedPlant.stateIterator(); stateIter.hasNext(); )
                {
                    State currState = stateIter.next();
                    String stateName = currState.getName().substring(0, currState.getName().indexOf("."));
                    int stateIndex = indexMap.getStateIndex(restrictedPlant, new State(stateName));
                    
                    currState.setIndex(stateIndex);
                    currState.setCost(costs[stateIndex]);
                }
                
                // Remove the specifications that have been synthesized
                String str = "";
                for (Iterator<Automaton> toBeSynthesizedIt = toBeSynthesized.iterator(); toBeSynthesizedIt.hasNext(); )
                {
                    Automaton isSynthesized = toBeSynthesizedIt.next();
                    
                    str += isSynthesized.getName() + " || ";
                    
                    if (isSynthesized.isSpecification())
                    {
                        allSpecs.removeAutomaton(isSynthesized);
                    }
                }
                
                String plantName = restrictedPlant.getName();
                plants.removeAutomaton(plantName);
                restrictedPlant.setName(plantName + "_constrained");
                plants.addAutomaton(restrictedPlant);
                
                logger.info(str.substring(0, str.lastIndexOf("||")) + " synthesized into " + restrictedPlant.getName());
            }
        }
                
        for (Iterator<Automaton> plantIt = plants.iterator(); plantIt.hasNext(); )
        {
            prepareAutomatonForMilp(plantIt.next());
        }
        
        for (Iterator<Automaton> specsIt = allSpecs.iterator(); specsIt.hasNext(); )
        {
            Automaton spec = specsIt.next();
            
            if (isMutexZone(spec))
            {
                zones.addAutomaton(spec);
            }
            else
            {
                externalSpecs.addAutomaton(spec);
            }
        }
        
        // Filling theAutomata with SHALLOW copies of plants and zones
        theAutomata = new Automata(plants, true);
        theAutomata.addAutomata(zones);
        theAutomata.addAutomata(externalSpecs);
        indexMap = new AutomataIndexMap(theAutomata);
        updateGui(theAutomata);
    }
    
    private void initMutexStates()
        throws Exception
    {
        bookingTics = new int[zones.size()][plants.size()][1];
        unbookingTics = new int[zones.size()][plants.size()][1];
        
        // Initializing all book/unbook-state indices to -1.
        // The ones that remain -1 at the output of this method correspond
        // to non-conflicting plant-zone-pairs.
        for (int i=0; i<zones.size(); i++)
        {
            for (int j=0; j<plants.size(); j++)
            {
                bookingTics[i][j][0] = -1;
                unbookingTics[i][j][0] = -1;
            }
        }
        
        for (int i=0; i<zones.size(); i++)
        {
            Automaton currZone = zones.getAutomatonAt(i);
            
            // 			ArrayList[] bookUnbookStatePairIndices = new ArrayList[plants.size()];
            
            for (int j=0; j<plants.size(); j++)
            {
                Automaton currPlant = plants.getAutomatonAt(j);
                
                // 				Alphabet commonAlphabet = AlphabetHelpers.intersect(currPlant.getAlphabet(), currZone.getAlphabet());
                
                Alphabet bookingAlphabet = AlphabetHelpers.intersect(currPlant.getAlphabet(), currZone.getInitialState().activeEvents(false));
                
                if (bookingAlphabet.size() > 0)
                {
                    Alphabet unbookingAlphabet = AlphabetHelpers.minus(AlphabetHelpers.intersect(currPlant.getAlphabet(), currZone.getAlphabet()), bookingAlphabet);
                    
                    ArrayList<State> bookingStates = new ArrayList<State>();
                    ArrayList<State> unbookingStates = new ArrayList<State>();
                    
                    for (Iterator<State> stateIter = currPlant.stateIterator(); stateIter.hasNext(); )
                    {
                        State currState = stateIter.next();
                        
                        Alphabet currStatesBookingAlphabet = AlphabetHelpers.intersect(currState.activeEvents(false), bookingAlphabet);
                        for (Iterator<LabeledEvent> currBookingEventsIter = currStatesBookingAlphabet.iterator(); currBookingEventsIter.hasNext(); )
                        {
                            ArrayList<State> possibleUnbookingStates = new ArrayList<State>();
                            
                            possibleUnbookingStates.add(currState.nextState(currBookingEventsIter.next()));
                            
                            while (possibleUnbookingStates.size() > 0)
                            {
                                State currPossibleUnbookingState = possibleUnbookingStates.remove(0);
                                
                                for (Iterator<Arc> outgoingArcsIter = currPossibleUnbookingState.outgoingArcsIterator(); outgoingArcsIter.hasNext(); )
                                {
                                    Arc currArc = outgoingArcsIter.next();
                                    
                                    if (unbookingAlphabet.contains(currArc.getEvent()))
                                    {
                                        bookingStates.add(currState);
                                        unbookingStates.add(currPossibleUnbookingState);
                                    }
                                    else
                                    {
                                        possibleUnbookingStates.add(currArc.getToState());
                                    }
                                }
                            }
                        }
                    }
                    
                    if (bookingStates.size() != unbookingStates.size())
                    {
                        String exceptionStr = "The numbers of book/unbook-states do not correspond. Something is wrong....\n";
                        exceptionStr += "nr_book_states[" + i + "][" + j + "] = " + bookingStates.size() + "\n";
                        exceptionStr += "nr_unbook_states[" + i + "][" + j + "] = " + unbookingStates.size() + "\n";
                        
                        throw new Exception(exceptionStr);
                    }
                    
                    bookingTics[i][j] = new int[bookingStates.size()];
                    unbookingTics[i][j] = new int[unbookingStates.size()];
                    for (int k=0; k<bookingStates.size(); k++)
                    {
                        bookingTics[i][j][k] = indexMap.getStateIndex(currPlant, bookingStates.get(k));
                        unbookingTics[i][j][k] = indexMap.getStateIndex(currPlant, unbookingStates.get(k));
                    }
                }
            }
        }
    }
    
    private boolean isMutexZone(Automaton spec)
    {
        if (spec.nbrOfStates() < 3)
        {
            return false;
        }
        
        State initialState = spec.getInitialState();
        if (!initialState.isAccepting())
        {
            return false;
        }
        
        if (spec.nbrOfAcceptingStates() != 1)
        {
            return false;
        }
        
        for (Iterator<State> stateIt = spec.stateIterator(); stateIt.hasNext(); )
        {
            State state = stateIt.next();
            if (!state.equals(initialState))
            {
                for (Iterator<State> nextStateIt = state.nextStateIterator(); nextStateIt.hasNext(); )
                {
                    State nextState = nextStateIt.next();
                    if (!nextState.equals(initialState))
                    {
                        return false;
                    }
                }
            }
        }
        
        return true;
    }
    
    // prec_temp
    private void createExternalConstraints()
    throws Exception
    {
        for (int i=0; i<externalSpecs.size(); i++)
        {
            Automaton currSpec = externalSpecs.getAutomatonAt(i);
            
            if (currSpec.nbrOfStates() == 2)
            {
                if (!currSpec.getInitialState().isAccepting())
                {
                    if (currSpec.getInitialState().nbrOfIncomingArcs() == 0)
                    {
                        createXORConstraints(currSpec);
                        continue;
                    }
                }
                else
                {
                    if (currSpec.getInitialState().nbrOfIncomingArcs() > 0)
                    {
                        if (currSpec.nbrOfAcceptingStates() == 1)
                        {
                            createPrecedenceConstraints(currSpec);
                            continue;
                        }
                    }
                }
            }
            
            throw new Exception("Specification " + currSpec.getName() + " was not recognized by the DES-MILP-converter");
        }
    }
    
    private void createXORConstraints(Automaton currSpec)
        throws Exception
    {
        // Replace some characters that the GLPK-solver would not accept
        String specName = currSpec.getName().trim();
        specName = specName.replace(".", "_");
        specName = specName.replace(" ", "_");
        String specStr = "xor_" + specName + " : ";
        
        State currSpecState = currSpec.getInitialState();
// 		for (Iterator<State> specStateIt = currSpec.stateIterator(); specStateIt.hasNext(); )
// 		{
// 			State currSpecState = specStateIt.next();
// 			if (!currSpecState.isAccepting())
// 			{
        
// 		ArrayList<TreeSet> altPathVariablesSet = new ArrayList<TreeSet>();
        ArrayList<ArrayList> altPathVariablesSet = new ArrayList<ArrayList>();
        
        for (Iterator<Automaton> plantIt = plants.iterator(); plantIt.hasNext(); )
        {
            Automaton currPlant = plantIt.next();
            Alphabet commonActiveAlphabet = AlphabetHelpers.intersect(currPlant.getAlphabet(), currSpecState.activeEvents(false));
            
            if (commonActiveAlphabet.size() > 0)
            {
// 				TreeSet<int[]> altPathVariables = new TreeSet<int[]>(new PathSplitIndexComparator());
                ArrayList<int[]> altPathVariables = new ArrayList<int[]>();
                for (Iterator<LabeledEvent> eventIt = commonActiveAlphabet.iterator(); eventIt.hasNext(); )
                {
                    LabeledEvent currEvent = eventIt.next();
                    for (Iterator <State> plantStateIt = currPlant.stateIterator(); plantStateIt.hasNext(); )
                    {
                        State currPlantState = plantStateIt.next();
                        if (currPlantState.activeEvents(false).contains(currEvent))
                        {
                            // Should start the search from the next state to find the nearest path split
                            findNearestPathSplits(currPlant, currPlantState.nextState(currEvent), altPathVariables, currEvent);
                        }
                    }
                }
                
// 				altPathVariables = replaceFalsePathSplits(currPlant, altPathVariables);
                
                for (Iterator<int[]> it = altPathVariables.iterator(); it.hasNext(); )
                {
                    int[] pathSplitState = it.next();
                    
                    if (pathSplitState[0] != NO_PATH_SPLIT_INDEX)
                    {
                        specStr += makeAltPathsVariable(indexMap.getAutomatonIndex(currPlant), pathSplitState[0], pathSplitState[1]) + " + ";
                    }
                    else
                    {
                        specStr += "1 + ";
                    }
                }
                
                altPathVariablesSet.add(altPathVariables);
            }
        }
        
// 		boolean dominatingPlantFound = false;
// 		for (int j=0; j<altPathVariablesSet.size(); j++)
// 		{
// 			if (altPathVariablesSet.get(j).size() == 0)
// 			{
// 				if (dominatingPlantFound)
// 				{
// 					throw new Exception(currSpec.getName() + " prevents the system from reaching its final state, since at least 2 plants want (but must not) execute a mutually exclussive event.");
// 				}
// 				else
// 				{
// 					dominatingPlantFound = true;
// 				}
// 			}
// 		}
        specStr = specStr.substring(0, specStr.lastIndexOf("+")) + "= 1;";
// 		if (dominatingPlantFound)
// 		{
// 			specStr += "= 0;";
// 		}
// 		else
// 		{
// 			specStr += "= 1;";
// 		}
        
        externalConstraints += specStr + "\n";
        // 	}
// 		}
    }
    
    //TODO: rensa upp här...
    private void createPrecedenceConstraints(Automaton currSpec)
        throws Exception
    {
        // Get the name of the specification and replace some characters that the GLPK-solver would not accept
        String specName = currSpec.getName().trim();
        specName = specName.replace(".", "_");
        specName = specName.replace(" ", "_");
        
        State initialStateInSpec = currSpec.getInitialState();
        ArrayList<int[]> precedingEventsInfo = new ArrayList<int[]>();
        ArrayList<int[]> followingEventsInfo = new ArrayList<int[]>();
        ArrayList<int[]> currEventsInfo = null;
        
        //TODO: used for the alternative precedence
        ArrayList<int[]> precedingPlantStates = new ArrayList<int[]>();
        ArrayList<int[]> followingPlantStates = new ArrayList<int[]>();
        ArrayList<int[]> currPlantStates = null;
        
        // Loops through the plant automata and finds the states in which the preceding/following
        // (starting/finishing) events of this spec are enabled. Also, the infos needed to retrieve 
        // upstreams path-split variables are found. It's all stored in precedingEventsInfo/followingEventsInfo as
        // {[delimiter (-1)] [plantIndex stateIndex] [pathSplit_from_1 pathSplit_to_1] ... [pathSplit_from_n pathSplit_to_n]}.
        for (Iterator<Automaton> plantIt = plants.iterator(); plantIt.hasNext(); )
        {
            Automaton plant = plantIt.next();
            Alphabet commonActiveAlphabet = AlphabetHelpers.intersect(plant.getAlphabet(), currSpec.getAlphabet());
            
            if (commonActiveAlphabet.size() > 0)
            {
                for (Iterator<LabeledEvent> eventIt = commonActiveAlphabet.iterator(); eventIt.hasNext(); )
                {
                    LabeledEvent currEvent = eventIt.next();
                    
                    if (initialStateInSpec.activeEvents(false).contains(currEvent))
                    {
                        currEventsInfo = precedingEventsInfo;
                        //TODO: alternative precedence
                        currPlantStates = precedingPlantStates;
                    }
                    else
                    {
                        currEventsInfo = followingEventsInfo;
                        //TODO: alternative precedence
                        currPlantStates = followingPlantStates;                        
                    }
                    
                    for (Iterator <State> plantStateIt = plant.stateIterator(); plantStateIt.hasNext(); )
                    {
                        State plantState = plantStateIt.next();
                        
                        if (plantState.activeEvents(false).contains(currEvent))
                        {
                            // Add a plant/state-delimiter
                            currEventsInfo.add(new int[]{-1});
                            // Add info about the plant and state indices
                            currEventsInfo.add(new int[]{indexMap.getAutomatonIndex(plant), indexMap.getStateIndex(plant, plantState)});
                            // Find nearest path splits above the current state
                            findNearestPathSplits(plant, plantState.nextState(currEvent), currEventsInfo, currEvent);
                            
                            //TODO: alternative precedence
                            currPlantStates.add(new int[]{indexMap.getAutomatonIndex(plant), indexMap.getStateIndex(plant, plantState)});
                        }
                    }
                }
            }
        }
        
        //TODO: Alternative precedence here...
//        ArrayList<ArrayList> allAlternatingPlantStatesInfo = getEveryAlternatingOrdering(precedingPlantStates, followingPlantStates);
        ArrayList<ArrayList> allPPlantStates = new ArrayList<ArrayList>();
        for (int[] info : precedingEventsInfo)
        {
            if (info.length == 1)
            {
                allPPlantStates.add(new ArrayList<int[]>());
            }
            else
            {
                allPPlantStates.get(allPPlantStates.size()-1).add(info);
            }
        }
        ArrayList<ArrayList> allFPlantStates = new ArrayList<ArrayList>();
        for (int[] info : followingEventsInfo)
        {
            if (info.length == 1)
            {
                allFPlantStates.add(new ArrayList<int[]>());
            }
            else
            {
                allFPlantStates.get(allFPlantStates.size()-1).add(info);
            }
        }
        
        Hashtable<String, String> internalPrecVarsTable = new Hashtable<String, String>();
        for (int i = 0; i < allPPlantStates.size() - 1; i++)
        {
            ArrayList<int[]> firstPlantStateInfo = allPPlantStates.get(i);
            ArrayList<int[]> secondPlantStateInfo = allPPlantStates.get(i+1);
            int[] firstPlantState = firstPlantStateInfo.get(0);
            int[] secondPlantState = secondPlantStateInfo.get(0);
            String internalPrecedenceVar = "r" + firstPlantState[0] + "_st" + firstPlantState[1] + 
                    "_before_r" + secondPlantState[0] + "_st" + secondPlantState[1];
            
            internalPrecVarsTable.put(getKeyFromPlantStates(firstPlantState, secondPlantState), internalPrecedenceVar);
            internalPrecVarDecl += "var " + internalPrecedenceVar + ", binary;\n";
        }
        for (int i = 0; i < allFPlantStates.size() - 1; i++)
        {
            ArrayList<int[]> firstPlantStateInfo = allFPlantStates.get(i);
            ArrayList<int[]> secondPlantStateInfo = allFPlantStates.get(i+1);
            int[] firstPlantState = firstPlantStateInfo.get(0);
            int[] secondPlantState = secondPlantStateInfo.get(0);
            String internalPrecedenceVar = "r" + firstPlantState[0] + "_st" + firstPlantState[1] + 
                    "_before_r" + secondPlantState[0] + "_st" + secondPlantState[1];
            
            internalPrecVarsTable.put(getKeyFromPlantStates(firstPlantState, secondPlantState), internalPrecedenceVar);
            internalPrecVarDecl += "var " + internalPrecedenceVar + ", binary;\n";
        }
        
        //Create the constraint specifying that the number of preceding and succeeding events should be equal
        String equalNumberConstrStr = " = ";
        for (ArrayList<int[]> plantStateInfo : allPPlantStates)
        {
            int[] plantState = plantStateInfo.get(0);
            for (int i = 1; i < plantStateInfo.size(); i++)
            {
                int[] pathSplit = plantStateInfo.get(i);
                if (pathSplit[0] != NO_PATH_SPLIT_INDEX)
                {
                    equalNumberConstrStr = " + " + makeAltPathsVariable(plantState[0], pathSplit[0], pathSplit[1]) + equalNumberConstrStr;
                }
                else
                {
                    equalNumberConstrStr = " + 1" + equalNumberConstrStr;
                }   
            }
        }
        for (ArrayList<int[]> plantStateInfo : allFPlantStates)
        {
            int[] plantState = plantStateInfo.get(0);
            for (int i = 1; i < plantStateInfo.size(); i++)
            {
                int[] pathSplit = plantStateInfo.get(i);
                if (pathSplit[0] != NO_PATH_SPLIT_INDEX)
                {
                    equalNumberConstrStr += makeAltPathsVariable(plantState[0], pathSplit[0], pathSplit[1]) + " + ";
                }
                else
                {
                    equalNumberConstrStr += "1 + ";
                }
            }
        }
        
        //TODO: rename cut(S/E)Index to cut(Start/End)Index
        int cutSIndex = 0;
        int cutEIndex = equalNumberConstrStr.length();
        if (equalNumberConstrStr.startsWith(" +"))
        {
            cutSIndex = equalNumberConstrStr.indexOf("+") + 2;
        }
        if (equalNumberConstrStr.endsWith("+ "))
        {
            cutEIndex = equalNumberConstrStr.lastIndexOf("+");
        }
        equalNumberConstrStr = equalNumberConstrStr.substring(cutSIndex, cutEIndex).trim() + ";\n";
        
        //TODO: rename all(P/F)PlantStates into all(Preceding/Following)PlantStates (i sinom tid)
        ArrayList<ArrayList> allAlternatingPlantStatesInfo = getEveryAlternatingOrdering(allPPlantStates, allFPlantStates);

        int caseCounter = 0;
        externalConstraints += "\n\n";
        for (ArrayList<ArrayList<int[]>> currAlternatingPlantStateInfoArray : allAlternatingPlantStatesInfo)
        {
            externalConstraints += "/* " + specName + ", case " + ++caseCounter + " */\n";            
            int constrCounter = 1;
            
            // Get the correct internal precedence variables for this combination of events
            String internalPrecStr = "";
            int nrDefaultInternalPrec = 0;
            for (int i = 0; i < currAlternatingPlantStateInfoArray.size() - 2; i++)
            {
                int[] firstPlantState = currAlternatingPlantStateInfoArray.get(i).get(0);
                int[] secondPlantState = currAlternatingPlantStateInfoArray.get(i+2).get(0);
                
                if (internalPrecVarsTable.get(getKeyFromPlantStates(firstPlantState, secondPlantState)) != null)
                {
                    nrDefaultInternalPrec++;
                    internalPrecStr += " - " + internalPrecVarsTable.get(getKeyFromPlantStates(firstPlantState, secondPlantState));
                }
                else if (internalPrecVarsTable.get(getKeyFromPlantStates(secondPlantState, firstPlantState)) != null)
                {
                    internalPrecStr += " + " + internalPrecVarsTable.get(getKeyFromPlantStates(secondPlantState, firstPlantState));
                }
                else
                {
                    throw new Exception("Key \"" + internalPrecVarsTable.get(getKeyFromPlantStates(firstPlantState, secondPlantState)) + 
                            "\" not found in the internal_precedence hashtable...");
                }
            }
            
            for (int i = 0; i < currAlternatingPlantStateInfoArray.size() - 1; i++)
            {
                ArrayList<int[]> precedingPlantStateInfo = currAlternatingPlantStateInfoArray.get(i);
                ArrayList<int[]> followingPlantStateInfo = currAlternatingPlantStateInfoArray.get(i+1);
                
                int[] precedingPlantState = precedingPlantStateInfo.get(0);
                int[] followingPlantState = followingPlantStateInfo.get(0);
                
                String precedingAltPathsStr = "";
                String followingAltPathsStr = "";
                int nrPathSplits = 0;
                for (int j = 1; j < precedingPlantStateInfo.size(); j++)
                {
                    int[] pathSplitIndices = precedingPlantStateInfo.get(j);
                    if (pathSplitIndices[0] != NO_PATH_SPLIT_INDEX)
                    {
                        nrPathSplits++;
                        precedingAltPathsStr += " - " + makeAltPathsVariable(precedingPlantState[0], pathSplitIndices[0], pathSplitIndices[1]);
                    }
                }
                for (int j = 1; j < followingPlantStateInfo.size(); j++)
                {
                    int[] pathSplitIndices = followingPlantStateInfo.get(j);                 
                    if (pathSplitIndices[0] != NO_PATH_SPLIT_INDEX)
                    {
                        nrPathSplits++;
                        followingAltPathsStr += " - " + makeAltPathsVariable(followingPlantState[0], pathSplitIndices[0], pathSplitIndices[1]);
                    }
                }                              

                externalConstraints += "NEW_multi_plant_prec_" + specName + "_" + caseCounter + "_" + constrCounter++ + " : time[" + 
                        followingPlantState[0] + ", " + followingPlantState[1] + "] >= time[" + 
                        precedingPlantState[0] + ", " + precedingPlantState[1] + "]";
                if (internalPrecStr != "" || nrPathSplits > 0)
                {
                    externalConstraints += " - bigM*(" + (nrDefaultInternalPrec + nrPathSplits) + 
                            precedingAltPathsStr + followingAltPathsStr + internalPrecStr + ")";
                }
                externalConstraints += " + epsilon;\n";
            }
        }
        externalConstraints += "/* " + specName + ", nr_starting_events == nr_finishing_events */\n";
        externalConstraints += "NEW_multi_plant_prec_" + specName + "_TOT : " + equalNumberConstrStr;
        //...done
        
//TODO: @Deprecated        
//        int[] currFollowingPlantAndState = null;
//        int[] currPrecedingPlantAndState = null;
//        String timeSpecStr = "\n";
//        String logicSpecStr = " = ";
////TODO: @Deprecated        
////        String reversalSumStr = "";
//        int counter = 1;
//        boolean firstLoop = true;
//        for (Iterator<int[]> followingRobotIt = followingEventsInfo.iterator(); followingRobotIt.hasNext(); )
//        {
//            int[] currFollowingIndices = followingRobotIt.next();
//            int nrPathSplitsInFollowingPlant = 0;
//            
//            if (currFollowingIndices.length == 1)
//            {
//                currFollowingPlantAndState = followingRobotIt.next();
//            }
//            else
//            {
//                String followingEventPathSplitVariableStr = "";
//                if (currFollowingIndices[0] != NO_PATH_SPLIT_INDEX)
//                {
//                    nrPathSplitsInFollowingPlant++;
//                    followingEventPathSplitVariableStr = " - " + makeAltPathsVariable(currFollowingPlantAndState[0], currFollowingIndices[0], currFollowingIndices[1]);
//                    logicSpecStr += makeAltPathsVariable(currFollowingPlantAndState[0], currFollowingIndices[0], currFollowingIndices[1]) + " + ";
//                }
//                else
//                {
//                    logicSpecStr += "1 + ";
//                }
//                
//                for (Iterator<int[]> precedingRobotIt = precedingEventsInfo.iterator(); precedingRobotIt.hasNext(); )
//                {
//                    int[] currPrecedingIndices = precedingRobotIt.next();
//                    int nrPathSplitsInPrecedingPlant = 0;
//                    
//                    if (currPrecedingIndices.length == 1)
//                    {
//                        currPrecedingPlantAndState = precedingRobotIt.next();
//                    }
//                    else
//                    {
//                        if (firstLoop)
//                        {
//                            if (currPrecedingIndices[0] != NO_PATH_SPLIT_INDEX)
//                            {
//                                logicSpecStr = " + " + makeAltPathsVariable(currPrecedingPlantAndState[0], currPrecedingIndices[0], currPrecedingIndices[1]) + logicSpecStr;
//                            }
//                            else
//                            {
//                                logicSpecStr = " + 1" + logicSpecStr;
//                            }
//                        }
//                        
//                        String precedingEventPathSplitVariableStr = "";
//                        if (currPrecedingIndices[0] != NO_PATH_SPLIT_INDEX)
//                        {
//                            nrPathSplitsInPrecedingPlant++;
//                            precedingEventPathSplitVariableStr = " - " + makeAltPathsVariable(currPrecedingPlantAndState[0], currPrecedingIndices[0], currPrecedingIndices[1]);
//                        }
//  
////TODO: @Deprecated                        
////                        String reversalVariable = specName + "_reverse_" + counter;
//                        timeSpecStr += "multi_plant_prec_" + specName + "_" + counter++ + " : time[" + 
//                                currFollowingPlantAndState[0] + ", " + currFollowingPlantAndState[1] + "] >= time[" + 
//                                currPrecedingPlantAndState[0] + ", " + currPrecedingPlantAndState[1] + "]";
//                        if (nrPathSplitsInFollowingPlant + nrPathSplitsInPrecedingPlant > 0)
//                        {
//                            timeSpecStr += " - bigM*(" + (nrPathSplitsInFollowingPlant + nrPathSplitsInPrecedingPlant) + 
//                                    precedingEventPathSplitVariableStr + followingEventPathSplitVariableStr + ")";
//                        }
//                        timeSpecStr += " + epsilon;\n";
//
////TODO: @Deprecated                        
////                        timeSpecStr += "dual_multi_plant_prec_" + specName + "_" + counter++ + " : time[" + 
////                                currPrecedingPlantAndState[0] + ", " + currPrecedingPlantAndState[1] + "] >= time[" +
////                                currFollowingPlantAndState[0] + ", " + currFollowingPlantAndState[1] + "]"; 
////                        if (nrPathSplitsInFollowingPlant + nrPathSplitsInPrecedingPlant > 0)
////                        {
////                            timeSpecStr += " - bigM*(" + (nrPathSplitsInFollowingPlant + nrPathSplitsInPrecedingPlant + 1) + 
////                                    " - " + reversalVariable + " - " + precedingEventPathSplitVariableStr + 
////                                    " - " + followingEventPathSplitVariableStr + ")";
////                        }
////                        timeSpecStr += " + epsilon;\n";                       
////                        precReversalVarDecl += "var " + reversalVariable + ", binary;\n";                       
////                        timeSpecStr += "limit_" + reversalVariable + " : (" + precedingEventPathSplitVariableStr + " + " + 
////                                followingEventPathSplitVariableStr + ") / 2 >= " + reversalVariable + ";\n"; 
////                        reversalSumStr += reversalVariable + " + ";
//                    }
//                }
//                firstLoop = false;
//            }
//        }
//        
//        int cutStartIndex = 0;
//        if (logicSpecStr.startsWith(" +"))
//        {
//            cutStartIndex = logicSpecStr.indexOf("+") + 2;
//        }
//        int cutEndIndex = logicSpecStr.length();
//        if (logicSpecStr.endsWith("+ "))
//        {
//            cutEndIndex = logicSpecStr.lastIndexOf("+");
//        }
//        logicSpecStr = logicSpecStr.substring(cutStartIndex, cutEndIndex).trim() + ";\n";
//        externalConstraints += timeSpecStr + "multi_plant_prec_" + specName + "_TOT : " + logicSpecStr;
        
//TODO: @Deprecated        
//        precReversalVarDecl += "var " +  
//        externalConstraints += "multi_plant_prec_" + specName + "_reversal_TOT : " + reversalSumStr + " = " + 
    }
    
// 	private StateSet findCommonEventFiringStatesInPlant(Automaton plant, Automaton spec)
// 	{
// 		StateSet firingStates = new StateSet();
    
// 		Alphabet commonActiveAlphabet = AlphabetHelpers.intersect(currPlant.getAlphabet(), currSpec.getInitialState().activeEvents(false));
    
// 		if (commonActiveAlphabet.size() > 0)
// 		{
// 			for (Iterator<LabeledEvent> eventIt = commonActiveAlphabet.iterator(); eventIt.hasNext(); )
// 			{
// 				LabeledEvent currEvent = eventIt.next();
// 				for (Iterator <State> plantStateIt = plant.stateIterator(); plantStateIt.hasNext(); )
// 				{
// 					State currPlantState = plantStateIt.next();
    // 					if (currPlantState.activeEvents(false).contains(currEvent))
// 					{
// 						firingStates.add(currPlantState);
// 					}
// 				}
// 			}
// 		}
    
// 		return firingStates;
// 	}
    
    /**
     *  Calls @findNearestPathSplits(auto, state, altPathsVariables, null), thus
     *  initiating search for the path splits on all transitions leading to state.
     *
     *  @param - auto, the automaton in which path splits may appear.
     *  @parem - state, the state above which path splits are searched for.
     *  @param - altPathVariables, the  @ArrayList containing the indices of 
     *           path split state pairs. The search is always ended by adding 
     *           NO_PATH_SPLIT_INDEX to the list.
     */
    private void findNearestPathSplits(Automaton auto, State state, ArrayList<int[]> altPathsVariables)
    {
        findNearestPathSplits(auto, state, altPathsVariables, null);
    }
    
    /**
     *  Finds all pairs of states {startState, endState} following the supplied 
     *  event upwards from the supplied state, such that there is a 
     *  path split in the supplied automaton at startState, leading in one 
     *  transition to endState. If the event is null, all upstreams path-split 
     *  states are found. Their inidices, as provided by @AutomataIndexMap, are
     *  stored in the supplied ArrayList.
     *
     *  @param - auto, the automaton in which path splits may appear.
     *  @parem - state, the state above which path splits are searched for.
     *  @param - altPathVariables, the  @ArrayList containing the indices of 
     *           path split state pairs. The search is always ended by adding 
     *           NO_PATH_SPLIT_INDEX to the list.
     *  @param - event, the event above (and including) which the search for 
     *           path splits is started. If null, all transition leading to the 
     *           state are considered. 
     */
    private void findNearestPathSplits(Automaton auto, State state, ArrayList<int[]> altPathsVariables, LabeledEvent event)
    {
        if (state.nbrOfIncomingArcs() == 0)
        { 
            // Indicates that there is no path split leading to the current state
            altPathsVariables.add(new int[]{NO_PATH_SPLIT_INDEX, NO_PATH_SPLIT_INDEX});
        }
        else
        {
            for (Iterator<Arc> incomingArcsIt = state.incomingArcsIterator(); incomingArcsIt.hasNext(); )
            {
                Arc currArc = incomingArcsIt.next();
                               
                // Normally, this method is called to find all split states ABOVE a certain event.
                // Thus, if the event is non-null, the other transitions leading to event.getTarget() 
                // should not be considered.
                if (event == null || currArc.getEvent().equals(event))
                {
                    State upstreamsState = currArc.getFromState();
                    if (upstreamsState.nbrOfOutgoingMultiArcs() == 1)
                    {   
                        // If one step has been taken upstreams from the branching event, all path splits 
                        // above become interesting. Thus, if no path splits are found immediately,
                        // the method is looped with event equal to null. 
                        findNearestPathSplits(auto, upstreamsState, altPathsVariables);
                    }
                    else
                    { 
                        // If a path split is found, the indices for the states "touched" by the splitting event,
                        // i.e. {splitEvent.getSource(), splitEvent.getTarget()} are stored in the supplied list.
                        altPathsVariables.add(new int[]{indexMap.getStateIndex(auto, upstreamsState), indexMap.getStateIndex(auto, state)});
                    }
                }
            }
        }
    }
    
// 	private TreeSet<int[]> replaceFalsePathSplits(Automaton auto, TreeSet<int[]> altPathVariables)
// 	{
// 		boolean falseAltPathFound = false;
// 		TreeSet<int[]> newAltPathVariables = new TreeSet<int[]>(new PathSplitIndexComparator());
// 		SortedSet<int[]> tail = altPathVariables.tailSet(new int[]{0, 0, -1});
    
// 		while (!tail.isEmpty())
// 		{
// 			int startStateIndex = tail.first()[0];
// 			int[] separatingElement = new int[]{startStateIndex + 1, 0, -1};
// 			SortedSet<int[]> head = tail.headSet(separatingElement);
// 			tail = tail.tailSet(separatingElement);
// 			State startState = indexMap.getStateAt(auto, startStateIndex);
// 			if (startState.nbrOfOutgoingMultiArcs() != head.size())
// 			{
// 				newAltPathVariables.addAll(head);
// 			}
// 			else
// 			{
// 				falseAltPathFound = true;
// 				findNearestPathSplits(auto, startState, newAltPathVariables);
// 			}
// 		}
    
// 		if (falseAltPathFound)
// 		{
// 			return replaceFalsePathSplits(auto, newAltPathVariables);
// 		}
    
// 		return newAltPathVariables;
// 	}
    
    
    /****************************************************************************************/
    /*                                 THE AUTOMATA-MILP-BRIDGE-METHODS                     */
    /****************************************************************************************/
    
    /**
     * Converts the automata to the MILP-formulation of the optimization problem.
     * Precedence, Mutual Exclusion, Cycle Time and Alternative Path constraints are
     * constructed.
     */
    private void convertAutomataToMilp()
    throws Exception
    {
        timer.restart();
        
        int nrOfPlants = plants.size();
        int nrOfZones = zones.size();
        
        // The string containing precedence constraints
        String precConstraints = "";
        
        // The string containing initial (precedence) constraints
        String initPrecConstraints = "";
        
        // The string containing mutex constraints
        String mutexConstraints = "";
        
        // The string containing mutex variables
        String mutexVariables = "";
        
        // The string containg alternative paths constraints
        String altPathsConstraints = "";
        
        // The string containing alternative paths variables
        String altPathsVarDecl = "";
        
        // The string containing the cycle time constraints
        String cycleTimeConstraints = "";
        
        // The string containing times for each state (delta-times)
        String deltaTimeStr = "param deltaTime default 0\n:";
        
        
        ////////////////////////////////////////////////////////////////////////////////////
        //	                          The constructing part                               //
        ////////////////////////////////////////////////////////////////////////////////////
        
        // prec_temp
        createExternalConstraints();
        
        // Finding maximum number of time variables per plant (i.e. max nr of states)
        int nrOfTics = 0;
        for (int i=0; i<nrOfPlants; i++)
        {
            int nbrOfStates = plants.getAutomatonAt(i).nbrOfStates();
            if (nbrOfStates > nrOfTics)
                nrOfTics = nbrOfStates;
        }
        
        // Making deltaTime-header
        for (int i=0; i<nrOfTics; i++)
            deltaTimeStr += "\t\t" + i;
        
        deltaTimeStr += " :=\n";
        
        // Extracting deltaTimes for each plant
        for (int i=0; i<nrOfPlants; i++)
        {
            deltaTimeStr += i;
            
            Automaton currPlant = plants.getAutomatonAt(i);
            int currPlantIndex = indexMap.getAutomatonIndex(currPlant);
            
            // Each index correspond to a Tic. For each Tic, a deltaTime is added
            double[] deltaTimes = new double[currPlant.nbrOfStates()];
            for (Iterator<State> stateIter = currPlant.stateIterator(); stateIter.hasNext(); )
            {
                State currState = stateIter.next();
                
                int currStateIndex = indexMap.getStateIndex(currPlant, currState);
                
                deltaTimes[currStateIndex] = currState.getCost();
                
                // If the current state has successors and is not initial, add precedence constraints
                // If the current state is initial, add an initial (precedence) constraint
                int nbrOfOutgoingMultiArcs = currState.nbrOfOutgoingMultiArcs();
                if (nbrOfOutgoingMultiArcs > 0)
                {
                    if (currState.isInitial())
                    {
                        initPrecConstraints += "initial_" + "r" + currPlantIndex + "_" + currStateIndex + " : ";
                        initPrecConstraints += "time[" + i + ", " + currStateIndex + "] >= deltaTime[" + i + ", " + currStateIndex + "];\n";
                    }
                    
                    Iterator<State> nextStates = currState.nextStateIterator();
                    
                    // If there is only one successor, add a precedence constraint
                    if (nbrOfOutgoingMultiArcs == 1)
                    {
                        State nextState = nextStates.next();
                        int nextStateIndex = indexMap.getStateIndex(currPlant, nextState);
                        
                        //TODO: fixa epsilon så det blir rätt tid i buildScheduleAutomaton()
                        precConstraints += "prec_" + "r" + currPlantIndex + "_" + currStateIndex + "_" + nextStateIndex + " : " + 
                                "time[" + i + ", " + nextStateIndex + "] >= time[" + i + ", " + currStateIndex + "] + deltaTime[" + i + 
                                ", " + nextStateIndex + "] + epsilon;\n";
                    }
//                     // If there are two successors, add one alternative-path variable and corresponding constraint
//                     else if (nbrOfOutgoingMultiArcs == 2)
//                     {
//                         State nextLeftState = nextStates.next();
//                         State nextRightState = nextStates.next();
//                         int nextLeftStateIndex = indexMap.getStateIndex(currPlant, nextLeftState);
//                         int nextRightStateIndex = indexMap.getStateIndex(currPlant, nextRightState);
                    
//                         String currAltPathsVariable = "r" + currPlantIndex + "_from_" + currStateIndex + "_to_" + nextLeftStateIndex;
                    
//                         altPathsVarDecl += "var " + currAltPathsVariable + ", binary;\n";
                    
//                         altPathsConstraints += "alt_paths_" + "r" + currPlantIndex + "_" + currStateIndex + " : ";
//                         altPathsConstraints += "time[" + i + ", " + nextLeftStateIndex + "] >= time[" + i + ", " + currStateIndex + "] + deltaTime[" + i + ", "  + nextLeftStateIndex + "] - bigM*(1 - " + currAltPathsVariable + ");\n";
                    
//                         pathCutTable.put(nextLeftState, currAltPathsVariable);
                    
//                         altPathsConstraints += "dual_alt_paths_" + "r" + currPlantIndex + "_" + currStateIndex + " : ";
//                         altPathsConstraints += "time[" + i + ", " + nextRightStateIndex + "] >= time[" + i + ", " + currStateIndex + "] + deltaTime[" + i + ", "  + nextRightStateIndex + "] - bigM*" + currAltPathsVariable + ";\n";
                    
//                         pathCutTable.put(nextRightState, "(1 - " + currAltPathsVariable + ")");
//                     }
                    // If there are several successors, add one alternative-path variable for each successor
                    else
                    {
                        int currAlternative = 0;
                        String sumConstraint = "alt_paths_" + "r" + currPlantIndex + "_" + currStateIndex + "_TOT : ";
                        
                        while (nextStates.hasNext())
                        {
                            State nextState = nextStates.next();
                            int nextStateIndex = indexMap.getStateIndex(currPlant, nextState);
                            
                            String currAltPathsVariable = makeAltPathsVariable(currPlantIndex, currStateIndex, nextStateIndex);
                            sumConstraint += currAltPathsVariable + " + ";
                            
                            altPathsVarDecl += "var " + currAltPathsVariable + ", binary;\n";
                            
                            //TODO: hantera epsilon i buildSheduleAutomaton()
                            altPathsConstraints += "alt_paths_" + currAltPathsVariable + " : " + 
                                    "time[" + i + ", " + nextStateIndex + "] >= time[" + i + ", " + currStateIndex + 
                                    "] + deltaTime[" + i + ", "  + nextStateIndex + "] - bigM*(1 - " + 
                                    currAltPathsVariable + ") + epsilon;\n";
                            
                            pathCutTable.put(nextState, "(1 - " + currAltPathsVariable + ")");
                            
                            currAlternative++;
                        }
                        
                        altPathsConstraints += sumConstraint.substring(0, sumConstraint.lastIndexOf("+")) + "= ";
                        
// 						TreeSet<int[]> nearestPathSplits = new TreeSet<int[]>(new PathSplitIndexComparator());
                        ArrayList<int[]> nearestPathSplits = new ArrayList<int[]>();
                        findNearestPathSplits(currPlant, currState, nearestPathSplits);
                        // if (nearestPathSplits.isEmpty())
// 						{
// 							altPathsConstraints += "1;\n";
// 						}
// 						else
// 						{
                        for (Iterator<int[]> splitIt = nearestPathSplits.iterator(); splitIt.hasNext(); )
                        {
                            int[] currPathSplit = splitIt.next();
                            
                            if (currPathSplit[0] != NO_PATH_SPLIT_INDEX)
                            {
                                altPathsConstraints += makeAltPathsVariable(currPlantIndex, currPathSplit[0], currPathSplit[1]) + " + ";
                            }
                            else
                            {
                                altPathsConstraints += "1 + ";
                            }
                        }
                        altPathsConstraints = altPathsConstraints.substring(0, altPathsConstraints.lastIndexOf(" +")) + ";\n";
// 						}
// 						altPathsConstraints += sumConstraint.substring(0, sumConstraint.lastIndexOf("+")) + "= 1;\n";
                        
                    }
                }
                else if (currState.isAccepting())
                {
                    // If the current state is accepting, a cycle time constaint is added,
                    // ensuring that the makespan is at least as long as the minimum cycle time of this plant
                    cycleTimeConstraints += "cycle_time_" + "r" + currPlantIndex + " : c >= " + "time[" + i + ", " + currStateIndex + "];\n";
                }
            }
            
            
            deltaTimeStr += "\t\t" + deltaTimes[0];
            for (int j=1; j<deltaTimes.length; j++)
            {
                String currDeltaTimeStr = "" + deltaTimes[j-1];
                if (currDeltaTimeStr.length() > 5)
                {
                    deltaTimeStr += "\t" + deltaTimes[j];
                }
                else
                {
                    deltaTimeStr += "\t\t" + deltaTimes[j];
                }
            }
            
            // If the number of states of the current automaton is less
            // than max_nr_of_states, the deltaTime-matrix is filled with points
            // i.e zeros.
            for (int j=currPlant.nbrOfStates(); j<nrOfTics; j++)
            {
                deltaTimeStr += "\t\t.";
            }
            
            if (i == nrOfPlants - 1)
                deltaTimeStr += ";";
            
            deltaTimeStr += "\n";
        }
        
        // Constructing the mutex constraints
        // for every zone...
        
        for (int i=0; i<bookingTics.length; i++)
        {
            // for every plant pair...
            for (int j1=0; j1<bookingTics[i].length-1; j1++)
            {
                for (int j2=j1+1; j2<bookingTics[i].length; j2++)
                {
                    // updates the variable index if the booking event is repeated;
                    int repeatedBooking = 0;
                    
                    // for every path combination that contains the event that books the current zone
                    for (int k1=0; k1<bookingTics[i][j1].length; k1++)
                    {
                        for (int k2=0; k2<bookingTics[i][j2].length; k2++)
                        {
                            if (bookingTics[i][j1][0] != -1 && bookingTics[i][j2][0] != -1)
                            {
                                repeatedBooking++;
                                
                                String currMutexVariable = "r" + j1 + "_books_z" + i + "_before_r" + j2 + "_var" + repeatedBooking;
                                
                                mutexVariables += "var " + currMutexVariable + ", binary;\n";
                                
                                //test
                                ArrayList<int[]> bList = new ArrayList<int[]>();
                                Automaton bPlant = indexMap.getAutomatonAt(j1);
                                State bState = indexMap.getStateAt(bPlant, bookingTics[i][j1][k1]);
                                findNearestPathSplits(bPlant, bState, bList);
                                ArrayList<int[]> uList = new ArrayList<int[]>();
                                Automaton uPlant = indexMap.getAutomatonAt(j2);
                                State uState = indexMap.getStateAt(uPlant, unbookingTics[i][j2][k2]);
                                findNearestPathSplits(uPlant, uState, uList);
                                int totalNrOfPathSplits = 0;
                                String altPathsCoupling = "";
                                for (int[] bSplitState : bList)
                                {
                                    if (bSplitState[0] != NO_PATH_SPLIT_INDEX)
                                    {
                                        totalNrOfPathSplits++;
                                        altPathsCoupling += " - " + makeAltPathsVariable(j1, bSplitState[0], bSplitState[1]);
                                    }                       
                                }
                                for (int[] uSplitState : uList)
                                {
                                    if (uSplitState[0] != NO_PATH_SPLIT_INDEX)
                                    {
                                        totalNrOfPathSplits++;
                                        altPathsCoupling += " - " + makeAltPathsVariable(j2, uSplitState[0], uSplitState[1]);
                                    }                       
                                }
                                mutexConstraints += "mutex_z" + i + "_r" + j1 + "_r" + j2 + "_var" + repeatedBooking + " : time[" + j1 + ", " + bookingTics[i][j1][k1] + "] >= " + "time[" + j2 + ", " + unbookingTics[i][j2][k2] + "] - bigM*";
                                if (totalNrOfPathSplits > 0)
                                {
                                    mutexConstraints += "(" + totalNrOfPathSplits + " + " + currMutexVariable + altPathsCoupling + ")";
                                }     
                                else
                                {
                                    mutexConstraints += currMutexVariable;
                                }
                                mutexConstraints += " + epsilon;\n";    
                                //test (forts...)
                                bList = new ArrayList<int[]>();
                                bPlant = uPlant;
                                bState = indexMap.getStateAt(bPlant, bookingTics[i][j2][k2]);
                                findNearestPathSplits(bPlant, bState, bList);
                                uList = new ArrayList<int[]>();
                                uPlant = indexMap.getAutomatonAt(j1);
                                uState = indexMap.getStateAt(uPlant, unbookingTics[i][j1][k1]);
                                findNearestPathSplits(uPlant, uState, uList);
                                totalNrOfPathSplits = 0;
                                altPathsCoupling = "";
                                for (int[] bSplitState : bList)
                                {
                                    if (bSplitState[0] != NO_PATH_SPLIT_INDEX)
                                    {
                                        totalNrOfPathSplits++;
                                        altPathsCoupling += " - " + makeAltPathsVariable(j2, bSplitState[0], bSplitState[1]);
                                    }                       
                                }
                                for (int[] uSplitState : uList)
                                {
                                    if (uSplitState[0] != NO_PATH_SPLIT_INDEX)
                                    {
                                        totalNrOfPathSplits++;
                                        altPathsCoupling += " - " + makeAltPathsVariable(j1, uSplitState[0], uSplitState[1]);
                                    }                       
                                }
                                mutexConstraints += "dual_mutex_z" + i + "_r" + j1 + "_r" + j2  + "_var" + repeatedBooking + " : time[" + j2 + ", " + bookingTics[i][j2][k2] + "] >= " + "time[" + j1 + ", " + unbookingTics[i][j1][k1] + "] - bigM*(";
                                if (totalNrOfPathSplits > 0)
                                {
                                    mutexConstraints += (totalNrOfPathSplits+1) + " - " + currMutexVariable + altPathsCoupling + ")";
                                }     
                                else
                                {
                                    mutexConstraints += "1 - " + currMutexVariable + ")";
                                }   
                                mutexConstraints += " + epsilon;\n";

// Replace by the test above
//                                String pathCutEnsurance = pathCutTable.get(indexMap.getStateAt(plants.getAutomatonAt(j1), k1));
//                                if (pathCutEnsurance != null)
//                                {
//                                    mutexConstraints += " - bigM*" + pathCutEnsurance;
//                                }
//                                mutexConstraints += ";\n";
//                                mutexConstraints += "dual_mutex_z" + i + "_r" + j1 + "_r" + j2  + "_var" + repeatedBooking + " : time[" + j2 + ", " + bookingTics[i][j2][k2] + "] >= " + "time[" + j1 + ", " + unbookingTics[i][j1][k1] + "] - bigM*(1 - " + currMutexVariable + ")" + " + " + epsilon;
//                                pathCutEnsurance = pathCutTable.get(indexMap.getStateAt(plants.getAutomatonAt(j2), k2));
//                                if (pathCutEnsurance != null)
//                                {
//                                    mutexConstraints += " - bigM*" + pathCutEnsurance;
//                                }
//                                mutexConstraints += ";\n";
                            }
                        }
                    }
                }
            }
        }
        
        ////////////////////////////////////////////////////////////////////////////////////
        //	                          The writing part                                    //
        ////////////////////////////////////////////////////////////////////////////////////
        
        BufferedWriter w = new BufferedWriter(new FileWriter(modelFile));
        
        // Definitions of parameters
        w.write("param nrOfPlants >= 0;");
        w.newLine();
        w.write("param nrOfZones >= 0;");
        w.newLine();
        w.write("param maxTic >= 0;");
        w.newLine();
        w.write("param bigM;");
        w.newLine();
        w.write("param epsilon >= 0;");
        w.newLine();
        
        // Definitions of sets
        w.newLine();
        w.write("set Plants := 0..nrOfPlants;");
        w.newLine();
        w.write("set Zones := 0..nrOfZones;");
        w.newLine();
        w.write("set Tics := 0..maxTic;");
        w.newLine();
        
        // Definitions of parameters, using sets as their input (must be in this order to avoid GLPK-complaints)
        w.newLine();
        w.write("param deltaTime{r in Plants, t in Tics};");
        w.newLine();
        
        // Definitions of variables
        w.newLine();
        w.write("var time{r in Plants, t in Tics};"); // >= 0;");
        w.newLine();
        w.write("var c;");
        w.newLine();
        w.write(altPathsVarDecl);
        w.write(mutexVariables);
        w.write(internalPrecVarDecl);
//TODO: @Deprecated        
//        w.write(precReversalVarDecl);
        w.newLine();
        
        // The objective function
        w.newLine();
        w.write("minimize makespan: c;");
        w.newLine();
        
        // The constraints section
        w.newLine();
        w.write("subject to");
        w.newLine();
        
        // The cycle time constraints
        w.newLine();
        w.write(cycleTimeConstraints);
        
        // 		w.write("cycle_time{r in Plants}: c >= time[r, maxTic];");
        // 		w.newLine();
        
        // The initial (precedence) constraints
        w.newLine();
        w.write(initPrecConstraints);
        
        // The precedence constraints
        w.newLine();
        w.write(precConstraints);
        
        // The alternative paths constraints
        w.newLine();
        w.write(altPathsConstraints);
        
        // The mutex constraints
        w.newLine();
        w.write(mutexConstraints);
        
        // The constraints due to external specifications
        w.write(externalConstraints);
        w.newLine();
        
        // The end of the model-section and the beginning of the data-section
        w.newLine();
        w.write("data;");
        w.newLine();
        
        // The numbers of plants resp. zones are given
        w.newLine();
        w.write("param nrOfPlants := " + (nrOfPlants - 1) + ";");
        w.newLine();
        w.write("param nrOfZones := " + (nrOfZones - 1) + ";");
        w.newLine();
        w.write("param bigM := " + BIG_M_VALUE + ";");
        w.newLine();
        // Behovs maxTic verkligen???
        w.write("param maxTic := " + (nrOfTics - 1) + ";");
        w.newLine();
        w.write("param epsilon := " + EPSILON + ";");
        
        w.newLine();
        w.write(deltaTimeStr);
        w.newLine();
        
        w.newLine();
        w.write("end;");
        w.flush();
        
        String str = "Time to set up the optimization problem: " + timer.elapsedTime() + "ms";
        logger.info(str);
        outputStr += "\t" + str + "\n";
    }
    
    private void processSolutionFile()
        throws Exception
    {
        // tillf...
        // 		for (int i=0; i<plants.size(); i++)
        // 		{
        // 			logger.warn("Plant = " + plants.getAutomatonAt(i).getName() + "; index = " + indexMap.getAutomatonIndex(plants.getAutomatonAt(i)));
        // 			for (Iterator<State> stir = plants.getAutomatonAt(i).stateIterator(); stir.hasNext(); )
        // 			{
        // 				State s = stir.next();
        // 				logger.info("State = " + s.getName() + "; index = " + indexMap.getStateIndex(plants.getAutomatonAt(i), s));
        // 			}
        // 		}
        //...
        
        optimalTimes = new double[plants.size()][];
        for (int i=0; i<optimalTimes.length; i++)
        {
            optimalTimes[i] = new double[plants.getAutomatonAt(i).nbrOfStates()];
        }
        
        pathChoices = new boolean[plants.size()][][];
        for (int i=0; i<pathChoices.length; i++)
        {
            pathChoices[i] = new boolean[indexMap.getAutomatonAt(i).nbrOfStates()][indexMap.getAutomatonAt(i).nbrOfStates()];
        }
        
        BufferedReader r = new BufferedReader(new FileReader(solutionFile));
        String str = r.readLine();
        
        // Go through the solution file and extract the suggested optimal times for each state
        while (str != null)
        {
            if (str.indexOf(" time[") > -1)
            {
                String strPlantIndex = str.substring(str.indexOf("[") + 1, str.indexOf(",")).trim();
                String strStateIndex = str.substring(str.indexOf(",") + 1, str.indexOf("]")).trim();
                String strCost = str.substring(str.indexOf("]") + 1).trim();
                
                int plantIndex = (new Integer(strPlantIndex)).intValue();
                int stateIndex = (new Integer(strStateIndex)).intValue();
                double cost = (new Double(strCost)).doubleValue();
                
                optimalTimes[plantIndex][stateIndex] = cost;
            }
            else if (str.indexOf("c ") >  -1)
            {
                String strMakespan = str.substring(str.indexOf("c") + 1).trim();
                makespan = removeEpsilons((new Double(strMakespan)).doubleValue());
            }
            else if (str.indexOf(" prec_") > -1)
            {
                str = str.substring(str.indexOf("_r") + 2);
                String strPlantIndex = str.substring(0, str.indexOf("_"));
                String strStartStateIndex = str.substring(str.indexOf("_") + 1, str.lastIndexOf("_"));
                String strEndStateIndex = str.substring(str.lastIndexOf("_") + 1);
                if (strEndStateIndex.indexOf(" ") > -1)
                {
                    strEndStateIndex = strEndStateIndex.substring(0, strEndStateIndex.indexOf(" "));
                }
                
                int plantIndex = (new Integer(strPlantIndex)).intValue();
                int startStateIndex = (new Integer(strStartStateIndex)).intValue();
                int endStateIndex = (new Integer(strEndStateIndex)).intValue();
                
                pathChoices[plantIndex][startStateIndex][endStateIndex] = true;
            }
            else if (str.indexOf("_from") > -1 && str.indexOf("alt_paths") < 0)
            {
                String strPlantIndex = str.substring(str.indexOf("r") + 1, str.indexOf("_"));
                str = str.substring(str.indexOf("_from_") + 6);
                String strStartStateIndex = str.substring(0, str.indexOf("_"));
                String strEndStateIndex = str.substring(str.lastIndexOf("_") + 1);
                if (strEndStateIndex.indexOf(" ") > -1)
                {
                    strEndStateIndex = strEndStateIndex.substring(0, strEndStateIndex.indexOf(" "));
                }
                
                int plantIndex = (new Integer(strPlantIndex)).intValue();
                int startStateIndex = (new Integer(strStartStateIndex)).intValue();
                int endStateIndex = (new Integer(strEndStateIndex)).intValue();
                
                if (str.indexOf(" 1") < 0)
                {
                    str = r.readLine();
                }
                
                if (str.indexOf(" 0") == str.lastIndexOf(" 0"))
                {
                    pathChoices[plantIndex][startStateIndex][endStateIndex] = true;
                }
            }
            
            str = r.readLine();
        }
        
        str = "OPTIMAL MAKESPAN: " + makespan + ".............................";
        logger.info(str);
        outputStr += "\t" + str + "\n";
    }
    
    private void callMilpSolver()
    throws Exception
    {
        logger.info("The MILP-solver started....");
        
        // Defines the name of the .exe-file as well the arguments (.mod and .sol file names)
        String[] cmds = new String[5];
        //cmds[0] = "C:\\Program Files\\glpk\\bin\\glpsol.exe";
        cmds[0] = "glpsol";
        cmds[1] = "-m";
        cmds[2] = modelFile.getAbsolutePath();
        cmds[3] = "-o";
        cmds[4] = solutionFile.getAbsolutePath();
        
        try
        {
            // Runs the MILP-solver with the arguments defined above
            milpProcess = Runtime.getRuntime().exec(cmds);
        }
        catch (IOException milpNotFoundException)
        {
            logger.error("The GLPK-solver 'glpsol.exe' not found. Make sure that it is registered in your path.");
            
            throw milpNotFoundException;
        }
        
        // Listens for the output of MILP (that is the input to this application)...
        BufferedReader milpEcho = new BufferedReader(new InputStreamReader(new DataInputStream(milpProcess.getInputStream())));
        
        // ...and prints it to stdout
        String milpEchoStr = "";
        String totalMilpEchoStr = "";
        while ((milpEchoStr = milpEcho.readLine()) != null)
        {
            totalMilpEchoStr += milpEchoStr + "\n";
            
//             if (milpEchoStr.contains("INTEGER OPTIMAL SOLUTION FOUND") || milpEchoStr.contains("Time") || milpEchoStr.contains("Memory"))
//             {
            
//                 // 				logger.info(milpEchoStr);
            
//                 // 				if (!milpEchoStr.contains("INTEGER OPTIMAL SOLUTION FOUND"))
//                 // 				{
//                 // 					outputStr += "\t" + milpEchoStr + "\n";
//                 // 				}
//             }
            if (milpEchoStr.contains("NO FEASIBLE SOLUTION") || milpEchoStr.contains("NO PRIMAL FEASIBLE SOLUTION"))
            {
                throw new Exception(milpEchoStr + " (specifications should be relaxed if possible).");
            }
            else if (milpEchoStr.contains("error"))
            {
                throw new Exception(totalMilpEchoStr);
            }
        }
    }
    
    public void requestStop()
    {
        requestStop(false);
    }
    
    public void requestStop(boolean disposeScheduleDialog)
    {
        isRunning = false;
        
        if (milpProcess != null)
        {
            milpProcess.destroy();
        }
        
        if (scheduleDialog != null && disposeScheduleDialog)
        {
            scheduleDialog.done();
        }
    }
    
    public boolean isStopped()
    {
        return !isRunning;
    }
    
    private State makeScheduleState(int[] stateIndices, boolean isInitial)
    {
        String stateName = "[";
        
        for (int i=0; i<theAutomata.size() - 1; i++)
        {
            stateName += indexMap.getStateAt(indexMap.getAutomatonAt(i), stateIndices[i]).getName() + ".";
        }
        stateName += indexMap.getStateAt(indexMap.getAutomatonAt(theAutomata.size()-1), stateIndices[theAutomata.size()-1]).getName() + "]";
        
        State scheduledState = new State(stateName);
        
        scheduledState.setInitial(isInitial);
        schedule.addState(scheduledState);
        
        return scheduledState;
    }
    
    private State makeScheduleState(int[] stateIndices)
    {
        return makeScheduleState(stateIndices, false);
    }
    
    public String getOutputString()
    {
        return outputStr;
    }
    
    /**
     *  This method prepares a plant for the MILP optimization algorithm.
     *  It checks whether the plant has an initial state (
     *  prior to the call to this method, the plant is composed with
     *  its specifications, i.e. specifications that only consist of events in
     *  this plant. If the specifications are too restrictive, an initial
     *  state may not exist). Next, if the initial state of the plant is also
     *  accepting, a dummy accepting state is added to the plant in order to
     *  allow the optimization algorithm to start. If there is a self-loop
     *  in the initial state (which should only occur after one run of the
     *  optimization algorithm), it is removed (otherwise MILP cannot function).
     *
     *  @param - The plant to be prepared for MILP-optimization.
     */
    private void prepareAutomatonForMilp(Automaton currPlant)
        throws Exception
    {
        State currInitialState = currPlant.getInitialState();
        
        // If there is no initial state, throw exception
        if (currInitialState == null)
        {
            int plantNameRootIndex = currPlant.getName().indexOf("_constr");
            String plantName;
            if (plantNameRootIndex < 0)
            {
                plantName = currPlant.getName();
            }
            else
            {
                plantName = currPlant.getName().substring(0, plantNameRootIndex);
            }
            throw new Exception(plantName + " has no initial state, possibly due to the restrictions imposed by its specifications. The system has thus no (optimal) path.");
        }
        
        // Remove the self-loops in the initial state. Such self-loops are sometimes created during
        // schedule construction to run a schedule repeatedly.
        ArrayList<Arc> arcsToBeRemoved = new ArrayList<Arc>();
        boolean selfLoopDetected = false;
        for (Iterator<Arc> arcIt = currInitialState.outgoingArcsIterator(); arcIt.hasNext();)
        {
            Arc arc = arcIt.next();
            if (arc.isSelfLoop())
            {
                arcsToBeRemoved.add(arc);
                selfLoopDetected = true;
            }
        }
        for (Arc arc : arcsToBeRemoved)
        {
            currPlant.removeArc(arc);
        }
        if (selfLoopDetected)
        {
            currPlant.remapStateIndices();
        }
             
        // Add a dummy accepting state if the initial state is accepting
        if (currInitialState.isAccepting())
        {
            currInitialState.setAccepting(false);
            
            State dummyState = new State("dummy_" + currInitialState.getName());
            currPlant.addState(dummyState);
            
            for (Iterator<Arc> incomingArcIt = currInitialState.incomingArcsIterator(); incomingArcIt.hasNext(); )
            {
                Arc currArc = incomingArcIt.next();
                
                currPlant.addArc(new Arc(currArc.getFromState(), dummyState, currArc.getEvent()));
            }
            
            currInitialState.removeIncomingArcs();
            
            dummyState.setAccepting(true);
            dummyState.setCost(0);
            
            currPlant.remapStateIndices();
        }
    }
    
    private synchronized void addAutomatonToGui(Automaton auto)
    throws Exception
    {
        if (scheduleDialog != null)
        {
            Gui theGui = null;
            
            try
            {
                theGui = ActionMan.getGui();
                theGui.addAutomaton(auto);
            }
            catch (Exception ex)
            {
                logger.warn("EXceptiON, gui = " + theGui);
                
                throw ex;
            }
        }
    }
    
    private synchronized void updateGui(Automata autos)
    throws Exception
    {
        if (scheduleDialog != null)
        {
            Gui theGui = null;
            
            try
            {
                theGui = ActionMan.getGui();
                
                // Remove old automata
                Automata selectedAutos = theGui.getSelectedAutomata();
                for (Iterator<Automaton> autIt = selectedAutos.iterator(); autIt.hasNext(); )
                {
                    Automaton selectedAuto = autIt.next();
                    if (!autos.containsAutomaton(selectedAuto.getName()))
                    {
                        theGui.getVisualProjectContainer().getActiveProject().removeAutomaton(selectedAuto);
                    }
                }
                
                // Add missing automata
                for (Iterator<Automaton> autIt = autos.iterator(); autIt.hasNext(); )
                {
                    Automaton auto = autIt.next();
                    if (!selectedAutos.containsAutomaton(auto.getName()))
                    {
                        theGui.addAutomaton(auto);
                    }
                }
            }
            catch (Exception ex)
            {
                logger.warn("EXceptiON, gui = " + theGui);
                
                throw ex;
            }
        }
    }
    
    /**
     * Returns an automaton describing the optimised schedule.
     */
    public Automaton getSchedule()
    {
        return schedule;
    }

    public int getBIG_M_VALUE()
    {
        return BIG_M_VALUE;
    }
    
    private String makeAltPathsVariable(int plantIndex, int fromStateIndex, int toStateIndex)
    {
        return "r" + plantIndex + "_from_" + fromStateIndex + "_to_" + toStateIndex;
    }
    
    /**
     *  Removes epsilons from the supplied time variable, by returning closest
     *  value that is smaller than time and cannot be affected by the sum of 
     *  epsilons.
     *
     *  @param - time 
     *  @return - the time without epsilons
     */
    private double removeEpsilons(double time)
    {
        // Initialize roundOffCoeff if this has not been done
        if (roundOffCoeff == -1)
        {
            int totalNrOfTimes = 0;
            for (int i = 0; i < optimalTimes.length; i++)
            {
                totalNrOfTimes += optimalTimes[i].length;
            }
            roundOffCoeff = EPSILON * Math.pow(10, ("" + totalNrOfTimes).length());
        }
        
        //Remove epsilons from the current time
        return Math.floor(time / roundOffCoeff) * roundOffCoeff;
    }
    
    private void getEveryOrdering(ArrayList<ArrayList> toOrder, 
            ArrayList<ArrayList> currOrder, ArrayList<ArrayList> allOrderings)
    {      
        if (toOrder.size() > 1)
        {
            for (Iterator<ArrayList> it = toOrder.iterator(); it.hasNext();)
            {
                ArrayList elem = it.next();
                ArrayList<ArrayList> newToOrder = new ArrayList<ArrayList>(toOrder);
                ArrayList<ArrayList> newCurrOrder = new ArrayList<ArrayList>(currOrder);
                newToOrder.remove(elem);
                newCurrOrder.add(elem);
                getEveryOrdering(newToOrder, newCurrOrder, allOrderings);
            }
        }
        else
        {
            currOrder.add(toOrder.get(0));
            allOrderings.add(currOrder);
        }       
    }
    
    private ArrayList<ArrayList> getEveryAlternatingOrdering(ArrayList<ArrayList> toOrder1, ArrayList<ArrayList> toOrder2)
    {
        ArrayList<ArrayList> allOrderings1 = new ArrayList<ArrayList>();
        ArrayList<ArrayList> allOrderings2 = new ArrayList<ArrayList>();
        getEveryOrdering(toOrder1, new ArrayList<ArrayList>(), allOrderings1);
        getEveryOrdering(toOrder2, new ArrayList<ArrayList>(), allOrderings2);
        
        ArrayList<ArrayList> allCombinedOrderings = new ArrayList<ArrayList>();       
        for (ArrayList<ArrayList> i1 : allOrderings1)
        {
            for (ArrayList<ArrayList> i2 : allOrderings2)
            {
                ArrayList<ArrayList> combinedOrdering = new ArrayList<ArrayList>();
                for (int i = 0; i < i1.size(); i++)
                {
                    combinedOrdering.add(i1.get(i));
                    combinedOrdering.add(i2.get(i));
                }
                allCombinedOrderings.add(combinedOrdering);
            }
        }
        
        return allCombinedOrderings;
//        
//        for (ArrayList<int[]> al : allCombinedOrderings)
//        {
//            String str = "";
//            for (int[] i : al)
//            {
//                str += i.intValue() + " ";
//            }
//            logger.warn("str...... = " + str);
//        }
    }
    
    private String getKeyFromPlantStates(int[] firstPlantState, int[] secondPlantState)
    {
        String key = "";
        for (int j = 0; j < firstPlantState.length; j++)
        {
            key += firstPlantState[j] + ",";
        }
        for (int j = 0; j < secondPlantState.length; j++)
        {
            key += secondPlantState[j] + ",";
        }

        return key;
    }
}

// class PathSplitIndexComparator
// 	implements Comparator<int[]>
// {
// 	public int compare(int[] o1, int[] o2)
// 	{
// 		int indexLength = Math.min(o1.length, o2.length);

// 		for (int i=0; i<indexLength; i++)
// 		{
// 			if (o1[i] != o2[i])
// 			{
// 				return o1[i] - o2[i];
// 			}
// 		}

// 		return 0;
// 	}
// }
