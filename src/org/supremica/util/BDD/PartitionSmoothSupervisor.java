

package org.supremica.util.BDD;

import java.util.*;

public class PartitionSmoothSupervisor extends DisjSupervisor {
    /** Constructor, passes to the base-class */
    public PartitionSmoothSupervisor(BDDAutomata manager, BDDAutomaton [] as) {
	super(manager,as);

    }


    /** Constructor, passes to the base-class */
    public PartitionSmoothSupervisor(BDDAutomata manager, Group plant, Group spec) {
	super(manager, plant, spec);

    }

    /** C++-style Destructor to cleanup unused BDD trees*/
    public void cleanup() {
	super.cleanup();
    }
    // --------------------------------------------------------

    // --------------------------------------------------------
    protected void computeReachables() {
		// statistic stuffs
		GrowFrame gf = BDDGrow.getGrowFrame(manager, "Forward reachability (PartitionSmooth)");

		timer.reset();
		SizeWatch.setOwner("PartitionSmoothSupervisor.computeReachables");

		// DisjPartition dp = getDisjPartition();
		Cluster [] clusters = dop.getClusters();
		int i,j, size = dop.getSize();

		int i_all   = manager.and(plant.getI(), spec.getI());
		int r_all_p, r_all = i_all;
		manager.ref(r_all); //gets derefed by orTo and finally a deref



		i = j = 0;
		do {
			// Util.notify("i = " + i + ", j = " + j + ", n = " + size);
			r_all_p = r_all;

			int tmp = manager.relProd(clusters[j].getTwave(), r_all, s_cube);
			int front= manager.replace( tmp, perm_sp2s);
			r_all = manager.orTo(r_all, front);
			manager.deref(front);
			manager.deref(tmp);

			if(r_all_p == r_all) {
				i =  i + 1;
				j = (j + 1) % size;
			} else i = 0;

			if(gf != null)    gf.add( r_all );

		} while( i < size);


		has_reachables = true;
		bdd_reachables = r_all;

		if(gf != null) gf.stopTimer();
		SizeWatch.report(bdd_reachables, "Qr");
		timer.report("Forward reachables found (PartitionSmooth)");

    }


    protected void computeCoReachables() {
		GrowFrame gf = BDDGrow.getGrowFrame(manager, "backward reachability (PartitionSmooth)");

		timer.reset();
		SizeWatch.setOwner("PartitionSmoothSupervisor.computecoReachables");

		Cluster [] clusters = dop.getClusters();
		int i,j, size = dop.getSize();
		int m_all = GroupHelper.getM(manager, spec, plant);

		// gets derefed in first orTo, but replace addes its own ref
		int r_all_p, r_all = manager.replace(m_all, perm_s2sp);

		// manager.ref(r_all); // gets derefed soon
		manager.deref(m_all); // we dont need m_all anymore


		SizeWatch.report(r_all, "Qm");


		i = j = 0;
		do {
			r_all_p = r_all;

			int tmp = manager.relProd(clusters[j].getTwave(), r_all, sp_cube);
			int tmp2= manager.replace( tmp, perm_s2sp);
			r_all = manager.orTo(r_all, tmp2);
			manager.deref(tmp2);
			manager.deref(tmp);

			if(r_all_p == r_all) {
				i =  i + 1;
				j = (j + 1) % size;
			} else i = 0;

			if(gf != null)    gf.add( r_all );

		} while( i < size);

		// move the result from S' to S:
		int ret = manager.replace(r_all, perm_sp2s);

		// cleanup:
		manager.deref(r_all);

		has_coreachables = true;
		bdd_coreachables = ret;

		if(gf != null) gf.stopTimer();
		SizeWatch.report(bdd_reachables, "Qco");
		timer.report("Co-reachables found (PartitionSmooth)");
    }
}
