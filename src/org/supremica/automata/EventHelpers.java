
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
package org.supremica.automata;

import java.util.Iterator;
import java.util.Set;


/**
 * A collection of useful methods for manipulating events.
 *
 *@author  Knut &Aring;kesson
 *@since  November 28, 2001
 *@see  org.supremica.automata.LabeledEvent
 */
public class EventHelpers
{
//	/**
//	 * Creates a new event from a set of events
//	 *
//	 * @param  eventSet Description of the Parameter
//	 * @return  Description of the Return Value
//	 * @exception  IllegalArgumentException Description of the Exception
//	 * @exception  Exception Description of the Exception
//	 */
//	private static LabeledEvent createEvent(EventsSet eventSet)
//		throws IllegalArgumentException, Exception
//	{
//		return createEvent(eventSet, true, true);
//	}

	/**
	 * Creates a new event from a set of events
	 *
	 *@param  eventSet Description of the Parameter
	 *@return  Description of the Return Value
	 */
	static LabeledEvent createEvent(final Set<LabeledEvent> eventSet, final boolean requireConsistentControllability,
									final boolean requireConsistentImmediate)
	{
		if (eventSet.size() <= 0)
		{
			throw new IllegalArgumentException("At least one event in the set is necessary");
		}

		final Iterator<LabeledEvent> eventIt = eventSet.iterator();
		final LabeledEvent firstEvent = eventIt.next();

		// Some initializations
		// This function (createEvent) is only used by AlphabetHelpers::getUnionAlphabet
		// That function (getUnionAlphabet) manages the id by itself, so avoiding id-fddlng
		// here would seem to be safe
		//              String id = firstEvent.getId();
		final String label = firstEvent.getLabel();
		final boolean controllable = firstEvent.isControllable();
		boolean prioritized = firstEvent.isPrioritized();
		final boolean operatorIncrease = firstEvent.isOperatorIncrease();
		final boolean operatorReset = firstEvent.isOperatorReset();
		final boolean observable = firstEvent.isObservable();
		final boolean immediate = firstEvent.isImmediate();

		while (eventIt.hasNext())
		{
			final LabeledEvent tmpEvent = eventIt.next();

			if (!label.equals(tmpEvent.getLabel()))
			{
				throw new IllegalArgumentException("All events must have the same label");
			}

			if (requireConsistentControllability && (controllable != tmpEvent.isControllable()))
			{
				final String errorMsg = "Controllability of an event must be the same in all automata. " +
					"Controllability of " + label + " is not consistent.";

				throw new IllegalArgumentException(errorMsg);
			}

			if (requireConsistentImmediate && (immediate != tmpEvent.isImmediate()))
			{
				final String errorMsg = "Immediate of an event must be the same in all automata. " +
					"Immediate of " + label + " is not consistent.";

				throw new IllegalArgumentException(errorMsg);
			}

			if (operatorIncrease != tmpEvent.isOperatorIncrease())
			{
				final String errorMsg = "OperatorIncrease of an event must be the same in all automata. " +
					"Operator of " + label + " is not consistent.";

				throw new IllegalArgumentException(errorMsg);
			}

			if (operatorReset != tmpEvent.isOperatorReset())
			{
				final String errorMsg = "OperatorReset of an event must be the same in all automata. " +
					"Operator of " + label + " is not consistent.";

				throw new IllegalArgumentException(errorMsg);
			}

			if (observable != tmpEvent.isObservable())
			{
				final String errorMsg = "Observability of an event must be the same in all automata. " +
					"Observability of " + label + " is not consistent.";

				throw new IllegalArgumentException(errorMsg);
			}

			prioritized = prioritized || tmpEvent.isPrioritized();
		}

		/*
		LabeledEvent theEvent = new LabeledEvent(label);    // , id);

		// theEvent.setId(id);
		// Do I need to tweak the id???
		theEvent.setControllable(controllable);
		theEvent.setPrioritized(prioritized);
		theEvent.setImmediate(immediate);
		theEvent.setOperatorIncrease(operatorIncrease);
		theEvent.setOperatorReset(operatorReset);
		theEvent.setObservable(observable);
		*/

		// The new event should be a copy, except for the prioritized status, that we may change!
		// The above construction was bound to give problems when something new was introduced!
		final LabeledEvent theEvent = firstEvent.clone(); // was: new LabeledEvent(firstEvent); not polite
		theEvent.setIndex(-1);
		theEvent.setPrioritized(prioritized);

		return theEvent;
	}
}
