
/*
 * Test of JGo for Grafchart.
 */
package org.jgrafchart;

import com.nwoods.jgo.*;
import java.awt.*;
import java.util.*;

/**
 * A Step is an Area containing a Rectangle and two Ports and
 * an optional Text Label.
 */
public class GCStep
	extends GrafcetObject
	implements GCIdent, Readable
{

	// static protected int stepCounter = 0;
	static final int CONNECT_SPOT = 10;

	/**
	 * A newly constructed GCStep is not usable until you've
	 * called initialize().
	 */
	public GCStep()
	{
		super();
	}

	public GCStep(Point loc, String labeltext)
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

		// if there is a string, create a label with a transparent
		// background that is centered
		if (labeltext != null)
		{
			myLabel = new JGoText(labeltext);

			myLabel.setSelectable(true);
			myLabel.setEditable(true);
			myLabel.setEditOnSingleClick(true);
			myLabel.setDraggable(false);
			myLabel.setAlignment(JGoText.ALIGN_RIGHT);
			myLabel.setTransparent(true);
		}
		else
		{

			// stepCounter++;
			// myLabel = new JGoText("S"+stepCounter);
			myLabel = new JGoText("S");

			myLabel.setSelectable(true);
			myLabel.setEditable(true);
			myLabel.setEditOnSingleClick(true);
			myLabel.setDraggable(false);
			myLabel.setAlignment(JGoText.ALIGN_RIGHT);
			myLabel.setTransparent(true);
		}
		;

		// create Inline and one Outline
		myInline = new JGoStroke();

		myInline.addPoint(10, 0);
		myInline.addPoint(10, 10);
		myInline.setSelectable(false);

		myOutline = new JGoStroke();

		myOutline.addPoint(20, 0);
		myOutline.addPoint(20, 10);
		myOutline.setSelectable(false);

		// create two Ports, which know how to make sure
		// connected JGoLinks have a reasonable end point
		myInPort = new GCStepInPort();

		myInPort.setSize(10, 10);
		myInPort.setToSpot(JGoObject.TopCenter);
		myInPort.setPen(JGoPen.Null);
		myInPort.setBrush(JGoBrush.Null);

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

		myActionRectangle = new JGoRectangle(new Point(), new Dimension(110, 50));

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

		addObjectAtTail(myInline);
		addObjectAtTail(myOutline);
		addObjectAtTail(myInPort);
		addObjectAtTail(myOutPort);
		addObjectAtTail(myToken);
		addObjectAtTail(myActionStroke);
		layoutChildren();
	}

	public JGoObject copyObject(JGoCopyEnvironment env)
	{
		GCStep newobj = (GCStep) super.copyObject(env);

		return newobj;
	}

	public void copyChildren(JGoArea newarea, JGoCopyEnvironment env)
	{
		GCStep newobj = (GCStep) newarea;

		if (myRectangle != null)
		{
			newobj.myRectangle = (JGoRectangle) myRectangle.copyObject(env);

			newobj.addObjectAtHead(newobj.myRectangle);
		}

		if (myLabel != null)
		{
			newobj.myLabel = (JGoText) myLabel.copyObject(env);

			// stepCounter++;
			// newobj.myLabel.setText("S"+stepCounter);
			newobj.addObjectAtTail(newobj.myLabel);
		}

		if (myInline != null)
		{
			newobj.myInline = (JGoStroke) myInline.copyObject(env);

			newobj.addObjectAtTail(newobj.myInline);
		}

		if (myOutline != null)
		{
			newobj.myOutline = (JGoStroke) myOutline.copyObject(env);

			newobj.addObjectAtTail(newobj.myOutline);
		}

		if (myInPort != null)
		{
			newobj.myInPort = (GCStepInPort) myInPort.copyObject(env);

			newobj.addObjectAtTail(newobj.myInPort);
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

		newobj.actionBlockVisible = actionBlockVisible;
		newobj.actionText = actionText;
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
			return getSpotLocation(Center, p);
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
			setSpotLocation(Center, x, y);
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
			Point p = myRectangle.getSpotLocation(LeftCenter);

			myLabel.setSpotLocation(RightCenter, (int) p.getX() - 8, (int) p.getY());

			// myLabel.setSpotLocation(LeftCenter,60,60);
		}

		if (myInline != null)
		{
			myInline.setSpotLocation(Bottom, myRectangle, TopCenter);
		}

		if (myOutline != null)
		{
			myOutline.setSpotLocation(Top, myRectangle, BottomCenter);
		}

		if (myInPort != null)
		{
			myInPort.setSpotLocation(TopCenter, myInline, Top);
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

			if (myRectangle != null)
			{
				int newRectx = getLeft() + (int) Math.rint((myRectangle.getLeft() - prevRect.x) * scaleFactorX);
				int newRecty = getTop() + (int) Math.rint((myRectangle.getTop() - prevRect.y) * scaleFactorY);
				int newRectwidth = (int) Math.rint(myRectangle.getWidth() * scaleFactorX);
				int newRectheight = (int) Math.rint(myRectangle.getHeight() * scaleFactorY);

				myRectangle.setBoundingRect(newRectx, newRecty, newRectwidth, newRectheight);
			}

			if (myInPort != null)
			{
				int newRectx = getLeft() + (int) Math.rint((myInPort.getLeft() - prevRect.x) * scaleFactorX);
				int newRecty = getTop() + (int) Math.rint((myInPort.getTop() - prevRect.y) * scaleFactorY);
				int newRectwidth = (int) Math.rint(myInPort.getWidth() * scaleFactorX);
				int newRectheight = (int) Math.rint(myInPort.getHeight() * scaleFactorY);

				myInPort.setBoundingRect(newRectx, newRecty, newRectwidth, newRectheight);
			}

			if (myOutPort != null)
			{
				int newRectx = getLeft() + (int) Math.rint((myOutPort.getLeft() - prevRect.x) * scaleFactorX);
				int newRecty = getTop() + (int) Math.rint((myOutPort.getTop() - prevRect.y) * scaleFactorY);
				int newRectwidth = (int) Math.rint(myOutPort.getWidth() * scaleFactorX);
				int newRectheight = (int) Math.rint(myOutPort.getHeight() * scaleFactorY);

				myOutPort.setBoundingRect(newRectx, newRecty, newRectwidth, newRectheight);
			}

			// don't scale the label
			layoutChildren();
		}
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

	public void addPrecedingTransition(GCTransition s)
	{
		precedingTransitions.add(s);
	}

	public void removePrecedingTransition(GCTransition s)
	{
		if (precedingTransitions.contains(s))
		{
			precedingTransitions.remove(precedingTransitions.indexOf(s));
		}
	}

	// public void removePointers() {
	// JGoListPosition pos = myInPort.getFirstLinkPos();
	// while (pos != null) {
	// JGoLink l = myInPort.getLinkAtPos(pos);
	// ((GCTransition)l.getFromPort().getParent()).removeSucceedingStep(this);
	// pos = myInPort.getNextLinkPos(pos);
	// }
	// pos = myOutPort.getFirstLinkPos();
	// while (pos != null) {
	// JGoLink l = myOutPort.getLinkAtPos(pos);
	// ((GCTransition)l.getToPort().getParent()).removePrecedingStep(this);
	// pos = myOutPort.getNextLinkPos(pos);
	// }
	// }
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
		if (node != null)
		{

			// System.out.println("Stored action executed");
			node.executeStoredActions();
		}
	}

	public void executeNormalActions(boolean b)
	{
		if (node != null)
		{
			node.executeNormalActions(b);
		}
	}

	public void executeExitActions()
	{
		if (node != null)
		{

			// System.out.println("Exit actions executed");
			node.executeExitActions();
		}
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
		myLabel.setFontSize(14);
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

		myActionLabel = null;

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

	public JGoStroke getInline()
	{
		return myInline;
	}

	public JGoStroke getOutline()
	{
		return myOutline;
	}

	public GCStepInPort getInPort()
	{
		return myInPort;
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
	public JGoRectangle myRectangle = null;
	public JGoText myLabel = null;
	public JGoStroke myInline = null;
	protected JGoStroke myOutline = null;
	protected JGoStroke myActionStroke = null;
	protected GCStepInPort myInPort = null;
	protected GCStepOutPort myOutPort = null;
	public JGoEllipse myToken = null;
	protected JGoRectangle myActionRectangle = null;
	protected JGoText myActionLabel = null;
	protected String actionText = ";";
	public boolean actionBlockVisible = false;
	public org.jgrafchart.Actions.SimpleNode node = null;
	protected ArrayList succeedingTransitions = new ArrayList();
	protected ArrayList precedingTransitions = new ArrayList();

	public String getName()
	{
		return myLabel.getText();
	}

	public boolean getBoolVal()
	{
		return false;
	}

	public boolean getOldBoolVal()
	{
		return false;
	}

	public int getIntVal()
	{
		return 0;
	}

	public int getOldIntVal()
	{
		return 0;
	}

	public String getStringVal()
	{
		return new String("");
	}

	public String getOldStringVal()
	{
		return new String("");
	}

	public static JGoPen standardPen = new JGoPen();
}
