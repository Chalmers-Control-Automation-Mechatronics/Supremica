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

package org.supremica.petrinet;

import java.util.*;

public class Place
{
	private List prevTransitions = new LinkedList();
	private List nextTransitions = new LinkedList();

	private List nextInhibitorTransitions = new LinkedList();

	private String identity;
	private int time = Integer.MIN_VALUE;
	private int marking = 0;

	private int index = -1;

	public Place(String identity)
	{
		this.identity = identity;
	}

	/**
	 * Copy constructor that does not copy the prev and next transitions.
	 **/
	public Place(Place orgPlace)
	{
		identity = orgPlace.identity;
		time = orgPlace.time;
		marking = orgPlace.time;
	}

	public String getIdentity()
	{
		return identity;
	}

	public void setTime(int time)
	{
		this.time = time;
	}

	public int getTime()
	{
		return time;
	}

	public void setMarking(int marking)
	{
		this.marking = marking;
	}

	public int getMarking()
	{
		return marking;
	}

	void addPrevTransition(Transition transition)
	{
		prevTransitions.add(transition);
	}

	void addNextTransition(Transition transition)
	{
		nextTransitions.add(transition);
	}

	void addNextInhibitorTransition(Transition transition)
	{
		nextInhibitorTransitions.add(transition);
	}

	public Iterator nextTransitionIterator()
	{
		return nextTransitions.iterator();
	}

	public Iterator nextInhibitorTransitionIterator()
	{
		return nextInhibitorTransitions.iterator();
	}

	public Iterator prevTransitionIterator()
	{
		return prevTransitions.iterator();
	}

	public String toString()
	{
		StringBuffer sb = new StringBuffer();

		sb.append("Place: " + identity + "\n");

		sb.append("\tPrevious transitions:\n");
		Iterator transitionsIt = prevTransitions.iterator();
		while (transitionsIt.hasNext())
		{
			Transition currTransition = (Transition)transitionsIt.next();
			sb.append("\t\t" + currTransition.getIdentity() + "\n");
		}

		sb.append("\tNext transitions:\n");
		transitionsIt = nextTransitions.iterator();
		while (transitionsIt.hasNext())
		{
			Transition currTransition = (Transition)transitionsIt.next();
			sb.append("\t\t" + currTransition.getIdentity() + "\n");
		}

		return sb.toString();
	}

	void setIndex(int index)
	{
		this.index = index;
	}

	public int getIndex()
	{
		return index;
	}
}