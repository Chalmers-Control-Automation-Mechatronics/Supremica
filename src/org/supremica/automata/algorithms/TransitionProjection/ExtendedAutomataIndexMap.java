
package org.supremica.automata.algorithms.TransitionProjection;

import java.util.HashMap;
import java.util.List;
import net.sourceforge.waters.model.module.*;
import org.supremica.automata.ExtendedAutomata;
import org.supremica.automata.ExtendedAutomaton;
import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;
import org.supremica.util.Args;

/**
 * ExtendedAutomataIndexMap class to index given EFA/DFA
 *
 * @author Mohammad Reza Shoaei (shoaei@chalmers.se)
 * @version %I%, %G%
 * @since 1.0
 */
public class ExtendedAutomataIndexMap {

    @SuppressWarnings("unused")
    private final Logger logger = LoggerFactory.createLogger(ExtendedAutomataIndexMap.class);

    private final HashMap<ExtendedAutomaton, Integer> exAutomatonToIndexMap;
    private final ExtendedAutomaton[] indexToExAutomatonArray;
    private final HashMap<ExtendedAutomatonAndLocationEntry, Integer> exAutomatonLocationEntryToIndexMap;
    private final HashMap<ExtendedAutomatonAndLocationIndexEntry, NodeProxy> exAutomatonLocationIndexEntryToLocationMap;
    private final HashMap<EventDeclProxy, Integer> eventToIndexMap;
    private final HashMap<SimpleExpressionProxy, Integer> gExpressionEntryToIndexMap;
    private final HashMap<Integer, SimpleExpressionProxy> gExpressionIndexEntryToExpressionMap;
    private final HashMap<BinaryExpressionProxy, Integer> aExpressionEntryToIndexMap;
    private final HashMap<Integer, BinaryExpressionProxy> aExpressionIndexEntryToExpressionMap;
    private final EventDeclProxy[] indexToEventArray;
    private final boolean hasActions[];
    private final boolean hasGuards[];

    ExtendedAutomataIndexMap(final ExtendedAutomata exAutomata) {
        // This useful variable stores the union of the extended automata events
        final List<EventDeclProxy> unionAlphabet = exAutomata.getUnionAlphabet();

        int initialLocationMapCapacity = 0;
        for (final ExtendedAutomaton currExAutomaton : exAutomata)
        {
            initialLocationMapCapacity += currExAutomaton.getNodes().size();
        }

        // The hashtables are initialized with appropriate capacities.
        exAutomatonToIndexMap = new HashMap<ExtendedAutomaton, Integer>(exAutomata.size()*2);
        indexToExAutomatonArray = new ExtendedAutomaton[exAutomata.size()];
        exAutomatonLocationEntryToIndexMap = new HashMap<ExtendedAutomatonAndLocationEntry, Integer>(initialLocationMapCapacity*2);
        exAutomatonLocationIndexEntryToLocationMap = new HashMap<ExtendedAutomatonAndLocationIndexEntry, NodeProxy>(initialLocationMapCapacity*2);
        eventToIndexMap = new HashMap<EventDeclProxy, Integer>(unionAlphabet.size()*2);
        indexToEventArray = new EventDeclProxy[unionAlphabet.size()];
        gExpressionEntryToIndexMap = new HashMap<SimpleExpressionProxy, Integer>();
        gExpressionIndexEntryToExpressionMap = new HashMap<Integer, SimpleExpressionProxy>();
        aExpressionEntryToIndexMap = new HashMap<BinaryExpressionProxy, Integer>();
        aExpressionIndexEntryToExpressionMap = new HashMap<Integer, BinaryExpressionProxy>();

        hasActions = new boolean[exAutomata.size()];
        hasGuards = new boolean[exAutomata.size()];
        // The exAutomatonIndex and the locationIndex hashmaps are filled
        int exAutomatonIndex = 0;
        int gIndex = 0;
        int aIndex = 0;
        for (final ExtendedAutomaton currExAutomaton : exAutomata)
        {
            // The exAutomatonIndex hashtable is updated
            exAutomatonToIndexMap.put(currExAutomaton, exAutomatonIndex);
            indexToExAutomatonArray[exAutomatonIndex] = currExAutomaton;
            hasGuards[exAutomatonIndex] = false;
            hasActions[exAutomatonIndex] = false;
            int locationIndex = 0;
            for (final NodeProxy currLocation : currExAutomaton.getNodes())
            {
                exAutomatonLocationEntryToIndexMap.put(new ExtendedAutomatonAndLocationEntry(currExAutomaton, currLocation), locationIndex);
                exAutomatonLocationIndexEntryToLocationMap.put(new ExtendedAutomatonAndLocationIndexEntry(currExAutomaton, locationIndex), currLocation);
                locationIndex++;
            }

            for(final SimpleExpressionProxy g : currExAutomaton.getAllGuards()){
                gExpressionEntryToIndexMap.put(g, gIndex);
                gExpressionIndexEntryToExpressionMap.put(gIndex, g);
                gIndex++;
            }

            for(final BinaryExpressionProxy a : currExAutomaton.getAllActions()){
                aExpressionEntryToIndexMap.put(a, aIndex);
                aExpressionIndexEntryToExpressionMap.put(aIndex, a);
                aIndex++;
            }

            hasGuards[exAutomatonIndex] = (gIndex > 0)?true:false;
            hasActions[exAutomatonIndex] = (aIndex > 0)?true:false;

            exAutomatonIndex++;
        }

        // The eventIndex map is filled
        int eventIndex = 0;
        for (final EventDeclProxy currEvent : unionAlphabet)
        {
            eventToIndexMap.put(currEvent, eventIndex);
            indexToEventArray[eventIndex] = currEvent;
            eventIndex++;
        }
    }


