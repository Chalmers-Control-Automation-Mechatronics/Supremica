package org.supremica.automata.algorithms.scheduling;

import java.util.*;

import org.supremica.log.*;
import org.supremica.automata.*;


/**
 * This class assumes that costs consists of three parts - int[] currCosts, int accCost and int[] effCosts.
 */
public class ModifiedVGAstar extends ModifiedAstar {

    private static Logger logger = LoggerFactory.createLogger(ModifiedVGAstar.class);
    private VisGraphBuilder vgBuilder;
    protected int effCostIndex;

    //Mkt tillfälligt
    java.io.BufferedWriter w;
    int nodeNr = 0; 
    ArrayList<int[]> logOpenList = new ArrayList<int[]>();
    ArrayList<int[]> logClosedList = new ArrayList<int[]>();
    ArrayList<int[]> logDiscardedList = new ArrayList<int[]>();

    public ModifiedVGAstar(Automata theAutomata) throws Exception  {
	super(theAutomata, "1-product relax");
    }
    
    public ModifiedVGAstar(Automata theAutomata, String heuristic) throws Exception {
	super(theAutomata, heuristic, true, false);
    }

    public ModifiedVGAstar(Automata theAutomata, String heuristic, boolean manualExpansion, boolean iterativeSearch) throws Exception {
	super(theAutomata, heuristic, manualExpansion, iterativeSearch);
    }

    protected void init(boolean manualExpansion) {
	super.init(manualExpansion);

	timer.start();
	vgBuilder = new VisGraphBuilder(plantAutomata, oneProdRelax);
	infoStr += "\tVisGraph-construction in " + timer.elapsedTime() + " ms\n";

	//Mkt tillfälligt
	try {
	    java.io.File fil = new java.io.File("C:\\Documents and Settings\\avenir\\Desktop\\log.txt");
	    w = new java.io.BufferedWriter(new java.io.FileWriter(fil));
	    w.write("Closed_Log_List:");
	    w.newLine();
	    w.newLine();
	}
	catch (Exception e) { e.printStackTrace(); }
    }

    protected void initAuxIndices() {
	super.initAuxIndices();

	effCostIndex = accCostIndex + 1;
    }

    protected String printNodeSignature(int[] node) {
	String s = super.printNodeSignature(node) + "; eff = ["; 

	for (int i=0; i<getActiveLength()-1; i++) 
	    s += node[i + effCostIndex] +  " ";
	s += node[effCostIndex + getActiveLength()-1] + "]";

	return s;
    }

    protected void branch(int[] currNode) {
	int key = getKey(currNode);
	int nodeArrayIndex = closedNodes.putNode(key, currNode);

	//mkt mkt tillfälligt
	if (false) {
// 	if ((!heuristic.equals("brute force")) && vgBuilder.isVisible(getEffCost(currNode))) {
	    logger.error("visibility discovered at " + searchCounter + " iteration");
	    goToGoal(currNode, key, nodeArrayIndex);
	}
 	else {	
	    Iterator childIter = expander.expandNode(currNode, activeAutomataIndex).iterator();

	    while (childIter.hasNext()) {
  		int[] nextNode = (int[])childIter.next();
		
		if (!isOnAList(nextNode)) {
// 		    logger.info("++  " + printArray(nextNode));
 		    addToLogFile(nextNode, true);
		    putOnOpenList(nextNode);
		}
		else 
		    addToLogFile(nextNode, false);
// 		    logger.info("-- " + printArray(nextNode));
 	    }
	}
    }

    // En mkt tillfällig metod (glöm inte att ta bort variablerna ovan)
    private void addToLogFile(int[] nextNode, boolean isNew) {
	try {
	    if (isNew) {
//  		logger.info("+ " + printArray(nextNode));
		logOpenList.add(nextNode);
	    }
	    else {
//  		logger.info("- " + printArray(nextNode));
		logDiscardedList.add(nextNode);
	    }

	    int currFromIndex = theAutomata.size() + AutomataIndexFormHelper.STATE_EXTRA_DATA;
	    int[] currFromNode = closedNodes.getNode(nextNode[currFromIndex], nextNode[currFromIndex+1]);
// 	    int[] currFromNode = new int[]{nextNode[currFromIndex], nextNode[currFromIndex + 1]};
	    boolean notClosed = true;
	
	    for (Iterator<int[]> iter = logClosedList.iterator(); iter.hasNext(); ) {
		int[] currClosedNode = iter.next();
		
// 		if ((currFromNode[0] == currClosedNode[0]) && (currFromNode[1] == currClosedNode[1]))
		if (currFromNode.equals(currClosedNode))
		    notClosed = false;
	    }

	    if (notClosed) {
		String str = nodeNr + "> ";

		for (int i=0; i<logOpenList.size(); i++) {
 		    int[] currOpenNode = logOpenList.get(i);
		
// 		    if ((currFromNode[0] == currOpenNode[0]) && (currFromNode[1] == currOpenNode[1])) {
		    if (currFromNode.equals(currOpenNode)) {
			str += "(q" + currFromNode[0] + "_q" + currFromNode[1] + ") -> ";
			str += "g = " + currOpenNode[accCostIndex] + " ";
			str += "Tv = [" + currOpenNode[currCostIndex] + " " + currOpenNode[currCostIndex+1] + "] ";
			str += "eff = [" + currOpenNode[effCostIndex] + " " + currOpenNode[effCostIndex+1] + "]";

			if (vgBuilder.isVisible(getEffCost(currFromNode)))
			    str += "  is visible";
		    
			break;
		    }
 		    else if ((currFromNode[0] == 0) && (currFromNode[1] == 0)) {
// 			int[] initial = makeInitialNode();
			str += "(q0_q0) -> ";
			str += "g = 0 ";
// 			str += "Tv = [" + initial[currCostIndex] + " " + initial[currCostIndex+1] + "] ";
			str += "Tv = [" + currFromNode[currCostIndex] + " " + currFromNode[currCostIndex+1] + "] ";
			str += "eff = [0 0]";

			if (vgBuilder.isVisible(getEffCost(currFromNode)))
			    str += "  is visible";
		    
			break;
		    }
		}

		logClosedList.add(currFromNode);
	
		w.write(str);
		w.newLine();

		nodeNr++;
	    }
	}
	catch (Exception e) {
	    logger.error("exception i ModifiedVGAstar: " + e.getMessage());
	}
    }

