
package net.sourceforge.waters.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import org.supremica.util.VPopupMenu;
import net.sourceforge.waters.model.module.SimpleNodeProxy;

/**
 * Popup for editing attributes of a node.
 */
class EditorEdgePopupMenu
	extends VPopupMenu
	implements ActionListener
{
	private EditorEdge edge;
	private EditorSurface parent;

	private JMenuItem renameItem;
	private JMenuItem deleteItem;
	//private JCheckBox initialBox;
	private JMenuItem initialItem;	

	public EditorEdgePopupMenu(EditorSurface parent, EditorEdge edge)
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
	}

	public void actionPerformed(ActionEvent e) 
	{
		if (e.getSource() == deleteItem)
		{
			parent.delEdge(edge);
			this.hide();
		}

		parent.repaint();
	}
}
