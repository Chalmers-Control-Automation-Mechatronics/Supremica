package org.supremica.util.BDD;


public class DisjSupervisor
	extends ConjSupervisor
{
	protected DisjOptimizer dop;
	protected DisjPartition disj_partition;
	protected int disj_size;

	/** Constructor, passes to the base-class */
	public DisjSupervisor(BDDAutomata manager, BDDAutomaton[] as)
	{
		super(manager, as);

		init_disj();
	}

	/** Constructor, passes to the base-class */
	public DisjSupervisor(BDDAutomata manager, Group plant, Group spec)
	{
		super(manager, plant, spec);

		init_disj();
	}

	/** C++-style Destructor to cleanup unused BDD trees*/
	public void cleanup()
	{
		if (disj_partition != null)
		{
			disj_partition.cleanup();

			disj_partition = null;
		}

		dop.cleanup();
		super.cleanup();
	}

	// --------------------------------------------------------
	private void init_disj()
	{
		disj_partition = null;    // not needed yet
		dop = new DisjOptimizer(manager, gh);
		disj_size = dop.getSize();
	}

	protected DisjPartition getDisjPartition()
	{
		if (disj_partition == null)
		{
			computeDisjPartition();
		}

		return disj_partition;
	}

	private void computeDisjPartition()
	{
		disj_partition = new DisjPartition(manager, dop);
	}

	// -----------------------------------------------

	/** return the some of disjunctive T sizes */
	public double sumOfTSize()
	{
		double ret = 0;
		Cluster[] cs = disj_partition.getClusters();
		int size = disj_partition.getNumberOfClusters();    // NOT cs.length !!

		for (int i = 0; i < size; i++)
		{
			ret += manager.nodeCount(cs[i].twave);
		}

		return ret;
	}

	/** get number of disjunctive partitions */
	public int getNumOfPartitions()
	{
		return disj_partition.getNumberOfClusters();
	}

	// ----------------------------------------------
	protected int internal_computeReachablesDisj(int i_all)
	{

		// statistic stuffs
		GrowFrame gf = BDDGrow.getGrowFrame(manager, "Forward reachability" + type());

		timer.reset();

		DisjPartition dp = getDisjPartition();

		SizeWatch.setOwner("DisjSupervisor.computeReachables");

		int r_all_p, r_all = i_all, front = i_all;

		manager.ref(i_all);    //gets derefed by orTo and finally a deref
		manager.ref(front);    // get derefed

		// NOTE: we cant use FrontierSetOptimizer here [ DisjPartition.image() used frontier sets]
		limit.reset();

		do
		{
			r_all_p = r_all;

			int tmp = dp.image(front);

			manager.deref(front);

			r_all = manager.orTo(r_all, tmp);
			front = tmp;

			if (gf != null)
			{
				gf.add(r_all);
			}
		}
		while ((r_all_p != r_all) &&!limit.stopped());

		manager.deref(front);

		if (gf != null)
		{
			gf.stopTimer();
		}

		SizeWatch.report(bdd_reachables, "Qr");
		timer.report("Forward reachables found (disjunctive)");

		// SizeWatch.report(r_all, "R");
		return r_all;
	}

	// --------------------------------------------------------
	protected void computeReachables()
	{
		int i_all = manager.and(plant.getI(), spec.getI());
		int ret = internal_computeReachablesDisj(i_all);

		manager.deref(i_all);

		has_reachables = true;
		bdd_reachables = ret;
	}

	// -------------------------------
	protected void computeCoReachables()
	{
		GrowFrame gf = BDDGrow.getGrowFrame(manager, "backward reachability" + type());

		timer.reset();

		DisjPartition dp = getDisjPartition();

		SizeWatch.setOwner("DisjSupervisor.computecoReachables");

		int m_all = GroupHelper.getM(manager, spec, plant);

		// gets derefed in first orTo, but replace addes its own ref
		int r_all_p, r_all = manager.replace(m_all, perm_s2sp);
		int front = r_all;

		manager.deref(m_all);    // we dont need m_all anymore
		manager.ref(front);    // gets derefed sson
		SizeWatch.report(r_all, "Qm");

		// NOTE: we cant use FrontierSetOptimizer here [ DisjPartition.preImage() used frontier sets]
		limit.reset();

		do
		{
			r_all_p = r_all;

			int tmp = dp.preImage(front);

			manager.deref(front);

			r_all = manager.orTo(r_all, tmp);
			front = tmp;

			if (gf != null)
			{
				gf.add(r_all);
			}
		}
		while ((r_all != r_all_p) &&!limit.stopped());

		// move the result from S' to S:
		int ret = manager.replace(r_all, perm_sp2s);

		// cleanup:
		manager.deref(front);
		manager.deref(r_all);

		has_coreachables = true;
		bdd_coreachables = ret;

		SizeWatch.report(bdd_reachables, "Qco");
		timer.report("Co-reachables found (disjuncted)");

		if (gf != null)
		{
			gf.stopTimer();
		}

		// SizeWatch.report(bdd_coreachables,"Coreachables");
	}

}