    /**
     * Returns the index corresponding to the current exAutomaton, as stored in the
     * exAutomaton index hashtable.
     *
     * @param exAutomaton the exAutomaton, whose index is requested
     * @return the index of this exAutomaton.
     */
    public int getExtendedAutomatonIndex(final ExtendedAutomaton exAutomaton)
    {
        Args.checkForNull(exAutomaton);
        return exAutomatonToIndexMap.get(exAutomaton);
    }

    public ExtendedAutomaton getExtendedAutomatonAt(final int index)
    {
        Args.checkForIndex(index);
        return indexToExAutomatonArray[index];
    }

    /**
     * Returns the index corresponding to the current event, as stored in the
     * event index hashtable.
     *
     * @param event the event, whose index is requested
     * @return the index of this event.
     */
    public int getEventIndex(final EventDeclProxy event)
    {
        Args.checkForNull(event);
        return eventToIndexMap.get(event);
    }

    public EventDeclProxy getEventAt(final int index)
    {
        Args.checkForIndex(index);
        return indexToEventArray[index];
    }

    /**
     * Returns the index corresponding to the current location, as stored in the
     * location index hashtable.
     *
     * @param exAutomaton the exAutomaton, containing the location
     * @param location the location, whose index is requested
     * @return the index of this location.
     */
    public int getLocationIndex(final ExtendedAutomaton exAutomaton, final NodeProxy location)
    {
        Args.checkForNull(exAutomaton);
        Args.checkForNull(location);
        return exAutomatonLocationEntryToIndexMap.get(new ExtendedAutomatonAndLocationEntry(exAutomaton, location));
    }

    public int getLocationIndex(final int exAutomatonIndex, final NodeProxy location)
    {
        if (exAutomatonIndex < 0)
        {
            throw new IndexOutOfBoundsException("automatonIndex has to >= 0");
        }
        final ExtendedAutomaton currExAutomaton = getExtendedAutomatonAt(exAutomatonIndex);
        return getLocationIndex(currExAutomaton, location);
    }

    public NodeProxy getLocationAt(final ExtendedAutomaton exAutomaton, final int locationIndex)
    {
        Args.checkForNull(exAutomaton);
        Args.checkForIndex(locationIndex);
        return exAutomatonLocationIndexEntryToLocationMap.get(new ExtendedAutomatonAndLocationIndexEntry(exAutomaton, locationIndex));
    }

