
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
import java.util.*;
import org.supremica.automata.*;

public class BDDAutomaton
{
    Automaton theAutomaton;
    BDDManager manager;
	BDDDomain sourceStateDomain;
	BDDDomain destStateDomain;
	BDDPairing sourceToDestPairing;
	BDDPairing destToSourcePairing;
	BDD transitionForwardBDD;
	BDD transitionBackwardBDD;


	public BDDAutomaton(BDDManager manager, Automaton theAutomaton, BDDDomain sourceStateDomain, BDDDomain destStateDomain)
	{
		this.manager = manager;

		this.theAutomaton = theAutomaton;

		this.sourceStateDomain = sourceStateDomain;
		this.destStateDomain = destStateDomain;

		sourceToDestPairing = manager.makePair(sourceStateDomain, destStateDomain);
		destToSourcePairing = manager.makePair(destStateDomain, sourceStateDomain);

		transitionForwardBDD = manager.zero();
		transitionBackwardBDD = manager.zero();
	}

	public void initialize()
	{
		BDD initialStates = manager.zero();

		BDD markedStates = manager.zero();
		BDD forbiddenStates = manager.zero();

		Alphabet inverseAlphabet = manager.getInverseAlphabet(theAutomaton);

		for (State currState : theAutomaton)
		{
			// First create all transitions in this automaton
			for (Iterator<Arc> arcIt = currState.outgoingArcsIterator(); arcIt.hasNext(); )
			{
				Arc currArc = arcIt.next();
				addTransition(currArc);
			}

			// Self loop events not in this alphabet
			for (LabeledEvent event : inverseAlphabet)
			{
				addTransition(currState, currState, event);
			}

			// Then add state properties
			int stateIndex = manager.getStateIndex(theAutomaton, currState);
			if (currState.isInitial())
			{
				manager.addState(initialStates, stateIndex, sourceStateDomain);
			}
			if (currState.isAccepting())
			{
				manager.addState(markedStates, stateIndex, sourceStateDomain);
			}
			if (currState.isForbidden())
			{
				manager.addState(forbiddenStates, stateIndex, sourceStateDomain);
			}
		}

		manager.addInitialStates(initialStates);
		manager.addMarkedStates(markedStates);
		manager.addForbiddenStates(forbiddenStates);
	}

	void addTransition(Arc theArc)
	{
		State sourceState = theArc.getSource();
		State destState = theArc.getTarget();
		LabeledEvent theEvent = theArc.getEvent();

		addTransition(sourceState, destState, theEvent);
	}

	void addTransition(State sourceState, State destState, LabeledEvent theEvent)
	{
		int sourceStateIndex = manager.getStateIndex(theAutomaton, sourceState);
		int destStateIndex = manager.getStateIndex(theAutomaton, destState);
		int eventIndex = manager.getEventIndex(theEvent);
		manager.addTransition(transitionForwardBDD, sourceStateIndex, sourceStateDomain, destStateIndex, destStateDomain, eventIndex, manager.getEventDomain());
		manager.addTransition(transitionBackwardBDD, destStateIndex, sourceStateDomain, sourceStateIndex, destStateDomain, eventIndex, manager.getEventDomain());
	}


	public int hashCode()
	{
		return theAutomaton.hashCode();
	}

	public boolean equals(Object other)
	{
		return theAutomaton.equals(other);
	}

	public BDDPairing getSourceToDestPairing()
	{
		return sourceToDestPairing;
	}

	public BDDPairing getDestToSourcePairing()
	{
		return destToSourcePairing;
	}

	public BDD getTransitionForwardBDD()
	{
		return transitionForwardBDD;
	}

	public BDD getTransitionBackwardBDD()
	{
		return transitionBackwardBDD;
	}

	public BDD getTransitionForwardConjunctiveBDD()
	{
		return getTransitionForwardBDD();
	}

	public BDD getTransitionBackwardConjunctiveBDD()
	{
		return getTransitionBackwardBDD();
	}

	public BDD getTransitionForwardDisjunctiveBDD()
	{
		return null;
	}

	public BDD getTransitionBackwardDisjunctiveBDD()
	{
		return null;
	}

}
