/********************** SetOfStateSets.java ******************/
// Makes a determinization through the subset construction method
// See vanNoord (2000), Fig 1
// There are two ways of calling Determinizer depending on how the epsilons should be specified
// With no 'epsilons' alphabet, each event is queried whether it isEpsilon or not
// With an 'epsilons' alphabet, the events of 'epsilons' are considered to be...well...epsilons

package org.supremica.automata.algorithms.standard;

import java.util.HashSet;
import java.util.Iterator;

import org.supremica.automata.*;

// Would this have any public use?
public class SetOfStateSets
{
	private HashSet theSet = null;

	// Private constructor for cloning
	private SetOfStateSets(HashSet hashset)
	{
		this.theSet = new HashSet(hashset);
	}

	// Create an empty set
	public SetOfStateSets()
	{
		theSet = new HashSet();
	}

	// Shallow copy (should it be deep?)
	public SetOfStateSets(SetOfStateSets ss)
	{
		this(ss.theSet);
	}

	public static SetOfStateSets union(SetOfStateSets s1, SetOfStateSets s2)
	{
		SetOfStateSets ss = new SetOfStateSets(s1);
		ss.union(s2);
		return ss;
	}

	public static SetOfStateSets intersect(SetOfStateSets s1, SetOfStateSets s2)
	{
		SetOfStateSets ss = new SetOfStateSets(s1);
		ss.intersect(s2);
		return ss;
	}

	// Make me the union of myself and s2
	public void union(SetOfStateSets s2)
	{
		theSet.addAll(s2.theSet);
	}

	// Make me the intersection of myself and s2
	public void intersect(SetOfStateSets s2)
	{
		theSet.retainAll(s2.theSet);
	}

	public boolean add(StateSet states)
	{
		return theSet.add(states);
	}

	public void clear()
	{
		theSet.clear();
	}

	// Shallow copy (is that what we mean by clone, really?)
	public Object clone()
	{
		return new SetOfStateSets(((HashSet)theSet.clone()));
	}

	public boolean contains(StateSet states)
	{
		return theSet.contains(states);
	}

	public boolean isEmpty()
	{
		return theSet.isEmpty();
	}

	public Iterator iterator()
	{
		return theSet.iterator();
	}

	public boolean remove(StateSet states)
	{
		return theSet.remove(states);
	}

	public int size()
	{
		return theSet.size();
	}

	// Return an arbitrary element. Note, assumes that at least one exists
	public StateSet get()
	{
		return (StateSet)iterator().next();
	}
}