/******************** ModifiedAstar2.java **************************
 * AKs implementation of Tobbes modified Astar search algo
 * Basically this is a guided tree-search algorithm, like
 *
 *      list processed = 0;                             // closed
 *      list waiting = initial_state;   // open
 *
 *      while still waiting
 *      {
 *              choose an element from waiting  // the choice is guided by heuristics
 *              generate successors of this element
 *              if a successor is not already waiting or processed
 *                      put it on waiting
 *              place the element on processed, remove it from waiting
 *      }
 */
package org.supremica.automata.algorithms.scheduling;

import java.util.*;

import org.supremica.log.*;
import org.supremica.automata.*;
import org.supremica.util.ActionTimer;

public class ModifiedAstar2 {
    private static Logger logger = LoggerFactory.createLogger(ModifiedAstar2.class);
    
    /** Starts ticking when the search/walk through the nodes is started. Shows the duration of the scheduling. */
    private ActionTimer timer;
    
    /** Contains "opened" but not yet examined (i.e. not "closed") nodes. */
    private ArrayList openList;
    
    /** Contains already examined (i.e. "closed") nodes. */
    private Hashtable<Integer, int[]> closedNodes;
    
    /** Hashtable containing the estimated cost for each robot, having states as keys. **/
    private ArrayList oneProdRelax;

    /** Hashtable containing the estimated cost for each combination of two robots **/
    private Hashtable[] twoProdRelax;
    
    /** The selected automata */
    private Automata theAutomata, plantAutomata;
    
    private int[] activeAutomataIndex;
    
    private int searchCounter;

    /** Handles the expansion of nodes - either manually or using Supremicas methods */
    private NodeExpander expander;

    /** If true, an iterative deepening search is used, which can be slower than the simple modified A*, but requires less memory. */
    private boolean iterativeSearch;
	
    /** Is used to translate the state indices to unique hash values */
    private int[] keyMapping;

    /** Decides whether 1-prod-relaxation should be used. */
    private boolean useOneProdRelax;
 
    /** Deprecated */
    public ModifiedAstar2(Automaton theAutomaton)
    {
// 	this.theAutomaton = theAutomaton;
// 	init(false);
    }

    public ModifiedAstar2(Automata theAutomata) {
	this(theAutomata, true, false);
    }

    public ModifiedAstar2(Automata theAutomata, boolean manualExpansion, boolean iterativeSearch) {
	this.theAutomata = theAutomata;
	this.iterativeSearch = iterativeSearch;

	init(manualExpansion);
    }

    private void init(boolean manualExpansion) {
	timer = new ActionTimer();
	openList = new ArrayList();
	closedNodes = new Hashtable<Integer, int[]>();
	expander = new NodeExpander(manualExpansion, theAutomata, this);
	
	plantAutomata = theAutomata.getPlantAutomata();
		
	keyMapping = new int[theAutomata.size()];
	keyMapping[0] = 1;
	for (int i=1; i<keyMapping.length; i++) {
	    keyMapping[i] = keyMapping[i-1] * (theAutomata.getAutomatonAt(i-1).nbrOfStates() + 1);
	}
	
	useOneProdRelax = true;
	
	if (theAutomata == null)
	    return;
	else {
	    int nrOfPlants = plantAutomata.size();
	    
	    oneProdRelax = new ArrayList(nrOfPlants);
	    
	    if (nrOfPlants > 2) {
		twoProdRelax = new Hashtable[nrOfPlants * (nrOfPlants - 1) / 2];
		for (int i=0; i<twoProdRelax.length; i++)
		    twoProdRelax[i] = new Hashtable();
	    }
	}
    }

	/**
	 *      Walks through the tree of possible paths in search for the optimal one.
	 */
	public int[] walk() throws Exception
	{
		if (theAutomata == null) {
			throw new Exception("Choose several automata to schedule...");
		}
		else {
			String infoStr = "Processing times:\n";

			timer.start();
			preprocess1();
			infoStr += "\t1st preprocessing in " + timer.elapsedTime() + " ms\n";	

			if (plantAutomata.size() > 2) {
				timer.start();
//				String prep2Info = preprocess2();
				preprocess2();
				infoStr += "\t2nd preprocessing in " + timer.elapsedTime() + " ms\n";
//				infoStr += prep2Info;
			}

			timer.start();
			searchCounter = 0;

			activeAutomataIndex = new int[plantAutomata.size()];
			for (int i=0; i<activeAutomataIndex.length; i++) {
				activeAutomataIndex[i] = theAutomata.getAutomatonIndex(plantAutomata.getAutomatonAt(i));
			}
			int[] accNode = scheduleFrom(makeInitialNode());
			
			infoStr += "\tA*-iterations: " + searchCounter + " in time: " + timer.elapsedTime() + " ms.\n";
			infoStr += "\t\t"+ printNodeSignature(accNode);
			logger.info(infoStr);

			return accNode;
    		}
	}
	
