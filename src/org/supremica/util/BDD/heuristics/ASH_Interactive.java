package org.supremica.util.BDD.heuristics;

import org.supremica.util.BDD.*;

/**
 * let the user change the automata
 */
public class ASH_Interactive
	extends AutomatonSelectionHeuristic
{
	private InteractiveChoice ic;

	public ASH_Interactive()
	{
		ic = new InteractiveChoice("Please choos the next automaton to be added");
	}

	public void choose(int queue_size) {}

	public int pick(int queue_size)
	{
		ic.removeAll();

		for (int i = 0; i < queue_size; i++)
		{
			ic.add(list[queue[i]].getName());
		}

		ic.show();

		int sel = ic.getSelected();

		if (sel == -1)
		{
			return super.pick(queue_size);
		}

		// place it at back so we can remove it
		int tmp = queue[queue_size - 1];

		queue[queue_size - 1] = queue[sel];
		queue[sel] = tmp;

		return queue[queue_size - 1];
	}
}
