package org.jgrafchart;

import com.nwoods.jgo.*;
import java.awt.*;
import java.util.*;

public class BooleanVariable
	extends InternalVariable
{
	private JGoText myTag = null;
	public boolean val = false;
	public boolean oldval = false;

	public BooleanVariable()
	{
		super();
	}

	public BooleanVariable(Point loc)
	{
		super();

		setSize(65, 45);
		setSelectable(true);
		setGrabChildSelection(false);
		setDraggable(true);
		setResizable(false);

		myBorder = new JGoRectangle(getTopLeft(), getSize());

		myBorder.setPen(new JGoPen(JGoPen.SOLID, 2, new Color(0.0F, 0.0F, 0.0F)));
		myBorder.setSelectable(false);
		myBorder.setDraggable(false);
		setLocation(loc);

		myName = new JGoText("Var");

		myName.setSelectable(true);
		myName.setEditable(true);
		myName.setEditOnSingleClick(true);
		myName.setDraggable(false);
		myName.setAlignment(JGoText.ALIGN_CENTER);
		myName.setTransparent(true);

		int temp;

		if (val)
		{
			temp = 1;
		}
		else
		{
			temp = 0;
		}

		myValue = new JGoText("" + temp);

		myValue.setSelectable(true);
		myValue.setEditable(true);
		myValue.setEditOnSingleClick(true);
		myValue.setDraggable(false);
		myValue.setAlignment(JGoText.ALIGN_LEFT);
		myValue.setTransparent(true);

		myTag = new JGoText("Bool ");

		myTag.setSelectable(false);
		myTag.setEditable(false);
		myTag.setEditOnSingleClick(false);
		myTag.setDraggable(false);
		myTag.setTransparent(true);
		myValue.setAlignment(JGoText.ALIGN_LEFT);
		addObjectAtHead(myBorder);
		addObjectAtTail(myName);
		addObjectAtTail(myValue);
		addObjectAtTail(myTag);
		layoutChildren();
	}

	public JGoObject copyObject(JGoCopyEnvironment env)
	{
		BooleanVariable newobj = (BooleanVariable) super.copyObject(env);

		return newobj;
	}

	public void copyChildren(JGoArea newarea, JGoCopyEnvironment env)
	{
		BooleanVariable newobj = (BooleanVariable) newarea;

		if (myBorder != null)
		{
			newobj.myBorder = (JGoRectangle) myBorder.copyObject(env);

			newobj.addObjectAtHead(newobj.myBorder);
		}

		if (myName != null)
		{
			newobj.myName = (JGoText) myName.copyObject(env);

			newobj.addObjectAtTail(newobj.myName);
		}

		if (myValue != null)
		{
			newobj.myValue = (JGoText) myValue.copyObject(env);

			newobj.addObjectAtTail(newobj.myValue);
		}

		if (myTag != null)
		{
			newobj.myTag = (JGoText) myTag.copyObject(env);

			newobj.addObjectAtTail(newobj.myTag);
		}
	}

	public void layoutChildren()
	{
		if (myBorder == null)
		{
			return;
		}

		if (myName != null)
		{
			myName.setSpotLocation(TopCenter, myBorder, BottomCenter);
		}

		if (myTag != null)
		{
			myTag.setSpotLocation(RightCenter, myBorder, Center);
		}

		if (myValue != null)
		{
			myValue.setSpotLocation(LeftCenter, myTag, RightCenter);
		}
	}

	public void setStoredBoolAction(boolean n)
	{
		if (n)
		{
			myValue.setText("" + 1);
		}
		else
		{
			myValue.setText("" + 0);
		}

		if (redirect == null)
		{
			oldval = val;
			val = n;
		}
		else
		{
			redirect.setStoredBoolAction(n);
		}
	}

	public void setStoredIntAction(int i) {}

	public boolean getBoolVal()
	{
		if (redirect == null)
		{
			return val;
		}
		else
		{
			boolean b = redirect.getBoolVal();

			if (b)
			{
				myValue.setText("" + 1);
			}
			else
			{
				myValue.setText("" + 0);
			}

			return b;
		}
	}

	public boolean getOldBoolVal()
	{
		if (redirect == null)
		{
			return oldval;
		}
		else
		{
			boolean b = redirect.getOldBoolVal();

			return b;
		}
	}

	public int getIntVal()
	{
		return 0;
	}

	public int getOldIntVal()
	{
		return 0;
	}

	public boolean isBoolean()
	{
		return true;
	}

	public void initializeDisplay()
	{
		getBoolVal();
	}
}
