package org.supremica.util.BDD;

import java.util.*;


/**
 * monotonicly increased smoothed reachability with 1 step delay.
 * lets the new automata to run until it reachaes fixpoint, then it returns the new result
 */


public class  DelayedSmoothSupervisor extends SmoothSupervisor {

    public DelayedSmoothSupervisor(BDDAutomata manager, Group p, Group sp) {
		super(manager,p,sp);
    }

    public DelayedSmoothSupervisor(BDDAutomata manager, BDDAutomaton [] as) {
		super(manager,as);
    }


	public String toString() {
		return "delayed smothing";
	}

	// compute the forward image
	protected int delay_forward(GrowFrame gf, Cluster c, int r) {
		int r_old = r, t = c.twave;

		do {
			r_old = r;
			int tmp1 = manager.relProd(t, r, s_cube);
			int tmp2 = manager.replace(tmp1, perm_sp2s);
			manager.deref(tmp1);
			r = manager.orTo(r, tmp2);
			manager.deref(tmp2);

			if(gf != null)    gf.add( r );

		} while(r != r_old);
		if(gf != null) gf.mark( "Releasing " + c.toString() );
		return r;
	}

	protected int delay_backward(GrowFrame gf, Cluster c, int r) {
		int r_old = r, t = c.twave;

		do {
			r_old = r;
			int tmp1 = manager.relProd(t, r, sp_cube);
			int tmp2 = manager.replace(tmp1, perm_s2sp);
			manager.deref(tmp1);
			r = manager.orTo(r, tmp2);
			manager.deref(tmp2);

			if(gf != null)    gf.add( r );

		} while(r != r_old);
		if(gf != null) gf.mark( "Releasing " + c.toString() );
		return r;
	}
}