    //mkt mkt tillfälligt
    protected void flushLog(int[] node) {
	try {
	    String str = nodeNr + "> ";
	    str += "(q" + node[0] + "_q" + node[1] + ") -> ";
	    str += "g = " + node[accCostIndex] + " ";
	    str += "Tv = [" + node[currCostIndex] + " " + node[currCostIndex+1] + "] ";
	    str += "eff = [" + node[effCostIndex] + " " + node[effCostIndex+1] + "]";

	    w.write(str);
	    w.newLine();

	    w.newLine();
	    w.newLine();
	    w.write("The path:");
	    w.newLine();
	    str = " -> " + nodeNr;
	    int ind = nodeNr-1;
	    int[] parent = getParent(node);
	    while (ind > 0) {
		if (logClosedList.get(ind).equals(parent)) {
		    str = " -> " + ind + str;
		    parent = getParent(parent);
		}

		ind--;
	    }
	    str = "0" + str;
	    w.write(str);
	    w.newLine();

	    w.newLine();
	    w.newLine();
	    w.write("Discarded_Log_List:");
	    w.newLine();
	    w.newLine();

	    for (int i=0; i<logDiscardedList.size(); i++) {
		int[] disc = logDiscardedList.get(i);
		w.write(printArray(disc));
		w.newLine();
	    }
		    
	    w.flush();
	}
	catch (Exception e) { e.printStackTrace(); }
    }

    protected void goToGoal(int[] currNode, int key, int nodeArrayIndex) {
	double closestDistance = Double.MAX_VALUE;
	int[] closestChild = new int[currNode.length];
	Iterator children = expander.expandNode(currNode, activeAutomataIndex).iterator();
	vgBuilder.setStart(getEffCost(currNode));
	
	while(children.hasNext()) {
	    int[] currChild = (int[])children.next();

	    double distance = vgBuilder.getDistanceToDiag(getEffCost(currChild));

	    if (distance < closestDistance) {
		closestDistance = distance;
		closestChild = currChild;
	    }
	}

	openList.add(0, closestChild);
    }
    
    protected int[] makeInitialNode() {
	int activeLength = getActiveLength();

	int[] initialStates = AutomataIndexFormHelper.createState(theAutomata.size());
	int[] initialCosts = new int[2*activeLength + 1];
	
	for (int i=0; i<theAutomata.size(); i++) 
	    initialStates[i] = theAutomata.getAutomatonAt(i).getInitialState().getIndex();
	
	for (int i=0; i<activeLength; i++) {
	    initialCosts[i] = theAutomata.getAutomatonAt(activeAutomataIndex[i]).getInitialState().getCost();
	    initialCosts[i + activeLength] = 0;
	}
	initialCosts[initialCosts.length-1] = 0;

	return expander.makeNode(initialStates, null, initialCosts);
    }

    public int[] updateCosts(int[] costs, int changedIndex, int newCost) {
	int currEffCostIndex = getActiveLength() + 1;
	int[] newCosts = new int[costs.length];

	for (int i=0; i<currEffCostIndex-1; i++) {
	    int effCostAddition = 0;

	    if (i == changedIndex) {
		newCosts[i] = newCost;
		effCostAddition = costs[changedIndex];
	    }
	    else {
		newCosts[i] = costs[i] - costs[changedIndex]; 
		
		if (newCosts[i] < 0)
		    newCosts[i] = 0;

		effCostAddition = costs[i] - newCosts[i];
	    }

	    newCosts[i+currEffCostIndex] = costs[i+currEffCostIndex] + effCostAddition;
	}

	newCosts[currEffCostIndex-1] = costs[currEffCostIndex-1] + costs[changedIndex];
	
	return newCosts;
    }

    private int[] getEffCost(int[] node) {
	int activeLength = getActiveLength();

	int[] effCost = new int[activeLength];

	for (int i=0; i<activeLength; i++)
	    effCost[i] = node[i + effCostIndex];

	return effCost;
    }
}