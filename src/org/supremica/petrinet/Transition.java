
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
package org.supremica.petrinet;

import java.util.*;

public class Transition
{
	private List<Place> prevPlaces = new LinkedList<Place>();
	private List<Place> nextPlaces = new LinkedList<Place>();
	private List<Place> prevInhibitorPlaces = new LinkedList<Place>();
	private String identity;
	private String label;
	boolean controllable = true;

	public Transition(String identity)
	{
		this(identity, null);
	}

	public Transition(String identity, String label)
	{
		this.identity = identity;
		this.label = label;
	}

	public Transition(Transition orgTransition)
	{
		identity = orgTransition.identity;
		label = orgTransition.label;
		controllable = orgTransition.controllable;
	}

	public void setIdentity(String identity)
	{
		this.identity = identity;
	}

	public String getIdentity()
	{
		return identity;
	}

	public void setLabel(String label)
	{
		this.label = label;
	}

	public String getLabel()
	{
		return label;
	}

	public void setControllable(boolean controllable)
	{
		this.controllable = controllable;
	}

	public boolean isControllable()
	{
		return controllable;
	}

	void addPrevPlace(Place place)
	{
		prevPlaces.add(place);
	}

	void addPrevInhibitorPlace(Place place)
	{
		prevInhibitorPlaces.add(place);
	}

	void addNextPlace(Place place)
	{
		nextPlaces.add(place);
	}

	public Iterator<Place> nextPlaceIterator()
	{
		return nextPlaces.iterator();
	}

	public Iterator<Place> prevPlaceIterator()
	{
		return prevPlaces.iterator();
	}

	public Iterator<Place> prevInhibitorPlaceIterator()
	{
		return prevInhibitorPlaces.iterator();
	}

	public boolean hasInhibitorArc()
	{
		return prevInhibitorPlaces.size() > 0;
	}

	public String toString()
	{
		StringBuffer sb = new StringBuffer();

		sb.append("Transition: " + identity + "\n");
		sb.append("\tPrevious places:\n");

		Iterator<Place> placeIt = prevPlaces.iterator();

		while (placeIt.hasNext())
		{
			Place currPlace = placeIt.next();

			sb.append("\t\t" + currPlace.getIdentity() + "\n");
		}

		sb.append("\tNext places:\n");

		placeIt = nextPlaces.iterator();

		while (placeIt.hasNext())
		{
			Place currPlace = placeIt.next();

			sb.append("\t\t" + currPlace.getIdentity() + "\n");
		}

		sb.append("\tPrev inhibitor places:\n");

		placeIt = prevInhibitorPlaces.iterator();

		while (placeIt.hasNext())
		{
			Place currPlace = placeIt.next();

			sb.append("\t\t" + currPlace.getIdentity() + "\n");
		}

		return sb.toString();
	}
}
