package org.supremica.automata.algorithms.standard;


import org.supremica.log.*;

import java.util.HashSet;
import java.util.Iterator;

import org.supremica.automata.LabeledEvent;
import org.supremica.automata.Automaton;
import org.supremica.automata.StateSet;
import org.supremica.automata.Alphabet;
import org.supremica.automata.State;
import org.supremica.automata.StateIterator;
import org.supremica.automata.Arc;
import org.supremica.automata.ArcIterator;

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
