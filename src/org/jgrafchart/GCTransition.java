
/*
 * Test of JGo for Grafchart.
 */
package org.jgrafchart;



import com.nwoods.jgo.*;

import java.awt.*;

import java.util.*;

import java.lang.*;

import org.jgrafchart.Transitions.*;


/**
 * A Step is an Area containing a Rectangle and two Ports and
 * an optional Text Label.
 */
public class GCTransition
	extends GenericTransition
	implements GCIdent
{

	public GCTransition()
	{
		super();
	}

	public GCTransition(Point loc, String labeltext)
	{

		super();

		setSize(30, 5);

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
			myLabel.setAlignment(JGoText.ALIGN_LEFT);
			myLabel.setAutoResize(true);
			myLabel.setClipping(false);
			myLabel.setTransparent(true);
			myLabel.setBold(true);
			myLabel.setFontSize(18);
		}

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
		myInPort = new GCTransitionInPort();

		myInPort.setSize(10, 10);
		myInPort.setToSpot(JGoObject.TopCenter);
		myInPort.setPen(JGoPen.Null);
		myInPort.setBrush(JGoBrush.Null);

		myOutPort = new GCTransitionOutPort();

		myOutPort.setSize(10, 10);
		myOutPort.setFromSpot(JGoObject.BottomCenter);
		myOutPort.setPen(JGoPen.Null);
		myOutPort.setBrush(JGoBrush.Null);

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

		// now position the label and the ports appropriately
		// relative to the rectangle
		layoutChildren();
	}

	public JGoObject copyObject(JGoCopyEnvironment env)
	{

		GCTransition newobj = (GCTransition) super.copyObject(env);

		return newobj;
	}

	public void copyChildren(JGoArea newarea, JGoCopyEnvironment env)
	{

		GCTransition newobj = (GCTransition) newarea;

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
			newobj.myInPort = (GCTransitionInPort) myInPort.copyObject(env);

			newobj.addObjectAtTail(newobj.myInPort);
		}

		if (myOutPort != null)
		{
			newobj.myOutPort = (GCTransitionOutPort) myOutPort.copyObject(env);

			newobj.addObjectAtTail(newobj.myOutPort);
		}
	}

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
	 * Keep the parts of a GCTransition positioned relative to each other
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

			// put the label above the node
			// myLabel.setSpotLocation(BottomCenter, myRectangle, TopCenter);
			// put the label below the node
			// myLabel.setSpotLocation(TopCenter, myRectangle, BottomCenter);
			Point p = myRectangle.getSpotLocation(RightCenter);

			myLabel.setSpotLocation(LeftCenter, (int) p.getX() + 8, (int) p.getY());
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

	// Convenience methods: control the ellipse's pen and brush
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

			if (gO instanceof GCStep)
			{
				GCStep s = (GCStep) gO;

				addPrecedingStep(s);
			}

			if (gO instanceof MacroStep)
			{
				MacroStep ms = (MacroStep) gO;

				addPrecedingStep(ms);

				if (ms.myContentDocument != null)
				{
					GCDocument doc = ms.myContentDocument;
					JGoListPosition pos1 = doc.getFirstObjectPos();
					JGoObject obj = doc.getObjectAtPos(pos1);

					while ((obj != null) && (pos1 != null))
					{
						if (obj instanceof ExitStep)
						{
							ExitStep ex = (ExitStep) obj;

							addPrecedingStep(ex);
						}

						pos1 = doc.getNextObjectPos(pos1);
						obj = doc.getObjectAtPos(pos1);
					}
				}
			}

			if (gO instanceof ParallelJoin)
			{
				ParallelJoin pj = (ParallelJoin) gO;

				pj.compileUpwards(this);
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

				if (ms.myContentDocument != null)
				{
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

				if (s instanceof ProcedureStep)
				{
					ProcedureStep ps = (ProcedureStep) s;

					ok = ok && ps.x && (ps.exStep != null) && ps.exStep.x;
				}
				else
				{
					ok = ok && s.x;
				}
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
				s = (GrafcetObject) i.next();

				s.deactivate();

				// s.executeExitActions();
				// s.executeNormalActions(false);
			}

			for (Iterator i = succeedingSteps.iterator(); i.hasNext(); )
			{
				s = (GrafcetObject) i.next();

				s.activate();

				// s.executeStoredActions();
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

	public GCTransitionInPort getInPort()
	{
		return myInPort;
	}

	public GCTransitionOutPort getOutPort()
	{
		return myOutPort;
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

	// State
	protected JGoRectangle myRectangle = null;
	protected JGoText myLabel = null;
	protected JGoStroke myInline = null;
	protected JGoStroke myOutline = null;
	protected GCTransitionInPort myInPort = null;
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
