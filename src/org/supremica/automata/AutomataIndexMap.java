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

public final class AutomataIndexMap
{
    private final Map<Automaton, Integer> automatonToIndexMap;
    private final Automaton[] indexToAutomatonArray;    
    private final Map<AutomatonAndStateEntry, Integer> automatonStateEntryToIndexMap;
    private final Map<AutomatonAndIntegerEntry, State> automatonIntegerEntryToStateMap;
    private final Map<LabeledEvent, Integer> eventToIndexMap;
    private final LabeledEvent[] indexToEventArray;
    
    private static Logger logger = LoggerFactory.createLogger(AutomataIndexMap.class);
    
    public AutomataIndexMap(final Automata theAutomata)
    {
        // This useful variable stores the union of the automata events
        Alphabet unionAlphabet = theAutomata.getUnionAlphabet();
        
        int initialStateMapCapacity = 0;
        for (Automaton currAutomaton : theAutomata)
        {
            initialStateMapCapacity += currAutomaton.nbrOfStates();
        }
 
        // The hashtables are initialized with appropriate capacities.
        automatonToIndexMap = new HashMap<Automaton, Integer>(theAutomata.size()*2);
        indexToAutomatonArray = new Automaton[theAutomata.size()];
        automatonStateEntryToIndexMap = new HashMap<AutomatonAndStateEntry, Integer>(initialStateMapCapacity*2);
        automatonIntegerEntryToStateMap = new HashMap<AutomatonAndIntegerEntry, State>(initialStateMapCapacity*2);
        eventToIndexMap = new HashMap<LabeledEvent, Integer>(unionAlphabet.size()*2);
        indexToEventArray = new LabeledEvent[unionAlphabet.size()];
        
        // The automatonIndex and the stateIndex hashmaps are filled
        int automatonIndex = 0;
        for (Automaton currAutomaton : theAutomata)
        {   
            // The automatonIndex hashtable is updated
            automatonToIndexMap.put(currAutomaton, automatonIndex);
            indexToAutomatonArray[automatonIndex] = currAutomaton;
            automatonIndex++;
            
            int stateIndex = 0;
            for (State currState : currAutomaton)
            {
                automatonStateEntryToIndexMap.put(new AutomatonAndStateEntry(currAutomaton, currState), stateIndex);
                automatonIntegerEntryToStateMap.put(new AutomatonAndIntegerEntry(currAutomaton, stateIndex), currState);
                stateIndex++;
            }
        }
        
        // The eventIndex map is filled
        int eventIndex = 0;
        for (LabeledEvent currEvent : unionAlphabet)
        {
            eventToIndexMap.put(currEvent, eventIndex);
            indexToEventArray[eventIndex] = currEvent;
            eventIndex++;
        }
    }
    
    /**
     * Returns the index corresponding to the current automaton, as stored in the
     * automaton index hashtable.
     *
     * @param automaton the automaton, whose index is requested
     * @return the index of this automaton.
     */
    public int getAutomatonIndex(final Automaton automaton)
    {
        if (automaton == null)
        {
            throw new IllegalArgumentException("automaton has to be non-null");
        }
        return automatonToIndexMap.get(automaton);
    }
    
    public Automaton getAutomatonAt(final int index)
    {
        if (index < 0)
        {
            throw new IndexOutOfBoundsException("index has to >= 0");
        }
        return indexToAutomatonArray[index];
    }
      
    /**
     * Returns the index corresponding to the current event, as stored in the
     * event index hashtable.
     *
     * @param event the event, whose index is requested
     * @return the index of this event.
     */
    public int getEventIndex(final LabeledEvent event)
    {
         if (event == null)
        {
            throw new IllegalArgumentException("event has to be non-null");
        }
        return eventToIndexMap.get(event);
    }
    
    public LabeledEvent getEvent(final int index)
    {
        if (index < 0)
        {
            throw new IndexOutOfBoundsException("index has to >= 0");
        }
        return indexToEventArray[index];
    }
    
    /**
     * Returns the index corresponding to the current state, as stored in the
     * state index hashtable.
     *
     * @param automaton the automaton, containing the state
     * @param state the state, whose index is requested
     * @return the index of this state.
     */
    public int getStateIndex(final Automaton automaton, final State state)
    {
        if (automaton == null)
        {
            throw new IllegalArgumentException("automaton has to be non-null");
        }
        if (state == null)
        {
            throw new IllegalArgumentException("state has to be non-null");
        }        
        return automatonStateEntryToIndexMap.get(new AutomatonAndStateEntry(automaton, state));
    }
    
    public State getStateAt(final Automaton automaton, final int stateIndex)
    {
        if (automaton == null)
        {
            throw new IllegalArgumentException("automaton has to be non-null");
        }
        if (stateIndex < 0)
        {
            throw new IndexOutOfBoundsException("index has to >= 0");
        }
        return automatonIntegerEntryToStateMap.get(new AutomatonAndIntegerEntry(automaton, stateIndex));
    }
    
    public State getStateAt(final int automatonIndex, final int stateIndex)
    {
        if (automatonIndex < 0)
        {
            throw new IndexOutOfBoundsException("automatonIndex has to >= 0");
        }
        if (stateIndex < 0)
        {
            throw new IndexOutOfBoundsException("stateIndex has to >= 0");
        }        
        Automaton currAutomaton = getAutomatonAt(automatonIndex);
        return getStateAt(currAutomaton, stateIndex);
    }
    
    static class AutomatonAndStateEntry
    {
        Automaton automaton;
        State state;
        
        public AutomatonAndStateEntry(final Automaton automaton, final State state)
        {
            this.automaton = automaton;
            this.state = state;
        }

        public int hashCode()
        {
            return (automaton.getName() + state.getName()).hashCode();
        }
        
        public boolean equals(Object other)
        {
            if (other instanceof AutomatonAndStateEntry)
            {
                return automaton.getName().equals(((AutomatonAndStateEntry) other).automaton.getName()) && state.getName().equals(((AutomatonAndStateEntry) other).state.getName());
            }
            return false;
        }
    }

    static class AutomatonAndIntegerEntry
    {
        Automaton automaton;
        int stateIndex;
        
        public AutomatonAndIntegerEntry(Automaton automaton, int stateIndex)
        {
            this.automaton = automaton;
            this.stateIndex = stateIndex;
        }

        public int hashCode()
        {
            return (automaton.getName().hashCode() * stateIndex);
        }
        
        public boolean equals(Object other)
        {
            if (other instanceof AutomatonAndIntegerEntry)
            {
                return automaton.getName().equals(((AutomatonAndIntegerEntry)other).automaton.getName()) && stateIndex == ((AutomatonAndIntegerEntry)other).stateIndex;
            }
            return false;
        }
    }
}
