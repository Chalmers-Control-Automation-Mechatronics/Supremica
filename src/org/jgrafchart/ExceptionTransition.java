package org.jgrafchart;

import com.nwoods.jgo.*;
import java.awt.*;
import java.util.*;
import java.lang.*;
import org.jgrafchart.Transitions.*;

public class ExceptionTransition
	extends GenericTransition
	implements GCIdent
{
	public ExceptionTransition()
	{
		super();
	}

	public ExceptionTransition(Point loc, String labeltext)
	{
		super();

		setSize(5, 30);
		setSelectable(true);
		setGrabChildSelection(false);
		setDraggable(true);
		setResizable(false);

		myRectangle = new JGoRectangle(getTopLeft(), getSize());

		myRectangle.setPen(new JGoPen(JGoPen.SOLID, 3, new Color(0.0F, 0.0F, 0.0F)));
		myRectangle.setSelectable(false);
		myRectangle.setDraggable(false);
		setLocation(loc);

		if (labeltext != null)
		{
			myLabel = new JGoText(labeltext);

			myLabel.setSelectable(true);
			myLabel.setEditable(true);
			myLabel.setEditOnSingleClick(true);
			myLabel.setDraggable(false);
			myLabel.setAlignment(JGoText.ALIGN_LEFT);
			myLabel.setAutoResize(true);
			myLabel.setClipping(false);
			myLabel.setTransparent(true);
			myLabel.setBold(true);
			myLabel.setFontSize(18);
		}

		myInline = new JGoStroke();

		myInline.addPoint(0, 0);
		myInline.addPoint(10, 0);
		myInline.setSelectable(false);
		myInline.setPen(new JGoPen(JGoPen.SOLID, 3, new Color(0.0F, 0.0F, 0.0F)));

		myOutline = new JGoStroke();

		myOutline.addPoint(0, 0);
		myOutline.addPoint(10, 0);
		myOutline.setSelectable(false);

		myInPort = new ExceptionTransitionInPort();

		myInPort.setSize(10, 10);
		myInPort.setToSpot(JGoObject.RightCenter);
		myInPort.setPen(JGoPen.Null);
		myInPort.setBrush(JGoBrush.Null);

		myOutPort = new GCTransitionOutPort();

		myOutPort.setSize(10, 10);
		myOutPort.setFromSpot(JGoObject.LeftCenter);
		myOutPort.setPen(JGoPen.Null);
		myOutPort.setBrush(JGoBrush.Null);
		addObjectAtHead(myRectangle);

		if (myLabel != null)
		{
			addObjectAtTail(myLabel);
		}

		addObjectAtTail(myInline);
		addObjectAtTail(myOutline);
		addObjectAtTail(myInPort);
		addObjectAtTail(myOutPort);
		layoutChildren();
	}

	public JGoObject copyObject(JGoCopyEnvironment env)
	{
		ExceptionTransition newobj = (ExceptionTransition) super.copyObject(env);

		return newobj;
	}

	public void copyChildren(JGoArea newarea, JGoCopyEnvironment env)
	{
		ExceptionTransition newobj = (ExceptionTransition) newarea;

		newobj.condition = condition;

		if (myRectangle != null)
		{
			newobj.myRectangle = (JGoRectangle) myRectangle.copyObject(env);

			newobj.addObjectAtHead(newobj.myRectangle);
		}

		if (myLabel != null)
		{
			newobj.myLabel = (JGoText) myLabel.copyObject(env);

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
			newobj.myInPort = (ExceptionTransitionInPort) myInPort.copyObject(env);

			newobj.addObjectAtTail(newobj.myInPort);
		}

		if (myOutPort != null)
		{
			newobj.myOutPort = (GCTransitionOutPort) myOutPort.copyObject(env);

			newobj.addObjectAtTail(newobj.myOutPort);
		}
	}

	public Point getLocation(Point p)
	{
		if (myRectangle != null)
		{
			return myRectangle.getSpotLocation(RightCenter, p);
		}
		else
		{
			return getSpotLocation(RightCenter, p);
		}
	}

	public void setLocation(int x, int y)
	{
		if (myRectangle != null)
		{
			myRectangle.setSpotLocation(RightCenter, x, y);
		}
		else
		{
			setSpotLocation(RightCenter, x, y);
		}

		layoutChildren();
	}

	public void layoutChildren()
	{
		if (myRectangle == null)
		{
			return;
		}

		if (myLabel != null)
		{

			// put the label above the node
			// myLabel.setSpotLocation(BottomCenter, myRectangle, TopCenter);
			// put the label below the node
			// myLabel.setSpotLocation(TopCenter, myRectangle, BottomCenter);
			Point p = myRectangle.getSpotLocation(BottomCenter);

			myLabel.setSpotLocation(TopCenter, (int) p.getX() + 13, (int) p.getY() - 3);
		}

		if (myInline != null)
		{
			myInline.setSpotLocation(LeftCenter, myRectangle, RightCenter);
		}

		if (myOutline != null)
		{
			myOutline.setSpotLocation(RightCenter, myRectangle, LeftCenter);
		}

		if (myInPort != null)
		{
			myInPort.setSpotLocation(RightCenter, myInline, RightCenter);
		}

		if (myOutPort != null)
		{
			myOutPort.setSpotLocation(LeftCenter, myOutline, LeftCenter);
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
		return true;
	}

	public boolean isStep()
	{
		return false;
	}

	public void addSucceedingStep(GrafcetObject s)
	{
		succeedingSteps.add(s);
	}

	public void removeSucceedingStep(GCStep s)
	{
		if (succeedingSteps.contains(s))
		{
			succeedingSteps.remove(succeedingSteps.indexOf(s));
		}
	}

	public void addPrecedingStep(GrafcetObject s)
	{
		precedingSteps.add(s);
	}

	public void removePrecedingStep(GCStep s)
	{
		if (precedingSteps.contains(s))
		{
			precedingSteps.remove(precedingSteps.indexOf(s));
		}
	}

	/*
	 * NISSE
	 * public void removePointers() {
	 *
	 *   JGoListPosition pos = myInPort.getFirstLinkPos();
	 *   while (pos != null) {
	 *     JGoLink l = myInPort.getLinkAtPos(pos);
	 *     ((GCStep)l.getFromPort().getParent()).removeSucceedingTransition(this);
	 *     pos = myInPort.getNextLinkPos(pos);
	 *   }
	 *   pos = myOutPort.getFirstLinkPos();
	 *   while (pos != null) {
	 *     JGoLink l = myOutPort.getLinkAtPos(pos);
	 *     ((GCStep)l.getToPort().getParent()).removePrecedingTransition(this);
	 *     pos = myOutPort.getNextLinkPos(pos);
	 *   }
	 * }
	 */
	public void compileStructure()
	{
		precedingSteps.clear();
		succeedingSteps.clear();

		JGoListPosition pos = myInPort.getFirstLinkPos();

		while (pos != null)
		{
			JGoLink l = myInPort.getLinkAtPos(pos);
			GrafcetObject gO = (GrafcetObject) l.getFromPort().getParent();

			if (gO instanceof MacroStep)
			{
				MacroStep ms = (MacroStep) gO;

				addPrecedingStep(ms);
			}

			pos = myInPort.getNextLinkPos(pos);
		}

		pos = myOutPort.getFirstLinkPos();

		while (pos != null)
		{
			JGoLink l = myOutPort.getLinkAtPos(pos);
			GrafcetObject gO = (GrafcetObject) l.getToPort().getParent();

			if (gO instanceof GCStep)
			{
				GCStep s = (GCStep) gO;

				addSucceedingStep(s);
			}

			if (gO instanceof ParallelSplit)
			{
				ParallelSplit ps = (ParallelSplit) gO;

				ps.compileDownwards(this);
			}

			if (gO instanceof MacroStep)
			{
				MacroStep ms = (MacroStep) gO;

				addSucceedingStep(ms);

				GCDocument doc = ms.myContentDocument;
				JGoListPosition pos1 = doc.getFirstObjectPos();
				JGoObject obj = doc.getObjectAtPos(pos1);

				while ((obj != null) && (pos1 != null))
				{
					if (obj instanceof EnterStep)
					{
						EnterStep ex = (EnterStep) obj;

						addSucceedingStep(ex);
					}

					pos1 = doc.getNextObjectPos(pos1);
					obj = doc.getObjectAtPos(pos1);
				}
			}

			pos = myOutPort.getNextLinkPos(pos);
		}
	}

	public void testAndFire()
	{
		GrafcetObject s;
		boolean ok = false;

		oldCondition = condition;
		condition = node.evaluate();

		if (condition)
		{
			if (!oldCondition)
			{

				// myLabel.setTextColor(new Color(0.0f,1.0f,0f));
				myRectangle.setBrush(greenSolidBrush);
			}

			ok = true;

			for (Iterator i = precedingSteps.iterator(); i.hasNext(); )
			{
				s = (GrafcetObject) i.next();

				if (s instanceof MacroStep) {}

				ok = ok && s.x;
			}
		}
		else
		{
			if (oldCondition)
			{

				// myLabel.setTextColor(new Color(1.0f,0f,0f));
				myRectangle.setBrush(redSolidBrush);
			}
		}

		if (condition && ok)
		{
			for (Iterator i = precedingSteps.iterator(); i.hasNext(); )
			{
				MacroStep ms = (MacroStep) i.next();

				ms.deactivateStrong();

				// s.executeNormalActions(false);
			}

			for (Iterator i = succeedingSteps.iterator(); i.hasNext(); )
			{
				s = (GrafcetObject) i.next();

				s.activate();
				s.executeStoredActions();

				// s.executeNormalActions(true);
			}
		}
	}

	public void initialize()
	{
		condition = node.evaluate();
		oldCondition = condition;

		if (condition)
		{
			myRectangle.setBrush(greenSolidBrush);
		}
		else
		{
			myRectangle.setBrush(redSolidBrush);
		}
	}

	public void stop()
	{
		myRectangle.setBrush(noFill);
	}

	public String getLabelText()
	{
		return myLabel.getText();
	}

	public void setLabelText(String s)
	{
		myLabel.setText(s);
	}

	public void setTextColor(Color s)
	{
		myLabel.setTextColor(s);
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

	public ExceptionTransitionInPort getInPort()
	{
		return myInPort;
	}

	public GCTransitionOutPort getOutPort()
	{
		return myOutPort;
	}

	protected JGoRectangle myRectangle = null;
	protected JGoText myLabel = null;
	protected JGoStroke myInline = null;
	protected JGoStroke myOutline = null;
	protected ExceptionTransitionInPort myInPort = null;
	protected GCTransitionOutPort myOutPort = null;
	public ArrayList succeedingSteps = new ArrayList();
	public ArrayList precedingSteps = new ArrayList();
	protected boolean condition = false;
	protected boolean oldCondition = false;

	// public SimpleNode node;
	static Color red = new Color(1.0f, 0f, 0f);
	static Color green = new Color(0f, 1.0f, 0f);
	static JGoBrush redSolidBrush = new JGoBrush(JGoBrush.SOLID, red);
	static JGoBrush greenSolidBrush = new JGoBrush(JGoBrush.SOLID, green);
	static JGoBrush noFill = new JGoBrush();
}
