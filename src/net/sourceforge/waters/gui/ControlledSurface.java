
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.gui
//# CLASS:   ControlledSurface
//###########################################################################
//# $Id: ControlledSurface.java,v 1.9 2005-02-22 07:54:28 flordal Exp $
//###########################################################################
package net.sourceforge.waters.gui;

import java.lang.*;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.awt.dnd.*;
import java.io.File;
import java.io.IOException;
import javax.xml.bind.JAXBException;
import net.sourceforge.waters.model.base.*;
import net.sourceforge.waters.model.module.*;
import net.sourceforge.waters.model.expr.*;
import java.util.Iterator;
import java.util.LinkedList;

public class ControlledSurface
	extends EditorSurface
	implements MouseMotionListener, MouseListener, KeyListener
{
	private ControlledSurface S;
	private EditorToolbar T;
	private EditorOptions Options;
	private EditorNode sNode = null;
	private int lastLineIndex = 0;
	private int lastX = 0;
	private int lastY = 0;
	private int xoff = 0;
	private int yoff = 0;
	private boolean controlPointsMove = false;
	private boolean nodesSnap = true;

	private boolean selectOnDrag = false;
	private boolean hasDragged = false;

	private LinkedList selectedObjects = new LinkedList();
	private EditorObject highlightedObject = null;

	public void setOptionsVisible(boolean v)
	{
		Options.setVisible(v);
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

	/** Handle the key released event from the text field. */
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
				delNode((EditorNode) o);
			}
			else if (o.getType() == EditorObject.EDGE)
			{
				delEdge((EditorEdge) o);
			}
			else if (o.getType() == EditorObject.NODEGROUP)
			{
				delNodeGroup((EditorNodeGroup) o);
			}
		}
	}

	private void select(EditorObject o)
	{
		if (!selectedObjects.contains(o))
		{
			selectedObjects.add(o);
			o.setSelected(true);
			selectAllWithParent(o);
		}
	}

	private void selectAllWithParent(EditorObject o)
	{
		for (int i = 0; i < labels.size(); i++)
		{
			if (((EditorLabel) labels.get(i)).getParent() == o)
			{
				select((EditorLabel) labels.get(i));
			}
		}

		for (int i = 0; i < events.size(); i++)
		{
			if (((EditorLabelGroup) events.get(i)).getParent() == o)
			{
				select((EditorLabelGroup) events.get(i));
			}
		}
	}

	private void deselect(EditorObject o)
	{
		if (selectedObjects.contains(o))
		{
			selectedObjects.remove(o);
			o.setSelected(false);
			//deselectAllWithParent(o);
		}
	}

	private void deselectAllWithParent(EditorObject o)
	{
		for (int i = 0; i < labels.size(); i++)
		{
			if (((EditorLabel) labels.get(i)).getParent() == o)
			{
				deselect((EditorLabel) labels.get(i));
			}
		}

		for (int i = 0; i < events.size(); i++)
		{
			if (((EditorLabelGroup) events.get(i)).getParent() == o)
			{
				deselect((EditorLabelGroup) events.get(i));
			}
		}
	}

	public void deselectAll()
	{
		selectedObjects.clear();
		super.deselectAll();
	}

	private void selectChange(EditorObject o)
	{
		// Only if SELECT is chosen multiple selection is possible...
		if (!(T.getPlace() == EditorToolbar.SELECT))
		{
			deselectAll();
		}

		// If object is selected unselect
		if (selectedObjects.contains(o))
		{
			deselect(o);
		}
		else
		{
			select(o);
		}
	}

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

   	public void mousePressed(MouseEvent e)
	{
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
					deselectAll();
				}

				// If SELECT is active, this means that we're starting a drag-select...
				if (T.getPlace() == EditorToolbar.SELECT)
				{
					// Start of drag-select?
					m_xoffset = e.getX();
					m_yoffset = e.getY();
					dragSelect = true;
				}

				// If NODEGROUP is active, we're adding a new group!
				if (T.getPlace() == EditorToolbar.NODEGROUP)
				{
					EditorNodeGroup nodeGroup;
					if (nodesSnap)
					{
						nodeGroup = addNodeGroup(Math.round(e.getX() / gridSize) * gridSize, Math.round(e.getY() / gridSize) * gridSize, 0, 0);
					}
					else
					{
						nodeGroup = addNodeGroup(e.getX(), e.getY(), 0, 0);
					}
					select(nodeGroup);
				}

				return;
			}
			else
			{
				// Clicking on something!

				if (!selectedObjects.contains(o))
				{
					// If control is down, we may select multiple things...
					if (!e.isControlDown())
					{
						deselectAll();
					}
				}

				// Select stuff!
				selectChange(o);

				// If dragging, we should select anyway!
				selectOnDrag = true;
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
				xoff = e.getX() - ((EditorNodeGroup) o).getX();
				yoff = e.getY() - ((EditorNodeGroup) o).getY();
			}
		}
		else if (e.getButton() == MouseEvent.BUTTON2)
		{
			System.out.println("Button 2!");
		}
		else if (e.getButton() == MouseEvent.BUTTON3)
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
		}

		repaint();
	}

	public boolean isFocusable()
	{
		return true;
	}

	public void mouseDragged(MouseEvent e)
	{
		// A drag is a move
		//mouseMoved(e);

		//if (e.getButton() == MouseEvent.BUTTON1) // Why not?
		{
			hasDragged = true;

			// If we're not in a nice mode, let's end it right here and right now
			if ((T.getPlace() != EditorToolbar.SELECT) &&
				(T.getPlace() != EditorToolbar.NODE) &&
				(T.getPlace() != EditorToolbar.EDGE) &&
				(T.getPlace() != EditorToolbar.NODEGROUP))
			{
				return;
			}

			// DragSelect!
			if (dragSelect)
			{
				/* Drag-select */
				event_x = e.getX();
				event_y = e.getY();

				paintImmediately(m_xoffset, m_yoffset, event_x, event_y);

				// Add or remove things that come in selection...
				// notImplemented();

				return;
			}

			// Multiple selection?
			if (T.getPlace() == EditorToolbar.SELECT)
			{
				// Drag all selected objects

				// Select on drag?
				if (selectOnDrag)
				{
					// Where are we pointing right now?
					EditorObject o = getObjectAtPosition(e.getX(), e.getY());
					if (o != null)
					{
						select(o);
					}

					selectOnDrag = false;
				}

				// The distances that we should move everything...
				int dx = 0;
				int dy = 0;

				// Are we using snap?
				if ((nodeIsSelected() || nodeGroupIsSelected()) && nodesSnap)
					//if (nodesSnap)
				{
					lastX = (lastX/gridSize) * gridSize;
					lastY = (lastY/gridSize) * gridSize;

					while (dx + gridSize < e.getX())
					{
						dx += gridSize;
					}

					if (e.getX() - dx > (dx + gridSize) - e.getX())
					{
						dx += gridSize;
					}

					while (dy + gridSize < e.getY())
					{
						dy += gridSize;
					}

					if (e.getY() - dy > (dy + gridSize) - e.getY())
					{
						dy += gridSize;
					}

					// So the real delta values are...
					dx -= lastX;
					dy -= lastY;
				}
				else
				{
					dx = e.getX() - lastX;
					dy = e.getY() - lastY;
				}

				// Update position
				lastX += dx;
				lastY += dy;

				// No move?
				if ((dx == 0) && (dy == 0))
				{
					return;
				}

				/*
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
						node.setX(oldx + dx);
						node.setY(oldy + dy);

						// Update edges
						if (controlPointsMove)
						{
							for (int i = 0; i < edges.size(); i++)
							{
								EditorEdge f = (EditorEdge) edges.get(i);

								if (f.getStartNode() == node)
								{
									f.updateControlPoint(oldx, oldy, true);
								}

								if (f.getEndNode() == node)
								{
									f.updateControlPoint(oldx, oldy, false);
								}
							}
						}
					}
					// Is it a nodegroup?
					else if (object.getType() == EditorObject.NODEGROUP)
					{

						EditorNodeGroup nodeGroup = (EditorNodeGroup) object;

						Rectangle2D.Double b = new Rectangle2D.Double();

						b.setRect(nodeGroup.getBounds());

						if (nodeGroup.getResizing() && (selectedObjects.size() == 1))
						{
							if (nodesSnap)
							{
								nodeGroup.resize(Math.round(e.getX() / gridSize) * gridSize, Math.round(e.getY() / gridSize) * gridSize);
							}
							else
							{
								nodeGroup.resize(e.getX(), e.getY());
							}
						}
						else
						{
							if (nodesSnap)
							{
								nodeGroup.moveGroup(Math.round((e.getX() - xoff) / gridSize) * gridSize, Math.round((e.getY() - yoff) / gridSize) * gridSize);
							}
							else
							{
								nodeGroup.moveGroup(e.getX() - xoff, e.getY() - yoff);
							}
						}

						if (intersectsRectangle(nodeGroup.getBounds()))
						{
							nodeGroup.setBounds(b);
						}

						/*
						EditorNodeGroup nodeGroup = (EditorNodeGroup) object;
						nodeGroup.moveGroup(nodeGroup.getX() + dx, nodeGroup.getY() + dy);
						*/
					}
					// Is it an edge?
					else if ((object.getType() == EditorObject.EDGE) && !controlPointsMove)
					{
						EditorEdge edge = (EditorEdge) object;
						edge.setTPoint(edge.getTPointX() + dx, edge.getTPointY() + dy);
					}

					// DONT MOVE LABELS IN MULTI MODE, (WITH THE OFFSETS IT'S NO FUN TO TRY TO GET IT RIGHT...)
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
			}
			else
			{
				// Single selection!

				// Edge drawing...
				if (T.getPlace() == EditorToolbar.EDGE)
				{
					// There should only be one object here, or maybe two, 
					// a node and it's label or an edge and it's labelgroup...
					// Let's make it an iterator anyway!
					for (Iterator it = selectedObjects.iterator(); it.hasNext(); )
					{
						// Which object is it?
						EditorObject object = (EditorObject) it.next();

						// If an edge is selected, you can drag!
						if (object.getType() == EditorObject.EDGE)
						{
							assert(selectedObjects.size() <= 2);

							EditorEdge edge = (EditorEdge) object;
							
							if (edge.getDragC())
							{
								edge.setTPoint(e.getX(), e.getY());
							}
							else if (edge.getDragT())
							{
								edge.setTarget(e.getX(), e.getY());
							}
							else if (edge.getDragS())
							{
								edge.setSource(e.getX(), e.getY());
							}
						}
						
						if (object.getType() == EditorObject.NODE)
						{
							assert(selectedObjects.size() <= 2);

							EditorNode node = (EditorNode) object;
							
							int[] dat = {node.getX(), node.getY(), e.getX(), e.getY()};
							
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
						else if (object.getType() == EditorObject.NODEGROUP)
						{
							assert(selectedObjects.size() == 1);

							EditorNodeGroup nodeGroup = (EditorNodeGroup) object;
							
							// Find point on the border of the group from where the line is drawn...
							Point2D.Double p = nodeGroup.setOnBounds(nodeGroup.getX() + xoff, nodeGroup.getY() + yoff);
							int[] dat = { (int) p.getX(), (int) p.getY(), e.getX(), e.getY() };
							
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
				}

				// Are we resizing a nodegroup?
				if (nodeGroupIsSelected() && ((T.getPlace() == EditorToolbar.NODEGROUP) || (T.getPlace() == EditorToolbar.SELECT)))
				{
					EditorNodeGroup nodeGroup = (EditorNodeGroup) selectedObjects.get(0);

					Rectangle2D.Double b = new Rectangle2D.Double();

					b.setRect(nodeGroup.getBounds());

					if (nodeGroup.getResizing())
					{
						if (nodesSnap)
						{
							nodeGroup.resize(Math.round(e.getX() / gridSize) * gridSize, Math.round(e.getY() / gridSize) * gridSize);
						}
						else
						{
							nodeGroup.resize(e.getX(), e.getY());
						}
					}
					else
					{
						if (nodesSnap)
						{
							nodeGroup.moveGroup(Math.round((e.getX() - xoff) / gridSize) * gridSize, Math.round((e.getY() - yoff) / gridSize) * gridSize);
						}
						else
						{
							nodeGroup.moveGroup(e.getX() - xoff, e.getY() - yoff);
						}
					}

					if (intersectsRectangle(nodeGroup.getBounds()))
					{
						nodeGroup.setBounds(b);
					}
				}
			}
		}

		repaint();
	}

	public void mouseReleased(MouseEvent e)
	{
		if (e.getButton() == MouseEvent.BUTTON1)
		{
			if (root.getBuffer() != null)
			{
				EditorEdge edge = null;

				for (int i = 0; i < edges.size(); i++)
				{
					if (((EditorEdge) edges.get(i)).wasClicked(e.getX(), e.getY()))
					{
						edge = (EditorEdge) edges.get(i);

						break;
					}
				}

				if (edge != null)
				{
					for (int i = 0; i < events.size(); i++)
					{
						if (((EditorLabelGroup) events.get(i)).getParent() == edge)
						{
							((EditorLabelGroup) events.get(i)).addEvent(root.getBuffer());

							break;
						}
					}
				}

				root.setBuffer(null);
			}

			// Draw an edge if EditorToolbar.EDGE is selected
			if (hasDragged && ((nodeIsSelected() || nodeGroupIsSelected()) && (T.getPlace() == EditorToolbar.EDGE)))
			{
				while (selectedObjects.size() != 0)
				{
					// This is thus the startingpoint
					EditorObject n1 = (EditorObject) selectedObjects.remove(0);
					deselect(n1);

					// This is the targetpoint
					EditorObject o2 = getObjectAtPosition(e.getX(), e.getY());
					if ((o2 != null) && (o2.getType() == EditorObject.NODE))
					{
						EditorNode n2 = (EditorNode) o2;

						// Add edge
						if (n1.getType() == EditorObject.NODE)
						{
							EditorNode n = (EditorNode) n1;
							addEdge(n1, n2, 0, 0);
						}
						else if (n1.getType() == EditorObject.NODEGROUP)
						{
							EditorNodeGroup n = (EditorNodeGroup) n1;
							addEdge(n, n2, n.getX() + xoff, n.getY() + yoff);
						}
					}
				}

				deselectAll();
			}

			/*
			if (nodeIsSelected() && ((T.getPlace() == EditorToolbar.NODE) || (T.getPlace() == EditorToolbar.SELECT)))
			{
				if (controlPointsMove)
				{
					for (int i = 0; i < edges.size(); i++)
					{
						EditorEdge f = (EditorEdge) edges.get(i);

						if ((selectedNode == f.getStartNode()) || (selectedNode == f.getEndNode()))
						{
							f.setTPoint(f.getTPointX(), f.getTPointY());
						}
					}
				}
			}
			*/

			// Stop resizing nodegroup
			if (nodeGroupIsSelected() && (selectedObjects.size() == 1))
			{
				EditorNodeGroup ng = (EditorNodeGroup) selectedObjects.remove(0);
				ng.setResizingFalse();

				if (ng.isEmpty())
				{
					delNodeGroup(ng);
				}
			}

			// Redefine edge if it has been changed
			if ((T.getPlace() == EditorToolbar.EDGE) || (T.getPlace() == EditorToolbar.SELECT))
			{
				if (edgeIsSelected() && (selectedObjects.size() <= 2))
				{
					EditorEdge edge = (EditorEdge) selectedObjects.remove(0);

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
					edge.setTPoint(edge.getTPointX(), edge.getTPointY());
				}
			}

			lines.clear();

			dragSelect = false;
			hasDragged = false;
			selectOnDrag = false;
		}

		repaint();
	}

	public void mouseMoved(MouseEvent e)
	{
		// Highlight things that are moved over...
		EditorObject o = getObjectAtPosition(e.getX(), e.getY());

		if ((highlightedObject != null) && !highlightedObject.equals(o))
		{
			highlightedObject.setHighlighted(false);
			highlightedAllWithParent(highlightedObject, false);
			highlightedObject = null;
		}

		if (o != null)
		{
			o.setHighlighted(true);
			highlightedObject = o;
			highlightedAllWithParent(highlightedObject, true);
		}

		repaint();
	}

	/**
	 * Changes the highlight status of all EditorObject:s with o as parent.
	 *
	 * @param o the parent of which all children should be highlighed.
	 * @param highlight if true, makes highlighted, otherwise removes highlight
	 */
	private void highlightedAllWithParent(EditorObject o, boolean highlight)
	{
		for (int i = 0; i < labels.size(); i++)
		{
			if (((EditorLabel) labels.get(i)).getParent() == o)
			{
				((EditorLabel) labels.get(i)).setHighlighted(highlight);
			}
		}

		for (int i = 0; i < events.size(); i++)
		{
			if (((EditorLabelGroup) events.get(i)).getParent() == o)
			{
				((EditorLabelGroup) events.get(i)).setHighlighted(highlight);
			}
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
			if ((e.getClickCount() == 1) && (T.getPlace() == EditorToolbar.NODE))
			{
				int dx = 0;
				int dy = 0;

				if (nodesSnap)
				{
					while (dx + gridSize < e.getX())
					{
						dx += gridSize;
					}

					if (e.getX() - dx > (dx + gridSize) - e.getX())
					{
						dx += gridSize;
					}

					while (dy + gridSize < e.getY())
					{
						dy += gridSize;
					}

					if (e.getY() - dy > (dy + gridSize) - e.getY())
					{
						dy += gridSize;
					}
				}
				else
				{
					dx = e.getX();
					dy = e.getY();
				}

				// Is there already a node present?
				if ((getObjectAtPosition(dx, dy) == null) || (getObjectAtPosition(dx, dy).getType() != EditorObject.NODE))
				{
					// Find a unique name!
					int i;
					for (i=0; i<=nodes.size(); i++)
					{
						boolean found = false;
						for (int j=0; j<nodes.size(); j++)
						{
							if (((EditorNode) nodes.get(j)).getName().equals("s" + i))
							{
								found = true;
								break;
							}
						}
						if (!found)
						{
							break;
						}
					}
					addNode("s" + i, dx, dy);

					//addLabel(getLastNode(), "", 0, break20);
					repaint();

					//SimpleNodeProxy np = new SimpleNodeProxy("s" + nodes.size());
					//PointGeometryProxy gp = new PointGeometryProxy(dx,dy);
					//np.setPointGeometry(gp);
					//graph.getNodes().add(np);
				}
			}

			if ((e.getClickCount() == 1) && (T.getPlace() == EditorToolbar.EDGE))
			{
				EditorObject o = getObjectAtPosition(e.getX(), e.getY());

				if (o == null)
				{
					deselectAll();

					return;
				}
			}

			// Set clicked node to initial
			if ((e.getClickCount() == 1) && (T.getPlace() == EditorToolbar.INITIAL))
			{
				EditorNode n = (EditorNode) getObjectAtPosition(e.getX(), e.getY());

				if (n == null)
				{
					return;
				}

				unsetAllInitial();
				n.setInitial(true);
				repaint();
			}

			/*
			if ((e.getClickCount() == 2) && (T.getPlace() == EditorToolbar.SELECT))
			{
				if (selectedNode != null)
				{
					if (selectedNode.getPropGroup().wasClicked(e.getX(), e.getY()) && selectedNode.getPropGroup().getVisible())
					{
						EditorPropGroup p = selectedNode.getPropGroup();

						p.setSelectedLabel(e.getX(), e.getY());
						repaint();
					}
				}

				EditorObject o = getObjectAtPosition(e.getX(), e.getY());

				if ((o != null) && (o.getType() == EditorObject.LABEL))
				{
					EditorLabel l = (EditorLabel) o;

					if (l == null)
					{
						return;
					}

					l.setEditing(true);
					repaint();
				}

				if ((o != null) && (o.getType() == EditorObject.LABELGROUP))
				{
					EditorLabelGroup l = (EditorLabelGroup) o;

					if (l == null)
					{
						return;
					}

					l.setSelectedLabel(e.getX(), e.getY());
					repaint();
				}

				if ((o != null) && (o.getType() == EditorObject.NODE))
				{
					EditorNode n = (EditorNode) o;

					if (n == null)
					{
						return;
					}

					n.getPropGroup().setVisible(true);
					repaint();
				}
			}
			*/
		}
	}

	public void createOptions(EditorWindowInterface root)
	{
		Options = new EditorOptions(root);
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

	public ControlledSurface(EditorToolbar et, EditorWindowInterface r)
	{
		S = this;
		T = et;
		root = r;
		gridColor = new Color(0.9f, 0.9f, 0.9f);

		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		this.addKeyListener(this);
		this.requestFocusInWindow();
	}
}
