package org.jgrafchart;

import com.nwoods.jgo.*;
import java.awt.*;
import java.util.*;

public class StringVariable
	extends InternalVariable
{
	private JGoText myTag = null;
	public String val = new String("");
	public String oldval = new String("");

	public StringVariable()
	{
		super();
	}

	public StringVariable(Point loc)
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

		myValue = new JGoText("" + val);

		myValue.setSelectable(true);
		myValue.setEditable(true);
		myValue.setEditOnSingleClick(true);
		myValue.setDraggable(false);
		myValue.setAlignment(JGoText.ALIGN_LEFT);
		myValue.setTransparent(true);

		myTag = new JGoText("Str ");

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
		StringVariable newobj = (StringVariable) super.copyObject(env);

		return newobj;
	}

	public void copyChildren(JGoArea newarea, JGoCopyEnvironment env)
	{
		StringVariable newobj = (StringVariable) newarea;

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

	public boolean isBoolean()
	{
		return false;
	}

	public boolean isInteger()
	{
		return false;
	}

	public boolean isString()
	{
		return true;
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
		if (redirect == null)
		{
			return val;
		}
		else
		{
			String s = redirect.getStringVal();

			myValue.setText(s);

			return s;
		}
	}

	public String getOldStringVal()
	{
		if (redirect == null)
		{
			return oldval;
		}
		else
		{
			return redirect.getOldStringVal();
		}
	}

	public void setStoredIntAction(int n) {}

	public void setStoredStringAction(String s)
	{
		myValue.setText(s);

		if (redirect == null)
		{
			oldval = val;
			val = s;
		}
		else
		{
			redirect.setStoredStringAction(s);
		}
	}

	public void setStoredBoolAction(boolean b) {}

	public void initializeDisplay()
	{
		getStringVal();
	}
}
