
package org.supremica.automata.algorithms.standard;

import org.supremica.automata.*;

class AlphaEpsilonTester
	implements EpsilonTester
{
	Alphabet events;
	boolean notin;

	public AlphaEpsilonTester(Alphabet events, boolean contains)
	{
		this.events = events;
		this.notin = !contains;
	}
	public boolean isThisEpsilon(LabeledEvent event)
	{
		/*
		if(notin)
		{
			return !events.contains(event);
		}
		else
		{
			return events.contains(event);
		}
		*/
		return notin^events.contains(event);
	}

	public String showWhatYouGot()
	{
		if (notin)
			return "keeping " + events.toString();
		else
			return "removing " + events.toString();
	}
}
