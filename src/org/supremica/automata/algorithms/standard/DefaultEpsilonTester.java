package org.supremica.automata.algorithms.standard;

import org.supremica.automata.LabeledEvent;

public class DefaultEpsilonTester
	implements EpsilonTester
{
	public boolean isThisEpsilon(LabeledEvent event)
	{
		return event.isEpsilon();
	}

	// debug only
	public String showWhatYouGot()
	{
		return "removing epsilons";
	}
}
