package net.sourceforge.waters.gui;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import javax.xml.bind.JAXBException;
import net.sourceforge.waters.model.base.*;
import net.sourceforge.waters.model.module.*;
import net.sourceforge.waters.model.expr.*;
import net.sourceforge.waters.xsd.module.AnchorPosition;

/** 
x * <p>The internal editor representation of node objects.</p>
 *
 * <p>Nodes store geometry and {@link EditorShade} information.</p>
 *
 * @author Gian Perrone
 */
public class EditorNode
	extends EditorObject
{
	protected int hash = 0;
	protected Point2D.Double position;
	private SimpleNodeProxy proxy;

	// Constants
	public static int WIDTH = 12;
	public static int RADIUS = WIDTH/2;
	public static double INITARROWANGLE = 3*Math.PI/4 + Math.PI/2; // 135 degrees plus correction
	public static int INITARROWLENGTH = 15; 

	/* Maximum number of colors shown in a node */
	private static int maxDrawnMarkings = 4;

	private EditorPropGroup propGroup;

	/*
	public EditorNode(int x, int y, EditorShade s, SimpleNodeProxy np, EditorSurface e)
	{
		// This is a node
		type = NODE;

		shade = s;
		proxy = np;

		// Set position
		position = new Point2D.Double(x, y);
		proxy.setPointGeometry(new PointGeometryProxy(position));

		// Not initial by default
		proxy.setInitial(false);

		propGroup = new EditorPropGroup(this, e);
	}
	*/

	public EditorNode(int x, int y, SimpleNodeProxy np, EditorSurface e)
	{
		this(np, e);

		// Override position
		position = new Point2D.Double(x, y);
		proxy.setPointGeometry(new PointGeometryProxy(position));
	}

	public EditorNode(SimpleNodeProxy np, EditorSurface e)
	{
		// This is a node
		type = NODE;

		// Variables
		proxy = np;

		// Find position
		if (proxy.getPointGeometry() == null)
		{
			position = new Point2D.Double(1000, 1000);
			proxy.setPointGeometry(new PointGeometryProxy(position));
		}
		else
		{
			position = new Point2D.Double(proxy.getPointGeometry().getPoint().getX(), 
										  proxy.getPointGeometry().getPoint().getY());
			proxy.getPointGeometry().setPoint(position);
		}

		// Init propositions
		propGroup = new EditorPropGroup(this, e);
	}

	public EditorPropGroup getPropGroup()
	{
		return propGroup;
	}

    public void addProposition(IdentifierProxy i)
    {
	if (!proxy.getPropositions().contains(i)) {
	    proxy.getPropositions().add(i);
	}
    }

	public void setInitial(boolean newinitial)
	{
		proxy.setInitial(newinitial);
	}

	public int hashCode()
	{
		return proxy.hashCode();
	}

	public boolean isInitial()
	{
		return proxy.isInitial();
	}

	public boolean setName(String n, JComponent c)
	{
		if (n.length() == 0)
		{
			return false;
		}

		try
		{
			proxy.setName(n);
		}
		catch (final DuplicateNameException e)
		{
			JOptionPane.showMessageDialog(c, e.getMessage());

			return false;
		}

		return true;
	}

	public String getName()
	{
		return proxy.getName();
	}

	public void setX(int newxposition)
	{
		position.setLocation(newxposition, getY());
	}

	public void setY(int newyposition)
	{
		position.setLocation(getX(), newyposition);
	}

	public int getX()
	{
		return (int) position.getX();
	}

	public int getY()
	{
		return (int) position.getY();
	}

	public Point2D.Double getPosition()
	{
		return position;
	}

	/**
	 * Returns true if the position (x, y) is above the drawn node (approximately).
	 */
	public boolean wasClicked(int x, int y)
	{
		// Within the square? Why not circle?
		return (((getX() - RADIUS) <= x) && 
				(x <= (getX() + RADIUS)) && 
				((getY() - RADIUS) <= y) && 
				(y <= (getY() + RADIUS)));
	}

	public Set getColours(java.util.List eventDeclList)
	{
		Set s = new HashSet();

		for (int i = 0; i < proxy.getPropositions().size(); i++)
		{
			String n = ((IdentifierProxy) proxy.getPropositions().get(i)).getName();

			for (int j = 0; j < eventDeclList.size(); j++)
			{
				if (((EventDeclProxy) eventDeclList.get(j)).getName().equals(n))
				{
					EventDeclProxy e = (EventDeclProxy) eventDeclList.get(j);
					ColorGeometryProxy c = e.getColorGeometry();

					if (c == null)
					{
						c = new ColorGeometryProxy(EditorColor.DEFAULTMARKINGCOLOR);

						e.setColorGeometry(c);
					}

					if (c.getColorSet().isEmpty())
					{
						c.getColorSet().add(EditorColor.DEFAULTMARKINGCOLOR);
					}

					Iterator iterator = c.getColorSet().iterator();

					while (iterator.hasNext())
					{
						s.add(iterator.next());
					}
				}
			}
		}

		return s;
	}

	public SimpleNodeProxy getProxy()
	{
		return proxy;
	}

	public void setProxy(SimpleNodeProxy snp)
	{
		proxy = snp;
	}

	public int radius()
	{
		return (int) RADIUS;
	}

	public int getWidth()
	{
		return WIDTH;
	}

	/**
	 * Returns an ellipse that outlines the node.
	 */
	public Ellipse2D.Double getEllipsicalOutline()
	{
		return new Ellipse2D.Double(getX() - RADIUS, getY() - RADIUS, WIDTH, WIDTH);
	}

	/**
	 * Returns a rectangle that outlines the node.
	 */
	public Rectangle2D.Double getRectangularOutline()
	{
		return new Rectangle2D.Double(getX() - RADIUS, getY() - RADIUS, WIDTH, WIDTH);
	}

	public void drawObject(Graphics g, java.util.List EventDeclList)
	{
		Graphics2D g2d = (Graphics2D) g;
		g2d.setStroke(BASICSTROKE);

		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setColor(getColor());

		propGroup.setPanelLocation();
		if (!isSelected())
		{
			propGroup.setVisible(false);
		}
		
		// Draw shadow?
		if (shadow && isHighlighted())
		{
			g2d.setStroke(SHADOWSTROKE); 
			g2d.setColor(getShadowColor());				
			g2d.drawOval(getX() - RADIUS, getY() - RADIUS, WIDTH, WIDTH);			
			g2d.setColor(getColor());
			g2d.setStroke(BASICSTROKE);
		}

		// Draw the inside of the node
		Set colours = getColours(EventDeclList);
		Iterator i = colours.iterator();
		if (colours.size() == 0)
		{
			// There is no marking!
			// Draw the background white!
			g2d.setColor(Color.WHITE);
			g2d.fillOval(getX() - RADIUS, getY() - RADIUS, WIDTH, WIDTH);
		}
		else if (colours.size() <= maxDrawnMarkings)
		{
			Arc2D.Double a = new Arc2D.Double();
			double startAngle = 0;
			double deltaAngle = (double) (360/colours.size());

			while (i.hasNext())
			{
				// There are markings but they are fewer than maxDrawnMarkings+1! 
				// Draw nice colored pies!!
				a.setArcByCenter(getX(), getY(), RADIUS, startAngle, deltaAngle, Arc2D.PIE);
				startAngle += deltaAngle;
				
				g2d.setColor((Color) i.next());
				g2d.fill(a);
				//g2d.draw(a);
			}
		}
		else
		{
			// More than maxDrawnMarkings markings! Use the default marking color and draw a cross on top!
			g2d.setColor(EditorColor.DEFAULTMARKINGCOLOR);
			g2d.fillOval(getX() - RADIUS, getY() - RADIUS, WIDTH, WIDTH);
			
			//g2d.setColor(EditorColor.DEFAULTCOLOR);
			g2d.setColor(Color.WHITE);
			g2d.drawLine(getX(), getY() - RADIUS, getX(), getY() + RADIUS);
			g2d.drawLine(getX() - RADIUS, getY(), getX() + RADIUS, getY());
		}		

		// Draw the border of the node
		g2d.setColor(getColor());
		if (isInitial())
		{
			// Draw initial state arrow
			drawInitialStateArrow(g2d);

			// Draw line thicker!
			//g2d.setStroke(DOUBLESTROKE);
		}
		g2d.drawOval(getX() - RADIUS, getY() - RADIUS, WIDTH, WIDTH);			
		g2d.setStroke(BASICSTROKE);
	}

	private void drawInitialStateArrow(Graphics2D g2d)
	{
		// Draw line
		int borderX = getX() + (int) ((RADIUS+4) * Math.sin(INITARROWANGLE));
		int borderY = getY() + (int) ((RADIUS+4) * Math.cos(INITARROWANGLE));
		int outerX = borderX + (int) (INITARROWLENGTH * Math.sin(INITARROWANGLE));
		int outerY = borderY + (int) (INITARROWLENGTH * Math.cos(INITARROWANGLE));
		g2d.setStroke(BASICSTROKE);
		g2d.drawLine(borderX, borderY, outerX, outerY);
		
		/*
		// Draw triangle
		int height = 8; // Triangle height
		int[] xcoords = new int[3];
		int[] ycoords = new int[3];
		//xcoords[0] = borderX;
		//ycoords[0] = borderY;
		xcoords[0] = getX() + (int) ((RADIUS+1) * Math.sin(INITARROWANGLE));
		ycoords[0] = getY() + (int) ((RADIUS+1) * Math.cos(INITARROWANGLE));
		xcoords[1] = xcoords[0] + (int) (height * Math.sin(INITARROWANGLE - Math.PI / 6));
		ycoords[1] = ycoords[0] + (int) (height * Math.cos(INITARROWANGLE - Math.PI / 6));
		xcoords[2] = xcoords[0] + (int) (height * Math.cos(Math.PI / 2 - (INITARROWANGLE + Math.PI / 6)));
		ycoords[2] = ycoords[0] + (int) (height * Math.sin(Math.PI / 2 - (INITARROWANGLE + Math.PI / 6)));
		g2d.fillPolygon(xcoords, ycoords, 3);
		*/

		// The angle and the point (the rest of the code is copied from EditorEdge!)
		double theta = INITARROWANGLE;
		int x = (int) Math.ceil(getX() + Math.sin(theta)*EditorNode.RADIUS);
		int y = (int) Math.ceil(getY() + Math.cos(theta)*EditorNode.RADIUS);

		// The length of the side of the arrow
		int length = 10;
		// The angular width of the arrow (half of it actually)
		double phi = Math.PI / 8;
		
		// Arrays of coordinates for the corners
		int[] xcoords = new int[3];
		int[] ycoords = new int[3];		
		
		// Draw arrow at the control point
		xcoords[0] = x;// - (int) ((Math.sqrt(Math.pow(length, 2) + Math.pow(length, 2))/2) * Math.sin(theta));
		ycoords[0] = y;// - (int) ((Math.sqrt(Math.pow(length, 2) + Math.pow(length, 2))/2) * Math.cos(theta));
		xcoords[1] = xcoords[0] + (int) (length * Math.sin(theta - phi));
		ycoords[1] = ycoords[0] + (int) (length * Math.cos(theta - phi));
		xcoords[2] = xcoords[0] + (int) (length * Math.cos(Math.PI / 2 - (theta + phi)));
		ycoords[2] = ycoords[0] + (int) (length * Math.sin(Math.PI / 2 - (theta + phi)));
		
		// Do the drawing!
		g2d.fillPolygon(xcoords, ycoords, 3); 
	}
}
