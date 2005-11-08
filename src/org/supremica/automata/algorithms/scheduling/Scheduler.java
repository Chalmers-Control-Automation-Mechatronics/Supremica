package org.supremica.automata.algorithms.scheduling;

import org.supremica.automata.Automaton;

public interface Scheduler
{
	/** Returns the representation of a marked state in an optimal schedule automaton. */
	public int[] schedule()
		throws Exception;

	/** Builds up an optimal schedule automaton from its marked state representation. */
	public Automaton buildScheduleAutomaton(int[] node)
		throws Exception;
}