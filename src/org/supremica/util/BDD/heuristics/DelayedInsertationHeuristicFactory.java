

package org.supremica.util.BDD.heuristics;

import org.supremica.util.BDD.*;

/**
 * Factory for DelayedInsertationeHuristic.
 *
 * TODO: add some _real_ huristic, because these all SUCK!
 */

public abstract class DelayedInsertationHeuristicFactory {

	public static DelayedInsertationHeuristic create(Cluster [] c, int n) {
		switch(Options.dssi_heuristics) {
			case Options.DSSI_RANDOM: return new DSSI_Random(c,n);
			case Options.DSSI_STACK: return new DSSI_Stack(c,n);
			case Options.DSSI_FIFO: return new DSSI_FIFO(c,n);
			case Options.DSSI_SMALLEST_BDD: return new DSSI_BDDSize(c,n, true);
			case Options.DSSI_LARGEST_BDD: return new DSSI_BDDSize(c,n, false);
			default:
			return null; // error
		}
	}
}