
/*
 *  Supremica Software License Agreement
 *
 *  The Supremica software is not in the public domain
 *  However, it is freely available without fee for education,
 *  research, and non-profit purposes.  By obtaining copies of
 *  this and other files that comprise the Supremica software,
 *  you, the Licensee, agree to abide by the following
 *  conditions and understandings with respect to the
 *  copyrighted software:
 *
 *  The software is copyrighted in the name of Supremica,
 *  and ownership of the software remains with Supremica.
 *
 *  Permission to use, copy, and modify this software and its
 *  documentation for education, research, and non-profit
 *  purposes is hereby granted to Licensee, provided that the
 *  copyright notice, the original author's names and unit
 *  identification, and this permission notice appear on all
 *  such copies, and that no charge be made for such copies.
 *  Any entity desiring permission to incorporate this software
 *  into commercial products or to use it for commercial
 *  purposes should contact:
 *
 *  Knut Akesson (KA), knut@supremica.org
 *  Supremica,
 *  Haradsgatan 26A
 *  431 42 Molndal
 *  SWEDEN
 *
 *  to discuss license terms. No cost evaluation licenses are
 *  available.
 *
 *  Licensee may not use the name, logo, or any other symbol
 *  of Supremica nor the names of any of its employees nor
 *  any adaptation thereof in advertising or publicity
 *  pertaining to the software without specific prior written
 *  approval of the Supremica.
 *
 *  SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 *  SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 *  IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 *  Supremica or KA shall not be liable for any damages
 *  suffered by Licensee from the use of this software.
 *
 *  Supremica is owned and represented by KA.
 */
package org.supremica.automata;

import java.util.*;

import java.awt.Point;

public class State
{
	public final static int MIN_COST = 0;
	public final static int MAX_COST = Integer.MAX_VALUE;
	public final static int UNDEF_COST = -1;
	public final static int UNDEF_POS = Integer.MIN_VALUE;
	private int index = -1;

	// id is the internal identifier, i.e. for directing arcs etc.
	private String id = "";

	// name is the external identifier, i.e. the string appearing in Supremica
	private String name = "";
	private boolean initial = false;
	private boolean accepting = false;
	private boolean forbidden = false;
	private boolean active = false;
	private boolean first = false;
	private boolean last = false;
	private int cost = UNDEF_COST;
	private boolean visited = false;
	private State assocState = null;
	// private Object equivClass = null;
	private StateSet stateClass = null;	//
	private int x = UNDEF_POS;
	private int y = UNDEF_POS;
	private int radius = 9;
	private boolean selected = false;

	// private StateNode stateNode = null;
	private LinkedList incomingArcs = new LinkedList();
	private LinkedList outgoingArcs = new LinkedList();
	private List outgoingArcSets = new LinkedList();
	private Listeners listeners = null;

	public State() {}

	public State(String id)
	{
		this();

		setId(id);
		setName(id);
	}

	public State(String id, String name)
	{
		this();

		setId(id);
		setId(name);
	}

