
/*
 *  Copyright © Northwoods Software Corporation, 1999-2002. All Rights
 *  Reserved.
 *
 *  Restricted Rights: Use, duplication, or disclosure by the U.S.
 *  Government is subject to restrictions as set forth in subparagraph
 *  (c) (1) (ii) of DFARS 252.227-7013, or in FAR 52.227-19, or in FAR
 *  52.227-14 Alt. III, as applicable.
 *
 */
package org.supremica.gui.recipeEditor;

import com.nwoods.jgo.*;

// import com.nwoods.jgo.examples.*;
import java.awt.*;

/**
 * OperationPort implements the kind of port used in OperationNode.
 */
public class OperationPort
	extends MultiPortNodePort
{

	/** Call initialize() before using. */
	public OperationPort() {}

	/**
	 * This creates a light gray ellipse port of the appropriate direction
	 * at the given offset relative to the icon.
	 */
	public OperationPort(boolean input, boolean output, int linkspot, Point offset, Dimension size, JGoObject icon, JGoArea parent, int id)
	{
		super(input, output, linkspot, offset, size, icon, parent);

		initialize(input, output, linkspot, offset, icon, parent, id);
	}

	public void initialize(boolean input, boolean output, int linkspot, Point offset, JGoObject icon, JGoArea parent, int id)
	{
		initialize(input, output, linkspot, offset, icon, parent);

		myID = id;
		myOrigSpot = linkspot;
	}

	public JGoObject copyObject(JGoCopyEnvironment env)
	{
		OperationPort p = (OperationPort) super.copyObject(env);

		if (p != null)
		{
			p.myID = myID;
			p.myOrigSpot = myOrigSpot;
		}

		return p;
	}

	public boolean doUncapturedMouseMove(int flags, Point dc, Point vc, JGoView view)
	{
		if ((isValidSource() || isValidDestination()) && (getLayer() != null) && getLayer().isModifiable())
		{
			view.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

			return true;
		}

		return false;
	}

	/**
	 * Change the appearance of the port according to how many links it has,
	 * and the direction that they are going (in or out).
	 * This method is called whenever a link has been added or removed from this port.
	 */
	public void linkChange()
	{
		int numlinks = getNumLinks();

		if (numlinks <= 0)
		{
			setStyle(JGoPort.StyleEllipse);
			setToSpot(myOrigSpot);
		}
		else
		{
			setStyle(JGoPort.StyleTriangle);

			JGoListPosition pos = getFirstLinkPos();

			while (pos != null)
			{
				JGoLink link = getLinkAtPos(pos);

				if (link.getFromPort() == this)
				{
					setToSpot(JGoObject.spotOpposite(myOrigSpot));
				}
				else
				{
					setToSpot(myOrigSpot);
				}

				break;
			}
		}
	}

	// Property
	public int getID()
	{
		return myID;
	}

	// State
	private int myID = -1;
	private int myOrigSpot = NoSpot;
}
