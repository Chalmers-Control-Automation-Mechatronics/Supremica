
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

import org.supremica.log.*;
import org.supremica.gui.Gui;
import org.supremica.automata.Alphabet;
import org.supremica.automata.AlphabetHelpers;
import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.gui.VisualProjectContainer;
import org.supremica.automata.AutomatonType;
import org.supremica.automata.EventsSet;
import org.supremica.automata.State;
import org.supremica.automata.LabeledEvent;

//-- MF -- Pass AutomatSelector a set of specs/sups/plants, 
//-- MF -- For each spec/sup it returns that automaton together with the plants with which it shares uc-events
//-- MF -- If closedSet == true the returned set is closed in that all plants that share uc-events with any plant in the set is also included
class AutomataSelector
{
	private boolean closedSet;
	private Automata globalSet;
	private Automata partialSet;
	private Iterator specIterator;
	private HashMap eventToAutomataMap = new HashMap();

	public AutomataSelector(Automata globalSet, boolean closedSet)
	{
		this.globalSet = globalSet;
		this.closedSet = closedSet;
		specIterator = globalSet.iterator();

		AlphabetAnalyzer alphabetAnalyzer = new AlphabetAnalyzer(globalSet);
		eventToAutomataMap = alphabetAnalyzer.getUncontrollableEventToPlantMap();
	}
	
	// Loop over supervisors/specifications and find plants containing equal uncontrollable events
	public Automata next()
	{
		partialSet.clear();
		
		while (specIterator.hasNext())
		{
			Automaton currSupervisorAutomaton = (Automaton) specIterator.next();
			
			if ((currSupervisorAutomaton.getType() == AutomatonType.Supervisor) || (currSupervisorAutomaton.getType() == AutomatonType.Specification))
			{
				// Examine uncontrollable events in currSupervisorAutomaton and select plants accordingly
				partialSet.addAutomaton(currSupervisorAutomaton);

				ArrayList eventList = new ArrayList(currSupervisorAutomaton.eventCollection());

				while (!eventList.isEmpty())
				{
					LabeledEvent currEvent = (LabeledEvent) eventList.remove(0);

					if (!currEvent.isControllable())
					{
						if (eventToAutomataMap.get(currEvent) != null)
						{
							Iterator plantIterator = ((Set) eventToAutomataMap.get(currEvent)).iterator();

							while (plantIterator.hasNext())
							{
								Automaton currPlantAutomaton = (Automaton) plantIterator.next();

								// This check is performed in eventToAutomataMap
								// if (currPlantAutomaton.getType() == AutomatonType.Plant)
								if (!partialSet.containsAutomaton(currPlantAutomaton))
								{
									partialSet.addAutomaton(currPlantAutomaton);

									// If we want a closed set, we need to add plants with
									// uncontrollable events common to the already added plants too...
									if (closedSet)
									{
										eventList.addAll(currPlantAutomaton.eventCollection());
									}
								}
							}
						}
					}
				}
			}
		}
		
		return partialSet; // empty, only when we're done. For a spec with no matching plants, this spec will be included	}
	}
}

public class AutomataSynthesizer
{
	private static Logger logger = LoggerFactory.createLogger(AutomataSynthesizer.class);
	private Automata theAutomata;
	private int nbrOfExecuters;
	private HashMap eventToAutomataMap = new HashMap();
	private AutomataSynchronizerHelper synchHelper;
	private ArrayList synchronizationExecuters;
	private SynchronizationOptions synchronizationOptions;
	private SynthesizerOptions synthesizerOptions;
	private int[] initialState;
	private VisualProjectContainer theVisualProjectContainer;
	private Gui gui;

	// For the optimization...
	private Automata newAutomata = new Automata();
	private boolean maximallyPermissive;

