
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
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Helper methods for processing alphabets.
 */

public class AlphabetHelpers
{
    private static Logger logger = LogManager.getLogger(AlphabetHelpers.class);

    public static Alphabet getUnionAlphabet(final Automata theAutomata)
    {
        return getUnionAlphabet(theAutomata, true, true);
    }

    public static Alphabet getUnionAlphabet(final Automata theAutomata, final boolean requireConsistentControllability, final boolean requireConsistentImmediate)
    {
        final Set<Alphabet> alphabets = new LinkedHashSet<Alphabet>();

        for (final Automaton currAutomaton : theAutomata)
        {
        	alphabets.add(currAutomaton.getAlphabet());
        }

        return getUnionAlphabet(alphabets, requireConsistentControllability, requireConsistentImmediate);
    }

    public static Alphabet getUnionAlphabet(final Set<Alphabet> alphabets)
    throws IllegalArgumentException, Exception
    {
        return getUnionAlphabet(alphabets, true, true);
    }

    /**
     * Compute a new alphabet as the union of a set of alphabets.
     * Adjust the Event attributes properly.
     */
    private static Alphabet getUnionAlphabet(final Set<Alphabet> alphabets, final boolean requireConsistentControllability, final boolean requireConsistentImmediate)
    {
        if (alphabets.size() < 1)
        {
            throw new IllegalArgumentException("At least one alphabet is necessary");
        }

        final Alphabet unionEvents = AlphabetHelpers.union(alphabets);
        final Alphabet newAlphabet = new Alphabet();

        // Iterate over all events - check consistency and add one for each label
        for (final LabeledEvent currEvent : unionEvents)
        {
        	final Set<LabeledEvent> eventsWithSameLabel = new LinkedHashSet<LabeledEvent>();

            // Iterate over all alphabets, and find those alphabets that
            // contain an event with currEvent.getLabel
            for (final Alphabet currAlphabet : alphabets)
            {
                if (currAlphabet.contains(currEvent.getLabel()))
                {
                    eventsWithSameLabel.add(currAlphabet.getEvent(currEvent.getLabel()));
                }
            }

            final LabeledEvent newEvent = EventHelpers.createEvent(eventsWithSameLabel, requireConsistentControllability, requireConsistentImmediate);

            // If we get here, the events are consistent (or consistency is not to be checked)
            // newEvent.setId(newAlphabet.getUniqueId(idPrefix));
            newAlphabet.addEvent(newEvent);
        }

        return newAlphabet;
    }

    /**
     * Computes the union of all events in eventsSet.
     * No manipulation of the events.
     *
     *@param  alphabets Description of the Parameter
     *@return  Description of the Return Value
     *@exception  IllegalArgumentException Description of the Exception
     */
    private static Alphabet union(final Set<Alphabet> alphabets)
    {
        if (alphabets.size() >= 1)
        {
            // this was >= 2 but why could we not have union over 1 or even 0 number of elements??
            // Build the new set of events
            final Iterator<Alphabet> eventsSetIt = alphabets.iterator();
            final TreeSet<LabeledEvent> tmpEvents = new TreeSet<LabeledEvent>(eventsSetIt.next().values());

            while (eventsSetIt.hasNext())
            {
                tmpEvents.addAll(eventsSetIt.next().values());
            }

            // Add all events to an Alphabet
            final Iterator<LabeledEvent> eventIt = tmpEvents.iterator();
            final Alphabet theEvents = new Alphabet();
            while (eventIt.hasNext())
            {
                theEvents.addEvent(eventIt.next());
            }

            return theEvents;
        }

        // at least 1 (not two ::MF) arguments are necessary
        throw new IllegalArgumentException("Not enough elements of events");
    }

    /**
     * Builds an returns a map mapping events to sets of automata.
     * (LabeledEvent) &rarr; (Automata).
     */
    public static EventToAutomataMap buildEventToAutomataMap(final Automata automata)
    {
        //HashMap map = new HashMap();
        final EventToAutomataMap map = new EventToAutomataMap();

        // Loop over automata
        for (final Automaton automaton : automata)
        {
            // Loop over alphabet
            for (final LabeledEvent event : automaton.getAlphabet())
            {
                // Insert in map if observable
                if (event.isObservable())
                    map.insert(event, automaton);
            }
        }

        return map;
    }

    /**
     *
     * Builds an returns a map mapping uncontrollable events to sets automata.
     * (uncontrollable LabeledEvent) &rarr; (Set of Automata-objects).
     */
    public static EventToAutomataMap buildUncontrollableEventToAutomataMap(final Automata automata)
    {
        //HashMap map = new HashMap();
        final EventToAutomataMap map = new EventToAutomataMap();

        // Loop over automata
        for (final Automaton automaton : automata)
        {
            // Loop over alphabet
            for (final LabeledEvent event : automaton.getAlphabet())
            {
                if (!event.isControllable())
                {
                    // Insert in map
                    map.insert(event, automaton);
                }
            }
        }

        return map;
    }

    /**
     * Computes and returns "a1 minus a2"
     */
    public static Alphabet minus(final Alphabet a1, final Alphabet a2)
    {
        final Alphabet result = new Alphabet(a1);

        result.minus(a2);

        return result;
    }

    /**
     * Computes and returns "a1 intersection a2"
     */
    public static Alphabet intersect(final Alphabet a1, final Alphabet a2)
    {
        final Alphabet result = new Alphabet(a1);

        result.intersect(a2);

        return result;
    }

    /**
     * Computes and returns "a1 union a2"
     */
    public static Alphabet union(final Alphabet a1, final Alphabet a2)
    {
        final Alphabet result = new Alphabet(a1);

        result.union(a2);

        return result;
    }

    /**
     * Returns true if there are no events with alphabetically equal names, i.e.
     * lowercase equal.
     *
     * Also warns if there are events that start or end with blank spaces.
     */
    public static boolean isEventNamesSafe(final Alphabet alphabet)
    {
        // Check that there are no spaces in the start or end of event names!
        for (final Iterator<LabeledEvent> evIt = alphabet.iterator(); evIt.hasNext(); )
        {
            final LabeledEvent event = evIt.next();
            if (event.getLabel().startsWith(" ") || event.getLabel().endsWith(" "))
            {
                logger.warn("The event " + event + " has spaces in the beginning or end of its label. This is not recommended.");
            }
        }

        // Chech that no event names have the same alphabetic value
        return !alphabet.hasEqualEventNamesIgnoringCase();
    }


}
