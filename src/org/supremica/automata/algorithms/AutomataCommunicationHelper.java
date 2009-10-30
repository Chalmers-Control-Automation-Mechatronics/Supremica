package org.supremica.automata.algorithms;

import java.util.*;
import org.supremica.automata.*;

/**
 *
 * This class contains small functions that have to do with automata interaction
 *
 */
public class AutomataCommunicationHelper
{

	/**
	 * returns the dependency group
	 *
	 */
	public static Collection<Automaton> getDependencyGroup(Automata selected, Automata all)
		throws Exception
	{
		all = new Automata(all, true);

		Collection<Automaton> toSelect = new HashSet<Automaton>();

		for (Iterator<Automaton> it = selected.iterator(); it.hasNext(); )
		{
			Automaton a = it.next();

			toSelect.add(a);
			all.removeAutomaton(a);
		}

		Alphabet selectedAlphabet = AlphabetHelpers.getUnionAlphabet(selected, false, false);

		for (Iterator<Automaton> it = all.iterator(); it.hasNext(); )
		{
			Automaton a = it.next();
			Alphabet alfa = a.getAlphabet();

			if (alfa.overlap(selectedAlphabet))
			{
				toSelect.add(a);
			}
		}

		return toSelect;
	}

	/**
	 * return the maximal component in the PCG graph.
	 *
	 * that is, retrun all the automata that interact with selected (including itself) and
	 * leave the rest.
	 *
	 * @return Vector of Automaton which is v, such that selected SUBSET-EQUAL v SUBSET-EQUAL all.
	 */
	public static Collection<Automaton> getMaximalComponent(Automata selected, Automata all)
		throws Exception
	{
		boolean done;

		all = new Automata(all, true);

		Collection<Automaton> toSelect = new HashSet<Automaton>();

		for (Iterator<Automaton> it = selected.iterator(); it.hasNext(); )
		{
			Automaton a = it.next();

			toSelect.add(a);
			all.removeAutomaton(a);
		}

		do
		{
			done = true;

			Alphabet selectedAlphabet = AlphabetHelpers.getUnionAlphabet(selected, false, false);

			for (Iterator<Automaton> it = all.iterator(); it.hasNext(); )
			{
				Automaton a = it.next();
				Alphabet alfa = a.getAlphabet();

				if (alfa.overlap(selectedAlphabet))
				{
					toSelect.add(a);
					selected.addAutomaton(a);

					done = false;
				}
			}

			for (Iterator<Automaton> it = toSelect.iterator(); it.hasNext(); )
			{
				Automaton a = it.next();

				all.removeAutomaton(a);
			}
		}
		while (!done);

		return toSelect;
	}

	/**
	 * splits the graph in the largest interacting components.
	 *
	 * @return Collection of Automata whose alphabet share no common events
	 */
	public static Collection<Automata> split(Automata all)
		throws Exception
	{
		Collection<Automata> v = new LinkedList<Automata>();

		all = new Automata(all, true);

		while (all.size() > 0)
		{

			// get the first automaton:
			Automaton first = all.iterator().next();
			Automata firstAut = new Automata(first);
			Automata componentAut = new Automata();
			Collection<Automaton> component = getMaximalComponent(firstAut, all);

			for (Iterator<Automaton> it = component.iterator(); it.hasNext(); )
			{
				Automaton a = it.next();

				all.removeAutomaton(a);
				componentAut.addAutomaton(a);
			}

			v.add(componentAut);
		}

		return v;
	}
}
