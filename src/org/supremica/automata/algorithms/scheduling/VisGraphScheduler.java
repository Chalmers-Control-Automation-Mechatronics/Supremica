package org.supremica.automata.algorithms.scheduling;

import java.util.*;
import javax.swing.*;
import java.awt.*;

import org.supremica.log.*;
import org.supremica.automata.*;
import org.supremica.gui.VisGraphDrawer;
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

	private boolean toDrawVisibilityGraph;

	private ActionTimer timer = new ActionTimer();

	public VisGraphScheduler(Automata theAutomata, boolean toDrawVisibilityGraph)
	{
		this.toDrawVisibilityGraph = toDrawVisibilityGraph;

		robots = theAutomata.getPlantAutomata();
		zones = theAutomata.getSpecificationAutomata();
	}

	public void schedule()
		throws Exception
	{
		timer.restart();

		extractGraphTimes();
		init();
		
		logger.info("Preprocessing in " + timer.elapsedTime() + "ms");

		timer.restart();

		// Consists of [self_index, parent_index, g-value, f-value]
		double[] currNode = new double[]{0, -1, 0, calcDistance(vertices.get(0), vertices.get(vertices.size() - 1))};
		openTree.add(currNode);

		while (! openTree.isEmpty())
		{
			currNode = openTree.first();

			if (isAcceptingNode(currNode))
			{
				logger.info("OPTIMAL SOLUTION.......... " + currNode[F_INDEX] + " in time " + timer.elapsedTime() + "ms");
				closedTree.put(new Double(currNode[SELF_INDEX]), currNode);
				break;
			}

			openTree.remove(currNode);
				
			branch(currNode);
		}

		
		if (toDrawVisibilityGraph)
		{
			timer.restart();
			drawVisibilityGraph(500, 500);
			logger.info("The visibility graph painted in " + timer.elapsedTime() + "ms");
		}
	}

	public Automaton buildScheduleAutomaton()
		throws Exception
	{	
		return null;
	}

	public void requestStop()
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
	private void extractGraphTimes()
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
				if (currState.nbrOfOutgoingArcs() > 1)
					throw new Exception("Visibility Graph cannot handle alternative routing. State " + currState.getName() + " has several outgoing arcs.");
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

			goalTimes[i] = currTime;
		}
	}

	/** Now works only for two robots */
	private void init()
	{
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
	}

	private double calcDistance(double[] start, double[] goal)
	{
		return Math.max(goal[0] - start[0], goal[1] - start[1]);
	}

	private void drawVisibilityGraph(int height, int width)
		throws Exception
	{
		VisGraphDrawer drawer = new VisGraphDrawer(450, 450, new String[]{robots.getAutomatonAt(0).getName(), robots.getAutomatonAt(1).getName()});

		drawer.setXYRange(goalTimes);
		for (int j=0; j<zones.size(); j++)
		{
			drawer.addZone(zoneBoundaryTimes[0].get(new Integer(j)), zoneBoundaryTimes[1].get(new Integer(j)), zones.getAutomatonAt(j).getName());
		}

		double[] currNode = closedTree.get(new Double(vertices.size() - 1));
		while (! isInitialNode(currNode))
		{
			// The drawing of the optimal solution
			double[] pathEndVertex = vertices.get((int) currNode[SELF_INDEX]);

			currNode = closedTree.get(new Double(currNode[PARENT_INDEX]));
			double[] pathStartVertex = vertices.get((int) currNode[SELF_INDEX]);

			drawer.addPath(pathStartVertex, pathEndVertex);
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
}
