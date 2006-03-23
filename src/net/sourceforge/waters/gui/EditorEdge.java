//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   EditorEdge
//###########################################################################
//# $Id: EditorEdge.java,v 1.37 2006-03-23 12:07:14 flordal Exp $
//###########################################################################

package net.sourceforge.waters.gui;

import java.util.ArrayList;
import java.util.List;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.*;

import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.observer.NodeMovedEvent;
import net.sourceforge.waters.gui.observer.Observer;
import net.sourceforge.waters.gui.observer.Subject;

import net.sourceforge.waters.model.module.BoxGeometryProxy;
import net.sourceforge.waters.model.module.PointGeometryProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;

import net.sourceforge.waters.subject.base.ModelChangeEvent;
import net.sourceforge.waters.subject.base.ModelObserver;
import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.NodeSubject;
import net.sourceforge.waters.subject.module.PointGeometrySubject;
import net.sourceforge.waters.subject.module.SplineGeometrySubject;

import net.sourceforge.waters.xsd.module.SplineKind;


/** 
 * <p>The editor's internal representation of edges.</p>
 *
 * <p>This represents edges within the graph of the component. It currently
 * supports Bezier-curve geometry, with a single control point only.</p>
 *
 * <p>The turning point of a curve is the point where the curve intersects
 * the line which is perpendicular to the line between the source
 * and target nodes.  This is used to represent the visible position of the
 * user-controllable point from which the control point is calculated.</p>
 *
 * @author Gian Perrone
 * @author Simon Ware
 */
