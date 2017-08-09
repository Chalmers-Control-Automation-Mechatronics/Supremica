/************************ Remover.java ************************************
 * Given a set of automata and a set of states, all outgoing transitions
 * from those states are removed. This algorithm is accessed from FindStates
 * once some states have actually been found (see PresentStates.java)
 */
package org.supremica.automata.algorithms;

import java.util.function.Predicate;

import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.State;
import org.supremica.gui.VisualProject;
import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;

/**
 * @author Martin Fabian
 */
public class Remover
{
	private static final Logger logger = LoggerFactory.createLogger(Forbidder.class);
	private final int[] selected_rows;
	private final SearchStates search_states;
	@SuppressWarnings("unused")
    private final VisualProject the_project;
	private final Automata selected_automata;

	public Remover(final Automata automata, final int[] selects, final SearchStates ss, final VisualProject project)
    {
		this.selected_automata = automata;
        this.selected_rows = selects;
        this.search_states = ss;
        this.the_project = project;
	}

	public boolean remove()
	{
       // For each global state...
        for(int i = 0; i < selected_rows.length; ++i)
        {
            final int index = selected_rows[i];	// get index for global state

            // for each automaton...
			final int num = selected_automata.nbrOfAutomata();
            for(int a = 0; a < num; ++a)
            {
                removeTransitions(a, index);
            }
		}
		// Need to purge inaccessible states. Or...?
		final int num = selected_automata.nbrOfAutomata();
		for(int i = 0; i < num; i++)
		{
			final Automaton automaton = selected_automata.getAutomatonAt(i);
			final Predicate<State> removePredicate = s -> (!s.isInitial() && !s.incomingArcsIterator().hasNext());
			final AutomatonPurge purger = new AutomatonPurge(automaton);
			purger.execute(removePredicate);
		}
		return true;
	}

	private void removeTransitions(final int automaton_index, final int state_index)
	{
        // Get automaton -- just for sanity check
        final Automaton automaton = selected_automata.getAutomatonAt(automaton_index);
        // get the local state
        final State state = search_states.getState(automaton_index, state_index);
        // This should have a corresponding state in automaton
		assert automaton.getStateWithName(state.getName()) != null : "Cannot find state " + state.getName() + " in automaton " + automaton.getName();
		logger.debug("Found state " + state.getName() + " in automaton " + automaton.getName());

		state.removeOutgoingArcs();	// This may leave inaccessible states, should purge
	}
}
