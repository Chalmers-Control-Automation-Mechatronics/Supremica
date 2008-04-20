/*
 * VelocityBalancer.java
 *
 * Created on den 8 augusti 2007, 12:32
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.supremica.automata.algorithms.scheduling;

import java.util.*;
import org.supremica.automata.*;
import org.supremica.automata.algorithms.*;
import org.supremica.automata.algorithms.scheduling.milp.IntArrayTreeSet;
import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;
import org.supremica.petrinet.Place;

/**
 * Creates a new instance of VelocityBalancer
 * @author Avenir Kobetski
 */
public class VelocityBalancer
{
    /** 
     * The points along the optimal path 
     * (time values of each state in the optimal schedule).
     */
    ArrayList<double[]> pathPoints = new ArrayList<double[]>();

    /**
     * The key points in the optimal schedule, i.e. the time
     * values such that consecutive points "see" eachother.
     */
    ArrayList<double[]> keyPoints = new ArrayList<double[]>();

    /** 
     * Each mutexLimits-variable contains the time values 
     * of when each plant enters and exits a zone (and null if that 
     * never happens). 
     */
//    ArrayList[] mutexLimits = null;
    ArrayList<double[]>[][] mutexLimitsNew =  null;

    /**
     * Possible deadlocks give rise to forbidden time-zones.
     */
//       ArrayList[] deadlockLimits = null;
    ArrayList<double[]>[][] deadlockLimitsNew =  null;
    
    /** The indices of path points where velocities should be changed, i.e. the indices of key points. */
    ArrayList<Integer> keyPointIndices = null;

    /**
     * The times that are recorded for the plants during simulation.
     * simulationTimes[internal_plant_index][internal_state_index]
     */
    double[][] simulationTimes = null;

    /**
     * The optimal event firing times for each plant.
     * firingTimes[internal_plant_index][internal_state_index]
     */
    double[][] firingTimes = null;
    
    AutomataIndexMap indexMap = null;
    Automata plants = null;
    Automata specs = null;
    Automaton schedule = null;
    Automata optimalSubPlants = null;
    
    int[] plantIndexMapping;
     
    /** The logger */
    private Logger logger = LoggerFactory.createLogger(this.getClass());
    
    Scheduler callingScheduler = null;
       
    public VelocityBalancer(Automata theAutomata)
        throws Exception
    {                   
        this(theAutomata, null);
    }
    public VelocityBalancer(Automata theAutomata, Scheduler callingScheduler)
        throws Exception
    {        
        this.callingScheduler = callingScheduler;
        
        // Initializes pointers to automata, time variables, indexMap, etc
        init(theAutomata);
        
        // Extracts plant times from the optimal schedule - TODO: RENAME
        extractFiringTimes();
        
//        findDeadlockLimits();
        
        // Feasibility check
        String tempStr = "\n";
        for (int i = 0; i < firingTimes.length; i++)
        {
            tempStr += "(firing) i = " + i + " --> ";
            for (int j = 0; j < firingTimes[i].length; j++)
            {
                tempStr += firingTimes[i][j] + " ";
            }
            tempStr += "\n";
        }
        for (int i = 0; i < simulationTimes.length; i++)
        {
            tempStr += "(simulation) i = " + i + " --> ";
            for (int j = 0; j < simulationTimes[i].length; j++)
            {
                tempStr += simulationTimes[i][j] + " ";
            }
            tempStr += "\n";
        }
        addToMessages(tempStr, SchedulingConstants.MESSAGE_TYPE_INFO);
        tempStr = "(path points): ";
        for (double[] teff : pathPoints)
        {
            tempStr += "[";
            for (int i = 0; i < teff.length; i++)
            {
                tempStr += teff[i] + " ";
            }
            tempStr = tempStr.trim() + "] ";           
        }
        addToMessages(tempStr, SchedulingConstants.MESSAGE_TYPE_INFO);
        tempStr = "(mutex limits): \n";
        for (int i = 0; i < mutexLimitsNew.length; i++)
        {
            for (int j = 0; j < mutexLimitsNew[i].length; j++)
            {
                tempStr += "i = " + i + ", j = " + j + ": ";
                for (double[] mutlim : mutexLimitsNew[i][j])
                {
                    tempStr += "[";
                    for (int k = 0; k < mutlim.length; k++)
                    {
                        tempStr += mutlim[k] + " ";
                    }
                    tempStr = tempStr.trim() + "] ";           
                }
            }
        }   
        addToMessages(tempStr, SchedulingConstants.MESSAGE_TYPE_ERROR); 
        
        
        // Creates a test problem. This is a temporary construction
//        makeTestPath();

        // Checks for each pair of points (except for consecutive points) 
        // whether they can see eachother.
        // This is needed only during the development phase (to see what happens with our small example)
//        for (int i=0; i<pathPoints.size()-2; i++)
//        {
//            for (int j=i+2; j<pathPoints.size(); j++)
//            {
//                String str = "";
//
//                for (int k=0; k<pathPoints.get(i).length; k++)
//                {
//                        str += pathPoints.get(i)[k] + " ";
//                }
//                str += "---> ";
//
//                for (int k=0; k<pathPoints.get(j).length; k++)
//                {
//                        str += pathPoints.get(j)[k] + " ";
//                }
//
//                if (areVisible(pathPoints.get(i), pathPoints.get(j)))
//                {
//                        str += "see eachother!!!!!!";
//                        addToMessages(str, SchedulingConstants.MESSAGE_TYPE_WARN);
//                }
//            }
//        }

        // Finds the key points, i.e. points allowing to decrease the number of robot stops 
        findKeyPoints();

        addToMessages("", SchedulingConstants.MESSAGE_TYPE_WARN);
        addToMessages("Initial key points:", SchedulingConstants.MESSAGE_TYPE_WARN);
        String str = "";
        for (int i=0; i<keyPoints.size(); i++)
        {
            for (int j=0; j<keyPoints.get(i).length; j++)
            {
                str += keyPoints.get(i)[j] + " ";
            }
            str += "---> ";
        }
        str += "KLART!!!";
        addToMessages(str, SchedulingConstants.MESSAGE_TYPE_WARN);
        
        // Loops through the key points and smooth out the robot velocities even more when possible
//        double[][] relativeVelocities = improveKeyPointsUsingVisibilitySmoothing();
//        int stateCounter = 0;
//        State[] currStates = new State[optimalSubPlants.size()];
//        for (int i=0; i<optimalSubPlants.size(); i++)
//        {
//            currStates[i] = optimalSubPlants.getAutomatonAt(i).getInitialState();
//        }
//        for (int i = 1; i < keyPointIndices.size(); i++)
//        {
//            while (stateCounter++ < keyPointIndices.get(i))
//            {
//                for (int j = 0; j < optimalSubPlants.size(); j++)
//                {
//                    if (!currStates[j].isAccepting() || stateCounter <= 1)
//                    {
//                        currStates[j].setName(currStates[j].getName() + "; velocity=" + relativeVelocities[i][j]);
//                        currStates[j] = currStates[j].outgoingArcsIterator().next().getToState();
//                    }
//                }
//            }
//        }
        
        // Prepare optimalSubPlants for the addition of the velocity profiles.
        // This is done by looping through keyPoints and recording which states
        // have the same velocities.
        double[][] relativeVelocities = improveKeyPointsUsingVisibilitySmoothing();
        setRelativeVelocitiesInPlants(relativeVelocities);

        // Gathers some statistics about the velocity changes (smooth schedule)
        calcVelocityStatisticsForVisibilitySmoothing(keyPoints);

        addToMessages("", SchedulingConstants.MESSAGE_TYPE_WARN);
        addToMessages("SINGLE-EVENT BALANCING.....", SchedulingConstants.MESSAGE_TYPE_WARN);
        calcVelocityStatisticsForEventSmoothing();

        addToMessages("", SchedulingConstants.MESSAGE_TYPE_WARN);
        addToMessages("UNPROCESSED SCHEDULE.....", SchedulingConstants.MESSAGE_TYPE_WARN);
        calcVelocityStatisticsForUnprocessedSchedule();
        
        addToMessages("Smoooth gliding.......", SchedulingConstants.MESSAGE_TYPE_WARN);	
    }
    
