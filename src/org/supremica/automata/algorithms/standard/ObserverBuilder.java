package org.supremica.automata.algorithms.standard;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.automata.Automaton;


public class ObserverBuilder
{
	private static Logger logger = LogManager.getLogger(ObserverBuilder.class);
	private final Determinizer determinizer;
	private final String orgAutomatonName;

	public ObserverBuilder(final Automaton automaton)
	{
		this(automaton, false);
	}

	public ObserverBuilder(final Automaton automaton, final boolean resolve)
	{
		final EpsilonTester epsilonTester = new ObserverEpsilonTester();

		this.determinizer = new Determinizer(automaton, epsilonTester);

		determinizer.checkControlInconsistencies(true);

		orgAutomatonName = automaton.getName();
	}

	public void execute()
	{
		determinizer.checkControlInconsistencies(true);
		determinizer.resolveControlInconsistencies(true);
		determinizer.execute();

		final boolean inconsistent = determinizer.isControlInconsistent();

		logger.info(orgAutomatonName + " is control inconsistent: " + inconsistent);
	}

	public boolean isObservable()
	{
		return !determinizer.isControlInconsistent();
	}

	public Automaton getNewAutomaton()
	{
		return determinizer.getNewAutomaton();
	}
}
