//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   EditorNodeGroup
//###########################################################################
//# $Id: EditorNodeGroup.java,v 1.22 2006-03-23 16:06:03 flordal Exp $
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
		resizingFrom = 0;
	}

	public int hashCode()
	{
		return subject.hashCode();
	}

	private Rectangle2D[] getCorners()
	{
		Rectangle2D[] corners = new Rectangle2D[8];
		Rectangle2D bounds = getBounds();
		for (int i = 0; i < corners.length; i++)
		{
			corners[i] = new Rectangle();
		}
		corners[UPPERLEFT].setFrameFromCenter(bounds.getMinX(), bounds.getMinY(), bounds.getMinX() + 2*TOLERANCE, bounds.getMinY() + 2*TOLERANCE);
		corners[UPPERRIGHT].setFrameFromCenter(bounds.getMaxX(), bounds.getMinY(), bounds.getMaxX() + 2*TOLERANCE, bounds.getMinY() + 2*TOLERANCE);
		corners[LOWERLEFT].setFrameFromCenter(bounds.getMinX(), bounds.getMaxY(), bounds.getMinX() + 2*TOLERANCE, bounds.getMaxY() + 2*TOLERANCE);
		corners[LOWERRIGHT].setFrameFromCenter(bounds.getMaxX(), bounds.getMaxY(), bounds.getMaxX() + 2*TOLERANCE, bounds.getMaxY() + 2*TOLERANCE);
		corners[UPPERMIDDLE].setFrameFromCenter(bounds.getCenterX(), bounds.getMinY(), bounds.getCenterX() + 2*TOLERANCE, bounds.getMinY() + 2*TOLERANCE);
		corners[MIDDLERIGHT].setFrameFromCenter(bounds.getMaxX(), bounds.getCenterY(), bounds.getMaxX() + 2*TOLERANCE, bounds.getCenterY() + 2*TOLERANCE);
		corners[LOWERMIDDLE].setFrameFromCenter(bounds.getCenterX(), bounds.getMaxY(), bounds.getCenterX() + 2*TOLERANCE, bounds.getMaxY() + 2*TOLERANCE);
		corners[MIDDLELEFT].setFrameFromCenter(bounds.getMinX(), bounds.getCenterY(), bounds.getMinX() + 2*TOLERANCE, bounds.getCenterY() + 2*TOLERANCE);
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
		// There's a bug here, resizing does not work properly if you
		// drag a control point past one of its neighbours (and so
		// "inverting" the rectangle)!

		GeometryProxy old = new BoxGeometrySubject((Rectangle2D)subject.getGeometry().getRectangle().clone());
		Rectangle2D bounds = new Rectangle2D.Double();
		Rectangle2D[] corners = getCorners();
		Point2D point = new Point2D.Double();
		// Is it a side or a corner control point?
		if (resizingFrom % 2 == 0)
		{
			// Resizing from a corner
			point.setLocation(corners[resizingFrom].getCenterX(),
							  corners[resizingFrom].getCenterY());
		}
		else
		{
			// Resizing from a side
			int i = resizingFrom + 1;
			if (i >= 8)
			{
				i -= 8;
			}
			point.setLocation(corners[resizingFrom - 1].getCenterX(),
							  corners[resizingFrom - 1].getCenterY());
			if (resizingFrom % 4 == 1)
			{
				x = (int)corners[i].getCenterX();
			}
			else
			{
				y = (int)corners[i].getCenterY();
			}
		} 
		//System.out.println("x " + point.getX() + " y " + point.getY() + " x " + x + " y " + y);
		bounds.setFrameFromDiagonal(point.getX(), point.getY(), x, y);
		subject.getGeometry().setRectangle(bounds);
		fireEditorChangedEvent(new NodeMovedEvent(old, subject.getGeometry(),
												  subject));
	}   

	public void setPosition(double x, double y)
	{
		Rectangle2D bounds = getBounds();
		int dx = (int)x - (int) bounds.getX();
		int dy = (int)y - (int) bounds.getY();

		GeometryProxy old = new BoxGeometrySubject((Rectangle2D)subject.getGeometry().getRectangle().clone());
		//System.out.println("old1 :" + ((BoxGeometryProxy)old).getRectangle());
		// Move rect
		bounds.setRect(bounds.getX() + dx, bounds.getY() + dy, bounds.getWidth(), bounds.getHeight());
		subject.getGeometry().setRectangle(bounds);
		//System.out.println("old2 :" + ((BoxGeometryProxy)old).getRectangle());
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
		//System.out.println("start");
		//System.out.println(x + "," + y);
		Rectangle2D[] corners = getCorners();
		Line2D.Double closest = new Line2D.Double(corners[0].getCenterX(), corners[0].getCenterY(),
							  corners[6].getCenterX(), corners[6].getCenterY());

		for (int i = 2; i < corners.length; i += 2)
		{
			Line2D.Double l = new Line2D.Double(corners[i].getCenterX(), corners[i].getCenterY(),
							    corners[i - 2].getCenterX(), corners[i - 2].getCenterY());

			if (closest.ptLineDist(x, y) > l.ptLineDist(x, y))
			{
				closest = l;
			}
		}
		//System.out.println(closest.getP1());
		//System.out.println(closest.getP2());
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
		//System.out.println(x + "," + y);
		return (new Point2D.Double(x, y));
	}

	public boolean wasClicked(int cX, int cY)
	{
		Rectangle2D[] corners = getCorners();
		resizing = false;

		for (int i = 0; i < corners.length; i++)
		{
			if (corners[i].contains(cX, cY))
			{
				resizing = true;

				int h = i - corners.length/2;

				if (h < 0)
				{
					h += corners.length;
				}

				resizingFrom = h;

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

	public void drawObject(Graphics g, boolean selected)
	{
		Graphics2D g2d = (Graphics2D) g;
		Rectangle2D bounds = getBounds();

		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setColor(getColor(selected));

		if (selected)
		{
			Rectangle2D[] corners = getCorners();
			// Draw handles
			for (int i = 0; i < corners.length; i++)
			{				
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
			g2d.setColor(getShadowColor(selected));
			g2d.drawRoundRect((int) bounds.getX(), (int) bounds.getY(),
					  (int) bounds.getWidth(), (int) bounds.getHeight(), CORNERDIAMETER, CORNERDIAMETER);
			g2d.setColor(getColor(selected));
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
	private int resizingFrom = -1;
	private Collection<Observer> mObservers = new ArrayList<Observer>();
	private ArrayList immediateChildren = new ArrayList();
	private GroupNodeSubject subject;
	private static int UPPERLEFT = 0;
	private static int UPPERMIDDLE = 1;
	private static int UPPERRIGHT = 2;
	private static int MIDDLERIGHT = 3;
	private static int LOWERRIGHT = 4;
	private static int LOWERMIDDLE = 5;
	private static int LOWERLEFT = 6;
	private static int MIDDLELEFT = 7;
	private static int CORNERDIAMETER = 15;
	private static int TOLERANCE = 4;
	private static int HANDLEWIDTH = 4;
}
