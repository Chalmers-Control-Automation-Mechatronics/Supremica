
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
package org.supremica.petrinet.algorithms;

import org.supremica.util.SupremicaException;
import java.util.*;
import java.io.*;
import org.supremica.petrinet.*;

public class PetriNetToDsx
{
	private PetriNet pn;

	public PetriNetToDsx(PetriNet pn)
		throws Exception
	{
		this.pn = pn;

		if (pn.hasInhibitorArcs())
		{
			throw new SupremicaException("PetriNets with inhibitor arcs are not supported");
		}
	}

	public void serialize(PrintWriter pw)
		throws Exception
	{
		pw.println("PETRINET;");
		pw.println("number of places: " + pn.nbrOfPlaces());
		pw.println("number of transitions: " + pn.nbrOfTransitions());

		// Print all places
		Iterator places = pn.placeIterator();

		while (places.hasNext())
		{
			Place place = (Place) places.next();

			pw.print(place.getIdentity());

			int currTime = place.getTime();

			if (currTime != Integer.MIN_VALUE)
			{
				pw.print("(" + currTime + ")");
			}

			int currMarking = place.getMarking();

			if (currMarking != 0)
			{
				pw.print("." + currMarking);
			}

			if (places.hasNext())
			{
				pw.print(", ");
			}
			else
			{
				pw.println(":");
			}
		}

		// Print all transitions
		Iterator transitionIt = pn.transitionIterator();

		while (transitionIt.hasNext())
		{
			Transition transition = (Transition) transitionIt.next();

			pw.print(transition.getIdentity());

			if (transition.getLabel() != null)
			{
				pw.print(".");

				if (!transition.isControllable())
				{
					pw.print("!");
				}

				pw.print(transition.getLabel());
			}

			if (transitionIt.hasNext())
			{
				pw.print(", ");
			}
			else
			{
				pw.println(":");
			}
		}

		// Print structure
		transitionIt = pn.transitionIterator();

		while (transitionIt.hasNext())
		{
			Transition currTransition = (Transition) transitionIt.next();

			pw.print(currTransition.getIdentity() + ": ");

			// Print prev places
			Iterator placeIt = currTransition.prevPlaceIterator();

			while (placeIt.hasNext())
			{
				Place currPlace = (Place) placeIt.next();

				pw.print(currPlace.getIdentity());

				if (placeIt.hasNext())
				{
					pw.print(", ");
				}
			}

			pw.print(": ");

			// Print next places
			placeIt = currTransition.nextPlaceIterator();

			while (placeIt.hasNext())
			{
				Place currPlace = (Place) placeIt.next();

				pw.print(currPlace.getIdentity());

				if (placeIt.hasNext())
				{
					pw.print(", ");
				}
			}

			pw.println("");
		}

		pw.flush();
	}

	public void serialize(String fileName)
		throws Exception
	{
		serialize(new PrintWriter(new FileWriter(fileName)));
	}

	public void serialize(OutputStream theStream)
		throws Exception
	{
		serialize(new PrintWriter(theStream));
	}

	public static void main(String args[])
		throws Exception
	{
		PetriNet firstPetriNet = new PetriNet("firstPetriNet");
		Place p1 = new Place("p1");
		Place p2 = new Place("p2");
		Place p3 = new Place("p3");
		Place p4 = new Place("p4");
		Place p5 = new Place("p5");
		Place p6 = new Place("p6");

		p1.setTime(4);
		p4.setTime(3);
		p1.setMarking(1);
		firstPetriNet.addPlace(p1);
		firstPetriNet.addPlace(p2);
		firstPetriNet.addPlace(p3);
		firstPetriNet.addPlace(p4);
		firstPetriNet.addPlace(p5);
		firstPetriNet.addPlace(p6);

		Transition t1 = new Transition("t1");
		Transition t2 = new Transition("t2");
		Transition t3 = new Transition("t3");
		Transition t4 = new Transition("t4");
		Transition t5 = new Transition("t5");
		Transition t6 = new Transition("t6");

		t2.setControllable(false);
		t2.setLabel("a");
		t3.setLabel("b");
		firstPetriNet.addTransition(t1);
		firstPetriNet.addTransition(t2);
		firstPetriNet.addTransition(t3);
		firstPetriNet.addTransition(t4);
		firstPetriNet.addTransition(t5);
		firstPetriNet.addTransition(t6);

		// Arcs from places to transitions
		firstPetriNet.addArc(p1, t1);
		firstPetriNet.addArc(p2, t2);
		firstPetriNet.addArc(p3, t2);
		firstPetriNet.addArc(p4, t3);
		firstPetriNet.addArc(p4, t4);
		firstPetriNet.addArc(p5, t5);
		firstPetriNet.addArc(p6, t6);

		// Arcs from transitions to places
		firstPetriNet.addArc(t1, p2);
		firstPetriNet.addArc(t1, p3);
		firstPetriNet.addArc(t2, p4);
		firstPetriNet.addArc(t3, p5);
		firstPetriNet.addArc(t4, p6);
		firstPetriNet.addArc(t5, p1);
		firstPetriNet.addArc(t6, p1);

		PetriNetToDsx exporter = new PetriNetToDsx(firstPetriNet);

		exporter.serialize(System.out);
	}
}
