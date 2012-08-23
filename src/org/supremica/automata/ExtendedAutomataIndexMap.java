package org.supremica.automata;

/**
 *
 * @author sajed
 */

import org.supremica.log.*;
import org.supremica.util.Args;
import java.util.*;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;
import net.sourceforge.waters.model.module.VariableMarkingProxy;

public class ExtendedAutomataIndexMap {

    private Map<String, Integer> exAutomatonToIndexMap;
    private Map<String,ExtendedAutomaton> nameToExAutomatonMap;
    private ExtendedAutomaton[] indexToExAutomatonArray;
    private Map<ExtendedAutomatonAndLocationEntry, Integer> automatonLocationEntryToIndexMap;
    private Map<ExtendedAutomatonAndIntegerEntry, NodeProxy> automatonIntegerEntryToLocationMap;
    private Map<EventDeclProxy, Integer> eventToIndexMap;
    private EventDeclProxy[] indexToEventArray;
    private Map<VariableComponentProxy, Integer> variableToIndexMap;
    private Map<Integer, VariableComponentProxy> indexToVariableMap;
    private Map<String, EventDeclProxy> eventIdToProxyMap;
    public Map<String, Integer> variableStringToIndexMap;
    private final Map<String, Integer> var2initValMap;
    private final Map<String, List<VariableMarkingProxy>> var2markedValMap;
    private final Map<String, Integer> val2indexMap;
    private final Map<Integer, String> index2valMap;
    private Set<String> EFANames = null;
    private Set<String> variableNames = null;
    ExtendedAutomata theExAutomata = null;
    @SuppressWarnings("unused")
    private static final Logger logger =
            LoggerFactory.createLogger(AutomataIndexMap.class);

    public ExtendedAutomataIndexMap() {
        var2initValMap = new HashMap<String, Integer>();
        var2markedValMap = new HashMap<String, List<VariableMarkingProxy>>();
        val2indexMap = new HashMap<String, Integer>();
        index2valMap = new HashMap<Integer, String>();
    }

