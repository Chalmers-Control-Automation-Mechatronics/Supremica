/******************** ModifiedAstar2.java **************************
 * AKs implementation of Tobbes modified Astar search algo
 * Basically this is a guided tree-search algorithm, like
 *
 *	list processed = 0;				// closed
 *	list waiting = initial_state; 	// open
 *
 *	while still waiting
 *	{
 *		choose an element from waiting	// the choice is guided by heuristics
 *		generate successors of this element
 *		if a successor is not already waiting or processed
 *			put it on waiting
 *		place the element on processed, remove it from waiting
 *	}
 */

package org.supremica.automata.algorithms.scheduling;

import java.io.*;
import java.util.*;

import org.supremica.log.*;
import org.supremica.automata.*;
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
	
	/** The automaton to be scheduled. */ //Den kanske man inte behöver spara... AK
	private Automaton theAutomaton;

	public ModifiedAstar2(Automaton theAutomaton) {
		this.theAutomaton = theAutomaton;
		
		init();
	}
	
	private void init() {
		timer = new ActionTimer();	
		openList = new ArrayList();
		closedList = new ArrayList();
	}
	
	/**
	 *	Walks through the tree of possible paths in search for the optimal one.  
	 */
	public Node walk() {
		timer.start();
		int counter = 0;

		openList.add(new Node(((State)theAutomaton.getInitialState())));
		
		
		while (!openList.isEmpty()) {
			Node currNode = (Node) openList.remove(0);
			counter++;
		
			if (insertIntoClosedList(currNode)) {			
				if (currNode.isAccepting()) {
					timer.stop();
					logger.info("Nr of searched states = " + counter);
									
					return currNode;
				}
				
				StateIterator states = currNode.getCorrespondingState().nextStateIterator();
	
				while (states.hasNext()) {
					Node currChildNode = new Node((State) states.nextState(), currNode);
					
					insertIntoOpenList(currChildNode);
				}	
			}		
		}
		
		return null;
	}
	
	/**
	 *	Inserts a state into openList in descending accumulatedTime order. 
	 */
	private void insertIntoOpenList(Node node) {
		int accCost = node.getAccumulatedCost();
		int cursor = 0;
		
		while ((cursor < openList.size()) && (accCost > ((Node) openList.get(cursor)).getAccumulatedCost())) {
			cursor++ ;
		}

		openList.add(cursor, node);
	}
	
	/**
	 *	Inserts the state into the closedList unless  an instance
	 *	of this state with a cheaper cost has already been examined and placed
	 *	into closedList. 
	 *
	 *	@return true if the insertion operation has been performed. 
	 */
	private boolean insertIntoClosedList(Node node) {
		String key = node.getId();
		int cursor = 0;
		
		while ((cursor < closedList.size()) && (key.compareTo(((Node) closedList.get(cursor)).getId()) > -1)) {
			Node currListNode =  (Node) closedList.get(cursor);
			
			if (key.compareTo(currListNode.getId()) == 0) { 
				if (isCheaper(node, currListNode)) {
					// om mindre -> Länka den stängda noden till state. Return true.
					currListNode = node;
					return true;
				}
				else if (isCheaper(currListNode, node)) {
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
	
	public Automaton buildScheduleAutomaton(Node currNode) {
		Automaton scheduleAuto = new Automaton();
		scheduleAuto.setComment("Schedule");

		logger.info("optimal cost = " + currNode.getAccumulatedCost() + "; search time = " + timer.elapsedTime() + " milliseconds");

		State nextState = new State(currNode.getCorrespondingState());
		scheduleAuto.addState(nextState);
				
		while (currNode.getParent() != null) {
			try {						
				State currState = currNode.getParent().getCorrespondingState();
				
				Arc currArc = findCurrentArc(currState, nextState);
				
				currState = new State(currState);
				
				scheduleAuto.addState(currState);
				scheduleAuto.getAlphabet().addEvent(currArc.getEvent(), false);
				scheduleAuto.addArc(new Arc(currState, nextState, currArc.getEvent()));
				
				currNode = currNode.getParent();
				nextState = currState;
			}
			catch (NullPointerException ex){
				logger.error("ModifiedAstar2::buildScheduleAutomaton() --> Could not find the arc between " + currNode.getId() + " and " + currNode.getParent().getId());
				logger.debug(ex.getStackTrace());				
			}
			
		}

		scheduleAuto.addState(currNode.getCorrespondingState());

		return scheduleAuto;
	}
	
	/**
	 *	Returns the Arc between two States. 
	 *	@return null if the Arc doesn't exist (this must not happen). 
	 */
	private Arc findCurrentArc(State parent, State child) {
		ArcIterator it = parent.outgoingArcsIterator();
		
		while (it.hasNext()) {
			Arc currArc = it.nextArc();
			
			if (currArc.getToState().getId().equals(child.getId()))
				return currArc;
		}
		
		return null;
	}
	
	/**
	 *	Checks if a possible accumulatedCost-loss is fully compensated by 
	 *	every currentCosts-savings. 
	 */
	private boolean isCheaper(Node theNode, Node checkNode) {
		if (theNode.getCurrentCosts() == null) {
			return (theNode.getAccumulatedCost() < checkNode.getAccumulatedCost());
		}
		else {
			int accCost1 = theNode.getAccumulatedCost();
			int accCost2 = checkNode.getAccumulatedCost();
			int[] currCosts1 = theNode.getCurrentCosts();
			int[] currCosts2 = checkNode.getCurrentCosts();
			
			for (int i=0; i<currCosts1.length; i++) {
				if ((accCost1 + currCosts1[i]) > (accCost2 + currCosts2[i]))
					return false;
			}
			
			return true;
		}
	}
}