	public AutomataSynthesizer(Gui gui, Automata theAutomata, SynchronizationOptions synchronizationOptions, SynthesizerOptions synthesizerOptions)
		throws Exception
	{
		Automaton currAutomaton;
		State currInitialState;

		this.theAutomata = theAutomata;
		this.synchronizationOptions = synchronizationOptions;
		this.synthesizerOptions = synthesizerOptions;
		initialState = new int[this.theAutomata.size() + 1];

		// + 1 status field
		nbrOfExecuters = this.synchronizationOptions.getNbrOfExecuters();
		this.gui = gui;
		theVisualProjectContainer = gui.getVisualProjectContainer();
		maximallyPermissive = synthesizerOptions.getMaximallyPermissive();

		SynthesisType synthesisType = synthesizerOptions.getSynthesisType();
		SynthesisAlgorithm synthesisAlgorithm = synthesizerOptions.getSynthesisAlgorithm();

		//-- MF -- Should this be tested here? There should be no possibility selecting invalid combinations!
		if (!AutomataSynthesizer.validOptions(synthesisType, synthesisAlgorithm))
		{
			throw new Exception("Illegal combination of synthesis type and algorithm");
		}

		try
		{
			synchHelper = new AutomataSynchronizerHelper(theAutomata, synchronizationOptions);

			AlphabetAnalyzer alphabetAnalyzer = new AlphabetAnalyzer(theAutomata);

			eventToAutomataMap = alphabetAnalyzer.getUncontrollableEventToPlantMap();

			// Build the initial state
			Iterator autIt = theAutomata.iterator();

			while (autIt.hasNext())
			{
				currAutomaton = (Automaton) autIt.next();
				currInitialState = currAutomaton.getInitialState();
				initialState[currAutomaton.getIndex()] = currInitialState.getIndex();
			}
		}
		catch (Exception e)
		{

			// -- MF -- System.err.println("Error while initializing synchronization helper. " + e);
			logger.error("Error while initializing synchronization helper. " + e);

			throw e;

			// e.printStackTrace();
		}
	}

	public static boolean validOptions(SynthesisType type, SynthesisAlgorithm algorithm)
	{
		if (type == SynthesisType.Unknown)
		{
			return false;
		}

		if (algorithm == SynthesisAlgorithm.Unknown)
		{
			return false;
		}
		else if (algorithm == SynthesisAlgorithm.IDD)
		{
			return false; // Not implemented (yet)
		}
		else if (algorithm == SynthesisAlgorithm.Modular)
		{
			if (type == SynthesisType.Controllable)
			{
				return true; // modular AND controllable ok
			}
			else if(type == SynthesisType.Nonblocking)
			{
				return false; // modular AND nonblocking not ok (at the moment)
			}
			else if(type == SynthesisType.Both)
			{
				return false; // modular AND nonblocking AND controllable not ok (at the moment)
			}
			
			return false;
		}
		else // it can only be monolithic (at the moment?)
		{
			return true;	// and monolithic we can do everything - right?
		}
	}

	// Synthesizes controllable supervisors
	public void execute()
		throws Exception
	{
		/* -- MF -- Old stuff (slightly refactored) */
		if(modularControllability()) // only add if something has been synthesized
		{
			if (synthesizerOptions.getOptimize())
			{
				optimize(theAutomata, new Automata(newAutomata));
			}

			// theVisualProjectContainer.add(newAutomata);
			gui.addAutomata(newAutomata);
		}
		else // nothing was synthesized, the system can be used as is - but what about non-blocking?
		{
			logger.info("No uncontrollabilities found, the specifications can be used as supervisors, as is");
		}
		
		/* -- MF -- new stuff
		
		if(synthesizerOptions.getSynthesisAlgorithm() == SynthesisAlgorithm.Monolithic)
		{
			// monolithic case, just whack the entire stuff into the monolithic algo	
			Automaton automaton = new Automaton();
			doMonolithic(theAutomata, automaton); // we always do something, at least we synch
			// if purge, it's already been done
			if(automaton == null)
				logger.debug("Something fishy here (anAutomaton == null)");
			else
				logger.debug("anAutomaton != null");
			gui.addAutomaton(automaton); // let the user choose the name
			
		}
		else // modular case
		{
			Automata modSupervisors = new Automata(); // collects the calculated supervisors
			Automaton automaton = new Automaton(); // "out" value for doMonolithic
			
			AutomataSelector selector = new AutomataSelector(theAutomata, synthesizerOptions.getMaximallyPermissive());
			for(Automata automata = selector.next(); automata.size() > 0; automata = selector.next())
			{
				if(automata.size() > 1) // no need to do anything for a singleton spec
				{
					if(doMonolithic(automata, automaton))
					{
						modSupervisors.addAutomaton(automaton);
					}
				}
			}
		}*/
	}
	
