
/********************* Calculator.java **********************/

// Interface for calculators that calc the bound for a new state
package org.supremica.automata.algorithms.scheduling;

public interface Calculator
{
	public Estimator getEstimator();

	public int calculate(Element elem);
}
