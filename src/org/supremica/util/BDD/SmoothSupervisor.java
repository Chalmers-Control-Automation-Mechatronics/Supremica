

package org.supremica.util.BDD;

/**
 * monotonicly increased smoothed reachability based on conjunctive transition relations
 *
 * see also SmoothWorksetSupervisor.java which uses the same algorithm but picks new
 * automata/clusters by using the workset algorithm...
 *
 * This one is better (time & space) in general, but does really bad in some special
 * cases (shoefactory comes to my mind...).
 */

// XXX: when choosing new automaton to be added:
// changing the order makes things much 'smoother' for serial stuff such as
// the wonhams production lines, BUT it uses gargabe-collection much more often.
// dont know why this happen yet...
//
// Compare results for 20 cell FSM:
//
// type        max-size     step-count   time (not on the same machine, forward ~ 2x faster)
// forward:
// revese:     1873           9190        177s
//              (reverse order is not affected is saturation is enabled ??)
//
// Note: seems like GrowFrame was guilty to a noticable part of the running-time :(
//


public class SmoothSupervisor extends DisjSupervisor {

    public SmoothSupervisor(BDDAutomata manager, Group p, Group sp) {
		super(manager,p,sp);
    }

    public SmoothSupervisor(BDDAutomata manager, BDDAutomaton [] as) {
		super(manager,as);
    }


	/**
	 * just return my names, make it easy for the subclasses
	 * to reuse my GUI related code that puts my/our name in the title..
	 */
	// public String toString() { return "smoothed"; }


	/**
	 * function called after a new automata is added to the monotonic set
	 * during a FORWARD serach.
	 * r is the current set of reachable states and the function should return
	 * a possibly modified 'r', due to some algorithm or just 'r' if no
	 * such algorithm is used
	 */
	protected int delay_forward(GrowFrame gf, Cluster c, int r) {
		return r;
	}

	/**
	 * function called after a new automata is added to the monotonic set
	 * during a BACKWARD serach.
	 * same as delay_forward() but this time r is the current set of reachable
	 * states in S' (NOT IN S)
	 */

	protected int delay_backward(GrowFrame gf, Cluster c, int r) {
		return r;
	}

	/**
	 * setup data needed for the delay operations (if any).
	 * called during start of a search.
	 *
	 */
	protected void init_delay() {
	}


	/**
	 * cleanup data used in the delay operations (if any).
	 * called after a search ends
	 *
	 */
	protected void cleanup_delay() {
	}

    // ------------------------------------------------------------------------
    protected void computeReachables() {
		// statistic stuffs
		GrowFrame gf = BDDGrow.getGrowFrame(manager, "Forward reachability" + type());

		timer.reset();
		MonotonicPartition dp = new MonotonicPartition(manager, plant.getSize() + spec.getSize());
		init_delay();

		SizeWatch.setOwner("SmoothSupervisor.computeReachables");
		int i_all = manager.and(plant.getI(), spec.getI());

		/*
		  // for some ODD reason, this makes things go SLOWER
		  // and the rachability requires MORE stpes ???
		if(Options.local_saturation) {
			// compute saturated I
			int i_first = i_all;
			i_all = manager.getZero(); manager.ref(i_all);
			BDDAutomaton [] as = gh.getSortedList();
			for(int i = 0; i < gh.getSize(); i++) {
			DependencySet ds = as[i].getDependencySet();
			int i2 = ds.getReachables( ds.getI());
			int i_others = manager.exists(i_first, ds.getCube());
			i2 = manager.andTo(i2, i_others);
			manager.deref(i_others);
			i_all = manager.orTo(i_all, i2);
			manager.deref(i2);
			}
			manager.deref(i_first);
		} // End of computing saturated I
		*/

		int r_all_p, r_all = i_all;
		manager.ref(i_all); //gets derefed by orTo and finally a deref


		// 0/1 smoothing
		int size = dop.getSize();
		Cluster [] clusters = dop.getClusters();

		boolean [] remaining = new boolean[size];
		for(int i = 0; i < size; i++)
			remaining[i] = true;


		limit.reset();
		// for(int a = 0; a < size; a++) {
		for(int a = size-1; a >= 0; a--) {
			if(remaining[a]) {
				remaining[a] = false;
				dp.add(clusters[a].twave);
				if(gf != null) gf.mark( clusters[a].toString() );
				r_all = delay_forward(gf, clusters[a], r_all); // do the 'delay' thing...
			}
			r_all = dp.forward(gf, limit, r_all);
		}




		// cleanup
		manager.deref(i_all);


		has_reachables = true;
		bdd_reachables = r_all;
		SizeWatch.report(r_all, "Qr");
		dp.cleanup();
		cleanup_delay();
		if(gf != null) gf.stopTimer();
		timer.report("Forward reachables found"+ type());
		// SizeWatch.report(r_all, "R");
    }
    // -------------------------------------------------------------------------------

    protected void computeCoReachables() {
		GrowFrame gf = BDDGrow.getGrowFrame(manager, "Backward reachability"+ type() );

		timer.reset();
		MonotonicPartition dp = new MonotonicPartition(manager, plant.getSize() + spec.getSize());
		init_delay();

		SizeWatch.setOwner("SmoothSupervisor.computeCoReachables");


		int m_all = GroupHelper.getM(manager, spec, plant);

		// gets derefed in first orTo ??
		int r_all_p, r_all = manager.replace(m_all, perm_s2sp);
		manager.deref(m_all);


		if(Options.local_saturation) {
			// TODO: compute saturated m_all (r_all right now)
		}

		SizeWatch.report(r_all, "Qm");

		// 0/1 smoothing
		int size = dop.getSize();
		Cluster [] clusters = dop.getClusters();
		boolean [] remaining = new boolean[size];
		for(int i = 0; i < size; i++)
			remaining[i] = true;



		limit.reset();
		for(int a = 0; a < size; a++) {
			if(remaining[a]) {
				remaining[a] = false;
				dp.add(clusters[a].twave);
				if(gf != null) gf.mark( clusters[a].toString() );
				r_all = delay_backward(gf, clusters[a], r_all); // do the 'delay' thing...
			}
			r_all = dp.backward(gf, limit, r_all);
		}


		int ret = manager.replace(r_all, perm_sp2s);

		// cleanup:
		manager.deref(r_all);


		has_coreachables = true;
		bdd_coreachables = ret;

		if(gf != null) gf.stopTimer();
		SizeWatch.report(bdd_coreachables, "Qco");
		timer.report("Co-reachables found ("+ toString() +")");

		dp.cleanup();
		cleanup_delay();
		// SizeWatch.report(bdd_coreachables,"Coreachables");
    }

}
