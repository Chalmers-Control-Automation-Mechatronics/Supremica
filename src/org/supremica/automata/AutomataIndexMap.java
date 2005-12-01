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

import org.supremica.log.*;

import java.util.*;

public class AutomataIndexMap 
{
	private Hashtable<String, Integer> automatonMap;
	private Hashtable<String, Integer> stateMap;
	private Hashtable<String, Integer> eventMap;
	
	private static Logger logger = LoggerFactory.createLogger(AutomataIndexMap.class);
	
	public AutomataIndexMap(Automata theAutomata)
	{
		// This useful variable stores the union of the automata events
		Alphabet unionAlphabet = theAutomata.getUnionAlphabet();

		// The initial capacity should be bigger than the number of objects divided by the load factor (which is default 0.75)
		int initialAutomatonMapCapacity = theAutomata.nbrOfAutomata()*4/3 + 1;

		int initialStateMapCapacity = 0;
		for (Iterator<Automaton> autoIter = theAutomata.iterator(); autoIter.hasNext(); )
		{
			initialStateMapCapacity += autoIter.next().nbrOfStates();
		}
		initialStateMapCapacity = initialStateMapCapacity*4/3 + 1;
			
		int initialEventMapCapacity = unionAlphabet.nbrOfEvents()*4/3 + 1;
			
		// The hashtables are initialized with appropriate capacities. 
		automatonMap = new Hashtable<String, Integer>(initialAutomatonMapCapacity);
		stateMap = new Hashtable<String, Integer>(initialStateMapCapacity);
		eventMap = new Hashtable<String, Integer>(initialEventMapCapacity);

		// The automatonIndex and the stateIndex hashtables are filled
		int automatonIndex = 0;
		int stateIndex = 0;
		for (Iterator<Automaton> autoIter = theAutomata.iterator(); autoIter.hasNext(); )
		{
			Automaton currAuto = autoIter.next();
			String currAutoName = currAuto.getName();

			// The automatonIndex hashtable is updated
			automatonMap.put(currAutoName, new Integer(automatonIndex++));

			for (Iterator<State> stateIter = currAuto.stateIterator(); stateIter.hasNext(); )
			{
				// The stateIndex hashtable is updated
				stateMap.put(currAutoName + "_" + stateIter.next().getName(), new Integer(stateIndex++));
			}
		}

		// The eventIndex hashtable is filled
		int eventIndex = 0;
		for (Iterator<LabeledEvent> eventIter = unionAlphabet.iterator(); eventIter.hasNext(); )
		{
			eventMap.put(eventIter.next().getLabel(), new Integer(eventIndex++));
		}
	}

	/**
	 * Returns the index corresponding to the current automaton, as stored in the 
	 * automaton index hashtable.
	 *
	 * @param the automaton, whose index is requested
	 * @return the index of this automaton.
	 */
	public int getAutomatonIndex(Automaton automaton)
	{
		return automatonMap.get(automaton.getName()).intValue();
	}

	/**
	 * Returns the index corresponding to the current event, as stored in the 
	 * event index hashtable.
	 *
	 * @param the event, whose index is requested
	 * @return the index of this event.
	 */
	public int getEventIndex(LabeledEvent event)
	{
		return eventMap.get(event.getLabel()).intValue();
	}

	/**
	 * Returns the index corresponding to the current event, as stored in the 
	 * event index hashtable.
	 *
	 * @param the event, whose index is requested
	 * @return the index of this event.
	 */
	public int getStateIndex(Automaton automaton, State state)
	{
		return stateMap.get(automaton.getName() + "_" + state.getName()).intValue();
	}
}