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
	public State walk() {
		timer.start();
		int counter = 0;
		openList.add(((State) theAutomaton.getInitialState()).copy());
		
		while (!openList.isEmpty()) {
			State currState = (State) openList.remove(0);
			counter++;
			
			// Inserts only if a cheaper instance of currState has not been examined earlier
			if (insertIntoClosedList(currState)) {			
				if (currState.isAccepting()) {
					timer.stop();
					logger.info("Nr of searched states = " + counter);
									
					return currState;
				}
					
				
				StateIterator states = currState.nextStateIterator();
	
				while (states.hasNext()) {
					// Här ska man stoppa in dem ordnade i openList om de 
					// (eller dess likar) inte redan har gåtts igenom. 
					State currChildState = ((State) states.nextState()).copy();
					currChildState.setParent(currState);
					currChildState.updateCosts(currState);
		
					insertIntoOpenList(currChildState);
				}	
			}		
		}
		
		return null;
	}
	
	/**
	 *	Inserts a state into openList in descending accumulatedTime order. 
	 */
	private void insertIntoOpenList(State state) {
		int accCost = state.getAccumulatedCost();
		int cursor = 0;
		
		while ((cursor < openList.size()) && (accCost > ((State) openList.get(cursor)).getAccumulatedCost())) {
			cursor++ ;
		}

		openList.add(cursor, state);
	}
	
	/**
	 *	Inserts the state into the closedList unless  an instance
	 *	of this state with a cheaper cost has already been examined and placed
	 *	into closedList. 
	 *
	 *	@return true if the insertion operation has been performed. 
	 */
	private boolean insertIntoClosedList(State state) {
		String key = state.getId();
		int cursor = 0;
		
		while ((cursor < closedList.size()) && (key.compareTo(((State) closedList.get(cursor)).getId()) > -1)) {
			State currListState =  (State) closedList.get(cursor);
			
			if (key.compareTo(currListState.getId()) == 0) { 
				if (isCheaper(state, currListState)) {
					// om mindre -> Länka den stängda noden till state. Return true.
					currListState = state;
					return true;
				}
				else if (isCheaper(currListState, state)) {
					// Om större -> returnera false, det ska inte läggas till i closedList
					return false;
				}
			}	
			
			++cursor;
		}
		
		// Utförs bara om inget annat returnerat innan
		closedList.add(cursor, state);
		
		return true;
	}
	
	public Automaton buildScheduleAutomaton(State currState) {
	//	ArrayList path = new ArrayList();
		Automaton scheduleAuto = new Automaton();
		scheduleAuto.setComment("Schedule");
				
		logger.info("optimal cost = " + currState.getAccumulatedCost() + "; search time = " + timer.elapsedTime() + " milliseconds");

		while (currState.getParent() != null) {
			try {
				Arc currArc = findCurrentArc(currState, currState.getParent());
				
				scheduleAuto.addState(currState);
				scheduleAuto.getAlphabet().addEvent(currArc.getEvent(), false);
				scheduleAuto.addArc(new Arc(currState.getParent(), currState, currArc.getEvent()));
				
				currState = currState.getParent();
			}
			catch (NullPointerException ex){
				logger.error("ModifiedAstar2::buildScheduleAutomaton() --> Could not find the arc between " + currState.getName() + " and " + currState.getParent().getName());
				logger.debug(ex.getStackTrace());				
			}
			
		}
		scheduleAuto.addState(currState);
		
		return scheduleAuto;
	}

	/**
	 *	Returns the Arc between two States. 
	 *	@return null if the Arc doesn't exist (this must not happen). 
	 */
	private Arc findCurrentArc(State child, State parent) {
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
	private boolean isCheaper(State theState, State checkState) {
		if (! (theState instanceof CompositeState)) {
			return (theState.getAccumulatedCost() < checkState.getAccumulatedCost());
		}
		else {
			int accCost1 = theState.getAccumulatedCost();
			int accCost2 = checkState.getAccumulatedCost();
			int[] currCosts1 = ((CompositeState) theState).getCurrentCosts();
			int[] currCosts2 = ((CompositeState) checkState).getCurrentCosts();
			
			for (int i=0; i<currCosts1.length; i++) {
				if ((accCost1 + currCosts1[i]) > (accCost2 + currCosts2[i]))
					return false;
			}
			
			return true;
		}
	}
}
