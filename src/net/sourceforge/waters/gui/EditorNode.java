
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.gui
//# CLASS:   EditorNode
//###########################################################################
//# $Id: EditorNode.java,v 1.4 2005-02-21 21:33:30 flordal Exp $
//###########################################################################
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
		position.setLocation(newxposition, position.getY());
	}

	public void setY(int newyposition)
	{
		position.setLocation(position.getX(), newyposition);
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
		return (((position.getX() - WIDTH / 2) <= Cxposition) && (Cxposition <= (position.getX() + WIDTH / 2)) && ((position.getY() - WIDTH / 2) <= Cyposition) && (Cyposition <= (position.getY() + WIDTH / 2)));
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
		return (int) WIDTH / 2;
	}

	public int getWidth()
	{
		return WIDTH;
	}

	public Ellipse2D.Double getEllipse()
	{
		return (new Ellipse2D.Double(position.getX() - WIDTH / 2, position.getY() - WIDTH / 2, WIDTH, WIDTH));
	}

	public void drawObject(Graphics g, java.util.List EventDeclList)
	{
		Graphics2D g2d = (Graphics2D) g;
		Set colours = getColours(EventDeclList);
		Iterator i = colours.iterator();

		propGroup.setPanelLocation();
		g2d.setColor(EditorColor.DEFAULTCOLOR);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		if (selected)
		{
			g2d.setColor(EditorColor.SELECTCOLOR);
			propGroup.setPanelLocation();
		}
		else
		{
			if (!getHighlighted())
			{
				g2d.setColor(EditorColor.DEFAULTCOLOR);
			}
			else
			{
				g2d.setColor(EditorColor.HIGHLIGHTCOLOR);
			}

			propGroup.setVisible(false);
		}

		double angle = 0;
		Rectangle2D.Double r = new Rectangle2D.Double();
		Arc2D.Double a = new Arc2D.Double();

		g2d.drawOval((int) position.getX() - (WIDTH / 2), (int) position.getY() - (WIDTH / 2), WIDTH, WIDTH);

		if (proxy.isInitial())
		{
			if (!selected)
			{
				if (!getHighlighted())
				{
					g2d.setColor(EditorColor.DEFAULTCOLOR);
				}
				else
				{
					g2d.setColor(EditorColor.HIGHLIGHTCOLOR);
				}
			}

			g2d.fillOval((int) position.getX() - (WIDTH / 2), (int) position.getY() - (WIDTH / 2), WIDTH + 1, WIDTH + 1);

			if (!i.hasNext())
			{
				g2d.setColor(new Color(shade.getRGB()));
				g2d.fillOval((int) position.getX() - (WIDTH / 2) + 2, (int) position.getY() - (WIDTH / 2) + 2, WIDTH - 3, WIDTH - 3);
			}

			if (colours.size() <= 4)
			{
				while (i.hasNext())
				{
					a.setArcByCenter(position.getX(), position.getY(), radius() - 2, angle, 360 / (double) colours.size(), Arc2D.PIE);

					angle += 360 / (double) colours.size();

					g2d.setColor((Color) i.next());
					g2d.fill(a);
					g2d.draw(a);
				}
			}
			else
			{
				g2d.setColor(EditorColor.DEFAULTMARKINGCOLOR);
				g2d.fillOval((int) position.getX() - (WIDTH / 2) + 2, (int) position.getY() - (WIDTH / 2) + 2, WIDTH - 2, WIDTH - 2);

				if (selected)
				{
					g2d.setColor(EditorColor.SELECTCOLOR);
					propGroup.setPanelLocation();
				}
				else
				{
					if (!getHighlighted())
					{
						g2d.setColor(EditorColor.DEFAULTCOLOR);
					}
					else
					{
						g2d.setColor(EditorColor.HIGHLIGHTCOLOR);
					}
				}
				
				g2d.drawLine((int) position.getX(), (int) position.getY() - (WIDTH / 2), (int) position.getX(), (int) position.getY() + (WIDTH / 2));
				g2d.drawLine((int) position.getX() - (WIDTH / 2), (int) position.getY(), (int) position.getX() + (WIDTH / 2), (int) position.getY());
			}
		}
		else
		{
			if (!i.hasNext())
			{
				g2d.setColor(new Color(shade.getRGB()));
				g2d.fillOval((int) position.getX() - (WIDTH / 2) + 1, (int) position.getY() - (WIDTH / 2) + 1, WIDTH - 1, WIDTH - 1);
			}

			if (colours.size() <= 4)
			{
				while (i.hasNext())
				{
					a.setArcByCenter(position.getX(), position.getY(), radius() - 1, angle, 360 / (double) colours.size(), Arc2D.PIE);

					angle += 360 / (double) colours.size();

					g2d.setColor((Color) i.next());
					g2d.fill(a);
					g2d.draw(a);
				}
			}
			else
			{
				g2d.setColor(EditorColor.DEFAULTMARKINGCOLOR);
				g2d.fillOval((int) position.getX() - (WIDTH / 2) + 1, (int) position.getY() - (WIDTH / 2) + 1, WIDTH - 1, WIDTH - 1);

				if (selected)
				{
					g2d.setColor(EditorColor.SELECTCOLOR);
					propGroup.setPanelLocation();
				}
				else
				{
					if (!getHighlighted())
					{
						g2d.setColor(EditorColor.DEFAULTCOLOR);
					}
					else
					{
						g2d.setColor(EditorColor.HIGHLIGHTCOLOR);
					}
				}

				g2d.drawLine((int) position.getX(), (int) position.getY() - (WIDTH / 2), (int) position.getX(), (int) position.getY() + (WIDTH / 2));
				g2d.drawLine((int) position.getX() - (WIDTH / 2), (int) position.getY(), (int) position.getX() + (WIDTH / 2), (int) position.getY());
			}
		}
	}
}
