package org.supremica.automata.algorithms.scheduling;

import java.util.*;

import org.supremica.log.*;
import org.supremica.automata.*;

public class VisGraphBuilder {

	//     private BufferedReader r;
	//     private ArrayList<Double>[] allTimes;
	//     private ArrayList<Integer>[] allIndices;

    private Automata plantAutomata;
	   
	/** 
	 * The estimated cost for each state of each robot.
	 * int[] = [robot_index, state_index].
	 */
    private int[][] oneProdRelax;

    private ArrayList<double[]> edges;
    private Hashtable<Integer, ArrayList<Integer>>[] totalZoneIndices;
    private double[][] zoneTimes;
    private double[] goal;
    private VisibilityChecker checker;
    private static Logger logger = LoggerFactory.createLogger(VisGraphBuilder.class);

    public VisGraphBuilder(Automata plantAutomata, int[][] oneProdRelax) {
	
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

    public void buildVisGraph() {
		extractTimes();
		generateTotalZoneIndices();

		//Detta funkar bara för två robotar och borde utökas/ändras......
		Enumeration<Integer> keys = totalZoneIndices[0].keys();
		while (keys.hasMoreElements()) {
			int key = keys.nextElement();
			ArrayList<Integer> r1TimeIndices = totalZoneIndices[0].get(key);
			ArrayList<Integer> r2TimeIndices = totalZoneIndices[1].get(key);

			if (key == -1) 
				goal = new double[]{zoneTimes[0][r1TimeIndices.get(0)], zoneTimes[1][r2TimeIndices.get(0)]};
			else if (! (r2TimeIndices == null)) {
				double[] r1 = new double[r1TimeIndices.size()];
				double[] r2 = new double[r2TimeIndices.size()];
				for (int i=0; i<r1.length; i++) {
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

    public void drawVisGraph() {
		logger.error("VisGraphBuilder.drawVisGraph() not implemented.........................");
    }

    private void extractTimes() {
		zoneTimes = new double[plantAutomata.size()][];

		for (int i=0; i<plantAutomata.size(); i++) {
			int[] relaxationTimes = oneProdRelax[i];
			double[] automatonZoneTimes = new double[relaxationTimes.length-1];
			double base = relaxationTimes[0] + plantAutomata.getAutomatonAt(i).getInitialState().getCost();
	    
			for (int j=0; j<automatonZoneTimes.length; j++) {
				automatonZoneTimes[j] = base - relaxationTimes[j];
			}

			zoneTimes[i] = automatonZoneTimes;
		}
    }

    private void generateTotalZoneIndices() {
		totalZoneIndices = new Hashtable[plantAutomata.size()];
 
		for (int i=0; i<plantAutomata.size(); i++) {
			Hashtable<Integer, ArrayList<Integer>> zoneIndices = new Hashtable<Integer, ArrayList<Integer>>();
			int counter = 0;
	    
			Automaton theAuto = plantAutomata.getAutomatonAt(i);
			State currState = theAuto.getInitialState();
	    
			while (!currState.isAccepting()) {
				Iterator<Arc> outgoingArcsIter = currState.outgoingArcsIterator();
		
				while (outgoingArcsIter.hasNext()) {
					Arc outgoingArc = outgoingArcsIter.next();
					String label = outgoingArc.getLabel();
					int zoneNr = -1;
		    
					if (label.contains("b"))
						zoneNr = (new Integer(label.substring(label.indexOf("b")+1).trim())).intValue();
					else if (label.contains("u"))
						zoneNr = (new Integer(label.substring(label.indexOf("u")+1).trim())).intValue();
		    
					ArrayList<Integer> localZoneIndices = zoneIndices.get(zoneNr);
					if (localZoneIndices == null) 
						localZoneIndices = new ArrayList<Integer>();
		    
					localZoneIndices.add(counter);
					zoneIndices.put(zoneNr, localZoneIndices);
		    
					currState = outgoingArc.getToState();
					counter++;
				}
			}
	    
			totalZoneIndices[i] = zoneIndices;
		}
    }

    public double[] getGoal() {
		return goal;
    }
    
    public ArrayList<double[]> getEdges() {
		return edges;
    }

    public boolean isVisible(double[] start, double[] goal) {
		checker.setStart(start);
		checker.setGoal(goal);

		return checker.isVisible();
    }

    public boolean isVisible(double[] start) {
		return isVisible(start, goal);
    }

    public double getDistanceToDiag(double[] point) {
		return checker.getDistanceToDiag(point);
    }

    public void setStart(double[] point) {
		checker.setStart(point);
    }
}