    public ExtendedAutomataIndexMap(final ExtendedAutomata theExAutomata) {
        this();

        this.theExAutomata = theExAutomata;
        // This useful variable stores the union of the automata events
        final List<EventDeclProxy> unionAlphabet = theExAutomata.getUnionAlphabet();

        /*       int initialLocationMapCapacity = 0;
        for (AbstractSubject currExAutomaton : theExAutomata)
        {
        initialLocationMapCapacity += currExAutomaton.nbrOfLocations();
        }
         */
        // The hashtables are initialized with appropriate capacities.
        exAutomatonToIndexMap = new HashMap<String, Integer>(theExAutomata.size() * 2);
        nameToExAutomatonMap = new HashMap<String, ExtendedAutomaton>(theExAutomata.size());
        indexToExAutomatonArray = new ExtendedAutomaton[theExAutomata.size()];
        automatonLocationEntryToIndexMap = new HashMap<ExtendedAutomatonAndLocationEntry, Integer>();
        automatonIntegerEntryToLocationMap = new HashMap<ExtendedAutomatonAndIntegerEntry, NodeProxy>();
        eventToIndexMap = new HashMap<EventDeclProxy, Integer>(unionAlphabet.size() * 2);
        indexToEventArray = new EventDeclProxy[unionAlphabet.size()];
        eventIdToProxyMap = new HashMap<String, EventDeclProxy>();
        variableToIndexMap = new HashMap<VariableComponentProxy, Integer>();
        indexToVariableMap = new HashMap<Integer, VariableComponentProxy>();

        variableStringToIndexMap = new HashMap<String, Integer>(theExAutomata.getVars().size());

        EFANames = new HashSet<String>(theExAutomata.size());
        variableNames = new HashSet<String>(theExAutomata.getVars().size());

        // The automatonIndex and the locationIndex hashmaps are filled
        int automatonIndex = 0;
        for (final ExtendedAutomaton currExAutomaton : theExAutomata) {
            // The automatonIndex hashtable is updated
            exAutomatonToIndexMap.put(currExAutomaton.getName(), automatonIndex);
            indexToExAutomatonArray[automatonIndex] = currExAutomaton;
            nameToExAutomatonMap.put(currExAutomaton.getName(), currExAutomaton);
            EFANames.add(currExAutomaton.getName());
            automatonIndex++;

            int locationIndex = 0;
            for (final NodeProxy currNode : currExAutomaton.getNodes()) {
                /*StringTokenizer st = new StringTokenizer(currNode.getName(), "_");
                st.nextToken();
                locationIndex = Integer.parseInt(st.nextToken());*/
//                System.err.println(currNode.getName()+": " + locationIndex);
                automatonLocationEntryToIndexMap.put(new ExtendedAutomatonAndLocationEntry(currExAutomaton.getName(), currNode.getName()), locationIndex);
                automatonIntegerEntryToLocationMap.put(new ExtendedAutomatonAndIntegerEntry(currExAutomaton.getName(), locationIndex), currNode);
                locationIndex++;
            }
        }

        int index = 0;
        final HashSet<Integer> integerDomain = new HashSet<Integer>();
        for (final VariableComponentProxy var : theExAutomata.getVars()) {
            indexToVariableMap.put(index, var);
            variableToIndexMap.put(var, index);
            variableStringToIndexMap.put(var.getName(), index);

            variableNames.add(var.getName());
            index++;

            final String varName = var.getName();
            final String range = var.getType().toString();

            if (range.contains(CompilerOperatorTable.getInstance().getRangeOperator().getName())) {
                for (int i = theExAutomata.getMinValueofVar(varName); i <= theExAutomata.getMaxValueofVar(varName); i++) {
                    val2indexMap.put("" + i, i);
                    index2valMap.put(i, "" + i);
                    integerDomain.add(i);
                }

                final int initialValue = val2indexMap.get(((BinaryExpressionProxy) var.getInitialStatePredicate()).getRight().toString());
                var2initValMap.put(varName, initialValue);
                var2markedValMap.put(varName, var.getVariableMarkings());
            }
        }

        index = 0;
        for (final VariableComponentProxy var : theExAutomata.getVars()) {
            final String varName = var.getName();
            final String range = var.getType().toString();

            if (range.contains(",")) {
                final StringTokenizer token = new StringTokenizer(range, ", { }");
                while (token.hasMoreTokens()) {
                    final String val = token.nextToken();
                    while (integerDomain.contains(index)) {
                        index++;
                    }

                    if (!val2indexMap.keySet().contains(val)) {
                        val2indexMap.put(val, index);
                        index2valMap.put(index, val);
                    } else {
                        index--;
                    }
                }

                final int initialValue = val2indexMap.get(((BinaryExpressionProxy) var.getInitialStatePredicate()).getRight().toString());
                var2initValMap.put(varName, initialValue);
                var2markedValMap.put(varName, var.getVariableMarkings());
            }
        }
        if (index > theExAutomata.getDomain()) {
            theExAutomata.extDomain(index);
        }

        // The eventIndex map is filled
        int eventIndex = 0;
        for (final EventDeclProxy currEvent : unionAlphabet) {
            eventIdToProxyMap.put(currEvent.getName(), currEvent);
            eventToIndexMap.put(currEvent, eventIndex);
            indexToEventArray[eventIndex] = currEvent;
            eventIndex++;
        }
    }

    public int isStringEFAorVar(String name) {
        if (EFANames.contains(name)) {
            return 0;
        } else if (variableNames.contains(name)) {
            return 1;
        }

        return 2;

    }

    public Integer getIndexOfVal(final String val) {
        return val2indexMap.get(val);
    }

    public String getValOfIndex(final int index) {
        return index2valMap.get(index);
    }

    public int getInitValueofVar(final String var) {
        return var2initValMap.get(var);
    }

    public List<VariableMarkingProxy> getMarkedPredicatesofVar(final String var) {
        return var2markedValMap.get(var);
    }

    public Integer getVariableIndex(final VariableComponentProxy var) {
        return variableToIndexMap.get(var);
    }

    public VariableComponentProxy getVariableAt(final int i) {
        return indexToVariableMap.get(i);
    }

