
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
import org.supremica.log.*;
import org.supremica.gui.*;
import org.supremica.automata.*;

// This one is used for doMonolithic to return two values
class MonolithicReturnValue
{
	public Automaton automaton;
	public boolean didSomething;
	public Alphabet disabledUncontrollableEvents;    // see AutomatonSynthesizer
}

/**
 * Does synthesis in automata-scale, modularily,
 * uses AutomatonSynthesizer for monolithic problems
 */
public class AutomataSynthesizer
	implements Stoppable
{
	private static Logger logger = LoggerFactory.createLogger(AutomataSynthesizer.class);
	private Automata theAutomata;
	private int nbrOfExecuters;
	private EventToAutomataMap eventToAutomataMap;
	private AutomataSynchronizerHelper synchHelper;
	private ArrayList synchronizationExecuters;
	private SynchronizationOptions synchronizationOptions;
	private SynthesizerOptions synthesizerOptions;

	private ExecutionDialog executionDialog = null;

	// For the stopping
	private boolean stopRequested = false;
	private Stoppable threadToStop = null;

	// For the optimization...
	private boolean maximallyPermissive;

	public AutomataSynthesizer(Automata theAutomata, SynchronizationOptions synchronizationOptions,
							   SynthesizerOptions synthesizerOptions)
		throws Exception, IllegalArgumentException
	{
		// initialization stuff that need no computation
		this.theAutomata = theAutomata;
		this.synchronizationOptions = synchronizationOptions;
		this.synthesizerOptions = synthesizerOptions;
		this.nbrOfExecuters = this.synchronizationOptions.getNbrOfExecuters();
		this.maximallyPermissive = synthesizerOptions.getMaximallyPermissive();

		// Some sanity tests (should already have been tested from ActionMan?)
		if ((synthesizerOptions.getSynthesisAlgorithm() == SynthesisAlgorithm.Modular) &&
			!theAutomata.isAllEventsPrioritized())
		{
			throw new IllegalArgumentException("All events are not prioritized!");
		}

		// evil BDD code inserted here by Arash
		if (synthesizerOptions.getSynthesisAlgorithm() == SynthesisAlgorithm.BDD)
		{
			SynthesisType typ = synthesizerOptions.getSynthesisType();

			if ((typ != SynthesisType.Both) && (typ != SynthesisType.Controllable) &&
				(typ != SynthesisType.Nonblocking))
			{
				throw new IllegalArgumentException("BDD algorithms currently only " +
												   "support supNB+C synthesis.");
			}

			// now, Do BDD Specific initialization here and skip the other stuff
			return;
		}

		// initialization stuff that do need extra computation and thus ignored when
		// doing BDD computation...
		SynthesisType synthesisType = synthesizerOptions.getSynthesisType();
		SynthesisAlgorithm synthesisAlgorithm = synthesizerOptions.getSynthesisAlgorithm();

		// Fix this later
		synthesizerOptions.setRememberDisabledUncontrollableEvents(true);

		try
		{
			synchHelper = new AutomataSynchronizerHelper(theAutomata, synchronizationOptions);

			eventToAutomataMap = AlphabetHelpers.buildUncontrollableEventToPlantsMap(theAutomata);

			/*
			// Build the initial state
			// Shouldn't this be done in Automata.java? Is initialState used at all here? Nope? /hugo
			Iterator autIt = theAutomata.iterator();
			while (autIt.hasNext())
			{
					currAutomaton = (Automaton) autIt.next();
					currInitialState = currAutomaton.getInitialState();
					initialState[currAutomaton.getIndex()] = currInitialState.getIndex();
			}
			*/
		}
		catch (Exception e)
		{
			logger.error("Error while initializing synchronization helper. " + e);
			logger.debug(e.getStackTrace());

			throw e;
		}
	}

	// Synthesizes supervisors
	public Automata execute()
		throws Exception
	{
		Automata result = new Automata();

		if (synthesizerOptions.getSynthesisAlgorithm() == SynthesisAlgorithm.MonolithicSingleFixpoint)
		{
			MonolithicReturnValue retval = doMonolithic(theAutomata, true);

			if (stopRequested)
			{
				return new Automata();
			}

			result.addAutomaton(retval.automaton);    // let the user choose the name later
		}
		else if (synthesizerOptions.getSynthesisAlgorithm() == SynthesisAlgorithm.Monolithic)
		{
			// Monolithic synthesis, just whack the entire stuff into the monolithic algo
			MonolithicReturnValue retval = doMonolithic(theAutomata, false);

			if (stopRequested)
			{
				return new Automata();
			}

			result.addAutomaton(retval.automaton);    // let the user choose the name later
		}
		else if (synthesizerOptions.getSynthesisAlgorithm() == SynthesisAlgorithm.Modular)
		{
			// Modular synthesis
			Automata newSupervisors = doModular(theAutomata);

			if (stopRequested)
			{
				return new Automata();
			}

			result.addAutomata(newSupervisors);    // let the user choose the name later
		}
		else if (synthesizerOptions.getSynthesisAlgorithm() == SynthesisAlgorithm.BDD)
		{
			// BDD synthesis
			SynthesisType typ = synthesizerOptions.getSynthesisType();
			boolean do_c = (typ == SynthesisType.Both) | (typ == SynthesisType.Controllable);
			boolean do_nb = (typ == SynthesisType.Both) | (typ == SynthesisType.Nonblocking);

			AutomataBDDSynthesizer bddSynthesizer = new AutomataBDDSynthesizer(theAutomata, do_nb, do_c);

			bddSynthesizer.execute();
			bddSynthesizer.cleanup();
		}
		else
		{
			logger.error("Unknown synthesis algorithm");
		}

		return result;
	}

	/**
	 * Removes from disabledUncontrollableEvents those events that are "insignificant"
	 * Returns the result, which is an altered disabledUncontrollableEvents
	 */
	private Alphabet checkMaximallyPermissive(Automata automata, Alphabet disabledUncontrollableEvents)
	{
		if (disabledUncontrollableEvents != null)
		{
			for (Iterator autIt = automata.iterator(); autIt.hasNext(); )
			{
				// disregard the uc-events of this spec/supervisor
				Automaton currAutomaton = (Automaton) autIt.next();

				if (currAutomaton.isSupervisor() || currAutomaton.isSpecification())
				{
					Alphabet currAlphabet = currAutomaton.getAlphabet();

					disabledUncontrollableEvents.minus(currAlphabet);
				}
			}

			// Remove those disabled events that are not included in another plant
			LinkedList eventsToBeRemoved = new LinkedList();

			for (Iterator evIt = disabledUncontrollableEvents.iterator(); evIt.hasNext(); )
			{
				LabeledEvent currEvent = (LabeledEvent) evIt.next();
				//Set currAutomata = (Set) eventToAutomataMap.get(currEvent);
				Automata currAutomata = eventToAutomataMap.get(currEvent);
				boolean removeEvent = true;

				// currAutomata contains those plant automata that contain this event.
				for (Iterator autIt = currAutomata.iterator();
						autIt.hasNext(); )
				{
					Automaton currAutomaton = (Automaton) autIt.next();

					if (currAutomaton.isPlant())
					{

						// Check if there is a plant not included in this
						// modular supervisor. If no such plant exists then remove this
						// event from the set of disabled events.
						if (!automata.containsAutomaton(currAutomaton.getName()))
						{
							removeEvent = false;
						}
					}
				}

				if (removeEvent)
				{
					eventsToBeRemoved.add(currEvent);
				}
			}

			for (Iterator evIt = eventsToBeRemoved.iterator(); evIt.hasNext(); )
			{
				LabeledEvent currEvent = (LabeledEvent) evIt.next();

				disabledUncontrollableEvents.removeEvent(currEvent);
			}
		}

		return disabledUncontrollableEvents;
	}

	/**
	 * Does modular synthesis...
	 */
	private Automata doModular(Automata aut)
		throws Exception
	{
		// Automata that collects the calculated supervisors
		Automata modSupervisors = new Automata();

		// Selector - always start with non-max perm
		AutomataSelector selector = new AutomataSelector(aut);

		// Initialize execution dialog
		java.awt.EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				if (executionDialog != null)
				{
					int nbrOfSpecAndSup = theAutomata.getSpecificationAndSupervisorAutomata().size();
					executionDialog.initProgressBar(0, nbrOfSpecAndSup);
				}
			}
		});

		// Loop over specs/sups AND their corresponding plants (dealt with by the selector)
		for (Automata automata = selector.next(); automata.size() > 0; automata = selector.next())
		{
			if (stopRequested)
			{
				return new Automata();
			}

			// In the non incremental approach, immediately add all plants that are related
			// by uncontrollable events. Otherwise this is done incrementally below
			if (synthesizerOptions.getMaximallyPermissive() &&
				!synthesizerOptions.getMaximallyPermissiveIncremental())
			{
				// Loop until no new uncontrollable events are found
				Alphabet uncontrollableEvents = automata.getUnionAlphabet().getUncontrollableAlphabet();
				int previousSize = 0;
				while (uncontrollableEvents.size() > previousSize)
				{
					automata = selector.addPlants(uncontrollableEvents);
					previousSize = uncontrollableEvents.size();
					uncontrollableEvents = automata.getUnionAlphabet().getUncontrollableAlphabet();
				}
			}

			// Do monolithic synthesis on this subsystem
			MonolithicReturnValue retval = doMonolithic(automata);

			if (stopRequested)
			{
				return new Automata();
			}

			// Did anything happen?
			if (retval.didSomething)
			{
				Alphabet disabledUncontrollableEvents = checkMaximallyPermissive(automata, retval.disabledUncontrollableEvents);

				// Do we care about max perm?
				if (synthesizerOptions.getMaximallyPermissive())
				{
					// As long as uncontrollable events had to be "disabled" by the supervisor that
					// instead could have been disabled by a plant (right?) repeat the below...
					// In this maner, we're *guaranteed* max perm
					while (disabledUncontrollableEvents.size() > 0)
					{
						// Note that in the nonincremental approach, this will add no new plants
						// since they are already added!
						automata = selector.addPlants(disabledUncontrollableEvents);

						// Do monolithic synthesis on this subsystem
						retval = doMonolithic(automata);

						if (stopRequested)
						{
							return new Automata();
						}

						disabledUncontrollableEvents = checkMaximallyPermissive(automata, retval.disabledUncontrollableEvents);
					}
				}
				else
				{
					// We do not care about max perm, but could at least notify
					if (disabledUncontrollableEvents.size() > 0)
					{
						// Not guranteed to be max perm
						logger.info("The synthesized supervisor '" + retval.automaton.getComment() +
									"' might not be maximally permissive since:");

						for (Iterator evIt = disabledUncontrollableEvents.iterator(); evIt.hasNext(); )
						{
							LabeledEvent currEvent = (LabeledEvent) evIt.next();

							logger.info(currEvent + " is included in the plant but not " +
										"in the supervisor.");
						}
					}
					else
					{
						// It's max perm in any case
						logger.info("The synthesized supervisor '" + retval.automaton.getComment() +
									"' is maximally permissive.");
					}
				}

				modSupervisors.addAutomaton(retval.automaton);
			}

			// Update execution dialog
			if (executionDialog != null)
			{
				executionDialog.setProgress(selector.getProgress());
			}
		}

		// If no spec/sup is in the selected automata, only nonblocking requires work
		// if we've not seen any spec, do monolithic synthesis on the entire set
		if (selector.hadSpec() == false)
		{
			logger.debug("No spec/sup seen, doing monolithic synthesis on the entire set.");

			MonolithicReturnValue retval = doMonolithic(aut);

			if (stopRequested)
			{
				return new Automata();
			}

			if (retval.didSomething)
			{
				modSupervisors.addAutomaton(retval.automaton);
			}
		}

		// Should we optimize the result (throw unnecessary supervisors away)
		if (synthesizerOptions.getOptimize())
		{
			if (executionDialog != null)
			{
				executionDialog.setMode(ExecutionDialogMode.synthesizingOptimizing);
				executionDialog.initProgressBar(0, modSupervisors.size());
			}

			optimize(aut, modSupervisors);
		}

		// Did we do anything at all?
		if (modSupervisors.size() == 0)
		{
			logger.info("No problems found, the current specifications and supervisors " +
						"can be used to supervise the system.");
		}

		// Nonblocking synthesis is not implemented...
		if ((synthesizerOptions.getSynthesisType() == SynthesisType.Nonblocking) || (synthesizerOptions.getSynthesisType() == SynthesisType.Both))
		{
			logger.warn("Currently global nonblocking is NOT guaranteed. The only guarantee " +
						"is that each supervisor is individually nonblocking with respect to the " +
						"plants it controls");
		}

		// Return the new supervisors
		return modSupervisors;
	}

	// se the real implementation below
	private MonolithicReturnValue doMonolithic(Automata automata)
		throws Exception
	{
		return doMonolithic(automata, false);    // <-- NOT single fixpoint as default for now (under development)
	}

	// This is the engine, synchronizes the given automata, and calcs the forbidden states
	private MonolithicReturnValue doMonolithic(Automata automata, boolean singleFixpoint)
		throws Exception    // simply throws everything upwards
	{
		logger.debug("AutomataSynthesizer::doMonolithic");

		MonolithicReturnValue retval = new MonolithicReturnValue();

		retval.didSomething = false;

		// If we synthesize a nonblocking, controllable and observable
		// supervisor a supremal supervisor is only guaranteed
		// to exist if all controllable events are observable.
		// If this is not the case then we can treat all
		// unobservable events as uncontrollable. This will
		// guarantee the existence of a supremal supervisor.
		// However this will not necessarily be the maximally
		// permissive supervisor. See Introduction to Discrete Event
		// Systems, Cassandras, Lafortune for a discussion about this
		// problem.
		if (synthesizerOptions.getSynthesisType() == SynthesisType.Observable)
		{
			Alphabet unionAlphabet = AlphabetHelpers.getUnionAlphabet(automata);
			Alphabet problemEvents = new Alphabet();

			for (Iterator<LabeledEvent> evIt = unionAlphabet.iterator();
					evIt.hasNext(); )
			{
				LabeledEvent currEvent = evIt.next();

				if (currEvent.isControllable() &&!currEvent.isObservable())
				{
					problemEvents.addEvent(currEvent);
				}
			}

			if (problemEvents.size() > 0)
			{
				// Make copy since we will change controllability
				Automata newAutomata = new Automata(automata);

				// Iterate over all the automata and change
				// controllability of the problem events
				for (Iterator autIt = newAutomata.iterator(); autIt.hasNext(); )
				{
					Automaton currAutomaton = (Automaton) autIt.next();
					Alphabet currAlphabet = currAutomaton.getAlphabet();

					// Iterator over the problem events
					for (Iterator<LabeledEvent> evIt = problemEvents.iterator();
							evIt.hasNext(); )
					{
						LabeledEvent currEvent = evIt.next();

						if (currAlphabet.contains(currEvent.getLabel()))
						{
							LabeledEvent currAutomatonEvent = currAlphabet.getEvent(currEvent.getLabel());

							currAutomatonEvent.setControllable(false);
						}
					}
				}

				StringBuffer sb = new StringBuffer();
				for (Iterator<LabeledEvent> evIt = problemEvents.iterator(); evIt.hasNext(); )
				{
					LabeledEvent currEvent = evIt.next();
					sb.append(currEvent + " ");
				}

				logger.warn(sb.toString() + "are controllable but not observable. This imply that a supremal " +
							"supervisor may not exist. To guarantee existence of such a supervisor the events " +
							"will be treated us uncontrollable from the supervisors point of view. However the " +
							"supervisor does not have to be maximally permissive.");

				automata = newAutomata;
			}
		}

		// Remember old setting
		boolean orgRememberDisabledEvents = synchronizationOptions.rememberDisabledEvents();

		// We must keep track of all events that we have disabled
		// This is used when checking for observability
		if (synthesizerOptions.getSynthesisType() == SynthesisType.Observable)
		{
			synchronizationOptions.setRememberDisabledEvents(true);
		}

		AutomataSynchronizer syncher = new AutomataSynchronizer(automata, synchronizationOptions);
		threadToStop = syncher;
		syncher.execute();
		threadToStop = null;

		if (stopRequested)
		{
			return null;
		}

		retval.automaton = syncher.getAutomaton();
		retval.didSomething |= !syncher.getHelper().getAutomataIsControllable();

		if (synthesizerOptions.getSynthesisType() == SynthesisType.Observable)
		{
			// Reset the synchronization type
			synchronizationOptions.setRememberDisabledEvents(orgRememberDisabledEvents);
		}

		// We need to synthesize even if the result above is controllable
		// Nonblocking may ruin controllability
		// ARASH: choose between triple and the single fixpoint algorithms:
		AutomatonSynthesizer synthesizer = singleFixpoint
										   ? new AutomatonSynthesizerSingleFixpoint(retval.automaton, synthesizerOptions)
										   : new AutomatonSynthesizer(retval.automaton, synthesizerOptions);
		threadToStop = synthesizer;
		retval.didSomething |= synthesizer.synthesize();
		threadToStop = null;

		if (stopRequested)
		{
			return null;
		}

		retval.disabledUncontrollableEvents = synthesizer.getDisabledUncontrollableEvents();
		retval.automaton = synthesizer.getAutomaton();

		// Set an apropriate name... (the name should be null afterwards)
		//retval.automaton.setComment("sup(" + retval.automaton.getName() + ")");
		//retval.automaton.setName(null);

		// Shall we reduce the supervisor?
		if (synthesizerOptions.getReduceSupervisors())
		{
			if (executionDialog != null)
			{
				executionDialog.setMode(ExecutionDialogMode.synthesizingReducing);
			}

			// Supervisor reduction only works if the supervisor is purged
			assert(synthesizerOptions.doPurge());

			// Add the reduced supervisor
			Automaton supervisor = retval.automaton;
			Automaton reducedSupervisor = AutomatonSplit.reduceAutomaton(supervisor, automata);

			retval.automaton = reducedSupervisor;

			if (executionDialog != null)
			{
				executionDialog.setMode(ExecutionDialogMode.synthesizing);
			}
		}

		// Return the result
		return retval;
	}

	/**
	 * Removes unnecessary automata, i.e. synthesized supervisors that don't affect the controllability.
	 * Note: At the moment, only controllability is checked, no non-blocking.
	 *
	 * After this method has completed, the unnecessary supervisors have been removed from
	 * candidateSupervisors.
	 *
	 * @param  theAutomata contains the originally given specs/sups and plants
	 * @param  candidateSupervisors the Automata-object containing the new supervisors, is altered!
	 */
	private void optimize(Automata theAutomata, Automata candidateSupervisors)
		throws Exception
	{
		logger.debug("AutomataSynthesizer.optimize");

		// Deep copy the new automata, so we can purge without affecting the originals
		Automata newSupervisors = new Automata(candidateSupervisors);

		// Make sure the automata are purged - they must be for the optimization to work...
		if (!synthesizerOptions.doPurge())
		{
			// We have not purged earlier - do that now!
			Iterator autIt = newSupervisors.iterator();
			while (autIt.hasNext())
			{
				AutomatonPurge automatonPurge = new AutomatonPurge((Automaton) autIt.next());
				automatonPurge.execute();
			}
		}

		Automata currAutomata = new Automata();
		currAutomata.addAutomata(theAutomata);
		currAutomata.addAutomata(newSupervisors);

		// Remove one of the candidate supervisors in newSupervisors at a time and see
		// if the behaviour of the rest of the system is included in that supervisor
		// (i.e. the system already behaves like that without the supervisor).

		// Remove the new automata one by one and examine if it had impact on the result.
		int progress = 0;
		for (int i = newSupervisors.size() - 1; i >= 0; i--)
		{
			Automaton currSupervisor = newSupervisors.getAutomatonAt(i);
			currAutomata.removeAutomaton(currSupervisor);
			progress++;

			// Prepare a verifier for verifying the need for this supervisor
			/*
			SynchronizationOptions synchronizationOptions;
			VerificationOptions verificationOptions;
			synchronizationOptions = SynchronizationOptions.getDefaultVerificationOptions();
			verificationOptions = VerificationOptions.getDefaultLanguageInclusionOptions();
			verificationOptions.setInclusionAutomata(currAutomata);
			verificationOptions.setOneEventAtATime(true);
			//verificationOptions.setNbrOfAttempts(2);
			verificationOptions.setSkipUncontrollabilityCheck(true);
			// currAutomata.setIndicies();
			Automata automata = new Automata(currAutomata, true);
			automata.addAutomaton(currSupervisor);
			*/
			VerificationOptions verificationOptions;
			SynchronizationOptions synchronizationOptions;
			verificationOptions = VerificationOptions.getDefaultControllabilityOptions();
			synchronizationOptions = SynchronizationOptions.getDefaultVerificationOptions();
			AutomataVerifier verifier = new AutomataVerifier(currAutomata, verificationOptions,
															 synchronizationOptions, null);

			if (stopRequested)
			{
				return;
			}

			// Will the supervisor affect the system at all?
			logger.verbose("Examining whether the supervisor candidate " +
						   currSupervisor + " is needed.");
			threadToStop = verifier;
			// if (AutomataVerifier.verifyModularInclusion(currAutomata, new Automata(currSupervisor)))
			if (verifier.verify())
			{
				// Nope, this one didn't matter! Remove it!
				// currAutomata.removeAutomaton(currSupervisor); // Use for LanguageInclusion
				candidateSupervisors.removeAutomaton(candidateSupervisors.getAutomatonAt(i));
			}
			else
			{
				// This one was important! Don't remove it and put it back!!
				currAutomata.addAutomaton(currSupervisor); // Not for LanguageInclusion
			}
			threadToStop = null;

			if (executionDialog != null)
			{
				executionDialog.setProgress(progress);
			}
		}

		/*
		AutomataVerifier theVerifier;
		VerificationOptions theVerificationOptions = new VerificationOptions();

		// Get the selected synchronizationOptions
		SynchronizationOptions syncOptions;
		try
		{
			syncOptions = new SynchronizationOptions();
		}
		catch (Exception ex)
		{
			logger.error("Exception in SynchronizationOptions." + ex);
			logger.debug(ex.getStackTrace());

			return;
		}

		// Is this the only necessary option?
		theVerificationOptions.setVerificationType(VerificationType.Controllability);

		// Remove the new automata one by one and examine if it had impact on the result.
		for (int i = newSupervisors.size() - 1; i >= 0; i--)
		{
			currAutomata.removeAutomaton(newSupervisors.getAutomatonAt(i));

			try
			{
                // Verify controllability with one automaton removed
				theVerifier = new AutomataVerifier(currAutomata, syncOptions, theVerificationOptions);

				if (theVerifier.verify())
				{    // This supervisor had no impact, throw it away!
					candidateSupervisors.removeAutomaton(candidateSupervisors.getAutomatonAt(i));
				}
				else
				{    // It had impact, put it back!
					currAutomata.addAutomaton(newSupervisors.getAutomatonAt(i));
				}
			}
			catch (IllegalArgumentException ex)
			{
				logger.error("AutomataSynthesizer.optimize: Illegal argument " + ex);
				logger.debug(ex.getStackTrace());

				return;
			}
			catch (Exception ex)
			{
				logger.error("Exception in AutomataSynthesizer.optimize. " + ex);
				logger.debug(ex.getStackTrace());

				return;
			}
		}
		*/
	}

	public void setExecutionDialog(ExecutionDialog dialog)
	{
		executionDialog = dialog;
	}

	/**
	 * Method that stops the synthesizer as soon as possible.
	 *
	 * @see  ExecutionDialog
	 */
	public void requestStop()
	{
		stopRequested = true;

		logger.debug("AutomataSynthesizer requested to stop.");

		// Stop currently executing thread!
		if (threadToStop != null)
		{
			threadToStop.requestStop();
		}
	}
}
