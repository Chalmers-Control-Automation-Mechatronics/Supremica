package org.supremica.util.BDD.heuristics;

import org.supremica.util.BDD.*;

/**
 * somehow preserve the order they were put in the queue.
 * more likely to follow the BDD order, for what its worth...
 */
public class ASH_Linear
	extends AutomatonSelectionHeuristic
{
	private boolean reverse;

	public ASH_Linear(boolean reverse)
	{
		this.reverse = reverse;
	}

	public void choose(int queue_size)
	{
		for (int i = 0; i < queue_size; i++)
		{
			queue_costs[i] = i;
		}

		// sort
		QuickSort.sort(queue, queue_costs, queue_size, reverse);
	}
}
