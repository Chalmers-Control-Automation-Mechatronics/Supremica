
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
 * MultiPortNodePort implements the kind of port used in MultiPortNode.
 * <p>
 * MultiPortNodePort has some smarts about the kinds of links
 * that can be made to this kind of port.
 * <p>
 * You can easily change the appearance of the port by calling
 * setPortStyle(JGoPort.StyleObject) and then setPortObject to
 * change the object used to represent the port.
 * For example you may want to use an instance of JGoImage.
 * These objects can be shared by more than one JGoPort (an
 * optimization introduced with version 2.1).
 */
public class MultiPortNodePort
	extends JGoPort
{

	/** Call initialize() before using. */
	public MultiPortNodePort()
	{
		super();
	}

	/**
	 * This creates a light gray ellipse port of the appropriate direction
	 * at the given offset relative to the icon.
	 */
	public MultiPortNodePort(boolean input, boolean output, int linkspot, Point offset, Dimension size, JGoObject icon, JGoArea parent)
	{
		super(new Point(icon.getLeft() + offset.x, icon.getTop() + offset.y), size);

		initialize(input, output, linkspot, offset, icon, parent);
	}

	public void initialize(boolean input, boolean output, int linkspot, Point offset, JGoObject icon, JGoArea parent)
	{
		setDraggable(false);

		// assume an outlined light gray ellipse
		setStyle(StyleEllipse);
		setPen(JGoPen.darkGray);
		setBrush(null);
		setValidDestination(input);
		setValidSource(output);
		setFromSpot(linkspot);
		setToSpot(linkspot);

		// put in the MultiPortNode area
		setTopLeft(icon.getLeft() + offset.x, icon.getTop() + offset.y);
		parent.addObjectAtTail(this);
	}

	public JGoObject copyObject(JGoCopyEnvironment env)
	{
		MultiPortNodePort p = (MultiPortNodePort) super.copyObject(env);

		if (p != null)
		{
			p.myMaxLinks = myMaxLinks;
		}

		return p;
	}

	/**
	 * A convenience method for returning the parent as a MultiPortNode.
	 */
	public MultiPortNode getNode()
	{
		return (MultiPortNode) getParent();
	}

	/** Return the maximum number of links that this port should have. */
	public int getMaxLinks()
	{
		return myMaxLinks;
	}

	/** Change the maximum number of links that this port should have. */
	public void setMaxLinks(int n)
	{
		int old = myMaxLinks;

		if ((old != n) && (n >= 0))
		{
			myMaxLinks = n;

			update(ChangedMaxLinks, old, null);
		}
	}

	/**
	 * Return the number of real links that are connected
	 * to this port.  This is more meaningful than the getNumLinks method
	 * because it ignores any temporary or invisible links.
	 */
	public int getNumValidLinks()
	{
		int num = 0;
		JGoListPosition pos = getFirstLinkPos();

		while (pos != null)
		{
			JGoLink link = getLinkAtPos(pos);

			pos = getNextLinkPos(pos);

			if (link.isVisible() && (link.getDocument() != null) && (link.getFromPort().getDocument() != null) && (link.getToPort().getDocument() != null))
			{
				num++;
			}
		}

		return num;
	}

	/**
	 * This override ensures users cannot make more than getMaxLinks() links
	 * at this port.  This method uses the getNumValidLinks() method to
	 * count how many real links there are at this port.
	 * <p>
	 * If getMaxLinks() returns zero, no linking is permitted from this port--
	 * JGoView.startNewLink() will return false.
	 */
	public boolean isValidSource()
	{
		return (getMaxLinks() > 0) && (getNumValidLinks() < getMaxLinks()) && super.isValidSource();
	}

	/**
	 * This override ensures users cannot make more than getMaxLinks() links
	 * at this port.  This method uses the getNumValidLinks() method to
	 * count how many real links there are at this port.
	 * <p>
	 * If getMaxLinks() returns zero, no linking is permitted from this port--
	 * JGoView.startNewLink() will return false.
	 */
	public boolean isValidDestination()
	{
		return (getMaxLinks() > 0) && (getNumValidLinks() < getMaxLinks()) && super.isValidDestination();
	}

	/**
	 * This implements some additional policies on linking:
	 * <ul>
	 * <li> don't allow linking from this port to another port on the same node
	 * <li> make sure a source is always connected to a destination
	 * <li> if there are already getMaxLinks() on the either port, don't permit more
	 * <li> don't allow it if there's already a link from this port to the TO port
	 * </ul>
	 */
	public boolean validLink(JGoPort to)
	{
		return (super.validLink(to) && (getParent() != to.getParent()) &&!alreadyLinked(to));
	}

	/**
	 * Return true if there already is a link between this port and
	 * the given port.
	 */
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

	/**
	 * Change the appearance of the port according to how many links it has
	 * connected: zero, the maximum, or some number in-between.
	 * This method is called whenever a link has been added or removed from this port.
	 */
	public void linkChange()
	{
		int numlinks = getNumLinks();

		if (numlinks <= 0)
		{
			setBrush(EMPTY);
		}
		else if (numlinks < getMaxLinks())
		{
			setBrush(OK);
		}
		else
		{
			setBrush(FULL);
		}
	}

	public void copyNewValueForRedo(JGoDocumentChangedEdit e)
	{
		switch (e.getFlags())
		{

		case ChangedMaxLinks :
			e.setNewValueInt(getMaxLinks());

			return;

		default :
			super.copyNewValueForRedo(e);

			return;
		}
	}

	public void changeValue(JGoDocumentChangedEdit e, boolean undo)
	{
		switch (e.getFlags())
		{

		case ChangedMaxLinks :
			setMaxLinks(e.getValueInt(undo));

			return;

		default :
			super.changeValue(e, undo);

			return;
		}
	}

	public void SVGWriteObject(DomDoc svgDoc, DomElement jGoElementGroup)
	{

		// Add MultiPortNodePort element
		DomElement jMultiPortNodePort = svgDoc.createJGoClassElement("com.nwoods.jgo.examples.MultiPortNodePort", jGoElementGroup);

		jMultiPortNodePort.setAttribute("maxlinks", Integer.toString(myMaxLinks));

		// Have superclass add to the JGoObject group
		super.SVGWriteObject(svgDoc, jGoElementGroup);
	}

	public DomNode SVGReadObject(DomDoc svgDoc, JGoDocument jGoDoc, DomElement svgElement, DomElement jGoChildElement)
	{
		if (jGoChildElement != null)
		{

			// This is a MultiPortNodePort element
			myMaxLinks = Integer.parseInt(jGoChildElement.getAttribute("maxlinks"));

			super.SVGReadObject(svgDoc, jGoDoc, svgElement, jGoChildElement.getNextSiblingJGoClassElement());
		}

		return svgElement.getNextSibling();
	}

	public static JGoBrush EMPTY = JGoBrush.Null;
	public static JGoBrush OK = new JGoBrush(JGoBrush.SOLID, Color.green);
	public static JGoBrush FULL = new JGoBrush(JGoBrush.SOLID, Color.red);
	public static final int ChangedMaxLinks = JGoDocumentEvent.LAST + 10110;

	// State
	private int myMaxLinks = 3;
}
