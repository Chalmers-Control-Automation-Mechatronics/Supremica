
/*
 *  Supremica Software License Agreement
 *
 *  The Supremica software is not in the public domain
 *  However, it is freely available without fee for education,
 *  research, and non-profit purposes.  By obtaining copies of
 *  this and other files that comprise the Supremica software,
 *  you, the Licensee, agree to abide by the following
 *  conditions and understandings with respect to the
 *  copyrighted software:
 *
 *  The software is copyrighted in the name of Supremica,
 *  and ownership of the software remains with Supremica.
 *
 *  Permission to use, copy, and modify this software and its
 *  documentation for education, research, and non-profit
 *  purposes is hereby granted to Licensee, provided that the
 *  copyright notice, the original author's names and unit
 *  identification, and this permission notice appear on all
 *  such copies, and that no charge be made for such copies.
 *  Any entity desiring permission to incorporate this software
 *  into commercial products or to use it for commercial
 *  purposes should contact:
 *
 *  Knut Akesson (KA), knut@supremica.org
 *  Supremica,
 *  Haradsgatan 26A
 *  431 42 Molndal
 *  SWEDEN
 *
 *  to discuss license terms. No cost evaluation licenses are
 *  available.
 *
 *  Licensee may not use the name, logo, or any other symbol
 *  of Supremica nor the names of any of its employees nor
 *  any adaptation thereof in advertising or publicity
 *  pertaining to the software without specific prior written
 *  approval of the Supremica.
 *
 *  SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 *  SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 *  IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 *  Supremica or KA shall not be liable for any damages
 *  suffered by Licensee from the use of this software.
 *
 *  Supremica is owned and represented by KA.
 */
package org.supremica.automata.algorithms;

import java.util.*;
import java.io.*;
import org.supremica.automata.Alphabet;
import org.supremica.automata.Arc;
import org.supremica.automata.Automaton;
import org.supremica.automata.State;
import org.supremica.automata.LabeledEvent;

public class AutomataExtender
{
	private Automaton orgAut;
	private Automaton newAut;
	private int k = 1;
	private static int MODE_REMOVE_UNCON_TOP_EVENTS = 1;
	private static int MODE_CHANGE_UNCON_TOP_EVENTS = 2;
	private int mode = MODE_CHANGE_UNCON_TOP_EVENTS;

	public AutomataExtender(Automaton aut)
	{
		orgAut = aut;
	}

	public void setK(int k)
	{
		this.k = k;
	}

	public int getK()
	{
		return k;
	}

	public void setMode(int mode)
	{
		this.mode = mode;
	}

	public int getMode()
	{
		return mode;
	}

