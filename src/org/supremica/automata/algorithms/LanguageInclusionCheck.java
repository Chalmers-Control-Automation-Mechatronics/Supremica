
/*
 *  Supremica Software License Agreement
 *
 *  The Supremica software is not in the public domain
 *  However, it is freely available without fee for education,
 *  research, and non-profit purposes.  By obtaining copies of
 *  this and other files that comprise the Supremica software,
 *  you, the Licensee, agree to abide by the following
 *  conditions and understandings with respect to the
 *  copyrighted software:
 *
 *  The software is copyrighted in the name of Supremica,
 *  and ownership of the software remains with Supremica.
 *
 *  Permission to use, copy, and modify this software and its
 *  documentation for education, research, and non-profit
 *  purposes is hereby granted to Licensee, provided that the
 *  copyright notice, the original author's names and unit
 *  identification, and this permission notice appear on all
 *  such copies, and that no charge be made for such copies.
 *  Any entity desiring permission to incorporate this software
 *  into commercial products or to use it for commercial
 *  purposes should contact:
 *
 *  Knut Akesson (KA), knut@supremica.org
 *  Supremica,
 *  Haradsgatan 26A
 *  431 42 Molndal
 *  SWEDEN
 *
 *  to discuss license terms. No cost evaluation licenses are
 *  available.
 *
 *  Licensee may not use the name, logo, or any other symbol
 *  of Supremica nor the names of any of its employees nor
 *  any adaptation thereof in advertising or publicity
 *  pertaining to the software without specific prior written
 *  approval of the Supremica.
 *
 *  SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 *  SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 *  IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 *  Supremica or KA shall not be liable for any damages
 *  suffered by Licensee from the use of this software.
 *
 *  Supremica is owned and represented by KA.
 */
package org.supremica.automata.algorithms;

import java.util.*;
import org.supremica.util.IntArrayHashTable;
import java.io.PrintWriter;
import org.supremica.gui.*;
import org.apache.log4j.*;
import org.supremica.automata.Alphabet;
import org.supremica.automata.AlphabetHelpers;
import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.AutomatonType;
import org.supremica.automata.EventsSet;
import org.supremica.automata.EventLabel;

/**
 *@author  ka
 *@created  November 28, 2001
 *@deprecated  No longer used! Use AutomataVerificationWorker instead!
 *@see  AutomataVerificationWorker
 */
public class LanguageInclusionCheck
{
	private static Category thisCategory = LogDisplay.createCategory(LanguageInclusionCheck.class.getName());
	private Automata AutomataA;
	private Automata AutomataB;
	private SynchronizationOptions syncOptions;

	public LanguageInclusionCheck(Automata AutomataA, Automata AutomataB, SynchronizationOptions syncOptions)
		throws IllegalArgumentException
	{

		// Make copies of AutomataA and AutomataB
		this.AutomataA = new Automata(AutomataA);
		this.AutomataB = new Automata(AutomataB);
		this.syncOptions = syncOptions;
	}

	/**
	 * Performs the language inclusion check, examines if L(AutomataA) is included in L(AutomataB)
	 *
	 *@return  Description of the Return Value
	 *@exception  Exception Description of the Exception
	 */
	public boolean execute()
		throws Exception
	{
		Automaton currAutomaton;
		Iterator eventIteratorA;
		Iterator eventIteratorB;

		// Compute the union alphabet of the events in automataA, mark all
		// events in automataA as uncontrollable and the automata as plants
		EventsSet theAlphabets = new EventsSet();
		Alphabet unionAlphabet;
		Iterator automatonIteratorA = AutomataA.iterator();

		while (automatonIteratorA.hasNext())
		{
			currAutomaton = (Automaton) automatonIteratorA.next();

			Alphabet currAlphabet = currAutomaton.getAlphabet();

			theAlphabets.add(currAlphabet);
			currAutomaton.setType(AutomatonType.Plant);

			eventIteratorA = currAutomaton.eventIterator();

			while (eventIteratorA.hasNext())
			{
				((EventLabel) eventIteratorA.next()).setControllable(false);
			}
		}

		if (theAlphabets.size() == 1)
		{
			unionAlphabet = (Alphabet) theAlphabets.get(0);
		}
		else
		{
			unionAlphabet = AlphabetHelpers.getUnionAlphabet(theAlphabets, "");
		}

		// Change events in the automata in automata B to uncontrollable if they
		// are included in the union alphabet found above, mark the automata as
		// specifications
		Iterator automatonIteratorB = AutomataB.iterator();
		EventLabel currEvent;

		while (automatonIteratorB.hasNext())
		{
			currAutomaton = (Automaton) automatonIteratorB.next();

			currAutomaton.setType(AutomatonType.Supervisor);

			eventIteratorB = currAutomaton.eventIterator();

			while (eventIteratorB.hasNext())
			{
				currEvent = (EventLabel) eventIteratorB.next();

				if (unionAlphabet.containsEventWithLabel(currEvent.getLabel()))
				{
					currEvent.setControllable(false);
				}
				else
				{
					currEvent.setControllable(true);
				}
			}
		}

		// After the above preparations, the language inclusion check
		// can be performed as a controllability check...
		AutomataA.addAutomata(AutomataB);

		AutomataFastControllabilityCheck controllabilityCheck = new AutomataFastControllabilityCheck(AutomataA, syncOptions);

		return controllabilityCheck.execute();
	}
}
