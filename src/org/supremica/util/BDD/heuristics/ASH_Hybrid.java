

package org.supremica.util.BDD.heuristics;

import org.supremica.util.BDD.*;



/**
 * some hybrid heuristic that compute the weighted sum of
 * different heuristics.
 */

public class ASH_Hybrid extends AutomatonSelectionHeuristic  {

	private double [] cost2;

	/** just to allocate cost2 */
	public void init(BDDAutomaton [] list, int [] queue, double [] queue_costs) {
		super.init(list, queue, queue_costs);
		cost2 = new double[queue.length];
	}


	public void choose(int queue_size) {


		// MOST UC-ARC
		double max1 = - Double.MAX_VALUE;
		for(int i = 0; i < queue_size; i++) {
				BDDAutomaton automaton = list[ queue[i] ];
				int tmp        = automaton.arcOverlapCount( relevant_events );
				int total_arcs  = automaton.getNumArcs();

				if(total_arcs == 0) // no arcs???
					cost2[i] = 0;
				else
					cost2[i] =  ((double) tmp) / ((double) total_arcs);

				if(cost2[i] > max1) max1 = cost2[i];
			}


		// MOST LOCAL EVENTS
		double max2 = - Double.MAX_VALUE;
		for(int i = 0; i < queue_size; i++) {
			BDDAutomaton automaton = list[ queue[i] ];
			queue_costs[i]  = automaton.eventOverlapCount( relevant_events );
			if(queue_costs[i] > max2) max2 = queue_costs[i];
		}



		// sum up!
		if(max1 == 0) max1 = 1;
		if(max2 == 0) max2 = 1;

		for(int i = 0; i < queue_size; i++) {
			queue_costs[i] = queue_costs[i] / max2 + cost2[i] / max1;
		}
		// sort
		QuickSort.sort(queue, queue_costs, queue_size, false);
	}
}