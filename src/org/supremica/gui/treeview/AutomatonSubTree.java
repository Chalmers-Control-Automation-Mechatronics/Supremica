
/********************* AutomatonSubTree.java *************/

// An AutomatonSubTree is a tree node with the automaton name as root 
// and the events as children and the states as children
package org.supremica.gui.treeview;

import javax.swing.*;
import org.supremica.automata.Automaton;
import org.supremica.gui.Supremica;

public class AutomatonSubTree
	extends SupremicaTreeNode
{
	private static ImageIcon plantIcon = new ImageIcon(Supremica.class.getResource("/icons/Plant16.gif"));
	private static ImageIcon specificationIcon = new ImageIcon(Supremica.class.getResource("/icons/Specification16.gif"));
	private static ImageIcon interfaceIcon = new ImageIcon(Supremica.class.getResource("/icons/Interface16.gif"));

	public AutomatonSubTree(Automaton automaton, boolean includeAlphabet, boolean includeStates)
	{

		//super(automaton.getName());
		super(automaton);

		// If we are to show either, but not both, the "Alphabet" and/or "State" nodes are unnecessary
		if (includeAlphabet && includeStates)
		{
			add(new AlphabetSubTree(automaton.getAlphabet()));
			add(new StateSetSubTree(automaton.getStateSet()));
		}
		else if (includeAlphabet)
		{
			AlphabetSubTree.buildSubTree(automaton.getAlphabet(), this);
		}
		else if (includeStates)
		{
			StateSetSubTree.buildSubTree(automaton.getStateSet(), this);
		}
	}

	public Icon getOpenIcon()
	{

		//return null;
		Automaton aut = (Automaton) userObject;

		if (aut.isPlant())
		{
			return plantIcon;
		}
		else if (aut.isSupervisor() || aut.isSpecification())
		{
			return specificationIcon;
		}
		else if (aut.isInterface())
		{
			return interfaceIcon;
		}
		else
		{
			return null;
		}
	}

	public Icon getClosedIcon()
	{

		//return null;
		return getOpenIcon();
	}

	public String toString()
	{
		return ((Automaton) userObject).getName();
	}
}
