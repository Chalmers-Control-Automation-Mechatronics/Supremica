
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

/**	MF fix issue #138 (Feb 2022)
 *	To make the abbreviation visible in the dropdown list, add toStringLong()
 *	which adds the abbreviation to the description (if they are different)
 *	Set toString() to call toStringLong() to get the strings to fill in the dropdown
**/
public enum MinimizationSelectingHeuristic
{
    MostLocal("Highest local ratio", "MaxL"),
    MostCommon("Highest common ratio", "MaxC"),
    MinimumStates("Minimum estimated number of states", "MinS"),
    SmallestAlphabetExtension("Smallest alphabet extension", "MinE"),
    MinimumFrontier("Minimum frontier", "MinF"),
    MinimumActualStates("Minimum actual number of states", "MinSync");

    /** Textual description. */
    private final String description;
    /** Textual description abbreviated. */
    private final String abbreviation;

    private MinimizationSelectingHeuristic(final String description)
    {
		this(description, description);
    }

    private MinimizationSelectingHeuristic(final String description, final String abbreviation)
    {
        this.description = description;
		this.abbreviation = abbreviation;
    }


    public String toStringDescription()
    {
        return description;
    }

    public String toStringAbbreviated()
    {
        return abbreviation;
    }

	// Duplicated code here, see MinimizationStrategy.java, MinimizationHeuristic.java,
	// MinimizationPreselectingHeuristic.java, MinimizationSelectingHeuristic.java
	// The common parts should be merged into one.
	public String toStringLong()
	{
		if (description != abbreviation)
		{
			// return description + "(" + abbreviation + ")";
			return "(" + abbreviation + ") " + description;
		}
		// If description and abbreviation are the same string, return just that
		return toStringDescription();
	}

    @Override
    public String toString()
    {
        return toStringLong();
    }

    public static MinimizationSelectingHeuristic toStrategy(final String description)
    {
        for (final MinimizationSelectingHeuristic strategy: values())
        {
            if (strategy.description.equals(description))
            {
                return strategy;
            }
        }
        return null;
    }
}
