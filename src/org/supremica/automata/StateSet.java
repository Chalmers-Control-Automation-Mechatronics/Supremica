
/********************** StateSet.java *************************/

// Implementation of a useful state set.
// Note that a Set implementation considers two elements e1 and e2 equal
// if e1.equals(e2) == true; Surely this also means that e2.equals(e1)
// must then also be true (but the docs do not require this).
// For State, equals(Object obj) compares id's, so in practice
// StateSet is only well-defined for states of the same automaton.
// StateSet guarantees an ordering defined by the names of the states
package org.supremica.automata;

import java.util.*;
import org.supremica.properties.SupremicaProperties;

public class StateSet
	extends TreeSet<State>
{
	//private SortedSet<State> theSet = null;
	//private HashMap<String,State> nameToStateMap = null;
	private State singleStateRepresentation = null;

	/**
	 * Create an empty set
	 */
	public StateSet()
	{
		super(new State.StateComparator());
	}

	public StateSet(Collection<? extends State> collection)
	{
		this();
		super.addAll(collection);
	}

	public boolean add(State state)
	{
		return modified(super.add(state));
	}

	public boolean addAll(Collection<? extends State> collection)
	{
		return modified(super.addAll(collection));
	}

	public void clear()
	{
		super.clear();
		modified(size() != 0);
	}

	public boolean remove(Object object)
	{
		return modified(super.remove(object));
	}

	public boolean removeAll(Collection collection)
	{
		return modified(super.removeAll(collection));
	}

	public boolean retainAll(Collection collection)
	{
		return modified(super.retainAll(collection));
	}

	/**
	 * Removes and returns an arbitrary state from the set.
	 */
	public State remove()
	{
		State state = super.first();
		remove(state);
		return state;
	}

	public boolean equals(Object obj)
	{
		StateSet states = (StateSet) obj;

		if (this == states)    // avoid testing for self comparison
		{
			return true;
		}

		return super.equals(states);
	}

	public String toString()
	{
		StringBuffer buf = new StringBuffer();

		buf.append("StateSet[" + size() + "]: {");

		Iterator<State> it = iterator();
		while (it.hasNext())
		{
			State state = it.next();

			//buf.append(state.getName());
			buf.append(state);
			if (it.hasNext())
				buf.append(",");
		}

		buf.append("}");

		return buf.toString();
	}

	///////////////
	// EXTENSION //
	///////////////

	/**
	 * When this StateSet is modified, it will have a new singleStateRepresentation.
	 */
	private boolean modified(boolean change)
	{
		if (change)
		{
			singleStateRepresentation = null;
		}
		return change;
	}

	public Iterator<Arc> outgoingArcsIterator()
	{
		return new StateSetArcIterator(this, true);
	}

	public Iterator<Arc> incomingArcsIterator()
	{
		return new StateSetArcIterator(this, false);
	}

	/**
	 * Returns the state set that can reach some state in the current state set in a transition
	 * associated with event.
	 */
	public StateSet previousStates(LabeledEvent event)
	{
		StateSet prevStates = new StateSet();

		for (Iterator<State> stateIt = iterator(); stateIt.hasNext(); )
		{
			for (Iterator<State> prevIt = stateIt.next().previousStateIterator(event); 
				 prevIt.hasNext(); )
			{
				prevStates.add(prevIt.next());
			}
		}

		return prevStates;
	}

	/**
	 * Returns the set of states that can be reached from the current state set by transitions
	 * associated with "event".
	 * @param considerEpsilonClosure If true, the epsilon closure before and after "event" is also condidered,
	 * if false, only one step along transitions with event "event" is considered.
	 */
	public StateSet nextStates(LabeledEvent event, boolean considerEpsilonClosure)
	{
		StateSet nextStates = new StateSet();

		// Find nextStatesSet of each state
		for (Iterator<State> stateIt = iterator(); stateIt.hasNext(); )
		{
			State state = stateIt.next();
			nextStates.addAll(state.nextStates(event, considerEpsilonClosure));
		}

		return nextStates;
	}

	/**
	 * Works just as epsilonClosure in State.
	 * @see State
	 */
	public StateSet epsilonClosure(boolean includeSelf)
	{
		StateSet result = new StateSet();
		
		// Include self?
		if (includeSelf)
		{
			result.addAll(this);
		}

		// Examine states 
		StateSet statesToExamine = new StateSet();
		statesToExamine.addAll(this);
		while (statesToExamine.size() != 0)
		{
			State currState = (State) statesToExamine.remove();

			for (Iterator<Arc> arcIt = currState.outgoingArcsIterator(); arcIt.hasNext(); )
			{
				Arc currArc = arcIt.next();
				State state = currArc.getToState();
				
				if (currArc.getEvent().isEpsilon() && !currArc.isSelfLoop() && 
					!result.contains(state))
				{
					statesToExamine.add(state);
					result.add(state);
				}
			}
		}

		return result;
	}

	/**
	 * Returns a single state representation of this StateSet. Either by constructing a new one
	 * or by returning a previously constructed one.
	 */
	public State getSingleStateRepresentation()
	{
		if (singleStateRepresentation == null)
		{
			singleStateRepresentation = createSingleStateRepresentation();
		}
		return singleStateRepresentation;
	}

	/**
	 * Creates a new state named as the composition of the states in this set
	 * Should use the globally defined state separator
	 *
	 * The "initial" attribute should be set in the automaton that this state
	 * should be long to, not here!
	 */
	private State createSingleStateRepresentation()
	{
		// boolean i = false;   // initial?
		boolean d = false;    // desired?
		boolean x = false;    // forbidden?
		StringBuffer buf = new StringBuffer();
		Iterator stateit = iterator();

		while (stateit.hasNext())
		{
			State state = (State) stateit.next();

			// Add to new name
			buf.append(state.getName());
			if (stateit.hasNext())
			{
				buf.append(SupremicaProperties.getStateSeparator());
			}

			// i |= state.isInitial();
			d |= state.isAccepting();
			x |= state.isForbidden();
		}

		// Get name for new state
		String newName = buf.toString();
		/*
		if (newName.length() > nameLimit)
		    {
		newName = newName.substring(0, nameLimit) + Math.random() + "...";
		}
		*/

		// Create new state
		State newstate = new State(newName);
		// if(i) newstate.setInitial(true);
		if (d) newstate.setAccepting(true);
		if (x) newstate.setForbidden(true);

		return newstate;
	}

	/**
	 * Inform each individual State of which StateSet it belongs to (this one).
	 */
	public void update()
	{
		for (Iterator<State> stateIt = iterator(); stateIt.hasNext(); )
		{
			State currState = stateIt.next();

			currState.setStateSet(this);
		}
	}

	/**
	 * Returns true if at least one state in this equivalence class is marked as 'initial'
	 */
	public boolean hasInitialState()
	{
		for (Iterator stateIt = iterator(); stateIt.hasNext(); )
		{
			State currState = (State) stateIt.next();

			if (currState.isInitial())
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * Returns true if at least one state in this equivalence class is marked as 'accepting'
	 */
	public boolean hasAcceptingState()
	{
		for (Iterator stateIt = iterator(); stateIt.hasNext(); )
		{
			State currState = (State) stateIt.next();

			if (currState.isAccepting())
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * Returns true if at least one state in this equivalence class is marked as 'forbidden'
	 */
	public boolean hasForbiddenState()
	{
		for (Iterator stateIt = iterator(); stateIt.hasNext(); )
		{
			State currState = (State) stateIt.next();

			if (currState.isForbidden())
			{
				return true;
			}
		}

		return false;
	}

	private static class StateSetArcIterator
		implements Iterator<Arc>
	{
		private Iterator<State> stateIterator = null;
		private Iterator<Arc> arcIterator = null;
		private boolean outgoing;

		public StateSetArcIterator(StateSet stateSet, boolean outgoing)
		{
			this.outgoing = outgoing;
			stateIterator = stateSet.iterator();
			
			// Find a state that has at least one outgoing/incoming arcs
			while (stateIterator.hasNext())
			{
				Iterator<Arc> arcIt;
				if (outgoing)
				{
					arcIt = stateIterator.next().outgoingArcsIterator();
				}
				else
				{
					arcIt = stateIterator.next().incomingArcsIterator();
				}
				
				// If there are arcs in this iterator, we're done!
				if (arcIt.hasNext())
				{
					arcIterator = arcIt;
					break;
				}
			}
		}

		public boolean hasNext()
		{
			if (arcIterator == null)
			{
				return false;
			}

			return arcIterator.hasNext();
		}
		
		public Arc next()
			throws NoSuchElementException
		{
			Arc arc = arcIterator.next();
			
			// Jump to the next state?
			if (!arcIterator.hasNext())
			{
				// Find a state that has outgoing arcs
				while (stateIterator.hasNext())
				{
					Iterator<Arc> arcIt;
					if (outgoing)
					{
						arcIt = stateIterator.next().outgoingArcsIterator();
					}
					else
					{
						arcIt = stateIterator.next().incomingArcsIterator();
					}

					// If there are arcs in this iterator, we're done!
					if (arcIt.hasNext())
					{
						arcIterator = arcIt;
						break;
					}
				}
			}

			return arc;
		}
		
		public void remove()
			throws UnsupportedOperationException, IllegalStateException
		{
			throw new UnsupportedOperationException();
		}
	}
}
