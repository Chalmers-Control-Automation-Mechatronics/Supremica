package org.supremica.automata.algorithms;

import java.util.Iterator;

import org.supremica.automata.Alphabet;
import org.supremica.automata.AlphabetHelpers;
import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.LabeledEvent;
import org.supremica.gui.ExecutionDialog;
import org.supremica.gui.ExecutionDialogMode;
import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;

// This one is used for doMonolithic to return two values
class MonolithicReturnValue
{
    public Automaton automaton;
    public boolean didSomething;
    public Alphabet disabledUncontrollableEvents;    // see AutomatonSynthesizer
}

public class MonolithicAutomataSynthesizer implements Stoppable {
	/**
	 * This method synchronizes the given automata, and calculates the forbidden
	 * states. Uses the ordinary synthesis algorithm.
	 */
	private static Logger logger = LoggerFactory
			.createLogger(MonolithicAutomataSynthesizer.class);

	/**
	 * This method synchronizes the given automata, and calculates the forbidden
	 * states.
	 */
	public MonolithicReturnValue synthesizeSupervisor(Automata automata,
			SynthesizerOptions synthesizerOptions,
			SynchronizationOptions synchronizationOptions,
			ExecutionDialog executionDialog,
			AutomataSynchronizerHelperStatistics helperStatistics,
			boolean singleFixpoint) {
		logger.verbose("Attempting monolithic synthesis for: " + automata);

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
		if (synthesizerOptions.getSynthesisType() == SynthesisType.NONBLOCKINGCONTROLLABLEOBSERVABLE) {
			Alphabet unionAlphabet = AlphabetHelpers.getUnionAlphabet(automata);
			Alphabet problemEvents = new Alphabet();

			for (LabeledEvent currEvent : unionAlphabet) {
				if (currEvent.isControllable() && !currEvent.isObservable()) {
					problemEvents.addEvent(currEvent);
				}
			}

			if (problemEvents.size() > 0) {
				// Make copy since we will change controllability
				Automata newAutomata = new Automata(automata);

				// Iterate over all the automata and change
				// controllability of the problem events
				for (Automaton currAutomaton : newAutomata) {
					Alphabet currAlphabet = currAutomaton.getAlphabet();

					// Iterator over the problem events
					for (Iterator<LabeledEvent> evIt = problemEvents.iterator(); evIt
							.hasNext();) {
						LabeledEvent currEvent = evIt.next();

						if (currAlphabet.contains(currEvent.getLabel())) {
							LabeledEvent currAutomatonEvent = currAlphabet
									.getEvent(currEvent.getLabel());

							currAutomatonEvent.setControllable(false);
						}
					}
				}

				StringBuffer sb = new StringBuffer();
				for (LabeledEvent currEvent : problemEvents) {
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
		boolean orgRememberDisabledEvents = synchronizationOptions
				.rememberDisabledEvents();

		// We must keep track of all events that we have disabled
		// This is used when checking for observability
		if (synthesizerOptions.getSynthesisType() == SynthesisType.NONBLOCKINGCONTROLLABLEOBSERVABLE) {
			synchronizationOptions.setRememberDisabledEvents(true);
		}

		if (synthesizerOptions.getSynthesisType() == SynthesisType.NONBLOCKING) {
			automata = new Automata(automata);
			// Only nonblocking? Then everything should be considered
			// controllable!
			for (Automaton automaton : automata) {
				for (LabeledEvent event : automaton.getAlphabet()) {
					event.setControllable(true);
				}
			}
		}

		AutomataSynchronizer syncher = new AutomataSynchronizer(automata,
				synchronizationOptions);
		syncher.getHelper().setExecutionDialog(executionDialog);
		threadToStop = syncher;
		syncher.execute();
		threadToStop = null;

		// Exctract statistics, add to global statistics set in helperData.
		AutomataSynchronizerHelper helper = syncher.getHelper();
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

		if (synthesizerOptions.getSynthesisType() == SynthesisType.NONBLOCKINGCONTROLLABLEOBSERVABLE) {
			// Reset the synchronization type
			synchronizationOptions
					.setRememberDisabledEvents(orgRememberDisabledEvents);
		}

		// We need to synthesize even if the result above is controllable
		// NONBLOCKING may ruin controllability
		// ARASH: choose between triple and the single fixpoint algorithms:
		AutomatonSynthesizer synthesizer = singleFixpoint ? 
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
			Automaton supervisor = retval.automaton;
			Automaton reducedSupervisor = AutomatonSplit.reduceAutomaton(
					supervisor, automata);
			retval.automaton = reducedSupervisor;

			if (executionDialog != null) {
				executionDialog.setMode(ExecutionDialogMode.SYNTHESIZING);
			}
		}

		// Return the result
		return retval;
	}

	private boolean stopRequested = false;
	private Stoppable threadToStop;

	@Override
	public boolean isStopped() {
		return stopRequested;
	}

	@Override
	public void requestStop() {
		stopRequested = true;
		if (threadToStop != null) {
			threadToStop.requestStop();
		}
	}
}