    private void init(Automata theAutomata)
        throws Exception
    {
        // Extract the schedule automaton from the supplied supervisor automata
        if (theAutomata.getSupervisorAutomata().size() == 1)
        {
            schedule = theAutomata.getSupervisorAutomata().getAutomatonAt(0);
        }
        else 
        {
            for (Automaton supAuto : theAutomata.getSupervisorAutomata())
            {
                State initState = supAuto.getInitialState();
                if (initState.getName().contains(("firing_time")))
                {
                    schedule = supAuto;
                }
            }
        }
        // If the search for schedule was unsuccessful, no balancing can be done...
        if (schedule == null)
        {
            throw new Exception("No schedule found in the supplied automata -> Velocity balancing is impossible.");
        }
        
        plants = theAutomata.getPlantAutomata();
        specs = theAutomata.getSpecificationAutomata(); 
        Automata powerlessSpecs = new Automata();
        for (Automaton spec : specs)
        {
            // We don't need to care about single-state specs, while zone specs are taken care of further down the code
            if (spec.nbrOfStates() == 2)
            {
                // If the non-initial state has outgoing transition, then this is a prec spec.
                // Such specs should be taken care of, but it is not done yet -> TODO...
                if (spec.getInitialState().nextStateIterator().next().nbrOfOutgoingArcs() > 0)
                {                
                    throw new Exception("The velocity balancing is not implemented for specifications of other type that zones. " +
                        "(Note that zones should have at least three states).");
                }
                else
                {
                    powerlessSpecs.addAutomaton(spec);
                }
            }
        }
        // We don't need to bother about the specifications that cannot cause 
        // deadlocks at this point, so we remove them. 
        specs.removeAutomata(powerlessSpecs);
        
        indexMap = new AutomataIndexMap(plants);
        
        plantIndexMapping = new int[plants.size()];
        for (int i = 0; i < plants.size(); i++)
        {
            plantIndexMapping[i] = indexMap.getAutomatonIndex(plants.getAutomatonAt(i));
        }
        
        firingTimes = new double[plants.size()][];
        simulationTimes = new double[plants.size()][];
        pathPoints = new ArrayList<double[]>();
        keyPointIndices = new ArrayList<Integer>();
        
        mutexLimitsNew = new ArrayList[specs.size()][plants.size()];
        for (int i = 0; i < mutexLimitsNew.length; i++)
        {
            for (int j = 0; j < mutexLimitsNew[i].length; j++)
            {
                mutexLimitsNew[i][j] = new ArrayList<double[]>();
            }
        }
        
        // Retrieve the parts of the original plants that are involved in the 
        // optimal schedule by partitioning the schedule into individual plants 
        // during the walk through the states of the optimal schedule. 
        optimalSubPlants = new Automata();
        for (int i = 0; i < plants.size(); i++)
        {
            Automaton currOptimalSubPlant = new Automaton(indexMap.getAutomatonAt(plantIndexMapping[i]).getName() + "_optimal");
            currOptimalSubPlant.setType(AutomatonType.PLANT);
            State initialState = new State("q0");
            initialState.setInitial(true);
            initialState.setAccepting(true);
            currOptimalSubPlant.addState(initialState);
            optimalSubPlants.addAutomaton(currOptimalSubPlant);
        }
    }

    /**
     * This method searches through the optimal schedule and extracts the optimal
     * firing times of the scheduled events. Also minimal residence times for 
     * these events are collected.
     */
    private void extractFiringTimes()
        throws Exception
    {
        // The sum of schedule costs from the initial state to the current state
        double currFiringTime = 0;
        
        // The array containing current path-point-values for each plant (these
        // values are denoted by T_{eff} in the velocity balancing paper).
        double[] currPathPoint = new double[plants.size()];
        for (int i = 0; i < currPathPoint.length; i++)
        {
            currPathPoint[i] = 0;
        }
        pathPoints.add(currPathPoint);
        
        // Temporary arrays of firing and simulations times (we need arrays since we don't know 
        // exactly how many events correspond to each robot are in the optimal schedule). 
        ArrayList<Double>[] firingTimesArrays = new ArrayList[plants.size()];
        ArrayList<Double>[] simulationTimesArrays = new ArrayList[plants.size()];
        for (int i = 0; i < firingTimesArrays.length; i++)
        {
            firingTimesArrays[i] = new ArrayList<Double>();
            simulationTimesArrays[i] = new ArrayList<Double>();            
        }

        // The plant states that build up the currently examimed schedule state, 
        // starting with the initial of course. 
        State[] currPlantStates = new State[plants.size()];
        for (int plantIndex = 0; plantIndex < currPlantStates.length; plantIndex++)
        {
            currPlantStates[plantIndex] = indexMap.getAutomatonAt(plantIndexMapping[plantIndex]).getInitialState();
        }
        
        double[] remainingTimesInState = new double[plants.size()];
        for (int i = 0; i < remainingTimesInState.length; i++)
        {
            remainingTimesInState[i] = plants.getAutomatonAt(plantIndexMapping[i]).getInitialState().getCost();
        }
     
        // Find a firing/simulation time for each schedule state, start from the initial state.
        State scheduleState = schedule.getInitialState();
        int statesLeftToCheck = schedule.nbrOfStates(); 
        while (statesLeftToCheck-- > 0)
        {            
            if (scheduleState.nbrOfOutgoingMultiArcs() > 1)
            {
                throw new Exception("Velocity balancing not implemented for a " +
                        "schedule with uncontrollable alternatives");
            }
            else
            {
                for (Iterator<Arc> arcIt = scheduleState.outgoingArcsIterator(); arcIt.hasNext();)
                {
                    LabeledEvent currEvent = arcIt.next().getEvent();
                    double currCost = -1;
                    int activeAutomatonIndex = -1;
                    
                    for (int plantIndex = 0; plantIndex < currPlantStates.length; plantIndex++)
                    {
                        String str = indexMap.getAutomatonAt(plantIndex).getName();
                        Alphabet currPlantEvents = currPlantStates[plantIndex].activeEvents(false);
                        if (currPlantEvents.contains(currEvent))
                        {
                            // Record the index of the automaton that contains the currently scheduled event
                            activeAutomatonIndex = plantIndex;
                            
                            // Update the current and the accumulated cost of the schedule state.
                            currCost = scheduleState.getCost();
                            currFiringTime += currCost;
                                    
                            firingTimesArrays[plantIndex].add(currFiringTime);
                            simulationTimesArrays[plantIndex].add(currPlantStates[plantIndex].getCost());
                                                        
                            scheduleState = scheduleState.nextState(currEvent);
                            currPlantStates[plantIndex] = currPlantStates[plantIndex].nextState(currEvent);
                            
                            break;
                        }       
                    }
                    
                    if (currCost == -1)
                    {
                        throw new Exception("The scheduled event (" + currEvent.getLabel() + ") not found in any plant.");
                    }
                    
                    // Create a new path point and adjust its values by letting each plant 
                    // run as long as possible, i.e. as long as either the cost of the 
                    // current scheduled event or the Tv-value (currRemainingTimeInState)
                    // "prevents" the plant from moving further through its cycle. 
                    // Of course, when treating the plant that fired the schedule event, 
                    // we know that the time update is equal to current cost of the schedule state.                    
                    double[] newPathPoint = new double[plants.size()];
                    for (int i = 0; i < currPathPoint.length; i++)
                    {                        
                        newPathPoint[i] = currPathPoint[i] + Math.min(currCost, remainingTimesInState[i]);
                        
                        if (i != activeAutomatonIndex)
                        {
                            remainingTimesInState[i] = Math.max(0, remainingTimesInState[i] - currCost);
                        }
                        else
                        {
                            remainingTimesInState[activeAutomatonIndex] = currPlantStates[activeAutomatonIndex].getCost();
                        }
//                        if (i == activeAutomatonIndex) // Update the active automaton
//                        {
//                            newPathPoint[i] = currPathPoint[i] + currCost;
//                        }
//                        else // Update inactive automata using the minimal of 
                            // the current schedule state cost and the remaining time in plant state
//                        {
//                            double prevPlantFiringTime = 0;
//                            if (firingTimesArrays[i].size() > 0)
//                            {
//                                prevPlantFiringTime = firingTimesArrays[i].get(firingTimesArrays[i].size() - 1);
//                            }
//                            double remainingTimeInState = Math.max(0, 
//                                    currPlantStates[i].getCost() - (currFiringTime - currCost - prevPlantFiringTime));
//                            newPathPoint[i] = currPathPoint[i] + Math.min(currCost, remainingTimeInState);
//                        }
                    }
                    
                    // Add the newly created path point to the list, avoiding repetitions of identical points
                    for (int i = 0; i < currPathPoint.length; i++)
                    {
                        if (currPathPoint[i] != newPathPoint[i])
                        {
                            currPathPoint = newPathPoint;
                            pathPoints.add(currPathPoint); 
                            break;
                        }
                    }
                    
                    // Find mutexLimits. This is done by finding the specification/zone that contains the 
                    // current schedule event and record the event execution time relative to the individual 
                    // robot cycle, i.e. sum(T(q_{i}^{P_active})), where q_{i}^{P_active} range from 
                    // q_{initial}^{P_active} up to and including q_{current}^{P_active} (this sum is stored in currPathPoint).
                    // If there is already an unfinished mutexLimit for current spec-plant-pair (last value of 
                    // mutexLimit-double[] is equal to -1), the current event represents unbooking. 
                    // Thus, the unfinished mutexLimit-double[] is completed. Otherwise a new mutexLimit-double[], 
                    // with the last value set to -1, is created.
                     for (int i = 0; i < specs.size(); i++)
                    {
                        if (specs.getAutomatonAt(i).getAlphabet().contains(currEvent))
                        {
                            int nrOfAddedMutexLimits = mutexLimitsNew[i][activeAutomatonIndex].size();
                            if ((nrOfAddedMutexLimits > 0) && (mutexLimitsNew[i][activeAutomatonIndex].get(nrOfAddedMutexLimits - 1)[1] == -1))
                            {
                                mutexLimitsNew[i][activeAutomatonIndex].get(nrOfAddedMutexLimits - 1)[1] = currPathPoint[activeAutomatonIndex];
                            }
                            else 
                            {
                                mutexLimitsNew[i][activeAutomatonIndex].add(new double[]{currPathPoint[activeAutomatonIndex], -1});
                            } 
                        }
                    }
                    
                    // Add the current event to corresponding optimal plant part
                    Automaton currOptimalSubPlant = optimalSubPlants.getAutomatonAt(plantIndexMapping[activeAutomatonIndex]);
                    State fromState = currOptimalSubPlant.getStateWithName("q" + (currOptimalSubPlant.nbrOfStates() - 1));
                    fromState.setCost(simulationTimesArrays[activeAutomatonIndex].get(
                            simulationTimesArrays[activeAutomatonIndex].size() - 1));
                    State toState = new State("q" + currOptimalSubPlant.nbrOfStates());
                    currOptimalSubPlant.addState(toState);
                    if (!currOptimalSubPlant.getAlphabet().contains(currEvent))
                    {
                        currOptimalSubPlant.getAlphabet().addEvent(currEvent);
                    }
                    currOptimalSubPlant.addArc(new Arc(fromState, toState, currEvent));
                }
            }
        }
        
        // Connect the currOptimalSubPlant as a loop by redirecting the last 
        // transition to the initial state
        for (int i = 0; i < optimalSubPlants.size(); i++)
        {
            Automaton currOptimalSubPlant = optimalSubPlants.getAutomatonAt(plantIndexMapping[i]);
            State fromState = currOptimalSubPlant.getStateWithName("q" + (currOptimalSubPlant.nbrOfStates() - 2));
            State prevToState = currOptimalSubPlant.getStateWithName("q" + (currOptimalSubPlant.nbrOfStates() - 1));
            currOptimalSubPlant.addArc(new Arc(fromState, currOptimalSubPlant.getInitialState(), 
                    fromState.outgoingArcsIterator().next().getEvent()));
            currOptimalSubPlant.removeState(prevToState);
        }        
        
        Automata plantsAndSpecs = new Automata(optimalSubPlants);
        plantsAndSpecs.addAutomata(specs);
        findDeadlockLimits(plantsAndSpecs);
        
        // Transfer the info about firing and simulation times from temporary containers
        // into more lasting ones. 
        for (int i = 0; i < firingTimes.length; i++)
        {
            firingTimes[i] = new double[firingTimesArrays[i].size()];
            simulationTimes[i] = new double[simulationTimesArrays[i].size()];
            for (int j = 0; j < firingTimes[i].length; j++)
            {
                firingTimes[i][j] = firingTimesArrays[i].get(j).doubleValue();
                simulationTimes[i][j] = simulationTimesArrays[i].get(j).doubleValue();
            }
        }   
    }
    
