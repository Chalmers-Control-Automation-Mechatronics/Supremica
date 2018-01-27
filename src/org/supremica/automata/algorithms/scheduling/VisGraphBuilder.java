package org.supremica.automata.algorithms.scheduling;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.automata.Arc;
import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.State;


public class VisGraphBuilder
{
    private final Automata plantAutomata;

	/**
	 * The estimated cost for each state of each robot.
	 * int[] = [robot_index, state_index].
	 */
    private int[][] oneProdRelax;

    private final ArrayList<double[]> edges;
    private Hashtable<Integer, ArrayList<Integer>>[] totalZoneIndices;
    private double[][] zoneTimes;
    private double[] goal;
    private final VisibilityChecker checker;
    private static Logger logger = LogManager.getLogger(VisGraphBuilder.class);

    public VisGraphBuilder(final Automata plantAutomata, final int[][] oneProdRelax)
	{

		this.plantAutomata = plantAutomata;

		for (int i=0; i<oneProdRelax.length; i++)
		{
			for (int j=0; j<oneProdRelax[i].length; j++)
			{
				this.oneProdRelax[i][j] = oneProdRelax[i][j];
			}
		}

		edges = new ArrayList<double[]>();
		goal = new double[plantAutomata.size()];

		buildVisGraph();

		checker = new VisibilityChecker(edges, goal);
    }

	//     public ZoneEdgeBuilder(String xmlFile) throws FileNotFoundException {
	// 	r = new BufferedReader(new FileReader(xmlFile));
	// 	allTimes = new ArrayList[2];
	// 	allIndices = new ArrayList[2];

	// 	try {
	// 	    processBuffer();
	// 	}
	// 	catch (IOException e) {
	// 	    e.printStackTrace();
	// 	}
	//     }

	//     private void processBuffer() throws IOException {
	// 	boolean automaton = false;
	// 	int counter = -1;
	// 	ArrayList<Double> times = new ArrayList<Double>();
	// 	ArrayList<Integer> indices = new ArrayList<Integer>();

	// 	String str = r.readLine();

	// 	while (str != null) {
	// 	    if (str.contains("Plant")) {
	// 		automaton = true;
	// 		counter++;
	// 	    }
	// 	    else if (str.contains("</Automaton"))
	// 		automaton = false;
	// 	    if (automaton && str.contains("<States>"))
	// 		times = new ArrayList<Double>();
	// 	    else if (automaton && str.contains("</States>"))
	// 		allTimes[counter] = times;
	// 	    else if (str.contains("cost") && !str.contains("accepting")) {
	// 		double currCost = (new Double(str.substring(str.indexOf("cost")+6, str.lastIndexOf("\"")))).doubleValue();
	// 		int size = times.size();

	// 		if (size > 0)
	// 		    times.add(times.get(times.size()-1) + currCost);
	// 		else
	// 		    times.add(currCost);
	// 	    }
	// 	    else if (automaton && str.contains("<Transitions"))
	// 		indices = new ArrayList<Integer>();
	// 	    else if (automaton && str.contains("</Transitions"))
	// 		allIndices[counter] = indices;
	// 	    else if (automaton && str.contains("event")) {
	// 		indices.add((new Integer(str.substring(str.indexOf("event")+7, str.lastIndexOf("\"")))).intValue());
	// 	    }

	// 	    str = r.readLine();
	// 	}
	//     }

    public void buildVisGraph()
	{
		extractTimes();
		generateTotalZoneIndices();

		//Detta funkar bara för två robotar och borde utökas/ändras......
		final Enumeration<Integer> keys = totalZoneIndices[0].keys();
		while (keys.hasMoreElements())
		{
			final int key = keys.nextElement();
			final ArrayList<Integer> r1TimeIndices = totalZoneIndices[0].get(key);
			final ArrayList<Integer> r2TimeIndices = totalZoneIndices[1].get(key);

			if (key == -1)
			{
				goal = new double[]{zoneTimes[0][r1TimeIndices.get(0)], zoneTimes[1][r2TimeIndices.get(0)]};
			}
			else if (! (r2TimeIndices == null))
			{
				final double[] r1 = new double[r1TimeIndices.size()];
				final double[] r2 = new double[r2TimeIndices.size()];

				for (int i=0; i<r1.length; i++)
				{
					r1[i] = zoneTimes[0][r1TimeIndices.get(i)];
					r2[i] = zoneTimes[1][r2TimeIndices.get(i)];
				}

				edges.add(new double[]{r1[0], r2[0], r1[1], r2[0]});
				edges.add(new double[]{r1[0], r2[0], r1[0], r2[1]});
				edges.add(new double[]{r1[1], r2[1], r1[0], r2[1]});
				edges.add(new double[]{r1[1], r2[1], r1[1], r2[0]});
			}
		}
    }

    public void drawVisGraph()
	{
		logger.error("VisGraphBuilder.drawVisGraph() not implemented.........................");
    }

    private void extractTimes()
	{
		zoneTimes = new double[plantAutomata.size()][];

		for (int i=0; i<plantAutomata.size(); i++)
		{
			final int[] relaxationTimes = oneProdRelax[i];
			final double[] automatonZoneTimes = new double[relaxationTimes.length-1];
			final double base = relaxationTimes[0] + plantAutomata.getAutomatonAt(i).getInitialState().getCost();

			for (int j=0; j<automatonZoneTimes.length; j++)
			{
				automatonZoneTimes[j] = base - relaxationTimes[j];
			}

			zoneTimes[i] = automatonZoneTimes;
		}
    }

    @SuppressWarnings("unchecked")
    private void generateTotalZoneIndices()
	{
		totalZoneIndices = new Hashtable[plantAutomata.size()];

		for (int i=0; i<plantAutomata.size(); i++)
		{
			final Hashtable<Integer, ArrayList<Integer>> zoneIndices = new Hashtable<Integer, ArrayList<Integer>>();
			int counter = 0;

			final Automaton theAuto = plantAutomata.getAutomatonAt(i);
			State currState = theAuto.getInitialState();

			while (!currState.isAccepting())
			{
				final Iterator<Arc> outgoingArcsIter = currState.outgoingArcsIterator();

				while (outgoingArcsIter.hasNext())
				{
					final Arc outgoingArc = outgoingArcsIter.next();
					final String label = outgoingArc.getLabel();
					int zoneNr = -1;

					if (label.contains("b"))
					{
						zoneNr = (new Integer(label.substring(label.indexOf("b")+1).trim())).intValue();
					}
					else if (label.contains("u"))
					{
						zoneNr = (new Integer(label.substring(label.indexOf("u")+1).trim())).intValue();
					}

					ArrayList<Integer> localZoneIndices = zoneIndices.get(zoneNr);
					if (localZoneIndices == null)
					{
						localZoneIndices = new ArrayList<Integer>();
					}

					localZoneIndices.add(counter);
					zoneIndices.put(zoneNr, localZoneIndices);

					currState = outgoingArc.getToState();
					counter++;
				}
			}

			totalZoneIndices[i] = zoneIndices;
		}
    }

    public double[] getGoal()
	{
		return goal;
    }

    public ArrayList<double[]> getEdges()
	{
		return edges;
    }

    public boolean isVisible(final double[] start, final double[] goal)
	{
		checker.setStart(start);
		checker.setGoal(goal);

		return checker.isVisible();
    }

    public boolean isVisible(final double[] start)
	{
		return isVisible(start, goal);
    }

    public double getDistanceToDiag(final double[] point)
	{
		return checker.getDistanceToDiag(point);
    }

    public void setStart(final double[] point)
	{
		checker.setStart(point);
    }
}
