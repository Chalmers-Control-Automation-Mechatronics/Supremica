package net.sourceforge.waters.gui.renderer;

import java.awt.geom.Point2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Line2D.Double;
import java.awt.Graphics2D;

public class InitialStateHandle
  extends AbstractRendererShape
  implements Handle
{
  private final GeneralPath mShape;
  
	public InitialStateHandle(double arrowAngle, Point2D point, int radius)
	{
		mShape = new GeneralPath(GeneralPath.WIND_NON_ZERO, 2);
		double borderX = point.getX() + ((radius+4) * Math.cos(arrowAngle));
		double borderY = point.getY() + ((radius+4) * Math.sin(arrowAngle));
		double outerX = borderX + (INITARROWLENGTH * Math.cos(arrowAngle));
		double outerY = borderY + (INITARROWLENGTH * Math.sin(arrowAngle));
		mShape.append(new Line2D.Double(outerX, outerY, borderX, borderY), false);
		double x = point.getX() + Math.cos(arrowAngle)*radius;
		double y = point.getY() + Math.sin(arrowAngle)*radius;		
		mShape.append(createArrow(x, y, arrowAngle), false);
	}
  
	/**
	 * These arrows become a little bit bigger than the edge
	 * arrows. This since they are rendered using different
	 * fill-methods? 
	 */
	private static GeneralPath createArrow(double x, double y, double arrowAngle)
	{
		//int length = 9;
		//double phi = Math.PI / 9.0;
		int length = EdgeProxyShape.ARROWSIDE;       // Same as edge arrows
		double phi = EdgeProxyShape.ARROWANGLEWIDTH; // Same as edge arrows
		double[] xcoords = new double[3];
		double[] ycoords = new double[3];
		// Draw arrow, the first pair of coordinates is the point
		xcoords[0] = x;
		ycoords[0] = y;
		xcoords[1] = xcoords[0] + length * Math.cos(arrowAngle - phi);
		ycoords[1] = ycoords[0] + length * Math.sin(arrowAngle - phi);
		xcoords[2] = xcoords[0] + length * Math.cos(arrowAngle + phi);
		ycoords[2] = ycoords[0] + length * Math.sin(arrowAngle + phi);
		GeneralPath arrow = new GeneralPath(GeneralPath.WIND_NON_ZERO, 3);
		arrow.append(new Line2D.Double(xcoords[0], ycoords[0],
									   xcoords[1], ycoords[1]), false);
		arrow.append(new Line2D.Double(xcoords[1], ycoords[1],
									   xcoords[2], ycoords[2]), true);
		arrow.append(new Line2D.Double(xcoords[2], ycoords[2],
									   xcoords[0], ycoords[0]), true);
		return arrow;
	}
	
	public GeneralPath getShape()
	{
		return mShape;
	}
	
	public HandleType getType()
	{
		return HandleType.INITIAL;
	}
	
	public void draw(Graphics2D g, RenderingInformation status)
	{
		super.draw(g, status);
		g.fill(getShape());
	}
  
	public boolean isClicked(int x, int y)
	{
		//System.err.println("Point: " + x + ", " + y);
		//System.err.println("Shape: " + mShape);
		return mShape.intersects(x - 1, y - 1, 2, 2);
	}
  
	private static final double INITARROWLENGTH = 15;
}
