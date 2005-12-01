package org.supremica.automata.algorithms.scheduling;

import org.supremica.automata.Automaton;
import org.supremica.automata.algorithms.Stoppable;

public interface Scheduler
	extends Stoppable
{
	/** Returns the representation of a marked state in an optimal schedule automaton. */
	public void schedule()
		throws Exception;

	/** Builds up an optimal schedule automaton from its marked state representation. */
	public Automaton buildScheduleAutomaton()
		throws Exception;
}