

package org.supremica.util.BDD.heuristics;

import org.supremica.util.BDD.*;


/**
 * Random ordering. it's random, what else can i say?
 *
 * This class was sponsored by:
 *    http://www.random.org/
 *
 * this was _very_ usefull whan testing the sanity of the modular algorithm :)
 *
 */
public class ASH_Random extends AutomatonSelectionHeuristic  {

	public void choose(int queue_size) {

		// super-elajt randomization algorithm ...

		for(int i = 0; i < queue_size; i++)
			queue_costs[i] = i;

		for(int i = 0; i < queue_size; i++)
		{
			int j  = (int) (Math.random() * queue_size);
			double tmp = queue_costs[i];
			queue_costs[i] = queue_costs[j];
			queue_costs[j] = tmp;
		}
		// sort
		QuickSort.sort(queue, queue_costs, queue_size, false);
	}
}