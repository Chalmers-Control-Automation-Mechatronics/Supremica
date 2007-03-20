package org.supremica.automata.algorithms.scheduling;

public interface Node
{
	/** 
	 * Returns the underlying double[]-representation of the node,
	 * that is used by Supremica to keep track of which state
	 * this node represents and what is its costs along some path. 
	 */
	public double[] getBasis();

	public void setBasis(double[] basis);

	public double getValueAt(int index);

	public void setValueAt(int index, double value);

	public Node emptyClone();
}
