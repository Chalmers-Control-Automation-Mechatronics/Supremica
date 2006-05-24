
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

	private JMenuItem editEdgeItem;
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

		item = new JMenuItem("Edit edge");
		item.addActionListener(this);
		editEdgeItem = item;
		this.add(item);
	}

	public void actionPerformed(ActionEvent e) 
	{
		if (e.getSource() == editEdgeItem)
		{
			EditorEditEdgeDialog.showDialog(edge.getSubject());
		}
		parent.repaint();
	}
}
