
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
 * Haradsgatan 26A
 * 431 42 Molndal
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
package org.supremica.automata;

import java.util.*;
import org.supremica.gui.*;
import org.apache.log4j.*;
import org.supremica.util.IDD.*;

public final class AutomataIDDForm
{
	private static Category thisCategory = LogDisplay.createCategory(AutomataIDDForm.class.getName());
	private Automata theAutomata;
	private AutomataIndexForm theAutomataIndexForm;
	private Automaton theAutomaton;
	private IDD initialStateIDD;
	private IDD[] enableEventIDDs;
	private int nbrOfEvents;

	public AutomataIDDForm(Automata theAutomata)
		throws Exception
	{
		this.theAutomata = theAutomata;
		theAutomaton = new Automaton();

		Alphabet theAlphabet = theAutomata.getUnionAlphabet();

		theAutomaton.setAlphabet(theAlphabet);

		theAutomataIndexForm = new AutomataIndexForm(theAutomata, theAutomaton);
		nbrOfEvents = theAlphabet.size();
	}

	private void buildEnableEventIDDs()
	{
		enableEventIDDs = new IDD[nbrOfEvents];

		for (int i = 0; i < nbrOfEvents; i++)
		{
			enableEventIDDs[i] = createEnableEventIDD(i);
		}
	}

	/**
	 *
	 */
	private IDD createEnableEventIDD(int eventId)
	{
		IDDBuilder builder = new IDDBuilder(theAutomataIndexForm.getAutomataSize());

		// Find all automata that has eventId in its alphabet
		// In each of these automata find all states that has eventId as
		// an outgoing eventId.
		// If an automaton includes eventId in it's alphabet but no state
		// has eventId as an outgoing event, then return the false IDD (== 0).
		boolean[][] alphabetEventsTable = theAutomataIndexForm.getAlphabetEventsTable();
		int[][][] enableEventsTable = theAutomataIndexForm.getEnableEventsTable();

		for (int i = 0; i < alphabetEventsTable.length; i++)
		{
			if (alphabetEventsTable[i][eventId])
			{    // automaton i includes eventId

				// Find all states that enables eventId
				int[] enableState = enableEventsTable[i][eventId];
				int j = 0;

				while (enableState[j] != Integer.MAX_VALUE)
				{
					j++;
				}

				if (j == 0)
				{
					return IDD.getFalseIDD(theAutomataIndexForm.getAutomataSize());
				}

				int[] theStates = new int[j];

				System.arraycopy(enableState, 0, theStates, 0, j);
				builder.or(i, theStates);
			}
		}

		return builder.getIDD();
	}
}
