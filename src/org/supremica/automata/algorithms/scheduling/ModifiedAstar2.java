 
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
	
	private TreeMap openMap, closedMap;
	
	
	/** Hashtable containing the estimated cost, having states as keys. **/
	private Hashtable oneProdRelax; 
	
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
		oneProdRelax = new Hashtable();
		openMap = new TreeMap();
		closedMap = new TreeMap();
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
			
			preprocess();
			initOnlineSynchronizer();
			
			logger.info("Time for preprocessing: " + timer.elapsedTime() + " ms");
			timer.start();
			
			while (!openList.isEmpty()) {
				Node currNode = (Node) openList.remove(0);				
				closedList.add(currNode);
				
				if (currNode.isAccepting()) {
					logger.info(currNode + ". States searched: " + closedList.size() + " in time: " + timer.elapsedTime() + " ms.");
					return currNode;
				}
				
				int[] currStateIndex = AutomataIndexFormHelper.createState(theAutomata.size());
				for (int i=0; i<currNode.size(); i++) 
					currStateIndex[i] = currNode.getState(i).getIndex();
				
				int[] currOutgoingEvents = onlineSynchronizer.getOutgoingEvents(currStateIndex);
				for (int i=0; i<currOutgoingEvents.length; i++) {
					if (onlineSynchronizer.isEnabled(currOutgoingEvents[i])) {
						int[] nextState = onlineSynchronizer.doTransition(currStateIndex, currOutgoingEvents[i]);
						State[] theStates = new State[theAutomata.size()];
						
						for (int j=0; j<theAutomata.size(); j++)
							theStates[j] = autoIndexForm.getState(j, nextState[j]);
						
						Node childNode = new Node(theStates, currNode);
						if (!isOnAList(childNode))
							putOnOpenList(childNode);
					}		
				}				
			}

			logger.error("Inget markerat tillstånd kunde hittas............");
			return null;
		}
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
	 * 			Checks if some node is on the closedList.
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

	private int[] makeInitialState() {
		// Build the initial state
		Automaton currAutomaton;
		State currInitialState;
		int[] initialState = AutomataIndexFormHelper.createState(theAutomata.size());

		// + 1 status field
		Iterator autIt = theAutomata.iterator();
		
		while (autIt.hasNext())
		{
			currAutomaton = (Automaton) autIt.next();
			currInitialState = currAutomaton.getInitialState();	
			initialState[currAutomaton.getIndex()] = currInitialState.getIndex();
		}

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
	private boolean preprocess() {	
		int plantCounter = 0;
		
		for (int i=0; i<theAutomata.size(); i++) {
			if (theAutomata.getAutomatonAt(i).getInitialState().getCost() != -1) {
				plantCounter++;
				
				Automaton theAuto = theAutomata.getAutomatonAt(i);
				State markedState = findAcceptingState(theAuto);
				ArrayList estList = new ArrayList();
				
				if (markedState == null)
					return false;
				else {
					Hashtable subHashtable = new Hashtable();
					
					subHashtable.put(markedState, new Integer(0));
					estList.add(markedState);
					
					while (!estList.isEmpty()) {
						ArcIterator incomingArcIterator = ((State)estList.remove(0)).incomingArcsIterator();
						
						while (incomingArcIterator.hasNext()) {
								Arc currArc = incomingArcIterator.nextArc();
								State currState = currArc.getFromState();
								int remainingCost = ((Integer)(subHashtable.get(currArc.getToState()))).intValue();
								
								if (subHashtable.get(currState) == null) {
									subHashtable.put(currState, new Integer(remainingCost + currArc.getToState().getCost()));							
									estList.add(currState);
								}
								else {
									int currRemainingCost = ((Integer)(subHashtable.get(currState))).intValue();
									int newRemainingCost = currArc.getToState().getCost() + remainingCost;
									
									if (newRemainingCost < currRemainingCost) {
										subHashtable.remove(currState);
										subHashtable.put(currState, new Integer(newRemainingCost));
									}
								}
							
						}
					}
				
					oneProdRelax.put(theAuto.getName(),subHashtable);
				}				
			}	
		}
		
		State[] theStates = new State[theAutomata.size()];
		
		for (int i=0; i<theStates.length; i++) 
			theStates[i] = theAutomata.getAutomatonAt(i).getInitialState(); 
		
		Node initialNode = new Node(theStates);
		openList.add(initialNode);
		//openMap.put(makeMapKey(initialNode), initialNode);
		
		return true;
	}
	
	private int calcEstimatedCost(Node theNode) {
		return theNode.getAccumulatedCost() + getOneProdRelaxation(theNode);
	}
	
	private int getOneProdRelaxation(Node theNode) {
		Automata plantAutomata = theAutomata.getPlantAutomata();
		int estimate = 0;
		
		//fult...... Asfult............
		for (int i=0; i<plantAutomata.size(); i++) {
			int altEstimate = theNode.getCurrentCosts()[i] + ((Integer)((Hashtable) oneProdRelax.get(plantAutomata.getAutomatonAt(i).getName())).get(theNode.getState(i))).intValue();
			if (altEstimate > estimate)
				estimate = altEstimate;	
		}

		return estimate;
	}
	
	// Borde kanske ligga i "Automaton.java" men det kanske inte är tillräckligt
	// generellt för det. Om man inte har att göra med schemaläggning, kan det väl
	// finnas flera markerade tillstånd. 
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
