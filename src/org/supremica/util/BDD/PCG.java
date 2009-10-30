package org.supremica.util.BDD;

import java.util.*;

// Process Transition Graphs (PCG) in java
public class PCG
{
	static final int NOT_CONNECTED = -1;

	private class Node
	{
		PCGNode id;
		int index;
		int[] weights;
		int size;
	}

	private int size;
	private HashMap<PCGNode, Node> map = new HashMap<PCGNode, Node>();
	private Node[] nodes;
	private PCGNode[] org_nodes;

	public PCG(Vector<?> nodes_)
	{
		size = nodes_.size();
		nodes = new Node[size];
		org_nodes = new PCGNode[size];

		Enumeration<?> e = nodes_.elements();
		int i = 0;

		while (e.hasMoreElements())
		{
			setNode(i++, (PCGNode) e.nextElement());
		}
	}

	public PCG(Object[] nodes_)
	{
		size = nodes_.length;
		nodes = new Node[size];

		for (int i = 0; i < size; i++)
		{
			setNode(i, (PCGNode) nodes_[i]);
		}
	}

	private void setNode(int index, PCGNode id)
	{
		nodes[index] = new Node();
		nodes[index].id = id;
		nodes[index].weights = new int[size];
		nodes[index].index = index;
		nodes[index].size = id.getSize();
		org_nodes[index] = id;

		for (int j = 0; j < size; j++)
		{
			nodes[index].weights[j] = NOT_CONNECTED;
		}

		map.put(id, nodes[index]);
	}

	public void connect(Object obj1, Object obj2, int weight)
		throws BDDException
	{
		Node node1 = map.get(obj1);
		Node node2 = map.get(obj2);

		BDDAssert.bddAssert(node1 != null, "first object not found");
		BDDAssert.bddAssert(node2 != null, "second object not found");

		node1.weights[node2.index] = weight;
		node2.weights[node1.index] = weight;
	}

	public int getSize(int index, int level)
	{
		return (size - index + 1) * nodes[index].size;
	}

	public int getWeight(int me, int next, int level)
	{
		return nodes[me].weights[next] * getSize(me, level);
	}

	private int getSmallest(boolean[] free)
		throws BDDException
	{
		int smallest = 0, index = 0;
		boolean first = true;

		for (int j = 0; j < size; j++)
		{
			if (free[j])
			{
				int size = nodes[j].id.getSize();

				if (first || (smallest < size))
				{
					smallest = size;
					first = false;
					index = j;
				}
			}
		}

		BDDAssert.bddAssert(!first, "[PCG.getSmallest] free-list is empty");

		return index;
	}

	// ------------------------------------------------------------
	public int[] getShortestPath()
		throws BDDException
	{
		int[] perm = null;

		if (size <= 1)
		{    // 0 or 1 components
			perm = new int[size];

			if (size == 1)
			{
				perm[0] = 0;    // the one and only
			}

			return perm;
		}

		perm = getShortestPath_greedy2();

		// perm = getShortestPath_complete();
		if (Options.debug_on)
		{
			Options.out.print("PCG group ordering: ");

			for (int j = 0; j < size; j++)
			{
				Options.out.print(nodes[perm[j]].id.getName() + "  ");
			}

			Options.out.println("  (score=" + perm[size] + ")");
		}

		// we dont want the last element (score) any more
		int[] ret = new int[size];

		for (int i = 0; i < size; i++)
		{
			ret[i] = perm[i];
		}

		return ret;
	}

	// ----------------------------------- complete search
	@SuppressWarnings("unused")
	private int[] getShortestPath_complete()
	{
		int[] perm = new int[size + 1];
		int[] path = new int[size];
		boolean[] free = new boolean[size];

		for (int i = 0; i < size; i++)
		{
			free[i] = true;
		}

		perm[size] = -1;

		getShortest_complete(perm, path, free, 0);

		return perm;
	}

	private int compute_path_cost(int[] path)
	{
		int ret = 0;

		for (int i = 1; i < size; i++)
		{
			ret += getWeight(path[i - 1], i, i);
		}

		return ret;
	}

	private void getShortest_complete(int[] perm, int[] path, boolean[] free, int level)
	{
		if (level == size)
		{
			int cost = compute_path_cost(path);

			if (cost > perm[size])
			{

				/*
				 * Options.out.println("path changed: " +cost + "  > " +perm[size]);
				 * for(int j = 0; j < size; j++)
				 * Options.out.print( nodes[path[j]].id.getName() + "  ");
				 * Options.out.println();
				 */
				for (int i = 0; i < size; i++)
				{
					perm[i] = path[i];
				}

				perm[size] = cost;
			}

			return;
		}

		for (int i = 0; i < size; i++)
		{
			if (free[i])
			{
				free[i] = false;
				path[level] = i;

				getShortest_complete(perm, path, free, level + 1);

				free[i] = true;
			}
		}
	}

