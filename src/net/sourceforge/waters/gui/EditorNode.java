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

/** <p>The internal editor representation of node objects.</p>
 *
 * <p>Nodes store geometry and {@link EditorShade} information.</p>
 *
 * @author Gian Perrone
 */
public class EditorNode
	extends EditorObject
{
	private EditorShade shade;
	protected int hash = 0;
	protected Point2D.Double position;
	private SimpleNodeProxy proxy;
	public static int WIDTH = 12;
	public static int RADIUS = WIDTH/2;
	private EditorPropGroup propGroup;

	public EditorNode(int x, int y, EditorShade s, SimpleNodeProxy np, EditorSurface e)
	{
		position = new Point2D.Double(x, y);
		type = NODE;
		shade = s;
		proxy = np;

		proxy.setInitial(false);
		proxy.setPointGeometry(new PointGeometryProxy(position));

		propGroup = new EditorPropGroup(this, e);
	}

	public EditorPropGroup getPropGroup()
	{
		return propGroup;
	}

	public EditorNode(EditorShade s, SimpleNodeProxy np, EditorSurface e)
	{
		type = NODE;
		shade = s;
		proxy = np;

		setHash(np.hashCode());

		if (proxy.getPointGeometry() == null)
		{
			position = new Point2D.Double(1000, 1000);

			proxy.setPointGeometry(new PointGeometryProxy(position));
		}
		else
		{
			position = new Point2D.Double(proxy.getPointGeometry().getPoint().getX(), proxy.getPointGeometry().getPoint().getY());

			proxy.getPointGeometry().setPoint(position);
		}

		propGroup = new EditorPropGroup(this, e);
	}

	public void setInitial(boolean newinitial)
	{
		proxy.setInitial(newinitial);
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

	public boolean wasClicked(int Cxposition, int Cyposition)
	{
		return (((getX() - RADIUS) <= Cxposition) && (Cxposition <= (getX() + RADIUS)) && ((getY() - RADIUS) <= Cyposition) && (Cyposition <= (getY() + RADIUS)));
	}

	public void setShade(EditorShade s)
	{
		shade = s;
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
		Set colours = getColours(EventDeclList);
		Iterator i = colours.iterator();

		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setColor(getColor());

		propGroup.setPanelLocation();
		if (!isSelected())
		{
			propGroup.setVisible(false);
		}
		
		// Draw the inside of the node
		if (colours.size() == 0)
		{
			// There is no marking!
			// Draw the background white!
			g2d.setColor(Color.WHITE);
			g2d.fillOval(getX() - RADIUS, getY() - RADIUS, WIDTH+1, WIDTH+1);
		}
		else if (colours.size() <= 4)
		{
			Arc2D.Double a = new Arc2D.Double();
			double startAngle = 0;
			double deltaAngle = (double) (360/colours.size());
			while (i.hasNext())
			{
				// There are markings but they are fewer than 5! 
				// Draw nice colored pies!!
				a.setArcByCenter(getX(), getY(), RADIUS+1, startAngle, deltaAngle, Arc2D.PIE);
				startAngle += deltaAngle;
				
				g2d.setColor((Color) i.next());
				g2d.fill(a);
				//g2d.draw(a);
			}
		}
		else
		{
			// More than four markings! Use the default marking color and draw a cross on top!
			g2d.setColor(EditorColor.DEFAULTMARKINGCOLOR);
			g2d.fillOval(getX() - RADIUS, getY() - RADIUS, WIDTH+1, WIDTH+1);
			
			//g2d.setColor(EditorColor.DEFAULTCOLOR);
			g2d.setColor(Color.WHITE);
			g2d.drawLine(getX(), getY() - RADIUS, getX(), getY() + RADIUS);
			g2d.drawLine(getX() - RADIUS, getY(), getX() + RADIUS, getY());
		}		

		// Draw the border of the node
		g2d.setColor(getColor());
		g2d.drawOval(getX() - RADIUS - 1, getY() - RADIUS - 1, WIDTH + 2, WIDTH + 2);
		if (isInitial())
		{
			// Draw it thicker!
			g2d.drawOval(getX() - RADIUS, getY() - RADIUS, WIDTH, WIDTH);
			//g2d.drawOval(getX() - RADIUS + 1, getY() - RADIUS + 1, WIDTH - 2, WIDTH - 2);			
		}
	}
}
