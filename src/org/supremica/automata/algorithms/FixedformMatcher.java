/*************************** FixedformMatcher.java *******************/

package org.supremica.automata.algorithms;

import org.apache.oro.text.regex.*;
import org.supremica.automata.algorithms.Matcher;
import org.supremica.automata.algorithms.SearchStates;

public class FixedformMatcher implements Matcher
{
	private PatternMatcher matcher;
	private Pattern[] patterns;
	
	public FixedformMatcher(PatternMatcher m, Pattern[] p)
	{
		matcher = m;
		patterns = p;
	}
	
	public boolean matches(SearchStates.StateIterator it)
	{
		for(int i = 0; it.hasNext(); ++i, it.inc())
		{
			if(!matcher.matches(it.getState().getName(), patterns[i]))
				return false;
		}
		return true;
	}
}
	