	// ----------------------------------- greedy, 2-step lookahead
	private int[] getShortestPath_greedy2()
		throws BDDException
	{

		// the last one, perm[size], is reserved for the total cost
		int[][] all_perm = new int[size][size + 1];
		boolean[] free = new boolean[size];

		// just for fun, we test all of them
		for (int i = 0; i < size; i++)
		{
			int[] perm = all_perm[i];

			for (int j = 0; j < size; j++)
			{
				free[j] = (i != j);    // all free but i
			}

			perm[0] = i;    // start with i
			perm[size] = getSize(i, 0);    // first one

			getShortest_greedy2(i, size - 1, free, perm);

			// BDDBDDAssert.debug("size_" +i+"=" + perm[size] + '\t');
		}

		int best_index = -1;
		int max_value = -1;

		for (int i = 0; i < size; i++)
		{
			if ((i == 0) || (max_value < all_perm[i][size]))
			{
				best_index = i;
				max_value = all_perm[i][size];
			}
		}

		if (Options.debug_on)
		{
			Options.out.println("First node in PCG : node_" + best_index);
		}

		return all_perm[best_index];
	}

	private void getShortest_greedy2(int me, int left, boolean[] free, int[] result)
		throws BDDException
	{

		// Options.out.println("Im " + me + " size " + size + "  left " + left);
		int level = size - left;

		if (left == 0)
		{
			return;
		}
		else if (left == 1)
		{
			int last = getNext_greedy1(me, free, level);

			result[size - left] = last;

			return;
		}

		int max = -1, index = -1;
		boolean first = true;

		for (int i = 0; i < size; i++)
		{
			if (free[i])
			{
				free[i] = false;

				int currw = getWeight(me, i, level);
				int next = getNext_greedy1(i, free, level);
				int nextw = getWeight(i, next, level);

				free[i] = true;

				if (first || (max < (currw + nextw)))
				{
					first = false;
					max = currw + nextw;
					index = i;
				}
			}
		}

		BDDAssert.bddAssert(index != -1, "1: INDEX == -1");

		// add current total weight of the search
		result[size] += getWeight(me, index, level);
		free[index] = false;
		result[size - left] = index;

		getShortest_greedy2(index, left - 1, free, result);
	}

	// ------------------------------------------------------------------------------
	@SuppressWarnings("unused")
	private int[] getShortestPath_greedy()
		throws BDDException
	{
		int[] perm = new int[size + 1];
		boolean[] free = new boolean[size];

		for (int i = 0; i < size; i++)
		{
			free[i] = true;
		}

		perm[size] = 0;    // total weight

		// get the smalles first
		int first = getSmallest(free);

		perm[0] = first;
		free[first] = false;

		int last = first;

		for (int i = 1 /* 1 not 0 ! */; i < size; i++)
		{
			BDDAssert.debug("i=" + i);

			int next = getNext_greedy1(last, free, i);

			perm[i] = next;
			free[next] = false;
			perm[size] += getWeight(last, next, i);
			last = next;
		}

		return perm;
	}

	private int getNext_greedy1(int me_, boolean[] free, int level)
		throws BDDException
	{
		boolean first = true;
		int index = -1, max = -1;
		for (int i = 0; i < size; i++)
		{
			if (free[i])
			{
				int w = getWeight(me_, i, level);    // me.weights[i];

				if (first)
				{
					first = false;
					index = i;
					max = w;
				}
				else if (max < w)
				{
					index = i;
					max = w;
				}
				else if ((max == w) && (Math.random() < 0.5))
				{
					index = i;
					max = w;
				}
			}
		}

		BDDAssert.bddAssert(index != -1, "2: INDEX == -1");

		return index;
	}

	// --------------------------------------------------------------
	// Nearest insertation TSP heruistic.
	// see Disrcrete Optimization lecture notes , chap 4: PH-2
	//
	// 1. chose a cycle C of length 3
	// 2. if V=V(C) then stop (C is a tour)
	// 3. Find a city p \in V - V(C) such there there exists a q \in V(C) satisfying
	//             c_pq   = min { min { c_ij | j \in V(C)} | i \in V - V(C)
	// 4. Determine an edge uv \in C such that
	//             c_up + c_pv - c_uv = {main c_ip + c_pj-c_ij| ic \in C }
	// 5. set C := ( C \ {uv}} \cup {up,pv} and goto 2
	public int[] getShortestPath_NearesInsertation()
	{

		// TODO
		return null;
	}

	// -----------------------------------------------------------------------------
	public void dump()
	{
		for (int i = 0; i < size; i++)
		{
			Options.out.println("Node_" + i + "  (" + nodes[i].id + ")");

			for (int j = 0; j < size; j++)
			{
				if (nodes[i].weights[j] != NOT_CONNECTED)
				{
					Options.out.println("\t --> " + j + "    (weight = " + nodes[i].weights[j] + ")");
				}
			}
		}
	}
}
