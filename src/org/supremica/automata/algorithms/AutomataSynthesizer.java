
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
import org.supremica.util.ActionTimer;
import org.supremica.log.*;
import org.supremica.gui.*;
import org.supremica.automata.*;

// This one is used for doMonolithic to return two values
class MonolithicReturnValue
{
	public Automaton automaton;
	public boolean didSomething;
	public Alphabet disabledEvents;    // see AutomatonSynthesizer
}

/**
 * Does synthesis in automata-scale, modularily,
 * uses AutomatonSynthesizer for monolithic problems
 */
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
	private VisualProjectContainer theVisualProjectContainer;
	private ActionTimer theTimer = new ActionTimer();
	private Gui gui;

	// For the optimization...
	private Automata newAutomata = new Automata();
	private boolean maximallyPermissive;

	public AutomataSynthesizer(Gui gui, Automata theAutomata, SynchronizationOptions synchronizationOptions, SynthesizerOptions synthesizerOptions)
		throws Exception, IllegalArgumentException
	{

		// initialization stuff that need no computation
		this.theAutomata = theAutomata;
		this.synchronizationOptions = synchronizationOptions;
		this.synthesizerOptions = synthesizerOptions;
		this.gui = gui;
		this.nbrOfExecuters = this.synchronizationOptions.getNbrOfExecuters();
		this.theVisualProjectContainer = gui.getVisualProjectContainer();
		this.maximallyPermissive = synthesizerOptions.getMaximallyPermissive();

		if (!theAutomata.isEventControllabilityConsistent())
		{
			throw new IllegalArgumentException("The automata are not consistent in " + "the controllability of some event.");
		}

		if ((synthesizerOptions.getSynthesisAlgorithm() == SynthesisAlgorithm.Modular) &&!theAutomata.isAllEventsPrioritized())
		{
			throw new IllegalArgumentException("All events are not prioritized!");
		}

		// evil BDD code inserted here by Arash
		if (synthesizerOptions.getSynthesisAlgorithm() == SynthesisAlgorithm.BDD)
		{
			SynthesisType typ = synthesizerOptions.getSynthesisType();

			if ((typ != SynthesisType.Both) && (typ != SynthesisType.Controllable) && (typ != SynthesisType.Nonblocking))
			{
				throw new IllegalArgumentException("BDD algorithms currently only " + "support supNB+C synthesis.");
			}

			// now, Do BDD Specific initialization here and skip the other stuff
			return;
		}

		// initialization stuff that do need extra computation and thus ignored when
		// doing BDD computation...
		SynthesisType synthesisType = synthesizerOptions.getSynthesisType();
		SynthesisAlgorithm synthesisAlgorithm = synthesizerOptions.getSynthesisAlgorithm();

		// Fix this later
		synthesizerOptions.setRememberDisabledEvents(true);

		//-- MF -- Should this be tested here? There should be no possibility selecting invalid combinations!
		if (!AutomataSynthesizer.validOptions(synthesisType, synthesisAlgorithm))
		{
			throw new IllegalArgumentException("Illegal combination of synthesis type and algorithm");
		}

		try
		{
			synchHelper = new AutomataSynchronizerHelper(theAutomata, synchronizationOptions);

			AlphabetAnalyzer alphabetAnalyzer = new AlphabetAnalyzer(theAutomata);

			eventToAutomataMap = alphabetAnalyzer.getUncontrollableEventToPlantMap();

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
			return false;    // Not implemented
		}
		else if (algorithm == SynthesisAlgorithm.BDD)
		{
			return true;
		}
		else if (algorithm == SynthesisAlgorithm.MonolithicSingleFixpoint)
		{
			return true;
		}
		else if (algorithm == SynthesisAlgorithm.Modular)

		// Was this really correct ? :
		// else if (algorithm == SynthesisAlgorithm.Monolithic)
		{

			// same as monolithic. in fact, anything but the NBC uses the monolithic code :)
			return true;
		}
		else if (algorithm == SynthesisAlgorithm.Monolithic)
		{
			return true;    // and monolithic we can do everything
		}
		else
		{
			return false;
		}
	}

	/**
	 * Return the time required to run this algorithm. It is only valid to
	 * call this method after the return of the execute method.
	 */
	public long elapsedTime()
	{
		return theTimer.elapsedTime();
	}

	// instead return the timer and let it do the time-to-string formatting
	public ActionTimer getTimer()
	{
		return theTimer;
	}

	// Synthesizes supervisors
	public void execute()
		throws Exception
	{
		if (synthesizerOptions.getSynthesisAlgorithm() == SynthesisAlgorithm.MonolithicSingleFixpoint)
		{
			theTimer.start();

			MonolithicReturnValue retval = doMonolithic(theAutomata, true);

			theTimer.stop();
			gui.addAutomaton(retval.automaton);    // let the user choose the name later
		}
		else if (synthesizerOptions.getSynthesisAlgorithm() == SynthesisAlgorithm.Monolithic)
		{
			theTimer.start();

			// monolithic case, just whack the entire stuff into the monolithic algo
			MonolithicReturnValue retval = doMonolithic(theAutomata, false);

			theTimer.stop();

			// retval.automaton.setComment("sup(" + retval.automaton.getComment() + ")");
			gui.addAutomaton(retval.automaton);    // let the user choose the name later
		}
		else if (synthesizerOptions.getSynthesisAlgorithm() == SynthesisAlgorithm.Modular)    // modular case
		{
			theTimer.start();

			Automata newSupervisors = doModular(theAutomata);

			theTimer.stop();
			gui.addAutomata(newSupervisors);    // let the user choose the name later
		}
		else if (synthesizerOptions.getSynthesisAlgorithm() == SynthesisAlgorithm.BDD)    // more BDD Stuff
		{
			SynthesisType typ = synthesizerOptions.getSynthesisType();
			boolean do_c = (typ == SynthesisType.Both) | (typ == SynthesisType.Controllable);
			boolean do_nb = (typ == SynthesisType.Both) | (typ == SynthesisType.Nonblocking);

			theTimer.start();

			AutomataBDDSynthesizer bddSynthesizer = new AutomataBDDSynthesizer(theAutomata, do_nb, do_c);

			bddSynthesizer.execute();
			bddSynthesizer.cleanup();
			theTimer.stop();
		}
		else
		{
			logger.error("Unknown synthesis algorithm");
		}
	}

	// Removes from disabledEvents those events that are "insignificant"
	// Returns the result, which is an altered disabledEvents
	private Alphabet checkMaximallyPermissive(Automata automata, Alphabet disabledEvents)
	{
		if (disabledEvents != null)
		{
			for (Iterator autIt = automata.iterator(); autIt.hasNext(); )
			{

				// disregard the uc-events of this spec/supervisor
				Automaton currAutomaton = (Automaton) autIt.next();

				if (currAutomaton.isSupervisor() || currAutomaton.isSpecification())
				{
					Alphabet currAlphabet = currAutomaton.getAlphabet();

					disabledEvents.minus(currAlphabet);
				}
			}

			// Remove those disabled events that are not included in another plant
			LinkedList eventsToBeRemoved = new LinkedList();

			for (Iterator evIt = disabledEvents.iterator(); evIt.hasNext(); )
			{
				LabeledEvent currEvent = (LabeledEvent) evIt.next();
				Set currAutomata = (Set) eventToAutomataMap.get(currEvent);
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

				disabledEvents.removeEvent(currEvent);
			}
		}

		return disabledEvents;
	}

	/**
	 * Does modular synthesis...
	 */
	private Automata doModular(Automata theAutomata)
		throws Exception
	{
		Automata modSupervisors = new Automata();    // collects the calculated supervisors
		AutomataSelector selector = new AutomataSelector(theAutomata);    // always start with non-max perm

		// Loop over specs/sups AND their corresponding plants (dealt with by the selector)
		for (Automata automata = selector.next(); automata.size() > 0;
				automata = selector.next())
		{

			// In the non incremental approach, immediately add all plants that are related
			// by uncontrollable events. Otherwise this is done incrementally below
			if (synthesizerOptions.getMaximallyPermissive() &&!synthesizerOptions.getMaximallyPermissiveIncremental())
			{
				int previousSize = 0;
				Alphabet uncontrollableEvents = automata.getUnionAlphabet().getUncontrollableAlphabet();

				// Loop until no new uncontrollable events are found
				while (uncontrollableEvents.size() > previousSize)
				{
					automata = selector.addPlants(uncontrollableEvents);
					previousSize = uncontrollableEvents.size();
					uncontrollableEvents = automata.getUnionAlphabet().getUncontrollableAlphabet();
				}

				logger.info("Alpha: " + uncontrollableEvents + " Aut: " + automata);
			}

			// Do monolithic synthesis on this subsystem
			MonolithicReturnValue retval = doMonolithic(automata);

			if (retval.didSomething)
			{

				// retval.automaton.setComment("sup(" + retval.automaton.getComment() + ")");
				Alphabet disabledEvents = checkMaximallyPermissive(automata, retval.disabledEvents);

				// Do we care about max perm?
				if (synthesizerOptions.getMaximallyPermissive())
				{
					while (disabledEvents.size() > 0)    // ...then do so until we're known to be maximally permissive...
					{

						// Note that in the nonincremental approach, this will add no new plants!
						automata = selector.addPlants(disabledEvents);
						retval = doMonolithic(automata);    // now we're *guaranteed* max perm

						//retval.automaton.setComment("sup(" + retval.automaton.getComment() + ")");
						disabledEvents = checkMaximallyPermissive(automata, retval.disabledEvents);
					}
				}
				else    // we should not care about max perm, but should notify
				{
					if (disabledEvents.size() > 0)    // not guranteed to be max perm
					{
						logger.info("The synthesized supervisor '" + retval.automaton.getComment() + "' might not be maximally permissive since:");

						for (Iterator evIt = disabledEvents.iterator();
								evIt.hasNext(); )
						{
							LabeledEvent currEvent = (LabeledEvent) evIt.next();

							logger.info(currEvent + " is included in the plant but not " + "in the supervisor.");
						}
					}
					else    // it's max perm in any case
					{
						logger.info("The synthesized supervisor '" + retval.automaton.getComment() + "' is maximally permissive.");
					}
				}

				/*
				// Should we reduce the supervisor?
				if (synthesizerOptions.getReduceSupervisors())
				{       // Add the reduced supervisor
						Automaton supervisor = retval.automaton;
						Automaton reducedSupervisor = AutomatonSplit.reduceAutomaton(supervisor, automata);
						modSupervisors.addAutomaton(reducedSupervisor);
				}
				else
				{       // Add the supervisor as is
						modSupervisors.addAutomaton(retval.automaton);
				}
				*/
				modSupervisors.addAutomaton(retval.automaton);
			}
		}

		// If no spec/sup is in the selected automata, only nonblocking requires work
		if (selector.hadSpec() == false)    // if we've not seen any spec, do monolithic synthesis on the entire set
		{
			logger.debug("No spec/sup seen, doing monolithic synthesis on the entire set.");

			MonolithicReturnValue retval = doMonolithic(theAutomata);

			if (retval.didSomething)
			{
				modSupervisors.addAutomaton(retval.automaton);
			}
		}

		// Should we optimize the result (throw unnecessary supervisors away)
		if (synthesizerOptions.getOptimize())
		{
			optimize(theAutomata, modSupervisors);
		}

		// Did we do anything at all?
		if (modSupervisors.size() == 0)
		{
			logger.info("No problems found, the current specifications and supervisors " + "can be used to supervise the system.");
		}

		// Nonblocking synthesis is not implemented...
		if ((synthesizerOptions.getSynthesisType() == SynthesisType.Nonblocking) || (synthesizerOptions.getSynthesisType() == SynthesisType.Both))
		{
			logger.info("NOTE! Currently global nonblocking is NOT guaranteed. The only guarantee " + "is that each supervisor is individually nonblocking with respect to the " + "plants it controls");
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

			for (EventIterator evIt = unionAlphabet.iterator();
					evIt.hasNext(); )
			{
				LabeledEvent currEvent = evIt.nextEvent();

				if (currEvent.isControllable() &&!currEvent.isObservable())
				{
					problemEvents.addEvent(currEvent);
				}
			}

			if (problemEvents.size() > 0)
			{
				Automata newAutomata = new Automata(automata);    // Make copy since we will change controllability

				// Iterate over all the automata and change
				// controllability of the problem events
				for (Iterator autIt = newAutomata.iterator(); autIt.hasNext(); )
				{
					Automaton currAutomaton = (Automaton) autIt.next();
					Alphabet currAlphabet = currAutomaton.getAlphabet();

					// Iterator over the problem events
					for (EventIterator evIt = problemEvents.iterator();
							evIt.hasNext(); )
					{
						LabeledEvent currEvent = evIt.nextEvent();

						if (currAlphabet.contains(currEvent.getLabel()))
						{
							LabeledEvent currAutomatonEvent = currAlphabet.getEvent(currEvent.getLabel());

							currAutomatonEvent.setControllable(false);
						}
					}
				}

				StringBuffer sb = new StringBuffer();

				for (EventIterator evIt = problemEvents.iterator();
						evIt.hasNext(); )
				{
					LabeledEvent currEvent = evIt.nextEvent();

					sb.append(currEvent + " ");
				}

				logger.warn(sb.toString() + "are controllable but not observable. This imply that a supremal supervisor may not exist. To guarantee existence of such a supervisor the events will be treated us uncontrollable from the supervisors point of view. However the supervisor does not have to be maximally permissive.");

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

		syncher.execute();    // should be able to interrupt this one, just not now...

		retval.automaton = syncher.getAutomaton();
		retval.didSomething |= !syncher.getHelper().getAutomataIsControllable();

		if (synthesizerOptions.getSynthesisType() == SynthesisType.Observable)
		{

			// Reset the synchronization type
			synchronizationOptions.setRememberDisabledEvents(orgRememberDisabledEvents);
		}

		// We need to synthesize even if the result above is controllable
		// Nonblocking may ruin controllability
		// ARASH: choose between tripple and the single fixpoint algorithms:
		AutomatonSynthesizer synthesizer = singleFixpoint
										   ? new AutomatonSynthesizerSingleFixpoint(retval.automaton, synthesizerOptions)
										   : new AutomatonSynthesizer(retval.automaton, synthesizerOptions);

		retval.didSomething |= synthesizer.synthesize();    // should also be able to interrupt this one....
		retval.disabledEvents = synthesizer.getDisabledEvents();
		retval.automaton = synthesizer.getAutomaton();

		// Set an apropriate name... (the name should be null afterwards)
		retval.automaton.setComment("sup(" + retval.automaton.getName() + ")");
		retval.automaton.setName(null);

		// Shall we reduce the supervisor?
		if (synthesizerOptions.getReduceSupervisors() && synthesizerOptions.doPurge())
		{    // Add the reduced supervisor
			Automaton supervisor = retval.automaton;
			Automaton reducedSupervisor = AutomatonSplit.reduceAutomaton(supervisor, automata);

			retval.automaton = reducedSupervisor;
		}
		else if (synthesizerOptions.getReduceSupervisors() &&!synthesizerOptions.doPurge())
		{
			logger.warn("Supervisor reduction only works if the supervisor is purged.");
		}

		// Return the result
		return retval;
	}

	/**
	 * Removes unnecessary automata, i.e. synthesized supervisors that don't affect the controllability.
	 * Note: At the moment, only controllability is checked, no non-blocking.
	 *
	 * @param  theAutomata contains the originally given specs/sups and plants
	 * @param  newAutomata the Automata-object containing the new supervisors, is altered!
	 */
	private void optimize(Automata theAutomata, Automata newAutomata)
	{
		logger.debug("AutomataSynthesizer.optimize");

		// Deep copy the new automata, so we can purge without affecting the originals
		Automata tempAutomata = new Automata(newAutomata);

		// Get the default synchronizationOptions
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

		// Make sure the automata are purged - they must be for the optimization to work...
		if (!synthesizerOptions.doPurge())
		{
			Iterator autIt = tempAutomata.iterator();

			while (autIt.hasNext())
			{
				AutomatonPurge automatonPurge = new AutomatonPurge((Automaton) autIt.next());

				automatonPurge.execute();
			}
		}

		Automata currAutomata = new Automata();

		currAutomata.addAutomata(theAutomata);
		currAutomata.addAutomata(tempAutomata);

		AutomataVerifier theVerifier;
		VerificationOptions theVerificationOptions = new VerificationOptions();

		// Is this the only necessary option?
		theVerificationOptions.setVerificationType(VerificationType.Controllability);

		// Remove the new automata one by one and examine if it had impact on the result.
		for (int i = tempAutomata.size() - 1; i >= 0; i--)
		{
			currAutomata.removeAutomaton(tempAutomata.getAutomatonAt(i));

			try
			{    // Verify controllability with one automaton removed
				theVerifier = new AutomataVerifier(currAutomata, syncOptions, theVerificationOptions);

				if (theVerifier.verify())
				{    // This supervisor had no impact, throw it away!
					newAutomata.removeAutomaton(newAutomata.getAutomatonAt(i));
				}
				else
				{    // It had impact, put it back!
					currAutomata.addAutomaton(tempAutomata.getAutomatonAt(i));
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
	}
}
