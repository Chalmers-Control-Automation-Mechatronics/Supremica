
/*
 * Test of JGo for Grafchart.
 */
package org.jgrafchart;



import com.nwoods.jgo.*;

import java.awt.*;


/**
 * A Step is an Area containing a Rectangle and two Ports and
 * an optional Text Label.
 */
public class GCStepInitial
	extends GCStep
{

	/**
	 * A newly constructed GCStep is not usable until you've
	 * called initialize().
	 */
	public GCStepInitial()
	{
		super();
	}

	public GCStepInitial(Point loc, String labeltext)
	{

		super(loc, labeltext);

		// create the inner rectangle
		innerRectangle = new JGoRectangle();

		innerRectangle.setPen(new JGoPen(JGoPen.SOLID, 2, new Color(0.0F, 0.0F, 0.0F)));
		innerRectangle.setSize(50, 50);
		innerRectangle.setSelectable(false);
		innerRectangle.setDraggable(false);
		addObjectAtHead(innerRectangle);
		bringObjectToFront(myToken);
		layoutChildren();
	}

	public JGoObject copyObject(JGoCopyEnvironment env)
	{

		GCStepInitial newobj = (GCStepInitial) super.copyObject(env);
		JGoEllipse myToken = newobj.getToken();

		if (myToken != null)
		{
			newobj.setBrush(JGoBrush.Null);
		}

		return newobj;
	}

	public void copyChildren(JGoArea newarea, JGoCopyEnvironment env)
	{

		super.copyChildren(newarea, env);

		GCStepInitial newobj = (GCStepInitial) newarea;

		if (innerRectangle != null)
		{
			newobj.innerRectangle = (JGoRectangle) innerRectangle.copyObject(env);

			newobj.addObjectAtHead(newobj.innerRectangle);
		}
	}

	public void layoutChildren()
	{

		super.layoutChildren();

		if (innerRectangle != null)
		{
			innerRectangle.setSpotLocation(Center, myRectangle, Center);
		}
	}

	public void geometryChange(Rectangle prevRect)
	{		// Cannot scale this step yet!
		super.geometryChange(prevRect);
	}

	public JGoRectangle getInner()
	{
		return innerRectangle;
	}

	protected JGoRectangle innerRectangle = null;
}