    private void findDeadlockLimits(Automata plantsAndSpecs)
        throws Exception
    {        
        AutomataSynchronizer synchronizer = new AutomataSynchronizer(plantsAndSpecs, SynchronizationOptions.getDefaultSynchronizationOptions());
        synchronizer.execute();
        Automaton synthAuto = synchronizer.getAutomaton();
        synthAuto.setName("Plants||Specs");
        synthAuto.setType(AutomatonType.PLANT);
                
        AutomataIndexMap synthIndexMap = new AutomataIndexMap(new Automata(synthAuto));
        
        // Contains the indices of allowed states that lead to a forbidden state in one transition,
        // together with the indices of the events leading to a forbidden state, i.e. one entry is 
        // [border_allowed_state_index, event_leading_to_forbidden_state_index].
        IntArrayTreeSet borderAllowedStates = new IntArrayTreeSet();
        
        ArrayList<State> listOfForbiddenRegionRoots = new ArrayList<State>();
        for (Iterator<State> stateIt = synthAuto.stateIterator(); stateIt.hasNext();)
        {
            State state = stateIt.next();
            if (state.isForbidden())
            { 
                boolean isRootOfForbiddenRegion = true;
                for (Iterator<Arc> incomingArcIt = state.incomingArcsIterator(); incomingArcIt.hasNext();)
                {
                    Arc incomingArc = incomingArcIt.next();
                
                    if (incomingArc.getFromState().isForbidden())
                    {
                        isRootOfForbiddenRegion = false;
                    }
                    else
                    {
                        borderAllowedStates.add(new int[]{synthIndexMap.getStateIndex(synthAuto, incomingArc.getFromState()), 
                            synthIndexMap.getEventIndex(incomingArc.getEvent())});
                    }
                }
                
                if (isRootOfForbiddenRegion)
                {   
                    listOfForbiddenRegionRoots.add(state);
                }
            }
        }
        
        // Create the deadlock limit array
        deadlockLimitsNew = new ArrayList[listOfForbiddenRegionRoots.size()][plants.size()];
        
        for (int j = 0; j < listOfForbiddenRegionRoots.size(); j++)
        {
            for (int k = 0; k < deadlockLimitsNew[j].length; k++)
            {
                deadlockLimitsNew[j][k] = new ArrayList<double[]>();
            }
            
            State state = listOfForbiddenRegionRoots.get(j);
                
//                    // This array contains true values for each plant that is involved in a deadlock in current forbidden state
//                    boolean[] isLockedPlant = new boolean[plants.size()];
//                    for (int i = 0; i < isLockedPlant.length; i++)
//                    {
//                        isLockedPlant[i] = false;
//                    }
//                    // Set isLockedPlant[i] to true if there is an event belonging to plants[i] leading to the root of 
//                    // the current forbidden region
//                    for (Iterator<Arc> incomingArcIt = state.incomingArcsIterator(); incomingArcIt.hasNext();)
//                    {
//                        LabeledEvent incomingEvent = incomingArcIt.next().getEvent();
//                        for (int i = 0; i < plants.size(); i++)
//                        {
//                            if (!isLockedPlant[i]) // No need to check plants that are already detected to be involved in deadlock
//                            {
//                                if (plants.getAutomatonAt(i).getAlphabet().contains(incomingEvent))
//                                {
//                                    isLockedPlant[i] = true;
//                                }
//                            }
//                        }
//                    }


//            double[] minDLLimit = new double[plants.size()];
//            double[] maxDLLimit = new double[plants.size()];
//            for (int i = 0; i < minDLLimit.length; i++)
//            {
//                minDLLimit[i] = Double.MAX_VALUE;
//                maxDLLimit[i] = 0;
//            }

            for (Iterator<Arc> incomingArcIt = state.incomingArcsIterator(); incomingArcIt.hasNext();)
            {
                Arc incomingArc = incomingArcIt.next();
                LabeledEvent incomingEvent = incomingArc.getEvent();

                for (int i = 0; i < plants.size(); i++)
                {
                    Automaton auto = plantsAndSpecs.getPlantAutomata().getAutomatonAt(plantIndexMapping[i]);
                    if (auto.getAlphabet().contains(incomingArc.getEvent()))
                    {
                        for (Iterator<State> stIt = auto.stateIterator(); stIt.hasNext();)
                        {
                            State stateInAuto = stIt.next();
                            if (stateInAuto.activeEvents(false).contains(incomingEvent))
                            {
                                double currMinDLLimit = 0;

                                //Assumes that the number of incoming transitions is one in this state
                                while (!stateInAuto.isInitial())
                                {
                                    currMinDLLimit += stateInAuto.getCost();
                                    stateInAuto = stateInAuto.incomingArcsIterator().next().getFromState();
                                }   
                                currMinDLLimit += stateInAuto.getCost();

                                int nrOfAddedDLLimits = deadlockLimitsNew[j][i].size();
                                if ((nrOfAddedDLLimits > 0) && (deadlockLimitsNew[j][i].get(nrOfAddedDLLimits - 1)[0] == -1))
                                {
                                    deadlockLimitsNew[j][i].get(nrOfAddedDLLimits - 1)[0] = currMinDLLimit;
                                }
                                else 
                                {
                                    deadlockLimitsNew[j][i].add(new double[]{currMinDLLimit, -1});
                                } 
                                
//                                if (minDLLimit[i] > currMinDLLimit)
//                                {
//                                    minDLLimit[i] = currMinDLLimit;
//                                }
                            }
                        }
                    }
                }                        

                ArrayList<State> stackOfAllowedStates = new ArrayList<State>();
                stackOfAllowedStates.add(incomingArc.getFromState());
                while (!stackOfAllowedStates.isEmpty())
                {
                    State allowedState = stackOfAllowedStates.remove(0);
                    for (Iterator<Arc> arcIt = allowedState.outgoingArcsIterator(); arcIt.hasNext();)
                    {
                        Arc arc = arcIt.next();
                        if (!arc.getEvent().equals(incomingEvent))
                        {
                            State nextState = arc.getToState();
                            if (borderAllowedStates.get(new int[]{synthIndexMap.getStateIndex(synthAuto, nextState),
                                                                    synthIndexMap.getEventIndex(incomingArc.getEvent())}) != null)
                            {
                                stackOfAllowedStates.add(nextState);
                            }
                            else
                            {
                                for (int i = 0; i < plants.size(); i++)
                                {
                                    Automaton auto = plantsAndSpecs.getPlantAutomata().getAutomatonAt(plantIndexMapping[i]);
                                    if (auto.getAlphabet().contains(arc.getEvent()))
                                    {
                                        for (Iterator<State> stIt = auto.stateIterator(); stIt.hasNext();)
                                        {
                                            State stateInAuto = stIt.next();
                                            if (stateInAuto.activeEvents(false).contains(arc.getEvent()))
                                            {
                                                double currMaxDLLimit = 0;

                                                // Assumes that the number of incoming transitions is one in this state
                                                while (!stateInAuto.isInitial())
                                                {
                                                    currMaxDLLimit += stateInAuto.getCost();
                                                    stateInAuto = stateInAuto.incomingArcsIterator().next().getFromState();
                                                }   
                                                currMaxDLLimit += stateInAuto.getCost();

                                                int nrOfAddedDLLimits = deadlockLimitsNew[j][i].size();
                                                if ((nrOfAddedDLLimits > 0) && (deadlockLimitsNew[j][i].get(nrOfAddedDLLimits - 1)[1] == -1))
                                                {
                                                    deadlockLimitsNew[j][i].get(nrOfAddedDLLimits - 1)[1] = currMaxDLLimit;
                                                }
                                                else 
                                                {
                                                    deadlockLimitsNew[j][i].add(new double[]{-1, currMaxDLLimit});
                                                } 
                                                
//                                                if (maxDLLimit[i] < currMaxDLLimit)
//                                                {
//                                                    maxDLLimit[i] = currMaxDLLimit;
//                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }  
            }
            
//            String dlStr = "State " + state.getName() + " is the root of forbidden region...\n";
//            for (int i = 0; i < maxDLLimit.length; i++)
//            {
//                dlStr += "\t[" + minDLLimit[i] + " " + maxDLLimit[i] + "]";
//            }
//            logger.info(dlStr);
        } 
    }

    /**
     * TEST-ing to add better key points, in order to reduce stop time.
     */
    private void tryNewKeyPoint(double[] newKeyPoint, double[] pointBefore, double[] pointAfter)
    {
        addToMessages("", SchedulingConstants.MESSAGE_TYPE_WARN);

        // Looking backward
        String str = "";
        for (int k=0; k<pointBefore.length; k++)
        {
            str += pointBefore[k] + " ";
        }
        str += "---> ";

        for (int k=0; k<newKeyPoint.length; k++)
        {
            str += newKeyPoint[k] + " ";
        }

        if (areVisible(pointBefore, newKeyPoint))
        {
            str += "see eachother!!!!!!";
            addToMessages(str, SchedulingConstants.MESSAGE_TYPE_WARN);
        }

        // Looking forward
        str = "";
        for (int k=0; k<newKeyPoint.length; k++)
        {
            str += newKeyPoint[k] + " "; 
        }
        str += "---> ";

        for (int k=0; k<pointAfter.length; k++)
        {
            str += pointAfter[k] + " ";
        }
        str += "---> ";

        if (areVisible(newKeyPoint, pointAfter))
        {
            str += "see eachother!!!!!!";
            addToMessages(str, SchedulingConstants.MESSAGE_TYPE_WARN);
        }
        // ...done (testing to add a better key point)
    }

    /**
     * Smooths out the velocities between each robot event
     * and calculates corresponding statistics.
     */
    private void calcVelocityStatisticsForEventSmoothing()
    {
        // here, relativeVelocities[robot_index][state_index]
        double[][] relativeVelocities = new double[simulationTimes.length][];

        for (int i=0; i<simulationTimes.length; i++)
        {
            String str = "";

            // The first element of relativeVelocities is zero (the robots are stopped)
            relativeVelocities[i] = new double[simulationTimes[i].length + 1];

            if (firingTimes[i][0] == 0)
            {
                relativeVelocities[i][1] = 0;
            }
            else
            {
                relativeVelocities[i][1] = simulationTimes[i][0] / firingTimes[i][0];
            }
            str += roundOff(relativeVelocities[i][1], 2) + " ";

            for (int j=1; j<simulationTimes[i].length; j++)
            {
                if (firingTimes[i][j] == firingTimes[i][j-1])
                {
                    relativeVelocities[i][j+1] = relativeVelocities[i][j];
                }
                else
                {
                    relativeVelocities[i][j+1] = simulationTimes[i][j] / (firingTimes[i][j] - firingTimes[i][j-1]);
                }
                
                str += roundOff(relativeVelocities[i][j+1], 2) + " ";
            }

            addToMessages("\t\t Rel. velocities per event in Rob_" + i + " : "+ str, SchedulingConstants.MESSAGE_TYPE_WARN);
        }

        // COPY-PASTE
        double[] nrOfMaxVelocityPassages = new double[relativeVelocities.length];
        double[] nrOfMinVelocityPassages = new double[relativeVelocities.length];
        double[] nrOfVelocityChanges = new double[relativeVelocities.length];
        double[] totalVelocityChange = new double[relativeVelocities.length];
        double[] meanVelocityChange = new double[relativeVelocities.length];

        addToMessages("Single-event balancing statistics: ", SchedulingConstants.MESSAGE_TYPE_WARN);
        for (int i=0; i<relativeVelocities.length; i++)
        {
            for (int j=0; j<relativeVelocities[i].length; j++)
            {
                // Counting the number of times when the robot is stopped 
                // or moving at its maximal speed (excepting the initial position)
                if (j>0) 
                {
                    if (roundOff(relativeVelocities[i][j], 2) == 0.0)
                    {
                        nrOfMinVelocityPassages[i]++;
                    }
                    else if (roundOff(relativeVelocities[i][j], 2) == 1.0)
                    {
                        nrOfMaxVelocityPassages[i]++;
                    }
                } 

                double velocityChange;
                if (j < relativeVelocities[i].length - 1)
                {
                    velocityChange = Math.abs(relativeVelocities[i][j+1] - relativeVelocities[i][j]);					
                }
                // don't forget the final decelaration of robots to a stop
                else
                {
                    velocityChange = relativeVelocities[i][j];
                }

                if (velocityChange > 0)
                {
                    nrOfVelocityChanges[i]++;
                }

                totalVelocityChange[i] += velocityChange;
            }

            meanVelocityChange[i] = totalVelocityChange[i] / nrOfVelocityChanges[i];

            // Round off the velocity changes for better presentation
            totalVelocityChange[i] = roundOff(totalVelocityChange[i], 2); 			
            meanVelocityChange[i] = roundOff(meanVelocityChange[i], 2);

            addToMessages("\t\t Rob_" + i + "... total change: " + totalVelocityChange[i] + 
                    "; nr of changes: " + nrOfVelocityChanges[i] + "; mean change: " + 
                    meanVelocityChange[i] + "; nr of stops: " + nrOfMinVelocityPassages[i] + 
                    "; nr of full-speed-runs: " + nrOfMaxVelocityPassages[i], 
                    SchedulingConstants.MESSAGE_TYPE_WARN);
        }
        // COPY-PASTE-DONE
    }

    /**
     * Calculates the velocity statistics for the unprocessed schedule,
     * i.e. when robots are moving at maximum speed until they suddenly
     * have to stop and let other robots pass by. 
     */
    private void calcVelocityStatisticsForUnprocessedSchedule()
    {
        double[] nrOfMaxVelocityPassages = new double[pathPoints.get(0).length];
        double[] nrOfMinVelocityPassages = new double[pathPoints.get(0).length];
        double[] nrOfVelocityChanges = new double[pathPoints.get(0).length];
        double[] totalVelocityChange = new double[pathPoints.get(0).length];
        double[] meanVelocityChange = new double[pathPoints.get(0).length];

        for (int plantIndex = 0; plantIndex < optimalSubPlants.size(); plantIndex++)
        {
            double accTime = 0;
            int firingTimeIndex = 0;
            State state = optimalSubPlants.getAutomatonAt(plantIndex).getInitialState();
            State nextState = state;
            String velPerEventStr = "(0) ";
            do
            {
                nextState = state.nextStateIterator().next();
                accTime += state.getCost();
                if (accTime == firingTimes[plantIndex][firingTimeIndex])
                {
                    velPerEventStr += "1.0 ";
                }
                else 
                {
                    velPerEventStr += "1.0->0.0 ";
                    accTime = firingTimes[plantIndex][firingTimeIndex];
                }
                
                firingTimeIndex++;
                state = nextState;
            } while (!nextState.isAccepting());
            addToMessages("\t\t Rel. velocities per event in Rob_" + plantIndex + " : " + velPerEventStr + "(0)",
                    SchedulingConstants.MESSAGE_TYPE_WARN);
        }
        
        addToMessages("Unprocessed schedule statistics:", SchedulingConstants.MESSAGE_TYPE_WARN);
        for (int j=0; j<pathPoints.get(0).length; j++)
        {
            boolean lastVelocityWasZero = true;

            for (int i=0; i<pathPoints.size()-1; i++)
            {
                double timeDiff;

                timeDiff = roundOff(pathPoints.get(i+1)[j] - pathPoints.get(i)[j], 2);

                // If robot_j has moved, it has moved with the maximum relative velocity, i.e. v=1 
                // (in the unprocessed case). Otherwise, the robot is stopped, i.e. v=0.
                if (timeDiff > 0.0)
                {				
                    // As said, robot_j is moving at its maximum speed
                    nrOfMaxVelocityPassages[j]++;

                    // If the velocity has changed since the previous passage, update the change counters
                    if (lastVelocityWasZero)
                    {
                        totalVelocityChange[j]++;
                        nrOfVelocityChanges[j]++;
                    }

                    lastVelocityWasZero = false;
                }
                else 
                {
                    // Else, robot_j is stopped
                    nrOfMinVelocityPassages[j]++;

                    if (!lastVelocityWasZero)
                    {
                        nrOfVelocityChanges[j]++;
                        totalVelocityChange[j]++;
                    }

                    lastVelocityWasZero = true;
                }
            }

            // If the last velocity before cycle stop was positive, update change counters
            if (!lastVelocityWasZero)
            {
                nrOfVelocityChanges[j]++;
                totalVelocityChange[j]++;
            }

            meanVelocityChange[j] = totalVelocityChange[j] / nrOfVelocityChanges[j];

            addToMessages("\t\t Rob_" + j + "... total change: " + totalVelocityChange[j] + 
                    "; nr of changes: " + nrOfVelocityChanges[j] + "; mean change: " + 
                    meanVelocityChange[j] + "; nr of stops: " + nrOfMinVelocityPassages[j] + 
                    "; nr of full-speed-runs: " + nrOfMaxVelocityPassages[j], 
                    SchedulingConstants.MESSAGE_TYPE_WARN);
        }
    }

    /**
     * Loops through the key points and calculates some statistics 
     * about the velocity changes, such as the total number of velocity changes,
     * as well as the total and mean amount of velocity change.
     */
    private void calcVelocityStatisticsForVisibilitySmoothing(ArrayList<double[]> points)
    {
        double[] timeToPrevKeyPoint = calcTimeToPreviousPoint(points);
        double[][] relativeVelocities = calcRelativeVelocities(points, timeToPrevKeyPoint);

        double[] nrOfMaxVelocityPassages = new double[points.get(0).length];
        double[] nrOfMinVelocityPassages = new double[points.get(0).length];
        double[] nrOfVelocityChanges = new double[points.get(0).length];
        double[] totalVelocityChange = new double[points.get(0).length];
        double[] meanVelocityChange = new double[points.get(0).length];

        addToMessages("Multi-balanced statistics: ", SchedulingConstants.MESSAGE_TYPE_WARN);
        for (int j=0; j<relativeVelocities[0].length; j++)
        {
            for (int i=0; i<relativeVelocities.length; i++)
            {
                // Counting the number of times when the robot is stopped (excepting the initial position)
                if ((i>0) && (roundOff(relativeVelocities[i][j], 2) == 0.0))
                {
                    nrOfMinVelocityPassages[j]++;
                } 

                if (roundOff(relativeVelocities[i][j], 2) == 1.0)
                {
                    nrOfMaxVelocityPassages[j]++;
                }

                double velocityChange;

                if (i < relativeVelocities.length - 1)
                {
                    velocityChange = Math.abs(relativeVelocities[i+1][j] - relativeVelocities[i][j]);					
                }
                // don't forget the final decelaration of robots to a stop
                else
                {
                    velocityChange = relativeVelocities[i][j];
                }

                if (velocityChange > 0)
                {
                    nrOfVelocityChanges[j]++;
                }

                totalVelocityChange[j] += velocityChange;
            }

            meanVelocityChange[j] = totalVelocityChange[j]/nrOfVelocityChanges[j];

            // Round off the velocity changes for better presentation
            totalVelocityChange[j] = roundOff(totalVelocityChange[j], 2); 			
            meanVelocityChange[j] = roundOff(meanVelocityChange[j], 2);

            addToMessages("\t\t Rob_" + j + "... total change: " + totalVelocityChange[j] + 
                    "; nr of changes: " + nrOfVelocityChanges[j] + "; mean change: " + 
                    meanVelocityChange[j] + "; nr of stops: " + nrOfMinVelocityPassages[j] + 
                    "; nr of full-speed-runs: " + nrOfMaxVelocityPassages[j], 
                    SchedulingConstants.MESSAGE_TYPE_WARN);
        }
    }

    /**
     * Calculates the (maximal) time differeces between the supplied points.
     * Each point "keeps track" of the distance to the previous point.
     *
     * @param a list of points
     * @return an array of time differences between the points
     */
    private double[] calcTimeToPreviousPoint(ArrayList<double[]> points)
    {
        // (Maximal) times to the previous key point are collected 
        // for each point (except the first one)
        double[] timeToPrevPoint = new double[points.size()];

        for (int i=1; i<timeToPrevPoint.length; i++)
        {
            timeToPrevPoint[i] = 0;

            for (int j=0; j<points.get(i).length; j++)
            {
                double currTimeDiff = points.get(i)[j] - points.get(i-1)[j];

                if (currTimeDiff > timeToPrevPoint[i])
                {
                    timeToPrevPoint[i] = currTimeDiff;
                }
            }

            // Nicer this way...
            timeToPrevPoint[i] = roundOff(timeToPrevPoint[i], 6);
        }

        return timeToPrevPoint;
    }

    /**
     * Calculates the relative robot velocities between pairs of points.
     * Every point corresponds to a number of velocity values (one per robot) 
     * by which it is reached (relativeVelocities[destination_key_point_index][robot_index]).
     */
    private double[][] calcRelativeVelocities(ArrayList<double[]> points, double[] timeToPrevPoint)
    {
        // Stores the relative robot velocities (max_velocity = 1)
        double[][] relativeVelocities = new double[points.size()][points.get(0).length];

        for (int i=1; i<points.size(); i++)
        {
            for (int j=0; j<points.get(i).length; j++)
            {
                relativeVelocities[i][j] = (points.get(i)[j] - points.get(i-1)[j]) / timeToPrevPoint[i];

                // Nicer this way...
                relativeVelocities[i][j] = roundOff(relativeVelocities[i][j], 6);
            }
        }

        return relativeVelocities;
    }

    /**
     * Goes through the key points and replaces them if possible, 
     * removing unnecessary robot stops. 
     */
    private double[][] improveKeyPointsUsingVisibilitySmoothing()
    {
        // Stores the maximal difference between the elements of this and the previous key point.
        double[] timeToPrevKeyPoint = calcTimeToPreviousPoint(keyPoints);

        // Stores the relative robot velocities (max_velocity = 1)
        double[][] relativeVelocities = calcRelativeVelocities(keyPoints, timeToPrevKeyPoint);

        // The largest velocity change from the previous point to the next is calculated 
        // for every key point. The robot with the largest velocity change is adjusted,
        // such that the velocity to the current and to the next point is set equal. 
        // The key points are then adjusted consequently.
        for (int i=1; i<keyPoints.size()-1; i++)
        {
            // The index of the robot that currently changes its velocity most.
            // If no robot can change its velocity without violating the visibility
            // (and thus collision-avoidance) constraints, this index becomes -1,
            // which terminates the while-loop.
            int maxDiffVelocityIndex;

            // The boolean array, recording whether the robot velocities have been checked
            // without velocity update. Initially, all elements of this array are false. 
            boolean[] checkedIndices = new boolean[keyPoints.get(i).length];

            // The velocities are updated for the current key point robot by robot
            // until no more adjustments can be made without compromising collision-avoidance.
            do 
            {
                double maxDiffVelocity = 0;
                maxDiffVelocityIndex = -1;

                // For each robot (j) that has not been checked yet in this loop...
                for (int j=0; j<keyPoints.get(i).length; j++)
                {
                    if (!checkedIndices[j])
                    {
                        double diffVelocity = Math.abs(relativeVelocities[i][j] - relativeVelocities[i-1][j]) + Math.abs(relativeVelocities[i+1][j] - relativeVelocities[i][j]);				

                        if (diffVelocity > maxDiffVelocity)
                        {
                            maxDiffVelocity = diffVelocity;
                            maxDiffVelocityIndex = j;
                        }
                    }
                }

                // If a  robot for velocity update has been found...
                if (maxDiffVelocityIndex > -1)
                {
                    // Calculate the new and smoother relative velocity of the robot that experiences maximum velocity change 
                    double smoothRelativeVelocity = (keyPoints.get(i+1)[maxDiffVelocityIndex] - keyPoints.get(i-1)[maxDiffVelocityIndex]) / (timeToPrevKeyPoint[i] + timeToPrevKeyPoint[i+1]);

                    // If there was no velocity change (for example if both passages were previously
                    // performed at max speed), there is no need to make the below calculations
                    if (roundOff(smoothRelativeVelocity, 4) == roundOff(relativeVelocities[i][maxDiffVelocityIndex], 4))
                    {
                        checkedIndices[maxDiffVelocityIndex] = true;
                    }
                    else
                    {
                        // Store temporarily the old time value for the robot that experiences maximum velocity change
                        double oldKeyTimePoint = keyPoints.get(i)[maxDiffVelocityIndex];

                        // Update the current key point, taking into account the change of velocity
                        keyPoints.get(i)[maxDiffVelocityIndex] = keyPoints.get(i-1)[maxDiffVelocityIndex] + smoothRelativeVelocity * timeToPrevKeyPoint[i];

                        // Check if the new key point is visible by its neighbours. If it is update 
                        // the relative velocities (remember that the current key point is already updated).
                        if (areVisible(keyPoints.get(i-1), keyPoints.get(i)) && areVisible(keyPoints.get(i), keyPoints.get(i+1)))
                        {
                            relativeVelocities[i][maxDiffVelocityIndex] = smoothRelativeVelocity;
                            relativeVelocities[i+1][maxDiffVelocityIndex] = smoothRelativeVelocity;

                            // Reset checkedIndices, since after the key point update, previously 
                            // impossible key point candidates can become visible. 
                            checkedIndices = new boolean[keyPoints.get(i).length];
                        }
                        // Otherwise, resume the old value of the key point and remember that this 
                        // point has been checked. 
                        else
                        {
                            keyPoints.get(i)[maxDiffVelocityIndex] = oldKeyTimePoint;
                            checkedIndices[maxDiffVelocityIndex] = true;
                        }
                    }
                }
            }
            while (maxDiffVelocityIndex > -1);
        }

        //TEMP
        addToMessages("", SchedulingConstants.MESSAGE_TYPE_WARN);
        addToMessages("MULTI-BALANCED SCHEDULE", SchedulingConstants.MESSAGE_TYPE_WARN);
        
        // Printing out the relative velocities per event
        for (int plantNr = 0; plantNr < optimalSubPlants.size(); plantNr++)
        {
            State state = optimalSubPlants.getAutomatonAt(plantNr).getInitialState();
            State nextState = state.nextStateIterator().next(); // every state should have exactly one successor
            int eff_time = 0;
            int keyPointNr = 1; // Start with the first key point since the 0th is trivial (0 0 0)
            String velPerPlantStr = "";
            do
            {
                nextState = state.nextStateIterator().next();
                
                eff_time += state.getCost(); // * relativeVelocities[keyPointNr][plantNr];
                while (eff_time > keyPoints.get(keyPointNr)[plantNr])
                {
                    keyPointNr++;
                }
                
                velPerPlantStr += roundOff(relativeVelocities[keyPointNr][plantNr], 2) + "(" + state.getName() + ") ";
                state = nextState;
            }
            while (!nextState.isAccepting());
            addToMessages("\t\t Rel. velocities per event in Rob_" + plantNr + " : "+ velPerPlantStr.trim(), 
                    SchedulingConstants.MESSAGE_TYPE_WARN);
        }
        
        addToMessages("Relative velocities per keypoint:", SchedulingConstants.MESSAGE_TYPE_WARN);
        for (int i=0; i<keyPoints.size(); i++)
        {
            String str = "";
            for (int j=0; j<keyPoints.get(0).length; j++)
            {
                str += roundOff(relativeVelocities[i][j], 2) + " ";
            }

            str += " in '";
            if (i > 0)
            {
                for (int j = 0; j < keyPoints.get(i-1).length; j++)
                {
                    str += roundOff(keyPoints.get(i-1)[j], 2) + " ";
                }
                str = str.trim() + "' -> '";
            }
            for (int j = 0; j < keyPoints.get(i).length; j++)
            {
                str += roundOff(keyPoints.get(i)[j], 2) + " ";
            }
            str = str.trim() + "'";
            addToMessages("\t\t " + str, SchedulingConstants.MESSAGE_TYPE_WARN);
        }
        
        addToMessages("Key points (after): ", SchedulingConstants.MESSAGE_TYPE_WARN);
        String str = "";
        for (int i=0; i<keyPoints.size(); i++)
        {
            for (int j=0; j<keyPoints.get(i).length; j++)
            {
                str += roundOff(keyPoints.get(i)[j], 2) + " ";
            }
            str += " --> ";
        }
        addToMessages("\t\t " + str, SchedulingConstants.MESSAGE_TYPE_WARN);
        
        return relativeVelocities;
    }

    /**
     * This method smooths out the schedule by finding its key states.
     * This is done by finding the shortest path in an unweighted graph 
     * using dynamic programming. 
     * (smallest number of steps, that is smallest number of velocity changes).
     */
    private void findKeyPoints()
    {
        // Used to find the smallest number of steps from each point to the goal
        int[] nrOfVelocityChangesBeforeGoal = new int[pathPoints.size()];

        // Used to keep track of the smoothest path from each point to the goal 
        int[] indexOfNextNode = new int[pathPoints.size()];

        // The initialization fase (the goal point is already there)
        nrOfVelocityChangesBeforeGoal[nrOfVelocityChangesBeforeGoal.length-1] = 0;
        indexOfNextNode[indexOfNextNode.length-1] = -1;

        // The backward-search fase
        for (int i=pathPoints.size()-2; i>-1; i--)
        {
            // Next path point is always visible (since it is a part of an allowed schedule)
            nrOfVelocityChangesBeforeGoal[i] = 1 + nrOfVelocityChangesBeforeGoal[i+1];
            indexOfNextNode[i] = i+1;

            // But maybe there is some other visible point, leading to less velocity changes...
            for (int j=i+2; j<pathPoints.size(); j++)
            {
                if (areVisible(pathPoints.get(i), pathPoints.get(j)))
                {
                    if (nrOfVelocityChangesBeforeGoal[i] > 1 + nrOfVelocityChangesBeforeGoal[j])
                    {
                        nrOfVelocityChangesBeforeGoal[i] = 1 + nrOfVelocityChangesBeforeGoal[j];
                        indexOfNextNode[i] = j;
                    }
                }
            }
        }

        // Going from the initial node, all the key nodes are collected using indexOfNextNode-array
        int currKeyPointIndex = 0;
        while (currKeyPointIndex > -1)
        {
            keyPointIndices.add(new Integer(currKeyPointIndex));
            
            // This (ugly) procedure is done to avoid mixing with path- and key points by accident
            double[] currPathPoint = pathPoints.get(currKeyPointIndex);
            double[] currKeyPoint = new double[currPathPoint.length];
            for (int i=0; i<currKeyPoint.length; i++)
            {
                currKeyPoint[i] = currPathPoint[i];
            }

            keyPoints.add(currKeyPoint);
            currKeyPointIndex = indexOfNextNode[currKeyPointIndex];	
        }
    }

    //@Deprecated
//    /**
//     * Checks if there is a straight line between two points, 
//     * that does not cross any zone.
//     *
//     * @param startPoint
//     * @param endPoint
//     * @return true if there is no obstacle on the straight line between the points.
//     */
//    private boolean areVisibleOld(double[] startPoint, double[] endPoint)
//    {
//        // For each mutex zone...
//        for (int i=0; i<mutexLimits.length; i++)
//        {
//            // Start and end times of collisions between the startPoint-endPoint-line and current mutex zone
//            ArrayList<double[]> collisionTimes = getCollisionTimesForZone(startPoint, endPoint, mutexLimits[i]);
//
//            // A collision occurs if (at least) two robots enter the mutex zone at the same time.
//            // The following is a check for common enter/exit time intervals.
//            if (collisionTimes.size() > 1)
//            {
//                // Find common collision time values, by checking
//                // pairwise intersections of all collision times for the current zone
//                for (int k=0; k<collisionTimes.size()-1; k++)
//                {
//                    for (int l=k+1; l<collisionTimes.size(); l++)
//                    {
//                        // if true, the current time intersection is positive, that is there is an obstacle along the road
//                        if ((collisionTimes.get(k)[0] < collisionTimes.get(l)[1]) && (collisionTimes.get(l)[0] < collisionTimes.get(k)[1]))
//                        {
//                            return false;
//                        }
//                    }
//                }
//            }
//        }
//
//        // For each deadlock-zone...
//        for (int i=0; i<deadlockLimits.length; i++)
//        {
//            // Start and end times of collisions between the startPoint-endPoint-line and current deadlock zone
//            ArrayList<double[]> collisionTimes = getCollisionTimesForZone(startPoint, endPoint, deadlockLimits[i]);
//
//            // There can be a circular wait situation only if all robots are inside the deadlock zone at some time...
//            if (collisionTimes.size() == deadlockLimits[i].size())
//            {
//                // The collision times are first initialized to their extreme values
//                double[] commonCollisionTimes = new double[]{0, 1};
//
//                // Next, intersection of all collision times is calculated
//                for (int k=0; k<collisionTimes.size(); k++)
//                {
//                    if (collisionTimes.get(k)[0] > commonCollisionTimes[0])
//                    {
//                        commonCollisionTimes[0] = collisionTimes.get(k)[0];
//                    }
//
//                    if (collisionTimes.get(k)[1] < commonCollisionTimes[1])
//                    {
//                        commonCollisionTimes[1] = collisionTimes.get(k)[1];
//                    }
//                }
//
//                // Finally, if the intersection of collision times is positive, we have a (deadlock) obstacle along the path
//                if (commonCollisionTimes[0] < commonCollisionTimes[1])
//                {
//                    return false;
//                }
//            }
//        }
//
//        // If no common collision time was found for any zone, startPoint and endPoint see eachother
//        return true;
//    }
    
   /**
     * Checks if there is a straight line between two points, 
     * that does not cross any zone.
     *
     * @param startPoint
     * @param endPoint
     * @return true if there is no obstacle on the straight line between the points.
     */
    private boolean areVisible(double[] startPoint, double[] endPoint)
    {
        // For each mutex zone...
        for (int i=0; i<mutexLimitsNew.length; i++)
        {
            // Start and end times of collisions between the startPoint-endPoint-line and current mutex zone
            ArrayList<double[]>[] collisionTimes = getCollisionTimesForZone(startPoint, endPoint, mutexLimitsNew[i]);
            
//            //temp
//            logger.error("Checking zone_" + i + ", " + startPoint[0] + " " + startPoint[1] + " " + startPoint[2] + " --> " + 
//                    endPoint[0] + " " + endPoint[1] + " " + endPoint[2] + ": coll_times = ");
//            for (int j = 0; j < collisionTimes.length; j++)
//            {
//                for (double[] ct : collisionTimes[j])
//                {
//                    logger.error("(plant_index = " + j + ") = " + ct[0] + " " + ct[1]);
//                }
//            }

            for (int j1 = 0; j1 < collisionTimes.length - 1; j1++)
            {
                for (int j2 = j1 + 1; j2 < collisionTimes.length; j2++)
                {
                    // A collision occurs if (at least) two robots enter the mutex zone at the same time.
                    // The following is a check for common enter/exit time intervals.
                    if ((collisionTimes[j1].size() > 0) && (collisionTimes[j2].size() > 0))
                    {
                        // Find common collision time values, by checking pairwise 
                        // intersections (w.r.t. the plants) of all collision times for the current zone
                        for (int k1=0; k1<collisionTimes[j1].size(); k1++)
                        {
                            for (int k2=0; k2<collisionTimes[j2].size(); k2++)
                            {
                                // if true, the current time intersection is positive, that is there is an obstacle along the road
                                if ((collisionTimes[j1].get(k1)[0] < collisionTimes[j2].get(k2)[1]) && 
                                        (collisionTimes[j2].get(k2)[0] < collisionTimes[j1].get(k1)[1]))
                                {
                                    return false;
                                }
                            }
                        }
                    }
                }
            }
        }

        // For each deadlock-zone...
        for (int i=0; i<deadlockLimitsNew.length; i++)
        {
            // Start and end times of collisions between the startPoint-endPoint-line and current deadlock zone
            ArrayList<double[]>[] collisionTimes = getCollisionTimesForZone(startPoint, endPoint, deadlockLimitsNew[i]);

            // If any robot that is involved in current circular wait does not pass through the 
            // corresponding DL-obstacle between startPoint and endPoint, this deadlock cannot occor.
            boolean deadlockPossible = true;
            for (int j = 0; j < deadlockLimitsNew[i].length; j++)
            {
                if ((deadlockLimitsNew[i][j].size() > 0) && (collisionTimes[j].size() == 0))
                {
                   deadlockPossible = false;
                }
            }
            
            if (deadlockPossible)
            {
                // If there is a legal intersection, then the path from startPoint to endPoint 
                // passes through a deadlock obstacle
                if (intersectRecursively(collisionTimes, 0).size() > 0)
                {
                    return false;
                }
                
//                // The collision times are first initialized to their extreme values
//                double[] commonCollisionTimes = new double[]{0, 1};

//                // Next, intersection of all collision times is calculated
//                for (int k=0; k<collisionTimes.size(); k++)
//                {
//                    if (collisionTimes.get(k)[0] > commonCollisionTimes[0])
//                    {
//                        commonCollisionTimes[0] = collisionTimes.get(k)[0];
//                    }
//
//                    if (collisionTimes.get(k)[1] < commonCollisionTimes[1])
//                    {
//                        commonCollisionTimes[1] = collisionTimes.get(k)[1];
//                    }
//                }

//                // Finally, if the intersection of collision times is positive, we have a (deadlock) obstacle along the path
//                if (commonCollisionTimes[0] < commonCollisionTimes[1])
//                {
//                    return false;
//                }
            }
        }

        // If no common collision time was found for any zone, startPoint and endPoint see eachother
        return true;
    }
    
    /**
     * This method is called recursively, looping through all indices of the 
     * supplied collisionTimes-array (i.e. fromIndex should start at 0). When 
     * fromIndex == collisionTimes.lastIndex, a list is created, containing collision times
     * between plants[fromIndex] and the current deadlock zone. For other fromIndices, 
     * the list of collision times is intersected with current collision times. In the
     * end, the intersections of all combinations of collision times (each combination 
     * intersected between all plant involved in the deadlock) are contained in the 
     * collision times list.
     */
    private ArrayList<double[]> intersectRecursively(ArrayList<double[]>[] collisionTimes, int fromIndex)
    {
        ArrayList<double[]> currCollisionIntersections = new ArrayList<double[]>();
        
        // Next, intersection of all collision times is calculated
        for (int i=0; i<collisionTimes[fromIndex].size(); i++)
        {           
            if (fromIndex == collisionTimes.length - 1)
            {
                currCollisionIntersections.add(new double[]{collisionTimes[fromIndex].get(i)[0], collisionTimes[fromIndex].get(i)[0]});
            }
            else
            {
                ArrayList<double[]> nextCollisionIntersections = intersectRecursively(collisionTimes, fromIndex+1);
                for (int j = 0; j < nextCollisionIntersections.size(); j++)
                {
                    double intersectionStart = Math.max(collisionTimes[fromIndex].get(i)[0], nextCollisionIntersections.get(j)[0]);
                    double intersectionEnd = Math.min(collisionTimes[fromIndex].get(i)[1], nextCollisionIntersections.get(j)[1]);

                    if (intersectionStart < intersectionEnd)
                    {
                        currCollisionIntersections.add(new double[]{intersectionStart, intersectionEnd});
                    }
                }
            }
        }
        
        return currCollisionIntersections;
    }

    /**
     * This method finds all times when collision occur between any robot and 
     * the zone described by zoneLimits along the line starting in startPoint 
     * and ending in endPoint.
     */
    private ArrayList<double[]>[] getCollisionTimesForZone(double[] startPoint, double[] endPoint, ArrayList<double[]>[] zoneLimits)
    {
        // This list is filled with start and end times of collisions between the startPoint-endPoint-line and the zones
        ArrayList<double[]>[] collisionTimes = new ArrayList[plants.size()];
        for (int i = 0; i < collisionTimes.length; i++)
        {
            collisionTimes[i] = new ArrayList<double[]>();
        }

        // For each robot...
        for (int i=0; i<zoneLimits.length; i++)
        {
            // For each time the current robot crosses the current zone...
            for (int j = 0; j < zoneLimits[i].size(); j++)
            {
                // The enter/exit times for the current robot-zone-pair (is null if the robot never enters the zone)
                double[] currZoneLimits = zoneLimits[i].get(j);

                // If the end points of the line are within the zone limits, we have a collision...
                if ((startPoint[i] < currZoneLimits[1]) && (endPoint[i] > currZoneLimits[0]))
                {
                    // The collisionStart/collisionEnd time values. These times correspond to 
                    // the parametrization of the line between startPoint and endPoint.
                    // Thus they belong to [0, 1].
                    double[] currCollisionTimes = new double[2];

                    // If the line starts within the line, the parametrization time value of 
                    // collisionStart is equal to zero. Else it corresponds to the first zone limit.
                    if (startPoint[i] >= currZoneLimits[0])
                    {
                        currCollisionTimes[0] = 0;
                    }
                    else
                    {
                        currCollisionTimes[0] = (currZoneLimits[0] - startPoint[i]) / (endPoint[i] - startPoint[i]);
                    }

                    // If the line ends within the line, the parametrization time value of 
                    // collisionEnd is equal to one. Else it corresponds to the second zone limit.
                    if (endPoint[i] <= currZoneLimits[1])
                    {
                        currCollisionTimes[1] = 1;
                    }
                    else
                    {
                        currCollisionTimes[1] = (currZoneLimits[1] - startPoint[i]) / (endPoint[i] - startPoint[i]);
                    }

                    // Update the list of collision times
                    collisionTimes[i].add(currCollisionTimes);
                }
            }
        }

        return collisionTimes;
    }
    
    //@Deprecated
//    /**
//     * This method finds all times when collision occur between any robot and 
//     * the zone described by zoneLimits along the line starting in startPoint 
//     * and ending in endPoint.
//     */
//    private ArrayList<double[]> getCollisionTimesForZoneOld(double[] startPoint, double[] endPoint, ArrayList zoneLimits)
//    {
//        // This list is filled with start and end times of collisions between the startPoint-endPoint-line and the zones
//        ArrayList<double[]> collisionTimes = new ArrayList<double[]>();
//
//        // For each robot...
//        for (int j=0; j<zoneLimits.size(); j++)
//        {
//            // The enter/exit times for the current robot-zone-pair (is null if the robot never enters the zone)
//            double[] currZoneLimits = (double[]) zoneLimits.get(j);
//
//            // If the current robot does enter the zone...
//            if (currZoneLimits != null)
//            {
//                // If the end points of the line are within the zone limits, we have a collision...
//                if ((startPoint[j] < currZoneLimits[1]) && (endPoint[j] > currZoneLimits[0]))
//                {
//                    // The collisionStart/collisionEnd time values. These times correspond to 
//                    // the parametrization of the line between startPoint and endPoint.
//                    // Thus they belong to [0, 1].
//                    double[] currCollisionTimes = new double[2];
//
//                    // If the line starts within the line, the parametrization time value of 
//                    // collisionStart is equal to zero. Else it corresponds to the first zone limit.
//                    if (startPoint[j] >= currZoneLimits[0])
//                    {
//                        currCollisionTimes[0] = 0;
//                    }
//                    else
//                    {
//                        currCollisionTimes[0] = (currZoneLimits[0] - startPoint[j]) / (endPoint[j] - startPoint[j]);
//                    }
//
//                    // If the line ends within the line, the parametrization time value of 
//                    // collisionEnd is equal to one. Else it corresponds to the second zone limit.
//                    if (endPoint[j] <= currZoneLimits[1])
//                    {
//                        currCollisionTimes[1] = 1;
//                    }
//                    else
//                    {
//                        currCollisionTimes[1] = (currZoneLimits[1] - startPoint[j]) / (endPoint[j] - startPoint[j]);
//                    }
//
//                    // Update the list of collision times
//                    collisionTimes.add(currCollisionTimes);
//                }
//            }
//        }
//
//        return collisionTimes;
//    }

    /**
     * A temporary method that makes a path to test the smoothing
     */
    private void makeTestPath()
    {
//        // Adds the state times of each robot (run independently)
//        simulationTimes = new double[3][];
//        simulationTimes[0] = new double[]{2, 2, 3, 2, 1, 2, 1};
//        simulationTimes[1] = new double[]{2, 2, 2, 3, 1, 1, 1};
//        simulationTimes[2] = new double[]{2, 3, 2, 1, 1, 1, 1};
//
//        // Adds the state firing times of each robot (run together)
//        firingTimes = new double[3][];
//        firingTimes[0] = new double[]{2, 4, 7, 9, 14, 18, 19};
//        firingTimes[1] = new double[]{7, 12, 14, 17, 18, 19, 20};
//        firingTimes[2] = new double[]{2, 9, 11, 12, 17, 19, 20};
//
//        // Adds the points of the optimal path (schedule)
//        pathPoints.add(new double[]{0, 0, 0});
//        pathPoints.add(new double[]{2, 2, 2});
//        pathPoints.add(new double[]{4, 2, 4});
//        pathPoints.add(new double[]{7, 2, 5});
//        pathPoints.add(new double[]{9, 4, 5});
//        pathPoints.add(new double[]{10, 4, 7});
//        pathPoints.add(new double[]{10, 4, 8});
//        pathPoints.add(new double[]{10, 6, 9});
//        pathPoints.add(new double[]{12, 9, 9});
//        pathPoints.add(new double[]{12, 10, 10});
//        pathPoints.add(new double[]{13, 11, 10});
//        pathPoints.add(new double[]{13, 12, 11});

//        // Three zones in this example...
//        mutexLimits = new ArrayList[3];
//
//        // Zone 1
//        mutexLimits[0] = new ArrayList<double[]>();
//        mutexLimits[0].add(new double[]{2, 9});
//        mutexLimits[0].add(new double[]{10, 11});
//        mutexLimits[0].add(new double[]{5, 7});
//
//        // Zone 2
//        mutexLimits[1] = new ArrayList<double[]>();
//        mutexLimits[1].add(new double[]{4, 7});
//        mutexLimits[1].add(new double[]{2, 9});
//        mutexLimits[1].add(new double[]{9, 10});
//
//        // Zone 3
//        mutexLimits[2] = new ArrayList<double[]>();
//        mutexLimits[2].add(new double[]{10, 12});
//        mutexLimits[2].add(new double[]{4, 6});
//        mutexLimits[2].add(new double[]{2, 8});
//
//        // ... and one deadlock
//        deadlockLimits = new ArrayList[1];
//
//        // Deadlock 1
//        deadlockLimits[0] = new ArrayList<double[]>();
//        deadlockLimits[0].add(new double[]{2, 4});
//        deadlockLimits[0].add(new double[]{2, 4});
//        deadlockLimits[0].add(new double[]{2, 5});
    }
    
    /**
     * Loops through the plant parts that are used in the optimal schedule and
     * sets relative velocities in each state, as given by supplied relativeVelocities.
     * Currently the velocities are added to the name of the state as 
     * state.name += "; velocity=x", where x is the relative velocity w.r.t. max velocity.
     */
    private void setRelativeVelocitiesInPlants(double[][] relativeVelocities)
    {
        for (int plantIndex = 0; plantIndex < optimalSubPlants.size(); plantIndex++)
        {
            State currState = optimalSubPlants.getAutomatonAt(plantIndexMapping[plantIndex]).getInitialState();
            double accCost = 0;
            int relVelIndex = 0;
            innerForLoop: for (double[] keyPoint : keyPoints)
            {
                // If we have looped through the optimalSubPlant and arrived at the initial state 
                // for the second time, then stop and continue with the next plant.
                if (currState.isInitial() && accCost > 0)
                {
                    break innerForLoop;
                }
                
                // Add "_velocity=counter" to the names of states that should be passed
                // before some keyPoint is reached. Counter will later be replaced by 
                // relativeVelocities. This is done now, since the values of keyPoints 
                // will change, while we want the event velocities to be set in right places. 
                while (accCost < keyPoint[plantIndex])
                {
                    // If we get back to a treated state during the while loop, break out
                    if (currState.getName().contains("velocity="))
                    {
                        break innerForLoop;
                    }
                    
                    currState.setName(currState.getName() + "; velocity=" + 
                            roundOff(relativeVelocities[relVelIndex][plantIndex], 2));
                    accCost += currState.getCost();
                    currState = currState.outgoingArcsIterator().next().getToState();
                }
                
                relVelIndex++;
            }
        }
    }

    /**
     * A method for rounding off floating numbers.
     */
    private double roundOff(double number, double nrOfDecimals)
    {
        return Math.round(number * Math.pow(10, nrOfDecimals)) / (Math.pow(10, nrOfDecimals) + 0.0);
    }
    
    private void addToMessages(String str, int msgType)
    {
        if (callingScheduler != null)
        {
            callingScheduler.addToMessages(str + "\n", msgType);
        }
        else
        {
            switch(msgType)
            {
                case SchedulingConstants.MESSAGE_TYPE_INFO:
                    logger.info(str);
                    break;
                case SchedulingConstants.MESSAGE_TYPE_WARN:
                    logger.warn(str);
                    break;
                case SchedulingConstants.MESSAGE_TYPE_ERROR:
                    logger.error(str);
                    break;
                default:
                    logger.error("Wrong message type at addToMessages()");
            }
        }
    }
    
    public Automata getOptimalSubPlants()
    {
        return optimalSubPlants;
    }
    
    public Automata getSubControllers()
    {
        Alphabet zoneAlphabet = new Alphabet();
        for (Automaton zone : specs)
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
                    if (optimalSubPlants.getAutomatonAt(i).getAlphabet().contains(arc.getEvent()))
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
}