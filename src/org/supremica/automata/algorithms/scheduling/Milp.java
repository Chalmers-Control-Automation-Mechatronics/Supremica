package org.supremica.automata.algorithms.scheduling;

import java.util.*;
import java.util.regex.*;
import java.io.*;

import org.supremica.automata.*;
import org.supremica.automata.algorithms.*;
import org.supremica.gui.ActionMan;
import org.supremica.gui.ScheduleDialog;
import org.supremica.log.*;
import org.supremica.util.ActionTimer;

public class Milp
    implements Scheduler
{

    /****************************************************************************************/
    /*                                 VARIABLE SECTION                                     */
    /****************************************************************************************/

    /** The logger */
    private static Logger logger = LoggerFactory.createLogger(Milp.class);

    /** The involved automata, plants, zone specifications */
    private Automata theAutomata, robots, zones;

    /** The project used for return result */
    private Project theProject;

    /** int[zone_nr][robot_nr][state_nr] - stores the states that fire booking/unbooking events */
    private int[][][] bookingTics, unbookingTics;

    /** The optimal cycle time (makespan) */
    private double makespan;

    /** The *.mod file that serves as an input to the Glpk-solver */
    private File modelFile;

    /** The *.sol file that stores the solution, i.e. the output of the Glpk-solver */
    private File solutionFile;

    /** The optimal times (for each robotstate) that the MILP solver returns */
    private double[][] optimalTimes = null;

    /** Is needed to kill the MILP-solver if necessary */
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
    private ScheduleDialog gui;

    /** Decides if the schedule should be built */
    private boolean buildSchedule;

    /** The output string */
    private String outputStr = "";

    /****************************************************************************************/
    /*                                 CONSTUCTORS                                          */
    /****************************************************************************************/

    public Milp(Project theProject, Automata theAutomata, boolean buildSchedule, ScheduleDialog gui)
    throws Exception
    {
        this.theAutomata = theAutomata;
        this.theProject = theProject;
        this.buildSchedule = buildSchedule;
        this.gui = gui;

        Thread milpThread = new Thread(this);
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
            outputStr += "\t" + totalTimeStr + "\n";
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
            gui.reset();
        }
    }

    /**
     * This method constructs an automaton representing the optimal schedule, using the
     * sequence of times (one time value for every state of the involved plant automata)
     * that the MILP-solver has generated.
     *
     * Does not return anything? The automaton representing the optimal schedule, as given by the MILP-solver
     */
    public void buildScheduleAutomaton()
    throws Exception
    {
        timer.restart();

        schedule = new Automaton();
        schedule.setComment("Schedule");

        SynchronizationStepper stepper = new SynchronizationStepper(theAutomata);

        // The current synchronized state indices, consisting of as well plant
        // as specification indices and used to step through the final graph following the
        // time schedule (which is needed to build the schedule automaton)
        int[] currComposedStateIndices = stepper.getInitialStateIndices();

        // The first set of state indices correspond to the initial state of the composed automaton.
        // They are thus used to construct the initial state of the schedule automaton.
        State currScheduledState = makeScheduleState(currComposedStateIndices, true);

        // This alphabet is needed to check which events are common to the robots
        // If several transitions with the same event are enabled simultaneously, naturally
        // the transition having the greatest time value should be fired
        Alphabet commonRobotEventsAlphabet = new Alphabet();
        for (int i=0; i<robots.size() - 1; i++)
        {
            Alphabet firstAlphabet = robots.getAutomatonAt(i).getAlphabet();

            for (int j=i+1; j<robots.size(); j++)
            {
                Alphabet secondAlphabet = robots.getAutomatonAt(j).getAlphabet();

                commonRobotEventsAlphabet.addEvents(AlphabetHelpers.intersect(firstAlphabet, secondAlphabet));
            }
        }

        // Walk from the initial state until an accepting (synchronized) state is found
        // In every step, the cheapest allowed transition is chosen
        // This is done until an accepting state is found for our schedule
        while (! currScheduledState.isAccepting())
        {
            // Every robot is checked for possible transitions and the one with smallest time value
            // is chosen.
            double smallestTime = Double.MAX_VALUE;

            // The event that is next to be fired in the schedule (i.e. the event corresponding to the smallest allowed time value) is stored
            LabeledEvent currOptimalEvent = null;

            // The index of a robot in the "robots"-variable. Is increased whenever a plant is found
            int robotIndex = -1;

            // Stores the highest firing times for each active synchronizing event
            Hashtable<LabeledEvent, Double> synchArcsInfo = new Hashtable<LabeledEvent, Double>();

            // Which automaton fires the "cheapest" transition...
            for (int i=0; i<theAutomata.size(); i++)
            {
                Automaton currRobot = theAutomata.getAutomatonAt(i);

                // Since the robots are supposed to fire events, the check for the "smallest time event"
                // is only done for the plants
                if (currRobot.isPlant())
                {
                    robotIndex++;

                    State currState = currRobot.getStateWithIndex(currComposedStateIndices[i]);
                    double currTime = optimalTimes[robotIndex][currComposedStateIndices[i]];

                    // Choose the smallest time (as long as it is not smaller than the previously scheduled time)...
                    if (currTime <= smallestTime)
                    {
                        for (Iterator<Arc> arcs = currState.outgoingArcsIterator(); arcs.hasNext(); )
                        {
                            Arc currArc = arcs.next();
                            LabeledEvent currEvent = currArc.getEvent();

                            // ... that correspoinds to an enabled transition
                            if (stepper.isEnabled(currEvent))
                            {
                                int nextStateIndex = indexMap.getStateIndex(currRobot, currArc.getToState());

                                // If the next node has lower time value, then it cannot belong
                                // to the optimal path, since precedence constraints are not fulfilled
                                // Otherwise, this could be the path
                                if (optimalTimes[robotIndex][nextStateIndex] >= currTime + currRobot.getStateWithIndex(nextStateIndex).getCost())
                                {
                                    // But! Care should be taken with the events common to any pair of robots.
                                    // When synched, the slowest robot should fire the synchronizing event.
                                    // If this event is unique for some robot...
                                    if (! commonRobotEventsAlphabet.contains(currEvent))
                                    {
                                        currOptimalEvent = currEvent;
                                        smallestTime = currTime;
                                    }
                                    // ... and if not, the current time is put away to be processed when all
                                    // the robots' outgoing events have been processed (for this state).
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

            currScheduledState = nextScheduledState;

            // If all the states that build up the current state are accepting, make the composed state accepting too
            boolean isAccepting = true;
            for (int i=0; i<theAutomata.size(); i++)
            {
                if (! theAutomata.getAutomatonAt(i).getStateWithIndex(currComposedStateIndices[i]).isAccepting())
                    isAccepting = false;
            }

            if (isAccepting)
            {
                if (smallestTime != makespan)
                    throw new Exception("Makespan value does NOT correspond to the cost of the final state of the schedule (sched_time = " + smallestTime + "; makespan = " + makespan + "). Something went wrong...");

                currScheduledState.setAccepting(true);
                currScheduledState.setName(currScheduledState.getName() + ";  makespan = " + makespan);
            }
        }

        String str = "Time to build the schedule: " + timer.elapsedTime() + "ms ";
        logger.info(str);
        outputStr += "\t" + str;

        synchronized (this)
        {
            //org.supremica.gui.Gui theGui = null;
            try
            {
				theProject.addAutomaton(schedule);
                //theGui = ActionMan.getGui();
                //theGui.addAutomaton(schedule);
// 				if (theGui != null)
// 					theGui.addAutomaton(schedule);
// 				else
// 					logger.info("THE GUI is NULL");
            }
            catch (Exception ex)
            {
                logger.warn("EXceptiON");
                throw ex;
            }
        }
    }


    /****************************************************************************************/
    /*                                 INIT METHODS                                         */
    /****************************************************************************************/

    /**
     * Creates the (temporary) *.mod- and *.sol- files that are used to communicate
     * with the MILP-solver (GLPK). The automata are preprocessed (synthes and purge)
     * while the information about the location of booking/unbooking events is collected.
     */
    private void initialize()
    throws Exception
    {
        modelFile = File.createTempFile("milp", ".mod");
        modelFile.deleteOnExit();

        solutionFile = File.createTempFile("milp", ".sol");
        solutionFile.deleteOnExit();

        indexMap = new AutomataIndexMap(theAutomata);
        pathCutTable = new Hashtable<State,String>();

        initAutomata();
        initMutexStates();
    }


    /**
     * Goes through the supplied automata and synchronizes all specifications
     * that do not represent zones with corresponding robot automata.
     * This assumes that robots and the specifications regulating their behavior
     * have similar roots. For example if robot.name = "ROBOT_A", the specification.name
     * should include "ROBOT_A". The resulting robot and zone automata are stored globally.
     */
    private void initAutomata()
    throws Exception
    {
        robots = theAutomata.getPlantAutomata();
        zones = theAutomata.getSpecificationAutomata();

        // The robots synchronized with all corresponding specifications (except mutex zone specifications)
        Automata restrictedRobots = new Automata();

        for (int i=0; i<robots.size(); i++)
        {
            Automaton currRobot = robots.getAutomatonAt(i);
            String currRobotName = currRobot.getName();

            Automata toBeSynthesized = new Automata(currRobot);
            int[] costs = new int[currRobot.nbrOfStates()];

            // Store the costs of each of the robots states
            for (Iterator<State> stateIter = currRobot.stateIterator(); stateIter.hasNext(); )
            {
                State currState = stateIter.next();
                int stateIndex = indexMap.getStateIndex(currRobot, currState);
                costs[stateIndex] = currState.getCost();
            }

            // Find the specifications that are to be synthesized with the current plant/robot
            // Their names should contain the name of the plant/robot that they constraint/specify
            for (Iterator<Automaton> zonesIterator = zones.iterator(); zonesIterator.hasNext(); )
            {
                Automaton currSpec = zonesIterator.next();

                if (currSpec.getName().contains(currRobotName))
                {
                    toBeSynthesized.addAutomaton(currSpec);
                }
            }

            // If there are several automata with similar names (one is a plant the other are
            // restricting specification), then perform a synthesis
            if (toBeSynthesized.size() > 1)
            {
                SynthesizerOptions synthesizerOptions = new SynthesizerOptions();
                synthesizerOptions.setSynthesisType(SynthesisType.NONBLOCKINGCONTROLLABLE);
                synthesizerOptions.setSynthesisAlgorithm(SynthesisAlgorithm.MONOLITHIC);
                synthesizerOptions.setPurge(true);
                synthesizerOptions.setMaximallyPermissive(true);
                synthesizerOptions.setMaximallyPermissiveIncremental(true);

                AutomataSynthesizer synthesizer = new AutomataSynthesizer(toBeSynthesized, SynchronizationOptions.getDefaultSynthesisOptions(), synthesizerOptions);

                Automaton restrictedRobot = synthesizer.execute().getAutomatonAt(0);
                restrictedRobot.setName(currRobotName);
                restrictedRobot.setType(AutomatonType.PLANT);

                // Set the state costs for the resulting synthesized automaton in an appropriate way
                for (Iterator<State> stateIter = restrictedRobot.stateIterator(); stateIter.hasNext(); )
                {
                    State currState = stateIter.next();
                    String stateName = currState.getName().substring(0, currState.getName().indexOf("."));
                    int stateIndex = indexMap.getStateIndex(restrictedRobot, new State(stateName));

                    currState.setIndex(stateIndex);
                    currState.setCost(costs[stateIndex]);
                }

                // Remove the specifications that have been synthesized
                for (Iterator<Automaton> toBeSynthesizedIterator = toBeSynthesized.iterator(); toBeSynthesizedIterator.hasNext(); )
                {
                    Automaton isSynthesized = toBeSynthesizedIterator.next();

                    if (isSynthesized.isSpecification())
                        zones.removeAutomaton(isSynthesized);
                }

                restrictedRobot.remapStateIndices();

                restrictedRobots.addAutomaton(restrictedRobot);
            }
            else
            {
                restrictedRobots.addAutomaton(currRobot);
            }
        }

        // Updating the automata variables
        robots = restrictedRobots;
        theAutomata = new Automata(robots);
        theAutomata.addAutomata(zones);
        indexMap = new AutomataIndexMap(theAutomata);
    }

    private void initMutexStates()
    throws Exception
    {
        bookingTics = new int[zones.size()][robots.size()][1];
        unbookingTics = new int[zones.size()][robots.size()][1];

        // Initializing all book/unbook-state indices to -1.
        // The ones that remain -1 at the output of this method correspond
        // to non-conflicting robot-zone-pairs.
        for (int i=0; i<zones.size(); i++)
        {
            for (int j=0; j<robots.size(); j++)
            {
                bookingTics[i][j][0] = -1;
                unbookingTics[i][j][0] = -1;
            }
        }

        for (int i=0; i<zones.size(); i++)
        {
            Automaton currZone = zones.getAutomatonAt(i);
// 			ArrayList[] bookUnbookStatePairIndices = new ArrayList[robots.size()];

            for (int j=0; j<robots.size(); j++)
            {
                Automaton currRobot = robots.getAutomatonAt(j);

// 				Alphabet commonAlphabet = AlphabetHelpers.intersect(currRobot.getAlphabet(), currZone.getAlphabet());
                Alphabet bookingAlphabet = AlphabetHelpers.intersect(currRobot.getAlphabet(), currZone.getInitialState().activeEvents(false));

                if (bookingAlphabet.size() > 0)
                {
                    Alphabet unbookingAlphabet = AlphabetHelpers.minus(AlphabetHelpers.intersect(currRobot.getAlphabet(), currZone.getAlphabet()), bookingAlphabet);
// 					bookUnbookStatePairIndices[j] = new ArrayList<int[]>();

                    ArrayList<State> bookingStates = new ArrayList<State>();
                    ArrayList<State> unbookingStates = new ArrayList<State>();

                    for (Iterator<State> stateIter = currRobot.stateIterator(); stateIter.hasNext(); )
                    {
                        State currState = stateIter.next();

                        Alphabet currStatesBookingAlphabet = AlphabetHelpers.intersect(currState.activeEvents(false), bookingAlphabet);
                        for (Iterator<LabeledEvent> currBookingEventsIter = currStatesBookingAlphabet.iterator(); currBookingEventsIter.hasNext(); )
                        {
// 							bookingStates.add(new int[]{indexMap.getStateIndex(currRobot, currState), indexMap.getEventIndex(currBookingEventsIter.next())});
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
                        bookingTics[i][j][k] = indexMap.getStateIndex(currRobot, bookingStates.get(k));
                        unbookingTics[i][j][k] = indexMap.getStateIndex(currRobot, unbookingStates.get(k));
                    }
                }
            }
        }
    }

    private void initMutexStates2()
    throws Exception
    {
        bookingTics = new int[zones.size()][robots.size()][1];
        unbookingTics = new int[zones.size()][robots.size()][1];

        // Initializing all book/unbook-state indices to -1.
        // The ones that remain -1 at the output of this method correspond
        // to non-conflicting robot-zone-pairs.
        for (int i=0; i<zones.size(); i++)
        {
            for (int j=0; j<robots.size(); j++)
            {
                bookingTics[i][j][0] = -1;
                unbookingTics[i][j][0] = -1;
            }
        }

        for (int i=0; i<zones.size(); i++)
        {
            State initial = zones.getAutomatonAt(i).getInitialState();

            for (Iterator<MultiArc> bookingMultiArcs = initial.outgoingMultiArcIterator(); bookingMultiArcs.hasNext(); )
            {
                Alphabet bookingAlphabet = new Alphabet();
                Alphabet unbookingAlphabet = new Alphabet();

                MultiArc currBookingMultiArc = bookingMultiArcs.next();

                // Adding booking event for each set of arcs i.e. for each book-state of the zone-specification
                for (Iterator<Arc> bookingArcs = currBookingMultiArc.iterator(); bookingArcs.hasNext(); )
                {
                    bookingAlphabet.addEvent(bookingArcs.next().getEvent());
                }

                for (Iterator<MultiArc> unbookingMultiArcs = currBookingMultiArc.getToState().outgoingMultiArcIterator(); unbookingMultiArcs.hasNext(); )
                {
                    MultiArc currUnbookingMultiArc = unbookingMultiArcs.next();

                    if (currUnbookingMultiArc.getToState().equals(initial))
                    {
                        for (Iterator<Arc> unbookingArcs = currUnbookingMultiArc.iterator(); unbookingArcs.hasNext(); )
                        {
                            unbookingAlphabet.addEvent(unbookingArcs.next().getEvent());
                        }

                        break;
                    }
                }

                // The robot set is searched for the states from which the above book/unbook events can be fired
                for (int j=0; j<robots.size(); j++)
                {
                    ArrayList<State> bookingStates = new ArrayList<State>();
                    ArrayList<State> unbookingStates = new ArrayList<State>();
                    Automaton currRobot = robots.getAutomatonAt(j);


                    // The following is done only for the robot that contains the corresponding unbooking-event
                    Alphabet currRobotZoneIntersectionAlphabet = AlphabetHelpers.intersect(currRobot.getAlphabet(), unbookingAlphabet);
                    if (currRobotZoneIntersectionAlphabet.nbrOfEvents() > 0)
                    {
                        // States that can lead directly to unbooking-events are found
                        for (Iterator<Arc> robotArcs = currRobot.arcIterator(); robotArcs.hasNext(); )
                        {
                            Arc currArc = robotArcs.next();

                            if (currRobotZoneIntersectionAlphabet.contains(currArc.getLabel()))
                                unbookingStates.add(currArc.getFromState());
                        }

                        // For each "unbooking"-state, a search up the robot is done until corresponding
                        // "booking"-states are found. Note that one "u"-state can correspond to several "b"-states.
                        for (int k=0; k<unbookingStates.size(); k++)
                        {
                            // If there are alternative paths to the current state, several booking states will be found.
                            // They should be matched by equal number of unbooking states.
                            int alternativeBookings = 0;

                            ArrayList<State> upstreamStates = new ArrayList<State>();
                            upstreamStates.add(unbookingStates.get(k));

                            while (!upstreamStates.isEmpty())
                            {
                                State currState = upstreamStates.remove(0);

                                // Every roadsplit is an alternative.
                                alternativeBookings += (currState.nbrOfIncomingArcs() - 1);

                                for (Iterator<Arc> incomingArcs = currState.incomingArcsIterator(); incomingArcs.hasNext(); )
                                {
                                    Arc currArc = incomingArcs.next();

                                    // If we hit the booking event, then add the state that fires it to the "bookingStates"
                                    if (bookingAlphabet.contains(currArc.getLabel()))
                                    {
                                        bookingStates.add(currArc.getFromState());
                                    }
                                    else
                                        upstreamStates.add(currArc.getFromState());
                                }

                                // A copy of the current unbooking state is added for every alternative path
                                // between the unbook state and the book states.
                                for (int l=0; l<alternativeBookings; l++)
                                    unbookingStates.add(k, unbookingStates.get(k));
                            }
                        }

                        bookingTics[i][j] = new int[bookingStates.size()];
                        unbookingTics[i][j] = new int[unbookingStates.size()];

                        if (bookingTics[i][j].length != unbookingTics[i][j].length)
                        {
                            String exceptionStr = "The numbers of book/unbook-states do not correspond. Something is wrong....\n";
                            exceptionStr += "nr_book_states[" + i + "][" + j + "] = " + bookingTics[i][j].length + "\n";
                            exceptionStr += "nr_unbook_states[" + i + "][" + j + "] = " + unbookingTics[i][j].length + "\n";

                            throw new Exception(exceptionStr);
                        }

                        for (int k=0; k<bookingTics[i][j].length; k++)
                        {
                            bookingTics[i][j][k] = indexMap.getStateIndex(currRobot, bookingStates.get(k));
                            unbookingTics[i][j][k] = indexMap.getStateIndex(currRobot, unbookingStates.get(k));
                        }

                        // This assumes that each zone-event is used by exactly one robot
                        break;
                    }
                }
            }
        }
    }


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

        int nrOfRobots = robots.size();
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
        String altPathsVariables = "";

        // The string containing the cycle time constraints
        String cycleTimeConstraints = "";

        // The string containing times for each state (delta-times)
        String deltaTimeStr = "param deltaTime default 0\n:";


        ////////////////////////////////////////////////////////////////////////////////////
        //	                          The constructing part                               //
        ////////////////////////////////////////////////////////////////////////////////////

        // Finding maximum number of time variables per robot (i.e. max nr of states)
        int nrOfTics = 0;
        for (int i=0; i<nrOfRobots; i++)
        {
            int nbrOfStates = robots.getAutomatonAt(i).nbrOfStates();
            if (nbrOfStates > nrOfTics)
                nrOfTics = nbrOfStates;
        }

        // Making deltaTime-header
        for (int i=0; i<nrOfTics; i++)
            deltaTimeStr += "\t" + i;

        deltaTimeStr += " :=\n";

        // Extracting deltaTimes for each robot
        for (int i=0; i<nrOfRobots; i++)
        {
            deltaTimeStr += i;

            Automaton currRobot = robots.getAutomatonAt(i);
            int currRobotIndex = indexMap.getAutomatonIndex(currRobot);

            // Each index correspond to a Tic. For each Tic, a deltaTime is added
            int[] deltaTimes = new int[currRobot.nbrOfStates()];
            for (Iterator<State> stateIter = currRobot.stateIterator(); stateIter.hasNext(); )
            {
                State currState = stateIter.next();

                int currStateIndex = indexMap.getStateIndex(currRobot, currState);

                deltaTimes[currStateIndex] = currState.getCost();

                // If the current state has successors and is not initial, add precedence constraints
                // If the current state is initial, add an initial (precedence) constraint
                int nbrOfOutgoingMultiArcs = currState.nbrOfOutgoingMultiArcs();
                if (nbrOfOutgoingMultiArcs > 0)
                {
                    if (currState.isInitial())
                    {
                        initPrecConstraints += "initial_" + "R" + currRobotIndex + "_" + currStateIndex + " : ";
                        initPrecConstraints += "time[" + i + ", " + currStateIndex + "] >= deltaTime[" + i + ", " + currStateIndex + "];\n";
                    }

                    Iterator<State> nextStates = currState.nextStateIterator();

                    // If there is only one successor, add a precedence constraint
                    if (nbrOfOutgoingMultiArcs == 1)
                    {
                        State nextState = nextStates.next();
                        int nextStateIndex = indexMap.getStateIndex(currRobot, nextState);

                        precConstraints += "prec_" + "R" + currRobotIndex + "_" + currStateIndex + "_" + nextStateIndex + " : ";
                        precConstraints += "time[" + i + ", " + nextStateIndex + "] >= time[" + i + ", " + currStateIndex + "] + deltaTime[" + i + ", " + nextStateIndex + "];\n";
                    }
                    // If there are two successors, add one alternative-path variable and corresponding constraint
                    else if (nbrOfOutgoingMultiArcs == 2)
                    {
                        State nextLeftState = nextStates.next();
                        State nextRightState = nextStates.next();
                        int nextLeftStateIndex = indexMap.getStateIndex(currRobot, nextLeftState);
                        int nextRightStateIndex = indexMap.getStateIndex(currRobot, nextRightState);

                        String currAltPathsVariable = "R" + currRobotIndex + "_goes_from_" + currStateIndex + "_to_" + nextLeftStateIndex;

                        altPathsVariables += "var " + currAltPathsVariable + ", binary;\n";

                        altPathsConstraints += "alt_paths_" + "R" + currRobotIndex + "_" + currStateIndex + " : ";
                        altPathsConstraints += "time[" + i + ", " + nextLeftStateIndex + "] >= time[" + i + ", " + currStateIndex + "] + deltaTime[" + i + ", "  + nextLeftStateIndex + "] - bigM*" + currAltPathsVariable + ";\n";

                        pathCutTable.put(nextLeftState, currAltPathsVariable);

                        altPathsConstraints += "dual_alt_paths_" + "R" + currRobotIndex + "_" + currStateIndex + " : ";
                        altPathsConstraints += "time[" + i + ", " + nextRightStateIndex + "] >= time[" + i + ", " + currStateIndex + "] + deltaTime[" + i + ", "  + nextRightStateIndex + "] - bigM*(1 - " + currAltPathsVariable + ");\n";

                        pathCutTable.put(nextRightState, "(1 - " + currAltPathsVariable + ")");
                    }
                    // If there are several successors, add one alternative-path variable for each successor
                    else if (nbrOfOutgoingMultiArcs > 2)
                    {
                        int currAlternative = 0;

                        while (nextStates.hasNext())
                        {
                            String currAltPathsVariable = "R" + currRobotIndex + "_" + currStateIndex + "_path_" + currAlternative;
                            State nextState = nextStates.next();
                            int nextStateIndex = indexMap.getStateIndex(currRobot, nextState);

                            altPathsVariables += "var " + currAltPathsVariable + ", binary;\n";

                            altPathsConstraints += "alt_paths_" + currAltPathsVariable + " : ";
                            altPathsConstraints += "time[" + i + ", " + nextStateIndex + "] >= time[" + i + ", " + currStateIndex + "] + deltaTime[" + i + ", "  + nextStateIndex + "] - bigM*(1 - " + currAltPathsVariable + ");\n";

                            pathCutTable.put(nextState, "(1 - " + currAltPathsVariable + ")");

                            currAlternative++;
                        }

                        altPathsConstraints += "alt_paths_" + "R" + currRobotIndex + "_" + currStateIndex + "_TOT : ";
                        for (int k=0; k<currAlternative - 1; k++)
                        {
                            altPathsConstraints += "R" + currRobotIndex + "_" + currStateIndex + "_path_" + k + " + ";
                        }
                        altPathsConstraints += "R" + currRobotIndex + "_" + currStateIndex + "_path_" + (currAlternative - 1) + " = 1;\n";
                    }
                }
                else if (currState.isAccepting())
                {
                    // If the current state is accepting, a cycle time constaint is added,
                    // ensuring that the makespan is at least as long as the minimum cycle time of this robot
                    cycleTimeConstraints += "cycle_time_" + "R" + currRobotIndex + " : c >= " + "time[" + i + ", " + currStateIndex + "];\n";
                }
            }

            for (int j=0; j<deltaTimes.length; j++)
            {
                deltaTimeStr += "\t" + deltaTimes[j];
            }

            // If the number of states of the current automaton is less
            // than max_nr_of_states, the deltaTime-matrix is filled with points
            // i.e zeros.
            for (int j=currRobot.nbrOfStates(); j<nrOfTics; j++)
            {
                deltaTimeStr += "\t.";
            }

            if (i == nrOfRobots - 1)
                deltaTimeStr += ";";

            deltaTimeStr += "\n";
        }

        // Constructing the mutex constraints
        // for every zone...

        // The safety buffer
        double epsilon = 0.1;

        for (int i=0; i<bookingTics.length; i++)
        {
            // for every robot pair...
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

                                mutexConstraints += "mutex_Z" + i + "_R" + j1 + "_R" + j2 + "_var" + repeatedBooking + " : time[" + j1 + ", " + bookingTics[i][j1][k1] + "] >= " + "time[" + j2 + ", " + unbookingTics[i][j2][k2] + "] - bigM*" + currMutexVariable + " + " + epsilon;

                                String pathCutEnsurance = pathCutTable.get(robots.getAutomatonAt(j1).getStateWithIndex(k1));
                                if (pathCutEnsurance != null)
                                {
                                    mutexConstraints += " - bigM*" + pathCutEnsurance;
                                }
                                mutexConstraints += ";\n";
                                mutexConstraints += "dual_mutex_Z" + i + "_R" + j1 + "_R" + j2  + "_var" + repeatedBooking + " : time[" + j2 + ", " + bookingTics[i][j2][k2] + "] >= " + "time[" + j1 + ", " + unbookingTics[i][j1][k1] + "] - bigM*(1 - " + currMutexVariable + ")" + " + " + epsilon;
                                pathCutEnsurance = pathCutTable.get(robots.getAutomatonAt(j2).getStateWithIndex(k2));
                                if (pathCutEnsurance != null)
                                {
                                    mutexConstraints += " - bigM*" + pathCutEnsurance;
                                }
                                mutexConstraints += ";\n";
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
        w.write("param nrOfRobots >= 0;");
        w.newLine();
        w.write("param nrOfZones >= 0;");
        w.newLine();
        w.write("param maxTic >= 0;");
        w.newLine();
        w.write("param bigM;");
        w.newLine();

        // Definitions of sets
        w.newLine();
        w.write("set Robots := 0..nrOfRobots;");
        w.newLine();
        w.write("set Zones := 0..nrOfZones;");
        w.newLine();
        w.write("set Tics := 0..maxTic;");
        w.newLine();

        // Definitions of parameters, using sets as their input (must be in this order to avoid GLPK-complaints)
        w.newLine();
        w.write("param deltaTime{r in Robots, t in Tics};");
        w.newLine();

        // Definitions of variables
        w.newLine();
        w.write("var time{r in Robots, t in Tics};"); // >= 0;");
        w.newLine();
        w.write("var c;");
        w.newLine();
        w.write(altPathsVariables);
        w.write(mutexVariables);
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
// 		w.write("cycle_time{r in Robots}: c >= time[r, maxTic];");
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
        w.newLine();

        // The end of the model-section and the beginning of the data-section
        w.newLine();
        w.write("data;");
        w.newLine();

        // The numbers of robots resp. zones are given
        w.newLine();
        w.write("param nrOfRobots := " + (nrOfRobots - 1) + ";");
        w.newLine();
        w.write("param nrOfZones := " + (nrOfZones - 1) + ";");
        w.newLine();
        w.write("param bigM := " + Short.MAX_VALUE + ";");
        w.newLine();
        // Behovs maxTic verkligen???
        w.write("param maxTic := " + (nrOfTics - 1) + ";");
        w.newLine();

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
// 		for (int i=0; i<robots.size(); i++)
// 		{
// 			logger.warn("Robot = " + robots.getAutomatonAt(i).getName() + "; index = " + indexMap.getAutomatonIndex(robots.getAutomatonAt(i)));
// 			for (Iterator<State> stir = robots.getAutomatonAt(i).stateIterator(); stir.hasNext(); )
// 			{
// 				State s = stir.next();
// 				logger.info("State = " + s.getName() + "; index = " + indexMap.getStateIndex(robots.getAutomatonAt(i), s));
// 			}
// 		}
        //...

        optimalTimes = new double[robots.size()][];

        for (int i=0; i<optimalTimes.length; i++)
            optimalTimes[i] = new double[robots.getAutomatonAt(i).nbrOfStates()];

        BufferedReader r = new BufferedReader(new FileReader(solutionFile));
        String str = r.readLine();

        // Go through the solution file and extract the suggested optimal times for each state
        while (str != null)
        {
            if (str.indexOf(" time[") > -1)
            {
                String strRobotIndex = str.substring(str.indexOf("[") + 1, str.indexOf(",")).trim();
                String strStateIndex = str.substring(str.indexOf(",") + 1, str.indexOf("]")).trim();
                String strCost = str.substring(str.indexOf("]") + 1).trim();

                int robotIndex = (new Integer(strRobotIndex)).intValue();
                int stateIndex = (new Integer(strStateIndex)).intValue();
                double cost = (new Double(strCost)).doubleValue();

                optimalTimes[robotIndex][stateIndex] = cost;
            }
            else if (str.indexOf("c ") >  -1)
            {
                String strMakespan = str.substring(str.indexOf("c") + 1).trim();
                makespan = (new Double(strMakespan)).doubleValue();
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
        cmds[0] = "C:\\Program Files\\glpk\\bin\\glpsol.exe";
        cmds[1] = "-m";
        cmds[2] = modelFile.getAbsolutePath();
        cmds[3] = "-o";
        cmds[4] = solutionFile.getAbsolutePath();

        // Runs the MILP-solver with the arguments defined above
        milpProcess = Runtime.getRuntime().exec(cmds);

        // Listens for the output of MILP (that is the input to this application)...
        BufferedReader milpEcho = new BufferedReader(new InputStreamReader(new DataInputStream(milpProcess.getInputStream())));

        // ...and prints it to stdout
        String milpEchoStr = "";
        String totalMilpEchoStr = "";
        while ((milpEchoStr = milpEcho.readLine()) != null)
        {
            totalMilpEchoStr += milpEchoStr + "\n";

            if (milpEchoStr.contains("INTEGER OPTIMAL SOLUTION FOUND") || milpEchoStr.contains("Time") || milpEchoStr.contains("Memory"))
            {
// 				logger.info(milpEchoStr);

// 				if (!milpEchoStr.contains("INTEGER OPTIMAL SOLUTION FOUND"))
// 				{
// 					outputStr += "\t" + milpEchoStr + "\n";
// 				}
            }
            else if (milpEchoStr.contains("error"))
            {
                throw new Exception(totalMilpEchoStr);
            }
        }
    }

    // private void killGlpk()
// 	{
// 		if (milpProcess != null)
// 			milpProcess.destroy();
// 	}

    public void requestStop()
    {
        requestStop(false);
    }

    public void requestStop(boolean disposeGui)
    {
        isRunning = false;

        if (disposeGui)
        {
            gui.done();
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
            stateName += theAutomata.getAutomatonAt(i).getStateWithIndex(stateIndices[i]).getName() + ".";
        }
        stateName += theAutomata.getAutomatonAt(theAutomata.size()-1).getStateWithIndex(stateIndices[theAutomata.size()-1]).getName() + "]";

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
}
