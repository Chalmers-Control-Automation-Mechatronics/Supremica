
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
package org.supremica.automata.BDD;

import net.sf.javabdd.*;
import org.supremica.util.SupremicaException;
import java.util.*;
import java.io.*;
import org.supremica.automata.*;
import org.supremica.automata.IO.*;

public class BDDAutomaton
{
    static BDDFactory factory;

    Map<State, Integer> stateToIntegerMap = new HashMap<State, Integer>();
    Map<Integer, State> integerToStateMap = new HashMap<Integer, State>();


	public BDDAutomaton(Automaton automaton)
	{
		initializeFactory();

		// Build maps
		int nbrOfStates = automaton.nbrOfStates();
		int i = 0;
		int initialState = -1;
		for (State state : automaton)
		{
			if (state.isInitial())
			{
				initialState = i;
			}
			stateToIntegerMap.put(state, i);
			integerToStateMap.put(i, state);
			System.out.println(state.getName() + ":" + i);
			i++;
		}

		BDDDomain sourceStateDomain = factory.extDomain(nbrOfStates);
		BDDDomain destStateDomain = factory.extDomain(nbrOfStates);
		BDDPairing sourceToDestPairing = factory.makePair(sourceStateDomain, destStateDomain);
		BDDPairing destToSourcePairing = factory.makePair(destStateDomain, sourceStateDomain);

		BDD transitionBDD = factory.zero();

		//
		// Note that toStringWithDomains is the least significant bit to the left.
		//

		// Build <q,q+>
		for (Iterator<Arc> ait = automaton.arcIterator(); ait.hasNext(); )
		{
			Arc arc = ait.next();

			State sourceState = arc.getFromState();
			State destState = arc.getToState();
			int sourceStateIndex = stateToIntegerMap.get(sourceState);
			int destStateIndex = stateToIntegerMap.get(destState);
			System.out.println("Transition BDD. Adding transition: " + sourceStateIndex + " " + destStateIndex);
			BDD sourceStateBDD = factory.buildCube(sourceStateIndex, sourceStateDomain.vars());
			System.out.println("sourceStateBDD: " + sourceStateBDD.toStringWithDomains());

			BDD destStateBDD = factory.buildCube(destStateIndex, destStateDomain.vars());
			System.out.println("destStateBDD: " + destStateBDD.toStringWithDomains());

			sourceStateBDD.andWith(destStateBDD);
			transitionBDD.orWith(sourceStateBDD);
			System.out.println("transitionBDD: " + transitionBDD.toStringWithDomains());
		}

		BDD newStatesBDD = factory.buildCube(initialState, sourceStateDomain.vars());
		System.out.println("initialState BDD: " + newStatesBDD.toStringWithDomains());

/*
		BDD andBDD = newStatesBDD.and(transitionBDD);
		System.out.println("and: " + andBDD.toStringWithDomains());

		BDD quantBDD = andBDD.exist(sourceStateDomain.set());
		System.out.println("quant: " + quantBDD.toStringWithDomains());

		BDD replaceBDD = quantBDD.replace(destToSourcePairing);
		System.out.println("replace: " + replaceBDD.toStringWithDomains());

		BDD orBDD = replaceBDD.or(newStatesBDD);
		System.out.println("or: " + orBDD.toStringWithDomains());
*/

		BDD reachableStatesBDD = null;

		do
		{
			reachableStatesBDD = newStatesBDD.id();
			newStatesBDD = reachableStatesBDD.relprod(transitionBDD, sourceStateDomain.set());
			newStatesBDD = newStatesBDD.replace(destToSourcePairing);
			newStatesBDD = reachableStatesBDD.or(newStatesBDD);
			System.out.println("reachableStates BDD: " + newStatesBDD.toStringWithDomains());
		}
		while (!reachableStatesBDD.equals(newStatesBDD));

		System.out.println("all reachableStates BDD: " + reachableStatesBDD.toStringWithDomains());
		System.out.println("nbr of reachable states: " + reachableStatesBDD.satCount(sourceStateDomain.set()));

	}

	public void initializeFactory()
	{
		if (factory == null)
		{
			factory = BDDFactory.init("java", 1000, 1000);
		}
	}
	public int reachableStates()
	{
		return -1;
	}

	public static void main(String[] args)
		throws Exception
	{
			System.err.println("Loading: " + args[0]);

			ProjectBuildFromXml builder = new ProjectBuildFromXml();
			Project theProject = builder.build(new File(args[0]));
			Automaton spec = theProject.getAutomaton("knut");

			BDDAutomaton bddSpec = new BDDAutomaton(spec);

//			assertTrue(specCopy.nbrOfStates() == 3);
//			assertTrue(specCopy.nbrOfTransitions() == 3);
//			assertTrue(specCopy.nbrOfEvents() == 3);

	}
}
