package org.supremica.automata.algorithms;

import java.util.Iterator;

import net.sourceforge.waters.model.analysis.AbortRequester;
import net.sourceforge.waters.model.analysis.Abortable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.automata.Alphabet;
import org.supremica.automata.AlphabetHelpers;
import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.LabeledEvent;
import org.supremica.gui.ExecutionDialog;
import org.supremica.gui.ExecutionDialogMode;
import org.supremica.properties.Config;


public class MonolithicAutomataSynthesizer implements Abortable {
	/**
	 * This method synchronises the given automata, and calculates the forbidden
	 * states. Uses the ordinary synthesis algorithm.
	 */
	private static Logger logger = LogManager.getLogger(MonolithicAutomataSynthesizer.class);

	/**
	 * This method synchronises the given automata, and calculates the forbidden
	 * states.
	 */
	public MonolithicReturnValue synthesizeSupervisor(Automata automata,
			final SynthesizerOptions synthesizerOptions,
			final SynchronizationOptions synchronizationOptions,
			final ExecutionDialog executionDialog,
			final AutomataSynchronizerHelperStatistics helperStatistics,
			final boolean singleFixpoint) {
		logger.info("Attempting monolithic synthesis for: " + automata);

		final MonolithicReturnValue retval = new MonolithicReturnValue();

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
		if (synthesizerOptions.getSynthesisType() == SynthesisType.NONBLOCKING_CONTROLLABLE_NORMAL) {
			final Alphabet unionAlphabet = AlphabetHelpers.getUnionAlphabet(automata);
			final Alphabet problemEvents = new Alphabet();

			for (final LabeledEvent currEvent : unionAlphabet) {
				if (currEvent.isControllable() && !currEvent.isObservable()) {
					problemEvents.addEvent(currEvent);
				}
			}

			if (problemEvents.size() > 0) {
				// Make copy since we will change controllability
				final Automata newAutomata = new Automata(automata);

				// Iterate over all the automata and change
				// controllability of the problem events
				for (final Automaton currAutomaton : newAutomata) {
					final Alphabet currAlphabet = currAutomaton.getAlphabet();

					// Iterator over the problem events
					for (final Iterator<LabeledEvent> evIt = problemEvents.iterator(); evIt
							.hasNext();) {
						final LabeledEvent currEvent = evIt.next();

						if (currAlphabet.contains(currEvent.getLabel())) {
							final LabeledEvent currAutomatonEvent = currAlphabet
									.getEvent(currEvent.getLabel());

							currAutomatonEvent.setControllable(false);
						}
					}
				}

				final StringBuilder sb = new StringBuilder();
				for (final LabeledEvent currEvent : problemEvents) {
					sb.append(currEvent + " ");
				}

				logger
						.warn(sb.toString()
								+ "are controllable but not observable. This implies that a supremal"
								+ "supervisor may not exist. To guarantee existence of such a supervisor the events "
								+ "will be treated us uncontrollable from the supervisors point of view. However the "
								+ "supervisor does not have to be maximally permissive.");

				automata = newAutomata;
			}
		}

		// Remember old setting
		final boolean orgRememberDisabledEvents = synchronizationOptions
				.rememberDisabledEvents();

		// We must keep track of all events that we have disabled
		// This is used when checking for observability
		if (synthesizerOptions.getSynthesisType() == SynthesisType.NONBLOCKING_CONTROLLABLE_NORMAL) {
			synchronizationOptions.setRememberDisabledEvents(true);
		}

		/*
		 * The collection uc_events is to keep track of which events are originally controllable
		 * If uc_evenst is non-empty at the end of the synthesis, this means that the
		 * uncontrollability of those events needs to be restored
		**/
		// final Alphabet uc_events = new Alphabet(); //
		// Note! Cannot use Alphabet here, since Alphabet does not allow multiple same-labeled events
		// A linked list will do just fine, since we only add, and then traverse once at the end (no search)
		final java.util.List<LabeledEvent> uc_events = new java.util.LinkedList<>();

		if (synthesizerOptions.getSynthesisType() == SynthesisType.NONBLOCKING)
		{
			automata = new Automata(automata);
			// Only nonblocking? Then everything should be considered controllable!
			// But don't forget to restore everything, see issue #38 // MF
			for (final Automaton automaton : automata)
			{
				for (final LabeledEvent event : automaton.getAlphabet())
				{
					if(!event.isControllable())
					{
						uc_events.add(event);	// Keep track that this events was uncontrollable...
						event.setControllable(true);	// ... then set it controllable.
					}
				}
			}
		}

		final AutomataSynchronizer syncher = new AutomataSynchronizer(automata,
				synchronizationOptions, Config.SYNTHESIS_SUP_AS_PLANT.getValue());
		syncher.getHelper().setExecutionDialog(executionDialog);
		threadToStop = syncher;
		syncher.execute();
		threadToStop = null;

		// Exctract statistics, add to global statistics set in helperData.
		final AutomataSynchronizerHelper helper = syncher.getHelper();
		helperStatistics.setNumberOfCheckedStates(helperStatistics
				.getNumberOfCheckedStates()
				+ helper.getHelperData().getNumberOfCheckedStates());
		helperStatistics.setNumberOfDeadlockedStates(helperStatistics
				.getNumberOfDeadlockedStates()
				+ helper.getHelperData().getNumberOfDeadlockedStates());
		helperStatistics.setNumberOfForbiddenStates(helperStatistics
				.getNumberOfForbiddenStates()
				+ helper.getHelperData().getNumberOfForbiddenStates());
		helperStatistics.setNumberOfReachableStates(helperStatistics
				.getNumberOfReachableStates()
				+ helper.getHelperData().getNumberOfReachableStates());

		if (stopRequested) {
			return null;
		}

		retval.automaton = syncher.getAutomaton();
		retval.didSomething |= !syncher.getHelper().getAutomataIsControllable();

		if (synthesizerOptions.getSynthesisType() == SynthesisType.NONBLOCKING_CONTROLLABLE_NORMAL) {
			// Reset the synchronization type
			synchronizationOptions
					.setRememberDisabledEvents(orgRememberDisabledEvents);
		}

		// We need to synthesize even if the result above is controllable
		// NONBLOCKING may ruin controllability
		// ARASH: choose between triple and the single fixpoint algorithms:
		final AutomatonSynthesizer synthesizer = singleFixpoint ?
				new AutomatonSynthesizerSingleFixpoint(retval.automaton, synthesizerOptions)
				: new AutomatonSynthesizer(retval.automaton, synthesizerOptions);
		threadToStop = synthesizer;
		retval.didSomething |= synthesizer.synthesize();
		threadToStop = null;

		if (stopRequested) {
			return null;
		}

		retval.disabledUncontrollableEvents = synthesizer
				.getDisabledUncontrollableEvents();
		retval.automaton = synthesizer.getAutomaton();

		// Shall we reduce the supervisor?
		if (synthesizerOptions.getReduceSupervisors()) {
			if (executionDialog != null) {
				executionDialog.setMode(ExecutionDialogMode.SYNTHESISREDUCING);
			}

			// Supervisor reduction only works if the supervisor is purged
			assert (synthesizerOptions.doPurge());

			// Add the reduced supervisor
			final Automaton supervisor = retval.automaton;
			final Automaton reducedSupervisor = AutomatonSplit.reduceAutomaton(
					supervisor, automata);
			retval.automaton = reducedSupervisor;

			if (executionDialog != null) {
				executionDialog.setMode(ExecutionDialogMode.SYNTHESIZING);
			}
		}

		// Restore the unontrollability of the events that may have been made
		// controllable for Monolithic Nonblocking (Explicit) synthesis, but
		// were originally uncontrollable
		for(final LabeledEvent ucev : uc_events)
			ucev.setControllable(false);

		// Return the result
		return retval;
	}

	private boolean stopRequested = false;
	private Abortable threadToStop;

	@Override
	public boolean isAborting() {
		return stopRequested;
	}

	@Override
	public void requestAbort(final AbortRequester sender) {
		stopRequested = true;
		if (threadToStop != null) {
			threadToStop.requestAbort(sender);
		}
	}

	@Override
	public void resetAbort(){
	  stopRequested = false;
	}
}
