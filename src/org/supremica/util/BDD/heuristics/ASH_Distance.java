

package org.supremica.util.BDD.heuristics;

import org.supremica.util.BDD.*;

/**
 * choose the closest automata in the BDD tree.
 */

public class ASH_Distance extends AutomatonSelectionHeuristic  {
	public void choose(int queue_size) {

		int center = first.getIndex();
		for(int i = 0; i < queue_size; i++)
			queue_costs[i] = Math.abs( center - list[ queue[i] ].getIndex() );

		// sort
		QuickSort.sort(queue, queue_costs, queue_size, true);
	}
}