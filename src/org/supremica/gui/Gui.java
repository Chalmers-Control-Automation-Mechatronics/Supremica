package org.supremica.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.util.*;
import java.io.*;
import org.supremica.*;
import org.supremica.automata.algorithms.*;
import org.supremica.comm.xmlrpc.*;
import org.supremica.gui.editor.*;
import org.supremica.gui.help.*;
import org.supremica.automata.Automata;
import org.supremica.automata.Project;
import org.supremica.automata.Automaton;
import org.supremica.automata.execution.Actions;
import org.supremica.automata.execution.Controls;
import org.supremica.gui.VisualProjectContainer;

public interface Gui
{
	public void error(String msg);

	// outputs an error message
	public void error(String msg, Throwable t);

	public void info(String msg);

	public void debug(String msg);

	public void repaint();

	public String getNewAutomatonName(String str, String def);

	// who uses this one?
	public void clearSelection();

	public void selectAll();

	public void close();

	public int addAutomata(Automata a)
		throws Exception;

	// returns number added
	public int addProject(Project p)
		throws Exception;

	// returns true if added
	public boolean addAutomaton(Automaton a);

	public Component getComponent();

	public void addAttributes(Project otherProject);

	//public void addActions(Actions theActions);

	//public void addControls(Controls theControls);

	// Do we need this one?
	public JFrame getFrame();

	// this should be the main frame
	public VisualProjectContainer getVisualProjectContainer();

	public Collection getSelectedAutomataAsCollection();

	public Automata getSelectedAutomata();

	public Project getSelectedProject();

	public FileSecurity getFileSecurity();
}
