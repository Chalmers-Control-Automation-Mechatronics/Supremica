
/********************** StateSet.java *************************/

// Implementation of a useful state set.
// Note that a Set implementation considers two elements e1 and e2 equal
// if e1.equals(e2) == true; Surely this also means that e2.equals(e1)
// must then also be true (but the docs do not require this).
// For State, equals(Object obj) compares id's, so in practice
// StateSet is only well-defined for states of the same automaton.
// StateSet gurantees an ordering defined by the names of the states
package org.supremica.automata;

import java.util.*;
import org.supremica.properties.SupremicaProperties;

public class StateSet
{
	private TreeSet theSet = null;
	private HashMap nameToStateMap = null;
	private State singleStateRepresentation = null;

	// Private constructor for cloning
	private StateSet(TreeSet setimpl)
	{
		this.theSet = new TreeSet(setimpl);
	}

	// Create an empty set
	public StateSet()
	{
		theSet = new TreeSet(new State.StateComparator());
	}

	/**
	 * Create a StateSet containing all the states of an automaton
	 */
	public StateSet(Automaton aut)
	{
		this();

		for (StateIterator stateIt = aut.stateIterator(); stateIt.hasNext(); )
		{
			this.add(stateIt.nextState());
		}
	}

	// Shallow copy (should it be deep?)
	public StateSet(StateSet ss)
	{
		this(ss.theSet);
	}

	/**
	 * Find the union of the two StateSet:s s1 and s2, returning a new StateSet.
	 */
	public static StateSet union(StateSet s1, StateSet s2)
	{
		StateSet ss = new StateSet(s1);

		ss.union(s2);

		return ss;
	}

	/**
	 * Intersect the two StateSet:s s1 and s2, returning a new StateSet.
	 */
	public static StateSet intersect(StateSet s1, StateSet s2)
	{
		StateSet ss = new StateSet(s1);

		ss.intersect(s2);

		return ss;
	}

	/**
	 * Make me the union of myself and s2.
	 */
	public void union(StateSet s2)
	{
		modified();
		theSet.addAll(s2.theSet);
	}

	/**
	 * Make me the intersection of myself and s2.
	 */
	public void intersect(StateSet s2)
	{
		modified();
		theSet.retainAll(s2.theSet);
	}

	public boolean add(State state)
	{
		modified();
		return theSet.add(state);
	}

	public boolean add(StateSet stateSet)
	{
		modified();
		return theSet.addAll(stateSet.theSet);
	}

	public boolean add(Collection collection)
	{
		modified();
		return theSet.addAll(collection);
	}

	public void clear()
	{
		modified();
		theSet.clear();
	}

	// Shallow copy (is that what we mean by clone, really?)
	public Object clone()
	{
		return new StateSet(((TreeSet) theSet.clone()));
	}

	public boolean contains(State state)
	{
		return theSet.contains(state);
	}

	public boolean isEmpty()
	{
		return theSet.isEmpty();
	}

	public StateIterator iterator()
	{
		return new StateIterator(theSet.iterator());
	}

	public ArcIterator outgoingArcsIterator()
	{
		return new ArcIterator(new StateSetArcIterator(this, true));
	}

	public ArcIterator incomingArcsIterator()
	{
		return new ArcIterator(new StateSetArcIterator(this, false));
	}

	public boolean remove(State state)
	{
		modified();
		return theSet.remove(state);
	}

	public void remove(StateSet set)
	{
		modified();
		for (StateIterator it = set.iterator(); it.hasNext(); )
		{
			theSet.remove(it.nextState());
		}
	}

	/**
	 * Removes and returns an arbitrary state from the set.
	 */
	public State remove()
	{
		State state = get();
		remove(state);
		return state;
	}

	public int size()
	{
		return theSet.size();
	}

	public boolean equals(StateSet s2)
	{
		if (this == s2)    // avoid testing for self comparison
		{
			return true;
		}

		return theSet.equals(s2.theSet);
	}

