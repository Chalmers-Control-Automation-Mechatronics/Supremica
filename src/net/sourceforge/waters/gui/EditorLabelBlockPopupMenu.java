
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
	private EditorLabelGroup mBlock;	

	public EditorLabelBlockPopupMenu(ControlledSurface parent, EditorLabelGroup block)
	{
		this.parent = parent;
		this.mBlock = block;

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

		// Disable "Add guard" if there is already a guard expression
		if(((EditorEdge) mBlock.getParent()).getEditorGuardActionBlock() != null) {
			if (((EditorEdge) mBlock.getParent()).getEditorGuardActionBlock().hasGuard())
			{
				addActionItem.setEnabled(false);
				addActionItem.setToolTipText("Label is already in default position");
			}
		}
	}

	public void actionPerformed(ActionEvent e) 
	{
		if (e.getSource() == addGuardItem)
		{
			/*Command deleteEdge = new DeleteEdgeCommand(parent, edge);
			parent.getEditorInterface().getUndoInterface().executeCommand(deleteEdge);
			this.hide();*/
			//TODO: add functionality
		}

		if (e.getSource() == addActionItem)
		{
			//TODO: add functionality
		}

		parent.repaint();
	}
}
