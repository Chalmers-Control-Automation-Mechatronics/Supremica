package org.supremica.automata.algorithms.scheduling;

public interface Relaxer
{
    /** 
	 * Returns the so-called relaxation value of the supplied node, 
	 * i.e. the method returns the estimated cost to some marked node 
	 * from the supplied node. 
	 */
    public double getRelaxation(Node node)
		throws Exception;
}