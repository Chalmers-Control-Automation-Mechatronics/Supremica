package org.supremica.automata.algorithms.scheduling;

import java.util.*;
import java.io.*;
import org.jacorb.orb.domain.TEST_POLICY_ID;

import org.supremica.automata.*;
import org.supremica.automata.algorithms.*;
import org.supremica.gui.ScheduleDialog;
import org.supremica.gui.ide.IDEReportInterface;
import org.supremica.gui.ide.actions.IDEAction;
import org.supremica.gui.ide.actions.IDEActionInterface;
import org.supremica.log.*;
import org.supremica.util.ActionTimer;

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
    private static final int BIG_M_VALUE = 1000;
    
    /** The involved automata, plants, zone specifications */
    private Automata theAutomata, plants, zones, externalSpecs;
       
    /** The project used for return result */
    // NOT USED YET...
    // private Project theProject;
    
    /** 
     * int[zone_nr][plant_nr][event/state-switch][state_nr] - stores the indices 
     * of the booking/unbooking events and the states that fire them. 
     */
    private int[][][][] bookingTics, unbookingTics;
   
    /** The indices of the event- and state-arrays in bookingTics/unbookingTics */
    private final static int STATE_SWITCH = 0;
    private final static int EVENT_SWITCH = 1;
    
    /** 
     * Mapping between plant_ + zone_ + bookingTics_indices and a mutex-variable-counter.
     * The keys consist of int[]'s of the form {zoneIndex, firstPlantIndex, secondPlantIndex, 
     * bookingTics[zoneIndex][firstPlantIndex], bookingTics[zoneIndex][secondPlantIndex]}. 
     * The stored value is the corresponding variable counter. 
     */
    private TreeMap<int[], Integer> mutexVarCounterMap = 
            new TreeMap<int[], Integer>(new IntArrayComparator());
    
    /** The optimal cycle time (makespan) */
    private double makespan;
    
    /** The *.mod file that serves as an input to the Glpk-solver */
    protected File modelFile;
    
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
    protected volatile boolean isRunning = false;
    
    /** The dialog box that launched this scheduler */
    protected ScheduleDialog scheduleDialog;
    
    /** Decides if the schedule should be built */
    protected boolean buildSchedule;
    
    /** The output string */
    private String outputStr = "";
    
    /** The constrainst represented by external specifications, in string form. */
    private String externalConstraints = "";
    
    /**
     *  The declaration of variables, used in internal ordering of precedence constraints.
     **/
    private String internalPrecVarDecl = "";
    
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
    
    /** 
     * This value is higher than 1 if the total time risk exceeding the bigM-value.
     * In that case, each statetime-value is divised by timeThroughBigMApprox. 
     */
    private double timeThroughBigMApprox = 1;
    
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
//        ActionTimer totalTimer = new ActionTimer();
//        ActionTimer partTimer = new ActionTimer();
        
        long totalTime = 0;
        
        if (isRunning)
        {
//            totalTimer.restart();
//            partTimer.restart();
            timer.restart();
            initialize();
        }
        
        // Converts automata to constraints that the MILP-solver takes as input (*.mod file)
        if (isRunning)
        {
            //TODO:temp
//            int anr = scheduleDialog.getIde().getIDE().getActiveProject().getAutomata().size();
            
            convertAutomataToMilp();
            
            long procTime = timer.elapsedTime();
            logger.info("Pre-processing time = " + procTime + "ms");
            totalTime += procTime;
        }
        
        //new... test...
        if (isRunning)
        {
            //temp-bortkommat
            createNonCrossbookingConstraintsNew();
        }
        
        // Calls the MILP-solver
        if (isRunning)
        {
            timer.restart();
            
            callMilpSolver();
            
            long procTime = timer.elapsedTime();
            logger.info("Optimization time = " + procTime + "ms");
            totalTime += procTime;
        }
        
        // Processes the output from the MILP-solver (*.sol file) and stores the optimal times for each state
        if (isRunning)
        {
            timer.restart();
            
            processSolutionFile();
        }
        
        // Builds the optimal schedule (if solicited)
        if (isRunning && buildSchedule)
        {
            buildScheduleAutomaton();
            
            long procTime = timer.elapsedTime();
            logger.info("Post-processing time (incl. schedule construction) = " + procTime + "ms");
            totalTime += procTime;
            logger.info("Total time = " + totalTime + "ms");
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
/*
        //TODO: temp (fulhack) - fixa bï¿½ttre schemabygge.
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
*/            
        // Create the automaton with a chosen name
        timer.stop(); // Stop the timer while waiting for the user to enter the name of the new schedule
        String scheduleName = "";
        while (scheduleName != null && scheduleName.trim() == "")
        {
            scheduleName = scheduleDialog.getIde().getActiveDocumentContainer().getAnalyzerPanel().getNewAutomatonName("Enter a name for the schedule", "Schedule");
        }       
        if (scheduleName == null)
        {
            return;
        }
        schedule = new Automaton(scheduleName);
        timer.start(); // Restart the timer
        
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
                            if (stepper.isEnabled(currEvent))
                            // temp (fulhack)
//                            if (synthState.nextState(currEvent) != null)
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
//            synthState = synthState.nextState(currOptimalEvent);

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
            plantAuto.getAlphabet().addEvent(resetEvent);
            plantAuto.addArc(new Arc(plantAuto.getInitialState(), plantAuto.getInitialState(), resetEvent));
        }
         
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
    protected void initialize()
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
        
        // Rescale the times in plants if necessary
