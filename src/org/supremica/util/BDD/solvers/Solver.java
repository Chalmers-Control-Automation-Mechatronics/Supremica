

package org.supremica.util.BDD.solvers;

/**
 * Ordering solver base class.
 *
 * "solvers" are used in the OrderingSolver, which is used by the AOH_TSP heuristic
 *
 *
 */
public abstract class Solver {
	protected int size;
	protected Node [] org,solved;
	public Solver(Node [] org_) {
		this.org = org_;
		this.size = org_.length;
		solved = new Node[size];
		solve();
	}
	public Node [] getShortestPath() { return solved; }
	public abstract void solve();
}