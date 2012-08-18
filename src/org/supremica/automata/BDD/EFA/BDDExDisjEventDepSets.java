package org.supremica.automata.BDD.EFA;

import gnu.trove.THashSet;
import gnu.trove.TIntHashSet;
import gnu.trove.TIntIntHashMap;
import gnu.trove.TIntIterator;
import gnu.trove.TIntObjectHashMap;
import gnu.trove.TIntObjectProcedure;
import gnu.trove.TIntProcedure;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDDomain;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;
import org.supremica.automata.ExtendedAutomaton;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.SimpleIdentifierSubject;
import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;


/**
 * One of three disjunctive partitioning techniques: Event-based disjunctive partitions
 *
 * @author zhennan
 * @version 1.0
 *
 */
public class BDDExDisjEventDepSets extends BDDExDisjDepSets {

    @SuppressWarnings("unused")
    private static Logger logger = LoggerFactory.createLogger(BDDExDisjEventDepSets.class);

    /* Define a map where the key is the index of an event in the index map while the value is an instance of EventDisParDepSet*/
    private TIntObjectHashMap<EventDisjParDepSet> events2EventDisParDepSet;

    /* Define a map where the key is the event index while the value is its corresponding BDD */
    private final TIntObjectHashMap<BDD> eventIndexToBDD;

    /* Define a map where the key is the index of an event in the index map while the value is the BDD expression of partitioned parts*/
    private final TIntObjectHashMap<BDD> eventToCompleteTransitionBDD;

    /* The base of this partitioning technique. The key is the event index, the value is another map where the key is automaton
     * while the values is which edges in this automaton are labeled by the corresponding event.*/
    private final TIntObjectHashMap<HashMap<ExtendedAutomaton, ArrayList<EdgeProxy>>> event2AutomatonsEdges;


    /** D^{e}(\sigma) and D^{v}(\sigma), refer to the paper. */
    private final TIntObjectHashMap<TIntHashSet> forwardEventDependencySet;
    private final TIntObjectHashMap<TIntHashSet> backwardEventDependencySet;

    private TIntObjectHashMap<TIntHashSet> forwardVariableDependencySet;
    private TIntObjectHashMap<TIntHashSet> backwardVariableDependencySet;

    private TIntObjectHashMap<TIntHashSet> forwardDependencyMap;
    private TIntObjectHashMap<TIntHashSet> backwardDependencyMap;

    /* This is for heuristics. During the fix-pointed iteration, it is the key to pick the most useful BDD for the next round.
     * Here I came up with several simple methods. Note that they are not proved yet.
     */
    private final TIntObjectHashMap<TIntIntHashMap> event2ForwardInfluencedEvens;
    private final TIntObjectHashMap<TIntIntHashMap> event2BackwardInfluencedEvents;


    /** A map from an event index to all of the updated variable names during partitioning. */
    private final TIntObjectHashMap<HashSet<String>> eventIndex2UpdatedVariables;
    /** A map from an event index to the variables appearing in the guard. */
    private final TIntObjectHashMap<HashSet<String>> eventIndex2GuardVariables;


    /** An event index list in which the included events are enabled from the initial location. */
    private final TIntHashSet initialComponentCandidates;
    /** An event index list in which the included events are enabled to the marked locations. */
    private final TIntHashSet markedComponentCandidates;
    /** An event index list in which all of included events are uncontrollable. */
    private final TIntHashSet uncontrollableComponentCandidates;


    /** The BDD representing the uncontrollable transition relation. */
    private  BDD uncontrollableTransitionRelationBDD;


    public BDDExDisjEventDepSets(final BDDExtendedAutomata bddAutomata)
    {
        super(bddAutomata);
        this.event2AutomatonsEdges = bddAutomata.event2AutomatonsEdges;
        this.size = orgAutomata.unionAlphabet.size();

        this.eventToCompleteTransitionBDD = new TIntObjectHashMap<BDD>(size);
        this.eventIndexToBDD = new TIntObjectHashMap<BDD>(size);

        this.forwardEventDependencySet = new TIntObjectHashMap<TIntHashSet>(size);
        this.backwardEventDependencySet = new TIntObjectHashMap<TIntHashSet>(size);

        this.event2ForwardInfluencedEvens = new TIntObjectHashMap<TIntIntHashMap>(size);
        this.event2BackwardInfluencedEvents = new TIntObjectHashMap<TIntIntHashMap>(size);

        this.eventIndex2UpdatedVariables = new TIntObjectHashMap<HashSet<String>>(size);
        this.eventIndex2GuardVariables = new TIntObjectHashMap<HashSet<String>>(size);

        this.initialComponentCandidates = new TIntHashSet();
        this.markedComponentCandidates = new TIntHashSet();
        this.uncontrollableComponentCandidates = new TIntHashSet();

        this.uncontrollableTransitionRelationBDD = manager.getZeroBDD();

        initialize();
    }

