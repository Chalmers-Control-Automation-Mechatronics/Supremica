package org.supremica.util.BDD;

import java.util.*;

public class IncompleteStateTree
{
	public class StateTreeNode
	{
		private String automaton;
		private String state;
		private Vector children;

		public StateTreeNode()
		{
			this(null, null);
		}

		public StateTreeNode(String name, String state)
		{
			this.automaton = name;
			this.state = state;
			this.children = new Vector();
		}

		public boolean empty()
		{
			return children.size() == 0;
		}

		public StateTreeNode insert(String state)
		{
			StateTreeNode ret = new StateTreeNode(null, state);

			children.add(ret);

			return ret;
		}

		public String getAutomaton()
		{
			return automaton;
		}

		public void setAutomaton(String automaton)
		{
			this.automaton = automaton;
		}

		public String getState()
		{
			return state;
		}

		public Vector getChildren()
		{
			return children;
		}

		public boolean isRoot()
		{
			return (automaton == null);
		}
	}
	;

	// -------------------------------------------------------------
	private StateTreeNode root;

	public IncompleteStateTree()
	{
		this.root = new StateTreeNode();
	}

	public StateTreeNode getRoot()
	{
		return root;
	}

	public boolean empty()
	{
		return root.empty();
	}
}
