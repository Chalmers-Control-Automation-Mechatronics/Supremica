/********************* AlphabetViewerSubTree.java *************/
// An AlphabetViewerSubTree is a tree node with the automaton name as root and the events as children

package org.supremica.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.tree.*;
import java.util.*;

import org.supremica.automata.Alphabet;
import org.supremica.automata.Automaton;
import org.supremica.automata.LabeledEvent;

import org.supremica.gui.EventSubTree;

public class AlphabetViewerSubTree	
	extends SupremicaTreeNode
{
	public AlphabetViewerSubTree(Automaton automaton)
	{
		super(automaton.getName());

		Iterator eventIt = automaton.getAlphabet().iterator();
		while (eventIt.hasNext())
		{
			LabeledEvent currEvent = (LabeledEvent) eventIt.next();
			add(new EventSubTree(currEvent));
		}	
	}
}
	