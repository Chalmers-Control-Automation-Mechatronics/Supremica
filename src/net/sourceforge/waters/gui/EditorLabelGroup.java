//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   EditorLabelGroup
//###########################################################################
//# $Id: EditorLabelGroup.java,v 1.14 2005-12-12 20:23:14 siw4 Exp $
//###########################################################################


package net.sourceforge.waters.gui;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.awt.font.*;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.border.*;

import net.sourceforge.waters.gui.command.Command;
import net.sourceforge.waters.gui.command.RemoveEventCommand;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.subject.base.ModelObserver;
import net.sourceforge.waters.subject.base.ModelChangeEvent;
import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.module.IdentifierSubject;
import net.sourceforge.waters.subject.module.LabelBlockSubject;
import net.sourceforge.waters.subject.module.LabelGeometrySubject;
import net.sourceforge.waters.xsd.module.AnchorPosition;


public class EditorLabelGroup
	extends EditorObject
	implements ModelObserver
{

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
	for (int i = 0; i < panel.getComponentCount(); i++)
	{
	    JLabel l = (JLabel) panel.getComponent(i);
	    
	    if (l.getBounds().contains(ex, ey))
		{
			return i;	 
	    }
	}
		return -1;
    }

	public void setPanelLocation()
	{
		int x = (int) (mSubject.getGeometry().getOffset().getX() + parent.getTPointX());    // - ((double)(horizontalA / 2) * panel.getWidth()));
		int y = (int) (mSubject.getGeometry().getOffset().getY() + parent.getTPointY());    // - ((double)(verticalA / 2) * panel.getHeight()));

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
		
		for (int i = 0; i < panel.getComponentCount(); i++)
		{
			JLabel l = (JLabel) panel.getComponent(i);

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
		mSubject.getGeometry().setOffset(new Point2D.Double(nxoff, nyoff));
	}

	public int getOffsetX()
	{
		return (int) mSubject.getGeometry().getOffset().getX();
	}

	public int getOffsetY()
	{
		return (int) mSubject.getGeometry().getOffset().getY();
	}

	public void setPosition(double x, double y)
	{
		mSubject.getGeometry().setOffset(new Point2D.Double(x - (int) parent.getTPointX(), y - (int) parent.getTPointY()));
	}

    public Point2D getPosition()
    {
	return new Point2D.Double(getX(), getY());
    }

    /**
	 * Puts stuff for events from subject.
	 */
	public void addEvent(IdentifierSubject ident)
	{
		final List<AbstractSubject> list = mSubject.getEventListModifiable();
	    if (list.add(ident)) {
			addToPanel(ident.toString());
			resizePanel();
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
		l.setBackground(getShadowColor());

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

	public EditorLabelGroup(EditorEdge par, final EditorSurface e)
	{
		// This is a labelgroup
		type = LABELGROUP;

		mSubject = par.getSubject().getLabelBlock();
		mSubject.addModelObserver(this);
		parent = par;
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

		if (mSubject.getGeometry() == null)	{
			final Point2D point = new Point(DEFAULTOFFSETX, DEFAULTOFFSETY);
			final LabelGeometrySubject geo =
				new LabelGeometrySubject(point, AnchorPosition.NW);
			mSubject.setGeometry(geo);
		}

		verticalA = TOP;
		horizontalA = LEFT;

		setAnchor(mSubject.getGeometry().getAnchor().getValue());

		for (final Proxy proxy : mSubject.getEventList())
		{
			final String text = proxy.toString();
			addToPanel(text);
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
				if ((selectedLabel > 0) && (selectedLabel < panel.getComponentCount()))
				{
					final List<AbstractSubject> list =
						mSubject.getEventListModifiable();
					final IdentifierSubject ident =
						(IdentifierSubject) list.get(selectedLabel);
					// Remove label and add to new position in list
					list.remove(selectedLabel);
					list.add(selectedLabel - 1, ident);
					// Clear all lists
					/*
					shadowPanel.removeAll();
					*/
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
				if ((selectedLabel >= 0) && (selectedLabel < panel.getComponentCount() - 1))
				{
					final List<AbstractSubject> list =
						mSubject.getEventListModifiable();
					final IdentifierSubject ident =
						(IdentifierSubject) list.get(selectedLabel);
					// Remove label and add to new position in list
					list.remove(selectedLabel);
					list.add(selectedLabel + 1, ident);													
					/*
					shadowPanel.removeAll();
					*/
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
			public void actionPerformed(ActionEvent action)
			{
				// Is a label selected?
				if ((selectedLabel >= 0) && (selectedLabel < panel.getComponentCount()))
				{
					// Remove event
					Command removeEvent = new RemoveEventCommand(getSubject(),
											  			   (IdentifierSubject)getSubject().getEventList().get(selectedLabel));
					e.getEditorInterface().getUndoInterface().executeCommand(removeEvent);
					selectedLabel = -1;
				}
			}
		});
	}
	
	public void modelChanged(ModelChangeEvent e)
	{
		panel.removeAll();
		for (final Proxy proxy : mSubject.getEventList())
		{
			final String text = proxy.toString();
			addToPanel(text);
		}
		resizePanel();
	}


	//#######################################################################
	//# Overrides for Abstract Base Class EditorObject
	public LabelBlockSubject getSubject()
	{
		return mSubject;
	}


	//#######################################################################
	//# Data Members
	private final EditorEdge parent;
	private LabelBlockSubject mSubject;
	private int verticalA = 1;
	private int horizontalA = 1;
	private int selectedLabel = -1;

	//private final ArrayList events;
	/** Holds labels that are shown on EditorSurface. */
	private final JPanel panel;

	public static final int LEFT = 0;
	public static final int RIGHT = 2;
	public static final int CENTER = 1;
	public static final int TOP = 0;
	public static final int BOTTOM = 2;

	public static final int DEFAULTOFFSETX = 0;
	public static final int DEFAULTOFFSETY = 10;

}
