
/*
 * Supremica Software License Agreement
 *
 * The Supremica software is not in the public domain
 * However, it is freely available without fee for education,
 * research, and non-profit purposes.  By obtaining copies of
 * this and other files that comprise the Supremica software,
 * you, the Licensee, agree to abide by the following
 * conditions and understandings with respect to the
 * copyrighted software:
 *
 * The software is copyrighted in the name of Supremica,
 * and ownership of the software remains with Supremica.
 *
 * Permission to use, copy, and modify this software and its
 * documentation for education, research, and non-profit
 * purposes is hereby granted to Licensee, provided that the
 * copyright notice, the original author's names and unit
 * identification, and this permission notice appear on all
 * such copies, and that no charge be made for such copies.
 * Any entity desiring permission to incorporate this software
 * into commercial products or to use it for commercial
 * purposes should contact:
 *
 * Knut Akesson (KA), knut@supremica.org
 * Supremica,
 * Haradsgatan 26A
 * 431 42 Molndal
 * SWEDEN
 *
 * to discuss license terms. No cost evaluation licenses are
 * available.
 *
 * Licensee may not use the name, logo, or any other symbol
 * of Supremica nor the names of any of its employees nor
 * any adaptation thereof in advertising or publicity
 * pertaining to the software without specific prior written
 * approval of the Supremica.
 *
 * SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 * SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 * IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 * Supremica or KA shall not be liable for any damages
 * suffered by Licensee from the use of this software.
 *
 * Supremica is owned and represented by KA.
 */
package org.supremica.gui.editor;

import com.nwoods.jgo.*;
import java.awt.*;
import org.supremica.automata.*;

