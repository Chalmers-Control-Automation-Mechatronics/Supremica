/************************ AlphabetSubTree.java *****************/
package org.supremica.gui.treeview;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.tree.*;
import java.util.*;
import org.supremica.automata.Alphabet;
import org.supremica.automata.Automaton;
import org.supremica.automata.LabeledEvent;

class AlphabetSubTree
	extends SupremicaTreeNode
{
	public AlphabetSubTree(Alphabet alpha)
	{
		super("Alphabet");
	
		buildSubTree(alpha, this);
	}
	
	// Note, we cannot builds a subtree without an Alphabet-root, since
	// such a tree would be a forest with no root. Therefore, we need to
	// take the node to insert to and insert into it
	static public void buildSubTree(Alphabet alpha, SupremicaTreeNode root)
	{
		Iterator eventIt = alpha.iterator();
		while (eventIt.hasNext())
		{
			LabeledEvent currEvent = (LabeledEvent) eventIt.next();
			root.add(new EventSubTree(currEvent));
		}
	}
}
	