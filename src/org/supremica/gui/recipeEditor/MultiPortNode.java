
/*
 *  Copyright © Northwoods Software Corporation, 1998-2002. All Rights
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

/**
 * A MultiPortNode has an icon, a label, and some number of ports.
 * It is not resizable.
 * The label is a MultiPortNodeLabel, initially positioned at the bottom center
 * of the icon.
 */
public class MultiPortNode
	extends JGoArea
{

	/** Create an empty MultiPortNode.  Call initialize() before using it. */
	public MultiPortNode()
	{
		super();
	}

	// The location is the top-left corner of the node;
	// the size is determined by the size of the icon and the other parts;
	// the labeltext may be null if no label is desired.
	public void initialize(Point loc, JGoObject icon, String labeltext)
	{

		// the area as a whole is not directly selectable using a mouse,
		// but the area can be selected by trying to select any of its
		// children, all of whom are currently !isSelectable().
		setSelectable(false);
		setGrabChildSelection(true);

		// the user can move this node around
		setDraggable(true);

		// the user cannot resize this node
		setResizable(false);

		myIcon = icon;

		icon.setTopLeft(loc);
		icon.setSelectable(false);
		addObjectAtHead(icon);

		// the label is a MultiPortNodeLabel, centered underneath the icon
		if (labeltext != null)
		{
			myLabel = new MultiPortNodeLabel(labeltext, icon, this);
		}

		setTopLeft(loc);
	}

	public void copyChildren(JGoArea newarea, JGoCopyEnvironment env)
	{
		MultiPortNode newobj = (MultiPortNode) newarea;

		if (myIcon != null)
		{
			newobj.myIcon = env.copy(myIcon);

			newobj.addObjectAtHead(newobj.myIcon);
		}

		if (myLabel != null)
		{
			newobj.myLabel = (JGoText) env.copy(myLabel);

			newobj.addObjectAtTail(newobj.myLabel);
		}

		super.copyChildren(newarea, env);
	}

	/**
	 * When an object is removed, make sure there are no more references from fields.
	 */
	public JGoObject removeObjectAtPos(JGoListPosition pos)
	{
		JGoObject child = super.removeObjectAtPos(pos);

		if (child == myLabel)
		{
			myLabel = null;
		}
		else if (child == myIcon)
		{
			myIcon = null;
		}

		return child;
	}

	public void SVGWriteObject(DomDoc svgDoc, DomElement jGoElementGroup)
	{

		// Add MultiPortNode element
		DomElement multiPortNode = svgDoc.createJGoClassElement("com.nwoods.jgo.examples.MultiPortNode", jGoElementGroup);

		// The following elements are all children is this area and so will be writen out
		// by JGoArea.SVGWriteObject().  We just need to update the references to them.
		if (myIcon != null)
		{
			svgDoc.registerReferencingNode(multiPortNode, "multiporticon", myIcon);
		}

		if (myLabel != null)
		{
			svgDoc.registerReferencingNode(multiPortNode, "multiportlabel", myLabel);
		}

		// Have superclass add to the JGoObject group
		super.SVGWriteObject(svgDoc, jGoElementGroup);
	}

	public DomNode SVGReadObject(DomDoc svgDoc, JGoDocument jGoDoc, DomElement svgElement, DomElement jGoChildElement)
	{
		if (jGoChildElement != null)
		{

			// This is a MultiPortNode element
			String multiporticon = jGoChildElement.getAttribute("multiporticon");

			svgDoc.registerReferencingObject(this, "multiporticon", multiporticon);

			String multiportlabel = jGoChildElement.getAttribute("multiportlabel");

			svgDoc.registerReferencingObject(this, "multiportlabel", multiportlabel);
			super.SVGReadObject(svgDoc, jGoDoc, svgElement, jGoChildElement.getNextSiblingElement());
		}

		return svgElement.getNextSibling();
	}

	public void SVGUpdateReference(String attr, Object referencedObject)
	{
		super.SVGUpdateReference(attr, referencedObject);

		if (attr.equals("multiporticon"))
		{
			myIcon = (JGoObject) referencedObject;
		}

		if (attr.equals("multiportlabel"))
		{
			myLabel = (JGoText) referencedObject;
		}
	}

	public JGoText getLabel()
	{
		return myLabel;
	}

	public JGoObject getIcon()
	{
		return myIcon;
	}

	// State
	protected JGoText myLabel = null;
	protected JGoObject myIcon = null;
}
