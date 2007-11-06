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
    ArrayList[] mutexLimits = null;

    /**
     * Possible deadlocks give rise to forbidden time-zones.
     */
    ArrayList[] deadlockLimits = null;

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
        
    /** The logger */
    private Logger logger = LoggerFactory.createLogger(this.getClass());
    
    // This ugly construction is needed (temporarily) to call VelocityBalancer.java 
    // directly from the GUI, without pre-optimization. 
    public VelocityBalancer(Automata plantAutomata, Automaton schedule)
        throws Exception
    {
        this(plantAutomata, new Automata(schedule));
    }
    
    public VelocityBalancer(Automata plantAutomata, Automata supervisors)
        throws Exception
    {
        Automaton schedule = null;
        
        // Extract the schedule automaton from the supplied supervisor automata
        if (supervisors.size() == 1)
        {
            schedule = supervisors.getAutomatonAt(0);
        }
        else 
        {
            for (Automaton supAuto : supervisors)
            {
                State initState = supAuto.getInitialState();
                if (initState.getName().contains(("firing time")))
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
        
        indexMap = new AutomataIndexMap(plantAutomata);
       
        // Extracts plant times from the optimal schedule
        extractFiringTimes(schedule, plantAutomata);
        
        // Feasibility check
        String tempStr = "";
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
        logger.info(tempStr);
//        
//        
//        // Creates a test problem. This is a temporary construction
//        makeTestPath();
//
//        // Checks for each pair of points (except for consecutive points) 
//        // whether they can see eachother.
//        // This is needed only during the development phase (to see what happens with our small example)
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
//                        System.out.println(str);
//                }
//            }
//        }
//
//        // Finds the key points, i.e. points allowing to decrease the number of robot stops 
//        findKeyPoints();
//
//        System.out.println("");
//        System.out.println("Initial key points:");
//        String str = "";
//        for (int i=0; i<keyPoints.size(); i++)
//        {
//            for (int j=0; j<keyPoints.get(i).length; j++)
//            {
//                str += keyPoints.get(i)[j] + " ";
//            }
//            str += "---> ";
//        }
//        str += "KLART!!!";
//        System.out.println(str);
//
//        // Loops through the key points and smooth out the robot velocities even more when possible
//        improveKeyPointsUsingVisibilitySmoothing();
//
//        // Gathers some statistics about the velocity changes (smooth schedule)
//        System.out.println("");
//        System.out.println("SMOOTH SCHEDULE......");
//        calcVelocityStatisticsForVisibilitySmoothing(keyPoints);
//
//        System.out.println("");
//        System.out.println("UNPROCESSED SCHEDULE.....");
//        calcVelocityStatisticsForUnprocessedSchedule();
//
//        System.out.println("");
//        System.out.println("EVENT SMOOTHING:");
//        calcVelocityStatisticsForEventSmoothing();

        System.out.println("Smoooth gliding.......");	
    }

    /**
     * This method searches through the optimal schedule and extracts the optimal
     * firing times of the scheduled events. Also minimal residence times for 
     * these events are collected.
     *
     * @param   the optimal schedule
     */
    private void extractFiringTimes(Automaton schedule, Automata plants)
        throws Exception
    {
        // The sum of schedule costs from the initial state to the current state
        double currFiringTime = 0;
        
        ArrayList<Double>[] firingTimesArrays = new ArrayList[plants.size()];
        ArrayList<Double>[] simulationTimesArrays = new ArrayList[plants.size()];
        for (int i = 0; i < firingTimesArrays.length; i++)
        {
            firingTimesArrays[i] = new ArrayList<Double>();
            simulationTimesArrays[i] = new ArrayList<Double>();            
        }

        State[] currPlantStates = new State[plants.size()];
        for (int plantIndex = 0; plantIndex < currPlantStates.length; plantIndex++)
        {
            currPlantStates[plantIndex] = indexMap.getAutomatonAt(plantIndex).getInitialState();
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
                    
                    for (int plantIndex = 0; plantIndex < currPlantStates.length; plantIndex++)
                    {
                        Alphabet currPlantEvents = currPlantStates[plantIndex].activeEvents(false);
                        if (currPlantEvents.contains(currEvent))
                        {
                            currFiringTime += scheduleState.getCost();
                                    
                            firingTimesArrays[plantIndex].add(currFiringTime);
                            simulationTimesArrays[plantIndex].add(currPlantStates[plantIndex].getCost());
                            
//                            logger.info("skriv mig" + firingTimes[i][j]);
                            
//                            simulationTimesArrays[plantIndex].add(new Double(currPlantStates[plantIndex].getCost()));
//                            firingTimesArrays[plantIndex].add(new Double(currFiringTime));
                            
                            scheduleState = scheduleState.nextState(currEvent);
                            currPlantStates[plantIndex] = currPlantStates[plantIndex].nextState(currEvent);
                            
                            break;
                        }                    
                    }
                }
            }
        }
        
        // Put the info about firing and simulation times from temporary containers
        // into more lasting ones. 
        firingTimes = new double[plants.size()][];
        simulationTimes = new double[plants.size()][];
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

    /**
     * TEST-ing to add better key points, in order to reduce stop time.
     */
    private void tryNewKeyPoint(double[] newKeyPoint, double[] pointBefore, double[] pointAfter)
    {
        System.out.println("");

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
            System.out.println(str);
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
            System.out.println(str);
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

            relativeVelocities[i][1] = simulationTimes[i][0] / firingTimes[i][0];
            str += roundOff(relativeVelocities[i][1], 2) + " ";

            for (int j=1; j<simulationTimes[i].length; j++)
            {
                relativeVelocities[i][j+1] = simulationTimes[i][j] / (firingTimes[i][j] - firingTimes[i][j-1]);
                str += roundOff(relativeVelocities[i][j+1], 2) + " ";
            }

            System.out.println(str);
        }

        // COPY-PASTE
        double[] nrOfMaxVelocityPassages = new double[relativeVelocities.length];
        double[] nrOfMinVelocityPassages = new double[relativeVelocities.length];
        double[] nrOfVelocityChanges = new double[relativeVelocities.length];
        double[] totalVelocityChange = new double[relativeVelocities.length];
        double[] meanVelocityChange = new double[relativeVelocities.length];

        System.out.println("");
        System.out.println("Smooth-per-event-velocity statistics: ");
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

            System.out.println("Rob_" + i + "... total change: " + totalVelocityChange[i] + "; nr of changes: " + nrOfVelocityChanges[i] + "; mean change: " + meanVelocityChange[i] + "; nr of stops: " + nrOfMinVelocityPassages[i] + "; nr of full-speed-runs: " + nrOfMaxVelocityPassages[i]);
        }
        System.out.println("");
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

        System.out.println("");
        System.out.println("Unprocessed schedule statistics:");
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

            System.out.println("Rob_" + j + "... total change: " + totalVelocityChange[j] + "; nr of changes: " + nrOfVelocityChanges[j] + "; mean change: " + meanVelocityChange[j] + "; nr of stops: " + nrOfMinVelocityPassages[j] + "; nr of full-speed-runs: " + nrOfMaxVelocityPassages[j]);
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

        System.out.println("");
        System.out.println("Velocity statistics: ");
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

            System.out.println("Rob_" + j + "... total change: " + totalVelocityChange[j] + "; nr of changes: " + nrOfVelocityChanges[j] + "; mean change: " + meanVelocityChange[j] + "; nr of stops: " + nrOfMinVelocityPassages[j] + "; nr of full-speed-runs: " + nrOfMaxVelocityPassages[j]);
        }
        System.out.println("");
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
    private void improveKeyPointsUsingVisibilitySmoothing()
    {
        // Stores the maximal difference between the elements of this and the previous key point.
        double[] timeToPrevKeyPoint = calcTimeToPreviousPoint(keyPoints);

        // Stores the relative robot velocities (max_velocity = 1)
        double[][] relativeVelocities = calcRelativeVelocities(keyPoints, timeToPrevKeyPoint);

        // The largest velocity change from the previous point to the next is calculated 
        // for every key point. The robot with the largest velocity change is adjusted,
        // such that the velocity to the current and to the next point is set equal. 
        // The key points are then adjusted consequently.
        System.out.println("");
        System.out.println("diff velocities:  ");
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
        System.out.println("");
        System.out.println("Relative velocities (after):");
        for (int i=0; i<keyPoints.size(); i++)
        {
            String str = "";
            for (int j=0; j<keyPoints.get(0).length; j++)
            {
                str += roundOff(relativeVelocities[i][j], 2) + " ";
            }
            System.out.println(str);
        }
        System.out.println("");

        System.out.println("");
        System.out.println("Key points (after): ");
        for (int i=0; i<keyPoints.size(); i++)
        {
            String str = "";
            for (int j=0; j<keyPoints.get(i).length; j++)
            {
                str += roundOff(keyPoints.get(i)[j], 2) + " ";
            }
            System.out.println(str);
        }
        System.out.println("");
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
        for (int i=0; i<mutexLimits.length; i++)
        {
            // Start and end times of collisions between the startPoint-endPoint-line and current mutex zone
            ArrayList<double[]> collisionTimes = getCollisionTimesForZone(startPoint, endPoint, mutexLimits[i]);

            // A collision occurs if (at least) two robots enter the mutex zone at the same time.
            // The following is a check for common enter/exit time intervals.
            if (collisionTimes.size() > 1)
            {
                // Find common collision time values, by checking
                // pairwise intersections of all collision times for the current zone
                for (int k=0; k<collisionTimes.size()-1; k++)
                {
                    for (int l=k+1; l<collisionTimes.size(); l++)
                    {
                        // if true, the current time intersection is positive, that is there is an obstacle along the road
                        if ((collisionTimes.get(k)[0] < collisionTimes.get(l)[1]) && (collisionTimes.get(l)[0] < collisionTimes.get(k)[1]))
                        {
                            return false;
                        }
                    }
                }
            }
        }

        // For each deadlock-zone...
        for (int i=0; i<deadlockLimits.length; i++)
        {
            // Start and end times of collisions between the startPoint-endPoint-line and current deadlock zone
            ArrayList<double[]> collisionTimes = getCollisionTimesForZone(startPoint, endPoint, deadlockLimits[i]);

            // There can be a circular wait situation only if all robots are inside the deadlock zone at some time...
            if (collisionTimes.size() == deadlockLimits[i].size())
            {
                // The collision times are first initialized to their extreme values
                double[] commonCollisionTimes = new double[]{0, 1};

                // Next, intersection of all collision times is calculated
                for (int k=0; k<collisionTimes.size(); k++)
                {
                    if (collisionTimes.get(k)[0] > commonCollisionTimes[0])
                    {
                        commonCollisionTimes[0] = collisionTimes.get(k)[0];
                    }

                    if (collisionTimes.get(k)[1] < commonCollisionTimes[1])
                    {
                        commonCollisionTimes[1] = collisionTimes.get(k)[1];
                    }
                }

                // Finally, if the intersection of collision times is positive, we have a (deadlock) obstacle along the path
                if (commonCollisionTimes[0] < commonCollisionTimes[1])
                {
                    return false;
                }
            }
        }

        // If no common collision time was found for any zone, startPoint and endPoint see eachother
        return true;
    }

    /**
     * This method finds all times when collision occur between any robot and 
     * the zone described by zoneLimits along the line starting in startPoint 
     * and ending in endPoint.
     */
    private ArrayList<double[]> getCollisionTimesForZone(double[] startPoint, double[] endPoint, ArrayList zoneLimits)
    {
        // This list is filled with start and end times of collisions between the startPoint-endPoint-line and the zones
        ArrayList<double[]> collisionTimes = new ArrayList<double[]>();

        // For each robot...
        for (int j=0; j<zoneLimits.size(); j++)
        {
            // The enter/exit times for the current robot-zone-pair (is null if the robot never enters the zone)
            double[] currZoneLimits = (double[]) zoneLimits.get(j);

            // If the current robot does enter the zone...
            if (currZoneLimits != null)
            {
                // If the end points of the line are within the zone limits, we have a collision...
                if ((startPoint[j] < currZoneLimits[1]) && (endPoint[j] > currZoneLimits[0]))
                {
                    // The collisionStart/collisionEnd time values. These times correspond to 
                    // the parametrization of the line between startPoint and endPoint.
                    // Thus they belong to [0, 1].
                    double[] currCollisionTimes = new double[2];

                    // If the line starts within the line, the parametrization time value of 
                    // collisionStart is equal to zero. Else it corresponds to the first zone limit.
                    if (startPoint[j] >= currZoneLimits[0])
                    {
                        currCollisionTimes[0] = 0;
                    }
                    else
                    {
                        currCollisionTimes[0] = (currZoneLimits[0] - startPoint[j]) / (endPoint[j] - startPoint[j]);
                    }

                    // If the line ends within the line, the parametrization time value of 
                    // collisionEnd is equal to one. Else it corresponds to the second zone limit.
                    if (endPoint[j] <= currZoneLimits[1])
                    {
                        currCollisionTimes[1] = 1;
                    }
                    else
                    {
                        currCollisionTimes[1] = (currZoneLimits[1] - startPoint[j]) / (endPoint[j] - startPoint[j]);
                    }

                    // Update the list of collision times
                    collisionTimes.add(currCollisionTimes);
                }
            }
        }

        return collisionTimes;
    }

    /**
     * A temporary method that makes a path to test the smoothing
     */
    private void makeTestPath()
    {
        // Adds the state times of each robot (run independently)
        simulationTimes = new double[3][];
        simulationTimes[0] = new double[]{2, 2, 3, 2, 1, 2, 1};
        simulationTimes[1] = new double[]{2, 2, 2, 3, 1, 1, 1};
        simulationTimes[2] = new double[]{2, 3, 2, 1, 1, 1, 1};

        // Adds the state firing times of each robot (run together)
        firingTimes = new double[3][];
        firingTimes[0] = new double[]{2, 4, 7, 9, 14, 18, 19};
        firingTimes[1] = new double[]{7, 12, 14, 17, 18, 19, 20};
        firingTimes[2] = new double[]{2, 9, 11, 12, 17, 19, 20};

        // Adds the points of the optimal path (schedule)
        pathPoints.add(new double[]{0, 0, 0});
        pathPoints.add(new double[]{2, 2, 2});
        pathPoints.add(new double[]{4, 2, 4});
        pathPoints.add(new double[]{7, 2, 5});
        pathPoints.add(new double[]{9, 4, 5});
        pathPoints.add(new double[]{10, 4, 7});
        pathPoints.add(new double[]{10, 4, 8});
        pathPoints.add(new double[]{10, 6, 9});
        pathPoints.add(new double[]{12, 9, 9});
        pathPoints.add(new double[]{12, 10, 10});
        pathPoints.add(new double[]{13, 11, 10});
        pathPoints.add(new double[]{13, 12, 11});

        // Three zones in this example...
        mutexLimits = new ArrayList[3];

        // Zone 1
        mutexLimits[0] = new ArrayList<double[]>();
        mutexLimits[0].add(new double[]{2, 9});
        mutexLimits[0].add(new double[]{10, 12});
        mutexLimits[0].add(new double[]{5, 7});

        // Zone 2
        mutexLimits[1] = new ArrayList<double[]>();
        mutexLimits[1].add(new double[]{4, 7});
        mutexLimits[1].add(new double[]{2, 9});
        mutexLimits[1].add(new double[]{9, 10});

        // Zone 3
        mutexLimits[2] = new ArrayList<double[]>();
        mutexLimits[2].add(new double[]{10, 12});
        mutexLimits[2].add(new double[]{4, 6});
        mutexLimits[2].add(new double[]{2, 8});

        // ... and one deadlock
        deadlockLimits = new ArrayList[1];

        // Deadlock 1
        deadlockLimits[0] = new ArrayList<double[]>();
        deadlockLimits[0].add(new double[]{2, 4});
        deadlockLimits[0].add(new double[]{2, 4});
        deadlockLimits[0].add(new double[]{2, 5});
    }

    /**
     * A method for rounding off floating numbers.
     */
    private double roundOff(double number, double nrOfDecimals)
    {
        return Math.round(number * Math.pow(10, nrOfDecimals)) / (Math.pow(10, nrOfDecimals) + 0.0);
    }
}