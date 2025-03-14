package org.supremica.automata.algorithms.scheduling;

import java.util.*;

public class OpenTreeComparator
	implements Comparator<Object> //<Object[]>

{
	private final int comparatorIndex;

	public OpenTreeComparator(final int comparatorIndex)
	{
		this.comparatorIndex = comparatorIndex;
	}

	/**
	 * If two nodes have equal estimate function value, the new node is placed
	 * first, to direct the search in depth rather than breadth direction.
	 */
	public int compare(final int[] newNode, final int[] openNode)
	{
		// If newNode and openNode point to the same location, then they are equal
		if (newNode == openNode)
		{
			return 0;
		}

		// If newNode and openNode do not point to the same location, but consist of
		// identical elements, then they are equal. Otherwise, the new node should be
		// placed first in the openTree.
		if (newNode[comparatorIndex] == openNode[comparatorIndex])
		{
			for (int i=0; i<newNode.length; i++)
			{
				if (newNode[i] != openNode[i])
				{
					return -1;
				}
			}

			return 0;
		}

		return newNode[comparatorIndex] - openNode[comparatorIndex];
	}


	/**
	 * If two node have equal estimate function value, the new node is placed
	 * first, to direct the search in depth rather than breadth direction.
 	 */
	public int compare(final double[] newNode, final double[] openNode)
	{
		// If newNode and openNode point to the same location, then they are equal
		if (newNode == openNode)
		{
			return 0;
		}

		// If newNode and openNode do not point to the same location, but have the
		// same comparison cost and consist of identical elements, then they are equal.
		// Otherwise (but still assuming that they have the same cost), the new node is
		// placed first.
		if (newNode[comparatorIndex] == openNode[comparatorIndex])
		{
			for (int i=0; i<newNode.length; i++)
			{
				if (newNode[i] != openNode[i])
				{
					return -1;
				}
			}

			return 0;
		}

		if (newNode[comparatorIndex] - openNode[comparatorIndex] < 0)
			return -1;

		return 1;
	}

	public int compare(final Node a, final Node b)
	{
		return compare(a.getBasis(), b.getBasis());
	}

	public int compare(final Object a, final Object b)
	{
		if (a instanceof int[])
		{
			return compare((int[]) a, (int[]) b);
		}
		else if (a instanceof double[])
		{
			return compare((double[]) a, (double[]) b);
		}
		else if (a instanceof Node)
		{
			return compare((Node) a, (Node) b);
		}
		else
		{
			return 0;
		}
	}
}