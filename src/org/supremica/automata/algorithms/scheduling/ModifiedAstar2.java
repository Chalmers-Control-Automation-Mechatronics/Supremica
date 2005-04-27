
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
	private Hashtable[] oneProdRelax;

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
//		openNodes = new TreeMap();

		plantAutomata = theAutomata.getPlantAutomata();

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
		// Den här if/else-slingan är ful och borde snyggas till (ersättas) en dag.
		// Körs igång om en synkning har valts som schemaläggningsoffer.
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
		// Om istället flera automater skall online-synkas-och-schemaläggas...
		else {
			String infoStr = "Processing times:\n";

			timer.start();
			preprocess1();
			infoStr += "\t1st preprocessing in " + timer.elapsedTime() + " ms\n";

			if (theAutomata.getPlantAutomata().size() > 2) {
				timer.start();
				String prep2Info = preprocess2();
				infoStr += "\t2nd preprocessing in " + timer.elapsedTime() + " ms\n";
				infoStr += prep2Info;
			}

//			currAutomataIndex = null;


			timer.start();
			searchCounter = 0;
//			Node currNode = scheduleFrom(new Node(makeInitialState()));






			activeAutomataIndex = new int[plantAutomata.size()];
			for (int i=0; i<activeAutomataIndex.length; i++) {
				activeAutomataIndex[i] = theAutomata.getAutomatonIndex(plantAutomata.getAutomatonAt(i));
			}
			int[] initialNode = makeInitialNode();
			int[] currNode = scheduleFrom(initialNode);

			String s1 = "";
			String s2 = "";
			for (int i=0; i<initialNode.length; i++) {
				s1 += initialNode[i] + " ";
				s2 += currNode[i] + " ";
			}
			logger.warn("initial = " + s1);
			logger.warn("final = " + s2);




			
/*

				infoStr += "\tA*-iterations: " + searchCounter + " in time: " + timer.elapsedTime() + " ms.\n";
				infoStr += "\t\t"+ currNode;
				logger.info(infoStr);

				return currNode;
*/
return null;
//			}
		}
	}

	private int[] scheduleFrom(int[] initNode) {
		int[] finalNode = new int[initNode.length];

		openList.clear();
		closedNodes.clear();
		openList.add(initNode);
		
		while(!openList.isEmpty()) {
			//Skall inte denna ökas på senare i slingan????
			searchCounter++;

			finalNode = (int[]) openList.remove(0);

			expandNode(finalNode);
		}

		return finalNode;
	}

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

	private Collection expandNode(int[] node) {
		int[] currStateIndex = AutomataIndexFormHelper.createState(theAutomata.size());
		for (int i=0; i<currStateIndex.length; i++)
			currStateIndex[i] = node[i];

		int[] currOutgoingEvents = onlineSynchronizer.getOutgoingEvents(currStateIndex);

		for (int i=0; i<currOutgoingEvents.length; i++) {
			if (onlineSynchronizer.isEnabled(currOutgoingEvents[i])) {
				int[] nextStateIndex = onlineSynchronizer.doTransition(currStateIndex, currOutgoingEvents[i]);

				boolean indexChanged = false;
				if (activeAutomataIndex.length < plantAutomata.size()) {
					for (int k=0; k<activeAutomataIndex.length; k++) {
						if (nextStateIndex[activeAutomataIndex[k]] != currStateIndex[activeAutomataIndex[k]])
							indexChanged = true;
					}
				}

				if (indexChanged || activeAutomataIndex.length == plantAutomata.size()) {
				//TODO: Skapa int[] newNode
/*
					State[] theStates = new State[currStateIndex.length-2];
					for (int j=0; j<theStates.length; j++)
						theStates[j] = autoIndexForm.getState(j, nextStateIndex[j]);

					Node newNode = new Node(theStates, node);
					if (!childNodes.containsValue(newNode))
						childNodes.put(newNode, newNode);
*/
				}
			}
		}

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

		logger.warn("curr node = " + node.toStringLight());
		String s = "";
		for (int a=0; a<currStateIndex.length; a++)
			s += currStateIndex[a] + " ";
		logger.info("index = " + s);

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
	// Också lite fult...
	private boolean isOnAList(Node node, boolean useOneProdRelax) {
		int estimatedCost = calcEstimatedCost(node, useOneProdRelax);

//		Collection values = openNodes.values();
//		Iterator iter = values.iterator();
		Iterator iter = openList.iterator();
		while (iter.hasNext()) {
			Object n = iter.next();
			
			if (node.equals(n)) {
				if (estimatedCost >= calcEstimatedCost((Node)n, useOneProdRelax))
					return true;
				else {
					openList.remove(openList.indexOf(n));
//					openNodes.
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

	// Bör kommas bort snart
	private State[] makeInitialState() {
		State[] initialState = new State[theAutomata.size()];

		for (int i=0; i<theAutomata.size(); i++)
			initialState[i] = theAutomata.getAutomatonAt(i).getInitialState();

		return initialState;
	}

	private int[] makeInitialNode() {
		int[] defaultStateIndex = AutomataIndexFormHelper.createState(theAutomata.size());
		int[] initialNode = new int[defaultStateIndex.length + theAutomata.size() + theAutomata.getPlantAutomata().size() + 1];
		Automata plantAutomata = theAutomata.getPlantAutomata();

		for (int i=0; i<theAutomata.size(); i++) 
			initialNode[i] = theAutomata.getAutomatonAt(i).getInitialState().getIndex();
		
		for (int i=theAutomata.size(); i<defaultStateIndex.length; i++)
			initialNode[i] = defaultStateIndex[i];

		for (int i=defaultStateIndex.length; i<(defaultStateIndex.length + theAutomata.size()); i++)
			initialNode[i] = -1;
	
		for (int i=0; i<plantAutomata.size(); i++) 
			initialNode[i + defaultStateIndex.length + theAutomata.size()] = plantAutomata.getAutomatonAt(i).getInitialState().getCost();

		initialNode[initialNode.length-1] = 0;

		return initialNode;
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
	private String preprocess2() {
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

//	 Det här är fult och borde göras om till att likan getTwoProdRelaxation på nåt sätt
	private int getOneProdRelaxation(Node theNode) {
		Automata plantAutomata = theAutomata.getPlantAutomata();
		int estimate = 0;

		// Den här upprepningen är det fula
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
	// Kan (och bör) nog tas bort så småningom (vid städning)
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
			// Varför just 10???
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
