package org.supremica.util.BDD.heuristics;

import org.supremica.util.BDD.*;

/**
 * the event that shares most number arcs associated with
 * of common (uncontrollable/considred) events comes first.
 */
public class ASH_MostCommonUncontrollableArcs
	extends AutomatonSelectionHeuristic
{
	public void choose(int queue_size)
	{
		for (int i = 0; i < queue_size; i++)
		{
			BDDAutomaton automaton = list[queue[i]];
			int tmp = automaton.arcOverlapCount(relevant_events);
			int total_arcs = automaton.getNumArcs();

			if (total_arcs == 0)    // no arcs???
			{
				queue_costs[i] = 0;
			}
			else
			{
				queue_costs[i] = ((double) tmp) / ((double) total_arcs);
			}
		}

		// sort
		QuickSort.sort(queue, queue_costs, queue_size, false);
	}
}
