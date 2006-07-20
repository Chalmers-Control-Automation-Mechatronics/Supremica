
package net.sourceforge.waters.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import org.supremica.util.VPopupMenu;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.gui.command.Command;
import net.sourceforge.waters.gui.command.DeleteNodeGroupCommand;

import net.sourceforge.waters.subject.module.GroupNodeSubject;

/**
 * Popup for editing attributes of a node.
 */
class EditorNodeGroupPopupMenu
	extends VPopupMenu
	implements ActionListener
{
	private GroupNodeSubject nodegroup;
	private ControlledSurface parent;

	private JMenuItem renameItem;
	private JMenuItem deleteItem;
	//private JCheckBox initialBox;
	private JMenuItem initialItem;	

	public EditorNodeGroupPopupMenu(ControlledSurface parent, GroupNodeSubject nodegroup)
	{
		this.parent = parent;
		this.nodegroup = nodegroup;

		init();
	}

	/**
	 * Initialize the menu.
	 */
	private void init()
	{
		JMenuItem item;

		item = new JMenuItem("Delete nodegroup");
		item.addActionListener(this);
		this.add(item);
		deleteItem = item;
	}

	public void actionPerformed(ActionEvent e) 
	{
		if (e.getSource() == deleteItem)
		{
			Command deleteNodeGroup = new DeleteNodeGroupCommand(parent.getGraph(), nodegroup);
			parent.getEditorInterface().getUndoInterface().executeCommand(deleteNodeGroup);		
			this.hide();
		}

		parent.repaint();
	}
}
