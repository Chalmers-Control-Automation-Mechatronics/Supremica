
// ***************** SearchStates.java *********************//

/*
 *  Given an Automata object and a Matcher object, online
 *  synch the automata and save the matching states
 *
 *  Is it useful to first search each automaton for states
 *  matching that automatons pattern? At least, if some
 *  automaton has no states matching its pattern, then no
 *  global match exists.
 */
package org.supremica.automata.algorithms;

import java.lang.Exception;
import java.util.HashSet;
import java.util.Iterator;
import org.apache.oro.text.regex.*;
import org.supremica.util.*;
import org.supremica.automata.Automata;
import org.supremica.automata.State;
import org.supremica.gui.ExecutionDialog;
import org.supremica.gui.ExecutionDialogMode;

// 
public class SearchStates
	extends Thread
	implements Stoppable
{
	private AutomataSynchronizer syncher = null;
	private IntArrayList list = null;
	private Matcher matcher = null;
	private ExecutionDialog exedlg = null;
	private /* volatile */ boolean stopRequested = false;

	public void run()    // throws Exception
	{		
		exedlg.setMode(ExecutionDialogMode.synchronizing);
		// java.awt.EventQueue.invokeLater(new Runnable()
		//	{
		//		public void run()
		//		{
		syncher.getHelper().setExecutionDialog(exedlg);
		//		}
		//	});
		
		try
		{
			syncher.execute();    // Starts the synch thread and waits for it to stop
		}
		catch (Exception excp)
		{

			// How to work this (exception in a worker thread)??
			return;
		}

		if (!stopRequested)
		{
			exedlg.setMode(ExecutionDialogMode.matchingStates);

			// Note the difference between the two getStateIterator.
			// This is AutomataSynchronizerHelper::getStateIterator, returns Iterator...
			for (Iterator it = syncher.getHelper().getStateIterator(); it.hasNext() &&!stopRequested; )
			{
				int[] composite_state = (int[]) it.next();

				// and this is SearchStates::getStateIterator, returns SearchStates::StateIterator
				if (matcher.matches(getStateIterator(composite_state)))
				{
					list.add(composite_state);
				}
			}

			if (stopRequested)
			{
				list = new IntArrayList();    // thread stopped - clear the list
			}
		}

		exedlg.setMode(ExecutionDialogMode.hide);
	}

	public void requestStop()
	{
		stopRequested = true;

		syncher.requestStop();
	}

	public boolean wasStopped()
	{
		return stopRequested;
	}

	public SearchStates(Automata automata, Matcher m)
		throws Exception
	{

		// !!Throws exception if automata is empty or has only one automaton!!
		this.syncher = new AutomataSynchronizer(automata, new SynchronizationOptions());
		this.matcher = m;
		this.list = new IntArrayList();    // Must create the list, in case the thread is stopped
	}

	// -- MF -- Not ideal here but we have a circular ref otherwise
	public void setExecutionDialog(ExecutionDialog exedlg)
	{
		this.exedlg = exedlg;
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
		int index;

		// holds the automaton index
		// ** Note, ctor should be private, but jikes 1.15 emits faulty bytecode then
		// ** javac and jikes 1.14 ok for private.
		// ** Do not instantiate, create only through getStateIterator()
		public StateIterator(State[][] s, int[] c)
		{
			states = s;
			composite = c;
			index = 0;
		}

		public boolean hasNext()
		{
			return index < composite.length - 1;

			// the last element of composite is not used
		}

		public State getState()
		{

			// get the current state of the current automaton
			return states[index][composite[index]];
		}

		public void inc()
		{

			// move to the next automaton
			++index;
		}
	}

	public StateIterator getStateIterator(int[] composite_state)
	{

		// 
		State[][] states = syncher.getHelper().getIndexFormStateTable();

		// 
		return new StateIterator(states, composite_state);
	}

	public String toString(int[] composite_state)
	{
		AutomataSynchronizerHelper helper = syncher.getHelper();
		State[][] states = helper.getIndexFormStateTable();
		StringBuffer str = new StringBuffer();

		for (int i = 0; i < states.length; ++i)
		{
			str.append(states[i][composite_state[i]].getName());
		}

		return new String(str);
	}
}
