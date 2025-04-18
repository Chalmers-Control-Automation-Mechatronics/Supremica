
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
 *  suffered by Licensee from the use of this oftware.
 *
 *  Supremica is owned and represented by KA.
 */
package org.supremica.automata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import net.sourceforge.waters.model.des.EventProxy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Important note:
 * If an event label is changed after it is inserted in
 * an Events object, then rehash must be called otherwise
 * strange errors will arise.
 *
 *@author  Knut &Aring;kesson
 *@since  November 28, 2001
 *@see  org.supremica.automata.AlphabetHelpers
 */
public class Alphabet
    implements Iterable<LabeledEvent>
{
    private static Logger logger = LogManager.getLogger(Alphabet.class);
    private int idIndex = 0;
    private Listeners listeners = null;
    private Map<String,LabeledEvent> theEvents = new TreeMap<String,LabeledEvent>();

    public Alphabet()
    {
    }

    public Alphabet(final Alphabet orgAlphabet)
    {
        this(orgAlphabet, true);
    }

    public Alphabet(final Alphabet orgAlphabet, final boolean keepUnobservable)
    {
        this();

        for (final Iterator<LabeledEvent> it = orgAlphabet.iterator(); it.hasNext(); )
        {
            final LabeledEvent orgEvent = it.next();

            if (keepUnobservable || orgEvent.isObservable())
            {
                final LabeledEvent newEvent = orgEvent.clone(); // was: new LabeledEvent(orgEvent); not polite!
                theEvents.put(newEvent.getLabel(), newEvent);
            }
        }

        idIndex = orgAlphabet.idIndex;

        rehash();
    }

    /**
     * Return the number of events.
     *
     *@return number of events
     */
    public int size()
    {
        return theEvents.size();
    }

    /**
     * Return the number of events.
     *
     *@return number of events
     */
    public int nbrOfEvents()
    {
        return size();
    }

    Set<EventProxy> getWatersEvents()
    {
        return new TreeSet<EventProxy>(this.theEvents.values());
    }

    Set<EventProxy> getWatersEventsWithPropositions()
    {
        final Set<EventProxy> currSet = getWatersEvents();
        currSet.add(State.acceptingProposition);
        currSet.add(State.forbiddenProposition);
        return currSet;
    }

    /**
     * Return an iterator to the events.
     *
     *@return  An iterator
     */
    @Override
    public Iterator<LabeledEvent> iterator()
    {
        return theEvents.values().iterator();
    }

    /**
     * Return an iterator to the controllable events.
     *
     *@return  An iterator
     */
    public Iterator<LabeledEvent> controllableEventIterator()
    {
        return new ControllableEventIterator(theEvents.values().iterator(), true);
    }

    /**
     * Return an iterator to the uncontrollable events.
     *
     *@return  An iterator
     */
    public Iterator<LabeledEvent> uncontrollableEventIterator()
    {
        return new ControllableEventIterator(theEvents.values().iterator(), false);
    }

    public void add(final LabeledEvent other)
    throws IllegalArgumentException
    {
        addEvent(other);
    }

    /**
     * Add an event. Throws exception if the event is null, has null label or
     * is already in the alphabet(!).
     */// Why does it throw exception if the event is already in the alphabet? This is a set!
    public void addEvent(final LabeledEvent ev)
    throws IllegalArgumentException
    {
        if (ev == null)
        {
            throw new IllegalArgumentException("addEvent: event mist be non-null");
        }

        if (ev.getLabel() == null)
        {
            throw new IllegalArgumentException("addEvent: event label mist be non-null");
        }

// Why does it throw exception if the event is already in the alphabet? Is this correct?
// Does all usages of addEvent have to test for containment before adding the event? It seems so.
// Can this be changed to only throw if the new event has properties different from the old one?
// MF
        if (contains(ev))
        {
            throw new IllegalArgumentException("addEvent: event is already in the alphabet ");
        }

        theEvents.put(ev.getLabel(), ev);
    }

    /**
     * Adds all events in another Alphabet to this Alphabet.
     * Makes sure they are not already included!
     */
    public void addEvents(final Alphabet otherEvents)
    {
        for (final Iterator<LabeledEvent> eventIt = otherEvents.iterator(); eventIt.hasNext(); )
        {
            final LabeledEvent currEvent = eventIt.next();

            if (!contains(currEvent))
            {
                addEvent(currEvent);
            }
        }
    }

    /**
     * Removes the LabeledEvent ev from the alphabet.
     */
    public void removeEvent(final LabeledEvent ev)
    throws IllegalArgumentException
    {
        removeEvent(ev.getLabel());
    }

    private void removeEvent(final String label)
    throws IllegalArgumentException
    {
        if (!contains(label))
        {
            throw new IllegalArgumentException("The event is not included in this alphabet");
        }

        theEvents.remove(label);
    }

    /**
     * True, if there exists an event with the same label, false otherwise.
     *
     *@param  event The event with an interesting label
     */
    public boolean contains(final LabeledEvent event)
    throws IllegalArgumentException
    {
        return contains(event.getLabel());
    }

    /**
     * True, if it exists an event with the label, false otherwise.
     *
     *@param  label The label of interest
     */
    public boolean contains(final String label)
    throws IllegalArgumentException
    {
        if (label == null)
        {
            throw new IllegalArgumentException("Event label must be non-null");
        }

        return theEvents.containsKey(label);
    }

    /**
     * Return the event with the given label.
     * Throw an exception if it does not exist.
     */
    public LabeledEvent getEvent(final String label)
    throws IllegalArgumentException
    {
        if (label == null)
        {
            throw new IllegalArgumentException("Event label must be non-null");
        }

        return theEvents.get(label);
    }

    /**
     * Returns the number of controllable events.
     */
    public int nbrOfControllableEvents()
    {
        int nbrOfFoundEvents = 0;

        for (final LabeledEvent currEvent : this)
        {
            if (currEvent.isControllable())
            {
                nbrOfFoundEvents++;
            }
        }

        return nbrOfFoundEvents;
    }

    /**
     * Returns the number of uncontrollable events.
     */
    public int nbrOfUncontrollableEvents()
    {
        int nbrOfFoundEvents = 0;

        for (final LabeledEvent currEvent : this)
        {
            if (!currEvent.isControllable())
            {
                nbrOfFoundEvents++;
            }
        }

        return nbrOfFoundEvents;
    }

    /**
     * Returns the number of unobservable events, epsilon events
     * are assumed to be unobservable
     */
    public int nbrOfUnobservableEvents()
    {
        int nbrOfFoundEvents = 0;

        for (final LabeledEvent currEvent : this)
        {
            if (currEvent.isUnobservable())
            {
                nbrOfFoundEvents++;
            }
        }

        return nbrOfFoundEvents;
    }

    /**
     * Returns  unobservable events, epsilon events
     * are assumed to be unobservable
     */

    public Alphabet getUnobservableEvents()
    {
        final Alphabet foundEvents = new Alphabet();

        for (final LabeledEvent currEvent : this)
        {
            if (currEvent.isUnobservable())
            {
                foundEvents.add(currEvent);
            }
        }

        return foundEvents;
    }

    /**
     * Returns the number of prioritized events.
     */
    public int nbrOfPrioritizedEvents()
    {
        int nbrOfFoundEvents = 0;

        for (final LabeledEvent currEvent : this)
        {
            if (currEvent.isPrioritized())
            {
                nbrOfFoundEvents++;
            }
        }

        return nbrOfFoundEvents;
    }

    /**
     * Returns the number of immediate events.
     */
    public int nbrOfImmediateEvents()
    {
        int nbrOfFoundEvents = 0;

        for (final Iterator<LabeledEvent> evIt = iterator(); evIt.hasNext(); )
        {
            final LabeledEvent currEvent = evIt.next();

            if (currEvent.isImmediate())
            {
                nbrOfFoundEvents++;
            }
        }

        return nbrOfFoundEvents;
    }

    public String toDebugString()
    {
        final StringBuilder tmpBuf = new StringBuilder(theEvents.toString());

        return tmpBuf.toString();
    }

    @Override
    public String toString()
    {
        final StringBuilder sbuf = new StringBuilder("{");

        if (size() > 0)
        {
            for (final Iterator<LabeledEvent> it = iterator(); it.hasNext(); )
            {
                final LabeledEvent event = it.next();

                sbuf.append(event);

                if (it.hasNext())
                {
                    sbuf.append(", ");
                }
            }
        }

        sbuf.append("}");
        return sbuf.toString();
    }

    /**
     * Returns collection of the events..
     *
     *@return  Description of the Return Value
     */
    public Collection<LabeledEvent> values()
    {
        return theEvents.values();
    }

    /**
     * Must be called after an event label is modified.
     */
    public void rehash()
    {
        final Map<String,LabeledEvent> newEvents = new TreeMap<String,LabeledEvent>();

        // Deep copy
        for (final LabeledEvent currEvent : this)
        {
            newEvents.put(currEvent.getLabel(), currEvent);
        }

        theEvents.clear();

        theEvents = newEvents;
    }

    static class ControllableEventIterator
        implements Iterator<LabeledEvent>
    {
        private final Iterator<LabeledEvent> theIterator;
        private final boolean controllableEvents;
        private LabeledEvent nextEvent = null;

        public ControllableEventIterator(final Iterator<LabeledEvent> theIterator, final boolean controllableEvents)
        {
            this.theIterator = theIterator;
            this.controllableEvents = controllableEvents;

            findNextEvent();
        }

        @Override
        public boolean hasNext()
        {
            return nextEvent != null;
        }

        @Override
        public LabeledEvent next()
        throws NoSuchElementException
        {
            if (nextEvent != null)
            {
                final LabeledEvent oldEvent = nextEvent;

                findNextEvent();

                return oldEvent;
            }
            else
            {
                throw new NoSuchElementException();
            }
        }

        @Override
        public void remove()
        throws UnsupportedOperationException, IllegalStateException
        {
            throw new UnsupportedOperationException();
        }

        private void findNextEvent()
        {
            while (theIterator.hasNext())
            {
                final LabeledEvent currEvent = theIterator.next();

                if (currEvent.isControllable() == controllableEvents)
                {
                    nextEvent = currEvent;

                    return;
                }
            }

            nextEvent = null;
        }
    }

    /**
     * Computes A \ B (difference) where A is this alphabet and B is other
     *
     *@param  other The other alphabet
     *@return the modified self.
     */
    public Alphabet minus(final Alphabet other)
    {
        for (final LabeledEvent event : other)
        {
            if (this.contains(event))
            {
                try
                {
                    this.removeEvent(event);

                    //  Quick check if this alphabet is almost empty
                    if ((this.size() == 1) && !other.contains(this.iterator().next()))
                    {
                        return this;
                    }
                    else if (this.size() == 0)
                    {
                        return this;
                    }
                }
                catch (final Exception ex)
                {
                    // This should be impossible
                    logger.error("Alphabet.minus. Trying to remove a non-existing event. " + ex);
                    logger.debug(ex.getStackTrace());
                }
            }
        }

        return this;
    }

    /**
     * Computes A intersection B, where A is this alphabet and B the other
     *
     *@param  other The other alphabet
     */
    public void intersect(final Alphabet other)
    {
        final List<LabeledEvent> removeList = new ArrayList<LabeledEvent>();

        for (final LabeledEvent currEvent : this)
        {
            if (!other.contains(currEvent))
            {
                removeList.add(currEvent);
            }
        }
        for (final LabeledEvent currEvent : removeList)
        {
            removeEvent(currEvent);
        }
    }

    /**
     * returns true if the two alphabets overlap, that is, they have at least one common event
     *
     *@param  other The other alphabet
     */
    public boolean overlap(final Alphabet other)
    {
        for (final LabeledEvent currEvent : this)
        {

            if (other.contains(currEvent))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns true if this alphabet is a subset of the specified alphabet.
     */
    public boolean isSubsetOf(final Alphabet other)
    {
        return (this.nbrOfEvents() == nbrOfCommonEvents(other));
    }

    /**
     * Computes A union B, where A is this alphabet and B is other
     *
     *@param  other The other alphabet
     */
    public void union(final Alphabet other)
    {
        theEvents.putAll(other.theEvents);
    }

    /**
     * Returns the controllable part of the alphabet.
     */
    public Alphabet getControllableAlphabet()
    {
        final Alphabet subAlphabet = new Alphabet();

        for (final Iterator<LabeledEvent> evIt = controllableEventIterator(); evIt.hasNext(); )
        {
            subAlphabet.addEvent(evIt.next());
        }

        return subAlphabet;
    }

    /**
     * Returns the uncontrollable part of the alphabet.
     */
    public Alphabet getUncontrollableAlphabet()
    {
        final Alphabet subAlphabet = new Alphabet();

        for (final Iterator<LabeledEvent> evIt = uncontrollableEventIterator();
        evIt.hasNext(); )
        {
            subAlphabet.addEvent(evIt.next());
        }

        return subAlphabet;
    }

    public void setIndices()
    {
        int i = 0;

        for (final Iterator<LabeledEvent> evIt = iterator(); evIt.hasNext(); )
        {
            final LabeledEvent currEvent = evIt.next();

            currEvent.setIndex(i++);
        }
    }

    public void setIndices(final Alphabet otherAlphabet)
    throws IllegalArgumentException
    {
        for (final Iterator<LabeledEvent> evIt = iterator(); evIt.hasNext(); )
        {
            final LabeledEvent currEvent = evIt.next();
            final LabeledEvent otherEvent = otherAlphabet.getEvent(currEvent.getLabel());

            if (otherEvent == null)
            {
                throw new IllegalArgumentException("otherAlphabet must contains all events in this alphabet");
            }

            currEvent.setIndex(otherEvent.getIndex());
        }
    }

    public boolean isAllEventsPrioritized()
    {
        for (final Iterator<LabeledEvent> evIt = iterator(); evIt.hasNext(); )
        {
            final LabeledEvent currEvent = evIt.next();

            if (!currEvent.isPrioritized())
            {
                return false;
            }
        }

        return true;
    }

    public boolean isAllEventsObservable()
    {
        for (final Iterator<LabeledEvent> evIt = iterator(); evIt.hasNext(); )
        {
            final LabeledEvent currEvent = evIt.next();

            if (!currEvent.isObservable())
            {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns true if this alphabet contains an event with the same label as other.
     */
    public boolean containsEqualEvent(final LabeledEvent otherEvent)
    {
        return contains(otherEvent);
    }

    /**
     * Returns true if the event with same label as other event is prioritized in this alphabet.
     */
    public boolean isPrioritized(final LabeledEvent otherEvent)
    throws IllegalArgumentException
    {
        if (!containsEqualEvent(otherEvent))
        {
            throw new IllegalArgumentException();
        }

        final LabeledEvent thisEvent = getEvent(otherEvent.getLabel());

        return thisEvent.isPrioritized();
    }

    /**
     * Returns true if the event with same label as other event is controllable in this alphabet.
     */
    public boolean isControllable(final LabeledEvent otherEvent)
    throws IllegalArgumentException
    {
        if (!containsEqualEvent(otherEvent))
        {
            throw new IllegalArgumentException();
        }

        final LabeledEvent thisEvent = getEvent(otherEvent.getLabel());

        return thisEvent.isControllable();
    }

    public boolean equalAlphabet(final Alphabet other)
    {
        if (nbrOfEvents() != other.nbrOfEvents())
        {
            //System.err.println("equalAlphabet::non equal nbr of events");
            return false;
        }

        if (nbrOfControllableEvents() != other.nbrOfControllableEvents())
        {
            //System.err.println("equalAlphabet::non equal nbr of controllable events");
            return false;
        }

        if (nbrOfPrioritizedEvents() != other.nbrOfPrioritizedEvents())
        {
            //System.err.println("equalAlphabet::non equal nbr of prioritized events");
            return false;
        }

        if (nbrOfUnobservableEvents() != other.nbrOfUnobservableEvents())
        {
            return false;
        }

        if (nbrOfImmediateEvents() != other.nbrOfImmediateEvents())
        {
            //System.err.println("equalAlphabet::non equal nbr of immediate events");
            return false;
        }

        if (nbrOfUnobservableEvents() != other.nbrOfUnobservableEvents())
        {
            //System.err.println("equalAlphabet::non equal nbr of epsilon events");
            return false;
        }

        return true;
    }

    public void clear()
    {
        theEvents.clear();
        rehash();
    }

    public Listeners getListeners()
    {
        if (listeners == null)
        {
            listeners = new Listeners(this);
        }

        return listeners;
    }

    @SuppressWarnings("unused")
	private void notifyListeners()
    {
        if (listeners != null)
        {
            listeners.notifyListeners();
        }
    }

    public int nbrOfCommonEvents(final Alphabet otherAlphabet)
    {
        int nbrOfCommon = 0;
        for (final Iterator<LabeledEvent> eventIterator = iterator(); eventIterator.hasNext(); )
        {
            if (otherAlphabet.contains(eventIterator.next()))
            {
                nbrOfCommon++;
            }
        }

        return nbrOfCommon;
    }

    public boolean hasCommonEvents(final Alphabet otherAlphabet)
    {
        for (final Iterator<LabeledEvent> eventIterator = iterator(); eventIterator.hasNext(); )
        {
            if (otherAlphabet.contains(eventIterator.next()))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns true if the alphabet has alphabetically (ignoring case) equal event names.
     */
    public boolean hasEqualEventNamesIgnoringCase()
    {
        // Make copy to make iterating easier...
        final Alphabet copy = new Alphabet(this);

        // Did we find any alphabetically equal names?
        boolean found = false;

        // Iterate
        for (final Iterator<LabeledEvent> copyIt = copy.iterator(); copyIt.hasNext(); )
        {
            final LabeledEvent eventA = copyIt.next();

            // Another iterator
            final Iterator<LabeledEvent> eventIt = iterator();

            // Make sure each pair is only examined once (assumes same order in both iterations!)
            while (!eventA.equals(eventIt.next()));

            while (eventIt.hasNext())
            {
                final LabeledEvent eventB = eventIt.next();

                // Compare names ignoring case
                if (eventA.getLabel().equalsIgnoreCase(eventB.getLabel()))
                {
                    logger.warn("The events " + eventA + " and " + eventB + " have very " +
                        "similar names but will be treated as different events!");

                    found = true;
                }
            }
        }

        return found;
    }

    @Override
    public boolean equals(final Object other)
    {
        if (other instanceof Alphabet)
        {
            return this.theEvents.equals(((Alphabet)other).theEvents);
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return theEvents.hashCode();
    }

    public static void main(final String[] args)
    {
        final Alphabet sigma1 = new Alphabet();

        sigma1.addEvent(new LabeledEvent("e1"));
        sigma1.addEvent(new LabeledEvent("e2"));

        final Alphabet sigma2 = new Alphabet();

        sigma2.addEvent(new LabeledEvent("e2"));
        sigma2.addEvent(new LabeledEvent("e3"));
        sigma1.union(sigma2);
        logger.info("sigma1 + sigma2 = " + sigma1.toString());
    }
}
