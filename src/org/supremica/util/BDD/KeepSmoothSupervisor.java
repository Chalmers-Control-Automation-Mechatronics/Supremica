package org.supremica.util.BDD;

import java.util.*;

public class KeepSmoothSupervisor
	extends DisjSupervisor
{

	/** Constructor, passes to the base-class */
	public KeepSmoothSupervisor(BDDAutomata manager, BDDAutomaton[] as)
	{
		super(manager, as);
	}

	/** Constructor, passes to the base-class */
	public KeepSmoothSupervisor(BDDAutomata manager, Group plant, Group spec)
	{
		super(manager, plant, spec);
	}

	/** C++-style Destructor to cleanup unused BDD trees*/
	public void cleanup()
	{
		super.cleanup();
	}

	protected void computeReachables()
	{

		// statistic stuffs
		GrowFrame gf = BDDGrow.getGrowFrame(manager, "Forward reachability" + type());

		timer.reset();
		SizeWatch.setOwner("KeepSmoothSupervisor.computeReachables");

		KeepSmoothPartition psp = new KeepSmoothPartition(manager, dop.getClusters(), dop.getSize());
		int i_all = manager.and(plant.getI(), spec.getI());
		int r_all_p, r_all = i_all;

		limit.reset();

		do
		{
			do
			{
				r_all_p = r_all;

				int tmp = psp.image(r_all);

				r_all = manager.orTo(r_all, tmp);

				manager.deref(tmp);

				if (gf != null)
				{
					gf.add(r_all);
				}
			}
			while ((r_all_p != r_all) &&!limit.stopped());
		}
		while (psp.step() &&!limit.stopped());

		// cleanup
		psp.cleanup();

		has_reachables = true;
		bdd_reachables = r_all;

		if (gf != null)
		{
			gf.stopTimer();
		}

		SizeWatch.report(bdd_reachables, "Qr");
		timer.report("Forward reachables found (keep smoothed)");
	}

	protected void computeCoReachables()
	{
		GrowFrame gf = BDDGrow.getGrowFrame(manager, "backward reachability" + type());

		timer.reset();
		SizeWatch.setOwner("KeepSmoothSupervisor.computeCoReachables");

		KeepSmoothPartition psp = new KeepSmoothPartition(manager, dop.getClusters(), dop.getSize());
		int m_all = GroupHelper.getM(manager, spec, plant);
		int r_all_p, r_all;

		r_all = manager.replace(m_all, perm_s2sp);

		manager.deref(m_all);
		SizeWatch.report(r_all, "Qm");
		limit.reset();

		do
		{
			do
			{
				r_all_p = r_all;

				int tmp = psp.preImage(r_all);

				r_all = manager.orTo(r_all, tmp);

				manager.deref(tmp);

				if (gf != null)
				{
					gf.add(r_all);
				}
			}
			while ((r_all_p != r_all) &&!limit.stopped());
		}
		while (psp.step() &&!limit.stopped());

		has_coreachables = true;
		bdd_coreachables = manager.replace(r_all, perm_sp2s);

		// cleanup:
		psp.cleanup();
		manager.deref(r_all);

		if (gf != null)
		{
			gf.stopTimer();
		}

		SizeWatch.report(bdd_coreachables, "Qco");
		timer.report("Co-reachables found (keep smoothed)");
	}

	/**
	 * Smoothing  based on having keep on some variables?
	 *
	 */
	private class KeepSmoothPartition
	{
		private int size, current;
		private Cluster[] clusters;
		private BDDAutomata manager;
		private KeepCluster[] keepclusters;
		private boolean empty_;
		private int s2sp, sp2s;
		private int cube, cubep;

		public KeepSmoothPartition(BDDAutomata manager, Cluster[] clusters, int size)
		{
			this.manager = manager;
			this.clusters = clusters;
			this.size = size;
			this.current = 0;
			this.empty_ = false;
			this.s2sp = manager.getPermuteS2Sp();
			this.sp2s = manager.getPermuteSp2S();
			this.cube = manager.getStateCube();
			this.cubep = manager.getStatepCube();

			BDDAssert.internalCheck(size > 0, "size <= 0 in KeepSmoothPartition. something went wrong earlier?");

			keepclusters = new KeepCluster[size];

			for (int i = 0; i < size; i++)
			{
				keepclusters[i] = new KeepCluster(manager, clusters[i]);
			}

			step();
		}

		public void cleanup()
		{
			for (int i = 0; i < size; i++)
			{
				keepclusters[i].cleanup();
			}
		}

		public boolean empty()
		{
			return empty_;
		}

		public boolean step()
		{
			if (!empty_)
			{
				if (keepclusters[current].empty())
				{

					// ASSUMING THAT current < size
					current++;

					if (current >= size)
					{
						empty_ = true;
					}
				}
				else
				{
					keepclusters[current].step();
				}

				return true;
			}

			return false;
		}

		/** 1-step forward rechables */
		public int image(int q_k)
		{
			int front = manager.getZero();

			manager.ref(front);

			for (int i = 0; i < current; i++)
			{
				int tmp = manager.relProd(keepclusters[i].getT(), q_k, cube);

				front = manager.orTo(front, tmp);

				manager.deref(tmp);
			}

			int front_s = manager.replace(front, sp2s);

			manager.deref(front);

			return front_s;
		}

		/** 1-step backward reachables.<br>
		 * Note: q_k must be in S' _not_ in S'!<br>
		 * the returned BDD is also in S'
		 */
		public int preImage(int q_k)
		{
			int front = manager.getZero();

			manager.ref(front);

			for (int i = 0; i < current; i++)
			{
				int tmp = manager.relProd(keepclusters[i].getT(), q_k, cubep);

				front = manager.orTo(front, tmp);

				manager.deref(tmp);
			}

			int q_kplus1 = manager.replace(front, s2sp);

			manager.deref(front);

			return q_kplus1;
		}

		/** This a Cluster, prepared for Keep Smoothing:
		 *  we have the intial lock (conjunction of all automata I-relation, mapped to S´)
		 *  we have all the support variables in S´ for the automata in cluster
		 */
		private class KeepCluster
		{
			private Cluster cluster;
			private BDDAutomata manager;
			private int bits, current;
			private int[] v_vars;
			private int bdd_curr_keep, bdd_curr_t;

			public KeepCluster(BDDAutomata manager, Cluster cluster)
			{
				this.cluster = cluster;
				this.manager = manager;
				this.bdd_curr_t = -1;

				// get the size of state vector, create the initial lock
				bits = current = 0;
				bdd_curr_keep = manager.getOne();

				manager.ref(bdd_curr_keep);

				for (Enumeration e = cluster.members.elements();
						e.hasMoreElements(); )
				{
					BDDAutomaton a = (BDDAutomaton) e.nextElement();

					bits += a.getNumStateBits();
					bdd_curr_keep = manager.andTo(bdd_curr_keep, a.getKeep());
				}

				// TODO: do have the right BDD order here? (cant sort bdd _trees_ ?)
				// get the actual S' bits:
				v_vars = new int[bits];
				bits = 0;

				for (Enumeration e = cluster.members.elements();
						e.hasMoreElements(); )
				{
					BDDAutomaton a = (BDDAutomaton) e.nextElement();
					int size = a.getNumStateBits();
					int[] vp = a.getVar();
					int[] v = a.getVarp();

					for (int i = 0; i < size; i++)
					{
						v_vars[bits++] = manager.and(v[i], vp[i]);
					}
				}

				// initilaized, now step forward one initial step
				step();
			}

			public void cleanup()
			{
				manager.deref(bdd_curr_keep);

				if (bdd_curr_t != -1)
				{
					manager.deref(bdd_curr_t);
				}

				for (int i = 0; i < bits; i++)
				{
					manager.deref(v_vars[i]);
				}
			}

			public boolean empty()
			{
				return current >= bits;
			}

			public boolean step()
			{
				if (!empty())
				{

					// update keep: remove a BDD variable pair from the keep constraint
					int tmp = bdd_curr_keep;

					bdd_curr_keep = manager.exists(bdd_curr_keep, v_vars[current++]);

					manager.deref(tmp);

					// update T
					if (bdd_curr_t != -1)
					{
						manager.deref(bdd_curr_t);
					}

					bdd_curr_t = manager.and(cluster.getTwave(), bdd_curr_keep);

					/*
					// DEBUG:
					manager.printSet(bdd_curr_keep);
					manager.printSet(bdd_curr_t);
					*/
					return true;
				}

				// else
				return false;    //  couldn't step...
			}

			public int getT()
			{
				return bdd_curr_t;
			}
		}
		;
	}
	;
}
;
