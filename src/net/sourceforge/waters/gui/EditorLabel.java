//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   EditorLabel
//###########################################################################
//# $Id: EditorLabel.java,v 1.17 2005-12-14 03:09:47 siw4 Exp $
//###########################################################################


package net.sourceforge.waters.gui;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.border.*;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.awt.font.*;
import java.lang.reflect.*;
import java.beans.*;

import net.sourceforge.waters.subject.base.ModelChangeEvent;
import net.sourceforge.waters.subject.base.ModelObserver;
import net.sourceforge.waters.subject.base.Subject;
import net.sourceforge.waters.subject.module.LabelGeometrySubject;
import net.sourceforge.waters.xsd.module.AnchorPosition;


/** 
 * <p>Editable label object for representing event and node names.</p>
 *
 * <p>A label works based on an offset from the central point of
 * the object it is tied to (the center of a node or the control point of
 * an edge, in this case).</p>
 *
 * @author Gian Perrone
 */

public class EditorLabel
	extends EditorObject
	implements ModelObserver
{

	/** 
	 * Get the object which this label is attached to
	 */
	public EditorNode getParent()
	{
		return mParent;
	}
	
	public void modelChanged(ModelChangeEvent e)
	{
		text.setText(getParent().getName());
		label.setText(getParent().getName());
	}

	public void removeFromSurface(EditorSurface e)
	{
		e.remove(text);
		/*
		e.remove(labelShadow);
		*/
		e.remove(label);
	}

	public void setEditing(boolean edit)
	{
		if (edit)
		{
			backup = text.getText();

			text.requestFocus();
			text.selectAll();
		}
		else
		{
			if (editing &&!backup.equals(text.getText()))
			{
				if (!mParent.setName(text.getText(), (JComponent) text.getRootPane()))
				{
					edit = true;

					text.requestFocus();
					text.selectAll();
				}
				
			}
		}

		editing = edit;

		text.setVisible(edit);
		label.setVisible(!edit);
		/*
		labelShadow.setVisible(!edit);
		*/
	}

	public boolean getEditing()
	{
		return editing;
	}
    
	public void setPosition(final double x, final double y)
	{
		final double px = mParent.getX();
		final double py = mParent.getY();
		setOffset(x - px, y - py);
	}

	public void setOffset(final double x, final double y)
	{
		final Point2D offset = new Point2D.Double(x, y);
		mGeometry.setOffset(offset);
	}

    public Point2D getPosition()
    {
		final double px = mParent.getX();
		final double py = mParent.getY();
		final Point2D result = mGeometry.getOffset();
		result.setLocation(px + result.getX(), py + result.getY());
		return result;
	}

	public int getOffsetX()
	{
	    return (int) mGeometry.getOffset().getX();
	}

	public int getOffsetY()
	{
	    return (int) mGeometry.getOffset().getY();
	}

	public void drawObject(Graphics g)
	{
        	if ((text == null) || (mParent == null))
		{
		    return;
		}

		if ((text.getText().length() == 0) && !editing)
		{
		    text.setText(backup);
		}

		if (text.getText().length() == 0)
		{
		    text.setSize(5, text.getHeight());
		}
		else
		{
		    text.setSize(text.getPreferredSize());
		}

		label.setForeground(getColor());
		/*
		if (shadow && isHighlighted())
		{
			labelShadow.setForeground(getShadowColor());
		}
		else
		{
		    labelShadow.setForeground(EditorColor.INVISIBLE);
		}
		*/

		int xposition = mParent.getX() + (int) mGeometry.getOffset().getX();
		int yposition = mParent.getY() + (int) mGeometry.getOffset().getY();

		text.setLocation(xposition - 1, yposition - 14);
		label.setLocation(xposition - 1, yposition - 14);
		label.setText(text.getText());
		label.setFont(text.getFont());
		label.setSize(label.getPreferredSize());

		/*
		labelShadow.setLocation(label.getX()+2, label.getY()+2);
		labelShadow.setText(text.getText());
		labelShadow.setSize(labelShadow.getPreferredSize());
		*/
		boundingRect.setRect(xposition - 1, yposition - 14, text.getWidth(), text.getHeight());

		// Draw shadow as background?
		if (shadow && isHighlighted())
		{
			Graphics2D g2d = (Graphics2D) g;
			g2d.setColor(getShadowColor());				
			g2d.fillRoundRect((int) boundingRect.getX(), (int) boundingRect.getY(), 
							  (int) boundingRect.getWidth(), (int) boundingRect.getHeight(),
							  20, 20);
			g2d.setColor(getColor());
		}

		/*
		// Draw shadow as underline?
		if (shadow && isHighlighted())
		{
			Graphics2D g2d = (Graphics2D) g;
			g2d.setStroke(SHADOWSTROKE); 
			g2d.setColor(getShadowColor());				
			g2d.drawLine((int) boundingRect.getX(), (int) (boundingRect.getY()+boundingRect.getHeight()), 
						 (int) (boundingRect.getX()+boundingRect.getWidth()), (int) (boundingRect.getY()+boundingRect.getHeight()));			
			g2d.setColor(getColor());
			g2d.setStroke(BASICSTROKE);
		}
		*/
	}

	public boolean wasClicked(int ex, int ey)
	{
		if (boundingRect == null)
		{
			return false;
		}

		return boundingRect.contains(ex, ey);
	}

	public int getX()
	{
		return (int) text.getBounds().getCenterX();
	}

	public int getY()
	{
		return (int) text.getBounds().getCenterY();
	}

	public int getHeight()
	{
		return text.getHeight();
	}

	public int getWidth()
	{
		return text.getWidth();
	}

	public void setHighlighted(boolean s)
	{
		super.setHighlighted(s);
		mParent.setHighlighted(s);
	}

	public EditorLabel(EditorNode par, String t, EditorSurface e)
	{
		par.getSubject().addModelObserver(this);
		// This is a label
		type = LABEL;

		if (par.getName() == null)
		{
			text = new JTextField(t);
		}
		else
		{
			text = new JTextField(par.getName());
		}

		backup = text.getText();

		if (par.getSubject().getLabelGeometry() == null)
		{
			final Point2D point = new Point(DEFAULTOFFSETX, DEFAULTOFFSETY);
			mGeometry = new LabelGeometrySubject(point, DEFAULTANCHOR);
			par.getSubject().setLabelGeometry(mGeometry);
		}
		else
		{
			mGeometry = par.getSubject().getLabelGeometry();
		}

		label = new JLabel(text.getText());
		
		text.setOpaque(false);
		label.setOpaque(false);
		text.setBorder(new EmptyBorder(text.getBorder().getBorderInsets(text)));
		label.setBorder(new EmptyBorder(text.getBorder().getBorderInsets(text)));
		text.setVisible(false);
		text.setForeground(EditorColor.DEFAULTCOLOR);
		e.add(text);
		e.add(label);

		/*
		labelShadow = new JLabel(text.getText());
		labelShadow.setOpaque(false);
		labelShadow.setBorder(new EmptyBorder(text.getBorder().getBorderInsets(text)));
		e.add(labelShadow);
		*/

		mParent = par;

		text.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "enter");
		text.getActionMap().put("enter", new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				setEditing(false);
			}
		});
		text.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "escape");
		text.getActionMap().put("escape", new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				text.setText(backup);
				setEditing(false);
			}
		});
	}


	//#######################################################################
	//# Overrides for Abstract Base Class EditorObject
	public Subject getSubject()
	{
		throw new UnsupportedOperationException
			("Class EditorLabel has no associated Subject!");
	}


	//########################################################################
	//# Data Members
	private JTextField text;
	private JLabel label;
	private String backup = "";
	private Rectangle boundingRect = new Rectangle();
	private boolean editing = false;
	private EditorNode mParent = null;
	private LabelGeometrySubject mGeometry;

    static final int DEFAULTOFFSETX = 5;
    static final int DEFAULTOFFSETY = 20;
    static final AnchorPosition DEFAULTANCHOR = AnchorPosition.NW;

}
