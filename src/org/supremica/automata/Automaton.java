
/*
 *  Supremica Software License Agreement
 *
 *  The Supremica software is not in the public domain
 *  However, it is freely available without fee for education,
 *  research, and non-profit purposes.  By obtaining copies of
 *  this and other files that comprise the Supremica software,
 *  you, the Licensee, agree to abide by the following
 *  conditions and understandings with respect to the
 *  copyrighted software:
 *
 *  The software is copyrighted in the name of Supremica,
 *  and ownership of the software remains with Supremica.
 *
 *  Permission to use, copy, and modify this software and its
 *  documentation for education, research, and non-profit
 *  purposes is hereby granted to Licensee, provided that the
 *  copyright notice, the original author's names and unit
 *  identification, and this permission notice appear on all
 *  such copies, and that no charge be made for such copies.
 *  Any entity desiring permission to incorporate this software
 *  into commercial products or to use it for commercial
 *  purposes should contact:
 *
 *  Knut Akesson (KA), knut@supremica.org
 *  Supremica,
 *  Haradsgatan 26A
 *  431 42 Molndal
 *  SWEDEN
 *
 *  to discuss license terms. No cost evaluation licenses are
 *  available.
 *
 *  Licensee may not use the name, logo, or any other symbol
 *  of Supremica nor the names of any of its employees nor
 *  any adaptation thereof in advertising or publicity
 *  pertaining to the software without specific prior written
 *  approval of the Supremica.
 *
 *  SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 *  SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 *  IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 *  Supremica or KA shall not be liable for any damages
 *  suffered by Licensee from the use of this software.
 *
 *  Supremica is owned and represented by KA.
 */
package org.supremica.automata;

import java.util.*;
import org.supremica.log.*;

