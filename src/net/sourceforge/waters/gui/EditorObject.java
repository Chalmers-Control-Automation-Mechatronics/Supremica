
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.gui
//# CLASS:   EditorObject
//###########################################################################
//# $Id: EditorObject.java,v 1.6 2005-03-03 05:36:29 flordal Exp $
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

	public void drawObject(Graphics g)
	{
		;
	}

	public void setHash(int hash)
	{
		this.hash = hash;
	}

	public int getHash()
	{
		return hash;
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
		else if (isHighlighted())
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
			return EditorColor.DEFAULTCOLOR;
		}
	}		

	public EditorObject()
	{
		selected = false;
	}
}
