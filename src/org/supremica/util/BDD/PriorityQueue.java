package org.supremica.util.BDD;

/**
 * this will probably be a priorityqueue of BDDAutomaton some day...
 *
 * [later same day] ok, I cant find the algorithm-book, lets do a stupid sorted version...
 *
 * If you think this code is stupid, you should see the C code Ericsson
 * has running in their AXE switches :)
 *
 */
public class PriorityQueue
{
	private int size, curr;
	private Object[] all;
	private double[] costs;
	private boolean changed;

	public PriorityQueue(int max_size)
	{
		costs = new double[max_size];
		all = new Object[max_size];
		size = max_size;
		curr = 0;
		changed = false;
	}

	public void insert(Object a, double c)
	{
		if (curr < size)
		{
			all[curr] = a;
			costs[curr] = c;

			curr++;

			changed = true;
		}
	}

	/** pop the largest object, returns null if non exists. call cost() afterwards to get its cost */
	public Object next()
	{
		if (curr == 0)
		{
			return null;
		}

		if (changed)
		{
			sort();
		}

		return all[--curr];
	}

	/** valid only after a call to next(), returns the cost of that object */
	public double cost()
	{
		return costs[curr];
	}

	public boolean empty()
	{
		return curr == 0;
	}

	// -- [quick sort code goes here] --------------------------------------
	private void sort()
	{
		changed = false;

		// now, sort the damn list:
		quicksort(0, curr - 1);
	}

	private void quicksort(int p, int r)
	{
		if (p < r)
		{
			int q = partition(p, r);

			quicksort(p, q - 1);
			quicksort(q + 1, r);
		}
	}

	private int partition(int p, int r)
	{
		double x = costs[r];
		int i = p - 1;

		for (int j = p; j < r; j++)
		{
			if (costs[j] <= x)
			{
				i++;

				// SWAP I <-> J
				swap(i, j);
			}
		}

		// SWAP I+1 <-> r
		i++;

		swap(i, r);

		return i;
	}

	private void swap(int a, int b)
	{
		if (a == b)
		{
			return;
		}

		Object tmpa;
		double tmpc;

		tmpc = costs[a];
		costs[a] = costs[b];
		costs[b] = tmpc;
		tmpa = all[a];
		all[a] = all[b];
		all[b] = tmpa;
	}

	// --[ test code ]----------------------------------------------------------
	public static void main(String[] args)
	{
		PriorityQueue pq = new PriorityQueue(10);

		for (int i = 0; i < 10; i++)
		{
			pq.insert(new String("" + i), (double) (i * 3 + (i % 5)));
		}

		while (!pq.empty())
		{
			Options.out.println("--> " + pq.next() + "/" + pq.cost());
		}
	}
}
