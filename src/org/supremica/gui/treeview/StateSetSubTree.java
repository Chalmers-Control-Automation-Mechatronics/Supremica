
/****************** StateSetSubTree.java **************/

// A StateSetSubTree is a tree node with "States" as root 
// and the states as children
package org.supremica.gui.treeview;

import javax.swing.*;
import java.util.*;
import org.supremica.automata.State;
import org.supremica.automata.StateSet;
import org.supremica.gui.Supremica;

public class StateSetSubTree
	extends SupremicaTreeNode
{
    private static final long serialVersionUID = 1L;

	private static ImageIcon statesIcon = new ImageIcon(Supremica.class.getResource("/icons/States16.gif"));

	public StateSetSubTree(StateSet states)
	{
		super("States");

		buildSubTree(states, this);
	}

	public static void buildSubTree(StateSet states, SupremicaTreeNode root)
	{
		Iterator<State> stateit = states.iterator();

		while (stateit.hasNext())
		{
			State state = (State) stateit.next();

			root.add(new StateSubTree(state));
		}
	}

	public Icon getOpenIcon()
	{
		return statesIcon;
	}

	public Icon getClosedIcon()
	{
		return getOpenIcon();
	}

	public Icon getLeafIcon()
	{
		return getOpenIcon();
	}
}
