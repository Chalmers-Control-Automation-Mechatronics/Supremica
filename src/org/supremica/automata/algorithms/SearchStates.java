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
class Matcher
{
	private PatternMatcher matcher;
	private Pattern[] patterns;
	
	Matcher(PatternMatcher m, Pattern[] p)
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

public class SearchStates
{
	private AutomataSynchronizer syncher = null;
	private IntArrayList list = null;
	
	public SearchStates(Automata automata) throws Exception
	{
		syncher = new AutomataSynchronizer(automata, new SynchronizationOptions());
	}
	
	public void search(PatternMatcher pm, Pattern[] ps) throws Exception
	{
		syncher.execute();
		Matcher matcher = new Matcher(pm, ps); 
		list = new IntArrayList();
		for(Iterator it = syncher.getHelper().getStateIterator(); it.hasNext(); )
		{
			int[] composite_state = (int[])it.next();
			if(matcher.matches(getStateIterator(composite_state)))
			{
				list.add(composite_state);
			}
		}
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
