/************************ StateSubTree.java *******************/
// A StateSubTree is a tree node with the state name as root 
// and the state properties as children

package org.supremica.guitreeview;

import org.supremica.automata.algorithms.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.tree.*;
import java.util.*;

import org.supremica.automata.State;

public class StateSubTree
	extends SupremicaTreeNode
{
	public StateSubTree(State state)
	{
		super(state);	// Note that this also caches the state for quick access

		if(state.isInitial())
		{
			SupremicaTreeNode initial = new SupremicaTreeNode("initial");
			add(initial);
		}
		if(state.isAccepting())
		{
			SupremicaTreeNode accepting = new SupremicaTreeNode("accepting");
			add(accepting);
		}
		if(state.isForbidden())
		{
			SupremicaTreeNode forbidden = new SupremicaTreeNode("forbidden");
			add(forbidden);
		}
	}
	
	// This calculates the number of direct leaf children
	// That is, the number of initial/accepting/forbidden leaf nodes
	public int numDirectLeafs()
	{
		State state = (State)getUserObject();
		int directleafs = 0;
		if(state.isInitial()) ++directleafs;
		if(state.isForbidden()) ++directleafs;
		if(state.isAccepting()) ++directleafs;
		return directleafs;
	}
}