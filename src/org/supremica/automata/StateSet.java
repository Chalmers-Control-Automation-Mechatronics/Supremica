/********************** StateSet.java *************************/
// Implementation of a useful state set.
// Note that a Set implmentation considers two elements e1 and e2 equal
// if e1.equals(e2) == true; Surely this also means that e2.equals(e1)
// must then also be true (but the docs do not require this).
// For State, equals(Object obj) compares id's, so in practice
// StateSet is only well-defined for states of the same automaton.

// StateSet gurantees an ordering defined by the names of the states

package org.supremica.automata;

import java.util.*;
import org.supremica.automata.State;

class StateComparator
	implements Comparator
{
	int compare(State s1, State s2)
	{
		return s1.getName().compareTo(s2.getName());
	}

	public int compare(Object o1, Object o2)
	{
		return compare((State)o1, (State)o2);
	}

}

public class StateSet
{
	private TreeSet theSet = null;

	// Private constructor for cloning
	private StateSet(TreeSet setimpl)
	{
		this.theSet = new TreeSet(setimpl);
	}

	// Create an empty set
	public StateSet()
	{
		theSet = new TreeSet(new StateComparator());
	}

	// Shallow copy (should it be deep?)
	public StateSet(StateSet ss)
	{
		this(ss.theSet);
	}

	public static StateSet union(StateSet s1, StateSet s2)
	{
		StateSet ss = new StateSet(s1);
		ss.union(s2);
		return ss;
	}

	public static StateSet intersect(StateSet s1, StateSet s2)
	{
		StateSet ss = new StateSet(s1);
		ss.intersect(s2);
		return ss;
	}

	// Make me the union of myself and s2
	public void union(StateSet s2)
	{
		theSet.addAll(s2.theSet);
	}

	// Make me the intersection of myself and s2
	public void intersect(StateSet s2)
	{
		theSet.retainAll(s2.theSet);
	}

	public boolean add(State state)
	{
		return theSet.add(state);
	}

	public boolean add(Collection collection)
	{
		return theSet.addAll(collection);
	}
	
	public void clear()
	{
		theSet.clear();
	}

	// Shallow copy (is that what we mean by clone, really?)
	public Object clone()
	{
		return new StateSet(((TreeSet)theSet.clone()));
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

	public boolean remove(State state)
	{
		return theSet.remove(state);
	}

	public int size()
	{
		return theSet.size();
	}

	public boolean equals(StateSet s2)
	{
		if(this == s2)	// avoid testing for self comparison
		{
			return true;
		}
		return theSet.equals(s2.theSet);
	}

	public int hashCode()
	{
		return theSet.hashCode();
	}

	public boolean equals(Object obj)
	{
		StateSet states = (StateSet)obj;
		return equals(states);
	}

	public String toString()
	{
		StringBuffer buf = new StringBuffer();

		buf.append("StateSet[" + size() + "]:{");

		Iterator it = iterator();
		while(it.hasNext())
		{
			State state = (State)it.next();
			buf.append(state.getName());
			buf.append(",");
		}
		buf.append("}");
		return buf.toString();
	}

	/**
	 *	@return an arbitrary, not yet iterated element. Note, assumes that at least one exists
	 */
	public State get()
	{
		return (State)iterator().next();
	}

	/** 
	 *	Creates a new state named as the composition of the states in this set
	 *  Should use the globally defined state separator (and thus, this method 
	 *	should not even be here)
	 */
	public State createNewState()
	{
		// boolean i = false;	// initial?
		boolean d = false;	// desired?
		boolean x = false;	// forbidden?

		StringBuffer buf = new StringBuffer();

		Iterator stateit = iterator();
		while(stateit.hasNext())
		{
			State state = (State)stateit.next();
			buf.append(state.getName());
			buf.append(".");	// STATE_SEPARATOR -- should be globally user definable

			// i |= state.isInitial();
			d |= state.isAccepting();
			x |= state.isForbidden();
		}

		buf.setLength(buf.length()-1);	// truncate last '.'
		// System.out.println("StateSet::createNewState -- " + buf.toString());
		State newstate = new State(buf.toString());

		// if(i) newstate.setInitial(true);
		if(d) newstate.setAccepting(true);
		if(x) newstate.setForbidden(true);

		return newstate;
	}

}
