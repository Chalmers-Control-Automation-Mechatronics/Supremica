package org.supremica.util.BDD;

import java.util.*;

/**
 * Smoothing  based on locking some paths in partitions?
 *
 */
public class PathSmoothPartition
{
	private int size, current;
	private Cluster[] clusters;
	private BDDAutomata manager;
	private PathCluster[] pathclusters;
	private boolean empty_;
	private int s2sp, sp2s;
	private int cube, cubep;

	public PathSmoothPartition(BDDAutomata manager, Cluster[] clusters, int size, boolean forward)
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

		BDDAssert.internalCheck(size > 0, "size <= 0 in PathSmoothPartition. something went wrong earlier?");

		pathclusters = new PathCluster[size];

		for (int i = 0; i < size; i++)
		{
			pathclusters[i] = new PathCluster(manager, clusters[i], forward);
		}

		step();
	}

	public void cleanup()
	{
		for (int i = 0; i < size; i++)
		{
			pathclusters[i].cleanup();
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
			if (pathclusters[current].empty())
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
				pathclusters[current].step();
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
			int tmp = manager.relProd(pathclusters[i].getT(), q_k, cube);

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
			int tmp = manager.relProd(pathclusters[i].getT(), q_k, cubep);

			front = manager.orTo(front, tmp);

			manager.deref(tmp);
		}

		int q_kplus1 = manager.replace(front, s2sp);

		manager.deref(front);

		return q_kplus1;
	}

	/** This a Cluster, prepared for Path Smoothing:
	 *  we have the intial lock (conjunction of all automata I-relation, mapped to S´)
	 *  we have all the support variables in S´ for the automata in cluster
	 */
	private class PathCluster
	{
		private Cluster cluster;
		private BDDAutomata manager;
		private int bits, current;
		private int[] v_cubep;
		private int bdd_curr_i, bdd_curr_t;

		public PathCluster(BDDAutomata manager, Cluster cluster, boolean forward)
		{
			this.cluster = cluster;
			this.manager = manager;
			this.bdd_curr_t = -1;

			// get the size of state vector, create the initial lock
			current = 0;
			bdd_curr_i = manager.getOne();

			manager.ref(bdd_curr_i);

			for (Iterator it = cluster.iterator(); it.hasNext(); ) {
				BDDAutomaton a = (BDDAutomaton) it.next();


				/** possible problem here: if we are going backward, nothing garanties that
				  * there is a single marked state? manye all states are marked?
				  */
				if (forward)
				{
					bdd_curr_i = manager.andTo(bdd_curr_i, a.getI());
				}
				else
				{
					int tmp = a.getM();

					if ((tmp == manager.getZero()) || (tmp == manager.getOne()))
					{
						tmp = a.getI();
					}

					bdd_curr_i = manager.andTo(bdd_curr_i, tmp);
				}
			}

			// transform lock from S to S'
			int tmp = bdd_curr_i;

			bdd_curr_i = manager.replace(bdd_curr_i, manager.getPermuteS2Sp());

			manager.deref(tmp);

			// get the actual S' bits:
			int bits = cluster.getSizeOfS();
			v_cubep = new int[bits];
			bits = 0;

			for (Iterator it = cluster.iterator(); it.hasNext(); ) {
				BDDAutomaton a = (BDDAutomaton) it.next();

				int size = a.getNumStateBits();
				int[] vp = a.getVarp();

				for (int i = 0; i < size; i++)
				{
					v_cubep[bits++] = vp[i];
				}
			}

			// EXPRIMENTAL: sort the vars:
			Util.sort_variable_list(manager, v_cubep, bits, false);

			// initilaized, now step forward one initial step
			step();
		}

		public void cleanup()
		{
			manager.deref(bdd_curr_i);

			if (bdd_curr_t != -1)
			{
				manager.deref(bdd_curr_t);
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

				// update I
				int tmp = bdd_curr_i;

				bdd_curr_i = manager.exists(bdd_curr_i, v_cubep[current++]);

				manager.deref(tmp);

				// update T
				if (bdd_curr_t != -1)
				{
					manager.deref(bdd_curr_t);
				}

				bdd_curr_t = manager.and(cluster.getTwave(), bdd_curr_i);

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
