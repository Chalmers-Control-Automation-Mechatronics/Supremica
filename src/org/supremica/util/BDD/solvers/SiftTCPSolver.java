package org.supremica.util.BDD.solvers;

import org.supremica.util.BDD.*;

/**
 * try
 *
 */
public class SiftTCPSolver
	extends TSPSolver
{
	private Node[] work = null;
	public int improvments;

	public SiftTCPSolver(Node[] org_)
	{
		super(org_);
	}

	public void solve()
	{
		super.solve();

		if (size < 2)
		{
			return;    // no need to wast time here
		}

		work = new Node[size];

		for (int i = 0; i < size; i++)
		{
			work[i] = solved[i];    // start with our TSP solution
		}

		double best = eval();

		improvments = 0;

		for (int j = 0; j < size * 5; j++)
		{
			int r = (int) (Math.random() * size);

			// int dir = (Math.random() >= 0.5) ? 3 : -3;
			int dir = 1 + (int) (Math.random() * (size - 1));
			int to = r + dir;

			// wrap around, not very clever, huh?
			while (to < 0)
			{
				to += size;
			}

			while (to >= size)
			{
				to -= size;
			}

			Node tmp = work[r];

			work[r] = work[to];
			work[to] = tmp;

			double score = eval();

			if (score < best)
			{
				score = best;

				for (int i = 0; i < size; i++)
				{
					solved[i] = work[i];
				}

				improvments++;
			}
			else
			{

				// change it back!
				tmp = work[r];
				work[r] = work[to];
				work[to] = tmp;
			}
		}

		if (Options.profile_on)
		{
			Options.out.println("TCP+sift algorithm " + ((improvments > 0)
														 ? (" imporved automata ordering " + improvments + " times.")
														 : (" could not improve the automata ordering.")));
		}
	}

	// --------------------------------------------------
	// taken from STCT

	/** the number of crossing component i */
	private double cross(int i)
	{
		double sum = 0;

		for (int j = 0; j < i; j++)
		{
			for (int k = i + 1; k < size; k++)
			{
				sum += work[k].wlocal[work[j].index_local];
			}
		}

		return sum;
	}

	/**
	 * The overall crossing, the measure of the optimality of the given ordering.
	 * Accroding to Zhang, the number "4" is only here to make the sum grow faster for large crosse(i)'es
	 */
	protected double eval()
	{
		double sum = 0;

		for (int i = 0; i < size; i++)
		{
			sum += Math.pow(4, cross(i));
		}

		return sum;
	}
}
