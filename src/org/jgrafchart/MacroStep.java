package org.jgrafchart;

import com.nwoods.jgo.*;
import java.awt.*;
import java.util.*;
import javax.swing.*;

/**
 * A Step is an Area containing a Rectangle and two Ports and
 * an optional Text Label.
 */
public class MacroStep
	extends GrafcetObject
	implements GCIdent, Readable
{
	static protected int stepCounter = 0;

	public MacroStep()
	{
		super();
	}

	public MacroStep(Point loc, String labeltext)
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

		myTopLeftStroke = new JGoStroke();

		myTopLeftStroke.addPoint(0, 15);
		myTopLeftStroke.addPoint(15, 0);
		myTopLeftStroke.setPen(new JGoPen(JGoPen.SOLID, 2, new Color(0.0F, 0.0F, 0.0F)));
		myTopLeftStroke.setSelectable(false);
		myTopLeftStroke.setDraggable(false);

		myTopRightStroke = new JGoStroke();

		myTopRightStroke.addPoint(0, 0);
		myTopRightStroke.addPoint(15, 15);
		myTopRightStroke.setPen(new JGoPen(JGoPen.SOLID, 2, new Color(0.0F, 0.0F, 0.0F)));
		myTopRightStroke.setSelectable(false);
		myTopRightStroke.setDraggable(false);

		myBottomLeftStroke = new JGoStroke();

		myBottomLeftStroke.addPoint(0, 0);
		myBottomLeftStroke.addPoint(15, 15);
		myBottomLeftStroke.setPen(new JGoPen(JGoPen.SOLID, 2, new Color(0.0F, 0.0F, 0.0F)));
		myBottomLeftStroke.setSelectable(false);
		myBottomLeftStroke.setDraggable(false);

		myBottomRightStroke = new JGoStroke();

		myBottomRightStroke.addPoint(0, 15);
		myBottomRightStroke.addPoint(15, 0);
		myBottomRightStroke.setPen(new JGoPen(JGoPen.SOLID, 2, new Color(0.0F, 0.0F, 0.0F)));
		myBottomRightStroke.setSelectable(false);
		myBottomRightStroke.setDraggable(false);

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
			stepCounter++;

			myLabel = new JGoText("M" + stepCounter);

			myLabel.setSelectable(true);
			myLabel.setEditable(true);
			myLabel.setEditOnSingleClick(true);
			myLabel.setDraggable(false);
			myLabel.setAlignment(JGoText.ALIGN_LEFT);
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

		myExcOutPort = new GCStepExceptionOutPort();

		myExcOutPort.setSize(6, 6);
		myExcOutPort.setStyle(JGoPort.StyleDiamond);
		myExcOutPort.setFromSpot(JGoObject.LeftCenter);

		// create the 'token'
		myToken = new JGoEllipse();

		myToken.setSize(20, 20);
		myToken.setSelectable(false);
		myToken.setDraggable(false);
		myToken.setPen(JGoPen.Null);
		myToken.setBrush(JGoBrush.Null);

		// add all the children to the area
		addObjectAtHead(myRectangle);
		addObjectAtTail(myTopLeftStroke);
		addObjectAtTail(myTopRightStroke);
		addObjectAtTail(myBottomLeftStroke);
		addObjectAtTail(myBottomRightStroke);

		if (myLabel != null)
		{
			addObjectAtTail(myLabel);
		}

		addObjectAtTail(myInline);
		addObjectAtTail(myOutline);
		addObjectAtTail(myInPort);
		addObjectAtTail(myOutPort);
		addObjectAtTail(myExcOutPort);
		addObjectAtTail(myToken);

		myContentDocument = new GCDocument();

		layoutChildren();
	}

	public JGoObject copyObject(JGoCopyEnvironment env)
	{
		MacroStep newobj = (MacroStep) super.copyObject(env);

		return newobj;
	}

	public void copyChildren(JGoArea newarea, JGoCopyEnvironment env)
	{
		MacroStep newobj = (MacroStep) newarea;

		if (myRectangle != null)
		{
			newobj.myRectangle = (JGoRectangle) myRectangle.copyObject(env);

			newobj.addObjectAtHead(newobj.myRectangle);
		}

		if (myTopLeftStroke != null)
		{
			newobj.myTopLeftStroke = (JGoStroke) myTopLeftStroke.copyObject(env);

			newobj.addObjectAtTail(newobj.myTopLeftStroke);
		}

		if (myTopRightStroke != null)
		{
			newobj.myTopRightStroke = (JGoStroke) myTopRightStroke.copyObject(env);

			newobj.addObjectAtTail(newobj.myTopRightStroke);
		}

		if (myBottomLeftStroke != null)
		{
			newobj.myBottomLeftStroke = (JGoStroke) myBottomLeftStroke.copyObject(env);

			newobj.addObjectAtTail(newobj.myBottomLeftStroke);
		}

		if (myBottomRightStroke != null)
		{
			newobj.myBottomRightStroke = (JGoStroke) myBottomRightStroke.copyObject(env);

			newobj.addObjectAtTail(newobj.myBottomRightStroke);
		}

		if (myLabel != null)
		{
			newobj.myLabel = (JGoText) myLabel.copyObject(env);

			stepCounter++;

			newobj.myLabel.setText("M" + stepCounter);
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

		if (myExcOutPort != null)
		{
			newobj.myExcOutPort = (GCStepExceptionOutPort) myExcOutPort.copyObject(env);

			newobj.addObjectAtTail(newobj.myExcOutPort);
		}

		if (myToken != null)
		{
			newobj.myToken = (JGoEllipse) myToken.copyObject(env);

			newobj.addObjectAtTail(newobj.myToken);
		}

		if (myContentDocument != null)
		{
			newobj.myContentDocument = new GCDocument();

			newobj.myContentDocument.copyFromCollection(myContentDocument);
		}
	}

	public Point getLocation(Point p)
	{
		if (myRectangle != null)
		{
			return myRectangle.getSpotLocation(Center, p);
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
			myRectangle.setSpotLocation(Center, x, y);
		}
		else
		{
			setSpotLocation(Center, x, y);
		}

		layoutChildren();
	}

	public void layoutChildren()
	{
		if (myRectangle == null)
		{
			return;
		}

		if (myTopLeftStroke != null)
		{
			myTopLeftStroke.setSpotLocation(TopLeft, myRectangle, TopLeft);
		}

		if (myTopRightStroke != null)
		{
			myTopRightStroke.setSpotLocation(TopRight, myRectangle, TopRight);
		}

		if (myBottomLeftStroke != null)
		{
			myBottomLeftStroke.setSpotLocation(BottomLeft, myRectangle, BottomLeft);
		}

		if (myBottomRightStroke != null)
		{
			myBottomRightStroke.setSpotLocation(BottomRight, myRectangle, BottomRight);
		}

		if (myLabel != null)
		{
			Point p = myRectangle.getSpotLocation(RightCenter);

			myLabel.setSpotLocation(LeftCenter, (int) p.getX() + 8, (int) p.getY() + 10);

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

		if (myExcOutPort != null)
		{
			myExcOutPort.setSpotLocation(LeftCenter, myRectangle, LeftCenter);
		}

		if (myToken != null)
		{
			myToken.setSpotLocation(Center, myRectangle, Center);
		}
	}

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

	public void activate()
	{
		myToken.setBrush(JGoBrush.black);

		newX = true;
	}

	public void deactivate()
	{
		myToken.setBrush(JGoBrush.Null);

		newX = false;

		if (((GCDocument) getDocument()).dimming)
		{
			myToken.setBrush(JGoBrush.lightGray);

			DimmerThread dt = new DimmerThread(this);

			dt.start();
		}
	}

	public void deactivateStrong()
	{
		myToken.setBrush(JGoBrush.Null);

		newX = false;

		if (((GCDocument) getDocument()).dimming)
		{
			myToken.setBrush(JGoBrush.lightGray);

			DimmerThread dt = new DimmerThread(this);

			dt.start();
		}

		deactivateBody(myContentDocument);
	}

	public void deactivateBody(GCDocument doc)
	{
		JGoListPosition pos = doc.getFirstObjectPos();
		JGoObject obj = doc.getObjectAtPos(pos);

		while ((obj != null) && (pos != null))
		{
			if (obj instanceof GCStep)
			{
				GCStep s = (GCStep) obj;

				s.myToken.setBrush(JGoBrush.Null);

				s.x = false;
				s.newX = false;

				if (doc.dimming)
				{
					s.myToken.setBrush(JGoBrush.lightGray);

					DimmerThread dt = new DimmerThread(s);

					dt.start();
				}
			}

			if (obj instanceof MacroStep)
			{
				MacroStep ms = (MacroStep) obj;

				ms.myToken.setBrush(JGoBrush.Null);

				ms.x = false;
				ms.newX = false;

				if (doc.dimming)
				{
					ms.myToken.setBrush(JGoBrush.lightGray);

					DimmerThread dt = new DimmerThread(ms);

					dt.start();
				}

				deactivateBody(ms.myContentDocument);
			}

			pos = doc.getNextObjectPos(pos);
			obj = doc.getObjectAtPos(pos);
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
		}
		;

		oldx = x;
		x = newX;
	}

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

	public JGoRectangle myRectangle = null;
	public JGoStroke myTopLeftStroke = null;
	public JGoStroke myTopRightStroke = null;
	public JGoStroke myBottomLeftStroke = null;
	public JGoStroke myBottomRightStroke = null;
	public JGoText myLabel = null;
	public JGoStroke myInline = null;
	protected JGoStroke myOutline = null;
	protected JGoStroke myActionStroke = null;
	protected GCStepInPort myInPort = null;
	protected GCStepOutPort myOutPort = null;
	protected GCStepExceptionOutPort myExcOutPort = null;
	protected JGoEllipse myToken = null;
	public GCDocument myContentDocument = null;
	public JInternalFrame frame = null;
	public GCView parentView = null;
	public GCView view = null;
	public Rectangle bounds = null;
	public double currentScale = 1.0;
	protected ArrayList succeedingTransitions = new ArrayList();
	protected ArrayList precedingTransitions = new ArrayList();
	public int stepCounterInt = 2;

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
}
