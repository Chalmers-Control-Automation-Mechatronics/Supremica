
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

import java.util.*;
import org.supremica.gui.*;
import org.supremica.log.*;

/**
 * A collection of useful methods for manipulating Event.
 *
 *@author  ka
 *@created  November 28, 2001
 *@see  org.supremica.automata.Event
 */
public class EventHelpers
{
	private static Logger logger = LoggerFactory.createLogger(EventHelpers.class);

	/**
	 * Creates a new event from a set of events
	 *
	 *@param  eventSet Description of the Parameter
	 *@param  prefix Description of the Parameter
	 *@return  Description of the Return Value
	 *@exception  IllegalArgumentException Description of the Exception
	 *@exception  Exception Description of the Exception
	 */
	public static LabeledEvent createEvent(EventsSet eventSet, String prefix)
		throws IllegalArgumentException, Exception
	{
		if (eventSet.size() <= 0)
		{
			throw new IllegalArgumentException("At least one event in the set is necessary");

			// IllegalArgumentException excp = new IllegalArgumentException("At least one event in the set is necessary");
			// excp.printStackTrace();
			// throw excp;
		}

		Iterator eventIt = eventSet.iterator();
		LabeledEvent tmpEvent = (LabeledEvent) eventIt.next();

		// Some initializations
		String id = tmpEvent.getId();
		String label = tmpEvent.getLabel();
		boolean controllable = tmpEvent.isControllable();
		boolean prioritized = tmpEvent.isPrioritized();
		boolean immediate = tmpEvent.isImmediate();

		while (eventIt.hasNext())
		{
			tmpEvent = (LabeledEvent) eventIt.next();

			if (!label.equals(tmpEvent.getLabel()))
			{
				throw new Exception("All events must have the same label");
			}

			if (controllable != tmpEvent.isControllable())
			{
				String errorMsg = "Controllability of an event must be the same in all automata. Controllability of " + label + " is not consistent.";

				throw new Exception(errorMsg);
			}

			if (immediate != tmpEvent.isImmediate())
			{
				String errorMsg = "Immediate of an event must be the same in all automata. Immediate of " + label + " is not consistent.";

				throw new Exception(errorMsg);
			}

			prioritized = prioritized || tmpEvent.isPrioritized();
		}

		LabeledEvent theEvent = new LabeledEvent(label);

		theEvent.setId(id);

		// Do I need to tweak the id???
		theEvent.setControllable(controllable);
		theEvent.setPrioritized(prioritized);
		theEvent.setImmediate(immediate);

		return theEvent;
	}
}
