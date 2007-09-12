package org.supremica.automata.algorithms.scheduling;

/* 
 * This class implements the basic node, wrapping some useful information stored in:
 *      double[] nodeBasis = [state_0_index, ..., state_m_index, -1, 0,
 *                            parent_node_key, index_of_curr_parent_within_parent_node 
 *                            (-1 if only one instance of the parent have been opened),
 *                            current_costs_0, ..., current_costs_k,
 *                            accumulated_cost, estimate_value].
 */
public class BasicNode
	implements Node
{
	protected double[] basis;

	public BasicNode(double[] basis)
	{
		setBasis(basis);
	}

	public double[] getBasis()
	{
		return basis;
	}

	public void setBasis(double[] basis)
	{
		if (basis == null)
		{
			this.basis = basis;
		}
		else
		{
			this.basis = new double[basis.length];
			for (int i=0; i<basis.length; i++)
			{
				this.basis[i] = basis[i];
			}
		}
	}

	public double getValueAt(int index)
	{
		return basis[index];
	}

	public void setValueAt(int index, double value)
	{
		basis[index] = value;
	}

	public Node emptyClone()
	{
		return new BasicNode(null);
	}
}