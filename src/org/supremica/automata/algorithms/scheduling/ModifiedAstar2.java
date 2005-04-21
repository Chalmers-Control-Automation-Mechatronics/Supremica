 
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
	
	/** Hashtable containing the estimated cost for each robot, having states as keys. **/
	private Hashtable[] oneProdRelax; 
	
	/** Hashtable containing the estimated cost for each combination of two robots **/
	private Hashtable[] twoProdRelax;
	
	/** Needed for online expansion of the Nodes **/
	private AutomataSynchronizerExecuter onlineSynchronizer;
	
	/** Det kanske kan inte beh�vs utan kan fixas genom helper (???) **/ 
	private AutomataIndexForm autoIndexForm;

	//Den kanske man inte beh�ver spara... AK
	private Automaton theAutomaton;
	
	private Automata theAutomata;
	
//	private int[] currAutomataIndex;
	
	private int searchCounter;

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
//		closedList = new ArrayList();
		closedNodes = new Hashtable();
		
		if (theAutomata == null)
			oneProdRelax[0] = new Hashtable();
		else {
			int nrOfPlants = theAutomata.getPlantAutomata().size();
			oneProdRelax = new Hashtable[nrOfPlants];
			for (int i=0; i<nrOfPlants; i++)
				oneProdRelax[i] = new Hashtable();	
		
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
	public Node walk()
	{
		// Den h�r if/else-slingan �r ful och borde snyggas till (ers�ttas) en dag. 
		// K�rs ig�ng om en synkning har valts som schemal�ggningsoffer.
		if (theAutomata == null) {
/*
			timer.start();
	
			int counter = 0;
	
			openList.add(new Node(((State) theAutomaton.getInitialState())));
	
			while (!openList.isEmpty())
			{
				Node currNode = (Node) openList.remove(0);
	
				counter++;
	
				if (insertIntoClosedList(currNode))
				{
					if (currNode.isAccepting())
					{
						timer.stop();
						logger.info("Nr of searched states = " + counter);
	
						return currNode;
					}
	
					StateIterator states = currNode.getCorrespondingState().nextStateIterator();
	
					while (states.hasNext())
					{
						Node currChildNode = new Node((State) states.nextState(), currNode);
	
						insertIntoOpenList(currChildNode);
					}
				}
			}
*/	
			return null;
		}
		// Om ist�llet flera automater skall online-synkas-och-schemal�ggas...
		else {
			timer.start();		
			preprocess1();
			logger.info("Time for 1st preprocessing: " + timer.elapsedTime() + " ms");
			
			if (theAutomata.getPlantAutomata().size() > 2) {
				timer.start();
				preprocess2();
				logger.info("Time for 2nd preprocessing: " + timer.elapsedTime() + " ms");
			}
			
//			currAutomataIndex = null;
			
		
			timer.start();
			Node currNode = scheduleFrom(new Node(makeInitialState()));

/*
			if (currNode == null)
				return null;
			else if (!currNode.isAccepting())
				return null;
			else {
*/
				logger.info(currNode + ". States searched: " + searchCounter + " in time: " + timer.elapsedTime() + " ms.");
				return currNode;
//			}
		}
	}
	
	private Node scheduleFrom(Node initNode) {
		return scheduleFrom(initNode, null);
	}
	
	private Node scheduleFrom(Node initNode, int[] currAutomataIndex) {
		openList.clear();
//		closedList.clear();
		closedNodes.clear();
		openList.add(initNode);
		
//		int counter = 0;
		
		while (!openList.isEmpty()) {
			searchCounter++;
			Node currNode = (Node) openList.remove(0);				
//			closedList.add(currNode);			
	
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
		
		logger.error("Inget markerat tillst�nd kunde hittas............");
		return null;
	}
	
	private Collection expandNode(Node node) {
		return expandNode(node, null);
	}
	
	private Collection expandNode(Node node, int[] currAutomataIndex) {
		//ArrayList childNodes = new ArrayList();
		Hashtable childNodes = new Hashtable();
		int[] currStateIndex = AutomataIndexFormHelper.createState(node.size());
		
		for (int i=0; i<node.size(); i++) 
			currStateIndex[i] = node.getState(i).getIndex();
	
		int[] currOutgoingEvents = onlineSynchronizer.getOutgoingEvents(currStateIndex);
//	logger.error("currOE.length = " + currOutgoingEvents.length);
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
	private void putOnOpenList(Node node, boolean useOneProdRelax) {
		int estimatedCost = calcEstimatedCost(node, useOneProdRelax);
		int counter = 0;
		Iterator iter = openList.iterator();
		
		while (iter.hasNext()) {
			Node n = (Node)iter.next();
			if (estimatedCost < calcEstimatedCost(n, useOneProdRelax)) {
				openList.add(counter, node);
				return;
			}
			counter++;
		}
		
		openList.add(node);
	}

	/**
	 * 			Checks if some node is on the openList or closedList. If found, a comparison 
	 * 			between the estimated costs is done to decide which node to keep as optimal. 
	 *  
	 * @param 	node
	 * @return 	true if node is already on the closedList and has an estimated cost not lower than 
	 * 		   	the guy on the closedList. 
	 */
	// Ocks� lite fult...
	private boolean isOnAList(Node node, boolean useOneProdRelax) {
		int estimatedCost = calcEstimatedCost(node, useOneProdRelax);
		
		Iterator iter = openList.iterator();
		while (iter.hasNext()) {
			Node n = (Node)iter.next();
			if (node.hashCode() == n.hashCode()) {
				if (estimatedCost >= calcEstimatedCost(n, useOneProdRelax))
					return true;
				else {
					openList.remove(openList.indexOf(n));
					return false;
				}
			}
		}
		
		if (closedNodes.containsKey(node)) {
			if (estimatedCost >= ((Integer)closedNodes.get(node)).intValue())
				return true;
			else {
				closedNodes.remove(node);
				return false;
			}
		}
		else
			return false;
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
		} catch (Exception e) {
			e.printStackTrace();
		}
		//helper.setCoExecuter(onlineSynchronizer);	
	}

	private State[] makeInitialState() {
		State[] initialState = new State[theAutomata.size()];

		for (int i=0; i<theAutomata.size(); i++)
			initialState[i] = theAutomata.getAutomatonAt(i).getInitialState();
		
		return initialState;
	}

	/**
	 * 			Calculates the costs for a one-product relaxation (i.e. as if there 
	 * 			would only be one robot in the cell) and stores it in the hashtable
	 * 			oneProdRelax.
	 * 
	 * @return	false, if any of the automata to be scheduled does not contain 
	 * 			an accepting state. 
	 * @return 	true, otherwise
	 */
	private boolean preprocess1() {		
		Automata plantAutomata = theAutomata.getPlantAutomata();
		
		for (int i=0; i<plantAutomata.size(); i++) {			
			Automaton theAuto = plantAutomata.getAutomatonAt(i);
			State markedState = findAcceptingState(theAuto);
			ArrayList estList = new ArrayList();
			
			if (markedState == null)
				return false;
			else {				
				oneProdRelax[i].put(markedState, new Integer(0));
				estList.add(markedState);
				
				while (!estList.isEmpty()) {
					ArcIterator incomingArcIterator = ((State)estList.remove(0)).incomingArcsIterator();
					
					while (incomingArcIterator.hasNext()) {
						Arc currArc = incomingArcIterator.nextArc();
						State currState = currArc.getFromState();
						int remainingCost = ((Integer)(oneProdRelax[i].get(currArc.getToState()))).intValue();
						
						if (oneProdRelax[i].get(currState) == null) {
							oneProdRelax[i].put(currState, new Integer(remainingCost + currArc.getToState().getCost()));							
							estList.add(currState);
						}
						else {
							int currRemainingCost = ((Integer)(oneProdRelax[i].get(currState))).intValue();
							int newRemainingCost = currArc.getToState().getCost() + remainingCost;
							
							if (newRemainingCost < currRemainingCost) {
								oneProdRelax[i].remove(currState);
								oneProdRelax[i].put(currState, new Integer(newRemainingCost));
							}
						}					
					}
				}			
			}	
		}
		
		return true;
	}
	
	/**
	 * 			Calculates the costs for a two-product relaxation (i.e. as if there 
	 * 			would only be two robots in the cell) and stores it in the hashtable
	 * 			twoProdRelax.
	 */
	private void preprocess2() {
		logger.warn("B�rjar preprocess2:a....");
		
//		int hashtableCounter = -1;
		Automata plantAutomata = theAutomata.getPlantAutomata();
		
		
		for (int i=0; i<1; i++) { //plantAutomata.size()-1; i++) {
			for (int j=i+1; j<i+2; j++) { //plantAutomata.size(); j++) {
//				hashtableCounter++;
				int hashtableIndex = calcHashtableIndex(i,j);
				
				int schedCounter = 0;
				
				int[] currAutomataIndex = new int[]{theAutomata.getAutomatonIndex(plantAutomata.getAutomatonAt(i)),
													theAutomata.getAutomatonIndex(plantAutomata.getAutomatonAt(j))};
/*				
				Automata currAutomata = new Automata(plantAutomata.getAutomatonAt(i));
				currAutomata.addAutomaton(plantAutomata.getAutomatonAt(j));
			currAutomata.addAutomata(theAutomata.getSpecificationAutomata());
*/
//				initOnlineSynchronizer(currAutomata);
//plantAutomata.getAutomatonAt(2).setDisabled(true);
				
				ArrayList tree = new ArrayList();
				
//				State[] theStates = makeInitialState();
				//State[] theStates = new State[]{plantAutomata.getAutomatonAt(i).getInitialState(), 
				//								plantAutomata.getAutomatonAt(j).getInitialState()};
//				State[] theStates = new State[currAutomata.size()];				
//				for (int k=0; k<theStates.length; k++)
//					theStates[k] = currAutomata.getAutomatonAt(k).getInitialState();
				
				tree.add(new Node(makeInitialState()));

				while (!tree.isEmpty()) {
					Node currNode = (Node)tree.remove(0);
					tree.addAll(expandNode(currNode, currAutomataIndex));	
		
					schedCounter++;
					currNode.resetCosts();
					Node accNode = scheduleFrom(currNode, currAutomataIndex);

					if (accNode != null)
						twoProdRelax[hashtableIndex].put(currNode, new Integer(accNode.getAccumulatedCost()));
				}
				
				logger.warn("Schedade " + schedCounter + " g�nger");
			}
		}
		
		logger.warn("Klar med preprocess2:andet");
	}
	
	/**
	 * 	R�knar ut en passande index f�r hashtabellen till en tv�robot-relaxering mha en 
	 * 	klurig (och f�rhoppningsvis korrekt) formel.
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
	private int calcEstimatedCost(Node theNode, boolean useOneProdRelax) {
/*		int[] costs = theNode.getCurrentCosts();
		int counter = 0;
		
		for (int i=0; i<costs.length; i++) {
			if (costs[i] >= 0)
				counter++;
		}
*/		
//		if (counter <= 2)
		if (useOneProdRelax)
			return theNode.getAccumulatedCost() + getOneProdRelaxation(theNode);
		else
			return theNode.getAccumulatedCost() + getTwoProdRelaxation(theNode);
	}
	
//	 Det h�r �r fult och borde g�ras om till att likan getTwoProdRelaxation p� n�t s�tt
	private int getOneProdRelaxation(Node theNode) {
		Automata plantAutomata = theAutomata.getPlantAutomata();
		int estimate = 0;
		
		// Den h�r upprepningen �r det fula
//		if (currAutomataIndex == null) {
			for (int i=0; i<plantAutomata.size(); i++) {
				int altEstimate = theNode.getCurrentCosts()[i] + ((Integer)oneProdRelax[i].get(theNode.getState(i))).intValue();
			
				if (altEstimate > estimate)
					estimate = altEstimate;	
			}
//		}
/*		else {
			for (int i=0; i<theNode.size(); i++) {
				int altEstimate = theNode.getCurrentCosts()[i] + ((Integer)oneProdRelax[currAutomataIndex[i]].get(theNode.getState(i))).intValue();
			
				if (altEstimate > estimate)
					estimate = altEstimate;	
			}
		}
*/		
		return estimate;
	}
	
	private int getTwoProdRelaxation(Node theNode) {
		int plantAutomataSize = theAutomata.getPlantAutomata().size();
		int[] currentCosts = theNode.getCurrentCosts();
//		int hashtableCounter = 0;
		
		int estimate = 0;
		for (int i=0; i<plantAutomataSize-1; i++) {
			for (int j=i+1; j<plantAutomataSize; j++) {
//				State[] theStates = new State[]{theNode.getState(i), theNode.getState(j)};
//				int altEstimate = ((Integer)twoProdRelax[hashtableCounter].get(new Node(theStates))).intValue();
				
				// Funkar bara om alla noder i den totala synkningen har 2-prod-relaxerats
				// Har de det?????????????
				int altEstimate = ((Integer)twoProdRelax[calcHashtableIndex(i,j)].get(theNode)).intValue();
				
				if (altEstimate > estimate)
					estimate = altEstimate;	
			}		
		}
		
		int minCurrCost = currentCosts[0];
		for (int i=1; i<currentCosts.length; i++) {
			if (currentCosts[i] < minCurrCost)
				minCurrCost = currentCosts[i];
		}

		return estimate + minCurrCost;
	}
	
	// Borde kanske ligga i "Automaton.java" men det kanske inte �r tillr�ckligt
	// generellt f�r det. Om man inte har att g�ra med schemal�ggning, kan det v�l
	// finnas flera markerade tillst�nd. 
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
	private void insertIntoOpenList(Node node)
	{
		int accCost = node.getAccumulatedCost();
		int cursor = 0;

		while ((cursor < openList.size()) && (accCost > ((Node) openList.get(cursor)).getAccumulatedCost()))
		{
			cursor++;
		}

		openList.add(cursor, node);
	}

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

					// om mindre -> L�nka den st�ngda noden till state. Return true.
					currListNode = node;

					return true;
				}
				else if (isCheaper(currListNode, node))
				{

					// Om st�rre -> returnera false, det ska inte l�ggas till i closedList
					return false;
				}
			}

			++cursor;
		}

		// Utf�rs bara om inget annat returnerat innan
		closedList.add(cursor, node);

		return true;
	}
