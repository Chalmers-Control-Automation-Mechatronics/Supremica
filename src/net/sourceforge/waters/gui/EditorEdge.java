
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.gui
//# CLASS:   EditorEdge
//###########################################################################
//# $Id: EditorEdge.java,v 1.14 2005-03-04 11:52:45 flordal Exp $
//###########################################################################
package net.sourceforge.waters.gui;

import java.util.*;
import java.awt.*;
import java.awt.geom.*;
import net.sourceforge.waters.model.module.*;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.base.ElementProxy;
import net.sourceforge.waters.model.module.NodeProxy;

/** <p>The editor's internal representation of edges.</p>
 *
 * <p>This represents edges within the graph of the component.  It currently supports
 *  Bezier-curve geometry, with a single control point only.</p>
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
{
	private int angle;
	/** The start can be either a node or a nodegroup. */
	private EditorObject startNode; 
	/** The end is a node. */
	private EditorNode endNode;

	private Point2D.Double tPoint;

	/** Boolean keeping track of whether the edge is a straight line or not. */
	private boolean straight;

	private Rectangle2D.Double source = new Rectangle2D.Double();
	private boolean dragS = false;
	private Rectangle2D.Double target = new Rectangle2D.Double();	
	private boolean dragT = false;	
	private Rectangle2D.Double center = new Rectangle2D.Double();
	private boolean dragC = false;

	private EdgeProxy proxy;
	private Point2D.Double start;
	private static double tearRatio = .8;
	private static int WIDTHD = 2;
	private static int WIDTHS = 5;

	public EditorEdge(EditorObject iStartNode, EditorNode iEndNode, int x, int y, EdgeProxy e)
	{
		startNode = iStartNode;
		endNode = iEndNode;
		proxy = e;
		
		if (startNode.getType() == NODE)
		{
			EditorNode s = (EditorNode) startNode;
			
			proxy.setSource((NodeProxy) s.getProxy());
			
			start = s.getPosition();
		}
		else
		{
			EditorNodeGroup s = (EditorNodeGroup) startNode;
			
			proxy.setSource((NodeProxy) s.getProxy());
			
			start = s.getPosition(x, y, this);
		}
		
		proxy.setTarget((NodeProxy) iEndNode.getProxy());
		proxy.setStartPoint(new PointGeometryProxy(start));
		proxy.setEndPoint(new PointGeometryProxy(endNode.getPosition()));
		proxy.getStartPoint().setPoint(start);
		proxy.getEndPoint().setPoint(endNode.getPosition());

		// Create new geometry if there is none
		if (proxy.getGeometry() == null)
		{
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
			proxy.setGeometry(new SplineGeometryProxy(l));
		}
		else
		{
			tPoint = new Point2D.Double(((Point2D) proxy.getGeometry().getPoints().get(0)).getX(), ((Point2D) proxy.getGeometry().getPoints().get(0)).getY());

			proxy.getGeometry().getPoints().remove(0);
			proxy.getGeometry().getPoints().add(tPoint);

			if (onLine(tPoint.getX(), tPoint.getY()))
			{
				straight = true;
			}
		}

		// Initialize the edge (somehow this strange call does exactly what we want)
		setTPoint(getTPointX(), getTPointY());

		type = EDGE;
	}

	public double getCPointX()
	{
		double result;
		if (proxy.getSource() == proxy.getTarget())
		{
			return tPoint.getX();
		}

		return (2 * tPoint.getX() - (start.getX() + endNode.getX()) / 2);
	}

	public double getCPointY()
	{
		if (proxy.getSource() == proxy.getTarget())
		{
			return tPoint.getY();
		}

		return (2 * tPoint.getY() - (start.getY() + endNode.getY()) / 2);
	}

	public Point2D.Double getCPoint()
	{
		return (new Point2D.Double(getCPointX(), getCPointY()));
	}

	public void setCPointX(double x)
	{
		setCPoint(x, tPoint.getY());
	}

	public void setCPointY(double y)
	{
		setCPoint(tPoint.getX(), y);
	}

	public void setCPoint(Point2D.Double p)
	{
		setCPoint(p.getX(), p.getY());
	}

	public void setCPoint(double x, double y)
	{
		if (proxy.getSource() == proxy.getTarget())
		{
			setTPoint(x, y);
		}
		
		setTPoint(.25 * (start.getX() + 2 * x + endNode.getX()), .25 * (start.getY() + 2 * y + endNode.getY()));
	}

	public Point2D.Double getStartPoint()
	{
		return start;
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
		source.setRect(start.getX(), start.getY(), 0, 0);
		center.setRect(0, 0, 0, 0);
		target.setRect(x, y, 0, 0);
	}

	public void setStartNode(EditorObject newstartNode, int x, int y)
	{
		double ox = -1;
		double oy = -1;

		if (startNode.getType() == NODEGROUP)
		{
			if (newstartNode == startNode)
			{
				ox = start.getX();
				oy = start.getY();
			}

			EditorNodeGroup s = (EditorNodeGroup) startNode;

			s.removePosition(start);
		}

		startNode = newstartNode;

		if (startNode.getType() == NODE)
		{
			EditorNode s = (EditorNode) startNode;

			start = s.getPosition();

			proxy.setSource((NodeProxy) s.getProxy());
		}
		else
		{
			EditorNodeGroup s = (EditorNodeGroup) startNode;

			start = s.getPosition(x, y, this);

			proxy.setSource((NodeProxy) s.getProxy());
		}

		if (ox != -1)
		{
			double nx = start.getX() - endNode.getX();
			double ny = start.getY() - endNode.getY();
			double tx = tPoint.getX() - endNode.getX();
			double ty = tPoint.getY() - endNode.getY();

			ox -= endNode.getX();
			oy -= endNode.getY();

			if (ox * nx < 0)
			{
				ox = ox * -1;
				tx = tx * -1;
			}

			if (oy * ny < 0)
			{
				oy = oy * -1;
				ty = ty * -1;
			}

			if (ox == 0)
			{
				ox = 1;
			}

			if (oy == 0)
			{
				oy = 1;
			}

			double a = (nx - ox) / oy;
			double b = (ny - oy) / ox;
			double dx = tx + a * ty;
			double dy = b * tx + ty;

			setTPoint(dx + endNode.getX(), dy + endNode.getY());
		}
		else if (isSelfLoop())
		{
			setTPoint(start.getX() + 30, start.getY() + 30);
		}
		else
		{
			setTPoint((start.getX() + endNode.getX()) / 2, (start.getY() + endNode.getY()) / 2);
		}

		dragS = false;

		proxy.getStartPoint().setPoint(start);
	}

	public void setEndNode(EditorNode newendNode)
	{
		endNode = newendNode;

		if (isSelfLoop())
		{
			setTPoint(start.getX() + 30, start.getY() + 30);
		}
		else
		{
			setTPoint((start.getX() + endNode.getX()) / 2, (start.getY() + endNode.getY()) / 2);
		}

		dragT = false;

		proxy.setTarget((NodeProxy) endNode.getProxy());
		proxy.getEndPoint().setPoint(endNode.getPosition());
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
			return tPoint.getX();
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
			return tPoint.getY();
		}

		//return tPoint.getY();
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
	public void setTPoint(double x, double y)
	{
		if (isSelfLoop())
		{
			straight = false; 
			
			tPoint.setLocation(x, y);
		}
		else
		{
			if (onLine(x, y))
			{
				straight = true;
				
				tPoint.setLocation((double) ((start.getX() + endNode.getX()) / 2), (double) ((start.getY() + endNode.getY()) / 2));
			}
			else
			{
				straight = false;
				
				tPoint.setLocation(x, y);
			}
		}

		center.setFrameFromCenter(tPoint.getX(), tPoint.getY(), tPoint.getX() + WIDTHD, tPoint.getY() + WIDTHD);
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
			tPoint.setLocation(tPoint.getX() + (endNode.getX() - (int) ox) / 2, tPoint.getY() + (endNode.getY() - (int) oy) / 2);

			return;
		}

		double Cx = tPoint.getX();
		double Cy = tPoint.getY();
		double Newx;
		double Newy;
		double Deltax;
		double Deltay;

		if (startN)
		{
			//          Cx = (.25 * (double)(ox + 2*getCPointX() + (double)endNode.getX())) - (double)endNode.getX();
			//  Cy = (.25 * (double)(oy + 2*getCPointY() + (double)endNode.getY())) - (double)endNode.getY();
			Cx -= endNode.getX();
			Cy -= endNode.getY();
			ox -= endNode.getX();
			oy -= endNode.getY();
			Newx = (double) (start.getX() - endNode.getX());
			Newy = (double) (start.getY() - endNode.getY());
		}
		else
		{
			//Cx = (.25 * (double)(ox + 2*getCPointX() + (double)start.getX())) - (double)start.getX();
			//Cy = (.25 * (double)(oy + 2*getCPointY() + (double)start.getY())) - (double)start.getY();
			Cx -= start.getX();
			Cy -= start.getY();
			ox -= start.getX();
			oy -= start.getY();
			Newx = endNode.getX() - start.getX();
			Newy = endNode.getY() - start.getY();
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
			Cx += start.getX();
			Cy += start.getY();
		}

		// This is where it sometimes goes wrong... Cx and Cy sometimes becomes NaN...
		setTPoint(Cx, Cy);
	}

	private boolean onLine(double x, double y)
	{
		Rectangle2D.Double r = new Rectangle2D.Double((double) start.getX(), (double) start.getY(), (double) (endNode.getX() - start.getX()), (double) (endNode.getY() - start.getY()));

		if (r.contains(x, y))
		{
			Line2D.Double l = new Line2D.Double(start.getX(), start.getY(), endNode.getX(), endNode.getY());

			return (l.intersects(x - 2, y - 2, 4, 4));
		}
		else
		{
			double m;

			if (start.getX() != endNode.getX())
			{
				m = (double) (start.getY() - endNode.getY()) / (double) (start.getX() - endNode.getX());

				double t = start.getY() - (m * start.getX());

				return (Math.abs(y - (m * x) - t) <= 2);
			}

			return (Math.abs((double) (x - start.getX())) <= 2);
		}
	}

	protected ArrayList createTear()
	{
		double dist = (double) Math.sqrt((getCPointX() - start.getX()) * (getCPointX() - start.getX()) + (getCPointY() - start.getY()) * (getCPointY() - start.getY())) * tearRatio;
		double r = dist / 2;
		double xP = (double) ((getCPointX() - start.getX()) * (1 - (tearRatio / 2))) + start.getX() - r;
		double yP = (double) ((getCPointY() - start.getY()) * (1 - (tearRatio / 2))) + start.getY() - r;
		double theta;

		if (getCPointY() == start.getY())
		{
			theta = Math.PI / 2;

			if (getCPointX() > start.getX())
			{
				theta += Math.PI;
			}
		}
		else
		{
			theta = Math.atan((double) (getCPointX() - start.getX()) / (double) (getCPointY() - start.getY()));
		}

		if (getCPointY() > start.getY())
		{
			theta += Math.PI;
		}

		Point2D.Double p = new Point2D.Double(start.getX(), start.getY());
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
		findIntersection(source, new Point2D.Double(start.getX(), start.getY()), (Point2D.Double) arc.getStartPoint());
		findIntersection(target, new Point2D.Double(start.getX(), start.getY()), (Point2D.Double) arc.getEndPoint());

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

		r.setFrameFromCenter(tPoint.getX(), tPoint.getY(), tPoint.getX() + WIDTHS, tPoint.getY() + WIDTHS);

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
		Line2D.Double l = new Line2D.Double(start.getX(), start.getY(), endNode.getX(), endNode.getY());

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

	private void drawArrow(double x1, double y1, double x2, double y2, int posX, int posY, boolean loop, Graphics2D g2d)
	{
		double theta;
		int l = 8;
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
			theta = Math.PI / 2;

			if (x1 < x2)
			{
				theta *= -1;
			}
		}
		else
		{
			theta = Math.atan((double) (x1 - x2) / (double) (y1 - y2));
		}

		int[] xcoords = new int[3];
		int[] ycoords = new int[3];

		if (loop)
		{
			theta -= Math.PI / 2;
		}

		xcoords[0] = posX - direction * (int) ((Math.sqrt(Math.pow(l, 2) + Math.pow(l, 2)) / 2) * Math.sin(theta));
		ycoords[0] = posY - direction * (int) ((Math.sqrt(Math.pow(l, 2) + Math.pow(l, 2)) / 2) * Math.cos(theta));
		xcoords[1] = xcoords[0] + (int) (l * Math.sin(theta - Math.PI / 6)) * direction;
		ycoords[1] = ycoords[0] + (int) (l * Math.cos(theta - Math.PI / 6)) * direction;
		xcoords[2] = xcoords[0] + (int) (l * Math.cos(Math.PI / 2 - (theta + Math.PI / 6))) * direction;
		ycoords[2] = ycoords[0] + (int) (l * Math.sin(Math.PI / 2 - (theta + Math.PI / 6))) * direction;

		g2d.fillPolygon(xcoords, ycoords, 3);
	}

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

	public EdgeProxy getProxy()
	{
		return proxy;
	}
	
	public QuadCurve2D.Double getCurve()
	{
		return new QuadCurve2D.Double(start.getX(), start.getY(), 
 									  getCPointX(), getCPointY(), 
									  endNode.getX(), endNode.getY());
	}
	
	public void drawObject(Graphics g)
	{
		Graphics2D g2d = (Graphics2D) g;
		
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setColor(getColor());
		
		if (source.isEmpty() && (dragT || dragS))
		{
			int x1 = (int) source.getCenterX();
			int y1 = (int) source.getCenterY();
			int x2 = (int) target.getCenterX();
			int y2 = (int) target.getCenterY(); 
			
			g2d.drawLine(x1, y1, x2, y2);
			drawArrow(x1, y1, x2, y2, (x1 + x2) / 2, (y1 + y2) / 2, false, g2d);
		}
		else if (isSelfLoop())
		{
			ArrayList a = createTear();
			
			g2d.draw((Arc2D.Double) a.get(0));
			g2d.draw((Line2D.Double) a.get(1));
			g2d.draw((Line2D.Double) a.get(2));
			drawArrow(getCPointX(), getCPointY(), 
					  start.getX(), start.getY(), 
					  (int) getTPointX(), (int) getTPointY(), 
					  true, g2d);
		}
		else
		{
			/*
			// Draw shadow
			if (isHighlighted())
			{
				g2d.setStroke(SHADOWSTROKE);
				g2d.setColor(getShadowColor());				
				g2d.draw(getCurve()); 
				g2d.setColor(getColor());
				g2d.setStroke(BASICSTROKE);
			}
			*/

			// Draw curve
			g2d.draw(getCurve());

			if (startNode.getType() == NODE)
			{
				findIntersection(source, new Point2D.Double(start.getX(), start.getY()), 
								 new Point2D.Double((double) getCPointX(), (double) getCPointY()));
			}
			else
			{
				source.setFrameFromCenter(start.getX(), start.getY(), 
										  start.getX() + WIDTHD, start.getY() + WIDTHD);
			}
			
			findIntersection(target, new Point2D.Double(endNode.getX(), endNode.getY()), 
							 new Point2D.Double((double) getCPointX(), (double) getCPointY()));
			drawArrow(start.getX(), start.getY(), 
					  endNode.getX(), endNode.getY(), 
					  (int) getTPointX(), (int) getTPointY(), false, g2d);
		}
		
		if (isSelected())
		{
			g2d.draw(source);
			g2d.fill(source);
			g2d.draw(target);
			g2d.fill(target);
		}
	}
}