    public Integer getVariableIndexByName(String name) {
        return variableStringToIndexMap.get(name);
    }

    public VariableComponentProxy getCorrepondentVariable(ExtendedAutomaton efa) {
        for (VariableComponentProxy var : theExAutomata.getVars()) {
            if (var.getName().contains(efa.getName())) {
                return var;
            }
        }

        return null;
    }

    public Integer getExAutomatonIndex(final String exAutomaton) {
        Args.checkForNull(exAutomaton);
        return exAutomatonToIndexMap.get(exAutomaton);
    }

    public ExtendedAutomaton getExAutomatonAt(final int index) {
        Args.checkForIndex(index);
        return indexToExAutomatonArray[index];
    }

    public ExtendedAutomaton getExAutomatonWithName(final String name) {
        Args.checkForNull(name);
        return nameToExAutomatonMap.get(name);
    }

    public int getEventIndex(final EventDeclProxy event) {
        Args.checkForNull(event);
        return eventToIndexMap.get(event);
    }

    public EventDeclProxy eventIdToProxy(final String id) {
        return eventIdToProxyMap.get(id);
    }

    public EventDeclProxy getEventAt(final int index) {
        Args.checkForIndex(index);
        return indexToEventArray[index];
    }

    public int getLocationIndex(final String automaton, final String location) {
        Args.checkForNull(automaton);
        Args.checkForNull(location);
        return automatonLocationEntryToIndexMap.get(new ExtendedAutomatonAndLocationEntry(automaton, location));
    }

    public int getLocationIndex(final int automatonIndex, final String location) {
        if (automatonIndex < 0) {
            throw new IndexOutOfBoundsException("automatonIndex has to >= 0");
        }
        final ExtendedAutomaton currAutomaton = getExAutomatonAt(automatonIndex);
        return getLocationIndex(currAutomaton.getName(), location);
    }

    public NodeProxy getLocationAt(final String automaton, final int locationIndex) {
        Args.checkForNull(automaton);
        Args.checkForIndex(locationIndex);
        return automatonIntegerEntryToLocationMap.get(new ExtendedAutomatonAndIntegerEntry(automaton, locationIndex));
    }

    public NodeProxy getLocationAt(final int automatonIndex, final int locationIndex) {
        if (automatonIndex < 0) {
            throw new IndexOutOfBoundsException("automatonIndex has to >= 0");
        }
        if (locationIndex < 0) {
            throw new IndexOutOfBoundsException("locationIndex has to >= 0");
        }
        final ExtendedAutomaton currAutomaton = getExAutomatonAt(automatonIndex);
        return getLocationAt(currAutomaton.getName(), locationIndex);
    }

    static class ExtendedAutomatonAndLocationEntry {

        String automaton;
        String location;

        public ExtendedAutomatonAndLocationEntry(final String automaton, final String location) {
            this.automaton = automaton;
            this.location = location;
        }

        @Override
        public int hashCode() {
            return (automaton + location).hashCode();
        }

        @Override
        public boolean equals(final Object other) {
            if (other instanceof ExtendedAutomatonAndLocationEntry) {
                return automaton.equals(((ExtendedAutomatonAndLocationEntry) other).automaton)
                        && location.equals(((ExtendedAutomatonAndLocationEntry) other).location);
            }
            return false;
        }
    }

    static class ExtendedAutomatonAndIntegerEntry {

        String automaton;
        int locationIndex;

        public ExtendedAutomatonAndIntegerEntry(final String automaton, final int locationIndex) {
            this.automaton = automaton;
            this.locationIndex = locationIndex;
        }

        @Override
        public int hashCode() {
            return (automaton.hashCode() * locationIndex);
        }

        @Override
        public boolean equals(final Object other) {
            if (other instanceof ExtendedAutomatonAndIntegerEntry) {
                return automaton.equals(((ExtendedAutomatonAndIntegerEntry) other).automaton)
                        && locationIndex == ((ExtendedAutomatonAndIntegerEntry) other).locationIndex;
            }
            return false;
        }
    }
}
