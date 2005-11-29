/******************** AbstractAstar.java **************************
 * AKs implementation of Tobbes modified Astar search algo
 * Basically this is a guided tree-search algorithm, like
 * *      list processed = 0;                             // closed
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

public abstract class AbstractAstar 
	implements Scheduler
{
	/****************************************************************************************/
	/*                                 VARIABLE SECTION                                     */
	/****************************************************************************************/


  	/** The indices of important parameters, that help to find them in the int[]-representions of nodes. */
	public static int ESTIMATE_INDEX, ACCUMULATED_COST_INDEX, CURRENT_COSTS_INDEX, PARENT_INDEX;
       
    /** 
	 * Contains promising search tree nodes, i.e. nodes that might lie on the optimal path. 
	 * They are "opened" but not yet examined (i.e. not "closed"). 
	 * OPEN list is better represented as a tree, ordered by the estimate values, f(n),
	 * while the tree vertices contain the int[] representations of the "opened" nodes. 
	 * 
	 * int[] node = [state_0_index, ..., state_m_index, -1, 0, 
	 *               parent_key, current_costs_0, ..., current_costs_k, 
	 *               accumulated_cost, estimate_value], 
	 * m being the number of selected automata, k the number of selected plants (robots), 
	 * current_costs are the Tv(n)-values, accumulated_cost = g(n) and estimate_value = f(n).
	 * (-1 and 0 are due to Supremicas AutomataIndexFormHelper information, namely 
	 * AutomataIndexFormHelper.STATE_EXTRA_DATA).
	 */
	protected TreeSet<int[]> openTree;
    
    /** 
	 * Contains already examined (i.e. "closed") search tree nodes (or rather their int[]-representations 
	 * @see #openTree).
	 * For efficiency, CLOSED should be represented as a tree. It is faster to search through 
	 * than a list and takes less place than a hashtable.
	 * If several nodes, corresponding to the same logical state, have been examined and 
	 * none of them is guaranteed to be better than the other, all the nodes are stored 
	 * as one int[]-variable. The closed nodes are unrolled and compared when necessary, 
	 * for example when a new node is to be added to the closedTree. This is done by 
	 * @see #ModifiedAstar.updateClosedTree(int[] node).
	 */
	protected TreeMap<Integer, int[]> closedTree;
    
    /** 
	 * The estimated cost for each state of each robot.
	 * int[] = [robot_index, state_index].
	 */
    protected int[][] oneProdRelax;

    /** Hashtable containing the estimated cost for each combination of two robots **/
    protected Hashtable[] twoProdRelax;
    
    /** The selected automata */
    protected Automata theAutomata, plantAutomata;
    
	/** 
	 * Contains the indices of the selected plants, i.e. activeAutomataIndex[0] contains the 
	 * index of the zeroth plant in the overall Automata, "theAutomata".
	 */
    protected int[] activeAutomataIndex;
    
	/** Counts the number of iterations */
    protected int iterationCounter;

    /** Starts ticking when the search/walk through the nodes is started. Shows the duration of the scheduling. */
    protected ActionTimer timer;

    /** Handles the expansion of nodes - either manually or using Supremicas methods */
    protected NodeExpander expander;

    /** Is used to translate the state indices to unique hash values */
    protected int[] keyMapping;

    /** 
	 * Decides whether 1-prod-relaxation should be used. Note that this variable is somewhat
	 * different from 'String heuristic'. For example, if heuristic = "2-product relax", 
	 * 1-product relaxation should be carried out first to collect enough information for the 
	 * 2-product relaxation. During this first phase, useOneProdRelax would be true although
	 * heuristic = "2-product relax".
	 */
    protected boolean useOneProdRelax;
    
	/** This string contains info about the scheduling, such as time, nr of iterations etc. */
    protected String infoStr =  "";

	/** 
	 * Which heuristic should be chosen. The value is normally supplied by the
	 * user through org.supremica.gui.ScheduleDialog.java.
	 * If not, the default value is "1-product relax". 
	 */
    protected String heuristic = "";

	/** Stores the maximum size of the openTree. */
	protected int maxOpenSize = 0;

    /** 
	 * If true, an iterative deepening search is used, which can be slower than 
	 * the simple modified A*, but requires less memory. 
	 * THIS FEATURE IS HOWEVER NOT IMPLEMENTED. 
	 */
    protected boolean iterativeSearch;
	
	/** 
	 * If this variable is set to true, the consistensy of the heuristic is guaranteed, 
	 * at the cost of some extra operations. 
	 */
	protected boolean consistentHeuristic = false;
	
	/**
	 * The output stream
	 */
	private static Logger logger = LoggerFactory.createLogger(AbstractAstar.class);


	/****************************************************************************************/
	/*                                 ABSTRACT METHODS                                     */
	/****************************************************************************************/

    protected abstract int[] makeInitialNode();

	protected abstract void branch(int[] currNode);


	/****************************************************************************************/
	/*                                 CONSTUCTORS                                          */
	/****************************************************************************************/

 
    public AbstractAstar(Automata theAutomata) throws Exception  
	{
		this(theAutomata, "1-product relax");
    }

    public AbstractAstar(Automata theAutomata, String heuristic) throws Exception 
	{
		this(theAutomata, heuristic, true, false);
    }

    public AbstractAstar(Automata theAutomata, String heuristic, boolean manualExpansion, boolean iterativeSearch) 
		throws Exception 
	{
		this.theAutomata = theAutomata;
		this.iterativeSearch = iterativeSearch;
		this.heuristic = heuristic;
		
		if (heuristic.equals("1-product relax"))
			useOneProdRelax = true;
		else if (heuristic.equals("2-product relax")) {
			if (theAutomata.getPlantAutomata().size() < 3)
				throw new Exception("2-product relax cannot be used for two or less products");
			
			useOneProdRelax = false;
		}

		init(manualExpansion);
    }


	/****************************************************************************************/
	/*                                 INIT METHODS                                         */
	/****************************************************************************************/


    protected void init(boolean manualExpansion) 
		throws Exception
	{
		timer = new ActionTimer();

		expander = new NodeExpander(manualExpansion, theAutomata, this);

		plantAutomata = theAutomata.getPlantAutomata();
		
		//Borde räcka med plantAutomata.size(), fast då kanske man måste ändra lite på andra ställen också
		keyMapping = new int[theAutomata.size()];
		keyMapping[0] = 1;
		for (int i=1; i<keyMapping.length; i++) {
			keyMapping[i] = keyMapping[i-1] * (theAutomata.getAutomatonAt(i-1).nbrOfStates() + 1);
		}
	
		// 	useOneProdRelax = true;
	
		if (theAutomata == null)
			return;
		else {
			int nrOfPlants = plantAutomata.size();
	    
			oneProdRelax = new int[nrOfPlants][];
	    
			if (nrOfPlants > 2) {
				twoProdRelax = new Hashtable[nrOfPlants * (nrOfPlants - 1) / 2];
				for (int i=0; i<twoProdRelax.length; i++)
					twoProdRelax[i] = new Hashtable();
			}

			infoStr = "Processing times:\n";
	    
			timer.start();
			preprocess1();
			infoStr += "\t1st preprocessing in " + timer.elapsedTime() + " ms\n";

			initAuxIndices();

			openTree = new TreeSet<int[]>(new OpenTreeComparator());
			closedTree = new TreeMap<Integer, int[]>();
		}	
    }

    protected void initAuxIndices() {
		activeAutomataIndex = new int[plantAutomata.size()];
		for (int i=0; i<activeAutomataIndex.length; i++) {
			activeAutomataIndex[i] = theAutomata.getAutomatonIndex(plantAutomata.getAutomatonAt(i));
		}

		PARENT_INDEX = theAutomata.size() + AutomataIndexFormHelper.STATE_EXTRA_DATA;
		
		// Detta borde ändras till STATIC_VARIBEL = 1 (och inte 2) => 2 ändringar
		CURRENT_COSTS_INDEX = PARENT_INDEX + 2; //ClosedNodes.CLOSED_NODE_INFO_SIZE;
		ACCUMULATED_COST_INDEX = CURRENT_COSTS_INDEX + activeAutomataIndex.length;
		ESTIMATE_INDEX = ACCUMULATED_COST_INDEX + 1;
    }


	/****************************************************************************************/
	/*                                 THE A*-ALGORITHM                                     */
	/****************************************************************************************/


    /**
     *      Walks through the tree of possible paths in search for the optimal one.
     */
    public int[] schedule() 
		throws Exception
    {
		if (theAutomata == null) {
			throw new Exception("Choose several automata to schedule...");
		}
		else {
			timer.start();
			iterationCounter = 0;

			int[] currNode = makeInitialNode();

			// Resets the OPEN and the CLOSED trees
			openTree.clear();
			closedTree.clear();

			// Initiates the OPEN tree by adding the initial node, corresponding to the initial 
			// state of the synchronous composition of the selected automata. 
			openTree.add(currNode);

			/** 
				Tillfälligt bortkommenterat (väntar på att 2-prod relaxeringen skall implementeras utan buggar)
				if (plantAutomata.size() > 2) {
				timer.start();
				//				String prep2Info = preprocess2();
				preprocess2();
				infoStr += "\t2nd preprocessing in " + timer.elapsedTime() + " ms\n";
				//				infoStr += prep2Info;
				}
			*/

			while(! openTree.isEmpty())
			{
				iterationCounter++;
				
				// Records the maximum size of the openTree
				int currOpenSize = openTree.size();
				if (currOpenSize > maxOpenSize)
					maxOpenSize = currOpenSize;
				
				// Removes the first node on OPEN. If it is accepting, the search is completed
				currNode = openTree.first();
				
				if (isAcceptingNode(currNode))
					break;
				
				boolean succesfullyRemoved =  openTree.remove(currNode);
				if (! succesfullyRemoved)
					throw new Exception("The node " + printNodeName(currNode) + " was not found on the openTree");
				
				// If the node is not accepting, it goes to the CLOSED tree if there is not a node there already
				// that represents the same logical state and is better than the current node in all 
				// "aspects" (lower cost in all directions). If the current node is promising (if it ends up on CLOSED),
				// its successors are found and put on the OPEN tree. 
				branch(currNode);
			}

			if (currNode == null || ! isAcceptingNode(currNode))
				throw new RuntimeException("An accepting state could not be found, nr of iteration = " + iterationCounter); 

			infoStr += "\tA*-iterations (nr of search calls through the closed tree): " + iterationCounter + "\n";
			infoStr += "\tIn time: " + timer.elapsedTime() + " ms\n";
			infoStr += "\tThe CLOSED tree contains (at the end) " + closedTree.size() + " elements\n";
			infoStr += "\tMax{OPEN.size} = " + maxOpenSize + "\n";
			infoStr += "\t\t" + "g = " + currNode[ACCUMULATED_COST_INDEX];
			
			logger.info(infoStr);
	    
			return currNode;
		}
    }

	/**
	 * This method puts the node "node" in the right place on the closedTree if the tree does not already
	 * contain any nodes that correspond to the same logical states than this node. 
	 * Otherwise, the method compares this node to those already examined. If this node is worse 
	 * (more expensive in every future direction) that any already examined node, it is discarded. 
	 * Conversely, if it is better than every examined node, this node is the only one to be stored in the 
	 * closedTree. If there are ties (in some directions one node is better than the other, but in others
	 * it is worse), this node, as well as all the discovered tie nodes are stored in the closedTree
	 * (note that all examined nodes that are always worse than "node" are discarded).
	 * 
	 * @param int[] node - the new node that might be added to the CLOSED tree.
	 * @return true, if the new node is added to the CLOSED tree
	 *         false, otherwise.
	 */
	protected boolean updateClosedTree(int[] node)
	{
		// The nodes corresponding to the same logical state (but different paths from the initial state)
		// as the new node. They are stored as one int[]-variable in the closedTree.
		int[] correspondingClosedNodes = closedTree.remove(new Integer(getKey(node)));

		// If the node (or its logical state collegues) has not yet been put on the closedTree, 
		// then it is simply added to CLOSED.
		if (correspondingClosedNodes == null)
		{
			closedTree.put(new Integer(getKey(node)), node);
		}
		else
		{
			// The internal indices of the nodes that can be either better or worse (cheaper or more expensive)
			// than the current node, depending on the future path ("internal index" meaning the node's number in the 
			// correspondingClosedNodes-int[]-array).
			ArrayList<Integer> tieIndices = new ArrayList<Integer>();

			int nodeLength = node.length;
			int nrOfClosedNodes = correspondingClosedNodes.length / nodeLength;

			// Each "internal" node should be compared to the current node 
			for (int i=0; i<nrOfClosedNodes; i++)
			{
				boolean newNodeIsAlwaysWorse = true;
				boolean newNodeIsAlwaysBetter = true;

				// The comparison is done for Tv_new[i] + g_new <> Tv_old[i] + g_old (forall i)
				for (int j=CURRENT_COSTS_INDEX; j<ACCUMULATED_COST_INDEX; j++)
				{
					int currCostDiff = (node[j] + node[ACCUMULATED_COST_INDEX]) - (correspondingClosedNodes[j + i*nodeLength] + correspondingClosedNodes[ACCUMULATED_COST_INDEX + i*nodeLength]);

					if (currCostDiff < 0)
						newNodeIsAlwaysWorse = false;
					else if (currCostDiff > 0)
						newNodeIsAlwaysBetter = false;
				}

				// If the new node is worse than any already examined node in every future direction,
				// it is thrown away;
				if (newNodeIsAlwaysWorse)
				{
					closedTree.put(new Integer(getKey(node)), correspondingClosedNodes);
					return false;
				}
				// else if the examined node is neither worse nor better, its index is added to the tieIndices
				else if (! newNodeIsAlwaysWorse && ! newNodeIsAlwaysBetter)
					tieIndices.add(new Integer(i));					
			}

			// Only ties (and the new node) are kept for the update of the closedTree. 
			int[] newClosedNode = new int[(tieIndices.size() + 1)*nodeLength];

			// The tie-nodes (if there are any) are copied to the new closedNode
			for (int i=0; i<tieIndices.size(); i++)
			{
				int currExaminedNodesIndex = tieIndices.get(i).intValue();
				for (int j=0; j<nodeLength; j++) 
				{
					newClosedNode[j + i*nodeLength] = correspondingClosedNodes[j + currExaminedNodesIndex*nodeLength];
				}
			}
			
			// The latest addition to the node-family (int[] node) is also added to the new closedNode
			for (int j=0; j<nodeLength; j++)
			{
				newClosedNode[j + tieIndices.size()*nodeLength] = node[j];
			}

			closedTree.put(new Integer(getKey(node)), newClosedNode);
		}

		return true;
	}

    /**
     * Calculates the costs for a one-product relaxation (i.e. as if there
     * would only be one robot in the cell) and stores it in oneProdRelax.
     */	
    protected void preprocess1() 
		throws Exception
	{
		for (int i=0; i<plantAutomata.size(); i++) 
		{
			Automaton theAuto = plantAutomata.getAutomatonAt(i);

			// An accepting state is found for the current automaton. In order 
			// for the algorithm to work properly, it is assumed that the model is 
			// such that the accepting state is also the last state in the sequence(s)
			// of operations, described by the current automaton. 
			State markedState = null;

			for (Iterator<State> stateIt = theAuto.stateIterator(); stateIt.hasNext(); )
			{
				markedState = stateIt.next();
		
				if (markedState.isAccepting())
					break;
			}

			if (! markedState.isAccepting())
				throw new Exception("No accepting state for " + theAuto.getName() + " was found during preprocessing...");


			ArrayList estList = new ArrayList();
	    
			oneProdRelax[i] = new int[theAuto.nbrOfStates()];
			for (int j=0; j<oneProdRelax[i].length; j++) 
				oneProdRelax[i][j] = -1;
	    
			if (markedState == null)
				return;
			else 
			{
				oneProdRelax[i][markedState.getIndex()] = markedState.getCost();

				estList.add(markedState);
		
				while (!estList.isEmpty()) 
				{
					Iterator<Arc> incomingArcIterator = ((State)estList.remove(0)).incomingArcsIterator();
		    
					while (incomingArcIterator.hasNext()) 
					{
						Arc currArc = incomingArcIterator.next();
						State currState = currArc.getFromState();
						State nextState = currArc.getToState();

						if (oneProdRelax[i][currState.getIndex()] == -1) 
						{
							oneProdRelax[i][currState.getIndex()] = oneProdRelax[i][nextState.getIndex()] + nextState.getCost();
							estList.add(currState);
						}
						else
						{
							int newRemainingCost = nextState.getCost() + oneProdRelax[i][nextState.getIndex()];

							if (newRemainingCost < oneProdRelax[i][currState.getIndex()])
								oneProdRelax[i][currState.getIndex()] = newRemainingCost;
						}
					}
				}
			}				
		}
    }

	//     protected void preprocess2() {
	// 	for (int i=0; i<plantAutomata.size()-1; i++) {
	// 	    for (int j=i+1; j<plantAutomata.size(); j++) {
	// 		int hashtableIndex = calcHashtableIndex(i,j);
		
	// 		activeAutomataIndex = new int[2];
	// 		activeAutomataIndex[0] = theAutomata.getAutomatonIndex(plantAutomata.getAutomatonAt(i));
	// 		activeAutomataIndex[1] = theAutomata.getAutomatonIndex(plantAutomata.getAutomatonAt(j));
		
	// 		int schedCounter = 0;
		
	// 		ArrayList activeNodes = new ArrayList();	
	// 		activeNodes.add(makeInitialNode());
		
	// 		while (!activeNodes.isEmpty()) {
	// 		    int[] currNode = (int[])activeNodes.remove(0);
		    
	// 		    if (! (twoProdRelax[hashtableIndex].containsKey(getKey(currNode)))) {
	// 			activeNodes.addAll(expander.expandNode(currNode, activeAutomataIndex));
			
	// 			schedCounter++;
	// 			int[] accNode = scheduleFrom(resetCosts(currNode));
			
	// 			if (accNode != null)
	// 			    twoProdRelax[hashtableIndex].put(getKey(currNode), new Integer(accNode[accNode.length-1]));
	// 		    }
	// 		}
	// 	    }
	// 	}
	//     }

    /**
     * 			Calculates the costs for a two-product relaxation (i.e. as if there
     * 			would only be two robots in the cell) and stores it in the hashtable
     * 			twoProdRelax.
     */
    /*	protected String preprocess2() {
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
  
	/**
	 * This method calculates the remaining cost for each robot/plant. The maximum remaining
	 * cost is returned to be used as an estimate of the total remaining cost of the system. 
	 * 
	 * @param int[] node - the current node
	 * @return int - the heuristic function, h(n), that guides the search, in this case it is the "1-product relaxation"
	 */
    protected int getOneProdRelaxation(int[] node) {
		int estimate = 0;
		int[] currCosts = expander.getCosts(node);
	
		for (int i=0; i<activeAutomataIndex.length; i++) {
			int altEstimate = currCosts[i] + oneProdRelax[i][node[activeAutomataIndex[i]]]; 
	    
			if (altEstimate > estimate)
				estimate = altEstimate;
		}
	
		return estimate;
    }

	//     int getTwoProdRelaxation(int[] node) {
	// 	int estimate = 0;
	// 	int[] currentCosts = expander.getCosts(node);
	// 	int plantAutomataSize = plantAutomata.size();
	// 	activeAutomataIndex = new int[2];
	
	// 	for (int i=0; i<plantAutomataSize-1; i++) {
	// 	    for (int j=i+1; j<plantAutomataSize; j++) {
	// 		activeAutomataIndex[0] = i;
	// 		activeAutomataIndex[1] = j;
		
	// 		// Funkar bara om alla noder i den totala synkningen har 2-prod-relaxerats
	// 		// Har de det?????????????
	// 		Object relaxation = twoProdRelax[calcHashtableIndex(i,j)].get(getKey(node));
	// 		if (relaxation != null) {
	// 		    int altEstimate = ((Integer) relaxation).intValue();
		    
	// 		    if (altEstimate > estimate)
	// 			estimate = altEstimate;
	// 		}
	// 	    }
	// 	}
	
	// 	int minCurrCost = currentCosts[0];
	// 	for (int i=1; i<currentCosts.length; i++) {
	// 	    if (currentCosts[i] < minCurrCost)
	// 		minCurrCost = currentCosts[i];
	// 	}
	
	// 	return estimate + minCurrCost;
	//     }

	/**
	 * Calculated the total estimated cost for the node, i.e. the sum of the accumulated cost
	 * and the estimate of the remaining cost (f(n) = g(n) + h(n)) is returned.
	 *
	 * @param int[] node - the node, whose estimated cost we want to know
	 * @return int totalEstimatedCost - f(n) = g(n) + h(n)
	 */
    public int calcEstimatedCost(int[] node) throws Exception {
		if (useOneProdRelax) {
			// 	    return node[node.length-1] + getOneProdRelaxation(node);

			// The following code inside the if-loop is needed to ensure consistency of the heuristic
			int[] parent = getParent(node);
	    
			int fNode = node[ACCUMULATED_COST_INDEX] + getOneProdRelaxation(node);
			int fParent = parent[ACCUMULATED_COST_INDEX] + getOneProdRelaxation(parent);

			if (fParent > fNode)
				return fParent;
			else
				return fNode;
		}
		else if (heuristic.equals("brute force"))
			return node[ACCUMULATED_COST_INDEX];
		else {
			logger.error("2-prod relaxation not implemented yet");
			return -1;
		}
		// 	    return node[node.length-1] + getTwoProdRelaxation(node);
    }

    public int[] updateCosts(int[] costs, int changedIndex, int newCost) {
		int[] newCosts = new int[costs.length];

		for (int i=0; i<costs.length-2; i++) {
			if (i == changedIndex)
				newCosts[i] = newCost;
			else {
				newCosts[i] = costs[i] - costs[changedIndex]; 
				if (newCosts[i] < 0)
					newCosts[i] = 0;
			}
		}
	
		// The accumulated cost update
		newCosts[newCosts.length-2] = costs[costs.length-2] + costs[changedIndex];

		// The f-update
		newCosts[newCosts.length-1] = costs[costs.length-1];
	
		return newCosts;
    }


	/****************************************************************************************/
	/*                      METHODS FOR BUILDING THE SCHEDULE AUTOMATON                     */
	/****************************************************************************************/


	/**
	 * Starting from an accepting state/node, this method walks its way back,
	 * using keys stored in PARENT_INDEX to find parents. When this is done, 
	 * an event, connecting the two nodes is found and added to the schedule, 
	 * while the parent becomes the next node in search of its parent. This
	 * is done until an initial node is found, which completes the construction.
	 *
	 * @param int[] currNode - the accepting node that makes it possible to build the schedule backwards
	 * @return Automaton schedule - the resulting schedule
	 */
    public Automaton buildScheduleAutomaton(int[] currNode) 
		throws Exception
	{
		timer.start();

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
				logger.error("AbstractAstar::buildScheduleAutomaton() --> Could not find the arc between " + printArray(currNode) + " and its parent" + printNodeName(getParent(currNode)));
				logger.debug(ex.getStackTrace());

				throw ex;
		    }
	    }
		
		logger.info("The schedule automaton was built in " + timer.elapsedTime() + " ms");
		
		return scheduleAuto;
    }
   
	/**
	* This method finds a parent to the current node by retrieving an element from 
	* the closedTree that corresponds to the key, stored in the current nodes PARENT_INDEX.
	* Caution should be made, since the closed element might consist of several nodes. 
	* The true parent is found by comparing the costs for all the nodes stored in the 
	* parent element. 
	*
	* @param int[] node - the node whose parent is seeked
	* @return int[] parentNode - the parent of the node 'node'
	*/
    protected int[] getParent(int[] node) 
		throws Exception 
	{
		// one object of the closedTree may contain several nodes (that all correspond
		// to the same logical state but different paths). 
		int[] parentCandidates = closedTree.get(new Integer(node[PARENT_INDEX]));

		int nrOfCandidates = parentCandidates.length / node.length;

		if (nrOfCandidates == 1)
			return parentCandidates;
		// which candidate is the true parent...
		else
		{
			// which plant (robot) fired the transition...
			int activePlantIndex = -1;

			// which cost is the new cost of the firing state...
			int newStateCost = -1;
			
			// activePlantIndex is found as the index that corresponds to a plant automaton
			// (in the plantAutomata-collection) and differs between this node and its parent
			for (int j=0; j<activeAutomataIndex.length; j++)
			{
				int currIndex = activeAutomataIndex[j];
				
				// This might look strange, but it is not. All parent candidates corresponds to 
				// the same logical state. In other words, it is enough to do the state indices check
				// for some of the parent candidates, why not the first.
				if (node[currIndex] != parentCandidates[currIndex])
				{
					activePlantIndex = j;
					
					// The new cost (after update) is equal to the cost of the state that is changed
					// during the transition, i.e. the state corresponding to the firing robot.
					newStateCost = theAutomata.getAutomatonAt(currIndex).getStateWithIndex(node[currIndex]).getCost();
					
					break;
				}
			}
			
			int[] parentCosts = new int[ESTIMATE_INDEX - CURRENT_COSTS_INDEX + 1];

			// The only thing that differs between the candidates is their cost-vectors
			// So, find the parent that gives correct cost update for the current state
			// and return it. Return null if nothing appropriate is found. 
			for (int i=0; i<nrOfCandidates; i++)
			{
				// Retrieving the costs of the current parent candidate
				for (int j=0; j<parentCosts.length; j++)
				{
					parentCosts[j + CURRENT_COSTS_INDEX] = parentCandidates[j + CURRENT_COSTS_INDEX + i*node.length];
				}

				int[] newCosts = updateCosts(parentCosts, activePlantIndex, newStateCost);

				// If the cost update (that corresponds to the transition from parent candidate to the current node
				// is equal to the costs of the current node, then the true parent is found.
				boolean isParent = true;
				for (int j=0; j<newCosts.length; j++)
				{
					if (newCosts[j] != node[j + CURRENT_COSTS_INDEX])
					{
						isParent = false;
						break;
					}
				}

				// If the parent was found, return it.
				if (isParent)
				{
					int[] parent = new int[node.length];

					for (int j=0; j<parent.length; j++)
						parent[j] = parentCandidates[j + i*node.length];

					return parent;
				}
			}

			return null;
		}		
    }   

	/**
	 * Checks if the current node contains a reference to its parent.
	 * The reference should be stored in the PARENT_INDEX. If its value 
	 * is -1, the current node has no recognized parent.
	 */
    protected boolean hasParent(int[] node) {
		if (node[PARENT_INDEX] == -1)
			return false;
	
		return true;
    }
    
	/**
	 * Finds an event between two nodes. In order to do this, the automaton that is 
	 * responsible for the transition, i.e. the plant automaton, whose indices differ 
	 * between the two nodes, is found.
	 *
	 * @param int[] fromNode - the "from" end of the seeked transition
	 * @param int[] toNode   - the "to" end of the seeked transition
	 * @return LabeledEvent connectingEvent - the event between "fromNode" and "toNode"
	 */
    protected LabeledEvent findCurrentEvent(int[] fromNode, int[] toNode) throws Exception {
		for (int i=0; i<theAutomata.size(); i++) {
			if (fromNode[i] != toNode[i]) {
				Automaton auto = theAutomata.getAutomatonAt(i);
				return auto.getLabeledEvent(auto.getStateWithIndex(fromNode[i]), auto.getStateWithIndex(toNode[i]));
			}
		}
	
		return null;
    }


	/****************************************************************************************/
	/*                                 AUXILIARY METHODS                                    */
	/****************************************************************************************/


    public String printArray(int[] node) {
		String s = "[";
		
		for (int i=0; i<node.length-1; i++) {
			s += node[i] + " ";
		}
		s += node[node.length-1] + "]";
		
		return s;
    }

	public String printArray(double[] array) 
	{
		String s = "[";
		
		for (int i=0; i<array.length-1; i++) 
		{
			s += array[i] + " ";
		}
		s += array[array.length-1] + "]";
		
		return s;
    }
	
    protected String printNodeSignature(int[] node) {
		String s = printNodeName(node) + "; Tv = [";
		
		int addIndex = node.length - (plantAutomata.size() + 1);
		for (int i=0; i<getActiveLength()-1; i++) 
			s += node[i + CURRENT_COSTS_INDEX] +  " ";
		s += node[ACCUMULATED_COST_INDEX-1] + "]; g = ";
		
		s += node[ACCUMULATED_COST_INDEX];
		
		return s;
    }
	
    protected String printNodeName(int[] node) {
		String s = "[";
		
		for (int i=0; i<theAutomata.size()-1; i++) {
			s += theAutomata.getAutomatonAt(i).getStateWithIndex(node[i]) + " ";
		}
		s += theAutomata.getAutomatonAt(theAutomata.size()-1).getStateWithIndex(node[theAutomata.size()-1]) + "]";
		
		return s;
    }
   
	/**
	 * Returns true if all the states that this node represents are accepting.
	 *
	 * @param int[] node - the current node
	 * @return boolean isAccepting - true if all the corresponing states are accepting
	 */
    protected boolean isAcceptingNode(int[] node) {
		for (int i=0; i<activeAutomataIndex.length; i++) {
			int index = activeAutomataIndex[i];
			if (!theAutomata.getAutomatonAt(index).getStateWithIndex(node[index]).isAccepting()) 
				return false;
		}
	
		return true;
    }

	/**
	 * Calculates a key that is used to order the nodes in the closedTree. 
	 * Every logical state maps uniquely to a key. Note though that the nodes
	 * corresponding to the same state get identical keys. 
	 *
	 * @param int[] node - the current node
	 * @return int key - the key for the node ordering in the closedTree
	 */
    public int getKey(int[] node) {
		int key = 0;
	
		for (int i=0; i<activeAutomataIndex.length; i++)
			key += node[activeAutomataIndex[i]]*keyMapping[activeAutomataIndex[i]];
	
		return key;
    }
    
    protected int[] resetCosts(int[] node) {	
		for (int i = 2*theAutomata.size() + AutomataIndexFormHelper.STATE_EXTRA_DATA; i<node.length; i++)
			node[i] = 0;
	
		return node;
    }
	
    /**
     * 	Calculates an index to Hashtable[] twoProdRelax.
     *
     * @param i
     * @param j
     * @return index of the 2-prod-relax-hashtable corresponing to robot_i and robot_j.
     */
	protected int calcHashtableIndex(int i, int j) 
	{
		int size = theAutomata.getPlantAutomata().size();
		
		return (int) (i*(size - 1.5) - 0.5*(i*i) - 1 + j);
	}
	
    public int getActiveLength() {
		return activeAutomataIndex.length; 
    }

    public int[] getActiveAutomataIndex() {
		return activeAutomataIndex;
    }
}
