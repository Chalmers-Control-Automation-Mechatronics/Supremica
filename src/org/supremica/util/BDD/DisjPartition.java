package org.supremica.util.BDD;

import java.util.*;

/** This represents a disjunctive partition where automata has been put in "clusters" */
public class DisjPartition
{
	private int max_size, curr;
	private JBDD manager;
	private DisjOptimizer dop;
	private int cube, cubep, s2sp, sp2s;
	private Cluster[] clusters;

	public DisjPartition(BDDAutomata manager, DisjOptimizer dop)
	{
		this.manager = manager;
		this.max_size = dop.getSize();
		this.dop = dop;
		this.curr = 0;
		this.cube = manager.getStateCube();
		this.cubep = manager.getStatepCube();
		this.s2sp = manager.getPermuteS2Sp();
		this.sp2s = manager.getPermuteSp2S();

		/* compute the clusters */
		Vector cv = new Vector();
		Cluster[] old = dop.getClusters();

		if (max_size > 0)
		{
			Cluster current = old[0];

			cv.addElement(current.copy());

			for (int i = 1; i < max_size; i++)
			{
				add(cv, old[i]);
			}
		}

		/* create a array from that Vector: */
		curr = cv.size();
		clusters = new Cluster[curr];

		Enumeration e = cv.elements();

		for (int i = 0; i < curr; i++)
		{
			clusters[i] = (Cluster) e.nextElement();
		}
	}

	public void cleanup()
	{
		for (int i = 0; i < curr; i++)
		{
			clusters[i].cleanup();
		}

		curr = 0;
	}

	/** DEBUG
public void check(String place){
			for(int i = 0; i < curr; i++) clusters[i].check(place);
	}
	*/

	// --------------------------------------------------
	public int getNumberOfClusters()
	{
		return curr;
	}

	public Cluster[] getClusters()
	{
		return clusters;
	}

	public int getCube()
	{
		return cube;
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

	/** stupid greedy insertation algorithm */
	public void add(Vector v, Cluster c)
	{
		for (Enumeration e = v.elements(); e.hasMoreElements(); )
		{
			Cluster c2 = (Cluster) e.nextElement();
			int tmp = manager.or(c2.getTwave(), c.getTwave());

			if (manager.nodeCount(tmp) < Options.max_partition_size)
			{
				c2.join(c);
				manager.deref(tmp);

				return;
			}

			manager.deref(tmp);
		}

		// if we got here, we need a new Cluster:
		v.addElement(c.copy());
	}

	// -------------------------------------------------
	public void report()
	{
		SizeWatch.setOwner("DisjPartition");

		for (int i = 0; i < curr; i++)
		{
			SizeWatch.report(clusters[i].getTwave(), "Cluster " + (i + 1));
		}
	}

	// --------------------------------------------------

	/** 1-step forward rechables */
	public int image(int q_k)
	{
		int front = manager.getZero();

		manager.ref(front);

		for (int i = 0; i < curr; i++)
		{
			int tmp = manager.relProd(clusters[i].getTwave(), q_k, cube);

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

		for (int i = 0; i < curr; i++)
		{
			int tmp = manager.relProd(clusters[i].getTwave(), q_k, cubep);

			front = manager.orTo(front, tmp);

			manager.deref(tmp);
		}

		int q_kplus1 = manager.replace(front, s2sp);

		manager.deref(front);

		return q_kplus1;
	}
}