	public String printArray(int[] node) {
		String s = "[";
		
		for (int i=0; i<node.length-1; i++) {
			s += node[i] + " ";
		}
		s += node[node.length-1] + "]";
		
		return s;
	}
	
	private String printNodeSignature(int[] node) {
		String s = printNodeName(node) + "; Tv = [";
		
		int addIndex = node.length - (plantAutomata.size() + 1);
		for (int i=0; i<plantAutomata.size()-1; i++) 
			s += node[i + addIndex] +  " ";
		s += node[node.length-2] + "]; g = ";
		
		s += node[node.length-1];
		
		return s;
	}
	
	private String printNodeName(int[] node) {
		String s = "[";
		
		for (int i=0; i<theAutomata.size()-1; i++) {
			s += theAutomata.getAutomatonAt(i).getStateWithIndex(node[i]) + " ";
		}
		s += theAutomata.getAutomatonAt(theAutomata.size()-1).getStateWithIndex(node[theAutomata.size()-1]) + "]";
		
		return s;
	}

    private int[] scheduleFrom(int[] initNode) {
	int[] currNode = new int[initNode.length];
	
	openList.clear();
	closedNodes.clear();
	openList.add(initNode);
	
	while(!openList.isEmpty()) {
	    searchCounter++;
	    
	    currNode = (int[]) openList.remove(0);
	    
	    if (isAcceptingNode(currNode)) {
		logger.warn("nodes in memory = " + closedNodes.size());
		return currNode;
	    }
	    
	    closedNodes.put(getKey(currNode), currNode);
	    
	    useOneProdRelax = false;
	    if (activeAutomataIndex.length <= 2 || plantAutomata.size() <= 2)
		useOneProdRelax = true;
	    
	    Iterator childIter = expander.expandNode(currNode, activeAutomataIndex).iterator();
	    while (childIter.hasNext()) {
		int[] nextNode = (int[])childIter.next();
		
		if (!isOnAList(nextNode))
		    putOnOpenList(nextNode);
	    }	
	}
		
	return currNode;
    }
	
	/**
	 * 			Checks if some node is on the openList or closedList. If found, a comparison
	 * 			between the estimated costs is done to decide which node to keep as optimal.
	 *
	 * @param 	node
	 * @return 	true if node is already on the closedList and has an estimated cost not lower than
	 * 		   	the guy on the closedList.
	 */
    private boolean isOnAList(int[] node) {
	Integer currKey = getKey(node);
	
	int estimatedCost = calcEstimatedCost(node);
	int index = -1;
	
	Iterator iter = openList.iterator();
	while (iter.hasNext()) {
	    index++;
	    
	    int[] openNode = (int[])iter.next();
	    
	    if (currKey.equals(getKey(openNode))) {
		if (higherCostInAllDirections(node, openNode))
		    return true;
		else if (higherCostInAllDirections(openNode, node)) {
// 		    openList.set(index, node);
		    openList.remove(index);
		    return false;
		}
		else
		    logger.error("banan");

		return false;
	    }
	}
	
	if (closedNodes.containsKey(currKey)) {
	    int[] closedNode = (int[]) closedNodes.get(currKey);

	    if (higherCostInAllDirections(node, closedNode))
		return true;
	    else if (higherCostInAllDirections(closedNode, node)) {
		purgeClosedList(closedNode);
// 		closedNodes.remove(node);
		return false;
	    }
	    else
		logger.warn("bananskal");
	    
	    return false; 
	}
	
	return false;
    }

    private boolean higherCostInAllDirections(int[] currNode, int[] existingNode) {
	int startIndex = 2*theAutomata.size() + AutomataIndexFormHelper.STATE_EXTRA_DATA;
	int accCostPosition = currNode.length - 1;

	for (int i=startIndex; i<accCostPosition; i++) {
	    if ((currNode[i]+currNode[accCostPosition]) < (existingNode[i]+existingNode[accCostPosition]))
		return false;
	}

	return true;
    }
    
