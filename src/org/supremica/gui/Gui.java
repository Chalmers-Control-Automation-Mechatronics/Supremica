package org.supremica.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.util.*;
import java.io.*;

import org.supremica.*;
import org.supremica.automata.*;
import org.supremica.automata.algorithms.*;
import org.supremica.comm.xmlrpc.*;
import org.supremica.gui.editor.*;
import org.supremica.gui.help.*;

public interface Gui
{
	public void error(String msg); // outputs an error message
	public void error(String msg, Throwable t);
	public void info(String msg);
	public void debug(String msg);
		
	public void repaint();
	public String getNewAutomatonName(String str, String def); // who uses this one?
	public void clearSelection();
	public void selectAll();
	
	public int addAutomata(Automata a) throws Exception; // returns number added
	public boolean addAutomaton(Automaton a); // returns true if added
	
	public Component getComponent(); 	// Do we need this one?
	public JFrame getFrame(); // this should be the main frame
	public AutomatonContainer getAutomatonContainer();
	public Collection getSelectedAutomataAsCollection();
	public Automata getSelectedAutomata();

	public FileSecurity getFileSecurity();
}
	
