
package net.sourceforge.waters.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import org.supremica.util.VPopupMenu;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.gui.command.Command;
import net.sourceforge.waters.gui.command.DeleteEdgeCommand;

/**
 * Popup for editing attributes of a node.
 */
class EditorLabelBlockPopupMenu
	extends VPopupMenu
	implements ActionListener
{
	private EditorEdge edge;
	private ControlledSurface parent;

	private JMenuItem addGuardItem;
	private JMenuItem addActionItem;
	private JMenuItem deleteGuardActionItem;
	private EditorLabelGroup mBlock;	
	private EditorGuardActionBlock mGA;
	public EditorLabelBlockPopupMenu(ControlledSurface parent, EditorLabelGroup block)
	{
		this.parent = parent;
		this.mBlock = block;
		edge = mBlock.getParent();
		mGA = edge.getEditorGuardActionBlock();

		init();
	}

	/**
	 * Initialize the menu.
	 */
	private void init()
	{
		JMenuItem item;

		item = new JMenuItem("Add guard");
		item.addActionListener(this);
		this.add(item);
		addGuardItem = item;

		item = new JMenuItem("Add action");
		item.addActionListener(this);
		this.add(item);
		addActionItem = item;

		item = new JMenuItem("Delete guard & action");
		item.addActionListener(this);
		this.add(item);
		deleteGuardActionItem = item;

		// Disable "Add guard" if there is already a guard expression
		if (mGA != null) 
		{
			if (mGA.hasGuard())
			{
				addGuardItem.setEnabled(false);
				addGuardItem.setToolTipText("A transition can have at most one guard expression.");
			}
		}

		//Disable "Delete" if there is no guard and action
		if (mGA == null)
		{
			deleteGuardActionItem.setEnabled(false);
			deleteGuardActionItem.setToolTipText("No guard or action in this block.");
		}
	}

	public void actionPerformed(ActionEvent e) 
	{
		if (e.getSource() == addGuardItem)
		{
			if(mGA == null) {
				parent.addGuardActionBlock(edge);
			}
			mGA = edge.getEditorGuardActionBlock();
			mGA.addGuard();
		}

		if (e.getSource() == addActionItem)
		{
			if(mGA == null) {
				parent.addGuardActionBlock(edge);
			}
			mGA = edge.getEditorGuardActionBlock();
			mGA.addAction();
		}

		if (e.getSource() == deleteGuardActionItem)
		{
			parent.removeGuardActionBlock(edge);
			mGA = null;
		}

		parent.repaint();
	}
}
