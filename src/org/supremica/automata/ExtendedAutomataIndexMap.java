package org.supremica.automata;

/**
 *
 * @author sajed
 */

import org.supremica.log.*;
import org.supremica.util.Args;
import java.util.*;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;

public class ExtendedAutomataIndexMap {

    private final Map<ExtendedAutomaton, Integer> exAutomatonToIndexMap;
    private final Map<String,ExtendedAutomaton> nameToExAutomatonMap;
    private final ExtendedAutomaton[] indexToExAutomatonArray;
    private final Map<ExtendedAutomatonAndLocationEntry, Integer> automatonLocationEntryToIndexMap;
    private final Map<ExtendedAutomatonAndIntegerEntry, NodeProxy> automatonIntegerEntryToLocationMap;
    private final Map<EventDeclProxy, Integer> eventToIndexMap;
    private final EventDeclProxy[] indexToEventArray;
    private final Map<VariableComponentProxy, Integer> variableToIndexMap;
    public final Map<String, Integer> variableStringToIndexMap;
    private final Map<Integer, VariableComponentProxy> indexToVariableMap;

    @SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.createLogger(AutomataIndexMap.class);

    public ExtendedAutomataIndexMap(ExtendedAutomata theExAutomata)
    {
        // This useful variable stores the union of the automata events
        List<EventDeclProxy> unionAlphabet = theExAutomata.getUnionAlphabet();

        int initialLocationMapCapacity = 0;
  /*      for (AbstractSubject currExAutomaton : theExAutomata)
        {
            initialLocationMapCapacity += currExAutomaton.nbrOfLocations();
        }
*/
        // The hashtables are initialized with appropriate capacities.
        exAutomatonToIndexMap = new HashMap<ExtendedAutomaton, Integer>(theExAutomata.size()*2);
        nameToExAutomatonMap = new HashMap<String, ExtendedAutomaton>(theExAutomata.size());
        indexToExAutomatonArray = new ExtendedAutomaton[theExAutomata.size()];
        automatonLocationEntryToIndexMap = new HashMap<ExtendedAutomatonAndLocationEntry, Integer>(initialLocationMapCapacity*2);
        automatonIntegerEntryToLocationMap = new HashMap<ExtendedAutomatonAndIntegerEntry, NodeProxy>(initialLocationMapCapacity*2);
        eventToIndexMap = new HashMap<EventDeclProxy, Integer>(unionAlphabet.size()*2);
        indexToEventArray = new EventDeclProxy[unionAlphabet.size()];

        variableToIndexMap = new HashMap<VariableComponentProxy, Integer>(theExAutomata.getVars().size());
        indexToVariableMap = new HashMap<Integer,VariableComponentProxy>(theExAutomata.getVars().size());

        variableStringToIndexMap = new HashMap<String, Integer>(theExAutomata.getVars().size());
        // The automatonIndex and the locationIndex hashmaps are filled
        int automatonIndex = 0;
        for (ExtendedAutomaton currExAutomaton : theExAutomata)
        {
            // The automatonIndex hashtable is updated
            exAutomatonToIndexMap.put(currExAutomaton, automatonIndex);
            indexToExAutomatonArray[automatonIndex] = currExAutomaton;
            nameToExAutomatonMap.put(currExAutomaton.getName(), currExAutomaton);
            automatonIndex++;

            int locationIndex = 0;
            for (NodeProxy currNode : currExAutomaton.getNodes())
            {
                automatonLocationEntryToIndexMap.put(new ExtendedAutomatonAndLocationEntry(currExAutomaton, currNode), locationIndex);
                automatonIntegerEntryToLocationMap.put(new ExtendedAutomatonAndIntegerEntry(currExAutomaton, locationIndex), currNode);
                locationIndex++;
            }
        }

        int variableIndex = 0;
        for(VariableComponentProxy var:theExAutomata.getVars())
        {
            indexToVariableMap.put(variableIndex, var);
            variableToIndexMap.put(var, variableIndex);
            variableStringToIndexMap.put(var.getName(),variableIndex);
            variableIndex++;
        }

        // The eventIndex map is filled
        int eventIndex = 0;
        for (EventDeclProxy currEvent : unionAlphabet)
        {
            eventToIndexMap.put(currEvent, eventIndex);
            indexToEventArray[eventIndex] = currEvent;
            eventIndex++;
        }
    }

