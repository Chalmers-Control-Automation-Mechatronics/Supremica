
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
import org.supremica.gui.Monitorable;

// 
public class SearchStates
	extends Thread
	implements Monitorable    // Stoppable
{
	private AutomataSynchronizer syncher = null;
	private IntArrayList list = null;
	private Matcher matcher = null;
	private ExecutionDialog exedlg = null;
	private /* volatile */ boolean stopRequested = false;
	private boolean mode = false;    // fals emeans sychronization mode, true is matching mode
	private int progress = 1;
	private String activity = "";
	private int test = 0;

	public void run()    // throws Exception
	{

		// syncher.getHelper().setExecutionDialog(exedlg);
		// exedlg.setMode(ExecutionDialogMode.synchronizing);
		System.out.println("SearchStates::run()");

		while (!stopRequested && (test < 10000))
		{
			test++;
		}

		mode = true;

		while (!stopRequested && (progress < 100))
		{
			try
			{
				progress += (int) (Math.random() * 20);

				sleep(500);
				System.out.println(progress);
			}
			catch (InterruptedException iexcp) {}
		}

		return;

		/*
		 *               syncher.getHelper().setExecutionDialog(exedlg);
		 *               exedlg.setMode(ExecutionDialogMode.synchronizing);
		 *
		 *               try
		 *               {
		 *                       syncher.execute();    // Starts the synch thread and waits for it to stop
		 *               }
		 *               catch (Exception excp)
		 *               {
		 *
		 *                       // How to work this (exception in a worker thread)??
		 *                       return;
		 *               }
		 *               mode = true; // macthing mode
		 *
		 *               if(!stopRequested)
		 *               {
		 *                       // exedlg.setMode(ExecutionDialogMode.matchingStates);
		 *                       int num_total = syncher.getHelper().getNumberOfStates();
		 *                       int num_processed = 0;
		 *                       // Note the difference between the two getStateIterator.
		 *                       // This is AutomataSynchronizerHelper::getStateIterator, returns Iterator...
		 *                       for (Iterator it = syncher.getHelper().getStateIterator(); it.hasNext() &&!stopRequested; )
		 *                       {
		 *                               int[] composite_state = (int[]) it.next();
		 *
		 *                               // and this is SearchStates::getStateIterator, returns SearchStates::StateIterator
		 *                               if (matcher.matches(getStateIterator(composite_state)))
		 *                               {
		 *                                       list.add(composite_state);
		 *                               }
		 *                               progress = ++num_processed / num_total;
		 *                       }
		 *
		 *                       if (stopRequested)
		 *                       {
		 *                               list = new IntArrayList();    // thread stopped - clear the list
		 *                       }
		 *               }
		 */
	}

	// These implement the Monitorable interface
	public int getProgress()
	{
		return progress;
	}

	public String getActivity()
	{
		if (mode)    // progress mode
		{
			return "Matching: " + progress + "% done";
		}
		else         // synching
		{
			return "Synching: " +    // syncher.getHelper().getNumberOfStates() + " states done";
				test + " states done";
		}
	}

	public void stopTask()
	{
		requestStop();
	}

	public ExecutionDialogMode getMode()
	{
		return null;
	}

	// end of Monitorable interace
	public void requestStop()
	{
		System.out.println("Stop requested");

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
