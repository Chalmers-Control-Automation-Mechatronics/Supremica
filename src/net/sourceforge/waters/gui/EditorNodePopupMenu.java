//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   EditorLabel
//###########################################################################
//# $Id: EditorNodePopupMenu.java,v 1.6 2005-11-03 01:24:15 robi Exp $
//###########################################################################


package net.sourceforge.waters.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import org.supremica.util.VPopupMenu;
import net.sourceforge.waters.model.module.SimpleNodeProxy;

/**
 * Popup for editing attributes of a node.
 */
class EditorNodePopupMenu
	extends VPopupMenu
	implements ActionListener
{
	private EditorNode node;
	private EditorSurface parent;

	private JMenuItem renameItem;
	private JMenuItem deleteItem;
	private JMenuItem initialItem;	
	private JMenuItem recallItem;	

	public EditorNodePopupMenu(EditorSurface parent, EditorNode node)
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

		item = new JMenuItem("Make initial");
		item.addActionListener(this);
		this.add(item);
		initialItem = item;

		// Disable "initial" is node already is initial
		if (node.isInitial())
		{
			initialItem.setEnabled(false);
			initialItem.setToolTipText("State is already initial");
		}

		item = new JMenuItem("Recall label");
		item.addActionListener(this);
		this.add(item);
		recallItem = item;

		// Disable "recall" if label is in right position (or maybe instead if it is close enough?)
		if ((parent.getLabel(node).getOffsetX() == EditorLabel.DEFAULTOFFSETX) && (parent.getLabel(node).getOffsetY() == EditorLabel.DEFAULTOFFSETY))
		{
			recallItem.setEnabled(false);
			recallItem.setToolTipText("Label is already in default position");
		}
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

		if (e.getSource() == initialItem)
		{
			if (!node.getSubject().isInitial())
			{
				parent.unsetAllInitial();
				node.getSubject().setInitial(true);
			}
		}

		if (e.getSource() == recallItem) {
			final EditorLabel label = parent.getLabel(node);
			label.setOffset(EditorLabel.DEFAULTOFFSETX,
							EditorLabel.DEFAULTOFFSETY);
		}

		parent.repaint();
	}
}
