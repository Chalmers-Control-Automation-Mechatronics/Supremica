
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
package org.supremica.automata.algorithms;

import java.util.*;
import org.supremica.log.*;
import org.supremica.automata.*;
import org.supremica.properties.SupremicaProperties;
import org.supremica.gui.*;
import org.supremica.util.ActionTimer;

public class AutomataMinimizer
	implements Stoppable
{
	private static Logger logger = LoggerFactory.createLogger(AutomataMinimizer.class);

	// Stoppable stuff
	private ExecutionDialog executionDialog;
	private Stoppable threadToStop = null;
	private boolean stopRequested = false;

	/** The automata being minimized (may be a copy of the original). */
	private Automata theAutomata;

	/** The supplied options. */
	private MinimizationOptions options;

	/** Largest single automaton considered */
	int largestAutomatonSize = 0;

	/**
	 * Basic constructor.
	 */
	public AutomataMinimizer(Automata theAutomata)
	{
		this.theAutomata = theAutomata;
	}

	/**
	 * Sets the executionDialog of this AutomataMinimizer. If executionDialog is null,
	 * the dialog is not updated.
	 */
	public void setExecutionDialog(ExecutionDialog executionDialog)
	{
		this.executionDialog = executionDialog;
	}

	/**
	 * Returns minimized automaton, minimized with respect to the supplied options.
	 */
	public Automaton getCompositionalMinimization(MinimizationOptions options)
		throws Exception
	{
		this.options = options;

		// Are the options valid?
		if (!options.isValid())
		{
			return null;
		}

		// Size in the beginning
		int nbrOfAutomata = theAutomata.size();

		// Initialize execution dialog
		if (executionDialog != null)
		{
			executionDialog.initProgressBar(0, theAutomata.size()-1);
		}

		// While there are more than two automata, compose and minimize!
		while (theAutomata.size() >= 2)
		{
			if (stopRequested)
			{
				return null;
			}

			// Get any automaton
			Automaton autA = theAutomata.getFirstAutomaton();
			Alphabet alphaA = autA.getAlphabet();

			// Find the pair (in which autA is a part) with the highest
			// "unique to total" (number of events) ratio
			double bestUniqueRatio = 0;
			double bestCommonRatio = 0;
			int bestSize = Integer.MAX_VALUE;
			Automaton bestAutB = null;
			Alphabet hideThese = null;
			for (int i=1; i<theAutomata.size(); i++)
			{
				Automaton autB = theAutomata.getAutomatonAt(i);
				Alphabet alphaB = autB.getAlphabet();

				// If there are no common events, try next automaton
				int nbrOfCommonEvents = alphaA.nbrOfCommonEvents(alphaB);
				if (nbrOfCommonEvents == 0)
				{
					if ((bestUniqueRatio == 0) && (bestCommonRatio == 0) && 
						(autB.nbrOfStates() < bestSize))
					{
						bestAutB = autB;
						bestSize = autB.nbrOfStates();
						hideThese = null;
					}
					continue;
				}

				// Calculate the alphabet of unique events
				Alphabet uniqueEvents = Alphabet.union(alphaA, alphaB);
				// The targetAlphabet should not be removed (although they may be "unique")!
				uniqueEvents.minus(options.getTargetAlphabet());
				// Remove events that are present in other automata
				for (int j=1; j<theAutomata.size(); j++)
				{
					// Skip autB (and autA since we iterate from 1 instead of 0)
					if (i == j)
					{
						continue;
					}

					// Remove the events that are present in C, they are not unique to A and B.
					Automaton autC = theAutomata.getAutomatonAt(j);
					Alphabet alphaC = autC.getAlphabet();
					uniqueEvents.minus(alphaC);

					// Early termination
					if (uniqueEvents.size() == 0)
					{
						break;
					}
				}

				// Find ratios
				int nbrOfUniqueEvents = uniqueEvents.size();
				int unionAlphabetSize = alphaA.size() + alphaB.size() - nbrOfCommonEvents;
				double thisUniqueRatio = ((double) nbrOfUniqueEvents)/((double) unionAlphabetSize);
				//double thisUniqueRatio = (double) nbrOfUniqueEvents;
				double thisCommonRatio = ((double) nbrOfCommonEvents)/((double) unionAlphabetSize);

				// Improvement?
				if (thisUniqueRatio > bestUniqueRatio)
				{
					bestAutB = autB;
					bestUniqueRatio = thisUniqueRatio;
					hideThese = uniqueEvents;
				}
				else if ((bestUniqueRatio == 0) && (thisCommonRatio > bestCommonRatio))
				{
					bestAutB = autB;
					bestCommonRatio = thisCommonRatio;
					hideThese = null;
				}
			}

			if (stopRequested)
			{
				return null;
			}

			// Generate automata to minimize
			Automata automata = new Automata();
			automata.addAutomaton(autA);
			automata.addAutomaton(bestAutB);
			
			// Was the system disjoint?
			if (!((bestUniqueRatio > 0) || (bestCommonRatio > 0)))
			{
				logger.warn("The system has disjoint parts. Preferrably, they should " + 
							"be treated separately if possible.");
			}

			/* Already taken care of above?
			// Minimize this part, but always spare events from targetAlphabet!
			if (hideThese != null)
			{
				hideThese.minus(options.getTargetAlphabet());
			}
			// */
			
			//logger.info("Hiding " + hideThese);
			//logger.info("Target " + options.getTargetAlphabet());

			// Perform the minimization, unless of course this is the last step 
			// and it should be skipped...
			Automaton min;
 			if (options.getSkipLast() && theAutomata.size() == 2)
			{
				// Just synch and hide
				min = AutomataSynchronizer.synchronizeAutomata(automata);
				min.hide(hideThese);
			}
			else
			{
				// Compose and minimize!
				min = monolithicMinimization(automata, hideThese);
			}

			if (stopRequested)
			{
				return null;
			}
			//min.remapStateIndices(); // Why did I do that?
			theAutomata.removeAutomata(automata);
			theAutomata.addAutomaton(min);

			// Dispose of originals
			automata.clear();
			
			/*
			// Update gui
			ActionMan.getGui().getVisualProjectContainer().getActiveProject().removeAutomata(automata);
			ActionMan.getGui().getVisualProjectContainer().getActiveProject().addAutomaton(min);
			try
			{
			    Thread.sleep(1000); // Doesn't help much, but removes some of the error messages...
			}
			catch (Exception apa)
			{
			
			}
			*/
			
			if (AutomatonMinimizer.debug)
			{
				logger.error("---------------------------------------------------------------------");
				logger.fatal("Progress: " + (nbrOfAutomata-1-theAutomata.size())*100/(nbrOfAutomata-1) + "%");
			}
			
			// Update execution dialog
			if (executionDialog != null)
			{
				executionDialog.setProgress(nbrOfAutomata-1-theAutomata.size());
			}
		}

		/*
		try
		{
			Thread.sleep(1000);
		}
		catch (Exception apa)
		{
		}
		ActionMan.getGui().getVisualProjectContainer().getActiveProject().addAutomata(theAutomata);
		*/

		// Present largest automaton size
		logger.verbose("The largest automaton examined had " + largestAutomatonSize + " states.");

		// Return the result of the minimization!
		return theAutomata.getFirstAutomaton();
	}

	/**
 	 * Composes automata and minimizes the result with hideThese considered as epsilon
 	 * events.
	 */
	private Automaton monolithicMinimization(Automata automata, Alphabet hideThese)
		throws Exception
	{
		ActionTimer synchTimer = new ActionTimer();

		if (AutomatonMinimizer.debug)
		{
			synchTimer.start();
		}

		// Synch and hide
		Automaton aut = AutomataSynchronizer.synchronizeAutomata(automata);
		aut.hide(hideThese);

 		if (AutomatonMinimizer.debug)
		{
			synchTimer.stop();
			logger.fatal("Synchronization: " + synchTimer);
		}

		// Examine for largest automaton size
		if (aut.nbrOfStates() > largestAutomatonSize)
		{
			largestAutomatonSize = aut.nbrOfStates();
		}

		// Is it at all possible to minimize? (It may actually be possible even
		// if there are no epsilons.)
		if (aut.nbrOfEpsilonTransitions() > 0)
		{
			AutomatonMinimizer minimizer = new AutomatonMinimizer(aut);
			minimizer.useShortStateNames(true);
			threadToStop = minimizer;
			aut = minimizer.getMinimizedAutomaton(options);
			threadToStop = null;
			
			if (stopRequested)
			{
				return null;
			}
		}

		return aut;
	}

	/**
	 * Method that stops AutomataMinimizer as soon as possible.
	 *
	 * @see  ExecutionDialog
	 */
	public void requestStop()
	{
		stopRequested = true;

		logger.debug("AutomataMinimizer requested to stop.");

		// Stop current minimization thread!
		if (threadToStop != null)
		{
			threadToStop.requestStop();
		}
	}
}
