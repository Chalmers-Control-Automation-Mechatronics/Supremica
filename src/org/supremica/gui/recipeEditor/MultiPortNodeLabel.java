
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
import java.awt.*;

/**
 * MultiPortNodeLabel is just a JGoText that treats a single click as a command
 * to start editing the text of the label.
 */
public class MultiPortNodeLabel
	extends JGoText
{

	/** Create an empty label for a MultiPortNode.  Call initialize() before using. */
	public MultiPortNodeLabel()
	{
		super();
	}

	/** Create a label containing the given text for a MultiPortNode */
	public MultiPortNodeLabel(String text, JGoObject icon, JGoArea parent)
	{
		super(text);

		initialize(text, icon, parent);
	}

	public void initialize(String text, JGoObject icon, JGoArea parent)
	{
		setSelectable(true);
		setDragsNode(false);
		setEditOnSingleClick(true);
		setTransparent(true);
		setAlignment(JGoText.ALIGN_CENTER);
		setSpotLocation(TopCenter, icon, BottomCenter);
		parent.addObjectAtTail(this);
	}

	public void SVGWriteObject(DomDoc svgDoc, DomElement jGoElementGroup)
	{

		// Add MultiPortNodeLabel element
		DomElement MultiPortNodeLabel = svgDoc.createJGoClassElement("com.nwoods.jgo.examples.MultiPortNodeLabel", jGoElementGroup);

		// Have superclass add to the JGoObject group
		super.SVGWriteObject(svgDoc, jGoElementGroup);
	}

	public DomNode SVGReadObject(DomDoc svgDoc, JGoDocument jGoDoc, DomElement svgElement, DomElement jGoChildElement)
	{
		if (jGoChildElement != null)
		{

			// This is a MultiPortNodeLabel element
			super.SVGReadObject(svgDoc, jGoDoc, svgElement, jGoChildElement.getNextSiblingJGoClassElement());
		}

		return svgElement.getNextSibling();
	}

	// a convenience property
	public MultiPortNode getNode()
	{
		return (MultiPortNode) getParent();
	}
}
