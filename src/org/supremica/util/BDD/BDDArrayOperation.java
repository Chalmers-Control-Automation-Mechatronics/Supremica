

package org.supremica.util.BDD;

/**
 * Experimental class for more efficient BDD operations with more than two operand:
 * result = bdd1 @ bdd2 @ ... @ bdd-n
 *
 *
 * The BDDs are assumed to have very different support set (their domains are almost disjoint),
 * which is in the case of, for example, BDDAutomaton transfer functions.
 */

public class BDDArrayOperation {
	private JBDD manager;

	private int [] array;
	private double [] cost; // the cost is currently nothing but distance to the top
	private int curr; // number of operands

	/** create an operations array */
	public BDDArrayOperation(JBDD manager) {
		this.manager = manager;

		// start with some default size
		array = new int[32];
		cost = new double[array.length];
	}


	/** must be called before exiting or you will get BDD ref-count leakage :)*/
	public void cleanup() {
		for(int i = 0; i < curr; i++)
			manager.deref( array[i] );

		curr = 0;
	}

	/** start all over. also frees the previous BDDs */
	public void reset() {
		cleanup();
	}

	// ----------------------------------------------------
	/** need to grow the table ? */
	private void grow() {
		int new_size = array.length * 3 + 1;

		int [] tmp1 = new int[new_size];
		double [] tmp2 = new double[new_size];

		for(int i = 0; i < array.length; i++) {
			tmp1[i] = array[i];
			tmp2[i] = cost[i];
		}

		array = tmp1;
		cost = tmp2;
	}

	/** add another BDD to this array */
	public void add(int bdd) {

		if(curr == array.length) grow();

		array[curr] = manager.ref(bdd);
		cost[curr] = (double) manager.internal_index(bdd);
		curr++;
	}



	/**
	 * compute the conjunction of all BDDs
	 * <p>NOTE: user is responsible for freeing the returned BDD.
	 */
	public int andAll() {

		// top automata first
		QuickSort.sort(array, cost, curr, false);

		return rec_and(0, curr-1);


		/*
		// the sequential version of rec_and() :
		int mult = manager.ref( array[0] );
		for(int i = 1; i < curr; i++) {
			mult = manager.andTo(mult, array[i]);
			// Options.out.println("i = "+ i + ", SIZE = " + manager.nodeCount(mult) );
		}

		return mult;
		*/

	}

	// recursive binary decomposition of AND of an array
	private int rec_and(int start, int end) {
		int ret = -1;
		if(start == end) ret = manager.ref( array[start]);
		else if(start == end-1) {
			ret = manager.and( array[start], array[end]);
		} else {
			int w = (start + end) / 2;
			int a1 = rec_and(start, w);
			int a2 = rec_and(w+1, end);
			ret = manager.and(a1, a2);
			manager.deref(a1);
			manager.deref(a2);
		}

		// DEBUG
		// if(end - start  > 5) Options.out.println("start=" + start + ",end=" + end + ",SIZE=" + manager.nodeCount(ret) );

		return ret;
	}

}
