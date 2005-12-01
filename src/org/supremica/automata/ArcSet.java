
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
 * Knarrhogsgatan 10
 * SE-431 60 MOLNDAL
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
package org.supremica.automata;

import java.util.*;

/**
 * A set of arcs with the same start state and end state.
 */
public class ArcSet
{
	private State fromState = null;
	private State toState = null;

	//private List theArcs = null;
	private Collection<Arc> theArcs = null;

	public ArcSet()
	{
		theArcs = new LinkedList<Arc>();
		//theArcs = new TreeSet(new ArcComparator());
	}

	/**
	 * In states, outgoing arcs are ordered in sets like this. So all
	 * arcs that go to a certain state are in the same set.
	 */
	public ArcSet(State from, State to)
	{
		this();

		fromState = from;
		toState = to;
	}

	public ArcSet(ArcSet other)
	{
		theArcs = new LinkedList<Arc>(other.theArcs);
		//this();
		//this.add(other);
	}

	public State getToState()
	{
		return toState;
	}

	public State getFromState()
	{
		return fromState;
	}

	public boolean isSelfLoop()
	{
		return toState.equals(fromState);
	}

	public boolean contains(Arc theArc)
	{
		for (Iterator<Arc> it = iterator(); it.hasNext(); )
		{
			if (theArc.equals(it.next()))
			{
				return true;
			}
		}
		return false;
	}

	public boolean contains(LabeledEvent event)
	{
		for (Iterator<Arc> it = iterator(); it.hasNext(); )
		{
			if (event.equals(it.next().getEvent()))
			{
				return true;
			}
		}
		return false;
	}

	private void addAll(ArcSet set)
	{
		for (Iterator<Arc> it = set.iterator(); it.hasNext(); )
		{
			this.addArc(it.next());
		}
	}

	public void add(Object obj)
	{
		addArc((Arc) obj);
	}
	public void addArc(Arc theArc)
	{
		theArcs.add(theArc);
	}

	public void removeArc(Arc theArc)
	{
		theArcs.remove(theArc);
	}

	public void clear()
	{
		theArcs.clear();
	}

	public int size()
	{
		return theArcs.size();
	}

	public Iterator<Arc> iterator()
	{
		return theArcs.iterator();
	}
}
