

package org.supremica.util.BDD.heuristics;

import org.supremica.util.BDD.*;

/**
 * First In/First Out DelayedInsertationHeuristic.
 * (this does not include the newly inster automaton)
 *
 */

public class DSSI_FIFO extends DelayedInsertationHeuristic {
	public DSSI_FIFO(Cluster []c, int n) { super(c,n); }

	protected  void do_order() {
		order[curr-1] = curr-1; // the new one
		for(int i = 0; i < curr-1; i++) order[i] = curr - i -1;
	}

}