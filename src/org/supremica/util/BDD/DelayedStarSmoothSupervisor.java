package org.supremica.util.BDD;

import java.util.*;


/**
 *
 * When I finally code this, it should delay all _each_ automata, such that
 *  1. it starts with the currently added automata (Cluster c in the argument)
 *  2. it always adds the "most relavant" automata.
 *  3. (we could use MonotonicPartition for this :)
 *
 */


public class  DelayedStarSmoothSupervisor extends DelayedSmoothSupervisor {

    public DelayedStarSmoothSupervisor(BDDAutomata manager, Group p, Group sp) {
		super(manager,p,sp);
		System.err.println("This algorithm is not implemented yet :(");
    }

    public DelayedStarSmoothSupervisor(BDDAutomata manager, BDDAutomaton [] as) {
		super(manager,as);
		System.err.println("This algorithm is not implemented yet :(");
    }


	public String toString() {
		return "delayed* smothing";
	}

	// compute the forward image
	protected int delay_forward(GrowFrame gf, Cluster c, int r) {
		return r; // TODO
	}

	protected int delay_backward(GrowFrame gf, Cluster c, int r) {
		return r; // TODO
	}
}