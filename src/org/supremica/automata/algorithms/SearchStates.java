package org.supremica.automata.algorithms;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.gui.MonitorableThread;
import org.supremica.properties.Config;


/**
 * <P>Given an Automata object and a Matcher object, online
 * synch the automata and save the matching states.</P>
 *
 * <P>Is it useful to first search each automaton for states
 * matching that automatons pattern? At least, if some
 * automaton has no states matching its pattern, then no
 * global match exists.</P>
 *
 * @author Martin Fabian
 */

public class SearchStates
    extends MonitorableThread
{
    private static final Logger logger = LogManager.getLogger(SearchStates.class);
    private AutomataSynchronizer syncher = null;
    private ArrayList<int[]> container = null;
    private StateMatcher matcher = null;
    protected /* volatile */ boolean stopRequested = false;
    protected boolean mode = false;    // false means sychronization mode, true is matching mode
    protected int progress = 1;

    private ArrayList<int[]> makeContainer()
    {
        return new ArrayList<>();
    }

    public SearchStates(final Automata automata, final StateMatcher m)
    throws Exception
    {
        setPriority(Thread.MIN_PRIORITY);

        // !!Throws exception if automata is empty or has only one automaton!!
        final SynchronizationOptions syncOptions = new SynchronizationOptions();

        syncOptions.setRequireConsistentControllability(false);
        syncOptions.setBuildAutomaton(false);    // don't build teh automaton until absolutely necessary

        this.syncher = new AutomataSynchronizer(automata, syncOptions, false);
        this.matcher = m;
        this.container = makeContainer();    // Must create the container, in case the thread is stopped
    }

    protected void synchronize()
    {
        try
        {
            syncher.execute();    // Starts the synch thread and waits for it to stop
        }
        catch (final Exception excp)
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
            final long num_total = syncher.getNumberOfStates();
            long num_processed = 0;

            // Note the difference between the two getStateIterator.
            // This is AutomataSynchronizerHelper::getStateIterator, returns Iterator...
            for (final Iterator<?> it = syncher.getHelper().getStateIterator();
            it.hasNext() && (!stopRequested); )
            {
                final int[] composite_state = (int[]) it.next();

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

    @Override
    public void run()    // throws Exception
    {
        mode = false;    // start with synching mode - initializer above does not do the trick?

        synchronize();

        mode = true;    // matching mode

        match();
    }

    // These implement the Monitorable interface
    @Override
    public int getProgress()
    {
        return progress;
    }

    @Override
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

    @Override
    public void stopTask()
    {

        // System.out.println("Stop requested");
        stopRequested = true;

        syncher.requestAbort();
    }

    @Override
    public boolean wasStopped()
    {
        return stopRequested;
    }

    public int numberFound()
    {
        return container.size();
    }

    // To iterate over the matched states
    public Iterator<int[]> iterator()
    {
        return container.iterator();
    }

    /**
     * Given index for an automaton and an index for a composite state, return that state
     */
    public org.supremica.automata.State getState(final int automaton, final int index)
    {
        final org.supremica.automata.State[][] states = syncher.getHelper().getIndexFormStateTable();    // should be cached?
        final int[] composite = container.get(index);

        return states[automaton][composite[automaton]];
    }

    /**
     * Iterator over a composite state, returns the State of the respective Automaton
     * External users should create StateIterators only through the getStateIterator method
     */
    public class StateIterator
    {
        private final org.supremica.automata.State[][] states;
        private final int[] composite;
        int index;	// holds the automaton index

        // Private, instantiate only through getStateIterator
        private StateIterator(final org.supremica.automata.State[][] s, final int[] c)
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
    public StateIterator getStateIterator(final int[] composite_state)
    {
        final org.supremica.automata.State[][] states = syncher.getHelper().getIndexFormStateTable();
        return new StateIterator(states, composite_state);
    }

    public String toString(final int[] composite_state)
    {
        final AutomataSynchronizerHelper helper = syncher.getHelper();
        final org.supremica.automata.State[][] states = helper.getIndexFormStateTable();
        final StringBuilder str = new StringBuilder();

        for (int i = 0; i < states.length; ++i)
        {
            str.append(states[i][composite_state[i]].getName());
            str.append(Config.GENERAL_STATE_SEPARATOR.get());
        }

        // Remove last state separator
        final int idx = str.lastIndexOf(Config.GENERAL_STATE_SEPARATOR.getAsString());
        str.delete(idx, str.length());

        return new String(str);
    }

    @SuppressWarnings("unused")
	private Automaton buildAutomaton()    // once the states have been created, we could build an entire automaton
    	throws Exception
    {
        return syncher.getAutomaton();
    }
}
