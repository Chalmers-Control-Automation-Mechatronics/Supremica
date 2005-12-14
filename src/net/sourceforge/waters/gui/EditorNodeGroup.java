//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   EditorNodeGroup
//###########################################################################
//# $Id: EditorNodeGroup.java,v 1.17 2005-12-14 03:09:47 siw4 Exp $
//###########################################################################


package net.sourceforge.waters.gui;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.*;

import net.sourceforge.waters.model.base.GeometryProxy;

import net.sourceforge.waters.model.module.BoxGeometryProxy;

import net.sourceforge.waters.subject.module.BoxGeometrySubject;
import net.sourceforge.waters.subject.module.GroupNodeSubject;
import net.sourceforge.waters.subject.module.NodeSubject;
import net.sourceforge.waters.xsd.module.AnchorPosition;

import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.observer.NodeMovedEvent;
import net.sourceforge.waters.gui.observer.Observer;
import net.sourceforge.waters.gui.observer.Subject;

public class EditorNodeGroup
	extends EditorObject
	implements Subject
{

	public EditorNodeGroup(GroupNodeSubject gn)
	{
		// This is a nodegroup
		type = NODEGROUP;

		subject = gn;

		if (subject.getGeometry() == null)
		{
			final Rectangle2D box = new Rectangle(1000, 1000, 10, 10);
			final BoxGeometrySubject geo = new BoxGeometrySubject(box);
			subject.setGeometry(geo);
		}

		resizing = getBounds().isEmpty();
		resizingFrom = new Point2D.Double(getBounds().getMinX(), getBounds().getMinY());
		setSelected(false);
	}

	public int hashCode()
	{
		return subject.hashCode();
	}

	private Rectangle2D[] getCorners()
	{
		Rectangle2D[] corners = new Rectangle2D[4];
		Rectangle2D bounds = getBounds();
		for (int i = 0; i < 4; i++)
		{
			corners[i] = new Rectangle();
		}
		corners[UPPERLEFT].setFrameFromCenter(bounds.getMinX(), bounds.getMinY(), bounds.getMinX() + 2*TOLERANCE, bounds.getMinY() + 2*TOLERANCE);
		corners[UPPERRIGHT].setFrameFromCenter(bounds.getMaxX(), bounds.getMinY(), bounds.getMaxX() + 2*TOLERANCE, bounds.getMinY() + 2*TOLERANCE);
		corners[LOWERLEFT].setFrameFromCenter(bounds.getMinX(), bounds.getMaxY(), bounds.getMinX() + 2*TOLERANCE, bounds.getMaxY() + 2*TOLERANCE);
		corners[LOWERRIGHT].setFrameFromCenter(bounds.getMaxX(), bounds.getMaxY(), bounds.getMaxX() + 2*TOLERANCE, bounds.getMaxY() + 2*TOLERANCE);
		return corners;
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
		GeometryProxy old = new BoxGeometrySubject((Rectangle2D)subject.getGeometry().getRectangle().clone());
		Rectangle2D bounds = new Rectangle2D.Double();
		bounds.setFrameFromDiagonal(resizingFrom.getX(), resizingFrom.getY(), x, y);
		subject.getGeometry().setRectangle(bounds);
		fireEditorChangedEvent(new NodeMovedEvent(old, subject.getGeometry(),
												  subject));
	}   

	//public void moveGroupTo(int x, int y)
	public void setPosition(double x, double y)
	{
		Rectangle2D bounds = getBounds();
		int dx = (int)x - (int) bounds.getX();
		int dy = (int)y - (int) bounds.getY();

		GeometryProxy old = new BoxGeometrySubject((Rectangle2D)subject.getGeometry().getRectangle().clone());
		System.out.println("old1 :" + ((BoxGeometryProxy)old).getRectangle());
		// Move rect
		bounds.setRect(bounds.getX() + dx, bounds.getY() + dy, bounds.getWidth(), bounds.getHeight());
		subject.getGeometry().setRectangle(bounds);
		System.out.println("old2 :" + ((BoxGeometryProxy)old).getRectangle());
		fireEditorChangedEvent(new NodeMovedEvent(old, subject.getGeometry(),
												  subject));
	}

    public Point2D getPosition()
    {
		return new Point2D.Double(getX(), getY());
    }

	public int getX()
	{
		return (int) subject.getGeometry().getRectangle().getMinX();
	}

	public int getY()
	{
		return (int) subject.getGeometry().getRectangle().getMinY();
	}
	
	public Point2D setOnBounds(Point2D p)
	{
		return setOnBounds(p.getX(), p.getY());
	}

	public Point2D setOnBounds(double x, double y)
	{
		System.out.println("start");
		System.out.println(x + "," + y);
		Rectangle2D[] corners = getCorners();
		Line2D.Double closest = new Line2D.Double(corners[0].getCenterX(), corners[0].getCenterY(),
							  corners[3].getCenterX(), corners[3].getCenterY());

		for (int i = 1; i < 4; i++)
		{
			Line2D.Double l = new Line2D.Double(corners[i].getCenterX(), corners[i].getCenterY(),
							    corners[i - 1].getCenterX(), corners[i - 1].getCenterY());

			if (closest.ptLineDist(x, y) > l.ptLineDist(x, y))
			{
				closest = l;
			}
		}
		System.out.println(closest.getP1());
		System.out.println(closest.getP2());
		if (closest.getX1() == closest.getX2())
		{
			x = (int) closest.getX1();
			int y1;
			int y2;
			if (closest.getY1() < closest.getY2())
			{
				y1 = (int)closest.getY1();
				y2 = (int)closest.getY2();
			}
			else
			{
				y1 = (int)closest.getY2();
				y2 = (int)closest.getY1();
			}
			if (y < y1 || y > y2)
			{
				if (Math.abs(closest.getY1() - y) < Math.abs(closest.getY2() - y))
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
			int x1;
			int x2;
			if (closest.getX1() < closest.getX2())
			{
				x1 = (int)closest.getX1();
				x2 = (int)closest.getX2();
			}
			else
			{
				x1 = (int)closest.getX2();
				x2 = (int)closest.getX1();
			}
			if (x < x1 || x > x2)
			{
				if (Math.abs(closest.getX1() - x) < Math.abs(closest.getX2() - x))
				{
					x = (int) closest.getX1();
				}
				else
				{
					x = (int) closest.getX2();
				}
			}
		}
		System.out.println(x + "," + y);
		return (new Point2D.Double(x, y));
	}

	public boolean wasClicked(int cX, int cY)
	{
		Rectangle2D[] corners = getCorners();
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

		Rectangle2D.Double innerBounds = new Rectangle2D.Double(getBounds().getX() + TOLERANCE, getBounds().getY() + TOLERANCE, getBounds().getWidth() - 2*TOLERANCE, getBounds().getHeight() - 2*TOLERANCE);

		return (getBounds().intersects(cX - TOLERANCE, cY - TOLERANCE, 2*TOLERANCE, 2*TOLERANCE) && !innerBounds.contains(cX, cY, 1, 1));
	}

	public Rectangle2D getBounds()
	{
		return subject.getGeometry().getRectangle();
	}

	public void setBounds(Rectangle2D.Double b)
	{
		GeometryProxy old = new BoxGeometrySubject((Rectangle2D)subject.getGeometry().getRectangle().clone());
		subject.getGeometry().setRectangle(b);
		/*for (int i = 0; i < points.size(); i++)
		{
			Point2D.Double p = (Point2D.Double) points.get(i);
			Point2D.Double r = (Point2D.Double) ratios.get(i);
			double ox = p.getX();
			double oy = p.getY();

			p.setLocation(bounds.getMinX() + r.getX() * bounds.getWidth(), bounds.getMinY() + r.getY() * bounds.getHeight());
			((EditorEdge) edges.get(i)).updateControlPoint(ox, oy, true);
		}*/
		fireEditorChangedEvent(new NodeMovedEvent(old, subject.getGeometry(),
												  subject));
	}

	public boolean isEmpty()
	{
		return getBounds().isEmpty();
	}

	public GroupNodeSubject getSubject()
	{
		return subject;
	}

	/**
	 * @return false if something went wrong, true otherwise.
	 */
	public boolean setChildNodes(ArrayList children, JComponent c)
	{
		boolean fail = false;
		final Collection<NodeSubject> subjects =
			new ArrayList<NodeSubject>(children.size());
		for (final Object child : children) {
			if (child instanceof EditorNode) {
				final EditorNode enode = (EditorNode) child;
				final NodeSubject subject = enode.getSubject();
				subjects.add(subject);
			} else if (child instanceof EditorNodeGroup) {
				final EditorNodeGroup enode = (EditorNodeGroup) child;
				final NodeSubject subject = enode.getSubject();
				subjects.add(subject);
			}
		}
		subject.setImmediateChildNodes(subjects);
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
		Rectangle2D bounds = getBounds();

		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setColor(getColor());

		if (isSelected())
		{
			// Draw handles
			for (int i = 0; i < 4; i++)
			{
				Rectangle2D[] corners = getCorners();
				g2d.fillRect((int) corners[i].getCenterX() - HANDLEWIDTH/2, (int) corners[i].getCenterY() - HANDLEWIDTH/2,
					     HANDLEWIDTH, HANDLEWIDTH);
			      //g2d.drawOval((int) corners[i].getCenterX() - HANDLEWIDTH/2, (int) corners[i].getCenterY() - HANDLEWIDTH/2,
			      //HANDLEWIDTH, HANDLEWIDTH);
			}
		}

		// Draw shadow
		if (shadow && isHighlighted())
		{
			g2d.setStroke(SHADOWSTROKE);
			g2d.setColor(getShadowColor());
			g2d.drawRoundRect((int) bounds.getX(), (int) bounds.getY(),
					  (int) bounds.getWidth(), (int) bounds.getHeight(), CORNERDIAMETER, CORNERDIAMETER);
			g2d.setColor(getColor());
			g2d.setStroke(BASICSTROKE);
		}

		g2d.setStroke(DOUBLESTROKE);
		g2d.drawRoundRect((int) bounds.getX(), (int) bounds.getY(),
				  (int) bounds.getWidth(), (int) bounds.getHeight(), CORNERDIAMETER, CORNERDIAMETER);
		g2d.setStroke(BASICSTROKE);
	}
	
	public void attach(Observer o)
	{
		mObservers.add(o);
	}
	
	public void detach(Observer o)
	{
		mObservers.remove(o);
	}
	
	public void fireEditorChangedEvent(EditorChangedEvent e)
	{
		for (Observer o : mObservers)
		{
			o.update(e);
		}
	}

	//########################################################################
	//# Data Members
	private boolean resizing;
	private Point2D.Double resizingFrom;
	private Collection<Observer> mObservers = new ArrayList<Observer>();
	private ArrayList immediateChildren = new ArrayList();
	private GroupNodeSubject subject;
	private static int UPPERLEFT = 0;
	private static int UPPERRIGHT = 1;
	private static int LOWERRIGHT = 2;
	private static int LOWERLEFT = 3;
	private static int CORNERDIAMETER = 15;
	private static int TOLERANCE = 4;
	private static int HANDLEWIDTH = 4;

}
