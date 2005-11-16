
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
import java.util.Iterator;
import org.supremica.log.*;
import org.supremica.util.IntArrayVector;
import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.gui.MonitorableThread;
import org.supremica.properties.SupremicaProperties;

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
		syncOptions.setBuildAutomaton(false);    // don't build teh automaton until absolutely necessary

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
			long num_total = syncher.getNumberOfStates();
			long num_processed = 0;

			// Note the difference between the two getStateIterator.
			// This is AutomataSynchronizerHelper::getStateIterator, returns Iterator...
			for (Iterator it = syncher.getHelper().getStateIterator();
					it.hasNext() && (!stopRequested); )
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
		else    // matching mode
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

	/**
	 * Given index for an automaton and an index for a composite state, return that state
	 */
	public org.supremica.automata.State getState(int automaton, int index)
	{
		org.supremica.automata.State[][] states = syncher.getHelper().getIndexFormStateTable();    // should be cached?
		int[] composite = container.getElement(index);

		return states[automaton][composite[automaton]];
	}

	/**
	 * Iterator over a composite state, returns the State of the respective Automaton
	 * External users should create StateIterators only through the getStateIterator method
	 */
	public class StateIterator
	{
		private org.supremica.automata.State[][] states;
		private int[] composite;
		int index;	// holds the automaton index
		
		// Private, instantiate only through getStateIterator
		private StateIterator(org.supremica.automata.State[][] s, int[] c)
		{
			states = s;
			composite = c;
			index = 0;

			logger.debug("getState states[" + states.length + "][" + states[0].length + "]");
			logger.debug("getState composite[" + composite.length + "]");
		}

		public boolean hasNext()
		{

			// the last element of composite is not used
			// return index < composite.length - 1;
			// did Knut change this to not use the last two elements??
			return index < composite.length - 2;

			// Yes! He f***ing did. Where else did this break code???
		}

		public org.supremica.automata.State getState()
		{

			// get the current state of the current automaton
			logger.debug("getState index: " + index);
			logger.debug("getState composite.length: " + composite.length);
			logger.debug("getState composite[index]: " + composite[index]);

			return states[index][composite[index]];
		}

		public void inc()
		{

			// move to the next automaton
			++index;
		}
	}

	/**
	 * External users use this method to create a StateIterator
	 * 
	 */
	public StateIterator getStateIterator(int[] composite_state)
	{
		org.supremica.automata.State[][] states = syncher.getHelper().getIndexFormStateTable();
		return new StateIterator(states, composite_state);
	}

	public String toString(int[] composite_state)
	{
		AutomataSynchronizerHelper helper = syncher.getHelper();
		org.supremica.automata.State[][] states = helper.getIndexFormStateTable();
		StringBuffer str = new StringBuffer();

		for (int i = 0; i < states.length; ++i)
		{
			str.append(states[i][composite_state[i]].getName());	
			str.append(SupremicaProperties.getStateSeparator());
		}

		// Remove last state separator
		int idx = str.lastIndexOf(SupremicaProperties.getStateSeparator());
		str.delete(idx, str.length());
		
		return new String(str);
	}

	private Automaton buildAutomaton()    // once the states have been created, we could build an entire automaton
		throws Exception
	{
		return syncher.getAutomaton();
	}
}
