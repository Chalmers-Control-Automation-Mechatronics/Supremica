
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

public class MinimizationStrategy
{
	private enum Type {MAXIMIZE, MINIMIZE, SPECIAL}

	private static Collection collection = new LinkedList();
	public static final MinimizationStrategy AtLeastOneLocal =
		new MinimizationStrategy("At least one local", true, Type.SPECIAL);
	public static final MinimizationStrategy AtLeastOneLocalMaxThree =
		new MinimizationStrategy("At least one local, max three", true, Type.SPECIAL);
	public static final MinimizationStrategy FewestTransitionsFirst =
		new MinimizationStrategy("Pair with fewest transition automaton", true, Type.MINIMIZE);
	public static final MinimizationStrategy FewestStatesFirst =
		new MinimizationStrategy("Pair with fewest states automaton", true, Type.MINIMIZE);
	public static final MinimizationStrategy FewestEventsFirst =
		new MinimizationStrategy("Pair with fewest events automaton", true, Type.MINIMIZE);
	public static final MinimizationStrategy MostTransitionsFirst =
		new MinimizationStrategy("Pair with most transitions automaton", true, Type.MAXIMIZE);
	public static final MinimizationStrategy MostStatesFirst =
		new MinimizationStrategy("Pair with most states automaton", true, Type.MAXIMIZE);
	public static final MinimizationStrategy MostEventsFirst =
		new MinimizationStrategy("Pair with most events automaton", true, Type.MAXIMIZE);
	public static final MinimizationStrategy RandomFirst =
		new MinimizationStrategy("Pair with random automaton", true, Type.MAXIMIZE);
	public static final MinimizationStrategy ExperimentalMin =
		new MinimizationStrategy("Experimental min", true, Type.MINIMIZE);
	public static final MinimizationStrategy ExperimentalMax =
		new MinimizationStrategy("Experimental max", true, Type.MAXIMIZE);
	public static final MinimizationStrategy Undefined =
		new MinimizationStrategy("Undefined", false, Type.SPECIAL);

	private String description = null;
	private Type type;

	private MinimizationStrategy(String description, boolean selectable, Type type)
	{
		if (selectable)
		{
			collection.add(this);
		}

		this.description = description;
		this.type = type;
	}

	public static Iterator iterator()
	{
		return collection.iterator();
	}

	public String toString()
	{
		return description;
	}

	public static MinimizationStrategy toStrategy(String string)
	{
		for (Iterator it = collection.iterator(); it.hasNext(); )
		{
			MinimizationStrategy thisOne = (MinimizationStrategy) it.next();
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
	 * Return the value of automata in this strategy.
	 */ 
	public int value(Automaton aut)
		throws Exception
	{
		if (this == MostStatesFirst || this == FewestStatesFirst)
			return aut.nbrOfStates();
		else if (this == MostTransitionsFirst || this == FewestTransitionsFirst) 
			return aut.nbrOfTransitions();
		else if (this == MostEventsFirst || this == FewestEventsFirst)
			return aut.nbrOfEvents();
		else if (this == ExperimentalMax || this == ExperimentalMin)
			return aut.nbrOfTransitions() + 1*aut.nbrOfEpsilonTransitions();
		else if (this == RandomFirst)
			return (int) (Math.random()*10000.0);
		else 
			throw new Exception("Unknown strategy.");
	}

    /**
     * Maximization criteria?
     */
	public boolean maximize()
	{
		return type == Type.MAXIMIZE;
	}

    /**
     * Minimization criteria?
     */
	public boolean minimize()
	{
		return type == Type.MINIMIZE;
	}

    /**
     * Special strategy? Does not return values.
     */
	public boolean isSpecial()
	{
		return type == Type.SPECIAL;
	}
}
