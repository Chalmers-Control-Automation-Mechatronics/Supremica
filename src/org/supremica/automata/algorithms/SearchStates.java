
/* ***************** SearchStates.java *********************
 *
 *  Given an Automata object and a Matcher object, online
 *  synch the automata and save the matching states
 *
 *  Is it useful to first search each automaton for states
 *  matching that automatons pattern? At least, if some
 *  automaton has no states matching its pattern, then no
 *  global match exists.
 */

//-- owner: MF

package org.supremica.automata.algorithms;

import java.lang.Exception;
import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.*;

import org.supremica.log.*;

import org.supremica.util.IntArrayList;
import org.supremica.util.IntArrayVector;
import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.State;
import org.supremica.gui.MonitorableThread;

public class SearchStates
	extends MonitorableThread
{
	private static Logger logger = LoggerFactory.createLogger(SearchStates.class);

	private AutomataSynchronizer syncher = null;
	private IntArrayVector container = null;
	private StateMatcher matcher = null;
	protected /* volatile */ boolean stopRequested = false;
	protected boolean mode = false;    // false means sychronization mode, true is matching mode
	protected int progress = 1;

	private IntArrayVector makeContainer()
	{
		return new IntArrayVector();
	}

	public SearchStates(Automata automata, StateMatcher m)
		throws Exception
	{
		setPriority(Thread.MIN_PRIORITY);

		// !!Throws exception if automata is empty or has only one automaton!!
		SynchronizationOptions syncOptions = new SynchronizationOptions();
		syncOptions.setRequireConsistentControllability(false);
		this.syncher = new AutomataSynchronizer(automata, syncOptions);
		this.matcher = m;
		this.container = makeContainer();    // Must create the container, in case the thread is stopped
	}

	protected void synchronize()
	{
		try
		{
			syncher.execute();    // Starts the synch thread and waits for it to stop
		}
		catch (Exception excp)
		{

			// How to work this (exception in a worker thread)??
			logger.debug(excp.getStackTrace());
			return;
		}
	}

	protected void match()
	{
		if (!stopRequested)
		{
			int num_total = syncher.getNumberOfStates();
			int num_processed = 0;

			// Note the difference between the two getStateIterator.
			// This is AutomataSynchronizerHelper::getStateIterator, returns Iterator...
			for (Iterator it = syncher.getHelper().getStateIterator(); it.hasNext() && (!stopRequested); )
			{
				int[] composite_state = (int[]) it.next();

				// ...and this is SearchStates::getStateIterator, returns SearchStates::StateIterator
				if (matcher.matches(getStateIterator(composite_state)))
				{
					container.add(composite_state);
				}

				progress = (int) ((++num_processed * 100) / num_total);
			}

			if (stopRequested)
			{
				container = makeContainer();    // thread stopped - clear the container
			}
		}
	}

	public void run()    // throws Exception
	{
		mode = false;    // start with synching mode - initializer above does not do the trick?

		synchronize();

		mode = true;    // matching mode

		match();
	}

	// These implement the Monitorable interface
	public int getProgress()
	{
		return progress;
	}

	public String getActivity()
	{
		if (!mode)    // synching mode
		{
			return "Synching: " + syncher.getNumberOfStates() + " states checked";
		}
		else          // matching mode
		{
			return "Matching: " + progress + "% done";
		}
	}

	public void stopTask()
	{

		// System.out.println("Stop requested");
		stopRequested = true;

		syncher.requestStop();
	}

	public boolean wasStopped()
	{
		return stopRequested;
	}

	public int numberFound()
	{
		return container.size();
	}

	// To iterate over the matched states
	public Iterator iterator()
	{
		return container.iterator();
	}
	// Given index for an automaton and a composite state, return that state
	public State getState(int automaton, int index)
	{
		State[][] states = syncher.getHelper().getIndexFormStateTable(); // should be cached?
		int[] composite = container.getElement(index);
		return states[automaton][composite[automaton]];
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

	public Automaton buildAutomaton() // once the states have been created, we could build an entire automaton
		throws Exception
	{
		return syncher.getAutomaton();
	}
}
