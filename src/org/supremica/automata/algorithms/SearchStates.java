//***************** SearchStates.java *********************//
/* Given an Automata object and a Matcher object, online
 * synch the automata and save the matching states
 *
 * Is it useful to first search each automaton for states
 * matching that automatons pattern? At least, if some 
 * automaton has no states matching its pattern, then no
 * global match exists.
 */
package org.supremica.automata.algorithms;

import java.lang.Exception;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.oro.text.regex.*;

import org.supremica.automata.*;
import org.supremica.automata.algorithms.*;
import org.supremica.util.*;

// Really a tester class
interface Matcher
{
	public boolean matches(SearchStates.StateIterator it);
}

class PartsMatcher implements Matcher
{
	private PatternMatcher matcher;
	private Pattern[] patterns;
	
	PartsMatcher(PatternMatcher m, Pattern[] p)
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
// 
class FreeformMatcher implements Matcher
{
	private PatternMatcher matcher;
	private Pattern pattern;
	
	FreeformMatcher(PatternMatcher m, Pattern p)
	{
		matcher = m;
		pattern = p;
	}
	
	public boolean matches(SearchStates.StateIterator it)
	{
		// Make up a global name
		StringBuffer state_name = new StringBuffer();
		
		while(it.hasNext())
		{
			state_name.append(it.getState().getName());
			it.inc();
			if(it.hasNext())	// should the user have control over how this string is built?
				state_name.append(','); 

		}
		return matcher.matches(state_name.toString(), pattern);
	}
}

// 
public class SearchStates
{
	private AutomataSynchronizer syncher = null;
	private IntArrayList list = null;
	
	private void search(Matcher matcher) throws Exception
	{
		syncher.execute();
		list = new IntArrayList();
		// Note the difference between the two getStateIterator. 
		// This is AutomataSynchronizerHelper::getStateIterator, returns Iterator...
		for(Iterator it = syncher.getHelper().getStateIterator(); it.hasNext(); )
		{
			int[] composite_state = (int[])it.next();
			// and this is SearchStates::getStateIterator, returns SearchStates::StateIterator
			if(matcher.matches(getStateIterator(composite_state)))
			{
				list.add(composite_state);
			}
		}
	}
	
	public SearchStates(Automata automata) throws Exception
	{
		//!!Throws exception if automata is empty or has only one automaton!!
		syncher = new AutomataSynchronizer(automata, new SynchronizationOptions());
	}
	
	// Search based on a pattern for each automaton
	public void search(PatternMatcher pm, Pattern[] ps) throws Exception
	{
		search(new PartsMatcher(pm, ps)); 
	}
	// Search based on a freeform pattern for the global states
	public void search(PatternMatcher pm, Pattern p) throws Exception
	{
		search(new FreeformMatcher(pm, p));
	}
	
	public int numberFound()
	{
		return list.size();
	}
	
	// To iterate over the matched states
	public Iterator iterator()
	{
		return list.iterator();
	}
	
	// iterates over the partial states
	public class StateIterator 
	{
		private State[][] states;
		private int[] composite;
		int index; // holds the automaton index
		
		private StateIterator(State[][] s, int[] c)
		{
			states = s;
			composite = c;
			index = 0;
		}
		public boolean hasNext()
		{
			return index < composite.length - 1; // the last element of composite is not used
		}
		public State getState() // get the current state of the current automaton
		{
			return states[index][composite[index]];
		}
		public void inc() // move to the next automaton
		{
			++index;
		}
	}	
	
	public StateIterator getStateIterator(int[] composite_state)
	{
		return new StateIterator(syncher.getHelper().getIndexFormStateTable(), composite_state);

	}
	public String toString(int[] composite_state)
	{
		AutomataSynchronizerHelper helper = syncher.getHelper();
		State[][] states = helper.getIndexFormStateTable();
		
		StringBuffer str = new StringBuffer();
		
		for(int i = 0; i < states.length; ++i)
		{
			str.append(states[i][composite_state[i]].getName());
		}
		
		return new String(str);
	}
}
