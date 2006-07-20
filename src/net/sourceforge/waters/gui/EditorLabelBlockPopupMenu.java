
package net.sourceforge.waters.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import org.supremica.util.VPopupMenu;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.gui.command.Command;
import net.sourceforge.waters.gui.command.DeleteEdgeCommand;
import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.LabelBlockSubject;

/**
 * Popup for editing attributes of a label block.
 */
class EditorLabelBlockPopupMenu
	extends VPopupMenu
	implements ActionListener
{
	private EdgeSubject mEdge;
	private ControlledSurface parent;

	private JMenuItem editEdgeItem;
		
	public EditorLabelBlockPopupMenu(ControlledSurface parent, 
								LabelBlockSubject block)
	{
		this.parent = parent;
		mEdge = (EdgeSubject)block.getParent();

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
			EditorEditEdgeDialog.showDialog(mEdge);
		}
		parent.repaint();
	}
}
