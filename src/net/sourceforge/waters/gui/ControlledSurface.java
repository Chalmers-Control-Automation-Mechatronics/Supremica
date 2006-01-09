//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   ControlledSurface
//###########################################################################
//# $Id: ControlledSurface.java,v 1.48 2006-01-09 00:25:33 siw4 Exp $
//###########################################################################
 
package net.sourceforge.waters.gui;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import javax.swing.*;

import net.sourceforge.waters.gui.observer.Observer;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.observer.ToolbarChangedEvent;
import net.sourceforge.waters.gui.command.*;
import net.sourceforge.waters.model.expr.*;
import net.sourceforge.waters.model.module.*;
import net.sourceforge.waters.subject.module.BoxGeometrySubject;
import net.sourceforge.waters.subject.module.GroupNodeSubject;
import net.sourceforge.waters.subject.module.IdentifierSubject;
import net.sourceforge.waters.subject.module.PlainEventListSubject;
import net.sourceforge.waters.xsd.base.EventKind;


public class ControlledSurface
	extends EditorSurface
	implements Observer, KeyListener//,MouseMotionListener, MouseListener
{
    public String getCommand()
    {
		return mToolbar.getCommand();
    }

    public void setToolbar(ControlledToolbar t)
    {
		mToolbar = t;
    }

	public void setOptionsVisible(boolean v)
	{
		options.setVisible(v);
	}

	public boolean getNodesSnap()
	{
		return nodesSnap;
	}

	public void setNodesSnap(boolean n)
	{
		nodesSnap = n;
	}

	public boolean getControlPointsMove()
	{
		return controlPointsMove;
	}

	public void setControlPointsMove(boolean c)
	{
		controlPointsMove = c;
	}

	public void keyPressed(KeyEvent e)
	{
		if ((e.getKeyCode() == KeyEvent.VK_BACK_SPACE) || (e.getKeyCode() == KeyEvent.VK_DELETE))
		{
			deleteSelected();
		}
	}

 	public void keyTyped(KeyEvent e)
	{
		;
	}

	/** 
	 * Handle the key released event from the text field. 
	 */
	public void keyReleased(KeyEvent e)
	{
		;
	}

	/**
	 * Deletes all selected objects.
	 */
	public void deleteSelected()
	{
		while (selectedObjects.size() != 0)
		{
			EditorObject o = (EditorObject) selectedObjects.remove(0);
			if (o.getType() == EditorObject.NODE)
			{
			    Command deleteNode = new DeleteNodeCommand(this, (EditorNode) o);
			    root.getUndoInterface().executeCommand(deleteNode);
			}
			else if (o.getType() == EditorObject.EDGE)
			{
			    Command deleteEdge = new DeleteEdgeCommand(this, (EditorEdge) o);
			    root.getUndoInterface().executeCommand(deleteEdge);
			}
			else if (o.getType() == EditorObject.NODEGROUP)
			{
			    Command deleteNodeGroup = new DeleteNodeGroupCommand(this, (EditorNodeGroup) o);
			    root.getUndoInterface().executeCommand(deleteNodeGroup);			
			}
		}
	}

	private void select(EditorObject o)
	{
		if (!selectedObjects.contains(o))
		{
			selectedObjects.add(o);
			o.setSelected(true);

			// Select children
			LinkedList children = getChildren(o);
			while (children.size() != 0)
			{
				//((EditorObject) children.remove(0)).setSelected(true);
				select((EditorObject) children.remove(0));
			}
		}
	}

	private void unselect(EditorObject o)
	{
		if (selectedObjects.contains(o))
		{
			selectedObjects.remove(o);
			o.setSelected(false);

			// Unselect children
			LinkedList children = getChildren(o);
			while (children.size() != 0)
			{
				((EditorObject) children.remove(0)).setSelected(false);
			}
		}
	}

	public void unselectAll()
	{
		selectedObjects.clear();
		super.unselectAll();
	}

	/*
	private void selectChange(EditorObject o)
	{
		// Only if SELECT is chosen multiple selection is possible...
		if (!(T.getPlace() == EditorToolbar.SELECT))
		{
			unselectAll();
		}

		// If object is selected unselect
		if (selectedObjects.contains(o))
		{
			unselect(o);
		}
		else
		{
			select(o);
		}
	}
	*/

	private boolean nodeIsSelected()
	{
		for (Iterator it = selectedObjects.iterator(); it.hasNext(); )
		{
			if (((EditorObject) it.next()).getType() == EditorObject.NODE)
			{
				return true;
			}
		}

		return false;
	}

	private boolean nodeGroupIsSelected()
	{
		for (Iterator it = selectedObjects.iterator(); it.hasNext(); )
		{
			if (((EditorObject) it.next()).getType() == EditorObject.NODEGROUP)
			{
				return true;
			}
		}

		return false;
	}

	private boolean edgeIsSelected()
	{
		for (Iterator it = selectedObjects.iterator(); it.hasNext(); )
		{
			if (((EditorObject) it.next()).getType() == EditorObject.EDGE)
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * Returns the closest coordinate (works for both x and y) lying on the grid.
	 */
	private int findGrid(int x)
	{
		return (x+gridSize/2)/gridSize*gridSize;
	}

   	public void mousePressed(MouseEvent e)
	{
		// This is for triggering the popup 
		maybeShowPopup(e);

		if (e.getButton() == MouseEvent.BUTTON1)
		{
			lastX = e.getX();
			lastY = e.getY();

			EditorObject o = getObjectAtPosition(e.getX(), e.getY());
			if (o == null)
			{
				// Clicking on whitespace!

				// If control is down, we may select multiple things...
				if (!e.isControlDown())
				{
					unselectAll();
				}

				// If SELECT is active, this means that we're starting a drag-select...
				if (getCommand() == SELECT)
				{
					// Start of drag-select?
					dragStartX = e.getX();
					dragStartY = e.getY();
					dragNowX = dragStartX;
					dragNowY = dragStartY;
					dragSelect = true;
				}

				// If NODEGROUP is active, we're adding a new group!
				if (getCommand() == NODEGROUP)
				{
				    Collection<NodeProxy> ic = Collections.emptyList();       
				    // EditorNodeGroup nodeGroup;
					if (nodesSnap)
					{
					    BoxGeometrySubject g = new BoxGeometrySubject(new Rectangle2D.Double(findGrid(e.getX()),
													       findGrid(e.getY()), 0, 0));
					    // nodeGroup = addNodeGroup(findGrid(e.getX()), findGrid(e.getY()), 0, 0);
					    GroupNodeSubject n = new GroupNodeSubject("NodeGroup" + nodeGroups.size(),
										      new PlainEventListSubject(), ic, g);	
					    newGroup = new EditorNodeGroup(n);
					}
					else
					{
					    BoxGeometrySubject g = new BoxGeometrySubject(new Rectangle2D.Double(e.getX(),
														 e.getY(), 0, 0));
					    // nodeGroup = addNodeGroup(e.getX(), e.getY(), 0, 0);
					    GroupNodeSubject n = new GroupNodeSubject("NodeGroup" + nodeGroups.size(),
										      new PlainEventListSubject(), ic, g);   	
					    newGroup = new EditorNodeGroup(n);
					}
					select(newGroup);
				}
			}
			else
			{
				// Clicking on something!

				// Should we unselect the currently selected objects?
				if (!selectedObjects.contains(o))
				{
					// If control is down, we may select multiple things...
					if (!e.isControlDown())
					{
						unselectAll();
					}
				}

				// Select stuff!
				//selectChange(o);

				// Only if SELECT is chosen multiple selection is possible...
				if (!(getCommand() == SELECT))
				{
					unselectAll();
				}
				
				// If object is selected prepare to unselect (don't unselect if dragging, though!)
				if (selectedObjects.contains(o))
				{
					toBeDeselected.add(o);
				}
				else
				{
					select(o);
				}	   

				/*
				if (o.getType() == EditorObject.EDGE)
				{
					EditorEdge edge = (EditorEdge) o;
					if (edge.wasClicked(e.getX(), e.getY()))
					{
						return;
					}
				}
				*/
				
				// Find offset values if a label or group was clicked
				if (o.getType() == EditorObject.LABELGROUP)
				{
					xoff = e.getX() - ((EditorLabelGroup) o).getX();
					yoff = e.getY() - ((EditorLabelGroup) o).getY();
				}
				else if (o.getType() == EditorObject.LABEL)
				{
					xoff = e.getX() - ((EditorLabel) o).getX();
					yoff = e.getY() - ((EditorLabel) o).getY();
				}
				else if (o.getType() == EditorObject.NODEGROUP)
				{
					// We need this offset to place new edges
					xoff = e.getX() - ((EditorNodeGroup) o).getX();
					yoff = e.getY() - ((EditorNodeGroup) o).getY();
				}
			}
		}
		else if (e.getButton() == MouseEvent.BUTTON2)
		{
			System.out.println("Button 2!");
		}
		else if (e.getButton() == MouseEvent.BUTTON3)
		{

		}

		repaint(false);
	}

	private void maybeShowPopup(MouseEvent e)
	{
		if (e.isPopupTrigger())
		{
			EditorObject o = getObjectAtPosition(e.getX(), e.getY());
			if (!(o == null))
			{
				if (o.getType() == EditorObject.NODE)
				{
					EditorNode object = (EditorNode) o;

					EditorNodePopupMenu popup = new EditorNodePopupMenu(this, object);
					popup.show(this, e.getX(), e.getY());
				}
				if (o.getType() == EditorObject.NODEGROUP)
				{
					EditorNodeGroup object = (EditorNodeGroup) o;

					EditorNodeGroupPopupMenu popup = new EditorNodeGroupPopupMenu(this, object);
					popup.show(this, e.getX(), e.getY());
				}
				if (o.getType() == EditorObject.EDGE)
				{
					EditorEdge object = (EditorEdge) o;

					EditorEdgePopupMenu popup = new EditorEdgePopupMenu(this, object);
					popup.show(this, e.getX(), e.getY());
				}
			}
			else
			{
				EditorSurfacePopupMenu popup = new EditorSurfacePopupMenu(this, selectedObjects);
				popup.show(this, e.getX(), e.getY());
			}
		}
	}	
	
	public boolean isFocusable()
	{
		return true;
	}

	public void mouseDragged(MouseEvent e)
	{
		//if (e.getButton() == MouseEvent.BUTTON1) // Why not?
		{
			hasDragged = true;

			// If we're not in a nice mode, let's end it right here and right now
			if ((getCommand() != SELECT) &&
				(getCommand() != NODE) &&
				(getCommand() != EDGE) &&
				(getCommand() != NODEGROUP))
			{
				return;
			}

			// Find the distances that the mouse has dragged (for moving and stuff)
			int dx = 0;
			int dy = 0;
			// Are we using snap?
			if (getCommand() != EDGE && (nodeIsSelected() || nodeGroupIsSelected()) && nodesSnap) 
			{
				lastX = findGrid(lastX);
				lastY = findGrid(lastY);
				
				int currX = findGrid(e.getX());
				int currY = findGrid(e.getY());
				
				// If the first selected node or nodegroup is not correctly
				// aligned already, we need a modifier to get it on the
				// grid again...
				int modX = 0;
				int modY = 0;
				for (final EditorObject o : selectedObjects) 
				{
					if ((o.getType() == EditorObject.NODE) || (o.getType() == EditorObject.NODEGROUP))
					{
						modX = findGrid(o.getX()) - o.getX();
						modY = findGrid(o.getY()) - o.getY();
					}
				}

				dx = currX - lastX + modX;
				dy = currY - lastY + modY;
			}
			else 
			{
				dx = e.getX() - lastX;
				dy = e.getY() - lastY;
			}

			// Update position
			lastX += dx;
			lastY += dy;

			// DragSelect!
			if (dragSelect)
			{
				dragNowX = e.getX();
				dragNowY = e.getY();

				toBeSelected = getDragSelection();

				// Select all that should be selected...
				super.unselectAll();
				// These have been selected previously and should still be
				// selected no matter what
				for (final EditorObject o : selectedObjects) 
				{
					o.setSelected(true);
					LinkedList children = getChildren(o);
					while (children.size() != 0)
					{
						((EditorObject) children.remove(0)).setSelected(true);
					}
				}
				// These are in the current drag selection
				for (final EditorObject o : toBeSelected) 
				{
					o.setSelected(true);
					LinkedList children = getChildren(o);
					while (children.size() != 0)
					{
						((EditorObject) children.remove(0)).setSelected(true);
					}
				}

				repaint(false);

				return;
			}

			// Multiple selection?
			if (getCommand() == SELECT)
			{
				// Don't unselect! We're dragging!
				toBeDeselected.clear();

				// Drag all selected objects

				// No move?
				if ((dx == 0) && (dy == 0))
				{
					return;
				}

				// is this the start of the move or a continuation of it 
				if (!selectedObjects.isEmpty()) 
				{
				    if (move == null) 
					{
						move = new MoveObjects(this, selectedObjects, new Point2D.Double(dx, dy));
				    } 
					else 
					{
						Point2D p = move.getDisplacement();
						p.setLocation(p.getX() + dx, p.getY() + dy);
						move.setDisplacement(p);
				    }
				}

				/* // Code for preventing nodes from ending up in the same place (but we allow that now)
				  for (int i = 0; i < nodes.size(); i++)
				  {
				  if (((EditorNode) nodes.get(i)).getPosition().distance(dx, dy) < selectedNode.getWidth() && ((EditorNode) nodes.get(i)) != selectedNode)
				  {
				  return;
				  }
				  }

				  for (int i = 0; i < nodeGroups.size(); i++)
				  {
				  if (((EditorNodeGroup) nodeGroups.get(i)).getBounds().intersects(new Rectangle(dx - EditorNode.WIDTH / 2, dy - EditorNode.WIDTH / 2, EditorNode.WIDTH, EditorNode.WIDTH)) &&!((EditorNodeGroup) nodeGroups.get(i)).getBounds().contains(new Rectangle(dx - EditorNode.WIDTH / 2, dy - EditorNode.WIDTH / 2, EditorNode.WIDTH, EditorNode.WIDTH)))
				  {
				  return;
				  }
				  }
				*/

				// Move every selected object!
				for (Iterator it = selectedObjects.iterator(); it.hasNext(); )
				{
					EditorObject object = (EditorObject) it.next();

					// Is it a node?
					if (object.getType() == EditorObject.NODE)
					{
						EditorNode node = (EditorNode) object;

						// Where did it use to be?
						int oldx = node.getX();
						int oldy = node.getY();

						// Move
						node.setPosition(oldx + dx, oldy + dy);
					}
					// Is it a nodegroup?
					else if (object.getType() == EditorObject.NODEGROUP)
					{
						EditorNodeGroup nodeGroup = (EditorNodeGroup) object;

						//Rectangle2D.Double b = new Rectangle2D.Double();
						//b.setRect(nodeGroup.getBounds());

						if (nodeGroup.getResizing() && (selectedObjects.size() == 1))
						{
							if (nodesSnap)
							{
								nodeGroup.resize(findGrid(e.getX()), findGrid(e.getY()));
							}
							else
							{
								nodeGroup.resize(e.getX(), e.getY());
							}
						}
						else
						{
							/*
							if (nodesSnap)
							{
								nodeGroup.moveGroup(Math.round((e.getX() - xoff) / gridSize) * gridSize, Math.round((e.getY() - yoff) / gridSize) * gridSize);
							}
							else
							{
								nodeGroup.moveGroup(e.getX() - xoff, e.getY() - yoff);
							}
							*/
						    nodeGroup.setPosition(nodeGroup.getX()+ dx, nodeGroup.getY() + dy);
	    
						}

						/* // Prevent nodegroups from moving onto nodes?
						   if (intersectsRectangle(nodeGroup.getBounds()))
						   {
						   nodeGroup.setBounds(b);
						   }
						*/

						/*
						EditorNodeGroup nodeGroup = (EditorNodeGroup) object;
						nodeGroup.moveGroup(nodeGroup.getX() + dx, nodeGroup.getY() + dy);
						*/
					}
					// Is it an edge?
					else if ((object.getType() == EditorObject.EDGE) && (!controlPointsMove || !(nodeIsSelected() || nodeGroupIsSelected())))
					{
						EditorEdge edge = (EditorEdge) object;
  						edge.setPosition(edge.getTPointX() + dx, edge.getTPointY() + dy);
					}					

					// DONT MOVE LABELS IN MULTI MODE, (IT'S NO FUN TO TRY TO GET THE OFFSETS RIGHT...)
					if (selectedObjects.size() == 1)
					{
						// Is it a label?
						if (object.getType() == EditorObject.LABEL) // Don't move!
						{
							EditorLabel label = (EditorLabel) object;
							//label.moveTo(label.getX() + dx, label.getY() + dy);
							//label.setX(label.getX() + dx);
							//label.setY(label.getY() + dy);
							label.setOffset(label.getOffsetX() + dx, label.getOffsetY() + dy);
						}
						// Is it a labelgroup?
						else if (object.getType() == EditorObject.LABELGROUP) // Don't move
						{
							EditorLabelGroup labelGroup = (EditorLabelGroup) object;
							
							//labelGroup.moveTo(labelGroup.getX() + dx, labelGroup.getY() + dy);
							//labelGroup.setX(labelGroup.getX() + dx);
							//labelGroup.setY(labelGroup.getY() + dy);
							labelGroup.setOffset(labelGroup.getOffsetX() + dx, labelGroup.getOffsetY() + dy);
						}
					}
				}

				examineCollisions();
			}
			else
			{
				// Single selection! (Multiple selection is only allowed in SELECT-mode.)
				
				// Edge drawing...
				if (getCommand() == EDGE )
				{
				    // No move?
				    if ((dx == 0) && (dy == 0))
				    {
						return;
				    }
				    // is this the start of the move or a continuation of it
				    if (!selectedObjects.isEmpty()) {
						if (move == null) {
							move = new MoveObjects(this, selectedObjects, new Point2D.Double(dx, dy));
						} else {
							Point2D p = move.getDisplacement();
							p.setLocation(p.getX() + dx, p.getY() + dy);
							move.setDisplacement(p);
						}
				    }
				    // There should only be one object here, or maybe two, 
					// a node and it's label or an edge and it's labelgroup...
					// Let's make it an iterator anyway!
					for (final EditorObject object : selectedObjects) {
						if (object.getType() == EditorObject.EDGE) {
							// If an edge is selected, you can drag!
							assert(selectedObjects.size() <= 2);
							EditorEdge edge = (EditorEdge) object;
							if (edge.getDragS()) {
								edge.setSource(e.getX(), e.getY());
							} else if (edge.getDragT())	{
								edge.setTarget(e.getX(), e.getY());
							} else {
								edge.setPosition(edge.getTPointX() + dx, edge.getTPointY() + dy);
							}
						} else if (object.getType() == EditorObject.NODE) {
							// If clicking on a node or nodegroup, draw a
							// new edge!
							assert(selectedObjects.size() <= 2);
							EditorNode node = (EditorNode) object;
							int[] dat = {node.getX(), node.getY(),
										 e.getX(), e.getY()};
							// Draw line!
							if ((lines != null) && (lines.size() > 0))
							{
								lines.set(0, dat);
							}
							else
							{
								lines.add(dat);
							}
						} else if (object.getType() == EditorObject.NODEGROUP) {
							assert(selectedObjects.size() == 1);
							EditorNodeGroup nodeGroup = (EditorNodeGroup) object;
							// Find point on the border of the group from where the line is drawn...
							Point2D p = nodeGroup.setOnBounds(nodeGroup.getX() + xoff, nodeGroup.getY() + yoff);
							int[] dat = {(int) p.getX(), (int) p.getY(), e.getX(), e.getY()};
							// Draw line!
							if ((lines != null) && (lines.size() > 0))
							{
								lines.set(0, dat);
							}
							else
							{
								lines.add(dat);
							}
						}
					}
				
					// Update highlighting!
					//updateHighlighting(e);
				}

				// Are we resizing a nodegroup?
				if (nodeGroupIsSelected() && ((getCommand() == NODEGROUP) || (getCommand() == SELECT)))
				{
					EditorNodeGroup nodeGroup = (EditorNodeGroup) selectedObjects.get(0);

					//Rectangle2D.Double b = new Rectangle2D.Double();
					//b.setRect(nodeGroup.getBounds());

					if (nodeGroup.getResizing())
					{
						if (nodesSnap)
						{
							nodeGroup.resize(findGrid(e.getX()), findGrid(e.getY()));
						}
						else
						{
							nodeGroup.resize(e.getX(), e.getY());
						}
					}
					else
					{
						/*
						if (nodesSnap)
						{
							nodeGroup.moveGroup(Math.round((e.getX() - xoff) / gridSize) * gridSize, Math.round((e.getY() - yoff) / gridSize) * gridSize);
						}
						else
						{
							nodeGroup.moveGroup(e.getX() - xoff, e.getY() - yoff);
						}
						*/
					    nodeGroup.setPosition(nodeGroup.getX()+ dx, nodeGroup.getY() + dy);
					}
					
					/* // Prevent nodegroups from moving onto nodes?
					if (intersectsRectangle(nodeGroup.getBounds()))
					{
						nodeGroup.setBounds(b);
					}
					*/
					
					examineCollisions();
				}
			}
		}

		repaint(false);
	}
	
	/**
	 * Updates highlighting based on the current location of the mouse pointer.
	 */
	private void updateHighlighting(Point2D e)
	{
		boolean needRepaint = false;

		// Highlight things that are moved over...
		EditorObject o = getObjectAtPosition((int)e.getX(), (int)e.getY());

		// Unhighlight highligted stuff not in focus
		if ((highlightedObject != null) && !highlightedObject.equals(o))
		{
			// Unhighlight children
			LinkedList children = getChildren(highlightedObject);
			while (children.size() != 0)
			{
				((EditorObject) children.remove(0)).setHighlighted(false);
			}

			// Unhighlight object
			highlightedObject.setHighlighted(false);
			highlightedObject = null;

			needRepaint = true;
		}

		// Highlight stuff in focus!
		if (o != null && !o.equals(highlightedObject))
		{
			// Highlight object
			highlightedObject = o;
			highlightedObject.setHighlighted(true);

			// Highlight children
			LinkedList children = getChildren(highlightedObject);
			while (children.size() != 0)  
			{
				((EditorObject) children.remove(0)).setHighlighted(true);
			}

			needRepaint = true;
		}

		// Need repaint?
		if (needRepaint)
		{
			repaint(false);
		}
    }

	public void mouseEntered(MouseEvent e)
	{
		;
	}

	public void mouseExited(MouseEvent e)
	{
		;
	}

	public void mouseClicked(MouseEvent e)
	{
		this.requestFocusInWindow();

		if (e.getButton() == MouseEvent.BUTTON1)
		{
			// Singleclick?
			if (e.getClickCount() == 1)
			{	
				// What was clicked?
				EditorObject o = getObjectAtPosition(e.getX(), e.getY());
				
				// Should we add a new node?
				if (getCommand() == NODE)
				{
					int posX;
					int posY;
					
					if (nodesSnap)
					{
						posX = findGrid(e.getX());
						posY = findGrid(e.getY());
					}
					else
					{
						posX = e.getX();
						posY = e.getY();
					}
					
					// Make sure there isn't already a node there!
					if (o == null || o.getType() != EditorObject.NODE)
					{
						Command createNode = new CreateNodeCommand(this, posX, posY);
						root.getUndoInterface().executeCommand(createNode);
						//addLabel(getLastNode(), "", 0, break20);
						
						//SimpleNodeProxy np = new SimpleNodeProxy("s" + nodes.size());
						//PointGeometryProxy gp = new PointGeometryProxy(posX,posY);
						//np.setPointGeometry(gp);
						//graph.getNodes().add(np);
					}
				}
				
				/** Nonsense?
					else if (T.getPlace() == EditorToolbar.EDGE)
					{
					if (o == null)
					{
					unselectAll();
					
					return;
					}
				*/
				
				// Set clicked node to initial
				if (getCommand() == INITIAL)
				{					
					if (o == null || o.getType() != EditorObject.NODE)
					{
						return;
					}
					
					EditorNode n = (EditorNode) o;
					unsetAllInitial();
					n.setInitial(true);
				}
	
				// Special stuff for labelgroup clicks? (This is not working properly!)
				if (getCommand() == SELECT && edgeIsSelected()) 
				{
					if (o == null || o.getType() != EditorObject.LABELGROUP)
					{
						return;
					}					

					EditorLabelGroup l = (EditorLabelGroup) o;
					l.setSelected(true);
					l.setSelectedLabel(e.getX(), e.getY());
				}
			}				
			// Doubleclick?
			else if (e.getClickCount() == 2)
			{
				// Change names on double clicking a label, change order when clicking labelgroup!
				if (getCommand() == SELECT)
				{
					/* What's this? No comments?
					   if (selectedNode != null)
					   {
					   if (selectedNode.getPropGroup().wasClicked(e.getX(), e.getY()) && selectedNode.getPropGroup().getVisible())
					   {
					   EditorPropGroup p = selectedNode.getPropGroup();
					   
					   p.setSelectedLabel(e.getX(), e.getY());
					   repaint();
					   }
					   }
					*/
					
					// What was clicked? 
					EditorObject o = getObjectAtPosition(e.getX(), e.getY());
					if (o != null)
					{
						if (o.getType() == EditorObject.LABEL)
						{
							EditorLabel l = (EditorLabel) o;
							
							l.setEditing(true);
						}
						else if (o.getType() == EditorObject.LABELGROUP)
						{
							EditorLabelGroup l = (EditorLabelGroup) o;
							
							l.setSelectedLabel(e.getX(), e.getY());
						}
						else if (o.getType() == EditorObject.NODE)
						{
							EditorNode n = (EditorNode) o;
							
							n.getPropGroup().setVisible(true);
						}
					}
				}
			}
		}
		
		// Repaint is done when you release the mouse button? (But that's before the click?)
		repaint();
	}

	public void createOptions(EditorWindowInterface root)
	{
		options = new EditorOptions(root);
	}

	/*
	public EditorLabelGroup getSelectedLabelGroup()
	{
		if (selectedEdge != null)
		{
			for (int i = 0; i < events.size(); i++)
			{
				if (((EditorLabelGroup) events.get(i)).getParent() == selectedEdge)
				{
					return ((EditorLabelGroup) events.get(i));
				}
			}
		}

		return selectedLabelGroup;
	}

	public EditorNode getSelectedNode()
	{
		return selectedNode;
	}
	*/

	public ControlledSurface(EditorWindowInterface r, ControlledToolbar t)
	{
		S = this;
		root = r;
		mToolbar = t;
		t.attach(this);
		this.addMouseListener(new SelectListener());
		this.addMouseMotionListener(new SelectListener());
		this.addKeyListener(this);
		this.requestFocusInWindow();
		dtListener = new DTListener();
		dropTarget = new DropTarget(this, dtListener);
	}


    private class DTListener extends DropTargetAdapter
    {
		//###################################################################
		//# Interface java.awt.dnd.DropTargetAdapter
		private void setDragOver(EditorObject o, int d)
		{
			if (o == null) {
				return;
			}
			o.setDragOver(d);
			if (o instanceof EditorNode) {
				for (Object ob : labels) {
					EditorLabel l = (EditorLabel)ob;
					if (l.getParent() == o) {
						l.setDragOver(d);
					}
				}	       
			} else if (o instanceof EditorLabel) {
				EditorLabel l = (EditorLabel)o;
				if (l.getParent() != null) {
					l.getParent().setDragOver(d);
				}
			} else if (o instanceof EditorEdge) {
				for (Object ob : events) {
					EditorLabelGroup l = (EditorLabelGroup)ob;
					if (l.getParent() == o) {
						l.setDragOver(d);
					}
				}
			} else if (o instanceof EditorLabelGroup) {
				EditorLabelGroup l = (EditorLabelGroup)o;
				if (l.getParent() != null) {
					l.getParent().setDragOver(d);
				}
			}
		}
		
		public void dragOver(final DropTargetDragEvent e)
		{
			int operation = DnDConstants.ACTION_MOVE;
			EditorObject o = null;
			if (mDrag != null) {
				setDragOver(mDrag, EditorObject.NOTDRAG);
			}
			mDrag = null;
			try {
				final IdentifierWithKind i = (IdentifierWithKind)
					e.getTransferable().getTransferData(FLAVOUR);
				final EventKind ek = i.getKind();
				final IdentifierSubject ip = i.getIdentifier();
				o = getObjectAtPosition((int) e.getLocation().getX(),
										(int) e.getLocation().getY());
				mDrag = o;
				if (ek == null) {
					if (o instanceof EditorNode) {
						operation = DnDConstants.ACTION_COPY;
					} else if (o instanceof EditorEdge) {
						operation = DnDConstants.ACTION_COPY;
					} else if (o instanceof EditorLabelGroup) {
						operation = DnDConstants.ACTION_COPY;
					} else if (o instanceof EditorLabel) {
						operation = DnDConstants.ACTION_COPY;
					}
				} else if (ek.equals(EventKind.PROPOSITION)) {
					if (o instanceof EditorNode) {
						operation = DnDConstants.ACTION_COPY;
					} else if (o instanceof EditorLabel) {
						operation = DnDConstants.ACTION_COPY;
					}
				} else if (ek.equals(EventKind.CONTROLLABLE) ||
						   ek.equals(EventKind.UNCONTROLLABLE)) {
					if (o instanceof EditorEdge) {
						operation = DnDConstants.ACTION_COPY;
					} else if (o instanceof EditorLabelGroup) {
						operation = DnDConstants.ACTION_COPY;
					}
				}
			} catch (final UnsupportedFlavorException exception) {
				throw new IllegalArgumentException(exception);
			} catch (final IOException exception) {
				throw new IllegalArgumentException(exception);
			}
			if (o != null) {
				if (operation == DnDConstants.ACTION_COPY) {
					setDragOver(o, EditorObject.CANDROP);
				} else {
					setDragOver(o, EditorObject.CANTDROP);
				}
			}
			updateHighlighting(e.getLocation());   
			e.getDropTargetContext().getDropTarget().setDefaultActions(operation);
			e.acceptDrag(operation);
		}

		public void drop(final DropTargetDropEvent e)
		{
			try {
				if (e.getDropTargetContext().getDropTarget().
					getDefaultActions() == DnDConstants.ACTION_COPY) {
					final IdentifierWithKind i = (IdentifierWithKind)
						e.getTransferable().getTransferData(FLAVOUR);
					final IdentifierSubject ip = i.getIdentifier();
					final EditorObject o =
						getObjectAtPosition((int) e.getLocation().getX(),
											(int) e.getLocation().getY());
					setDragOver(o, EditorObject.NOTDRAG);
					if (o instanceof EditorNode) {
						addToNode((EditorNode) o, ip);
						e.dropComplete(true);
						return;
					} else if (o instanceof EditorEdge) {
						addToEdge((EditorEdge) o, ip);
						e.dropComplete(true);
						return;
					} else if (o instanceof EditorLabelGroup) {
						addToLabelGroup((EditorLabelGroup) o, ip);
						e.dropComplete(true);
						return;
					} else if (o instanceof EditorLabel) {
						addToLabel((EditorLabel) o, ip);
						e.dropComplete(true);
						return;
					}
				}
			} catch (final UnsupportedFlavorException exception) {
				throw new IllegalArgumentException(exception);
			} catch (final IOException exception) {
				throw new IllegalArgumentException(exception);
			}
			e.dropComplete(false);
		}


		//###################################################################
		//# Auxiliary Methods
		private void addToNode(EditorNode n, IdentifierSubject i)
		{
			final IdentifierSubject cloned = i.clone();
			Command addEvent = new AddEventCommand(n.getSubject().getPropositions(),
												  cloned, 0);
			root.getUndoInterface().executeCommand(addEvent);
			repaint();
		}

		private void addToLabel(EditorLabel l, IdentifierSubject i)
		{
			addToNode(l.getParent(), i);
		}

		private void addToEdge(EditorEdge e, IdentifierSubject ip)
		{
			for (int i = 0; i < events.size(); i++)	{
				EditorLabelGroup g = (EditorLabelGroup) events.get(i);
				if (g.getParent() == (EditorObject) e) {
					addToLabelGroup(g, ip);	
				}
			}
		}

		private void addToLabelGroup(EditorLabelGroup l, IdentifierSubject i)
		{
			final IdentifierSubject cloned = i.clone();
			Command addEvent = new AddEventCommand(l.getSubject(),
												  cloned, 0);
			root.getUndoInterface().executeCommand(addEvent);
			repaint();
		}


		//###################################################################
		//# Data Members
		private EditorObject mDrag = null;

	}
	
	public void update(EditorChangedEvent e)
	{
		if (e instanceof ToolbarChangedEvent)
		{
			MouseListener[] mouse = getMouseListeners();
			MouseMotionListener[] motion = getMouseMotionListeners();
			for (int i = 0; i < mouse.length; i++)
			{
				removeMouseListener(mouse[i]);
			}
			for (int i = 0; i < motion.length; i++)
			{
				removeMouseMotionListener(motion[i]);
			}
			String c = getCommand();
			if (c == SELECT)
			{
				addMouseListener(new SelectListener());
				addMouseMotionListener(new SelectListener());
			}
			if (c == EDGE)
			{
				addMouseListener(new EdgeListener());
				addMouseMotionListener(new EdgeListener());
			}
			if (c == NODEGROUP)
			{
				addMouseListener(new GroupNodeListener());
				addMouseMotionListener(new GroupNodeListener());
			}
			if (c == NODE)
			{
				addMouseListener(new NodeListener());
				addMouseMotionListener(new NodeListener());
			}
			if (c == INITIAL)
			{
				addMouseListener(new InitialListener());
				addMouseMotionListener(new InitialListener());
			}
		}
	}
	
	private class SelectListener
		extends MouseAdapter
		implements MouseMotionListener
	{
		
		public void mouseClicked(MouseEvent e)
		{
			requestFocusInWindow();

			if (e.getButton() == MouseEvent.BUTTON1)
			{
				// Singleclick?
				if (e.getClickCount() == 1)
				{	
					// What was clicked?
					EditorObject o = getObjectAtPosition(e.getX(), e.getY());
					
					// Special stuff for labelgroup clicks? (This is not working properly!)
					if (getCommand() == SELECT && edgeIsSelected()) 
					{
						if (o == null || o.getType() != EditorObject.LABELGROUP)
						{
							return;
						}					
	
						EditorLabelGroup l = (EditorLabelGroup) o;
						l.setSelected(true);
						l.setSelectedLabel(e.getX(), e.getY());
					}
				}				
				// Doubleclick?
				else if (e.getClickCount() == 2)
				{
					// Change names on double clicking a label, change order when clicking labelgroup!
					/* What's this? No comments?
					   if (selectedNode != null)
					   {
					   if (selectedNode.getPropGroup().wasClicked(e.getX(), e.getY()) && selectedNode.getPropGroup().getVisible())
					   {
					   EditorPropGroup p = selectedNode.getPropGroup();
					   
					   p.setSelectedLabel(e.getX(), e.getY());
					   repaint();
					   }
					   }
					*/
					
					// What was clicked? 
					EditorObject o = getObjectAtPosition(e.getX(), e.getY());
					if (o != null)
					{
						if (o.getType() == EditorObject.LABEL)
						{
							EditorLabel l = (EditorLabel) o;
							
							l.setEditing(true);
						}
						else if (o.getType() == EditorObject.LABELGROUP)
						{
							EditorLabelGroup l = (EditorLabelGroup) o;
							
							l.setSelectedLabel(e.getX(), e.getY());
						}
						else if (o.getType() == EditorObject.NODE)
						{
							EditorNode n = (EditorNode) o;
							
							n.getPropGroup().setVisible(true);
						}
					}
				}
			}
		}
		
		public void mousePressed(MouseEvent e)
		{
			// This is for triggering the popup 
			maybeShowPopup(e);
	
			if (e.getButton() == MouseEvent.BUTTON1)
			{
				lastX = e.getX();
				lastY = e.getY();
	
				EditorObject o = getObjectAtPosition(e.getX(), e.getY());
				if (o == null)
				{
					// Clicking on whitespace!
	
					// If control is down, we may select multiple things...
					if (!e.isControlDown())
					{
						unselectAll();
					}
	
					// If SELECT is active, this means that we're starting a drag-select...
					// Start of drag-select?
					dragStartX = e.getX();
					dragStartY = e.getY();
					dragNowX = dragStartX;
					dragNowY = dragStartY;
					dragSelect = true;
				}
				else
				{
					// Clicking on something!
	
					// Should we unselect the currently selected objects?
					if (!selectedObjects.contains(o))
					{
						// If control is down, we may select multiple things...
						if (!e.isControlDown())
						{
							unselectAll();
						}
					}
	
					// Select stuff!
					//selectChange(o);						
					
					// If object is selected prepare to unselect (don't unselect if dragging, though!)
					if (selectedObjects.contains(o))
					{
						toBeDeselected.add(o);
					}
					else
					{
						select(o);
					}	   
	
					/*
					if (o.getType() == EditorObject.EDGE)
					{
						EditorEdge edge = (EditorEdge) o;
						if (edge.wasClicked(e.getX(), e.getY()))
						{
							return;
						}
					}
					*/
					
					// Find offset values if a label or group was clicked
					if (o.getType() == EditorObject.LABELGROUP)
					{
						xoff = e.getX() - ((EditorLabelGroup) o).getX();
						yoff = e.getY() - ((EditorLabelGroup) o).getY();
					}
					else if (o.getType() == EditorObject.LABEL)
					{
						xoff = e.getX() - ((EditorLabel) o).getX();
						yoff = e.getY() - ((EditorLabel) o).getY();
					}
					else if (o.getType() == EditorObject.NODEGROUP)
					{
						// We need this offset to place new edges
						xoff = e.getX() - ((EditorNodeGroup) o).getX();
						yoff = e.getY() - ((EditorNodeGroup) o).getY();
					}
				}
			}
			else if (e.getButton() == MouseEvent.BUTTON2)
			{
				EditorObject o = getObjectAtPosition(e.getX(), e.getY());
				if (o != null)
				{
					if (o.getType() == EditorObject.NODE)
					{
						EditorNode n = (EditorNode) o;
						
						n.getPropGroup().setVisible(true);
					}
				}
			}
			else if (e.getButton() == MouseEvent.BUTTON3)
			{
	
			}
	
			repaint(false);
		}
		
		public void mouseReleased(MouseEvent e)
		{
			if (move != null) {
				if (move.getDisplacement().distance(0,0) != 0) {
					root.getUndoInterface().addUndoable(move);
				}
				move = null;
			}
	
			// This is for triggering the popup 
			maybeShowPopup(e);
	
			// Make the temporary selection definite
			while (toBeSelected.size() > 0)
			{
				select((EditorObject) toBeSelected.remove(0));
			}
			while (toBeDeselected.size() > 0)
			{
				unselect((EditorObject) toBeDeselected.remove(0));
			}
	
			if (e.getButton() == MouseEvent.BUTTON1)
			{
				// Stop resizing nodegroup
				if (nodeGroupIsSelected() && (selectedObjects.size() == 1) && newGroup == null)
				{
					EditorNodeGroup ng = (EditorNodeGroup) selectedObjects.get(0);
					ng.setResizingFalse();
	
					if (ng.isEmpty())
					{
						Command c = new DeleteNodeGroupCommand(ControlledSurface.this, ng);
						root.getUndoInterface().executeCommand(c);				 
					}
				}
				if (newGroup != null) {
					newGroup.setResizingFalse();
					Command c = new CreateNodeGroupCommand(ControlledSurface.this, newGroup);
					newGroup = null;
					root.getUndoInterface().executeCommand(c);
				}
	
				// Redefine edge if it has been changed
				if (edgeIsSelected() && (selectedObjects.size() <= 2))
				{
					EditorObject obj = (EditorObject) selectedObjects.get(0);
					EditorEdge edge;
					if (obj.getType() == EditorObject.EDGE)
					{
						edge = (EditorEdge) obj;
					}
					else
					{
						edge = (EditorEdge) selectedObjects.get(1);
					}

					EditorObject n = getNodeOrNodeGroupAtPosition(e.getX(), e.getY());

					if (n != null)
					{
						if (n.getType() == EditorObject.NODE)
						{
							if (edge.getDragT() && (n != edge.getEndNode()))
							{
								edge.setEndNode((EditorNode) n);
							}
							else if (edge.getDragS() && (n != edge.getStartNode()))
							{
								edge.setStartNode(n, e.getX(), e.getY());
							}
						}
						else if (n.getType() == EditorObject.NODEGROUP)
						{
							if (edge.getDragS())
							{
								edge.setStartNode(n, e.getX(), e.getY());
							}
						}
					}

					edge.setDragT(false);
					edge.setDragS(false);
					edge.setDragC(false);
					//edge.setTPoint(edge.getTPointX(), edge.getTPointY());
				}
	
				lines.clear();
			}
	
			repaint(hasDragged);
	
			dragSelect = false;
			hasDragged = false;
		}
		
		public void mouseDragged(MouseEvent e)
		{
			//if (e.getButton() == MouseEvent.BUTTON1) // Why not?
			{
				hasDragged = true;
				
				// Find the distances that the mouse has dragged (for moving and stuff)
				int dx = 0;
				int dy = 0;
				// Are we using snap?
				if ((nodeIsSelected() || nodeGroupIsSelected()) && nodesSnap) 
				{
					lastX = findGrid(lastX);
					lastY = findGrid(lastY);
					
					int currX = findGrid(e.getX());
					int currY = findGrid(e.getY());
					
					// If the first selected node or nodegroup is not correctly
					// aligned already, we need a modifier to get it on the
					// grid again...
					int modX = 0;
					int modY = 0;
					for (final EditorObject o : selectedObjects) 
					{
						if ((o.getType() == EditorObject.NODE) || (o.getType() == EditorObject.NODEGROUP))
						{
							modX = findGrid(o.getX()) - o.getX();
							modY = findGrid(o.getY()) - o.getY();
						}
					}
	
					dx = currX - lastX + modX;
					dy = currY - lastY + modY;
				}
				else 
				{
					dx = e.getX() - lastX;
					dy = e.getY() - lastY;
				}
	
				// Update position
				lastX += dx;
				lastY += dy;
	
				// DragSelect!
				if (dragSelect)
				{
					dragNowX = e.getX();
					dragNowY = e.getY();
	
					toBeSelected = getDragSelection();
	
					// Select all that should be selected...
					ControlledSurface.super.unselectAll();
					// These have been selected previously and should still be
					// selected no matter what
					for (final EditorObject o : selectedObjects) 
					{
						o.setSelected(true);
						LinkedList children = getChildren(o);
						while (children.size() != 0)
						{
							((EditorObject) children.remove(0)).setSelected(true);
						}
					}
					// These are in the current drag selection
					for (final EditorObject o : toBeSelected) 
					{
						o.setSelected(true);
						LinkedList children = getChildren(o);
						while (children.size() != 0)
						{
							((EditorObject) children.remove(0)).setSelected(true);
						}
					}
	
					repaint(false);
	
					return;
				}
	
				// Multiple selection?
			
				// Don't unselect! We're dragging!
				toBeDeselected.clear();

				// Drag all selected objects

				// No move?
				if ((dx == 0) && (dy == 0))
				{
					return;
				}

				// is this the start of the move or a continuation of it 
				if (!selectedObjects.isEmpty()) 
				{
					if (move == null) 
					{
						move = new MoveObjects(ControlledSurface.this, selectedObjects, new Point2D.Double(dx, dy));
					} 
					else 
					{
						Point2D p = move.getDisplacement();
						p.setLocation(p.getX() + dx, p.getY() + dy);
						move.setDisplacement(p);
					}
				}

				/* // Code for preventing nodes from ending up in the same place (but we allow that now)
				  for (int i = 0; i < nodes.size(); i++)
				  {
				  if (((EditorNode) nodes.get(i)).getPosition().distance(dx, dy) < selectedNode.getWidth() && ((EditorNode) nodes.get(i)) != selectedNode)
				  {
				  return;
				  }
				  }

				  for (int i = 0; i < nodeGroups.size(); i++)
				  {
				  if (((EditorNodeGroup) nodeGroups.get(i)).getBounds().intersects(new Rectangle(dx - EditorNode.WIDTH / 2, dy - EditorNode.WIDTH / 2, EditorNode.WIDTH, EditorNode.WIDTH)) &&!((EditorNodeGroup) nodeGroups.get(i)).getBounds().contains(new Rectangle(dx - EditorNode.WIDTH / 2, dy - EditorNode.WIDTH / 2, EditorNode.WIDTH, EditorNode.WIDTH)))
				  {
				  return;
				  }
				  }
				*/

				// Move every selected object!
				for (Iterator it = selectedObjects.iterator(); it.hasNext(); )
				{
					EditorObject object = (EditorObject) it.next();

					// Is it a node?
					if (object.getType() == EditorObject.NODE)
					{
						EditorNode node = (EditorNode) object;

						// Where did it use to be?
						int oldx = node.getX();
						int oldy = node.getY();

						// Move
						node.setPosition(oldx + dx, oldy + dy);
					}
					// Is it a nodegroup?
					else if (object.getType() == EditorObject.NODEGROUP)
					{
						EditorNodeGroup nodeGroup = (EditorNodeGroup) object;

						//Rectangle2D.Double b = new Rectangle2D.Double();
						//b.setRect(nodeGroup.getBounds());

						if (nodeGroup.getResizing() && (selectedObjects.size() == 1))
						{
							if (nodesSnap)
							{
								nodeGroup.resize(findGrid(e.getX()), findGrid(e.getY()));
							}
							else
							{
								nodeGroup.resize(e.getX(), e.getY());
							}
						}
						else
						{
							/*
							if (nodesSnap)
							{
								nodeGroup.moveGroup(Math.round((e.getX() - xoff) / gridSize) * gridSize, Math.round((e.getY() - yoff) / gridSize) * gridSize);
							}
							else
							{
								nodeGroup.moveGroup(e.getX() - xoff, e.getY() - yoff);
							}
							*/
							nodeGroup.setPosition(nodeGroup.getX()+ dx, nodeGroup.getY() + dy);
		
						}

						/* // Prevent nodegroups from moving onto nodes?
						   if (intersectsRectangle(nodeGroup.getBounds()))
						   {
						   nodeGroup.setBounds(b);
						   }
						*/

						/*
						EditorNodeGroup nodeGroup = (EditorNodeGroup) object;
						nodeGroup.moveGroup(nodeGroup.getX() + dx, nodeGroup.getY() + dy);
						*/
					}
					// Is it an edge?
					else if ((object.getType() == EditorObject.EDGE) && (!controlPointsMove || !(nodeIsSelected() || nodeGroupIsSelected())))
					{
						EditorEdge edge = (EditorEdge) object;
						if (edge.getDragS())
						{
							edge.setSource(e.getX(), e.getY());
						}
						else if (edge.getDragT())
						{
							edge.setTarget(e.getX(), e.getY());
						}
						else
						{
							edge.setPosition(edge.getTPointX() + dx, edge.getTPointY() + dy);
						}
					}					

					// DONT MOVE LABELS IN MULTI MODE, (IT'S NO FUN TO TRY TO GET THE OFFSETS RIGHT...)
					if (selectedObjects.size() == 1)
					{
						// Is it a label?
						if (object.getType() == EditorObject.LABEL) // Don't move!
						{
							EditorLabel label = (EditorLabel) object;
							//label.moveTo(label.getX() + dx, label.getY() + dy);
							//label.setX(label.getX() + dx);
							//label.setY(label.getY() + dy);
							label.setOffset(label.getOffsetX() + dx, label.getOffsetY() + dy);
						}
						// Is it a labelgroup?
						else if (object.getType() == EditorObject.LABELGROUP) // Don't move
						{
							EditorLabelGroup labelGroup = (EditorLabelGroup) object;
							
							//labelGroup.moveTo(labelGroup.getX() + dx, labelGroup.getY() + dy);
							//labelGroup.setX(labelGroup.getX() + dx);
							//labelGroup.setY(labelGroup.getY() + dy);
							labelGroup.setOffset(labelGroup.getOffsetX() + dx, labelGroup.getOffsetY() + dy);
						}
					}
				}

				examineCollisions();
				
			}
	
			repaint(false);
		}
		
		public void mouseMoved(MouseEvent e)
		{
			updateHighlighting(e.getPoint());
		}
		
	}
	
	private class NodeListener
		extends MouseAdapter
		implements MouseMotionListener
	{
		
		public void mouseClicked(MouseEvent e)
		{
			requestFocusInWindow();
	
			if (e.getButton() == MouseEvent.BUTTON1)
			{
				// Singleclick?
				if (e.getClickCount() == 1)
				{	
					// What was clicked?
					//EditorObject o = getObjectAtPosition(e.getX(), e.getY());
					
					// Should we add a new node?
										
					/** Nonsense?
						else if (T.getPlace() == EditorToolbar.EDGE)
						{
						if (o == null)
						{
						unselectAll();
						
						return;
						}
					*/										
				}				
				// Doubleclick?
				else if (e.getClickCount() == 2)
				{
					
				}
			}
			
			// Repaint is done when you release the mouse button? (But that's before the click?)
			repaint();
		}
		
		public void mousePressed(MouseEvent e)
		{
			// This is for triggering the popup 
			maybeShowPopup(e);
	
			if (e.getButton() == MouseEvent.BUTTON1)
			{
				lastX = e.getX();
				lastY = e.getY();
	
				EditorObject o = getObjectAtPosition(e.getX(), e.getY());
				if (o == null)
				{
					// Clicking on whitespace!
	
					// If control is down, we may select multiple things...
					if (!e.isControlDown())
					{
						unselectAll();
						int posX;
						int posY;
						
						if (nodesSnap)
						{
							posX = findGrid(e.getX());
							posY = findGrid(e.getY());
						}
						else
						{
							posX = e.getX();
							posY = e.getY();
						}
						
						Command createNode = new CreateNodeCommand(ControlledSurface.this, posX, posY);
						root.getUndoInterface().executeCommand(createNode);
						//addLabel(getLastNode(), "", 0, break20);
						
						//SimpleNodeProxy np = new SimpleNodeProxy("s" + nodes.size());
						//PointGeometryProxy gp = new PointGeometryProxy(posX,posY);
						//np.setPointGeometry(gp);
						//graph.getNodes().add(np);
					}
				}
				else
				{
					// Clicking on something!
	
					// Should we unselect the currently selected objects?
					if (!selectedObjects.contains(o))
					{
						// If control is down, we may select multiple things...
						if (!e.isControlDown())
						{
							unselectAll();
						}
					}
	
					// Select stuff!
					//selectChange(o);
	
					unselectAll();
					
					
					// If object is selected prepare to unselect (don't unselect if dragging, though!)
					if (selectedObjects.contains(o))
					{
						toBeDeselected.add(o);
					}
					else
					{
						select(o);
					}
					
					// Find offset values if a label or group was clicked
					if (o.getType() == EditorObject.LABELGROUP)
					{
						xoff = e.getX() - ((EditorLabelGroup) o).getX();
						yoff = e.getY() - ((EditorLabelGroup) o).getY();
					}
					else if (o.getType() == EditorObject.LABEL)
					{
						xoff = e.getX() - ((EditorLabel) o).getX();
						yoff = e.getY() - ((EditorLabel) o).getY();
					}
					else if (o.getType() == EditorObject.NODEGROUP)
					{
						// We need this offset to place new edges
						xoff = e.getX() - ((EditorNodeGroup) o).getX();
						yoff = e.getY() - ((EditorNodeGroup) o).getY();
					}
				}
			}
			else if (e.getButton() == MouseEvent.BUTTON2)
			{
				System.out.println("Button 2!");
			}
			else if (e.getButton() == MouseEvent.BUTTON3)
			{
	
			}
	
			repaint(false);
		}
		
		public void mouseReleased(MouseEvent e)
		{
			if (move != null) {
				if (move.getDisplacement().distance(0,0) != 0) {
					root.getUndoInterface().addUndoable(move);
				}
				move = null;
			}
	
			// This is for triggering the popup 
			maybeShowPopup(e);
	
			// Make the temporary selection definite
			while (toBeSelected.size() > 0)
			{
				select((EditorObject) toBeSelected.remove(0));
			}
			while (toBeDeselected.size() > 0)
			{
				unselect((EditorObject) toBeDeselected.remove(0));
			}
	
			if (e.getButton() == MouseEvent.BUTTON1)
			{
				lines.clear();
			}
	
			repaint(hasDragged);
	
			dragSelect = false;
			hasDragged = false;
		}
		
		public void mouseDragged(MouseEvent e)
		{
			//if (e.getButton() == MouseEvent.BUTTON1) // Why not?
			{
				hasDragged = true;				
				
				// Find the distances that the mouse has dragged (for moving and stuff)
				int dx = 0;
				int dy = 0;
				// Are we using snap?
				if ((nodeIsSelected() || nodeGroupIsSelected()) && nodesSnap) 
				{
					lastX = findGrid(lastX);
					lastY = findGrid(lastY);
					
					int currX = findGrid(e.getX());
					int currY = findGrid(e.getY());
					
					// If the first selected node or nodegroup is not correctly
					// aligned already, we need a modifier to get it on the
					// grid again...
					int modX = 0;
					int modY = 0;
					for (final EditorObject o : selectedObjects) 
					{
						if ((o.getType() == EditorObject.NODE) || (o.getType() == EditorObject.NODEGROUP))
						{
							modX = findGrid(o.getX()) - o.getX();
							modY = findGrid(o.getY()) - o.getY();
						}
					}
	
					dx = currX - lastX + modX;
					dy = currY - lastY + modY;
				}
				else 
				{
					dx = e.getX() - lastX;
					dy = e.getY() - lastY;
				}
	
				// Update position
				lastX += dx;
				lastY += dy;
	
				// DragSelect!
				if (dragSelect)
				{
					dragNowX = e.getX();
					dragNowY = e.getY();
	
					toBeSelected = getDragSelection();
	
					// Select all that should be selected...
					ControlledSurface.super.unselectAll();
					// These have been selected previously and should still be
					// selected no matter what
					for (final EditorObject o : selectedObjects) 
					{
						o.setSelected(true);
						LinkedList children = getChildren(o);
						while (children.size() != 0)
						{
							((EditorObject) children.remove(0)).setSelected(true);
						}
					}
					// These are in the current drag selection
					for (final EditorObject o : toBeSelected) 
					{
						o.setSelected(true);
						LinkedList children = getChildren(o);
						while (children.size() != 0)
						{
							((EditorObject) children.remove(0)).setSelected(true);
						}
					}
	
					repaint(false);
	
					return;
				}
	
				// Multiple selection?
				// Single selection! (Multiple selection is only allowed in SELECT-mode.)								
			}
	
			repaint(false);
		}
		
		public void mouseMoved(MouseEvent e)
		{
			updateHighlighting(e.getPoint());
		}
	}
	
	private class EdgeListener
		extends MouseAdapter
		implements MouseMotionListener
	{
		public void mousePressed(MouseEvent e)
		{
			// This is for triggering the popup 
			maybeShowPopup(e);
	
			if (e.getButton() == MouseEvent.BUTTON1)
			{
				lastX = e.getX();
				lastY = e.getY();
	
				EditorObject o = getObjectAtPosition(e.getX(), e.getY());
				if (o == null)
				{
					if (!e.isControlDown())
					{
						unselectAll();
					}
				}
				else
				{
					// Clicking on something!
	
					// Should we unselect the currently selected objects?
					if (!selectedObjects.contains(o))
					{
						// If control is down, we may select multiple things...
						if (!e.isControlDown())
						{
							unselectAll();
						}
					}
	
					unselectAll();
					
					// If object is selected prepare to unselect (don't unselect if dragging, though!)
					if (selectedObjects.contains(o))
					{
						toBeDeselected.add(o);
					}
					else
					{
						select(o);
					}	   
	
					/*
					if (o.getType() == EditorObject.EDGE)
					{
						EditorEdge edge = (EditorEdge) o;
						if (edge.wasClicked(e.getX(), e.getY()))
						{
							return;
						}
					}
					*/
					
					// Find offset values if a label or group was clicked
					if (o.getType() == EditorObject.LABELGROUP)
					{
						xoff = e.getX() - ((EditorLabelGroup) o).getX();
						yoff = e.getY() - ((EditorLabelGroup) o).getY();
					}
					else if (o.getType() == EditorObject.LABEL)
					{
						xoff = e.getX() - ((EditorLabel) o).getX();
						yoff = e.getY() - ((EditorLabel) o).getY();
					}
					else if (o.getType() == EditorObject.NODEGROUP)
					{
						// We need this offset to place new edges
						xoff = e.getX() - ((EditorNodeGroup) o).getX();
						yoff = e.getY() - ((EditorNodeGroup) o).getY();
					}
				}
			}
			else if (e.getButton() == MouseEvent.BUTTON2)
			{
				System.out.println("Button 2!");
			}
			else if (e.getButton() == MouseEvent.BUTTON3)
			{
	
			}
	
			repaint(false);
		}
		
		public void mouseReleased(MouseEvent e)
		{
			if (move != null) {
				if (move.getDisplacement().distance(0,0) != 0) {
					root.getUndoInterface().addUndoable(move);
				}
				move = null;
			}
	
			// This is for triggering the popup 
			maybeShowPopup(e);
	
			// Make the temporary selection definite
			while (toBeSelected.size() > 0)
			{
				select((EditorObject) toBeSelected.remove(0));
			}
			while (toBeDeselected.size() > 0)
			{
				unselect((EditorObject) toBeDeselected.remove(0));
			}
	
			if (e.getButton() == MouseEvent.BUTTON1)
			{
				// Draw an edge if EditorToolbar.EDGE is selected
				if (hasDragged && ((nodeIsSelected() || nodeGroupIsSelected())))
				{
					while (selectedObjects.size() != 0)
					{
						// This is thus the startingpoint
						EditorObject n1 = (EditorObject) selectedObjects.get(0);
						unselect(n1);
	
						// This is the targetpoint
						EditorObject o2 = getObjectAtPosition(e.getX(), e.getY());
						if ((o2 != null) && (o2.getType() == EditorObject.NODE))
						{
							EditorNode n2 = (EditorNode) o2;						
							// Add edge
							if (n1.getType() == EditorObject.NODE)
							{
								EditorNode n = (EditorNode) n1;
								Command createEdge = new CreateEdgeCommand(ControlledSurface.this, n1, n2, 0, 0);
								root.getUndoInterface().executeCommand(createEdge);
							}
							else if (n1.getType() == EditorObject.NODEGROUP)
							{
								EditorNodeGroup n = (EditorNodeGroup) n1;
								Command createEdge = new CreateEdgeCommand(ControlledSurface.this ,n, n2,
													   n.getX() + xoff, n.getY() + yoff);
								root.getUndoInterface().executeCommand(createEdge);
							}
						}
					}
	
					unselectAll();
				}	
	
				
				if (edgeIsSelected() && (selectedObjects.size() <= 2))
				{
					EditorObject obj = (EditorObject) selectedObjects.get(0);
					EditorEdge edge;
					if (obj.getType() == EditorObject.EDGE)
					{
						edge = (EditorEdge) obj;
					}
					else
					{
						edge = (EditorEdge) selectedObjects.get(1);
					}

					EditorObject n = getNodeOrNodeGroupAtPosition(e.getX(), e.getY());

					if (n != null)
					{
						if (n.getType() == EditorObject.NODE)
						{
							if (edge.getDragT() && (n != edge.getEndNode()))
							{
								edge.setEndNode((EditorNode) n);
							}
							else if (edge.getDragS() && (n != edge.getStartNode()))
							{
								edge.setStartNode(n, e.getX(), e.getY());
							}
						}
						else if (n.getType() == EditorObject.NODEGROUP)
						{
							if (edge.getDragS())
							{
								edge.setStartNode(n, e.getX(), e.getY());
							}
						}
					}

					edge.setDragT(false);
					edge.setDragS(false);
					edge.setDragC(false);
					//edge.setTPoint(edge.getTPointX(), edge.getTPointY());
				}
	
				lines.clear();
			}
	
			repaint(hasDragged);
	
			dragSelect = false;
			hasDragged = false;
		}
		
		public void mouseDragged(MouseEvent e)
		{
			//if (e.getButton() == MouseEvent.BUTTON1) // Why not?
			{
				hasDragged = true;
	
				// Find the distances that the mouse has dragged (for moving and stuff)
				int dx = 0;
				int dy = 0;
				// Are we using snap?
				dx = e.getX() - lastX;
				dy = e.getY() - lastY;
	
				// Update position
				lastX += dx;
				lastY += dy;
	
				// DragSelect!
				if (dragSelect)
				{
					dragNowX = e.getX();
					dragNowY = e.getY();
	
					toBeSelected = getDragSelection();
	
					// Select all that should be selected...
					ControlledSurface.super.unselectAll();
					// These have been selected previously and should still be
					// selected no matter what
					for (final EditorObject o : selectedObjects) 
					{
						o.setSelected(true);
						LinkedList children = getChildren(o);
						while (children.size() != 0)
						{
							((EditorObject) children.remove(0)).setSelected(true);
						}
					}
					// These are in the current drag selection
					for (final EditorObject o : toBeSelected) 
					{
						o.setSelected(true);
						LinkedList children = getChildren(o);
						while (children.size() != 0)
						{
							((EditorObject) children.remove(0)).setSelected(true);
						}
					}
	
					repaint(false);
	
					return;
				}
	
				// Single selection! (Multiple selection is only allowed in SELECT-mode.)
				
				// Edge drawing...
				// No move?
				if ((dx == 0) && (dy == 0))
				{
					return;
				}
				// is this the start of the move or a continuation of it
				if (!selectedObjects.isEmpty() && edgeIsSelected()) {
					if (move == null) {
						move = new MoveObjects(ControlledSurface.this, selectedObjects, new Point2D.Double(dx, dy));
					} else {
						Point2D p = move.getDisplacement();
						p.setLocation(p.getX() + dx, p.getY() + dy);
						move.setDisplacement(p);
					}
				}
				// There should only be one object here, or maybe two, 
				// a node and it's label or an edge and it's labelgroup...
				// Let's make it an iterator anyway!
				for (final EditorObject object : selectedObjects) {
					if (object.getType() == EditorObject.EDGE) {
						// If an edge is selected, you can drag!
						assert(selectedObjects.size() <= 2);
						EditorEdge edge = (EditorEdge) object;
						if (edge.getDragS()) {
							edge.setSource(e.getX(), e.getY());
						} else if (edge.getDragT())	{
							edge.setTarget(e.getX(), e.getY());
						} else {
							edge.setPosition(edge.getTPointX() + dx, edge.getTPointY() + dy);
						}
					} else if (object.getType() == EditorObject.NODE) {
						// If clicking on a node or nodegroup, draw a
						// new edge!
						assert(selectedObjects.size() <= 2);
						EditorNode node = (EditorNode) object;
						int[] dat = {node.getX(), node.getY(),
									 e.getX(), e.getY()};
						// Draw line!
						if ((lines != null) && (lines.size() > 0))
						{
							lines.set(0, dat);
						}
						else
						{
							lines.add(dat);
						}
					} else if (object.getType() == EditorObject.NODEGROUP) {
						assert(selectedObjects.size() == 1);
						EditorNodeGroup nodeGroup = (EditorNodeGroup) object;
						// Find point on the border of the group from where the line is drawn...
						Point2D p = nodeGroup.setOnBounds(nodeGroup.getX() + xoff, nodeGroup.getY() + yoff);
						int[] dat = {(int) p.getX(), (int) p.getY(), e.getX(), e.getY()};
						// Draw line!
						if ((lines != null) && (lines.size() > 0))
						{
							lines.set(0, dat);
						}
						else
						{
							lines.add(dat);
						}
					}
				}
			
				// Update highlighting!
				//updateHighlighting(e);								
			}
	
			repaint(false);
		}
	
		public void mouseMoved(MouseEvent e)
		{
			updateHighlighting(e.getPoint());
		}
	}
	
	private class GroupNodeListener
		extends MouseAdapter
		implements MouseMotionListener
	{
		public void mousePressed(MouseEvent e)
		{
			// This is for triggering the popup 
			maybeShowPopup(e);
	
			if (e.getButton() == MouseEvent.BUTTON1)
			{
				lastX = e.getX();
				lastY = e.getY();
	
				EditorObject o = getObjectAtPosition(e.getX(), e.getY());
				if (o == null)
				{
	
					// If control is down, we may select multiple things...
					if (!e.isControlDown())
					{
						unselectAll();
					}
					
					// If NODEGROUP is active, we're adding a new group!
						
					Collection<NodeProxy> ic = Collections.emptyList();       
					// EditorNodeGroup nodeGroup;
					if (nodesSnap)
					{
						BoxGeometrySubject g = new BoxGeometrySubject(new Rectangle2D.Double(findGrid(e.getX()),
														   findGrid(e.getY()), 0, 0));
						// nodeGroup = addNodeGroup(findGrid(e.getX()), findGrid(e.getY()), 0, 0);
						GroupNodeSubject n = new GroupNodeSubject("NodeGroup" + nodeGroups.size(),
											  new PlainEventListSubject(), ic, g);	
						newGroup = new EditorNodeGroup(n);
					}
					else
					{
						BoxGeometrySubject g = new BoxGeometrySubject(new Rectangle2D.Double(e.getX(),
														 e.getY(), 0, 0));
						// nodeGroup = addNodeGroup(e.getX(), e.getY(), 0, 0);
						GroupNodeSubject n = new GroupNodeSubject("NodeGroup" + nodeGroups.size(),
											  new PlainEventListSubject(), ic, g);   	
						newGroup = new EditorNodeGroup(n);
					}
					select(newGroup);
				}
				else
				{
					// Clicking on something!
	
					// Should we unselect the currently selected objects?
					if (!selectedObjects.contains(o))
					{
						// If control is down, we may select multiple things...
						if (!e.isControlDown())
						{
							unselectAll();
						}
					}
	
					unselectAll();					
					
					// If object is selected prepare to unselect (don't unselect if dragging, though!)
					if (selectedObjects.contains(o))
					{
						toBeDeselected.add(o);
					}
					else
					{
						select(o);
					}	   
	
					// Find offset values if a label or group was clicked
					if (o.getType() == EditorObject.LABELGROUP)
					{
						xoff = e.getX() - ((EditorLabelGroup) o).getX();
						yoff = e.getY() - ((EditorLabelGroup) o).getY();
					}
					else if (o.getType() == EditorObject.LABEL)
					{
						xoff = e.getX() - ((EditorLabel) o).getX();
						yoff = e.getY() - ((EditorLabel) o).getY();
					}
					else if (o.getType() == EditorObject.NODEGROUP)
					{
						// We need this offset to place new edges
						xoff = e.getX() - ((EditorNodeGroup) o).getX();
						yoff = e.getY() - ((EditorNodeGroup) o).getY();
					}
				}
			}
			else if (e.getButton() == MouseEvent.BUTTON2)
			{
				System.out.println("Button 2!");
			}
			else if (e.getButton() == MouseEvent.BUTTON3)
			{
	
			}
	
			repaint(false);
		}
		
		public void mouseReleased(MouseEvent e)
		{
			if (move != null) {
				if (move.getDisplacement().distance(0,0) != 0) {
					root.getUndoInterface().addUndoable(move);
				}
				move = null;
			}
	
			// This is for triggering the popup 
			maybeShowPopup(e);
	
			// Make the temporary selection definite
			while (toBeSelected.size() > 0)
			{
				select((EditorObject) toBeSelected.remove(0));
			}
			while (toBeDeselected.size() > 0)
			{
				unselect((EditorObject) toBeDeselected.remove(0));
			}
	
			if (e.getButton() == MouseEvent.BUTTON1)
			{
				// Stop resizing nodegroup
				if (nodeGroupIsSelected() && (selectedObjects.size() == 1) && newGroup == null)
				{
					EditorNodeGroup ng = (EditorNodeGroup) selectedObjects.get(0);
					ng.setResizingFalse();
	
					if (ng.isEmpty())
					{
						Command c = new DeleteNodeGroupCommand(ControlledSurface.this, ng);
						root.getUndoInterface().executeCommand(c);				 
					}
				}
				if (newGroup != null) {
					newGroup.setResizingFalse();
					Command c = new CreateNodeGroupCommand(ControlledSurface.this, newGroup);
					newGroup = null;
					root.getUndoInterface().executeCommand(c);
				}
	
				lines.clear();
			}
	
			repaint(hasDragged);
	
			dragSelect = false;
			hasDragged = false;
		}
		
		public void mouseDragged(MouseEvent e)
		{
			//if (e.getButton() == MouseEvent.BUTTON1) // Why not?
			{
				hasDragged = true;				
	
				// Find the distances that the mouse has dragged (for moving and stuff)
				int dx = 0;
				int dy = 0;
				// Are we using snap?
				if ((nodeIsSelected() || nodeGroupIsSelected()) && nodesSnap) 
				{
					lastX = findGrid(lastX);
					lastY = findGrid(lastY);
					
					int currX = findGrid(e.getX());
					int currY = findGrid(e.getY());
					
					// If the first selected node or nodegroup is not correctly
					// aligned already, we need a modifier to get it on the
					// grid again...
					int modX = 0;
					int modY = 0;
					for (final EditorObject o : selectedObjects) 
					{
						if ((o.getType() == EditorObject.NODE) || (o.getType() == EditorObject.NODEGROUP))
						{
							modX = findGrid(o.getX()) - o.getX();
							modY = findGrid(o.getY()) - o.getY();
						}
					}
	
					dx = currX - lastX + modX;
					dy = currY - lastY + modY;
				}
				else 
				{
					dx = e.getX() - lastX;
					dy = e.getY() - lastY;
				}
	
				// Update position
				lastX += dx;
				lastY += dy;
	
				// DragSelect!
				if (dragSelect)
				{
					dragNowX = e.getX();
					dragNowY = e.getY();
	
					toBeSelected = getDragSelection();
	
					// Select all that should be selected...
					ControlledSurface.super.unselectAll();
					// These have been selected previously and should still be
					// selected no matter what
					for (final EditorObject o : selectedObjects) 
					{
						o.setSelected(true);
						LinkedList children = getChildren(o);
						while (children.size() != 0)
						{
							((EditorObject) children.remove(0)).setSelected(true);
						}
					}
					// These are in the current drag selection
					for (final EditorObject o : toBeSelected) 
					{
						o.setSelected(true);
						LinkedList children = getChildren(o);
						while (children.size() != 0)
						{
							((EditorObject) children.remove(0)).setSelected(true);
						}
					}
	
					repaint(false);
	
					return;
				}
	
				// Single selection! (Multiple selection is only allowed in SELECT-mode.)
				
				// Are we resizing a nodegroup?
				if (nodeGroupIsSelected())
				{
					EditorNodeGroup nodeGroup = (EditorNodeGroup) selectedObjects.get(0);

					//Rectangle2D.Double b = new Rectangle2D.Double();
					//b.setRect(nodeGroup.getBounds());

					if (nodeGroup.getResizing())
					{
						if (nodesSnap)
						{
							nodeGroup.resize(findGrid(e.getX()), findGrid(e.getY()));
						}
						else
						{
							nodeGroup.resize(e.getX(), e.getY());
						}
					}
					else
					{
						/*
						if (nodesSnap)
						{
							nodeGroup.moveGroup(Math.round((e.getX() - xoff) / gridSize) * gridSize, Math.round((e.getY() - yoff) / gridSize) * gridSize);
						}
						else
						{
							nodeGroup.moveGroup(e.getX() - xoff, e.getY() - yoff);
						}
						*/
						nodeGroup.setPosition(nodeGroup.getX()+ dx, nodeGroup.getY() + dy);
					}
					
					/* // Prevent nodegroups from moving onto nodes?
					if (intersectsRectangle(nodeGroup.getBounds()))
					{
						nodeGroup.setBounds(b);
					}
					*/
					
					examineCollisions();
				}
			}
	
			repaint(false);
		}
	
		public void mouseMoved(MouseEvent e)
		{
			updateHighlighting(e.getPoint());
		}
	}
	
	private class InitialListener
		extends MouseAdapter
		implements MouseMotionListener
	{
		public void mousePressed(MouseEvent e)
		{
			EditorObject o = getObjectAtPosition(e.getX(), e.getY());
			if (o == null || o.getType() != EditorObject.NODE)
			{
				return;
			}
			
			EditorNode n = (EditorNode) o;
			unsetAllInitial();
			n.setInitial(true);
			repaint();
		}
		
		public void mouseDragged(MouseEvent e)
		{
			;
		}
		
		public void mouseMoved(MouseEvent e)
		{
			updateHighlighting(e.getPoint());
		}
	}
    //#######################################################################
	//# Data Members
	private ControlledSurface S;
	private ControlledToolbar mToolbar;
	private EditorOptions options;
	private EditorNode sNode = null;
	private int lastX = 0;
	private int lastY = 0;
	private int xoff = 0;
	private int yoff = 0;
	private boolean controlPointsMove = true;
	private boolean nodesSnap = true;

	private boolean hasDragged = false;

	/** List of currently selected EditorObject:s. */
	private LinkedList<EditorObject> selectedObjects = new LinkedList<EditorObject>();
	/** List of EditorObject:s that are to become selected. */
	private LinkedList<EditorObject> toBeSelected = new LinkedList<EditorObject>();
	/** List of EditorObject:s that are to become unselected. */
	private LinkedList<EditorObject> toBeDeselected = new LinkedList<EditorObject>();

	/** The currently highlighted EditorObject (under the mouse pointer). */
	private EditorObject highlightedObject = null;

    private Move move;

    private DropTarget dropTarget;
    private DropTargetListener dtListener;


    //#######################################################################
	//# Class Constants
	public static final String SELECT = "select";
	public static final String NODE = "node";
	public static final String NODEGROUP = "nodegroup";
	public static final String INITIAL = "initial";
	public static final String EDGE = "edge";
	public static final String EVENT = "event";

	private static final DataFlavor FLAVOUR =
		new DataFlavor(IdentifierWithKind.class, "IdentifierWithKind");
}
