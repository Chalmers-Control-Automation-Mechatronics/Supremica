
/*
 * Supremica Software License Agreement
 *
 * The Supremica software is not in the public domain
 * However, it is freely available without fee for education,
 * research, and non-profit purposes.  By obtaining copies of
 * this and other files that comprise the Supremica software,
 * you, the Licensee, agree to abide by the following
 * conditions and understandings with respect to the
 * copyrighted software:
 *
 * The software is copyrighted in the name of Supremica,
 * and ownership of the software remains with Supremica.
 *
 * Permission to use, copy, and modify this software and its
 * documentation for education, research, and non-profit
 * purposes is hereby granted to Licensee, provided that the
 * copyright notice, the original author's names and unit
 * identification, and this permission notice appear on all
 * such copies, and that no charge be made for such copies.
 * Any entity desiring permission to incorporate this software
 * into commercial products or to use it for commercial
 * purposes should contact:
 *
 * Knut Akesson (KA), knut@supremica.org
 * Supremica,
 * Knarrhogsgatan 10
 * SE-431 60 MOLNDAL
 * SWEDEN
 *
 * to discuss license terms. No cost evaluation licenses are
 * available.
 *
 * Licensee may not use the name, logo, or any other symbol
 * of Supremica nor the names of any of its employees nor
 * any adaptation thereof in advertising or publicity
 * pertaining to the software without specific prior written
 * approval of the Supremica.
 *
 * SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 * SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 * IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 * Supremica or KA shall not be liable for any damages
 * suffered by Licensee from the use of this software.
 *
 * Supremica is owned and represented by KA.
 */
package org.supremica.automata.algorithms;

import java.util.*;
import org.supremica.automata.*;

/**
 * Changes the name of all states in the automata. The state name will
 * be "prefix" followed by an integer, followed by "postfix".
 */
public class DefaultEventRenamer
	implements EventRenamer
{
	protected Automata theAutomata;
	protected String prefixString = "e_";
	protected String postfixString = "";
	protected int currIndex = 0;

	public DefaultEventRenamer(Automata theAutomata)
	{
		this.theAutomata = theAutomata;
	}

	public DefaultEventRenamer(Automaton theAutomaton)
	{
		this(new Automata(theAutomaton));
	}

	public void setPrefix(String theString)
	{
		this.prefixString = theString;
	}

	public String getPrefix()
	{
		return prefixString;
	}

	public void setPostfix(String theString)
	{
		this.postfixString = theString;
	}

	public String getPostfix()
	{
		return postfixString;
	}

	public void execute()
	{
		if (prefixString == null)
		{
			prefixString = "";
		}

		if (postfixString == null)
		{
			postfixString = "";
		}

		// Build union alphabet
		Alphabet unionAlphabet = theAutomata.setIndicies();

		// Build a map from old events to new.
		HashMap oldToNew = new HashMap();

		for (Iterator theIt = unionAlphabet.iterator(); theIt.hasNext(); )
		{
			LabeledEvent currEvent = (LabeledEvent) theIt.next();

			oldToNew.put(currEvent.getLabel(), newLabel(currEvent));
		}

		// Change the labels
		for (Iterator theIterator = theAutomata.iterator();
				theIterator.hasNext(); )
		{
			Automaton currAutomaton = (Automaton) theIterator.next();

			for (Iterator evIt = currAutomaton.eventIterator();
					evIt.hasNext(); )
			{
				LabeledEvent currEvent = (LabeledEvent) evIt.next();
				String newEvent = (String) oldToNew.get(currEvent.getLabel());

				currEvent.setLabel(newEvent);
			}
		}
	}

	/**
	 * This method computes a new label for current event.
	 * Override this method to change its implementation
	 **/
	protected String newLabel(LabeledEvent currEvent)
	{
		currIndex++;

		return prefixString + (currIndex - 1) + postfixString;
	}
}
