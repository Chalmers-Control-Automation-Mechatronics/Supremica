
/*
 * Supremica Software License Agreement
 *
 * The Supremica software is not in the public domain
 * However, it is freely available without fee for education,
 * research, and non-profit purposes.  By obtaining copies of
 * this and other files that comprise the Supremica software,
 * you, the Licensee, agree to abide by the following
 * conditions and understandings with respect to the
 * copyrighted software:
 *
 * The software is copyrighted in the name of Supremica,
 * and ownership of the software remains with Supremica.
 *
 * Permission to use, copy, and modify this software and its
 * documentation for education, research, and non-profit
 * purposes is hereby granted to Licensee, provided that the
 * copyright notice, the original author's names and unit
 * identification, and this permission notice appear on all
 * such copies, and that no charge be made for such copies.
 * Any entity desiring permission to incorporate this software
 * into commercial products or to use it for commercial
 * purposes should contact:
 *
 * Knut Akesson (KA), knut@supremica.org
 * Supremica,
 * Haradsgatan 26A
 * 431 42 Molndal
 * SWEDEN
 *
 * to discuss license terms. No cost evaluation licenses are
 * available.
 *
 * Licensee may not use the name, logo, or any other symbol
 * of Supremica nor the names of any of its employees nor
 * any adaptation thereof in advertising or publicity
 * pertaining to the software without specific prior written
 * approval of the Supremica.
 *
 * SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 * SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 * IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 * Supremica or KA shall not be liable for any damages
 * suffered by Licensee from the use of this software.
 *
 * Supremica is owned and represented by KA.
 */
package org.supremica.automata;

import org.supremica.util.SupremicaException;
import java.util.*;
import org.supremica.log.*;
import org.supremica.util.SupremicaIterator;
import org.supremica.properties.SupremicaProperties;

