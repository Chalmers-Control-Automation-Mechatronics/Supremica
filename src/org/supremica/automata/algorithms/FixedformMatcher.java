
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
 * Haradsgatan 26A
 * 431 42 Molndal
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

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import org.supremica.automata.algorithms.StateMatcher;
import org.supremica.automata.algorithms.SearchStates;
import org.supremica.automata.State;

public class FixedformMatcher
	implements StateMatcher
{
	private Pattern[] patterns;
	private StateMatcherOptions[] options;

	public FixedformMatcher(Pattern[] patterns, StateMatcherOptions[] options)
	{
		this.patterns = patterns;
		this.options = options;
	}

	public boolean matches(SearchStates.StateIterator it)
	{
		for (int i = 0; it.hasNext(); ++i, it.inc())
		{
			State currState = it.getState();
			Matcher matcher = patterns[i].matcher(currState.getName());

			if (!matcher.matches())
			{
				return false;
			}

			if (!options[i].matches(currState))
			{
				return false;
			}
		}

		return true;
	}
}