*/	
	public Automaton buildScheduleAutomaton(Node currNode)
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

	/**
	 *      Returns the Arc between two States.
	 *      @return null if the Arc doesn't exist (this must not happen).
	 */
	// Kan (och b�r) nog tas bort s� sm�ningom (vid st�dning)
	private Arc findCurrentArc(State parent, State child)
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
	
	private LabeledEvent findCurrentEvent(Node fromNode, Node toNode) throws Exception {
		for (int i=0; i<fromNode.size(); i++) {
			if (!fromNode.getState(i).getName().equals(toNode.getState(i).getName())) {
				return theAutomata.getAutomatonAt(i).getLabeledEvent(fromNode.getState(i), toNode.getState(i));
			}
		}
		
		return null;
	}
/*	
	public Integer makeMapKey(Node node)
	{
		int hash = 1;
		
		for (int i = 0; i < node.size(); i++) {
			hash += hash * node.getState(i).getIndex();
			// Varf�r just 10???
			hash *= 10;
		}
		
		hash += hash * 100 * calcEstimatedCost(node);
		
		return new Integer(hash);
	}
*/
	/**
	 *      Checks if a possible accumulatedCost-loss is fully compensated by
	 *      every currentCosts-savings.
	 */
	private boolean isCheaper(Node theNode, Node checkNode)
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
}
