package org.supremica.util.BDD;


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
		GrowFrame gf = BDDGrow.getGrowFrame(manager, "Forward reachability" + type());
		timer.reset();
		SizeWatch.setOwner("PathSmoothSupervisor.computeReachables");

		PathSmoothPartition psp = new PathSmoothPartition(manager, dop.getClusters(), dop.getSize(), true);

		int i_all   = manager.and(plant.getI(), spec.getI());
		int r_all_p, r_all = i_all;

		limit.reset();
		do {
			do {
				r_all_p = r_all;

				int tmp = psp.image(r_all);
				r_all = manager.orTo(r_all, tmp);
				manager.deref( tmp );

				if(gf != null)    gf.add( r_all );
			} while(r_all_p != r_all && ! limit.stopped());
		} while(psp.step() && !limit.stopped());


		// cleanup
		psp.cleanup();

		has_reachables = true;
		bdd_reachables = r_all;

		if(gf != null) gf.stopTimer();
		SizeWatch.report(bdd_reachables, "Qr");
		timer.report("Forward reachables found (path smoothed)");
	}


	protected void computeCoReachables() {
		GrowFrame gf = BDDGrow.getGrowFrame(manager, "Backward reachability" + type() );

		timer.reset();
		SizeWatch.setOwner("PathSmoothSupervisor.computeCoReachables");
		PathSmoothPartition psp = new PathSmoothPartition(manager, dop.getClusters(), dop.getSize(), false);

		int m_all = GroupHelper.getM(manager, spec, plant);
		int r_all_p, r_all;
		r_all = manager.replace(m_all, perm_s2sp);
		manager.deref(m_all);


		SizeWatch.report(r_all, "Qm");


		limit.reset();
		do {
			do {
				r_all_p = r_all;
				int tmp = psp.preImage(r_all);
				r_all = manager.orTo(r_all, tmp);
				manager.deref(tmp);

				if(gf != null)    gf.add( r_all );
			} while(r_all_p != r_all && !limit.stopped());
		} while(psp.step() && !limit.stopped());

		has_coreachables = true;
		bdd_coreachables = manager.replace(r_all, perm_sp2s);

		// cleanup:
		psp.cleanup();

		if(gf != null) gf.stopTimer();
		SizeWatch.report(bdd_coreachables, "Qco");
		timer.report("Co-reachables found (path smoothed)");
    }
}