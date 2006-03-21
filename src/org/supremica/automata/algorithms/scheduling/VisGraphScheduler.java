package org.supremica.automata.algorithms.scheduling;

import java.util.*;
import javax.swing.*;
import java.awt.*;

import org.supremica.automata.*;
import org.supremica.gui.ScheduleDialog;
import org.supremica.gui.VisGraphDrawer;
import org.supremica.log.*;
import org.supremica.util.ActionTimer;

public class VisGraphScheduler 
	implements Scheduler
{
	/** The logger */
	private static Logger logger = LoggerFactory.createLogger(VisGraphScheduler.class);

	static final int SELF_INDEX = 0;
	private static final int PARENT_INDEX = SELF_INDEX + 1;
	private static final int G_INDEX = PARENT_INDEX + 1;
	private static final int F_INDEX = G_INDEX + 1;

	private static final String ACCEPTING_NODE_NOT_FOUND_EXCEPTION = "Accepting node not found during scheduling";
	
	/** The involved automata, plants, zone specifications */
	private Automata robots, zones;

	/** The times corresponding to booking and unbooking events for every robot and zone */
	private Hashtable<Integer, double[]>[] zoneBoundaryTimes = null;

	/** The coordinates of the visibility graph vertices, together with the distance to goal */
	private ArrayList<double[]> vertices = new ArrayList<double[]>();

	/** The finishing times of each robot (when run independently of the others) */
	private double[] goalTimes = null;

	/** The visibility checker */
	private VisibilityChecker visibilityChecker;

	private TreeSet<double[]> openTree;
	private TreeMap<Double, double[]> closedTree;

	/** To draw or not to draw - this is the question... */
	private boolean toDrawVisibilityGraph;

	/** Determines if this visibility graph is only used for relaxation (and is thus called by another scheduler) */
	private boolean isRelaxationProvider = false;

	private ActionTimer timer = new ActionTimer();

	/** The index mapping handler */
	private AutomataIndexMap indexMap;

	/** This boolean is true if the scheduler-thread should be (is) running */
	private volatile boolean isRunning = false;

	/** The dialog box that launched this scheduler */
	private ScheduleDialog gui;

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

	public VisGraphScheduler (Automata theAutomata, boolean toDrawVisibilityGraph, ScheduleDialog gui)
		throws Exception
	{
		this(theAutomata.getPlantAutomata(), theAutomata.getSpecificationAutomata(), toDrawVisibilityGraph, gui);
	}

	public VisGraphScheduler (Automata robots, Automata zones, boolean toDrawVisibilityGraph, ScheduleDialog gui)
		throws Exception
	{
		this(robots, zones, toDrawVisibilityGraph, false, gui);
	}
	
	public VisGraphScheduler (Automata robots, Automata zones, boolean toDrawVisibilityGraph, boolean isRelaxationProvider, ScheduleDialog gui)
		throws Exception
	{
		this.toDrawVisibilityGraph = toDrawVisibilityGraph;
		
		this.robots = robots;
		this.zones = zones;
		this.gui = gui;
		this.isRelaxationProvider = isRelaxationProvider;

		Thread vgThread = new Thread(this);
		isRunning = true;
		vgThread.start();	
	}

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

					logger.info("The visibility graph painted in " + timer.elapsedTime() + "ms");
				}	

				if (gui != null)
				{
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
				else
				{
					throw new Exception("Gui is NULL as far as VisGraphScheduler is concerned. The scheduling thread was not interrupted properly");
				}
			}
		}
		catch (Exception ex)
		{
			logger.error("Visibility Graph::schedule() -> " + ex);
			logger.debug(ex.getStackTrace());
		}
	}

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
						logger.info("OPTIMAL SOLUTION.......... " + currNode[G_INDEX] + " in time " + timer.elapsedTime() + "ms");
					}

					closedTree.put(new Double(currNode[SELF_INDEX]), currNode);
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
	 * @param - the time coordinates of the starting point
	 * @return - the minimal makespan
	 */
	public synchronized double scheduleFrom(double[] fromTimes)
		throws Exception
	{
		int exceptionCounter = 0;

		double distanceToGoal = calcDistance(fromTimes, goalTimes);
		if (distanceToGoal == 0)
		{
			return 0;
		}
		
		// Removes the previous start vertex
		vertices.remove(0);
		
		// Initializes the new start vertex
		double[] newStartVertex = new double[fromTimes.length + 1];
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
		catch (Exception e)
		{
			if (e.getMessage().equals(ACCEPTING_NODE_NOT_FOUND_EXCEPTION))
			{
				return Double.MAX_VALUE;
			}
			else
			{
				throw e;
			}
		}
		
		// The optimal solution is returned
		return closedTree.get(new Double(vertices.size() - 1))[G_INDEX];
	}

	public void buildScheduleAutomaton()
		throws Exception
	{
	}

	private void branch(double[] currNode)
	{
		double[] correspondingClosedNode = closedTree.get(new Double(currNode[SELF_INDEX]));

		// If the current vertice has not been examined yet OR previous examination was more expensive...
		if (correspondingClosedNode == null || currNode[F_INDEX] < correspondingClosedNode[F_INDEX])
		{
			// ... then add the children of the current vertice to the open list
			ArrayList<Integer> visibleVerticeIndices = visibilityChecker.getVisibleIndices(vertices.get((int) currNode[SELF_INDEX]), vertices);

			for (int i=0; i<visibleVerticeIndices.size(); i++)
			{
				int newSelfIndex = visibleVerticeIndices.get(i).intValue();
				double[] newVertex = vertices.get(newSelfIndex);
				double[] newNode = new double[currNode.length];

				newNode[SELF_INDEX] = newSelfIndex;
				newNode[PARENT_INDEX] = currNode[SELF_INDEX];
				newNode[G_INDEX] = currNode[G_INDEX] + calcDistance(vertices.get((int) currNode[SELF_INDEX]), newVertex);
				newNode[F_INDEX] = newNode[G_INDEX] + newVertex[newVertex.length - 1];

				openTree.add(newNode);
			}

			// ... and place a better vertice-instance on the closed list
			closedTree.put(new Double(currNode[SELF_INDEX]), currNode);
		}
	}

	/**
	 * Iterates through the states of each robot, recording the booking and 
	 * the corresponding unbooking times. Also the total (independent) running
	 * time of each robot is stored.
	 */
	private synchronized void extractGraphTimes()
		throws Exception 
	{
		zoneBoundaryTimes = new Hashtable[robots.size()]; 
		goalTimes = new double[robots.size()];

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

					Arc currArc = (Arc) currState.outgoingArcsIterator().next();
					LabeledEvent currEvent = currArc.getEvent();

					for (int j=0; j<zones.size(); j++)
					{
						Automaton currZone = zones.getAutomatonAt(j);

						if (currZone.getAlphabet().contains(currEvent))
						{
							if (currZone.getInitialState().activeEvents(false).contains(currEvent))
							{
								zoneBoundaryTimes[i].put(new Integer(j), new double[]{currTime, -1});
							}
							else
							{
								double[] currTimePair = zoneBoundaryTimes[i].get(new Integer(j));
								currTimePair[1] = currTime;
								zoneBoundaryTimes[i].put(new Integer(j), currTimePair);
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
		}
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
			logger.info("Preprocessing in " + timer.elapsedTime() + "ms");
		}
		
		indexMap = new AutomataIndexMap(robots);
		ArrayList<double[]> edges = new ArrayList<double[]>();

		vertices.add(new double[]{0, 0, Math.max(goalTimes[0], goalTimes[1])});

		for (int j=0; j<zones.size(); j++)
		{
			double[] firstBoundaryTimes = zoneBoundaryTimes[0].get(new Integer(j));
			double[] secondBoundaryTimes = zoneBoundaryTimes[1].get(new Integer(j));

			if (firstBoundaryTimes != null && secondBoundaryTimes != null)
			{
				// Initialize vertices (NW and SE)
				double[] vertice = new double[3];
				vertice[0]= firstBoundaryTimes[0];
				vertice[1]= secondBoundaryTimes[1];
				vertice[vertice.length - 1] = calcDistance(vertice, goalTimes);
				vertices.add(vertice);

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

		isInitialized = true;
	}

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

	private synchronized double calcDistance(double[] start, double[] goal)
	{
		return Math.max(goal[0] - start[0], goal[1] - start[1]);
	}

	private synchronized void drawVisibilityGraph(int height, int width)
		throws Exception
	{
		VisGraphDrawer drawer = new VisGraphDrawer(450, 450, new String[]{robots.getAutomatonAt(0).getName(), robots.getAutomatonAt(1).getName()});

		drawer.setXYRange(goalTimes);
		for (int j=0; j<zones.size(); j++)
		{
			if (isRunning)
			{
				drawer.addZone(zoneBoundaryTimes[0].get(new Integer(j)), zoneBoundaryTimes[1].get(new Integer(j)), zones.getAutomatonAt(j).getName());
			}
			else
			{
				return;
			}
		}

		double[] currNode = closedTree.get(new Double(vertices.size() - 1));
		while (! isInitialNode(currNode))
		{
			if (isRunning)
			{
				// The drawing of the optimal solution
				double[] pathEndVertex = vertices.get((int) currNode[SELF_INDEX]);

				currNode = closedTree.get(new Double(currNode[PARENT_INDEX]));
				double[] pathStartVertex = vertices.get((int) currNode[SELF_INDEX]);

				drawer.addPath(pathStartVertex, pathEndVertex);
			}
			else
			{
				return;
			}
		}
	}

	private boolean isAcceptingNode(double[] node)
	{
		if (node[SELF_INDEX] == vertices.size() - 1)
			return true;

		return false;
	}

	private boolean isInitialNode(double[] node)
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
}
