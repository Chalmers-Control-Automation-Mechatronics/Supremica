
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

// import org.supremica.log.*;
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

public class AutomataSynthesizer
{

	// private static Logger logger = LoggerFactory.createLogger(AutomataSynthesizer.class);
	private Automata theAutomata;
	private int nbrOfExecuters;
	private HashMap eventToAutomataMap = new HashMap();
	private AutomataSynchronizerHelper synchHelper;
	private ArrayList synchronizationExecuters;
	private SynchronizationOptions synchronizationOptions;
	private SynthesizerOptions synthesizerOptions;
	private int[] initialState;
	private VisualProjectContainer theVisualProjectContainer;
	private Gui workbench;

	// For the optimization...
	private Automata newAutomata = new Automata();
	private boolean maximallyPermissive;

	public AutomataSynthesizer(Gui workbench, Automata theAutomata, SynchronizationOptions synchronizationOptions, SynthesizerOptions synthesizerOptions)
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
		this.workbench = workbench;
		theVisualProjectContainer = workbench.getVisualProjectContainer();
		maximallyPermissive = synthesizerOptions.getMaximallyPermissive();

		SynthesisType synthesisType = synthesizerOptions.getSynthesisType();
		SynthesisAlgorithm synthesisAlgorithm = synthesizerOptions.getSynthesisAlgorithm();

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
			workbench.error("Error while initializing synchronization helper. " + e);

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

		if (algorithm == SynthesisAlgorithm.IDD)
		{
			return false;
		}

		if (algorithm == SynthesisAlgorithm.Modular)
		{
			if (type == SynthesisType.Controllable)
			{
				return true;
			}

			return false;
		}

		return true;
	}

	// Synthesizes controllable supervisors
	public void execute()
		throws Exception
	{
		LabeledEvent currEvent;
		Automaton theAutomaton;
		Automaton currPlantAutomaton;
		Automaton currSupervisorAutomaton;
		ArrayList selectedAutomata = new ArrayList();

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

						// Only add supervisors with uncontrollable states
						// Print the names of the automata in selectedAutomata
						Object[] automatonArray = selectedAutomata.toArray();
						String automataNames = ((Automaton) automatonArray[0]).getName();

						for (int i = 1; i < automatonArray.length; i++)
						{
							automataNames = automataNames + " || " + ((Automaton) automatonArray[i]).getName();
						}

						// -- MF -- logger.info(automataNames);
						workbench.info(automataNames);

						AutomataSynchronizerExecuter currExec = (AutomataSynchronizerExecuter) synchronizationExecuters.get(0);

						currExec.buildAutomaton(false);

						try
						{
							theAutomaton = new Automaton(synchHelper.getAutomaton());

							String newName = theVisualProjectContainer.getActiveProject().getUniqueAutomatonName("Synth_" + ((Automaton) selectedAutomata.get(0)).getName());

							theAutomaton.setName(newName);
							theAutomaton.setType(AutomatonType.Supervisor);
							theAutomaton.setAlphabet(unionAlphabet(selectedAutomata));

							AutomatonSynthesizer synthesizer = new AutomatonSynthesizer(workbench, theAutomaton, synthesizerOptions);

							synthesizer.synthesize();
							newAutomata.addAutomaton(theAutomaton);
						}
						catch (Exception ex)
						{

							// -- MF -- logger.error("Exception while adding the new automaton.");
							workbench.error("Exception while adding the new automaton.");
						}
					}
				}

				selectedAutomata.clear();
			}
		}

		if (synthesizerOptions.getOptimize())
		{
			optimize(theAutomata, new Automata(newAutomata));
		}

		// theVisualProjectContainer.add(newAutomata);
		workbench.addAutomata(newAutomata);
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
			workbench.error("Error while generating union alphabet: " + e);

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

			// -- MF -- logger.error("Exception in SynchronizationOptions." + ex);
			workbench.error("Exception in SynchronizationOptions." + ex);

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

				// -- MF -- logger.error("Exception in AutomataSynthesizer.optimize. " + ex);
				workbench.error("Exception in AutomataSynthesizer.optimize. " + ex);

				return;
			}
		}
	}
}
