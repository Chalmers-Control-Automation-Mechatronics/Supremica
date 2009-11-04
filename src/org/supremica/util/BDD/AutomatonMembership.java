package org.supremica.util.BDD;

import java.util.*;

/**
 * Accepts a BDDAutomaton ba if it has the same name as one
 * of the Supremica Automata in theAutomata
 */
public class AutomatonMembership
	implements GroupMembership
{
	private org.supremica.automata.Automata theAutomata;

	public AutomatonMembership(org.supremica.automata.Automata a)
	{
		theAutomata = a;
	}

	public boolean shouldInclude(BDDAutomaton a)
	{
		String name = a.getName();
		Iterator<org.supremica.automata.Automaton> autIt = theAutomata.iterator();

		while (autIt.hasNext())
		{
			org.supremica.automata.Automaton supa = (org.supremica.automata.Automaton) autIt.next();

			if (name.equals(supa.getName()))
			{
				return true;
			}
		}

		return false;
	}
}
