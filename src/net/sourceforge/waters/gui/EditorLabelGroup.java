
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.gui
//# CLASS:   EditorLabelGroup
//###########################################################################
//# $Id: EditorLabelGroup.java,v 1.3 2005-02-21 10:22:09 flordal Exp $
//###########################################################################
package net.sourceforge.waters.gui;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.font.*;
import java.lang.reflect.*;
import java.util.*;
import java.beans.*;
import net.sourceforge.waters.xsd.module.AnchorPosition;
import net.sourceforge.waters.model.module.*;
import net.sourceforge.waters.model.expr.IdentifierProxy;

public class EditorLabelGroup
	extends EditorObject
{
	public static int LEFT = 0;
	public static int RIGHT = 2;
	public static int CENTER = 1;
	public static int TOP = 0;
	public static int BOTTOM = 2;
	private int verticalA = 1;
	private int horizontalA = 1;
	private int selectedLabel = -1;
	private final JPanel panel;
	private final EditorEdge parent;
	private final ArrayList events;
	private LabelBlockProxy proxy;

	public void removeFromSurface(EditorSurface e)
	{
		e.remove(panel);
	}

	public EditorEdge getParent()
	{
		return parent;
	}

	public boolean wasClicked(int ex, int ey)
	{
		if (panel == null)
		{
			return false;
		}

		Rectangle r = new Rectangle(panel.getX(), panel.getY(), panel.getWidth(), panel.getHeight());

		selectedLabel = -1;

		return (r.contains(ex, ey));
	}

	public void setSelectedLabel(int ex, int ey)
	{
		ex -= panel.getX();
		ey -= panel.getY();

		panel.requestFocus();

		for (int i = 0; i < events.size(); i++)
		{
			JLabel l = (JLabel) events.get(i);

			if (l.getBounds().contains(ex, ey))
			{
				selectedLabel = i;

				return;
			}
		}

		selectedLabel = -1;
	}

	public void setPanelLocation()
	{
		int x = (int) (proxy.getGeometry().getOffset().getX() + parent.getTPointX());    // - ((double)(horizontalA / 2) * panel.getWidth()));
		int y = (int) (proxy.getGeometry().getOffset().getY() + parent.getTPointY());    // - ((double)(verticalA / 2) * panel.getHeight()));

		panel.setLocation(x, y);

		for (int i = 0; i < events.size(); i++)
		{
			JLabel l = (JLabel) events.get(i);

			if (selected)
			{
				l.setForeground(Color.BLUE);
			}
			else
			{
				l.setForeground(Color.BLACK);
			}

			if (i == selectedLabel)
			{
				l.setForeground(Color.RED);
			}
		}
	}

	public void setOffset(int nxoff, int nyoff)
	{
		proxy.getGeometry().setOffset(new Point2D.Double(nxoff, nyoff));
	}

	public int getOffsetX()
	{
		return (int) proxy.getGeometry().getOffset().getX();
	}

	public int getOffsetY()
	{
		return (int) proxy.getGeometry().getOffset().getY();
	}

	public void moveTo(int x, int y)
	{
		proxy.getGeometry().setOffset(new Point2D.Double(x - (int) parent.getTPointX(), y - (int) parent.getTPointY()));
	}

	public void addEvent(IdentifierProxy i)    //put stuff for events from proxy
	{
		proxy.add(i);
		addToPanel(i.toString());
		resizePanel();
	}

	public void removeEvent(int i)    //put stuff for events from proxy
	{
		JLabel l = (JLabel) events.get(i);

		events.remove(i);
		panel.remove(l);

		for (int j = 0; j < proxy.size(); j++)
		{
			if (((IdentifierProxy) proxy.get(j)).toString().equals(l.getText()))
			{
				proxy.remove(i);
			}
		}
	}

	public void setAnchor(String anchor)
	{
		if (anchor.equals(AnchorPosition.NW))
		{
			verticalA = TOP;
			horizontalA = LEFT;
		}

		if (anchor.equals(AnchorPosition.N))
		{
			verticalA = TOP;
			horizontalA = CENTER;
		}

		if (anchor.equals(AnchorPosition.NE))
		{
			verticalA = TOP;
			horizontalA = RIGHT;
		}

		if (anchor.equals(AnchorPosition.W))
		{
			verticalA = CENTER;
			horizontalA = LEFT;
		}

		if (anchor.equals(AnchorPosition.C))
		{
			verticalA = CENTER;
			horizontalA = CENTER;
		}

		if (anchor.equals(AnchorPosition.E))
		{
			verticalA = CENTER;
			horizontalA = RIGHT;
		}

		if (anchor.equals(AnchorPosition.SW))
		{
			verticalA = BOTTOM;
			horizontalA = LEFT;
		}

		if (anchor.equals(AnchorPosition.S))
		{
			verticalA = BOTTOM;
			horizontalA = CENTER;
		}

		if (anchor.equals(AnchorPosition.SE))
		{
			verticalA = BOTTOM;
			horizontalA = RIGHT;
		}
	}

	private void addToPanel(String n)
	{
		JLabel l = new JLabel(n);

		l.setBorder(new EmptyBorder(0, 0, 0, 0));
		l.setOpaque(false);
		panel.add(l);
		events.add(l);
	}

	private void resizePanel()
	{
		int height = 0;
		int width = 0;

		for (int i = 0; i < panel.getComponentCount(); i++)
		{
			JComponent c = (JComponent) panel.getComponent(i);

			c.setLocation(0, height);
			c.setSize(c.getPreferredSize());

			height += c.getHeight();

			if (width < c.getWidth())
			{
				width = c.getWidth();
			}
		}

		panel.setSize(width, height);
	}

	public int getX()
	{
		return panel.getX();
	}

	public int getY()
	{
		return panel.getY();
	}

	public EditorLabelGroup(EditorEdge par, EditorSurface e)
	{
		proxy = par.getProxy().getLabelBlock();
		parent = par;
		events = new ArrayList();
		panel = new JPanel();

		panel.setVisible(true);
		panel.setOpaque(false);
		panel.setLayout(null);

		if (proxy.getGeometry() == null)
		{
			proxy.setGeometry(new LabelGeometryProxy(0, 10, AnchorPosition.NW));
		}

		verticalA = TOP;
		horizontalA = LEFT;

		setAnchor(proxy.getGeometry().getAnchor().getValue());

		for (int i = 0; i < proxy.size(); i++)
		{
			addToPanel(((IdentifierProxy) proxy.get(i)).toString());
		}

		resizePanel();
		e.add(panel);
		setPanelLocation();

		type = LABELGROUP;

		panel.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "up");
		panel.getActionMap().put("up", new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				if ((selectedLabel > 0) && (selectedLabel < events.size()))
				{
					IdentifierProxy i = (IdentifierProxy) proxy.get(selectedLabel);

					proxy.remove(selectedLabel);
					proxy.add(selectedLabel - 1, i);
					events.clear();
					panel.removeAll();

					for (int j = 0; j < proxy.size(); j++)
					{
						addToPanel(((IdentifierProxy) proxy.get(j)).toString());
					}

					resizePanel();

					selectedLabel--;
				}
			}
		});
		panel.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "down");
		panel.getActionMap().put("down", new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				if ((selectedLabel >= 0) && (selectedLabel < events.size() - 1))
				{
					IdentifierProxy i = (IdentifierProxy) proxy.get(selectedLabel);

					proxy.remove(selectedLabel);
					proxy.add(selectedLabel + 1, i);
					events.clear();
					panel.removeAll();

					for (int j = 0; j < proxy.size(); j++)
					{
						addToPanel(((IdentifierProxy) proxy.get(j)).toString());
					}

					resizePanel();

					selectedLabel++;
				}
			}
		});
	}
}
