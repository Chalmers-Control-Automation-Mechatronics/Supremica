
/******************** EventSubTree.java *******************/

// An EventSubTree is a tree node with the event name as root
// and the event properties as children
package org.supremica.gui.treeview;

import javax.swing.*;
import org.supremica.automata.LabeledEvent;
import org.supremica.gui.Supremica;

public class EventSubTree
	extends SupremicaTreeNode
{
	private static ImageIcon controllableIcon = new ImageIcon(Supremica.class.getResource("/icons/ControllableEvent16.gif"));
	private static ImageIcon uncontrollableIcon = new ImageIcon(Supremica.class.getResource("/icons/UncontrollableEvent16.gif"));

	public EventSubTree(LabeledEvent event)
	{
		super(event);    // Note that this also caches the event for quick access

		SupremicaTreeNode currControllableNode = new SupremicaTreeNode("controllable: " + event.isControllable());

		add(currControllableNode);

		SupremicaTreeNode currPrioritizedNode = new SupremicaTreeNode("prioritized: " + event.isPrioritized());

		add(currPrioritizedNode);

		SupremicaTreeNode currObservableNode = new SupremicaTreeNode("observable: " + event.isObservable());

		add(currObservableNode);

		SupremicaTreeNode currOperatorNode = new SupremicaTreeNode("operator: " + event.isOperator());

		add(currOperatorNode);

		SupremicaTreeNode isEpsilonNode = new SupremicaTreeNode("epsilon: " + event.isEpsilon());

		add(isEpsilonNode);
	}

	// Change this to reflect the correct number of children/properties/leaves
	// Could this be calculated from sizeof(LabeledEvent)? It should not.
	// This depends only on the above construction
	//
	// UM... this method is never used, right? What is this?   /hguo
	public int numDirectLeafs()
	{
		return 5;
	}

	public Icon getOpenIcon()
	{

		//return null;
		if (((LabeledEvent) userObject).isControllable())
		{
			return controllableIcon;
		}
		else
		{
			return uncontrollableIcon;
		}
	}

	public Icon getClosedIcon()
	{

		//return null;
		return getOpenIcon();
	}

	public String toString()
	{
		return ((LabeledEvent) userObject).getLabel();
	}
}
