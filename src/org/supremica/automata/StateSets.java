package org.supremica.automata;

import java.util.*;

public class StateSets
{
	private LinkedList stateSets = new LinkedList();

	public void add(StateSet set)
	{
		stateSets.add(set);
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
	 * Returns the first StateSet in this StateSets.
	 */
	public StateSet getFirst()
	{
		StateSet set = (StateSet) stateSets.getFirst();
		return set;
	}
	
	/**
	 * Removes stateSet from this StateSets.
	 */
	public void remove(StateSet stateSet)
	{
		stateSets.remove(stateSet);
	}

	public Iterator iterator()
	{
		return stateSets.iterator();
	}

	public Iterator safeIterator()
	{
		return ((LinkedList) stateSets.clone()).iterator();
	}

	public void clear()
	{
		stateSets.clear();
	}

	public int size()
	{
		return stateSets.size();
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
}
