

package org.supremica.util.BDD.solvers;

import org.supremica.util.BDD.*;

import java.util.*;


/**
 * depth first ordering, AKA topological sort
 *
 * XXX: we need to a) find a better starting point b) traverse the nodes w.r.t weights
 * XXX: for (b), we suggest the use of dijkstra's algo...
 *
 */
public class DFSSolver extends Solver {

	private int count;

	public DFSSolver(Node [] org_ ) {
		super(org_);
	}

	public void solve() {
		for(int i = 0; i < size; i++) org[i].extra1 = org[i].extra2 = 0;

		count = 0;
		dfs(0); // start with the first state (TO BE CHANGED)

		solved = new Node[size];
		for(int i = 0; i < size; i++) solved[ org[i].extra2 ] = org[i];

	}

	private void dfs(int j) {
		org[j].extra1 = 1;
		org[j].extra2 = count++;

		for(int i = 0; i < size; i++) {
			// traverse in the original array-order, TO BE CHANGED
			if(i != j && (org[i].wlocal[i] > 0) && org[i].extra1 == 0) {
				dfs(i);
			}
		}
	}
}
