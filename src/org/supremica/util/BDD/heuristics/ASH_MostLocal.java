

package org.supremica.util.BDD.heuristics;

import org.supremica.util.BDD.*;

/**
 * We try to get find the automata thet gives the most number of local events.
 * since this is not always possible, we look further steps by taking into account
 * common events (see MostCommonEvents heuristics.
 */

public class ASH_MostLocal extends AutomatonSelectionHeuristic  {
	public void choose(int queue_size) {


		// further steps:
		int max = 0;
		for(int i = 0; i < queue_size; i++) {
			BDDAutomaton automaton = list[ queue[i] ];
			queue_costs[i]  = automaton.eventOverlapCount( partition_events );
			if(max > queue_costs[i]) max = (int)queue_costs[i];
		}

		if(max == 0) max = 1;

		// one step look-ahead
		int len = event_usage.length;
		for(int i = 0; i < queue_size; i++) {
			boolean [] this_use = list[ queue[i] ].getEventCareSet(false);
			int count = 0;
			for(int j = 0; j < len; j++) {
				if(event_usage[i] == 1 && this_use[j]) {
					count++;
				}
			}
			queue_costs[i] += max * count;
		}




		// sort
		QuickSort.sort(queue, queue_costs, queue_size, false);
	}
}