	public boolean equals(Object obj)
	{
		StateSet states = (StateSet) obj;

		return equals(states);
	}

	public int hashCode()
	{
		return theSet.hashCode();
	}

	public String toString()
	{
		StringBuffer buf = new StringBuffer();

		buf.append("StateSet[" + size() + "]: {");

		Iterator it = iterator();

		while (it.hasNext())
		{
			State state = (State) it.next();

			//buf.append(state.getName());
			buf.append(state);
			if (it.hasNext())
				buf.append(",");
		}

		buf.append("}");

		return buf.toString();
	}

	/**
	 * Returns the state set that can reach some state in the current state set in a transition
	 * associated with event.
	 */
	public StateSet previousStates(LabeledEvent event)
	{
		StateSet prevStates = new StateSet();

		for (StateIterator stateIt = iterator(); stateIt.hasNext(); )
		{
			for (StateIterator prevIt = stateIt.nextState().previousStateIterator(event); 
				 prevIt.hasNext(); )
			{
				prevStates.add(prevIt.nextState());
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
		for (StateIterator stateIt = iterator(); stateIt.hasNext(); )
		{
			State state = stateIt.nextState();
			nextStates.add(state.nextStates(event, considerEpsilonClosure));
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
			result.add(this);
		}

		// Examine states 
		StateSet statesToExamine = new StateSet();
		statesToExamine.add(this);
		while (statesToExamine.size() != 0)
		{
			State currState = (State) statesToExamine.remove();

			for (ArcIterator arcIt = currState.outgoingArcsIterator(); arcIt.hasNext(); )
			{
				Arc currArc = arcIt.nextArc();
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
	 * @return an arbitrary, not yet iterated element. Note, assumes that at least one exists.
	 */
	public State get()
	{
		return (State) iterator().next();
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
		StateIterator stateIt = iterator();

		while (stateIt.hasNext())
		{
			State currState = stateIt.nextState();

			currState.setStateSet(this);
		}
	}

	/**
	 * Returns true if at least one state in this equivalence class is marked as 'initial'
	 */
	public boolean hasInitialState()
	{
		Iterator stateIt = iterator();

		while (stateIt.hasNext())
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
		Iterator stateIt = iterator();

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

	/**
	 * Returns true if at least one state in this equivalence class is marked as 'forbidden'
	 */
	public boolean hasForbiddenState()
	{
		Iterator stateIt = iterator();

		while (stateIt.hasNext())
		{
			State currState = (State) stateIt.next();

			if (currState.isForbidden())
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * When this StateSet is modified, it will have a new singleStateRepresentation.
	 */
	private void modified()
	{
		singleStateRepresentation = null;
	}

	private class StateSetArcIterator
		implements Iterator
	{
		private StateIterator stateIterator = null;
		private ArcIterator arcIterator = null;
		private boolean outgoing;

		public StateSetArcIterator(StateSet stateSet, boolean outgoing)
		{
			this.outgoing = outgoing;
			stateIterator = stateSet.iterator();
			
			// Find a state that has at least one outgoing/incoming arcs
			while (stateIterator.hasNext())
			{
				ArcIterator arcIt;
				if (outgoing)
				{
					arcIt = stateIterator.nextState().outgoingArcsIterator();
				}
				else
				{
					arcIt = stateIterator.nextState().incomingArcsIterator();
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
		
		public Object next()
			throws NoSuchElementException
		{
			return nextArc();
		}
		
		public Arc nextArc()
			throws NoSuchElementException
		{
			Arc arc = arcIterator.nextArc();
			
			// Jump to the next state?
			if (!arcIterator.hasNext())
			{
				// Find a state that has outgoing arcs
				while (stateIterator.hasNext())
				{
					ArcIterator arcIt;
					if (outgoing)
					{
						arcIt = stateIterator.nextState().outgoingArcsIterator();
					}
					else
					{
						arcIt = stateIterator.nextState().incomingArcsIterator();
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
