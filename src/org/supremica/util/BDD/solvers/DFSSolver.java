
package org.supremica.util.BDD.solvers;

import org.supremica.util.BDD.*;

import java.util.*;


/**
 * depth first ordering, AKA topological sort
 *
 * XXX: we should traverse the nodes w.r.t weights (maybe dijkstra's algo...)
 *
 * XXX: we havent tested if the starting point given by get_least_connected is really good !
 *
 */
public class DFSSolver extends Solver {

	private int count;

	public DFSSolver(Node [] org_ ) { super(org_); }

	public void solve() {
		for(int i = 0; i < size; i++) org[i].extra1 = org[i].extra2 = 0;

		count = 0;

		// WAS: dfs(0); // start with the first state
		dfs( get_least_connected() );


		// fix those with no ordering:
		for(int i = 0; i < size; i++) if(org[i].extra1 == 0)  org[i].extra2 = count++;

		// now, sort according to our new DFS order
		for(int i = 0; i < size; i++) solved[ org[i].extra2 ] = org[i];

	}

	/** recursively DFS mark the graph */
	private void dfs(int j) {
		org[j].extra1 = 1;
		org[j].extra2 = count++;

		for(int i = 0; i < size; i++) {
			// traverse in the original array-order, TO BE CHANGED
			if(i != j && (org[i].wlocal[j] > 0) && org[i].extra1 == 0) {
				dfs(i);
			}
		}
	}

}
