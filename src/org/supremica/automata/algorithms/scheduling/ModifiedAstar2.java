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
import org.supremica.automata.algorithms.AutomataSynchronizerExecuter;
import org.supremica.automata.algorithms.AutomataSynchronizerHelper;
import org.supremica.automata.algorithms.SynchronizationOptions;
import org.supremica.util.ActionTimer;

public class ModifiedAstar2
{
	private static Logger logger = LoggerFactory.createLogger(ModifiedAstar2.class);

	/** Starts ticking when the search/walk through the nodes is started. Shows the duration of the scheduling. */
	private ActionTimer timer;

	/** Contains "opened" but not yet examined (i.e. not "closed") nodes. */
	private ArrayList openList;

	/** Contains already examined (i.e. "closed") nodes. */
	//private ArrayList closedList;
	private Hashtable closedNodes;
	
//	private TreeMap openNodes;

	/** Hashtable containing the estimated cost for each robot, having states as keys. **/
//	private Hashtable[] oneProdRelax;
	private ArrayList oneProdRelax;

	/** Hashtable containing the estimated cost for each combination of two robots **/
	private Hashtable[] twoProdRelax;

	/** Needed for online expansion of the Nodes **/
	private AutomataSynchronizerExecuter onlineSynchronizer;

	/** Det kanske kan inte behövs utan kan fixas genom helper (???) **/
	private AutomataIndexForm autoIndexForm;

	//Den kanske man inte behöver spara... AK
	private Automaton theAutomaton;

	private Automata theAutomata;

	private Automata plantAutomata;

	private int[] activeAutomataIndex;

	private int searchCounter;
	
	// Hjälper till att omvandla stateIndex-ar till hash-värden.
	private int[] keyMapping;
	
	boolean useOneProdRelax;

	public ModifiedAstar2(Automaton theAutomaton)
	{
		this.theAutomaton = theAutomaton;
		init();
	}

	public ModifiedAstar2(Automata theAutomata) {
		this.theAutomata = theAutomata;
		init();
	}

