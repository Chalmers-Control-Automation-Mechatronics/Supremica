/********************* AutomatonSubTree.java *************/
// An AutomatonSubTree is a tree node with the automaton name as root 
// and the events as children and the states as children

package org.supremica.gui.treeview;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.tree.*;
import java.util.*;

import org.supremica.automata.Alphabet;
import org.supremica.automata.Automaton;
import org.supremica.automata.LabeledEvent;

public class AutomatonSubTree	
	extends SupremicaTreeNode
{
	public AutomatonSubTree(Automaton automaton, boolean includeAlphabet, boolean includeStates)
	{
		super(automaton.getName());

		// If we are to show either, but not both, the "Alphabet" and/or "State" nodes are unnecessary
		if(includeAlphabet && includeStates)
		{
			add(new AlphabetSubTree(automaton.getAlphabet()));
			add(new StateSetSubTree(automaton.getStateSet()));
		}
		else // now we know that both are not valid
		if(includeAlphabet)
		{
			AlphabetSubTree.buildSubTree(automaton.getAlphabet(), this);
		}
		else
		if(includeStates)
		{
			StateSetSubTree.buildSubTree(automaton.getStateSet(), this);
		}
		// else // none
	}
}
	
