 
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
	private ArrayList closedList;
	
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
		closedList = new ArrayList();
		
		if (theAutomata == null)
			oneProdRelax[0] = new Hashtable();
		else {
			initOnlineSynchronizer();
			
			int nrOfPlants = theAutomata.getPlantAutomata().size();
			oneProdRelax = new Hashtable[nrOfPlants];
			for (int i=0; i<nrOfPlants; i++)
				oneProdRelax[i] = new Hashtable();	
		
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
	public Node walk()
	{
		// Den här if/else-slingan är ful och borde snyggas till (ersättas) en dag. 
		// Körs igång om en synkning har valts som schemaläggningsoffer.
		if (theAutomata == null) {
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
	
			return null;
		}
		// Om istället flera automater skall online-synkas-och-schemaläggas...
		else {
			timer.start();		
			preprocess1();
			logger.info("Time for 1st preprocessing: " + timer.elapsedTime() + " ms");
			
			if (theAutomata.getPlantAutomata().size() > 2) {
				timer.start();
				preprocess2();
				logger.info("Time for 2nd preprocessing: " + timer.elapsedTime() + " ms");
			}
			
			timer.start();
			return scheduleFrom(new Node(makeInitialState()));
		}
	}
	
	private Node scheduleFrom(Node initNode) {
		openList.clear();
		closedList.clear();
		openList.add(initNode);
		
		int counter = 0;
		
		while (!openList.isEmpty()) {
			counter++;
			Node currNode = (Node) openList.remove(0);				
			closedList.add(currNode);
			
			if (currNode.isAccepting()) {
				logger.info(currNode + ". States searched: " + closedList.size() + " in time: " + timer.elapsedTime() + " ms.");
				logger.info("A-stjärnat " + counter + " ggr");
				return currNode;
			}
			
			Iterator childIter = expandNode(currNode).iterator();
			while (childIter.hasNext()) {
				Node childNode = (Node)childIter.next();
				if (!isOnAList(childNode))
					putOnOpenList(childNode);
			}				
		}
		
		logger.error("Inget markerat tillstånd kunde hittas............");
		return null;
	}
	
	private ArrayList expandNode(Node node) {
		ArrayList childNodes = new ArrayList();
		
		int[] currStateIndex = AutomataIndexFormHelper.createState(node.size());
		for (int i=0; i<node.size(); i++) 
			currStateIndex[i] = node.getState(i).getIndex();
		
		int[] currOutgoingEvents = onlineSynchronizer.getOutgoingEvents(currStateIndex);
		for (int i=0; i<currOutgoingEvents.length; i++) {
			if (onlineSynchronizer.isEnabled(currOutgoingEvents[i])) {
				int[] nextState = onlineSynchronizer.doTransition(currStateIndex, currOutgoingEvents[i]);
				State[] theStates = new State[theAutomata.size()];
				
				for (int j=0; j<theAutomata.size(); j++)
					theStates[j] = autoIndexForm.getState(j, nextState[j]);
				
				childNodes.add(new Node(theStates, node));
			}
		}
		
		return childNodes;
	}

	/**
	 * 			Inserts the node into the openList according to the estimatedCost (ascending).
	 * @param 	node
	 */
	private void putOnOpenList(Node node) {
		int estimatedCost = calcEstimatedCost(node);
		int counter = 0;
		Iterator iter = openList.iterator();
		
		while (iter.hasNext()) {
			Node n = (Node)iter.next();
			if (estimatedCost < calcEstimatedCost(n)) {
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
	private boolean isOnAList(Node node) {
		int estimatedCost = calcEstimatedCost(node);
		
		Iterator iter = openList.iterator();
		while (iter.hasNext()) {
			Node n = (Node)iter.next();
			if (node.equals(n)) {
				if (estimatedCost >= calcEstimatedCost(n))
					return true;
				else {
					openList.remove(openList.indexOf(n));
					return false;
				}
			}
		}
		
		iter = closedList.iterator();
		
		while(iter.hasNext()) {
			Node n = (Node)iter.next();
			if (node.equals(n)) {
				if (estimatedCost >= calcEstimatedCost(n))
					return true;
				else {
					closedList.remove(closedList.indexOf(n));
					return false;
				}
			}			
		}
		
		return false;
	}

	/**
	 * @param initialState
	 */
	private void initOnlineSynchronizer() {
		//	Get current options
		SynchronizationOptions syncOptions = new SynchronizationOptions();
		syncOptions.setBuildAutomaton(false);
		syncOptions.setRequireConsistentControllability(false);

		try {
			AutomataSynchronizerHelper helper = new AutomataSynchronizerHelper(theAutomata, syncOptions);
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
		int hashtableCounter = -1;
		Automata plantAutomata = theAutomata.getPlantAutomata();
		for (int i=0; i<plantAutomata.size()-1; i++) {
			for (int j=i+1; j<plantAutomata.size(); j++) {
				hashtableCounter++;
				
				ArrayList tree = new ArrayList();
				State[] theStates = new State[]{plantAutomata.getAutomatonAt(i).getInitialState(), 
												plantAutomata.getAutomatonAt(j).getInitialState()};		
				tree.add(new Node(theStates));
				
				while (!tree.isEmpty()) {
					Node currNode = (Node)tree.remove(0);
					tree.add(expandNode(currNode));
					
					Node accNode = scheduleFrom(currNode);
					twoProdRelax[hashtableCounter].put(currNode, new Integer(accNode.getAccumulatedCost()));					
				}
			}
		}
	}
	
	private int calcEstimatedCost(Node theNode) {
		int[] costs = theNode.getCurrentCosts();
		int counter = 0;
		
		for (int i=0; i<costs.length; i++) {
			if (costs[i] >= 0)
				counter++;
		}
		
		if (counter <= 2) 
			return theNode.getAccumulatedCost() + getOneProdRelaxation(theNode);
		else
			return theNode.getAccumulatedCost() + getTwoProdRelaxation(theNode);
	}
	
	private int getOneProdRelaxation(Node theNode) {
		Automata plantAutomata = theAutomata.getPlantAutomata();
		int estimate = 0;
		
		for (int i=0; i<plantAutomata.size(); i++) {
			int altEstimate = theNode.getCurrentCosts()[i] + ((Integer)oneProdRelax[i].get(theNode.getState(i))).intValue();
		
			if (altEstimate > estimate)
				estimate = altEstimate;	
		}

		return estimate;
	}
	
	private int getTwoProdRelaxation(Node theNode) {
		int plantAutomataSize = theAutomata.getPlantAutomata().size();
		int estimate = 0;
		int hashtableCounter = 0;
		
		for (int i=0; i<plantAutomataSize-1; i++) {
			for (int j=i+1; j<plantAutomataSize; j++) {
				State[] theStates = new State[]{theNode.getState(i), theNode.getState(j)};
				int altEstimate = ((Integer)twoProdRelax[hashtableCounter].get(new Node(theStates))).intValue();
				
				if (altEstimate > estimate)
					estimate = altEstimate;	
			}		
		}

		return estimate;
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
	private boolean insertIntoClosedList(Node node)
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

		State initState = new State(currNode.toString());
		initState.setInitial(true);
		scheduleAuto.addState(initState);

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
	
	public Integer makeMapKey(Node node)
	{
		int hash = 1;
		
		for (int i = 0; i < node.size(); i++) {
			hash += hash * node.getState(i).getIndex();
			hash *= 10;
		}
		
		hash += hash * 100 * calcEstimatedCost(node);
		
		return new Integer(hash);
	}

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