    @Override
    protected final void initialize()
    {
        /* Initialize eventIndex2UpdatedVariables and eventIndex2GuardVariables*/
        for (int i = 0; i < size; i++)
        {
            eventIndex2GuardVariables.put(i, new HashSet<String>());
            eventIndex2UpdatedVariables.put(i, new HashSet<String>());
        }

        events2EventDisParDepSet = new TIntObjectHashMap<EventDisjParDepSet>(size);

        event2AutomatonsEdges.forEachKey(new TIntProcedure()
        {
            @Override
            public boolean execute(final int eventIndex) {
                final HashMap<ExtendedAutomaton, ArrayList<EdgeProxy>> automatonsEdges = event2AutomatonsEdges.get(eventIndex);
                /* Use the event index and edges where it appears in to construct the instance of EventDisjParDepSet. */
                final EventDisjParDepSet eventDisParSet = new EventDisjParDepSet(eventIndex, automatonsEdges);
                events2EventDisParDepSet.put(eventIndex, eventDisParSet);
                return true;
            }
        });
    }

    @Override
    public TIntObjectHashMap<BDD> getComponentToComponentTransMap()
    {
        if (eventToCompleteTransitionBDD.isEmpty()) {
            events2EventDisParDepSet.forEachEntry(new TIntObjectProcedure<EventDisjParDepSet>()
            {
                @Override
                public boolean execute(final int eventIndex, final EventDisjParDepSet t)
                {
                    eventToCompleteTransitionBDD.put(eventIndex, t.eventForwardCompleteTransitions);
                    return true;
                }
            });
        }
        return eventToCompleteTransitionBDD;
    }

    public TIntObjectHashMap<BDD> getEventIndexToBDDMap()
    {
        if (eventIndexToBDD.isEmpty())
        {
            events2EventDisParDepSet.forEachEntry(new TIntObjectProcedure<EventDisjParDepSet>()
            {
                @Override
                public boolean execute(final int i, final EventDisjParDepSet t)
                {
                    eventIndexToBDD.put(i, t.eventBDD);
                    return true;
                }
            });
        }
        return eventIndexToBDD;
    }

    @Override
    public TIntObjectHashMap<TIntHashSet> getForwardDependentComponentMap()
    {
        if (forwardDependencyMap == null) {
            buildVariableDependentComponent();
            forwardDependencyMap = new TIntObjectHashMap<TIntHashSet>(size);
            for(int i = 0; i < size; i++)
                forwardDependencyMap.put(i, new TIntHashSet());

            forwardVariableDependencySet.forEachEntry(new TIntObjectProcedure<TIntHashSet>() {

                @Override
                public boolean execute(final int eventIndex, final TIntHashSet eventIndexSet) {
                    forwardDependencyMap.put(eventIndex, eventIndexSet);
                    return true;
                }
            });

            event2AutomatonsEdges.forEachEntry(new TIntObjectProcedure<HashMap<ExtendedAutomaton, ArrayList<EdgeProxy>>>() {

                @Override
                public boolean execute(final int eventIndex, final HashMap<ExtendedAutomaton, ArrayList<EdgeProxy>> automataEdges) {
                    for (final Map.Entry<ExtendedAutomaton, ArrayList<EdgeProxy>> entry : automataEdges.entrySet()) {
                        final ExtendedAutomaton currExAutomaton = entry.getKey();

                        final List<EdgeProxy> edgesOfCurrExAutomaton = entry.getValue();
                        for (final EdgeProxy anEdge : edgesOfCurrExAutomaton) {
                            final NodeProxy targetLocation = anEdge.getTarget();

                            for (final EdgeSubject anOutgoingEdge : currExAutomaton.getLocationToOutgoingEdgesMap().get(targetLocation)) {
                                for (final Proxy event : anOutgoingEdge.getLabelBlock().getEventIdentifierList()) {
                                    final String eventName = ((SimpleIdentifierSubject) event).getName();
                                    final EventDeclProxy theEvent = bddExAutomata.getExtendedAutomata().eventIdToProxy(eventName);
                                    if (!forwardEventDependencySet.containsKey(eventIndex)) {
                                        forwardEventDependencySet.put(eventIndex, new TIntHashSet());
                                    }
                                    forwardEventDependencySet.get(eventIndex).add(theIndexMap.getEventIndex(theEvent));
                                    forwardDependencyMap.get(eventIndex).add(theIndexMap.getEventIndex(theEvent));
                                }
                            }
                        }
                    }
                    return true;
                }
            });
        }
        return forwardDependencyMap;
    }

