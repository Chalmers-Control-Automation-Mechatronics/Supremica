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
    private static Logger logger = LoggerFactory.createLogger(AbstractAstar.class);
    
    /** Starts ticking when the search/walk through the nodes is started. Shows the duration of the scheduling. */
    protected ActionTimer timer;
    
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
    
    protected int searchCounter;

    /** Handles the expansion of nodes - either manually or using Supremicas methods */
    protected NodeExpander expander;

    /** If true, an iterative deepening search is used, which can be slower than the simple modified A*, but requires less memory. */
    protected boolean iterativeSearch;

    /** Is used to translate the state indices to unique hash values */
    protected int[] keyMapping;

    /** Decides whether 1-prod-relaxation should be used. */
    protected boolean useOneProdRelax;
    
    protected boolean consistentHeuristic = false;
 
    protected int accCostIndex, currCostIndex, parentIndex;
	public static int ESTIMATE_INDEX;

    protected String infoStr =  "";

    protected String heuristic = "";

    protected int[] currOptimalNode = null;

	/** Stores the maximum size of the openTree. */
	protected int maxOpenSize = 0;

    public AbstractAstar(Automata theAutomata) throws Exception  {
		this(theAutomata, "1-product relax");
    }

    public AbstractAstar(Automata theAutomata, String heuristic) throws Exception {
		this(theAutomata, heuristic, true, false);
    }

    public AbstractAstar(Automata theAutomata, String heuristic, boolean manualExpansion, boolean iterativeSearch) throws Exception {
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

	//////////////////////////////////////////////////////////////////////////////////////////
	//                                 ABSTRACT METHODS                                     //
	//////////////////////////////////////////////////////////////////////////////////////////

    protected abstract int[] makeInitialNode();

	protected abstract void branch(int[] currNode);


	//////////////////////////////////////////////////////////////////////////////////////////
	//                                                                                      //
	//////////////////////////////////////////////////////////////////////////////////////////

    protected void init(boolean manualExpansion) 
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
			// Tillfälligt bortkommenterat	    
			// 	    if (plantAutomata.size() > 2) {
			// 		timer.start();
			// 		//				String prep2Info = preprocess2();
			// 		preprocess2();
			// 		infoStr += "\t2nd preprocessing in " + timer.elapsedTime() + " ms\n";
			// 		//				infoStr += prep2Info;
			// 	    }
	    
			//Tillf
			long millis = System.currentTimeMillis();

			timer.start();
			searchCounter = 0;

			int[] accNode = scheduleFrom(makeInitialNode());
			if (accNode == null)
				throw new RuntimeException("no marked state found, nr of iteration = " + searchCounter); 

			infoStr += "\tA*-iterations (nr of search calls through the closed tree): " + searchCounter + "\n";
			infoStr += "\tIn time: " + timer.elapsedTime() + " ms\n";
			infoStr += "\tThe CLOSED tree contains (at the end) " + closedTree.size() + " elements\n";
			infoStr += "\tMax{OPEN.size} = " + maxOpenSize + "\n";
			// 	    infoStr += "\t\t"+ printNodeSignature(accNode);
			infoStr += "\t\t" + "g = " + accNode[accCostIndex];
			
			logger.info(infoStr);
	    
			return accNode;
		}
    }

    protected void initAuxIndices() {
		activeAutomataIndex = new int[plantAutomata.size()];
		for (int i=0; i<activeAutomataIndex.length; i++) {
			activeAutomataIndex[i] = theAutomata.getAutomatonIndex(plantAutomata.getAutomatonAt(i));
		}

		parentIndex = theAutomata.size() + AutomataIndexFormHelper.STATE_EXTRA_DATA;
		
		// Detta borde ändras till STATIC_VARIBEL = 1 (och inte 2) => 2 ändringar
		currCostIndex = parentIndex + 2; //ClosedNodes.CLOSED_NODE_INFO_SIZE;
		accCostIndex = currCostIndex + activeAutomataIndex.length;
		ESTIMATE_INDEX = accCostIndex + 1;
    }
    
    public String printArray(int[] node) {
		String s = "[";
		
		for (int i=0; i<node.length-1; i++) {
			s += node[i] + " ";
		}
		s += node[node.length-1] + "]";
		
		return s;
    }
    
    public String printArray(double[] array) {
		String s = "[";
		
		for (int i=0; i<array.length-1; i++) {
			s += array[i] + " ";
		}
		s += array[array.length-1] + "]";
		
		return s;
    }
	
    protected String printNodeSignature(int[] node) {
		String s = printNodeName(node) + "; Tv = [";
		
		int addIndex = node.length - (plantAutomata.size() + 1);
		for (int i=0; i<getActiveLength()-1; i++) 
			s += node[i + currCostIndex] +  " ";
		s += node[accCostIndex-1] + "]; g = ";
		
		s += node[accCostIndex];
		
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

    protected int[] scheduleFrom(int[] initNode) {
		try {
			int[] currNode = new int[initNode.length];
			
			openTree.clear();
			closedTree.clear();

			openTree.add(initNode);

			while(! openTree.isEmpty())
			{
				searchCounter++;

				// Tillf ...
				int currOpenSize = openTree.size();
				if (currOpenSize > maxOpenSize)
					maxOpenSize = currOpenSize;
				// ... fram hit

				currNode = openTree.first();

				if (isAcceptingNode(currNode))
					return currNode;

				boolean succesfullyRemoved =  openTree.remove(currNode);
				if (! succesfullyRemoved)
					throw new Exception("The node " + printNodeName(currNode) + " was not found on the openTree");

				branch(currNode);
			}

			logger.error("An accepting node could not be found...");
			return currNode;
		}
		catch (Exception e) 
		{ 
			e.printStackTrace(); 
			return null; 
		}
    }

    protected boolean higherCostInAllDirections(int[] currNode, int[] existingNode) {
		for (int i=0; i<activeAutomataIndex.length; i++) {
			if ((currNode[i+currCostIndex] + currNode[accCostIndex]) < (existingNode[i+currCostIndex]+existingNode[accCostIndex]))
				return false;
		}

		return true;
    }

    protected boolean smallerCostInAllDirections(int[] currNode, int[] existingNode) {
		return higherCostInAllDirections(existingNode, currNode);
    }

    protected boolean higherCostInAllDirections(int[] currNode, ArrayList<int[]> existingNodes) {
		for (Iterator<int[]> iter = existingNodes.iterator(); iter.hasNext(); )
			if (!higherCostInAllDirections(currNode, iter.next()))
				return false;

		return true;
    }

    protected int smallerCostInAllDirectionsIndex(int[] currNode, ArrayList<int[]> existingNodes) {
		for (int i=0; i<existingNodes.size(); i++) {
			if (smallerCostInAllDirections(currNode, existingNodes.get(i)))
				return i;
		}
	 
		return -1;
    }
    
    public int calcEstimatedCost(int[] node) throws Exception {
		if (useOneProdRelax) {
			// 	    return node[node.length-1] + getOneProdRelaxation(node);

			// The following code inside the if-loop is needed to ensure consistency of the heuristic
			int[] parent = getParent(node);
	    
			int fNode = node[accCostIndex] + getOneProdRelaxation(node);
			int fParent = parent[accCostIndex] + getOneProdRelaxation(parent);

			if (fParent > fNode)
				return fParent;
			else
				return fNode;
		}
		else if (heuristic.equals("brute force"))
			return node[accCostIndex];
		else {
			logger.error("2-prod relaxation not implemented yet");
			return -1;
		}
		// 	    return node[node.length-1] + getTwoProdRelaxation(node);
    }
    
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
    
    protected boolean isAcceptingNode(int[] node) {
		for (int i=0; i<activeAutomataIndex.length; i++) {
			int index = activeAutomataIndex[i];
			if (!theAutomata.getAutomatonAt(index).getStateWithIndex(node[index]).isAccepting()) 
				return false;
		}
	
		return true;
    }

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
	
    protected int[] getStates(int[] node) {
		int[] states = new int[theAutomata.size()];
	
		for (int i=0; i<states.length; i++)
			states[i] = node[i];
	
		return states;
    }
    
    /**
     * Calculates the costs for a one-product relaxation (i.e. as if there
     * would only be one robot in the cell) and stores it in oneProdRelax.
     */	
    protected void preprocess1() {
		for (int i=0; i<plantAutomata.size(); i++) {
			Automaton theAuto = plantAutomata.getAutomatonAt(i);
			State markedState = findAcceptingState(theAuto);
			ArrayList estList = new ArrayList();
	    
			oneProdRelax[i] = new int[theAuto.nbrOfStates()];
			for (int j=0; j<oneProdRelax[i].length; j++) 
				oneProdRelax[i][j] = -1;
	    
			if (markedState == null)
				return;
			else {
				oneProdRelax[i][markedState.getIndex()] = markedState.getCost();

				estList.add(markedState);
		
				while (!estList.isEmpty()) {
					ArcIterator incomingArcIterator = ((State)estList.remove(0)).incomingArcsIterator();
		    
					while (incomingArcIterator.hasNext()) {
						Arc currArc = incomingArcIterator.nextArc();
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
		protected int calcHashtableIndex(int i, int j) {
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
    
    protected boolean hasParent(int[] node) {
		if (node[theAutomata.size() + AutomataIndexFormHelper.STATE_EXTRA_DATA] == -1)
			return false;
	
		return true;
    }
    
    protected int[] getParent(int[] node) 
		throws Exception 
	{
		// one object of the closedTree may contain several nodes (that all correspond
		// to the same logical state but different paths). 
		int[] parentCandidates = closedTree.get(new Integer(node[parentIndex]));

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
			
			int[] parentCosts = new int[ESTIMATE_INDEX - currCostIndex + 1];

			// The only thing that differs between the candidates is their cost-vectors
			// So, find the parent that gives correct cost update for the current state
			// and return it. Return null if nothing appropriate is found. 
			for (int i=0; i<nrOfCandidates; i++)
			{
				// Retrieving the costs of the current parent candidate
				for (int j=0; j<parentCosts.length; j++)
				{
					parentCosts[j + currCostIndex] = parentCandidates[j + currCostIndex + i*node.length];
				}

				int[] newCosts = updateCosts(parentCosts, activePlantIndex, newStateCost);

				// If the cost update (that corresponds to the transition from parent candidate to the current node
				// is equal to the costs of the current node, then the true parent is found.
				boolean isParent = true;
				for (int j=0; j<newCosts.length; j++)
				{
					if (newCosts[j] != node[j + currCostIndex])
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
		
		logger.info(timer.elapsedTime() + " ms was needed to build the schedule automaton");
		
		return scheduleAuto;
    }
    
    protected LabeledEvent findCurrentEvent(int[] fromNode, int[] toNode) throws Exception {
		for (int i=0; i<theAutomata.size(); i++) {
			if (fromNode[i] != toNode[i]) {
				Automaton auto = theAutomata.getAutomatonAt(i);
				return auto.getLabeledEvent(auto.getStateWithIndex(fromNode[i]), auto.getStateWithIndex(toNode[i]));
			}
		}
	
		return null;
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

    public int getActiveLength() {
		return activeAutomataIndex.length; 
    }

    public int getCurrCostIndex() {
		return currCostIndex;
    }	

    public int[] getActiveAutomataIndex() {
		return activeAutomataIndex;
    }
}
