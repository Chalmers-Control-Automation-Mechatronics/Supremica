package org.supremica.automata.algorithms.scheduling;

import net.sourceforge.waters.model.analysis.Abortable;

import org.supremica.automata.Automaton;

public interface Scheduler
    extends Abortable, Runnable
{
	/** Starts the search thread. */
	public void startSearchThread();

    /** Returns the representation of a marked state in an optimal schedule automaton. */
    public void schedule()
		throws Exception;

    /** Builds up an optimal schedule automaton from its marked state representation. */
    public void buildScheduleAutomaton()
		throws Exception;

	//@Deprecated
    ///** Returns a string containing information about the optimization process. */
    //public String getOutputString();

	/** Returns the schedule automaton. */
	public Automaton getSchedule();

	/**
         * Returns all messages of the supplied type that have been generated
         * during a run. Message types are defined in {@link SchedulingConstants}.
         */
	public String getMessages(int msgType);

	/** Returns all debug messages that have been generated during a run. */
	public Object[] getDebugMessages();

        public void addToMessages(String additionStr, int messageType);
}