    @Override
    public TIntObjectHashMap<TIntHashSet> getBackwardDependentComponentMap()
    {
        if (backwardDependencyMap == null) {
            buildVariableDependentComponent();
            backwardDependencyMap = new TIntObjectHashMap<TIntHashSet>(size);
            for(int i = 0; i < size; i++)
                backwardDependencyMap.put(i, new TIntHashSet());

            backwardVariableDependencySet.forEachEntry(new TIntObjectProcedure<TIntHashSet>() {

                @Override
                public boolean execute(final int eventIndex, final TIntHashSet eventIndexSet) {
                    backwardDependencyMap.put(eventIndex, eventIndexSet);
                    return true;
                }
            });
            event2AutomatonsEdges.forEachEntry(new TIntObjectProcedure<HashMap<ExtendedAutomaton, ArrayList<EdgeProxy>>>() {

                @Override
                public boolean execute(final int eventIndex, final HashMap<ExtendedAutomaton, ArrayList<EdgeProxy>> automataEdges) {
                    for (final Map.Entry<ExtendedAutomaton, ArrayList<EdgeProxy>> entry : automataEdges.entrySet()) {
                        for (final EdgeProxy anEdge : entry.getValue()) {
                            for (final EdgeSubject anIngoingEdge : entry.getKey().getLocationToIngoingEdgesMap().get(anEdge.getSource())) {
                                for (final Proxy event : anIngoingEdge.getLabelBlock().getEventIdentifierList()) {
                                    final String eventName = ((SimpleIdentifierSubject) event).getName();
                                    final EventDeclProxy theEvent = bddExAutomata.getExtendedAutomata().eventIdToProxy(eventName);
                                    if (!backwardEventDependencySet.containsKey(eventIndex)) {
                                        backwardEventDependencySet.put(eventIndex, new TIntHashSet());
                                    }
                                    backwardEventDependencySet.get(eventIndex).add(theIndexMap.getEventIndex(theEvent));
                                    backwardDependencyMap.get(eventIndex).add(theIndexMap.getEventIndex(theEvent));
                                }
                            }
                        }
                    }
                    return true;
                }
            });
        }

        return backwardDependencyMap;
    }

    /* build both forward and backward variable dependency set at the same time. */
    private void buildVariableDependentComponent()
    {
        if (forwardVariableDependencySet == null && backwardVariableDependencySet == null) {

            forwardVariableDependencySet = new TIntObjectHashMap<TIntHashSet>(size);
            backwardVariableDependencySet = new TIntObjectHashMap<TIntHashSet>(size);

            final int[] eventIndexAsKeys = events2EventDisParDepSet.keys();
            for (int i = 0; i < eventIndexAsKeys.length; i++) {
                final int iEventIndex = eventIndexAsKeys[i];
                final HashSet<String> updatedVariables = eventIndex2UpdatedVariables.get(iEventIndex);
                final HashSet<String> guardVariables = eventIndex2GuardVariables.get(iEventIndex);
                for (int j = 0; j < eventIndexAsKeys.length; j++) {
                    if (i != j) {
                        final int influencedEventIndex = eventIndexAsKeys[j];
                        final Set<String> tempUpdatedVariables =
                          new THashSet<String>(updatedVariables);
                        final Set<String> tempGuardVariables =
                          new THashSet<String>(guardVariables);
                        tempUpdatedVariables.retainAll(eventIndex2GuardVariables.get(influencedEventIndex));
                        tempGuardVariables.retainAll(eventIndex2UpdatedVariables.get(influencedEventIndex));
                        if (!tempUpdatedVariables.isEmpty() ||
                                (eventIndex2GuardVariables.get(influencedEventIndex).isEmpty() &&
                                !eventIndex2UpdatedVariables.get(influencedEventIndex).isEmpty())) {
                            if (!forwardVariableDependencySet.containsKey(iEventIndex)) {
                                forwardVariableDependencySet.put(iEventIndex, new TIntHashSet());
                            }
                            forwardVariableDependencySet.get(iEventIndex).add(influencedEventIndex);
                        }
                        if (!tempGuardVariables.isEmpty() ||
                                (eventIndex2UpdatedVariables.get(influencedEventIndex).isEmpty()) &&
                                !eventIndex2GuardVariables.get(influencedEventIndex).isEmpty()) {
                            if (!backwardVariableDependencySet.containsKey(iEventIndex)) {
                                backwardVariableDependencySet.put(iEventIndex, new TIntHashSet());
                            }
                            backwardVariableDependencySet.get(iEventIndex).add(influencedEventIndex);
                        }
                    }
                }
            }
        }
    }

