
package org.supremica.util.BDD.solvers;

import org.supremica.util.BDD.*;

import java.util.*;


/**
 * ordering solver based on simulated annealing.
 *
 * Taken from Z Zhang's Master thesis:
 *   "Smart TCT: an efficient algorithm for supervisory control design"
 *   algorithm "findBestOrder", page 55
 *
 * To honour Zhang, we have keept his structure of the code as in his thesis
 *
 */

public class STCTSolver extends Solver {

	public Node [] work;

	public STCTSolver(Node [] org_ ) {	super(org_); }


	public void solve() {
		double overallResult = Double.POSITIVE_INFINITY;
		double oldResult = Double.POSITIVE_INFINITY;
		work = new Node[size];

		double [] Force = new double[size];

		for(int n = 0; n < 3 * size; n++) {
			initialize_random_order();
			for(int step = size/2; step > 0; step--) {
				boolean changed = true;
				while(changed) {
					changed = false;

					for(int i = 0; i < size; i++) Force[i] = force(i);
					while(true) {
						int i = max(Force);
						int newPosition = -1;

						if(Force[i] == 0) break;
						if(Force[i] > 0) {
							newPosition = i +step;
							if(newPosition >= size) newPosition = size - 1;
						} else {
							newPosition = i - step;
							if(newPosition < 0) newPosition = 0;
						}

						// swap the order of element i and newPosition
						Node tmp = work[i]; work[i] = work[newPosition]; work[newPosition] = tmp;
						double tmpd = Force[i]; Force[i] = Force[newPosition]; Force[newPosition] = tmpd;

						double newResult = eval();
						if(newResult < oldResult) {
							changed = true;
							oldResult = newResult;
							step = size / 2;
							break;
						} else {
							// swap back
							tmp = work[i]; work[i] = work[newPosition]; work[newPosition] = tmp;
							tmpd = Force[i]; Force[i] = Force[newPosition]; Force[newPosition] = tmpd;
						}
						Force[i] = 0;
					}
				}
			}

			if(oldResult < overallResult) {
				for(int i = 0; i < size; i++) solved[i] = work[i];
				overallResult = oldResult;
			}
		}


		// OK, WHAT IF size == 1 OR SOMETHING LIKE THAT ??
		if(overallResult == Double.POSITIVE_INFINITY) {
			for(int i = 0; i < size; i++) solved[i] = org[i];
		}
	}


	/** return the index of the max element in this vector */
	private int max(double [] vector) {
		int best_index = 0;
		double best = Double.NEGATIVE_INFINITY;

		for(int i = 0; i < size; i++) {
			if(vector[i] < best) {
				best = vector[i];
				best_index = i;
			}
		}
		return best_index;
	}

	/**
	 * Force evaluates how likely a given component should be moved to another location
	 * and in which direction it should go
	 */

	private int force(int k) {
		int sum = 0;

		int me = work[k].extra1;
		for(int i = 0; i < size; i++) {
			int curr = work[i].extra1;
			if(i != k && work[k].wlocal[curr] > 0) sum += ( i - k);
		}
		return sum;
	}

	/** the number of crossing component i */
	private double cross(int i) {
		double sum = 0;
		for(int j = 0; j < i; j++)
			for(int k = i+1; k < size; k++)	{
			sum += work[k].wlocal[ work[j].extra1];
		}
		return sum;
	}

	/**
	 * The overall crossing, the measure of the optimality of the given ordering.
	 * Accroding to Zhang, the number "4" is only here to make the sum grow faster for large crosse(i)'es
	 */
	private double eval() {
		double sum = 0;
		for(int i = 0; i < size; i++)
			sum += Math.pow(4, cross(i) );
		return sum;
	}

	/** just create some random order to begin with */
	private void initialize_random_order() {

		for(int i = 0; i < size; i++) {
			work[i] = org[i];
			work[i].extra1 = i; // good to have an index
		}

		for(int i = 0; i < size; i++) {
			int j = (int)(Math.random() * size);
			Node tmp = work[i];
			work[i] = work[j];
			work[j] = tmp;
		}

	}
}