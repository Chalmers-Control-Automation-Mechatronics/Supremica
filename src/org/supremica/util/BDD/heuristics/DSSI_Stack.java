

package org.supremica.util.BDD.heuristics;

import org.supremica.util.BDD.*;

/**
 * Stack (FILO) DelayedInsertationHeuristic.
 *
 */

public class DSSI_Stack extends DelayedInsertationHeuristic {
	public DSSI_Stack(Cluster []c, int n) { super(c,n); }

	protected  void do_order() {
		// as simple as it can get
		for(int i = 0; i < curr; i++) order[i] = i;
	}

}