
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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.util.*;
import com.nwoods.jgo.*;
import org.supremica.automata.*;

// AutomatonDocument, for this example, has just a few properties:
// Location and Link Pen.
// The latter two may appear to belong to a view instead of being
// part of a document, but here these attributes can be conveniently
// stored persistently.
public class AutomatonDocument
	extends JGoDocument
{

	// Event hints
	public static final int NameChanged = JGoDocumentEvent.LAST + 1;
	public static final int LocationChanged = JGoDocumentEvent.LAST + 2;

	// State
	private JGoPen myPen = JGoPen.make(JGoPen.SOLID, 2, Color.black);
	private String myLocation = "";
	private AutomatonContainer theAutomatonContainer = null;
	private Automaton theAutomaton = null;
	private boolean changed = false;
	private boolean layoutNeeded = false;

	public AutomatonDocument(AutomatonContainer theAutomatonContainer, Automaton theAutomaton)
	{
		this.theAutomatonContainer = theAutomatonContainer;
		this.theAutomaton = theAutomaton;

		build();
	}

	public String getName()
	{
		return theAutomaton.getName();
	}

	public String getLocation()
	{
		return myLocation;
	}

	public void setLocation(String newloc)
	{
		String oldLocation = getLocation();

		if (!oldLocation.equals(newloc))
		{
			myLocation = newloc;

			fireUpdate(LocationChanged, 0, null, 0, oldLocation);
		}
	}

	public StateNode newStateNode(Point p)
	{
		State newState = new State(theAutomaton.getUniqueStateId());

		theAutomaton.addState(newState);
		newState.setXY((int) p.getX(), (int) p.getY());

		return newStateNode(newState);
	}

	public StateNode newStateNode(State theState)
	{
		StateNode stateNode = new StateNode(theState);

		stateNode.initialize();
		addObjectAtTail(stateNode);

		return stateNode;
	}

	public JGoLink newLink(StateNode from, StateNode to)
	{
		JGoLink ll = new JGoLink(from.getPort(), to.getPort());

		ll.setPen(getLinkPen());
		addObjectAtHead(ll);
		ll.setArrowHeads(false, true);

		return ll;
	}

	public JGoLink newLink(StateNode from, StateNode to, ArcSet theArcSet)
	{
		JGoLabeledLink ll = new JGoLabeledLink(from.getPort(), to.getPort());

		// JGoText textLabel = new JGoText(label);
		Labels labels = null;

		try
		{
			labels = new Labels(this, theArcSet);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.err.println("AutomatonDocement: exception while constructing labels");
		}

		ll.setMidLabel(labels);
		ll.setPen(getLinkPen());
		addObjectAtHead(ll);
		ll.setArrowHeads(false, true);

		return ll;
	}

	// creating a new link between layout nodes.
	public JGoLink newLink(JGoPort from, JGoPort to)
	{
		JGoLink ll = new JGoLink(from, to);

		ll.setPen(getLinkPen());
		addObjectAtHead(ll);
		ll.setArrowHeads(false, true);

		return ll;
	}

	public JGoPen getLinkPen()
	{
		return myPen;
	}

	public void setLinkPen(JGoPen p)
	{
		if (!getLinkPen().equals(p))
		{
			myPen = p;

			// now update all links
			JGoListPosition pos = getFirstObjectPos();

			while (pos != null)
			{
				JGoObject obj = getObjectAtPos(pos);

				// only consider top-level objects
				pos = getNextObjectPosAtTop(pos);

				if (obj instanceof JGoLink)
				{
					JGoLink link = (JGoLink) obj;

					link.setPen(p);
				}
			}
		}
	}

	public StatePort pickPort(Point pointToCheck)
	{
		JGoListPosition pos = this.getLastObjectPos();

		while (pos != null)
		{
			JGoObject obj = this.getObjectAtPos(pos);

			pos = this.getPrevObjectPos(pos);

			if (obj.isVisible() && obj.isPointInObj(pointToCheck))
			{
				if (obj instanceof JGoArea)
				{

					// handle inside area
					JGoObject child = ((JGoArea) obj).pickObject(pointToCheck, false);

					if (child != null)
					{
						obj = child;
					}
				}

				if (obj instanceof StatePort)
				{
					return (StatePort) obj;
				}
			}
		}

		return null;
	}

	public boolean isChanged()
	{
		return changed;
	}

	public void setChanged(boolean b)
	{
		changed = b;
	}

	public boolean isLayoutNeeded()
	{
		return layoutNeeded;
	}

	public void setLayoutNeeded(boolean layoutNeeded)
	{
		this.layoutNeeded = layoutNeeded;
	}

	public Automaton getAutomaton()
	{
		return theAutomaton;
	}

	public void build()
	{
		HashMap stateToStateNodeMap = new HashMap(theAutomaton.nbrOfStates());

		// First add all states
		Iterator stateIt = theAutomaton.stateIterator();

		while (stateIt.hasNext())
		{
			State currState = (State) stateIt.next();
			StateNode currStateNode = newStateNode(currState);

			currStateNode.initialize();
			stateToStateNodeMap.put(currState, currStateNode);
		}

		// Then add all transitions
		stateIt = theAutomaton.stateIterator();

		while (stateIt.hasNext())
		{
			State fromState = (State) stateIt.next();
			StateNode fromStateNode = (StateNode) stateToStateNodeMap.get(fromState);
			Iterator arcSetIt = fromState.outgoingArcSetIterator();

			while (arcSetIt.hasNext())
			{
				ArcSet currArcSet = (ArcSet) arcSetIt.next();
				State toState = currArcSet.getToState();
				StateNode toStateNode = (StateNode) stateToStateNodeMap.get(toState);

				newLink(fromStateNode, toStateNode, currArcSet);
			}
		}

		setLayoutNeeded(true);

		// setLayoutNeeded(!theAutomaton.hasLayout());
	}
}
