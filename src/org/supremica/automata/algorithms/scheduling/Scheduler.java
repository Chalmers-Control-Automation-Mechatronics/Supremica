package org.supremica.automata.algorithms.scheduling;

import org.supremica.automata.algorithms.Stoppable;

public interface Scheduler
	extends Stoppable, Runnable
{
	/** Returns the representation of a marked state in an optimal schedule automaton. */
	public void schedule()
		throws Exception;

	/** Builds up an optimal schedule automaton from its marked state representation. */
	public void buildScheduleAutomaton()
		throws Exception;

	/** Returns a string containing information about the optimization process. */
	public String getOutputString();
}