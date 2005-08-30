
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.gui
//# CLASS:   EditorNodeGroup
//###########################################################################
//# $Id: EditorNodeGroup.java,v 1.13 2005-08-30 00:18:45 siw4 Exp $
//###########################################################################
package net.sourceforge.waters.gui;

import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import javax.xml.bind.JAXBException;
import net.sourceforge.waters.model.base.*;
import net.sourceforge.waters.model.module.*;
import net.sourceforge.waters.model.expr.*;
import net.sourceforge.waters.xsd.module.AnchorPosition;
import java.util.ArrayList;

public class EditorNodeGroup
	extends EditorObject
{
	private boolean resizing;
	private Point2D.Double resizingFrom;
	private Rectangle2D.Double bounds;
	private Rectangle[] corners = new Rectangle[4];
	private ArrayList points = new ArrayList();
	private ArrayList ratios = new ArrayList();
	private ArrayList edges = new ArrayList();
	private ArrayList immediateChildren = new ArrayList();
	private GroupNodeProxy proxy;
	private static int UPPERLEFT = 0;
	private static int UPPERRIGHT = 1;
	private static int LOWERRIGHT = 2;
	private static int LOWERLEFT = 3;
	private static int CORNERDIAMETER = 15;
	private static int TOLERANCE = 4;
	private static int HANDLEWIDTH = 4;

	public EditorNodeGroup(GroupNodeProxy gn)
	{
		// This is a nodegroup
		type = NODEGROUP;

		proxy = gn;

		if (gn.getGeometry() == null)
		{
			gn.setGeometry(new BoxGeometryProxy(1000, 1000, 10, 10));
		}

		bounds = new Rectangle2D.Double();

		bounds.setRect(gn.getGeometry().getRectangle());
		gn.getGeometry().setRectangle(bounds);

		resizing = bounds.isEmpty();
		resizingFrom = new Point2D.Double(bounds.getMinX(), bounds.getMinY());
		setSelected(false);

		for (int i = 0; i < 4; i++)
		{
			corners[i] = new Rectangle();
		}

		setCorners();
	}

	public int hashCode()
	{
		return proxy.hashCode();
	}

	private void setCorners()
	{
		corners[UPPERLEFT].setFrameFromCenter(bounds.getMinX(), bounds.getMinY(), bounds.getMinX() + 2*TOLERANCE, bounds.getMinY() + 2*TOLERANCE);
		corners[UPPERRIGHT].setFrameFromCenter(bounds.getMaxX(), bounds.getMinY(), bounds.getMaxX() + 2*TOLERANCE, bounds.getMinY() + 2*TOLERANCE);
		corners[LOWERLEFT].setFrameFromCenter(bounds.getMinX(), bounds.getMaxY(), bounds.getMinX() + 2*TOLERANCE, bounds.getMaxY() + 2*TOLERANCE);
		corners[LOWERRIGHT].setFrameFromCenter(bounds.getMaxX(), bounds.getMaxY(), bounds.getMaxX() + 2*TOLERANCE, bounds.getMaxY() + 2*TOLERANCE);
	}

	public boolean getResizing()
	{
		return resizing;
	}

	public void setResizingFalse()
	{
		resizing = false;
	}

	public void resize(int x, int y)
	{
		bounds.setFrameFromDiagonal(resizingFrom.getX(), resizingFrom.getY(), x, y);

		for (int i = 0; i < points.size(); i++)
		{
			Point2D.Double p = (Point2D.Double) points.get(i);
			Point2D.Double r = (Point2D.Double) ratios.get(i);
			double ox = p.getX();
			double oy = p.getY();

			p.setLocation(bounds.getMinX() + r.getX() * bounds.getWidth(), bounds.getMinY() + r.getY() * bounds.getHeight());
			((EditorEdge) edges.get(i)).updateControlPoint(ox, oy, true);
		}

		setCorners();
	}   

	//public void moveGroupTo(int x, int y)
	public void setPosition(double x, double y)
	{
		int dx = (int)x - (int) bounds.getX();
		int dy = (int)y - (int) bounds.getY();

		// Move points
		for (int i = 0; i < points.size(); i++)
		{
			Point2D.Double p = (Point2D.Double) points.get(i);

			p.setLocation(p.getX() + dx, p.getY() + dy);
			((EditorEdge) edges.get(i)).updateControlPoint(p.getX() - dx, p.getY() - dy, true);
		}

		// Move rect
		bounds.setRect(bounds.getX() + dx, bounds.getY() + dy, bounds.getWidth(), bounds.getHeight());
		setCorners();
	}

    public Point2D getPosition()
    {
	return new Point2D.Double(getX(),getY());
    }

	public int getX()
	{
		return (int) corners[UPPERLEFT].getCenterX();
	}

	public int getY()
	{
		return (int) corners[UPPERLEFT].getCenterY();
	}

	public Point2D.Double setOnBounds(int x, int y)
	{
		Line2D.Double closest = new Line2D.Double(corners[0].getCenterX(), corners[0].getCenterY(), corners[3].getCenterX(), corners[3].getCenterY());

		for (int i = 1; i < 4; i++)
		{
			Line2D.Double l = new Line2D.Double(corners[i].getCenterX(), corners[i].getCenterY(), corners[i - 1].getCenterX(), corners[i - 1].getCenterY());

			if (closest.ptLineDist(x, y) > l.ptLineDist(x, y))
			{
				closest = l;
			}
		}

		if (closest.getX1() == closest.getX2())
		{
			x = (int) closest.getX1();

			if (closest.ptLineDist(x, y) > .2)
			{
				if (Math.abs(closest.getY1() - y) < (closest.getY2() - y))
				{
					y = (int) closest.getY1();
				}
				else
				{
					y = (int) closest.getY2();
				}
			}
		}
		else
		{
			y = (int) closest.getY1();

			if (closest.ptLineDist(x, y) > .2)
			{
				if (Math.abs(closest.getX1() - x) < (closest.getX2() - x))
				{
					x = (int) closest.getX1();
				}
				else
				{
					x = (int) closest.getX2();
				}
			}
		}

		return (new Point2D.Double(x, y));
	}

	public Point2D.Double getPosition(int x, int y, EditorEdge e)
	{
		Point2D.Double p = setOnBounds(x, y);
		Point2D.Double r = new Point2D.Double((double) ((p.getX() - bounds.getMinX()) / bounds.getWidth()), (double) ((p.getY() - bounds.getMinY()) / bounds.getHeight()));

		points.add(p);
		ratios.add(r);
		edges.add(e);

		return p;
	}

	public void removePosition(Point2D p)
	{
		for (int j = 0; j< points.size(); j++)
		{
			Point2D point = (Point2D) points.get(j);
		}

		int i = points.indexOf(p);

		if (i < 0)
		{
			// Already removed?
			return;
		}
		points.remove(i);
		ratios.remove(i);
		edges.remove(i);
	}

	public boolean wasClicked(int cX, int cY)
	{
		resizing = false;

		for (int i = 0; i < 4; i++)
		{
			if (corners[i].contains(cX, cY))
			{
				resizing = true;

				int h = i - 2;

				if (h < 0)
				{
					h += 4;
				}

				resizingFrom.setLocation(corners[h].getCenterX(), corners[h].getCenterY());

				break;
			}
		}

		Rectangle2D.Double innerBounds = new Rectangle2D.Double(bounds.getX() + TOLERANCE, bounds.getY() + TOLERANCE, bounds.getWidth() - 2*TOLERANCE, bounds.getHeight() - 2*TOLERANCE);

		return (bounds.intersects(cX - TOLERANCE, cY - TOLERANCE, 2*TOLERANCE, 2*TOLERANCE) && !innerBounds.contains(cX, cY, 1, 1));
	}

	public Rectangle2D.Double getBounds()
	{
		return bounds;
	}

	public void setBounds(Rectangle2D.Double b)
	{
		bounds.setRect(b);

		for (int i = 0; i < points.size(); i++)
		{
			Point2D.Double p = (Point2D.Double) points.get(i);
			Point2D.Double r = (Point2D.Double) ratios.get(i);
			double ox = p.getX();
			double oy = p.getY();

			p.setLocation(bounds.getMinX() + r.getX() * bounds.getWidth(), bounds.getMinY() + r.getY() * bounds.getHeight());
			((EditorEdge) edges.get(i)).updateControlPoint(ox, oy, true);
		}

		setCorners();
	}

	public boolean isEmpty()
	{
		return bounds.isEmpty();
	}

	public void setProxy(GroupNodeProxy gn)
	{
		proxy = gn;
	}

	public GroupNodeProxy getProxy()
	{
		return proxy;
	}

	/**
	 * @return false if something went wrong, true otherwise.
	 */
	public boolean setChildNodes(ArrayList children, JComponent c)
	{
		boolean fail = false;
		ArrayList a = new ArrayList(children.size());

		for (int i = 0; i < children.size(); i++)
		{
			if (children.get(i) instanceof EditorNode)
			{
				a.add(((EditorNode) children.get(i)).getProxy());
			}
			else if (children.get(i) instanceof EditorNodeGroup)
			{
				a.add(((EditorNodeGroup) children.get(i)).getProxy());
			}
		}

		try
		{
			proxy.setImmediateChildNodes(a);
		}
		catch (final CyclicGroupNodeException e)
		{
			JOptionPane.showMessageDialog(c, e.getMessage());

			fail = true;
		}
		catch (final DuplicateNameException e)
		{
			JOptionPane.showMessageDialog(c, e.getMessage());

			fail = true;
		}

		if (fail)
		{
			return false;
		}

		immediateChildren = children;

		return true;
	}

	public void setSelected(boolean s)
	{
		super.setSelected(s);

		/*
		if (s == true)
		{
			for (int i = 0; i < immediateChildren.size(); i++)
			{
				((EditorObject) immediateChildren.get(i)).setSelected();
			}
		}
		*/
	}

	public void drawObject(Graphics g)
	{
		Graphics2D g2d = (Graphics2D) g;

		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setColor(getColor());

		if (isSelected())
		{
			// Draw handles
			for (int i = 0; i < 4; i++)
			{
				g2d.fillRect((int) corners[i].getCenterX() - HANDLEWIDTH/2, (int) corners[i].getCenterY() - HANDLEWIDTH/2, HANDLEWIDTH, HANDLEWIDTH);
				//g2d.drawOval((int) corners[i].getCenterX() - HANDLEWIDTH/2, (int) corners[i].getCenterY() - HANDLEWIDTH/2, HANDLEWIDTH, HANDLEWIDTH);
			}
		}

		// Draw shadow
		if (shadow && isHighlighted())
		{
			g2d.setStroke(SHADOWSTROKE);
			g2d.setColor(getShadowColor());
			g2d.drawRoundRect((int) bounds.getX(), (int) bounds.getY(), (int) bounds.getWidth(), (int) bounds.getHeight(), CORNERDIAMETER, CORNERDIAMETER);
			g2d.setColor(getColor());
			g2d.setStroke(BASICSTROKE);
		}

		g2d.setStroke(DOUBLESTROKE);
		g2d.drawRoundRect((int) bounds.getX(), (int) bounds.getY(), (int) bounds.getWidth(), (int) bounds.getHeight(), CORNERDIAMETER, CORNERDIAMETER);
		g2d.setStroke(BASICSTROKE);
	}
}
