package org.supremica.automata.algorithms.standard;

import org.supremica.log.*;

import org.supremica.automata.*;

public class ObserverBuilder
{
	private static Logger logger = LoggerFactory.createLogger(ObserverBuilder.class);

	private Determinizer determinizer;
	private String orgAutomatonName;

	public ObserverBuilder(Automaton automaton)
	{
		this(automaton, false);
	}

	public ObserverBuilder(Automaton automaton, boolean resolve)
	{
		EpsilonTester epsilonTester = new ObserverEpsilonTester();
		this.determinizer = new Determinizer(automaton, epsilonTester);
		determinizer.checkControlInconsistencies(true);
		orgAutomatonName = automaton.getName();
	}
	
	public void execute()
	{
		determinizer.checkControlInconsistencies(true);
		determinizer.resolveControlInconsistencies(true);
		determinizer.execute();
		boolean inconsistent = determinizer.isControlInconsistent();
		logger.debug(orgAutomatonName + " is control inconsistent: " + inconsistent);
	}

	public Automaton getNewAutomaton()
	{
		return determinizer.getNewAutomaton();
	}
}