    /* For the heuristic desicision procedure. */
    @Override
    protected TIntObjectHashMap<TIntIntHashMap> getForwardComponentToInfluencedComponentMap()
    {
        if (event2ForwardInfluencedEvens.isEmpty()) {
            if (!orgAutomata.getVars().isEmpty()) { // EFAs
                final int[] eventIndicesAsKeys = eventIndex2UpdatedVariables.keys();
                for (int i = 0; i < eventIndicesAsKeys.length; i++) {
                    final int currentEventIndex = eventIndicesAsKeys[i];
                    /* A set of variables updated by the event index */
                    final HashSet<String> updatedVariablesByTheCurrentEvent = eventIndex2UpdatedVariables.get(currentEventIndex);
                    final TIntIntHashMap tmp = new TIntIntHashMap();
                    /* Get the set of dependent evemts (event and variable dependent) */
                    final TIntHashSet dependentEvents = getForwardDependentComponentMap().get(currentEventIndex);
                    for(final TIntIterator itr = dependentEvents.iterator(); itr.hasNext();){
                        final int dependentEventIndex = itr.next();
                        final HashSet<String> guardVariablesByThisEvent = new HashSet<String>(eventIndex2GuardVariables.get(dependentEventIndex));
                        guardVariablesByThisEvent.retainAll(updatedVariablesByTheCurrentEvent);
                        tmp.put(dependentEventIndex, guardVariablesByThisEvent.size());
                    }
                    event2ForwardInfluencedEvens.put(currentEventIndex, tmp);
                }
            } else {  // DFAs, i.e. no variable
                final int[] eventIndicesAsKeys = getComponentToComponentTransMap().keys();
                for (int i = 0; i < eventIndicesAsKeys.length; i++) {
                    final int currentEventIndex = eventIndicesAsKeys[i];
                    final TIntIntHashMap tmp = new TIntIntHashMap();
                    final TIntHashSet dependentEvents = getForwardDependentComponentMap().get(currentEventIndex);
                    for(final TIntIterator itr = dependentEvents.iterator(); itr.hasNext();){
                        final int dependentEventIndex = itr.next();
                        tmp.put(dependentEventIndex, getForwardDependentComponentMap().get(dependentEventIndex).size());
                    }
                    event2ForwardInfluencedEvens.put(currentEventIndex, tmp);
                }
            }
        }
        return event2ForwardInfluencedEvens;
    }

    @Override
    protected TIntObjectHashMap<TIntIntHashMap> getBackwardComponentToInfluencedComponentMap()
    {
        if (event2BackwardInfluencedEvents.isEmpty()) {
            if (!orgAutomata.getVars().isEmpty()) {
                final int[] eventIndicesAsKeys = eventIndex2GuardVariables.keys();
                for (int i = 0; i < eventIndicesAsKeys.length; i++) {
                    final int currentEventIndex = eventIndicesAsKeys[i];
                    /* Find the set of variables appearing in the guards of edges labeled by the current event. */
                    final HashSet<String> guardVariablesByTheCurrentEvent = eventIndex2GuardVariables.get(currentEventIndex);
                    /* For the current event index, the guard variables might be the result of updated variables of edges labeled
                       by other events true
                       Go over each one, find the set of updated variables. Find the shared variables and the number is the value.
                     */
                    final TIntIntHashMap tmp = new TIntIntHashMap();
                    final TIntHashSet dependentEvents = getBackwardDependentComponentMap().get(currentEventIndex);
                    for(final TIntIterator itr = dependentEvents.iterator(); itr.hasNext();){
                        final int dependentEventIndex = itr.next();
                        final HashSet<String> updatedVariablesByThisEvent = new HashSet<String>(eventIndex2UpdatedVariables.get(dependentEventIndex));
                        updatedVariablesByThisEvent.retainAll(guardVariablesByTheCurrentEvent);
                        tmp.put(dependentEventIndex, updatedVariablesByThisEvent.size());
                    }
                    event2BackwardInfluencedEvents.put(currentEventIndex, tmp);
                }
            } else {
                final int[] eventIndicesAsKeys = getComponentToComponentTransMap().keys();
                for (int i = 0; i < eventIndicesAsKeys.length; i++) {
                    final int currentEventIndex = eventIndicesAsKeys[i];
                    final TIntIntHashMap tmp = new TIntIntHashMap();
                    final TIntHashSet dependentEvents = getBackwardDependentComponentMap().get(currentEventIndex);
                    for(final TIntIterator itr = dependentEvents.iterator(); itr.hasNext();){
                        final int dependentEventIndex = itr.next();
                        tmp.put(dependentEventIndex, getBackwardDependentComponentMap().get(dependentEventIndex).size());
                    }
                    event2BackwardInfluencedEvents.put(currentEventIndex, tmp);
                }
            }
        }
        return event2BackwardInfluencedEvents;
    }

    /** Access to the eventIndex2UpdatedVariables map */
    public TIntObjectHashMap<HashSet<String>> getEventIndex2UpdatedVariables() {
        return eventIndex2UpdatedVariables;
    }

    /** Access to the eventIndex2GuardVariables map */
    public TIntObjectHashMap<HashSet<String>> getEventIndex2GuardVariables() {
        return eventIndex2GuardVariables;
    }

    @Override
    protected TIntHashSet getInitialComponentCandidates() {
        return initialComponentCandidates;
    }

    @Override
    protected TIntHashSet getMarkedComponentCandidates() {
        return markedComponentCandidates;
    }

    @Override
    protected TIntHashSet getUncontrollableComponentCandidates() {
        return uncontrollableComponentCandidates;
    }