    /**
     * Removes the node purgedNode and all its successors from the closed list. 
     * @param int[] purgedNode - the root of the tree that is to be removed from the closed list. 
     */
    private void purgeClosedList(int[] purgedNode) {
	ArrayList<int[]> toPurge = new ArrayList<int[]>();
	toPurge.add(purgedNode);
	
	while (!toPurge.isEmpty()) {
	    int[] currNode = toPurge.remove(0);

	    Integer key = getKey(currNode);
	    if (closedNodes.containsKey(key)) {
		closedNodes.remove(key);
	    
		toPurge.addAll(expander.expandNode(currNode, activeAutomataIndex));
	    }
	}
    }
	
    private int calcEstimatedCost(int[] node) {
	if (useOneProdRelax)
	    return node[node.length-1] + getOneProdRelaxation(node);
	else
	    return node[node.length-1] + getTwoProdRelaxation(node);
    }
    
    private int getOneProdRelaxation(int[] node) {
	int estimate = 0;
	int[] currCosts = expander.getCosts(node);
	
	for (int i=0; i<activeAutomataIndex.length; i++) {
	    //int altEstimate = theNode.getCurrentCosts()[i] + ((Integer)oneProdRelax[i].get(theNode.getState(i))).intValue();
	    int altEstimate = currCosts[i] + ((int[])oneProdRelax.get(i))[node[activeAutomataIndex[i]]]; 
	    
	    if (altEstimate > estimate)
		estimate = altEstimate;
	}
	
	return estimate;
    }

    private int getTwoProdRelaxation(int[] node) {
	int estimate = 0;
	int[] currentCosts = expander.getCosts(node);
	int plantAutomataSize = plantAutomata.size();
	activeAutomataIndex = new int[2];
	
	for (int i=0; i<plantAutomataSize-1; i++) {
	    for (int j=i+1; j<plantAutomataSize; j++) {
		activeAutomataIndex[0] = i;
		activeAutomataIndex[1] = j;
		
		// Funkar bara om alla noder i den totala synkningen har 2-prod-relaxerats
		// Har de det?????????????
		Object relaxation = twoProdRelax[calcHashtableIndex(i,j)].get(getKey(node));
		if (relaxation != null) {
		    int altEstimate = ((Integer) relaxation).intValue();
		    
		    if (altEstimate > estimate)
			estimate = altEstimate;
		}
	    }
	}
	
	int minCurrCost = currentCosts[0];
	for (int i=1; i<currentCosts.length; i++) {
	    if (currentCosts[i] < minCurrCost)
		minCurrCost = currentCosts[i];
	}
	
	return estimate + minCurrCost;
    }
    
    private boolean isAcceptingNode(int[] node) {
	for (int i=0; i<activeAutomataIndex.length; i++) {
	    int index = activeAutomataIndex[i];
	    if (!theAutomata.getAutomatonAt(index).getStateWithIndex(node[index]).isAccepting()) 
		return false;
	}
	
	return true;
    }

    public Integer getKey(int[] node) {
	int key = 0;
	
	for (int i=0; i<activeAutomataIndex.length; i++)
	    key += node[activeAutomataIndex[i]]*keyMapping[activeAutomataIndex[i]];
	
	return new Integer(key);
    }
    
    private int[] resetCosts(int[] node) {	
	for (int i = 2*theAutomata.size() + AutomataIndexFormHelper.STATE_EXTRA_DATA; i<node.length; i++)
	    node[i] = 0;
	
	return node;
    }
	
    private int[] getStates(int[] node) {
	int[] states = new int[theAutomata.size()];
	
	for (int i=0; i<states.length; i++)
	    states[i] = node[i];
	
	return states;
    }
    
    /**
     * Inserts the node into the openList according to the estimatedCost (ascending).
     * @param 	node
     */
    private void putOnOpenList(int[] node) {
	int estimatedCost = calcEstimatedCost(node);
	int counter = 0;
	Iterator iter = openList.iterator();
	
	while (iter.hasNext()) {
	    int[] openNode = (int[])iter.next();
	    
	    if (estimatedCost <= calcEstimatedCost(openNode)) {
		openList.add(counter, node);
		return;
	    }
	    
	    counter++;
	}
	
	openList.add(node);
    }

    private int[] makeInitialNode() {
	int[] initialStates = AutomataIndexFormHelper.createState(theAutomata.size());
	int[] initialCosts = new int[plantAutomata.size() + 1];
	
	for (int i=0; i<theAutomata.size(); i++) 
	    initialStates[i] = theAutomata.getAutomatonAt(i).getInitialState().getIndex();
	
	for (int i=0; i<initialCosts.length-1; i++) 
	    initialCosts[i] = plantAutomata.getAutomatonAt(i).getInitialState().getCost();
	initialCosts[initialCosts.length-1] = 0;
	
	return expander.makeNode(initialStates, null, initialCosts);
    }

