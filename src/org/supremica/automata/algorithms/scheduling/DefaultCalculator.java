/***************** DefaultCalculator.java ***************/
// Calculates the non weithged sum of g + h

package org.supremica.automata.algorithms.scheduling;

import org.supremica.automata.Automata;

public class DefaultCalculator
	implements Calculator
{
	Estimator estimator = null;
	
	public DefaultCalculator(Estimator estimator)
	{
		this.estimator = estimator;
	}
	
	public DefaultCalculator(Automata automata)
	{
		this(new DefaultEstimator(automata));
	}
	
	public Estimator getEstimator()
	{
		return estimator;
	}
	
	public int calculate(Element elem)
	{
		return elem.getCost() + estimator.h(elem);
	}
}