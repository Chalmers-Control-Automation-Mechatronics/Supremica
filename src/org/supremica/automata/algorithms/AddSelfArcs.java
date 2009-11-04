
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
import org.supremica.automata.Alphabet;
import org.supremica.automata.Arc;
import org.supremica.automata.Automaton;
import org.supremica.automata.State;
import org.supremica.automata.LabeledEvent;

public class AddSelfArcs
{
	private AddSelfArcs() {}

	public static void execute(Automaton theAutomaton)
		throws Exception
	{
		execute(theAutomaton, false);
	}

	/**
	 *@param  theAutomaton Description of the Parameter
	 *@param  expandedSystem Description of the Parameter
	 *@exception  Exception Description of the Exception
	 */
	public static void execute(Automaton theAutomaton, boolean expandedSystem)
		throws Exception
	{
		Alphabet theAlphabet = theAutomaton.getAlphabet();
		LabeledEvent passEvent = null;

		// String passEventId = null;
		if (expandedSystem)
		{
			try
			{
				passEvent = theAlphabet.getEvent("pass");
			}
			catch (Exception ex) {}

			if (passEvent != null)
			{
				theAlphabet.removeEvent(passEvent);

				// passEventId = passEvent.getId();
			}
		}

		// Handle all states with an pass event first.
		// This to make sure that the state after the pass event is not modified.
		if (expandedSystem && (passEvent != null))
		{
			Iterator<State> stateIt = theAutomaton.stateIterator();

			while (stateIt.hasNext())
			{
				State currState = (State) stateIt.next();

//                              if (containsPassEvent(currState, theAlphabet, passEventId))
				if (containsPassEvent(currState, theAlphabet, passEvent))
				{

//                                      doPassState(currState, theAutomaton, passEventId);
					doPassState(currState, theAutomaton, passEvent);
				}
			}

			stateIt = theAutomaton.stateIterator();

			while (stateIt.hasNext())
			{
				State currState = (State) stateIt.next();

//                              if (!containsPassEvent(currState, theAlphabet, passEventId))
				if (!containsPassEvent(currState, theAlphabet, passEvent))
				{

//                                      doState(currState, theAutomaton, passEventId);
					doState(currState, theAutomaton, passEvent);
				}
			}
		}
		else
		{

			// The standard case
			Iterator<State> stateIt = theAutomaton.stateIterator();

			while (stateIt.hasNext())
			{
				State currState = (State) stateIt.next();

//                              doState(currState, theAutomaton, passEventId);
				doState(currState, theAutomaton, passEvent);
			}
		}

		if (passEvent != null)
		{
			theAlphabet.addEvent(passEvent);
		}
	}

/*
		private static void x_doState(State currState, Automaton theAutomaton, String passEventId)
				throws Exception
		{
				Alphabet currAlphabet = new Alphabet(theAutomaton.getAlphabet());

				// Remove all events that are possible from the current state
				Iterator arcIt = currState.outgoingArcsIterator();

				while (arcIt.hasNext())
				{
						Arc currArc = (Arc) arcIt.next();
						// String currEventId = currArc.getEventId();

						if ((passEventId == null) ||!passEventId.equals(currEventId))
						{
								LabeledEvent currEvent = currArc.getEvent(); // currAlphabet.getEventWithId(currEventId);

								currAlphabet.removeEvent(currEvent);
						}
				}

				// Add all remaining events as self loop to the current state
				Iterator eventIt = currAlphabet.iterator();

				while (eventIt.hasNext())
				{
						LabeledEvent currEvent = (LabeledEvent) eventIt.next();
						// Arc currArc = new Arc(currState, currState, currEvent.getId());
						Arc currArc = new Arc(currState, currState, currEvent);

						theAutomaton.addArc(currArc);
				}
		}

		private static void x_doPassState(State currState, Automaton theAutomaton, String passEventId)
				throws Exception
		{
				Alphabet currAlphabet = new Alphabet(theAutomaton.getAlphabet());

				// Remove all events that are possible from the current state
				Iterator arcIt = currState.outgoingArcsIterator();

				while (arcIt.hasNext())
				{
						Arc currArc = (Arc) arcIt.next();
						// String currEventId = currArc.getEventId();

						if ((passEventId == null) || !passEventId.equals(currEventId))
						{
								LabeledEvent currEvent = currArc.getEvent(); // currAlphabet.getEventWithId(currEventId);

								if (currEvent.getLabel().equals("pass"))
								{
										State toState = currArc.getToState();
										Iterator passArcIt = toState.outgoingArcsIterator();

										while (arcIt.hasNext())
										{
												Arc currPassArc = (Arc) passArcIt.next();
												String currPassEventId = currPassArc.getEventId();
												LabeledEvent currPassEvent = currAlphabet.getEventWithId(currEventId);

												currAlphabet.removeEvent(currPassEvent);
										}
								}
								else
								{
										currAlphabet.removeEvent(currEvent);
								}
						}
				}

				// Add all remaining events as self loop to the current state
				Iterator eventIt = currAlphabet.iterator();

				while (eventIt.hasNext())
				{
						LabeledEvent currEvent = (LabeledEvent) eventIt.next();
						// Arc currArc = new Arc(currState, currState, currEvent.getId());
						Arc currArc = new Arc(currState, currState, currEvent);

						theAutomaton.addArc(currArc);
				}
		}

		private static boolean x_containsPassEvent(State theState, Alphabet currAlphabet, String passEventId)
				throws Exception
		{
				if (passEventId == null)
				{
						return false;
				}

				Iterator arcIt = theState.outgoingArcsIterator();

				while (arcIt.hasNext())
				{
						Arc currArc = (Arc) arcIt.next();
						String currEventId = currArc.getEventId();

						if ((passEventId == null) ||!passEventId.equals(currEventId))
						{
								return true;
						}
				}

				return false;
		}
*/
	private static void doState(State currState, Automaton theAutomaton, LabeledEvent passEvent)
		throws Exception
	{
		Alphabet currAlphabet = new Alphabet(theAutomaton.getAlphabet());

		// Remove all events that are possible from the current state
		Iterator<Arc> arcIt = currState.outgoingArcsIterator();

		while (arcIt.hasNext())
		{
			Arc currArc = (Arc) arcIt.next();

			// String currEventId = currArc.getEventId();
			LabeledEvent currEvent = currArc.getEvent();

			if ((passEvent == null) ||!passEvent.equals(currEvent))
			{

				// LabeledEvent currEvent = currArc.getEvent(); // currAlphabet.getEventWithId(currEventId);
				currAlphabet.removeEvent(currEvent);
			}
		}

		// Add all remaining events as self loop to the current state
		Iterator<LabeledEvent> eventIt = currAlphabet.iterator();

		while (eventIt.hasNext())
		{
			LabeledEvent currEvent = eventIt.next();

			// Arc currArc = new Arc(currState, currState, currEvent.getId());
			Arc currArc = new Arc(currState, currState, currEvent);

			theAutomaton.addArc(currArc);
		}
	}