    @Override
    protected BDD getUncontrollableTransitionRelationBDD() {
        if (uncontrollableTransitionRelationBDD.isZero()) {
            for (final TIntIterator componentItr = getUncontrollableComponentCandidates().iterator(); componentItr.hasNext();) {
                uncontrollableTransitionRelationBDD = uncontrollableTransitionRelationBDD
                        .or(getComponentToComponentTransMap().get(componentItr.next()));
            }
        }
        return uncontrollableTransitionRelationBDD;
    }

    /* For each event, initiate an instance of EventDisjParDepSet. */

    class EventDisjParDepSet {

        private final int eventIndex;
        private BDD eventBDD;
        private final HashMap<ExtendedAutomaton, ArrayList<EdgeProxy>> includingAutomata2Edges;
        private final BDD eventForwardCompleteTransitions;
        private BDD eventForwardTransitionWithoutActions;

        public EventDisjParDepSet(final int eventIndex, final HashMap<ExtendedAutomaton, ArrayList<EdgeProxy>> includingAutomata2Edges)
        {
            this.eventIndex = eventIndex;
            this.includingAutomata2Edges = includingAutomata2Edges;
            this.eventForwardCompleteTransitions = manager.getZeroBDD();
            this.eventForwardTransitionWithoutActions = manager.getZeroBDD();
            initialize();
        }

