

package org.supremica.util.BDD.heuristics;

import org.supremica.util.BDD.*;

/**
 * Randomized DelayedInsertationHeuristic.
 *
 */

public class DSSI_Random extends DelayedInsertationHeuristic {
	public DSSI_Random(Cluster []c, int n) { super(c,n); }

	protected  void do_order() {
		for(int i = 0; i < curr; i++) order[i] = i;
		if(curr > 1) // randomize order of all but the new automata!
			Util.permutate(order, curr-1);
	}

}