
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.gui
//# CLASS:   EditorObject
//###########################################################################
//# $Id: EditorObject.java,v 1.3 2005-02-20 23:32:54 robi Exp $
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
	protected boolean selected = false;
	protected int type = 0;
	private int hash = 0;
	public static int EDGE = 1;
	public static int NODE = 2;
	public static int NODEGROUP = 3;
	public static int LABEL = 4;
	public static int LABELGROUP = 5;

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

	public void setSelected()
	{
		selected = true;
	}

	public boolean getSelected()
	{
		return selected;
	}

	public EditorObject()
	{
		selected = false;
	}
}
