
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

	/** Event to automata map, for choosing the next task in compositional minimization. */
	private EventToAutomataMap eventToAutomataMap;

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

		// For each event, fint the automata that has this event in its alphabet
		if (options.getMinimizationStrategy() == MinimizationStrategy.AtLeastOneUnique)
		{
			eventToAutomataMap = AlphabetHelpers.buildEventToAutomataMap(theAutomata);
		}
		
		// As long as there are at least two automata, compose and minimize!
		while (theAutomata.size() >= 2)
		{
			// Get next automata to minimize
			MinimizationTask task = getNextMinimizationTask();

			if (stopRequested)
			{
				return null;
			}

			Automata automata = task.getAutomata();
			Alphabet hideThese = task.getEventsToHide();
			
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

			// Adjust the eventToAutomataMap?
			if (options.getMinimizationStrategy() == MinimizationStrategy.AtLeastOneUnique)
			{
				// Remove the examined automata from the map
				for (EventIterator it = automata.getUnionAlphabet().iterator(); it.hasNext(); )
				{
					Automata aut = eventToAutomataMap.get(it.nextEvent());
					if (aut != null)
					{
						aut.removeAutomata(automata);
					}
				}
				// And add the new one!
				for (EventIterator it = min.getAlphabet().iterator(); it.hasNext(); )
				{
					eventToAutomataMap.put(it.nextEvent(), min);
				}
			}

			//min.remapStateIndices(); // Why did I do that?
			theAutomata.removeAutomata(automata);
			theAutomata.addAutomaton(min);

			// Dispose of originals
			automata.clear();

			if (AutomatonMinimizer.debug)
			{
				logger.error("---------------------------------------------------------------------");
				logger.fatal("Progress: " + (nbrOfAutomata-theAutomata.size())*100/(nbrOfAutomata-1) + "%");
			}
			
			// Update execution dialog
			if (executionDialog != null)
			{
				executionDialog.setProgress(nbrOfAutomata-theAutomata.size());
			}
		}

		// Print total reduction statistics
		AutomatonMinimizer.printTotal();
		AutomatonMinimizer.resetTotal();

		// Present largest automaton size
		logger.verbose("The largest automaton examined had " + largestAutomatonSize + " states.");

		// Return the result of the minimization!
		return theAutomata.getFirstAutomaton();
	}

	/**
	 * Class holding info about what should be done in the next minimization. Which automata and
	 * which events that can be abstracted to epsilons.
	 */
	private class MinimizationTask
	{
		private Automata automata;
		private Alphabet eventsToHide;

		public MinimizationTask(Automata automata, Alphabet eventsToHide)
		{
			this.automata = automata;
			this.eventsToHide = eventsToHide;
		}

		public Automata getAutomata()
		{
			return automata;
		}

		public Alphabet getEventsToHide()
		{
			return eventsToHide;
		}
	}

	/**
	 * Returns the next Automata that is predicted to be the best one to do minimization on next.
	 */
	private MinimizationTask getNextMinimizationTask()
	{
		Automata result;
		Alphabet hideThese;

		// Which strategy should be used to select the next task?
		MinimizationStrategy strategy = options.getMinimizationStrategy();
		if (strategy == MinimizationStrategy.BestPair)
		{
			result = new Automata();

			// Get any automaton
			Automaton autA = theAutomata.getFirstAutomaton();
			Alphabet alphaA = autA.getAlphabet();
			
			// Find the pair (in which autA is a part) with the highest
			// "unique to total" (number of events) ratio
			double bestUniqueRatio = 0;
			double bestCommonRatio = 0;
			int bestSize = Integer.MAX_VALUE;
			Automaton bestAutB = null;
			hideThese = null;
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
				Alphabet uniqueEvents = AlphabetHelpers.union(alphaA, alphaB);
				// The targetAlphabet should not be removed (although those events may be "unique")!
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
			
			// Generate result
			result.addAutomaton(autA);
			result.addAutomaton(bestAutB);
			if (hideThese == null)
			{
				hideThese = new Alphabet();
			}

			// Was the system disjoint?
			if (!((bestUniqueRatio > 0) || (bestCommonRatio > 0)))
			{
				logger.warn("The system has disjoint parts. Preferrably, they should " + 
							"be treated separately if possible.");
			}
		}	
		else if (strategy == MinimizationStrategy.AtLeastOneUnique)
		{
			// Target alphabet
			Alphabet targetAlphabet = options.getTargetAlphabet();

			// The result so far
			result = null;

			// Look through the map and find the smallest set of automata
			EventIterator evIt = eventToAutomataMap.iterator();
			while (evIt.hasNext())
			{
				LabeledEvent event = evIt.nextEvent();

				if (stopRequested)
				{
					return null;
				}

				// Skip the events in targetAlphabet and epsilon events!
				if (targetAlphabet.contains(event) || event.isEpsilon())
				{
					continue;
				}

				// Get the automata that have this event in their alphabet
				Automata automata = eventToAutomataMap.get(event);

				// Take as few automata as possible as the next task
				if ((result == null) || (automata.size() < result.size()))
				{
					result = automata;
					if (result.size() == 1)
					{
						break;
					}
				}
			}

			// Did we find an apropriate result?
			if (result != null)
			{
				// Which events should be hidden?
				hideThese = getUniqueEvents(result, theAutomata);
				
				//logger.info(result);
				//logger.info(hideThese);
			}
			else
			{
				// This must mean that there were only targetAlphabet events left? We should not
				// take all of them at once (they may be many!) but I'll do that for now...
				result = new Automata(theAutomata);

				// Just in case... but it should be empty?
				hideThese = result.getUnionAlphabet();
				hideThese.minus(targetAlphabet);
				assert(hideThese.size() == 0);
			}

			// Adjust the EventToAutomataMap! (We don't want to recalculate it every time.)
			// Remove the hidden ones
			for (EventIterator it = hideThese.iterator(); it.hasNext(); )
			{
				eventToAutomataMap.remove(it.nextEvent());
			}
		}
		else
		{
			logger.error("Error in AutomataMinimizer, undefined MinimizationStrategy.");
			requestStop();
			return null;
		}

		// Remember that we should never hide the events in options.getTargetAlphabet()!!!
		assert(AlphabetHelpers.intersect(hideThese, options.getTargetAlphabet()).size() == 0);

		// Return result
		return new MinimizationTask(result, hideThese);
	}

	/**
	 * This method examines how many unique events there are in autA with respect to autB.
	 * If automata from autA are included in autB, they are ignored.
	 */
	private Alphabet getUniqueEvents(Automata autA, Automata autB)
	{
		/*
		Automata autNotA = new Automata(autB);
		autNotA.removeAutomata(autA);

		Alphabet alphaA = autA.getUnionAlphabet();
		Alphabet alphaNotA = autNotA.getUnionAlphabet();
		*/

		Alphabet unique = autA.getUnionAlphabet();
		Alphabet toBeRemoved = new Alphabet();
		for (EventIterator it = unique.iterator(); it.hasNext(); )
		{
			LabeledEvent event = it.nextEvent();

			Automata sharers = eventToAutomataMap.get(event);
			
			for (AutomatonIterator autIt = sharers.iterator(); autIt.hasNext(); )
			{
				// If there is an automaton here that is not in autA, this event is not unique to autA...
				if (!autA.containsAutomaton(autIt.nextAutomaton()))
				{
					toBeRemoved.addEvent(event);
					break;
				}
			}
		}
		unique.minus(toBeRemoved);

		return unique;
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

		// We don't really care about the state names, keep them short!
		boolean useShortStateNames = true;

		// Synchronize, or if there's just one automaton, just find it
		Automaton aut;
		if (automata.size() > 1)
		{
			// Synch
			SynchronizationOptions synchOptions = SynchronizationOptions.getDefaultSynchronizationOptions();
			synchOptions.setUseShortStateNames(useShortStateNames);
			aut = AutomataSynchronizer.synchronizeAutomata(automata, synchOptions);
		}
		else
		{
			/*
			if (useShortStateNames)
			{
				EnumerateStates en = new EnumerateStates(automata, "q");				
				en.execute();
			}
			*/
			aut = automata.getFirstAutomaton();

			// This is probably one of the originals, so we might need to make a copy!
			if (options.getKeepOriginal())
			{
				aut = new Automaton(aut);
			}
		}

		// Hide the events!
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
		// if there are no epsilons... but I don't care...)
		if (aut.nbrOfEpsilonTransitions() > 0)
		{
			AutomatonMinimizer minimizer = new AutomatonMinimizer(aut);
			minimizer.useShortStateNames(useShortStateNames);
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
