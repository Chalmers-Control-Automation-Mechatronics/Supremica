package org.supremica.automata;

import java.util.*;

public class StateSets
{
	private HashSet stateSets;

	// Private constructor for cloning
	private StateSets(HashSet hashset)
	{
		stateSets = new HashSet(hashset);
	}

	public StateSets()
	{
		stateSets = new HashSet();
	}

	// Shallow copy (should it be deep?)
	public StateSets(StateSets ss)
	{
		this(ss.stateSets);
	}

	public boolean add(StateSet set)
	{
		return stateSets.add(set);
	}

	public void add(StateSets sets)
	{
		Iterator setIt = sets.iterator();

		while (setIt.hasNext())
		{
			StateSet stateSet = (StateSet) setIt.next();

			stateSet.update();
			stateSets.add(stateSet);
		}
	}

	/**
	 * Removes stateSet from this StateSets.
	 */
	public void remove(StateSet stateSet)
	{
		stateSets.remove(stateSet);
	}

	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		Iterator stateSetIt = stateSets.iterator();

		sb.append("(");

		while (stateSetIt.hasNext())
		{
			StateSet stateSet = (StateSet) stateSetIt.next();

			sb.append(stateSet);
		}

		sb.append(")");

		return sb.toString();
	}

	public void update()
	{
		for (Iterator setIt = iterator(); setIt.hasNext(); )
		{
			((StateSet) setIt.next()).update();
		}
	}

	public static StateSets union(StateSets s1, StateSets s2)
	{
		StateSets ss = new StateSets(s1);

		ss.union(s2);

		return ss;
	}

	public static StateSets intersect(StateSets s1, StateSets s2)
	{
		StateSets ss = new StateSets(s1);

		ss.intersect(s2);

		return ss;
	}

	// Make me the union of myself and s2
	public void union(StateSets s2)
	{
		stateSets.addAll(s2.stateSets);
	}

	// Make me the intersection of myself and s2
	public void intersect(StateSets s2)
	{
		stateSets.retainAll(s2.stateSets);
	}

	public void clear()
	{
		stateSets.clear();
	}

	/*
	// Shallow copy (is that what we mean by clone, really?)
	public Object clone()
	{
		return new StateSets(((HashSet) stateSets.clone()));
	}
	*/

	public boolean contains(StateSet states)
	{
		return stateSets.contains(states);
	}

	public boolean isEmpty()
	{
		return stateSets.isEmpty();
	}

	public Iterator iterator()
	{
		return stateSets.iterator();
	}

	public Object[] toArray()
	{
		return stateSets.toArray();
	}

	public int size()
	{
		return stateSets.size();
	}

	// Return an arbitrary element. Note, assumes that at least one exists
	public StateSet get()
	{
		return (StateSet) iterator().next();
	}
}
