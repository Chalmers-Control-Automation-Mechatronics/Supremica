/****************** StateSetSubTree.java **************/
// A StateSetSubTree is a tree node with "States" as root 
// and the states as children

package org.supremica.gui.treeview;

import org.supremica.automata.algorithms.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.tree.*;
import java.util.*;

import org.supremica.automata.State;
import org.supremica.automata.StateSet;

public class StateSetSubTree
	extends SupremicaTreeNode
{
	public StateSetSubTree(StateSet states)
	{
		super("States");

		buildSubTree(states, this);		
	}
	
	public static void buildSubTree(StateSet states, SupremicaTreeNode root)
	{
		Iterator stateit = states.iterator();
		while(stateit.hasNext())
		{
			State state = (State)stateit.next();
			root.add(new StateSubTree(state));
		}
	}
}