package org.supremica.util.BDD.encoding;

import org.supremica.util.BDD.*;
import java.util.*;

/**
 * Do a DFS encoding from the first state [initial/marked].
 * States that are not reachable will be encoded in default order.
 */
public class DFSEncoding
	implements Encoding
{
	private boolean from_i;

	public DFSEncoding(boolean from_i)
	{
		this.from_i = from_i;
	}

	public void encode(Automaton a)
	{
		State[] states = a.getStates().getStateVector();
		int size = states.length;
		int count = 0;
		State[] stack = new State[size];
		int[] codes = new int[size];
		int tos = 0;

		for (int i = 0; i < size; i++)
		{
			states[i].extra1 = i;
			codes[i] = -1;    // not reached

			if ((from_i && states[i].isInitial()) || (!from_i && states[i].isMarked()))
			{
				stack[tos++] = states[i];
				codes[i] = count++;
			}
		}

		while (tos > 0)
		{
			State state = stack[--tos];

			for (Enumeration e = from_i
								 ? state.out()
								 : state.in(); e.hasMoreElements(); )
			{
				Arc arc = (Arc) e.nextElement();
				State s = from_i
						  ? arc.toState()
						  : arc.fromState();
				int index = s.extra1;

				if (codes[index] == -1)
				{
					codes[index] = count++;
					stack[tos++] = s;
				}
			}
		}

		// write the results to the structure:
		for (int i = 0; i < size; i++)
		{
			if (codes[i] == -1)
			{
				codes[i] = count++;    // ok, just give the rest some default numbers:
			}

			states[i].setCode(codes[i]);
		}
	}
}