public class Automaton
	implements ArcListener
{
	private static Logger logger = LoggerFactory.createLogger(Automaton.class);

	private Alphabet alphabet;
	private String name;
	private String comment;
	private List theStates;
	private int index = -1;
	private Map idStateMap;	// Want fast lookup on both id and index (but not name?)
	private Map indexStateMap;
	private ArcSet theArcs;
	private State initialState;
	private boolean isDisabled = false;
	private AutomatonType type = AutomatonType.Undefined;
	private int uniqueStateIndex = 0;
	
	// Graphical stuff
	private boolean hasLayout = false;
	private int width = -1;
	private int height = -1;

	// master and slave automata are only valid if this automaton is an interface
	// Shouldn't then Interface inherit from Automata?
	private Automata masterAutomata = null;
	private Automata slaveAutomata = null;

	private AutomatonListeners listeners = null;

	public Automaton()
	{
		alphabet = new Alphabet();
		idStateMap = new HashMap();
		indexStateMap = new HashMap();
		theStates = new LinkedList();
		theArcs = new ArcSet();
		masterAutomata = new Automata();
		slaveAutomata = new Automata();
	}

	public Automaton(String name)
	{
		this();

		setName(name);
	}

	public Automaton(Automaton orgAut)
	{
		this();

		Alphabet orgAlphabet = orgAut.getAlphabet();
		Alphabet newAlphabet = new Alphabet(orgAlphabet);

		type = orgAut.type;

		setName(orgAut.getName());
		setAlphabet(newAlphabet);

		// Create all states
		Iterator states = orgAut.stateIterator();

		while (states.hasNext())
		{
			State orgState = (State) states.next();
			State newState = new State(orgState);

			addState(newState);
		}

		try
		{

			// Create all transitions
			states = orgAut.stateIterator();

			while (states.hasNext())
			{
				State orgSourceState = (State) states.next();
				State newSourceState = getStateWithId(orgSourceState.getId());
				Iterator outgoingArcs = orgSourceState.outgoingArcsIterator();

				while (outgoingArcs.hasNext())
				{
					Arc orgArc = (Arc) outgoingArcs.next();
					State orgDestState = orgArc.getToState();
					LabeledEvent currEvent = orgAlphabet.getEventWithId(orgArc.getEventId());
					State newDestState = getStateWithId(orgDestState.getId());
					Arc newArc = new Arc(newSourceState, newDestState, currEvent.getId());

					addArc(newArc);
				}
			}
		}
		catch (Exception e)
		{
			System.err.println("Error while copying transitions");
			System.exit(0);
		}
	}

	public void setType(AutomatonType type)
	{
		this.type = type;
	}

	public AutomatonType getType()
	{
		return type;
	}

	public boolean isSupervisor()
	{
		return type == AutomatonType.Supervisor;
	}

	public boolean isSpecification()
	{
		return type == AutomatonType.Specification;
	}

	public boolean isPlant()
	{
		return type == AutomatonType.Plant;
	}

	public boolean isInterface()
	{
		return type == AutomatonType.Interface;
	}

	public void setName(String name)
	{
		String oldName = this.name;

		this.name = name;

		notifyListeners(AutomatonListeners.MODE_AUTOMATON_RENAMED, oldName);
	}

	public String getName()
	{
		return name;
	}
	public String getComment()
	{
		return comment;
	}
	public void setComment(String comment)
	{
		this.comment = comment;
	}
	public void setDisabled(boolean isDisabled)
	{
		this.isDisabled = isDisabled;
	}

	public boolean isDisabled()
	{
		return isDisabled;
	}

	public void addState(State state)
	{
		theStates.add(state);
		if (state.getIndex() == -1)
		{
			state.setIndex(getUniqueStateIndex());
		}
		idStateMap.put(state.getId(), state);
		indexStateMap.put(new Integer(state.getIndex()), state);

		if (state.isInitial())
		{
			this.initialState = state;
		}

		notifyListeners(AutomatonListeners.MODE_STATE_ADDED, state);
	}

	// If a state with this id (and/or name?) already exists, return the existing state
	// Else, add this state and return it
	public State addStateChecked(State state)
	{
		State existing = (State)idStateMap.get(state.getId());
		if(existing != null)
		{
			return existing;
		}
		// else, add it as usual
		addState(state);
		return state;
	}
	
	public void removeState(State state)
	{
		if (state == initialState)
		{
			initialState = null;
		}

		theStates.remove(state);
		state.removeArcs();
		idStateMap.remove(state.getId());
		indexStateMap.remove(new Integer(state.getIndex()));
		notifyListeners(AutomatonListeners.MODE_STATE_REMOVED, state);
	}

	public boolean hasInitialState()
	{
		return initialState != null;
	}

	public State getInitialState()
	{
		return initialState;
	}
	
	// This is a fixx, for now - see bug report
	public void setInitialState(State state)
	{
		State oldinit = getInitialState();
		
		State newinit = getState(state);
		if(newinit == null)
		{
			throw new RuntimeException("No such state. id = " + state.getId());
		}
		
		newinit.setInitial(true);
		initialState = newinit;
		
		if(oldinit != null)
		{
			oldinit.setInitial(false);
		}
	}
	
	/**
	 * Returns a uniquely named (and id'ed) state.
	 * Passing null or empty prefix sets prefix to 'q'
	 * The new state is not added to the state set
	 */
	public State createUniqueState(String prefix)
	{
		StringBuffer name = null;
		if(prefix == null || prefix.equals(""))
		{
			name = new StringBuffer("q" + uniqueStateIndex++);
		}
		else
		{
			name = new StringBuffer(prefix);
		}

		while(containsStateWithId(name.toString()))
		{
			name.append(uniqueStateIndex++);
		}
		return new State(name.toString());
	}
	
	/**
	 * Returns a uniquely named (and id'ed) state.
	 * Add it to the state set
	 * Passing null or empty prefix sets prefix to 'q'
	 */
	public State createAndAddUniqueState(String prefix)
	{
		State newstate = createUniqueState(prefix);
		addState(newstate);
		return newstate;
	}
	/**
	 * Returns true if it finds one accepting state, else returns false
	 * Iterates over all states _only_if_ no accepting states exist (or only
	 * the last one is accepting)
	 */
	public boolean hasAcceptingState()
	{
		Iterator stateIt = stateIterator();

		while (stateIt.hasNext())
		{
			State currState = (State) stateIt.next();

			if (currState.isAccepting())
			{
				return true;
			}
		}
		return false;
	}

	public boolean hasSelfLoop()
	{
		for(Iterator arcIt = theArcs.iterator(); arcIt.hasNext();)
		{
			Arc currArc = (Arc)arcIt.next();
			if (currArc.isSelfLoop())
			{
				return false;
			}
		}
		return false;
	}

	public boolean isDeterministic()
	{
		HashSet foundEvents = new HashSet();

		for (Iterator stateIt = stateIterator(); stateIt.hasNext(); )
		{
			State currState = (State)stateIt.next();
			foundEvents.clear();
			for (Iterator evIt = outgoingEventsIterator(currState); evIt.hasNext(); )
			{
				LabeledEvent currEvent = (LabeledEvent)evIt.next();
				boolean newElement = foundEvents.add(currEvent.getLabel());
				if (!newElement)
				{
					return false;
				}
			}
		}

		return true;
	}

	public boolean isAllEventsPrioritized()
	{
		return alphabet.isAllEventsPrioritized();
	}

	/**
	 * When adding an arc, both the two states associated with the
	 * arc _must_ already be contained in the automaton, otherwise the
	 * behavior is undefined.
	 */
	public void addArc(Arc arc)
	{
		arc.getListeners().addListener(this);
		theArcs.addArc(arc);
		notifyListeners(AutomatonListeners.MODE_ARC_ADDED, arc);
	}

	public void removeArc(Arc arc)
	{
		theArcs.removeArc(arc);
		arc.clear();
		notifyListeners(AutomatonListeners.MODE_ARC_REMOVED, arc);
	}

	public boolean containsState(State state)
	{
		return idStateMap.containsKey(state.getId());
	}

	public State getState(State state)
	{
		return (State) idStateMap.get(state.getId());
	}

	public boolean containsStateWithId(String id)
	{
		return idStateMap.containsKey(id);
	}

	public State getStateWithId(String id)
	{
		return (State) idStateMap.get(id);
	}

	/**
	 * This is an ugly method that only are needed when dealing
	 * with automataIndexForm. All methods that works with index
	 * needs special initialisation that is not automatically done.
	 * This method is not recommended for general use.
	 */
	public boolean containsStateWithIndex(int index)
	{
		return indexStateMap.containsKey(new Integer(index));
	}

	/**
	 * This is an ugly method that only are needed when dealing
	 * with automataIndexForm. All methods that works with index
	 * needs special initialisation that is not automatically done.
	 * This method is not recommended for general use.
	 */
	public State getStateWithIndex(int index)
	{
		return (State) indexStateMap.get(new Integer(index));
	}

	public String getStateNameWithIndex(int index)
	{
		return (((State) (indexStateMap.get(new Integer(index)))).getName());
	}

	public LabeledEvent getEvent(String eventId)
		throws Exception
	{
		return alphabet.getEventWithId(eventId);
	}

	public LabeledEvent getEvent(Arc theArc)
		throws Exception
	{
		return getEvent(theArc.getEventId());
	}

	public String getLabel(Arc theArc)
		throws Exception
	{
		LabeledEvent theEvent = getEvent(theArc);
		return theEvent.getLabel();
	}

	/**
	 * Use isInAlphabet instead
	 * @deprecated
	 */
	public boolean containsEventWithLabel(String eventLabel)
	{
		return hasEventInAlphabet(eventLabel);
	}

	public boolean hasEventInAlphabet(String eventLabel)
	{
		return alphabet.containsEventWithLabel(eventLabel);
	}

	/**
	 * Returns an iterator to all states in this automaton
	 * that has an event with eventLabel as an outoing event.
	 */
	public Iterator statesThatEnableEventIterator(String eventLabel)
	{
		StateIterator stateIt = new StateIterator(eventLabel, true);
		return stateIt;
	}

	/**
	 * Returns true if the event with label eventLabel is prioritized in this
	 * automaton. If the event is not included in this automaton or is not
	 * prioritized then it returns false.
	 */
	public boolean isEventPrioritized(String eventLabel)
	{
		if (!containsEventWithLabel(eventLabel))
		{
			return false;
		}
		LabeledEvent thisEvent = null;
		try
		{
			thisEvent = getEventWithLabel(eventLabel);
		}
		catch (Exception ex)
		{
			logger.error("Automaton.isEventPrioritzed: Error in getEventWithLabel");
			return false;
		}
		return thisEvent.isPrioritized();
	}

	public LabeledEvent getEventWithLabel(String eventLabel)
		throws Exception
	{
		return alphabet.getEventWithLabel(eventLabel);
	}


	public int nbrOfStates()
	{
		return idStateMap.size();
	}

	public int nbrOfEvents()
	{
		return alphabet.size();
	}

	public int nbrOfTransitions()
	{
		return theArcs.size();
	}

	public int nbrOfAcceptingStates()
	{
		int nbrOfAcceptingStates = 0;
		Iterator stateIt = stateIterator();

		while (stateIt.hasNext())
		{
			State currState = (State) stateIt.next();

			if (currState.isAccepting())
			{
				nbrOfAcceptingStates++;
			}
		}

		return nbrOfAcceptingStates;
	}

	public int nbrOfForbiddenStates()
	{
		int nbrOfForbiddenStates = 0;
		Iterator stateIt = stateIterator();

		while (stateIt.hasNext())
		{
			State currState = (State) stateIt.next();

			if (currState.isForbidden())
			{
				nbrOfForbiddenStates++;
			}
		}

		return nbrOfForbiddenStates;
	}

	public int nbrOfAcceptingAndForbiddenStates()
	{
		int nbrOfAcceptingAndForbiddenStates = 0;
		Iterator stateIt = stateIterator();

		while (stateIt.hasNext())
		{
			State currState = (State) stateIt.next();

			if (currState.isAccepting() && currState.isForbidden())
			{
				nbrOfAcceptingAndForbiddenStates++;
			}
		}

		return nbrOfAcceptingAndForbiddenStates;
	}

	public Iterator stateIterator()	// Should be 'getStateIterator'?
	{
		return theStates.iterator();
	}

	/**
	 * Use this iterator instead of stateIterator when you will add or
	 * remove states in this automaton.
	 */
	public Iterator safeStateIterator()
	{
		return (new LinkedList(theStates)).iterator();
	}

	public Iterator arcIterator()
	{
		return theArcs.iterator();
	}

	public Iterator safeArcIterator()
	{
		return (new ArcSet(theArcs)).iterator();
	}

	public Iterator outgoingEventsIterator(State theState)
	{
		Iterator arcIt = theState.outgoingArcsIterator();
		return new EventIterator(arcIt);
	}

	public Iterator incomingEventsIterator(State theState)
	{
		Iterator arcIt = theState.incomingArcsIterator();
		return new EventIterator(arcIt);
	}

	/**
	 * These are only valid if the automatonType is interface.
	 */
	public Automata getMasterAutomata()
	{
		return masterAutomata;
	}

	public Automata getSlaveAutomata()
	{
		return slaveAutomata;
	}

	public void purgeInterfaceAutomata(Automata validAutomata)
	{
		purgeInterfaceAutomata(masterAutomata, validAutomata);
		purgeInterfaceAutomata(slaveAutomata, validAutomata);
	}

	private void purgeInterfaceAutomata(Automata currDependencies, Automata validAutomata)
	{
		List toBeRemoved = new ArrayList();

		for (Iterator autIt = currDependencies.iterator(); autIt.hasNext(); )
		{
			Automaton currAutomaton = (Automaton)autIt.next();
			if (!validAutomata.containsAutomaton(currAutomaton))
			{
				toBeRemoved.add(currAutomaton);
			}
		}

		for (Iterator autIt = toBeRemoved.iterator(); autIt.hasNext(); )
		{
			Automaton currAutomaton = (Automaton)autIt.next();
			currDependencies.removeAutomaton(currAutomaton);
		}
	}

	/**
	 *@return  Description of the Return Value
	 */
	public Iterator groupedArcIterator()
	{
		return null;
	}

	public Iterator eventIterator()
	{
		return alphabet.iterator();
	}

	public Collection eventCollection()
	{
		return alphabet.values();
	}

	public Alphabet getAlphabet()
	{
		return alphabet;
	}

	public void setAlphabet(Alphabet alphabet)
	{
		this.alphabet = alphabet;
	}

	public void setIndex(int index)
	{
		this.index = index;
	}

	public int getIndex()
	{
		return index;
	}

	public int getSynchIndex()
	{
		return getIndex();
	}

	// Don't do this in public
	private String getUniqueStateId()
	{
		String newId;

		do
		{
			newId = "q" + uniqueStateIndex++;
		}
		while (containsStateWithId(newId));

		return newId;
	}

	// Don't do this in public
	private int getUniqueStateIndex()
	{
		while (containsStateWithIndex(uniqueStateIndex))
		{
			uniqueStateIndex++;
		}
		uniqueStateIndex++;
		return uniqueStateIndex - 1;
	}

	public void clearVisitedStates()
	{
		Iterator stateIt = stateIterator();

		while (stateIt.hasNext())
		{
			State currState = (State) stateIt.next();

			currState.setVisited(false);
		}
	}

	private void removeAssociatedStateFromUnvisitedStates()
	{
		Iterator stateIt = stateIterator();

		while (stateIt.hasNext())
		{
			State currState = (State) stateIt.next();
			if (!currState.isVisited())
			{
				currState.setAssociatedState(null);
			}
		}
	}

	/**
	 * Returns the shortest trace from the initial state to toState.
	 */
	public LabelTrace getTrace(State toState)
		throws Exception
	{
		return getTrace(getInitialState(), toState);
	}

	/**
	 * Returns the shortest trace from fromState to toState,
	 */
	public LabelTrace getTrace(State fromState, State toState)
		throws Exception
	{
		if (fromState == null)
		{
			throw new Exception("Automaton.getTrace: fromState is null");
		}
		if (toState == null)
		{
			throw new Exception("Automaton.getTrace: toState is null");
		}

		computeShortestPath(fromState);

		LabelTrace theTrace = new LabelTrace();

		// Remove the associated state from all states that
		// is not on the way between fromState and toState.
		State thisState = toState;
		thisState.setVisited(true);
		State prevState = thisState.getAssociatedState();
		while (prevState != null)
		{
			LabeledEvent currEvent = getLabeledEvent(prevState, thisState);
			if (currEvent == null)
			{
				throw new Exception("Could not find an arc from " + prevState.getName() + " to " + thisState.getName());
			}
			theTrace.addFirst(currEvent.getLabel());
			thisState = prevState;
			prevState = prevState.getAssociatedState();
		}

		reverseAssociatedState(toState);

		return theTrace;
	}

	private void computeShortestPath(State fromState)
		throws Exception
	{
		if (fromState == null)
		{
			throw new Exception("Automaton.getTrace: fromState is null");
		}

		clearVisitedStates();

		// This implements a breath first search
		LinkedList openStates = new LinkedList();
		fromState.setAssociatedState(null);
		openStates.addLast(fromState);
		fromState.setVisited(true);
		while (openStates.size() > 0)
		{
			State currState = (State)openStates.removeFirst();
			for (Iterator arcIt = currState.outgoingArcsIterator(); arcIt.hasNext();)
			{
				Arc currArc = (Arc)arcIt.next();
				State currToState = currArc.getToState();
				if (!currToState.isVisited())
				{
					currToState.setAssociatedState(currState);
					currToState.setVisited(true);
					openStates.addLast(currToState);
				}
			}
		}
	}

	/**
	 * In computeShortestPath the assciatedStates are backwards. I.e. they
	 * point from the to states towards the initial state. After the computation
	 * we want the arcs in the opposite direction.
	 */
	private void reverseAssociatedState(State toState)
	{
		clearVisitedStates();
		reverseAssociatedState(toState, null);
		removeAssociatedStateFromUnvisitedStates();
	}

	private void reverseAssociatedState(State currState, State nextState)
	{
		if (currState != null)
		{
			currState.setVisited(true);
		}
		if (currState.getAssociatedState() == null)
		{
			currState.setAssociatedState(nextState);
			return;
		}
		reverseAssociatedState(currState.getAssociatedState(), currState);
		currState.setAssociatedState(nextState);
	}

	/**
	 * Returns a event on a arc that starts in fromState and ens in toState.
	 * If no such event exists, then null is returned.
	 */
	public LabeledEvent getLabeledEvent(State fromState, State toState)
		throws Exception
	{
		for (Iterator arcIt = fromState.outgoingArcsIterator(); arcIt.hasNext();)
		{
			Arc currArc = (Arc)arcIt.next();
			State currToState = currArc.getToState();
			if (currToState == toState)
			{
				return getEvent(currArc.getEventId());
			}
		}
		return null;
	}

	public void removeAllStates()
	{
		beginTransaction();
		idStateMap.clear();
		indexStateMap.clear();
		theStates.clear();

		initialState = null;

		if (listeners != null)
		{
			listeners.setUpdateNeeded(true);
		}

		endTransaction();
	}

	public State getState(int x, int y)
	{
		Iterator stateIt = stateIterator();

		while (stateIt.hasNext())
		{
			State currState = (State) stateIt.next();

			if (currState.contains(x, y))
			{
				return currState;
			}
		}

		return null;
	}

	public boolean hasLayout()
	{
		return hasLayout;
	}

	public void setHasLayout(boolean hasLayout)
	{
		this.hasLayout = hasLayout;
	}

	public int getWidth()
	{
		return width;
	}

	public void setWidth(int width)
	{
		this.width = width;
	}


	public int getHeight()
	{
		return height;
	}

	public void setHeight(int height)
	{
		this.height = height;
	}

	public boolean validLayout()
	{
		if (width < 0 || height < 0)
		{
			return false;
		}
		for (Iterator stateIt = stateIterator(); stateIt.hasNext(); )
		{
			State currState = (State)stateIt.next();
			if (!currState.validLayout())
			{
				return false;
			}
		}
		return true;
	}

	public long checksum()
	{

		// Ad-hoc checksum algorithm
		long checksum = 0;

		for (Iterator sIt = stateIterator(); sIt.hasNext(); )
		{
			State currState = (State) sIt.next();
			int part1 = 1;
			int part2 = 2;
			String id = currState.getId();

			if (id != null)
			{
				part1 = id.hashCode();
			}

			String name = currState.getName();

			if (name != null)
			{
				part2 = name.hashCode();
			}

			int part3 = currState.nbrOfIncomingArcs();
			int part4 = currState.nbrOfOutgoingArcs();

			checksum = part1 + part2 + part3 + part4;
		}

		int part5 = nbrOfStates();
		int part6 = nbrOfEvents();
		int part7 = nbrOfTransitions();
		int part8 = 1;

		if (name != null)
		{
			part8 = name.hashCode();
		}

		if (part5 > 0)
		{
			checksum = checksum * part5;
		}

		if (part6 > 0)
		{
			checksum = checksum * part6;
		}

		if (part7 > 0)
		{
			checksum = checksum * part7;
		}

		if (part8 > 0)
		{
			checksum = checksum * part8;
		}

		return checksum;
	}

	public Listeners getListeners()
	{
		if (listeners == null)
		{
			listeners = new AutomatonListeners(this);
		}

		return listeners;
	}

	public void addListener(AutomatonListener listener)
	{
		Listeners currListeners = getListeners();

		currListeners.addListener(listener);
	}

	private void notifyListeners(int mode, Object o)
	{
		if (listeners != null)
		{
			listeners.notifyListeners(mode, o);
		}
	}

	private void notifyListeners()
	{
		if (listeners != null)
		{
			listeners.notifyListeners();
		}
	}

	public void invalidate()
	{
		if (listeners != null)
		{
			listeners.notifyListeners();
		}
	}

	public void beginTransaction()
	{
		if (listeners != null)
		{
			listeners.beginTransaction();
		}
	}

	public void endTransaction()
	{
		if (listeners != null)
		{
			listeners.endTransaction();
		}
	}

	public void updated(Object o) {}

	public void arcAdded(Arc arc) {}

	public void arcRemoved(Arc arc)
	{
		theArcs.removeArc(arc);
	}

	public int hashCode()
	{
		return name.hashCode();
	}

	public static void main(String[] args)
	{
		Automaton theAutomaton = new Automaton();

		State stateZero = new State("zero");
		theAutomaton.addState(stateZero);

		State st = theAutomaton.getStateWithIndex(0); // should be stateZero (not?)
		if(st == null)
				System.err.println("st == null");
		else
				System.err.println("st != null");
	}

	class EventIterator
		implements Iterator
	{
		private Iterator arcIt;

		public EventIterator(Iterator arcIt)
		{
			this.arcIt = arcIt;
		}

		public boolean hasNext()
		{
			return arcIt.hasNext();
		}

		public Object next()
		{
			Arc nextArc = (Arc)arcIt.next();
			String eventId = nextArc.getEventId();
			LabeledEvent nextEvent = null;
			try
			{
				nextEvent = getEvent(eventId);
			}
			catch (Exception ex)
			{
				logger.error("Automaton::EventIterator.next: Error in getEvent");
			}
			return nextEvent;
		}

		public void remove()
		{
			throw new UnsupportedOperationException();
		}
	}

	class StateIterator
		implements Iterator
	{
		private Iterator arcIt;
		private State currState = null;
		private String eventLabel;
		private boolean outgoing;

		public StateIterator(String eventLabel, boolean outgoing)
		{
			this.eventLabel = eventLabel;
			this.outgoing = outgoing;
			this.arcIt = theArcs.iterator();
			this.currState = null;
			findNext();
		}

		public boolean hasNext()
		{
			return currState != null;
		}

		public Object next()
		{
			State returnState = currState;
			findNext();
			return returnState;
		}

		public void remove()
		{
			throw new UnsupportedOperationException();
		}

		private void findNext()
		{
			while (arcIt.hasNext())
			{
				Arc currArc = (Arc)arcIt.next();
				String currLabel = null;
				try
				{
					currLabel = getLabel(currArc);
				}
				catch (Exception ex)
				{
					logger.error("Automaton::StateIterator.findNext: Error in getLabel");
				}
				if (eventLabel.equals(currLabel))
				{
					if (outgoing)
					{
						currState = currArc.getFromState();
						return;
					}
					else
					{
						currState = currArc.getToState();
						return;
					}
				}
			}
			currState = null;
		}
	}
}
