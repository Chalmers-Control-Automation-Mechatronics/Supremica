
/********************* AlphabetViewerSubTree.java *************/

// An AlphabetViewerSubTree is a tree node with the automaton name as root and the events as children
package org.supremica.gui.treeview;

import java.util.*;
import org.supremica.automata.Automaton;
import org.supremica.automata.LabeledEvent;

public class AlphabetViewerSubTree
	extends SupremicaTreeNode
{
	/**
	 * DEPRECATED! Use AlphabetSubTree instead!!
	 */
	private AlphabetViewerSubTree(Automaton automaton)
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