	private void init()
	{
		timer = new ActionTimer();
		openList = new ArrayList();
		closedNodes = new Hashtable();

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

			initOnlineSynchronizer(theAutomata);
		}
	}

	/**
	 *      Walks through the tree of possible paths in search for the optimal one.
	 */
	public int[] walk() throws Exception
	{
		expandNode(makeInitialNode());
		
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
	
	private String printArray(int[] node) {
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
			
			if (isAcceptingNode(currNode))
				return currNode;
			
			closedNodes.put(getKey(currNode), currNode);
			
			useOneProdRelax = false;
			if (activeAutomataIndex.length <= 2 || plantAutomata.size() <= 2)
				useOneProdRelax = true;

			Iterator childIter = expandNode(currNode).iterator();
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
				if (estimatedCost >= calcEstimatedCost(openNode))
					return true;
				else {
					openList.set(index, node);
					return false;
				}
			}
		}

		if (closedNodes.containsKey(currKey)) {
			if (estimatedCost >= calcEstimatedCost((int[])closedNodes.get(currKey)))
				return true;
			else {
				closedNodes.remove(node);
				return false;
			}
		}
	
		return false;
	}
	
	private int calcEstimatedCost(int[] node) {
		if (useOneProdRelax)
			return node[node.length-1] + getOneProdRelaxation(node);
		else
			return node[node.length-1] + getTwoProdRelaxation(node);
	}
	
	private int getOneProdRelaxation(int[] node) {
		int estimate = 0;
		int[] currCosts = getCosts(node);

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
		int[] currentCosts = getCosts(node);
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

	private int getTwoProdRelaxation(Node theNode) {
		int plantAutomataSize = plantAutomata.size();
		int[] currentCosts = theNode.getCurrentCosts();

		int estimate = 0;
		for (int i=0; i<plantAutomataSize-1; i++) {
			for (int j=i+1; j<plantAutomataSize; j++) {

				// Funkar bara om alla noder i den totala synkningen har 2-prod-relaxerats
				// Har de det?????????????
				Object relaxation = twoProdRelax[calcHashtableIndex(i,j)].get(theNode);
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
/*
	private Node scheduleFrom(Node initNode) {
		return scheduleFrom(initNode, null);
	}

	private Node scheduleFrom(Node initNode, int[] currAutomataIndex) {
		openList.clear();
//		openNodes.clear();
		closedNodes.clear();
		openList.add(initNode);
//		openNodes.put(new Double(0), initNode);
		
		while (!openList.isEmpty()) {
//		while (!openNodes.isEmpty()) {
			searchCounter++;
			Node currNode = (Node) openList.remove(0);
//			Node currNode = (Node) openNodes.remove(openNodes.firstKey());
		
			if (currNode.isAccepting())
				return currNode;
			else if (currAutomataIndex != null) {
				boolean acceptingWhenRelaxed = true;

				for (int i=0; i<currAutomataIndex.length; i++) {
					if (!currNode.getState(currAutomataIndex[i]).isAccepting())
						acceptingWhenRelaxed = false;
				}

				if (acceptingWhenRelaxed)
					return currNode;
			}

			boolean useOneProdRelax = false;
			if (currAutomataIndex != null || theAutomata.getPlantAutomata().size() <= 2)
				useOneProdRelax = true;

			closedNodes.put(currNode, new Integer(calcEstimatedCost(currNode, useOneProdRelax)));

			Iterator childIter = expandNode(currNode, currAutomataIndex).iterator();
			while (childIter.hasNext()) {
				Node childNode = (Node)childIter.next();

				if (!isOnAList(childNode, useOneProdRelax))
					putOnOpenList(childNode, useOneProdRelax);
			}
		}

		logger.error("Inget markerat tillstånd kunde hittas............");
		return null;
	}
*/
	private Collection<Integer> expandNode(int[] node) {
		
		Hashtable childNodes = new Hashtable();
		int[] currStateIndex = AutomataIndexFormHelper.createState(theAutomata.size());
		for (int i=0; i<currStateIndex.length; i++)
			currStateIndex[i] = node[i];

		int[] currOutgoingEvents = onlineSynchronizer.getOutgoingEvents(currStateIndex);

		for (int i=0; i<currOutgoingEvents.length; i++) {
			if (onlineSynchronizer.isEnabled(currOutgoingEvents[i])) {
				int[] nextStateIndex = onlineSynchronizer.doTransition(currStateIndex, currOutgoingEvents[i]);

				int changedIndex = -1;
				for (int k=0; k<activeAutomataIndex.length; k++) {
					if (nextStateIndex[activeAutomataIndex[k]] != currStateIndex[activeAutomataIndex[k]]) {
						changedIndex = k;
						break;
					}
				}

				if (changedIndex > -1) { // || activeAutomataIndex.length == plantAutomata.size()) {
					Integer currKey = getKey(nextStateIndex);

					if (!childNodes.contains(currKey)) {
						int newCost = plantAutomata.getAutomatonAt(changedIndex).getStateWithIndex(nextStateIndex[activeAutomataIndex[changedIndex]]).getCost();
						int[] newCosts = updateCosts(getCosts(node), changedIndex, newCost);
					
						childNodes.put(currKey, makeNode(nextStateIndex, node, newCosts));
					}
				}
			}
		}

		return childNodes.values();
	}

	private Integer getKey(int[] node) {
		int key = 0;
		
		for (int i=0; i<activeAutomataIndex.length; i++)
			key += node[activeAutomataIndex[i]]*keyMapping[activeAutomataIndex[i]];
		
		return new Integer(key);
	}
	
	private int[] makeNode(int[] stateIndices, int[] parentNode, int[] costs) {
		int[] newNode = new int[stateIndices.length + theAutomata.size() + costs.length];
		
		for (int i=0; i<stateIndices.length; i++)
			newNode[i] = stateIndices[i];
		if (parentNode != null) {
			for (int i=0; i<theAutomata.size(); i++)
				newNode[i + stateIndices.length] = parentNode[i];
		}
		else {
			for (int i=0; i<theAutomata.size(); i++)
				newNode[i + stateIndices.length] = -1;
		}
		for (int i=0; i<costs.length; i++)
			newNode[i + stateIndices.length + theAutomata.size()] = costs[i];
		
		return newNode;
	}
	
	private int[] updateCosts(int[] costs, int changedIndex, int newCost) {
		int[] newCosts = new int[costs.length];
		
		for (int i=0; i<costs.length-1; i++) {
			if (i == changedIndex)
				newCosts[i] = newCost;
			else {
				newCosts[i] = costs[i] - costs[changedIndex]; 
				if (newCosts[i] < 0)
					newCosts[i] = 0;
			}
		}
		
		newCosts[newCosts.length-1] = costs[costs.length-1] + costs[changedIndex];
		
		return newCosts;
	}
	
	private int[] resetCosts(int[] node) {	
		for (int i = 2*theAutomata.size() + AutomataIndexFormHelper.STATE_EXTRA_DATA; i<node.length; i++)
			node[i] = 0;
		
		return node;
	}
	
	private int[] getCosts(int[] node) {
		int[] costs = new int[plantAutomata.size() + 1];
		int startIndex = 2*theAutomata.size() + AutomataIndexFormHelper.STATE_EXTRA_DATA;
		
		for (int i=0; i<costs.length; i++)
			costs[i] = node[startIndex + i];
		
		return costs;
	}
	
	private int[] getStates(int[] node) {
		int[] states = new int[theAutomata.size()];
		
		for (int i=0; i<states.length; i++)
			states[i] = node[i];
		
		return states;
	}

	private Collection expandNode(Node node) {
		return expandNode(node, null);
	}

	private Collection expandNode(Node node, int[] currAutomataIndex) {
		Hashtable childNodes = new Hashtable();
		int[] currStateIndex = AutomataIndexFormHelper.createState(node.size());

		for (int i=0; i<node.size(); i++)
			currStateIndex[i] = node.getState(i).getIndex();

		int[] currOutgoingEvents = onlineSynchronizer.getOutgoingEvents(currStateIndex);

		for (int i=0; i<currOutgoingEvents.length; i++) {
			if (onlineSynchronizer.isEnabled(currOutgoingEvents[i])) {
				int[] nextStateIndex = onlineSynchronizer.doTransition(currStateIndex, currOutgoingEvents[i]);

				boolean indexChanged = false;
				if (currAutomataIndex != null) {
					for (int k=0; k<currAutomataIndex.length; k++) {
						if (nextStateIndex[currAutomataIndex[k]] != currStateIndex[currAutomataIndex[k]])
							indexChanged = true;
					}
				}

				if (currAutomataIndex == null || indexChanged) {
					State[] theStates = new State[currStateIndex.length-2];
					for (int j=0; j<theStates.length; j++)
						theStates[j] = autoIndexForm.getState(j, nextStateIndex[j]);

					Node newNode = new Node(theStates, node);
					if (!childNodes.containsValue(newNode))
						childNodes.put(newNode, newNode);
				}
			}
		}

		return childNodes.values();
	}
	
	/**
	 * 			Inserts the node into the openList according to the estimatedCost (ascending).
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

	/**
	 * @param initialState
	 */
	private void initOnlineSynchronizer(Automata synchedAutos) {
		//	Get current options
		SynchronizationOptions syncOptions = new SynchronizationOptions();
		syncOptions.setBuildAutomaton(false);
		syncOptions.setRequireConsistentControllability(false);

		try {
			AutomataSynchronizerHelper helper = new AutomataSynchronizerHelper(synchedAutos, syncOptions);
			onlineSynchronizer = new AutomataSynchronizerExecuter(helper);
			onlineSynchronizer.initialize();
			autoIndexForm = helper.getAutomataIndexForm();
			
			// Så fult borde det väl inte vara ändå... Buggen borde tas om hand i AutomataIndexForm tycker man. 
			for (int i=0; i<theAutomata.size(); i++)
				theAutomata.getAutomatonAt(i).remapStateIndices();
		} catch (Exception e) {
			e.printStackTrace();
		}
		//helper.setCoExecuter(onlineSynchronizer);
	}

	// Bör kommas bort snart
/*	private State[] makeInitialState() {
		State[] initialState = new State[theAutomata.size()];

		for (int i=0; i<theAutomata.size(); i++)
			initialState[i] = theAutomata.getAutomatonAt(i).getInitialState();

		return initialState;
	}
*/
	
	private int[] makeInitialNode() {
		int[] initialStates = AutomataIndexFormHelper.createState(theAutomata.size());
		int[] initialCosts = new int[plantAutomata.size() + 1];

		for (int i=0; i<theAutomata.size(); i++) 
			initialStates[i] = theAutomata.getAutomatonAt(i).getInitialState().getIndex();
		
		for (int i=0; i<initialCosts.length-1; i++) 
			initialCosts[i] = plantAutomata.getAutomatonAt(i).getInitialState().getCost();
		initialCosts[initialCosts.length-1] = 0;

		return makeNode(initialStates, null, initialCosts);
	}

	/**
	 * 			Calculates the costs for a one-product relaxation (i.e. as if there
	 * 			would only be one robot in the cell) and stores it in oneProdRelax.
	 *
	 * @return	false, if any of the automata to be scheduled does not contain
	 * 			an accepting state.
	 * @return 	true, otherwise
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
						activeNodes.addAll(expandNode(currNode));
		
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

	/**
	 * 	Räknar ut en passande index för hashtabellen till en tvårobot-relaxering mha en
	 * 	klurig (och förhoppningsvis korrekt) formel.
	 * @param i
	 * @param j
	 * @return
	 */
	private int calcHashtableIndex(int i, int j) {
		int size = theAutomata.getPlantAutomata().size();

		return (int) (i*(size - 1.5) - 0.5*(i*i) - 1 + j);
	}
/*
	private int calcEstimatedCost(Node theNode) {
		return calcEstimatedCost(theNode, false);
	}
*/

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

	/**
	 *      Inserts a state into openList in descending accumulatedTime order.
	 */
/*	private void insertIntoOpenList(Node node)
	{
		int accCost = node.getAccumulatedCost();
		int cursor = 0;

		while ((cursor < openList.size()) && (accCost > ((Node) openList.get(cursor)).getAccumulatedCost()))
		{
			cursor++;
		}

		openList.add(cursor, node);
	}
*/
	/**
	 *      Inserts the state into the closedList unless  an instance
	 *      of this state with a cheaper cost has already been examined and placed
	 *      into closedList.
	 *
	 *      @return true if the insertion operation has been performed.
	 */
/*	private boolean insertIntoClosedList(Node node)
	{
		String key = node.getName();
		int cursor = 0;

		while ((cursor < closedList.size()) && (key.compareTo(((Node) closedList.get(cursor)).getName()) > -1))
		{
			Node currListNode = (Node) closedList.get(cursor);

			if (key.compareTo(currListNode.getName()) == 0)
			{
				if (isCheaper(node, currListNode))
				{

					// om mindre -> Länka den stängda noden till state. Return true.
					currListNode = node;

					return true;
				}
				else if (isCheaper(currListNode, node))
				{

					// Om större -> returnera false, det ska inte läggas till i closedList
					return false;
				}
			}

			++cursor;
		}

		// Utförs bara om inget annat returnerat innan
		closedList.add(cursor, node);

		return true;
	}
*/
	
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
	
/*	public Automaton buildScheduleAutomaton(Node currNode)
	{
		Automaton scheduleAuto = new Automaton();
		scheduleAuto.setComment("Schedule");

		State nextState = new State(currNode.toString());
		nextState.setAccepting(true);
		scheduleAuto.addState(nextState);

		while (currNode.getParent() != null)
		{
			try
			{
				State currState = new State(currNode.getParent().toString());
				LabeledEvent event = findCurrentEvent(currNode.getParent(), currNode);

				if (currNode.getParent().getParent() == null)
					currState.setInitial(true);

				scheduleAuto.addState(currState);
				scheduleAuto.getAlphabet().addEvent(event);
				scheduleAuto.addArc(new Arc(currState, nextState, event));

				currNode = currNode.getParent();
				nextState = currState;
			}
			catch (Exception ex)
			{
				logger.error("ModifiedAstar2::buildScheduleAutomaton() --> Could not find the arc between " + currNode + " and " + currNode.getParent());
				logger.debug(ex.getStackTrace());
			}
		}

		return scheduleAuto;
	}
*/
	
	/**
	 *      Returns the Arc between two States.
	 *      @return null if the Arc doesn't exist (this must not happen).
	 */
	// Kan (och bör) nog tas bort så småningom (vid städning)
/*	private Arc findCurrentArc(State parent, State child)
	{
		ArcIterator it = parent.outgoingArcsIterator();

		while (it.hasNext())
		{
			Arc currArc = it.nextArc();

			//if (currArc.getToState().getId().equals(child.getId()))
			if (currArc.getToState().equals(child))
			{
				return currArc;
			}
		}

		assert(false);

		return null;
	}
*/
	
/*	private LabeledEvent findCurrentEvent(Node fromNode, Node toNode) throws Exception {
		for (int i=0; i<fromNode.size(); i++) {
			if (!fromNode.getState(i).getName().equals(toNode.getState(i).getName())) {
				return theAutomata.getAutomatonAt(i).getLabeledEvent(fromNode.getState(i), toNode.getState(i));
			}
		}

		return null;
	}
*/
	
	private LabeledEvent findCurrentEvent(int[] fromNode, int[] toNode) throws Exception {
		for (int i=0; i<theAutomata.size(); i++) {
			if (fromNode[i] != toNode[i]) {
				Automaton auto = theAutomata.getAutomatonAt(i);
				return auto.getLabeledEvent(auto.getStateWithIndex(fromNode[i]), auto.getStateWithIndex(toNode[i]));
			}
		}

		return null;
	}

	/**
	 *      Checks if a possible accumulatedCost-loss is fully compensated by
	 *      every currentCosts-savings.
	 */
/*	private boolean isCheaper(Node theNode, Node checkNode)
	{
		if (theNode.getCurrentCosts() == null)
		{
			return (theNode.getAccumulatedCost() < checkNode.getAccumulatedCost());
		}
		else
		{
			int accCost1 = theNode.getAccumulatedCost();
			int accCost2 = checkNode.getAccumulatedCost();
			int[] currCosts1 = theNode.getCurrentCosts();
			int[] currCosts2 = checkNode.getCurrentCosts();

			for (int i = 0; i < currCosts1.length; i++)
			{
				if ((accCost1 + currCosts1[i]) > (accCost2 + currCosts2[i]))
				{
					return false;
				}
			}

			return true;
		}
	}
*/
/*	
	private int calcEstimatedCost(Node theNode, boolean useOneProdRelax) {
		if (useOneProdRelax)
			return theNode.getAccumulatedCost() + getOneProdRelaxation(theNode);
		else
			return theNode.getAccumulatedCost() + getTwoProdRelaxation(theNode);
	}
*/
}
