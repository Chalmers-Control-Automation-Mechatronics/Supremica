/**************** StateSet.java *********************/
// Implementation of a useful state set. 
// Note that a Set implmentation considers two elements e1 and e2 equal
// if e1.equals(e2) == true; Surely this also means that e2.equals(e1)
// must then also be true (but the docs do not require this).
// For State, equals(Object obj) compares id's, so in practice
// StateSet is only well-defined for states of the same automaton.

package org.supremica.automata;

import java.util.*;
import org.supremica.automata.State;


public class StateSet
//	extends AbstractSet
{
	private HashSet theSet = null;

	// Private constructor for cloning
	private StateSet(HashSet hashset)
	{
		this.theSet = new HashSet(hashset);
	}
	
	// Create an empty set
	public StateSet()
	{
		theSet = new HashSet();
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
		/* Iterate over self and remove all that're not in s2
		Iterator it = iterator();
		while(it.hasNext())
		{
			State state = (State)it.next();
			if(!s2.contains(state))
				remove(state);
		}*/
		theSet.retainAll(s2.theSet);
	}

	public boolean add(State state)
	{
		return theSet.add(state);
	}

	public void clear()
	{
		theSet.clear();
	}

	// Shallow copy (is that what we mean by clone, really?)
	public Object clone()
	{
		return new StateSet(((HashSet)theSet.clone()));
	}

	public boolean contains(State state)
	{
		return theSet.contains(state);
	}

	public boolean isEmpty()
	{
		return theSet.isEmpty();
	}

	public Iterator iterator()
	{
		return theSet.iterator();
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
			// System.out.println("Self comparison " + toString());
			return true;
		}
		// System.out.println(toString() + " equals " + s2.toString() + " == " + (theSet.equals(s2.theSet) ? "true" : "false"));
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
	
	// Return an arbitrary element. Note, assumes that at least one exists
	public State get()
	{
		return (State)iterator().next();
	}
	
	// Create a new state named as the composition of the states in this set
	// Should use the globally defined state separator (and thus, this method should not even be here)
	public State createNewState()
	{
		boolean i = false;	// initial?
		boolean d = false;	// desired?
		boolean x = false;	// forbidden?
		
		StringBuffer buf = new StringBuffer();

		Iterator stateit = iterator();
		while(stateit.hasNext())
		{
			State state = (State)stateit.next();
			buf.append(state.getName());
			buf.append(".");	// STATE_SEPARATOR -- should be globally user definable
			
			i |= state.isInitial();
			d |= state.isAccepting();
			x |= state.isForbidden();
		}

		buf.setLength(buf.length()-1);	// truncate last '.'
		System.out.println("StateSet::createNewState -- " + buf.toString());
		State newstate = new State(buf.toString());
		
		if(i) newstate.setInitial(true);
		if(d) newstate.setAccepting(true);
		if(x) newstate.setForbidden(true);
		
		return newstate;
	}
	
	public static void main(String[] args)
	{
		State q0 = new State("q0"); // id and name, set to the same
		State q1 = new State("q1");
		State q2 = new State("q2");
		State q3 = new State("q3");
		
		StateSet oneset = new StateSet();
		oneset.add(q0);
		oneset.add(q1);
		// oneset.add(q2);
		// oneset.add(q3);
		
		StateSet twoset = new StateSet();
		twoset.add(q0);
		twoset.add(q1);
		// twoset.add(q2);
		// twoset.add(q3);
		
		System.out.println("oneset = " + oneset.toString());
		System.out.println("twoset = " + twoset.toString());
		
		System.out.println("[oneset == oneset] == " +  (oneset == oneset ? "true" : "false"));
		System.out.println("oneset.equals(oneset) == " + (oneset.equals(oneset) ? "true" : "false"));
		System.out.println("[oneset == twoset] == " +  (oneset == twoset ? "true" : "false"));
		System.out.println("oneset.equals(twoset) == " + (oneset.equals(twoset) ? "true" : "false")); // false here with default equals!
	}

}