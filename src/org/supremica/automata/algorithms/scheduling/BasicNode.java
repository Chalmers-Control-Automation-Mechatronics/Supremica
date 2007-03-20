package org.supremica.automata.algorithms.scheduling;

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