    /**
     * Calculates the costs for a one-product relaxation (i.e. as if there
     * would only be one robot in the cell) and stores it in oneProdRelax.
     */	
    private void preprocess1() {
	for (int i=0; i<plantAutomata.size(); i++) {
	    Automaton theAuto = plantAutomata.getAutomatonAt(i);
	    State markedState = findAcceptingState(theAuto);
	    ArrayList estList = new ArrayList();
	    
	    int[] relax = new int[theAuto.nbrOfStates()];
	    for (int j=0; j<relax.length; j++) 
		relax[j] = -1;
	    
	    if (markedState == null)
		return;
	    else {
		relax[markedState.getIndex()] = markedState.getCost();
		estList.add(markedState);
		
		while (!estList.isEmpty()) {
		    ArcIterator incomingArcIterator = ((State)estList.remove(0)).incomingArcsIterator();
		    
		    while (incomingArcIterator.hasNext()) {
			Arc currArc = incomingArcIterator.nextArc();
			State currState = currArc.getFromState();
			State nextState = currArc.getToState();
			//int remainingCost = ((Integer)(oneProdRelax[i].get(currArc.getToState()))).intValue();
			
			if (relax[currState.getIndex()] == -1) {
			    relax[currState.getIndex()] = relax[nextState.getIndex()] + nextState.getCost();
			    estList.add(currState);			
			}
			else {
			    int newRemainingCost = nextState.getCost() +  relax[nextState.getIndex()];
			    
			    if (newRemainingCost < relax[currState.getIndex()]) 
				relax[currState.getIndex()] = newRemainingCost;
			}
		    }
		}
	    }				
	    
	    oneProdRelax.add(i, relax);
	}
    }
    
    private void preprocess2() {
	for (int i=0; i<plantAutomata.size()-1; i++) {
	    for (int j=i+1; j<plantAutomata.size(); j++) {
		int hashtableIndex = calcHashtableIndex(i,j);
		
		activeAutomataIndex = new int[2];
		activeAutomataIndex[0] = theAutomata.getAutomatonIndex(plantAutomata.getAutomatonAt(i));
		activeAutomataIndex[1] = theAutomata.getAutomatonIndex(plantAutomata.getAutomatonAt(j));
		
		int schedCounter = 0;
		
		ArrayList activeNodes = new ArrayList();	
		activeNodes.add(makeInitialNode());
		
		while (!activeNodes.isEmpty()) {
		    int[] currNode = (int[])activeNodes.remove(0);
		    
		    if (! (twoProdRelax[hashtableIndex].containsKey(getKey(currNode)))) {
			activeNodes.addAll(expander.expandNode(currNode, activeAutomataIndex));
			
			schedCounter++;
			int[] accNode = scheduleFrom(resetCosts(currNode));
			
			if (accNode != null)
			    twoProdRelax[hashtableIndex].put(getKey(currNode), new Integer(accNode[accNode.length-1]));
		    }
		}
	    }
	}
    }

    /**
     * 			Calculates the costs for a two-product relaxation (i.e. as if there
     * 			would only be two robots in the cell) and stores it in the hashtable
     * 			twoProdRelax.
     */
    /*	private String preprocess2() {
      String infoStr = "";
      Automata plantAutomata = theAutomata.getPlantAutomata();
      
      for (int i=0; i<plantAutomata.size()-1; i++) {
      for (int j=i+1; j<plantAutomata.size(); j++) {
      int hashtableIndex = calcHashtableIndex(i,j);
      
      int schedCounter = 0;
      
      int[] currAutomataIndex = new int[]{theAutomata.getAutomatonIndex(plantAutomata.getAutomatonAt(i)),
      theAutomata.getAutomatonIndex(plantAutomata.getAutomatonAt(j))};
      
      ArrayList activeNodes = new ArrayList();
      
      activeNodes.add(new Node(makeInitialState()));
      
      while (!activeNodes.isEmpty()) {
      Node currNode = (Node)activeNodes.remove(0);
      
      if (! (twoProdRelax[hashtableIndex].containsKey(currNode))) {
      activeNodes.addAll(expandNode(currNode, currAutomataIndex));
      
      schedCounter++;
      currNode.resetCosts();
      Node accNode = scheduleFrom(currNode, currAutomataIndex);
      
      if (accNode != null)
      twoProdRelax[hashtableIndex].put(currNode, new Integer(accNode.getAccumulatedCost()));
      else {
      String plantNames = "";
      for (int k=0; k<currAutomataIndex.length-1; k++)
      plantNames += theAutomata.getAutomatonAt(currAutomataIndex[k]).getName() + "||";
							plantNames += theAutomata.getAutomatonAt(currAutomataIndex[currAutomataIndex.length-1]).getName();

							logger.warn("Är " + currNode.toStringLight() + " låst så in i helvete för " + plantNames + "???");
						}

					}
				}

				String plantNames = "";
				for (int k=0; k<currAutomataIndex.length-1; k++)
					plantNames += theAutomata.getAutomatonAt(currAutomataIndex[k]).getName() + "||";
				plantNames += theAutomata.getAutomatonAt(currAutomataIndex[currAutomataIndex.length-1]).getName();

				infoStr += "\t\t" + plantNames + ": " + schedCounter + " nodes relaxed\n";
			}
		}

		return infoStr;
	}
*/

