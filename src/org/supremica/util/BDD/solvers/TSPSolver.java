package org.supremica.util.BDD.solvers;

import org.supremica.util.BDD.*;

/**
 * TSP ordering: solve a TSP problem by a 2-step lookahead greedy algo.
 * note thet the weights are DYNAMIC, so this is not really TSP.
 * besides, the starting point DOES matter here :(
 */
public class TSPSolver
	extends Solver
{
	public TSPSolver(Node[] org_)
	{
		super(org_);
	}

	public void solve()
	{
		int[] tour = TSP_tour();

		// find the best place to cut the loop:
		// The idea is first to (A) find the place where the connection
		// a-->b is weakest and store all such b's...
		IntArray candidates = new IntArray();    // the set of those with smallest split-cost

		// candidates.min() is the cost of splitting an arc, less is good
		for (int i = 0; i < size; i++)
		{
			int prev = (i + size - 1) % size;
			double cost = cost(tour[prev], tour[i], 1);

			if ((i == 0) || (cost < candidates.getMin()))
			{
				candidates.clear();
				candidates.add(i);
			}
			else if (cost == candidates.getMin())
			{
				candidates.add(i);
			}
		}

		// .. then we (B) choose the smallest b in size, since we want the
		// smallest trees near to the top [THIS IS NOT OPTIMAL]:
		int start_index = 0;
		int smallest = 0;

		for (int i = 0; i < candidates.getSize(); i++)
		{
			int b = candidates.get(i);
			int siz = org[b].size;

			if ((i == 0) || (siz < smallest))
			{
				smallest = siz;
				start_index = b;
			}
		}

		// insert them in that order:
		for (int i = 0; i < size; i++)
		{
			solved[i] = org[tour[(i + start_index) % size]];
		}
	}

	// Currently, a 2-step look-ahead greedy max-weight TSP tour
	private int[] TSP_tour()
	{
		int size = org.length;
		int curr = 0;
		int[] tour = new int[size];
		boolean[] used = new boolean[size];

		for (int i = 0; i < size; i++)
		{
			used[i] = false;
		}

		// insert first:
		int first = 0;    // we could choose one at random too

		used[first] = true;
		tour[curr++] = first;

		int last = first;

		while (curr < size)
		{
			int best_index = -1;
			double best_cost = Double.NEGATIVE_INFINITY;

			for (int j = 0; j < size; j++)
			{
				if (!used[j])
				{
					if (curr == size - 1)
					{    // only one left ?
						best_index = j;
					}
					else
					{
						used[j] = true;

						for (int k = 0; k < size /* && best_index != j*/; k++)
						{
							if (!used[k])
							{
								double cost = cost(last, j, 1) + cost(j, k, 1);

								if (cost > best_cost)
								{
									best_cost = cost;
									best_index = j;
								}
							}
						}

						used[j] = false;
					}
				}
			}

			used[best_index] = true;
			tour[curr++] = last = best_index;
		}

		// DEBUG
		// Options.out.print("tour = ");
		// for(int i = 0; i < size; i++) Options.out.print(" " + org[tour[i]].org.getName());
		// Options.out.println();
		return tour;
	}
}
