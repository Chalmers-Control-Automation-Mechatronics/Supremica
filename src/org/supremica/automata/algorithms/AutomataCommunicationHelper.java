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
	public static Vector getDependencyGroup(Automata selected, Automata all)
		throws Exception
	{
		all = new Automata(all, true);
		Vector toSelect = new Vector();

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

	public static Vector getMaximalComponent(Automata selected, Automata all)
		throws Exception
	{

		boolean done;
		all = new Automata(all, true);
		Vector toSelect = new Vector();

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

			for (Enumeration e = toSelect.elements() ; e.hasMoreElements() ;)
			{
				Automaton a = (Automaton) e.nextElement();
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
	 public static Vector split(Automata all)
	 	throws Exception
	 {
		 Vector v = new Vector();
		 all = new Automata(all, true);

		while(all.size() > 0)   {
			// get the first automaton:
			Automaton first = all.iterator().nextAutomaton();
			Automata firstAut = new Automata(first);

			Vector component = getMaximalComponent(firstAut, all);

			Automata componentAut = new Automata();
			for (Enumeration e = component.elements() ; e.hasMoreElements() ;)
			{
				Automaton a = (Automaton) e.nextElement();
				all.removeAutomaton(a);
				componentAut.addAutomaton(a);
			}

			v.add( componentAut);
		}

		 return v;
	 }
}
