
/************************ EnumerateStates.java ***************/

// Walks the state set and renames the states as <prfx><num>
// where <prfx> is given and <num> is calculated. Initial state
// is always numbered 0
// Note that the original automata are altered
package org.supremica.automata.algorithms;

import java.util.*;
import org.supremica.automata.State;
import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;

public class EnumerateStates
{
	Automata automata = null;
	StringBuffer prefix = null;
	int prefixlen = 0;

	/**
	 * Creates enumerator for the supplied automaton.
	 */
	public EnumerateStates(Automaton automaton, String prefix)
	{
		this(new Automata(automaton), prefix);
	}

	/**
	 * Creates enumerator for the supplied automata.
	 */
	public EnumerateStates(Automata automata, String prefix)
	{
		this.automata = automata;
		this.prefix = new StringBuffer(prefix);
		this.prefixlen = prefix.length();
	}

	/**
	 * Makes sure the enumeration is made.
	 */
	public void execute()
	{
		Iterator autit = automata.iterator();

		while (autit.hasNext())
		{
			enumerate((Automaton) autit.next());
		}
	}

	/**
	 * Enumerates the states in automaton.
	 */
	private void enumerate(Automaton automaton)
	{
		automaton.beginTransaction();
		prefix.append("0");

		State init = automaton.getInitialState();

		if (init != null)
		{
			init.setName(prefix.toString());
		}

		prefix.setLength(prefixlen);

		int num = 1;
		Iterator stateit = automaton.stateIterator();
		while (stateit.hasNext())
		{
			State state = (State) stateit.next();

			if (!state.isInitial())
			{
				prefix.append(num);
				state.setName(prefix.toString());

				num++;

				prefix.setLength(prefixlen);
			}
		}

		automaton.invalidate();
		automaton.endTransaction();
	}
}
