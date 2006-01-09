
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
class EditorEdgePopupMenu
	extends VPopupMenu
	implements ActionListener
{
	private EditorEdge edge;
	private ControlledSurface parent;

	private JMenuItem deleteItem;
	private JMenuItem recallItem;	

	public EditorEdgePopupMenu(ControlledSurface parent, EditorEdge edge)
	{
		this.parent = parent;
		this.edge = edge;

		init();
	}

	/**
	 * Initialize the menu.
	 */
	private void init()
	{
		JMenuItem item;

		item = new JMenuItem("Delete edge");
		item.addActionListener(this);
		this.add(item);
		deleteItem = item;

		item = new JMenuItem("Recall label");
		item.addActionListener(this);
		this.add(item);
		recallItem = item;

		// Disable "recall" if label is in right position (or maybe instead if it is close enough?)
		if ((parent.getLabelGroup(edge).getOffsetX() == EditorLabelGroup.DEFAULTOFFSETX) && (parent.getLabelGroup(edge).getOffsetY() == EditorLabelGroup.DEFAULTOFFSETY))
		{
			recallItem.setEnabled(false);
			recallItem.setToolTipText("Label is already in default position");
		}
	}

	public void actionPerformed(ActionEvent e) 
	{
		if (e.getSource() == deleteItem)
		{
			Command deleteEdge = new DeleteEdgeCommand(parent, edge);
			parent.getEditorInterface().getUndoInterface().executeCommand(deleteEdge);
			this.hide();
		}

		if (e.getSource() == recallItem)
		{
			parent.getLabelGroup(edge).setOffset(EditorLabelGroup.DEFAULTOFFSETX, EditorLabelGroup.DEFAULTOFFSETY);
		}

		parent.repaint();
	}
}