//        rescalePlantTimes();
                
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
    
    /** 
     * Rescales the times in plants if their total sum is close to the bigM-value.
     */
    private void rescalePlantTimes()
    {
        double maxCost = 0;
        int totNrStates = 0;
        
        for (Iterator<Automaton> autIt = plants.iterator(); autIt.hasNext();)
        {
            Automaton auto = autIt.next();
            
            for (Iterator<State> stateIt = auto.stateIterator(); stateIt.hasNext();)
            {
                State state = stateIt.next();
                totNrStates++;
                
                if (state.getCost() > maxCost)
                {
                    maxCost = state.getCost();
                }
            }
        }
        timeThroughBigMApprox = (maxCost * totNrStates) / BIG_M_VALUE;
//        timeThroughBigMApprox = Math.ceil((maxCost * totNrStates) / BIG_M_VALUE);
        if (timeThroughBigMApprox > 1) // Rescale the times if their total maximal sum is higher than bigM
        {
            for (Iterator<Automaton> autIt = plants.iterator(); autIt.hasNext();)
            {
                Automaton auto = autIt.next();

                for (Iterator<State> stateIt = auto.stateIterator(); stateIt.hasNext();)
                {
                    State state = stateIt.next();
                    state.setCost(state.getCost() / timeThroughBigMApprox);
                }
            }
        }
        else // Otherwise, reset the timeThroughBigMApprox-variable.
        {
            timeThroughBigMApprox = 1;
        }
    }
    
    private void initMutexStates()
        throws Exception
    {
        bookingTics = new int[zones.size()][plants.size()][2][1];
        unbookingTics = new int[zones.size()][plants.size()][2][1];
        
        // Initializing all book/unbook-state indices to -1.
        // The ones that remain -1 at the output of this method correspond
        // to non-conflicting plant-zone-pairs.
        for (int i=0; i<zones.size(); i++)
        {
            for (int j=0; j<plants.size(); j++)
            {
                bookingTics[i][j][STATE_SWITCH][0] = -1;
                unbookingTics[i][j][STATE_SWITCH][0] = -1;
                bookingTics[i][j][EVENT_SWITCH][0] = -1;
                unbookingTics[i][j][EVENT_SWITCH][0] = -1;
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
                    ArrayList<LabeledEvent> bookingEvents = new ArrayList<LabeledEvent>();
                    ArrayList<LabeledEvent> unbookingEvents = new ArrayList<LabeledEvent>();
                    
                    for (Iterator<State> stateIter = currPlant.stateIterator(); stateIter.hasNext(); )
                    {
                        State currState = stateIter.next();
                        
                        Alphabet currStatesBookingAlphabet = AlphabetHelpers.intersect(currState.activeEvents(false), bookingAlphabet);
                        for (Iterator<LabeledEvent> currBookingEventsIter = currStatesBookingAlphabet.iterator(); currBookingEventsIter.hasNext(); )
                        {
                            ArrayList<State> possibleUnbookingStates = new ArrayList<State>();
                            LabeledEvent currBookingEvent = currBookingEventsIter.next();
                            
                            possibleUnbookingStates.add(currState.nextState(currBookingEvent));
                            
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
                                        bookingEvents.add(currBookingEvent);
                                        unbookingEvents.add(currArc.getEvent());
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
                    
                    bookingTics[i][j][STATE_SWITCH] = new int[bookingStates.size()];
                    unbookingTics[i][j][STATE_SWITCH] = new int[unbookingStates.size()];
                    bookingTics[i][j][EVENT_SWITCH] = new int[bookingStates.size()];
                    unbookingTics[i][j][EVENT_SWITCH] = new int[unbookingStates.size()];
                    for (int k=0; k<bookingStates.size(); k++)
                    {
                        bookingTics[i][j][STATE_SWITCH][k] = indexMap.getStateIndex(currPlant, bookingStates.get(k));
                        unbookingTics[i][j][STATE_SWITCH][k] = indexMap.getStateIndex(currPlant, unbookingStates.get(k));
                        bookingTics[i][j][EVENT_SWITCH][k] = indexMap.getEventIndex(bookingEvents.get(k));
                        unbookingTics[i][j][EVENT_SWITCH][k] = indexMap.getEventIndex(unbookingEvents.get(k));
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
// 				TreeSet<int[]> altPathVariables = new TreeSet<int[]>(new PathSplipIndexComparator());
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
//        ArrayList<int[]> precedingPlantStates = new ArrayList<int[]>();
//        ArrayList<int[]> followingPlantStates = new ArrayList<int[]>();
//        ArrayList<int[]> currPlantStates = null;
        
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
                //test
                for (Iterator<State> stateIt = currSpec.stateIterator(); stateIt.hasNext();)
                {
                    State specState = stateIt.next();
                    if (specState.isInitial())
                    {
                        currEventsInfo = precedingEventsInfo;
                        //TODO: alternative precedence
//                        currPlantStates = precedingPlantStates;
                    }
                    else
                    {
                        currEventsInfo = followingEventsInfo;
                        //TODO: alternative precedence
//                        currPlantStates = followingPlantStates;                        
                    }
                    
                    for (Iterator<LabeledEvent> eventIt = specState.activeEvents(false).iterator(); eventIt.hasNext();)
                    {
                        LabeledEvent currEvent = eventIt.next();
                        if (commonActiveAlphabet.contains(currEvent))
                        {
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
//                                    currPlantStates.add(new int[]{indexMap.getAutomatonIndex(plant), indexMap.getStateIndex(plant, plantState)});
                                }
                            }
                        }
                    }
                }
            }
        }
         
        // Partition eventsInfo into allPlantStates. Every super-array of allPlantStates
        // contains info about one plantState and its altPaths
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
        
        // The nr of internal variables is the sum of all possible pairs of the variables
        int nrInternalPrecVars = (allPPlantStates.size() * (allPPlantStates.size() - 1) + 
                allFPlantStates.size() * (allFPlantStates.size() - 1)) / 2;
        // Hashtable for the internal precedence variables. Its size is increased 
        // somewhat to avoid its enlargement later on
        Hashtable<IntKey, InternalPrecVariable> internalPrecVarsTable = 
                new Hashtable<IntKey, InternalPrecVariable>((int)Math.ceil(1.5 * nrInternalPrecVars));    
        // Construct every possible internal precedence variable and put it into 
        // the hashtable, together with the corresponding id
        int idCounter = 0;
        for (int i = 0; i < allPPlantStates.size() - 1; i++)
        {
            ArrayList<int[]> firstPlantStateInfo = allPPlantStates.get(i);
            for (int j = i+1; j < allPPlantStates.size(); j++)
            {
                ArrayList<int[]> secondPlantStateInfo = allPPlantStates.get(j);
                int[] firstPlantState = firstPlantStateInfo.get(0);
                int[] secondPlantState = secondPlantStateInfo.get(0);
                String internalPrecedenceVar = "r" + firstPlantState[0] + "_st" + firstPlantState[1] + 
                        "_before_r" + secondPlantState[0] + "_st" + secondPlantState[1];

                internalPrecVarsTable.put(new IntKey(firstPlantState, secondPlantState), 
                        new InternalPrecVariable(internalPrecedenceVar, idCounter++));
                internalPrecVarDecl += "var " + internalPrecedenceVar + ", binary;\n"; 
            }
        }
        for (int i = 0; i < allFPlantStates.size() - 1; i++)
        {
            ArrayList<int[]> firstPlantStateInfo = allFPlantStates.get(i);
            for (int j = i+1; j < allFPlantStates.size(); j++)
            {
                ArrayList<int[]> secondPlantStateInfo = allFPlantStates.get(j);
                int[] firstPlantState = firstPlantStateInfo.get(0);
                int[] secondPlantState = secondPlantStateInfo.get(0);
                String internalPrecedenceVar = "r" + firstPlantState[0] + "_st" + firstPlantState[1] + 
                        "_before_r" + secondPlantState[0] + "_st" + secondPlantState[1];

                internalPrecVarsTable.put(new IntKey(firstPlantState, secondPlantState), 
                        new InternalPrecVariable(internalPrecedenceVar, idCounter++));
                internalPrecVarDecl += "var " + internalPrecedenceVar + ", binary;\n";   
            }
        }
        
        // Get every possible (precedence-)event sequence (of maximal length)
        ArrayList<ArrayList> allAlternatingPlantStatesInfo = getEveryAlternatingOrdering(allPPlantStates, allFPlantStates);

        // Kepps track of the internal precedence variable values that are used 
        // (the others will be forbidden by additional MILP-constraints)
        BooleanCombinationTreeSet usedVariableCombinations = new BooleanCombinationTreeSet();
        
        int caseCounter = 0;
        externalConstraints += "\n\n";
        for (ArrayList<ArrayList<int[]>> currAlternatingPlantStateInfoArray : allAlternatingPlantStatesInfo)
        {            
            String equalNumberPConstrStr = "";
            String equalNumberFConstrStr = "";
            
            externalConstraints += "/* " + specName + ", case " + ++caseCounter + " */\n";            
            int constrCounter = 1;
            
            // Create the object representing curring combination of variable values.
            // -1 if the default internal prec.variable value, otherwise the 
            // variable value that makes this case possible (0 or 1) will be stored 
            // in each element of currVariableCombination (index = variable.getId()).
            int[] currVariableCombination = new int[internalPrecVarsTable.keySet().size()];
            for (int i = 0; i < currVariableCombination.length; i++)
            {
                currVariableCombination[i] = -1;
            }
            
            // Get the correct internal precedence variables for this combination of events
            String internalPrecStr = "";
            int nrDefaultInternalPrec = 0;
            ArrayList<int[]> checkedPPlantStates = new ArrayList<int[]>();
            ArrayList<int[]> checkedFPlantStates = new ArrayList<int[]>();
            ArrayList<int[]> currCheckedPlantStates = null;
            ArrayList<ArrayList> currAllPlantStates = null;
            for (int i = 0; i < currAlternatingPlantStateInfoArray.size(); i++)
            {
                // Check if the current event is "preceding" or "following"
                boolean isPreceding = true;
                if (Math.IEEEremainder(i, 2) == 0)
                {
                    currCheckedPlantStates = checkedPPlantStates;
                    currAllPlantStates = allPPlantStates;
                }
                else
                {
                    currCheckedPlantStates = checkedFPlantStates;
                    currAllPlantStates = allFPlantStates;
                    isPreceding = false;
                }
                
                ArrayList<int[]> currPlantStateInfo = currAlternatingPlantStateInfoArray.get(i);
                int[] currPlantState = currPlantStateInfo.get(0);
                
                // This constructs "equalNumberConstr" making sure that the numbers 
                // of preceding and following events that actually occur are equal.
                for (int j = 1; j < currPlantStateInfo.size(); j++)
                {
                    int[] pathSplit = currPlantStateInfo.get(j);
                    if (pathSplit[0] != NO_PATH_SPLIT_INDEX)
                    {
                        if (isPreceding)
                        {
                            equalNumberPConstrStr += makeAltPathsVariable(currPlantState[0], pathSplit[0], pathSplit[1]) + " + ";
                        }
                        else
                        {
                            equalNumberFConstrStr += makeAltPathsVariable(currPlantState[0], pathSplit[0], pathSplit[1]) + " + ";
                        }
                    }
                    else
                    {
                        if (isPreceding)
                        {
                            equalNumberPConstrStr += "1 + ";
                        }
                        else
                        {
                            equalNumberFConstrStr += "1 + ";
                        }
                    }   
                }
                
                // Create the case variable (combination of internal precedence variables)
                // that accepts or negates this case. Also update the info about 
                // currently used variable values (currVariableCombination).
                currCheckedPlantStates.add(currPlantState);
                for (int j = 0; j < currAllPlantStates.size(); j++)
                {
                    int[] followingPlantState = (int[]) currAllPlantStates.get(j).get(0);
                    if (!currCheckedPlantStates.contains(followingPlantState))
                    {
                        if (internalPrecVarsTable.get(new IntKey(currPlantState, followingPlantState)) != null)
                        {
                            // If currInternalPrecVar should be true for this case to hold, then 
                            // "(1 - currInternalPrecVar)" should be added to the case variable, 
                            // thus nrDefaultInternalPrec is incremented.
                            nrDefaultInternalPrec++;
                            InternalPrecVariable currInternalPrecVar = internalPrecVarsTable.get(new IntKey(currPlantState, followingPlantState));
                            internalPrecStr += " - " + currInternalPrecVar.getName();
                            currVariableCombination[currInternalPrecVar.getIndex()] = 1;
                        }
                        else if (internalPrecVarsTable.get(new IntKey(followingPlantState, currPlantState)) != null)
                        {
                            InternalPrecVariable currInternalPrecVar = internalPrecVarsTable.get(new IntKey(followingPlantState, currPlantState));
                            internalPrecStr += " + " + currInternalPrecVar.getName();
                            currVariableCombination[currInternalPrecVar.getIndex()] = 0;
                        }
                        else
                        {
                            throw new Exception("MILP Exception --> No key found (in the internal_precedence hashtable) for \"" + 
                                    currPlantState[0] + "_" + currPlantState[1] + ", " + followingPlantState[0] + "_" + followingPlantState[1] + "\"...");
                        }
                    }
                }     
            }
            
            // Remember this combination of internal precedence variable values
            usedVariableCombinations.add(currVariableCombination);
            
            // Remove the trailing "+" in the constraint strings
            int cutIndex = equalNumberPConstrStr.length();
            if (equalNumberPConstrStr.endsWith("+ "))
            {
                cutIndex = equalNumberPConstrStr.lastIndexOf("+");
            }
            equalNumberPConstrStr = equalNumberPConstrStr.substring(0, cutIndex).trim();
            cutIndex = equalNumberFConstrStr.length();
            if (equalNumberFConstrStr.endsWith("+ "))
            {
                cutIndex = equalNumberFConstrStr.lastIndexOf("+");
            }
            equalNumberFConstrStr = equalNumberFConstrStr.substring(0, cutIndex).trim();

            // Add the case variable, if it exists, to the constraint string
            String tailStr = ";\n";
            if (internalPrecStr != "")
            {
                tailStr = " - bigM*(" + nrDefaultInternalPrec + internalPrecStr + ")" + tailStr;
            }   
            
            // nr_starting_events == nr_finishing_events 
            // (to devalidate this constraint if this case is not chosen, the equality is implemented 
            // with help of "... >= ... - M*f_case" and "... <= ... - M*f_case"
            externalConstraints += "multi_plant_prec_" + specName + "_TOT_" + caseCounter + " : " + equalNumberPConstrStr +
                    " >= " + equalNumberFConstrStr + tailStr;
            externalConstraints += "multi_plant_prec_" + specName + "_TOT_dual" + caseCounter + " : " + equalNumberFConstrStr +
                    " >= " + equalNumberPConstrStr + tailStr;            
                       
            // Constructs several types of constraints. Firstly, there are precedence 
            // constraints, making sure that each plant-state comes after every plant-state 
            // that has lower index in the current variable ordering (precedingAltPathsStr/followingAltPathsStr). 
            // Sedondly the  "start_before_finish"- and "finish_before_start"-logic 
            // is constructed, preventing 2 consecutive start-events or finish-events.
            for (int fIndex = 1; fIndex < currAlternatingPlantStateInfoArray.size(); fIndex++)
            {
                ArrayList<int[]> followingPlantStateInfo = currAlternatingPlantStateInfoArray.get(fIndex);
                int[] followingPlantState = followingPlantStateInfo.get(0);
                
                // Checks whether the current event belongs to the finish-events
                boolean fIsFinishEvent = true;
                if (Math.IEEEremainder(fIndex, 2) == 0)
                {
                    fIsFinishEvent = false;
                }
                
                String startFinishLogicStrLeft = "";
                String startFinishLogicStrRight = "";
                if (fIsFinishEvent)
                {
                    startFinishLogicStrLeft = "start_before_finish";
                }
                else
                {
                    startFinishLogicStrLeft = "finish_before_start";
                }
                // As well caseCounter as constrCounter are used to give this constraint a unique id
                startFinishLogicStrLeft += "_r" + followingPlantState[0] + "_st" + followingPlantState[1] + "_" + caseCounter + "_" +  constrCounter + " : ";              
                
                // Adds corresponding path split variables that negate the constraint 
                // if the involved "following" event never happen
                int nrFPathSplits = 0;
                String followingAltPathsStr = "";
                for (int j = 1; j < followingPlantStateInfo.size(); j++)
                {
                    int[] pathSplitIndices = followingPlantStateInfo.get(j);                 
                    if (pathSplitIndices[0] != NO_PATH_SPLIT_INDEX)
                    {
                        nrFPathSplits++;
                        String altPathsVar = makeAltPathsVariable(followingPlantState[0], pathSplitIndices[0], pathSplitIndices[1]);
                        followingAltPathsStr += " - " + altPathsVar;
                        startFinishLogicStrRight += altPathsVar + " + ";
                    }
                    else
                    {
                        startFinishLogicStrRight += "1" + " + ";
                    }
                }       
                
                // Every event prior to the current (fIndex) event in the current 
                // variable order is treated
                for (int pIndex = 0; pIndex < fIndex; pIndex++)
                {
                    ArrayList<int[]> precedingPlantStateInfo = currAlternatingPlantStateInfoArray.get(pIndex);         
                    int[] precedingPlantState = precedingPlantStateInfo.get(0);
                    
                    // Correct sign is chosen depending on whether the preceding 
                    // event is starting or finishing
                    boolean pIsFinishEvent = true;
                    if (Math.IEEEremainder(pIndex, 2) == 0)
                    {
                        pIsFinishEvent = false;
                    }
                    
                    if (fIsFinishEvent)
                    {
                        if (pIndex != 0)
                        {
                            if (pIsFinishEvent)
                            {
                                startFinishLogicStrLeft += " - ";
                            }
                            else
                            {
                                startFinishLogicStrLeft += " + ";
                            }
                        }
                    }
                    else
                    {
                        if (pIsFinishEvent)
                        {
                            startFinishLogicStrLeft += " + ";
                        }
                        else
                        {
                            startFinishLogicStrLeft += " - ";
                        }
                    }
 
                    // Adds corresponding path split variables that negate the constraint 
                    // if the involved "preceding" event never happen
                    String precedingAltPathsStr = "";
                    int nrPPathSplits = 0;
                    for (int j = 1; j < precedingPlantStateInfo.size(); j++)
                    {
                        int[] pathSplitIndices = precedingPlantStateInfo.get(j);
                        if (pathSplitIndices[0] != NO_PATH_SPLIT_INDEX)
                        {
                            nrPPathSplits++;
                            String altPathsVar = makeAltPathsVariable(precedingPlantState[0], pathSplitIndices[0], pathSplitIndices[1]);
                            precedingAltPathsStr += " - " + altPathsVar;
                            startFinishLogicStrLeft += altPathsVar;
                        }
                        else
                        {
                            startFinishLogicStrLeft += "1";
                        }
                    }                       

                    // Finally a constraint per treated plant-state-pair is added to the MILP-formulation
                    externalConstraints += "multi_plant_prec_" + specName + "_" + caseCounter + "_" + constrCounter++ + " : time[" + 
                            followingPlantState[0] + ", " + followingPlantState[1] + "] >= time[" + 
                            precedingPlantState[0] + ", " + precedingPlantState[1] + "]";
                    // If there exist variables that possibly negate this case, they are added to the constraint
                    if (internalPrecStr != "" || (nrPPathSplits + nrFPathSplits) > 0)
                    {
                        externalConstraints += " - bigM*(" + (nrDefaultInternalPrec + nrPPathSplits + nrFPathSplits) + 
                                precedingAltPathsStr + followingAltPathsStr + internalPrecStr + ")";
                    }
                    externalConstraints += " + epsilon;\n";
                }
                
                // Cuts the last "+"-term
                if (startFinishLogicStrRight.contains("+"))
                {
                    startFinishLogicStrRight = startFinishLogicStrRight.substring(0, startFinishLogicStrRight.lastIndexOf("+")).trim();
                }
                // Adds -1 if this is a start-event
                if (!fIsFinishEvent)
                {
                    startFinishLogicStrRight += " - 1";
                }
                // "sigma_finish always preceeded by a sigma_start" is added
                externalConstraints += startFinishLogicStrLeft + " >= " + startFinishLogicStrRight;
                if (internalPrecStr != "" || nrDefaultInternalPrec > 0)
                {
                    externalConstraints += " - bigM*(" + nrDefaultInternalPrec + internalPrecStr + ")";
                }
                externalConstraints += ";\n";          
            }

            // For each event that does not belong to the current variable ordering,
            // a constraint, "0 >= alt_paths - M*(x -  f_case) is added. This makes
            // sure that the paths leading to the corresponding plant-state is never 
            // reached if this variable ordering (case) is chosen.
            ArrayList<ArrayList> allPlantStates = new ArrayList<ArrayList>(allPPlantStates);
            allPlantStates.addAll(allFPlantStates);
            for (ArrayList<int[]> unusedPlantStateInfo : allPlantStates)
            {
                if (!isPlantStateInArray(currAlternatingPlantStateInfoArray, unusedPlantStateInfo)) //fulhack denna isPlantStateInArray
                {
                    String unusedPlantStateConstr = "0 >= ";

                    for (int i = 1; i < unusedPlantStateInfo.size(); i++)
                    {
                        if (unusedPlantStateInfo.get(i)[0] != NO_PATH_SPLIT_INDEX)
                        {
                            unusedPlantStateConstr += makeAltPathsVariable(unusedPlantStateInfo.get(0)[0], 
                            unusedPlantStateInfo.get(i)[0], unusedPlantStateInfo.get(i)[1]) + " + ";
                        }
                        else
                        {
                            unusedPlantStateConstr += "1 + ";
                        }
                    }
                    
                    if (unusedPlantStateConstr.contains("+"))
                    {
                        unusedPlantStateConstr = unusedPlantStateConstr.substring(0, unusedPlantStateConstr.lastIndexOf("+")).trim();
                    }                
                    
                    externalConstraints += "multi_plant_prec_" + specName + "_" + caseCounter + "_" + constrCounter++ + " : " + 
                                unusedPlantStateConstr;
                    if (internalPrecStr != "") 
                    {
                        externalConstraints += " - bigM*(" + nrDefaultInternalPrec + internalPrecStr + ");\n";
                    }
                }
            }
        }        
        
        // A constraint per unused combination of internal precedence variable 
        // values is added. The constraint looks as "0 >= 1 -  M*f_unused_case.
        // Thus, an unused variable combination can never occur, meaning that
        // one case (one variable ordering) is always chosen (if a solution exists). 
        externalConstraints += "/* Unused combinations should never occur...*/\n";
        ArrayList<int[]> allBooleanCombinations = getAllBooleanVarCombinations(nrInternalPrecVars, null);
        for (int i = 0; i<allBooleanCombinations.size(); i++)
        {
            int[] currBoolCombination = allBooleanCombinations.get(i);
            
            if (!usedVariableCombinations.contains(currBoolCombination))
            {
                externalConstraints += "unused_internal_prec_combination_" + i + " : 0 >= 1";
            
                String unusedCombinationConstraint = "";
                int nrActiveInternalVars = 0;
                for (InternalPrecVariable internalVar : internalPrecVarsTable.values())
                {
                    if (currBoolCombination[internalVar.getIndex()] == 1)
                    {
                        unusedCombinationConstraint += " - ";
                        nrActiveInternalVars++;
                    }
                    else
                    {
                        unusedCombinationConstraint += " + ";
                    }
                    unusedCombinationConstraint += internalVar.getName();
                }

                externalConstraints += " - bigM*(" + nrActiveInternalVars + unusedCombinationConstraint + ");\n";
            }  
        }
        
        // Adding 101/010-constraints that make sure that certain combinations of 
        // internal precedece variables, such as c<a<b<c and c>a>b>c are disregarded 
        // by the MILP-solver. Nice thought, but may be unnecessary now that 
        // unusedCombinationConstraints are added. However, if they do not duplicate 
        // completely (check), then they could remain (the more constraints the better, 
        // unless this affects pre-processing time significantly). 
        // TODO: se kommentarerna precis ovan.
        externalConstraints += "/* 101- & 010-disablement, avoiding impossible event sequences */\n";
        // The index to separate different 101/010-constraint instances
        int disablementCounter = 1;
        // For start-event...
        int nrPPlantStates = allPPlantStates.size();
        for (int i = 0; i < nrPPlantStates - 2; i++)
        {           
            int[] firstPlantState = ((ArrayList<int[]>)allPPlantStates.get(i)).get(0);
            for (int j = i+1; j < nrPPlantStates - 1; j++)
            {
                int[] secondPlantState = ((ArrayList<int[]>)allPPlantStates.get(j)).get(0);
                String firstAlpha = internalPrecVarsTable.get(new IntKey(firstPlantState, secondPlantState)).getName();
                for (int k = j+1; k < nrPPlantStates; k++)
                {
                    int[] thirdPlantState = ((ArrayList<int[]>)allPPlantStates.get(k)).get(0);
                    String secondAlpha = internalPrecVarsTable.get(new IntKey(firstPlantState, thirdPlantState)).getName();
                    String thirdAlpha = internalPrecVarsTable.get(new IntKey(secondPlantState, thirdPlantState)).getName();
                    
                    // 101-constraint
                    String oneZeroOneDisablement = "2 - " + firstAlpha + " + " + secondAlpha + " - " + thirdAlpha + " >= 1;\n";
                    externalConstraints += "ozo_disablement_" + disablementCounter++ +  " : " + oneZeroOneDisablement;
                    // 010-constraint
                    String zeroOneZeroDisablement = "1 + " + firstAlpha + " - " + secondAlpha + " + " + thirdAlpha + " >= 1;\n";
                    externalConstraints += "zoz_disablement_" + disablementCounter++ +  " : " + zeroOneZeroDisablement;
                }
            }
        }
        // ...and for finish-events
        int nrFPlantStates = allFPlantStates.size();
        for (int i = 0; i < nrFPlantStates - 2; i++)
        {           
            int[] firstPlantState = ((ArrayList<int[]>)allFPlantStates.get(i)).get(0);
            for (int j = i+1; j < nrFPlantStates - 1; j++)
            {
                int[] secondPlantState = ((ArrayList<int[]>)allFPlantStates.get(j)).get(0);
                String firstAlpha = internalPrecVarsTable.get(new IntKey(firstPlantState, secondPlantState)).getName();
                for (int k = j+1; k < nrFPlantStates; k++)
                {
                    int[] thirdPlantState = ((ArrayList<int[]>)allFPlantStates.get(k)).get(0);
                    String secondAlpha = internalPrecVarsTable.get(new IntKey(firstPlantState, thirdPlantState)).getName();
                    String thirdAlpha = internalPrecVarsTable.get(new IntKey(secondPlantState, thirdPlantState)).getName();
                    
                    // 101-constraint
                    String oneZeroOneDisablement = "2 - " + firstAlpha + " + " + secondAlpha + " - " + thirdAlpha + " >= 1;\n";
                    externalConstraints += "ozo_disablement_" + disablementCounter++ +  " : " + oneZeroOneDisablement;
                    // 010-constraint
                    String zeroOneZeroDisablement = "1 + " + firstAlpha + " - " + secondAlpha + " + " + thirdAlpha + " >= 1;\n";
                    externalConstraints += "zoz_disablement_" + disablementCounter++ +  " : " + zeroOneZeroDisablement;
                }
            }
        }
    }
    
    /**
     *  Calls @findNearestPathSplits(auto, state, altPathsVariables, null), thus
     *  initiating search for the path splits on all transitions leading to state.
     *
     *  @param auto the automaton in which path splits may appear.
     *  @param state the state above which path splits are searched for.
     *  @param altPathVariables the  @ArrayList containing the indices of 
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
     *  @param auto, the automaton in which path splits may appear.
     *  @parem state, the state above which path splits are searched for.
     *  @param altPathVariables, the  @ArrayList containing the indices of 
     *           path split state pairs. The search is always ended by adding 
     *           NO_PATH_SPLIT_INDEX to the list.
     *  @param event, the event above (and including) which the search for 
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
    
    private void createNonCrossbookingConstraintsNew()
        throws Exception
    {       
        //temp
        logger.warn("CREATE_NON_CROSSBOOKING_CONSTRAINTS NOW...");
        
        // Each entry of this map contains an ArrayList of pairs of consecutive 
        // booking states, e.g. [Z1_b_index Z2_b_index]. These states are local to some plant, 
        // e.g. Pi, and mean that Pi can book Z1 in the state corresponding to Z1_b_index 
        // whereafter Z2 is booked in Z2_index without Z1 beeing unbooked in between. 
        // The keys are int[]-objects of type [plant_index, first_zone_index, second_zone_index].
        TreeMap<int[], ArrayList<int[]>> consecutiveBookingTicsIndices = 
                new TreeMap<int[], ArrayList<int[]>>(new IntArrayComparator()); 
        
        // Find all consecutive bookingTics, i.e. when a plant first books Z_i and then Z_j
        for (int p = 0; p < plants.size(); p++)
        {
            for (int z1 = 0; z1 < bookingTics.length; z1++)
            {
                for (int z2 = 0; z2 < bookingTics.length; z2++)
                {
                    if (z1 != z2)
                    {
                        if ((bookingTics[z1][p][STATE_SWITCH][0] != -1) && (bookingTics[z2][p][STATE_SWITCH][0] != -1))
                        {
                            ArrayList<int[]> currConsecutiveBookingTicsIndices = findBookingStatesSequences(p, z1, z2);
                            if (currConsecutiveBookingTicsIndices.size() > 0)
                            {
                                consecutiveBookingTicsIndices.put(new int[]{p, z1, z2}, currConsecutiveBookingTicsIndices);
                            }
                        }
                    }
                }
            }
        }
        
        //temp-test
        for (Iterator<int[]> keysIt = consecutiveBookingTicsIndices.keySet().iterator(); keysIt.hasNext();)
        {
            int[] key = keysIt.next();
            String stre = "key: ";
            for (int i = 0; i < key.length; i++)
            {
                stre += key[i] + " ";
            }
            stre += ": ";
            
            for (int[] tic : consecutiveBookingTicsIndices.get(key))
            {
                for (int i = 0; i < tic.length; i++)
                {    
                    stre += bookingTics[key[i+1]][key[0]][STATE_SWITCH][tic[i]] + " ";
                }
                stre += ", ";
            }
            logger.error("consecutiveBookings = " + stre);
        }
        //...temp-test
        
        // Find the consecutive bookings that are made reversely by different plants
        // Check for (almost) all combinations of z1&z2 
        int counter = 1;
        for (int z1 = 0; z1 < zones.size(); z1++)
        {
            for (int z2 = 0; z2 < zones.size(); z2++)
            {
                if (z1 != z2)
                {
                    // Used in for-loops to decrease code-repetition
                    int[] zoneIndices = new int[]{z1, z2};

                    // Check for all combinations of p1 < p2 (the mutex variables 
                    // are always of the form pi_books_zx_before_pj, where i < j)
                    for (int p1 = 0; p1 < plants.size() - 1; p1++)
                    {
                        // If p1 books z1 and then z2 consecutively
                        ArrayList<int[]> consecutiveTicsP1 = consecutiveBookingTicsIndices.get(new int[]{p1, z1, z2});
                        if (consecutiveTicsP1 != null)
                        {                       
                            for (int p2 = p1 + 1; p2 < plants.size(); p2++)
                            {
                                // If also p2 books the zones reversely, i.e. first z2 and then z1, then 
                                // constraint construction begins...
                                ArrayList<int[]> consecutiveTicsP2 = consecutiveBookingTicsIndices.get(new int[]{p2, z2, z1});
                                if (consecutiveTicsP2 != null) 
                                {    
                                    //temp-test
                                    String str = "Found match... ";
                                    for (int[] inP1 : consecutiveTicsP1)
                                    {
                                        str += bookingTics[z1][p1][STATE_SWITCH][inP1[0]] + " ";
                                        str += bookingTics[z2][p1][STATE_SWITCH][inP1[1]] + " ";
                                        str += ", ";
                                    }
                                    str += "; ";
                                    for (int[] inP2 : consecutiveTicsP2)
                                    {
                                        str += bookingTics[z2][p2][STATE_SWITCH][inP2[0]] + " ";
                                        str += bookingTics[z1][p2][STATE_SWITCH][inP2[1]] + " ";
                                        str += ", ";
                                    }
                                    logger.warn(str);                                        
                                    //...temp-test

                                    for (int[] tic1 : consecutiveTicsP1)
                                    {
                                        // Find nearest upwards path splits for each consecutive booking
                                        ArrayList<int[]> pathSplitInfosP1 = new ArrayList<int[]>();
                                        for (int zInd = 0; zInd < zoneIndices.length; zInd++)
                                        {
                                            int stateIndex = bookingTics[zoneIndices[zInd]][p1][STATE_SWITCH][tic1[zInd]];
                                            int eventIndex = bookingTics[zoneIndices[zInd]][p1][EVENT_SWITCH][tic1[zInd]];
                                            State bState = indexMap.getStateAt(plants.getAutomatonAt(p1), stateIndex);
                                            LabeledEvent bEvent = indexMap.getEventAt(eventIndex);

                                            findNearestPathSplits(plants.getAutomatonAt(p1), bState.nextState(bEvent), 
                                                    pathSplitInfosP1, bEvent);    
                                        }

                                        String pathSplitStrP1 = "";
                                        int pathSplitCounterP1 = 0;
                                        for (Iterator<int[]> it = pathSplitInfosP1.iterator(); it.hasNext();)
                                        {
                                            int[] pathSplitInfo = it.next();     
                                            if (pathSplitInfo[0] != NO_PATH_SPLIT_INDEX)
                                            {
                                                pathSplitCounterP1++;
                                                pathSplitStrP1 += " - " + makeAltPathsVariable(p1, pathSplitInfo[0], pathSplitInfo[1]);
                                            }
                                        }

                                        String noncrossConstrStr = "";
                                        String dualNoncrossConstrStr = "";
                                        ArrayList<int[]> pathSplitInfosP2 = new ArrayList<int[]>();
                                        for (int[] tic2 : consecutiveTicsP2)
                                        {          
                                            for (int zInd = 0; zInd < zoneIndices.length; zInd++)
                                            {      
                                                // dualZInd is used to reverse tic2, that needs to be reversed 
                                                // (so that the indices are right) since tic1 describes booking 
                                                // z1 -> z2, while tic1 represents z2 -> z1
                                                int dualZInd = tic2.length - 1 - zInd;

                                                noncrossConstrStr += "r" + p1 + "_books_z" + zoneIndices[zInd] + "_before_r" + p2;
                                                dualNoncrossConstrStr += "r" + p1 + "_books_z" + zoneIndices[dualZInd] + "_before_r" + p2;

                                                // If there is a "var_x"-appendix corresponding to the current variable, it should be appended 
                                                Integer varCounter = mutexVarCounterMap.get(new int[]{zoneIndices[zInd], p1, p2, 
                                                        tic1[zInd], tic2[dualZInd]});
                                                if (varCounter != null)
                                                {
                                                    noncrossConstrStr += "_var" + varCounter;
                                                }
                                                // If there is a "var_x"-appendix corresponding to the dual variable, it should be appended 
                                                varCounter = mutexVarCounterMap.get(new int[]{zoneIndices[dualZInd], p1, p2, 
                                                        tic1[dualZInd], tic2[zInd]});
                                                if (varCounter != null)
                                                {
                                                    dualNoncrossConstrStr += "_var" + varCounter;
                                                }

                                                if (! noncrossConstrStr.contains(">="))
                                                {
                                                    noncrossConstrStr = "non_crossbooking_" + counter + " : " + noncrossConstrStr + " >= ";
                                                    dualNoncrossConstrStr = "non_crossbooking_" + counter++ + "_dual : " + dualNoncrossConstrStr + " >= ";
                                                }

                                                int stateIndex = bookingTics[zInd][p2][STATE_SWITCH][tic1[zInd]];
                                                int eventIndex = bookingTics[zInd][p2][EVENT_SWITCH][tic1[zInd]];
                                                State bState = indexMap.getStateAt(plants.getAutomatonAt(p2), stateIndex);
                                                LabeledEvent bEvent = indexMap.getEventAt(eventIndex);

                                                findNearestPathSplits(plants.getAutomatonAt(p2), bState.nextState(bEvent), pathSplitInfosP2, bEvent);    
                                            }

                                            int pathSplitCounterP2 = 0;
                                            String pathSplitStrP2 = "";
                                            for (Iterator<int[]> it = pathSplitInfosP2.iterator(); it.hasNext();)
                                            {
                                                int[] pathSplitInfo = it.next();     
                                                if (pathSplitInfo[0] != NO_PATH_SPLIT_INDEX)
                                                {
                                                    pathSplitCounterP2++;
                                                    pathSplitStrP2 += " - " + makeAltPathsVariable(p2, pathSplitInfo[0], pathSplitInfo[1]);
                                                }
                                            }

                                            if ((pathSplitCounterP1 + pathSplitCounterP2) > 0)  
                                            {
                                                noncrossConstrStr += " - bigM*(" + (pathSplitCounterP1 + pathSplitCounterP2) + 
                                                        pathSplitStrP1 + pathSplitStrP2 + ")";
                                                dualNoncrossConstrStr += " - bigM*(" + (pathSplitCounterP1 + pathSplitCounterP2) + 
                                                        pathSplitStrP1 + pathSplitStrP2 + ")";
                                            }

                                            noncrossConstrStr += ";\n";
                                            dualNoncrossConstrStr += ";\n";
                                        }

                                        //temp
                                        logger.warn("str = " + noncrossConstrStr);
                                        logger.warn("dual_str = " + dualNoncrossConstrStr);

                                        //TODO: lägg in constr-stängen i MILP-en och testa optimera
                                        //TODO: gör så att dessa begr. aldrig bryts vid random-MILP
                                        //TODO: testa om "var" funkar bra (orka?)
                                        //TODO: dokumentera i /tankar/milp.tex
                                        //TODO: VelocityBalansering
                                        //TODO: Vad lämnade Hugo för smaskens?
                                    }
                                }
                            }
                        }   
                    }
                }           
            }
        }
    }
    
    /**
     * This method creates additional constraints that (may) speed up the optimization. 
     * The constraints prevent two plants moving towards eachother from cross-booking two zones.
     * More specifically, suppose that R1 passes first through Z1 and then Z2, while R2 moves 
     * from the other direction, Z2 and then Z1. Then if R1 book Z2 before R2, then it must also
     * book Z1 before R2, and vice versa. This poses additional constraint on the boolean variables: 
     * R1_books_Z1_before_R2 >= R1_books_Z2_before_R2 - M*(4 - Z1_reached_by_R1 - Z2_reached_by_R1 - 
     * Z1_reached_by_R2 - Z2_reached_by_R2).
     */
    private void createNonCrossbookingConstraints()
        throws Exception
    {       
        for (int z1 = 0; z1 < bookingTics.length; z1++)
        {
            for (int z2 = 0; z2 < bookingTics.length; z2++)
            {
                if (z1 != z2)
                {
                    ArrayList<int[]>[] bookingTicsIndices = new ArrayList[plants.size()];
                    for (int p = 0; p < plants.size(); p++)
                    {
                        if ((bookingTics[z1][p][STATE_SWITCH][0] != -1) && (bookingTics[z2][p][STATE_SWITCH][0] != -1))
                        {
                            bookingTicsIndices[p] = findBookingStatesSequences(p, z1, z2);
                        }
                    }
                    
                    for (int p1 = 0; p1 < bookingTicsIndices.length - 1; p1++)
                    {
                        for (int p2 = p1+1; p2 < bookingTicsIndices.length; p2++)
                        {
                            if ((bookingTicsIndices[p1].size() > 0) && (bookingTicsIndices[p2].size() > 0))
                            {
                                Automaton plant = plants.getAutomatonAt(p2);
                                
                                ArrayList<int[]>[] pathSplitInfosP2 = new ArrayList[bookingTicsIndices[p2].size()];
                                for (int ticPair = 0; ticPair < bookingTicsIndices[p2].size(); ticPair++)
                                {
                                    pathSplitInfosP2[ticPair] = new ArrayList<int[]>();
                                    for (int tic = 0; tic < bookingTicsIndices[p2].get(ticPair).length; tic++)
                                    {
                                        int stateIndex = bookingTics[z2][p2][STATE_SWITCH][bookingTicsIndices[p2].get(ticPair)[tic]];
                                        int eventIndex = bookingTics[z2][p2][EVENT_SWITCH][bookingTicsIndices[p2].get(ticPair)[tic]];
                                        State bState = indexMap.getStateAt(plant, stateIndex);
                                        LabeledEvent bEvent = indexMap.getEventAt(eventIndex);

                                        findNearestPathSplits(plant, bState.nextState(bEvent), pathSplitInfosP2[ticPair], bEvent);
                                    }
                                }

                                plant = plants.getAutomatonAt(p1);
                                
//                                ArrayList<String> boolVariableRoots = new ArrayList<String>();
//                                boolVariableRoots.add("r" + p1 + "_books_z" + z1 + "_before_r" + p2);
//                                boolVariableRoots.add("r" + p1 + "_books_z" + z2 + "_before_r" + p2);
                                for (int ticPair = 0; ticPair < bookingTicsIndices[p1].size(); ticPair++)
                                {        
//                                    ArrayList<String> timeVariableRoots = new ArrayList<String>();
                                    ArrayList<int[]> pathSplitInfosP1 = new ArrayList<int[]>();
                                    for (int tic = 0; tic < bookingTicsIndices[p1].get(ticPair).length; tic++)
                                    {
//                                        ArrayList<int[]> currPathSplitInfo = new ArrayList<int[]>();
                                        int stateIndex = bookingTics[z1][p1][STATE_SWITCH][bookingTicsIndices[p1].get(ticPair)[tic]];
                                        int eventIndex = bookingTics[z1][p1][EVENT_SWITCH][bookingTicsIndices[p1].get(ticPair)[tic]];
                                        State bState = indexMap.getStateAt(plant, stateIndex);
                                        LabeledEvent bEvent = indexMap.getEventAt(eventIndex);

                                        findNearestPathSplits(plant, bState.nextState(bEvent), pathSplitInfosP1, bEvent);
                                        
//                                        timeVariableRoots.add("time[" + p1 + ", " + stateIndex + "]");
                                    }

                                    
                                    
//                                    BufferedReader r = new BufferedReader(new FileReader(modelFile));
//                                    String str;
//                                    while ((str = r.readLine()) != null)
//                                    {
//                                        String foundBoolVariableRoot = null;
//                                        boolVarSearch: for (String boolVariableRoot : boolVariableRoots)
//                                        {
//                                            if (str.contains(boolVariableRoot))
//                                            {
//                                                foundBoolVariableRoot = boolVariableRoot;
//                                                break boolVarSearch;
//                                            }
//                                        }
//                                        
//                                        if (foundBoolVariableRoot != null)
//                                        {
//                                            for (String timeVariableRoot : timeVariableRoots)
//                                            {
//                                                if (str.contains(timeVariableRoot))
//                                                {
//                                                    str = str.substring(str.indexOf(foundBoolVariableRoot));
//                                                    str = str.substring(0, str.indexOf(" ")).trim();
//                                                }   
//                                            }
//                                        }
//                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
                
//                ArrayList<Integer> plantsUsingZones = new ArrayList<Integer>(); //TODO...
                
//        for (int i = 0; i < bookingTics.length - 1; i++)
//        {
//            for (int j = i+1; j < bookingTics.length; j++)
//            {
//                ArrayList[][] bookingTicsIndices = new ArrayList[plants.size()][];
////                ArrayList<Integer> plantsUsingZones = new ArrayList<Integer>(); //TODO...
//                for (int k = 0; k < plants.size(); k++)
//                {
//                    if ((bookingTics[i][k][0] != -1) && (bookingTics[j][k][0] != -1))
//                    {
////                        plantsUsingZones.add(new Integer(k));
//                        int plantIndex = indexMap.getAutomatonIndex(plants.getAutomatonAt(k));
//                        
//                        bookingTicsIndices[plantIndex] = new ArrayList[2];
//                        bookingTicsIndices[plantIndex][0] = findBookingStatesSequences(bookingTics[j][plantIndex], bookingTics[i][plantIndex], plantIndex);
//                        bookingTicsIndices[plantIndex][1] = findBookingStatesSequences(bookingTics[i][plantIndex], bookingTics[j][plantIndex], plantIndex);
//                    }
//                }
//                
//                outerfor: for (int k1 = 0; k1 < plants.size() - 1; k1++)
//                {
//                    for (int k2 = k1 + 1; k2 < plants.size(); k2++)
//                    {
//                        logger.info("i = " + i + "; j = " + j + "; k1 = " + k1 + "; k2 = " + k2);
//                        if (bookingTicsIndices[k1] != null && bookingTicsIndices[k2] != null)
//                        {
//                            int[] activePlantIndices = new int[]{k1, k2};
//                            int[] activeZoneIndices = new int[]{i, j};
//                                                
//                            String pathSplitStr = "";
//                            int nrOfPathSplits = 0;
//                            for (int pInd1 = 0; pInd1 < activePlantIndices.length; pInd1++)
//                            {
//                                int pInd2 = 1 - pInd1;
//                                if ((bookingTicsIndices[activePlantIndices[pInd1]][0].size() > 0) 
//                                && (bookingTicsIndices[activePlantIndices[pInd2]][1].size() > 0))
//                                {                                            
////                                    Automaton activePlant = plants.getAutomatonAt(activePlantIndices[pInd1]);
////
////                                    for (int zInd1 = 0; zInd1 < activeZoneIndices.length; zInd1++)
////                                    {
////                                        Automaton activeZone = zones.getAutomatonAt(activeZoneIndices[zInd1]);
////
////                                        //temp
////                                        logger.error("pind1 = " + pInd1 + "; pind2 = " + pInd2 + "; zind = " + zInd1);
////                                        logger.error("Checking " + activePlant.getName() + "_" + activeZone.getName()); 
//                                                
//                                        for (Iterator bookingPairsIt = bookingTicsIndices[activePlantIndices[pInd1]][0].iterator(); bookingPairsIt.hasNext();)
//                                        {
//                                            int[] currBookingPair = (int[]) bookingPairsIt.next();
//
//                                            for (int stInd = 0; stInd < currBookingPair.length; stInd++)
//                                            {
//                                                State currState = indexMap.getStateAt(plants.getAutomatonAt(activePlantIndices[ind1]), currBookingPair[stInd]);
//                                                ArrayList<int[]> nearestPathSplits = new ArrayList<int[]>();
//                                                Alphabet activeEvents = AlphabetHelpers.intersect(currState.activeEvents(false), zones.getAutomatonAt(activeZoneIndices[stInd]).getAlphabet());
//                                                for (Iterator<LabeledEvent> activeEventIt = activeEvents.iterator(); activeEventIt.hasNext();)
//                                                {
//                                                    LabeledEvent activeEvent = activeEventIt.next();
//                                                    findNearestPathSplits(plants.getAutomatonAt(activePlantIndices[ind1]), currState.nextState(activeEvent), nearestPathSplits, activeEvent);
//                                                }
//                                                for (Iterator<int[]> it = nearestPathSplits.iterator(); it.hasNext();)
//                                                {
//                                                    int[] pathSplitInfo = it.next();
//                                                    if (pathSplitInfo[0] != NO_PATH_SPLIT_INDEX)
//                                                    {
//                                                        pathSplitStr += " - " + makeAltPathsVariable(k1, pathSplitInfo[0], pathSplitInfo[1]);
//                                                        nrOfPathSplits++;
//                                                    }
//                                                }                                        
//                                            }
//                                        }
//                                        for (Iterator bookingPairsIt = bookingTicsIndices[activePlantIndices[pInd2]][1].iterator(); bookingPairsIt.hasNext();)
//                                        {
//                                            int[] currBookingPair = (int[]) bookingPairsIt.next();
//
//                                            for (int stInd = 0; stInd < currBookingPair.length; stInd++)
//                                            {
//                                                State currState = indexMap.getStateAt(plants.getAutomatonAt(activePlantIndices[ind2]), currBookingPair[stInd]);
//                                                ArrayList<int[]> nearestPathSplits = new ArrayList<int[]>();
//                                                Alphabet activeEvents = AlphabetHelpers.intersect(currState.activeEvents(false), zones.getAutomatonAt(activeZoneIndices[1 - stInd]).getAlphabet());
//                                                for (Iterator<LabeledEvent> activeEventIt = activeEvents.iterator(); activeEventIt.hasNext();)
//                                                {
//                                                    LabeledEvent activeEvent = activeEventIt.next();
//                                                    findNearestPathSplits(plants.getAutomatonAt(activePlantIndices[ind2]), currState.nextState(activeEvent), nearestPathSplits, activeEvent);
//                                                }
//                                                for (Iterator<int[]> it = nearestPathSplits.iterator(); it.hasNext();)
//                                                {
//                                                    int[] pathSplitInfo = it.next();
//                                                    if (pathSplitInfo[0] != NO_PATH_SPLIT_INDEX)
//                                                    {
//                                                        pathSplitStr += " - " + makeAltPathsVariable(k1, pathSplitInfo[0], pathSplitInfo[1]);
//                                                        nrOfPathSplits++;
//                                                    }
//                                                }                                        
//                                            }
//                                        }
//                                        
//                                        int zInd2 = (int) Math.IEEEremainder(zInd1 + 1, 2);
//                                        String wStr = "R" + activePlantIndices[pInd1] + "_books_Z" + activeZoneIndices[zInd1] + 
//                                                "_before_R" + activePlantIndices[pInd2] + " >= " + "R" + activePlantIndices[pInd1] + 
//                                                "_books_Z" + activeZoneIndices[zInd2] + "_before_R" + activePlantIndices[pInd2];
//                                        if (nrOfPathSplits > 0)
//                                        {
//                                            wStr += " - bigM*(" + nrOfPathSplits + pathSplitStr + ")";
//
//                                        }
//                                        else //TEMP
//                                            wStr += " - nada";
//                                        wStr += ";\n";
//                                        logger.warn(wStr);
//                                    }
//                                }
//                            }
//                        }
//                        
//                        //temp
//                        break outerfor;
//                    }
//                }
//            }
//        }
//    }
    
    /**
     * Finds all pairs of booking tics, such that the plant with index plantIndex 
     * books the mutex zone with index pZoneIndex first and the zone corresponding 
     * to fZoneIndex second without(!) unbooking pZoneIndex in between.
     *
     * @param   pZoneIndex  the internal index of the firstly booked zone
     * @param   fZoneIndex  the internal index of the secondly booked zone
     * @param   plantIndex  the internal index of the plant, booking the mutex zones
     * @return  array of int[], containg pairs of booking tics indices
     */
    private ArrayList<int[]> findBookingStatesSequences(int plantIndex, int pZoneIndex, int fZoneIndex)
    {       
        ArrayList<int[]> bookingStateSequences = new ArrayList<int[]>();

        Automaton plant = plants.getAutomatonAt(plantIndex);
        
        // For faster processing, the booking tics of the preceding zone are put into a hashtable 
        Hashtable<Integer, Integer> pBStateIndicesTable = new Hashtable<Integer, Integer>();
        for (int i = 0; i < bookingTics[pZoneIndex][plantIndex][STATE_SWITCH].length; i++)
        {
            pBStateIndicesTable.put(new Integer(bookingTics[pZoneIndex][plantIndex][STATE_SWITCH][i]), new Integer(i));
        }
        
        // Find the events that are common to the current plant pZone 
        Alphabet pCommonAlphabet = AlphabetHelpers.intersect(
                plants.getAutomatonAt(plantIndex).getAlphabet(),
                zones.getAutomatonAt(pZoneIndex).getAlphabet());
        // Find the events in the plant that unbook pZone
        Alphabet pUnbookingAlphabet = AlphabetHelpers.minus(pCommonAlphabet, 
                zones.getAutomatonAt(pZoneIndex).getInitialState().activeEvents(false));
        // The remaining common events book pZone
        Alphabet pBookingAlphabet = AlphabetHelpers.minus(pCommonAlphabet, pUnbookingAlphabet);
        
        // The loop searching for preceding bookings to each booking of fZone that 
        // fulfil the requirements of "consecutive booking"
        for (int i = 0; i < bookingTics[fZoneIndex][plantIndex][STATE_SWITCH].length; i++)
        {
            
            State fState = indexMap.getStateAt(plant, bookingTics[fZoneIndex][plantIndex][STATE_SWITCH][i]);
                       
            ArrayList<State> upstreamsStates = new ArrayList<State>();
            upstreamsStates.add(fState);
            
            // Starting with the booking of fZone, we search upwards
            while (!upstreamsStates.isEmpty())
            {
                State currUpstreamsState = upstreamsStates.remove(0);
                for (Iterator<Arc> incomingArcsIt = currUpstreamsState.incomingArcsIterator(); incomingArcsIt.hasNext();)
                {
                    Arc incomingArc = incomingArcsIt.next();

                    // If the incoming event does not unbook pZone...
                    if (!pUnbookingAlphabet.contains(incomingArc.getEvent()))
                    {
                        // If that event actually books pZone, then a consecutive sequence is added to the list
                        if (pBookingAlphabet.contains(incomingArc.getEvent()))
                        {
                            int bookingStateIndex = indexMap.getStateIndex(plant, incomingArc.getFromState());                                 
                            bookingStateSequences.add(new int[]{
                                pBStateIndicesTable.get(new Integer(bookingStateIndex)).intValue(), i});
                        }
                        // Otherwise, if we neither find booking nor unbooking of pZone,
                        // the search is continued by adding all states that lead to the current state. 
                        // By checking for the unbooking of pZone, we avoid sequences like "Ri_bj -> Ri_uj -> Ri_bk",
                        // that do not comply with the definition of "consecutive booking".
                        else
                        {
                            upstreamsStates.add(incomingArc.getFromState());
                        }
                    }
                }
            }
        }
        
        //temp
        if (bookingStateSequences.size() > 0)
        {
            logger.error("found " + bookingStateSequences.size() + " sequences");
        }

        return bookingStateSequences;
    }
    
    /****************************************************************************************/
    /*                                 THE AUTOMATA-MILP-BRIDGE-METHODS                     */
    /****************************************************************************************/
    
    /**
     * Converts the automata to the MILP-formulation of the optimization problem.
     * Precedence, Mutual Exclusion, Cycle Time and Alternative Path constraints are
     * constructed.
     */
    protected void convertAutomataToMilp()
    throws Exception
    {        
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
            int currplantIndex = indexMap.getAutomatonIndex(currPlant);
            
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
                        initPrecConstraints += "initial_" + "r" + currplantIndex + "_" + currStateIndex + " : ";
                        initPrecConstraints += "time[" + i + ", " + currStateIndex + "] >= deltaTime[" + i + ", " + currStateIndex + "];\n";
                    }
                    
                    Iterator<State> nextStates = currState.nextStateIterator();
                    
                    // If there is only one successor, add a precedence constraint
                    if (nbrOfOutgoingMultiArcs == 1)
                    {
                        State nextState = nextStates.next();
                        int nextStateIndex = indexMap.getStateIndex(currPlant, nextState);
                        
                        precConstraints += "prec_" + "r" + currplantIndex + "_" + currStateIndex + "_" + nextStateIndex + " : " + 
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
                    
//                         String currAltPathsVariable = "r" + currplantIndex + "_from_" + currStateIndex + "_to_" + nextLeftStateIndex;
                    
//                         altPathsVarDecl += "var " + currAltPathsVariable + ", binary;\n";
                    
//                         altPathsConstraints += "alt_paths_" + "r" + currplantIndex + "_" + currStateIndex + " : ";
//                         altPathsConstraints += "time[" + i + ", " + nextLeftStateIndex + "] >= time[" + i + ", " + currStateIndex + "] + deltaTime[" + i + ", "  + nextLeftStateIndex + "] - bigM*(1 - " + currAltPathsVariable + ");\n";
                    
//                         pathCutTable.put(nextLeftState, currAltPathsVariable);
                    
//                         altPathsConstraints += "dual_alt_paths_" + "r" + currplantIndex + "_" + currStateIndex + " : ";
//                         altPathsConstraints += "time[" + i + ", " + nextRightStateIndex + "] >= time[" + i + ", " + currStateIndex + "] + deltaTime[" + i + ", "  + nextRightStateIndex + "] - bigM*" + currAltPathsVariable + ";\n";
                    
//                         pathCutTable.put(nextRightState, "(1 - " + currAltPathsVariable + ")");
//                     }
                    // If there are several successors, add one alternative-path variable for each successor
                    else
                    {
                        int currAlternative = 0;
                        String sumConstraint = "alt_paths_" + "r" + currplantIndex + "_" + currStateIndex + "_TOT : ";
                        
                        while (nextStates.hasNext())
                        {
                            State nextState = nextStates.next();
                            int nextStateIndex = indexMap.getStateIndex(currPlant, nextState);
                            
                            String currAltPathsVariable = makeAltPathsVariable(currplantIndex, currStateIndex, nextStateIndex);
                            sumConstraint += currAltPathsVariable + " + ";
                            
                            altPathsVarDecl += "var " + currAltPathsVariable + ", binary;\n";
                            
                            altPathsConstraints += "alt_paths_" + currAltPathsVariable + " : " + 
                                    "time[" + i + ", " + nextStateIndex + "] >= time[" + i + ", " + currStateIndex + 
                                    "] + deltaTime[" + i + ", "  + nextStateIndex + "] - bigM*(1 - " + 
                                    currAltPathsVariable + ") + epsilon;\n";
                            
                            pathCutTable.put(nextState, "(1 - " + currAltPathsVariable + ")");
                            
                            currAlternative++;
                        }
                        
                        altPathsConstraints += sumConstraint.substring(0, sumConstraint.lastIndexOf("+")) + "= ";
                        
// 						TreeSet<int[]> nearestPathSplits = new TreeSet<int[]>(new PathSplipIndexComparator());
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
                                altPathsConstraints += makeAltPathsVariable(currplantIndex, currPathSplit[0], currPathSplit[1]) + " + ";
                            }
                            else
                            {
                                altPathsConstraints += "1 + ";
                            }
                        }
                        altPathsConstraints = altPathsConstraints.substring(0, altPathsConstraints.lastIndexOf(" +")) + ";\n";
// 						}
// 						altPathsConstraints += sumConstraint.substring(0, sumConstraint.laspIndexOf("+")) + "= 1;\n";
                        
                    }
                }
                else if (currState.isAccepting())
                {
                    // If the current state is accepting, a cycle time constaint is added,
                    // ensuring that the makespan is at least as long as the minimum cycle time of this plant
                    cycleTimeConstraints += "cycle_time_" + "r" + currplantIndex + " : c >= " + "time[" + i + ", " + currStateIndex + "];\n";
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
                    for (int k1=0; k1<bookingTics[i][j1][STATE_SWITCH].length; k1++)
                    {
                        for (int k2=0; k2<bookingTics[i][j2][STATE_SWITCH].length; k2++)
                        {
                            if (bookingTics[i][j1][STATE_SWITCH][0] != -1 && bookingTics[i][j2][STATE_SWITCH][0] != -1)
                            {
                                repeatedBooking++;
                                
                                // Fills the map, that is used in restiction of cross-booking
                                mutexVarCounterMap.put(new int[]{i, j1, j2, k1, k2}, new Integer(repeatedBooking));
                                
                                String currMutexVariable = "r" + j1 + "_books_z" + i + "_before_r" + j2 + "_var" + repeatedBooking;
                                
                                mutexVariables += "var " + currMutexVariable + ", binary;\n";
                                
                                //test
                                ArrayList<int[]> bList = new ArrayList<int[]>();
                                Automaton bPlant = indexMap.getAutomatonAt(j1);
                                State bState = indexMap.getStateAt(bPlant, bookingTics[i][j1][STATE_SWITCH][k1]);
                                LabeledEvent bEvent = indexMap.getEventAt(bookingTics[i][j1][EVENT_SWITCH][k1]);
                                findNearestPathSplits(bPlant, bState.nextState(bEvent), bList, bEvent);
                                
                                //temp
                                String bName = bEvent.getName();
                                int sIndex = bookingTics[i][j1][STATE_SWITCH][k1];
                                int pIndex = j1;
                                int zIndex = i;
                                int blistlength = bList.size();
                                
                                ArrayList<int[]> uList = new ArrayList<int[]>();
                                Automaton uPlant = indexMap.getAutomatonAt(j2);
                                State uState = indexMap.getStateAt(uPlant, unbookingTics[i][j2][STATE_SWITCH][k2]);
                                LabeledEvent uEvent = indexMap.getEventAt(unbookingTics[i][j2][EVENT_SWITCH][k2]);
                                findNearestPathSplits(uPlant, uState.nextState(uEvent), uList, uEvent);
                                
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
                                mutexConstraints += "mutex_z" + i + "_r" + j1 + "_r" + j2 + "_var" + repeatedBooking + " : time[" + j1 + ", " + bookingTics[i][j1][STATE_SWITCH][k1] + "] >= " + "time[" + j2 + ", " + unbookingTics[i][j2][STATE_SWITCH][k2] + "] - bigM*";
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
                                bState = indexMap.getStateAt(bPlant, bookingTics[i][j2][STATE_SWITCH][k2]);
                                bEvent = indexMap.getEventAt(bookingTics[i][j2][EVENT_SWITCH][k2]);
                                
                                findNearestPathSplits(bPlant, bState.nextState(bEvent), bList, bEvent);
                                uList = new ArrayList<int[]>();
                                uPlant = indexMap.getAutomatonAt(j1);
                                uState = indexMap.getStateAt(uPlant, unbookingTics[i][j1][STATE_SWITCH][k1]);
                                uEvent = indexMap.getEventAt(unbookingTics[i][j1][EVENT_SWITCH][k1]);
                                
                                findNearestPathSplits(uPlant, uState.nextState(uEvent), uList, uEvent);
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
                                mutexConstraints += "dual_mutex_z" + i + "_r" + j1 + "_r" + j2  + "_var" + repeatedBooking + " : time[" + j2 + ", " + bookingTics[i][j2][STATE_SWITCH][k2] + "] >= " + "time[" + j1 + ", " + unbookingTics[i][j1][STATE_SWITCH][k1] + "] - bigM*(";
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
//                                mutexConstraints += "dual_mutex_z" + i + "_r" + j1 + "_r" + j2  + "_var" + repeatedBooking + " : time[" + j2 + ", " + bookingTics[i][j2][STATE_SWITCH][k2] + "] >= " + "time[" + j1 + ", " + unbookingTics[i][j1][STATE_SWITCH][k1] + "] - bigM*(1 - " + currMutexVariable + ")" + " + " + epsilon;
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
    }
    
    protected void processSolutionFile()
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
                String strplantIndex = str.substring(str.indexOf("[") + 1, str.indexOf(",")).trim();
                String strStateIndex = str.substring(str.indexOf(",") + 1, str.indexOf("]")).trim();
                String strCost = str.substring(str.indexOf("]") + 1).trim();
                
                int plantIndex = (new Integer(strplantIndex)).intValue();
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
                String strplantIndex = str.substring(0, str.indexOf("_"));
                String strStartStateIndex = str.substring(str.indexOf("_") + 1, str.lastIndexOf("_"));
                String strEndStateIndex = str.substring(str.lastIndexOf("_") + 1);
                if (strEndStateIndex.indexOf(" ") > -1)
                {
                    strEndStateIndex = strEndStateIndex.substring(0, strEndStateIndex.indexOf(" "));
                }
                
                int plantIndex = (new Integer(strplantIndex)).intValue();
                int startStateIndex = (new Integer(strStartStateIndex)).intValue();
                int endStateIndex = (new Integer(strEndStateIndex)).intValue();
                
                pathChoices[plantIndex][startStateIndex][endStateIndex] = true;
            }
            else if (str.indexOf("_from") > -1 && str.indexOf("alt_paths") < 0)
            {
                String strplantIndex = str.substring(str.indexOf("r") + 1, str.indexOf("_"));
                str = str.substring(str.indexOf("_from_") + 6);
                String strStartStateIndex = str.substring(0, str.indexOf("_"));
                String strEndStateIndex = str.substring(str.lastIndexOf("_") + 1);
                if (strEndStateIndex.indexOf(" ") > -1)
                {
                    strEndStateIndex = strEndStateIndex.substring(0, strEndStateIndex.indexOf(" "));
                }
                
                int plantIndex = (new Integer(strplantIndex)).intValue();
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
    
    protected void callMilpSolver()
    throws Exception
    {
        callMilpSolver(modelFile);
    }
                
    protected void callMilpSolver(File currModelFile)
    throws Exception
    {
        logger.info("Callingt the MILP-solver....");
        
        // Defines the name of the .exe-file as well the arguments (.mod and .sol file names)
        String[] cmds = new String[5];
        //cmds[0] = "C:\\Program Files\\glpk\\bin\\glpsol.exe";
        cmds[0] = "glpsol";
        cmds[1] = "-m";
        cmds[2] = currModelFile.getAbsolutePath();
        cmds[3] = "-o";
        cmds[4] = solutionFile.getAbsolutePath();
        
        String str = "";
        for (int i = 0; i < cmds.length; i++)
        {
            str += " " + cmds[i];
        }
        logger.info("cmds =" + str);
        
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
            if (milpEchoStr.contains("NO") && milpEchoStr.contains("FEASIBLE SOLUTION"))
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
     *  @param currPlant The plant to be prepared for MILP-optimization.
     */
    private void prepareAutomatonForMilp(Automaton currPlant)
        throws Exception
    {
        State currInitialState = currPlant.getInitialState();
        
        // If there is no initial state, throw exception
        if (currInitialState == null)
        {
            int plantNameRoopIndex = currPlant.getName().indexOf("_constr");
            String plantName;
            if (plantNameRoopIndex < 0)
            {
                plantName = currPlant.getName();
            }
            else
            {
                plantName = currPlant.getName().substring(0, plantNameRoopIndex);
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
        addAutomatonToGui(auto, true);
    }
    
    private synchronized void addAutomatonToGui(Automaton auto, boolean addToEditor)
    throws Exception
    {
        if (scheduleDialog != null)
        {
            IDEActionInterface ide = null;
            
            try
            {
                ide = scheduleDialog.getIde();
                ide.getIDE().getActiveDocumentContainer().getAnalyzerPanel().addAutomaton(auto);
                
                // The following part should be somewhere else, but unfortunately the communication between the editor
                // and the analyzer are not automatic in the IDE, tuff luck...
                if (addToEditor)
                {
                    if (ide.getActiveDocumentContainer().getEditorPanel() != null)
                    {
                        // Compile into Waters module
                        net.sourceforge.waters.model.marshaller.ProductDESImporter importer = 
                                new net.sourceforge.waters.model.marshaller.ProductDESImporter(net.sourceforge.waters.subject.module.ModuleSubjectFactory.getInstance());

                        net.sourceforge.waters.model.module.SimpleComponentProxy component = importer.importComponent(schedule);
                        if (ide.getActiveDocumentContainer().getEditorPanel().getEditorPanelInterface().componentNameAvailable(component.getName()))
                        {
                            // Add to current module
                            try
                            {
                                ide.getActiveDocumentContainer().getEditorPanel().getEditorPanelInterface().addComponent((net.sourceforge.waters.subject.base.AbstractSubject) component);

                                // Add all (new) events to the module
                                net.sourceforge.waters.subject.module.ModuleSubject module = ide.getActiveDocumentContainer().getEditorPanel().getEditorPanelInterface().getModuleSubject();
                                boolean problem = false;
                                for (LabeledEvent event: schedule.getAlphabet())
                                {
                                    if (!event.getName().contains("["))
                                    {
                                        if (!module.getEventDeclListModifiable().containsName(event.getName()))
                                        {
                                            final net.sourceforge.waters.model.des.EventProxy proxy = 
                                                    (net.sourceforge.waters.model.des.EventProxy) event;
                                            final net.sourceforge.waters.subject.module.EventDeclSubject decl =
                                                new net.sourceforge.waters.subject.module.EventDeclSubject(proxy.getName(),
                                                proxy.getKind(),
                                                proxy.isObservable(),
                                                net.sourceforge.waters.xsd.module.ScopeKind.LOCAL,
                                                null, null);
                                            module.getEventDeclListModifiable().add(decl);
                                        }
                                    }
                                    else
                                    {
                                        problem = true;
                                    }
                                }
                                if (problem)
                                {
                                    javax.swing.JOptionPane.showMessageDialog(ide.getFrame(), "There is a problem in the back-translation of parametrised events.", "Alert", javax.swing.JOptionPane.WARNING_MESSAGE);
                                }
                            }
                            catch (Exception ex)
                            {
                                ide.getIDE().error("Could not add " + schedule + " to editor." + ex);
                            }
                        }
                        else
                        {
                            javax.swing.JOptionPane.showMessageDialog(ide.getFrame(), "Component: " + component.getName() + " already exists in editor", "Duplicate Name", javax.swing.JOptionPane.ERROR_MESSAGE);
                        }
                    }
                    else
                    {
                        javax.swing.JOptionPane.showMessageDialog(ide.getFrame(), "The editor is unknown. The schedule was not added.", "Editor null", javax.swing.JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
            catch (Exception ex)
            {
                logger.warn("EXceptiON, ide = " + ide);            
                throw ex;
            }
        }
    }
    
    private synchronized void updateGui(Automata autos)
    throws Exception
    {
        if (scheduleDialog != null)
        {
            IDEActionInterface ide = null;
            
            try
            {
                ide = scheduleDialog.getIde();
                
                // Remove old automata
                Automata selectedAutos = ide.getIDE().getActiveDocumentContainer().getAnalyzerPanel().getSelectedAutomata();
                for (Iterator<Automaton> autIt = selectedAutos.iterator(); autIt.hasNext(); )
                {
                    Automaton selectedAuto = autIt.next();
                    if (!autos.containsAutomaton(selectedAuto.getName()))
                    {
//                        ide.getVisualProjectContainer().getActiveProject().removeAutomaton(selectedAuto);
                        ide.getIDE().getActiveDocumentContainer().getAnalyzerPanel().getVisualProject().removeAutomaton(selectedAuto);
                    }
                }
                
                // Add missing automata
                for (Iterator<Automaton> autIt = autos.iterator(); autIt.hasNext(); )
                {
                    Automaton auto = autIt.next();
                    if (!selectedAutos.containsAutomaton(auto.getName()))
                    {
                        ide.getIDE().getActiveDocumentContainer().getAnalyzerPanel().addAutomaton(auto);
                    }
                }
            }
            catch (Exception ex)
            {
                logger.warn("EXceptiON, ide = " + ide);
                
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
     *  @param time 
     *  @return the time without epsilons
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
        
//        logger.warn("epsi = " + EPSILON + "; roundOff = " + roundOffCoeff + "; time = " + time + "; " +
//                "rTime = " + Math.floor(time / roundOffCoeff) * roundOffCoeff + 
//                "; rScTime = " + Math.floor(time / roundOffCoeff) * roundOffCoeff * timeThroughBigMApprox + 
//                "; ScRTime = " + Math.floor((time * timeThroughBigMApprox) / roundOffCoeff) * roundOffCoeff);
        
        // Remove epsilons from the current time.
        // The timeThroughBigMApprox is equal to 1 unless a rescaling has been done.
        // If the system was rescaled, the times return to their original (or rather correct) values.
        return Math.floor(time / roundOffCoeff) * roundOffCoeff * timeThroughBigMApprox;
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
        //test
        for (int i = 0; i < toOrder1.size(); i++)
        {
            int[] plantStateInOrder1 = (int[]) toOrder1.get(i).get(0);
            for (int j = 0; j < toOrder2.size(); j++)
            {
                int[] plantStateInOrder2 = (int[]) toOrder2.get(j).get(0);
                if ((plantStateInOrder1[0] == plantStateInOrder2[0]) && (plantStateInOrder1[1] == plantStateInOrder2[1]))
                {
                    ArrayList<ArrayList> newToOrder1 = new ArrayList<ArrayList>(toOrder1);
                    ArrayList<ArrayList> newToOrder2 = new ArrayList<ArrayList>(toOrder2);
                    newToOrder1.remove(i);
                    newToOrder2.remove(j);
                    
                    ArrayList<ArrayList> combinedAlternativeOrderings = getEveryAlternatingOrdering(toOrder1, newToOrder2);
                    combinedAlternativeOrderings.addAll(getEveryAlternatingOrdering(newToOrder1, toOrder2));
                    return combinedAlternativeOrderings;
                }
            }
        }
        
        ArrayList<ArrayList> allOrderings1 = new ArrayList<ArrayList>();
        ArrayList<ArrayList> allOrderings2 = new ArrayList<ArrayList>();
        getEveryOrdering(toOrder1, new ArrayList<ArrayList>(), allOrderings1);
        getEveryOrdering(toOrder2, new ArrayList<ArrayList>(), allOrderings2);
        
        ArrayList<ArrayList> allCombinedOrderings = new ArrayList<ArrayList>();       
        for (ArrayList<ArrayList> i1 : allOrderings1)
        {
            for (ArrayList<ArrayList> i2 : allOrderings2)
            {
                int commonSize = Math.min(i1.size(), i2.size());
                ArrayList<ArrayList> combinedOrdering = new ArrayList<ArrayList>();
                for (int i = 0; i < commonSize; i++)
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
    
    /**
     * FULHACK (ugly implementation).
     */
    private boolean isPlantStateInArray(ArrayList<ArrayList<int[]>> array, ArrayList<int[]> plantStateInfo)
    {
        mainloop: for (ArrayList<int[]> inArray : array)
        {
            if (inArray.size() != plantStateInfo.size())
            {
                continue mainloop;
            }
            
            for (int i = 0; i < inArray.size(); i++)
            {
                if (inArray.get(i).length != plantStateInfo.get(i).length)
                {
                    continue mainloop;
                }
                
                for (int j = 0; j < inArray.get(i).length; j++)
                {
                    if (inArray.get(i)[j] != plantStateInfo.get(i)[j])
                    {
                        continue mainloop;
                    }
                }
            }

            // If this point has been reached, then two identical plantStateInfos have been found 
            return true;
        }

        return false;
    }
    
    private ArrayList<int[]> getAllBooleanVarCombinations(int length, ArrayList<int[]> combinationList)
    {                
        ArrayList<int[]> newCombinationList = new ArrayList<int[]>();

        if (length == 0)
        {
            return newCombinationList;
        }
        
        if (combinationList == null)
        {
            combinationList = new ArrayList<int[]>();
            
            int[] newElement = new int[length];
            for (int i = 0; i < newElement.length; i++)
            {
                newElement[i] = -1;
            }
            combinationList.add(newElement);
        }
        
        for (Iterator<int[]> it = combinationList.iterator(); it.hasNext();)
        {
            int[] element = it.next();
            for (int i = 0; i < element.length; i++)
            {
                if (element[i] == -1)
                {
                    element[i] = 0;
                    newCombinationList.add(element);
                    
                    int[] dualElement = new int[length];
                    for (int j = 0; j < dualElement.length; j++)
                    {
                        dualElement[j] = element[j];
                    }
                    dualElement[i] = 1;
                    newCombinationList.add(dualElement);
                    
                    break;
                }
            }
        }
        
        if (newCombinationList.get(0)[length-1] != -1)
        {
            return newCombinationList;
        }
        
        return getAllBooleanVarCombinations(length, newCombinationList);
    }
}

/**
 * Encapsulates two int[]-objects and creates a unique hash code for this 
 * combination. Used as a hashtable key (in internalPrecTable) to distinguish 
 * between different orderings of precedence variables.
 */
class IntKey
{
    int[] firstInt, secondInt;
    IntKey(int[] firstInt, int[] secondInt)
    {
        this.firstInt = firstInt;
        this.secondInt = secondInt;        
    }

    public int hashCode()
    {
        return 31*firstInt.hashCode() + secondInt.hashCode();
    }
    
    /**
     * Compares two IntKey-objects and returns true if they contain identical 
     * int[]-instances (thus two IntKeys can differ although  the elements in 
     * the underlying int[]'s are identical). 
     *
     * @param   obj the (hopefully IntKey) object to be compared with
     * @return  true if the underlying int[]-instances are identical.
     */
    public boolean equals(Object obj)
    {
        if (! (obj instanceof IntKey))
        {
            return false;
        }

        IntKey intKey = (IntKey) obj;
        if (firstInt.equals(intKey.getFirstInt()) && secondInt.equals(intKey.getSecondInt()))
        {
            return true;
        }
        
        return false;
    }
    
    public int[] getFirstInt()
    {
        return firstInt;
    }
    public int[] getSecondInt()
    {
        return secondInt;
    }
}

/**
 * This class represents an internal precedence variable that is in MILP-
 * formulation a boolean of type "r_x1_st_y1_before_r_x2_y2". This class
 * contains the name of the variable as it appears in the MILP-formulation and 
 * its (unique) index in the array of internal precedence variables 
 * (currVariableCombination or currBoolCombination), that is used
 * to contruct constraints forbidding impossible or unused combinations. 
 */
class InternalPrecVariable
{
    /** The name of the internal precedence variable. */
    private String name;
    /** The index of the variable in the array of internal precedence variables. */
    private int index;
    
    InternalPrecVariable(String name, int index)
    {
        this.name = name;
        this.index = index;
    }
    
    String getName()
    {
        return name;
    }
    
    int getIndex()
    {
        return index;
    }
}

/** 
 * This class enables comparison between two int[]-objects, being equal if and 
 * only if all elements of the objects are identical. Otherwise, the object with 
 * the smallest leftmost element is labeled as smaller. If the leftmost elements
 * are identical, the next-leftmost elements are considered, etc. The comparator
 * implemented in this class is used to order the elements of the BooleanCombinationTreeSet.
 */
class IntArrayComparator
	implements Comparator<int[]>
{
    /**
     * Returns the value of the first element that differ between the 
     * supplied int[]-objects (firstNode[i] - secondNode[i]). If all elements 
     * are identical, zero is returned.
     *
     * @param   firstNode   the first int[]-object
     * @param   secondNode  the second int[]-object
     * @return  the difference between the first element that has not the same 
     *          value in the supplied int[]-objects, i.e. firstNode[i] - secondNode[i], 
     *          where firstNode[j] = secondNode[j] forall j < i.
     */
    public int compare(int[] firstNode, int[] secondNode)
    {
        for (int i = 0; i < firstNode.length; i++)
        {
            int currDiff = firstNode[i] - secondNode[i];
            if (currDiff != 0)
            {
                return currDiff;
            }
        }

        return 0;
    }
}

/**
 * This class extends a TreeSet to order int[]-objects that represent boolean
 * combinations of internal precedence variables. Only the combination of 
 * variables used in the current precedence specification are added to this 
 * BooleanCombinationTreeSet. The int[]-objects that serve as input to this tree 
 * consist of values in {-1, 0, 1}, while the int[]-objects stored in the tree 
 * correspond to possible combinations of boolean internal precedence variables, 
 * that is they can only contain 0's or 1's. The value of -1 means that the 
 * corresponding internal precedence variable may be of any value. Thus, each 
 * -1-value is converted into two possibilities (0 and 1) and the number of int[]-
 * objects added to the tree is multiplied by two. 
 */
class BooleanCombinationTreeSet
        extends TreeSet<int[]>
{
    /**
     * Creates a TreeSet using {@link IntArrayComparator} to order int[]-objects.
     */
    BooleanCombinationTreeSet()
    {
        super(new IntArrayComparator());
    }
    
    /**
     * Adds the supplied combination of variables to an array and calls 
     * {@link #addArray(ArrayList) addArray} 
     * to add all combinations that do not negate this int[]-object to the 
     * BooleanCombinationTreeSet.
     *
     * @param   boolCombination the int[]-object representing a combination of
     *                          internal precedence variable values.
     * @return  true    if an object was added;
     *          false   otherwise.
     */
    public boolean add(int[] boolCombination)
    {
        ArrayList<int[]> combinationsToBeAdded = new ArrayList<int[]>();
        combinationsToBeAdded.add(boolCombination);
        
        return addArray(combinationsToBeAdded);
    }

    /**
     * The value of -1 in any int[]-object supplied to this method means that the 
     * corresponding internal precedence variable may be of any value. Thus, this
     * method finds all -1-values in the current combination of boolean variables 
     * replaces them with 0's and 1's, by copying the corresponding int[]-object,
     * add the new objects to the array and calls itself. When no -1-values are
     * found, all int[]-objects in the array (that is all variable combinations
     * that do not negate the original one) are added to the BooleanCombinationTreeSet.
     *
     * @param  combinationsToBeAdded    an array list containing int[]-objects that 
     *                                  represent variable combinations that should 
     *                                  be added to the tree.
     * @return  true    if an object was added;
     *          false   otherwise.                
     */
    private boolean addArray(ArrayList<int[]> combinationsToBeAdded)
    {
        ArrayList<int[]> newCombinationsToBeAdded = new ArrayList<int[]>();
        for (int i = 0; i < combinationsToBeAdded.get(0).length; i++)
        {
            if (combinationsToBeAdded.get(0)[i] == -1)
            {
                for (Iterator<int[]> it = combinationsToBeAdded.iterator(); it.hasNext();)
                {
                    int[] currCombination = it.next();
                    
                    currCombination[i] = 0;
                    newCombinationsToBeAdded.add(currCombination);
                    
                    int[] newCombination = new int[currCombination.length];
                    for (int j = 0; j < currCombination.length; j++)
                    {
                        newCombination[j] = currCombination[j];
                    }
                    newCombination[i] = 1;
                    newCombinationsToBeAdded.add(newCombination);
                }
                
                return addArray(newCombinationsToBeAdded);
            }
        }
        
        boolean elementAdded = false;
        for (Iterator<int[]> it = combinationsToBeAdded.iterator(); it.hasNext();)
        {
            int[] currCombination = it.next();
            if (super.add(currCombination))
            {
                elementAdded = true;
            }
        }
        
        return elementAdded;
    }
}