    public NodeProxy getLocationAt(final int exAutomatonIndex, final int locationIndex)
    {
        if (exAutomatonIndex < 0)
        {
            throw new IndexOutOfBoundsException("automatonIndex has to >= 0");
        }
        if (locationIndex < 0)
        {
            throw new IndexOutOfBoundsException("stateIndex has to >= 0");
        }
        final ExtendedAutomaton currExAutomaton = getExtendedAutomatonAt(exAutomatonIndex);
        return getLocationAt(currExAutomaton, locationIndex);
    }

    public int getGuardExpressionIndex(final SimpleExpressionProxy exp)
    {
        Args.checkForNull(exp);
        return gExpressionEntryToIndexMap.get(exp);
    }

    public SimpleExpressionProxy getGuardExpressionAt(final int expIndex)
    {
        Args.checkForIndex(expIndex);
        return gExpressionIndexEntryToExpressionMap.get(expIndex);
    }

    public int getActionExpressionIndex(final BinaryExpressionProxy exp)
    {
        Args.checkForNull(exp);
        return aExpressionEntryToIndexMap.get(exp);
    }

    public BinaryExpressionProxy getActionExpressionAt(final int expIndex)
    {
        Args.checkForIndex(expIndex);
        return aExpressionIndexEntryToExpressionMap.get(expIndex);
    }

    public boolean hasAnyGuard(final int exAutomatonIndex){
        return hasGuards[exAutomatonIndex];
    }

    public boolean hasAnyAction(final int exAutomatonIndex){
        return hasActions[exAutomatonIndex];
    }

    static class ExtendedAutomatonAndLocationEntry
    {
        ExtendedAutomaton exAutomaton;
        NodeProxy location;

        public ExtendedAutomatonAndLocationEntry(final ExtendedAutomaton exAutomaton, final NodeProxy location)
        {
            this.exAutomaton = exAutomaton;
            this.location = location;
        }

        @Override
        public int hashCode()
        {
            return (exAutomaton.getName() + location.getName()).hashCode();
        }

        @Override
        public boolean equals(final Object other)
        {
            if (other instanceof ExtendedAutomatonAndLocationEntry)
            {
                return exAutomaton.getName().equals(((ExtendedAutomatonAndLocationEntry) other).exAutomaton.getName()) && location.getName().equals(((ExtendedAutomatonAndLocationEntry) other).location.getName());
            }
            return false;
        }
    }

    static class ExtendedAutomatonAndLocationIndexEntry
    {
        ExtendedAutomaton exAutomaton;
        int locationIndex;

        public ExtendedAutomatonAndLocationIndexEntry(final ExtendedAutomaton automaton, final int locationIndex)
        {
            this.exAutomaton = automaton;
            this.locationIndex = locationIndex;
        }

        @Override
        public int hashCode()
        {
            return (exAutomaton.getName().hashCode() * locationIndex);
        }

        @Override
        public boolean equals(final Object other)
        {
            if (other instanceof ExtendedAutomatonAndLocationIndexEntry)
            {
                return exAutomaton.getName().equals(((ExtendedAutomatonAndLocationIndexEntry)other).exAutomaton.getName()) && locationIndex == ((ExtendedAutomatonAndLocationIndexEntry)other).locationIndex;
            }
            return false;
        }
    }

    static class ExtendedAutomatonAndExpressionEntry
    {
        private final ExpressionProxy exp;

        public ExtendedAutomatonAndExpressionEntry(final ExpressionProxy exp)
        {
            this.exp = exp;
        }

        @Override
        public int hashCode()
        {
            return (exp.toString()).hashCode();
        }

        @Override
        public boolean equals(final Object other)
        {
            if (other instanceof ExtendedAutomatonAndExpressionEntry)
            {
                return exp.toString().equals(((ExtendedAutomatonAndExpressionEntry) other).exp.toString());
            }
            return false;
        }
    }

}
