package org.supremica.util.BDD;

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

	public Automata()
	{
		alphabet = new EventManager();
	}

	public Vector getAutomata()
	{
		return automata;
	}

	public Automaton createAutomaton(String name)
	{
		BDDAssert.bddAssert(!closed, "[Automata.createAutomaton] BAD function call");

		current = new Automaton(name, alphabet);

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

	public void close()
	{
		BDDAssert.bddAssert(!closed, "[Automata.close] BAD function call");
		alphabet.close();

		Enumeration e = automata.elements();

		while (e.hasMoreElements())
		{
			Automaton a = (Automaton) e.nextElement();

			a.close2();
		}

		// get PCG graph
		Timer timer = new Timer();
		PCG pcg = new PCG(automata);
		int components = automata.size();

		for (int i = 0; i < components; i++)
		{    // get weights
			Automaton a1 = (Automaton) automata.elementAt(i);

			for (int j = 0; j < i; j++)
			{
				Automaton a2 = (Automaton) automata.elementAt(j);
				int cc = a1.getCommunicationComplexity(a2);

				if (cc != 0)
				{
					pcg.connect(a1, a2, cc);
				}
			}
		}

		// pcg.dump();
		grupp_ordering = pcg.getShortestPath();

		// now, reorder
		Vector tmp = new Vector();

		for (int i = 0; i < components; i++)
		{
			tmp.addElement(automata.elementAt(grupp_ordering[i]));
		}

		automata.removeAllElements();    // just for fun

		automata = tmp;

		timer.report("PCG reodering done");

		// compute upper-bound and allocated states size
		total_size = 1;
		variable_count = 0;

		for (e = automata.elements(); e.hasMoreElements(); )
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
		variable_count = variable_count * 2 + Util.log2ceil(alphabet.getSize());

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
