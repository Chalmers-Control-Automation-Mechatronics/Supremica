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

package org.supremica.automata.algorithms;

import org.supremica.automata.*;
import java.util.*;

public class AlphabetNormalize
{
	private Automaton theAutomaton;
	private HashMap labelMap;

	public AlphabetNormalize(Automaton theAutomaton)
	{
		this.theAutomaton = theAutomaton;
	}

	public void execute()
		throws Exception
	{
		labelMap = new HashMap();

		Alphabet theAlphabet = theAutomaton.getAlphabet();
		Iterator eventIt = theAlphabet.eventIterator();
		while (eventIt.hasNext())
		{
			Event currEvent = (Event)eventIt.next();
			LinkedList currList = (LinkedList)labelMap.get(currEvent.getLabel());
			if (currList == null)
			{
				LinkedList newList = new LinkedList();
				newList.add(currEvent);
				labelMap.put(currEvent.getLabel(), newList);
			}
			else
			{
				currList.add(currEvent);
			}
		}

//		printLabelMap(labelMap);
		removeControllable(labelMap);
//		printLabelMap(labelMap);

		Iterator arcIt = theAutomaton.arcIterator();
		while (arcIt.hasNext())
		{
			Arc currArc = (Arc)arcIt.next();
			String eventId = currArc.getEventId();
			Event currEvent = theAlphabet.getEventWithId(eventId);
			String currLabel = currEvent.getLabel();
			LinkedList currList = (LinkedList)labelMap.get(currLabel);
			Event firstEvent = (Event)currList.getFirst();
			currArc.setEvent(firstEvent.getId());
		}
	}

	public void removeControllable(Map labelMap)
	{
		Set entrySet = labelMap.entrySet();

		Iterator entryIt = entrySet.iterator();
		while (entryIt.hasNext())
		{
			Map.Entry currEntry = (Map.Entry)entryIt.next();
			//String label = (String)currEntry.getKey();
			LinkedList list = (LinkedList)currEntry.getValue();
			if (list.size() > 1)
			{
				//System.err.println(label);
				ListIterator eventIt = list.listIterator();
				while (eventIt.hasNext())
				{
					Event currEvent = (Event)eventIt.next();
					if (currEvent.isControllable())
					{
						eventIt.remove();
					}
					//System.err.println("   " + currEvent.getId() + "   " + currEvent.getLabel());
				}
			}
		}
	}

	public void printLabelMap(Map labelMap)
	{
		System.err.println("***********");
		Set entrySet = labelMap.entrySet();

		Iterator entryIt = entrySet.iterator();
		while (entryIt.hasNext())
		{
			Map.Entry currEntry = (Map.Entry)entryIt.next();
			String label = (String)currEntry.getKey();
			List list = (List)currEntry.getValue();
			System.err.println(label);
			Iterator eventIt = list.iterator();
			while (eventIt.hasNext())
			{
				Event currEvent = (Event)eventIt.next();
				System.err.println("   " + currEvent.getId() + "   " +  (currEvent.isControllable() ? "" : "!") + currEvent.getLabel());
			}
		}
	}
}
