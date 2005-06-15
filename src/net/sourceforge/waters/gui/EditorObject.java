
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.gui
//# CLASS:   EditorObject
//###########################################################################
//# $Id: EditorObject.java,v 1.12 2005-06-15 09:19:14 flordal Exp $
//###########################################################################
package net.sourceforge.waters.gui;

import java.awt.*;

/**
 * <p>The super-class for all objects internally stored within an
 * EditorSurface.</p>
 *
 * @author Gian Perrone
 */
public class EditorObject
{
	protected boolean visible;
	protected int type = 0;
	private int hash = 0;
	public static int EDGE = 1;
	public static int NODE = 2;
	public static int NODEGROUP = 3;
	public static int LABEL = 4;
	public static int LABELGROUP = 5;

	// What status has this object got in the editor window? Determines color.
	private boolean selected = false;
	private boolean highlighted = false;
	private boolean error = false;

	// Should we draw a shadow on highlighted objects?
	protected boolean shadow = true;

	// Different pens for drawing
	public final Stroke BASICSTROKE = new BasicStroke();
	public final Stroke DOUBLESTROKE = new BasicStroke(2); 	
	public final Stroke SHADOWSTROKE = new BasicStroke(10, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND); 	

	public void drawObject(Graphics g)
	{
		;
	}

	/* Replaced by hashCode()-methods in extending  classes.
	public void setHash(int hash)
	{
		this.hash = hash;
	}

	public int getHash()
	{
		return hash;
	}
	*/

	public int getX()
	{
		return 0;
	}
	public int getY()
	{
		return 0;
	}

	public int getType()
	{
		return type;
	}

	public void setSelected(boolean s)
	{
		selected = s;
	}

	public boolean isSelected()
	{
		return selected;
	}

	public void setHighlighted(boolean s)
	{
		highlighted = s;
	}

	public boolean isHighlighted()
	{
		return highlighted;
	}

	public void setError(boolean s)
	{
		error = s;
	}

	public boolean isError()
	{
		return error;
	}

	public Color getColor()
	{
		// In order of importance
		if (isError())
		{
			if (getType() == NODE)
			{
				return EditorColor.ERRORCOLOR_NODE;
			}
			return EditorColor.ERRORCOLOR;
		}
		else if (isSelected())
		{
			return EditorColor.SELECTCOLOR;
		}
		else if (isHighlighted() && !shadow)
		{
			return EditorColor.HIGHLIGHTCOLOR;
		}
		else
		{
			// Defaults
			if (getType() == NODEGROUP)
			{
				return EditorColor.DEFAULTCOLOR_NODEGROUP;
			}
			else if (getType() == LABEL)
			{
				return EditorColor.DEFAULTCOLOR_LABEL;
			}
			return EditorColor.DEFAULTCOLOR;
		}
	}		

	public Color getShadowColor()
	{
		// Overrides
		if (!isSelected() && !isError() && getType() == NODEGROUP)
		{
			// Unfortunately, the light gray color gives a too weak shadow!
			return EditorColor.shadow(EditorColor.DEFAULTCOLOR_NODEGROUP.darker().darker());
		}

		// Return the shadowed variant of the ordinary color of this object
		return EditorColor.shadow(getColor());
	}

	public EditorObject()
	{
		selected = false;
	}
}
