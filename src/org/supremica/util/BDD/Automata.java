package org.supremica.util.BDD;

import org.supremica.util.BDD.heuristics.*;
import java.util.*;
import java.io.*;

public class Automata
{
	private Automaton current = null;
	private EventManager alphabet;
	private Vector automata = new Vector();
	private boolean closed = false;
	private int[] grupp_ordering = null;
	private double total_size = 0, total_size_allocated = 0;
	private int variable_count = 0;
	private long reordering_time;

	public Automata()
	{
		alphabet = new EventManager();
	}

	public Vector getAutomata()
	{
		return automata;
	}

	public Automaton createAutomaton(Object org, String name)
		throws BDDException
	{
		BDDAssert.bddAssert(!closed, "[Automata.createAutomaton] BAD function call");

		current = new Automaton(name, alphabet, org);

		automata.addElement(current);

		return current;
	}

	public Automaton getCurrent()
	{
		return current;
	}

	public EventManager getAlphabeth()
	{
		return alphabet;
	}

	public double getTotalSize()
	{
		return total_size;
	}

	public double getTotalSizeAllocated()
	{
		return total_size_allocated;
	}

	public int getVariableCount()
	{
		return variable_count;
	}

	public long getReorderingTime()
	{
		return reordering_time;
	}

	public int[][] getCommunicationMatrix()
		throws BDDException
	{
		int components = automata.size();
		int[][] ret = new int[components][components];

		for (int i = 0; i < components; i++)
		{
			Automaton a1 = (Automaton) automata.elementAt(i);

			ret[i][i] = a1.getCommunicationComplexity(a1);    // not actually needed :)

			for (int j = 0; j < i; j++)
			{
				Automaton a2 = (Automaton) automata.elementAt(j);
				int cc = a1.getCommunicationComplexity(a2);

				ret[i][j] = ret[j][i] = cc;
			}
		}

		return ret;
	}

	public void close()
		throws BDDException
	{
		BDDAssert.bddAssert(!closed, "[Automata.close] BAD function call");
		alphabet.close();

		int components;

		for (Enumeration e = automata.elements(); e.hasMoreElements(); )
		{
			Automaton a = (Automaton) e.nextElement();

			a.close2();
		}

		// --[ Automata ordering which affects the BDD variable ordering starts HERE ] --
		Timer timer = new Timer();    // This times the ordering procedure
		AutomataOrderingHeuristic aoh = AutomataOrderingHeuristicFactory.createInstance(this);

		grupp_ordering = aoh.ordering();

		timer.report("Automata reordering '" + AutomataOrderingHeuristicFactory.getName() + "' done");

		reordering_time = timer.getElapsed();

		// maybe the user needs to change something ?
		if (Options.user_alters_PCG)
		{
			PCGFrame frame = new PCGFrame(grupp_ordering, automata);

			frame.getUserPermutation();
		}

		// now, use the new ordering to re-order the list:
		components = automata.size();

		Vector tmp = new Vector();

		for (int i = 0; i < components; i++)
		{
			tmp.addElement(automata.elementAt(grupp_ordering[i]));
		}

		automata.removeAllElements();    // just for fun

		automata = tmp;

		// compute upper-bound and allocated states size
		total_size = 1;
		variable_count = 0;

		for (Enumeration e = automata.elements(); e.hasMoreElements(); )
		{
			Automaton a = (Automaton) e.nextElement();
			int size = a.getStatesSize();

			if (size > 0)
			{
				total_size *= size;
				variable_count += a.getStateVectorSize();
			}
		}

		total_size_allocated = Math.pow(2, variable_count);
		variable_count = variable_count * 3 + Util.log2ceil(alphabet.getSize());

		if (Options.debug_on || Options.profile_on)
		{
			Options.out.println("The theoretical number of states is " + Util.showHugeNumber(total_size));
			Options.out.println("Universe size is " + Util.showHugeNumber(total_size_allocated));
		}

		// .... and, we are done
		closed = true;
	}

	public void dump(PrintStream ps)
	{
		for (Enumeration e = automata.elements(); e.hasMoreElements(); )
		{
			Automaton a = (Automaton) e.nextElement();

			a.dump(ps);
		}
	}

	public void stats(PrintStream ps)
	{
		ps.println("" + automata.size() + " automata (" + (long) total_size + " states max, " + (long) total_size_allocated + " allocated)");
	}
}
