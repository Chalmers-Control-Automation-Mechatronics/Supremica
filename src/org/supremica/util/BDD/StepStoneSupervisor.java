package org.supremica.util.BDD;

/**
 * StepStone reachability, a mixture of the workset algo and Valmari's P1 Fixed-point
 * heuristics
 *
 */
public class StepStoneSupervisor
	extends WorksetSupervisor
{

	/** Constructor, passes to the base-class */
	public StepStoneSupervisor(BDDAutomata manager, BDDAutomaton[] as)
	{
		super(manager, as);
	}

	/** Constructor, passes to the base-class */
	public StepStoneSupervisor(BDDAutomata manager, Group plant, Group spec)
	{
		super(manager, plant, spec);
	}

	/** C++-style Destructor to cleanup unused BDD trees*/
	public void cleanup()
	{
		super.cleanup();
	}

	// --------------------------------------------------------
	protected int internal_computeReachablesWorkset(int bdd_i)
	{

		// statistic stuffs
		GrowFrame gf = BDDGrow.getGrowFrame(manager, "Forward reachability" + type());

		timer.reset();
		SizeWatch.setOwner("StepStoneSupervisor.computeReachables");

		Workset workset = getWorkset(false);
		int r_all = manager.ref(bdd_i);

		limit.reset();

		while (!workset.empty() &&!limit.stopped())
		{
			int p = workset.pickOne();
			int r_all_old = r_all;
			int tmp = manager.relProd(clusters[p].getTwave(), r_all, s_cube);
			int tmp2 = manager.replace(tmp, perm_sp2s);

			manager.deref(tmp);

			r_all = manager.orTo(r_all, tmp2);

			manager.deref(tmp2);
			workset.nonfixpoint_advance(p, (r_all != r_all_old));

			if (gf != null)
			{
				gf.add(r_all);
			}
		}

		if (gf != null)
		{
			gf.stopTimer();
		}

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

		limit.reset();

		while (!workset.empty() &&!limit.stopped())
		{
			int p = workset.pickOne();
			int r_all_old = r_all;
			int tmp = manager.relProd(clusters[p].getTwave(), r_all, sp_cube);
			int tmp2 = manager.replace(tmp, perm_s2sp);

			manager.deref(tmp);

			r_all = manager.orTo(r_all, tmp2);

			manager.deref(tmp2);
			workset.nonfixpoint_advance(p, (r_all != r_all_old));

			if (gf != null)
			{
				gf.add(r_all);
			}
		}

		int ret = manager.replace(r_all, perm_sp2s);

		manager.deref(r_all);

		if (gf != null)
		{
			gf.stopTimer();
		}

		SizeWatch.report(bdd_reachables, "Qr");
		timer.report("Backward reachables found (step stone)");
		workset.done();

		return ret;
	}


	// --- [ safe state supervisory stuff ] -----------------------------------------
	// XXX: this fails when doing supNBC on AGV!!
	protected int restrictedBackward(int marked, int forbidden)
	{

		// statistic stuffs
		GrowFrame gf = BDDGrow.getGrowFrame(manager, "restrictedBackward " + type());
		SizeWatch.setOwner("StepStoneSupervisor.restrictedBackward");
		timer.reset();

		Workset workset = getWorkset(false);
		int r_permitted = manager.not(forbidden);

		marked = manager.and(marked, r_permitted);
		int r_all = manager.replace(marked, perm_s2sp);

		while (!workset.empty())
		{
			int p = workset.pickOne();

			int tmp = manager.relProd(clusters[p].getTwave(), r_all, sp_cube);
			tmp = manager.andTo(tmp, r_permitted);
			int tmp2 = manager.replace(tmp, perm_s2sp);
			manager.deref(tmp);


			int r_all_old = r_all;
			r_all = manager.orTo(r_all, tmp2);
			workset.nonfixpoint_advance(p, r_all != r_all_old);

			manager.deref(tmp2);


			if (gf != null)
			{
				gf.add(r_all);
			}
		}

		int ret = manager.replace(r_all, perm_sp2s);

		manager.deref(marked);
		manager.deref(r_all);
		manager.deref(r_permitted);

		if (gf != null)
		{
			gf.stopTimer();
		}

		SizeWatch.report(ret, "Qrestricted_backward");
		timer.report("restrictedBackward (StepStone)");
		workset.done();

		return ret;
	}


	// -------------------------------------------------------

	// XXX: maybe this also fails, but just not on the AGV model???
	protected int uncontrollableBackward(int states)
	{
		GrowFrame gf = BDDGrow.getGrowFrame(manager, "uncontrollableBackward " + type());

		timer.reset();
		SizeWatch.setOwner("StepStoneSupervisor.uncontrollableBackward");

		Workset workset = getWorkset(false);
		int r_all_p, r_all = manager.replace(states, perm_s2sp);



		while (!workset.empty())
		{
			int p = workset.pickOne();


			int tmp = manager.relProd(clusters[p].getTwaveUncontrollable(), r_all, sp_cube);
			int tmp2 = manager.replace(tmp, perm_s2sp);
			manager.deref(tmp);

			int r_all_old = r_all;
			r_all = manager.orTo(r_all, tmp2);
			workset.nonfixpoint_advance(p, (r_all != r_all_old));
			manager.deref(tmp2);

			if (gf != null)
			{
				gf.add(r_all);
			}
		}

		int ret = manager.replace(r_all, perm_sp2s);
		manager.deref(r_all);

		if (gf != null)
		{
			gf.stopTimer();
		}

		SizeWatch.report(ret, "Quncontrollable_backward");
		timer.report("uncontrollableBackward (StepStone)");
		workset.done();

		return ret;
	}

}
