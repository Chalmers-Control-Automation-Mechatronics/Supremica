
package net.sourceforge.waters.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import org.supremica.util.VPopupMenu;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import java.util.List;

/**
 * Popup for editing general stuff redarding the selected objects.
 */
class EditorSurfacePopupMenu
	extends VPopupMenu
	implements ActionListener
{
	private List selectedObjects;
	private ControlledSurface parent;

	private JMenuItem renameItem;
	private JMenuItem deleteItem;
	private JMenuItem initialItem;	
	private JMenuItem recallItem;	

	public EditorSurfacePopupMenu(ControlledSurface parent, List selectedObjects)
	{
		this.parent = parent;
		this.selectedObjects = selectedObjects;

		init();
	}

	/**
	 * Initialize the menu.
	 */
	private void init()
	{
		JMenuItem item;

		item = new JMenuItem("Delete selected objects");
		item.addActionListener(this);
		this.add(item);
		deleteItem = item;
	}

	public void actionPerformed(ActionEvent e) 
	{
		if (e.getSource() == deleteItem)
		{
			parent.deleteSelected();
			this.hide();
		}

		parent.repaint();
	}
}
