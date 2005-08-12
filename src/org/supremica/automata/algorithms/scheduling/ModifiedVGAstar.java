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
    }

    protected void initAuxIndices() {
	super.initAuxIndices();

	effCostIndex = accCostIndex + 1;
    }

    protected void branch(int[] currNode) {
	closedNodes.putNode(getKey(currNode), currNode);

	if (vgBuilder.isVisible(getEffCost(currNode))) {
	    logger.error("visibility discovered at " + searchCounter + " iteration");
	    goToGoal(currNode);
	}
 	else {	
	    Iterator childIter = expander.expandNode(currNode, activeAutomataIndex).iterator();
	    while (childIter.hasNext()) {
  		int[] nextNode = (int[])childIter.next();
		
		if (!isOnAList(nextNode))
		    putOnOpenList(nextNode);
 	    }
	}
    }

    protected void goToGoal(int[] currNode) {
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
	int effCostIndex = getActiveLength() + 1;
	int[] newCosts = new int[costs.length];

	for (int i=0; i<effCostIndex-1; i++) {
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

	    newCosts[i+effCostIndex] = costs[i+effCostIndex] + effCostAddition;
	}

	newCosts[effCostIndex-1] = costs[effCostIndex-1] + costs[changedIndex];
	
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