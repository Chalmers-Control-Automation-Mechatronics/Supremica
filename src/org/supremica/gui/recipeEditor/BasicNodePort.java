
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

/**
 * To get the link arrowheads' point at the edge of the ellipse or rectangle,
 * we need to override how the link point is computed, rather
 * than depending on the built-in mechanism specifying a spot.
 */
public class BasicNodePort
	extends JGoPort
{
	public BasicNodePort()
	{
		super();

		setSelectable(false);
		setDraggable(false);
		setStyle(StyleEllipse);    // black circle/ellipse by default

		// use custom link spots for both links coming in and going out
		setFromSpot(JGoObject.NoSpot);
		setToSpot(JGoObject.NoSpot);
	}

	/**
	 * Return a point on the edge of or inside the ellipse or rectangle, rather
	 * than a point on the port itself.
	 */
	public Point getLinkPointFromPoint(int x, int y, Point p)
	{
		BasicNode node = getNode();
		JGoDrawable obj = node.getDrawable();
		Rectangle rect = obj.getBoundingRect();
		int a = rect.width / 2;
		int b = rect.height / 2;
		int cx = getLeft() + getWidth() / 2;
		int cy = getTop() + getHeight() / 2;

		if (p == null)
		{
			p = new Point();
		}

		p.x = x;
		p.y = y;

		// if (x,y) is inside the object, just return it instead of finding the edge intersection
		if (!obj.isPointInObj(p))
		{
			if (node.isRectangular())
			{
				JGoRectangle.getNearestIntersectionPoint(rect.x, rect.y, rect.width, rect.height, x, y, cx, cy, p);
			}
			else
			{
				JGoEllipse.getNearestIntersectionPoint(rect.x, rect.y, rect.width, rect.height, x, y, p);
			}
		}

		return p;
	}

	public boolean validLink(JGoPort to)
	{
		return super.validLink(to) &&!alreadyLinked(to);
	}

	// return true if there already is a link from this port to
	// the given port
	public boolean alreadyLinked(JGoPort dst)
	{
		JGoListPosition pos = getFirstLinkPos();

		while (pos != null)
		{
			JGoLink link = getLinkAtPos(pos);

			pos = getNextLinkPos(pos);

			if ((link.getFromPort() == this) && (link.getToPort() == dst))
			{
				return true;
			}
		}

		return false;
	}

	public void SVGWriteObject(DomDoc svgDoc, DomElement jGoElementGroup)
	{

		// Add BasicNodePort element
		DomElement jBasicNode = svgDoc.createJGoClassElement("com.nwoods.jgo.examples.BasicNodePort", jGoElementGroup);

		// Have superclass add to the JGoObject group
		super.SVGWriteObject(svgDoc, jGoElementGroup);
	}

	public DomNode SVGReadObject(DomDoc svgDoc, JGoDocument jGoDoc, DomElement svgElement, DomElement jGoChildElement)
	{
		if (jGoChildElement != null)
		{

			// This is a BasicNodePort element
			super.SVGReadObject(svgDoc, jGoDoc, svgElement, jGoChildElement.getNextSiblingJGoClassElement());
		}

		return svgElement.getNextSibling();
	}

	/**
	 * A convenience method for returning the parent as a BasicNode.
	 */
	public BasicNode getNode()
	{
		return (BasicNode) getParent();
	}
}
