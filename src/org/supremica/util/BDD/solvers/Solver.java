package org.supremica.util.BDD.solvers;

import org.supremica.util.BDD.*;

/**
 * Ordering solver base class.
 *
 * "solvers" are used in the OrderingSolver, which is used by the AOH_TSP heuristic
 *
 *
 */
public abstract class Solver
{
	protected int size;
	protected Node[] org, solved;

	private double [] internal_weight; /** for internal sorting */
	private Node [] internal_object; /** for internal sorting */

	public Solver(Node[] org_)
	{
		this.org = org_;
		this.size = org_.length;
		this.solved = new Node[size];

		this.internal_object = new Node[size];
		this.internal_weight = new double[size];

		solve();

		if(Options.profile_on)
		{
			int [] order = new int[size];
			for(int i = 0; i < order.length; i++) order[i] = solved[i].index;

			double cost = totalCost(order);
			Options.out.println("--> [Solver] ordering cost = " + cost);
		}
	}

	public Node[] getShortestPath()
	{
		return solved;
	}

	public abstract void solve();

	protected double cost(int from, int to, int distance)
	{

		// this is VERY non-theoretical :)
		return Math.pow(org[from].wlocal[to], Math.log(1 + Math.abs(distance)));

		// return org[from].wlocal[to] * Math.abs(distance); // <-- not that powerful

		// return Math.pow( org[from].wlocal[to] * Math.abs(distance), 1.5);
	}

	protected double totalCost(int[] order)
	{
		double ret = 0.0;

		for (int i = 0; i < size; i++)
		{
			for (int j = 0; j < i; j++)
			{    // range: 0..i-1 , assume the cost is symmetric!
				ret += cost(i, j, order[i] - order[j]);
			}
		}

		return ret;
	}

	/** get the node with least number of connections */
	protected int get_least_connected()
	{
		int best_index = 0;
		int best = Integer.MAX_VALUE;

		for (int i = 0; i < size; i++)
		{
			int curr = 0;

			for (int j = 0; j < size; j++)
			{
				if ((i != j) && (org[i].wlocal[j] > 0))
				{
					curr++;
				}
			}

			if (curr < best)
			{
				best = curr;
				best_index = i;
			}
		}

		return best_index;
	}

	/** get the node with most number of connections */
	protected int get_most_connected()
	{
		int best_index = 0;
		int best = 0;    // cant get any lower than zero

		for (int i = 0; i < size; i++)
		{
			int curr = 0;

			for (int j = 0; j < size; j++)
			{
				if ((i != j) && (org[i].wlocal[j] > 0))
				{
					curr++;
				}
			}

			if (curr > best)
			{
				best = curr;
				best_index = i;
			}
		}

		return best_index;
	}

	/**
	 * just sort so the automaton with largest dependency gets places on top of stack and
	 * therefor traversed first :)
	 *
	 * <p> side-effect: org[].extra3 changed
	 */
	protected final void sort(int[] data, int start, int end, int parent)
	{

		// only one element, nothing to do
		if(start == end)
		{
			return;
		}

		/*
		// The old version:
		for (int i = start; i < end; i++)
		{
			int min = i;

			for (int j = i + 1; j < end; j++)
			{
				if (org[data[j]].wlocal[parent] < org[data[min]].wlocal[parent])
				{
					min = j;
				}
			}

			int tmp = data[min];

			data[min] = data[i];
			data[i] = tmp;
		}
		*/


		// 1. first make an (object, weight) pair so we can send it to quick-sort
		int count = 0;
		for(int i = start; i <= end; i++)
		{
			internal_weight[count]  = org[data[i]].wlocal[parent];
			internal_object[count]  = org[data[i]];
			internal_object[count].extra3 = data[i];
			count++;
		}

		// 2. disturb the order for better sorting (TODO)


		// 3. sort...
		QuickSort.sort(internal_object, internal_weight,count, true);

		// 4. ... and write back
		for(int i = 0; i < count; i++)
		{
			int value = internal_object[i].extra3;
			int index = i + start;
			data[index] =  value;
		}
	}
}
