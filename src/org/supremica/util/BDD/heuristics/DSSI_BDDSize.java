


package org.supremica.util.BDD.heuristics;

import org.supremica.util.BDD.*;

/**
 * Smallest/Largest BDD DelayedInsertationHeuristic:
 *  after the current automaton, sort the other cluster in T~ -size order
 */

public class DSSI_BDDSize extends DelayedInsertationHeuristic {
	private double [] cost;
	private boolean smallest_first;

	public DSSI_BDDSize(Cluster []c, int n, boolean smallest_first) {
		super(c,n);
		this.cost = new double[n];
		this.smallest_first = smallest_first;
	}

	protected  void do_order() {
		order[curr-1] = curr-1; // the new one

		if(curr > 1) {
			for(int i = 0; i < curr-1; i++) {
				order[i] = i;
				cost[i] = cluster_stack[i].getBDDSize();
			}
			QuickSort.sort(order, cost, curr-1, smallest_first);
		}
	}

}