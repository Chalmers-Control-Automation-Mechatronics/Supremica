
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
 * A Comment is an Area containing a Text and a 3DNoteRect.
 * There are no ports.
 * <p>
 * If the text is changed, the rectangle is automatically resized to
 * "hold" the text.
 * <p>
 * By default the text is not editable by the user, but it can be
 * made editable by calling setEditable(true).
 */
public class Comment
	extends JGoArea
{

	/** Create an empty Comment.  Call initialize(String) before using it. */
	public Comment()
	{
		super();
	}

	/** Create a Comment displaying the given String. */
	public Comment(String s)
	{
		super();

		initialize(s);
	}

	public void initialize(String s)
	{
		setResizable(false);
		setGrabChildSelection(true);

		myRect = new JGo3DNoteRect();

		myRect.setSelectable(false);
		myRect.setPen(JGoPen.lightGray);
		myRect.setBrush(JGoBrush.makeStockBrush(new Color(0xFF, 0xFF, 0xCC)));

		myLabel = new JGoText(s);

		myLabel.setMultiline(true);
		myLabel.setSelectable(false);
		myLabel.setResizable(false);
		myLabel.setDraggable(false);
		myLabel.setEditable(false);
		myLabel.setEditOnSingleClick(true);    // in case it becomes editable
		myLabel.setTransparent(true);
		addObjectAtHead(myRect);
		addObjectAtTail(myLabel);
	}

	public void copyChildren(JGoArea newarea, JGoCopyEnvironment env)
	{

		// don't bother calling JGoArea's default implementation,
		// so that we can set our fields explicitly
		Comment newobj = (Comment) newarea;

		if (myRect != null)
		{
			newobj.myRect = (JGo3DNoteRect) env.copy(myRect);

			newobj.addObjectAtHead(newobj.myRect);
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

		if (child == myRect)
		{
			myRect = null;
		}
		else if (child == myLabel)
		{
			myLabel = null;
		}

		return child;
	}

	/**
	 * If the text label changed position/size on its own (i.e., not because
	 * the area was moved or resized), then make sure the rectangle
	 * is just bigger than the text
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
	 * Position all the parts of the area relative to the text label.
	 * Leave room for the JGo3DNoteRect decorations.
	 */
	protected void layoutChildren()
	{
		JGoText label = getLabel();

		if (label != null)
		{
			JGo3DNoteRect rect = getRect();

			if (rect != null)
			{
				rect.setBoundingRect(label.getLeft() - 4, label.getTop() - 2, label.getWidth() + 12, label.getHeight() + 4 + rect.getFlapSize());
			}
		}
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

		if (attr.equals("rectobj"))
		{
			myRect = (JGo3DNoteRect) referencedObject;
		}
		else if (attr.equals("label"))
		{
			myLabel = (JGoText) referencedObject;
		}
	}

	public void SVGWriteObject(DomDoc svgDoc, DomElement jGoElementGroup)
	{

		// Add Comment element
		DomElement jComment = svgDoc.createJGoClassElement("com.nwoods.jgo.examples.Comment", jGoElementGroup);

		// The following elements are all children of this area and so will be writen out
		// by JGoArea.SVGWriteObject().  We just need to update the references to them.
		if (myRect != null)
		{
			svgDoc.registerReferencingNode(jComment, "rectobj", myRect);
		}

		if (myLabel != null)
		{
			svgDoc.registerReferencingNode(jComment, "label", myLabel);
		}

		// Have superclass add to the JGoObject group
		super.SVGWriteObject(svgDoc, jGoElementGroup);
	}

	public DomNode SVGReadObject(DomDoc svgDoc, JGoDocument jGoDoc, DomElement svgElement, DomElement jGoChildElement)
	{
		if (jGoChildElement != null)
		{

			// This is a Comment element
			String rectobj = jGoChildElement.getAttribute("rectobj");

			svgDoc.registerReferencingObject(this, "rectobj", rectobj);

			String label = jGoChildElement.getAttribute("label");

			svgDoc.registerReferencingObject(this, "label", label);
			super.SVGReadObject(svgDoc, jGoDoc, svgElement, jGoChildElement.getNextSiblingJGoClassElement());
		}

		return svgElement.getNextSibling();
	}

	// get access to the parts of the node
	public JGoText getLabel()
	{
		return myLabel;
	}

	public JGo3DNoteRect getRect()
	{
		return myRect;
	}

	// These are convenience methods
	public String getText()
	{
		return getLabel().getText();
	}

	public void setText(String s)
	{
		getLabel().setText(s);
	}

	public boolean isEditable()
	{
		return getLabel().isEditable();
	}

	public void setEditable(boolean e)
	{
		getLabel().setEditable(e);
	}

	// State
	private JGoText myLabel = null;
	private JGo3DNoteRect myRect = null;
}
