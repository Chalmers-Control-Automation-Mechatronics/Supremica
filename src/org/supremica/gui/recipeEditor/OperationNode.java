
/*
 *  Copyright © Northwoods Software Corporation, 2000-2002. All Rights
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
import java.awt.*;
import java.net.URL;

//import com.nwoods.jgo.examples.*;
// Activities are implemented as MultiPortNodes.
//
// Properties include Text, OperationType, and ID.
// The Text property is actually just the MultiPortNode's Label's Text.
//
// This class also supports a standard set of icons (as JGoImages)
// and the notion of a standard size for the node.
public class OperationNode
	extends MultiPortNode
{
	public OperationNode() {}

	public JGoObject copyObject(JGoCopyEnvironment env)
	{
		OperationNode newobj = (OperationNode) super.copyObject(env);

		if (newobj != null)
		{

			// the ID should *not* be copied blindly--leave as -1 so
			// that we can fix it up later in ownerChange
			// the JGoObjects that are part of this area are copied
			// separately by copyChildren()
			newobj.myOperationType = myOperationType;

			// no other fields to copy--the Text is actually on a sub-object
		}

		return newobj;
	}

	public void initialize(int acttype, int id)
	{
		super.initialize(myStdPoint, getImage(acttype), getLabelString(acttype));

		myID = id;
		myOperationType = acttype;

		if (getLabel() != null)
		{
			getLabel().setBold(true);
			getLabel().setTextColor(new Color(0, 127, 0));
		}

		resetEditability();
	}

	public void addScatteredPorts(int numports)
	{
		if (getOperationType() == Start)
		{
			OperationPort p = new OperationPort(false, true, JGoObject.SideRight, new Point(40, 25), new Dimension(8, 8), getIcon(), this, 0);

			p.setMaxLinks(999999);
		}
		else if (getOperationType() == Finish)
		{
			OperationPort p = new OperationPort(true, false, JGoObject.SideLeft, new Point(10, 25), new Dimension(8, 8), getIcon(), this, 0);

			p.setMaxLinks(999999);
		}
		else
		{
			for (int i = 0; i < numports; i++)
			{
				Point offset = new Point(8 * ((int) (Math.random() * 6)), 8 * ((int) (Math.random() * 6)));
				int spot = JGoObject.Center;

				if (offset.x > offset.y)
				{
					if (offset.x < 40 - offset.y)
					{
						spot = JGoObject.TopMiddle;
					}
					else
					{
						spot = JGoObject.SideRight;
					}
				}
				else
				{
					if (offset.x > 40 - offset.y)
					{
						spot = JGoObject.BottomMiddle;
					}
					else
					{
						spot = JGoObject.SideLeft;
					}
				}

				OperationPort p = new OperationPort(true, true, spot, offset, new Dimension(8, 8), getIcon(), this, i);

				p.setMaxLinks(1);
			}
		}
	}

	public RecipeDocument getDoc()
	{
		return (RecipeDocument) getDocument();
	}

	// make sure that whenever an OperationNode is added to a RecipeDocument,
	// it has a unique ID
	protected void ownerChange(JGoObjectCollection oldOwner, JGoObjectCollection newOwner, JGoObject mainObject)
	{
		if ((oldOwner == null) && (newOwner != null) && (newOwner instanceof RecipeDocument))
		{
			if (getID() == -1)
			{
				RecipeDocument doc = (RecipeDocument) newOwner;

				myID = doc.getNextNodeID();
			}
		}
	}

	// Start and Finish nodes do not have editable labels
	public void resetEditability()
	{
		if (getLabel() != null)
		{
			getLabel().setEditable((getOperationType() != Start) && (getOperationType() != Finish));
		}
	}

	// You'll probably want to replace this with somewhat more interesting information
	public String getToolTipText()
	{
		return "a tooltip";
	}

	// Events
	public boolean doMouseDblClick(int modifiers, Point dc, Point vc, JGoView view)
	{
		if (view instanceof RecipeView)
		{
			RecipeView processView = (RecipeView) view;

			processView.editOperation(this);

			return true;
		}

		return false;
	}

	// Properties
	public int getID()
	{
		return myID;
	}

	public int getOperationType()
	{
		return myOperationType;
	}

	public String getText()
	{
		if (getLabel() != null)
		{
			return getLabel().getText();
		}
		else
		{
			return "";
		}
	}

	public void setText(String s)
	{
		if (getLabel() != null)
		{
			getLabel().setText(s);
		}
	}

	// find a port with a given ID
	public OperationPort getPortByID(int id)
	{
		JGoListPosition pos = getFirstObjectPos();

		while (pos != null)
		{
			JGoObject obj = getObjectAtPos(pos);

			pos = getNextObjectPos(pos);

			if (obj instanceof OperationPort)
			{
				OperationPort p = (OperationPort) obj;

				if (p.getID() == id)
				{
					return p;
				}
			}
		}

		return null;
	}

	// Static methods
	public static JGoImage getImage(int acttype)
	{
		JGoImage image = null;

		switch (acttype)
		{

		case Start :
			image = new JGoImage(myStdPoint, getStdSize());

			image.loadImage(OperationNode.class.getResource("images/star.gif"), true);
			break;

		case Finish :
			image = new JGoImage(myStdPoint, getStdSize());

			image.loadImage(OperationNode.class.getResource("images/star.gif"), true);
			break;

		default :
		case Operation :
			image = new JGoImage(myStdPoint, getStdSize());

			image.loadImage(OperationNode.class.getResource("images/doc.gif"), true);
			break;
		}

		return image;
	}

	public static String getLabelString(int acttype)
	{
		switch (acttype)
		{

		case Start :
			return "Start";

		case Finish :
			return "Finish";

		default :
		case Operation :
			return "Operation" + Integer.toString(acttype - Operation + 1);
		}
	}

	public static Dimension getStdSize()
	{
		return myStdSize;
	}

	public static void setStdSize(int w, int h)
	{
		myStdSize.width = w;
		myStdSize.height = h;
	}

	// Icon/Operation types
	public static final int Start = 0;
	public static final int Finish = 1;
	public static final int Operation = 2;
	private static Dimension myStdSize = new Dimension(50, 50);
	private static Point myStdPoint = new Point();    // don't care

	// State
	private int myID = -1;
	private int myOperationType = 0;
}