	//-- MF -- This synthesizes modular controllable supervisors (no non-blocking, that is)
	//-- MF -- Thsi is the original one
	//-- MF -- Returns whether anything has been synthesized
	private boolean modularControllability()
		throws Exception
	{
		LabeledEvent currEvent;
		Automaton theAutomaton;
		Automaton currPlantAutomaton;
		Automaton currSupervisorAutomaton;
		ArrayList selectedAutomata = new ArrayList();
		boolean foundUncontrollable = false;
		
		// Iterator eventIterator;
		Iterator plantIterator;

		// Loop over supervisors/specifications and find plants containing equal uncontrollable events
		Iterator supervisorIterator = theAutomata.iterator();

		while (supervisorIterator.hasNext())
		{
			currSupervisorAutomaton = (Automaton) supervisorIterator.next();
			
			if ((currSupervisorAutomaton.getType() == AutomatonType.Supervisor) || (currSupervisorAutomaton.getType() == AutomatonType.Specification))
			{
				// Examine uncontrollable events in currSupervisorAutomaton and select plants accordingly
				selectedAutomata.add(currSupervisorAutomaton);

				ArrayList eventList = new ArrayList(currSupervisorAutomaton.eventCollection());

				// eventIterator = currSupervisorAutomaton.eventIterator();
				// while (eventIterator.hasNext())
				while (!eventList.isEmpty())
				{

					// currEvent = (Event) eventIterator.next();
					currEvent = (LabeledEvent) eventList.remove(0);

					if (!currEvent.isControllable())
					{
						if (eventToAutomataMap.get(currEvent) != null)
						{
							plantIterator = ((Set) eventToAutomataMap.get(currEvent)).iterator();

							while (plantIterator.hasNext())
							{
								currPlantAutomaton = (Automaton) plantIterator.next();

								// This check is performed in eventToAutomataMap
								// if (currPlantAutomaton.getType() == AutomatonType.Plant)
								if (!selectedAutomata.contains(currPlantAutomaton))
								{
									selectedAutomata.add(currPlantAutomaton);

									// If we want a maximally permissive result, we need to add plants with
									// uncontrollable events common to the already added plants too...
									if (maximallyPermissive)
									{
										eventList.addAll(currPlantAutomaton.eventCollection());
									}
								}
							}
						}
					}
				}
								
				if (selectedAutomata.size() > 1)
				{

					// Clear the hash-table and set some variables in the synchronization helper
					synchHelper.clear();
					synchHelper.addState(initialState);
					synchHelper.getAutomaton().removeAllStates();

					// Essential when building more than one automaton
					// Allocate and initialize the synchronizationExecuters
					ArrayList synchronizationExecuters = new ArrayList(nbrOfExecuters);

					for (int i = 0; i < nbrOfExecuters; i++)
					{
						AutomataSynchronizerExecuter currSynchronizationExecuter = new AutomataSynchronizerExecuter(synchHelper);

						synchronizationExecuters.add(currSynchronizationExecuter);
					}

					// Start all the synchronization executers and wait for completion
					// For the moment we assume that we only have one thread
					for (int i = 0; i < synchronizationExecuters.size(); i++)
					{
						AutomataSynchronizerExecuter currExec = (AutomataSynchronizerExecuter) synchronizationExecuters.get(i);

						currExec.selectAutomata(selectedAutomata);
						currExec.start();
					}

					((AutomataSynchronizerExecuter) synchronizationExecuters.get(0)).join();

					if (!synchHelper.getAutomataIsControllable())
					{
						foundUncontrollable = true;
						
						// Only add supervisors with uncontrollable states
						// Print the names of the automata in selectedAutomata
						Object[] automatonArray = selectedAutomata.toArray();
						String automataNames = ((Automaton) automatonArray[0]).getName();

						for (int i = 1; i < automatonArray.length; i++)
						{
							automataNames = automataNames + " || " + ((Automaton) automatonArray[i]).getName();
						}

						logger.info(automataNames);

						AutomataSynchronizerExecuter currExec = (AutomataSynchronizerExecuter) synchronizationExecuters.get(0);

						currExec.buildAutomaton(false);

						try
						{
							theAutomaton = new Automaton(synchHelper.getAutomaton());

							String newName = theVisualProjectContainer.getActiveProject().getUniqueAutomatonName("Synth_" + ((Automaton) selectedAutomata.get(0)).getName());

							theAutomaton.setName(newName);
							theAutomaton.setType(AutomatonType.Supervisor);
							theAutomaton.setAlphabet(unionAlphabet(selectedAutomata));

							AutomatonSynthesizer synthesizer = new AutomatonSynthesizer(gui, theAutomaton, synthesizerOptions);

							synthesizer.synthesize();
							newAutomata.addAutomaton(theAutomaton);
						}
						catch (Exception ex)
						{

							// -- MF -- logger.error("Exception while adding the new automaton.");
							logger.error("Exception while adding the new automaton.");
						}
					}
				}

				selectedAutomata.clear();
			}
		}
/*
		if (synthesizerOptions.getOptimize())
		{
			optimize(theAutomata, new Automata(newAutomata));
		}

		if(foundUncontrollable) // only add if something has been synthesized
		{
			// theVisualProjectContainer.add(newAutomata);
			gui.addAutomata(newAutomata);
		}
		else // nothing was synthesized, the system can be used as is - but what about non-blocking?
		{
			logger.info("No uncontrollabilities found, the specifications can be used as supervisors, as is");
		}
*/
		return foundUncontrollable;
	}