        private void initialize()
        {
            /* Create a BDD based on the event index, there is no need. */
            final BDDDomain eventDomain = bddExAutomata.getEventDomain();
            eventBDD = manager.getFactory().buildCube(eventIndex, eventDomain.vars());

            final Set<ExtendedAutomaton> includedAutomata = includingAutomata2Edges.keySet();
            final Iterator<ExtendedAutomaton> autIterator = includedAutomata.iterator();

            /* I reused Sajed's efficient approach with a little modification to construct the event-based BDD partitions.*/
            BDD transWithoutActions = null;
            BDD[] transCorrespondingToUpdatedVariablesWithoutActions = null;
            BDD[] transCorrespondingToUpdatedVariables = null;

            final BDD[] conjunctiveTransToUpdatedVariables = new BDD[orgAutomata.getVars().size()];

            /* For some automata, there is at least edge labeled by the event index comes from an initial location, but it doesn't mean this event is qualified for the
            initial component during reachability search. We have to make sure that the event is qualified for all of the automata. Here is the boolean variable
            which can help us do that.
             */
            boolean eventIsQualifiedForInitialComponent = false;
            boolean eventIsQualifiedForMarkedComponent = false;

            if (autIterator.hasNext())
            {
                final ExtendedAutomaton firstAutomaton = autIterator.next();
                final AutIncludingTransLabeledByEvent firstAutTransWithEvent = new AutIncludingTransLabeledByEvent(includingAutomata2Edges, eventIndex, firstAutomaton);

                transWithoutActions = firstAutTransWithEvent.getTransitionBDDWithoutActions();
                transCorrespondingToUpdatedVariablesWithoutActions = firstAutTransWithEvent. getTransitionBDDCorrespondingToUpdatedVariablesWithoutActions();
                transCorrespondingToUpdatedVariables = firstAutTransWithEvent.getTransitionBDDCorrespondingToUpdatedVariables();

                for (int i = 0; i < transCorrespondingToUpdatedVariables.length; i++) {
                    conjunctiveTransToUpdatedVariables[i] = transCorrespondingToUpdatedVariables[i].id();
                }

                eventIsQualifiedForInitialComponent = firstAutTransWithEvent.isQualifiedForInitialComponent();
                eventIsQualifiedForMarkedComponent = firstAutTransWithEvent.isQualifiedForMarkedComponent();
            }

            if (includedAutomata.size() == 1)
            {
                for (final VariableComponentProxy var : orgAutomata.getVars())
                {
                    final int varIndex = theIndexMap.getVariableIndex(var);
                    final BDD noneUpdateVar =
                            bddExAutomata.BDDBitVecTargetVarsMap.get(var.getName()).equ(bddExAutomata.BDDBitVecSourceVarsMap.get(var.getName()));
                    transCorrespondingToUpdatedVariables[varIndex] =
                            transCorrespondingToUpdatedVariablesWithoutActions[varIndex].ite(transCorrespondingToUpdatedVariables[varIndex],noneUpdateVar);
                }

                if (eventIsQualifiedForInitialComponent)
                {
                    initialComponentCandidates.add(eventIndex);
                }

                if (eventIsQualifiedForMarkedComponent)
                {
                    markedComponentCandidates.add(eventIndex);
                }
            }

            while (autIterator.hasNext())
            {
                final ExtendedAutomaton currAutomaton = autIterator.next();
                final AutIncludingTransLabeledByEvent currAutIncTrans = new AutIncludingTransLabeledByEvent(includingAutomata2Edges, eventIndex, currAutomaton);

                final BDD currTransWithoutActions = currAutIncTrans.getTransitionBDDWithoutActions();
                final BDD[] currTransCorrespondingToUpdatedVariables = currAutIncTrans.getTransitionBDDCorrespondingToUpdatedVariables();
                final BDD[] currTransCorrespondingToUpdatedVariablesWithoutActions = currAutIncTrans.getTransitionBDDCorrespondingToUpdatedVariablesWithoutActions();

                for (final VariableComponentProxy var : orgAutomata.getVars())
                {
                    final int varIndex = theIndexMap.getVariableIndex(var);

                    final BDD transConditionBDD = transCorrespondingToUpdatedVariablesWithoutActions[varIndex];
                    final BDD transStatementBDD = transCorrespondingToUpdatedVariables[varIndex];

                    final BDD currTransConditionBDD = currTransCorrespondingToUpdatedVariablesWithoutActions[varIndex];
                    final BDD currTransStatementBDD = currTransCorrespondingToUpdatedVariables[varIndex];

                    final BDD firstUpdateCondition = transConditionBDD.and(currTransConditionBDD.not());
                    final BDD firstUpdatedVar = firstUpdateCondition.and(transStatementBDD);

                    final BDD secondUpdateCondition = currTransConditionBDD.and(transConditionBDD.not());
                    final BDD secondUpdatedVar = secondUpdateCondition.and(currTransStatementBDD);

                    transCorrespondingToUpdatedVariablesWithoutActions[varIndex] = transConditionBDD.or(currTransConditionBDD);

                    final BDD bothUpdatedVar = transStatementBDD.and(currTransStatementBDD);

                    transCorrespondingToUpdatedVariables[varIndex] = firstUpdatedVar.or(secondUpdatedVar).or(bothUpdatedVar);
                }

                for (final VariableComponentProxy var : orgAutomata.getVars())
                {
                    final int varIndex = theIndexMap.getVariableIndex(var);
                    final BDD compensate = bddExAutomata.BDDBitVecTargetVarsMap.get(var.getName()).equ(bddExAutomata.BDDBitVecSourceVarsMap.get(var.getName()));
                    transCorrespondingToUpdatedVariables[varIndex] = transCorrespondingToUpdatedVariablesWithoutActions[varIndex].ite(transCorrespondingToUpdatedVariables[varIndex],compensate);
                }

                transWithoutActions.andWith(currTransWithoutActions);

                /* Update eventIsQualifiedForInitialComponent and eventIsQualifiedForMarkedComponent */
                eventIsQualifiedForInitialComponent = eventIsQualifiedForInitialComponent && currAutIncTrans.isQualifiedForInitialComponent();
                eventIsQualifiedForMarkedComponent = eventIsQualifiedForMarkedComponent && currAutIncTrans.isQualifiedForMarkedComponent();
            }

            final BDD tmp = manager.getOneBDD();
            for (int i = 0; i < bddExAutomata.orgExAutomata.getVars().size(); i++)
            {
                final String varName = theIndexMap.getVariableAt(i).getName();
                transCorrespondingToUpdatedVariables[i] =
                        transCorrespondingToUpdatedVariables[i].and(bddExAutomata.BDDBitVecSourceVarsMap.get(varName).lte(bddExAutomata.getMaxBDDBitVecOf(varName)));
                transCorrespondingToUpdatedVariables[i] =
                        transCorrespondingToUpdatedVariables[i].and(bddExAutomata.BDDBitVecTargetVarsMap.get(varName).lte(bddExAutomata.getMaxBDDBitVecOf(varName)));
                transCorrespondingToUpdatedVariables[i] =
                        transCorrespondingToUpdatedVariables[i].and(bddExAutomata.BDDBitVecSourceVarsMap.get(varName).gte(bddExAutomata.getMinBDDBitVecOf(varName)));
                transCorrespondingToUpdatedVariables[i] =
                        transCorrespondingToUpdatedVariables[i].and(bddExAutomata.BDDBitVecTargetVarsMap.get(varName).gte(bddExAutomata.getMinBDDBitVecOf(varName)));

                tmp.andWith(transCorrespondingToUpdatedVariables[i]);
            }
            /* Merge with "keep" BDD */
            final Set<ExtendedAutomaton> includedAutomataSet = includingAutomata2Edges.keySet();
            BDD keep = manager.getOneBDD();
            for (final ExtendedAutomaton extendedAutomaton : theExAutomata)
            {
                if (!includedAutomataSet.contains(extendedAutomaton))
                {
                    final BDDExtendedAutomaton bddAutomaton = bddExAutomata.automatonToBDDAutomatonMap.get(extendedAutomaton);
                    keep = keep.and(bddAutomaton.getSelfLoopsBDD());
                }
            }

            transWithoutActions.andWith(keep);

            eventForwardTransitionWithoutActions = eventForwardTransitionWithoutActions.or(transWithoutActions);

            transWithoutActions.andWith(tmp);

            final BDD isolatedTransBDD = transWithoutActions;

            eventForwardCompleteTransitions.orWith(isolatedTransBDD);

            /* Determine whether or not to put this event as the initialComponentCandidate set or markedComponentCandidate set. */
            if (eventIsQualifiedForInitialComponent)
            {
                initialComponentCandidates.add(eventIndex);
            }

            if (eventIsQualifiedForMarkedComponent)
            {
                markedComponentCandidates.add(eventIndex);
            }

            /* If the event is uncontrollable, put it into the uncontrollableComponentCandidates */
            if (bddExAutomata.plantUncontrollableEventIndexList.contains(eventIndex) || bddExAutomata.specUncontrollableEventIndexList.contains(eventIndex))
            {
                uncontrollableComponentCandidates.add(eventIndex);
            }
        }
    }

