package org.jgrafchart;



import com.nwoods.jgo.*;

import java.awt.*;

import java.util.*;

import javax.swing.*;


public class ProcedureStep
	extends MacroStep
	implements Readable
{

	public ProcedureStep()
	{
		super();
	}

	public ProcedureStep(Point loc, String labeltext)
	{

		// super();
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

		myTopLeftPolygon = new JGoPolygon();

		myTopLeftPolygon.addPoint(0, 0);
		myTopLeftPolygon.addPoint(0, 15);
		myTopLeftPolygon.addPoint(15, 0);
		myTopLeftPolygon.setPen(new JGoPen(JGoPen.SOLID, 2, new Color(0.0F, 0.0F, 0.0F)));
		myTopLeftPolygon.setBrush(blackBrush);
		myTopLeftPolygon.setSelectable(false);
		myTopLeftPolygon.setDraggable(false);

		myTopRightPolygon = new JGoPolygon();

		myTopRightPolygon.addPoint(0, 0);
		myTopRightPolygon.addPoint(15, 0);
		myTopRightPolygon.addPoint(15, 15);
		myTopRightPolygon.setPen(new JGoPen(JGoPen.SOLID, 2, new Color(0.0F, 0.0F, 0.0F)));
		myTopRightPolygon.setBrush(blackBrush);
		myTopRightPolygon.setSelectable(false);
		myTopRightPolygon.setDraggable(false);

		myBottomLeftPolygon = new JGoPolygon();

		myBottomLeftPolygon.addPoint(0, 0);
		myBottomLeftPolygon.addPoint(15, 15);
		myBottomLeftPolygon.addPoint(0, 15);
		myBottomLeftPolygon.setPen(new JGoPen(JGoPen.SOLID, 2, new Color(0.0F, 0.0F, 0.0F)));
		myBottomLeftPolygon.setBrush(blackBrush);
		myBottomLeftPolygon.setSelectable(false);
		myBottomLeftPolygon.setDraggable(false);

		myBottomRightPolygon = new JGoPolygon();

		myBottomRightPolygon.addPoint(0, 15);
		myBottomRightPolygon.addPoint(15, 0);
		myBottomRightPolygon.addPoint(15, 15);
		myBottomRightPolygon.setPen(new JGoPen(JGoPen.SOLID, 2, new Color(0.0F, 0.0F, 0.0F)));
		myBottomRightPolygon.setSelectable(false);
		myBottomRightPolygon.setDraggable(false);

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
			myLabel = new JGoText("P");

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
		addObjectAtTail(myTopLeftPolygon);
		addObjectAtTail(myTopRightPolygon);
		addObjectAtTail(myBottomLeftPolygon);
		addObjectAtTail(myBottomRightPolygon);

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

		ProcedureStep newobj = (ProcedureStep) super.copyObject(env);

		return newobj;
	}

	public void copyChildren(JGoArea newarea, JGoCopyEnvironment env)
	{

		ProcedureStep newobj = (ProcedureStep) newarea;

		if (myRectangle != null)
		{
			newobj.myRectangle = (JGoRectangle) myRectangle.copyObject(env);

			newobj.addObjectAtHead(newobj.myRectangle);
		}

		if (myTopLeftPolygon != null)
		{
			newobj.myTopLeftPolygon = (JGoPolygon) myTopLeftPolygon.copyObject(env);

			newobj.addObjectAtTail(newobj.myTopLeftPolygon);
		}

		if (myTopRightPolygon != null)
		{
			newobj.myTopRightPolygon = (JGoPolygon) myTopRightPolygon.copyObject(env);

			newobj.addObjectAtTail(newobj.myTopRightPolygon);
		}

		if (myBottomLeftPolygon != null)
		{
			newobj.myBottomLeftPolygon = (JGoPolygon) myBottomLeftPolygon.copyObject(env);

			newobj.addObjectAtTail(newobj.myBottomLeftPolygon);
		}

		if (myBottomRightPolygon != null)
		{
			newobj.myBottomRightPolygon = (JGoPolygon) myBottomRightPolygon.copyObject(env);

			newobj.addObjectAtTail(newobj.myBottomRightPolygon);
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

		if (myTopLeftPolygon != null)
		{
			myTopLeftPolygon.setSpotLocation(TopLeft, myRectangle, TopLeft);
		}

		if (myTopRightPolygon != null)
		{
			myTopRightPolygon.setSpotLocation(TopRight, myRectangle, TopRight);
		}

		if (myBottomLeftPolygon != null)
		{
			myBottomLeftPolygon.setSpotLocation(BottomLeft, myRectangle, BottomLeft);
		}

		if (myBottomRightPolygon != null)
		{
			myBottomRightPolygon.setSpotLocation(BottomRight, myRectangle, BottomRight);
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

	public void activate()
	{

		myToken.setBrush(JGoBrush.black);

		newX = true;

		// System.out.println("procedure " + procedure);
		// System.out.println("procedure.myContDoc " + procedure.myContentDocument);
		boolean found = false;
		Object o1;
		String strval = procNode.evaluateString();

		System.out.println("EvaluateString" + strval);

		for (Iterator i = symbolList.iterator(); !found && i.hasNext(); )
		{
			o1 = (Object) i.next();

			if (o1 instanceof GrafcetProcedure)
			{
				GrafcetProcedure gp1 = (GrafcetProcedure) o1;

				if (strval.compareTo(gp1.myLabel.getText()) == 0)
				{
					procedure = gp1;
					found = true;
				}
			}
		}

		myContentDocument = new GCDocument();

		myContentDocument.copyFromCollection(procedure.myContentDocument);

		// System.out.println("After copyFromCollection");
		// System.out.println("symbolList " + symbolList);
		ArrayList sl = viewOwner.getBasicApp().compileDocument(myContentDocument, symbolList);

		paramNode.compile2(sl);
		viewOwner.initializeDocument(myContentDocument);

		// System.out.println("After CompileDocument");
		JGoListPosition pos1 = myContentDocument.getFirstObjectPos();
		JGoObject obj = myContentDocument.getObjectAtPos(pos1);

		while ((obj != null) && (pos1 != null))
		{
			if (obj instanceof EnterStep)
			{
				EnterStep ex = (EnterStep) obj;

				ex.activate();
			}

			if (obj instanceof ExitStep)
			{
				ExitStep es = (ExitStep) obj;

				exStep = es;
			}

			pos1 = myContentDocument.getNextObjectPos(pos1);
			obj = myContentDocument.getObjectAtPos(pos1);
		}
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

		if (myContentDocument != null)
		{
			JGoListPosition pos1 = myContentDocument.getFirstObjectPos();
			JGoObject obj = myContentDocument.getObjectAtPos(pos1);

			while ((obj != null) && (pos1 != null))
			{
				if (obj instanceof ExitStep)
				{
					ExitStep ex = (ExitStep) obj;

					ex.deactivate();
				}

				pos1 = myContentDocument.getNextObjectPos(pos1);
				obj = myContentDocument.getObjectAtPos(pos1);
			}

			myContentDocument = null;
			exStep = null;

			if (frame != null)
			{
				try
				{
					frame.setClosed(true);
				}
				catch (Exception x) {}

				frame = null;

				viewOwner.getBasicApp().setCurrentView(viewOwner);
			}
		}
	}

	public JGoPolygon myTopLeftPolygon = null;
	public JGoPolygon myTopRightPolygon = null;
	public JGoPolygon myBottomLeftPolygon = null;
	public JGoPolygon myBottomRightPolygon = null;
	public GrafcetProcedure procedure = null;
	public ArrayList symbolList = null;
	public GCView viewOwner = null;
	public ExitStep exStep = null;
	public String gp = new String("");
	public String parameters = new String("");
	public static Color black = new Color(0.0f, 0.0f, 0.0f);
	public static JGoBrush blackBrush = new JGoBrush(JGoBrush.SOLID, black);
	public org.jgrafchart.Actions.SimpleNode procNode = null;
	public org.jgrafchart.Actions.SimpleNode paramNode = null;

	public String getName()
	{
		return myLabel.getText();
	}
}
