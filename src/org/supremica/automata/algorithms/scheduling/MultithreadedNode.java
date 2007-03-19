/******************** MultithreadedNode.java **************************
 *
 * Extension of the node object, used in the A* search. This object
 * contains the usual node-information, such as the indices of the 
 * underlying states, costs, f-, g- and Tv-values, etc. In addition,
 * this object contains a set of pointers to subthreads that originate
 * their search from this node. 
 */

package org.supremica.automata.algorithms.scheduling;

import java.util.ArrayList;

public class MultithreadedNode
{
	private double[] nodeBasis;
	private ArrayList subthreads;

	public MultithreadedNode(double[] nodeBasis, ArrayList subthreads)
	{
		this.nodeBasis = new double[nodeBasis.length];
		for (int i=0; i<nodeBasis.length; i++)
		{
			this.nodeBasis[i] = nodeBasis[i];
		}

		this.subthreads = subthreads;
	}

	public MultithreadedNode(double[] nodeBasis)
	{
		this(nodeBasis, null);
	}

	public double[] getNodeBasis()
	{
		return nodeBasis;
	}

	public ArrayList getSubthreads()
	{
		return subthreads;
	}
}