public class StateNode
	extends JGoArea
{
	static int myStdSize = 20;

	// State
	protected JGoEllipse myEllipse = null;
	protected JGoText myLabel = null;
	protected StatePort thePort = null;
	protected State theState = null;

	/**
	* A newly constructed StateNode is not usable until you've
	* called initialize().
	*/
	public StateNode(State theState)
	{
		super();

		this.theState = theState;
	}

	public JGoObject copyObject(JGoCopyEnvironment env)
	{
		StateNode newobj = (StateNode) super.copyObject(env);

		if (newobj != null)
		{

			// the JGoObjects that are part of this area are copied
			// separately by copyChildren()
			// no other fields to copy
		}

		return newobj;
	}

	// Fix how to copy a state !??
	public void copyChildren(JGoArea newarea, JGoCopyEnvironment env)
	{

		// don't bother calling JGoArea's default implementation,
		// so that we can set our fields explicitly
		StateNode newobj = (StateNode) newarea;

		if (myEllipse != null)
		{
			newobj.myEllipse = (JGoEllipse) myEllipse.copyObject(env);

			newobj.addObjectAtHead(newobj.myEllipse);
		}

		if (myLabel != null)
		{
			newobj.myLabel = (JGoText) myLabel.copyObject(env);

			newobj.addObjectAtTail(newobj.myLabel);
		}

		if (theState != null)
		{
			newobj.theState = new State(theState);

			// newobj.theState.setId(); // New id is needed
		}

		Point c = newobj.myEllipse.getSpotLocation(Center);
		StatePort op = thePort;
		StatePort np = (StatePort) op.copyObject(env);

		newobj.thePort = np;

		newobj.addObjectAtTail(np);

		np.myEllipse = newobj.myEllipse;
	}

	/**
	* Keep the parts of a StateNode positioned relative to each other
	* by setting their locations using some of the standard spots of
	* a JGoObject.
	* <p>
	* By default the label will be positioned at the bottom of the node,
	* above the ellipse.  To change this to be below the ellipse, at
	* the bottom of the node, change the myLabel.setSpotLocation() call.
	*/
	public void layoutChildren()
	{
		if (myEllipse == null)
		{
			return;
		}

		if (myLabel != null)
		{

			// put the label above the node
			myLabel.setSpotLocation(TopCenter, myEllipse, BottomCenter);

			// put the label below the node
			// myLabel.setSpotLocation(TopCenter, myEllipse, BottomCenter);
		}

		if (thePort != null)
		{
			Point c = myEllipse.getSpotLocation(Center);

			thePort.setSpotLocation(Center, c.x, c.y);
		}
	}

	public void initialize()
	{
		if (theState == null)
		{
			System.err.println("State in StateNode is null");

			return;
		}

		setSize(myStdSize, myStdSize);

		// the area as a whole is not directly selectable using a mouse,
		// but the area can be selected by trying to select any of its
		// children, all of whom are currently !isSelectable().
		setSelectable(false);
		setGrabChildSelection(true);

		// the user can move this node around
		setDraggable(true);

		// the user cannot resize this node
		setResizable(false);

		// create the bigger circle/ellipse around and behind the port
		myEllipse = new JGoEllipse(getTopLeft(), getSize());

		myEllipse.setSelectable(false);
		myEllipse.setDraggable(false);

		// can't setLocation until myEllipse exists
		myEllipse.setSpotLocation(JGoObject.Center, theState.getLocation());

		// if there is a string, create a label with a transparent
		// background that is centered
		String labeltext = theState.getName();

		if (labeltext == null)
		{
			labeltext = theState.getId();
		}

		/*
		 *               if (labeltext != null)
		 *               {
		 *                       myLabel = new JGoText(labeltext);
		 *                       myLabel.setSelectable(false);
		 *                       myLabel.setDraggable(false);
		 *                       myLabel.setAlignment(JGoText.ALIGN_CENTER);
		 *                       myLabel.setTransparent(true);
		 *               }
		 */

		// create a Port, which knows how to make sure
		// connected JGoLinks have a reasonable end point
		thePort = new StatePort();
		thePort.myEllipse = myEllipse;

		thePort.setSize(8, 8);

		// add all the children to the area
		addObjectAtHead(myEllipse);

		if (myLabel != null)
		{
			addObjectAtTail(myLabel);
		}

		addObjectAtTail(thePort);

		// now position the label and port appropriately
		// relative to the ellipse
		layoutChildren();
		setBrush(JGoBrush.white);
		setColor(Color.red);
	}

	public String getToolTipText()
	{
		String stateId = theState.getId();

		if (stateId == null)
		{
			return null;
		}

		String stateName = theState.getName();

		if ((stateName == null) || stateName.equals(""))
		{
			return "Id: \"" + stateId + "\"";
		}

		return "Id: \"" + stateId + "\" Name: \"" + stateName + "\"";
	}

	public State getState()
	{
		return theState;
	}

	// Convenience methods: control the ellipse's pen and brush
	public JGoPen getPen()
	{
		return myEllipse.getPen();
	}

	public void setPen(JGoPen p)
	{
		myEllipse.setPen(p);
	}

	public JGoBrush getBrush()
	{
		return myEllipse.getBrush();
	}

	public void setBrush(JGoBrush b)
	{
		myEllipse.setBrush(b);
	}

	public Color getColor()
	{
		return getPen().getColor();
	}

	public void setColor(Color c)
	{
		setPen(JGoPen.makeStockPen(c));
	}

	/*
	 *       public void colorChange()
	 *       {
	 *               Color c = getColor();
	 *               if (c == Color.red)
	 *               {
	 *                       setColor(Color.green);
	 *               }
	 *               else if (c == Color.green)
	 *               {
	 *                       setColor(Color.blue);
	 *               }
	 *               else if (c == Color.blue)
	 *               {
	 *                       setColor(Color.red);
	 *               }
	 *       }
	 */
	public JGoPort getPort()
	{
		return thePort;
	}

	static int getStdSize()
	{
		return myStdSize;
	}

	static void setStdSize(int size)
	{
		myStdSize = size;
	}

	public JGoObject getEllipse()
	{
		return myEllipse;
	}

	public JGoText getLabel()
	{
		return myLabel;
	}
}
