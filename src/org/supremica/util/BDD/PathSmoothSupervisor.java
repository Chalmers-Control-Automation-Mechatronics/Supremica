package org.supremica.util.BDD;

import java.util.*;


public class PathSmoothSupervisor extends DisjSupervisor
{



    /** Constructor, passes to the base-class */
    public PathSmoothSupervisor(BDDAutomata manager, BDDAutomaton [] as) {
		super(manager,as);
    }


    /** Constructor, passes to the base-class */
    public PathSmoothSupervisor(BDDAutomata manager, Group plant, Group spec) {
		super(manager, plant, spec);
    }

    /** C++-style Destructor to cleanup unused BDD trees*/
    public void cleanup() {

		super.cleanup();
    }



    protected void computeReachables() {

		// statistic stuffs
		GrowFrame gf = null;
		if(Options.show_grow) gf = new GrowFrame("Forward reachability (path smoothed)");
		timer.reset();
		SizeWatch.setOwner("PathSmoothSupervisor.computeReachables");


		DisjPartition dp = getDisjPartition();
		PathSmoothPartition psp = new PathSmoothPartition(manager, dp.getClusters(), dp.getNumberOfClusters(), true);

		int i_all   = manager.and(plant.getI(), spec.getI());
		int r_all_p, r_all = i_all, front = i_all;
		manager.ref(i_all); //gets derefed by orTo and finally a deref
		manager.ref(front); // get derefed


		do {
			do {
				r_all_p = r_all;

				int new_front = dp.image(front);
				r_all = manager.orTo(r_all, new_front);
				manager.deref(front);
				front = new_front;

				if(gf != null)    gf.add( manager.nodeCount( r_all));
			} while(r_all_p != r_all);
		} while(psp.step());


		// cleanup
		manager.deref(front);
		manager.deref(i_all);
		psp.cleanup();

		has_reachables = true;
		bdd_reachables = r_all;

		SizeWatch.report(bdd_reachables, "Qr");
		timer.report("Forward reachables found (path smoothed)");
	}


  protected void computeCoReachables() {
	GrowFrame gf = null;
	if(Options.show_grow) gf = new GrowFrame("backward reachability (path smoothed)");

	timer.reset();
	SizeWatch.setOwner("PathSmoothSupervisor.computeCoReachables");
	DisjPartition dp = getDisjPartition();
	PathSmoothPartition psp = new PathSmoothPartition(manager, dp.getClusters(), dp.getNumberOfClusters(), false);

	int permute1 = manager.getPermuteS2Sp();
	int permute2 = manager.getPermuteSp2S();

	int m_all = GroupHelper.getM(manager, spec, plant);
	int front, r_all_p, r_all;
	front = r_all = manager.replace(m_all, permute1);
	manager.deref(m_all);
	manager.ref(r_all);


	SizeWatch.report(r_all, "Qm");


	do {
		do {
			r_all_p = r_all;
			int new_front = dp.preImage(front);
			r_all = manager.orTo(r_all, new_front);
			manager.deref(front);
			front = new_front;

			if(gf != null)    gf.add( manager.nodeCount( r_all));
		} while(r_all_p != r_all);
	} while(psp.step());

	has_coreachables = true;
	bdd_coreachables = manager.replace(r_all, permute2);

	// cleanup:
	psp.cleanup();
	manager.deref(r_all);
	manager.deref(front);


	SizeWatch.report(bdd_coreachables, "Qco");
	timer.report("Co-reachables found (path smoothed)");
	if(gf != null) gf.stopTimer();
	dp.cleanup();
    }
}