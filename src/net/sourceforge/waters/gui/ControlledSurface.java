
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.gui
//# CLASS:   ControlledSurface
//###########################################################################
//# $Id: ControlledSurface.java,v 1.3 2005-02-19 03:26:56 flordal Exp $
//###########################################################################
package net.sourceforge.waters.gui;

import java.lang.*;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.awt.dnd.*;
import java.util.ArrayList;
import java.io.File;
import java.io.IOException;
import javax.xml.bind.JAXBException;
import net.sourceforge.waters.model.base.*;
import net.sourceforge.waters.model.module.*;
import net.sourceforge.waters.model.expr.*;

public class ControlledSurface
	extends EditorSurface
	implements MouseMotionListener, MouseListener, KeyListener
{
	private ControlledSurface S;
	private EditorToolbar T;
	private EditorNode selectedNode = null;
	private EditorEdge selectedEdge = null;
	private EditorLabel selectedLabel = null;
	private EditorLabelGroup selectedLabelGroup = null;
	private EditorNodeGroup selectedNodeGroup = null;
	private EditorOptions Options;
	private EditorNode sNode = null;
	private int lastLineIndex = 0;
	private int xoff = 0;
	private int yoff = 0;
	private boolean controlPointsMove = true;
	private boolean nodesSnap = true;

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
			if (selectedNode != null)
			{
				delNode(selectedNode);
			}

			if (selectedEdge != null)
			{
				delEdge(selectedEdge);
			}

			if (selectedNodeGroup != null)
			{
				delNodeGroup(selectedNodeGroup);
			}
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

	public void deleteSelected()
	{
		if (selectedNode != null)
		{
			delNode(selectedNode);
		}

		if (selectedEdge != null)
		{
			delEdge(selectedEdge);
		}

		if (selectedNodeGroup != null)
		{
			delNodeGroup(selectedNodeGroup);
		}
	}

	public void mousePressed(MouseEvent e)
	{
		if (e.getButton() == MouseEvent.BUTTON1)
		{
			if (selectedEdge != null)
			{
				if (selectedEdge.wasClicked(e.getX(), e.getY()))
				{
					return;
				}
			}
			
			EditorObject o = getObjectAtPosition(e.getX(), e.getY());
			if (o == null)
			{
				// It there's nothing there, the user is perhaps going to drag-select?
				if (T.getPlace() == EditorToolbar.SELECT)
				{
					// Start of drag-select?
					m_xoffset = e.getX();
					m_yoffset = e.getY();
					
					dragSelect = true;
				}
				
				selectedNode = null;
				selectedEdge = null;
				selectedLabel = null;
				selectedLabelGroup = null;
				selectedNodeGroup = null;
				
				if (T.getPlace() == EditorToolbar.NODEGROUP)
				{
					if (!nodesSnap)
					{
						addNodeGroup(e.getX(), e.getY(), 0, 0);
					}
					else
					{
						addNodeGroup(Math.round(e.getX() / gridSize) * gridSize, Math.round(e.getY() / gridSize) * gridSize, 0, 0);
					}
					
					selectedNodeGroup = getLastNodeGroup();
					
					return;
				}
				
				return;
			}
			else if (o.getType() == EditorObject.NODE)
			{
				selectedNode = (EditorNode) o;
				
				deselectAll();
				
				selectedEdge = null;
				selectedLabel = null;
				selectedNodeGroup = null;
				selectedNode.setSelected(true);
				selectedLabelGroup = null;
				
				repaint();
			}
			else if (o.getType() == EditorObject.EDGE)
			{
				selectedEdge = (EditorEdge) o;

				deselectAll();

				selectedEdge.setSelected(true);
				selectedLabel = null;
				selectedNodeGroup = null;
				selectedNode = null;
				selectedLabelGroup = null;

				repaint();
			}
			else if (o.getType() == EditorObject.LABEL)
			{
				selectedLabel = (EditorLabel) o;

				deselectAll();

				selectedEdge = null;
				selectedLabel.setSelected(true);
				selectedNode = null;
				selectedNodeGroup = null;
				selectedLabelGroup = null;

				xoff = e.getX() - selectedLabel.getX();
				yoff = e.getY() - selectedLabel.getY();

				repaint();
			}
			else if (o.getType() == EditorObject.LABELGROUP)
			{
				selectedLabelGroup = (EditorLabelGroup) o;

				deselectAll();

				selectedEdge = null;
				selectedLabel = null;
				selectedNode = null;
				selectedNodeGroup = null;
				selectedLabelGroup.setSelected(true);

				xoff = e.getX() - selectedLabelGroup.getX();
				yoff = e.getY() - selectedLabelGroup.getY();

				repaint();
			}
			else if (o.getType() == EditorObject.NODEGROUP)
			{
				selectedNodeGroup = (EditorNodeGroup) o;

				deselectAll();

				selectedEdge = null;
				selectedLabel = null;
				selectedNode = null;
				selectedNodeGroup.setSelected(true);
				selectedLabelGroup = null;

				xoff = e.getX() - selectedNodeGroup.getX();
				yoff = e.getY() - selectedNodeGroup.getY();

				repaint();
			}
		}
		else if (e.getButton() == MouseEvent.BUTTON2)
		{
			System.out.println("Button 2!");
		}
		else if (e.getButton() == MouseEvent.BUTTON3)
		{
			EditorObject o = getObjectAtPosition(e.getX(), e.getY());
			if (o.getType() == EditorObject.NODE)
			{
				selectedNode = (EditorNode) o;
				
				NodePopupMenu popup = new NodePopupMenu(this, selectedNode);
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
		// if (e.getButton() == MouseEvent.BUTTON1) // Why not?
		{
			if ((T.getPlace() != EditorToolbar.SELECT) && (T.getPlace() != EditorToolbar.NODE) && (T.getPlace() != EditorToolbar.EDGE) && (T.getPlace() != EditorToolbar.NODEGROUP))
			{
				return;
			}

			if (dragSelect)
			{
				/* Drag-select */
				event_x = e.getX();
				event_y = e.getY();

				paintImmediately(m_xoffset, m_yoffset, event_x, event_y);
			}

			int dx = 0;
			int dy = 0;

			if ((selectedNode != null) && ((T.getPlace() == EditorToolbar.NODE) || (T.getPlace() == EditorToolbar.SELECT)))
			{
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

				int oldx = selectedNode.getX();
				int oldy = selectedNode.getY();

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

				selectedNode.setX(dx);
				selectedNode.setY(dy);

				if (controlPointsMove)
				{
					for (int i = 0; i < edges.size(); i++)
					{
						EditorEdge f = (EditorEdge) edges.get(i);

						if ((f.getStartNode() == selectedNode) || (f.getStartNode() == selectedNodeGroup))
						{
							f.updateControlPoint(oldx, oldy, true);
						}

						if (f.getEndNode() == selectedNode)
						{
							f.updateControlPoint(oldx, oldy, false);
						}
					}
				}

				repaint();
			}

			if ((selectedEdge != null) && ((T.getPlace() == EditorToolbar.EDGE) || (T.getPlace() == EditorToolbar.SELECT)))
			{
				if (selectedEdge.getDragC())
				{
					selectedEdge.setTPoint(e.getX(), e.getY());
					repaint();
				}
				else if (selectedEdge.getDragT())
				{
					selectedEdge.setTarget(e.getX(), e.getY());
					repaint();
				}
				else if (selectedEdge.getDragS())
				{
					System.out.println("DragS :" + selectedEdge.getDragS());
					selectedEdge.setSource(e.getX(), e.getY());
					repaint();
				}
			}

			if (selectedNodeGroup != null)
			{
				System.out.println("it is selected");
			}

			if ((selectedNodeGroup != null) && ((T.getPlace() == EditorToolbar.SELECT) || (T.getPlace() == EditorToolbar.NODEGROUP)))
			{
				Rectangle2D.Double b = new Rectangle2D.Double();

				b.setRect(selectedNodeGroup.getBounds());

				if (selectedNodeGroup.getResizing())
				{
					if (nodesSnap)
					{
						selectedNodeGroup.resize(Math.round(e.getX() / gridSize) * gridSize, Math.round(e.getY() / gridSize) * gridSize);
					}
					else
					{
						selectedNodeGroup.resize(e.getX(), e.getY());
					}
				}
				else
				{
					if (nodesSnap)
					{
						selectedNodeGroup.moveGroup(Math.round((e.getX() - xoff) / gridSize) * gridSize, Math.round((e.getY() - yoff) / gridSize) * gridSize);
					}
					else
					{
						selectedNodeGroup.moveGroup(e.getX() - xoff, e.getY() - yoff);
					}
				}

				if (intersectsRectangle(selectedNodeGroup.getBounds()))
				{
					selectedNodeGroup.setBounds(b);
				}

				repaint();
			}

			if ((selectedLabel != null) && (T.getPlace() == EditorToolbar.SELECT))
			{
				selectedLabel.moveTo(e.getX() - xoff, e.getY() - yoff);

				int[] dat = { selectedLabel.getX(), selectedLabel.getY(),
							  selectedLabel.getParent().getX(),
							  selectedLabel.getParent().getY() };

				if ((lines != null) && (lines.size() > 0))
				{
					lines.set(0, dat);
				}
				else
				{
					lines.add(dat);
				}

				repaint();
			}

			if ((selectedLabelGroup != null) && (T.getPlace() == EditorToolbar.SELECT))
			{
				selectedLabelGroup.moveTo(e.getX() - xoff, e.getY() - yoff);
				repaint();
			}

			if ((selectedNode != null) && (T.getPlace() == EditorToolbar.EDGE))
			{
				int[] dat = { selectedNode.getX(), selectedNode.getY(), e.getX(),
							  e.getY() };

				if ((lines != null) && (lines.size() > 0))
				{
					lines.set(0, dat);
				}
				else
				{
					lines.add(dat);
				}

				repaint();
			}

			if ((selectedNodeGroup != null) && (T.getPlace() == EditorToolbar.EDGE))
			{
				Point2D.Double p = selectedNodeGroup.setOnBounds(selectedNodeGroup.getX() + xoff, selectedNodeGroup.getY() + yoff);
				int[] dat = { (int) p.getX(), (int) p.getY(), e.getX(), e.getY() };

				if ((lines != null) && (lines.size() > 0))
				{
					lines.set(0, dat);
				}
				else
				{
					lines.add(dat);
				}

				repaint();
			}
		}
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

			if (((selectedNode != null) || (selectedNodeGroup != null)) && (T.getPlace() == EditorToolbar.EDGE))
			{
				EditorNode n2 = (EditorNode) getObjectAtPosition(e.getX(), e.getY());

				if (n2 != null)
				{
					if (selectedNode != null)
					{
						addEdge((EditorObject) selectedNode, n2, 0, 0);
					}
					else
					{
						addEdge((EditorObject) selectedNodeGroup, n2, selectedNodeGroup.getX() + xoff, selectedNodeGroup.getY() + yoff);
					}

					if (selectedNode == n2)
					{
						getLastEdge().updateControlPoint(selectedNode.getX(), selectedNode.getY() - 40, true);
					}
				}

				selectedNode = null;
				selectedEdge = null;
				selectedLabel = null;
				selectedNodeGroup = null;
				selectedLabelGroup = null;
			}

			if ((selectedNode != null) && ((T.getPlace() == EditorToolbar.NODE) || (T.getPlace() == EditorToolbar.SELECT)))
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

			if (selectedNodeGroup != null)
			{
				/*
				  if (selectedNodeGroup.getResizing() && nodesSnap) 
				  {
				  selectedNodeGroup.resize(Math.round(e.getX()/gridSize)*gridSize, Math.round(e.getY()/gridSize)*gridSize);
				  }
				*/
				selectedNodeGroup.setResizingFalse();

				if (selectedNodeGroup.isEmpty())
				{
					delNodeGroup(selectedNodeGroup);

					selectedNodeGroup = null;
				}
			}

			if (selectedEdge != null)
			{
				System.out.println("released DragS:" + selectedEdge.getDragS());

				if ((T.getPlace() == EditorToolbar.EDGE) || (T.getPlace() == EditorToolbar.SELECT))
				{
					EditorObject n = getObjectAtPosition(e.getX(), e.getY());

					if (n != null)
					{
						if (n.getType() == EditorObject.NODE)
						{
							if (selectedEdge.getDragT() && (n != selectedEdge.getEndNode()))
							{
								selectedEdge.setEndNode((EditorNode) n);
							}
							else if (selectedEdge.getDragS() && (n != selectedEdge.getStartNode()))
							{
								selectedEdge.setStartNode(n, e.getX(), e.getY());
							}
						}
						else if (n.getType() == EditorObject.NODEGROUP)
						{
							System.out.println("is a nodeGroup when mouse released" + selectedEdge.getDragS());

							if (selectedEdge.getDragS())
							{
								selectedEdge.setStartNode(n, e.getX(), e.getY());
							}
						}
					}

					selectedEdge.setDragT(false);
					selectedEdge.setDragS(false);
					selectedEdge.setDragC(false);
					selectedEdge.setTPoint(selectedEdge.getTPointX(), selectedEdge.getTPointY());

					selectedNode = null;
					selectedLabel = null;
					selectedLabelGroup = null;
					selectedNodeGroup = null;
				}
			}

			/*selectedNode = null;
			  selectedEdge = null;
			  selectedLabel = null;*/
			lines.clear();

			dragSelect = false;

			repaint();
		}
	}

	public void mouseMoved(MouseEvent e)
	{
		;
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
					addNode("s" + nodes.size(), dx, dy);

					//addLabel(getLastNode(), "", 0, break20);
					repaint();

					//SimpleNodeProxy np = new SimpleNodeProxy("s" + nodes.size());
					//PointGeometryProxy gp = new PointGeometryProxy(dx,dy);
					//np.setPointGeometry(gp);
					//graph.getNodes().add(np);
				}
			}

			if ((e.getClickCount() == 1) && (T.getPlace() == EditorToolbar.SELECT))
			{
				EditorObject o = getObjectAtPosition(e.getX(), e.getY());

				if (o == null)
				{
					if (selectedNode != null)
					{
						if (selectedNode.getPropGroup().wasClicked(e.getX(), e.getY()) && selectedNode.getPropGroup().getVisible())
						{
							selectedNode.getPropGroup().setSelected(true);
							repaint();

							return;
						}
					}

					deselectAll();

					return;
				}

				if (o.getType() == EditorObject.NODE)
				{
					EditorNode n = (EditorNode) o;

					deselectAll();

					selectedNode = n;
					selectedEdge = null;
					selectedLabel = null;
					selectedLabelGroup = null;
					selectedNodeGroup = null;

					n.setSelected(true);
					selectAllWithParent(n);
				}

				if (o.getType() == EditorObject.EDGE)
				{
					EditorEdge n = (EditorEdge) o;

					deselectAll();
					n.setSelected(true);

					selectedNode = null;
					selectedEdge = n;
					selectedLabel = null;
					selectedLabelGroup = null;
					selectedNodeGroup = null;

					selectAllWithParent(n);
				}

				if (o.getType() == EditorObject.LABEL)
				{
					EditorLabel n = (EditorLabel) o;

					deselectAll();
					n.setSelected(true);

					selectedNode = null;
					selectedEdge = null;
					selectedLabel = n;
					selectedNodeGroup = null;
					selectedLabelGroup = null;
					xoff = e.getX() - n.getX();
					yoff = e.getY() - n.getY();
				}

				if (o.getType() == EditorObject.LABELGROUP)
				{
					EditorLabelGroup n = (EditorLabelGroup) o;

					deselectAll();
					n.setSelected(true);

					selectedNode = null;
					selectedEdge = null;
					selectedLabel = null;
					selectedLabelGroup = n;
					selectedNodeGroup = null;
					xoff = e.getX() - n.getX();
					yoff = e.getY() - n.getY();
				}

				if (o.getType() == EditorObject.NODEGROUP)
				{
					EditorNodeGroup n = (EditorNodeGroup) o;

					deselectAll();
					n.setSelected(true);

					selectedNode = null;
					selectedEdge = null;
					selectedLabel = null;
					selectedNodeGroup = n;
					selectedLabelGroup = null;
					xoff = e.getX() - n.getX();
					yoff = e.getY() - n.getY();
				}
			}

			if ((e.getClickCount() == 1) && (T.getPlace() == EditorToolbar.EDGE))
			{
				EditorObject o = getObjectAtPosition(e.getX(), e.getY());

				if (o == null)
				{
					selectedNode = null;
					selectedEdge = null;
					selectedLabel = null;
					selectedLabelGroup = null;

					deselectAll();

					return;
				}
			}

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
		}
	}

	public void createOptions(EditorWindow root)
	{
		Options = new EditorOptions(root);
	}

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

	public ControlledSurface(EditorToolbar et, EditorWindow r)
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
