package org.jgrafchart;



import com.nwoods.jgo.*;

import java.awt.*;

import java.util.*;


public class ParallelSplit
	extends GrafcetObject
	implements GCIdent
{

	public JGoStroke myTopLine = null;
	public JGoStroke myBottomLine = null;
	public JGoStroke myInline = null;
	public JGoStroke myOutline1 = null;
	public JGoStroke myOutline2 = null;
	public GCStepInPort myInPort = null;
	public GCTransitionOutPort myOutPort1 = null;
	public GCTransitionOutPort myOutPort2 = null;

	public ParallelSplit()
	{
		super();
	}

	public ParallelSplit(Point loc)
	{

		super();

		setSize(280, 20);
		setSelectable(true);
		setGrabChildSelection(true);
		setDraggable(true);
		setResizable(false);

		myTopLine = new JGoStroke();

		myTopLine.addPoint(0, 0);
		myTopLine.addPoint(280, 0);
		myTopLine.setSelectable(false);

		myBottomLine = new JGoStroke();

		myBottomLine.addPoint(0, 0);
		myBottomLine.addPoint(280, 0);
		myBottomLine.setSelectable(false);

		myInline = new JGoStroke();

		myInline.addPoint(10, 0);
		myInline.addPoint(10, 10);
		myInline.setSelectable(false);

		myOutline1 = new JGoStroke();

		myOutline1.addPoint(20, 0);
		myOutline1.addPoint(20, 10);
		myOutline1.setSelectable(false);

		myOutline2 = new JGoStroke();

		myOutline2.addPoint(20, 0);
		myOutline2.addPoint(20, 10);
		myOutline2.setSelectable(false);

		myInPort = new GCStepInPort();

		myInPort.setSize(10, 10);
		myInPort.setToSpot(JGoObject.TopCenter);
		myInPort.setPen(JGoPen.Null);
		myInPort.setBrush(JGoBrush.Null);

		myOutPort1 = new GCTransitionOutPort();

		myOutPort1.setSize(10, 10);
		myOutPort1.setFromSpot(JGoObject.BottomCenter);
		myOutPort1.setPen(JGoPen.Null);
		myOutPort1.setBrush(JGoBrush.Null);

		myOutPort2 = new GCTransitionOutPort();

		myOutPort2.setSize(10, 10);
		myOutPort2.setFromSpot(JGoObject.BottomCenter);
		myOutPort2.setPen(JGoPen.Null);
		myOutPort2.setBrush(JGoBrush.Null);
		addObjectAtTail(myTopLine);
		addObjectAtTail(myBottomLine);
		addObjectAtTail(myInline);
		addObjectAtTail(myOutline1);
		addObjectAtTail(myOutline2);
		addObjectAtTail(myInPort);
		addObjectAtTail(myOutPort1);
		addObjectAtTail(myOutPort2);
		setLocation(loc);
		layoutChildren();
	}

	public JGoObject copyObject(JGoCopyEnvironment env)
	{

		ParallelSplit newobj = (ParallelSplit) super.copyObject(env);

		return newobj;
	}

	public void copyChildren(JGoArea newarea, JGoCopyEnvironment env)
	{

		ParallelSplit newobj = (ParallelSplit) newarea;

		newobj.myTopLine = (JGoStroke) myTopLine.copyObject(env);

		newobj.addObjectAtTail(newobj.myTopLine);

		newobj.myBottomLine = (JGoStroke) myBottomLine.copyObject(env);

		newobj.addObjectAtTail(newobj.myBottomLine);

		newobj.myInline = (JGoStroke) myInline.copyObject(env);

		newobj.addObjectAtTail(newobj.myInline);

		newobj.myOutline1 = (JGoStroke) myOutline1.copyObject(env);

		newobj.addObjectAtTail(newobj.myOutline1);

		newobj.myOutline2 = (JGoStroke) myOutline2.copyObject(env);

		newobj.addObjectAtTail(newobj.myOutline2);

		newobj.myInPort = (GCStepInPort) myInPort.copyObject(env);

		newobj.addObjectAtTail(newobj.myInPort);

		newobj.myOutPort1 = (GCTransitionOutPort) myOutPort1.copyObject(env);

		newobj.addObjectAtTail(newobj.myOutPort1);

		newobj.myOutPort2 = (GCTransitionOutPort) myOutPort2.copyObject(env);

		newobj.addObjectAtTail(newobj.myOutPort2);
	}

	public Point getLocation(Point p)
	{
		return myTopLine.getSpotLocation(TopCenter, p);
	}

	public void setLocation(int x, int y)
	{
		myTopLine.setSpotLocation(TopCenter, x, y);
		layoutChildren();
	}

	public void layoutChildren()
	{

		if (myTopLine == null)
		{
			return;
		}

		if (myBottomLine != null)
		{
			Point p = myTopLine.getSpotLocation(BottomCenter);

			myBottomLine.setSpotLocation(TopCenter, (int) p.getX(), (int) p.getY() + 4);
		}

		if (myInline != null)
		{
			myInline.setSpotLocation(Bottom, myTopLine, TopCenter);
		}

		if (myOutline1 != null)
		{
			Point p = myBottomLine.getSpotLocation(BottomCenter);

			myOutline1.setSpotLocation(Top, (int) p.getX() - 110, (int) p.getY());
		}

		if (myOutline2 != null)
		{
			Point p = myBottomLine.getSpotLocation(BottomCenter);

			myOutline2.setSpotLocation(Top, (int) p.getX() + 110, (int) p.getY());
		}

		if (myInPort != null)
		{
			myInPort.setSpotLocation(TopCenter, myInline, Top);
		}

		if (myOutPort1 != null)
		{
			myOutPort1.setSpotLocation(BottomCenter, myOutline1, Bottom);
		}

		if (myOutPort2 != null)
		{
			myOutPort2.setSpotLocation(BottomCenter, myOutline2, Bottom);
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

	public boolean isTransition()
	{
		return true;
	}

	public boolean isStep()
	{
		return true;
	}

	public void compileDownwards(GenericTransition t)
	{

		JGoListPosition pos = myOutPort1.getFirstLinkPos();

		while (pos != null)
		{
			JGoLink l = myOutPort1.getLinkAtPos(pos);
			GrafcetObject gO = (GrafcetObject) l.getToPort().getParent();

			if (gO instanceof GCStep)
			{
				GCStep s = (GCStep) gO;

				t.addSucceedingStep(s);
			}

			if (gO instanceof MacroStep)
			{
				MacroStep ms = (MacroStep) gO;

				t.addSucceedingStep(ms);

				GCDocument doc = ms.myContentDocument;
				JGoListPosition pos1 = doc.getFirstObjectPos();
				JGoObject obj = doc.getObjectAtPos(pos1);

				while ((obj != null) && (pos1 != null))
				{
					if (obj instanceof EnterStep)
					{
						EnterStep ex = (EnterStep) obj;

						t.addSucceedingStep(ex);
					}

					pos1 = doc.getNextObjectPos(pos1);
					obj = doc.getObjectAtPos(pos1);
				}
			}

			if (gO instanceof ParallelSplit)
			{
				ParallelSplit ps = (ParallelSplit) gO;

				ps.compileDownwards(t);
			}

			pos = myOutPort1.getNextLinkPos(pos);
		}

		pos = myOutPort2.getFirstLinkPos();

		while (pos != null)
		{
			JGoLink l = myOutPort2.getLinkAtPos(pos);
			GrafcetObject gO = (GrafcetObject) l.getToPort().getParent();

			if (gO instanceof GCStep)
			{
				GCStep s = (GCStep) gO;

				t.addSucceedingStep(s);
			}

			if (gO instanceof MacroStep)
			{
				MacroStep ms = (MacroStep) gO;

				t.addSucceedingStep(ms);

				GCDocument doc = ms.myContentDocument;
				JGoListPosition pos1 = doc.getFirstObjectPos();
				JGoObject obj = doc.getObjectAtPos(pos1);

				while ((obj != null) && (pos1 != null))
				{
					if (obj instanceof EnterStep)
					{
						EnterStep ex = (EnterStep) obj;

						t.addSucceedingStep(ex);
					}

					pos1 = doc.getNextObjectPos(pos1);
					obj = doc.getObjectAtPos(pos1);
				}
			}

			if (gO instanceof ParallelSplit)
			{
				ParallelSplit ps = (ParallelSplit) gO;

				ps.compileDownwards(t);
			}

			pos = myOutPort2.getNextLinkPos(pos);
		}
	}
}
