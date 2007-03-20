
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

import java.util.*;
import org.supremica.log.*;
import net.sourceforge.waters.model.des.EventProxy;

/**
 * Important note:
 * If an event label is changed after it is inserted in
 * an Events object, then rehash must be called otherwise
 * strange errors will arise.
 *
 *@author  ka
 *@since  November 28, 2001
 *@see  org.supremica.automata.AlphabetHelpers
 */
public class Alphabet
{
    private static Logger logger = LoggerFactory.createLogger(Alphabet.class);
    private int idIndex = 0;
    private Listeners listeners = null;
    private Map<String,LabeledEvent> theEvents = new TreeMap<String,LabeledEvent>();

    public Alphabet()
    {
    }

    public Alphabet(Alphabet orgAlphabet)
    {
        this(orgAlphabet, true);
    }

    public Alphabet(Alphabet orgAlphabet, boolean keepUnobservable)
    {
        this();

        for (Iterator<LabeledEvent> it = orgAlphabet.iterator(); it.hasNext(); )
        {
			LabeledEvent orgEvent = it.next();

			if (keepUnobservable || orgEvent.isObservable())
			{
            	LabeledEvent newEvent = new LabeledEvent(orgEvent);
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
        Set<EventProxy> currSet = getWatersEvents();
        currSet.add(State.acceptingProposition);
        currSet.add(State.acceptingProposition);
        return currSet;
    }

    /**
     * Return an iterator to the events.
     *
     *@return  An iterator
     */
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

    public void add(Object other)
        throws IllegalArgumentException
    {
        if (other instanceof LabeledEvent)
        {
            throw new IllegalArgumentException("other must be of type LabeledEvent");
        }
        addEvent((LabeledEvent)other);
    }
    
    /**
     * Add an event. Thorws exception if the event is null, has null label or
     * is already in the alphabet(!).
     */
    public void addEvent(LabeledEvent ev)
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

        if (contains(ev))
        {
            throw new IllegalArgumentException("getEvent: event is already in the alphabet ");
        }

        theEvents.put(ev.getLabel(), ev);
    }

    /**
     * Adds all events in another Alphabet to this Alphabet.
     * Makes sure they are not already included!
     */
    public void addEvents(Alphabet otherEvents)
    {
        for (Iterator<LabeledEvent> eventIt = otherEvents.iterator(); eventIt.hasNext(); )
        {
            LabeledEvent currEvent = eventIt.next();

            if (!contains(currEvent))
            {
                addEvent(currEvent);
            }
        }
    }

    /**
     * Removes the LabeledEvent ev from the alphabet.
     */
    public void removeEvent(LabeledEvent ev)
    throws IllegalArgumentException
    {
                /*
                if (!includes(ev))
                {
                        throw new IllegalArgumentException("The event is not included in this alphabet");
                }
                 */

        removeEvent(ev.getLabel());
    }

    private void removeEvent(String label)
    throws IllegalArgumentException
    {
        if (!contains(label))
        {
            throw new IllegalArgumentException("The event is not included in this alphabet");
        }

        theEvents.remove(label);
    }

    /**
     * True, if the event is in the set already, false otherwise.
     *
     *@param  theEvent Description of the Parameter
     *@return  Description of the Return Value
     */
        /*
        public boolean containsEvent(LabeledEvent theEvent)
        {
                return theEvents.containsValue(theEvent);
        }
         */

    /**
     * True, if there exists an event with the same label, false otherwise.
     *
     *@param  event The event with an interesting label
     */
    public boolean contains(LabeledEvent event)
    throws IllegalArgumentException
    {
        return contains(event.getLabel());
    }

    /**
     * True, if it exists an event with the label, false otherwise.
     *
     *@param  label The label of interest
     */
    public boolean contains(String label)
    throws IllegalArgumentException
    {
        if (label == null)
        {
            throw new IllegalArgumentException("Event label must be non-null");
        }

        return theEvents.containsKey(label);
    }

    /**
     * Given an event, returns an "equal" event from this alphabet
     * The def of "equal" is an internal matter.
     * Use this method instead of fiddling with event ids in user code
     * Returns null if the event does not exist
     */
        /*
        public LabeledEvent getEvent(LabeledEvent ev)
                throws IllegalArgumentException
        {
                if (ev == null)
                {
                        throw new IllegalArgumentException("getEvent: event must be non-null");
                }

                return theEvents.getEvent(ev.getLabel());
        }
         */

    /**
     * Return the event with the given label.
     * Throw an exception if it does not exist.
     *
     *@param  label Description of the Parameter
     *@return  The eventWithLabel value
     *@exception  Exception Description of the Exception
     */
    public LabeledEvent getEvent(String label)
    throws IllegalArgumentException
    {
        if (label == null)
        {
            throw new IllegalArgumentException("Event label must be non-null");
        }

        return theEvents.get(label);
    }

    public LabeledEvent getEventWithIndex(int index)
    throws IllegalArgumentException
    {
        for (Iterator<LabeledEvent> evIt = iterator(); evIt.hasNext(); )
        {
            LabeledEvent currEvent = evIt.next();

            if (currEvent.getIndex() == index)
            {
                return currEvent;
            }
        }

        throw new IllegalArgumentException("No event with index '" + index + "' exists");
    }

    /**
     * Returns the number of controllable events.
     */
    public int nbrOfControllableEvents()
    {
        int nbrOfFoundEvents = 0;

        for (Iterator<LabeledEvent> evIt = iterator(); evIt.hasNext(); )
        {
            LabeledEvent currEvent = evIt.next();
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

        for (Iterator<LabeledEvent> evIt = iterator(); evIt.hasNext(); )
        {
            LabeledEvent currEvent = evIt.next();
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

        for (Iterator<LabeledEvent> evIt = iterator(); evIt.hasNext(); )
        {
            LabeledEvent currEvent = evIt.next();

            if (!currEvent.isObservable())
            {
                nbrOfFoundEvents++;
            }
        }

        return nbrOfFoundEvents;
    }

    /**
     * Returns the number of prioritized events.
     */
    public int nbrOfPrioritizedEvents()
    {
        int nbrOfFoundEvents = 0;

        for (Iterator<LabeledEvent> evIt = iterator(); evIt.hasNext(); )
        {
            LabeledEvent currEvent = evIt.next();

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

        for (Iterator<LabeledEvent> evIt = iterator(); evIt.hasNext(); )
        {
            LabeledEvent currEvent = evIt.next();

            if (currEvent.isImmediate())
            {
                nbrOfFoundEvents++;
            }
        }

        return nbrOfFoundEvents;
    }

    public String toDebugString()
    {
        StringBuffer tmpBuf = new StringBuffer(theEvents.toString());

        return tmpBuf.toString();
    }

    public String toString()
    {
        StringBuffer sbuf = new StringBuffer("{");

        if (size() > 0)
        {
            for (Iterator<LabeledEvent> it = iterator(); it.hasNext(); )
            {
                LabeledEvent event = it.next();

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
    public Collection values()
    {
        return theEvents.values();
    }

    /**
     * Must be called after an event label is modified.
     */
    public void rehash()
    {
        Map<String,LabeledEvent> newEvents = new TreeMap<String,LabeledEvent>();

        // Deep copy
        for (Iterator it = iterator(); it.hasNext(); )
        {
            LabeledEvent currEvent = (LabeledEvent) it.next();

            newEvents.put(currEvent.getLabel(), currEvent);
        }

        theEvents.clear();

        theEvents = newEvents;
    }

    static class ControllableEventIterator
        implements Iterator<LabeledEvent>
    {
        private final Iterator theIterator;
        private final boolean controllableEvents;
        private Object nextEvent = null;

        public ControllableEventIterator(Iterator theIterator, boolean controllableEvents)
        {
            this.theIterator = theIterator;
            this.controllableEvents = controllableEvents;

            findNextEvent();
        }

        public boolean hasNext()
        {
            return nextEvent != null;
        }

        public LabeledEvent next()
        throws NoSuchElementException
        {
            if (nextEvent != null)
            {
                Object oldEvent = nextEvent;

                findNextEvent();

                return (LabeledEvent) oldEvent;
            }
            else
            {
                throw new NoSuchElementException();
            }
        }

        public void remove()
        throws UnsupportedOperationException, IllegalStateException
        {
            throw new UnsupportedOperationException();
        }

        private void findNextEvent()
        {
            while (theIterator.hasNext())
            {
                LabeledEvent currEvent = (LabeledEvent) theIterator.next();

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
    public Alphabet minus(Alphabet other)
    {
        for (Iterator<LabeledEvent> alphIt = other.iterator(); alphIt.hasNext(); )
        {
            LabeledEvent currEvent = alphIt.next();

            if (contains(currEvent))
            {
                try
                {
                    removeEvent(currEvent);

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
                catch (Exception ex)
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
    public void intersect(Alphabet other)
    {
        ArrayList removeList = new ArrayList();

        for (Iterator alphIt = this.iterator(); alphIt.hasNext(); )
        {
            LabeledEvent currEvent = (LabeledEvent) alphIt.next();

            if (!other.contains(currEvent))
            {
                removeList.add(currEvent);
            }
        }

        for (Iterator removeIt = removeList.iterator(); removeIt.hasNext(); )
        {
            LabeledEvent currEvent = (LabeledEvent) removeIt.next();

            //String currEvent = (String) removeIt.next();
            removeEvent(currEvent);
        }
    }

    /**
     * returns true if the two alphabets overlap, that is, they have at least one common event
     *
     *@param  other The other alphabet
     */
    public boolean overlap(Alphabet other)
    {
        for (Iterator alphIt = this.iterator(); alphIt.hasNext(); )
        {
            LabeledEvent currEvent = (LabeledEvent) alphIt.next();

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
    public boolean isSubsetOf(Alphabet other)
    {
        return (this.nbrOfEvents() == nbrOfCommonEvents(other));
    }

    /**
     * Computes A union B, where A is this alphabet and B is other
     *
     *@param  other The other alphabet
     */
    public void union(Alphabet other)
    {
        theEvents.putAll(other.theEvents);
    }

    /**
     * Returns the controllable part of the alphabet.
     */
    public Alphabet getControllableAlphabet()
    {
        Alphabet subAlphabet = new Alphabet();

        for (Iterator<LabeledEvent> evIt = controllableEventIterator(); evIt.hasNext(); )
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
        Alphabet subAlphabet = new Alphabet();

        for (Iterator<LabeledEvent> evIt = uncontrollableEventIterator();
        evIt.hasNext(); )
        {
            subAlphabet.addEvent(evIt.next());
        }

        return subAlphabet;
    }

    public void setIndices()
    {
        int i = 0;

        for (Iterator<LabeledEvent> evIt = iterator(); evIt.hasNext(); )
        {
            LabeledEvent currEvent = evIt.next();

            currEvent.setSynchIndex(i++);
        }
    }

    public void setIndices(Alphabet otherAlphabet)
    throws IllegalArgumentException
    {
        for (Iterator<LabeledEvent> evIt = iterator(); evIt.hasNext(); )
        {
            LabeledEvent currEvent = evIt.next();
            LabeledEvent otherEvent = otherAlphabet.getEvent(currEvent.getLabel());

            if (otherEvent == null)
            {
                throw new IllegalArgumentException("otherAlphabet must contains all events in this alphabet");
            }

            currEvent.setSynchIndex(otherEvent.getSynchIndex());
        }
    }

    public boolean isAllEventsPrioritized()
    {
        for (Iterator evIt = iterator(); evIt.hasNext(); )
        {
            LabeledEvent currEvent = (LabeledEvent) evIt.next();

            if (!currEvent.isPrioritized())
            {
                return false;
            }
        }

        return true;
    }

    public boolean isAllEventsObservable()
    {
        for (Iterator evIt = iterator(); evIt.hasNext(); )
        {
            LabeledEvent currEvent = (LabeledEvent) evIt.next();

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
    public boolean containsEqualEvent(LabeledEvent otherEvent)
    {
        return contains(otherEvent);
    }

    /**
     * Returns true if the event with same label as other event is prioritized in this alphabet.
     */
    public boolean isPrioritized(LabeledEvent otherEvent)
    throws IllegalArgumentException
    {
        if (!containsEqualEvent(otherEvent))
        {
            throw new IllegalArgumentException();
        }

        LabeledEvent thisEvent = getEvent(otherEvent.getLabel());

        return thisEvent.isPrioritized();
    }

    /**
     * Returns true if the event with same label as other event is controllable in this alphabet.
     */
    public boolean isControllable(LabeledEvent otherEvent)
    throws IllegalArgumentException
    {
        if (!containsEqualEvent(otherEvent))
        {
            throw new IllegalArgumentException();
        }

        LabeledEvent thisEvent = getEvent(otherEvent.getLabel());

        return thisEvent.isControllable();
    }

    public boolean equalAlphabet(Alphabet other)
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

    private void notifyListeners()
    {
        if (listeners != null)
        {
            listeners.notifyListeners();
        }
    }

    public int nbrOfCommonEvents(Alphabet otherAlphabet)
    {
        int nbrOfCommon = 0;
        for (Iterator<LabeledEvent> eventIterator = iterator(); eventIterator.hasNext(); )
        {
            if (otherAlphabet.contains(eventIterator.next()))
            {
                nbrOfCommon++;
            }
        }

        return nbrOfCommon;
    }

    public boolean hasCommonEvents(Alphabet otherAlphabet)
    {
        for (Iterator<LabeledEvent> eventIterator = iterator(); eventIterator.hasNext(); )
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
        Alphabet copy = new Alphabet(this);

        // Did we find any alphabetically equal names?
        boolean found = false;

        // Iterate
        for (Iterator<LabeledEvent> copyIt = copy.iterator(); copyIt.hasNext(); )
        {
            LabeledEvent eventA = copyIt.next();

            // Another iterator
            Iterator<LabeledEvent> eventIt = iterator();

            // Make sure each pair is only examined once (assumes same order in both iterations!)
            while (!eventA.equals(eventIt.next()));

            while (eventIt.hasNext())
            {
                LabeledEvent eventB = eventIt.next();

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

    public boolean equals(Object other)
    {
        if (other instanceof Alphabet)
        {
            return this.theEvents.equals(((Alphabet)other).theEvents);            
        }
        return false;
    }

    public int hashCode()
    {
        return theEvents.hashCode();
    }

    public static void main(String[] args)
    {
        Alphabet sigma1 = new Alphabet();

        sigma1.addEvent(new LabeledEvent("e1"));
        sigma1.addEvent(new LabeledEvent("e2"));

        Alphabet sigma2 = new Alphabet();

        sigma2.addEvent(new LabeledEvent("e2"));
        sigma2.addEvent(new LabeledEvent("e3"));
        sigma1.union(sigma2);
        logger.info("sigma1 + sigma2 = " + sigma1.toString());
    }
}
