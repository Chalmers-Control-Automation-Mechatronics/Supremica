package org.supremica.gui;

import java.awt.*;
import javax.swing.*;
import java.util.*;
import org.supremica.automata.algorithms.*;
import org.supremica.automata.Automata;
import org.supremica.automata.Project;
import org.supremica.automata.Automaton;
import org.supremica.gui.VisualProjectContainer;

public interface Gui
{
	void error(String msg);

	// outputs an error message
	void error(String msg, Throwable t);

	void info(String msg);

	void debug(String msg);

	void repaint();

	String getNewAutomatonName(String str, String def);

	// who uses this one?
	// ActionMan does!!
	void clearSelection();

	void invertSelection();

	void selectAll();

	void selectAutomata(int[] a);

	void selectAutomata(Collection a);

	void unselectAutomaton(int s);

	void close();

	int addAutomata(Automata a)
		throws Exception;

	// returns number added
	int addProject(Project p)
		throws Exception;

	// returns true if added
	boolean addAutomaton(Automaton a);

	Component getComponent();

	void addAttributes(Project otherProject);

	//public void addActions(Actions theActions);

	//public void addControls(Controls theControls);

	// Do we need this one?
	JFrame getFrame();

	// this should be the main frame
	VisualProjectContainer getVisualProjectContainer();

	Collection getSelectedAutomataAsCollection();

	Automata getSelectedAutomata();
	Automata getUnselectedAutomata();

	Project getSelectedProject();

	FileSecurity getFileSecurity();

	void show();
}
