
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
 * This is container class for storing several
 * PetriNets together.
 */
public class PetriNets
{
	private HashMap<String, PetriNet> thePetriNets = new HashMap<String, PetriNet>();

	public PetriNets() {}

	public void addPetriNet(PetriNet thePetriNet)
		throws Exception
	{
		thePetriNets.put(thePetriNet.getIdentity(), thePetriNet);
	}

	public void removePetriNet(PetriNet thePetriNet)
		throws Exception
	{
		removePetriNet(thePetriNet.getIdentity());
	}

	public void removePetriNet(String petriNetName)
		throws Exception
	{
		if (!contains(petriNetName))
		{
			throw new SupremicaException(petriNetName + " does not exists");
		}

		thePetriNets.remove(petriNetName);
	}

	public PetriNet getPetriNet(String petriNetName)
	{
		return thePetriNets.get(petriNetName);
	}

	public boolean contains(PetriNet thePetriNet)
	{
		return contains(thePetriNet.getIdentity());
	}

	public boolean contains(String petriNetName)
	{
		return thePetriNets.containsKey(petriNetName);
	}

	public Iterator<PetriNet> iterator()
	{
		return thePetriNets.values().iterator();
	}

	public static void main(String args[])
		throws Exception
	{
		PetriNets thePetriNets = new PetriNets();
		PetriNet firstPetriNet = new PetriNet("firstPetriNet");

		thePetriNets.addPetriNet(firstPetriNet);

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

		// firstPetriNet.addArc(p5, t5);
		// firstPetriNet.addArc(p6, t6);
		// Arcs from transitions to places
		firstPetriNet.addArc(t1, p2);
		firstPetriNet.addArc(t1, p3);
		firstPetriNet.addArc(t2, p4);
		firstPetriNet.addArc(t3, p5);
		firstPetriNet.addArc(t4, p6);

		// firstPetriNet.addArc(t5, p1);
		// firstPetriNet.addArc(t6, p1);
		PetriNet secondPetriNet = firstPetriNet.createCopy("secondPetriNet");

		thePetriNets.addPetriNet(secondPetriNet);

		Iterator<PetriNet> petriNetIt = thePetriNets.iterator();

		while (petriNetIt.hasNext())
		{
			PetriNet currPetriNet = petriNetIt.next();

			System.out.println(currPetriNet);
		}
	}
}
