package org.supremica.automata.algorithms.scheduling.milp;

import java.util.*;
import java.io.*;

import org.supremica.automata.*;
import org.supremica.automata.algorithms.*;
import org.supremica.automata.algorithms.scheduling.*;
import org.supremica.util.ActionTimer;

//TODO (always): Structure the code. Comment better.
public class Milp
        implements Scheduler
{
    /****************************************************************************************/
    /*                                 VARIABLE SECTION                                     */
    /****************************************************************************************/
    
    private static final int NO_PATH_SPLIT_INDEX = -1;
    
    /** A big enough value used by the MILP-solver (should be greater than any time-variable). */
    public static final int BIG_M_VALUE = 1000;
    
    /**
     *  The safety buffer between unbooking and booking, used in MILP. To use the
     *  automatic deduction of epsilon from the optmal time values in
     *  {@link buildScheduleAutomaton}, it should be a power of 10. For correct
     *  functioning, this variable should be strictly smaller than 10^(-x), where
     *  x is the total number of (individual) plalnt states.
     */
    public static final double EPSILON = 0.001;
    
    /** The involved automata, plants, zone specifications */
    private Automata theAutomata, plants, zones, externalSpecs;
    
    /**
     * The bridge to a MILP-solver, allowing to:
     * 1) build appropriate MILP models from the info supplied by this class;
     * 2) launch the MILP-solver;
     * 3) process the solution retrieving event occurrence times.
     */
    private MilpSolverUI milpSolver = null;
    
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
    
//    /** The optimal cycle time (makespan) */
//    private double makespan;
//
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
    
    /** Decides if the schedule should be built */
    protected boolean buildSchedule;
    
    /** The output string */
    private String outputStr = "";
    
    /**
     * Contains the info about the nearest path splits above a given state. The
     * indices of the IntArrayTreeSet (this storage format is chosen to avoid
     * storage of repetetive entries) are [plantsArrayIndex, stateIndex], while the
     * objects contained in the arrays contain pointers to the
     * [plantIndex, fromStateIndex, toStateIndex]-objects stored in altPathVariablesTree.
     */
    private IntArrayTreeSet[][] upstreamsAltPathVars = null;
    
    /** The array of strings containing alternative paths variables */
    private ArrayList<String> altPathVariablesStrArray = null; //TO BE DEPRECATED
    private IntArrayTreeSet altPathVariablesTree = null;
    
    /** The string containing mutex variables */
    private ArrayList<String> mutexVariables = null;
    
    /**
     * The declaration of variables, used in internal ordering of precedence constraints.
     **/
    private ArrayList<String> internalPrecVariables = null; //TO BE DEPRECATED
    /**
     * These variables describe the internal event ordering in the external 
     * precedence specifications. For example, suppose that we have a specification 
     * stating that first sigma_a or sigma_b should happen, followed by 
     * sigma_c or sigma_d. Then two variables are needed, namely 
     * 'sigma_a_before_sigma_b' and 'sigma_c_before_sigma_d'. These are stored 
     * as ordered eventIndices, e.g. [sigma_a, sigma_b] where 
     * indexMap.getEventIndex(sigma_a) < indexMap.getEventIndex(sigma_b).
     * (IntArrayTreeSet is used to avoid variable repetition). 
     **/
    private IntArrayTreeSet externalPrecVariables = null;
    
    /**
     * The ArrayList containing the cycle time constraints.
     * Each constraint is stored as an int[] containing {timePlantIndex, stateIndex}.
     */
    private ArrayList<int[]> cycleTimeConstraints = null;
    
    /**
     * The ArrayList containing initial (precedence) constraints.
     * Each constraint is stored as an int[] containing {timePlantIndex, stateIndex}.
     */
    private ArrayList<int[]> initPrecConstraints = null;
    
    /**
     * The ArrayList containing precedence constraints.
     * Each constraint is stored as an int[] containing {timeIndex, fromStateIndex, toStateIndex}.
     */
    private ArrayList<int[]> precConstraints = null;
    
    /**
     * The ArrayList containg alternative paths constraints.
     * Each constraint has a body and an id. The body is a string describing
     * the mathematical formulation of the constraint, while the id is an int-array
     * containing either:
     * [plant_time_index, from_state_index, to_state_index] - normal alt.paths inequality;
     * [plant_time_index, from_state_index] - sum of alt.paths constraints <= 1.
     */
    private ArrayList<Constraint> altPathsConstraints = null;
    
    /**
     * The ArrayList containing mutex constraint objects.
     * Each constraint has a body and an id. The body is a string describing
     * the mathematical formulation of the constraint, while the id is an int-array
     * containing [zone_index, first_plant_index, second_plant_index, repeated_booking_index],
     * which makes it possible to create unique headers for this constraint in the
     * language specific to some MILP-solver.
     */
    private ArrayList<Constraint> mutexConstraints = null;
    
    private ArrayList<ArrayList<int[]>> xorConstraints = null;
    
    private ArrayList<ArrayList<ArrayList<ArrayList<int[]>>>> sharedEventConstraints = null;
    
    /**
     * Contains the minimal state times of each plant,
     * indexed as [milpPlantIndex, stateIndex].
     */
    private double[][] deltaTimes = null;
    
    /** The constraints represented by external specifications, in string form. */
    private String externalConstraints = "";
    
    /**
     * The constraints preventing circular wait situations du to cross-booking of 
     * zones. A simple example of this is when R_i first books Z_k and then Z_l 
     * (without unboking Z_k), while R_j does the reverse (Z_l -> Z_k), 
     * the robot robot ordering should then be the same for Z_k and
     * Z_l to avoid deadlock. This is taken care of during the optimization, but
     * with the help of these constraints, the search field is narrowed, which
     * should decrease the running time. Also, approximative solutions are easier
     * to obtains using these constraints. 
     * 
     * Each CircularWaitConstraintBlock contains a list of int[]-objects and a boolean.
     * The boolean keeps track of whether any buffer corresponds to the current
     * constraint, while each int[] consists of the following indices: 
     * [plant1, plant2, zone, bookingTicPlant1, bookingTicPlant2].
     */
    private ArrayList<CircularWaitConstraintBlock> circularWaitConstraints = null;
    
    /*
     * The thread that performs the search for the optimal solution
     */
    private Thread milpThread;
    
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
    
    //TODO: totally ugly (snabb fix): needs to be redone! (se nedan)
    private ArrayList<int[]>[] consecutiveBookingEdges;
    
    protected String infoMsgs = "";
    protected String warnMsgs = "";
    protected String errorMsgs = "";
    protected ArrayList<StackTraceElement[]> debugMsgs = new ArrayList<StackTraceElement[]>();
    
    protected boolean balanceVelocities = false;
    
    /****************************************************************************************/
    /*                                 CONSTUCTORS                                          */
    /****************************************************************************************/
    
    public Milp(Automata theAutomata, boolean buildSchedule)
    throws Exception
    {
        this(theAutomata, buildSchedule, SchedulingConstants.MILP_GLPK);
    }
    
    public Milp(Automata theAutomata, boolean buildSchedule, String milpSolverName)
        throws Exception
    {
        this(theAutomata, buildSchedule, milpSolverName, false);
    }
    
    public Milp(Automata theAutomata, boolean buildSchedule, boolean balanceVelocities)
    throws Exception
    {
        this(theAutomata, buildSchedule, SchedulingConstants.MILP_GLPK, balanceVelocities);
    }
    
    public Milp(Automata theAutomata, boolean buildSchedule, String milpSolverName, boolean balanceVelocities)
    throws Exception
    {
        this.theAutomata = theAutomata;
        this.buildSchedule = buildSchedule;
        this.balanceVelocities = balanceVelocities;
        
        if (milpSolverName.equals(SchedulingConstants.MILP_GLPK))
        {
            milpSolver = new GlpkUI(this);
        }
        else if (milpSolverName.equals(SchedulingConstants.MILP_CBC))
        {
            milpSolver = new CbcUI(this);
        }
        else
        {
            throw new Exception(milpSolverName + " = is not a known MILP-solver.");
        }
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
            
            if (isRunning)
            {
                requestStop(true);
            }
            else
            {
                errorMsgs += "Scheduling interrupted";
            }
        }
        catch (Exception ex)
        {
            //milpSolver.cleanUp();
            
            isRunning = false;
            
            errorMsgs += "Milp::schedule() -> " + ex.getMessage();
            debugMsgs.add(ex.getStackTrace());
            ex.printStackTrace();//temp
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
        long totalTime = 0;
                      
        if (isRunning)
        {
            timer.restart();
            initialize();
        }
        
        // Converts automata to constraints that the MILP-solver takes as input (*.mod file)
        if (isRunning)
        {      
            createExternalConstraints();
            createBasicConstraints();
            
            //new... test... (should be called when mutex constraints already are created)
            //TODO: better name... better implementation...
            ActionTimer at = new ActionTimer();
            at.start();
            createCircularWaitConstraints();
            at.stop();
            addToMessages("Time for the creation of noncrossbooking constraints = " + 
                    at.elapsedTime() + "ms", SchedulingConstants.MESSAGE_TYPE_INFO);
           
            
            milpSolver.createModelFile();
            
            long procTime = timer.elapsedTime();
            addToMessages("\tPre-processing time = " + procTime + "ms\n", SchedulingConstants.MESSAGE_TYPE_INFO);
            totalTime += procTime;
        }
        
        // Calls the MILP-solver
        if (isRunning)
        {
            timer.restart();
            
            milpSolver.launchMilpSolver();
            
            long procTime = timer.elapsedTime();
            infoMsgs += "\tOptimization time = " + procTime + "ms\n";
            totalTime += procTime;
        }
        
        // Processes the output from the MILP-solver (*.sol file) and stores the optimal times for each state
        if (isRunning)
        {
            timer.restart();
            
            milpSolver.processSolutionFile();
            String t = "";
        }
        
        // Builds the optimal schedule (if solicited)
        if (isRunning && buildSchedule)
        {
            buildScheduleAutomaton();

            long procTime = timer.elapsedTime();
            infoMsgs += "\tPost-processing time (incl. schedule construction) = " + procTime + "ms\n";
            totalTime += procTime;
            infoMsgs += "\tTotal time = " + totalTime + "ms\n";
        }
        
        if (isRunning && buildSchedule && balanceVelocities)
        {
            balanceVelocities();
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
        //TODO: temp (fulhack) - fixa b�ttre schemabygge.
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
        //timer.stop(); // Stop the timer while waiting for the user to enter the name of the new schedule
        schedule = new Automaton("Schedule");
        schedule.setType(AutomatonType.SUPERVISOR);
        //timer.start(); // Restart the timer
        
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
        
        // The time accumulated along the optimal path so far
        double accumulatedTime = 0;
        
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
                    double currTime = milpSolver.getOptimalTimes()[plantIndex][currComposedStateIndices[plantIndex]];
                    
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
                                if (currState.nbrOfOutgoingMultiArcs() == 1  || 
                                        milpSolver.getOptimalAltPathVariables()[plantIndex][currStateIndex][nextStateIndex])
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
            
            // Set the cost of the current state
            currScheduledState.setCost(smallestTime - accumulatedTime);
            // Update the accumulated time
            accumulatedTime = smallestTime;
            
            // Make a transition (in the synchronizer) to the state that is reachable in one step at cheapest cost
            // This state will be the next parting point in our walk
            currComposedStateIndices = stepper.step(currComposedStateIndices, currOptimalEvent);
            
            // Update the schedule automaton
            State nextScheduledState;
            
            // If all the states that build up the current state are accepting, make the composed state accepting too
            boolean isAccepting = true;
            for (int i=0; i<theAutomata.size(); i++)
            {
                if (! indexMap.getStateAt(indexMap.getAutomatonAt(i), currComposedStateIndices[i]).isAccepting())
                {
                    isAccepting = false;
                }
            }
            
            // Connect the last transition of the schedule to the initial state (that is also made marked)
            if (isAccepting)
            {
//                if (smallestTime != makespan)
//                {
//                    throw new Exception("Makespan value does NOT correspond to the cost of the final state of the schedule (sched_time = " + smallestTime + "; makespan = " + makespan + "). Something went wrong...");
//                }
                
                nextScheduledState = schedule.getInitialState();
                nextScheduledState.setAccepting(true);
                nextScheduledState.setName(nextScheduledState.getName() + ";  makespan = " + smallestTime);
            }
            else
            {
                // Add the next state to the schedule
                nextScheduledState = makeScheduleState(currComposedStateIndices);
            }
            
            // Add the transition time to the name of the state-to-be-in-the-schedule
            currScheduledState.setName(currScheduledState.getName() + ";  firing_time = " + smallestTime);
            
            if (! schedule.getAlphabet().contains(currOptimalEvent))
            {
                schedule.getAlphabet().addEvent(currOptimalEvent);
            }
            schedule.addArc(new Arc(currScheduledState, nextScheduledState, currOptimalEvent));
            
            //temp (fulhack)
//            synthState = synthState.nextState(currOptimalEvent);
            
            currScheduledState = nextScheduledState;
        }
        
        
        
//        // A dummy reset-event that returns the schedule automaton from its accepting
//        // to its initial state. Needed to describe repetitive working cycles...
//        String resetEventName = "reset";
//        while (theAutomata.getPlantAutomata().getUnionAlphabet().contains(resetEventName))
//        {
//            resetEventName += "1";
//        }
//        LabeledEvent resetEvent = new LabeledEvent(resetEventName);
//
//        // The reset event brings the schedule to its initial state...
//        schedule.getAlphabet().addEvent(resetEvent);
//        schedule.addArc(new Arc(currScheduledState, schedule.getInitialState(), resetEvent));
//
//        // ...But then also the participating plants should have this event in their initial states
//        for (Automaton plantAuto : theAutomata.getPlantAutomata())
//        {
//            plantAuto.getAlphabet().addEvent(resetEvent);
//            plantAuto.addArc(new Arc(plantAuto.getInitialState(), plantAuto.getInitialState(), resetEvent));
//        }
    }
    
    /**
     * This method create an instance of VelocityBalancer, supplying the selected 
     * automata (theAutomata) and the newly built schedule, if it exists. 
     * VelocityBalancer balances then the velocities of the selected plants.
     */
    protected void balanceVelocities()
        throws Exception
    {
        if (schedule == null)
        {
            addToMessages("Velocities were not balanced since the schedule could not be found.",
                    SchedulingConstants.MESSAGE_TYPE_ERROR);
            return;
        }
        
        Automata autosToBeBalanced = theAutomata.clone();
        autosToBeBalanced.addAutomaton(schedule);
        
        new VelocityBalancer(autosToBeBalanced, this);
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
        deltaTimes = new double[theAutomata.getPlantAutomata().size()][];
        
        altPathVariablesStrArray = new ArrayList<String>(); //TO BE DEPRECATED
        altPathVariablesTree = new IntArrayTreeSet();
        mutexVariables = new ArrayList<String>();
        internalPrecVariables = new ArrayList<String>(); //TO BE DEPRECATED
        externalPrecVariables = new IntArrayTreeSet();
        
        cycleTimeConstraints = new ArrayList<int[]>();
        initPrecConstraints = new ArrayList<int[]>();
        precConstraints = new ArrayList<int[]>();
        mutexConstraints = new ArrayList<Constraint>();
        altPathsConstraints = new ArrayList<Constraint>();
        xorConstraints = new ArrayList<ArrayList<int[]>>();
        circularWaitConstraints = new ArrayList<CircularWaitConstraintBlock>();
        sharedEventConstraints = new ArrayList<ArrayList<ArrayList<ArrayList<int[]>>>>();
        
        pathCutTable = new Hashtable<State,String>();
        
        initAutomata();
        initMutexStates();
        
        initAltPathVarTrees();
        
        milpSolver.initialize();
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
        // Making all state names coherent to avoid bugs due to inappropriate state names
        for (Iterator<Automaton> autIt = theAutomata.iterator(); autIt.hasNext(); )
        {
            Automaton auto = autIt.next();
            int counter = 0;
            for (Iterator<State> stateIt = auto.stateIterator(); stateIt.hasNext(); )
            {
                State currState = stateIt.next();
                currState.setName("q" + counter++);
            }
        }
        
        // The index map is initialized
        indexMap = new AutomataIndexMap(theAutomata);
               
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
                warnMsgs += "Specification " + spec.getName() + " has no common events with any of the plants";
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
                
                infoMsgs += "\t" + str.substring(0, str.lastIndexOf("||")) + " synthesized into " + restrictedPlant.getName() + "\n";
            }
        }
        
        // Rescale the times in plants if necessary
