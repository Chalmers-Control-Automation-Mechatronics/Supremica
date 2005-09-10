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

		try {
		    if (!isOnAList(nextNode)) {
// 		    logger.info("++  " + printArray(nextNode));
 		    addToLogFile(nextNode, true);
		    putOnOpenList(nextNode);
		    }
		    else 
			addToLogFile(nextNode, false);
		    // 		    logger.info("-- " + printArray(nextNode));
		}
		catch (Exception e) {
		    e.printStackTrace();
		}
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

	    generatePath(node);

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

    protected void generatePath(int[] node) throws Exception {
	ArrayList<int[]> keyNodes = new ArrayList<int[]>();
	int[] latestVisibleNode = node; 
	int[] currGoalNode = node;
	keyNodes.add(node);

	String str = " -> " + nodeNr;
	int ind = nodeNr-1;
	int[] parent = getParent(node);

	w.newLine();
	w.newLine();
	w.write("The path:");
	w.newLine();

	while (ind >= 0) {
	    if (logClosedList.get(ind).equals(parent)) {
		str = ind + str;

		if (ind != 0)
		    str = " -> " + str;
		
		if (ind == 0 || !vgBuilder.isVisible(getEffCost(parent), getEffCost(currGoalNode))) {
		    for (int i=keyNodes.size()-1; i>=0; i--) {
			if (vgBuilder.isVisible(getEffCost(parent), getEffCost(keyNodes.get(i)))) {
			    for (int j=0; j<i; j++)
				keyNodes.remove(j);

			    break;
			}
		    }
	
		    if (ind == 0)
			keyNodes.add(0, parent);
		    else {
			keyNodes.add(0, latestVisibleNode);
			currGoalNode = latestVisibleNode;
		    }
		}
		
		latestVisibleNode = parent;

		if (ind > 0)
		    parent = getParent(parent);
	    }

	    ind--;
	}

	w.write(str);
	w.newLine();

	balanceSchedule(keyNodes);

	logger.error("key nodes: ");
	for (int i=0; i<keyNodes.size(); i++) 
	    logger.warn(printArray(keyNodes.get(i)));
    }

    // Postprocessing - skall fixa en jämn hastighet
    protected void balanceSchedule(ArrayList<int[]> keyNodes) throws Exception {
	w.newLine();
	w.newLine();

	logger.info(printNodeSignature(makeInitialNode()));
	w.write("\t" + printNodeSignature(makeInitialNode()));
	w.newLine();
	
	int[] currNode = keyNodes.get(0);

	for (int i=0; i<keyNodes.size()-1; i++) {
	    String str = "";

	    int[] currStartNode = keyNodes.get(i);
	    int[] nextNode = keyNodes.get(i+1);
	    
	    int[] delta = new int[]{nextNode[effCostIndex] - currStartNode[effCostIndex], nextNode[effCostIndex+1] - currStartNode[effCostIndex+1]};

	    for (int j=accCostIndex; j<currNode.length; j++)
		currNode[j] = currStartNode[j];

	    str += "[" + currStartNode[effCostIndex] + ", " + currStartNode[effCostIndex+1] + "] ---> [" + nextNode[effCostIndex] + ", " + nextNode[effCostIndex+1] + "]";
	    
	    if (delta[0] == 0) {
		str += ": NO CHANGE (TOP SPEED); velocity = v1_v2";
		w.write(str);

		while (currNode[effCostIndex] != nextNode[effCostIndex] || currNode[effCostIndex+1] != nextNode[effCostIndex+1]) {
		    currNode = (int[]) expander.expandNode(currNode, new int[]{activeAutomataIndex[1]}).iterator().next();

		    logger.info(printNodeSignature(currNode));
		    w.write("\t" + printNodeSignature(currNode));
		    w.newLine();
		}
	    }
	    else if (delta[1] == 0) {
		str += ": NO CHANGE (TOP SPEED); velocity = v1_v2";
		w.write(str);
		w.newLine();

		while (currNode[effCostIndex] != nextNode[effCostIndex] || currNode[effCostIndex+1] != nextNode[effCostIndex+1]) {
		    currNode = (int[]) expander.expandNode(currNode, new int[]{activeAutomataIndex[0]}).iterator().next();

		    logger.info(printNodeSignature(currNode));
		    w.write("\t" + printNodeSignature(currNode));
		    w.newLine();
		}
	    }
	    else {
		int divisor = 1;

		if (delta[0] == delta[1]) {
		    str += ": DIAGONAL MOVEMENT (TOP SPEED); velocity = v1_v2";
		    w.write(str);
		    w.newLine();
		    
		    for (int j=0; j<delta.length; j++)
			delta[j] = 1;
		}
		else {
		    int fastRobotIndex = -1;

		    if (delta[0] < delta[1]) 
			fastRobotIndex = 0;
		    else 
			fastRobotIndex = 1;

		    if (Math.IEEEremainder(delta[1 - fastRobotIndex], delta[fastRobotIndex]) == 0) {
			delta[fastRobotIndex] = delta[1 - fastRobotIndex]/delta[fastRobotIndex];
			delta[1 - fastRobotIndex] = 1;
		    }
		    else {
			divisor = delta[fastRobotIndex];
			delta[fastRobotIndex] = delta[1 - fastRobotIndex];
			delta[1 - fastRobotIndex] = divisor;
		    }
	    
		    if (fastRobotIndex == 0) {
			str += ": Slow down for robot_1; velocity = (" + divisor + "/" + delta[0] + "*v1)_v2";
			w.write(str);
			w.newLine();
		    }
		    else {
			str += ": Slow down for robot_2; velocity = v1_(" + divisor + "/" + delta[1] + "*v2)";
			w.write(str);
			w.newLine();
		    }

		    //Uppdatering (förskjutning) av effCost som beror på omskalningen av VG-axlarna
		    for (int j=i+1; j<keyNodes.size(); j++) {
			int[] keyNode = keyNodes.remove(j);

			for (int k=0; k<delta.length; k++) 
			    keyNode[effCostIndex + k] += delta[k] - divisor;
			
			keyNodes.add(j, keyNode);
		    }
		    //Uppdateringen klar
		
		    for (int j=0; j<delta.length; j++) 
			currNode[currCostIndex + j] *= delta[j];
		}

		double pathTime = 0;
		while ((pathTime + currStartNode[effCostIndex] != nextNode[effCostIndex]) || (pathTime + currStartNode[effCostIndex+1] != nextNode[effCostIndex+1])) {
		    int fastestIndex = 0;
		    if (currNode[currCostIndex] > currNode[currCostIndex+1])
			fastestIndex = 1;	    
		    
		    Iterator childIt = expander.expandNode(currNode, new int[]{activeAutomataIndex[fastestIndex]}).iterator();
		    //Ifall spec-en blockerar denna (den bästa) riktning (vilket inte borde hända eftersom zonerna borde vara långt borta, väljs en annan väg. 
		    if (!childIt.hasNext()) {
			fastestIndex = 1 - fastestIndex;
			childIt = expander.expandNode(currNode, new int[]{activeAutomataIndex[fastestIndex]}).iterator();

			if (!childIt.hasNext())
			    throw new Exception("NullPointerException in straight-line-expansion, curr node = " + printArray(currNode));
		    }

		    int[] child = (int[]) childIt.next();
		    child[currCostIndex + fastestIndex] *= delta[fastestIndex];

		    currNode = child;

		    pathTime = ((double)(currNode[effCostIndex] - currStartNode[effCostIndex]))/divisor;

		    //utskrift - tillfälligt
		    double g = ((double)(currNode[accCostIndex] - currStartNode[accCostIndex]))/divisor + currStartNode[accCostIndex];
		    double[] eff = new double[2];
		    for (int j=0; j<eff.length; j++) 
			eff[j] = ((double)(currNode[effCostIndex+j] - currStartNode[effCostIndex+j]))/divisor + currStartNode[effCostIndex+j];
		    double[] tv = new double[2];
		    for (int j=0; j<tv.length; j++) 
			tv[j] = ((double) currNode[currCostIndex+j])/divisor;

		    logger.info(printNodeName(currNode) + "; Tv = [" + tv[0] + " " + tv[1] + "]; g = " + g + "; eff = " + printArray(eff));

		    w.write("\t" + printNodeName(currNode) + "; Tv = [" + tv[0] + " " + tv[1] + "]; g = " + g + "; eff = " + printArray(eff));
		    w.newLine();
		}
	    }
	}

	while (!isAcceptingNode(currNode)) {
	    currNode = (int[]) expander.expandNode(currNode, activeAutomataIndex).iterator().next();
	    logger.info(printNodeSignature(currNode));
	    w.write("\t" + printNodeSignature(currNode));
	    w.newLine();
	}
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