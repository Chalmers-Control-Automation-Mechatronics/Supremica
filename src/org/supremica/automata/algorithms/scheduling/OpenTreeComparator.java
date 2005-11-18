package org.supremica.automata.algorithms.scheduling;

import java.util.*;

public class OpenTreeComparator
	implements Comparator<int[]>
{

	/**
	 * If two node have equal estimate function value, the new node is placed
	 * first, to direct the search in depth rather than breadth direction.
	 */
	public int compare(int[] newNode, int[] openNode)
	{
		// If newNode and openNode point to the same location, then they are equal
		if (newNode == openNode)
			return 0;

		// If newNode and openNode do not point to the same location, but consist of 
		// identical elements, then they are equal. Otherwise, the new node should be 
		// placed first in the openTree.
		if (newNode[AbstractAstar.ESTIMATE_INDEX] == openNode[AbstractAstar.ESTIMATE_INDEX])
		{
			for (int i=0; i<newNode.length; i++)
			{
				if (newNode[i] != openNode[i])
					return -1;
			}

			return 0;
		}
		
		return newNode[AbstractAstar.ESTIMATE_INDEX] - openNode[AbstractAstar.ESTIMATE_INDEX]; 
	}
}