	//-- MF -- This is the engine, synchronizes the given automata, and calcs the forbidden states
	//-- MF -- Returns true if states have been forbidden
	//-- MF -- anAutomaton is an out-parameter, the synched result
	private boolean doMonolithic(Automata automata, Automaton anAutomaton)
		throws Exception // simply throws everything upwards
	{
		boolean didSomething = false;
		
		AutomataSynchronizer syncher = new AutomataSynchronizer(automata, synchronizationOptions);
		syncher.execute(); // should be able to interrupt this one, just not now...
		anAutomaton = syncher.getAutomaton();
		didSomething |= syncher.getHelper().getAutomataIsControllable();
		
		// We need to synthesize even if the result above is controllable
		// Nonblocking may ruin this 
				
		AutomatonSynthesizer synthesizer = new AutomatonSynthesizer(gui, anAutomaton, synthesizerOptions);
		didSomething |= synthesizer.synthesize(); // should also be able to interrupt this one....
		
		return didSomething;
	}
	
	/**
	 * Returns union alphabet of the automata in selectedAutomata
	 *
	 *@param  selectedAutomata Description of the Parameter
	 *@return  Description of the Return Value
	 *@exception  Exception Description of the Exception
	 */
	public Alphabet unionAlphabet(ArrayList selectedAutomata)
		throws Exception
	{
		Alphabet theAlphabet = new Alphabet();
		EventsSet theAlphabets = new EventsSet();
		Iterator autIt = selectedAutomata.iterator();

		while (autIt.hasNext())
		{
			Automaton currAutomaton = (Automaton) autIt.next();
			Alphabet currAlphabet = currAutomaton.getAlphabet();

			theAlphabets.add(currAlphabet);
		}

		try
		{
			theAlphabet = AlphabetHelpers.getUnionAlphabet(theAlphabets, "a");
		}
		catch (Exception e)
		{

			// System.err.println("Error while generating union alphabet: " + e);
			// -- MF -- logger.error("Error while generating union alphabet: " + e);
			gui.error("Error while generating union alphabet: " + e);

			throw e;
		}

		// Correct the id:s on the events...
		Alphabet unionAlphabet = synchHelper.getAutomaton().getAlphabet();
		Iterator eventIt = theAlphabet.iterator();
		LabeledEvent currEvent;

		while (eventIt.hasNext())
		{
			currEvent = (LabeledEvent) eventIt.next();

			currEvent.setId(unionAlphabet.getEventWithLabel(currEvent.getLabel()).getId());
		}

		theAlphabet.rehash();

		return theAlphabet;
	}

	/**
	 * Removes unnecessary automata, i.e. synthesized supervisors that don't affect the controllability.
	 *
	 *@param  newAutomata the Automata-object containing the new supervisors.
	 *@param  theAutomata Description of the Parameter
	 */
	private void optimize(Automata theAutomata, Automata newAutomata)
	{
		Automata currAutomata = new Automata();
		AutomataFastControllabilityCheck theFastControllabilityCheck;
		SynchronizationOptions syncOptions;

		try
		{
			syncOptions = new SynchronizationOptions();
		}
		catch (Exception ex)
		{
			logger.error("Exception in SynchronizationOptions." + ex);

			return;
		}

		if (!synthesizerOptions.doPurge())
		{

			// The automata aren't purged but they must be for the optimization to work...
			Iterator autIt = newAutomata.iterator();

			while (autIt.hasNext())
			{
				AutomatonPurge automatonPurge = new AutomatonPurge((Automaton) autIt.next());

				automatonPurge.execute();
			}
		}

		currAutomata.addAutomata(theAutomata);
		currAutomata.addAutomata(newAutomata);

		for (int i = newAutomata.size() - 1; i >= 0; i--)
		{
			currAutomata.removeAutomaton(newAutomata.getAutomatonAt(i));

			try
			{
				theFastControllabilityCheck = new AutomataFastControllabilityCheck(currAutomata, syncOptions);

				if (theFastControllabilityCheck.execute())
				{
					this.newAutomata.removeAutomaton(this.newAutomata.getAutomatonAt(i));
				}
				else
				{
					currAutomata.addAutomaton(newAutomata.getAutomatonAt(i));
				}
			}
			catch (Exception ex)
			{
				logger.error("Exception in AutomataSynthesizer.optimize. " + ex);

				return;
			}
		}
	}
}
