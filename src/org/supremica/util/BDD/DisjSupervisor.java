

package org.supremica.util.BDD;

import java.util.*;

public class DisjSupervisor extends ConjSupervisor {
    protected DisjOptimizer dop;
    protected DisjPartition disj_partition;
    protected int disj_size;

    /** Constructor, passes to the base-class */
    public DisjSupervisor(BDDAutomata manager, BDDAutomaton [] as) {
		super(manager,as);
		init_disj();
    }


    /** Constructor, passes to the base-class */
    public DisjSupervisor(BDDAutomata manager, Group plant, Group spec) {
		super(manager, plant, spec);
		init_disj();
    }

    /** C++-style Destructor to cleanup unused BDD trees*/
    public void cleanup() {
		if(disj_partition != null) {
			disj_partition.cleanup();
			disj_partition = null;
		}
		dop.cleanup();
		super.cleanup();
    }
    // --------------------------------------------------------
    private void init_disj() {
		disj_partition = null; // not needed yet
		dop = new DisjOptimizer(manager, gh);
		disj_size  = dop.getSize();
    }
    protected DisjPartition getDisjPartition() {
		if(disj_partition == null) computeDisjPartition();
		return disj_partition;
    }
    private void computeDisjPartition() {
		disj_partition = new DisjPartition(manager, dop);
    }




    protected int internal_computeReachablesDisj(int i_all) {
		// statistic stuffs
		GrowFrame gf = null;
		if(Options.show_grow)
			gf = new GrowFrame("Forward reachability (disjunctive)");

		timer.reset();
		DisjPartition dp = getDisjPartition();
		SizeWatch.setOwner("DisjSupervisor.computeReachables");
		int r_all_p, r_all = i_all, front = i_all;
		manager.ref(i_all); //gets derefed by orTo and finally a deref
		manager.ref(front); // get derefed


		do {
			r_all_p = r_all;

			int new_front = dp.image(front);
			r_all = manager.orTo(r_all, new_front);
			manager.deref(front);
			front = new_front;

			if(gf != null)    gf.add( manager.nodeCount( r_all));
		} while(r_all_p != r_all);


		manager.deref(front);


		if(gf != null) gf.stopTimer();
		SizeWatch.report(bdd_reachables, "Qr");
		timer.report("Forward reachables found (disjunctive)");
		// SizeWatch.report(r_all, "R");
		return r_all;
	}


    // --------------------------------------------------------
    protected void computeReachables() {

		int i_all = manager.and(plant.getI(), spec.getI());
		int ret = internal_computeReachablesDisj(i_all);
		manager.deref(i_all);

		has_reachables = true;
		bdd_reachables = ret;
    }

	// -------------------------------
    protected void computeCoReachables() {

		GrowFrame gf = null;
		if(Options.show_grow) gf = new GrowFrame("backward reachability (disjuncted)");

		timer.reset();
		DisjPartition dp = getDisjPartition();
		SizeWatch.setOwner("DisjSupervisor.computecoReachables");

		int permute1 = manager.getPermuteS2Sp();
		int permute2 = manager.getPermuteSp2S();

		int m_all = GroupHelper.getM(manager, spec, plant);

		// gets derefed in first orTo, but replace addes its own ref
		int r_all_p, r_all = manager.replace(m_all, permute1);
		int front = r_all;

		manager.deref(m_all); // we dont need m_all anymore
		manager.ref(front); // gets derefed sson

		SizeWatch.report(r_all, "Qm");


		do {

			r_all_p = r_all;
			int new_front = dp.preImage(front);
			r_all = manager.orTo(r_all, new_front);
			manager.deref(front);
			front = new_front;
			if(gf != null)    gf.add( manager.nodeCount( r_all));
		} while(r_all != r_all_p);

		// move the result from S' to S:
		int ret = manager.replace(r_all, permute2);

		// cleanup:
		manager.deref(front);
		manager.deref(r_all);

		has_coreachables = true;
		bdd_coreachables = ret;

		SizeWatch.report(bdd_reachables, "Qco");
		timer.report("Co-reachables found (disjuncted)");

		if(gf != null) gf.stopTimer();
		// SizeWatch.report(bdd_coreachables,"Coreachables");
	}

}
