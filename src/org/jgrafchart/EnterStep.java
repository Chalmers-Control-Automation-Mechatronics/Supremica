package org.jgrafchart;



import com.nwoods.jgo.*;

import java.awt.*;

import java.util.*;


/**
 * A Step is an Area containing a Rectangle and two Ports and
 * an optional Text Label.
 */
public class EnterStep
	extends GCStep
	implements GCIdent
{

	static final int CONNECT_SPOT = 10;

	/**
	 * A newly constructed GCStep is not usable until you've
	 * called initialize().
	 */
	public EnterStep()
	{
		super();
	}

	public EnterStep(Point loc, String labeltext)
	{

		super();

		setSize(60, 60);

		// the area as a whole is not directly selectable using a mouse,
		// but the area can be selected by trying to select any of its
		// children, all of whom are currently !isSelectable().
		setSelectable(true);
		setGrabChildSelection(false);

		// the user can move this node around
		setDraggable(true);

		// the user cannot resize this node
		setResizable(false);

		// create the big rectangle
		myRectangle = new JGoRectangle(getTopLeft(), getSize());

		myRectangle.setPen(new JGoPen(JGoPen.SOLID, 2, new Color(0.0F, 0.0F, 0.0F)));
		myRectangle.setSelectable(false);
		myRectangle.setDraggable(false);

		// can't setLocation until myRectangle exists
		setLocation(loc);

		myArrow = new JGoStroke();

		myArrow.setPen(new JGoPen(JGoPen.SOLID, 2, new Color(0.0F, 0.0F, 0.0F)));
		myArrow.addPoint(20, 0);
		myArrow.addPoint(40, 0);
		myArrow.addPoint(40, 10);
		myArrow.addPoint(50, 10);
		myArrow.addPoint(30, 20);
		myArrow.addPoint(10, 10);
		myArrow.addPoint(20, 10);
		myArrow.addPoint(20, 0);
		myArrow.setSelectable(false);

		// if there is a string, create a label with a transparent
		// background that is centered
		if (labeltext != null)
		{
			myLabel = new JGoText(labeltext);

			myLabel.setSelectable(true);
			myLabel.setEditable(true);
			myLabel.setEditOnSingleClick(true);
			myLabel.setDraggable(false);
			myLabel.setAlignment(JGoText.ALIGN_LEFT);
			myLabel.setTransparent(true);
		}
		else
		{

			// stepCounter++;
			myLabel = new JGoText("S");

			myLabel.setSelectable(true);
			myLabel.setEditable(true);
			myLabel.setEditOnSingleClick(true);
			myLabel.setDraggable(false);
			myLabel.setAlignment(JGoText.ALIGN_LEFT);
			myLabel.setTransparent(true);
		}
		;

		// create Inline and one Outline
		myOutline = new JGoStroke();

		myOutline.addPoint(20, 0);
		myOutline.addPoint(20, 10);
		myOutline.setSelectable(false);

		// create two Ports, which know how to make sure
		// connected JGoLinks have a reasonable end point
		myOutPort = new GCStepOutPort();

		myOutPort.setSize(10, 10);
		myOutPort.setFromSpot(JGoObject.BottomCenter);
		myOutPort.setPen(JGoPen.Null);
		myOutPort.setBrush(JGoBrush.Null);

		// create the 'token'
		myToken = new JGoEllipse();

		myToken.setSize(20, 20);
		myToken.setSelectable(false);
		myToken.setDraggable(false);
		myToken.setPen(JGoPen.Null);
		myToken.setBrush(JGoBrush.Null);

		// create the actionblock
		myActionStroke = new JGoStroke();

		myActionStroke.addPoint(0, 10);
		myActionStroke.addPoint(30, 10);
		myActionStroke.setPen(JGoPen.Null);
		myActionStroke.setBrush(JGoBrush.Null);
		myActionStroke.setSelectable(false);

		myActionRectangle = new JGoRectangle(new Point(), new Dimension(80, 50));

		myActionRectangle.setPen(JGoPen.Null);
		myActionRectangle.setBrush(JGoBrush.Null);
		myActionRectangle.setSelectable(false);
		myActionRectangle.setDraggable(false);

		// add all the children to the area
		addObjectAtHead(myRectangle);

		if (myLabel != null)
		{
			addObjectAtTail(myLabel);
		}

		addObjectAtTail(myArrow);
		addObjectAtTail(myOutline);
		addObjectAtTail(myOutPort);
		addObjectAtTail(myToken);
		addObjectAtTail(myActionStroke);
		layoutChildren();
	}

	public JGoObject copyObject(JGoCopyEnvironment env)
	{

		EnterStep newobj = (EnterStep) super.copyObject(env);

		return newobj;
	}

	public void copyChildren(JGoArea newarea, JGoCopyEnvironment env)
	{

		EnterStep newobj = (EnterStep) newarea;

		if (myRectangle != null)
		{
			newobj.myRectangle = (JGoRectangle) myRectangle.copyObject(env);

			newobj.addObjectAtHead(newobj.myRectangle);
		}

		if (myArrow != null)
		{
			newobj.myArrow = (JGoStroke) myArrow.copyObject(env);

			newobj.addObjectAtTail(newobj.myArrow);
		}

		if (myLabel != null)
		{
			newobj.myLabel = (JGoText) myLabel.copyObject(env);

			// stepCounter++;
			// newobj.myLabel.setText("S"+stepCounter);
			newobj.addObjectAtTail(newobj.myLabel);
		}

		if (myOutline != null)
		{
			newobj.myOutline = (JGoStroke) myOutline.copyObject(env);

			newobj.addObjectAtTail(newobj.myOutline);
		}

		if (myOutPort != null)
		{
			newobj.myOutPort = (GCStepOutPort) myOutPort.copyObject(env);

			newobj.addObjectAtTail(newobj.myOutPort);
		}

		if (myToken != null)
		{
			newobj.myToken = (JGoEllipse) myToken.copyObject(env);

			newobj.addObjectAtTail(newobj.myToken);
		}

		if (myActionStroke != null)
		{
			newobj.myActionStroke = (JGoStroke) myActionStroke.copyObject(env);

			newobj.addObjectAtTail(newobj.myActionStroke);
		}

		if (myActionRectangle != null)
		{
			newobj.myActionRectangle = (JGoRectangle) myActionRectangle.copyObject(env);

			newobj.addObjectAtTail(newobj.myActionRectangle);
		}

		if (myActionLabel != null)
		{
			newobj.myActionLabel = (JGoText) myActionLabel.copyObject(env);

			newobj.addObjectAtTail(newobj.myActionLabel);
		}
	}

	/**
	 * Specify the location of the node; the size is constant.
	 * If labeltext is null, do not create a label.
	 */

	// this area's natural "location" is the center of the rectangle
	public Point getLocation(Point p)
	{

		if (myRectangle != null)
		{
			return myRectangle.getSpotLocation(TopCenter, p);
		}
		else
		{
			return getSpotLocation(TopCenter, p);
		}
	}

	public void setLocation(int x, int y)
	{

		if (myRectangle != null)
		{
			myRectangle.setSpotLocation(TopCenter, x, y);
		}
		else
		{
			setSpotLocation(TopCenter, x, y);
		}

		layoutChildren();
	}

	/**
	 * Keep the parts of a GCStep positioned relative to each other
	 * by setting their locations using some of the standard spots of
	 * a JGoObject.
	 *
	 * By default the label will be positioned at the top of the node,
	 * above the ellipse.  To change this to be below the rectangle, at
	 * the bottom of the node, change the myLabel.setSpotLocation() call.
	 */
	public void layoutChildren()
	{

		if (myRectangle == null)
		{
			return;
		}

		if (myLabel != null)
		{
			Point p = myRectangle.getSpotLocation(RightCenter);

			myLabel.setSpotLocation(LeftCenter, (int) p.getX() + 8, (int) p.getY() + 10);

			// myLabel.setSpotLocation(LeftCenter,60,60);
		}

		if (myArrow != null)
		{
			myArrow.setSpotLocation(BottomCenter, myRectangle, TopCenter);
		}

		if (myOutline != null)
		{
			myOutline.setSpotLocation(Top, myRectangle, BottomCenter);
		}

		if (myOutPort != null)
		{
			myOutPort.setSpotLocation(BottomCenter, myOutline, Bottom);
		}

		if (myToken != null)
		{
			myToken.setSpotLocation(Center, myRectangle, Center);
		}

		if (myActionStroke != null)
		{
			myActionStroke.setSpotLocation(LeftCenter, myRectangle, RightCenter);
		}

		if (myActionRectangle != null)
		{
			myActionRectangle.setSpotLocation(LeftCenter, myActionStroke, RightCenter);
		}

		if (myActionLabel != null)
		{
			Point p1 = myActionRectangle.getSpotLocation(TopLeft);

			myActionLabel.setSpotLocation(TopLeft, (int) p1.getX() + 5, (int) p1.getY() + 1);
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

		layoutChildren();
	}

	// Convenience methods: control the rectangle's pen and brush
	public JGoPen getPen()
	{
		return myRectangle.getPen();
	}

	public void setPen(JGoPen p)
	{
		myRectangle.setPen(p);
	}

	public JGoBrush getBrush()
	{
		return myRectangle.getBrush();
	}

	public void setBrush(JGoBrush b)
	{
		myRectangle.setBrush(b);
	}

	public boolean isTransition()
	{
		return false;
	}

	public boolean isStep()
	{
		return true;
	}

	public void addSucceedingTransition(GCTransition s)
	{
		succeedingTransitions.add(s);
	}

	public void removeSucceedingTransition(GCTransition s)
	{

		if (succeedingTransitions.contains(s))
		{
			succeedingTransitions.remove(succeedingTransitions.indexOf(s));
		}
	}

	public void activate()
	{

		myToken.setBrush(JGoBrush.black);

		newX = true;
	}

	public void deactivate()
	{

		myToken.setBrush(JGoBrush.Null);

		newX = false;

		executeExitActions();

		if (((GCDocument) getDocument()).dimming)
		{
			myToken.setBrush(JGoBrush.lightGray);

			DimmerThread dt = new DimmerThread(this);

			dt.start();
		}
	}

	public void changeState()
	{

		if (!x)
		{
			timer = 0;
		}
		;

		if (x)
		{
			timer++;

			if (node != null)
			{
				node.executePeriodicActions();
			}
			;
		}
		;

		oldx = x;
		x = newX;

		if ((x &&!oldx) || (!x && oldx))
		{
			node.executeNormalActions(x);
		}

		if (x &&!oldx)
		{
			executeStoredActions();
		}
	}

	public void executeStoredActions()
	{

		// System.out.println("GCStep.executeStoredActions");
		// System.out.println("Inside EnterStep.executeStoredActions " + node);
		node.executeStoredActions();
	}

	public void executeNormalActions(boolean b)
	{
		node.executeNormalActions(b);
	}

	public void showActionBlock()
	{

		actionBlockVisible = true;

		myActionStroke.setPen(JGoPen.black);
		myActionRectangle.setPen(JGoPen.black);

		myActionLabel = new JGoText(actionText);

		myActionLabel.setSelectable(true);
		myActionLabel.setEditable(true);
		myActionLabel.setEditOnSingleClick(true);
		myActionLabel.setDraggable(false);
		myActionLabel.setAlignment(JGoText.ALIGN_LEFT);
		myActionLabel.setTransparent(true);
		myActionLabel.setBold(true);
		myActionLabel.setMultiline(true);
		addObjectAtTail(myActionLabel);
		layoutChildren();
	}

	public void hideActionBlock()
	{

		actionBlockVisible = false;

		myActionStroke.setPen(JGoPen.Null);
		myActionRectangle.setPen(JGoPen.Null);

		actionText = myActionLabel.getText();

		removeObject(myActionLabel);
		layoutChildren();
	}

	// get access to the parts of the node
	public JGoObject getRectangle()
	{
		return myRectangle;
	}

	public JGoText getLabel()
	{
		return myLabel;
	}

	public JGoStroke getOutline()
	{
		return myOutline;
	}

	public GCStepOutPort getOutPort()
	{
		return myOutPort;
	}

	public JGoEllipse getToken()
	{
		return myToken;
	}

	// State
	public JGoStroke myArrow = null;
}
