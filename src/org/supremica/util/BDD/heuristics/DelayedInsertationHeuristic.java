

package org.supremica.util.BDD.heuristics;

import org.supremica.util.BDD.*;

/**
 * interface for heuristics that suggests insertation order of automata in the
 * Delayed* smoothing algorithm.
 *
 * Note that the first automata (last in the queue) MUST be inserted firs!
 *
 */

public abstract class DelayedInsertationHeuristic {

	protected Cluster [] cluster_stack;
	protected int [] order;
	protected int curr;

	public DelayedInsertationHeuristic (Cluster [] stack, int size) {
		cluster_stack = stack;
		order = new int[size];
		curr = 0;
	}


	public void init(int size) {
		curr = size;
		do_order();
	}


	public boolean empty() { return curr == 0;  }
	public Cluster next()  {
		if(curr == 0) return null;
		return cluster_stack [ order[ --curr ] ];
	}

	/**
	 * Here is where all the work is done.
	 *
	 * before entring, cluster_stack holds 'curr' automata with the last beeing the
	 * currently inserted.
	 *
	 * at exit, order[0..curr-1] states the STACK-order decided by the heuristics
	 */
	protected abstract void do_order();




}