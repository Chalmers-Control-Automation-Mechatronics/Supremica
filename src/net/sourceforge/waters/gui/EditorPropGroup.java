//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   EditorLabelGroup
//###########################################################################
//# $Id: EditorPropGroup.java,v 1.7 2005-12-12 20:23:14 siw4 Exp $
//###########################################################################


package net.sourceforge.waters.gui;

import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;

import net.sourceforge.waters.subject.base.ModelObserver;
import net.sourceforge.waters.subject.base.ModelChangeEvent;
import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.module.EventListExpressionSubject;
import net.sourceforge.waters.subject.module.IdentifierSubject;
import net.sourceforge.waters.xsd.module.AnchorPosition;


public class EditorPropGroup
	extends EditorObject
	implements ModelObserver
{

	//#######################################################################
	//# Constructor
	public EditorPropGroup(EditorNode par, EditorSurface e)
	{
		// This is a... labelgroup!
		type = LABELGROUP;

		mSubject = par.getSubject().getPropositions();
		mSubject.addModelObserver(this);
		parent = par;
		panel = new JPanel();

		panel.setVisible(false);
		panel.setOpaque(false);
		panel.setLayout(null);

		verticalA = TOP;
		horizontalA = LEFT;
		offset = new Point(5, 5);

		final List<AbstractSubject> list = mSubject.getEventListModifiable();
		for (final AbstractSubject entry : list) {
			addToPanel(entry);
		}
		
		e.add(panel);
		setPanelLocation();

		panel.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "up");
		panel.getActionMap().put("up", new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				if ((selectedLabel > 0) && (selectedLabel < list.size()))
				{
					IdentifierSubject i =
						(IdentifierSubject) list.get(selectedLabel);
					list.remove(selectedLabel);
					list.add(selectedLabel - 1, i);					
					selectedLabel--;
				}
			}
		});

		panel.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "down");
		panel.getActionMap().put("down", new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				if ((selectedLabel >= 0) && (selectedLabel < list.size() - 1))
				{
					IdentifierSubject i =
						(IdentifierSubject) list.get(selectedLabel);
					list.remove(selectedLabel);
					list.add(selectedLabel + 1, i);
					selectedLabel++;
				}
			}
		});
	}


	public void removeFromSurface(EditorSurface e)
	{
		e.remove(panel);
	}

	public EditorNode getParent()
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
		for (int i = 0; i < panel.getComponentCount(); i++)	{
			final JLabel label = (JLabel)panel.getComponent(i);
			if (label.getBounds().contains(ex, ey))	{
				selectedLabel = i;
				return;
			}
		}
		selectedLabel = -1;
	}

	public void setPanelLocation()
	{
		int x = (int) (offset.getX() + parent.getX());    // - ((double)(horizontalA / 2) * panel.getWidth()));
		int y = (int) (offset.getY() + parent.getY());    // - ((double)(verticalA / 2) * panel.getHeight()));

		panel.setLocation(x, y);
		for (int i = 0; i < panel.getComponentCount(); i++) {
			final JLabel label = (JLabel)panel.getComponent(i);
			if (i == selectedLabel)	{
				label.setForeground(Color.RED);
			} else {
				label.setForeground(getColor());
			}
		}
	}

	public void setVisible(boolean v)
	{
		panel.setVisible(v);
	}

	public boolean getVisible()
	{
		return panel.isVisible();
	}

	public void setOffSet(int nxoff, int nyoff)
	{
		offset.setLocation(nxoff, nyoff);
	}

	public void setPosition(double x, double y)
	{
		offset.setLocation(x - (int) parent.getX(), y - (int) parent.getY());
	}

	public void addEvent(final IdentifierSubject ident)
	{
		final List<AbstractSubject> list = mSubject.getEventListModifiable();
		list.add(ident);
		addToPanel(ident);
		resizePanel();
		panel.getParent().repaint();
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

	private void addToPanel(final AbstractSubject entry)
	{
		final String name = entry.toString();
		final JLabel l = new JLabel(name);
		l.setBorder(new EmptyBorder(0, 0, 0, 0));
		l.setOpaque(false);
		panel.add(l);
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

        public Point2D getPosition()
        {
	    return new Point2D.Double(getX(), getY());
	}
    

	//#######################################################################
	//# Overrides for Abstract Base Class EditorObject
	public EventListExpressionSubject getSubject()
	{
		return mSubject;
	}
	
	public void modelChanged(ModelChangeEvent e)
	{
		panel.removeAll();
		final List<AbstractSubject> list = mSubject.getEventListModifiable();
		for (final AbstractSubject entry : list) {
			addToPanel(entry);
		}
		resizePanel();
	}


	//#######################################################################
	//# Data Members
	private int verticalA = 1;
	private int horizontalA = 1;
	private int selectedLabel = -1;
	private final JPanel panel;
	private final EditorNode parent;
	private final Point offset;
	private final EventListExpressionSubject mSubject;

	public static final int LEFT = 0;
	public static final int RIGHT = 2;
	public static final int CENTER = 1;
	public static final int TOP = 0;
	public static final int BOTTOM = 2;

}
