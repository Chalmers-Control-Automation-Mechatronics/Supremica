package org.jgrafchart;



import com.nwoods.jgo.*;

import java.awt.*;

import java.util.*;

// import se.lth.control.realtime.*;
import org.jgrafchart.io.*;


/**
 * A DigitalIn is an Area containing a JGoStroke and two text labels
 */
public class DigitalOut
	extends JGoArea
	implements Writable
{

	static protected int digitalOutputCounter = 0;
	protected JGoStroke myBorder = null;
	public JGoText myOuttext = null;
	public JGoText myChanFixed = null;
	public JGoText myChannel = null;
	public int channel = -1;
	protected JGoText myValue = null;
	public boolean val = false;
	public boolean setLow = false;
	public boolean setHigh = false;
	static Color red = new Color(1.0f, 0f, 0f);
	static Color green = new Color(0f, 1.0f, 0f);
	static JGoBrush redSolidBrush = new JGoBrush(JGoBrush.SOLID, red);
	static JGoBrush greenSolidBrush = new JGoBrush(JGoBrush.SOLID, green);
	static JGoBrush noFill = new JGoBrush();
	static JGoPen redPen = new JGoPen(JGoPen.SOLID, 2, red);
	static JGoPen greenPen = new JGoPen(JGoPen.SOLID, 2, green);
	static JGoPen standardPen = new JGoPen(JGoPen.SOLID, 2, new Color(0.0F, 0.0F, 0.0F));
	public DigitalOutput digOut = null;

	public DigitalOut()
	{
		super();
	}

	public DigitalOut(Point loc, String labeltext)
	{

		super();

		setSize(80, 60);
		setSelectable(true);
		setGrabChildSelection(false);
		setDraggable(true);
		setResizable(false);

		myBorder = new JGoStroke();

		myBorder.setPen(new JGoPen(JGoPen.SOLID, 2, new Color(0.0F, 0.0F, 0.0F)));
		myBorder.addPoint(0, 0);
		myBorder.addPoint(60, 0);
		myBorder.addPoint(80, 30);
		myBorder.addPoint(60, 60);
		myBorder.addPoint(0, 60);
		myBorder.addPoint(0, 0);
		myBorder.setSelectable(false);
		myBorder.setDraggable(false);
		myBorder.setPen(redPen);
		setLocation(loc);

		digitalOutputCounter++;

		myOuttext = new JGoText("DOut" + digitalOutputCounter);

		myOuttext.setSelectable(true);
		myOuttext.setEditable(true);
		myOuttext.setEditOnSingleClick(true);
		myOuttext.setDraggable(false);
		myOuttext.setAlignment(JGoText.ALIGN_LEFT);
		myOuttext.setTransparent(true);

		myChanFixed = new JGoText("Chan:");

		myChanFixed.setSelectable(false);
		myChanFixed.setEditable(false);
		myChanFixed.setDraggable(false);
		myChanFixed.setAlignment(JGoText.ALIGN_LEFT);
		myChanFixed.setTransparent(true);

		myChannel = new JGoText("" + digitalOutputCounter);

		myChannel.setSelectable(true);
		myChannel.setEditable(true);
		myChannel.setEditOnSingleClick(true);
		myChannel.setDraggable(false);
		myChannel.setAlignment(JGoText.ALIGN_LEFT);
		myChannel.setTransparent(true);

		myValue = new JGoText(labeltext);

		myValue.setSelectable(false);
		myValue.setEditable(false);
		myValue.setDraggable(false);
		myValue.setAlignment(JGoText.ALIGN_LEFT);
		myValue.setTransparent(true);
		addObjectAtHead(myBorder);
		addObjectAtTail(myOuttext);
		addObjectAtTail(myChannel);
		addObjectAtTail(myChanFixed);
		addObjectAtTail(myValue);
		layoutChildren();
	}

	public JGoObject copyObject(JGoCopyEnvironment env)
	{

		DigitalOut newobj = (DigitalOut) super.copyObject(env);

		return newobj;
	}

	public void copyChildren(JGoArea newarea, JGoCopyEnvironment env)
	{

		DigitalOut newobj = (DigitalOut) newarea;

		if (myBorder != null)
		{
			newobj.myBorder = (JGoStroke) myBorder.copyObject(env);

			newobj.addObjectAtHead(newobj.myBorder);
		}

		if (myOuttext != null)
		{
			newobj.myOuttext = (JGoText) myOuttext.copyObject(env);

			digitalOutputCounter++;

			newobj.myOuttext.setText("DOut" + digitalOutputCounter);
			newobj.addObjectAtTail(newobj.myOuttext);
		}

		if (myChannel != null)
		{
			newobj.myChannel = (JGoText) myChannel.copyObject(env);

			newobj.myChannel.setText("" + digitalOutputCounter);
			newobj.addObjectAtTail(newobj.myChannel);
		}

		if (myChanFixed != null)
		{
			newobj.myChanFixed = (JGoText) myChanFixed.copyObject(env);

			newobj.addObjectAtTail(newobj.myChanFixed);
		}

		if (myValue != null)
		{
			newobj.myValue = (JGoText) myValue.copyObject(env);

			newobj.addObjectAtTail(newobj.myValue);
		}
	}

	public Point getLocation(Point p)
	{
		return getSpotLocation(Center, p);
	}

	public void setLocation(int x, int y)
	{
		myBorder.setSpotLocation(Center, x, y);
		layoutChildren();
	}

	public void layoutChildren()
	{

		if (myBorder == null)
		{
			return;
		}

		if (myOuttext != null)
		{
			Point p = myBorder.getSpotLocation(BottomCenter);

			myOuttext.setSpotLocation(TopCenter, (int) p.getX() - 5, (int) p.getY() + 7);
		}

		if (myChanFixed != null)
		{
			myChanFixed.setSpotLocation(TopRight, myBorder, TopCenter);
		}

		if (myChannel != null)
		{
			myChannel.setSpotLocation(LeftCenter, myChanFixed, RightCenter);
		}

		if (myValue != null)
		{
			myValue.setSpotLocation(Center, myBorder, Center);
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

	public void compile()
	{

		if (!((GCDocument) getDocument()).isSimulating())
		{
			int newChan = Integer.parseInt(myChannel.getText());

			if ((channel != newChan) || (digOut == null))
			{
				channel = newChan;

				try
				{
					digOut = DigitalIOFactory.getDigitialIO().getOutput(channel);
				}
				catch (Exception x)
				{
					System.out.println(x.getMessage());
					x.printStackTrace();
				}
			}
		}
	}

	public void setStoredBoolAction(boolean newval)
	{

		val = newval;

		if (newval)
		{
			myValue.setText("1");
			myBorder.setPen(greenPen);

			if (!((GCDocument) getDocument()).isSimulating())
			{
				digOut.set(true);

				// System.out.println("Digout 1");
			}
		}
		else
		{
			myValue.setText("0");
			myBorder.setPen(redPen);

			if (!((GCDocument) getDocument()).isSimulating())
			{
				digOut.set(false);

				// System.out.println("DigOut 0");
			}
		}

		// System.out.println("DigitalIn.setStoredAction");
		// System.out.println(newval);
		layoutChildren();
	}

	public void setStoredIntAction(int i) {}

	public void setNormalAction(boolean b)
	{

		// System.out.println("DigitalOut.SetNormalAction");
		if (b)
		{
			setHigh = true;
		}
		else
		{
			setLow = true;
		}
	}

	public void effectuateNormalActions()
	{

		// System.out.println("DigitalOut.effectuateNormalActions");
		if (setLow &&!setHigh)
		{
			myValue.setText("0");
			myBorder.setPen(redPen);

			if (!((GCDocument) getDocument()).isSimulating())
			{
				digOut.set(false);

				// System.out.println("DigOut 0");
			}

			val = false;

			layoutChildren();
		}
		else
		{
			if (setHigh)
			{
				myValue.setText("1");
				myBorder.setPen(greenPen);

				if (!((GCDocument) getDocument()).isSimulating())
				{
					digOut.set(true);

					// System.out.println("DigOut 1");
				}

				val = true;

				layoutChildren();
			}
		}

		setLow = false;
		setHigh = false;
	}

	public boolean isBoolean()
	{
		return true;
	}

	public boolean isInteger()
	{
		return false;
	}

	public boolean isString()
	{
		return false;
	}

	public void setStoredStringAction(String s) {}

	public String getName()
	{
		return myOuttext.getText();
	}
}
