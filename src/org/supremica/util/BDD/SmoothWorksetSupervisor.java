package org.supremica.util.BDD;

/**
 * monotonicly increased smoothed reachability based on conjunctive transition relations
 * the difference to SmoothSupervisor.java is that SmoothWorksetSupervisor.java picks
 * new automatoa/clusters based on the workset algorithm (see Workset.java and
 * WorksetSupervisor.init_worksets() for example).
 *
 * However, SmoothSupervisor.java uses the current BDD ordering which is often better but
 * in some weird cases that I haven't figured it out yet...
 */
public class SmoothWorksetSupervisor
	extends WorksetSupervisor
{
	public SmoothWorksetSupervisor(BDDAutomata manager, Group p, Group sp)
	{
		super(manager, p, sp);
	}

	public SmoothWorksetSupervisor(BDDAutomata manager, BDDAutomaton[] as)
	{
		super(manager, as);
	}

	// ------------------------------------------------------------------------
	protected void computeReachables()
	{
		Workset workset = getWorkset(true);

		// statistic stuffs
		GrowFrame gf = BDDGrow.getGrowFrame(manager, "Forward reachability" + type());

		SizeWatch.setOwner("SmoothWorksetSupervisor.computeReachables");
		timer.reset();

		MonotonicPartition dp = new MonotonicPartition(manager, plant.getSize() + spec.getSize());
		int i_all = manager.and(plant.getI(), spec.getI());
		int r_all_p, r_all = i_all;

		manager.ref(i_all);    //gets derefed by orTo and finally a deref

		// 0/1 smoothing
		int size = dop.getSize();
		Cluster[] clusters = dop.getClusters();

		limit.reset();

		for (int a = 0; (a < size) &&!limit.wasStopped(); a++)
		{
			int p = workset.pickOneExcelsuive();

			dp.add(clusters[p].getTwave());

			if (gf != null)
			{
				gf.mark(clusters[p].toString());
			}

			int r_all_pp, front_s, front_sp;

			do
			{
				r_all_pp = r_all;

				int front = dp.image(r_all);

				r_all = manager.orTo(r_all, front);

				manager.deref(front);

				if (gf != null)
				{
					gf.add(r_all);
				}
			}
			while ((r_all != r_all_pp) &&!limit.stopped());

			workset.advance(p, true);
		}

		// cleanup
		manager.deref(i_all);

		has_reachables = true;
		bdd_reachables = r_all;

		if (gf != null)
		{
			gf.stopTimer();
		}

		SizeWatch.report(r_all, "Qr");
		timer.report("Forward reachables found (smoothed+workset)");

		// SizeWatch.report(r_all, "R");
		dp.cleanup();
	}

	// -------------------------------------------------------------------------------
	protected void computeCoReachables()
	{
		Workset workset = getWorkset(true);
		GrowFrame gf = BDDGrow.getGrowFrame(manager, "Backward reachability" + type());

		SizeWatch.setOwner("SmoothWorksetSupervisor.computeCoReachables");
		timer.reset();

		MonotonicPartition dp = new MonotonicPartition(manager, plant.getSize() + spec.getSize());
		int m_all = GroupHelper.getM(manager, spec, plant);
		int r_all_p, r_all = manager.replace(m_all, perm_s2sp);    // gets derefed in first orTo ??

		manager.deref(m_all);

		if (Options.local_saturation)
		{

			// TODO: compute saturated m_all (r_all right now)
		}

		SizeWatch.report(r_all, "Qm");

		// 0/1 smoothing
		int size = dop.getSize();
		Cluster[] clusters = dop.getClusters();

		limit.reset();

		for (int a = 0; (a < size) &&!limit.stopped(); a++)
		{
			int p = workset.pickOneExcelsuive();

			dp.add(clusters[p].getTwave());

			if (gf != null)
			{
				gf.mark(clusters[p].toString());
			}

			int r_all_org, r_all_pp, front_s, front_sp;

			r_all_org = r_all;

			do
			{
				r_all_pp = r_all;

				int front = dp.preImage(r_all);

				r_all = manager.orTo(r_all, front);

				manager.deref(front);

				if (gf != null)
				{
					gf.add(r_all);
				}
			}
			while ((r_all != r_all_pp) &&!limit.stopped());

			workset.advance(p, true);
		}

		int ret = manager.replace(r_all, perm_sp2s);

		// cleanup:
		manager.deref(r_all);

		has_coreachables = true;
		bdd_coreachables = ret;

		if (gf != null)
		{
			gf.stopTimer();
		}

		SizeWatch.report(bdd_coreachables, "Qco");
		timer.report("Co-reachables found (smoothed+workset)");
		dp.cleanup();

		// SizeWatch.report(bdd_coreachables,"Coreachables");
	}
}
