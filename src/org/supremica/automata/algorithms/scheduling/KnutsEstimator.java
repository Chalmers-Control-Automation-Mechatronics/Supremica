/**************** KnutsEstimator.java **********************/
package org.supremica.automata.algorithms.scheduling;

import org.supremica.automata.*;

class KnutsEstimator
	extends DefaultEstimator
{
	public KnutsEstimator(Automata automata)
	{
		super(automata);
	}
	
	// Try the estimate max Tv
	public int h(Element state)			// For this composite state, return an estimate
	{
		int[] arr = state.getTimeArray();
		int max = 0;
		for(int i = 0; i < arr.length; ++i)
		{
			if(arr[i] > max)
			{
				max = arr[i];
			}
		}
		return max;
	}
}
	