    /** A help class, which is used for constructing event-based BDD partitioning on the BDD level
     *   The whole idea of constructing event-based BDD partition is that:
     *   1. For each event, we iterate each automaton to get all the edges labeled with the event
     *   2. From those edges on the current automaton, we create
     *       a. A single BDD for the transitions without actions;
     *       b. A BDD array which holds the variable cared transition for each variable (used as conditions in If-Then-Else operator)
     *       c. A BDD array which holds the variable cared transition and actions for each variable (used as statement in If-Then-Else operator)
     *   3. After we get an instance for each automaton, we merge them together on the BDD level.
     *   This class focuses on part 2
     */
    class AutIncludingTransLabeledByEvent {

        private final HashMap<ExtendedAutomaton, ArrayList<EdgeProxy>> includingAutomata2Edges;
        private final ExtendedAutomaton anAutomaton;
        private final int eventIndex;
        private BDD transitionBDDWithoutActions;
        private final BDD[] transitionBDDCorrespondingToUpdatedVariables;
        private final BDD[] transitionBDDCorrespondingToUpdatedVariablesWithoutActions;

        /* When iterate each edge, for at least one edge, if the source location of it is an initial location, qualifiedForInitialComponent is set true. */
        private boolean qualifiedForInitialComponent = false;

        /* When iterate each edge, for at least one edge, if the target location of it is an marked location, qualifiedForMarkedComponent is set true. */
        private boolean qualifiedForMarkedComponent = false;

        /** Constructor*/
        public AutIncludingTransLabeledByEvent(final HashMap<ExtendedAutomaton, ArrayList<EdgeProxy>> includingAutomata2Edges, final int eventIndex, final ExtendedAutomaton anAutomaton)
        {
            this.includingAutomata2Edges = includingAutomata2Edges;
            this.eventIndex = eventIndex;
            this.anAutomaton = anAutomaton;
            /* Create these two BDD arrays */
            this.transitionBDDCorrespondingToUpdatedVariablesWithoutActions = new BDD[orgAutomata.getVars().size()];
            this.transitionBDDCorrespondingToUpdatedVariables = new BDD[orgAutomata.getVars().size()];

            initialize();
        }

        private void initialize()
        {
            /* First initialize the BDD and two BDD arrays*/
            transitionBDDWithoutActions = manager.getZeroBDD(); // It should be zero-BDD, because we want the union of BDD for all edges in one automaton
            for (final VariableComponentProxy var : orgAutomata.getVars()) {
                transitionBDDCorrespondingToUpdatedVariablesWithoutActions[theIndexMap.getVariableIndexByName(var.getName())] = manager. getZeroBDD();
                transitionBDDCorrespondingToUpdatedVariables[theIndexMap.getVariableIndexByName(var.getName())] = manager. getZeroBDD();
            }

            /* Go through each edge and build these two BDD arrays*/
            for (final Iterator<EdgeProxy> edgeIterator = includingAutomata2Edges.get(anAutomaton).iterator(); edgeIterator.hasNext();)
            {
                final EdgeProxy anEdge = edgeIterator.next();

                if (anAutomaton.isLocationInitial(anEdge.getSource()))
                {
                    qualifiedForInitialComponent = true;
                }

                if (anAutomaton.isLocationAccepted(anEdge.getTarget()) || bddExAutomata.getBDDExAutomaton(anAutomaton).getAllMarked())
                {
                    qualifiedForMarkedComponent = true;
                }

                /* Construct the BDD for the edge but exclude the actions. */
                final BDD transitionWithoutActionsOnCurrEdge = getEdgeBDDWithoutActions(anAutomaton, anEdge);

                /* Keep track of the updated variable index */
                final TIntHashSet updatedVariableIndexSet = new TIntHashSet();

                /* Try another idea */
                final TIntObjectHashMap<BDD> varIndex2Action = new TIntObjectHashMap<BDD>(orgAutomata.getVars().size());

                List<BinaryExpressionProxy> actions = null;
                final BDD actionsBDD = manager.getOneBDD();

                if (anEdge.getGuardActionBlock() != null && anEdge.getGuardActionBlock().getActions() != null && !anEdge.getGuardActionBlock().getActions().isEmpty())
                {
                    actions = anEdge.getGuardActionBlock().getActions();
                    /* Iterate each action to extract variables and build BDD*/
                    for (final BinaryExpressionProxy anAction : actions)
                    {
                        final String updatedVariableOfThisAction = ((SimpleIdentifierProxy) anAction.getLeft()).getName();
                        final int varIndex = theIndexMap.getVariableIndexByName(updatedVariableOfThisAction);
                        updatedVariableIndexSet.add(theIndexMap.getVariableIndexByName(updatedVariableOfThisAction));
                        /* Use it for heuristics*/
                        eventIndex2UpdatedVariables.get(eventIndex).add(updatedVariableOfThisAction);
                        /*BDD-AND each actionBDD */
                        varIndex2Action.put(varIndex,transitionWithoutActionsOnCurrEdge.and(manager.action2BDD(anAction)));
                        actionsBDD.andWith(manager.action2BDD(anAction));
                    }
                }

                /* Now we have, for each edge (1) forward BDD transition without actions (2) actionsBDD (3) the updated variable set
                Next, we merge them together
                (1) based on the indices of updated variables, OR with them on the BDD array and return this BDD array
                (2) Or with transitionBDDWithoutActions with transitionWithoutActionsOnCurrEdge
                 */
                for (final TIntIterator itr = updatedVariableIndexSet.iterator(); itr.hasNext();)
                {
                    final int variableIndex = itr.next();
                    transitionBDDCorrespondingToUpdatedVariablesWithoutActions[variableIndex] =
                            transitionBDDCorrespondingToUpdatedVariablesWithoutActions[variableIndex].or(transitionWithoutActionsOnCurrEdge);
                }

                varIndex2Action.forEachEntry(new TIntObjectProcedure<BDD>()
                {
                    @Override
                    public boolean execute(final int varIndex, final BDD action) {
                        transitionBDDCorrespondingToUpdatedVariables[varIndex] = transitionBDDCorrespondingToUpdatedVariables[varIndex].or(action);
                        return true;
                    }
                });

                transitionBDDWithoutActions.orWith(transitionWithoutActionsOnCurrEdge);
            }
        }

