//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   EditorLabel
//###########################################################################
//# $Id: EditorNodePopupMenu.java,v 1.7 2005-12-01 16:46:39 flordal Exp $
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

	private JMenuItem deleteItem;
	private JMenuItem initialItem;	
	private JMenuItem recallItem;	
	private JMenuItem clearItem;	

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

		item = new JMenuItem("Delete node");
		item.addActionListener(this);
		this.add(item);
		deleteItem = item;

		item = new JMenuItem("Make initial");
		item.addActionListener(this);
		this.add(item);
		// Disable "initial" is node already is initial
		if (node.isInitial())
		{
			item.setEnabled(false);
			item.setToolTipText("State is already initial");
		}
		initialItem = item;

		item = new JMenuItem("Recall label");
		item.addActionListener(this);
		this.add(item);
		// Disable "recall" if label is in right position (or maybe instead if it is close enough?)
		if ((parent.getLabel(node).getOffsetX() == EditorLabel.DEFAULTOFFSETX) && 
			(parent.getLabel(node).getOffsetY() == EditorLabel.DEFAULTOFFSETY))
		{
			item.setEnabled(false);
			item.setToolTipText("Label is already in default position");
		}
		recallItem = item;

		item = new JMenuItem("Clear marking");
		item.addActionListener(this);
		this.add(item);
		// Disable if there are no propositions
		if (!node.hasPropositions())
		{
			item.setEnabled(false);
			item.setToolTipText("State has no marking");

		}
		clearItem = item;
	}

	public void actionPerformed(ActionEvent e) 
	{
		if (e.getSource() == deleteItem)
		{
			parent.delNode(node);
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

		if (e.getSource() == clearItem)
		{
			node.clearPropositions();
		}

		this.hide();
		parent.repaint();
	}
}
