
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

import org.supremica.util.SupremicaException;
import java.util.*;

/**
 * This is a Petri net class
 *
 * Example of usage:
 *
 *  PetriNet myPN  = new PetriNet("myPN");
 *
 *  Place p1 = new Place("p1");
 *  Place p2 = new Place("p2");
 *
 *  p1.setTime(4); // optional
 *
 *      p1.setMarking(1); // optional
 *
 *  myPN.addPlace(p1);
 *  myPN.addPlace(p2);
 *
 *      Transition t1 = new Transition("t1");
 *
 *      t1.setControllable(false); // optional
 *      t1.setLabel("a"); // optional
 *      t2.setLabel("b"); // optional
 *
 *  myPN.addTransition(t1);
 *  myPN.addTransition(t2);
 *
 *  myPN.addArc(p1, t1);
 *  myPN.addArc(t1, p2);
 **/
public class PetriNet
{
	private HashMap<String, Place> places = new HashMap<String, Place>();
	private List<Place> orderedPlaces = new LinkedList<Place>();
	private HashMap<String, Transition> transitions = new HashMap<String, Transition>();
	private List<Transition> orderedTransitions = new LinkedList<Transition>();
	private String identity;

	public PetriNet(String identity)
	{
		this.identity = identity;
	}

	public String getIdentity()
	{
		return identity;
	}

	public void setIdentity(String identity)
	{
		this.identity = identity;
	}

	public void addPlace(Place place)
		throws Exception
	{
		String id = place.getIdentity();

		if (id == null)
		{
			throw new SupremicaException("Identity must be non null");
		}

		if (places.containsKey(id))
		{
			throw new SupremicaException(id + " already exists");
		}

		places.put(id, place);
		place.setIndex(orderedPlaces.size());
		orderedPlaces.add(place);
	}

	public void addTransition(Transition transition)
		throws Exception
	{
		String id = transition.getIdentity();

		if (id == null)
		{
			throw new SupremicaException("Identity must be non null");
		}

		if (transitions.containsKey(id))
		{
			throw new SupremicaException(id + " already exists");
		}

		transitions.put(id, transition);
		orderedTransitions.add(transition);
	}

	public void addArc(Place place, Transition transition)
		throws Exception
	{
		place.addNextTransition(transition);
		transition.addPrevPlace(place);
	}

	public void addInhibitorArc(Place place, Transition transition)
		throws Exception
	{
		place.addNextInhibitorTransition(transition);
		transition.addPrevInhibitorPlace(place);
	}

	public void addArc(Transition transition, Place place)
		throws Exception
	{
		place.addPrevTransition(transition);
		transition.addNextPlace(place);
	}

	public Place getPlace(String identity)
	{
		return places.get(identity);
	}

	public Transition getTransition(String identity)
	{
		return transitions.get(identity);
	}

	public Iterator<Place> placeIterator()
	{
		return orderedPlaces.iterator();
	}

	public Iterator<Transition> transitionIterator()
	{
		return orderedTransitions.iterator();
	}

	public int nbrOfPlaces()
	{
		return orderedPlaces.size();
	}

	public int nbrOfTransitions()
	{
		return orderedTransitions.size();
	}

	/**
	 * Creates a clone, including new places and transitions,
	 * of this petrinet.
	 */
	public PetriNet createCopy(String newIdentity)
		throws Exception
	{
		PetriNet newPetriNet = new PetriNet(newIdentity);

		// Create copies of all places
		Iterator<Place> placeIt = placeIterator();

		while (placeIt.hasNext())
		{
			Place currPlace = placeIt.next();
			Place newPlace = new Place(currPlace);

			newPetriNet.addPlace(newPlace);
		}

		// Create copies of all transitions
		Iterator<Transition> transitionIt = transitionIterator();

		while (transitionIt.hasNext())
		{
			Transition currTransition = transitionIt.next();
			Transition newTransition = new Transition(currTransition);

			newPetriNet.addTransition(newTransition);
		}

		// Create copies of all arcs
		// Each element is responsible for creating its outgoing arcs
		// Start with creating all outgoing arcs from the operations
		placeIt = placeIterator();

		while (placeIt.hasNext())
		{
			Place orgPlace = placeIt.next();
			Place newPlace = newPetriNet.getPlace(orgPlace.getIdentity());

			transitionIt = orgPlace.nextTransitionIterator();

			while (transitionIt.hasNext())
			{
				Transition orgTransition = transitionIt.next();
				Transition newTransition = newPetriNet.getTransition(orgTransition.getIdentity());

				newPetriNet.addArc(newPlace, newTransition);
			}
		}

		// Now create all outgoing arcs from the transitions
		transitionIt = transitionIterator();

		while (transitionIt.hasNext())
		{
			Transition orgTransition = transitionIt.next();
			Transition newTransition = newPetriNet.getTransition(orgTransition.getIdentity());

			placeIt = orgTransition.nextPlaceIterator();

			while (placeIt.hasNext())
			{
				Place orgPlace = placeIt.next();
				Place newPlace = newPetriNet.getPlace(orgPlace.getIdentity());

				newPetriNet.addArc(newTransition, newPlace);
			}
		}

		return newPetriNet;
	}

	public boolean hasInhibitorArcs()
	{
		Iterator<Transition> transitionIt = transitionIterator();

		while (transitionIt.hasNext())
		{
			Transition currTransition = transitionIt.next();

			if (currTransition.hasInhibitorArc())
			{
				return true;
			}
		}

		return false;
	}

	public String toString()
	{
		StringBuffer sb = new StringBuffer();

		sb.append("PetriNet: " + identity + "\n");

		Iterator<Place> placeIt = placeIterator();

		while (placeIt.hasNext())
		{
			Place currPlace = placeIt.next();

			sb.append(currPlace);
		}

		Iterator<Transition> transitionIt = transitionIterator();

		while (transitionIt.hasNext())
		{
			Transition currTransition = transitionIt.next();

			sb.append(currTransition);
		}

		return sb.toString();
	}
}