        private BDD getEdgeBDDWithoutActions(final ExtendedAutomaton anAutomaton, final EdgeProxy anEdge)
        {
            final BDDDomain sourceLocationDomain = bddExAutomata.getSourceLocationDomain(anAutomaton.getName());
            final BDDDomain destLocationDomain = bddExAutomata.getDestLocationDomain(anAutomaton.getName());

            final NodeProxy sourceLocation = anEdge.getSource();
            final NodeProxy destLocation = anEdge.getTarget();

            final int sourceLocationIndex = bddExAutomata.getLocationIndex(anAutomaton, sourceLocation);
            final int destLocationIndex = bddExAutomata.getLocationIndex(anAutomaton, destLocation);

            final BDD sourceBDD = manager.getFactory().buildCube(sourceLocationIndex, sourceLocationDomain.vars());
            final BDD destBDD = manager.getFactory().buildCube(destLocationIndex, destLocationDomain.vars());

            BDD guardBDD = manager.getOneBDD();
            List<SimpleExpressionProxy> guards = null;
            if (anEdge.getGuardActionBlock() != null)
            {
                guards = anEdge.getGuardActionBlock().getGuards();
            }
            if (guards != null && guards.size() > 0)
            {
                /* Use it for heuristics*/
                eventIndex2GuardVariables.get(eventIndex).addAll(extractVariablesFromTheEdge(anEdge));
                guardBDD = manager.guard2BDD(guards.get(0));
            }

            sourceBDD.andWith(destBDD);
            sourceBDD.andWith(guardBDD);
            return sourceBDD;
        }

        /* Find which EFA variables are in this edge. */
        private HashSet<String> extractVariablesFromTheEdge(final EdgeProxy anEdge)
        {
            final HashSet<String> extractedVariables = new HashSet<String>();

            if (anEdge.getGuardActionBlock() != null && anEdge.getGuardActionBlock().getGuards() != null && !anEdge.getGuardActionBlock().getGuards().isEmpty())
            {
                final String guardString = anEdge.getGuardActionBlock().getGuards().get(0).toString();

                for (final VariableComponentProxy var : orgAutomata.getVars())
                {
                    if (guardString.contains(var.getName()))
                    {
                        extractedVariables.add(var.getName());
                    }
                }
            }
            return extractedVariables;
        }

        public BDD getTransitionBDDWithoutActions()
        {
            return transitionBDDWithoutActions;
        }

        public BDD[] getTransitionBDDCorrespondingToUpdatedVariablesWithoutActions()
        {
            return transitionBDDCorrespondingToUpdatedVariablesWithoutActions;
        }

        public BDD[] getTransitionBDDCorrespondingToUpdatedVariables()
        {
            return transitionBDDCorrespondingToUpdatedVariables;
        }

        public boolean isQualifiedForInitialComponent()
        {
            return qualifiedForInitialComponent;
        }

        public boolean isQualifiedForMarkedComponent()
        {
            return qualifiedForMarkedComponent;
        }
    }
}
