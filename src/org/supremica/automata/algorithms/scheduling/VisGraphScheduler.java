package org.supremica.automata.algorithms.scheduling;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.TreeMap;
import java.util.TreeSet;

import net.sourceforge.waters.model.analysis.AbortRequester;

import org.supremica.automata.Arc;
import org.supremica.automata.Automata;
import org.supremica.automata.AutomataIndexMap;
import org.supremica.automata.Automaton;
import org.supremica.automata.LabeledEvent;
import org.supremica.automata.State;
import org.supremica.gui.VisGraphDrawer;
import org.supremica.util.ActionTimer;

public class VisGraphScheduler
    implements Scheduler
{
    static final int SELF_INDEX = 0;
    private static final int PARENT_INDEX = SELF_INDEX + 1;
    private static final int G_INDEX = PARENT_INDEX + 1;
    private static final int F_INDEX = G_INDEX + 1;

    private static final String ACCEPTING_NODE_NOT_FOUND_EXCEPTION = "Accepting node not found during scheduling";

    /** The involved automata, plants, zone specifications */
    private final Automata robots, zones;

    /** The times corresponding to booking and unbooking events for every robot and zone */
    private Hashtable<Integer, double[]>[] zoneBoundaryTimes = null;

    /** The coordinates of the visibility graph vertices, together with the distance to goal */
    private final ArrayList<double[]> vertices = new ArrayList<double[]>();

    /** The finishing times of each robot (when run independently of the others) */
    private double[] goalTimes = null;

    /** The visibility checker */
    private VisibilityChecker visibilityChecker;

    private TreeSet<double[]> openTree;
    private TreeMap<Double, double[]> closedTree;

    /** To draw or not to draw - this is the question... */
    private final boolean toDrawVisibilityGraph;

    /** Determines if this visibility graph is only used for relaxation (and is thus called by another scheduler) */
    private boolean isRelaxationProvider = false;

    private final ActionTimer timer = new ActionTimer();

    /** The index mapping handler */
    @SuppressWarnings("unused")
	private AutomataIndexMap indexMap;

    /** This boolean is true if the scheduler-thread should be (is) running */
    private volatile boolean isRunning = false;

    /**
     * Assures that the mutex zones of the graph have been initialized
     * (is needed for correct functionning when this thread is started from
     * another thread for relaxation purposes).
     */
    private volatile boolean isInitialized = false;

    /**
     * Assures that the scheduling is done (is needed for correct functionning when
     * this thread is started from another thread for relaxation purposes).
     */
    private volatile boolean schedulingDone = false;

    private double[] optimalTimesFromVertices;

    //MKT tillf
    public long totalVisCheckTime = 0;

    /** The output string */
    private final String outputStr = "";

    /** The number of zones that each robot occupies during its cycle */
    private double[] additionalTimes;

    protected String infoMsgs = "";
    protected String warnMsgs = "";
    protected String errorMsgs = "";
    protected ArrayList<StackTraceElement[]> debugMsgs = new ArrayList<StackTraceElement[]>();

	/*
	 * The thread that performs the search for the optimal solution
	 */
	private Thread vgThread;

    public VisGraphScheduler(final Automata theAutomata, final boolean toDrawVisibilityGraph)
    throws Exception
    {
        this(theAutomata.getPlantAutomata(), theAutomata.getSpecificationAutomata(), toDrawVisibilityGraph);
    }

    public VisGraphScheduler(final Automata robots, final Automata zones, final boolean toDrawVisibilityGraph)
    throws Exception
    {
        this(robots, zones, toDrawVisibilityGraph, false);
    }

    public VisGraphScheduler(final Automata robots, final Automata zones, final boolean toDrawVisibilityGraph, final boolean isRelaxationProvider)
    throws Exception
    {
        this.toDrawVisibilityGraph = toDrawVisibilityGraph;

        this.robots = robots;
        this.zones = zones;
        this.isRelaxationProvider = isRelaxationProvider;
    }

	@Override
  public void startSearchThread()
	{
        vgThread = new Thread(this);
        isRunning = true;
        vgThread.start();
	}

    @Override
    public void run()
    {
        try
        {
            if (isRunning)
            {
                init();
            }

            if (!isRelaxationProvider)
            {
                if (isRunning)
                {
                    schedule();
                }

                if (isRunning && toDrawVisibilityGraph)
                {
                    timer.restart();
                    drawVisibilityGraph(500, 500);

                    infoMsgs += "The visibility graph painted in " + timer.elapsedTime() + "ms";
                }
            }
        }
        catch (final Exception ex)
        {
            errorMsgs += "Visibility Graph::schedule() -> " + ex;
            debugMsgs.add(ex.getStackTrace());
        }
    }

    @Override
    public void schedule()
    throws Exception
    {
        schedulingDone = false;

        timer.restart();

        openTree.clear();
        closedTree.clear();

        // Consists of [self_index, parent_index, g-value, f-value]
        double[] currNode = new double[]{0, -1, 0, vertices.get(0)[robots.size()]};
        openTree.add(currNode);

        while (! openTree.isEmpty())
        {
            if (isRunning)
            {
                currNode = openTree.first();

                if (isAcceptingNode(currNode))
                {
                    // If the algorithm was called from the GUI, print the optimal time value there
                    if (!isRelaxationProvider)
                    {
                        infoMsgs += "\tOPTIMAL SOLUTION.......... " + currNode[G_INDEX] +
                                " in time " + timer.elapsedTime() + "ms\n";
                    }

                    closedTree.put(currNode[SELF_INDEX], currNode);
                    break;
                }

                openTree.remove(currNode);

                branch(currNode);
            }
            else
            {
                break;
            }
        }

        schedulingDone = true;

        // If an accepting node could not be found although the scheduling process has not been interrupted,
        // then something is wrong (unless this method is called from scheduleFrom())
        if (isRunning && !isAcceptingNode(currNode))
        {
            throw new Exception(ACCEPTING_NODE_NOT_FOUND_EXCEPTION);
        }
    }

    /**
     * This method allows to set the starting point, from which the scheduling is done.
     *
     * @param fromTimes the time coordinates of the starting point
     * @return the minimal makespan
     */
    public synchronized double reallyScheduleFrom(final double[] fromTimes)
    throws Exception
    {
        final double distanceToGoal = calcDistance(fromTimes, goalTimes);
        if (distanceToGoal == 0)
        {
            return 0;
        }

        // Removes the previous start vertex
// 		vertices.remove(0);

        // Initializes the new start vertex
        final double[] newStartVertex = new double[fromTimes.length + 1];
        for (int i=0; i<fromTimes.length; i++)
        {
            newStartVertex[i] = fromTimes[i];
        }
        newStartVertex[newStartVertex.length - 1] = distanceToGoal;

        // Adds the new start vertex to the vertices-collection
        vertices.add(0, newStartVertex);

        // ... and off we go...
        try
        {
            schedule();
        }
        catch (final Exception e)
        {
            if (e.getMessage().equals(ACCEPTING_NODE_NOT_FOUND_EXCEPTION))
            {
                vertices.remove(0);

// 				return Double.MAX_VALUE;
                double maxCycleTime = 0;
                for (int i=0; i<goalTimes.length; i++)
                {
                    maxCycleTime += goalTimes[i];
                }
                return maxCycleTime;
            }
            else
            {
                throw e;
            }
        }

        vertices.remove(0);

        // The optimal solution is returned
        // (when (if) building the schedule automaton, remember that the indices
        // of closed states are shifted by 1 due to the removal of the first vertice (above))
        return closedTree.get((double) vertices.size())[G_INDEX];
    }


// 	/**
// 	 * This method allows to set the starting point, from which the scheduling is done.
// 	 *
// 	 * @param - the time coordinates of the starting point
// 	 * @return - the minimal makespan
// 	 */
// 	public synchronized double reallyScheduleFrom(double[] fromTimes)
// 		throws Exception
// 	{
// 		return scheduleFrom(fromTimes);
// 	}

    /**
     * This method allows to set the starting point, from which the scheduling is done.
     *
     * @param fromTimes the time coordinates of the starting point
     * @return the minimal makespan
     */
    public synchronized double scheduleFrom(final double[] fromTimes)
    throws Exception
    {
        schedulingDone = false;

        final double distanceToGoal = calcDistance(fromTimes, goalTimes);
        if (distanceToGoal == 0)
        {
            schedulingDone = true;
            return 0;
        }

// 		double minCostToGoal = Double.MAX_VALUE;
        double minCostToGoal = 0;
        for (int i=0; i<goalTimes.length; i++)
        {
            minCostToGoal += goalTimes[i];
        }

        //tillf
        final ActionTimer visCheckTimer = new ActionTimer();
        visCheckTimer.restart();
// 		ArrayList<Integer> visibleVerticeIndices = visibilityChecker.getVisibleIndices(fromTimes, vertices);
        //tillf (test)
        final ArrayList<Integer> visibleVerticeIndices = new ArrayList<Integer>(vertices.size());
        for (int i=0; i<vertices.size(); i++)
        {
            visibleVerticeIndices.add(i);
        }
        totalVisCheckTime += visCheckTimer.elapsedTime();

        for (int i=0; i<visibleVerticeIndices.size(); i++)
        {
            final double costToGoal = calcDistance(fromTimes, vertices.get(visibleVerticeIndices.get(i))) + optimalTimesFromVertices[visibleVerticeIndices.get(i)];
            if (costToGoal < minCostToGoal)
            {
                visibilityChecker.setStart(fromTimes);
                visibilityChecker.setGoal(vertices.get(visibleVerticeIndices.get(i)));
                if (visibilityChecker.isVisible())
                {
                    minCostToGoal = costToGoal;
                }
            }
        }

        schedulingDone = true;

        return minCostToGoal;
    }

    @Override
    public void buildScheduleAutomaton()
    throws Exception
    {
    }

    //TODO: make it useful
    @Override
    public Automaton getSchedule()
    {
	return null;
    }

    private void branch(final double[] currNode)
    {
        final double[] correspondingClosedNode = closedTree.get(currNode[SELF_INDEX]);

        // If the current vertex has not been examined yet OR previous examination was more expensive...
        if (correspondingClosedNode == null || currNode[F_INDEX] < correspondingClosedNode[F_INDEX])
        {
            // ... then add the children of the current vertice to the open list
            final ArrayList<Integer> visibleVerticeIndices = visibilityChecker.getVisibleIndices(vertices.get((int) currNode[SELF_INDEX]), vertices);

            for (int i=0; i<visibleVerticeIndices.size(); i++)
            {
                final int newSelfIndex = visibleVerticeIndices.get(i).intValue();
                final double[] newVertex = vertices.get(newSelfIndex);
                final double[] newNode = new double[currNode.length];

                newNode[SELF_INDEX] = newSelfIndex;
                newNode[PARENT_INDEX] = currNode[SELF_INDEX];
                newNode[G_INDEX] = currNode[G_INDEX] + calcDistance(vertices.get((int) currNode[SELF_INDEX]), newVertex);
                newNode[F_INDEX] = newNode[G_INDEX] + newVertex[newVertex.length - 1];

                openTree.add(newNode);
            }

            // ... and place a better vertex-instance on the closed list
            closedTree.put(currNode[SELF_INDEX], currNode);
        }
    }

    /**
     * Iterates through the states of each robot, recording the booking and
     * the corresponding unbooking times. Also the total (independent) running
     * time of each robot is stored.
     */
    @SuppressWarnings("unchecked")
    private synchronized void extractGraphTimes()
    throws Exception
    {
        // One hashtable per robot containing 2-sized doubles,
        // representing the booking and unbooking sequences of corresponding zones.
        // The zone indices are used as the keys of the hashtable.
        zoneBoundaryTimes = new Hashtable[robots.size()];
        goalTimes = new double[robots.size()];

        // The times that are added to compensate for the independent bookings that lie ahead
        additionalTimes = new double[robots.size()];

        for (int i=0; i<robots.size(); i++)
        {
            double currTime = 0;

            zoneBoundaryTimes[i] = new Hashtable<Integer, double[]>(2 * zones.size());

            State currState = robots.getAutomatonAt(i).getInitialState();

            while (! currState.isAccepting())
            {
                if (isRunning)
                {
                    if (currState.nbrOfOutgoingArcs() > 1)
                    {
                        throw new Exception("Visibility Graph cannot handle alternative routing. State " + currState.getName() + " has several outgoing arcs.");
                    }

                    currTime += currState.getCost();

                    final Arc currArc = currState.outgoingArcsIterator().next();
                    final LabeledEvent currEvent = currArc.getEvent();

                    for (int j=0; j<zones.size(); j++)
                    {
                        final Automaton currZone = zones.getAutomatonAt(j);

                        if (currZone.getAlphabet().contains(currEvent))
                        {
                            // If the current event is a booking event...
                            if (currZone.getInitialState().activeEvents(false).contains(currEvent))
                            {
                                zoneBoundaryTimes[i].put(j, new double[]{currTime, -1});
                                additionalTimes[i]++;
                            }
                            // If the current event is an unbooking event...
                            else
                            {
                                final double[] currTimePair = zoneBoundaryTimes[i].get(j);
                                currTimePair[1] = currTime;
                                zoneBoundaryTimes[i].put(j, currTimePair);
                            }
                        }
                    }

                    currState = currArc.getToState();
                }
                else
                {
                    return;
                }
            }

            goalTimes[i] = currTime;
            additionalTimes[i] = goalTimes[i] / additionalTimes[i];
        }

// 		//TEST (Add some time for each booking event that is not active since it is
// 		// is probable that the risk of collision might delay the schedule somewhat);
// 		TreeSet<Double>[] timesOfInactiveZones = new TreeSet[2];
// 		timesOfInactiveZones[0] = new TreeSet<Double>();
// 		timesOfInactiveZones[1] = new TreeSet<Double>();
// 		for (int j=0; j<zones.size(); j++)
// 		{
// 			// The zone boundaries of the first robot
// 			double[] firstBoundaryTimes = zoneBoundaryTimes[0].get(new Integer(j));

// 			// The zone boundaries of the second robot
// 			double[] secondBoundaryTimes = zoneBoundaryTimes[1].get(new Integer(j));

// 			// If the current zone is only booked by one robot, i.e. if it is
// 			// inactive, its time is recorded and later used to shift the following times
// 			// It is important that every inactive zone results in a time shifting (thus += 0.000001)
// 			if (firstBoundaryTimes == null && secondBoundaryTimes != null)
// 			{
// 				double inactiveBoundaryTime = secondBoundaryTimes[0];
// 				boolean inactiveZoneAdded = false;
// 				while (!inactiveZoneAdded)
// 				{
// 					inactiveZoneAdded = timesOfInactiveZones[1].add(new Double(inactiveBoundaryTime));
// 					inactiveBoundaryTime += 0.000001;
// 				}
// 			}
// 			else if (secondBoundaryTimes == null && firstBoundaryTimes != null)
// 			{
// 				double inactiveBoundaryTime = firstBoundaryTimes[0];
// 				boolean inactiveZoneAdded = false;
// 				while (!inactiveZoneAdded)
// 				{
// 					inactiveZoneAdded = timesOfInactiveZones[0].add(new Double(inactiveBoundaryTime));
// 					inactiveBoundaryTime += 0.000001;
// 				}
// 			}
// 		}

// 		double PROPORTIONAL_DELAY_CONSTANT = 0;
// // 		double PROPORTIONAL_DELAY_CONSTANT = 0.25 * Math.random();
// 		for (int i=0; i<robots.size(); i++)
// 		{
// 			for (int j=0; j<zones.size(); j++)
// 			{
// 				double[] currBoundaryTime = zoneBoundaryTimes[i].get(new Integer(j));
// 				int partnerRobotIndex = (int)Math.IEEEremainder(i + 1, 2);
// 				if (currBoundaryTime != null && zoneBoundaryTimes[partnerRobotIndex].get(new Integer(j)) != null)
// 				{
// 					for (int k=0; k<currBoundaryTime.length; k++)
// 					{
// 						Double currBookingTime = new Double(currBoundaryTime[k]);

// 						int nrOfprecedingInactiveZones = timesOfInactiveZones[i].headSet(currBookingTime).size();
// 						currBoundaryTime[k] += nrOfprecedingInactiveZones * PROPORTIONAL_DELAY_CONSTANT * additionalTimes[i];
// 					}
// 				}
// 			}

// 			int nrOfprecedingInactiveZones = timesOfInactiveZones[i].headSet(new Double(goalTimes[i])).size();
// 			goalTimes[i] += nrOfprecedingInactiveZones * PROPORTIONAL_DELAY_CONSTANT * additionalTimes[i];
// 		}
    }

    /** Now works only for two robots */
    private synchronized void init()
    throws Exception
    {
        timer.restart();

        extractGraphTimes();

        // If the call came from GUI, print the preprocessing time
        if (!isRelaxationProvider)
        {
            infoMsgs += "\tPreprocessing in " + timer.elapsedTime() + "ms\n";
        }

        indexMap = new AutomataIndexMap(robots);
        final ArrayList<double[]> edges = new ArrayList<double[]>();

        vertices.add(new double[]{0, 0, Math.max(goalTimes[0], goalTimes[1])});

        for (int j=0; j<zones.size(); j++)
        {
            // The zone boundaries of the first robot
            final double[] firstBoundaryTimes = zoneBoundaryTimes[0].get(j);

            // The zone boundaries of the second robot
            final double[] secondBoundaryTimes = zoneBoundaryTimes[1].get(j);

            // Two vertices (NW and SE) are added to the graph. But this is done only if
            // both boundary times are non-null, i.e. if both robots use the current zone.
            if (firstBoundaryTimes != null && secondBoundaryTimes != null)
            {
                // Initialize vertices (NW)
                double[] vertice = new double[3];
                vertice[0]= firstBoundaryTimes[0];
                vertice[1]= secondBoundaryTimes[1];
                vertice[vertice.length - 1] = calcDistance(vertice, goalTimes);
                vertices.add(vertice);

                // Initialize vertices (SE)
                vertice = new double[3];
                vertice[0]= firstBoundaryTimes[1];
                vertice[1]= secondBoundaryTimes[0];
                vertice[vertice.length - 1] = calcDistance(vertice, goalTimes);
                vertices.add(vertice);

                // Initialize edges
                edges.add(new double[]{firstBoundaryTimes[0], secondBoundaryTimes[0], firstBoundaryTimes[1], secondBoundaryTimes[0]});
                edges.add(new double[]{firstBoundaryTimes[0], secondBoundaryTimes[0], firstBoundaryTimes[0], secondBoundaryTimes[1]});
                edges.add(new double[]{firstBoundaryTimes[1], secondBoundaryTimes[1], firstBoundaryTimes[0], secondBoundaryTimes[1]});
                edges.add(new double[]{firstBoundaryTimes[1], secondBoundaryTimes[1], firstBoundaryTimes[1], secondBoundaryTimes[0]});
            }
        }

        // Adding the goal vertice
        vertices.add(new double[]{goalTimes[0], goalTimes[1], 0});

        // Initialize the visibility checker using the newly constructed edges
        visibilityChecker = new VisibilityChecker(edges);

        // Initializing the A*-lists
        openTree = new TreeSet<double[]>(new OpenTreeComparator(F_INDEX));
        closedTree = new TreeMap<Double, double[]>();

        //UnderConstruction
        if (isRelaxationProvider)
        {
            final ActionTimer preprocessTimer = new ActionTimer();
            preprocessTimer.restart();
            optimalTimesFromVertices = new double[vertices.size()];
            for (int i=0; i<optimalTimesFromVertices.length; i++)
            {
                optimalTimesFromVertices[i] = reallyScheduleFrom(vertices.get(i));
            }
            // String str = "vertice optimization (" + optimalTimesFromVertices.length + " vertices) done in " + preprocessTimer.elapsedTime() + "ms";
// 			logger.info(str);
// 			outputStr += "\t" + str + "\n";
        }

        isInitialized = true;
    }

    @Override
    public void requestAbort(final AbortRequester sender)
    {
        requestStop(false);
    }

    @Override
    public boolean isAborting()
    {
        return !isRunning;
    }

    @Override
    public void resetAbort(){
      isRunning = true;
    }

    public void requestStop(final boolean disposeGui)
    {
        isRunning = false;
    }

    private synchronized double calcDistance(final double[] start, final double[] goal)
    {
        return Math.max(goal[0] - start[0], goal[1] - start[1]);
    }

    private synchronized void drawVisibilityGraph(final int height, final int width)
    throws Exception
    {
        final VisGraphDrawer drawer = new VisGraphDrawer(450, 450, new String[]{robots.getAutomatonAt(0).getName(), robots.getAutomatonAt(1).getName()});

        drawer.setXYRange(goalTimes);
        for (int j=0; j<zones.size(); j++)
        {
            if (isRunning)
            {
                drawer.addZone(zoneBoundaryTimes[0].get(j), zoneBoundaryTimes[1].get(j), zones.getAutomatonAt(j).getName());
            }
            else
            {
                return;
            }
        }

        double[] currNode = closedTree.get(vertices.size() - 1.0);
        while (! isInitialNode(currNode))
        {
            if (isRunning)
            {
                // The drawing of the optimal solution
                final double[] pathEndVertex = vertices.get((int) currNode[SELF_INDEX]);

                currNode = closedTree.get(currNode[PARENT_INDEX]);
                final double[] pathStartVertex = vertices.get((int) currNode[SELF_INDEX]);

                drawer.addPath(pathStartVertex, pathEndVertex);
            }
            else
            {
                return;
            }
        }
    }

    private boolean isAcceptingNode(final double[] node)
    {
        if (node[SELF_INDEX] == vertices.size() - 1)
            return true;

        return false;
    }

    private boolean isInitialNode(final double[] node)
    {
        if (node[SELF_INDEX] == 0)
            return true;

        return false;
    }

    public boolean isInitialized()
    {
        return isInitialized;
    }

    public boolean schedulingDone()
    {
        return schedulingDone;
    }

    public String getOutputString()
    {
        return outputStr;
    }

    public double[] getOptimalTimesFromVertices()
    {
        return optimalTimesFromVertices;
    }

    //Tillf
    public String printArray(final double[] array)
    {
        String s = "[";

        for (int i=0; i<array.length-1; i++)
        {
            s += array[i] + " ";
        }
        s += array[array.length-1] + "]";

        return s;
    }

    @Override
    public String getMessages(final int msgType)
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

    @Override
    public Object[] getDebugMessages()
    {
        return debugMsgs.toArray();
    }

    @Override
    public void addToMessages(final String additionStr, final int messageType)
    {
        switch (messageType)
        {
            case SchedulingConstants.MESSAGE_TYPE_INFO:
                infoMsgs += additionStr;
                break;
            case SchedulingConstants.MESSAGE_TYPE_WARN:
                warnMsgs += additionStr;
                break;
            case SchedulingConstants.MESSAGE_TYPE_ERROR:
                errorMsgs += additionStr;
                break;
            default:
                warnMsgs += "Message type incorrect when adding \"" + additionStr + "\" to the messages";
                break;
        }
    }
}
