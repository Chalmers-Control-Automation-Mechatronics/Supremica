package org.supremica.gui.ide;

import javax.swing.JFrame;
import org.supremica.automata.Automaton;
import org.supremica.automata.Automata;

public interface IDEReportInterface
{
	void error(String msg);

	// outputs an error message
	void error(String msg, Throwable t);

	void info(String msg);

	void debug(String msg);

	JFrame getFrame();

	boolean addAutomaton(Automaton theAutomaton);

	int addAutomata(Automata theAutomata);

}
