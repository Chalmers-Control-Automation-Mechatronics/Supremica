
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
 * Knarrhogsgatan 10
 * SE-431 60 MOLNDAL
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
package org.supremica.automata.algorithms.minimization;

import java.util.*;
import org.supremica.automata.*;

public class MinimizationHeuristic
{
	private static final int MAXIMIZE = 0;
	private static final int MINIMIZE = 1;
	private static final int SPECIAL = 2;

	private static Collection collection = new LinkedList();
	public static final MinimizationHeuristic MostLocal =
		new MinimizationHeuristic("Highest local ratio", true, MAXIMIZE);
	public static final MinimizationHeuristic LeastExtension =
		new MinimizationHeuristic("Least extension of alphabet", true, MINIMIZE);
	public static final MinimizationHeuristic MostStates =
		new MinimizationHeuristic("Most states", true, MAXIMIZE);
	public static final MinimizationHeuristic FewestStates =
		new MinimizationHeuristic("Fewest states", true, MINIMIZE);
	public static final MinimizationHeuristic MostEvents =
		new MinimizationHeuristic("Most events", true, MAXIMIZE);
	public static final MinimizationHeuristic FewestEvents =
		new MinimizationHeuristic("Fewest events", true, MINIMIZE);
	public static final MinimizationHeuristic MostTransitions =
		new MinimizationHeuristic("Most transitions", true, MAXIMIZE);
	public static final MinimizationHeuristic FewestTransitions =
		new MinimizationHeuristic("Fewest transitions", true, MINIMIZE);
	public static final MinimizationHeuristic MostAutomata =
		new MinimizationHeuristic("Most automata", true, MAXIMIZE);
	public static final MinimizationHeuristic FewestAutomata =
		new MinimizationHeuristic("Fewest automata", true, MINIMIZE);
	public static final MinimizationHeuristic Random =
		new MinimizationHeuristic("Random order", true, MAXIMIZE);
	public static final MinimizationHeuristic Undefined =
		new MinimizationHeuristic("Undefined", false, SPECIAL);

	private String description = null;
	private int maximize;

	private MinimizationHeuristic(String description, boolean selectable, int maximize)
	{
		if (selectable)
		{
			collection.add(this);
		}

		this.description = description;
		this.maximize = maximize;
	}

	public static Iterator iterator()
	{
		return collection.iterator();
	}

	public String toString()
	{
		return description;
	}

	public static MinimizationHeuristic toHeuristic(String string)
	{
		for (Iterator it = collection.iterator(); it.hasNext(); )
		{
			MinimizationHeuristic thisOne = (MinimizationHeuristic) it.next();
			if (string.equals(thisOne.toString()))
			{
				return thisOne;
			}
		}

		return Undefined;
	}

	public static Object[] toArray()
	{
		return collection.toArray();
	}

	/**
	 * Return the value of automata in this heuristic.
	 *
	 * @param eventToAutomataMap is a map from all (global) events to all (global) automata.
	 */ 
	public int value(Automata selection, EventToAutomataMap eventToAutomataMap, Alphabet targetAlphabet)
		throws Exception
	{
		if (this == MostLocal)
		{
			Alphabet localEvents = MinimizationHelper.getLocalEvents(selection, eventToAutomataMap);
			localEvents.minus(targetAlphabet);
			int nbrOfLocalEvents = localEvents.size();
			int unionAlphabetSize = selection.getUnionAlphabet().size();
			return (int) (10000 * ((double) nbrOfLocalEvents)/((double) unionAlphabetSize));
		}
		else if (this == LeastExtension)
		{
			int unionAlphabetSize = selection.getUnionAlphabet().size();
			int largestAlphabetSize = 0;
			for (Iterator<Automaton> autIt = selection.iterator(); autIt.hasNext(); )
			{
				int size = autIt.next().getAlphabet().size();
				if (size > largestAlphabetSize)
				{
					largestAlphabetSize = size;
				}
			}
			return (int) (1000 * ((double) unionAlphabetSize)/((double) largestAlphabetSize));
		}
		if (this == MostStates || this == FewestStates)
		{
			// Prod
			int value = 1;
			for (Iterator<Automaton> autIt = selection.iterator(); autIt.hasNext(); )
				value *= autIt.next().nbrOfStates();
			return value;
		}
		else if (this == MostEvents || this == FewestEvents) 
		{
			return selection.getUnionAlphabet().size();
		}
		else if (this == MostTransitions || this == FewestTransitions) 
		{
			// Prod
			int value = 1;
			for (Iterator<Automaton> autIt = selection.iterator(); autIt.hasNext(); )
				value *= autIt.next().nbrOfTransitions();
			return value;
		}
		else if (this == MostAutomata || this == FewestAutomata)
		{
			return selection.size();
		}
		else if (this == Random)
		{
			return (int) (Math.random()*10000.0);
		}
		else
		{
			throw new Exception("Unknown heuristic.");
		}
	}

    /**
     * Maximization criteria?
     */
	public boolean maximize()
	{
		return maximize == MAXIMIZE;
	}

    /**
     * Minimization criteria?
     */
	public boolean minimize()
	{
		return maximize == MINIMIZE;
	}
}
