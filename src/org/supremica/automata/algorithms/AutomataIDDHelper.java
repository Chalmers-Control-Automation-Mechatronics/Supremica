
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

import org.supremica.util.IDD.*;
import org.supremica.log.*;
import java.util.*;
import org.supremica.automata.Alphabet;
import org.supremica.automata.AlphabetHelpers;
import org.supremica.automata.Automata;
import org.supremica.automata.AutomataIndexForm;
import org.supremica.automata.Automaton;
import org.supremica.automata.EventsSet;

public final class AutomataIDDHelper
{
	private static Logger logger = LoggerFactory.createLogger(AutomataSynchronizerHelper.class);
	private AutomataIndexForm theAutomataIndexForm;
	private Automata theAutomata;
	private Automaton theAutomaton;
	private IDDOptions iddOptions = null;
	private IDD[] enableEventPredicateArray;

	public AutomataIDDHelper(Automata theAutomata, IDDOptions iddOptions)
		throws Exception
	{
		if (theAutomata == null)
		{
			throw new Exception("theAutomata must be non-null");
		}

		if (iddOptions == null)
		{
			throw new Exception("IDDOptions must be non-null");
		}

		this.theAutomata = theAutomata;
		this.iddOptions = iddOptions;
		theAutomaton = new Automaton();

		// Compute the new alphabet
		EventsSet theAlphabets = new EventsSet();
		Iterator autIt = theAutomata.iterator();

		while (autIt.hasNext())
		{
			Automaton currAutomaton = (Automaton) autIt.next();
			Alphabet currAlphabet = currAutomaton.getAlphabet();

			theAlphabets.add(currAlphabet);
		}

		try
		{
			Alphabet theAlphabet = AlphabetHelpers.getUnionAlphabet(theAlphabets);    //, "a");

			theAutomaton.getAlphabet().union(theAlphabet);
		}
		catch (Exception e)
		{
			logger.error("Error while generating union alphabet: " + e);
			logger.debug(e.getStackTrace());

			throw e;
		}

		try
		{
			theAutomataIndexForm = new AutomataIndexForm(theAutomata, theAutomaton);
		}
		catch (Exception e)
		{
			logger.error("Error while computing AutomataIndexForm");
			logger.debug(e.getStackTrace());

			throw e;
		}
	}

	public void clear() {}

	public IDDOptions getIddOptions()
	{
		return iddOptions;
	}

	public Automaton getAutomaton()
	{
		return theAutomaton;
	}

	public Automata getAutomata()
	{
		return theAutomata;
	}

	public AutomataIndexForm getAutomataIndexForm()
	{
		return theAutomataIndexForm;
	}

	/*
	 *  private boolean buildEnableEventPredicates()
	 *  {
	 *  Alphabet unionAlphabet = theAutomaton.getAlphabet();
	 *  enableEventPredicateArray = new IDD[unionAlphabet.getSize()];
	 *
	 *  }
	 */
}
