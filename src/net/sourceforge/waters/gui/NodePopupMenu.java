
package net.sourceforge.waters.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import org.supremica.util.VPopupMenu;
import net.sourceforge.waters.model.module.SimpleNodeProxy;

/**
 * Popup for editing attributes of a node.
 */
class NodePopupMenu
	extends VPopupMenu
	implements ActionListener
{
	private EditorNode node;
	private EditorSurface parent;

	private JMenuItem renameItem;
	private JMenuItem deleteItem;
	//private JCheckBox initialBox;
	private JMenuItem initialItem;	

	public NodePopupMenu(EditorSurface parent, EditorNode node)
	{
		this.parent = parent;
		this.node = node;

		init();
	}

	/**
	 * Initialize the menu.
	 */
	private void init()
	{
		JMenuItem item;
		item = new JMenuItem("Rename node");
		item.addActionListener(this);
		this.add(item);
		item.setEnabled(false);
		item.setToolTipText("Not implemented yet");
		renameItem = item;

		item = new JMenuItem("Delete node");
		item.addActionListener(this);
		this.add(item);
		deleteItem = item;

		/*
		initialBox = new JCheckBox("Initial", node.isInitial());
		initialBox.addActionListener(this);
		this.add(initialBox);
		*/

		item = new JMenuItem("Make initial");
		item.addActionListener(this);
		this.add(item);
		initialItem = item;
	}

	public void actionPerformed(ActionEvent e) 
	{
		if (e.getSource() == renameItem)
		{
			node.setName("Apa", parent);
			this.hide();
		}

		if (e.getSource() == deleteItem)
		{
			parent.delNode(node);
			this.hide();
		}

		/*
		if (e.getSource() == initialBox)
		{
			parent.unsetAllInitial();
			node.getProxy().setInitial(initialBox.isSelected());
		}
		*/

		if (e.getSource() == initialItem)
		{
			if (!node.getProxy().isInitial())
			{
				parent.unsetAllInitial();
				node.getProxy().setInitial(true);
			}
		}

		parent.repaint();
	}
}