	private static void doPassState(State currState, Automaton theAutomaton, LabeledEvent passEvent)
		throws Exception
	{
		Alphabet currAlphabet = new Alphabet(theAutomaton.getAlphabet());

		// Remove all events that are possible from the current state
		Iterator<Arc> arcIt = currState.outgoingArcsIterator();

		while (arcIt.hasNext())
		{
			Arc currArc = (Arc) arcIt.next();

			// String currEventId = currArc.getEventId();
			LabeledEvent currEvent = currArc.getEvent();

			if ((passEvent == null) ||!passEvent.equals(currEvent))
			{

				// LabeledEvent currEvent = currArc.getEvent(); // currAlphabet.getEventWithId(currEventId);
				if (currEvent.getLabel().equals("pass"))
				{
					State toState = currArc.getToState();
					Iterator<Arc> passArcIt = toState.outgoingArcsIterator();

					while (arcIt.hasNext())
					{
						Arc currPassArc = (Arc) passArcIt.next();

						// String currPassEventId = currPassArc.getEventId();
						LabeledEvent currPassEvent = currPassArc.getEvent();    // currAlphabet.getEventWithId(currEventId);

						currAlphabet.removeEvent(currPassEvent);
					}
				}
				else
				{
					currAlphabet.removeEvent(currEvent);
				}
			}
		}

		// Add all remaining events as self loop to the current state
		Iterator<LabeledEvent> eventIt = currAlphabet.iterator();

		while (eventIt.hasNext())
		{
			LabeledEvent currEvent = (LabeledEvent) eventIt.next();

			// Arc currArc = new Arc(currState, currState, currEvent.getId());
			Arc currArc = new Arc(currState, currState, currEvent);

			theAutomaton.addArc(currArc);
		}
	}

	private static boolean containsPassEvent(State theState, Alphabet currAlpha, LabeledEvent passEvent)
	{
		if (passEvent == null)
		{
			return false;
		}

		Iterator<Arc> arcIt = theState.outgoingArcsIterator();

		while (arcIt.hasNext())
		{
			Arc currArc = arcIt.next();

			// String currEventId = currArc.getEventId();
			LabeledEvent currEvent = currArc.getEvent();

			if ( /* (passEventId == null) || */!passEvent.equals(currEvent))
			{
				return true;
			}
		}

		return false;
	}
}
