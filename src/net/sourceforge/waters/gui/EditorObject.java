
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.gui
//# CLASS:   EditorObject
//###########################################################################
//# $Id: EditorObject.java,v 1.19 2005-09-14 15:16:09 flordal Exp $
//###########################################################################
package net.sourceforge.waters.gui;

import java.awt.*;
import java.awt.geom.Point2D;

/**
 * <p>The super-class for all objects internally stored within an
 * EditorSurface.</p>
 *
 * @author Gian Perrone
 */
public abstract class EditorObject
{   
	protected boolean visible;
	protected int type = 0;
	private int hash = 0;
	public static int EDGE = 1;
	public static int NODE = 2;
	public static int NODEGROUP = 3;
	public static int LABEL = 4;
	public static int LABELGROUP = 5;

    /** is not being draggedOver*/
    public static int NOTDRAG = 0;
    /** is being draggedOver and can drop data*/
    public static int CANDROP = 1;
    /** is being draggedOver but can't drop data*/
    public static int CANTDROP = 2;

	// What status has this object got in the editor window? Determines color.
	private boolean selected = false;
	private boolean highlighted = false;
	private boolean error = false;
    private int mDragOver = NOTDRAG;

	// Should we draw a shadow on highlighted objects?
	protected boolean shadow = true;

	// Different pen sizes for drawing
	/** Single line width, used as default when painting on screen. */
	public static final Stroke SINGLESTROKE = new BasicStroke(); 	
	/** Double line width, used for nodegroup border. */
	public static final Stroke DOUBLESTROKE = new BasicStroke(2); 	
	/** Thick line used for drawing shadows. */
	public static final Stroke SHADOWSTROKE = new BasicStroke(10, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND);
	/** Used as the basic stroke when printing - "hairline" width. */
	public static final Stroke THINSTROKE = new BasicStroke(0.25f);		
	/** The default pen size. Is not {@code final} since it changes when printing. */
	public static Stroke BASICSTROKE = SINGLESTROKE;

	public EditorObject()
	{
		selected = false;
	}

	/**
	 * Returns the type of this object.
	 */
	public int getType()
	{
		return type;
	}

	/**
	 * Sets the selection status of this object.
	 */
	public void setSelected(boolean s)
	{
		selected = s;
	}
	/**
	 * @return {@code true} if selected, {@code false} otherwise.
	 */
	public boolean isSelected()
	{
		return selected;
	}

	/**
	 * Sets the highlight status of this object.
	 */
	public void setHighlighted(boolean s)
	{
		highlighted = s;
	}
	/**
	 * @return {@code true} if highlighted, {@code false} otherwise.
	 */
	public boolean isHighlighted()
	{
		return highlighted;
	}

	/**
	 * Sets the error status of this object.
	 */
	public void setError(boolean s)
	{
		error = s;
	}
	/**
	 * @return {@code true} if there is an error with this object, {@code false} otherwise.
	 */
	public boolean isError()
	{
		return error;
	}

	/**
	 * Sets the dragover status of this object.
	 */
    public void setDragOver(int d)
    {
		mDragOver = d;
    }
	/**
	 * Returns the dragover status of this object.
	 */
    public int getDragOver()
    {
		return mDragOver;
    }

	/**
	 * Returns the apropriate color for painting this object.
	 */
	public Color getColor()
	{
		// In order of importance
		if(isError())
		{
			if(getType() == NODE)
			{
				return EditorColor.ERRORCOLOR_NODE;
			}
			return EditorColor.ERRORCOLOR;
		}
		else if(getDragOver() == CANDROP)
		{
		    return EditorColor.CANDROP;
		}
		else if(getDragOver() == CANTDROP)
		{
		    return EditorColor.CANTDROP;
		}
		else if(isSelected())
		{
			return EditorColor.SELECTCOLOR;
		}
		else if(isHighlighted() && !shadow)
		{
			return EditorColor.HIGHLIGHTCOLOR;
		}
		else
		{
			// Defaults
			if(getType() == NODEGROUP)
			{
				return EditorColor.DEFAULTCOLOR_NODEGROUP;
			}
			else if(getType() == LABEL)
			{
				return EditorColor.DEFAULTCOLOR_LABEL;
			}
			return EditorColor.DEFAULTCOLOR;
		}
	}		

	/**
	 * Returns a lighter shade of the color of the object for drawing a "shadow".
	 */
	public Color getShadowColor()
	{
		// Overrides
		if(!isSelected() && !isError() && getType() == NODEGROUP)
		{
			// Unfortunately, the light gray color gives a too weak shadow!
			return EditorColor.shadow(getColor().darker().darker().darker());
		}

		// Return the shadowed variant of the ordinary color of this object
		return EditorColor.shadow(getColor());
	}

	/**
	 * Changes the default stroke.
	 */
	public static void setBasicStroke(Stroke stroke)
	{
		BASICSTROKE = stroke;
	}

	public void drawObject(Graphics g)
	{
		;
	}

	public abstract int getX();

	public abstract int getY();

    public abstract void setPosition(double x, double y);

    public abstract Point2D getPosition();
}
