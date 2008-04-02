package org.supremica.util.BDD.heuristics;

import org.supremica.util.BDD.*;
import java.util.*;

/**
 * random ordering technique
 */
public class AOH_Random
	extends AutomataOrderingHeuristic
{
	private int[] order;

	public int[] ordering()
	{
		return order;
	}

	public void init(Automata a)
		throws BDDException
	{
		Vector automata = a.getAutomata();
		int size = automata.size();

		order = new int[size];

		for (int i = 0; i < size; i++)
		{
			order[i] = i;
		}

/*		for (int i = 0; i < size; i++)
		{
			int j = (int) (Math.random() * size);
			int tmp = order[i];

			order[i] = order[j];
			order[j] = tmp;
		}
 */               
	}
}
