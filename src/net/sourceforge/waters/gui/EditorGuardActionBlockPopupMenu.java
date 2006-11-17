
package net.sourceforge.waters.gui;

import java.awt.event.*;
import javax.swing.*;
import org.supremica.util.VPopupMenu;
import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.GuardActionBlockSubject;
import net.sourceforge.waters.subject.module.LabelBlockSubject;

/**
 * Popup for editing attributes of a label block.
 */
class EditorGuardActionBlockPopupMenu
	extends VPopupMenu
	implements ActionListener
{
	private EdgeSubject mEdge;
	private ControlledSurface parent;

	private JMenuItem editEdgeItem;
		
	public EditorGuardActionBlockPopupMenu(ControlledSurface parent, 
			GuardActionBlockSubject block)
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
