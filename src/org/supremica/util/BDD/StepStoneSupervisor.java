

package org.supremica.util.BDD;

import java.util.*;

/**
 * StepStone reachability, a mixture of the workset algo and Valmari's P1 Fixed-point
 * heuristics
 *
 */

public class StepStoneSupervisor extends WorksetSupervisor {

    /** Constructor, passes to the base-class */
    public StepStoneSupervisor(BDDAutomata manager, BDDAutomaton [] as) {
		super(manager,as);

    }


    /** Constructor, passes to the base-class */
    public StepStoneSupervisor(BDDAutomata manager, Group plant, Group spec) {
		super(manager, plant, spec);
    }

    /** C++-style Destructor to cleanup unused BDD trees*/
    public void cleanup() {
		super.cleanup();
    }

    // --------------------------------------------------------
	protected int internal_computeReachablesWorkset(int bdd_i) {
		// statistic stuffs
		GrowFrame gf = BDDGrow.getGrowFrame(manager, "Forward reachability" + type());

		timer.reset();
		SizeWatch.setOwner("StepStoneSupervisor.computeReachables");
		Workset workset = getWorkset(false);

		int r_all = manager.ref(bdd_i);

		while(!workset.empty()) {
			int p = workset.pickOne();
			int r_all_old = r_all;

			int tmp = manager.relProd(clusters[p].getTwave() , r_all, s_cube);
			int tmp2 = manager.replace(tmp, perm_sp2s);
			manager.deref(tmp);

			r_all = manager.orTo(r_all, tmp2);
			manager.deref(tmp2);

			workset.exclusive_advance(p, (r_all != r_all_old) );
			if(gf != null) gf.add(r_all);
		}


		if(gf != null) gf.stopTimer();

		timer.report("Forward reachables found (step stone)");
		workset.done();

		return r_all;

	}



	protected int internal_computeCoReachablesWorkset(int m_all)
	{

		// statistic stuffs
		GrowFrame gf = BDDGrow.getGrowFrame(manager, "Backward reachability" + type());

		timer.reset();

		SizeWatch.setOwner("StepStoneSupervisor.computeReachables");
		Workset workset = getWorkset(false);

		int r_all = manager.replace(m_all, perm_s2sp);

		while(!workset.empty()) {
			int p = workset.pickOne();
			int r_all_old = r_all;

			int tmp = manager.relProd(clusters[p].getTwave(), r_all, sp_cube);
			int tmp2 = manager.replace(tmp, perm_s2sp);
			manager.deref(tmp);
			r_all = manager.orTo(r_all, tmp2);
			manager.deref(tmp2);

			workset.exclusive_advance(p, (r_all != r_all_old) );
			if (gf != null)	gf.add( r_all );
		}


		int ret = manager.replace(r_all, perm_sp2s);
		manager.deref(r_all);

		if(gf != null) gf.stopTimer();
		SizeWatch.report(bdd_reachables, "Qr");
		timer.report("Backward reachables found (step stone)");
		workset.done();
		return ret;
	}
}
