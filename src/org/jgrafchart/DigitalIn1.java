package org.jgrafchart;

import com.nwoods.jgo.*;
import java.awt.*;
import java.util.*;
import se.lth.control.realtime.*;

public class DigitalIn1
	extends DigitalIn
	implements Readable
{
	public JGoEllipse myCircle = null;

	public DigitalIn1()
	{
		super();
	}

	public DigitalIn1(Point loc)
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
		myBorder.addPoint(80, 0);
		myBorder.addPoint(80, 60);
		myBorder.addPoint(0, 60);
		myBorder.addPoint(20, 30);
		myBorder.addPoint(0, 0);
		myBorder.setSelectable(false);
		myBorder.setDraggable(false);
		myBorder.setPen(redPen);
		setLocation(loc);

		digitalInputCounter++;

		myIntext = new JGoText("DIn" + digitalInputCounter);

		myIntext.setSelectable(true);
		myIntext.setEditable(true);
		myIntext.setEditOnSingleClick(true);
		myIntext.setDraggable(false);
		myIntext.setAlignment(JGoText.ALIGN_LEFT);
		myIntext.setTransparent(true);

		myChanFixed = new JGoText("Chan:");

		myChanFixed.setSelectable(false);
		myChanFixed.setEditable(false);
		myChanFixed.setDraggable(false);
		myChanFixed.setAlignment(JGoText.ALIGN_LEFT);
		myChanFixed.setTransparent(true);

		myChannel = new JGoText("" + digitalInputCounter);

		myChannel.setSelectable(true);
		myChannel.setEditable(true);
		myChannel.setEditOnSingleClick(true);
		myChannel.setDraggable(false);
		myChannel.setAlignment(JGoText.ALIGN_LEFT);
		myChannel.setTransparent(true);

		myValue = new JGoText("0");

		myValue.setSelectable(true);
		myValue.setEditable(true);
		myValue.setEditOnSingleClick(true);
		myValue.setDraggable(false);
		myValue.setAlignment(JGoText.ALIGN_LEFT);
		myValue.setTransparent(true);

		myCircle = new JGoEllipse();

		myCircle.setSize(6, 6);
		myCircle.setSelectable(false);
		myCircle.setDraggable(false);
		myCircle.setPen(greenPen);
		addObjectAtHead(myBorder);
		addObjectAtTail(myIntext);
		addObjectAtTail(myChannel);
		addObjectAtTail(myChanFixed);
		addObjectAtTail(myValue);
		addObjectAtTail(myCircle);
		layoutChildren();
	}

	public JGoObject copyObject(JGoCopyEnvironment env)
	{
		DigitalIn1 newobj = (DigitalIn1) super.copyObject(env);

		return newobj;
	}

	public void copyChildren(JGoArea newarea, JGoCopyEnvironment env)
	{
		DigitalIn1 newobj = (DigitalIn1) newarea;

		if (myBorder != null)
		{
			newobj.myBorder = (JGoStroke) myBorder.copyObject(env);

			newobj.addObjectAtHead(newobj.myBorder);
		}

		if (myIntext != null)
		{
			newobj.myIntext = (JGoText) myIntext.copyObject(env);

			digitalInputCounter++;

			newobj.myIntext.setText("DIn" + digitalInputCounter);
			newobj.addObjectAtTail(newobj.myIntext);
		}

		if (myChannel != null)
		{
			newobj.myChannel = (JGoText) myChannel.copyObject(env);

			newobj.myChannel.setText("" + digitalInputCounter);
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

		if (myCircle != null)
		{
			newobj.myCircle = (JGoEllipse) myCircle.copyObject(env);

			newobj.addObjectAtTail(newobj.myCircle);
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

		if (myIntext != null)
		{
			Point p = myBorder.getSpotLocation(BottomCenter);

			myIntext.setSpotLocation(TopCenter, (int) p.getX(), (int) p.getY() + 7);
		}

		if (myChanFixed != null)
		{
			myChanFixed.setSpotLocation(TopCenter, myBorder, TopCenter);
		}

		if (myChannel != null)
		{
			myChannel.setSpotLocation(LeftCenter, myChanFixed, RightCenter);
		}

		if (myValue != null)
		{
			myValue.setSpotLocation(Center, myBorder, Center);
		}

		if (myCircle != null)
		{
			Point p1 = myBorder.getSpotLocation(LeftCenter);

			myCircle.setSpotLocation(RightCenter, (int) p1.getX() + 20, (int) p1.getY());
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

	public void readInput()
	{
		oldval = val;

		if (((GCDocument) getDocument()).isSimulating())
		{
			String s = myValue.getText();

			if (s.compareTo("0") == 0)
			{
				val = false;

				if (oldval)
				{
					myBorder.setPen(redPen);
					myCircle.setPen(greenPen);
				}
			}
			else
			{
				val = true;

				if (!oldval)
				{
					myBorder.setPen(greenPen);
					myCircle.setPen(redPen);
				}
			}
		}
		else
		{
			val = !digIn.get();

			// System.out.println("Digin "+ val);
			if (!val)
			{
				myBorder.setPen(redPen);
				myCircle.setPen(greenPen);
				myValue.setText("0");
			}
			else
			{
				myBorder.setPen(greenPen);
				myCircle.setPen(redPen);
				myValue.setText("1");
			}
		}
	}

	public void initialize()
	{
		compile();

		if (((GCDocument) getDocument()).isSimulating())
		{
			String s = myValue.getText();

			if (s.compareTo("0") == 0)
			{
				val = true;

				myBorder.setPen(redPen);
				myCircle.setPen(greenPen);
			}
			else
			{
				val = false;

				myBorder.setPen(greenPen);
				myCircle.setPen(redPen);
			}

			oldval = val;
		}
		else
		{
			val = !digIn.get();

			if (!val)
			{
				myBorder.setPen(redPen);
				myCircle.setPen(greenPen);
				myValue.setText("0");
			}
			else
			{
				myBorder.setPen(greenPen);
				myCircle.setPen(redPen);
				myValue.setText("1");
			}

			oldval = val;
		}
	}

	public boolean doMouseDblClick(int mod, java.awt.Point dc, java.awt.Point vc, JGoView view)
	{
		String s = myValue.getText();

		if (s.compareTo("0") == 0)
		{
			myValue.setText("1");
		}
		else
		{
			myValue.setText("0");
		}

		return true;
	}
}
