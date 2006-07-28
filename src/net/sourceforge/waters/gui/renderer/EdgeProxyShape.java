package net.sourceforge.waters.gui.renderer;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;

import java.awt.geom.Arc2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.QuadCurve2D.Double;
import java.awt.geom.Rectangle2D;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.gui.ModuleWindow;
import java.util.List;
import java.util.ArrayList;
import net.sourceforge.waters.gui.renderer.Handle.HandleType;
import net.sourceforge.waters.model.module.SimpleNodeProxy;

public abstract class EdgeProxyShape
	extends AbstractProxyShape
{
	protected EdgeProxyShape(EdgeProxy edge)
	{
		super(edge);
		if (edge.getGeometry() != null)
		{
			mTurn = edge.getGeometry().getPoints().get(0);
		}
		else
		{
			mTurn = GeometryTools.getMidPoint(
											  GeometryTools.getPosition(edge.getSource()), 
											  GeometryTools.getPosition(edge.getTarget()));
		}
		if (edge.getStartPoint() != null)
		{
			mStart = edge.getStartPoint().getPoint();
		}
		else
		{
			mStart = GeometryTools.defaultPosition(edge.getSource(), mTurn);
		}
		if (edge.getEndPoint() != null)
		{
			mEnd = edge.getEndPoint().getPoint();
		}
		else
		{
			mEnd = GeometryTools.defaultPosition(edge.getTarget(), mTurn);
		}
		mHandles = new ArrayList<Handle>(2);
	}
	
	public List<Handle> getHandles()
	{
		return mHandles;
	}
  
	public Point2D getTurningPoint()
	{
		return mTurn;
	}
	
	public Point2D getStartPoint()
	{
		return mStart;
	}
	
	public Point2D getEndPoint()
	{
		return mEnd;
	}
	
	public EdgeProxy getProxy()
	{
		return (EdgeProxy)super.getProxy();
	}
	
	public void draw(Graphics2D g, RenderingInformation status)
	{
		super.draw(g, status);
		g.setStroke(BASICSTROKE);
		g.setColor(status.getColor());
		if (ARROWATEND)
		{
			// The direction of the arrow calculated from two
			// coordinate pairs. Per default startpoint to endpoint,
			// but the start, (x1, y1), needs to be changed if the
			// line is curved...
			double x1 = mStart.getX(); // Default
			double y1 = mStart.getY(); // Default
			double x2 = mEnd.getX();
			double y2 = mEnd.getY();

			// Need to find the real direction of the line when it
			// reaches the endpoint...

			// Get the shape of the line
			Shape curve = getShape();
			
			// Find the coordinate of the second-to-last segment of the curve
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

			// Draw arrow, pointing in the direction given by (x1,y1),
			// (x2,y2), at a distance SimpleNodeProxyShape.RADIUS+1 from
			// the end point!
			drawArrow(x1, y1, x2, y2, SimpleNodeProxyShape.RADIUS+1, g);
		}
		else
		{
			double dx = Math.sin(arrowAngle()) * 4.5;
			double dy = Math.cos(arrowAngle()) * 4.5;
			drawArrow((int)(mTurn.getX() - dx), (int)(mTurn.getY() - dy), arrowAngle(), g);
		}
	}
	
	protected abstract double arrowAngle();

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
		// Arrays of coordinates for the corners
		int[] xcoords = new int[3];
		int[] ycoords = new int[3];		
		
		// Draw arrow, the first pair of coordinates is the point
		xcoords[0] = x;
		ycoords[0] = y;
		xcoords[1] = xcoords[0] + (int) Math.round(ARROWSIDE * Math.sin(theta - ARROWANGLEWIDTH/2.0));
		ycoords[1] = ycoords[0] + (int) Math.round(ARROWSIDE * Math.cos(theta - ARROWANGLEWIDTH/2.0));
		xcoords[2] = xcoords[0] + (int) Math.round(ARROWSIDE * Math.cos(Math.PI / 2.0 - (theta + ARROWANGLEWIDTH/2.0)));
		ycoords[2] = ycoords[0] + (int) Math.round(ARROWSIDE * Math.sin(Math.PI / 2.0 - (theta + ARROWANGLEWIDTH/2.0)));
		
		// Do the drawing!
		g2d.drawPolygon(xcoords, ycoords, 3);
		g2d.fillPolygon(xcoords, ycoords, 3); 
	}
	
	public static class QuadCurve
		extends EdgeProxyShape
	{
		public QuadCurve(EdgeProxy e)
		{
			super(e);
			Point2D p1 = getStartPoint();
			Point2D p2 = getEndPoint();
			Point2D c = getTurningPoint();
			double r = SimpleNodeProxyShape.RADIUS;
			QuadCurve2D curve = new QuadCurve2D.Double(p1.getX(), p1.getY(), c.getX(),
													   c.getY(), p2.getX(), p2.getY());
			// set quadcurve to be on edge of node
			if (e.getSource() instanceof SimpleNodeProxy) {
				if (p1.equals(((SimpleNodeProxy)e.getSource())
							  .getPointGeometry().getPoint())) {
					p1 = GeometryTools.getRadialPoint(GeometryTools.getControlPoint(curve)
													  , p1, r);
				}
			}
			if (e.getTarget() instanceof SimpleNodeProxy) {
				if (p2.equals(((SimpleNodeProxy)e.getTarget())
							  .getPointGeometry().getPoint())) {
					p2 = GeometryTools.getRadialPoint(GeometryTools.getControlPoint(curve)
													  , p2, r);
				}
			}
			mCurve = new QuadCurve2D.Double(p1.getX(), p1.getY(), c.getX(),
											c.getY(), p2.getX(), p2.getY());
			mCurve.setCurve(p1, convertToControl(c, mCurve), p2);
			// change to control point
			mHandles.add(new DefaultHandle(p1, Handle.HandleType.SOURCE));
			mHandles.add(new DefaultHandle(p2, Handle.HandleType.TARGET));
		}
		
		public QuadCurve2D getShape()
		{
			return mCurve;
		}
		
		public boolean isClicked(int ex, int ey)
		{
			for (Handle h : getHandles()) {
				if (h.isClicked(ex, ey)) {
					return true;
				}
			}
			Rectangle2D rect = new Rectangle2D.Double(ex - 2, ey - 2, 4, 4);
			QuadCurve2D q1 = new QuadCurve2D.Double();
			QuadCurve2D q2 = new QuadCurve2D.Double();
			QuadCurve2D.subdivide(getShape(), q1, q2);
			return ((q1.intersects(rect) || q2.intersects(rect)) &&
					getShape().intersects(rect) && !getShape().contains(rect));
		}
		
		public static Point2D convertToControl(Point2D p, QuadCurve2D q)
		{
			return new Point2D.Double(convertToControl(q.getX1(), q.getX2(), p.getX()),
									  convertToControl(q.getY1(), q.getY2(), p.getY()));
		}
		
		public static Point2D convertToTurn(Point2D p, QuadCurve2D q)
		{
			return new Point2D.Double(convertToTurn(q.getX1(), q.getX2(), p.getX()),
									  convertToTurn(q.getY1(), q.getY2(), p.getY()));
		}
		
		private static double convertToControl(double start, double end, double center)
		{
			return (2 * center - (start + end) / 2);
		}
		
		private static double convertToTurn(double start, double end, double center)
		{
			return .25 * (start + 2 * center + end);
		}
		
		protected double arrowAngle()
		{
			Point2D p1 = mCurve.getP1();
			Point2D p2 = mCurve.getP2();
			return -(Math.atan2(p2.getY() - p1.getY(), p2.getX() - p1.getX())
				   	 + Math.PI / 2);
		}
		
		private final QuadCurve2D mCurve;
	}
	
	public static class Tear
		extends EdgeProxyShape
	{
		public Tear(EdgeProxy e)
		{
			super(e);
			Point2D p = getStartPoint();
			Point2D c = getTurningPoint();
			double dist = (double) Math.sqrt(Math.pow(c.getX() - p.getX(), 2) + 
											 Math.pow(c.getY() - p.getY(), 2));
			double r = (dist * (1 -TEARRATIO)) / 2;
			double theta = Math.atan2(c.getY() - p.getY(), c.getX() - p.getX());
			Rectangle2D rect = new Rectangle2D.Double(Math.cos(theta) * (dist - r) + p.getX() - r,
													  Math.sin(theta) * (dist - r) + p.getY() - r,
													  r * 2, r * 2);
			Arc2D arc = new Arc2D.Double(rect, -(Math.toDegrees(theta) + ARCEXTENT/2),
										 ARCEXTENT, Arc2D.OPEN);
			// different r for setting up where the handle is positioned
			r = SimpleNodeProxyShape.RADIUS;
			Point2D p1;
			Point2D p2;
			if (e.getSource() instanceof  SimpleNodeProxy) {
				p1 = GeometryTools.getRadialPoint(arc.getStartPoint(), p, r);
			} else {
				p1 = p;
			}
			if (e.getTarget() instanceof  SimpleNodeProxy) {
				p2 = GeometryTools.getRadialPoint(arc.getEndPoint(), p, r);
			} else {
				p2 = p;
			}
			Line2D line1 = new Line2D.Double(p1, arc.getStartPoint());
			Line2D line2 = new Line2D.Double(arc.getEndPoint(), p2);
			mCurve = new GeneralPath(GeneralPath.WIND_NON_ZERO, 3);
			mCurve.append(line1, false);
			mCurve.append(arc , true);
			mCurve.append(line2, true);
			mCurve.closePath();
			mHandles.add(new DefaultHandle(p1, Handle.HandleType.SOURCE));
			mHandles.add(new DefaultHandle(p2, Handle.HandleType.TARGET));
		}
		
		public GeneralPath getShape()
		{
			return mCurve;
		}
		
		public boolean isClicked(int ex, int ey)
		{
			for (Handle h : getHandles()) {
				if (h.isClicked(ex, ey)) {
					return true;
				}
			}
			Rectangle2D rect = new Rectangle2D.Double(ex - 2, ey - 2, 4, 4);
			return (getShape().intersects(rect) && !getShape().contains(rect));
		}
		
		protected double arrowAngle()
		{
			Point2D p1 = getTurningPoint();
			Point2D p2 = getEndPoint();
			return -Math.atan2(p2.getY() - p1.getY(), p2.getX() - p1.getX());
		}
		
		public static double TEARRATIO = 0.25;
		public static double ARCEXTENT = 225;
		
		private final GeneralPath mCurve;
	}
  
	public static boolean getArrowAtEnd()
	{
		return ARROWATEND;
	}
	public static void setArrowAtEnd(boolean set)
	{
		ARROWATEND = set;
	}
	
	/** Arrows at end of edge or in the middle? */
	private static boolean ARROWATEND = ModuleWindow.DES_COURSE_VERSION;
	
	protected final List<Handle> mHandles;
	private final Point2D mTurn;
	private final Point2D mStart;
	private final Point2D mEnd;

	/** The length of the side of the arrow. */
	public static final int ARROWSIDE = 8;
	/** The width of the point of the arrow. */
	public static final double ARROWANGLEWIDTH = Math.PI / 3.5;
	//public static final double ARROWANGLEWIDTH = Math.PI / 2;
}
