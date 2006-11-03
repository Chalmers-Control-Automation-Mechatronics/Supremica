
package net.sourceforge.waters.gui;

import java.awt.event.*;
import javax.swing.*;
import org.supremica.util.VPopupMenu;
import net.sourceforge.waters.gui.command.Command;
import net.sourceforge.waters.gui.command.DeleteEdgeCommand;
import net.sourceforge.waters.gui.command.FlipEdgeCommand;
import net.sourceforge.waters.gui.renderer.LabelBlockProxyShape;

import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.GroupNodeSubject;

/**
 * Popup for editing attributes of a node.
 */
class EditorEdgePopupMenu
	extends VPopupMenu
	implements ActionListener
{
	private EdgeSubject edge;
	private ControlledSurface parent;

	private JMenuItem deleteItem;
	private JMenuItem recallItem;	
	private JMenuItem flipItem;		
	private JMenuItem editEdgeItem;	

	public EditorEdgePopupMenu(ControlledSurface parent, EdgeSubject edge)
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

		item = new JMenuItem("Edit Edge");
		item.addActionListener(this);
		this.add(item);
		editEdgeItem = item;
		
		item = new JMenuItem("Delete edge");
		item.addActionListener(this);
		this.add(item);
		deleteItem = item;

		item = new JMenuItem("Recall label");
		item.addActionListener(this);
		this.add(item);
		recallItem = item;

		item = new JMenuItem("Flip edge");
		item.addActionListener(this);
		this.add(item);
		flipItem = item;

		// Disable "recall" if label is in right position (or maybe instead if it is close enough?)
		if ((edge.getLabelBlock().getGeometry().getOffset().getX() == LabelBlockProxyShape.DEFAULTOFFSETX) &&
        (edge.getLabelBlock().getGeometry().getOffset().getY() == LabelBlockProxyShape.DEFAULTOFFSETY))
		{
			recallItem.setEnabled(false);
			recallItem.setToolTipText("Label is already in default position");
		}
		
		// Disable "flip" if startnode is a nodegroup
		if (edge.getSource() instanceof GroupNodeSubject)
		{
			flipItem.setEnabled(false);
			flipItem.setToolTipText("Can't make an edge end in a nodegroup");
		}
		// Disable "flip" if selfloop
		if (edge.getSource() == edge.getTarget())
		{
			flipItem.setEnabled(false);
			flipItem.setToolTipText("Selfloop");
		}
	}

	public void actionPerformed(ActionEvent e) 
	{
		if (e.getSource() == editEdgeItem)
		{
			EditorEditEdgeDialog.showDialog(edge);
			this.hide();
		}

		if (e.getSource() == deleteItem)
		{
			Command deleteEdge = new DeleteEdgeCommand(parent.getGraph(), edge);
			parent.getEditorInterface().getUndoInterface().executeCommand(deleteEdge);
			this.hide();
		}

		if (e.getSource() == recallItem)
		{
			System.out.println("Implement with Command Later");
			//parent.getLabelGroup(edge).setOffset(EditorLabelGroup.DEFAULTOFFSETX, EditorLabelGroup.DEFAULTOFFSETY);
		}

		if (e.getSource() == flipItem)
		{
			Command flipEdge = new FlipEdgeCommand(edge);
			parent.getEditorInterface().getUndoInterface().executeCommand(flipEdge);
		}

		parent.repaint();
	}
}
