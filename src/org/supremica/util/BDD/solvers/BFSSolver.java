
package org.supremica.util.BDD.solvers;

import org.supremica.util.BDD.*;

import java.util.*;


/**
 * breadth first ordering
 *
 * XXX: we should traverse the nodes w.r.t weights (maybe dijkstra's algo...)
 *
 * XXX: we need a better starting point :)
 *
 */
public class BFSSolver extends Solver {

	private int count;

	public BFSSolver(Node [] org_ ) { super(org_); }

	public void solve() {
		for(int i = 0; i < size; i++) org[i].extra1 = org[i].extra2 = 0;

		count = 0;

		IntQueue queue = new IntQueue(size);
		queue.enqueue( (int)(Math.random() * size) );

		while(!queue.empty() ) {
			int curr = queue.dequeue();
			org[curr].extra1 = 1;
			org[curr].extra2 = count++;

			for(int i = 0; i < size; i++) {
				// traverse in the original array-order, TO BE CHANGED
				if(i != curr && (org[i].wlocal[curr] > 0) && org[i].extra1 == 0) {
					org[i].extra1 = -1; // otherwise, we would insert same node multiple times!
					queue.enqueue(i);
				}
			}
		}

		// fix those with no ordering:
		for(int i = 0; i < size; i++) if(org[i].extra1 == 0)  org[i].extra2 = count++;

		// now, sort according to our new DFS order
		for(int i = 0; i < size; i++) solved[ org[i].extra2 ] = org[i];

	}
}