//        rescalePlantTimes();
                
        // Prepare the plants before scheduling (add dummy initial state if needed, remove selfloops if needed)
        SchedulingHelper.preparePlantsForScheduling(plants);
        
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
        //updateGui(theAutomata); @Deprecated
        
        // Before constructing the MILP-formulation, ensure that the plants do not contain loops 
        // (throw exception if they do)
        checkForLoops(plants);
        
//        //temp
//        for (Iterator<Automaton> autIt = plants.iterator(); autIt.hasNext();)
//        {
//            Automaton auto = autIt.next();
//            for (Iterator<State> stateIt = auto.stateIterator(); stateIt.hasNext();)
//            {
//                State state = stateIt.next();
//                addToMessages(auto.getName() + "." + state.getName() + "  -->  " + 
//                        indexMap.getStateIndex(auto, state) + ", cost = " + state.getCost(), SchedulingConstants.MESSAGE_TYPE_INFO);
//            }
//        }
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
                    
                    if (bookingStates.size() > 0)
                    {
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
                            //temp: choose one
                            createExternalPrecedenceConstraints(currSpec);
                            createExternalPrecedenceConstraintsNew(currSpec);
                            
                            //temp_test
                            
                            
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
        
        ArrayList<int[]> currXorConstraints = new ArrayList<int[]>();
        
        State currSpecState = currSpec.getInitialState();
                
        for (int i=0; i<plants.size(); i++)
        {
            Automaton currPlant = plants.getAutomatonAt(i);
            Alphabet commonActiveAlphabet = AlphabetHelpers.intersect(currPlant.getAlphabet(), currSpecState.activeEvents(false));

            // If this plant shares events with the current specification...
            if (commonActiveAlphabet.size() > 0)
            {
                // For each common event...
                for (Iterator<LabeledEvent> eventIt = commonActiveAlphabet.iterator(); eventIt.hasNext(); )
                {
                    LabeledEvent currEvent = eventIt.next();
                    // Find the state(s) in the plant where the common event can occur
                    for (Iterator <State> plantStateIt = currPlant.stateIterator(); plantStateIt.hasNext(); )
                    {
                        State currPlantState = plantStateIt.next();
                        if (currPlantState.activeEvents(false).contains(currEvent))
                        {
                            int currPlantIndex = indexMap.getAutomatonIndex(currPlant);
                            int currStateIndex = indexMap.getStateIndex(currPlant, currPlantState);
                            int currEventIndex = indexMap.getEventIndex(currEvent);
                              
                            // Find the closest alt.path-variables leading to the current plant state
                            Collection<int[]> currPathSplitVars = getActiveAltPathVars(new int[]{currPlantIndex, currStateIndex, currEventIndex});                            
                            if (currPathSplitVars.size() == 0)
                            {
                                // If this state is always reached from the initial state, add a default alt.path variable
                                currXorConstraints.add(new int[]{-1});
                            }
                            else
                            {
                                // Else, add the closest alt.path-variables
                                currXorConstraints.addAll(currPathSplitVars);
                            }
                        }
                    }
                }
            }
        }
       
        // Collect all alt.path-variables coupled with the events in this specification into one block for future use
        xorConstraints.add(currXorConstraints);
    }
    
    //TO BE INAUGURATED
    private void createExternalPrecedenceConstraintsNew(Automaton currSpec)
    {
        ArrayList<int[]>[] involvedEventInfoList = new ArrayList[2];
        for (int i = 0; i < involvedEventInfoList.length; i++)
        {
            involvedEventInfoList[i] = new ArrayList<int[]>();
        }
        
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
                for (Iterator<State> stateIt = currSpec.stateIterator(); stateIt.hasNext();)
                {
                    State specState = stateIt.next();
                    
                    // Set the index of the involvedEventInfoList-array (0 if the current events are of
                    // preceding-type and 1 if they are of following-type
                    int ieiType = 0;
                    if (! specState.isInitial())
                    {
                        ieiType = 1;
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
                                    involvedEventInfoList[ieiType].add(new int[]{
                                        indexMap.getAutomatonIndex(plant),
                                        indexMap.getStateIndex(plant, plantState),
                                        indexMap.getEventIndex(currEvent)
                                    });
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Store new external precedence variables as [sigma_a, sigma_b], 
        // where indexMap.getEventIndex(sigma_a) < indexMap.getEventIndex(sigma_b).
        for (int i = 0; i < involvedEventInfoList.length; i++)
        {
            for (int j=0; j<involvedEventInfoList[i].size()-1; j++)
            {
                for (int k=j+1; k<involvedEventInfoList[i].size(); k++)
                {
                    int jEventIndex = involvedEventInfoList[i].get(j)[2];
                    int kEventIndex = involvedEventInfoList[i].get(k)[2];
                    
                    if (j < k)
                    {
                        externalPrecVariables.add(new int[]{j, k});
                    }
                    else if (k < j)
                    {
                        externalPrecVariables.add(new int[]{k, j});
                    }
                    // else if (j == k), there should be no variable since we are then 
                    // dealing with the same event fired from another state
                }
            }
        }
        
        //temp
        for (int i = 0; i < 2; i++)
        {
            String str = currSpec.getName() + "_NEW";
            if (i == 0)
            {
                str += " (P) : ";
            }
            else
            {
                str += " (F) : ";
            }
            
            for (int[] ips : involvedEventInfoList[i])
            {
                for (int j = 0; j < ips.length - 1; j++)
                {
                    str += ips[j] + ",";
                }
                str += " ";
                for (int[] upSt : getActiveAltPathVars(ips))
                {
                    for (int j = 0; j < upSt.length; j++)
                    {
                        str += upSt[j] + ",";
                    }
                    str += " ";
                }
            }
            addToMessages(str, SchedulingConstants.MESSAGE_TYPE_INFO);
        }
        // ...to here
        
        ArrayList<int[]>[] permutations = new ArrayList[2];
        permutations[0] = getIndexPermutations(involvedEventInfoList[0].size());
        permutations[1] = permutations[0];
        if (involvedEventInfoList[0].size() != involvedEventInfoList[1].size())
        {
            permutations[1] = getIndexPermutations(involvedEventInfoList[1].size());
        }
        int maxNrOfEventPairs = Math.min(involvedEventInfoList[0].size(), involvedEventInfoList[1].size());
        
        for (int[] pPerm : permutations[0])
        {
            for (int[] fPerm : permutations[1])
            {
                String str = "Perm: ";
                for (int i = 0; i < maxNrOfEventPairs; i++)
                {
                    int[] currPEventInfo = involvedEventInfoList[0].get(pPerm[i]);
                    str += currPEventInfo[0] + " " + currPEventInfo[1];
                    for (int[] altpath : getActiveAltPathVars(currPEventInfo))
                    {
                        str += altpath[1] + " " + altpath[2] + " ";
                    }
                    str += " -> ";
                    int[] currFEventInfo = involvedEventInfoList[1].get(fPerm[i]);
                    str += currFEventInfo[0] + " " + currFEventInfo[1];
                    for (int[] altpath : getActiveAltPathVars(currFEventInfo))
                    {
                        str += altpath[1] + " " + altpath[2];
                    }
                    str += " -> ";
                }
                
                addToMessages(str, SchedulingConstants.MESSAGE_TYPE_INFO);
            }
        }
    }
    
    //TO BE DEPRECATED
    private void createExternalPrecedenceConstraints(Automaton currSpec)
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
        
        
        //temp
        //OLD
        for (ArrayList<int[]> al : allPPlantStates)
        {
            String str = currSpec.getName() + "_OLD (P) : ";
            for (int[] inal : al)
            {
                for (int i = 0; i < inal.length; i++)
                {
                    str += inal[i] + ",";
                }
                str += " ";
            }
            addToMessages(str, SchedulingConstants.MESSAGE_TYPE_INFO);
        }
        for (ArrayList<int[]> al : allFPlantStates)
        {
            String str = currSpec.getName() + "_OLD (F) : ";
            for (int[] inal : al)
            {
                for (int i = 0; i < inal.length; i++)
                {
                    str += inal[i] + ",";
                }
                str += " ";
            }
            addToMessages(str, SchedulingConstants.MESSAGE_TYPE_INFO);
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
                internalPrecVariables.add(internalPrecedenceVar);
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
                internalPrecVariables.add(internalPrecedenceVar);
            }
        }
        
        // Get every possible (precedence-)event sequence (of maximal length)
        ArrayList<ArrayList> allAlternatingPlantStatesInfo = getEveryAlternatingOrdering(allPPlantStates, allFPlantStates);
        
        //temp
        for (ArrayList<ArrayList> arr : allAlternatingPlantStatesInfo)
        {
            addToMessages("New arr", SchedulingConstants.MESSAGE_TYPE_WARN);
            for (ArrayList subarr : arr)
            {
                String str = "";
                for (Object subsub : subarr)
                {
                    int[] in = (int[]) subsub;
                    for (int i = 0; i < in.length; i++)
                    {
                        str += in[i] + " ";
                    }
                    str += "; ";
                }
                addToMessages(str, SchedulingConstants.MESSAGE_TYPE_WARN);
            }
        }
        
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
     *  Calls {@link #findNearestPathSplits(Automaton, State, ArrayList<int[]>, LabeledEvent)}, thus
     *  initiating search for the path splits on all transitions leading to state.
     *
     *
     * @param auto the automaton in which path splits may appear.
     * @param state the state above which path splits are searched for.
     * @param altPathVariables the  ArrayList containing the indices of
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
     *  states are found. Their inidices, as provided by {@link #AutomataIndexMap}, are
     *  stored in the supplied ArrayList.
     *
     *
     * @param auto, the automaton in which path splits may appear.
     * @param altPathVariables, the ArrayList containing the indices of
     *           path split state pairs. The search is always ended by adding
     *           NO_PATH_SPLIT_INDEX to the list.
     * @param event, the event above (and including) which the search for
     *           path splits is started. If null, all transition leading to the
     *           state are considered.
     * @parem state, the state above which path splits are searched for.
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
    
    /**
     * This method creates additional constraints that (should) speed up the optimization.
     * These constraints prevent two plants from booking two zones in different order if
     * there is no possibility to unbook the zones between their bookings. For example,
     * suppose that R1 first enters Z1 and then directly books Z2 (without unbooking Z1),
     * while R2 moves in the same manner from the other direction (Z2 -> Z1). Then if
     * R1 book Z2 before R2, then it must also book Z1 before R2, and vice versa.
     * This poses additional constraint on the boolean variables:
     * R1_books_Z1_before_R2 >= R1_books_Z2_before_R2 - M*(4 - Z1_reached_by_R1 - Z2_reached_by_R1 -
     * Z1_reached_by_R2 - Z2_reached_by_R2).
     * Of course, the situation is identical if the robots book the zones in the same order,
     * the important thing here is that there is no unbooking between the zones.
     */
    private void createCircularWaitConstraints()
    throws Exception
    {
        // Connectivity-test
        ArrayList<LabeledEvent[]> bEventPairs = new ArrayList<LabeledEvent[]>();
        
        // Each entry of this map contains an ArrayList of pairs of consecutive
        // booking states together with the information about whether there is
        // a buffer between the bookings or not, e.g. [Z1_b_index Z2_b_index isBuffer].
        // These states are local to some plant, e.g. Pi, and mean that Pi
        // can book Z1 in the state corresponding to Z1_b_index whereafter Z2 is booked
        // in Z2_index at some point. If the second booking is done without Z1 beeing unbooked,
        // isBuffer is equal to 1.
        // The keys are int[]-objects of type [plant_index, first_zone_index, second_zone_index].
        TreeMap<int[], ArrayList<int[]>> consecutiveBookingTicsIndices =
                new TreeMap<int[], ArrayList<int[]>>(new IntArrayComparator());
        
        // Find and store all consecutive bookingTics, i.e. when a plant first
        // books Z_i and then Z_j without unbooking Z_i
        
        //TODO: totally ugly (snabb fix): needs to be redone! (se nedan)
        consecutiveBookingEdges = new ArrayList[zones.size()];
        for (int z = 0; z < zones.size(); z++)
        {
            consecutiveBookingEdges[z] = new ArrayList<int[]>();
        }
        
        
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
                                
                                //TODO: totally ugly (snabb fix): needs to be redone!
                                for (int i = 0; i < currConsecutiveBookingTicsIndices.size(); i++)
                                {
                                    consecutiveBookingEdges[z1].add(new int[]{z2, p,
                                    currConsecutiveBookingTicsIndices.get(i)[0],
                                    currConsecutiveBookingTicsIndices.get(i)[1],
                                    currConsecutiveBookingTicsIndices.get(i)[2]});
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Är följande gamla kommentarer? /AK - 080422
        // TODO: sigue leendo... (UNDER CONSTRUCTION...)
        // Denna for-slinga �r n�stan en upprepning av det som g�rs l�ngre ner
        // (m�ste komma h�r f�r att f� med ALLA bEventPar och inte bara de �verlappande).
        // Detta �r dock endast tillf�lligt (hoppas jag, annars strukturera om)
        // d� den nedanst�ende endast funkar f�r 2 plantor och b�r p� sikt f�rsvinna
        // (tror jag) /AK
        // Skall infon om tillst�nd vara med i bEventPairs??? /AK
//TODO: De f�ljande tre raderna skall inte vara bortkommenterade (�N) utan �r det f�r att slippa l�gga
//      till BookingPairsGraphExplorer.java i CVS-en. Vad skall g�ras h�r???
//        BookingPairsGraphExplorer graphExplorer = new BookingPairsGraphExplorer(
//            consecutiveBookingTicsIndices.keySet().toArray(new int[][]{}));
//        graphExplorer.findConnectedCycles();
        // Sanity-check...
        
        // Create a graph explorer
        ConnectedComponentsGraph cycleFinder =  
                new ConnectedComponentsGraph(consecutiveBookingEdges, plants.size());
        
        // Find and enumerate all cycles where every edge is of different color
        ArrayList<ArrayList<ConnectedComponentEdge>> rainbowCycles = cycleFinder.enumerateAllCycles();
        for (ArrayList<ConnectedComponentEdge> cycle : rainbowCycles)
        {
           CircularWaitConstraintBlock currCircularWaitConstraints = new CircularWaitConstraintBlock();
           boolean bufferInCycle = false;

           // For each edge in the current rainbow cycle
           for (int i = 0; i < cycle.size(); i++)
           {
               ConnectedComponentEdge precedingEdge;
               if (i != 0)
               {
                   precedingEdge = cycle.get(i-1);
               }
               else
               {
                   precedingEdge = cycle.get(cycle.size() - 1);
               }

               // Convert the information stored in the edge into plant-state-bookingtic-form
               int zoneIndex = cycle.get(i).getFromVertice().getVerticeIndex();
               int firstPlantIndex = precedingEdge.getColor();
               int secondPlantIndex = cycle.get(i).getColor();
               int firstTic = precedingEdge.getToTic();
               int secondTic = cycle.get(i).getFromTic();
               
               // Remember that this cycle contains buffers if one is found on any edge 
               if (cycle.get(i).getBufferExists())
               {
                   bufferInCycle = true;                    
               }

               // Add the plant-state-bookingtic-info to the current constraint block
               currCircularWaitConstraints.add(new int[]{
                       firstPlantIndex, secondPlantIndex, zoneIndex, firstTic, secondTic
                   });  
            }

            currCircularWaitConstraints.setBuffer(bufferInCycle);
            circularWaitConstraints.add(currCircularWaitConstraints);
        } 
        
// @Deprecated: t�nkte om...
//                    for (int p = 0; p < plants.size(); p++)
//                    {
//                        ArrayList<int[]> consecutiveTics = consecutiveBookingTicsIndices.get(new int[]{p, z1, z2});
//                        if (consecutiveTics != null)
//                        {
//                            for (int[] tic : consecutiveTics)
//                            {
//                                // The current booking-events-pair of p
//                                LabeledEvent[] currBEventPair = new LabeledEvent[currZoneIndices.length];
//
//                                for (int localZInd = 0; localZInd < currZoneIndices.length; localZInd++)
//                                {
//                                    int eventIndex = bookingTics[currZoneIndices[localZInd]][p][EVENT_SWITCH][tic[localZInd]];
//                                    LabeledEvent bEvent = indexMap.getEventAt(eventIndex);
//
//                                    currBEventPair[localZInd] = bEvent;
//                                }
//
//                                bEventPairs.add(currBEventPair);
//                            }
//                        }
//                    }
        
        
        
        
        
        
//        OBS!!! TILLF�LLIGT BORTKOMMENTERAT PGA BUGG (INDEX_OUT_OF_BOUNDS_EXCEPTION f�r /artiklar/AK/journal_milp/xml-filer/connected_components_abc.wmod)
//        KOMMENTERA IN IGEN (ALT. ANV�ND SOM INSPIRATION F�R ATT L�GGA IN MILP-CONSTRAINTS) VID TILLF�LLE.
//        
//        
//        
//        
//        
//        // Find the consecutive bookings of the same zone-pairs that are made by different
//        // plants. Check for (almost) all combinations of z1&z2
//        int counter = 1;
//        for (int z1 = 0; z1 < zones.size(); z1++)
//        {
//            for (int z2 = 0; z2 < zones.size(); z2++)
//            {
//                if (z1 != z2)
//                {
//                    // Used in for-loops to decrease code-repetition
//                    int[] currZoneIndices = new int[]{z1, z2};
//                    
//                    // Check for all combinations of p1 < p2 (this suffices since the mutex
//                    // variables are always of the form "pi_books_zx_before_pj", where i < j)
//                    for (int p1 = 0; p1 < plants.size() - 1; p1++)
//                    {
//                        ArrayList<int[]> consecutiveTicsP1 = consecutiveBookingTicsIndices.get(new int[]{p1, z1, z2});
//                        if (consecutiveTicsP1 != null)
//                        {
//                            // If p1 books z1 and then z2 consecutively (consecutiveTicsP1 = non_null), then
//                            // there should be a consecutive booking secuense in som plant with higher index than p1...
//                            for (int p2 = p1 + 1; p2 < plants.size(); p2++)
//                            {
//                                // To check both for the booking z1 -> z2 and z2 -> z1, a new index variable (zInd)
//                                // is needed
//                                for (int zInd = 0; zInd < currZoneIndices.length; zInd++)
//                                {
//                                    // If also p2 books the same zones consecutively, i.e. no unbooking between
//                                    // z1 and z2, then constraint construction begins...
//                                    ArrayList<int[]> consecutiveTicsP2 = consecutiveBookingTicsIndices.get(
//                                            new int[]{p2, currZoneIndices[zInd], currZoneIndices[1 - zInd]});
//                                    if (consecutiveTicsP2 != null)
//                                    {
//                                        for (int[] tic1 : consecutiveTicsP1)
//                                        {
//                                            // Find nearest upwards path splits for each consecutive booking of p1
//                                            ArrayList<int[]> pathSplitInfosP1 = new ArrayList<int[]>();
//                                            for (int localZInd = 0; localZInd < currZoneIndices.length; localZInd++)
//                                            {
//                                                int stateIndex = bookingTics[currZoneIndices[localZInd]][p1][STATE_SWITCH][tic1[localZInd]];
//                                                int eventIndex = bookingTics[currZoneIndices[localZInd]][p1][EVENT_SWITCH][tic1[localZInd]];
//                                                State bState = indexMap.getStateAt(plants.getAutomatonAt(p1), stateIndex);
//                                                LabeledEvent bEvent = indexMap.getEventAt(eventIndex);
//                                                
//                                                findNearestPathSplits(plants.getAutomatonAt(p1), bState.nextState(bEvent),
//                                                        pathSplitInfosP1, bEvent);
//                                            }
//                                            
//                                            // Create path split variables from the path split indices (for p1)
//                                            String pathSplitStrP1 = "";
//                                            int pathSplitCounterP1 = 0;
//                                            for (Iterator<int[]> it = pathSplitInfosP1.iterator(); it.hasNext();)
//                                            {
//                                                int[] pathSplitInfo = it.next();
//                                                if (pathSplitInfo[0] != NO_PATH_SPLIT_INDEX)
//                                                {
//                                                    pathSplitCounterP1++;
//                                                    pathSplitStrP1 += " - " + makeAltPathsVariable(p1, pathSplitInfo[0], pathSplitInfo[1]);
//                                                }
//                                            }
//                                            
//                                            // Create path split variables for p2 and write the consecutive
//                                            // booking constraints (primal and dual) to the MILP-file
//                                            String nonCrossConstrStr = "";
//                                            String dualNonCrossConstrStr = "";
//                                            ArrayList<int[]> pathSplitInfosP2 = new ArrayList<int[]>();
//                                            for (int[] tic2 : consecutiveTicsP2)
//                                            {
//                                                
//                                                for (int localZInd = 0; localZInd < currZoneIndices.length; localZInd++)
//                                                {
//                                                    // This index is the combination of localZInd and zInd, ensuring that the tic2-elements
//                                                    // used below correspond to the tic1[localZInd] (that is tic2[correspondingZInd] represents
//                                                    // the same zone as tic1[localZInd]).
//                                                    int correspondingZInd = (int)Math.IEEEremainder(zInd + localZInd, 2);
//                                                    
//                                                    nonCrossConstrStr += "r" + p1 + "_books_z" + currZoneIndices[localZInd] + "_before_r" + p2;
//                                                    dualNonCrossConstrStr += "r" + p1 + "_books_z" + currZoneIndices[1 - localZInd] + "_before_r" + p2;
//                                                    
//                                                    // If there is a "var_x"-appendix corresponding to the current variable, it should be appended
//                                                    Integer varCounter = mutexVarCounterMap.get(new int[]{currZoneIndices[localZInd], p1, p2,
//                                                    tic1[localZInd], tic2[correspondingZInd]});
//                                                    if (varCounter != null)
//                                                    {
//                                                        nonCrossConstrStr += "_var" + varCounter;
//                                                    }
//                                                    // If there is a "var_x"-appendix corresponding to the dual variable, it should be appended
//                                                    varCounter = mutexVarCounterMap.get(new int[]{currZoneIndices[1 - localZInd], p1, p2,
//                                                    tic1[1 - localZInd], tic2[1 - correspondingZInd]});
//                                                    if (varCounter != null)
//                                                    {
//                                                        dualNonCrossConstrStr += "_var" + varCounter;
//                                                    }
//                                                    
//                                                    if (! nonCrossConstrStr.contains(">="))
//                                                    {
//                                                        nonCrossConstrStr = "consecutive_booking_" + counter + " : " + nonCrossConstrStr + " >= ";
//                                                        dualNonCrossConstrStr = "consecutive_booking_" + counter++ + "_dual : " + dualNonCrossConstrStr + " >= ";
//                                                    }
//                                                    
//                                                    // Find nearest upwards path splits for each consecutive booking of p2
//                                                    int stateIndex = bookingTics[localZInd][p2][STATE_SWITCH][tic2[correspondingZInd]];
//                                                    int eventIndex = bookingTics[localZInd][p2][EVENT_SWITCH][tic2[correspondingZInd]];
//                                                    State bState = indexMap.getStateAt(plants.getAutomatonAt(p2), stateIndex);
//                                                    LabeledEvent bEvent = indexMap.getEventAt(eventIndex);
//                                                    
//                                                    findNearestPathSplits(plants.getAutomatonAt(p2), bState.nextState(bEvent),
//                                                            pathSplitInfosP2, bEvent);
//                                                }
//                                                
//                                                // Create path split variables from the path split indices (for p2)
//                                                int pathSplitCounterP2 = 0;
//                                                String pathSplitStrP2 = "";
//                                                for (Iterator<int[]> it = pathSplitInfosP2.iterator(); it.hasNext();)
//                                                {
//                                                    int[] pathSplitInfo = it.next();
//                                                    if (pathSplitInfo[0] != NO_PATH_SPLIT_INDEX)
//                                                    {
//                                                        pathSplitCounterP2++;
//                                                        pathSplitStrP2 += " - " + makeAltPathsVariable(p2, pathSplitInfo[0], pathSplitInfo[1]);
//                                                    }
//                                                }
//                                                
//                                                // Add the path split variables to the newly created constraints
//                                                if ((pathSplitCounterP1 + pathSplitCounterP2) > 0)
//                                                {
//                                                    nonCrossConstrStr += " - bigM*(" + (pathSplitCounterP1 + pathSplitCounterP2) +
//                                                            pathSplitStrP1 + pathSplitStrP2 + ")";
//                                                    dualNonCrossConstrStr += " - bigM*(" + (pathSplitCounterP1 + pathSplitCounterP2) +
//                                                            pathSplitStrP1 + pathSplitStrP2 + ")";
//                                                }
//                                                
//                                                // Add the constraints to the constraint collection
//                                                nonCrossbookingConstraints += nonCrossConstrStr + ";\n";
//                                                nonCrossbookingConstraints += dualNonCrossConstrStr + ";\n";
//                                            }
//                                        }
//                                        
//                                        //TODO: Installera och lek med RobotStudio
//                                        //TODO: Varf�r blir g*_suboptimal = 0 f�r FT6x6???
//                                        //      ... jo, f�r att den inte hittar ngn l�sning.
//                                        //      1) connected components beh�ver fixas;
//                                        //      2) path-variablerna �r ju ocks� connectade... fixa.
//                                        //TODO: hyfsa till RandomPathUsingMilp.java;
//                                        //      man beh�ver kanske inte det systematiska variabelvalet.
//                                        //      Det �r nog s� om alla robotar b�rjar utanf�r zoner.
//                                        //      Annars fundera...
//                                        //TODO: dokumentera i /tankar/milp.tex
//                                        //TODO: VelocityBalansering
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
        
//        BookingPairsGraphExplorer graphExplorer = new BookingPairsGraphExplorer(bEventPairs);
//        graphExplorer.findConnectedCycles();
    }
    
    private void initAltPathVarTrees()
    {
        upstreamsAltPathVars = new IntArrayTreeSet[plants.size()][];
        
        for (int i = 0; i < plants.size(); i++)
        {
            Automaton plant = plants.getAutomatonAt(i);
            
            upstreamsAltPathVars[i] = new IntArrayTreeSet[plant.nbrOfStates()];
            for (int j = 0; j < upstreamsAltPathVars[i].length; j++)
            {
                upstreamsAltPathVars[i][j] = new IntArrayTreeSet();
            }
            
            ArrayList<State> uncheckedStates = new ArrayList<State>();
            uncheckedStates.add(plant.getInitialState());
            while (! uncheckedStates.isEmpty())
            {
                State state = uncheckedStates.remove(0);
                int currStateIndex = indexMap.getStateIndex(plant, state);
                if (state.nbrOfOutgoingMultiArcs() == 1)
                {
                    State nextState = state.nextStateIterator().next();
                    uncheckedStates.add(nextState);
                    
                    int nextStateIndex = indexMap.getStateIndex(plant, nextState);
                    upstreamsAltPathVars[i][nextStateIndex].addAll(upstreamsAltPathVars[i][currStateIndex]);
                }
                else if (state.nbrOfOutgoingMultiArcs() > 1)
                {
                    for (Iterator<State> stateIt = state.nextStateIterator(); stateIt.hasNext();)
                    {
                        State nextState = stateIt.next();
                        int nextStateIndex = indexMap.getStateIndex(plant, nextState);
                        
                        uncheckedStates.add(nextState);
                        
                        // If the variable representing current path split has not yet been encoutered,
                        // store it.
                        altPathVariablesTree.add(new int[]{
                            indexMap.getAutomatonIndex(plant), currStateIndex, nextStateIndex});
                        
                        // Get the pointer to the current alt path variable and store it in the upstreamsAltPathVars
                        upstreamsAltPathVars[i][nextStateIndex].add(altPathVariablesTree.get(new int[]{
                            indexMap.getAutomatonIndex(plant), currStateIndex, nextStateIndex}));
                    }
                }
            }
        }
    }
    
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
        
        // This array holds true values (i.e. =1) for each examined state that has a
        // pZone-unbooking event in the trace to the fZone-booking.
        int[] uEventFoundDownstreams = new int[plant.nbrOfStates()];
        
        // For faster processing, the booking tics of the preceding zone are put into a hashtable
        Hashtable<Integer, Integer> pBStateIndicesTable = new Hashtable<Integer, Integer>();
        for (int i = 0; i < bookingTics[pZoneIndex][plantIndex][STATE_SWITCH].length; i++)
        {
            pBStateIndicesTable.put(new Integer(bookingTics[pZoneIndex][plantIndex][STATE_SWITCH][i]), new Integer(i));
        }
        
        // Find the events that are common to the current plant and pZone
        Alphabet pCommonAlphabet = AlphabetHelpers.intersect(
                plants.getAutomatonAt(plantIndex).getAlphabet(),
                zones.getAutomatonAt(pZoneIndex).getAlphabet());
        // Find the events in the plant that unbook pZone
        Alphabet pUnbookingAlphabet = AlphabetHelpers.minus(pCommonAlphabet,
                zones.getAutomatonAt(pZoneIndex).getInitialState().activeEvents(false));
        // The remaining common events book pZone
        Alphabet pBookingAlphabet = AlphabetHelpers.minus(pCommonAlphabet, pUnbookingAlphabet);
        // All booking events except for the ones that book pZone or fZone (move outside the method)
        // TODO: Move the method to an egen class
        // TODO: Make this method more efficient (do we have to call it 1.000.000 times with (p1,z1,z2)?
        //       Or could it be enough to call with (p)?
        Alphabet remainingBookingAlphabet = new Alphabet();
        for (int i = 0; i < zones.size(); i++)
        {
            if (i != pZoneIndex && i != fZoneIndex)
            {
                remainingBookingAlphabet = AlphabetHelpers.union(remainingBookingAlphabet,
                        zones.getAutomatonAt(i).getInitialState().activeEvents(true));
            }
        }
        remainingBookingAlphabet = AlphabetHelpers.intersect(
                plants.getAutomatonAt(plantIndex).getAlphabet(), remainingBookingAlphabet);
        
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
                // Remove the last state in the list (important to search deepwards to avoid
                // confusion in the values of "uEventFoundDownstreams"
                State currUpstreamsState = upstreamsStates.remove(upstreamsStates.size() - 1);
                
                for (Iterator<Arc> incomingArcsIt = currUpstreamsState.incomingArcsIterator(); incomingArcsIt.hasNext();)
                {
                    Arc incomingArc = incomingArcsIt.next();
                    
                    // If the incoming event does not unbook pZone...
                    if (!pUnbookingAlphabet.contains(incomingArc.getEvent()))
                    {
                        int currStateIndex = indexMap.getStateIndex(plant, incomingArc.getFromState());
                        
                        // Propagate the information about the pZone-unbooking event
                        uEventFoundDownstreams[currStateIndex] =
                                uEventFoundDownstreams[indexMap.getStateIndex(plant, incomingArc.getToState())];
                        
                        // If that event actually books pZone, then a consecutive sequence is added to the list
                        if (pBookingAlphabet.contains(incomingArc.getEvent()))
                        {
                            // Adding a pair of consecutive booking states (with or without a buffer in between)
                            bookingStateSequences.add(new int[]{
                                pBStateIndicesTable.get(new Integer(currStateIndex)).intValue(), i,
                                uEventFoundDownstreams[currStateIndex]});
                        }
                        // Otherwise, if we neither find unbooking of pZone nor booking of any other zone,
                        // the search is continued by adding all states that lead to the current state.
                        // By checking for the unbooking of pZone, we avoid sequences like "Ri_bj -> Ri_uj -> Ri_bk",
                        // that do not comply with the definition of "consecutive booking".
                        else if (!remainingBookingAlphabet.contains(incomingArc.getEvent()))
                        {
                            upstreamsStates.add(incomingArc.getFromState());
                        }
                    }
                    else
                    {
                        // An unbooking event was found which must be recorded
                        uEventFoundDownstreams[indexMap.getStateIndex(plant, incomingArc.getFromState())] = 1;
                        
                        upstreamsStates.add(incomingArc.getFromState());
                    }
                }
            }
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
    protected void createBasicConstraints()
    throws Exception
    {
        int nrOfPlants = plants.size();
        int nrOfZones = zones.size();
        
        // This alphabet is needed to check which events are shared by the plants.
        // The firing times of corresponding transitions should be equal for each shared event.
        // Also, the altPathVariableValues leading to such transitions should agree.
        Alphabet sharedPlantAlphabet = new Alphabet();
        for (int i=0; i<plants.size() - 1; i++)
        {
            Alphabet firstAlphabet = plants.getAutomatonAt(i).getAlphabet();
            
            for (int j=i+1; j<plants.size(); j++)
            {
                Alphabet secondAlphabet = plants.getAutomatonAt(j).getAlphabet();
                
                sharedPlantAlphabet.addEvents(AlphabetHelpers.intersect(firstAlphabet, secondAlphabet));
            }
        }
        
        // Each value of this map contains a list of time variables corresponding to a shared event 
        // (the keys of the map are the event names).
        Hashtable<LabeledEvent, IntArrayTreeSet> sharedEventTimeVarsMap = new Hashtable<LabeledEvent,IntArrayTreeSet>();
        
        for (int i=0; i<nrOfPlants; i++)
        {
            Automaton currPlant = plants.getAutomatonAt(i);
            int currPlantIndex = indexMap.getAutomatonIndex(currPlant);
            
            deltaTimes[i] = new double[currPlant.nbrOfStates()];
            for (Iterator<State> stateIter = currPlant.stateIterator(); stateIter.hasNext(); )
            {
                State currState = stateIter.next();
                
                int currStateIndex = indexMap.getStateIndex(currPlant, currState);
                
                deltaTimes[i][currStateIndex] = currState.getCost();
                
                // If the current state has successors and is not initial, add precedence constraints
                // If the current state is initial, add an initial (precedence) constraint
                int nbrOfOutgoingMultiArcs = currState.nbrOfOutgoingMultiArcs();
                if (nbrOfOutgoingMultiArcs > 0)
                {
                    for (Iterator<MultiArc> multiArcIt = currState.outgoingMultiArcIterator(); multiArcIt.hasNext();)
                    {
                        MultiArc multiArc = multiArcIt.next();
                        for (LabeledEvent ev : multiArc.getEvents())
                        {
                            if (sharedPlantAlphabet.contains(ev))
                            {
                                IntArrayTreeSet sharedVarList = sharedEventTimeVarsMap.get(ev);
                                if (sharedVarList == null)
                                {
                                    sharedVarList = new IntArrayTreeSet();
                                }
                                sharedVarList.add(new int[]{i, currStateIndex});
                                sharedEventTimeVarsMap.put(ev, sharedVarList);
                            }
                        }
                    }
                    
                    if (currState.isInitial())
                    {
                        initPrecConstraints.add(new int[]{i, currStateIndex});
                    }
                    
                    Iterator<State> nextStates = currState.nextStateIterator();
                    
                    // If there is only one successor, add a precedence constraint
                    if (nbrOfOutgoingMultiArcs == 1)
                    {
                        State nextState = nextStates.next();
                        int nextStateIndex = indexMap.getStateIndex(currPlant, nextState);
                        
                        precConstraints.add(new int[]{i, currStateIndex, nextStateIndex});
                    }
//                     // If there are two successors, add one alternative-path variable and corresponding constraint
//                     else if (nbrOfOutgoingMultiArcs == 2)
//                     {
//                         State nextLeftState = nextStates.next();
//                         State nextRightState = nextStates.next();
//                         int nextLeftStateIndex = indexMap.getStateIndex(currPlant, nextLeftState);
//                         int nextRightStateIndex = indexMap.getStateIndex(currPlant, nextRightState);
                    
//                         String currAltPathsVariable = "r" + currPlantIndex + "_from_" + currStateIndex + "_to_" + nextLeftStateIndex;
                    
//                         altPathVariablesStrArray.add(currAltPathsVariable);
                    
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
                        String sumConstraint = "";
                        
                        while (nextStates.hasNext())
                        {
                            State nextState = nextStates.next();
                            int nextStateIndex = indexMap.getStateIndex(currPlant, nextState);
                            
                            String currAltPathsVariable = makeAltPathsVariable(i, currStateIndex, nextStateIndex);
                            sumConstraint += currAltPathsVariable + " + ";
                            
                            altPathVariablesStrArray.add(currAltPathsVariable); //TO BE DEPRECATED
//                            altPathVariablesTree.add(new int[]{indexMap.getAutomatonIndex(currPlant),
//                                currStateIndex, nextStateIndex});
                            
                            String altPathsConstraintsBody = "time[" + i + ", " + nextStateIndex + "] >= time["
                                    + i + ", " + currStateIndex + "] + deltaTime[" + i + ", "  + nextStateIndex +
                                    "] - bigM*(1 - " + currAltPathsVariable + ") + epsilon";
                            altPathsConstraints.add(new Constraint(new int[]{i, currStateIndex, nextStateIndex},
                                    altPathsConstraintsBody));
                            
                            pathCutTable.put(nextState, "(1 - " + currAltPathsVariable + ")");
                            
                            currAlternative++;
                        }
                        
                        sumConstraint = sumConstraint.substring(0, sumConstraint.lastIndexOf("+")) + "= ";
                        
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
                                sumConstraint += makeAltPathsVariable(i, currPathSplit[0], currPathSplit[1]) + " + ";
                            }
                            else
                            {
                                sumConstraint += "1 + ";
                            }
                        }
                        sumConstraint = sumConstraint.substring(0, sumConstraint.lastIndexOf(" +"));
                        altPathsConstraints.add(new Constraint(new int[]{i, currStateIndex}, sumConstraint));
// 						}
// 						altPathsConstraints += sumConstraint.substring(0, sumConstraint.laspIndexOf("+")) + "= 1;\n";
                        
                    }
                }
                else if (currState.isAccepting())
                {
                    // If the current state is accepting, a cycle time constaint is added,
                    // ensuring that the makespan is at least as long as the minimum cycle time of this plant
                    cycleTimeConstraints.add(new int[]{i, currStateIndex});
                }
            }
        }
        
        // Add the constraints for the time variables associated with shared events
        for (LabeledEvent sharedEvent : sharedEventTimeVarsMap.keySet())
        {
            ArrayList<ArrayList<ArrayList<int[]>>> currSharedEventInfoList = new ArrayList<ArrayList<ArrayList<int[]>>>();
            
            IntArrayTreeSet sharedTimeVars = sharedEventTimeVarsMap.get(sharedEvent);
            int currCounter = 0;
            
            int prevRobotIndex = -1;
            ArrayList<ArrayList<int[]>> currSharedEventInRobotList = null;
            for (Iterator<int[]> sharedTimeVarIt = sharedTimeVars.iterator(); sharedTimeVarIt.hasNext();)
            {
                int[] sharedTimeVar = sharedTimeVarIt.next();
                if (sharedTimeVar[0] != prevRobotIndex)
                {
                    currSharedEventInRobotList = new ArrayList<ArrayList<int[]>>();
                    currSharedEventInfoList.add(currSharedEventInRobotList);
                    prevRobotIndex = sharedTimeVar[0];
                }
                
                ArrayList<int[]> currSharedEventInStateList = new ArrayList<int[]>();
                currSharedEventInRobotList.add(currSharedEventInStateList);
                
                currSharedEventInStateList.add(sharedTimeVar);
                currSharedEventInStateList.addAll(getActiveAltPathVars(new int[]{sharedTimeVar[0], sharedTimeVar[1], indexMap.getEventIndex(sharedEvent)}));
                
                
//                for (int j = i+1; j < sharedTimeVars.size(); j++)
//                {
//                    String header = "shared_event_" + sharedEvent.getLabel() + "_" + currCounter + " : ";
//                    int[] firstSharedTimeVar = sharedTimeVars.get(i);
//                    int[] secondSharedTimeVar = sharedTimeVars.get(j);
//                    System.out.println(header + "time[" + firstSharedTimeVar[0] + ", " + firstSharedTimeVar[1] + 
//                            "] = time[" + secondSharedTimeVar[0] + ", " + secondSharedTimeVar[1] + "];");
//                    
//                    //int[] firstUpstreamsAltPathInfo = upstreamsAltPathVars[sharedTimeVars.get(i)[0]][sharedTimeVars.get(i)[1]]
//                    //        int[] firstUpstreamsAltPathInfo = upstreamsAltPathVars[sharedTimeVars.get(i)[0]][sharedTimeVars.get(i)[1]]
//                    String body = "shared_event_" + sharedEvent.getLabel() + "_alt_paths_" + currCounter++ + " : ";
//                    Collection<int[]> firstAltPathInfoList = getActiveAltPathVars(new int[]{firstSharedTimeVar[0], firstSharedTimeVar[1], 
//                        indexMap.getEventIndex(sharedEvent)});
//                    Collection<int[]> secondAltPathInfoList = getActiveAltPathVars(new int[]{secondSharedTimeVar[0], secondSharedTimeVar[1], 
//                        indexMap.getEventIndex(sharedEvent)});
//                    
//                                        
//                    if (firstAltPathInfoList.size() == 0)
//                    {
//                        body += "1";
//                    }
//                    else
//                    {
//                        for (int[] altPathInfo : firstAltPathInfoList)
//                        {
//                            body += makeAltPathsVariable(altPathInfo[0], altPathInfo[1], altPathInfo[2]) + " + ";
//                        }
//                        body = body.substring(0, body.lastIndexOf("+")).trim();
//                    }
//                    body += " = ";
//                    if (secondAltPathInfoList.size() == 0)
//                    {
//                        body += "1";
//                    }
//                    else
//                    {
//                        for (int[] altPathInfo : secondAltPathInfoList)
//                        {
//                            body += makeAltPathsVariable(altPathInfo[0], altPathInfo[1], altPathInfo[2]) + " + ";
//                        }
//                        body = body.substring(0, body.lastIndexOf("+")).trim();
//                    }
//                    body += ";";
//                    System.out.println(body);
//                }
            }
            
            //TODO... fixa omtolkningen i GLPK OCH CBC
            sharedEventConstraints.add(currSharedEventInfoList);
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
                                
                                mutexVariables.add(currMutexVariable);
                                
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
                                String mutexConstraintBody = "time[" + j1 + ", " + bookingTics[i][j1][STATE_SWITCH][k1] +
                                        "] >= " + "time[" + j2 + ", " + unbookingTics[i][j2][STATE_SWITCH][k2] + "] - bigM*";
                                if (totalNrOfPathSplits > 0)
                                {
                                    mutexConstraintBody += "(" + totalNrOfPathSplits + " + " + currMutexVariable + altPathsCoupling + ")";
                                }
                                else
                                {
                                    mutexConstraintBody += currMutexVariable;
                                }
                                mutexConstraintBody += " + epsilon";
                                mutexConstraints.add(new Constraint(new int[]{i, j1, j2, repeatedBooking}, mutexConstraintBody));
                                
                                
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
                                mutexConstraintBody = "time[" + j2 + ", " + bookingTics[i][j2][STATE_SWITCH][k2] +
                                        "] >= " + "time[" + j1 + ", " + unbookingTics[i][j1][STATE_SWITCH][k1] + "] - bigM*(";
                                if (totalNrOfPathSplits > 0)
                                {
                                    mutexConstraintBody += (totalNrOfPathSplits+1) + " - " + currMutexVariable + altPathsCoupling + ")";
                                }
                                else
                                {
                                    mutexConstraintBody += "1 - " + currMutexVariable + ")";
                                }
                                mutexConstraintBody += " + epsilon";
                                mutexConstraints.add(new Constraint(new int[]{i, j1, j2, repeatedBooking}, mutexConstraintBody));
                                
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
    }
    
    public void requestStop()
    {
        requestStop(false);
    }
    
    public void requestStop(boolean disposeScheduleDialog)
    {
        isRunning = false;
        
        milpSolver.cleanUp();
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
    
//@Deprecated
//    private synchronized void updateGui(Automata autos)
//	    throws Exception
//    {
//        if (scheduleDialog != null)
//        {
//            IDEActionInterface ide = null;
//
//            try
//            {
//                ide = scheduleDialog.getIde();
//
//                // Remove old automata
//                Automata selectedAutos = ide.getIDE().getActiveDocumentContainer().getAnalyzerPanel().getSelectedAutomata();
//                for (Iterator<Automaton> autIt = selectedAutos.iterator(); autIt.hasNext(); )
//                {
//                    Automaton selectedAuto = autIt.next();
//                    if (!autos.containsAutomaton(selectedAuto.getName()))
//                    {
////                        ide.getVisualProjectContainer().getActiveProject().removeAutomaton(selectedAuto);
//                        ide.getIDE().getActiveDocumentContainer().getAnalyzerPanel().getVisualProject().removeAutomaton(selectedAuto);
//                    }
//                }
//
//                // Add missing automata
//                for (Iterator<Automaton> autIt = autos.iterator(); autIt.hasNext(); )
//                {
//                    Automaton auto = autIt.next();
//                    if (!selectedAutos.containsAutomaton(auto.getName()))
//                    {
//                        ide.getIDE().getActiveDocumentContainer().getAnalyzerPanel().addAutomaton(auto);
//                    }
//                }
//            }
//            catch (Exception ex)
//            {
//                //temp_uc
////                logger.warn("EXceptiON, ide = " + ide);
//
//                throw ex;
//            }
//        }
//    }
    
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
    
    //TO BE DEPRECATED
    public String makeAltPathsVariable(int plantIndex, int fromStateIndex, int toStateIndex)
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
    public double removeEpsilons(double time)
    {
        // Initialize roundOffCoeff if this has not been done
        if (roundOffCoeff == -1)
        {
            int totalNrOfTimes = 0;
            for (int i = 0; i < deltaTimes.length; i++)
            {
                totalNrOfTimes += deltaTimes[i].length;
            }
            roundOffCoeff = EPSILON * Math.pow(10, ("" + totalNrOfTimes).length());
        }
        
        // Remove epsilons from the current time.
        // The timeThroughBigMApprox is equal to 1 unless a rescaling has been done.
        // If the system was rescaled, the times return to their original (or rather correct) values.
        return Math.floor(time / roundOffCoeff) * roundOffCoeff * timeThroughBigMApprox;
    }
    
//    private ArrayList<int[][]> getEveryOrderingNew(ArrayList<int[]>[] involvedEventInfo)
//    {
//        int eventsInEachOrdering = Math.max(involvedEventInfo[0].size(), involvedEventInfo[1].size());
//
//    }
    
    /**
     * This method creates all permutation of indices in an array of length n.
     *
     * @param   the length of each permutation.
     * @return  a list of all permutations of length  n.
     */
    private ArrayList<int[]> getIndexPermutations(int n)
    {
        ArrayList<int[]> permutations = new ArrayList<int[]>();
        
        int[] descOrderIndex = new int[n];
        int[] descOrderDir = new int[n];
        int[] perm = new int[n];
        for (int i = 0; i < perm.length; i++)
        {
            perm[i] = i;
            descOrderIndex[i] = perm.length - (i + 1);
            descOrderDir[i] = -1;
        }
        
        int[] permToAdd = new int[perm.length];
        for (int i = 0; i < permToAdd.length; i++)
        {
            permToAdd[i] = perm[i];
        }
        permutations.add(permToAdd);
        
        boolean swapping = true;
        while (swapping)
        {
            outerForLoop: for (int i = 0; i < descOrderDir.length; i++)
            {
                // If no swapping was performed in this for-loop, it's time to stop iterating
                swapping = false;
                
                int swappableIndex = descOrderIndex[i] + descOrderDir[i];
                if (swappableIndex >= 0 && swappableIndex < n)
                {
                    if (perm[descOrderIndex[i]] > perm[swappableIndex])
                    {
                        // Swap
                        int temp = perm[swappableIndex];
                        perm[swappableIndex] = perm[descOrderIndex[i]];
                        perm[descOrderIndex[i]] = temp;
                        
                        // Add the permutation to the array
                        permToAdd = new int[perm.length];
                        for (int k = 0; k < permToAdd.length; k++)
                        {
                            permToAdd[k] = perm[k];
                        }
                        permutations.add(permToAdd);
                        
                        // Update the order indices
                        int swappableDescOrderIndex = n - (temp + 1);
                        descOrderIndex[i] += descOrderDir[i];
                        descOrderIndex[swappableDescOrderIndex] -= descOrderDir[i];
                        
                        // Change the swapping directions of all integers that are higher than
                        // the currently swapping one
                        for (int j = 0; j < i; j++)
                        {
                            descOrderDir[j] *= -1;
                        }
                        
                        // Keep on iterating
                        swapping = true;
                        
                        break outerForLoop;
                    }
                }
            }
        }
        
        return permutations;
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
    
//    //TODO: INAUGURATED
//    private ArrayList<int[]> getEveryAlternatingOrderingNew(ArrayList<int[]> toOrder1, ArrayList<int[]> toOrder2)
//    {
//        for (int i = 0; i < toOrder1.size(); i++)
//        {
//            int[] plantStateInOrder1 = toOrder1.get(i);
//            for (int j = 0; j < toOrder2.size(); j++)
//            {
//                int[] plantStateInOrder2 = toOrder2.get(j);
//                if ((plantStateInOrder1[0] == plantStateInOrder2[0]) && (plantStateInOrder1[1] == plantStateInOrder2[1]))
//                {
//                    ArrayList<int[]> newToOrder1 = new ArrayList<int[]>(toOrder1);
//                    ArrayList<int[]> newToOrder2 = new ArrayList<int[]>(toOrder2);
//                    newToOrder1.remove(i);
//                    newToOrder2.remove(j);
//
//                    ArrayList<int[]> combinedAlternativeOrderings = getEveryAlternatingOrderingNew(toOrder1, newToOrder2);
//                    combinedAlternativeOrderings.addAll(getEveryAlternatingOrderingNew(newToOrder1, toOrder2));
//                    return combinedAlternativeOrderings;
//                }
//            }
//        }
//
//        ArrayList<int[]> allOrderings1 = new ArrayList<int[]>();
//        ArrayList<int[]> allOrderings2 = new ArrayList<int[]>();
//        getEveryOrderingNew(toOrder1, new ArrayList<int[]>(), allOrderings1);
//        getEveryOrderingNew(toOrder2, new ArrayList<int[]>(), allOrderings2);
//
//        ArrayList<int[]> allCombinedOrderings = new ArrayList<ArrayList>();
//        for (ArrayList<int[]> i1 : allOrderings1)
//        {
//            for (ArrayList<int[]> i2 : allOrderings2)
//            {
//                int commonSize = Math.min(i1.size(), i2.size());
//                ArrayList<ArrayList> combinedOrdering = new ArrayList<ArrayList>();
//                for (int i = 0; i < commonSize; i++)
//                {
//                    combinedOrdering.add(i1.get(i));
//                    combinedOrdering.add(i2.get(i));
//                }
//                allCombinedOrderings.add(combinedOrdering);
//            }
//        }
//
//        return allCombinedOrderings;
//    }
    
    //TODO: DEPRECATED
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
    
    /**
     * This method returns the nearest variables upstreams from a given
     * [plantIndex stateIndex]-pair. If there is also an eventIndex supplied,
     * the upstreams variables are only returned if there is no path split in the
     * stateIndex-state. Otherwise, the alt. path variable representing that
     * split is returned.
     *
     * @param   [plantIndex stateIndex eventIndex] (the last entry is optional)
     * @return  the collection of alt. path variables that correspond to the event
     *          actually occurring in stateIndex-state.
     */
    private Collection<int[]> getActiveAltPathVars(int[] plantStateEvent)
    {
        if (plantStateEvent.length == 3)
        {
            State state = indexMap.getStateAt(plantStateEvent[0], plantStateEvent[1]);
            if (state.nbrOfOutgoingMultiArcs() > 1)
            {
                int nextStateIndex = indexMap.getStateIndex(plantStateEvent[0],
                        state.nextState(indexMap.getEventAt(plantStateEvent[2])));
                
                ArrayList<int[]> activeAltPathVars = new ArrayList<int[]>();
                activeAltPathVars.add(altPathVariablesTree.get(new int[]{
                    plantStateEvent[0], plantStateEvent[1], nextStateIndex
                }));
                
                return activeAltPathVars;
            }
        }
        
        return upstreamsAltPathVars[plantStateEvent[0]][plantStateEvent[1]];
    }
    
    public String getMessages(int msgType)
    {
        switch (msgType)
        {
            case(SchedulingConstants.MESSAGE_TYPE_INFO):
                return infoMsgs;
            case(SchedulingConstants.MESSAGE_TYPE_WARN):
                return warnMsgs;
            case(SchedulingConstants.MESSAGE_TYPE_ERROR):
                return errorMsgs;
            default:
                errorMsgs += "Wrong message type supplied to Milp.getMessages()";
                return null;
        }
    }
    
    public Object[] getDebugMessages()
    {
        return debugMsgs.toArray();
    }
    
    public void addToMessages(String newMessage, int msgType)
    {
        addToMessages(newMessage, msgType, false);
    }
    
    public void addToMessages(String newMessage, int msgType, boolean noLineBreak)
    {
        String lineBreakStr = "";
        if (!noLineBreak)
        {
            lineBreakStr = "\n";
        }
        
        switch (msgType)
        {
            case(SchedulingConstants.MESSAGE_TYPE_INFO):
                infoMsgs += (String) newMessage + lineBreakStr;
                break;
            case(SchedulingConstants.MESSAGE_TYPE_WARN):
                warnMsgs += (String) newMessage + lineBreakStr;
                break;
            case(SchedulingConstants.MESSAGE_TYPE_ERROR):
                errorMsgs += (String) newMessage + lineBreakStr;
                break;
            default:
                errorMsgs += "Wrong message type supplied to Milp.addToMessages() (messageStr = " + newMessage + ")";
        }
    }
    
    public double[][] getDeltaTimes()
    {
        return deltaTimes;
    }
    
    public int getNrOfZones()
    {
        return zones.size();
    }
    
    public ArrayList<String> getAltPathVariablesStrArray()
    {
        return altPathVariablesStrArray;
    }
    public IntArrayTreeSet getAltPathVaribles()
    {
        return altPathVariablesTree;
    }
    public ArrayList<String> getMutexVariables()
    {
        return mutexVariables;
    }
    public ArrayList<String> getInternalPrecVariables()
    {
        return internalPrecVariables;
    }
    public TreeMap<int[], Integer> getMutexVarCounterMap()
    {
        return mutexVarCounterMap;
    }
    public ArrayList<int[]> getCycleTimeConstraints()
    {
        return cycleTimeConstraints;
    }
    public ArrayList<int[]> getInitPrecConstraints()
    {
        return initPrecConstraints;
    }
    public ArrayList<int[]> getPrecConstraints()
    {
        return precConstraints;
    }
    public ArrayList<Constraint> getMutexConstraints()
    {
        return mutexConstraints;
    }
    public ArrayList<Constraint> getAltPathsConstraints()
    {
        int a = 0;
        return altPathsConstraints;
    }
    public ArrayList<ArrayList<int[]>> getXorConstraints()
    {
        return xorConstraints;
    }
    public ArrayList<CircularWaitConstraintBlock> getCircularWaitConstraints()
    {
        return circularWaitConstraints;
    }
    public ArrayList<ArrayList<ArrayList<ArrayList<int[]>>>> getSharedEventConstraints()
    {
        return sharedEventConstraints;
    }
    
    public Automata getSubControllers()
    throws MilpException
    {
        if (schedule == null)
        {
            throw new MilpException("No shedule found to partition into subcontrollers.");
        }
        
        Alphabet zoneAlphabet = new Alphabet();
        for (Automaton zone : zones)
        {
            zoneAlphabet.union(zone.getAlphabet());
        } 
        
        Automata subControllers =  new Automata();
        for (int i = 0; i < plants.size(); i++)
        {
            Automaton subController = new Automaton("subC_" + plants.getAutomatonAt(i).getName());
            State currState = new State("q0");
            currState.setInitial(true);
            currState.setAccepting(true);
            subController.addState(currState);
            subControllers.addAutomaton(subController);
        }    
        Automaton subController = new Automaton("subC_" + schedule.getName());
        State currState = new State("q0");
        currState.setInitial(true);
        currState.setAccepting(true);
        subController.addState(currState);
        subControllers.addAutomaton(subController);
        
        State scheduleState = schedule.getInitialState();
        boolean firstLoop = true;
        while (firstLoop || !scheduleState.isInitial())
        {
            firstLoop = false;
            Arc arc = scheduleState.outgoingArcsIterator().next();
            
            if (zoneAlphabet.contains(arc.getEvent()))
            {
                Automaton commonSubController = subControllers.getAutomatonAt(subControllers.size()-1);
                State currCommonState = commonSubController.getStateWithName("q" + (commonSubController.nbrOfStates() - 1));
                State nextCommonState = new State("q" + commonSubController.nbrOfStates());
                Arc newArc = new Arc(currCommonState, nextCommonState, arc.getEvent());
                commonSubController.addState(nextCommonState);
                commonSubController.addArc(newArc);
                if (!commonSubController.getAlphabet().contains(arc.getEvent()))
                {
                    commonSubController.getAlphabet().addEvent(arc.getEvent());
                }
            }
            else
            {
                for (int i = 0; i < plants.size(); i++)
                {
                    if (plants.getAutomatonAt(i).getAlphabet().contains(arc.getEvent()))
                    {
                        Automaton currSubController = subControllers.getAutomatonAt(i);
                        State currSubState = currSubController.getStateWithName("q" + (currSubController.nbrOfStates() - 1));
                        State nextSubState = new State("q" + currSubController.nbrOfStates());
                        Arc newArc = new Arc(currSubState, nextSubState, arc.getEvent());
                        currSubController.addState(nextSubState);
                        currSubController.addArc(newArc);
                        if (!currSubController.getAlphabet().contains(arc.getEvent()))
                        {
                            currSubController.getAlphabet().addEvent(arc.getEvent());
                        }
                    }
                }
            }
            
            scheduleState = arc.getToState();
        }
         
        for (int i = 0; i < subControllers.size(); i++)
        {
            Automaton currSubC = subControllers.getAutomatonAt(i);
            for (int j=0; j<currSubC.nbrOfStates(); j++)
            {
                State state = currSubC.getStateWithIndex(j);
                if (!state.outgoingArcsIterator().hasNext())
                {
                    Arc inArc = state.incomingArcsIterator().next();
                    currSubC.addArc(new Arc(inArc.getFromState(), currSubC.getInitialState(), inArc.getEvent()));
                    currSubC.removeState(state);
                    break;
                }
            }
        }
        
        return subControllers;
    }
    
    /**
     * Check whether the plant contains any loop, in which case an exception is thrown.
     * This is done using BookingPairsGraphExplorer with as many colors as there are states 
     * (since we don't want any limitation here, any cycle should be found). The fromTic-, toTic-
     * and bufferExists-parameters of the Edge class are not used in this case. 
     */
    private void checkForLoops(Automata autos)
        throws MilpException
    {
        String exceptionStr = "";
        
        for (Iterator<Automaton> autIt = autos.iterator(); autIt.hasNext();)
        {
            Automaton auto = autIt.next();
            
            ArrayList<int[]>[] edgeInfos = new ArrayList[auto.nbrOfStates()];
            for (Iterator<State> stateIt = auto.stateIterator(); stateIt.hasNext();)
            {
                State state = stateIt.next();
                int stateIndex = indexMap.getStateIndex(auto, state);
                edgeInfos[stateIndex] = new ArrayList<int[]>();

                for (Iterator<State> nextStateIt = state.nextStateIterator(); nextStateIt.hasNext(); )
                {
                    State nextState = nextStateIt.next();
                    if (state.equalState(nextState))
                    {
                        // If a self-loop is found, throw exception
                        throw new MilpException("Self-loop found in '" + auto.getName() + "', state '" + state.getName() + "'. MILP-formulation impossible.");
                    }
                    else
                    {
                        // Otherwise, construct the edges representing this automaton
                        edgeInfos[stateIndex].add(new int[]{indexMap.getStateIndex(auto, nextState), stateIndex, -1, -1, -1});
                    }
                }
            }

            // Enumerate all cycles using BookingPairsGraphExplorer.java (note that the number of colors is equal to the number of states).
            // If a cycle was detected, throw exception displaying the states that are involved in the cycle(s).
            ConnectedComponentsGraph cycleFinder = new ConnectedComponentsGraph(edgeInfos, edgeInfos.length);
            ArrayList<ArrayList<ConnectedComponentEdge>> cycles = cycleFinder.enumerateAllCycles();
            if (! cycles.isEmpty())
            {
                String loopStr = "";
                for (Iterator<ArrayList<ConnectedComponentEdge>> cycleIt = cycles.iterator(); cycleIt.hasNext();)
                {
                    loopStr += "\n-> ";

                    ArrayList<ConnectedComponentEdge> cycle = cycleIt.next();
                    for (Iterator<ConnectedComponentEdge> edgeIt = cycle.iterator(); edgeIt.hasNext();)
                    {
                        ConnectedComponentEdge edge = edgeIt.next();
                        loopStr += indexMap.getStateAt(auto, edge.getToVertice().getVerticeIndex()).getName() +  " -> ";
                    }
                }

                exceptionStr += "\nLoops found in '" + auto.getName() + "':" + loopStr;
            }
        }
        
        if (!exceptionStr.equals(""))
        {
            throw new MilpException(exceptionStr + "\nMILP-formulation impossible!");
        }
    }
}
