
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.gui
//# CLASS:   EditorSurface
//###########################################################################
//# $Id: EditorSurface.java,v 1.9 2005-03-03 05:36:29 flordal Exp $
//###########################################################################
package net.sourceforge.waters.gui;

import java.awt.print.*;
import javax.swing.*;
import javax.swing.text.*;
import java.util.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.io.File;
import java.io.IOException;
import javax.xml.bind.JAXBException;
import net.sourceforge.waters.model.base.*;
import net.sourceforge.waters.model.module.*;
import net.sourceforge.waters.model.expr.*;
import net.sourceforge.waters.xsd.module.AnchorPosition;

/**
 * <p>A component which allows for the display of module data.</p>
 *
 * <p>The EditorSurface is a viewer-only component.  It can load components from {@link ModuleProxy} objects
 * and translate them into its internal storage format, which allows for it to be displayed.
 * To provide interactive editing of an EditorSurface, use a {@link ControlledSurface}.</p>
 *
 * @author Gian Perrone
 */
public class EditorSurface
	extends JComponent
{
	protected boolean showGrid = true;
	protected EditorWindowInterface root;
	protected int gridSize = 16;
	protected Color backgroundColor = new Color(1.0f, 1.0f, 1.0f);
	protected Color gridColor = new Color(0.0f, 0.0f, 0.0f);
	protected ArrayList nodes;
	protected ArrayList nodeGroups;
	protected ArrayList edges;
	protected ArrayList labels;
	protected ArrayList lines;
	protected ArrayList shades;
	protected ArrayList events;
	protected int dragStartX;
	protected int dragStartY;
	protected int dragNowX;
	protected int dragNowY;
	protected boolean dragSelect = false;
	protected EditorShade shade;
	protected GraphProxy graph;

	public int getGridSize()
	{
		return gridSize;
	}

	public void setGridSize(int g)
	{
		gridSize = g;
	}

	public boolean getShowGrid()
	{
		return showGrid;
	}

	public void setShowGrid(boolean s)
	{
		showGrid = s;
	}

	public void setShade(EditorShade s)
	{
		shade = s;
	}

	protected void paintGrid(Graphics g)
	{
		g.setColor(backgroundColor);
		g.fillRect(0, 0, getWidth(), getHeight());
		g.setColor(gridColor);

		// Draw grid iff showGrid is true
		if (showGrid)
		{
			int x = 0;
			int y = 0;

			while (x < getWidth())
			{
				g.drawLine(x, 0, x, getHeight());

				x += gridSize;
			}

			while (y < getHeight())
			{
				g.drawLine(0, y, getWidth(), y);

				y += gridSize;
			}
		}
	}

	protected void paintComponent(Graphics g)
	{
		minimizeSize();
		paintGrid(g);

		if (nodes == null)
		{
			return;
		}

		for (int i = 0; i < nodeGroups.size(); i++)
		{
			EditorNodeGroup n = (EditorNodeGroup) nodeGroups.get(i);

			n.drawObject(g);

			ArrayList groups = new ArrayList();
			ArrayList children = new ArrayList();

			for (int j = 0; j < nodeGroups.size(); j++)
			{
				EditorNodeGroup n2 = (EditorNodeGroup) nodeGroups.get(j);

				if (n2 == n)
				{
					continue;
				}

				if (n.getBounds().contains(n2.getBounds()))
				{
					boolean notImmediate = false;

					for (int k = 0; k < groups.size(); k++)
					{
						EditorNodeGroup n3 = (EditorNodeGroup) groups.get(k);

						if (n3.getBounds().contains(n2.getBounds()))
						{
							notImmediate = true;

							break;
						}
						else if (n2.getBounds().contains(n3.getBounds()))
						{
							groups.remove(n3);

							break;
						}
					}

					if (!notImmediate)
					{
						groups.add(n2);
					}
				}
			}

			for (int j = 0; j < nodes.size(); j++)
			{
				EditorNode n2 = (EditorNode) nodes.get(j);
				boolean notImmediate = false;

				for (int k = 0; k < groups.size(); k++)
				{
					EditorNodeGroup n3 = (EditorNodeGroup) groups.get(k);

					if (n3.getBounds().contains(n2.getPosition()))
					{
						notImmediate = true;

						break;
					}
				}

				if (!notImmediate)
				{
					if (n.getBounds().contains(n2.getPosition()))
					{
						children.add(n2);
					}
				}
			}

			children.addAll(groups);
			n.setChildNodes(children, (JComponent) this);
		}

		for (int i = 0; i < edges.size(); i++)
		{
			EditorEdge n = (EditorEdge) edges.get(i);

			n.drawObject(g);
		}

		for (int i = 0; i < nodes.size(); i++)
		{
			EditorNode n = (EditorNode) nodes.get(i);

			n.drawObject(g, root.getEventDeclList());
		}

		for (int i = 0; i < labels.size(); i++)
		{
			EditorLabel l = (EditorLabel) labels.get(i);

			l.drawObject(g);
		}

		for (int i = 0; i < events.size(); i++)
		{
			EditorLabelGroup l = (EditorLabelGroup) events.get(i);

			l.setPanelLocation();
		}

		for (int i = 0; i < lines.size(); i++)
		{
			int[] l = (int[]) lines.get(i);

			g.setColor(Color.BLACK);
			g.drawLine(l[0], l[1], l[2], l[3]);
		}

		if (dragSelect)
		{
			showDragSelect(g);
		}
	}

	public static boolean isSimpleComponentProxy(Object o)
	{
		return (o instanceof SimpleComponentProxy);
	}

	public void addNode(String name, int x, int y)
	{
		SimpleNodeProxy np = new SimpleNodeProxy(name);

		graph.getNodes().add(np);

		EditorNode n = new EditorNode(x, y, shade, np, this);

		nodes.add(n);
		addLabel(n, name);
		minimizeSize();
		repaint();
	}

	public void addNode(SimpleNodeProxy np)
	{
		EditorNode n = new EditorNode(shade, np, this);

		nodes.add(n);
		addLabel(n, np.getName());
		minimizeSize();
		repaint();
	}

	public EditorNodeGroup addNodeGroup(int x, int y, int w, int h)
	{
		GroupNodeProxy n = new GroupNodeProxy("NodeGroup" + nodeGroups.size());

		n.setGeometry(new BoxGeometryProxy(x, y, w, h));

		EditorNodeGroup g = new EditorNodeGroup(n);

		nodeGroups.add(g);
		graph.getNodes().add(n);
		repaint();

		return g;
	}

	public void addNodeGroup(GroupNodeProxy n)
	{
		EditorNodeGroup g = new EditorNodeGroup(n);

		nodeGroups.add(g);
		repaint();
	}

	public EditorNodeGroup getLastNodeGroup()
	{
		if (!nodeGroups.isEmpty())
		{
			return (EditorNodeGroup) nodeGroups.get(nodeGroups.size() - 1);
		}

		return null;
	}

	public EditorNode getLastNode()
	{
		if (nodes.size() > 0)
		{
			return (EditorNode) nodes.get(nodes.size() - 1);
		}

		return null;
	}

	public EditorNode findNodeByHash(int hash)
	{
		for (int i = 0; i < nodes.size(); i++)
		{
			EditorNode n = (EditorNode) nodes.get(i);

			if (n.getHash() == hash)
			{
				return n;
			}
		}

		return null;
	}

	public EditorNodeGroup findNodeGroupByHash(int hash)
	{
		for (int i = 0; i < nodeGroups.size(); i++)
		{
			EditorNodeGroup n = (EditorNodeGroup) nodeGroups.get(i);

			if (n.getHash() == hash)
			{
				return n;
			}
		}

		return null;
	}

	public void addEdge(EditorObject n1, EditorNode n2, int x, int y)
	{
		EdgeProxy ep;

		if (n1.getType() == EditorObject.NODE)
		{
			ep = new EdgeProxy((NodeProxy) ((EditorNode) n1).getProxy(), (NodeProxy) n2.getProxy());
		}
		else
		{
			ep = new EdgeProxy((NodeProxy) ((EditorNodeGroup) n1).getProxy(), (NodeProxy) n2.getProxy());
		}

		graph.getEdges().add(ep);

		EditorEdge e = new EditorEdge(n1, n2, x, y, ep);

		edges.add(e);
		addLabelGroup(e);
		minimizeSize();
		repaint();
	}

	public void addEdge(EditorObject n1, EditorNode n2, EdgeProxy ep)
	{
		int x = n2.getX();
		int y = n2.getY();

		if (ep.getStartPoint() != null)
		{
			x = (int) ep.getStartPoint().getPoint().getX();
			y = (int) ep.getStartPoint().getPoint().getY();
		}

		EditorEdge e = new EditorEdge(n1, n2, x, y, ep);

		edges.add(e);
		addLabelGroup(e);
		minimizeSize();
		repaint();
	}

	public EditorEdge getLastEdge()
	{
		if (edges.size() > 0)
		{
			return (EditorEdge) edges.get(edges.size() - 1);
		}

		return null;
	}

	public void addLabel(EditorNode o, String label)
	{
		labels.add(new EditorLabel(o, label, this));
		repaint();
	}

	public void addLabelGroup(EditorEdge e)
	{
		events.add(new EditorLabelGroup(e, this));
		repaint();
	}

	public void addLabelGroup(EditorLabelGroup g)
	{
		events.add(g);
		repaint();
	}

	public EditorLabel getLastLabel()
	{
		if (labels.size() > 0)
		{
			return (EditorLabel) labels.get(labels.size() - 1);
		}

		return null;
	}

	public EditorLabel getLabel(EditorNode n)
	{
		for (int i = 0; i < labels.size(); i++)
		{
			EditorLabel e = (EditorLabel) labels.get(i);

			if (e.getParent() == (EditorObject) n)
			{
				return e;
			}
		}

		return null;
	}

	public EditorLabelGroup getLabelGroup(EditorEdge e)
	{
		for (int i = 0; i < events.size(); i++)
		{
			EditorLabelGroup g = (EditorLabelGroup) events.get(i);

			if (g.getParent() == (EditorObject) e)
			{
				return g;
			}
		}

		return null;
	}

	public void delNode(EditorNode n)
	{
		for (int i = 0; i < edges.size(); i++)
		{
			EditorEdge e = (EditorEdge) edges.get(i);

			if ((e.getStartNode() == n) || (e.getEndNode() == n))
			{
				delEdge(e);
				delNode(n);
			}
		}

		for (int i = 0; i < labels.size(); i++)
		{
			EditorLabel e = (EditorLabel) labels.get(i);

			if (e.getParent() == (EditorObject) n)
			{
				delLabel(e);
				delNode(n);
			}
		}

		nodes.remove(n);
		graph.getNodes().remove(n.getProxy());
		examineCollisions();
		repaint();
	}

	public void delNodeGroup(EditorNodeGroup n)
	{
		for (int i = 0; i < edges.size(); i++)
		{
			EditorEdge e = (EditorEdge) edges.get(i);

			if (e.getStartNode() == n)
			{
				delEdge(e);
			}
		}

		nodeGroups.remove(n);
		graph.getNodes().remove(n.getProxy());
		examineCollisions();
		repaint();
	}

	public void delEdge(EditorEdge e)
	{
		for (int i = 0; i < events.size(); i++)
		{
			EditorLabelGroup g = (EditorLabelGroup) events.get(i);

			if (g.getParent() == (EditorObject) e)
			{
				delLabelGroup(g);
				delEdge(e);
			}
		}

		if (e.getStartNode().getType() == EditorObject.NODEGROUP)
		{
			((EditorNodeGroup) e.getStartNode()).removePosition(e.getStartPoint());
		}

		edges.remove(e);
		graph.getEdges().remove(e);
		repaint();
	}

	public void delLabel(EditorLabel l)
	{
		int i = labels.indexOf(l);

		if (i != -1)
		{
			l.removeFromSurface(this);
			labels.remove(i);
		}

		repaint();
	}

	public void delLabelGroup(EditorLabelGroup g)
	{
		events.remove(g);
		g.removeFromSurface(this);
		repaint();
	}

	public void clearAll()
	{
		nodes.clear();
		edges.clear();
		lines.clear();

		for (int i = 0; i < labels.size(); i++)
		{
			((EditorLabel) labels.get(i)).removeFromSurface(this);
		}

		labels.clear();

		for (int i = 0; i < events.size(); i++)
		{
			((EditorLabelGroup) events.get(i)).removeFromSurface(this);
		}

		events.clear();
		nodeGroups.clear();
		repaint();
	}

	public void showDragSelect(Graphics g)
	{
		//Graphics2D g2d = (Graphics2D) this.getGraphics();
		Graphics2D g2d = (Graphics2D) g;

		g2d.setColor(EditorColor.DRAGSELECTCOLOR);
		g2d.fill(getDragRectangle());
		//g2d.fill(new Rectangle(dragStartX, dragStartY, dragNowX - dragStartX, dragNowY - dragStartY));
	}

	// When you're dragging an edge, you only want nodes or nodegroups...
	public EditorObject getNodeOrNodeGroupAtPosition(int ex, int ey)
	{
		for (int i = 0; i < nodes.size(); i++)
		{
			if (((EditorNode) nodes.get(i)).wasClicked(ex, ey))
			{
				return (EditorNode) nodes.get(i);
			}
		}

		for (int i = 0; i < nodeGroups.size(); i++)
		{
			if (((EditorNodeGroup) nodeGroups.get(i)).wasClicked(ex, ey))
			{
				return (EditorNodeGroup) nodeGroups.get(i);
			}
		}

		return null;
	}

	public EditorObject getObjectAtPosition(int ex, int ey)
	{
		// Order of precedence: Nodes, Edges, Labels
		for (int i = 0; i < nodes.size(); i++)
		{
			if (((EditorNode) nodes.get(i)).wasClicked(ex, ey))
			{
				return (EditorNode) nodes.get(i);
			}
		}

		// But edges before nodegroups, because they are hiding the edges otherwise...
		for (int i = 0; i < edges.size(); i++)
		{
			if (((EditorEdge) edges.get(i)).wasClicked(ex, ey))
			{
				return (EditorEdge) edges.get(i);
			}
		}

		for (int i = 0; i < nodeGroups.size(); i++)
		{
			if (((EditorNodeGroup) nodeGroups.get(i)).wasClicked(ex, ey))
			{
				return (EditorNodeGroup) nodeGroups.get(i);
			}
		}

		for (int i = 0; i < labels.size(); i++)
		{
			if (((EditorLabel) labels.get(i)).wasClicked(ex, ey))
			{
				return (EditorLabel) labels.get(i);
			}
		}

		for (int i = 0; i < events.size(); i++)
		{
			if (((EditorLabelGroup) events.get(i)).wasClicked(ex, ey))
			{
				return (EditorLabelGroup) events.get(i);
			}
		}

		return null;
	}

	/**
	 * Returns a list of all children of an object.
	 */
	public LinkedList getChildren(EditorObject o)
	{
		LinkedList children = new LinkedList();

		for (int i = 0; i < labels.size(); i++)
		{
			if (((EditorLabel) labels.get(i)).getParent() == o)
			{
				children.add((EditorLabel) labels.get(i));
			}
		}

		for (int i = 0; i < events.size(); i++)
		{
			if (((EditorLabelGroup) events.get(i)).getParent() == o)
			{
				children.add((EditorLabelGroup) events.get(i));
			}
		}
		
		return children;
	}

	public Rectangle getDragRectangle()
	{
		int x;
		int y;
		int w;
		int h;
		if (dragStartX > dragNowX)
		{
			x = dragNowX;
			w = dragStartX - dragNowX;
		}
		else
		{
			x = dragStartX;
			w = dragNowX - dragStartX;
		}
		
		if (dragStartY > dragNowY)
		{
			y = dragNowY;
			h = dragStartY - dragNowY;
		}
		else
		{
			y = dragStartY;
			h = dragNowY - dragStartY;
		}
		
		return new Rectangle(x,y,w,h);
	}

	/**
	 * Returns a list of all objects within a rectangle (to be selected).
	 */
	public LinkedList getDragSelection()
	{
		Rectangle r = getDragRectangle();

		LinkedList selection = new LinkedList();

		for (int i = 0; i < nodeGroups.size(); i++)
		{
			EditorNodeGroup n = (EditorNodeGroup) nodeGroups.get(i);

			if (r.getBounds().contains(n.getBounds()))
			{
				selection.add(n);
			}
		}

		for (int i = 0; i < nodes.size(); i++)
		{
			EditorNode n = (EditorNode) nodes.get(i);
			
			if (r.getBounds().contains(n.getPosition()))
			{
				selection.add(n);
			}
		}
		
		return selection;
	}

	/**
	 * Sets the error status of nodes and nodegroups. If a nodegroup is placed on top of
	 * a node, they are both set as being erroneous.
	 */
	public void examineCollisions()
	{
		for (Iterator it = nodes.iterator(); it.hasNext(); )
		{
			EditorNode node = (EditorNode) it.next();
			node.setError(nodeIsColliding(node));
			
			// Set error on children as well
			LinkedList children = getChildren(node);
			while (children.size() != 0)
			{
				((EditorObject) children.remove(0)).setError(node.isError());
			}
		}
		for (Iterator it = nodeGroups.iterator(); it.hasNext(); )
		{
			EditorNodeGroup nodeGroup = (EditorNodeGroup) it.next();
			nodeGroup.setError(nodeGroupIsColliding(nodeGroup));

			// Set error on children as well
			LinkedList children = getChildren(nodeGroup);
			while (children.size() != 0)
			{
				((EditorObject) children.remove(0)).setError(nodeGroup.isError());
			}
		}
	}

	public void deselectAll()
	{
		for (int i = 0; i < nodes.size(); i++)
		{
			EditorNode n = (EditorNode) nodes.get(i);

			n.setSelected(false);
			nodes.set(i, n);
		}

		for (int i = 0; i < edges.size(); i++)
		{
			EditorEdge e = (EditorEdge) edges.get(i);

			e.setSelected(false);
			edges.set(i, e);
		}

		for (int i = 0; i < labels.size(); i++)
		{
			EditorLabel l = (EditorLabel) labels.get(i);

			l.setSelected(false);
			l.setEditing(false);
			labels.set(i, l);
		}

		for (int i = 0; i < events.size(); i++)
		{
			EditorLabelGroup g = (EditorLabelGroup) events.get(i);

			g.setSelected(false);
			events.set(i, g);
		}

		for (int i = 0; i < nodeGroups.size(); i++)
		{
			EditorNodeGroup n = (EditorNodeGroup) nodeGroups.get(i);

			n.setSelected(false);
			nodeGroups.set(i, n);
		}
	}

	public void unsetAllInitial()
	{
		for (int i = 0; i < nodes.size(); i++)
		{
			EditorNode n = (EditorNode) nodes.get(i);

			n.setInitial(false);
			nodes.set(i, n);
		}
	}

	/**
	 * <p>Loads a Component from a Module.</p>
	 * @param module The module to load events from
	 * @param element The element to read
	 */
	public void loadElement(ModuleProxy module, ElementProxy element)
	{
		ArrayList unloadedNodeGroups = new ArrayList();
		ArrayList unloadedEdges = new ArrayList();

		if (isSimpleComponentProxy(element))
		{
			SimpleComponentProxy cp = (SimpleComponentProxy) element;
			ArrayList noGeometry = new ArrayList();

			graph = cp.getGraph();

			final Collection mNodes = graph.getNodes();
			final Iterator iter = mNodes.iterator();

			while (iter.hasNext())
			{
				NodeProxy temp = (NodeProxy) iter.next();

				if (temp instanceof SimpleNodeProxy)
				{
					SimpleNodeProxy np = (SimpleNodeProxy) temp;

					if (np.getPointGeometry() == null)
					{
						noGeometry.add(np);
					}
					else
					{
						addNode(np);
					}
				}
				else if (temp instanceof GroupNodeProxy)
				{
					GroupNodeProxy gn = (GroupNodeProxy) temp;

					if (gn.getGeometry() == null)
					{
						unloadedNodeGroups.add(gn);
					}
					else
					{
						addNodeGroup(gn);
					}
				}
			}

			int size = (int) Math.sqrt(noGeometry.size());

			if (size < Math.sqrt(noGeometry.size()))
			{
				size++;
			}

			Rectangle2D.Double r = new Rectangle2D.Double();
			boolean found = false;
			int x = 0;
			int y = 0;

			for (y = gridSize - EditorNode.WIDTH / 2; y < getHeight();
					y += gridSize)
			{
				for (x = gridSize - EditorNode.WIDTH / 2; x < getWidth();
						x += gridSize)
				{
					r.setRect(x, y, gridSize * (size - 1) * 4 + EditorNode.WIDTH, gridSize * (size - 1) * 4 + EditorNode.WIDTH);

					found = true;

					for (int i = 0; i < nodes.size(); i++)
					{
						EditorNode n = (EditorNode) nodes.get(i);

						if (n.getEllipse().intersects(r))
						{
							found = false;

							break;
						}
					}

					if (!found)
					{
						continue;
					}

					for (int i = 0; i < nodeGroups.size(); i++)
					{
						if (((EditorNodeGroup) nodeGroups.get(i)).getBounds().intersects(r))
						{
							found = false;

							break;
						}
					}

					if (found)
					{
						break;
					}
				}

				if (found)
				{
					break;
				}
			}

			if (!found)
			{
				x = getWidth();
				y = getHeight();
			}

			for (int i = 0; (i < size) && (i * size < noGeometry.size()); i++)
			{
				for (int j = 0;
						(j < size) && (i * size + j < noGeometry.size()); j++)
				{
					if (noGeometry.get(i * size + j) instanceof SimpleNodeProxy)
					{
						SimpleNodeProxy np = (SimpleNodeProxy) noGeometry.get(i * size + j);
						Point2D.Double p = new Point2D.Double(x + (j * gridSize * 4) + EditorNode.WIDTH / 2, y + (i * gridSize * 4) + EditorNode.WIDTH / 2);

						np.setPointGeometry(new PointGeometryProxy(p));
						addNode(np);
					}
				}
			}

			java.util.List edges = graph.getEdges();

			for (int j = 0; j < edges.size(); j++)
			{
				EdgeProxy e = (EdgeProxy) edges.get(j);
				EditorObject s = null;
				EditorNode t = null;

				if ((e.getSource() != null) && (e.getTarget() != null))
				{
					s = findNodeByHash(e.getSource().hashCode());

					if (s == null)
					{
						s = findNodeGroupByHash(e.getSource().hashCode());
					}

					t = findNodeByHash(e.getTarget().hashCode());

					if ((s != null) && (t != null))
					{
						addEdge(s, t, e);
						getLastEdge().setHash(e.hashCode());
					}
					else
					{
						unloadedEdges.add(e);
					}
				}
				else
				{
					//TODO: Do something here!
					System.err.println("SOURCE OR TARGET IS NULL!");
				}
			}
		}

		repaint();

		if (unloadedNodeGroups.size() > 0)
		{
			JOptionPane.showMessageDialog(this, new String("" + unloadedNodeGroups.size() + " NodeGroups were not possible to load due to lack of geometry data"));

			for (int i = 0; i < unloadedNodeGroups.size(); i++)
			{
				graph.getNodes().remove(unloadedNodeGroups.get(i));
			}
		}

		if (unloadedEdges.size() > 0)
		{
			JOptionPane.showMessageDialog(this, new String("" + unloadedEdges.size() + " Edges were not possible to load due to lack of source or target data"));

			for (int i = 0; i < unloadedEdges.size(); i++)
			{
				graph.getEdges().remove(unloadedEdges.get(i));
			}
		}
	}

	public ArrayList getShades()
	{
		return shades;
	}

	public void setShades(ArrayList s)
	{
		shades = s;
	}

	/**
	 * This should reduce the size of the controlled surface to the minimum required
	 */
	public void minimizeSize()
	{
		int largestX = 500;
		int largestY = 500;

		for (int i = 0; i < nodes.size(); i++)
		{
			if (((EditorNode) nodes.get(i)).getX() > largestX)
			{
				largestX = ((EditorNode) nodes.get(i)).getX();
			}

			if (((EditorNode) nodes.get(i)).getY() > largestY)
			{
				largestY = ((EditorNode) nodes.get(i)).getY();
			}
		}

		for (int i = 0; i < edges.size(); i++)
		{
			if (((EditorEdge) edges.get(i)).getTPointX() > largestX)
			{
				largestX = ((EditorEdge) edges.get(i)).getX();
			}

			if (((EditorEdge) edges.get(i)).getTPointY() > largestY)
			{
				largestY = ((EditorEdge) edges.get(i)).getY();
			}
		}

		setPreferredSize(new Dimension(largestX + gridSize * 10, largestY + gridSize * 10));
	}

	public boolean nodeGroupIsColliding(EditorNodeGroup ng)
	{
		Rectangle2D.Double r = ng.getBounds();
		Rectangle2D.Double e = new Rectangle2D.Double();

		for (int i = 0; i < nodes.size(); i++)
		{
			EditorNode n = (EditorNode) nodes.get(i);

			e.setFrameFromCenter(n.getX(), n.getY(), n.getX() + n.radius(), n.getY() + n.radius());

			if (r.intersects(e) &&!r.contains(e))
			{
				return true;
			}
		}

		return false;
	}

	public boolean nodeIsColliding(EditorNode n)
	{
		Rectangle2D.Double e = new Rectangle2D.Double();
		e.setFrameFromCenter(n.getX(), n.getY(), n.getX() + n.radius(), n.getY() + n.radius());

		for (int i = 0; i < nodeGroups.size(); i++)
		{
			EditorNodeGroup ng = (EditorNodeGroup) nodeGroups.get(i);
			Rectangle2D.Double r = ng.getBounds();

			if (r.intersects(e) &&!r.contains(e))
			{
				return true;
			}
		}

		return false;
	}

	// TODO: This should take a ModuleProxy as a parameter
	public EditorSurface()
	{
		nodes = new ArrayList();
		nodeGroups = new ArrayList();
		edges = new ArrayList();
		labels = new ArrayList();
		lines = new ArrayList();
		shades = new ArrayList();
		events = new ArrayList();

		shades.add(new EditorShade("Default", 255, 255, 255));

		shade = (EditorShade) shades.get(0);
	}
}