    public int getVariableIndex(VariableComponentProxy var)
    {
        return variableToIndexMap.get(var);
    }

    public VariableComponentProxy getVariableAt(int i)
    {
        return indexToVariableMap.get(i);
    }

    public int getExAutomatonIndex(final ExtendedAutomaton exAutomaton)
    {
        Args.checkForNull(exAutomaton);
        return exAutomatonToIndexMap.get(exAutomaton);
    }

    public ExtendedAutomaton getExAutomatonAt(final int index)
    {
        Args.checkForIndex(index);
        return indexToExAutomatonArray[index];
    }

    public ExtendedAutomaton getExAutomatonWithName(final String name)
    {
        Args.checkForNull(name);
        return nameToExAutomatonMap.get(name);
    }

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


    public int getLocationIndex(final ExtendedAutomaton automaton, final NodeProxy location)
    {
        Args.checkForNull(automaton);
        Args.checkForNull(location);
        return automatonLocationEntryToIndexMap.get(new ExtendedAutomatonAndLocationEntry(automaton, location));
    }

    public int getLocationIndex(final int automatonIndex, final NodeProxy location)
    {
        if (automatonIndex < 0)
        {
            throw new IndexOutOfBoundsException("automatonIndex has to >= 0");
        }
        ExtendedAutomaton currAutomaton = getAutomatonAt(automatonIndex);
        return getLocationIndex(currAutomaton, location);
    }

    public NodeProxy getLocationAt(final ExtendedAutomaton automaton, final int locationIndex)
    {
        Args.checkForNull(automaton);
        Args.checkForIndex(locationIndex);
        return automatonIntegerEntryToLocationMap.get(new ExtendedAutomatonAndIntegerEntry(automaton, locationIndex));
    }

    public NodeProxy getLocationAt(final int automatonIndex, final int locationIndex)
    {
        if (automatonIndex < 0)
        {
            throw new IndexOutOfBoundsException("automatonIndex has to >= 0");
        }
        if (locationIndex < 0)
        {
            throw new IndexOutOfBoundsException("locationIndex has to >= 0");
        }
        ExtendedAutomaton currAutomaton = getAutomatonAt(automatonIndex);
        return getLocationAt(currAutomaton, locationIndex);
    }

    public ExtendedAutomaton getAutomatonAt(final int index)
    {
        Args.checkForIndex(index);
        return indexToExAutomatonArray[index];
    }



    static class ExtendedAutomatonAndLocationEntry
    {
        ExtendedAutomaton automaton;
        NodeProxy location;

        public ExtendedAutomatonAndLocationEntry(final ExtendedAutomaton automaton, final NodeProxy location)
        {
            this.automaton = automaton;
            this.location = location;
        }

        public int hashCode()
        {
            return (automaton.getName() + location.getName()).hashCode();
        }

        public boolean equals(Object other)
        {
            if (other instanceof ExtendedAutomatonAndLocationEntry)
            {
                return automaton.getName().equals(((ExtendedAutomatonAndLocationEntry) other).automaton.getName()) && location.getName().equals(((ExtendedAutomatonAndLocationEntry) other).location.getName());
            }
            return false;
        }
    }

    static class ExtendedAutomatonAndIntegerEntry
    {
        ExtendedAutomaton automaton;
        int locationIndex;

        public ExtendedAutomatonAndIntegerEntry(ExtendedAutomaton automaton, int locationIndex)
        {
            this.automaton = automaton;
            this.locationIndex = locationIndex;
        }

        public int hashCode()
        {
            return (automaton.getName().hashCode() * locationIndex);
        }

        public boolean equals(Object other)
        {
            if (other instanceof ExtendedAutomatonAndIntegerEntry)
            {
                return automaton.getName().equals(((ExtendedAutomatonAndIntegerEntry)other).automaton.getName()) && locationIndex == ((ExtendedAutomatonAndIntegerEntry)other).locationIndex;
            }
            return false;
        }
    }


}
