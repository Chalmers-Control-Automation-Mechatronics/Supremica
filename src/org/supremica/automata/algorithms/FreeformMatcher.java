
/************************ FreeformMatcher.java ***************************/
package org.supremica.automata.algorithms;

import org.apache.oro.text.regex.*;
import org.supremica.automata.algorithms.Matcher;
import org.supremica.automata.algorithms.SearchStates;

public class FreeformMatcher
	implements Matcher
{
	private PatternMatcher matcher;
	private Pattern pattern;
	private String state_sep;

	public FreeformMatcher(PatternMatcher m, Pattern p, String s)
	{
		matcher = m;
		pattern = p;
		state_sep = s;

		// dbg: System.err.println("FreeformMatcher::constructing");
	}

	public boolean matches(SearchStates.StateIterator it)
	{

		// Make up a global name
		StringBuffer state_name = new StringBuffer();

		while (it.hasNext())
		{
			state_name.append(it.getState().getName());
			it.inc();

			if (it.hasNext())    // the user now has control over how this string is built
			{
				state_name.append(state_sep);
			}
		}

		/*
		 * Debug stuff
		 * boolean result = matcher.matches(state_name.toString(), pattern);
		 * System.err.println("FreeformMatcher::matching(\"" + state_name.toString() + "\"," + pattern.getPattern() + ") = " + new Boolean(result).toString());
		 * return result;
		 */
		return matcher.matches(state_name.toString(), pattern);
	}
}
