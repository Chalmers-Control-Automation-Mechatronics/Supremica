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
	extends BasicNode
{
	private ArrayList<MultithreadedAstar> subthreads;

	public MultithreadedNode(double[] basis)
	{
		this(basis, null);
	}

	public MultithreadedNode(double[] basis,
	                         ArrayList<MultithreadedAstar> subthreads)
	{
		super(basis);

		this.subthreads = subthreads;
	}

	public Node emptyClone()
	{
		return new MultithreadedNode(null, null);
	}

	public ArrayList<MultithreadedAstar> getSubthreads()
	{
		return subthreads;
	}

	public void addSubthread(MultithreadedAstar subthread)
	{
		if (subthreads == null)
		{
			subthreads = new ArrayList<MultithreadedAstar>();
		}

		subthreads.add(subthread);
	}
}