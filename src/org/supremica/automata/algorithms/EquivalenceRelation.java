
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
package org.supremica.automata.algorithms;

import java.util.*;

public class EquivalenceRelation
{
	private static List collection = new LinkedList();
	/** Language equivalence. */
	public static final EquivalenceRelation LanguageEquivalence =
		new EquivalenceRelation("Language equivalence", true);
	/** Conflict equivalence. */
	public static final EquivalenceRelation ConflictEquivalence =
		new EquivalenceRelation("Conflict equivalence", true);
	/** Supervision equivalence. */
	public static final EquivalenceRelation SupervisionEquivalence =
		new EquivalenceRelation("Supervision equivalence", true);
	/** Observation equivalence (aka Weak bisimulation equivalence). */
	public static final EquivalenceRelation ObservationEquivalence =
		new EquivalenceRelation("Observation equivalence", true);
	/** Bisimulation equivalence (aka Strong bisimulation equivalence). */
	public static final EquivalenceRelation BisimulationEquivalence =
		new EquivalenceRelation("Bisimulation equivalence", true);
	/** Failures equivalence. */
	public static final EquivalenceRelation FailureEquivalence =
		new EquivalenceRelation("Failure equivalence", false);
	/** Undefined equivalence relation. */
	public static final EquivalenceRelation Undefined =
		new EquivalenceRelation("Undefined", false);

	/** For minimization using the conflict equivalence minimization rules only. */
	public static final EquivalenceRelation ConflictEquivalenceMinimizationRules =
		new EquivalenceRelation("Conflict equivalence minimization rules", false);

	/**	The name of the equivalence relation. */
	private String identifier;

	private EquivalenceRelation(String identifier, boolean add)
	{
		if (add)
		{
			collection.add(this);
		}

		this.identifier = identifier;
	}

	public static Iterator iterator()
	{
		return collection.iterator();
	}

	public String toString()
	{
		return identifier;
	}

	public static EquivalenceRelation toType(String string)
	{
		for (Iterator it = collection.iterator(); it.hasNext(); )
		{
			EquivalenceRelation thisOne = (EquivalenceRelation) it.next();
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
}
