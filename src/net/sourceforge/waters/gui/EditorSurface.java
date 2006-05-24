//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   EditorSurface
//###########################################################################
//# $Id: EditorSurface.java,v 1.52 2006-05-24 12:01:56 martin Exp $
//###########################################################################


package net.sourceforge.waters.gui;

import java.awt.*;
import java.awt.geom.*;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import javax.swing.*;
import javax.swing.text.*;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.base.ModelChangeEvent;
import net.sourceforge.waters.subject.base.ModelObserver;
import net.sourceforge.waters.subject.module.*;
import net.sourceforge.waters.xsd.module.AnchorPosition;
import net.sourceforge.waters.xsd.module.GuardActionBlock;


/**
 * <p>A component which allows for the display of module data.</p>
 *
 * <p>The EditorSurface is a viewer-only component. It can load components
 * from {@link ModuleSubject} objects and translate them into its internal
 * storage format, which allows for it to be displayed. To provide
 * interactive editing of an EditorSurface, use a {@link
 * ControlledSurface}.</p>
 *
 * @author Gian Perrone
 */

public class EditorSurface
	extends JComponent
	implements Printable,
			   ModelObserver
{
	/**
	 * Increase bounds for label & guardAction panels.
	 */
	public static final int TEXTSHADOWMARGIN = 2; 
	protected boolean showGrid = true;
	protected EditorWindowInterface root;
	protected int gridSize = 16;
	protected Color backgroundColor = new Color(1.0f, 1.0f, 1.0f);
	protected Color gridColor = new Color(0.9f, 0.9f, 0.9f);
	protected ArrayList nodes;
	protected ArrayList nodeGroups;
	protected ArrayList edges;
	protected ArrayList labels;
	protected ArrayList lines;
	protected ArrayList shades;
	protected ArrayList events;
	protected ArrayList mGuardActionBlocks;
	protected int dragStartX;
	protected int dragStartY;
	protected int dragNowX;
	protected int dragNowY;
	protected boolean dragSelect = false;
	protected EditorShade shade;
	protected GraphSubject graph;
    protected EditorNodeGroup newGroup = null;
	private Rectangle drawnAreaBounds = null;
	
	public void modelChanged(ModelChangeEvent e)
	{
		repaint();
	}
	
	public boolean isSelected(EditorObject o)
	{
		return false;
	}
	
	public GraphProxy getGraph()
	{
		return graph;
	}
	
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

	/**
	 * Called when printing.
	 */
	protected void printComponent(Graphics g)
	{
		// Paint using hairline
		EditorObject.setBasicStroke(EditorObject.THINSTROKE);
		paintComponent(g, true);
		EditorObject.setBasicStroke(EditorObject.SINGLESTROKE);
	}

	/**
	 * Called when painting.
	 */
	protected void paintComponent(Graphics g)
	{
		paintComponent(g, false);
	}

	/**
	 * Paints the surface on {@code g}.
	 */
	private void paintComponent(Graphics g, boolean printing)
	{
		// Only paint the grid if we're not printing!
		if (!printing)
		{
			paintGrid(g);
		}
		
 		// Draw nodegroups
		for (int i = 0; i < nodeGroups.size(); i++)
		{
			EditorNodeGroup n = (EditorNodeGroup) nodeGroups.get(i);
			n.drawObject(g, isSelected(n));

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
			boolean ok = n.setChildNodes(children, (JComponent) this);
			if (!ok)
			{
				JOptionPane.showMessageDialog(this, "Removing nodegroup.");

				delNodeGroup(n);
				break;
			}
		}
		
		// If there is a new group being created, draw it!
		if (newGroup != null) 
		{
			newGroup.drawObject(g, isSelected(newGroup));
		}
		
		// Draw edges
		for (int i = 0; i < edges.size(); i++)
		{
			EditorEdge edge = (EditorEdge) edges.get(i);
			edge.drawObject(g, isSelected(edge));

			/*
			QuadCurve2D.Double curve = edge.getCurve();
			//PathIterator it = curve.getPathIterator(new AffineTransform());
			FlatteningPathIterator it = new FlatteningPathIterator (curve.getPathIterator(new AffineTransform()), 2, 100);
			while (!it.isDone())
			{
				double[] segment = new double[6];
				int type = it.currentSegment(segment);
				
				g.setColor(Color.ORANGE);
				g.drawOval((int) segment[0]-1, (int) segment[1]-1, 3, 3);
				
				it.next();
			}
			*/
		}

		// Draw lines (edges being drawn)
		for (int i = 0; i < lines.size(); i++)
		{ 
			int[] l = (int[]) lines.get(i);

			g.setColor(Color.BLACK);
			g.drawLine(l[0], l[1], l[2], l[3]);
		}

		// Draw nodes
		for (int i = 0; i < nodes.size(); i++)
		{
			EditorNode n = (EditorNode) nodes.get(i);
			n.drawObject(g, isSelected(n));
		}

		// Draw node labels
		for (int i = 0; i < labels.size(); i++)
		{
			EditorLabel l = (EditorLabel) labels.get(i);
			l.drawObject(g, isSelected(l) || isSelected(l.getParent()));
		}

		// Draw event labels
		for (int i = 0; i < events.size(); i++)
		{
			EditorLabelGroup l = (EditorLabelGroup) events.get(i);
			l.setPanelLocation(isSelected(l) || isSelected(l.getParent()));

			// Why is this done here? Why are labelgroups treated
			// differently? EditorLabelGroup:s don't have drawObject
			// methods, they are drawn as panels?
			// Draw shadow
			if (l.shadow && l.isHighlighted())
			{
				Rectangle bounds = l.getBounds();
				g.setColor(l.getShadowColor(isSelected(l) || isSelected(l.getParent())));
				g.fillRoundRect((int) bounds.getX()-TEXTSHADOWMARGIN, 
								(int) bounds.getY()-TEXTSHADOWMARGIN, 
								(int) bounds.getWidth()+2*TEXTSHADOWMARGIN, 
								(int) bounds.getHeight()+2*TEXTSHADOWMARGIN, 
								20, 20);
			}
		}
		
		// Draw guard/action blocks
		for (int i = 0; i < mGuardActionBlocks.size(); i++)
		{
			EditorGuardActionBlock block = (EditorGuardActionBlock) mGuardActionBlocks.get(i);
			block.setPanelLocation();

			if (block.shadow && block.isHighlighted())
			{
				Rectangle bounds = block.getBounds();
				g.setColor(block.getShadowColor(isSelected(block) || isSelected(block.getParent())));
				g.fillRoundRect((int) bounds.getX()-TEXTSHADOWMARGIN, 
								(int) bounds.getY()-TEXTSHADOWMARGIN, 
								(int) bounds.getWidth()+2*TEXTSHADOWMARGIN, 
								(int) bounds.getHeight()+2*TEXTSHADOWMARGIN, 
								20, 20);
			}
		}

		// Draw selection area
		if (dragSelect)
		{
			showDragSelect(g);
		}
		
		/*
		// Test: Print outline of drawn area (just to see that it's OK)
		Rectangle rect = getDrawnAreaBounds();
		g.setColor(Color.PINK);
		g.drawRect((int) rect.getX(), (int) rect.getY(), (int) rect.getWidth(), (int) rect.getHeight());
		*/
	}

	public static boolean isSimpleComponentSubject(Object o)
	{
		return (o instanceof SimpleComponentSubject);
	}

	public void addNode(String name, int x, int y)
	{
	    Collection<Proxy> ev = Collections.emptyList();
	    SimpleNodeSubject np = new SimpleNodeSubject
			(name,
			 new PlainEventListSubject(ev),
			 false,
			 new PointGeometrySubject(new Point2D.Double(x, y)),
			 null,
			 null);
	    EditorNode n = new EditorNode(x, y, np, this);
	    addNode(n);
	}

	public void addNode(SimpleNodeSubject np)
	{
		EditorNode n = new EditorNode(np, this);

		addNode(n);
	}

	public void addNode(final EditorNode n)
	{
		final Collection<NodeSubject> subjects = graph.getNodesModifiable();
		subjects.add(n.getSubject());
	    nodes.add(n);
	    addLabel(n, n.getName());
	    repaint();
	    examineCollisions();  
	}


	public void delNode(EditorNode n)
	{
		for (int i = edges.size()-1; i >= 0; i--) {
			EditorEdge e = (EditorEdge) edges.get(i);
			if ((e.getStartNode() == n) || (e.getEndNode() == n))
			{
				delEdge(e);
			}
		}

		for (int i = labels.size()-1; i >= 0; i--)
		{
			EditorLabel e = (EditorLabel) labels.get(i);

			if (e.getParent() == (EditorObject) n)
			{
				delLabel(e);
			}
		}

		nodes.remove(n);
		final Collection<NodeSubject> subjects = graph.getNodesModifiable();
		final NodeSubject subject = n.getSubject();
		subjects.remove(subject);

		examineCollisions();
		repaint();
	}


	public EditorNodeGroup addNodeGroup(int x, int y, int w, int h)
	{
	    Collection<Proxy> ev = Collections.emptyList();
	    Collection<NodeProxy> ic = Collections.emptyList();
	    BoxGeometrySubject g = new BoxGeometrySubject(new Rectangle2D.Double(x, y, w, h));
	    GroupNodeSubject n = new GroupNodeSubject("NodeGroup" + nodeGroups.size(), new PlainEventListSubject(ev), ic, g);     
	    return addNodeGroup(n);
	}


	public EditorNodeGroup addNodeGroup(GroupNodeSubject subject)
	{
		final EditorNodeGroup enode = new EditorNodeGroup(subject);
		return addNodeGroup(enode);
	}


	public EditorNodeGroup addNodeGroup(final EditorNodeGroup enode)
	{
		final Collection<NodeSubject> subjects = graph.getNodesModifiable();
		final NodeSubject subject = enode.getSubject();
		subjects.add(subject);
	    nodeGroups.add(enode);
	    examineCollisions();
	    repaint();
	    return enode;
	}


	public EditorNode findNodeByHash(int hash)
	{
		for (int i = 0; i < nodes.size(); i++)
		{
			EditorNode n = (EditorNode) nodes.get(i);

			if (n.hashCode() == hash)
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

			if (n.hashCode() == hash)
			{
				return n;
			}
		}

		return null;
	}

	public EditorEdge addEdge(final EditorEdge edge)
	{
		final Collection<EdgeSubject> subjects = graph.getEdgesModifiable();
		subjects.add(edge.getSubject());
	    edges.add(edge);
	    addLabelGroup(edge);
	    addGuardActionBlock(edge);
	    edge.resizePanels();
	    repaint();
	    return edge;
	}

	public EditorEdge addEdge(EditorObject n1, EditorNode n2, EdgeSubject ep)
	{
		int x = n2.getX();
		int y = n2.getY();

		if (ep.getStartPoint() != null)
		{
			x = (int) ep.getStartPoint().getPoint().getX();
			y = (int) ep.getStartPoint().getPoint().getY();
		}
		EditorEdge e = new EditorEdge(n1, n2, x, y, ep, this);

		edges.add(e);
		addLabelGroup(e);
		addGuardActionBlock(e);
	    e.resizePanels();
		repaint();

		return e;
	}

	public void delEdge(EditorEdge e)
	{
		for (int i = 0; i < events.size(); i++)
		{
			EditorLabelGroup g = (EditorLabelGroup) events.get(i);

			if (g.getParent() == (EditorObject) e)
			{
				delLabelGroup(g);
			}
		}
		
		//delete guardActionBlock
		final EditorGuardActionBlock gA = e.getEditorGuardActionBlock();
		if(gA != null) {
			delGuardActionBlock(gA);
		}
		

		if (e.getStartNode().getType() == EditorObject.NODEGROUP)
		{
			((EditorNodeGroup) e.getStartNode()).detach(e);
		}
		else
		{
			((EditorNode) e.getStartNode()).detach(e);
		}
		e.getEndNode().detach(e);

		edges.remove(e);
		final EdgeSubject subject = e.getSubject();
		final Collection<EdgeSubject> subjects = graph.getEdgesModifiable();
		subjects.remove(subject);

		repaint();
	}


	public void addLabel(EditorNode o, String label)
	{
		labels.add(new EditorLabel(o, label, this));
		repaint();
	}

	public void addLabelGroup(EditorEdge edge)
	{
		EditorLabelGroup labelGroup = 
			new EditorLabelGroup(edge, this);
		events.add(labelGroup);
		edge.setEditorLabelGroup(labelGroup);
		repaint();
	}

	public void addLabelGroup(EditorLabelGroup g)
	{
		events.add(g);
		repaint();
	}

	public void addGuardActionBlock(EditorEdge edge) 
	{
		if (edge.getSubject().getGuardActionBlock() == null) 
		{
			edge.getSubject().setGuardActionBlock(new GuardActionBlockSubject("", "", null));
		}
		EditorGuardActionBlock guardActionBlock = 
			new EditorGuardActionBlock(edge, this);
		mGuardActionBlocks.add(guardActionBlock);
		edge.setEditorGuardActionBlock(guardActionBlock);
		repaint();
	}

	public void removeGuardActionBlock(EditorEdge edge) 
	{
		EditorGuardActionBlock gab = edge.getEditorGuardActionBlock();
		mGuardActionBlocks.remove(gab);
		edge.setEditorGuardActionBlock(null);
		edge.getSubject().setGuardActionBlock(null);
		repaint();
	}

	/*
	public EditorLabel getLastLabel()
	{
		if (labels.size() > 0)
		{
			return (EditorLabel) labels.get(labels.size() - 1);
		}

		return null;
	}
	*/

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

	public void delNodeGroup(EditorNodeGroup n)
	{
		for (int i = edges.size()-1; i >= 0; i--)
		{
			EditorEdge e = (EditorEdge) edges.get(i);

			if (e.getStartNode() == n)
			{
				delEdge(e);
			}
		}

		nodeGroups.remove(n);
		final Collection<NodeSubject> subjects = graph.getNodesModifiable();
		final NodeSubject subject = n.getSubject();
		subjects.remove(subject);

		examineCollisions();
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
	
	public void delGuardActionBlock(EditorGuardActionBlock gA)
	{
		mGuardActionBlocks.remove(gA);
		gA.removeFromSurface(this);
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
		
		for (int i = 0; i < mGuardActionBlocks.size(); i++)
		{
			((EditorGuardActionBlock) mGuardActionBlocks.get(i)).removeFromSurface(this);
		}

		events.clear();
		mGuardActionBlocks.clear();
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

		// But edges before nodegroups, because they are hiding the edge handles otherwise...
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

		for (int i = 0; i < mGuardActionBlocks.size(); i++)
		{
			if (((EditorGuardActionBlock) mGuardActionBlocks.get(i)).wasClicked(ex, ey))
			{
				return (EditorLabelGroup) mGuardActionBlocks.get(i);
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
		LinkedList selection = new LinkedList();

		// The bounds of the drag
		Rectangle bounds = getDragRectangle().getBounds();
		
		for (int i = 0; i < nodeGroups.size(); i++)
		{
			EditorNodeGroup ng = (EditorNodeGroup) nodeGroups.get(i);

			if (bounds.contains(ng.getBounds()))
			{
				selection.add(ng);
			}
		}

		for (int i = 0; i < nodes.size(); i++)
		{
			EditorNode n = (EditorNode) nodes.get(i);
			
			if (bounds.contains(n.getRectangularOutline()))
			{
				selection.add(n);
			}
		}
		
		for (int i = 0; i < edges.size(); i++)
		{
			EditorEdge e = (EditorEdge) edges.get(i);
			
			if (bounds.contains(e.getSourceHandle()) && bounds.contains(e.getCenterHandle()) && bounds.contains(e.getTargetHandle()))
			{
				selection.add(e);
			}
		}
		
		for (int i = 0; i < labels.size(); i++)
		{
			EditorLabel l = (EditorLabel) labels.get(i);
			
			if (bounds.contains(l.getBounds()))
			{
				selection.add(l);
			}
		}
		
		for (int i = 0; i < events.size(); i++)
		{
			EditorLabelGroup l = (EditorLabelGroup) events.get(i);
			
			if (bounds.contains(l.getBounds()))
			{
				selection.add(l);
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

 	/* Obsolete? TODO:check and remove
 	public void unselectAll()
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

		for (int i = 0; i < mGuardActionPairs.size(); i++)
		{
			EditorGuardActionBlock gA = 
				(EditorGuardActionBlock) mGuardActionPairs.get(i);

			gA.setSelected(false);
			mGuardActionPairs.set(i, gA);
		}

		for (int i = 0; i < nodeGroups.size(); i++)
		{
			EditorNodeGroup n = (EditorNodeGroup) nodeGroups.get(i);

			n.setSelected(false);
			nodeGroups.set(i, n);
		}
	}*/

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
	public void loadElement(ModuleSubject module, AbstractSubject element)
	{
		ArrayList unloadedNodeGroups = new ArrayList();
		ArrayList unloadedEdges = new ArrayList();

		if (isSimpleComponentSubject(element))
		{
			SimpleComponentSubject cp = (SimpleComponentSubject) element;
			ArrayList noGeometry = new ArrayList();

			if (graph != null)
			{
				graph.removeModelObserver(this);
			}
			graph = cp.getGraph();
			graph.addModelObserver(this);
			
			events.add(new EditorLabelGroup(graph.getBlockedEvents(), this));

			final Collection mNodes = graph.getNodes();
			final Iterator iter = mNodes.iterator();

			while (iter.hasNext())
			{
				NodeSubject temp = (NodeSubject) iter.next();

				if (temp instanceof SimpleNodeSubject)
				{
					SimpleNodeSubject np = (SimpleNodeSubject) temp;

					if (np.getPointGeometry() == null)
					{
						noGeometry.add(np);
					}
					else
					{
						addNode(np);
					}
				}
				else if (temp instanceof GroupNodeSubject)
				{
					GroupNodeSubject gn = (GroupNodeSubject) temp;

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

			for (y = gridSize - EditorNode.RADIUS; y < getHeight();
					y += gridSize)
			{
				for (x = gridSize - EditorNode.RADIUS; x < getWidth();
						x += gridSize)
				{
					r.setRect(x, y, gridSize * (size - 1) * 4 + EditorNode.RADIUS*2, gridSize * (size - 1) * 4 + EditorNode.RADIUS*2);

					found = true;

					for (int i = 0; i < nodes.size(); i++)
					{
						EditorNode n = (EditorNode) nodes.get(i);

						if (n.getEllipsicalOutline().intersects(r))
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
				for (int j = 0; (j < size) && (i * size + j < noGeometry.size()); j++)
				{
					if (noGeometry.get(i * size + j) instanceof SimpleNodeSubject)
					{
						SimpleNodeSubject np = (SimpleNodeSubject) noGeometry.get(i * size + j);
						Point2D.Double p = new Point2D.Double(x + (j * gridSize * 4) + EditorNode.RADIUS,
										      y + (i * gridSize * 4) + EditorNode.RADIUS);
						np.setPointGeometry(new PointGeometrySubject(p));
						addNode(np);
					}
				}
			}

			java.util.List edges = graph.getEdges();
			for (int j = 0; j < edges.size(); j++)
			{
				EdgeSubject e = (EdgeSubject) edges.get(j);
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
						EditorEdge edge = addEdge(s, t, e);
						//edge.setHash(e.hashCode());
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
	 * This should adjust the size of the controlled surface to the minimum required
	 */
	public void minimizeSize()
	{
		Rectangle area = getDrawnAreaBounds();

		int width = (int) area.getWidth();
		int height = (int) area.getHeight();

 		/* // We want the bounds to be tight, right? Or why was this? 
		if (width < 500)
		{
			width = 500;
		}

		if (height < 500)
		{
			height = 500;
		}
		*/

		setPreferredSize(new Dimension(width + gridSize * 10, height + gridSize * 10));
	}

	/**
	 * Calculates and returns a rectangle that includes everything drawn on the surface.
	 */
	public Rectangle getDrawnAreaBounds()
	{
		// If we don't need to recalculate, don't!
		if (drawnAreaBounds != null)
		{
			return drawnAreaBounds;
		}

		// How much spacing should there be
		int SPACING = 0;

		// The extreme values
		double minX = Double.MAX_VALUE;
		double maxX = 0;
		double minY = Double.MAX_VALUE;
		double maxY = 0;

		// Local measures
		double x;
		double y;
		double mod;
		
		// Nodes...
		for (int i = 0; i < nodes.size(); i++)
		{
			EditorNode node = (EditorNode) nodes.get(i);
			
			x = node.getX();
			mod = node.RADIUS + 2 + SPACING; // The 2 is there to compensate for rounding?
			if (x + mod > maxX)
			{
				maxX = x + mod;
			}
			
			if (x - mod < minX)
			{
				minX = x - mod;
			}
			
			y = node.getY();
			if (y + mod > maxY)
			{
				maxY = y + mod;
			}
			
			if (y - mod < minY)
			{
				minY = y - mod;
			}
			
			// For the initial state, the arrow needs to be considered!
			if (node.isInitial())
			{
				x = x + (int) (EditorNode.INITARROWLENGTH * Math.sin(EditorNode.INITARROWANGLE));
				y = y + (int) (EditorNode.INITARROWLENGTH * Math.cos(EditorNode.INITARROWANGLE));
				
				if (x + mod > maxX)
				{
					maxX = x + mod;
				}
				
				if (x - mod < minX)
				{
					minX = x - mod;
				}
				
				if (y + mod > maxY)
				{
					maxY = y + mod;
				}
				
				if (y - mod < minY)
				{
					minY = y - mod;
				}
			}
		}
		
		// Nodegroups...
		for (int i = 0; i < nodeGroups.size(); i++)
		{
			EditorNodeGroup ng = (EditorNodeGroup) nodeGroups.get(i);
			Rectangle2D bound = ng.getBounds();
			x = bound.getX();
			mod = bound.getWidth() + 2 + SPACING;
			if (x + mod > maxX)
			{
				maxX = x + mod;
			}

			mod = 2 + SPACING;
			if (x - mod < minX)
			{
				minX = x - mod;
			}

			y = bound.getY();
			mod = bound.getHeight() + 2 + SPACING;
			if (y + mod > maxY)
			{
				maxY = y + mod;
			}

			mod = 2 + SPACING;
			if (y - mod < minY)
			{
				minY = y - mod;
			}
		}

		// Edges...
		for (int i = 0; i < edges.size(); i++)
		{
			EditorEdge edge = (EditorEdge) edges.get(i);

			// The bounds of the arrow
			x = edge.getTPointX();
			mod = 4 + SPACING;
			if (x + mod > maxX)
			{
				maxX = x + mod;
			}

			if (x - mod < minX)
			{
				minX = x - mod;
			}

			y = edge.getTPointY();
			if (y + mod > maxY)
			{
				maxY = y + mod;
			}

			if (y - mod < minY)
			{
				minY = y - mod;
			}

			// The bounds of the curve
			if (edge.isSelfLoop())
			{
				ArrayList loop = edge.createTear();
				Arc2D.Double arc = (Arc2D.Double) loop.get(0);

				Rectangle bound = arc.getBounds();
				x = bound.getX();
				mod = bound.getWidth() + SPACING;
				if (x + mod > maxX)
				{
					maxX = x + mod;
				}
				
				mod = 2 + SPACING;
				if (x - mod < minX)
				{
					minX = x - mod;
				}
				
				y = bound.getY();
				mod = bound.getHeight() + SPACING;
				if (y + mod > maxY)
				{
					maxY = y + mod;
				}
				
				mod = 2 + SPACING;
				if (y - mod < minY)
				{
					minY = y - mod;
				}
			}
			else
			{
				// Approximate curve
				QuadCurve2D.Double curve = edge.getCurve();
				FlatteningPathIterator it = 
					new FlatteningPathIterator(curve.getPathIterator(new AffineTransform()), 5.0, 5);
				while (!it.isDone())
				{
					double[] segment = new double[6];
					int type = it.currentSegment(segment);
					
					// Only care about the first point, it's not such a big deal, anyway?
					x = segment[0];
					mod = 2 + SPACING;
					if (x + mod > maxX)
					{
						maxX = x + mod;
					}
					
					if (x - mod < minX)
					{
						minX = x - mod;
					}
					
					y = segment[1];
					if (y + mod > maxY)
					{
						maxY = y + mod;
					}
					
					if (y - mod < minY)
					{
						minY = y - mod;
					}
					
					it.next();
				}
			}
		}

		// Node labels...
		for (int i = 0; i < labels.size(); i++)
		{
			EditorLabel label = ((EditorLabel) labels.get(i));
			x = label.getX();
			mod = label.getWidth()/2 - 2 + SPACING;
			if (x + mod > maxX)
			{
				maxX = x + mod;
			}

			if (x - mod < minX)
			{
				minX = x - mod;
			}

			y = label.getY();
			mod = label.getHeight()/2 - 2 + SPACING;
			if (y + mod > maxY)
			{
				maxY = y + mod;
			}

			if (y - mod < minY)
			{
				minY = y - mod;
			}
		}

		// Edge labels...
		for (int i = 0; i < events.size(); i++)
		{
			EditorLabelGroup labelGroup = ((EditorLabelGroup) events.get(i));
			x = labelGroup.getX(); // This is not the center!
			mod = labelGroup.getWidth() + SPACING;
			if (x + mod > maxX)
			{
				maxX = x + mod;
			}

			mod = SPACING;
			if (x - mod < minX)
			{
				minX = x - mod;
			}

			y = labelGroup.getY(); // This is not the center!
			mod = labelGroup.getHeight() + SPACING;
			if (y + mod > maxY)
			{
				maxY = y + mod;
			}

			mod = SPACING;
			if (y - mod < minY)
			{
				minY = y - mod;
			}
		}
		
		// Guard Action Blocks
		for (int i = 0; i < mGuardActionBlocks.size(); i++)
		{
			EditorGuardActionBlock gABlock = ((EditorGuardActionBlock) mGuardActionBlocks.get(i));
			x = gABlock.getX(); // This is not the center!
			mod = gABlock.getWidth() + SPACING;
			if (x + mod > maxX)
			{
				maxX = x + mod;
			}

			mod = SPACING;
			if (x - mod < minX)
			{
				minX = x - mod;
			}

			y = gABlock.getY(); // This is not the center!
			mod = gABlock.getHeight() + SPACING;
			if (y + mod > maxY)
			{
				maxY = y + mod;
			}

			mod = SPACING;
			if (y - mod < minY)
			{
				minY = y - mod;
			}
		}

		//Avoid stupid values
		if ((maxX < minX) || (maxY < minY))
		{
			drawnAreaBounds = new Rectangle(0,0,0,0);
		}
		else
		{
			drawnAreaBounds = new Rectangle((int) minX, (int) minY, (int) (maxX-minX),(int) (maxY-minY));
		}

		return drawnAreaBounds;
	}

	/**
	 * Returns true if the nodegroup ng is colliding with anything (a node).
	 */
	public boolean nodeGroupIsColliding(EditorNodeGroup ng)
	{
		Rectangle2D r = ng.getBounds();
		Rectangle2D e = new Rectangle2D.Double();

		for (int i = 0; i < nodes.size(); i++)
		{
			EditorNode n = (EditorNode) nodes.get(i);

			e.setFrameFromCenter(n.getX(), n.getY(), n.getX() + n.RADIUS, n.getY() + n.RADIUS);

			if (r.intersects(e) &&!r.contains(e))
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * Returns true if the node n is colliding with anything (another node or a nodegroup).
	 */
	public boolean nodeIsColliding(EditorNode n)
	{
		Rectangle2D.Double e = new Rectangle2D.Double();
		e.setFrameFromCenter(n.getX(), n.getY(), n.getX() + n.RADIUS, n.getY() + n.RADIUS);

		for (int i = 0; i < nodeGroups.size(); i++)
		{
			EditorNodeGroup ng = (EditorNodeGroup) nodeGroups.get(i);
			Rectangle2D r = ng.getBounds();

			if (r.intersects(e) &&!r.contains(e))
			{
				return true;
			}
		}

		for (int i = 0; i < nodes.size(); i++)
		{
			EditorNode n2 = (EditorNode) nodes.get(i);

			if ((n != n2) && Math.sqrt(Math.pow(n.getX()-n2.getX(), 2) + Math.pow(n.getY()-n2.getY(), 2)) < n2.RADIUS*2+2)
			{
				return true;
			}
		}

		return false;
	}

	// TODO: This should take a ModuleSubject as a parameter
	public EditorSurface()
	{
		
		nodes = new ArrayList();
		nodeGroups = new ArrayList();
		edges = new ArrayList();
		labels = new ArrayList();
		lines = new ArrayList();
		shades = new ArrayList();
		events = new ArrayList();
		mGuardActionBlocks = new ArrayList();

		shades.add(new EditorShade("Default", 255, 255, 255));

		shade = (EditorShade) shades.get(0);
	}

	/**
	 * Implementation of the Printable interface.
	 */
	public int print(Graphics g, PageFormat pageFormat, int page) 
	{  
		final double INCH = 72;
		
        Graphics2D g2d;
		
        // Validate the page number, we only print the first page
        if (page == 0) 
		{
			g2d = (Graphics2D) g;

			// Translate the origin so that (0,0) is in the upper left corner of the printable area
			g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

			/*
			// Print page margin
			{
				Line2D.Double line = new Line2D.Double ();
				int i;
				g2d.setColor(Color.ORANGE);

				// Draw the vertical lines
				line.setLine (0, 0, 0, pageFormat.getHeight());
				g2d.draw (line);
				line.setLine (pageFormat.getImageableWidth(), 0, pageFormat.getImageableWidth(), pageFormat.getImageableHeight());
				g2d.draw (line);
				
				// Draw the horizontal lines
				line.setLine (0, 0, pageFormat.getImageableWidth(), 0);
				g2d.draw (line);
				line.setLine (0, pageFormat.getImageableHeight(), pageFormat.getImageableWidth(), pageFormat.getImageableHeight());
				g2d.draw (line);
			}
			*/

			// Get the bounds of the actual drawing
			Rectangle area = getDrawnAreaBounds();
 
			// This is the place to do rescaling if the figure won't fit on the page!
			double scaleX = pageFormat.getImageableWidth() / (area.getWidth());
			double scaleY = pageFormat.getImageableHeight() / (area.getHeight());
			double scale = scaleX < scaleY ? scaleX : scaleY;
			if (scale < 1)
			{
				System.out.println("Rescaling figure to fit page. Scale: " + scale);
				g2d.scale(scale, scale);
			}   

			// Put drawing at (0, 0)
			g2d.translate(-area.getX(), -area.getY());

			// Put the current figure into the Graphics object!
			print(g);

			// OK to print!
			return (PAGE_EXISTS);
        }
        else
		{
			return (NO_SUCH_PAGE);
		}
	}

	public void repaint(boolean boundsMaybeChanged)
	{
		if (boundsMaybeChanged)
		{
			minimizeSize();
			drawnAreaBounds = null;
		}

		super.repaint();
	}

	public void repaint()
	{
		repaint(true);
	}

    public EditorWindowInterface getEditorInterface()
    {
		return root;
    }

    public java.util.List getNodes()
    {
		return nodes;
    }

    public java.util.List getNodeGroups()
    {
		return nodeGroups;
    }

    public java.util.List getEdges()
    {
		return edges;
    }

    public java.util.List getLabels()
    {
		return labels;
    }

    public java.util.List getLabelGroups()
    {
		return events;
    }
    
    public java.util.List getGuardActionBlocks()
    {
    	return mGuardActionBlocks;
    }
}

