package org.supremica.util.BDD.solvers;

import org.supremica.util.BDD.*;

/**
 * breadth first ordering
 *
 * XXX: we must test if traversing the nodes w.r.t weights  order really helps?
 *
 */
public class BFSSolver
	extends Solver
{
	private int count;

	public BFSSolver(Node[] org_)
	{
		super(org_);
	}

	public void solve()
	{
		int[] tmp = new int[size];
		int[] best = new int[size];
		int[] stack = new int[size];
		double best_cost = Double.MAX_VALUE;
		IntQueue queue = new IntQueue(size);

		// we dont know where to start, so we will try them all :(
		for (int first = 0; first < size; first++)
		{

			// reset
			for (int i = 0; i < size; i++)
			{
				org[i].extra1 = org[i].extra2 = 0;
				tmp[i] = -1;    // delete last rounds crap, makes it easier for the integrity checker
			}

			// more reset code
			queue.reset();

			count = 0;

			// start clean, with the first node only
			queue.enqueue(first);

			while (!queue.empty())
			{
				int curr = queue.dequeue();

				org[curr].extra1 = 1;
				org[curr].extra2 = tmp[curr] = count++;

				int tos = 0;    // top of stack

				for (int i = 0; i < size; i++)
				{

					// traverse in the original array-order, then sort the results
					if ((i != curr) && (org[i].wlocal[curr] > 0) && (org[i].extra1 == 0))
					{
						org[i].extra1 = -1;    // otherwise, we would insert same node multiple times!
						stack[tos++] = i;
					}
				}

				// insert the sorted stack to the queue
				if (tos > 0)
				{
					sort(stack, 0, tos - 1, curr);    // SORT

					for (int i = 0; i < tos; i++)
					{
						queue.enqueue(stack[i]);    // ENQUEUE SORTED LIST
					}
				}
			}

			// fix those with no ordering:
			// XXX: this should (?) not happen (PCGs are connected graphs!)
			for (int i = 0; i < size; i++)
			{
				if (org[i].extra1 == 0)
				{
					org[i].extra2 = tmp[i] = count++;
				}
			}


			// extra integrity test
			if(Options.test_integrity)
			{
				// see if we screwed ...
				if (verify_order_integrity(tmp))
				{

					// WHAT DO WE DO NOW???
				}
			}



			double cost = totalCost(tmp);

			if (cost < best_cost)
			{
				best_cost = 0;

				for (int i = 0; i < size; i++)
				{
					best[i] = tmp[i];
				}
			}
		}

		// write back the best result
		for (int i = 0; i < size; i++)
		{
			solved[best[i]] = org[i];
		}
	}

	/**
	 * extra code needed to find a nasty bug in the BFS order algo.
	 *
	 * (ok, it was not that nasty, i simply screwed up. i am not superman, you know...)
	 */
	private boolean verify_order_integrity(int[] order)
	{
		boolean fatal = false;

		for (int i = 0; i < size; i++)
		{
			if ((order[i] < 0) || (order[i] >= size))
			{
				System.err.println("BFS order integrity check failed: INVALID order for component " + i + ": " + order[i]);

				fatal = true;
			}
		}

		if (fatal)
		{
			return false;
		}

		for (int i = 0; i < size; i++)
		{
			org[i].extra1 = 0;
		}

		for (int i = 0; i < size; i++)
		{
			org[order[i]].extra1++;
		}

		for (int i = 0; i < size; i++)
		{
			if (org[i].extra1 != 1)
			{
				System.err.println("BFS order integrity check failed: component " + i + " mentioned " + org[i].extra1 + " times");

				return false;
			}
		}

		return true;
	}
}
