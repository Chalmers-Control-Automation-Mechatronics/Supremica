package org.supremica.automata.algorithms;

import java.util.*;
import org.supremica.automata.*;


/**
 *
 * This class contains small functions that have to do with automata interaction
 *
 */

public class AutomataCommunicationHelper {

	/**
	 * returns the dependency group
	 *
	 */
	public static Collection getDependencyGroup(Automata selected, Automata all)
		throws Exception
	{
		all = new Automata(all, true);
		Collection toSelect = new HashSet();


		for (AutomatonIterator it = selected.iterator(); it.hasNext(); )
		{
			Automaton a = it.nextAutomaton();
			toSelect.add( a);
			all.removeAutomaton(a);
		}

		Alphabet  selectedAlphabet = AlphabetHelpers.getUnionAlphabet(selected, false, false);

		for (AutomatonIterator it = all.iterator(); it.hasNext(); )
		{
			Automaton a = it.nextAutomaton();
			Alphabet alfa = a.getAlphabet();
			if(alfa.overlap(selectedAlphabet))
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
	 * @retruns Vector of Automaton which is v, such that selected SUBSET-EQUAL v SUBSET-EQUAL all.
	 */

	public static Collection getMaximalComponent(Automata selected, Automata all)
		throws Exception
	{

		boolean done;
		all = new Automata(all, true);
		Collection toSelect = new HashSet();

		for (AutomatonIterator it = selected.iterator(); it.hasNext(); )
		{
			Automaton a = it.nextAutomaton();
			toSelect.add( a);
			all.removeAutomaton(a);
		}
		do
		{
			done = true;
			Alphabet  selectedAlphabet = AlphabetHelpers.getUnionAlphabet(selected, false, false);

			for (AutomatonIterator it = all.iterator(); it.hasNext(); )
			{
				Automaton a = it.nextAutomaton();

				Alphabet alfa = a.getAlphabet();
				if(alfa.overlap(selectedAlphabet))
				{
					toSelect.add(a);
					selected.addAutomaton(a);
					done = false;
				}
			}

			for (Iterator it = toSelect.iterator() ; it.hasNext() ;)
			{
				Automaton a = (Automaton) it.next();
				all.removeAutomaton(a);
			}

		}
		while(! done);
		return toSelect;
	}


	/**
	 * splits the graph in the largest interacting components.
	 *
	 * @returns Vector of Automata whose alphabet share no common events
	 */
	 public static Collection split(Automata all)
	 	throws Exception
	 {
		 Collection v = new LinkedList();
		 all = new Automata(all, true);

		while(all.size() > 0)   {
			// get the first automaton:
			Automaton first = all.iterator().nextAutomaton();
			Automata firstAut = new Automata(first);


			Automata componentAut = new Automata();
			Collection component = getMaximalComponent(firstAut, all);

			for (Iterator it = component.iterator() ; it.hasNext() ;)
			{
				Automaton a = (Automaton) it.next();
				all.removeAutomaton(a);
				componentAut.addAutomaton(a);
			}

			v.add( componentAut);
		}

		 return v;
	 }
}
