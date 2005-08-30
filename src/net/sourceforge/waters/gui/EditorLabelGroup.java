
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.gui
//# CLASS:   EditorLabelGroup
//###########################################################################
//# $Id: EditorLabelGroup.java,v 1.12 2005-08-30 00:18:45 siw4 Exp $
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
	private final EditorEdge parent;
	private LabelBlockProxy proxy;

	private final ArrayList events;
	/** Holds labels that are shown on EditorSurface. */
	private final JPanel panel;
	/*
	private final JPanel shadowPanel;
	*/

	public static final int DEFAULTOFFSETX = 0;
	public static final int DEFAULTOFFSETY = 10;

	public void removeFromSurface(EditorSurface e)
	{
		e.remove(panel);
		/*
		e.remove(shadowPanel);
		*/
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

		return (r.contains(ex, ey));
	}

	public Rectangle getBounds()
	{
		return panel.getBounds();
	}

	public void setHighlighted(boolean s)
	{
		super.setHighlighted(s);
		parent.setHighlighted(s);
	}

	public void setSelectedLabel(int ex, int ey)
	{
		ex -= panel.getX();
		ey -= panel.getY();

		panel.requestFocus();

		selectedLabel = getLabelIndexAt(ex, ey);
	}

    private int getLabelIndexAt(int ex, int ey)
    {
	for (int i = 0; i < events.size(); i++) {
	    JLabel l = (JLabel) events.get(i);
	    
	    if (l.getBounds().contains(ex, ey)) {
		return i;	 
	    }
	}
	return -1;
    }

	public void setPanelLocation()
	{
		int x = (int) (proxy.getGeometry().getOffset().getX() + parent.getTPointX());    // - ((double)(horizontalA / 2) * panel.getWidth()));
		int y = (int) (proxy.getGeometry().getOffset().getY() + parent.getTPointY());    // - ((double)(verticalA / 2) * panel.getHeight()));

		panel.setLocation(x, y);
		/*
		shadowPanel.setLocation(x+2, y+2);
		*/

		/*
		// Draw shadow
		if (isHighlighted())
		{
			shadowPanel.setVisible(true);

			for (int i = 0; i < shadowPanel.getComponentCount(); i++)
			{
				JComponent c = (JComponent) shadowPanel.getComponent(i);
				c.setForeground(getShadowColor());
			}
		}
		else
		{
			shadowPanel.setVisible(false);
		}
		*/
		
		for (int i = 0; i < events.size(); i++)
		{
			JLabel l = (JLabel) events.get(i);

			l.setForeground(getColor());

			if (i == selectedLabel)
			{
				l.setForeground(Color.RED);
			}

			/*
			// Draw shadow
			if (isHighlighted())
			{
				//l.setOpaque(true);
				l.setBackground(getShadowColor());
			}
			else
			{
				//l.setOpaque(false);
				l.setBackground(EditorColor.INVISIBLE);
			}
			*/
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

	public void setPosition(double x, double y)
	{
		proxy.getGeometry().setOffset(new Point2D.Double(x - (int) parent.getTPointX(), y - (int) parent.getTPointY()));
	}

    public Point2D getPosition()
    {
	return new Point2D.Double(getX(), getY());
    }

	public void addEvent(IdentifierProxy i)    //put stuff for events from proxy
	{
	    if (!proxy.contains(i)) {
		proxy.add(i);
		addToPanel(i.toString());
		resizePanel();
	    }
	}

	public void removeEvent(int i)    //put stuff for events from proxy
	{
		events.remove(i);
		panel.remove(i);
		/*
		shadowPanel.remove(i);
		*/
		proxy.remove(i);
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
		l.setBackground(getShadowColor());

		events.add(l);
		panel.add(l);
		/*
		shadowPanel.add(new JLabel(l.getText()));
		*/
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

			/*
			c = (JComponent) shadowPanel.getComponent(i);
			c.setLocation(0, height);
			c.setSize(c.getPreferredSize());
			*/

			height += c.getHeight();

			if (width < c.getWidth())
			{
				width = c.getWidth();
			}
		}

		panel.setSize(width, height);
		/*
		shadowPanel.setSize(width, height);
		*/

		panel.repaint();
	}

	public int getX()
	{
		return panel.getX();
	}

	public int getY()
	{
		return panel.getY();
	}

	public int getWidth()
	{
		return panel.getWidth();
	}

	public int getHeight()
	{
		return panel.getHeight();
	}

	public void setSelected(boolean s)
	{
		super.setSelected(s);
		if (!s) {
		    selectedLabel = -1;
		}
	}

	public EditorLabelGroup(EditorEdge par, EditorSurface e)
	{
		// This is a labelgroup
		type = LABELGROUP;

		proxy = par.getProxy().getLabelBlock();
		parent = par;
		events = new ArrayList();
		panel = new JPanel();
		/*
		shadowPanel = new JPanel();
		*/

		panel.setVisible(true);
		panel.setOpaque(false);
		panel.setLayout(null);

		/*
		shadowPanel.setVisible(true);
		shadowPanel.setOpaque(false);
		shadowPanel.setLayout(null);
		*/

		if (proxy.getGeometry() == null)
		{
			proxy.setGeometry(new LabelGeometryProxy(DEFAULTOFFSETX, DEFAULTOFFSETY, AnchorPosition.NW));
		}

		verticalA = TOP;
		horizontalA = LEFT;

		setAnchor(proxy.getGeometry().getAnchor().getValue());

		for (int i = 0; i < proxy.size(); i++)
		{
			addToPanel(((IdentifierProxy) proxy.get(i)).toString());
		}

		resizePanel();
		/*
		e.add(shadowPanel);
		*/
		e.add(panel);
		setPanelLocation();

		panel.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "up");
		panel.getActionMap().put("up", new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				if ((selectedLabel > 0) && (selectedLabel < events.size()))
				{
					IdentifierProxy i = (IdentifierProxy) proxy.get(selectedLabel);

					// Remove label and add to new position in list
					proxy.remove(selectedLabel);
					proxy.add(selectedLabel - 1, i);

					// Clear all lists
					events.clear();
					panel.removeAll();
					/*
					shadowPanel.removeAll();
					*/

					for (int j = 0; j < proxy.size(); j++)
					{
						addToPanel(((IdentifierProxy) proxy.get(j)).toString());
					}

					resizePanel();
					panel.repaint();
					/*
					shadowPanel.repaint();
					*/

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

					// Remove label and add to new position in list
					proxy.remove(selectedLabel);
					proxy.add(selectedLabel + 1, i);

					// Clear all lists
					events.clear();
					panel.removeAll();
					/*
					shadowPanel.removeAll();
					*/

					for (int j = 0; j < proxy.size(); j++)
					{
						addToPanel(((IdentifierProxy) proxy.get(j)).toString());
					}

					resizePanel();
					panel.repaint();
					/*
					shadowPanel.repaint();
					*/

					selectedLabel++;
				}
			}
		});
		panel.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "delete");
		panel.getActionMap().put("delete", new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				// Is a label selected?
				if ((selectedLabel >= 0) && (selectedLabel < events.size()))
				{
					// Remove event
					removeEvent(selectedLabel);

					resizePanel();

					selectedLabel = -1;
				}
			}
		});
	}
}
