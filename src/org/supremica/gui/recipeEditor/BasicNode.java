
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
 * A BasicNode is an Area containing an Ellipse or Rectangle and
 * one Port and an optional Text label.
 */
public class BasicNode
	extends JGoArea
{

	/**
	 * A newly constructed BasicNode is not usable until you've
	 * called initialize().
	 */
	public BasicNode()
	{
		super();
	}

	public JGoDrawable createDrawable()
	{
		JGoDrawable d;

		if (isRectangular())
		{
			d = new JGoRectangle();
		}
		else
		{
			d = new JGoEllipse();
		}

		d.setSelectable(false);
		d.setDraggable(false);
		d.setSize(20, 20);

		return d;
	}

	public JGoText createLabel(String s)
	{
		JGoText l = new JGoText(s);

		l.setSelectable(false);
		l.setDraggable(false);
		l.setEditOnSingleClick(true);
		l.setAlignment(getLabelAlignment());
		l.setTransparent(true);

		return l;
	}

	public void copyChildren(JGoArea newarea, JGoCopyEnvironment env)
	{

		// don't bother calling JGoArea's default implementation,
		// so that we can set our fields explicitly, since all of the
		// children are stored in fields
		BasicNode newobj = (BasicNode) newarea;

		newobj.myRectangular = myRectangular;
		newobj.myLabelSpot = myLabelSpot;

		if (myDrawable != null)
		{
			newobj.myDrawable = (JGoDrawable) env.copy(myDrawable);

			newobj.addObjectAtHead(newobj.myDrawable);
		}

		if (myPort != null)
		{
			newobj.myPort = (BasicNodePort) env.copy(myPort);

			newobj.addObjectAtTail(newobj.myPort);
		}

		if (myLabel != null)
		{
			newobj.myLabel = (JGoText) env.copy(myLabel);

			newobj.addObjectAtTail(newobj.myLabel);
		}
	}

	/**
	 * When an object is removed, make sure there are no more references from fields.
	 */
	public JGoObject removeObjectAtPos(JGoListPosition pos)
	{
		JGoObject child = super.removeObjectAtPos(pos);

		if (child == myDrawable)
		{
			myDrawable = null;
		}
		else if (child == myLabel)
		{
			myLabel = null;
		}
		else if (child == myPort)
		{
			myPort = null;
		}

		return child;
	}

	public void initialize(Point loc, String labeltext, boolean rect)
	{
		myRectangular = rect;

		initialize(loc, labeltext);
	}

	/**
	 * Specify the location of the node; the size is constant.
	 * If labeltext is null, do not create a label.
	 */
	public void initialize(Point loc, String labeltext)
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

		// create the circle/ellipse around and behind the port
		myDrawable = createDrawable();

		// can't setLocation until myDrawable exists
		setLocation(loc);

		// if there is a string, create a label with a transparent
		// background that is centered
		if (labeltext != null)
		{
			myLabel = createLabel(labeltext);
		}

		// create a Port, which knows how to make sure
		// connected JGoLinks have a reasonable end point
		myPort = new BasicNodePort();

		myPort.setSize(7, 7);

		if (getLabelSpot() == Center)
		{
			getPort().setStyle(JGoPort.StyleHidden);
		}
		else
		{
			getPort().setStyle(JGoPort.StyleEllipse);
		}

		// add all the children to the area
		addObjectAtHead(myDrawable);
		addObjectAtTail(myPort);

		if (myLabel != null)
		{
			addObjectAtTail(myLabel);
		}
	}

	// this area's natural "location" is the center of the ellipse
	public Point getLocation(Point p)
	{
		if (getDrawable() != null)
		{
			return getDrawable().getSpotLocation(Center, p);
		}
		else
		{
			return getSpotLocation(Center, p);
		}
	}

	public void setLocation(int x, int y)
	{
		if (getDrawable() != null)
		{
			getDrawable().setSpotLocation(Center, x, y);
			layoutChildren();
		}
		else
		{
			setSpotLocation(Center, x, y);
		}
	}

	/**
	 * Keep the parts of a BasicNode positioned relative to each other
	 * by setting their locations using some of the standard spots of
	 * a JGoObject.
	 * <p>
	 * By default the label will be positioned at the top of the node,
	 * above the drawable object.  To change this to be below the object, at
	 * the bottom of the node, call setLabelSpot with a new spot relative
	 * to the object.
	 */
	public void layoutChildren()
	{
		JGoDrawable obj = getDrawable();

		if (obj == null)
		{
			return;
		}

		JGoText lab = getLabel();

		if (lab != null)
		{
			int spot = getLabelSpot();

			if (spot == Center)
			{
				int cx = obj.getLeft() + obj.getWidth() / 2;
				int cy = obj.getTop() + obj.getHeight() / 2;
				int w = lab.getWidth();
				int h = lab.getHeight();

				lab.setTopLeft(cx - w / 2, cy - h / 2);

				if (obj instanceof JGoEllipse)
				{
					w += 20;
					h += 10;
				}
				else
				{
					w += 10;
					h += 5;
				}

				JGoPen pen = obj.getPen();

				if (pen != null)
				{
					w += pen.getWidth();
					h += pen.getWidth();
				}

				obj.setBoundingRect(cx - w / 2, cy - h / 2, w, h);

				if (getPort() != null)
				{
					getPort().setBoundingRect(obj.getBoundingRect());
				}
			}
			else
			{
				lab.setSpotLocation(spotOpposite(spot), obj, spot);
			}
		}

		if (getPort() != null)
		{
			getPort().setSpotLocation(Center, obj, Center);
		}
	}

	/**
	 * If this object is resized, do the part positioning lay out again,
	 * but ignore the label (i.e., the text will not be resized).
	 */
	public void geometryChange(Rectangle prevRect)
	{

		// see if this is just a move and not a scale
		if ((prevRect.width == getWidth()) && (prevRect.height == getHeight()))
		{

			// let the default JGoArea implementation do the work
			super.geometryChange(prevRect);
		}
		else
		{

			// Otherwise, we have to handle the general case of scaling:
			double scaleFactorX = 1;

			if (prevRect.width != 0)
			{
				scaleFactorX = ((double) getWidth()) / ((double) prevRect.width);
			}

			double scaleFactorY = 1;

			if (prevRect.height != 0)
			{
				scaleFactorY = ((double) getHeight()) / ((double) prevRect.height);
			}

			JGoDrawable obj = getDrawable();

			if (obj != null)
			{
				int newRectx = getLeft() + (int) Math.rint((obj.getLeft() - prevRect.x) * scaleFactorX);
				int newRecty = getTop() + (int) Math.rint((obj.getTop() - prevRect.y) * scaleFactorY);
				int newRectwidth = (int) Math.rint(obj.getWidth() * scaleFactorX);
				int newRectheight = (int) Math.rint(obj.getHeight() * scaleFactorY);

				obj.setBoundingRect(newRectx, newRecty, newRectwidth, newRectheight);
			}

			JGoPort prt = getPort();

			if (prt != null)
			{
				int newRectx = getLeft() + (int) Math.rint((prt.getLeft() - prevRect.x) * scaleFactorX);
				int newRecty = getTop() + (int) Math.rint((prt.getTop() - prevRect.y) * scaleFactorY);
				int newRectwidth = (int) Math.rint(prt.getWidth() * scaleFactorX);
				int newRectheight = (int) Math.rint(prt.getHeight() * scaleFactorY);

				prt.setBoundingRect(newRectx, newRecty, newRectwidth, newRectheight);
			}

			// don't scale the label, but position it appropriately
			JGoText lab = getLabel();

			if (lab != null)
			{
				int spot = getLabelSpot();

				lab.setSpotLocation(spotOpposite(spot), obj, spot);
			}
		}
	}

	/**
	 * If the text label changed position/size on its own (i.e., not because
	 * the area was moved or resized), then make sure the rectangle
	 * is just bigger than the text and that the ports are positioned
	 * correctly relative to the rectangle.
	 */
	protected boolean geometryChangeChild(JGoObject child, Rectangle prevRect)
	{
		if (super.geometryChangeChild(child, prevRect))
		{
			if (child == getLabel())
			{
				layoutChildren();
			}

			return true;
		}

		return false;
	}

	/**
	 * Let single click on a label mean start editing that label.
	 * Because the label is not selectable, a mouse click will be passed
	 * on up to its parent, which will be this area.
	 */
	public boolean doMouseClick(int modifiers, Point dc, Point vc, JGoView view)
	{
		JGoText lab = getLabel();

		if ((lab != null) && lab.isEditable() && lab.isEditOnSingleClick())
		{
			JGoObject obj = view.pickDocObject(dc, false);

			if ((obj == lab) && (obj.getLayer() != null) && obj.getLayer().isModifiable())
			{
				lab.doStartEdit(view, vc);

				return true;
			}
		}

		return false;
	}

	public void SVGUpdateReference(String attr, Object referencedObject)
	{
		super.SVGUpdateReference(attr, referencedObject);

		if (attr.equals("drawable"))
		{
			myDrawable = (JGoDrawable) referencedObject;
		}
		else if (attr.equals("label"))
		{
			myLabel = (JGoText) referencedObject;
		}
		else if (attr.equals("port"))
		{
			myPort = (BasicNodePort) referencedObject;
		}
	}

	public void SVGWriteObject(DomDoc svgDoc, DomElement jGoElementGroup)
	{

		// Add BasicNode element
		DomElement jBasicNode = svgDoc.createJGoClassElement("com.nwoods.jgo.examples.BasicNode", jGoElementGroup);

		jBasicNode.setAttribute("rectangular", myRectangular
											   ? "true"
											   : "false");
		jBasicNode.setAttribute("labelspot", Integer.toString(myLabelSpot));

		// The following elements are all children of this area and so will be writen out
		// by JGoArea.SVGWriteObject().  We just need to update the references to them.
		if (myDrawable != null)
		{
			svgDoc.registerReferencingNode(jBasicNode, "drawable", myDrawable);
		}

		if (myLabel != null)
		{
			svgDoc.registerReferencingNode(jBasicNode, "label", myLabel);
		}

		if (myPort != null)
		{
			svgDoc.registerReferencingNode(jBasicNode, "port", myPort);
		}

		// Have superclass add to the JGoObject group
		super.SVGWriteObject(svgDoc, jGoElementGroup);
	}

	public DomNode SVGReadObject(DomDoc svgDoc, JGoDocument jGoDoc, DomElement svgElement, DomElement jGoChildElement)
	{
		if (jGoChildElement != null)
		{

			// This is a BasicNode element
			myRectangular = jGoChildElement.getAttribute("rectangular").equals("true");
			myLabelSpot = Integer.parseInt(jGoChildElement.getAttribute("labelspot"));

			String drawable = jGoChildElement.getAttribute("drawable");

			svgDoc.registerReferencingObject(this, "drawable", drawable);

			String label = jGoChildElement.getAttribute("label");

			svgDoc.registerReferencingObject(this, "label", label);

			String port = jGoChildElement.getAttribute("port");

			svgDoc.registerReferencingObject(this, "port", port);
			super.SVGReadObject(svgDoc, jGoDoc, svgElement, jGoChildElement.getNextSiblingJGoClassElement());
		}

		return svgElement.getNextSibling();
	}

	// get access to the parts of the node
	public JGoDrawable getDrawable()
	{
		return myDrawable;
	}

	public JGoText getLabel()
	{
		return myLabel;
	}

	public BasicNodePort getPort()
	{
		return myPort;
	}

	// determine where the label is placed relative to the ellipse/rectangle
	public int getLabelSpot()
	{
		return myLabelSpot;
	}

	public void setLabelSpot(int s)
	{
		int old = myLabelSpot;

		if (old != s)
		{
			myLabelSpot = s;

			update(ChangedLabelSpot, old, null);
			portStyleChanged();

			if (getLabel() != null)
			{
				getLabel().setAlignment(getLabelAlignment());
			}

			layoutChildren();
		}
	}

	public void portStyleChanged()
	{
		JGoPort p = getPort();

		if (p != null)
		{
			if (getLabelSpot() == Center)
			{
				p.setStyle(JGoPort.StyleHidden);
				setResizable(false);
			}
			else
			{

				// make the drawable and the port small again, but maintaining the center location
				p.setStyle(JGoPort.StyleEllipse);

				JGoDrawable obj = getDrawable();
				int cx = obj.getLeft() + obj.getWidth() / 2;
				int cy = obj.getTop() + obj.getHeight() / 2;
				Rectangle prect = new Rectangle(cx - 3, cy - 3, 7, 7);

				obj.setBoundingRect(cx - prect.width / 2 - 7, cy - prect.height / 2 - 7, prect.width + 2 * 7, prect.height + 2 * 7);
				p.setBoundingRect(prect);
			}
		}
	}

	public int getLabelAlignment()
	{
		switch (getLabelSpot())
		{

		case TopRight :
		case RightCenter :
		case BottomRight :
			return JGoText.ALIGN_LEFT;

		case TopLeft :
		case LeftCenter :
		case BottomLeft :
			return JGoText.ALIGN_RIGHT;

		default :
			return JGoText.ALIGN_CENTER;
		}
	}

	// Convenience methods: control the drawable's pen and brush, and the label's text string
	public boolean isRectangular()
	{
		return myRectangular;
	}

	public JGoPen getPen()
	{
		return getDrawable().getPen();
	}

	public void setPen(JGoPen p)
	{
		getDrawable().setPen(p);
	}

	public JGoBrush getBrush()
	{
		return getDrawable().getBrush();
	}

	public void setBrush(JGoBrush b)
	{
		getDrawable().setBrush(b);
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
		if (s == null) {}
		else if (getLabel() == null)
		{
			myLabel = createLabel(s);

			addObjectAtTail(myLabel);
			layoutChildren();
		}
		else
		{
			getLabel().setText(s);
		}
	}

	public void copyNewValueForRedo(JGoDocumentChangedEdit e)
	{
		switch (e.getFlags())
		{

		case ChangedLabelSpot :
			e.setNewValueInt(getLabelSpot());

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

		case ChangedLabelSpot :
			setLabelSpot(e.getValueInt(undo));

			return;

		default :
			super.changeValue(e, undo);

			return;
		}
	}

	public static final int ChangedLabelSpot = JGoDocumentEvent.LAST + 2101;

	// State
	protected boolean myRectangular = false;
	protected JGoDrawable myDrawable = null;
	protected JGoText myLabel = null;
	protected BasicNodePort myPort = null;
	protected int myLabelSpot = TopCenter;

	// A real application will have some other data associated with
	// the node, holding state and methods to be called according to
	// the needs of the application.
}
