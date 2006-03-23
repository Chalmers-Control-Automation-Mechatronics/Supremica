//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   EditorLabel
//###########################################################################
//# $Id: EditorLabel.java,v 1.28 2006-03-23 12:07:14 flordal Exp $
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
import java.awt.geom.Rectangle2D;
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
	
	public void modelChanged(ModelChangeEvent event)
	{
		if (event.getKind() == ModelChangeEvent.NAME_CHANGED) {
			final String name = getParent().getName();
			label.setText(name);
		}
	}

	public void removeFromSurface(EditorSurface e)
	{
		/*
		e.remove(labelShadow);
		*/
		e.remove(label);
	}

	public void setEditing(boolean edit)
	{
		editing = edit;

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

	public void drawObject(Graphics g, boolean selected)
	{
        if (mParent == null)
		{
		    return;
		}

		label.setForeground(getColor(selected));

		int xposition = mParent.getX() + (int) mGeometry.getOffset().getX();
		int yposition = mParent.getY() + (int) mGeometry.getOffset().getY();

		label.setLocation(xposition, yposition);
		label.setSize(label.getPreferredSize());

		/*
		labelShadow.setLocation(label.getX()+2, label.getY()+2);
		labelShadow.setText(text.getText());
		labelShadow.setSize(labelShadow.getPreferredSize());
		*/
		boundingRect.setRect(xposition, yposition, label.getWidth(), label.getHeight());

		// Draw shadow as background?
		if (shadow && isHighlighted())
		{
			Graphics2D g2d = (Graphics2D) g;
			g2d.setColor(getShadowColor(selected));				
			g2d.fillRoundRect((int) boundingRect.getX()-EditorSurface.TEXTSHADOWMARGIN, 
							  (int) boundingRect.getY()-EditorSurface.TEXTSHADOWMARGIN, 
							  (int) boundingRect.getWidth()+2*EditorSurface.TEXTSHADOWMARGIN, 
							  (int) boundingRect.getHeight()+2*EditorSurface.TEXTSHADOWMARGIN,
							  20, 20);
			g2d.setColor(getColor(selected));
		}

		/*
		// Draw shadow as underline?
		if (shadow && isHighlighted())
		{
			Graphics2D g2d = (Graphics2D) g;
			g2d.setStroke(SHADOWSTROKE); 
			g2d.setColor(getShadowColor(selected));				
			g2d.drawLine((int) boundingRect.getX(), (int) (boundingRect.getY()+boundingRect.getHeight()), 
						 (int) (boundingRect.getX()+boundingRect.getWidth()), (int) (boundingRect.getY()+boundingRect.getHeight()));			
			g2d.setColor(getColor(selected));
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
		return (int) mParent.getX() + getOffsetX();
	}

	public int getY()
	{
		return (int) mParent.getY() + getOffsetY();
	}

	public int getHeight()
	{
		return label.getHeight();
	}

	public int getWidth()
	{
		return label.getWidth();
	}
	
	public Rectangle2D getBounds()
	{
		return label.getBounds();
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
			label = new JLabel(t);
		}
		else
		{
			label = new JLabel(par.getName());
		}
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
		label.setOpaque(false);
		//label.setBorder(new EmptyBorder(label.getBorder().getBorderInsets(label)));
		e.add(label);
		/*
		labelShadow = new JLabel(text.getText());
		labelShadow.setOpaque(false);
		labelShadow.setBorder(new EmptyBorder(text.getBorder().getBorderInsets(text)));
		e.add(labelShadow);
		*/

		mParent = par;
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
	private JLabel label;
	private Rectangle boundingRect = new Rectangle();
	private boolean editing = false;
	private EditorNode mParent = null;
	private LabelGeometrySubject mGeometry;

    static final int DEFAULTOFFSETX = 5;
    static final int DEFAULTOFFSETY = 20;
    static final AnchorPosition DEFAULTANCHOR = AnchorPosition.NW;

}
