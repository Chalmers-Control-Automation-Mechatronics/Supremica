

package org.supremica.util.BDD.heuristics;

import org.supremica.util.BDD.*;



/**
 * the event that shares most number of common events comes first.
 *
 * NO matter how many times they are used!
 */

public class ASH_MostCommonEvents extends AutomatonSelectionHeuristic  {

	public void choose(int queue_size) {
		for(int i = 0; i < queue_size; i++) {
			BDDAutomaton automaton = list[ queue[i] ];
			queue_costs[i]  = automaton.eventOverlapCount( partition_events );
		}

		// sort
		QuickSort.sort(queue, queue_costs, queue_size, false);
	}
}