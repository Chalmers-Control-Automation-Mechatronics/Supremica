package org.supremica.util.BDD;

// TODO:
// apply the conjunctive transition relations in reverse order
// (to build the BDD bottom up ?)
public class ConjPartition
{
	private int max_size, curr;
	private int[] delta;
	private JBDD manager;
	private int cube_s, cube_sp, cube_e, cube_se, cube_spe;
	private int s2sp, sp2s;

	public ConjPartition(BDDAutomata manager, int max_size)
	{
		this.manager = manager;
		this.max_size = max_size;
		this.curr = 0;
		this.cube_s = manager.getStateCube();
		this.cube_sp = manager.getStatepCube();
		this.cube_e = manager.getEventCube();
		this.cube_se = manager.and(cube_s, cube_e);
		this.cube_spe = manager.and(cube_sp, cube_e);
		this.s2sp = manager.getPermuteS2Sp();
		this.sp2s = manager.getPermuteSp2S();
		this.delta = new int[max_size];
	}

	public void cleanup()
	{
		manager.deref(cube_se);
		manager.deref(cube_spe);

		for (int i = 0; i < curr; i++)
		{
			manager.deref(delta[i]);
		}

		curr = 0;
	}

	// --------------------------------------------------
	public int getNumberOfClusters()
	{
		return curr;
	}

	public int getCube()
	{
		return cube_s;
	}

	public int getS2Sp()
	{
		return s2sp;
	}

	public int getSp2s()
	{
		return sp2s;
	}

	// --------------------------------------------------
	public void add(int delta_k)
	{

		// simple/idiotic/stupid insertation
		for (int i = 0; i < curr; i++)
		{
			int tmp = manager.and(delta[i], delta_k);

			if (manager.nodeCount(tmp) < Options.max_partition_size)
			{

				// keep it
				manager.deref(delta[i]);

				delta[i] = tmp;

				return;
			}

			/* else */
			manager.deref(tmp);
		}

		/* no good found, create a new one */
		BDDAssert.internalCheck(curr < max_size, "Partition overflow");

		delta[curr] = delta_k;

		manager.ref(delta[curr]);

		curr++;
	}

	// -------------------------------------------------
	public void report()
	{
		SizeWatch.setOwner("ConjPartition");

		for (int i = 0; i < curr; i++)
		{
			SizeWatch.report(delta[i], "Cluster " + (i + 1));
		}
	}

	// --------------------------------------------------

	/** 1-step forward rechables */
	public int image(int q_k)
	{
		int front = q_k;

		manager.ref(front);

		for (int i = 0; i < curr; i++)
		{
			front = manager.andTo(front, delta[i]);
		}

		int tmp = manager.exists(front, cube_se);

		manager.deref(front);

		int tmp2 = manager.replace(tmp, sp2s);

		manager.deref(tmp);

		return tmp2;
	}

	/**
	 * 1-step forward rechables.
	 * Consider only events in the mask.
	 * TODO: this is a good place to deploy operator scheduling (to add mask before or after conjunction)
	 **/
	public int image(int q_k, int event_mask)
	{
		int front = q_k;

		manager.ref(front);

		for (int i = 0; i < curr; i++)
		{
			front = manager.andTo(front, delta[i]);
		}

		int tmp = manager.relProd(front, event_mask, cube_se);

		manager.deref(front);

		int tmp2 = manager.replace(tmp, sp2s);

		manager.deref(tmp);

		return tmp2;
	}

	/** 1-step backward reachables.<br>
	 * Note: q_k must be in S' _not_ in S'!<br>
	 * the returned BDD is also in S'
	 */
	public int preImage(int q_k)
	{
		int front = q_k;

		manager.ref(front);

		for (int i = 0; i < curr; i++)
		{
			front = manager.andTo(front, delta[i]);
		}

		int tmp = manager.exists(front, cube_spe);

		manager.deref(front);

		int tmp2 = manager.replace(tmp, s2sp);

		manager.deref(tmp);

		return tmp2;
	}
}