	/**
	 * This copy constructor does only copy the states attributes.
	 * The incoming and outgoing arcs are not copied.
	 *
	 *@param  otherState Description of the Parameter
	 */
	public State(State otherState)
	{
		this();

		index = otherState.index;
		id = otherState.id;
		name = otherState.name;
		initial = otherState.initial;
		accepting = otherState.accepting;
		forbidden = otherState.forbidden;
		first = otherState.first;
		last = otherState.last;
		cost = otherState.cost;
		// equivClass = otherState.equivClass;
		stateClass = otherState.stateClass;
		visited = otherState.visited;
		x = otherState.x;
		y = otherState.y;
		radius = otherState.radius;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	/**
	 * This is an ugly method that only are needed when dealing
	 * with automataIndexForm. All methods that works with index
	 * needs special initialisation that is not automatically done.
	 * This method is not recommended for general use.
	 */
	public void setIndex(int index)
	{
		this.index = index;
	}

	/**
	 * This is an ugly method that only are needed when dealing
	 * with automataIndexForm. All methods that works with index
	 * needs special initialisation that is not automatically done.
	 * This method is not recommended for general use.
	 */
	public int getIndex()
	{
		return index;
	}

	public int getSynchIndex()
	{
		return getIndex();
	}

	public boolean isFirst()
	{
		return first;
	}

	public void setFirst(boolean first)
	{
		this.first = first;
	}

	public boolean isLast()
	{
		return last;
	}

	public void setLast(boolean last)
	{
		this.last = last;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String toString()
	{
		return name;
	}

	public boolean isInitial()
	{
		return initial;
	}

	public void setInitial(boolean initial)
	{
		this.initial = initial;
	}

	public boolean isAccepting()
	{
		return accepting;
	}

	public void setAccepting(boolean accepting)
	{
		this.accepting = accepting;
	}

	public boolean isForbidden()
	{
		return forbidden;
	}

	public void setForbidden(boolean forbidden)
	{
		this.forbidden = forbidden;

		if (forbidden)
		{
			cost = State.MAX_COST;
		}
		else
		{
			cost = State.MIN_COST;
		}
	}

	public void setActive(boolean active)
	{
		this.active = active;
	}

	public boolean isActive()
	{
		return active;
	}

	/**
	 * This is only valid after setAssociatedState
	 * has been called.
	 */
	public State getAssociatedState()
	{
		return assocState;
	}

	/**
	 * Set a state as the associated state.
	 * This is used when computing the shortest
	 * trace to a state.
	 */
	public void setAssociatedState(State assocState)
	{
		this.assocState = assocState;
	}

	public void setSelected(boolean selected)
	{
		this.selected = selected;
	}

	public boolean isSelected()
	{
		return selected;
	}

	public void setXY(int x, int y)
	{
		this.x = x;
		this.y = y;
	}

	public int getX()
	{
		return x;
	}

	public int getY()
	{
		return y;
	}

	public boolean validLayout()
	{
		if (x < 0 || y < 0)
		{
			return false;
		}
		return true;
	}

	public Point getLocation()
	{
		return new Point(x, y);
	}

	public void setRadius(int radius)
	{
		this.radius = radius;
	}

	public int getRadius()
	{
		return radius;
	}

	public boolean equals(Object state)
	{
		return id.equals(((State) state).id);
	}

	public int hashCode()
	{
		return id.hashCode();
	}

	public void addIncomingArc(Arc theArc)
	{
		incomingArcs.addLast(theArc);
	}

	public void addOutgoingArc(Arc theArc)
	{
		outgoingArcs.addLast(theArc);

		ArcSet theArcSet = getArcSet(theArc);

		if (theArcSet == null)
		{

			// Did not find an arcset - generate one.
			State fromState = theArc.getToState();

			theArcSet = new ArcSet(theArc.getFromState(), theArc.getToState());

			outgoingArcSets.add(theArcSet);
		}

		theArcSet.addArc(theArc);
	}

	public void removeIncomingArc(Arc theArc)
	{
		incomingArcs.remove(theArc);
	}

	public void removeOutgoingArc(Arc theArc)
	{
		outgoingArcs.remove(theArc);

		ArcSet theArcSet = getArcSet(theArc);

		if (theArcSet != null)
		{
			outgoingArcSets.remove(theArcSet);
		}
	}

	private ArcSet getArcSet(Arc theArc)
	{
		State toState = theArc.getToState();

		for (Iterator arcSetIt = outgoingArcSets.iterator(); arcSetIt.hasNext(); )
		{
			ArcSet currArcSet = (ArcSet) arcSetIt.next();

			if (currArcSet.getToState() == toState)
			{
				return currArcSet;
			}
		}

		// Couldn't find an arcset
		return null;
	}

	public Iterator outgoingArcsIterator()
	{
		return outgoingArcs.iterator();
	}

	public Iterator outgoingArcSetIterator()
	{
		return outgoingArcSets.iterator();
	}

	public Iterator safeOutgoingArcsIterator()
	{
		return ((LinkedList) outgoingArcs.clone()).iterator();
	}

	public Iterator incomingArcsIterator()
	{
		return incomingArcs.iterator();
	}

	public int nbrOfIncomingArcs()
	{
		return incomingArcs.size();
	}

	public int nbrOfOutgoingArcs()
	{
		return outgoingArcs.size();
	}

	public int nbrOfOutgoingArcSets()
	{
		return outgoingArcSets.size();
	}

	public boolean isDeadlock()
	{
		return outgoingArcs.size() == 0;
	}

	public void setCost(int cost)
	{
		this.cost = cost;
	}

	public int getCost()
	{
		return cost;
	}

	public void setStateClass(StateSet stateClass) // setEquivalenceClass(Object equivClass)
	{
		// this.equivClass = equivClass;
		this.stateClass = stateClass;
	}

	public StateSet getStateClass() // Object getEquivalenceClass()
	{
		return stateClass; // equivClass;
	}

	public void setVisited(boolean visited)
	{
		this.visited = visited;
	}

	public boolean isVisited()
	{
		return visited;
	}

	public void removeArcs()
	{
		LinkedList outArcs = (LinkedList) outgoingArcs.clone();
		LinkedList inArcs = (LinkedList) incomingArcs.clone();
		Iterator arcIt = outArcs.iterator();

		while (arcIt.hasNext())
		{
			Arc currArc = (Arc) arcIt.next();

			currArc.clear();
		}

		arcIt = inArcs.iterator();

		while (arcIt.hasNext())
		{
			Arc currArc = (Arc) arcIt.next();

			currArc.clear();
		}

		outgoingArcs.clear();
		incomingArcs.clear();
		outArcs.clear();
		inArcs.clear();
	}

	public boolean isSafe()
	{
		return cost < MAX_COST;
	}

	/**
	 * Follow the event e and return the next state.
	 * If e is not active then return null.
	 *
	 *@param  e Description of the Parameter
	 *@return  Description of the Return Value
	 */
	public State nextState(LabeledEvent event)
	{
		Iterator outgoingArcsIt = outgoingArcs.iterator();
		// String eventId = e.getId();


		while (outgoingArcsIt.hasNext())
		{
			Arc currArc = (Arc) outgoingArcsIt.next();

			// if (currArc.getEventId().equals(eventId))
			if(currArc.getEvent().equals(event))
			{
				return currArc.getToState();
			}
		}

		return null;
	}


	public boolean contains(int x1, int y1)
	{
		int radius2 = radius * radius;
		int distance2 = (x - x1) * (x - x1) + (y - y1) * (y - y1);

		return (distance2 <= radius2);
	}

	public Listeners getListeners()
	{
		if (listeners == null)
		{
			listeners = new Listeners(this);
		}

		return listeners;
	}

	private void notifyListeners()
	{
		if (listeners != null)
		{
			listeners.notifyListeners();
		}
	}
}
