/******************** TwoProductRelaxation.java ****************/
package org.supremica.automata.algorithms.scheduling;

import org.supremica.automata.*;

class TwoProductRelaxation 
	extends DefaultEstimator
{
	
	public TwoProductRelaxation(Automata automata)	// Here we should precalculate the estimates
	{
		super(automata);
		// calc the two-product relaxation estimates
	}
}
	