/***************** WeightedCalculator.java **********************/
package org.supremica.automata.algorithms.scheduling;

import org.supremica.automata.Automata;

public class WeightedCalculator
	extends DefaultCalculator
{
	private double gWeight = 1;
	private double hWeight = 1;
	
	public WeightedCalculator(Automata automata, double gWeight, double hWeight)
	{
		super(automata);
		this.gWeight = gWeight;
		this.hWeight = hWeight;
	}
	public WeightedCalculator(Estimator estimator, double gWeight, double hWeight)
	{
		super(estimator);
		this.gWeight = gWeight;
		this.hWeight = hWeight;
	}

	public int calculate(Element elem)
	{
		return (int)(gWeight * elem.getCost() + hWeight * estimator.h(elem));
	}

}