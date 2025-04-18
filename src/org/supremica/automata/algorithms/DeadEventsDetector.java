
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

import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.automata.Alphabet;
import org.supremica.automata.Arc;
import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.LabeledEvent;


/**
 * Finds all events in the alphabet that does not belong to a transition
 */
public class DeadEventsDetector
{
    public DeadEventsDetector(final Automaton automaton)
    {
        mAutomaton = automaton;
    }

    public DeadEventsDetector(final Automata automata)
    {
		final AutomataSynchronizer synchronizer = new AutomataSynchronizer(automata, new SynchronizationOptions(), false);
       	synchronizer.execute();
       	mAutomaton = synchronizer.getAutomaton();
    }


    public void execute()
    {

        final Alphabet deadEvents = new Alphabet(mAutomaton.getAlphabet());

        for (final Iterator<Arc> arcIt = mAutomaton.arcIterator(); arcIt.hasNext(); )
        {
            final Arc currArc = arcIt.next();

			final LabeledEvent currEvent = currArc.getEvent();
			if (deadEvents.contains(currEvent))
			{
				deadEvents.removeEvent(currEvent);
				if (deadEvents.size() == 0)
				{
					break;
				}
			}
        }

		if (deadEvents.size() == 0)
		{
			mLogger.info("No dead events");
		}
		else
		{
			mLogger.info("The following events are dead");
			for (final Iterator<LabeledEvent> it = deadEvents.iterator(); it.hasNext(); )
			{
				final LabeledEvent event = it.next();
				mLogger.info(event.getLabel());
			}
		}
    }


    private final Automaton mAutomaton;
    private static Logger mLogger = LogManager.getLogger(DeadEventsDetector.class);
}
