
package org.supremica.util.BDD.solvers;

import org.supremica.util.BDD.*;

import java.util.*;


/**
 * breadth first ordering
 *
 * XXX: we should traverse the nodes w.r.t weights ??
 *
 */
public class BFSSolver extends Solver {

	private int count;

	public BFSSolver(Node [] org_ ) { super(org_); }

	public void solve() {
		for(int i = 0; i < size; i++) org[i].extra1 = org[i].extra2 = 0;

		count = 0;

		int [] tmp = new int[size];
		int [] best = new int[size];
		double best_cost = Double.MAX_VALUE;

		IntQueue queue = new IntQueue(size);
		// we dont know where to start, so we will try them all :(
		for(int first = 0; first < size; first++) {
			queue.reset();
			queue.enqueue( first );

			while(!queue.empty() ) {
				int curr = queue.dequeue();
				org[curr].extra1 = 1;
				org[curr].extra2 = tmp[curr] = count++;

				for(int i = 0; i < size; i++) {
					// traverse in the original array-order, TO BE CHANGED
					if(i != curr && (org[i].wlocal[curr] > 0) && org[i].extra1 == 0) {
						org[i].extra1 = -1; // otherwise, we would insert same node multiple times!
						queue.enqueue(i);
					}
				}
			}

			// fix those with no ordering:
			for(int i = 0; i < size; i++) if(org[i].extra1 == 0)  org[i].extra2 = tmp[i] = count++;

			double cost = totalCost(tmp);
			if(cost < best_cost) {
				best_cost = 0;
				for(int i = 0; i < size; i++) best[i] = tmp[i];
			}
		}

		// now, sort according to our new DFS order
		for(int i = 0; i < size; i++) solved[ best[i] ] = org[i];

	}


}
