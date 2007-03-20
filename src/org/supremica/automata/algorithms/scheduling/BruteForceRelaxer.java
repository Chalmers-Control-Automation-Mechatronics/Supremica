package org.supremica.automata.algorithms.scheduling;

public class BruteForceRelaxer
	implements Relaxer
{
	public BruteForceRelaxer()
	{
	}

   	/**
	 * This returns the brute-force relaxation value for the supplied node, 
	 * which is always 0.
	 * 
	 * @param node the current node
	 * @return double the heuristic function, h(n), that guides the search, 
	 * in this case it is the "brute-force relaxation"
	 */
    public double getRelaxation(Node node) 
		throws Exception
	{
		return 0;
    }
}