package org.supremica.util.BDD;

/**
 *
 * This is where the workset algorithm is performed.
 *
 * This is an optimization of the simple disjunctive algorithm when we choose a
 * new automaton to traverse only if something has happened earlier that may affect
 * that automata.
 *
 *  algorithm:
 *
 *  \Delta = { all automata }
 * while \Delta \neq \emptyset {
 *     "pick_one" automaton i from \Delta
 *     R´:= R;
 *     R := Forward(R, \wave{\delta}^i);
 *     if (R \neq R') {
 *            \Delta := \Delta \cup "affect"(i);
 *     }
 * }
 *
 * in this implementation, we use these simple assumptions
 * affect(automaton A) = {set of all automata that share events ( "interacts" ) with A (see also DependencySet) }
 * pick_one(from \Delta) = pick the automata that has been  "affected" min/most number of times
 *                        ( see function pick_one for this)
 *
 * For better performance, we use the disjunctive clusters instead of the automata itself here
 *
 */
public class WorksetSupervisor
	extends DisjSupervisor
{

	/**
	 * dependency matrix:  n = dependent[automata][0] is the number of dependent automata.
   * dependent[automata][1] .. dependent[automata][n] are the dependent automata.
	 */
	protected int[][] dependent;
	protected int size;
	protected BDDAutomaton[] bas;
	protected Cluster[] clusters;
	protected Workset workset;

	/** Constructor, passes to the base-class */
	public WorksetSupervisor(BDDAutomata manager, BDDAutomaton[] as)
	{
		super(manager, as);

		init_worksets();
	}

	/** Constructor, passes to the base-class */
	public WorksetSupervisor(BDDAutomata manager, Group plant, Group spec)
	{
		super(manager, plant, spec);

		init_worksets();
	}

	/** C++-style Destructor to cleanup unused BDD trees*/
	public void cleanup()
	{
		super.cleanup();
	}

	/** this build ups the dependency matrix, once for all */
	private void init_worksets()
	{
		workset = null;    // not created yet
		size = dop.getSize();
		clusters = dop.getClusters();

		dependent = new int[size][size + 1];

		for (int i = 0; i < size; i++)
		{
			dependent[i][0] = 0;
		}

		for (int i = 0; i < size; i++)
		{
			for (int j = i + 1; j < size; j++)
			{
				if (clusters[i].interact(clusters[j]))
				{
					dependent[i][1 + dependent[i][0]++] = j;
					dependent[j][1 + dependent[j][0]++] = i;
				}
			}
		}
	}

	/** workset to start from: all automata are enabled */
	protected Workset getWorkset(boolean monotonic)
	{
		if (workset == null)
		{
			workset = new Workset(clusters, size, dependent);
		}

		workset.init_workset(monotonic);

		return workset;
	}

	// ------------------------------------------------------------------------------------------------------
	// TODO: creating these two would require major changes to the workset algorithm :(
	// public int getReachables(boolean [] events)
	// public int getReachables(boolean [] events, int intial_states)

	/**
	 * do a FORWARD reachability search.
	 * start from the given (set of) initial state(s)
	 */
	public int getReachables(int initial_states)
	{
		return internal_computeReachablesWorkset(initial_states);
	}

	/**
	 * do a FORWARD reachability search fro mthe initial state
	 */
	protected void computeReachables()
	{
		int i_all = manager.and(plant.getI(), spec.getI());
		int ret = internal_computeReachablesWorkset(i_all);

		manager.deref(i_all);

		has_reachables = true;
		bdd_reachables = ret;
	}

	protected int internal_computeReachablesWorkset(int bdd_i)
	{

		// statistic stuffs
		GrowFrame gf = BDDGrow.getGrowFrame(manager, "Forward reachability" + type());

		timer.reset();
		SizeWatch.setOwner("WorksetSupervisor.computeReachables");

		Workset workset = getWorkset(false);
		int r_all_p, r_all = manager.ref(bdd_i);

		if(Options.profile_on) {
			Options.out.println("Disj_forward  Level-1 dep: " + getLevel1Dependency(true));
		}

		// initial burst mode
		if (Options.burst_mode)
		{
			for (int i = 0; i < clusters.length; i++)
			{
				do
				{
					r_all_p = r_all;

					int tmp = manager.relProd(clusters[i].getTwave(), bdd_i, s_cube);
					int tmp2 = manager.replace(tmp, perm_sp2s);

					manager.deref(tmp);

					r_all = manager.orTo(r_all, tmp2);

					manager.deref(tmp2);

					if (gf != null)
					{
						gf.add(r_all);
					}
				}
				while (r_all_p != r_all);
			}

			if (gf != null)
			{
				gf.mark("Burst done");
			}
		}

		limit.reset();

		while (!workset.empty() &&!limit.stopped())
		{
			int p = workset.pickOne();
			int r_all_org = r_all;

			do
			{
				r_all_p = r_all;

				int tmp = manager.relProd(clusters[p].getTwave(), r_all, s_cube);
				int tmp2 = manager.replace(tmp, perm_sp2s);

				manager.deref(tmp);

				r_all = manager.orTo(r_all, tmp2);

				manager.deref(tmp2);

				if (gf != null)
				{
					gf.add(r_all);
				}
			}
			while (r_all_p != r_all);

			workset.advance(p, r_all != r_all_org);
		}

		if (gf != null)
		{
			gf.stopTimer();
		}

		timer.report("Forward reachables found (workset)");
		workset.done();

		return r_all;
	}

	// ---------------------------------------------------------------------------------------
	protected void computeCoReachables()
	{
		int m_all = GroupHelper.getM(manager, spec, plant);

		bdd_coreachables = internal_computeCoReachablesWorkset(m_all);
		has_coreachables = true;

		manager.deref(m_all);
	}

	protected int internal_computeCoReachablesWorkset(int m_all)
	{

		// statistic stuffs
		GrowFrame gf = BDDGrow.getGrowFrame(manager, "Backward reachability" + type());

		timer.reset();
		SizeWatch.setOwner("WorksetSupervisor.computeCoReachables");

		Workset workset = getWorkset(false);
		int r_all_p, r_all = manager.replace(m_all, perm_s2sp);

		if(Options.profile_on) {
			Options.out.println("Disj_backward Level-1 dep: " + getLevel1Dependency(false));
		}

		// initial burst mode:
		if (Options.burst_mode)
		{
			for (int i = 0; i < clusters.length; i++)
			{
				do
				{
					r_all_p = r_all;

					int tmp = manager.relProd(clusters[i].getTwave(), m_all, sp_cube);
					int tmp2 = manager.replace(tmp, perm_s2sp);

					manager.deref(tmp);

					r_all = manager.orTo(r_all, tmp2);

					manager.deref(tmp2);

					NodeCountStatistics.getInstance().addBdd(r_all, "Backward reachability");
					if (gf != null)
					{
						gf.add(r_all);
					}
				}
				while (r_all_p != r_all);
			}

			if (gf != null)
			{
				gf.mark("Burst done");
			}
		}

		limit.reset();

		while (!workset.empty() &&!limit.stopped())
		{
			int p = workset.pickOne();
			int r_all_org = r_all;

			do
			{
				r_all_p = r_all;

				int tmp = manager.relProd(clusters[p].getTwave(), r_all, sp_cube);
				int tmp2 = manager.replace(tmp, perm_s2sp);

				manager.deref(tmp);

				r_all = manager.orTo(r_all, tmp2);

				manager.deref(tmp2);

				if (gf != null)
				{
					gf.add(r_all);
				}
			}
			while (r_all_p != r_all);

			workset.advance(p, r_all != r_all_org);
		}

		int ret = manager.replace(r_all, perm_sp2s);

		manager.deref(r_all);

		if (gf != null)
		{
			gf.stopTimer();
		}

		SizeWatch.report(ret, "Qr");
		timer.report("Backward reachables found (workset)");
		workset.done();

		return ret;
	}


	// --- [ safe state supervisory stuff ] -----------------------------------------

	protected int restrictedBackward(int marked, int forbidden)
	{

		// statistic stuffs
		GrowFrame gf = BDDGrow.getGrowFrame(manager, "restrictedBackward " + type());

		timer.reset();
		SizeWatch.setOwner("WorksetSupervisor.restrictedBackward");

		Workset workset = getWorkset(false);
		int r_permitted = manager.not(forbidden);
		marked = manager.and(marked, r_permitted);
		int r_all_p, r_all = manager.replace(marked, perm_s2sp);


		while (!workset.empty())
		{
			int p = workset.pickOne();
			int r_all_org = r_all;

			do
			{
				r_all_p = r_all;

				int tmp = manager.relProd(clusters[p].getTwave(), r_all, sp_cube);

				tmp = manager.andTo(tmp, r_permitted);
				int tmp2 = manager.replace(tmp, perm_s2sp);
				manager.deref(tmp);

				r_all = manager.orTo(r_all, tmp2);

				manager.deref(tmp2);

				NodeCountStatistics.getInstance().addBdd(r_all, "restrictedBackward in WorksetSupervisor");
				if (gf != null)
				{
					gf.add(r_all);
				}
			}
			while (r_all_p != r_all);

			workset.advance(p, r_all != r_all_org);
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
		timer.report("restrictedBackward (workset)");
		workset.done();

		return ret;
	}


	// -------------------------------------------------------

	protected int uncontrollableBackward(int states)
	{
		GrowFrame gf = BDDGrow.getGrowFrame(manager, "uncontrollableBackward " + type());

		timer.reset();
		SizeWatch.setOwner("WorksetSupervisor.uncontrollableBackward");

		Workset workset = getWorkset(false);
		int r_all_p, r_all = manager.replace(states, perm_s2sp);

		while (!workset.empty())
		{
			int p = workset.pickOne();
			int r_all_org = r_all;

			do
			{
				r_all_p = r_all;

				int tmp = manager.relProd(clusters[p].getTwaveUncontrollable(), r_all, sp_cube);

				int tmp2 = manager.replace(tmp, perm_s2sp);
				manager.deref(tmp);

				r_all = manager.orTo(r_all, tmp2);

				manager.deref(tmp2);

				NodeCountStatistics.getInstance().addBdd(r_all, "uncontrollableBackward");
				if (gf != null)
				{
					gf.add(r_all);
				}
			}
			while (r_all_p != r_all);

			workset.advance(p, r_all != r_all_org);
		}

		int ret = manager.replace(r_all, perm_sp2s);
		manager.deref(r_all);

		if (gf != null)
		{
			gf.stopTimer();
		}

		SizeWatch.report(ret, "Quncontrollable_backward");
		timer.report("uncontrollableBackward (workset)");
		workset.done();

		return ret;
	}

// -----------------------------------------------------------------
	/**
	 * compute the level-1 dependency set data for this model (only available if disjunctive)
	 */
	public DependencyData getLevel1Dependency(boolean go_forward) {
		if(workset == null) {
			return super.getLevel1Dependency(go_forward);
		} else {
			return workset.getLevel1Dependency(go_forward);
		}
	}
}