public class EditorEdge
    extends EditorObject
    implements ModelObserver,
				Observer
{
	/** The start can be either a node or a nodegroup. */
	private EditorObject startNode; 
	/** The end is a node. */
	private EditorNode endNode;
	
	/** The label block (events) **/
	private EditorLabelGroup mEditorLabelGroup;
	
	/** The guard+action block **/
	private EditorGuardActionBlock mEditorGuardActionBlock;
	
	/** Boolean keeping track of whether the edge is a straight line or not. */
	private boolean straight;

	/** Arrows at end of edge or in the middle? */
	private static boolean arrowAtEnd = ModuleWindow.DES_COURSE_VERSION;

	// Handles
	private Rectangle2D.Double source = new Rectangle2D.Double();
	private boolean dragS = false;
	private Rectangle2D.Double target = new Rectangle2D.Double();	
	private boolean dragT = false;	
	private Rectangle2D.Double center = new Rectangle2D.Double();
	private boolean dragC = false;
	private EdgeSubject subject;
	private static double tearRatio = .8;
	private static int WIDTHD = 2;
	private static int WIDTHS = 5;
	private EditorSurface mParent;

	public EditorEdge(EditorObject iStartNode, EditorNode iEndNode,
			int x, int y, EdgeSubject e, EditorSurface parent)
	{
		// This is an edge
		type = EDGE;
		Point2D start = null;
		startNode = iStartNode;
		endNode = iEndNode;
		subject = e;
		endNode.attach(this);
		mParent = parent;
		if (startNode.getType() == NODE)
		{
			EditorNode s = (EditorNode) startNode;
			
			subject.setSource((NodeSubject) s.getSubject());
			
			start = s.getPosition();
			
			s.attach(this);
		}
		else
		{
			EditorNodeGroup s = (EditorNodeGroup) startNode;
			
			subject.setSource((NodeSubject) s.getSubject());
		
			start = s.setOnBounds(x, y);
			
			s.attach(this);
		}
		
		subject.setTarget((NodeSubject) iEndNode.getSubject());
		subject.setStartPoint(new PointGeometrySubject(start));
		subject.setEndPoint(new PointGeometrySubject(endNode.getPosition()));

		// Create new geometry if there is none
		if (subject.getGeometry() == null)
		{
			Point2D.Double tPoint = null;
			if (!isSelfLoop())
			{
				straight = true;
				tPoint = new Point2D.Double((start.getX() + endNode.getX()) / 2, (start.getY() + endNode.getY()) / 2);
			}
			else
			{
				tPoint = new Point2D.Double();
				straight = false;

				if (start.getX() - 30 >= 0)
				{
					tPoint.setLocation(start.getX() - 30, tPoint.getY());
				}
				else
				{
					tPoint.setLocation(start.getX() + 30, tPoint.getY());
				}

				if (start.getY() - 30 >= 0)
				{
					tPoint.setLocation(tPoint.getX(), start.getY() - 30);
				}
				else
				{
					tPoint.setLocation(tPoint.getX(), start.getY() + 30);
				}
			}

			ArrayList l = new ArrayList(1);

			l.add(tPoint);
			subject.setGeometry
				(new SplineGeometrySubject(l, SplineKind.INTERPOLATING));
		}
		else
		{
			final SplineGeometrySubject geo = subject.getGeometry();
			final List<Point2D> points = geo.getPointsModifiable();
			final Point2D point0 = points.get(0);
			final double x0 = point0.getX();
			final double y0 = point0.getY();
			if (onLine(x0, y0)) {
				straight = true;
			}
		}

		// Initialize the edge (somehow this strange call does exactly what
		// we want) Without this call, when the mouse is moved above the
		// handle of one of the _curved_ edges, the labelgroup on that edge
		// jumps to the side!! Presumably TPoint is not properly set before
		// this call...
		setPosition(getTPointX(), getTPointY());
	}
	
	public void update(EditorChangedEvent e)
	{
		//System.out.println("stuff");
		if (e.getType() == EditorChangedEvent.NODEMOVED)
		{
			//System.out.println("old start:" + getStartPoint());		
			NodeMovedEvent n = (NodeMovedEvent)e;
			boolean start = subject.getSource() == n.getNode();
			double oldx;
			double oldy;
			if (n.getNode() instanceof SimpleNodeProxy)
			{
				oldx = ((PointGeometryProxy)n.getOld()).getPoint().getX();
				oldy = ((PointGeometryProxy)n.getOld()).getPoint().getY();
				//System.out.println(oldx + " " + oldy);
				if (start)
				{
					subject.getStartPoint().setPoint(((PointGeometryProxy)
													  n.getNew()).getPoint());
				}
				else
				{
					subject.getEndPoint().setPoint(((PointGeometryProxy)
												  n.getNew()).getPoint());
				}
			}
			else
			{
				Rectangle2D oldR = ((BoxGeometryProxy)n.getOld()).getRectangle();
				Rectangle2D newR = ((BoxGeometryProxy)n.getNew()).getRectangle();
				//System.out.println("Old Geometry: " + oldR);
				//System.out.println("New Geometry: " + newR);
				if (start)
				{
					oldx = subject.getStartPoint().getPoint().getX();
					oldy = subject.getStartPoint().getPoint().getY();
				}
				else
				{
					oldx = subject.getEndPoint().getPoint().getX();
					oldy = subject.getEndPoint().getPoint().getY();
				}
				double dx = ((oldx - oldR.getMinX()) * (newR.getMaxX() - newR.getMinX()))
					 	/ (oldR.getMaxX() - oldR.getMinX());
				double dy = ((oldy - oldR.getMinY()) * (newR.getMaxY() - newR.getMinY()))
					 	/ (oldR.getMaxY() - oldR.getMinY());
				if (start)
				{
					subject.getStartPoint().setPoint(new Point2D.Double(newR.getMinX() + dx, newR.getMinY() + dy));
				}
				else
				{
					subject.getEndPoint().setPoint(new Point2D.Double(newR.getMinX() + dx, newR.getMinY() + dy));
				}
			}
			//System.out.println("Old TPoint: " + getPosition());
			updateControlPoint(oldx, oldy, start);
			//System.out.println("New Tpoint: " + getPosition());
			//System.out.println("New Start Point: " + getStartPoint());
		}
	}
	
	public Point2D getStartPoint()
	{
		return subject.getStartPoint().getPoint();
	}

	public double getCPointX()
	{
		double result;
		if (subject.getSource() == subject.getTarget())
		{
			return getPosition().getX();
		}

		return (2 * getPosition().getX() - (getStartPoint().getX() + endNode.getX()) / 2);
	}

	public double getCPointY()
	{
		if (subject.getSource() == subject.getTarget())
		{
			return getPosition().getY();
		}

		return (2 * getPosition().getY() - (getStartPoint().getY() + endNode.getY()) / 2);
	}

	public Point2D getCPoint()
	{
		return (new Point2D.Double(getCPointX(), getCPointY()));
	}

	public void setCPointX(double x)
	{
		setCPoint(x, getCPointY());
	}

	public void setCPointY(double y)
	{
		setCPoint(getCPointX(), y);
	}

	public void setCPoint(Point2D.Double p)
	{
		setCPoint(p.getX(), p.getY());
	}

	public void setCPoint(double x, double y)
	{
		if (subject.getSource() == subject.getTarget())
		{
			setPosition(x, y);
		}
		
		setPosition(.25 * (getStartPoint().getX() + 2 * x + endNode.getX()), .25 * (getStartPoint().getY() + 2 * y + endNode.getY()));
	}

	public Rectangle2D.Double getSourceHandle()
	{
		return source;
	}

	public Rectangle2D.Double getTargetHandle()
	{
		return target;
	}

	public Rectangle2D.Double getCenterHandle()
	{
		return center;
	}

	public void setSource(int x, int y)
	{
		source.setRect(x, y, 0, 0);
		center.setRect(0, 0, 0, 0);
		target.setRect(endNode.getX(), endNode.getY(), 0, 0);
	}

	public void setTarget(int x, int y)
	{
		source.setRect(getStartPoint().getX(), getStartPoint().getY(), 0, 0);
		center.setRect(0, 0, 0, 0);
		target.setRect(x, y, 0, 0);
	}

	public void setStartNode(EditorObject newstartNode, int x, int y)
	{
		double ox = -1;
		double oy = -1;

		if (startNode instanceof EditorNodeGroup)
		{
			if (newstartNode == startNode)
			{
				ox = getStartPoint().getX();
				oy = getStartPoint().getY();
			}

			EditorNodeGroup s = (EditorNodeGroup) startNode;

			s.detach(this);
		} 
		else 
		{
		    EditorNode s = (EditorNode)startNode;
		    s.detach(this);
		}

		startNode = newstartNode;

		if (startNode instanceof EditorNode)
		{
			EditorNode s = (EditorNode) startNode;

			subject.getStartPoint().setPoint(s.getPosition());

			subject.setSource(s.getSubject());

			s.attach(this);
		}
		else
		{
			EditorNodeGroup s = (EditorNodeGroup) startNode;

			subject.getStartPoint().setPoint(s.setOnBounds(x, y));
			
			s.attach(this);
			
			subject.setSource(s.getSubject());
		}

		if (ox != -1)
		{
			updateControlPoint(ox, oy, true);
		}
		else if (isSelfLoop())
		{
			setPosition(getStartPoint().getX() + 30, getStartPoint().getY() + 30);
		}
		else
		{
			setPosition((getStartPoint().getX() + endNode.getX()) / 2,
						(getStartPoint().getY() + endNode.getY()) / 2);
		}

		dragS = false;		
	}

	public void setEndNode(EditorNode newendNode)
	{
	    endNode.detach(this);
	    endNode = newendNode;
	    newendNode.attach(this);
	    if (isSelfLoop())
		{
		    setPosition(getStartPoint().getX() + 30,
						getStartPoint().getY() + 30);
		}
	    else
		{
		    setPosition((getStartPoint().getX() + endNode.getX()) / 2,
						(getStartPoint().getY() + endNode.getY()) / 2);
		}
	    
	    dragT = false;
	    
	    subject.setTarget(endNode.getSubject());
	    subject.getEndPoint().setPoint(endNode.getPosition());
	}

	public EditorObject getStartNode()
	{
	    return startNode;
	}
    
    public EditorNode getEndNode()
    {
	    return endNode;
	}

	public void setDragT(boolean d)
	{
		dragT = d;
	}

	public boolean getDragT()
	{
		return dragT;
	}

	public void setDragS(boolean d)
	{
		dragS = d;
	}

	public boolean getDragS()
	{
		return dragS;
	}

	public void setDragC(boolean d)
	{
		dragC = d;
	}

	public boolean getDragC()
	{
		return dragC;
	}

	/** 
	 * Get the X coordinate of the turning point of the curve
	 * returns The X coordinate of the curve turning point
	 */
	public double getTPointX()
	{
		if (center.isEmpty() && (dragS || dragT))
		{
			return (source.getCenterX() + target.getCenterX()) / 2;
		}
		else
		{
			return getPosition().getX();
		}

		//return tPoint.getX();
	}

	/** 
	 * Get the Y coordinate of the turning point of the curve
	 * returns The Y coordinate of the curve turning point
	 */
	public double getTPointY()
	{
		if (center.isEmpty() && (dragS || dragT))
		{
			return (source.getCenterY() + target.getCenterY()) / 2;
		}
		else
		{
			return getPosition().getY();
		}

		//return tPoint.getY();
	}

    public Point2D getPosition()
    {
		return subject.getGeometry().getPoints().get(0);
    }

	/** 
	 * A synonym for getTPointX()
	 * Returns the X coordinate of the curve turning point
	 */
	public int getX()
	{
		return (int) getTPointX();
	}

	/** 
	 * A synonym for getTPointY()
	 * Returns the Y coordinate of the curve turning point
	 */
	public int getY()
	{
		return (int) getTPointY();
	}

	/**
	 * Set the screen coordinates of the turning point of the edge.
	 */
	public void setPosition(double x, double y)
	{
		if (isSelfLoop())
		{
			straight = false; 
			
			subject.getGeometry().getPointsModifiable().set(0, new Point2D.Double(x, y));
		}
		else
		{
			if (onLine(x, y))
			{
				straight = true;
				
				subject.getGeometry().getPointsModifiable().set(0 , new Point2D.Double(
				(double) ((getStartPoint().getX() + endNode.getX()) / 2), (double) ((getStartPoint().getY() + endNode.getY()) / 2)));
			}
			else
			{
				straight = false;
				
				subject.getGeometry().getPointsModifiable().set(0, new Point2D.Double(x, y));
			}
		}

		center.setFrameFromCenter(getPosition().getX(), getPosition().getY(), getPosition().getX() + WIDTHD, getPosition().getY() + WIDTHD);
	}

	public int hashCode()
	{
		return subject.hashCode(); 
	}

	/**
	 * Set the edge control point (rather than the turning point)
	 */

	/** 
	 * Recaculate the position of the control point, based on the turning point
	 * @param ox The old X coordinate of the control point
	 * @param oy The old Y position of the control point
	 * @param startN True if the node being moved is the source node.
	 */
	public void updateControlPoint(double ox, double oy, boolean startN)
	{
		if (isSelfLoop())
		{
			subject.getGeometry().getPointsModifiable().set(0 ,
					new Point2D.Double(getTPointX() + (endNode.getX() - (int) ox),
					getTPointY() + (endNode.getY() - (int) oy)));
			
			return;
		}

		double Cx = getTPointX();
		double Cy = getTPointY();
		double Newx;
		double Newy;
		double Deltax;
		double Deltay;

		if (startN)
		{
			//  Cx = (.25 * (double)(ox + 2*getCPointX() + (double)endNode.getX())) - (double)endNode.getX();
			//  Cy = (.25 * (double)(oy + 2*getCPointY() + (double)endNode.getY())) - (double)endNode.getY();
			Cx -= endNode.getX();
			Cy -= endNode.getY();
			ox -= endNode.getX();
			oy -= endNode.getY();
			Newx = (double) (getStartPoint().getX() - endNode.getX());
			Newy = (double) (getStartPoint().getY() - endNode.getY());
		}
		else
		{
			//Cx = (.25 * (double)(ox + 2*getCPointX() + (double)start.getX())) - (double)start.getX();
			//Cy = (.25 * (double)(oy + 2*getCPointY() + (double)start.getY())) - (double)start.getY();
			Cx -= getStartPoint().getX();
			Cy -= getStartPoint().getY();
			ox -= getStartPoint().getX();
			oy -= getStartPoint().getY();
			Newx = endNode.getX() - getStartPoint().getX();
			Newy = endNode.getY() - getStartPoint().getY();
		}

		// If ox and oy are 0, this becomes 0...
		double divide = Math.pow(ox, 2) + Math.pow(oy, 2);
		if (Math.abs(divide) < Double.MIN_VALUE)
		{
			if (divide >= 0)
			{
				divide = 2*Double.MIN_VALUE;
			}
			else
			{
				divide = -2*Double.MIN_VALUE;
			}
		}

		// ... which is not good here!
		double a1 = (Newx * ox + oy * Newy) / divide;
		double a2 = ((oy * Newx) - (ox * Newy)) / divide;
		double a3 = -a2;
		double a4 = a1;

		Deltax = (a1 * Cx + a2 * Cy);
		Deltay = (a3 * Cx + a4 * Cy);
		Cx = Deltax;
		Cy = Deltay;

		if (startN)
		{
			Cx += endNode.getX();
			Cy += endNode.getY();
		}
		else
		{
			Cx += getStartPoint().getX();
			Cy += getStartPoint().getY();
		}

		// This is where it sometimes goes wrong... Cx and Cy sometimes becomes NaN...
		setPosition(Cx, Cy);
	}

	private boolean onLine(double x, double y)
	{
		// If we're "behind" a node, we're not on the line...
		if (((getStartPoint().getX() < x) && (endNode.getX() < x)) || 
			((getStartPoint().getY() < y) && (endNode.getY() < y)) || 
			((getStartPoint().getX() > x) && (endNode.getX() > x)) || 
			((getStartPoint().getY() > y) && (endNode.getY() > y)))
		{
			return false;
		}

		Rectangle2D.Double r = new Rectangle2D.Double((double) getStartPoint().getX(), (double) getStartPoint().getY(), (double) (endNode.getX() - getStartPoint().getX()), (double) (endNode.getY() - getStartPoint().getY()));

		if (r.contains(x, y))
		{
			Line2D.Double l = new Line2D.Double(getStartPoint().getX(), getStartPoint().getY(), endNode.getX(), endNode.getY());

			return (l.intersects(x - 2, y - 2, 4, 4));
		}
		else
		{
			double m;

			if (getStartPoint().getX() != endNode.getX())
			{
				m = (double) (getStartPoint().getY() - endNode.getY()) / (double) (getStartPoint().getX() - endNode.getX());

				double t = getStartPoint().getY() - (m * getStartPoint().getX());

				return (Math.abs(y - (m * x) - t) <= 2);
			}

			return (Math.abs((double) (x - getStartPoint().getX())) <= 2);
		}
	}
	
	/**
	 * Calculates the tear, i.e. the drop-shaped line used for selfloops.
	 */
	protected ArrayList createTear()
	{
		double dist = (double) Math.sqrt(Math.pow(getCPointX() - getStartPoint().getX(), 2) + 
										 Math.pow(getCPointY() - getStartPoint().getY(), 2)) * tearRatio;
		double r = dist / 2;
		double xP = (double) ((getCPointX() - getStartPoint().getX()) * (1 - (tearRatio / 2))) + getStartPoint().getX() - r;
		double yP = (double) ((getCPointY() - getStartPoint().getY()) * (1 - (tearRatio / 2))) + getStartPoint().getY() - r;
		double theta;

		if (getCPointY() == getStartPoint().getY())
		{
			theta = Math.PI / 2.0;

			if (getCPointX() > getStartPoint().getX())
			{
				theta += Math.PI;
			}
		}
		else
		{
			theta = Math.atan((double) (getCPointX() - getStartPoint().getX()) / (double) (getCPointY() - getStartPoint().getY()));
		}

		if (getCPointY() > getStartPoint().getY())
		{
			theta += Math.PI;
		}

		Point2D.Double p = new Point2D.Double(getStartPoint().getX(), getStartPoint().getY());
		Arc2D.Double arc = new Arc2D.Double(xP, yP, dist, dist, Math.toDegrees(theta) + 315, (double) 270, Arc2D.OPEN);
		Line2D.Double l1 = new Line2D.Double(arc.getStartPoint(), p);
		Line2D.Double l2 = new Line2D.Double(arc.getEndPoint(), p);
		Rectangle2D.Double r2 = new Rectangle2D.Double(p.getX(), p.getY(), 0, 0);

		r2.add(arc.getStartPoint());
		r2.add(arc.getEndPoint());

		ArrayList a = new ArrayList(4);

		a.add(arc);
		a.add(l1);
		a.add(l2);
		a.add(r2);
		findIntersection(source, new Point2D.Double(getStartPoint().getX(), getStartPoint().getY()), (Point2D.Double) arc.getStartPoint());
		findIntersection(target, new Point2D.Double(getStartPoint().getX(), getStartPoint().getY()), (Point2D.Double) arc.getEndPoint());

		return a;
	}

	private boolean onCircle(double x, double y)
	{
		ArrayList a = createTear();
		Arc2D.Double arc = (Arc2D.Double) a.get(0);
		Line2D.Double l1 = (Line2D.Double) a.get(1);
		Line2D.Double l2 = (Line2D.Double) a.get(2);
		Rectangle2D.Double r = new Rectangle2D.Double(x - 2, y - 2, 4, 4);
		Rectangle2D.Double r2 = (Rectangle2D.Double) a.get(3);

		return ((arc.intersects(r) &&!arc.contains(r) &&!r2.contains(x, y)) || (l1.intersects(r)) || (l2.intersects(r)));
	}

	/** 
	 * Used to determine whether the object in question would be selected by given mouse click
	 * @param Cxposition The click X position
	 * @param Cyposition The click Y position
	 */
	public boolean wasClicked(int Cxposition, int Cyposition)
	{
		Rectangle2D.Double r = new Rectangle2D.Double();

		r.setFrameFromCenter(getTPointX(), getTPointY(), getTPointX() + WIDTHS, getTPointY() + WIDTHS);

		dragC = r.contains(Cxposition, Cyposition);

		r.setFrameFromCenter(source.getCenterX(), source.getCenterY(), source.getCenterX() + WIDTHS, source.getCenterY() + WIDTHS);

		dragS = r.contains(Cxposition, Cyposition);

		r.setFrameFromCenter(target.getCenterX(), target.getCenterY(), target.getCenterX() + WIDTHS, target.getCenterY() + WIDTHS);

		dragT = r.contains(Cxposition, Cyposition);

		if (dragC || dragS || dragT)
		{
			return true;
		}

		if (isSelfLoop())
		{
			return (onCircle(Cxposition, Cyposition));
		}

		//return (Math.abs(Cxposition-controlPoint.getX()) < 5 && Math.abs(Cyposition-controlPoint.getY()) < 5);

		int n = 0;

		// The straight line between start and end
		Line2D.Double l = new Line2D.Double(getStartPoint().getX(), getStartPoint().getY(), endNode.getX(), endNode.getY());

  		// The curve that is this edge
		QuadCurve2D.Double q = getCurve();

		if (straight)
		{
			return (l.intersects(Cxposition - 2, Cyposition - 2, 4, 4));
		}
		else if (l.intersects(Cxposition - 2, Cyposition - 2, 4, 4))
		{
			return false;
		}
		else
		{
			return ((q.intersects(Cxposition - 2, Cyposition - 2, 4, 4)) && 
					(!q.contains(Cxposition - 2, Cyposition - 2, 4, 4)));
		}
	}

	public boolean isSelfLoop()
	{
		return startNode == endNode;
	}

	/**
	 * Draws an arrow at the point (posX, posY), which is presumed to be the middle of the edge from 
	 * (x1, y1) to (x2, y2). If the edge is a self-loop the argument loop should be true.
	 *
	 * This is the "original" drawArrow method, the methods below this one are cleaned up
	 * versions. This one, however is still used for drawing arrows "in the middle" of edges.
	 *
	 * @param x1 x-coordinate of starting point of edge
	 * @param y1 y-coordinate of starting point of edge
	 * @param x2 x-coordinate of ending point of edge
	 * @param y2 y-coordinate of ending point of edge
	 * @param posX x-coordinate of arrow point
	 * @param posY y-coordinate of arrow point
	 * @param loop true if edge is a loop
	 * @param g2d the graphical surface where the arrow should be drawn
	 */ 
	private static void drawArrow(double x1, double y1, 
						   double x2, double y2, 
						   int posX, int posY, 
						   boolean loop, Graphics2D g2d)
	{
		double theta;
		double phi = Math.PI / 6.0;
		int length = 9;
		int direction;

		if (y1 >= y2)
		{
			direction = 1;
		}
		else
		{
			direction = -1;
		}

		if (y1 == y2)
		{
			theta = Math.PI / 2.0;

			if (x1 < x2)
			{
				theta *= -1;
			}
		}
		else
		{
			theta = Math.atan((double) (x1 - x2) / (double) (y1 - y2));
		}

		if (loop)
		{
			theta -= Math.PI / 2.0;
		}
		
		int[] xcoords = new int[3];
		int[] ycoords = new int[3];

		// The first pair of coordinates is the point of the arrow, it is adjusted somewhat
		// from (posX, posY)
		//xcoords[0] = posX - direction * (int) ((Math.sqrt(Math.pow(length, 2) + Math.pow(length, 2)) / 2) * Math.sin(theta));
		//ycoords[0] = posY - direction * (int) ((Math.sqrt(Math.pow(length, 2) + Math.pow(length, 2)) / 2) * Math.cos(theta));
		xcoords[0] = posX - direction * (int) (length / Math.sqrt(2) * Math.sin(theta)); // The same as above?
		ycoords[0] = posY - direction * (int) (length / Math.sqrt(2) * Math.cos(theta)); // The same as above?
		xcoords[1] = xcoords[0] + (int) (length * Math.sin(theta - phi)) * direction;
		ycoords[1] = ycoords[0] + (int) (length * Math.cos(theta - phi)) * direction;
		xcoords[2] = xcoords[0] + (int) (length * Math.cos(Math.PI / 2.0 - (theta + phi))) * direction;
		ycoords[2] = ycoords[0] + (int) (length * Math.sin(Math.PI / 2.0 - (theta + phi))) * direction;

		g2d.fillPolygon(xcoords, ycoords, 3);

		//drawArrow(posX, posY, theta, g2d);
	}

	/**
	 * Draws an arrow with its point EditorNode.RADIUS away from (x2, y2) pointing in the direction 
	 * indicated by the two points (x1, y1) and (x2, y2).
	 */
	private static void drawArrow(double x1, double y1, double x2, double y2, Graphics2D g2d)
	{
		drawArrow(x1, y1, x2, y2, EditorNode.RADIUS, g2d);
	}

	/**
	 * Draws an arrow with its point {@code distance} away from (x2, y2) pointing in the direction indicated 
	 * by the two points (x1, y1) and (x2, y2).
	 */
	private static void drawArrow(double x1, double y1, double x2, double y2, int distance, Graphics2D g2d)
	{
		//Find angle!
		double theta;
		if (y1 == y2)
		{
			theta = Math.PI / 2.0;
			
			if (x1 < x2)
			{
				theta *= -1;
			}
		}
		else
		{
			theta = Math.atan((double) (x1 - x2) / (double) (y1 - y2));
		}
		// Did arctan give the correct angle or should we add 180 degrees?
		if (y1 < y2)
		{
			theta += Math.PI;
		}
		
		// Find position!
		int posX = (int) Math.round(x2 + Math.sin(theta)*distance);
		int posY = (int) Math.round(y2 + Math.cos(theta)*distance);
		
		// Draw arrow!
		drawArrow(posX, posY, theta, g2d);
	}
	
	/**
	 * Draws on g2d an arrow with its point in (x, y), pointing in the angle theta.
	 */
	public static void drawArrow(int x, int y, double theta, Graphics2D g2d)
	{
		// The length of the side of the arrow
		int length = 9;
		// The angular width of the arrow (half of it actually)
		double phi = Math.PI / 9.0;
		
		// Arrays of coordinates for the corners
		int[] xcoords = new int[3];
		int[] ycoords = new int[3];		
		
		// Draw arrow, the first pair of coordinates is the point
		xcoords[0] = x;
		ycoords[0] = y;
		xcoords[1] = xcoords[0] + (int) Math.round(length * Math.sin(theta - phi));
		ycoords[1] = ycoords[0] + (int) Math.round(length * Math.cos(theta - phi));
		xcoords[2] = xcoords[0] + (int) Math.round(length * Math.cos(Math.PI / 2.0 - (theta + phi)));
		ycoords[2] = ycoords[0] + (int) Math.round(length * Math.sin(Math.PI / 2.0 - (theta + phi)));
		
		// Do the drawing!
		g2d.fillPolygon(xcoords, ycoords, 3); 
	}

	// What does this method do? /hguo
	private void findIntersection(Rectangle2D.Double i, Point2D.Double s, Point2D.Double e)
	{
		e.setLocation(e.getX() - s.getX(), e.getY() - s.getY());

		if (0 == e.getX())
		{
			/*      if (l.intersects(s.getX()-1, s.getY() + endNode.getWidth()/2-1, 2, 2)){
				i.setFrameFromCenter(s.getX(), s.getY() + endNode.getWidth()/2+WIDTH, s.getX() + WIDTH,
				s.getY() + endNode.getWidth()/2 + 2* WIDTH);
			}
			else{
				i.setFrameFromCenter(s.getX(), s.getY() - endNode.getWidth()/2-WIDTH, s.getX() + WIDTH, s.getY() - endNode.getWidth()/r);
				}*/
			e.setLocation(e.getX() + .000000001, e.getY());
		}

		//      else{
		double m = e.getY() / e.getX();
		double x = Math.sqrt(Math.pow(endNode.getWidth() / 2 + WIDTHD, 2) / (Math.pow(m, 2) + 1));

		if (e.getX() < 0)
		{
			x = -x;
		}

		double y = m * x;
		
		x += s.getX();
		y += s.getY();

		i.setFrameFromCenter(x, y, x + WIDTHD, y + WIDTHD);

		//  }
	}

	public EdgeSubject getSubject()
	{
		return subject;
	}
	
	public QuadCurve2D.Double getCurve()
	{
		return new QuadCurve2D.Double(getStartPoint().getX(), getStartPoint().getY(), 
 									  getCPointX(), getCPointY(), 
									  endNode.getX(), endNode.getY());
	}
	
	public void drawObject(Graphics g, boolean selected)
	{
		Graphics2D g2d = (Graphics2D) g;
		g2d.setStroke(BASICSTROKE);
		
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setColor(getColor(selected));
		
		if (source.isEmpty() && (dragT || dragS))
		{
			int x1 = (int) source.getCenterX();
			int y1 = (int) source.getCenterY();
			int x2 = (int) target.getCenterX();
			int y2 = (int) target.getCenterY(); 
			
			// Draw shadow
			if (shadow && isHighlighted())
			{
				g2d.setStroke(SHADOWSTROKE);
				g2d.setColor(getShadowColor(selected));
				g2d.drawLine(x1, y1, x2, y2);
				g2d.setColor(getColor(selected));
				g2d.setStroke(BASICSTROKE);
			}

			g2d.drawLine(x1, y1, x2, y2);

			if (arrowAtEnd)
			{
				drawArrow(x1, y1, x2, y2, 0, g2d);
			}
			else
			{
				drawArrow(x1, y1, x2, y2, (x1 + x2) / 2, (y1 + y2) / 2, false, g2d);
			}
		}
		else if (isSelfLoop())
		{
			ArrayList a = createTear();

			// Initialize the edge (somehow this strange call does exactly what we want)
			setPosition(getTPointX(), getTPointY());
			
			// Draw shadow
			if (shadow && isHighlighted())
			{
				g2d.setStroke(SHADOWSTROKE);
				g2d.setColor(getShadowColor(selected));
				g2d.draw((Arc2D.Double) a.get(0));
				g2d.draw((Line2D.Double) a.get(1));
				g2d.draw((Line2D.Double) a.get(2));
				g2d.setColor(getColor(selected));
				g2d.setStroke(BASICSTROKE);
			}

			// Draw curve
			g2d.draw((Arc2D.Double) a.get(0));
			g2d.draw((Line2D.Double) a.get(1));
			g2d.draw((Line2D.Double) a.get(2));
			
			if (arrowAtEnd)
			{
				Line2D.Double endline = (Line2D.Double) a.get(2);
				drawArrow((int) endline.getX1(), (int) endline.getY1(), 
						  (int) endline.getX2(), (int) endline.getY2(), 
						  g2d);
			}
			else
			{
				drawArrow(getCPointX(), getCPointY(), 
						  getStartPoint().getX(), getStartPoint().getY(), 
						  (int) getTPointX(), (int) getTPointY(), 
						  true, g2d);
			}
		}
		else
		{
			// Draw shadow
			if (shadow && isHighlighted())
			{
				g2d.setStroke(SHADOWSTROKE);
				g2d.setColor(getShadowColor(selected));	
				g2d.draw(getCurve()); 
				g2d.setColor(getColor(selected));
				g2d.setStroke(BASICSTROKE);
			}

			// Draw curve
			g2d.draw(getCurve());

			if (startNode.getType() == NODE)
			{
				findIntersection(source, new Point2D.Double(getStartPoint().getX(), getStartPoint().getY()), 
								 new Point2D.Double((double) getCPointX(), (double) getCPointY()));
			}
			else
			{
				source.setFrameFromCenter(getStartPoint().getX(), getStartPoint().getY(), 
										  getStartPoint().getX() + WIDTHD, getStartPoint().getY() + WIDTHD);
			}
			
			// Draw arrow
			findIntersection(target, new Point2D.Double(endNode.getX(), endNode.getY()), 
							 new Point2D.Double((double) getCPointX(), (double) getCPointY()));
			if (arrowAtEnd)
			{
				// Find the (second) last approximation point and use it and the endNode to draw the arrow!
				double x1 = getStartPoint().getX();
				double y1 = getStartPoint().getY();
				double x2 = endNode.getX();
				double y2 = endNode.getY();
				QuadCurve2D.Double curve = getCurve();
				FlatteningPathIterator it = 
					new FlatteningPathIterator(curve.getPathIterator(new AffineTransform()), 0.5, 25);
				while (!it.isDone())
				{
					double[] segment = new double[6];
					int type = it.currentSegment(segment);
					
					it.next();
					
					// If there is another one (the last one?) take the current one!
					if (!it.isDone())
					{
						x1 = segment[0];				
						y1 = segment[1];
					}
				}
				
				drawArrow(x1, y1, x2, y2, g2d);
			}					
			else	 
			{
				drawArrow(getStartPoint().getX(), getStartPoint().getY(), 
						  endNode.getX(), endNode.getY(), 
						  (int) getTPointX(), (int) getTPointY(), 
						  false, g2d);
			}
			
			/*
			// Draw approximated curve
			QuadCurve2D.Double curve = getCurve();
			FlatteningPathIterator it = 
				new FlatteningPathIterator(curve.getPathIterator(new AffineTransform()), 5.0, 5);
			while (!it.isDone())
			{
				double[] segment = new double[6];
				int type = it.currentSegment(segment);
				
				// Only care about the first point, it's not such a big deal, anyway?
				double x = segment[0];				
				double y = segment[1];

				g2d.setColor(Color.RED);
				g2d.draw(new Ellipse2D.Double(x,y,1,1));	

				it.next();
			}
			*/
		}
		
		if (selected)
		{
			//g2d.draw(source);
			g2d.fill(source);
			//g2d.draw(target);
			g2d.fill(target);
		}
	}

	public static boolean getArrowAtEnd()
	{
		return arrowAtEnd;
	}
	public static void setArrowAtEnd(boolean set)
	{
		arrowAtEnd = set;
	}

	public EditorGuardActionBlock getEditorGuardActionBlock() {
		return mEditorGuardActionBlock;
	}

	public void setEditorGuardActionBlock(
			EditorGuardActionBlock editorGuardActionBlock) {
		mEditorGuardActionBlock = editorGuardActionBlock;
	}

	public EditorLabelGroup getEditorLabelGroup() {
		return mEditorLabelGroup;
	}

	public void setEditorLabelGroup(EditorLabelGroup editorLabelGroup) {
		mEditorLabelGroup = editorLabelGroup;
	}


	//#######################################################################
	//# Interface net.sourceforge.waters.subject.base.ModelObserver
	public void modelChanged(final ModelChangeEvent event)
	{
	}

	public void resizePanels() {
		int panelWidth = 0;
		int newPanelWidth = 0;
		if(mEditorGuardActionBlock != null) {
			newPanelWidth = mEditorGuardActionBlock.resizePanel(panelWidth);
			panelWidth = newPanelWidth;
		}
		if(mEditorLabelGroup != null) {
			newPanelWidth = mEditorLabelGroup.resizePanel(panelWidth);
		}
		if(newPanelWidth > panelWidth && mEditorGuardActionBlock != null) {
			newPanelWidth = mEditorGuardActionBlock.resizePanel(newPanelWidth);
		}
	}

	public EditorSurface getParent() {
		return mParent;
	}

	public void setParent(EditorSurface parent) {
		mParent = parent;
	}

}
