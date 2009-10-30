package org.supremica.automata;

import java.util.*;

public class StateSets
	extends HashSet<StateSet>
{
    private static final long serialVersionUID = 1L;

    public StateSets()
	{
		super(); 
	}

	public StateSets(Collection<? extends StateSet> collection)
	{
		super(collection);
	}

	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("(");
		for (StateSet set : this)
		{
			sb.append(set);			
		}
		sb.append(")");

		return sb.toString();
	}

	///////////////
	// EXTENSION //
	///////////////

	public void update()
	{
		for (StateSet set : this)
		{
			set.update();
		}
	}

	/**
	 * Return an arbitrary element. 
	 */
	public StateSet get()
	{
		if (iterator().hasNext())
			return iterator().next();
		else
			return null;
	}

	public static StateSets union(StateSets s1, StateSets s2)
	{
		StateSets ss = new StateSets(s1);

		ss.addAll(s2);

		return ss;
	}

	public static StateSets intersect(StateSets s1, StateSets s2)
	{
		StateSets ss = new StateSets(s1);

		ss.retainAll(s2);

		return ss;
	}
}