public class Automaton
	implements ArcListener
{
	private static Logger logger = LoggerFactory.createLogger(Automaton.class);
	private final Alphabet alphabet;

	/**
	 * The name of the automaton.
	 */
	private String name;

	/**
	 * A temporary name, used as a suggestion for a name to the gui when
	 * adding a new automaton, so that the gui can avoid giving two automata
	 * the same name.
	 */
	private String comment;

	// private List theStates = new LinkedList();
	private final StateSet theStates = new StateSet();
	private int index = -1;
	private Map idStateMap;    // Want fast lookup on both id and index (but not name?)
	private Map indexStateMap;
	private ArcSet theArcs;
	private State initialState;
	private boolean isDisabled = false;
	private AutomatonType type = AutomatonType.Specification;
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

		alphabet.union(orgAlphabet);

		type = orgAut.type;

		if (orgAut.hasName())
		{
			setName(orgAut.getName());
		}

		setComment(orgAut.getComment());

		// Create all states
		for (StateIterator states = orgAut.stateIterator(); states.hasNext(); )
		{
			State orgState = states.nextState();
			State newState = new State(orgState);

			addState(newState);
		}

		try
		{
			// Create all transitions
			for (StateIterator states = orgAut.stateIterator();
					states.hasNext(); )
			{
				State orgSourceState = (State) states.next();
				State newSourceState = getStateWithId(orgSourceState.getId());

				for (ArcIterator outgoingArcs = orgSourceState.safeOutgoingArcsIterator();
						outgoingArcs.hasNext(); )
				{
					Arc orgArc = outgoingArcs.nextArc();
					State orgDestState = orgArc.getToState();
					State newDestState = getStateWithId(orgDestState.getId());
					LabeledEvent currEvent = alphabet.getEvent(orgArc.getEvent());
					Arc newArc = new Arc(newSourceState, newDestState, currEvent);

					addArc(newArc);
				}
			}
		}
		catch (Exception ex)
		{
			logger.error("Error while copying transitions", ex);
			ex.printStackTrace();
			logger.debug(ex.getStackTrace());
		}
	}

	public void setType(AutomatonType type)
		throws IllegalArgumentException
	{
		if (type == null)
		{
			throw new IllegalArgumentException("Type must be non-null");
		}

		this.type = type;
	}

	public AutomatonType getType()
	{
		if (type == null)
		{
			return AutomatonType.Undefined;
		}

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

	public boolean isUndefined()
	{
		return type == AutomatonType.Undefined;
	}

	public void setName(String name)
		throws IllegalArgumentException
	{
		/*
		if (name == null)
		{
				throw new IllegalArgumentException("Name must be non-null");
		}
		*/
		String oldName = this.name;

		this.name = name;

		notifyListeners(AutomatonListeners.MODE_AUTOMATON_RENAMED, oldName);
	}

	/**
	 * Returns the name of the automaton, or, if there is no name, returns the comment
	 */
	public String getName()
	{
		if ((name == null) || (name.equals("") && !getComment().equals("")))
		{
			// This solved some ugly problems...
			// but this isn't all that beautiful either... /hguo
			if (comment != null)
			{
				return getComment();
			}
			else
			{
				logger.error("Error in Automata.java. Automaton with empty name and comment detected!");
			}

			return "";
		}

		return name;
	}

	/**
	 * Returns true if the automaton has a name (not comment) that is not null or empty.
	 */
	public boolean hasName()
	{
		return !((name == null) || (name == ""));
	}

	/**
	 * Returns the comment of the automaton.
	 */
	public String getComment()
	{
		if (comment == null)
		{
			return "";
		}

		return comment;
	}

	public void setComment(String comment)
		throws IllegalArgumentException
	{
		if (comment == null)
		{
			throw new IllegalArgumentException("Comment must be non-null");
		}

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

	// Quick and ugly fixx, see bug report
	// When working with the state-indices, sometimes, somehow the indices in
	// indexStateMap become different from the indices stored in the States
	// This func iterates over the states and rebuilds the map
	// Used by ModifiedAstar
	//  When is the slow and beautiful fixx due?
	public void remapStateIndices()
	{
		indexStateMap.clear();
		idStateMap.clear();

		for (StateIterator stit = stateIterator(); stit.hasNext(); )
		{
			State state = stit.nextState();

			indexStateMap.put(new Integer(state.getIndex()), state);
			idStateMap.put(state.getId(), state);
		}
	}

	public void addState(State state)
		throws IllegalArgumentException
	{
		if (state == null)
		{
			throw new IllegalArgumentException("State must be non-null");
		}

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

	/**
	 * Returns a list of control inconsisten events among the
	 * given states are consistent.
	 * We have an inconsistency if an event is explicitly
	 * removed in one state and explicitly enabled in another.
	 * To represent the explicitly removed events they must
	 * exist from the state but they are assumed to end in
	 * an forbidden state.
	 * The states in stateset must be contained in the current automaton.
	 */
	public Alphabet getControlInconsistentEvents(StateSet stateset)
	{
		Alphabet explicitlyForbiddenEvents = new Alphabet();

		// We start by computing the set of explicitly
		// forbidden events
		for (StateIterator stateIt = stateIterator(); stateIt.hasNext(); )
		{
			State currState = stateIt.nextState();

			for (ArcIterator arcIt = currState.outgoingArcsIterator();
					arcIt.hasNext(); )
			{
				Arc currArc = arcIt.nextArc();
				State nextState = currArc.getToState();

				if (nextState.isForbidden())
				{
					LabeledEvent currEvent = currArc.getEvent();

					if (!explicitlyForbiddenEvents.includes(currEvent))
					{
						try
						{
							explicitlyForbiddenEvents.addEvent(currEvent);
						}
						catch (Exception ex)
						{
							logger.error("Could not add event in getControlInconsistentStates", ex);
						}
					}
				}
			}
		}

		// We continuing by iterating over the
		// all explicitly allowed events and check
		// that those are not in the list of explicitly
		// forbidden events
		Alphabet controlInconsistentEvents = new Alphabet();

		for (StateIterator stateIt = stateIterator(); stateIt.hasNext(); )
		{
			State currState = stateIt.nextState();

			for (ArcIterator arcIt = currState.outgoingArcsIterator();
					arcIt.hasNext(); )
			{
				Arc currArc = arcIt.nextArc();
				State nextState = currArc.getToState();

				if (!nextState.isForbidden())
				{
					LabeledEvent currEvent = currArc.getEvent();

					if (explicitlyForbiddenEvents.includes(currEvent))
					{
						try
						{
							controlInconsistentEvents.addEvent(currEvent);
						}
						catch (Exception ex)
						{
							logger.error("Could not add event in getControlInconsistentStates", ex);
						}
					}
				}
			}
		}

		return controlInconsistentEvents;
	}

	/**
	 * Sets an explicitly allowed arc (event) to point
	 * to a forbidden state if that events is involved
	 * in a control inconsistency. See getControlInconsistentEvents.
	 */
	public Alphabet resolveControlInconsistencies(StateSet stateset)
	{
		Alphabet explicitlyForbiddenEvents = new Alphabet();
		Map eventToStateMap = new HashMap();

		// We start by computing the set of explicitly
		// forbidden events
		for (StateIterator stateIt = stateIterator(); stateIt.hasNext(); )
		{
			State currState = stateIt.nextState();

			for (ArcIterator arcIt = currState.outgoingArcsIterator();
					arcIt.hasNext(); )
			{
				Arc currArc = arcIt.nextArc();
				State nextState = currArc.getToState();

				if (nextState.isForbidden())
				{
					LabeledEvent currEvent = currArc.getEvent();

					if (!explicitlyForbiddenEvents.includes(currEvent))
					{
						try
						{
							explicitlyForbiddenEvents.addEvent(currEvent);
							eventToStateMap.put(currEvent, nextState);
						}
						catch (Exception ex)
						{
							logger.error("Could not add event in resolveControlInconsistencies", ex);
						}
					}
				}
			}
		}

		// We continuing by iterating over the
		// all explicitly allowed events and check
		// that those are not in the list of explicitly
		// forbidden events
		Alphabet controlInconsistentEvents = new Alphabet();

		for (StateIterator stateIt = stateIterator(); stateIt.hasNext(); )
		{
			State currState = stateIt.nextState();

			for (ArcIterator arcIt = currState.outgoingArcsIterator();
					arcIt.hasNext(); )
			{
				Arc currArc = arcIt.nextArc();
				State nextState = currArc.getToState();

				if (!nextState.isForbidden())
				{
					LabeledEvent currEvent = currArc.getEvent();

					if (explicitlyForbiddenEvents.includes(currEvent))
					{
						try
						{
							State forbiddenState = (State) eventToStateMap.get(currEvent);

							currArc.setToState(forbiddenState);
							controlInconsistentEvents.addEvent(currEvent);
						}
						catch (Exception ex)
						{
							logger.error("Could not add event in getControlInconsistentStates", ex);
						}
					}
				}
			}
		}

		return controlInconsistentEvents;
	}

	// If a state with this id (and/or name?) already exists, return the existing state
	// Else, add this state and return it
	public State addStateChecked(State state)
		throws IllegalArgumentException
	{
		if (state == null)
		{
			throw new IllegalArgumentException("State must be non-null");
		}

		State existing = (State) idStateMap.get(state.getId());

		if (existing != null)
		{
			return existing;
		}

		// else, add it as usual
		addState(state);

		return state;
	}

	public void removeState(State state)
		throws IllegalArgumentException
	{
		if (state == null)
		{
			throw new IllegalArgumentException("State must be non-null");
		}

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

	/**
	 * Returns true if this is the null automaton, i.e. this
	 * automaton does not have an initial state.
	 */
	public boolean isNullAutomaton()
	{
		return !hasInitialState();
	}

	// This is a fixx, for now - see bug report
	public void setInitialState(State state)
		throws IllegalArgumentException
	{
		if (state == null)
		{
			throw new IllegalArgumentException("State must be non-null");
		}

		State oldinit = getInitialState();
		State newinit = getState(state);

		if (newinit == null)
		{
			throw new IllegalStateException("No such state. id = " + state.getId());
		}

		newinit.setInitial(true);

		initialState = newinit;

		if (oldinit != null)
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

		if ((prefix == null) || prefix.equals(""))
		{
			name = new StringBuffer("q" + uniqueStateIndex++);
		}
		else
		{
			name = new StringBuffer(prefix);
		}

		while (containsStateWithId(name.toString()))
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
		for (StateIterator stateIt = stateIterator(); stateIt.hasNext(); )
		{
			State currState = stateIt.nextState();

			if (currState.isAccepting())
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * True if automaton has at least one self loop.
	 */
	public boolean hasSelfLoop()
	{
		for (ArcIterator arcIt = theArcs.iterator(); arcIt.hasNext(); )
		{
			Arc currArc = arcIt.nextArc();

			if (currArc.isSelfLoop())
			{
				return true;
			}
		}

		return false;
	}

	public boolean isDeterministic()
	{
		HashSet foundEvents = new HashSet();

		for (SupremicaIterator stateIt = new SupremicaIterator(stateIterator());
				stateIt.isValid(); stateIt.increase())
		{
			State currState = (State) stateIt.get();

			foundEvents.clear();

			for (EventIterator evIt = outgoingEventsIterator(currState);
					evIt.hasNext(); )
			{
				LabeledEvent currEvent = evIt.nextEvent();

				// Epsilon event?
				if (currEvent.isEpsilon())
				{
					return false;
				}

				// Has this event been seen in another transition from this state?
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

	public boolean isAllEventsObservable()
	{
		return alphabet.isAllEventsObservable();
	}

	/**
	 * When adding an arc, both the two states associated with the
	 * arc _must_ already be contained in the automaton, otherwise the
	 * behavior is undefined.
	 */
	public void addArc(Arc arc)
		throws IllegalArgumentException
	{
		if (arc == null)
		{
			throw new IllegalArgumentException("Arc must be non-null");
		}

		// Add the arc to the individual states
		arc.getFromState().addOutgoingArc(arc);
		arc.getToState().addIncomingArc(arc);

		arc.getListeners().addListener(this);
		theArcs.addArc(arc);
		notifyListeners(AutomatonListeners.MODE_ARC_ADDED, arc);
	}

	public void removeArc(Arc arc)
		throws IllegalArgumentException
	{
		if (arc == null)
		{
			throw new IllegalArgumentException("Arc must be non-null");
		}

		arc.clear();
		theArcs.removeArc(arc);
		notifyListeners(AutomatonListeners.MODE_ARC_REMOVED, arc);
	}

	/*
	public void removeArcs(ArcSet arcSet)
	{
		try
		{
			for (ArcIterator arcIt = arcSet.iterator(); arcIt.hasNext(); )
			{
				removeArc(arcIt.nextArc());
			}
		}
		catch (Exception ex)
		{
			logger.error("Error in Automaton.java when removing arcs.");
		}
	}
	*/

	public boolean containsState(State state)
		throws IllegalArgumentException
	{
		if (state == null)
		{
			throw new IllegalArgumentException("State must be non-null");
		}

		return idStateMap.containsKey(state.getId());
	}

	public StateSet getStateSet()
	{
		return theStates;
	}

	// Note, searches on id - only call this with states in this automaton
	public State getState(State state)
		throws IllegalArgumentException
	{
		if (state == null)
		{
			throw new IllegalArgumentException("State must be non-null");
		}

		return (State) idStateMap.get(state.getId());
	}

	// Given this state, which belongs to this stateset, return a unique id-string
	public String getUniqueStateId(State state)
	{

		// prereq: state is in theStates:
		return state.getId();    // at the moment do the simplest thing
	}

	/**
	 * True if a state with the name exists, otherwise false.
	 */
	public boolean containsStateWithName(String name)
		throws IllegalArgumentException
	{
		if (name == null)
		{
			throw new IllegalArgumentException("Name must be non-null");
		}

		State theState = getStateWithName(name);

		return theState != null;
	}

	/**
	 * Returns the state with the asked for name if it exists, otherwise null.
	 */
	public State getStateWithName(String name)
		throws IllegalArgumentException
	{
		if (name == null)
		{
			throw new IllegalArgumentException("Name must be non-null");
		}

		for (StateIterator stateIt = stateIterator(); stateIt.hasNext(); )
		{
			State currState = stateIt.nextState();

			if (currState.getName().equals(name))
			{
				return currState;
			}
		}

		return null;
	}

	private boolean containsStateWithId(String id)
	{
		return idStateMap.containsKey(id);
	}

	private State getStateWithId(String id)
	{
		return (State) idStateMap.get(id);
	}

	// The index stuff should be exclusive to AutomataIndexForm, but how to manage that?

	/**
	 * This is an ugly method that is only needed when dealing
	 * with automataIndexForm. All methods that works with index
	 * needs special initialisation that is not automatically done.
	 * This method is not recommended for general use.
	 */
	public boolean containsStateWithIndex(int index)
	{
		return indexStateMap.containsKey(new Integer(index));
	}

	/**
	 * This is an ugly method that only is needed when dealing
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

	// end index stuff
	// What the f*** are these doing here?

	/*
	private LabeledEvent getEvent(String eventId)
			throws Exception
	{
			// return alphabet.getEventWithId(eventId);
			return alphabet.getEvent(new LabeledEvent(eventId));
	}
	*/

	/**
	 * Returns an iterator to all states in this automaton
	 * that has an event with eventLabel as an outoing event.
	 */
	public StateIterator statesThatEnableEventIterator(String eventLabel)
		throws IllegalStateException
	{
		if (eventLabel == null)
		{
			throw new IllegalArgumentException("EventLabel must be non-null");
		}

		StateIterator stateIt = new InternalStateIterator(eventLabel, true);

		return stateIt;
	}

	/**
	 * Returns true if the event with label eventLabel is prioritized in this
	 * automaton. If the event is not included in this automaton or is not
	 * prioritized then it returns false.
	 */
	public boolean isEventPrioritized(String eventLabel)
		throws IllegalArgumentException
	{
		if (eventLabel == null)
		{
			throw new IllegalArgumentException("EventLabel must be non-null");
		}

		if (alphabet.contains(eventLabel))
		{
			LabeledEvent thisEvent = alphabet.getEvent(eventLabel);

			return thisEvent.isPrioritized();
		}
		else
		{
			return false;
		}
	}

	public LabeledEvent getEvent(String eventLabel)
		throws IllegalArgumentException
	{
		return alphabet.getEvent(eventLabel);
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

	public int nbrOfEpsilonTransitions()
	{
		int amount = 0;

		for (ArcIterator arcIt = theArcs.iterator(); arcIt.hasNext(); )
		{
			Arc currArc = arcIt.nextArc();

			if (currArc.getEvent().isEpsilon())
			{
				amount++;
			}
		}

		return amount;
	}

	/**
	 * Amount of selfloops in automaton.
	 */
	public int nbrOfSelfLoops()
	{
		int amount = 0;

		for (ArcIterator arcIt = theArcs.iterator(); arcIt.hasNext(); )
		{
			Arc currArc = arcIt.nextArc();

			if (currArc.isSelfLoop())
			{
				amount++;
			}
		}

		return amount;
	}

	/**
	 * Amount of selfloops of the events in anAlphabet
	 */
	public int nbrOfSelfLoops(Alphabet anAlphabet)
	{
		int amount = 0;

		for (ArcIterator arcIt = theArcs.iterator(); arcIt.hasNext(); )
		{
			Arc currArc = arcIt.nextArc();

			if (currArc.isSelfLoop() && anAlphabet.contains(currArc.getEvent()))
			{
				amount++;
			}
		}

		return amount;
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

	public int nbrOfMutuallyAcceptingStates()
	{
		int nbrOfAcceptingStates = 0;
		Iterator stateIt = stateIterator();

		while (stateIt.hasNext())
		{
			State currState = (State) stateIt.next();

			if (currState.isMutuallyAccepting())
			{
				nbrOfAcceptingStates++;
			}
		}

		return nbrOfAcceptingStates;
	}

	public int nbrOfMutuallyAcceptingNotForbiddenStates()
	{
		int nbrOfAcceptingStates = 0;
		Iterator stateIt = stateIterator();

		while (stateIt.hasNext())
		{
			State currState = (State) stateIt.next();

			if (currState.isMutuallyAccepting() &&!currState.isForbidden())
			{
				nbrOfAcceptingStates++;
			}
		}

		return nbrOfAcceptingStates;
	}

	public int nbrOfForbiddenStates()
	{
		int nbrOfForbiddenStates = 0;

		for (StateIterator stateIt = stateIterator(); stateIt.hasNext(); )
		{
			State currState = stateIt.nextState();

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

		for (StateIterator stateIt = stateIterator(); stateIt.hasNext(); )
		{
			State currState = stateIt.nextState();

			if (currState.isAccepting() && currState.isForbidden())
			{
				nbrOfAcceptingAndForbiddenStates++;
			}
		}

		return nbrOfAcceptingAndForbiddenStates;
	}

	/**
	 * Returns depth of automaton.
	 */
	public int depth()
	{
		int depth = 0;

		for (StateIterator stateIterator = stateIterator();
				stateIterator.hasNext(); )
		{

			// Measure the shortest trace to the state.
			State currState = stateIterator.nextState();
			int stateDepth;

			try
			{
				stateDepth = getTrace(currState).size();
			}
			catch (Exception ex)
			{
				logger.error(ex);

				return Integer.MAX_VALUE;
			}

			if (stateDepth > depth)
			{
				depth = stateDepth;
			}
		}

		return depth;
	}

	/**
	 * Returns sum of depths of transitions of events in anAlphabet.
	 */
	public int depthSum(Alphabet anAlphabet)
	{
		int depthSum = 0;

		for (StateIterator stateIterator = stateIterator();
				stateIterator.hasNext(); )
		{

			// Measure the shortest trace to the state.
			State currState = stateIterator.nextState();
			int stateDepth;

			try
			{
				stateDepth = getTrace(currState).size();
			}
			catch (Exception ex)
			{
				logger.error(ex);

				return Integer.MAX_VALUE;
			}

			// Calculate sum of
			for (ArcIterator arcIterator = currState.outgoingArcsIterator();
					arcIterator.hasNext(); )
			{
				if (anAlphabet.contains(arcIterator.nextEvent()))
				{
					depthSum += stateDepth;
				}
			}
		}

		return depthSum;
	}

	public StateIterator stateIterator()
	{
		return new StateIterator(theStates.iterator());
	}

	/**
	 * Use this iterator instead of stateIterator when you will add or
	 * remove states in this automaton.
	 */
	public StateIterator safeStateIterator()
	{
		return new StateIterator((new StateSet(theStates)).iterator());
	}

	public ArcIterator arcIterator()
	{
		return theArcs.iterator();
	}

	public ArcIterator safeArcIterator()
	{
		return (new ArcSet(theArcs)).iterator();
	}

	public boolean containsArc(Arc arc)
	{
		return theArcs.containsArc(arc);
	}

	public EventIterator outgoingEventsIterator(State theState)
	{
		Iterator arcIt = theState.outgoingArcsIterator();

		return new InternalEventIterator(arcIt);
	}

	public EventIterator incomingEventsIterator(State theState)
	{
		Iterator arcIt = theState.incomingArcsIterator();

		return new InternalEventIterator(arcIt);
	}

	/**
	 * These are only valid if the automatonType is interface.
	 */
	public Automata getMasterAutomata()
		throws IllegalStateException
	{
		if (!isInterface())
		{
			throw new IllegalStateException("This Automaton is not an interface");
		}

		return masterAutomata;
	}

	public Automata getSlaveAutomata()
		throws IllegalStateException
	{
		if (!isInterface())
		{
			throw new IllegalStateException("This Automaton is not an interface");
		}

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
			Automaton currAutomaton = (Automaton) autIt.next();

			if (!validAutomata.containsAutomaton(currAutomaton))
			{
				toBeRemoved.add(currAutomaton);
			}
		}

		for (Iterator autIt = toBeRemoved.iterator(); autIt.hasNext(); )
		{
			Automaton currAutomaton = (Automaton) autIt.next();

			currDependencies.removeAutomaton(currAutomaton);
		}
	}

	public EventIterator eventIterator()
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

/*
		public void setAlphabet(Alphabet alphabet)
				throws IllegalArgumentException
		{
				if (alphabet == null)
				{
						throw new IllegalArgumentException("Alphabet must be non-null");
				}
				this.alphabet = alphabet;
		}
*/

	/**
	 * In some situation, for example in the dot output
	 * not all state identities can be accepeted. For examepl
	 * dot does not handle dots in the statename.
	 * This method resets all state identities to valid names
	 */
	public void normalizeStateIdentities()
	{
		setStateIndicies();

		for (StateIterator stateIt = stateIterator(); stateIt.hasNext(); )
		{
			State currState = stateIt.nextState();

			currState.setId("q_" + currState.getIndex());
		}

		remapStateIndices();
	}

	public void setIndicies()
	{
		setIndicies(0);
	}

	void setIndicies(int automatonIndex)
	{
		index = automatonIndex;

		alphabet.setIndicies();
		setStateIndicies();
	}

	void setIndicies(int automatonIndex, Alphabet otherAlphabet)
	{
		index = automatonIndex;

		alphabet.setIndicies(otherAlphabet);
		setStateIndicies();
	}

	private void setStateIndicies()
	{
		int i = 0;

		for (StateIterator stateIt = stateIterator(); stateIt.hasNext(); )
		{
			State currState = stateIt.nextState();

			currState.setIndex(i++);
		}
	}

	void setIndex(int index)
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
		for (StateIterator stateIt = stateIterator(); stateIt.hasNext(); )
		{
			State currState = stateIt.nextState();

			currState.setVisited(false);
		}
	}

	public void clearSelectedStates()
	{
		for (StateIterator stateIt = stateIterator(); stateIt.hasNext(); )
		{
			State currState = stateIt.nextState();

			currState.setSelected(false);
		}
	}

	private void removeAssociatedStateFromUnvisitedStates()
	{
		for (StateIterator stateIt = stateIterator(); stateIt.hasNext(); )
		{
			State currState = stateIt.nextState();

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
		throws Exception, IllegalArgumentException, IllegalStateException
	{
		if (toState == null)
		{
			throw new IllegalArgumentException("Automaton.getTrace: toState must be non-null");
		}

		if (!hasInitialState())
		{
			throw new IllegalStateException("There is no initial state");
		}

		return getTrace(getInitialState(), toState);
	}

	/**
	 * Returns the shortest trace from fromState to toState,
	 */
	public LabelTrace getTrace(State fromState, State toState)
		throws Exception, IllegalArgumentException
	{
		if (fromState == null)
		{
			throw new IllegalArgumentException("Automaton.getTrace: fromState must be non-null");
		}

		if (toState == null)
		{
			throw new IllegalArgumentException("Automaton.getTrace: toState must be non-null");
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
				throw new SupremicaException("Could not find an arc from " + prevState.getName() + " to " + thisState.getName());
			}

			theTrace.addFirst(currEvent.getLabel());

			thisState = prevState;
			prevState = prevState.getAssociatedState();
		}

		reverseAssociatedState(toState);

		return theTrace;
	}

	private void computeShortestPath(State fromState)
		throws IllegalArgumentException
	{
		if (fromState == null)
		{
			throw new IllegalArgumentException("Automaton.getTrace: fromState is null");
		}

		clearVisitedStates();

		// This implements a breath first search
		LinkedList openStates = new LinkedList();

		fromState.setAssociatedState(null);
		openStates.addLast(fromState);
		fromState.setVisited(true);

		while (openStates.size() > 0)
		{
			State currState = (State) openStates.removeFirst();

			for (Iterator arcIt = currState.outgoingArcsIterator();
					arcIt.hasNext(); )
			{
				Arc currArc = (Arc) arcIt.next();
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
	 * Removes mutually accepting status from all states.
	 */

	/*
	public void setMutuallyAcceptingStatus(boolean status)
	{

	}
	*/

	/**
	 * Backwards extend accepting and mutually accepting states along transitions with safeEvents.
	 * some kind of "coreachability along a subset of the alphabet".
	 */
	public void extendMutuallyAccepting(Alphabet safeEvents)
	{
		beginTransaction();

		boolean changes = true;

		while (changes)
		{
			changes = false;

			for (Iterator stateIt = stateIterator(); stateIt.hasNext(); )
			{
				State currState = (State) stateIt.next();

				// This state must be mutually accepting and not forbidden?
				//if (currState.isMutuallyAccepting() && !currState.isForbidden())
				// We don't care about forbidden states...
				if (currState.isMutuallyAccepting())
				{
					for (ArcIterator arcIt = currState.incomingArcsIterator();
							arcIt.hasNext(); )
					{
						Arc currArc = arcIt.nextArc();
						LabeledEvent arcEvent = currArc.getEvent();

						if (safeEvents.contains(arcEvent.getLabel()))
						{
							State fromState = currArc.getFromState();

							if (!fromState.isMutuallyAccepting())
							{
								fromState.setMutuallyAccepting(true);

								//fromState.setAccepting(true);
								changes = true;
							}
						}
					}
				}
			}
		}

		invalidate();
		endTransaction();
	}

	public void setAllStatesAsAccepting()
	{
		setAllStatesAsAccepting(false);
	}

	public void setAllStatesAsAccepting(boolean keepForbidden)
	{
		beginTransaction();

		for (StateIterator stateIt = stateIterator(); stateIt.hasNext(); )
		{
			State currState = stateIt.nextState();

			if (keepForbidden)
			{
				if (!currState.isForbidden())
				{
					currState.setAccepting(true);
				}
			}
			else
			{
				currState.setAccepting(true);
				currState.setForbidden(false);
			}
		}

		invalidate();
		endTransaction();
	}

	public void setAllMutuallyAcceptingStatesAsAccepting()
	{
		setAllMutuallyAcceptingStatesAsAccepting(false);
	}

	public void setAllMutuallyAcceptingStatesAsAccepting(boolean keepForbidden)
	{
		beginTransaction();

		for (StateIterator stateIt = stateIterator(); stateIt.hasNext(); )
		{
			State currState = stateIt.nextState();

			if (currState.isMutuallyAccepting())
			{
				if (keepForbidden)
				{
					if (!currState.isForbidden())
					{
						currState.setAccepting(true);
					}
				}
				else
				{
					currState.setAccepting(true);
					currState.setForbidden(false);
				}
			}
		}

		invalidate();
		endTransaction();
	}

	/**
	 * Makes all mutually accepting or accepting states non-accepting and all non-accepting states accepting.
	 */
	public void invertMarking()
	{
		for (StateIterator stateIt = stateIterator(); stateIt.hasNext(); )
		{
			State currState = stateIt.nextState();

			if (currState.isMutuallyAccepting() || currState.isAccepting())
			{
				currState.setAccepting(false);
			}
			else
			{
				currState.setAccepting(true);
			}
		}
	}

	/**
	 * Returns a event on a arc that starts in fromState and ends in toState.
	 * If no such event exists, null is returned.
	 */
	public LabeledEvent getLabeledEvent(State fromState, State toState)
		throws Exception
	{
		for (ArcIterator arcIt = fromState.outgoingArcsIterator();
				arcIt.hasNext(); )
		{
			Arc currArc = arcIt.nextArc();
			State currToState = currArc.getToState();

			if (currToState == toState)
			{
				return currArc.getEvent();
			}
		}

		return null;
	}

	/**
	 * Hides (makes epsilon) the supplied events.
	 */
	public void hide(Alphabet alpha)
	{
		// Don't hide nothing!
		if ((alpha == null) || (alpha.size() == 0))
		{
			return;
		}

		Automaton aut = this;

		// Remove the hidden events from alphabet
		aut.getAlphabet().minus(alpha);

		// Get/create silent event tau
		LabeledEvent tau = aut.getAlphabet().getEvent("tau");
		if (tau == null)
		{
			tau = new LabeledEvent("tau");
			tau.setEpsilon(true);
			aut.getAlphabet().addEvent(tau);
		}
		else
		{
			if (!tau.isEpsilon())
			{
				logger.error("The event name 'tau' is reserved and must be unobservable!");
				return;
			}
		}

		// Modify arcs
		for (ArcIterator arcIt = aut.arcIterator(); arcIt.hasNext(); )
		{
			Arc arc = arcIt.nextArc();

			// Hide this one?
			if (alpha.contains(arc.getEvent()))
			{
				arc.setEvent(tau);
			}
		}
	}

	/**
	 * Merges two states, giving the new state the union of incoming and outgoing transitions,
	 * if at least one state was accepting, the result is accepting, and similarily for
	 * initial and forbidden states
	 */
	public State mergeStates(State one, State two)
	{
		// Don't merge if equal
		if (one.equals(two))
		{
			return one;
		}

		// Make new state
		State newState = new State(one.getName() + SupremicaProperties.getStateSeparator() + two.getName());
		addState(newState);
		
		// Adjust name length
		if (newState.getName().length() > 201804)
		{
			newState.setName(newState.getName().substring(0,10) + Math.random());
		}

		if (one.isAccepting() || two.isAccepting())
		{
			newState.setAccepting(true);
		}
		if (one.isForbidden() || two.isForbidden())
		{
			newState.setForbidden(true);
		}
		if (one.isInitial() || two.isInitial())
		{
			newState.setInitial(true);
			setInitialState(newState);
		}

		// Add transitions
		LinkedList toBeAdded = new LinkedList();
		for (ArcIterator arcIt = one.outgoingArcsIterator(); arcIt.hasNext(); )
		{
			Arc arc = arcIt.nextArc();
			State toState = arc.getToState();
			if (toState.equals(one) || toState.equals(two))
			{
				toState = newState;
			}

			toBeAdded.add(new Arc(newState, toState, arc.getEvent()));
		}
		for (ArcIterator arcIt = two.outgoingArcsIterator(); arcIt.hasNext(); )
		{
			Arc arc = arcIt.nextArc();
			State toState = arc.getToState();
			if (toState.equals(one) || toState.equals(two))
			{
				toState = newState;
			}

			toBeAdded.add(new Arc(newState, toState, arc.getEvent()));
		}
		for (ArcIterator arcIt = one.incomingArcsIterator(); arcIt.hasNext(); )
		{
			Arc arc = arcIt.nextArc();
			State fromState = arc.getFromState();
			if (fromState.equals(one) || fromState.equals(two))
			{
				fromState = newState;
			}

			toBeAdded.add(new Arc(fromState, newState, arc.getEvent()));
		}
		for (ArcIterator arcIt = two.incomingArcsIterator(); arcIt.hasNext(); )
		{
			Arc arc = arcIt.nextArc();
			State fromState = arc.getFromState();
			if (fromState.equals(one) || fromState.equals(two))
			{
				fromState = newState;
			}

			toBeAdded.add(new Arc(fromState, newState, arc.getEvent()));
		}
		// Add the new arcs!
		while (toBeAdded.size() != 0)
		{
			// Add if not already there
			Arc arc = (Arc) toBeAdded.remove(0);
			if (!arc.getFromState().containsOutgoingArc(arc))
			{
				addArc(arc);
			}
		}

		// Remove the states
		removeState(one);
		removeState(two);

		/*
		// Adjust the index of the new state (see the "Här blir det fel" discussion in AutomataIndexForm)
		if (one.getIndex() < two.getIndex())
		{
			// Take over the index of state one
			newState.setIndex(one.getIndex());
		}
		else
		{
			// Take over the index of state two
			newState.setIndex(two.getIndex());
		}
		*/

		// Return the new state
		return newState;
	}

	public void removeAllStates()
	{
		beginTransaction();
		idStateMap.clear();
		indexStateMap.clear();
		theStates.clear();
		theArcs.clear();

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
		if ((width < 0) || (height < 0))
		{
			return false;
		}

		for (Iterator stateIt = stateIterator(); stateIt.hasNext(); )
		{
			State currState = (State) stateIt.next();

			if (!currState.validLayout())
			{
				return false;
			}
		}

		return true;
	}

	public int nbrOfControllableEvents()
	{
		return alphabet.nbrOfControllableEvents();
	}

	public int nbrOfPrioritizedEvents()
	{
		return alphabet.nbrOfPrioritizedEvents();
	}

	public int nbrOfImmediateEvents()
	{
		return alphabet.nbrOfImmediateEvents();
	}

	public int nbrOfEpsilonEvents()
	{
		return alphabet.nbrOfEpsilonEvents();
	}

	public int nbrOfUnobservableEvents()
	{
		return alphabet.nbrOfUnobservableEvents();
	}

	/**
	 * Returns true if there are no obvious differences between this
	 * automaton and the other. Note, that this method only compares the
	 * number of states and transitions, etc. This method does not guarantee
	 * that the two automata generates the same language.
	 *
	 * Shouldn't there really be a CompareAutomata class?
	 * This class should have methods like areIsomorphic() and languageEqual()
	 */
	public boolean equalAutomaton(Automaton other)
	{
		boolean debug = true;

		// Should type, name, comment really be considered?
		if (getType() != other.getType())
		{
			if (debug)
			{
				System.err.println("equalAutomaton::non equal type");
			}

			return false;
		}

		if (!getName().equals(other.getName()))
		{
			if (debug)
			{

				// System.err.println(getName() + " " + other.getName());
				System.err.println("equalAutomaton::non equal name");
			}

			return false;
		}

		if (!getComment().equals(other.getComment()))
		{
			if (debug)
			{
				System.err.println("equalAutomaton::non equal comment");
			}

			return false;
		}

		// The following stuff seems useful to consider
		if (hasAcceptingState() != other.hasAcceptingState())
		{
			if (debug)
			{
				System.err.println("equalAutomaton::non equal accepting state");
			}

			return false;
		}

		if (hasSelfLoop() != other.hasSelfLoop())
		{
			if (debug)
			{
				System.err.println("equalAutomaton::non equal has self loop");
			}

			return false;
		}

		if (isDeterministic() != other.isDeterministic())
		{
			if (debug)
			{
				System.err.println("equalAutomaton::non equal is deterministic");
			}

			return false;
		}

		if (isAllEventsPrioritized() != other.isAllEventsPrioritized())
		{
			if (debug)
			{
				System.err.println("equalAutomaton::non equal is all events prioritized");
			}

			return false;
		}

		if (nbrOfAcceptingStates() != other.nbrOfAcceptingStates())
		{
			if (debug)
			{
				System.err.println("equalAutomaton::non equal name nbr of accepting states");
			}

			return false;
		}

		if (nbrOfForbiddenStates() != other.nbrOfForbiddenStates())
		{
			if (debug)
			{
				System.err.println("equalAutomaton::non equal nbr of forbidden states");
			}

			return false;
		}

		if (nbrOfAcceptingAndForbiddenStates() != other.nbrOfAcceptingAndForbiddenStates())
		{
			if (debug)
			{
				System.err.println("equalAutomaton::non equal name nbr of accepting and forbidden states");
			}

			return false;
		}

		if (!alphabet.equalAlphabet(other.alphabet))
		{
			if (debug)
			{
				System.err.println("equalAutomaton::non equal alphabet");
			}

			return false;
		}

		return true;
	}

	public long checksum()
	{

		// Ad-hoc checksum algorithm
		long checksum = 0;

		for (StateIterator sIt = stateIterator(); sIt.hasNext(); )
		{
			State currState = sIt.nextState();
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

		// System.err.println("beginTransaction");
		if (listeners != null)
		{
			listeners.beginTransaction();
		}
	}

	public void endTransaction()
	{

		// System.err.println("endTransaction");
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

		// Generate hascode from name, or if that's null, from the comment
		if (name != null)
		{
			return name.hashCode();
		}
		else if (comment != null)
		{
			return comment.hashCode();
		}

		logger.error("Error in Automaton.java. Some automaton has both name and comment empty! This is NOT recommended!");

		return 0;
	}

	public static void main(String[] args)
	{
		Automaton theAutomaton = new Automaton();
		State stateZero = new State("zero");

		theAutomaton.addState(stateZero);

		State st = theAutomaton.getStateWithIndex(0);    // should be stateZero (not?)

		if (st == null)
		{
			System.err.println("st == null");
		}
		else
		{
			System.err.println("st != null");
		}
	}

	class InternalEventIterator
		extends EventIterator
	{
		private final Iterator arcIt;

		public InternalEventIterator(Iterator arcIt)
		{
			super(null);

			this.arcIt = arcIt;
		}

		public boolean hasNext()
		{
			return arcIt.hasNext();
		}

		public Object next()    // really returns LabeledEvent
		{
			Arc nextArc = (Arc) arcIt.next();

			// String eventId = nextArc.getEventId();
			LabeledEvent nextEvent = null;

			try
			{
				nextEvent = nextArc.getEvent();    // eventId);
			}
			catch (Exception ex)
			{
				logger.error("Automaton::InternalEventIterator.next: Error in getEvent", ex);
				logger.debug(ex.getStackTrace());
			}

			return nextEvent;
		}

		public void remove()
		{
			throw new UnsupportedOperationException();
		}
	}

	class InternalStateIterator
		extends StateIterator
	{
		private final ArcIterator arcIt;
		private State currState = null;
		private String eventLabel;
		private boolean outgoing;

		public InternalStateIterator(String eventLabel, boolean outgoing)
		{
			super(null);

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
				Arc currArc = arcIt.nextArc();
				String currLabel = currArc.getLabel();

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

	// These are useful for debugging (etc)
	public String toDebugString()
	{
		StringBuffer sbuf = new StringBuffer();

		sbuf.append(getName());
		sbuf.append("::");

		for (Iterator it = arcIterator(); it.hasNext(); )
		{
			Arc arc = (Arc) it.next();

			sbuf.append(arc.toString());
		}

		return sbuf.toString();
	}

	public String toString()
	{
		return "'" + getName() + "'";
	}

	// toCode writes Java code - Note, the names are used as-is, which means there may be blanks in variabel names!
	public String toCode()
	{
		StringBuffer sbuf = new StringBuffer();

		sbuf.append("Automaton " + getName() + " = new Automaton(\"" + getName() + "\");");
		sbuf.append("\t\t{\t\t\t" + getName() + ".setType(AutomatonType." + getType().toString() + ");\n");

		for (StateIterator sit = stateIterator(); sit.hasNext(); )
		{
			State state = sit.nextState();

			sbuf.append("State " + state.getName() + " = new State(\"" + state.getName() + "\");");
			sbuf.append("\t" + state.getName() + ".setCost(" + state.getCost() + ");");
			sbuf.append("\t" + getName() + ".addState(" + state.getName() + ");");

			if (state.isInitial())
			{
				sbuf.append("\t" + getName() + ".setInitialState(" + state.getName() + ");");
			}

			if (state.isAccepting())
			{
				sbuf.append("\t" + state.getName() + ".setAccepting(true);");
			}

			sbuf.append("\n");
		}

		for (EventIterator eit = getAlphabet().iterator(); eit.hasNext(); )
		{
			LabeledEvent ev = eit.nextEvent();

			sbuf.append("LabeledEvent " + ev.getLabel() + " = new LabeledEvent(\"" + ev.getLabel() + "\");");
			sbuf.append("\t" + getName() + ".getAlphabet().addEvent(" + ev.getLabel() + ");\n");
		}

		for (ArcIterator ait = arcIterator(); ait.hasNext(); )
		{
			Arc arc = ait.nextArc();

			sbuf.append(getName() + ".addArc(new Arc(" + arc.getFromState().getName() + ", " + arc.getToState().getName() + ", " + arc.getEvent().getLabel() + "));\n");
		}

		sbuf.append("}\n");

		return sbuf.toString();
	}
}