	///////////////////////////////////////////////////////////////////////////
	// Vad är detta? Kommentarer på svenska!??!! Supremica-koncernens        //
	// officiella språk är ju engelska!!                                     //
	///////////////////////////////////////////////////////////////////////////

    /**
     * 	Calculates an index to Hashtable[] twoProdRelax.
     *
     * @param i
     * @param j
     * @return index of the 2-prod-relax-hashtable corresponing to robot_i and robot_j.
     */
     private int calcHashtableIndex(int i, int j) {
	 int size = theAutomata.getPlantAutomata().size();
	 
	 return (int) (i*(size - 1.5) - 0.5*(i*i) - 1 + j);
     }
    
    // Borde kanske ligga i "Automaton.java" men det kanske inte är tillräckligt
    // generellt för det. Om man inte har att göra med schemaläggning, kan det väl
    // finnas flera markerade tillstånd.
    // Kan nog rensas bort snart.
    /**
     * Returns some accepting state if it finds one, else returns null.
     * Iterates over all states _only_if_ no accepting states exist (or only
     * the last one is accepting)
     */
    public State findAcceptingState(Automaton auto) {
	for (StateIterator stateIt = auto.stateIterator(); stateIt.hasNext(); )
	    {
		State currState = stateIt.nextState();
		
		if (currState.isAccepting())
		    {
			return currState;
		    }
	    }
	
	return null;
    }
    
    private boolean hasParent(int[] node) {
	if (node[theAutomata.size() + AutomataIndexFormHelper.STATE_EXTRA_DATA] == -1)
	    return false;
	
	return true;
    }
    
    private int[] getParent(int[] node) {
	int addIndex = theAutomata.size() + AutomataIndexFormHelper.STATE_EXTRA_DATA;
	
	int[] parentIndex = new int[theAutomata.size()];
	for (int i=0; i<parentIndex.length; i++)
	    parentIndex[i] = node[i + addIndex];
	
	return (int[]) closedNodes.get(getKey(parentIndex));
    }
    
    public Automaton buildScheduleAutomaton(int[] currNode) {
	Automaton scheduleAuto = new Automaton();
	scheduleAuto.setComment("Schedule");
	
	State nextState = new State(printNodeSignature(currNode));
	nextState.setAccepting(true);
	scheduleAuto.addState(nextState);
	
	while (hasParent(currNode))
	    {
		try
		    {
			int[] parent = getParent(currNode);
			State currState = new State(printNodeSignature(parent));
			LabeledEvent event = findCurrentEvent(parent, currNode);
			
			if (!hasParent(parent))
			    currState.setInitial(true);
			
			scheduleAuto.addState(currState);
			scheduleAuto.getAlphabet().addEvent(event);
			scheduleAuto.addArc(new Arc(currState, nextState, event));
			
			currNode = parent;
			nextState = currState;
		    }
		catch (Exception ex)
		    {
			logger.error("ModifiedAstar2::buildScheduleAutomaton() --> Could not find the arc between " + printNodeName(currNode) + " and " + printNodeName(getParent(currNode)));
			logger.debug(ex.getStackTrace());
		    }
	    }
	
	return scheduleAuto;
    }
    
    private LabeledEvent findCurrentEvent(int[] fromNode, int[] toNode) throws Exception {
	for (int i=0; i<theAutomata.size(); i++) {
	    if (fromNode[i] != toNode[i]) {
		Automaton auto = theAutomata.getAutomatonAt(i);
		return auto.getLabeledEvent(auto.getStateWithIndex(fromNode[i]), auto.getStateWithIndex(toNode[i]));
	    }
	}
	
	return null;
    }
}
