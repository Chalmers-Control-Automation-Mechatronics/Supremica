
package org.supremica.util.BDD.solvers;

import org.supremica.util.BDD.*;

import java.util.*;


/**
 * STCT:SA code, but start with a godd initial solution (from TSP)
 *
 */

public class BootstrapSTCTSolver extends STCTSolver {

	private TSPSolver tsp = null;

	public BootstrapSTCTSolver(Node [] org_ ) { super(org_); }




	/** lets start woth a "good" initial solution! */
	protected void pre_solve() {
		super.pre_solve();

		if(tsp == null) tsp = new TSPSolver(org);
		// get a TSP solution as our initial solution
		tsp.solve();
		for(int i = 0; i < size; i++)
			work[i] = solved[i] = tsp.solved[i];
		overallResult = eval();

	}
}