	public void execute()
		throws Exception
	{
		newAut = new Automaton();

		newAut.setName(orgAut.getName());

		//Alphabet orgAlphabet = orgAut.getAlphabet();
		Alphabet newAlphabet = new Alphabet(orgAut.getAlphabet());
		// String passEventId = orgAlphabet.getUniqueId(newAut.getName());
		LabeledEvent passEvent = new LabeledEvent("pass", "3.1415926");
		newAlphabet.addEvent(passEvent);

		if (mode == MODE_REMOVE_UNCON_TOP_EVENTS)
		{
			// LabeledEvent passEvent = new LabeledEvent("pass");

			passEvent.setControllable(true);
//			passEvent.setId(passEventId);
			newAlphabet.addEvent(passEvent);
		}

		newAut.getAlphabet().union(newAlphabet);

		HashMap stateMap = new HashMap(orgAut.nbrOfStates());
		int nbrOfStateCopies = Math.max(2, k + 1);

		// Add a controllable version of each uncontrollable event
		if (mode == MODE_CHANGE_UNCON_TOP_EVENTS)
		{
			LinkedList newEvents = new LinkedList();
			Iterator eventIt = newAlphabet.iterator();

			while (eventIt.hasNext())
			{
				LabeledEvent currEvent = (LabeledEvent) eventIt.next();

				if (!currEvent.isControllable())
				{
					LabeledEvent newEvent = new LabeledEvent(currEvent.getLabel(), currEvent.getLabel() + "_c");
					newEvent.setPrioritized(currEvent.isPrioritized());
					newEvent.setControllable(true);
					newEvents.add(newEvent);
				}
			}

			// add the events to the new alphabet
			eventIt = newEvents.iterator();
			while (eventIt.hasNext())
			{
				LabeledEvent currEvent = (LabeledEvent) eventIt.next();
				newAlphabet.addEvent(currEvent);
			}
		}

		// Create all states
		Iterator states = orgAut.stateIterator();

		while (states.hasNext())
		{
			State orgState = (State) states.next();
			ArrayList newStates = new ArrayList(nbrOfStateCopies);

			for (int i = 0; i < nbrOfStateCopies; i++)
			{
				State newState = new State(orgState);

				newState.setId(orgState.getId() + "_" + i);

				StringBuffer labelExt = new StringBuffer("");

				for (int j = 0; j < i; j++)
				{

					// labelExt.append("'");
					labelExt.append("p");
				}

				newState.setName(orgState.getName() + labelExt.toString());

				// Assume that we have seen zero unobservable event
				// when we start the system
				if ((i > 0) && newState.isInitial())
				{
					newState.setInitial(false);
				}

				newStates.add(newState);
				newAut.addState(newState);
			}

			stateMap.put(orgState, newStates);
		}

		// Create all transitions
		states = orgAut.stateIterator();

		while (states.hasNext())
		{
			State orgSourceState = (State) states.next();
			ArrayList newStates = (ArrayList) stateMap.get(orgSourceState);

			for (int i = 0; i < nbrOfStateCopies; i++)
			{
				State newSourceState = (State) newStates.get(i);
				Iterator outgoingArcs = orgSourceState.outgoingArcsIterator();

				while (outgoingArcs.hasNext())
				{
					Arc orgArc = (Arc) outgoingArcs.next();
					State orgDestState = orgArc.getToState();
					// LabeledEvent currEvent = orgAlphabet.getEventWithId(orgArc.getEventId());
					// BIG WARNING, Red Flag here, may be broken...
					LabeledEvent currEvent = newAlphabet.getEvent(orgArc.getEvent()); // newAlphabet.getEventWithId(orgArc.getEventId());

					if (i < k)
					{

						// Copy all transitions
						if (currEvent.isControllable())
						{

							// Add an arc to the "first" copy of orgDestState
							State newDestState = (State) ((ArrayList) stateMap.get(orgDestState)).get(0);
							// Arc newArc = new Arc(newSourceState, newDestState, currEvent.getId());
							Arc newArc = new Arc(newSourceState, newDestState, currEvent);

							newAut.addArc(newArc);
						}
						else
						{

							// Add an arc to the i + 1 copy of orgDestState
							State newDestState = (State) ((ArrayList) stateMap.get(orgDestState)).get(i + 1);
							// Arc newArc = new Arc(newSourceState, newDestState, currEvent.getId());
							Arc newArc = new Arc(newSourceState, newDestState, currEvent);

							newAut.addArc(newArc);
						}
					}
					else
					{

						// Copy only controllable transitions
						if (currEvent.isControllable())
						{

							// Add an arc to the "first" copy of orgDestState
							State newDestState = (State) ((ArrayList) stateMap.get(orgDestState)).get(0);
							// Arc newArc = new Arc(newSourceState, newDestState, currEvent.getId());
							Arc newArc = new Arc(newSourceState, newDestState, currEvent);

							newAut.addArc(newArc);
						}

						if (mode == MODE_CHANGE_UNCON_TOP_EVENTS)
						{
							if (!currEvent.isControllable())
							{
								State newDestState = (State) ((ArrayList) stateMap.get(orgDestState)).get(k);
								// Arc newArc = new Arc(newSourceState, newDestState, currEvent.getId() + "_c");
								// WARNING Red Flag, may be broken...
								// Do we know this event id (currEvent.getId() + "_c") exists? What if not?
								// It does, it was created above and added to newAlphabet
								LabeledEvent cEvent = newAlphabet.getEvent(new LabeledEvent()); // newAlphabet.getEventWithId(currEvent.getId() + "_c");
								Arc newArc = new Arc(newSourceState, newDestState, cEvent);

								newAut.addArc(newArc);
							}
						}
					}
				}

				// Add a controllable version of each uncontrollable event
				if (mode == MODE_REMOVE_UNCON_TOP_EVENTS)
				{

					// Add pass event
					if (i == nbrOfStateCopies - 1)
					{
						State newDestState = (State) newStates.get(i - 1);
						// Arc newArc = new Arc(newSourceState, newDestState, passEventId);
						// WARNING, Red Flag, may be broken...
						Arc newArc = new Arc(newSourceState, newDestState, passEvent);

						newAut.addArc(newArc);
					}
				}
			}
		}
	}

	public Automaton getNewAutomaton()
	{
		return newAut;
	}
}
