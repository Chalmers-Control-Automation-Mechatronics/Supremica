/*********************** DefaultEstimator.java ******************/
package org.supremica.automata.algorithms.scheduling;

import org.supremica.automata.*;

public class DefaultEstimator
	implements Estimator
{
	// Assumptions about automata:
	//	* plants are the resources
	//	* specs are the product routes
	private Automata automata;
	
	public DefaultEstimator(Automata automata)	// Here we should precalculate the estimates
	{
		this.automata = automata;
	}
	
	public Automata getAutomata()		// Return the stored automata
	{
		return automata;
	}
	
	public int h(Element state)			// For this composite state, return an estimate
	{
		return 0;	// 0 is always less than the exact "estimate" h*, so should always give the optimal
	}
}
	