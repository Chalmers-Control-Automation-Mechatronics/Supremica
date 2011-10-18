package org.supremica.automata.BDD.EFA;

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
import java.util.Set;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDDomain;
import net.sourceforge.waters.model.module.VariableComponentProxy;
import org.supremica.automata.ExtendedAutomaton;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
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

    /* Define a map where the key is the index of an event in the index map while the value is the BDD expression of partitioned parts*/
    private final TIntObjectHashMap<BDD> eventToCompleteTransitionBDD;

    /* A field from BDDExtendedAutoma: an event can appear in any edges of any automaton. This map wants to trace them.*/
    private final TIntObjectHashMap<HashMap<ExtendedAutomaton, ArrayList<EdgeProxy>>> event2AutomatonsEdges;

    /* This is for heuristics. During the fix-pointed iteration, it is the key to pick the most useful BDD for the next round. Here we came up
    with several simple methods. Note that they are not proved yet...*/
    private final TIntObjectHashMap<TIntIntHashMap> event2ForwardInfluencedEvens;
    private final TIntObjectHashMap<TIntIntHashMap> event2BackwardInfluencedEvents;

    /** A map from an event index to all of the updated variable names during partitioning. */
    TIntObjectHashMap<HashSet<String>> eventIndex2UpdatedVariables;

    /** A map from an event index to the variables in the guard associated with the transition */
    private final TIntObjectHashMap<HashSet<String>> eventIndex2GuardVariables;

    /** An event index list in which the source states of the edges labeled by the event index are the initial locations. */
    private final TIntHashSet initialComponentCandidates;

    /** An event index list in which the target states of the edges labeled by the event index are the marked locations*/
    private final TIntHashSet markedComponentCandidates;

    /** An event index list with which the edges labeled are uncontrollable events */
    private final TIntHashSet uncontrollableComponentCandidates;

    /* Try another idea for variable based partitioning */
    /** The BDD array variableUpdateBDD keeps track of each variable updating for all events
     *   Each BDD in the array is like the form (sourceLocation, guards, event, action only concerning the variable, targetLocation)
     *   locations don't have to be global (add keep).
     *   Beside, target locations can be skipped.
     */
   BDD[] variableUpdateBDD;

   /** A map where the key is the event index and value is the BDD for transitions without actions. */
   TIntObjectHashMap<BDD> eventToTransitionBDDwithoutActions;


    public BDDExDisjEventDepSets(final BDDExtendedAutomata bddAutomata) {
        super(bddAutomata);
        this.event2AutomatonsEdges = bddAutomata.event2AutomatonsEdges;
        this.size = orgAutomata.unionAlphabet.size();

        this.eventToCompleteTransitionBDD = new TIntObjectHashMap<BDD>(size);

        this.event2ForwardInfluencedEvens = new TIntObjectHashMap<TIntIntHashMap>(size);
        this.event2BackwardInfluencedEvents = new TIntObjectHashMap<TIntIntHashMap>(size);

        this.eventToTransitionBDDwithoutActions = new TIntObjectHashMap<BDD>(size);

        this.eventIndex2UpdatedVariables = new TIntObjectHashMap<HashSet<String>>(size);
        this.eventIndex2GuardVariables = new TIntObjectHashMap<HashSet<String>>(size);

        this.initialComponentCandidates = new TIntHashSet();
        this.markedComponentCandidates = new TIntHashSet();
        this.uncontrollableComponentCandidates = new TIntHashSet();

        /* Create variableUpdateBDD array and initialize it */
        this.variableUpdateBDD = new BDD[orgAutomata.getVars().size()];
        for(int i = 0; i < variableUpdateBDD.length; i++)
            variableUpdateBDD[i] = manager.getZeroBDD();

        initialize();
    }

    @Override
    protected final void initialize() {

        /* Initialize eventIndex2UpdatedVariables and eventIndex2GuardVariables*/
        for(int i = 0; i < size; i++){
            eventIndex2GuardVariables.put(i, new HashSet<String>());
            eventIndex2UpdatedVariables.put(i, new HashSet<String>());
        }

        events2EventDisParDepSet = new TIntObjectHashMap<EventDisjParDepSet>(size);
        event2AutomatonsEdges.forEachKey(new TIntProcedure() {

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

    public TIntObjectHashMap<BDD> getEventToTransitionBDDwithoutActions(){
        return eventToTransitionBDDwithoutActions;
    }

    public BDD[] getVariableUpdateBDD(){
        return variableUpdateBDD;
    }

    @Override
    public TIntObjectHashMap<BDD> getComponentToComponentTransMap() {
        events2EventDisParDepSet.forEachEntry(new TIntObjectProcedure<EventDisjParDepSet>() {

            @Override
            public boolean execute(final int eventIndex, final EventDisjParDepSet t) {
                eventToCompleteTransitionBDD.put(eventIndex, t.eventForwardCompleteTransitions);
                return true;
            }
        });
        return eventToCompleteTransitionBDD;
    }

    /** These two heuristics need to be improved. */
    @Override
    protected TIntObjectHashMap<TIntIntHashMap> getForwardComponentToInfluencedComponentMap() {

        /* Use these two map about variables to construct this field */
        if (!orgAutomata.getVars().isEmpty()) {

            final int[] eventIndicesAsKeys = eventIndex2UpdatedVariables.keys();

            for (int i = 0; i < eventIndicesAsKeys.length; i++) {

                final int currentEventIndex = eventIndicesAsKeys[i];

                /* A set of variables updated by the event index */
                final HashSet<String> updatedVariablesByTheCurrentEvent = eventIndex2UpdatedVariables.get(currentEventIndex);
                final TIntIntHashMap tmp = new TIntIntHashMap();

                /* For the current event index, the updated variables might cause the guards of edges labeled by other events true
                    Go over each one, find the set of variables on the guards. Find the shared variables and the number is the value. */
                for (int j = 0; j < eventIndicesAsKeys.length; j++) {
                    if (j != i) {
                        final HashSet<String> guardVariablesByThisEvent = eventIndex2GuardVariables.get(eventIndicesAsKeys[j]);
                        final HashSet<String> differentVariables = new HashSet<String>(guardVariablesByThisEvent);
                        differentVariables.removeAll(updatedVariablesByTheCurrentEvent);
                        tmp.put(eventIndicesAsKeys[j], guardVariablesByThisEvent.size() - differentVariables.size());
                    }
                }

                event2ForwardInfluencedEvens.put(eventIndicesAsKeys[i], tmp);
            }
        } else {  /* pure DFAs */
            /* For DFAs, ituitive exhausive search will be used. It might cause state space exposion problem.
                Therefore, automaton based partition technique is recommended
             */
            int[] eventIndicesAsKeys = null;
            if(eventToCompleteTransitionBDD.isEmpty())
                eventIndicesAsKeys = getComponentToComponentTransMap().keys();
            else
                eventIndicesAsKeys = eventToCompleteTransitionBDD.keys();

            getComponentToComponentTransMap().keys();
            for (int i = 0; i < eventIndicesAsKeys.length; i++) {
                final TIntIntHashMap tmp = new TIntIntHashMap();

                for (int j = 0; j < eventIndicesAsKeys.length; j++) {
                    if (j != i) {
                        tmp.put(eventIndicesAsKeys[j], 0);
                    }
                }
                event2ForwardInfluencedEvens.put(eventIndicesAsKeys[i], tmp);
            }
        }
        return event2ForwardInfluencedEvens;
    }

    @Override
    protected TIntObjectHashMap<TIntIntHashMap> getBackwardComponentToInfluencedComponentMap() {

        /* The same as before. However this time we want to build the table for backward search.
            Once an event based BDD partition is chosen and executed. It is because the guards are true. Then we find
            the set of updated variables by other event based BDD partitions. From this, we build the heuristic table.
         */
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
                for (int j = 0; j < eventIndicesAsKeys.length; j++) {
                    if (j != i) {
                        final HashSet<String> updatedVariablesByThisEvent = eventIndex2UpdatedVariables.get(eventIndicesAsKeys[j]);
                        final HashSet<String> differentVariables = new HashSet<String>(updatedVariablesByThisEvent);
                        differentVariables.removeAll(guardVariablesByTheCurrentEvent);
                        tmp.put(eventIndicesAsKeys[j], updatedVariablesByThisEvent.size() - differentVariables.size());
                    }
                }

                event2BackwardInfluencedEvents.put(eventIndicesAsKeys[i], tmp);
            }
        } else {

            int[] eventIndicesAsKeys = null;
            if(eventToCompleteTransitionBDD.isEmpty())
                eventIndicesAsKeys = getComponentToComponentTransMap().keys();
            else
                eventIndicesAsKeys = eventToCompleteTransitionBDD.keys();

            for (int i = 0; i < eventIndicesAsKeys.length; i++) {
                final TIntIntHashMap tmp = new TIntIntHashMap();
                for (int j = 0; j < eventIndicesAsKeys.length; j++) {
                    if (j != i) {
                        tmp.put(eventIndicesAsKeys[j], 0);
                    }
                }
                event2BackwardInfluencedEvents.put(eventIndicesAsKeys[i], tmp);
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
        BDD uncontrollableTransitionRelationBDD = manager.getZeroBDD();
        for(final TIntIterator componentItr = getUncontrollableComponentCandidates().iterator(); componentItr.hasNext();){
            uncontrollableTransitionRelationBDD = uncontrollableTransitionRelationBDD.or(getComponentToComponentTransMap()
                    .get(componentItr.next()));
        }
        return uncontrollableTransitionRelationBDD;
    }

    /* For each event, initiate an instance of EventDisjParDepSet. */
    class EventDisjParDepSet {

        private final int eventIndex;
        private final HashMap<ExtendedAutomaton, ArrayList<EdgeProxy>> includingAutomata2Edges;
        private final BDD eventForwardCompleteTransitions;
        private BDD eventForwardTransitionWithoutActions;

        public EventDisjParDepSet(final int eventIndex, final HashMap<ExtendedAutomaton, ArrayList<EdgeProxy>> includingAutomata2Edges) {
            this.eventIndex = eventIndex;
            this.includingAutomata2Edges = includingAutomata2Edges;
            this.eventForwardCompleteTransitions = manager.getZeroBDD();
            this.eventForwardTransitionWithoutActions = manager.getZeroBDD();
            initialize();
        }

        private void initialize() {

            /* Create a BDD based on the event index, there is no need. */
            //final BDDDomain eventDomain = bddExAutomata.getEventDomain();
            //final BDD eventBDD = manager.getFactory().buildCube(eventIndex, eventDomain.vars());

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

            if(autIterator.hasNext()){

                final ExtendedAutomaton firstAutomaton = autIterator.next();
                final AutIncludingTransLabeledByEvent firstAutTransWithEvent = new AutIncludingTransLabeledByEvent(includingAutomata2Edges, eventIndex, firstAutomaton);

                transWithoutActions = firstAutTransWithEvent.getTransitionBDDWithoutActions();
                transCorrespondingToUpdatedVariablesWithoutActions = firstAutTransWithEvent.getTransitionBDDCorrespondingToUpdatedVariablesWithoutActions();
                transCorrespondingToUpdatedVariables = firstAutTransWithEvent.getTransitionBDDCorrespondingToUpdatedVariables();

                for(int i = 0; i < transCorrespondingToUpdatedVariables.length; i++){
                    conjunctiveTransToUpdatedVariables[i] = transCorrespondingToUpdatedVariables[i].id();
                }

                eventIsQualifiedForInitialComponent = firstAutTransWithEvent.isQualifiedForInitialComponent();
                eventIsQualifiedForMarkedComponent = firstAutTransWithEvent.isQualifiedForMarkedComponent();
            }

            if (includedAutomata.size() == 1) {
                for (final VariableComponentProxy var : orgAutomata.getVars()) {
                    final int varIndex = theIndexMap.getVariableIndex(var);
                    final BDD noneUpdateVar = bddExAutomata.BDDBitVecTargetVarsMap.get(var.getName()).equ(bddExAutomata.BDDBitVecSourceVarsMap.get(var.getName()));
                    transCorrespondingToUpdatedVariables[varIndex] = transCorrespondingToUpdatedVariablesWithoutActions[varIndex].ite(transCorrespondingToUpdatedVariables[varIndex], noneUpdateVar);
                    variableUpdateBDD[varIndex].orWith(conjunctiveTransToUpdatedVariables[varIndex]);
                }

                if(eventIsQualifiedForInitialComponent)
                    initialComponentCandidates.add(eventIndex);

                if(eventIsQualifiedForMarkedComponent)
                    markedComponentCandidates.add(eventIndex);
            }

            while (autIterator.hasNext()) {

                final ExtendedAutomaton currAutomaton = autIterator.next();
                final AutIncludingTransLabeledByEvent currAutIncTrans = new AutIncludingTransLabeledByEvent(includingAutomata2Edges, eventIndex, currAutomaton);

                final BDD currTransWithoutActions = currAutIncTrans.getTransitionBDDWithoutActions();
                final BDD[] currTransCorrespondingToUpdatedVariables = currAutIncTrans.getTransitionBDDCorrespondingToUpdatedVariables();
                final BDD[] currTransCorrespondingToUpdatedVariablesWithoutActions = currAutIncTrans.getTransitionBDDCorrespondingToUpdatedVariablesWithoutActions();

                for (final VariableComponentProxy var : orgAutomata.getVars()) {

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

                /** Try another idea */
                for (final VariableComponentProxy var : orgAutomata.getVars()) {

                    final int varIndex = theIndexMap.getVariableIndex(var);

                    final BDD t = transCorrespondingToUpdatedVariables[varIndex];

                    variableUpdateBDD[varIndex] = variableUpdateBDD[varIndex].or(t);
                }

                for (final VariableComponentProxy var : orgAutomata.getVars()) {
                    final int varIndex = theIndexMap.getVariableIndex(var);
                    final BDD compensate = bddExAutomata.BDDBitVecTargetVarsMap.get(var.getName()).equ(bddExAutomata.BDDBitVecSourceVarsMap.get(var.getName()));
                    transCorrespondingToUpdatedVariables[varIndex] = transCorrespondingToUpdatedVariablesWithoutActions[varIndex].ite(transCorrespondingToUpdatedVariables[varIndex], compensate);
                }

                transWithoutActions.andWith(currTransWithoutActions);

                /* Update eventIsQualifiedForInitialComponent and eventIsQualifiedForMarkedComponent */
                eventIsQualifiedForInitialComponent = eventIsQualifiedForInitialComponent && currAutIncTrans.isQualifiedForInitialComponent();
                eventIsQualifiedForMarkedComponent = eventIsQualifiedForMarkedComponent && currAutIncTrans.isQualifiedForMarkedComponent();
            }

            final BDD tmp = manager.getOneBDD();
            for (int i = 0; i < bddExAutomata.orgExAutomata.getVars().size(); i++) {

                final String varName = theIndexMap.getVariableAt(i).getName();
                transCorrespondingToUpdatedVariables[i] = transCorrespondingToUpdatedVariables[i].and(bddExAutomata.BDDBitVecSourceVarsMap.get(varName)
                        .lte(bddExAutomata.getMaxBDDBitVecOf(varName)));
                transCorrespondingToUpdatedVariables[i] = transCorrespondingToUpdatedVariables[i].and(bddExAutomata.BDDBitVecTargetVarsMap.get(varName)
                        .lte(bddExAutomata.getMaxBDDBitVecOf(varName)));
                transCorrespondingToUpdatedVariables[i] = transCorrespondingToUpdatedVariables[i].and(bddExAutomata.BDDBitVecSourceVarsMap.get(varName)
                        .gte(bddExAutomata.getMinBDDBitVecOf(varName)));
                transCorrespondingToUpdatedVariables[i] = transCorrespondingToUpdatedVariables[i].and(bddExAutomata.BDDBitVecTargetVarsMap.get(varName)
                        .gte(bddExAutomata.getMinBDDBitVecOf(varName)));

                tmp.andWith(transCorrespondingToUpdatedVariables[i]);
            }



            /* Merge with "keep" BDD */
            final Set<ExtendedAutomaton> includedAutomataSet = includingAutomata2Edges.keySet();
            BDD keep = manager.getOneBDD();
            for (final ExtendedAutomaton extendedAutomaton : theExAutomata) {
                if (!includedAutomataSet.contains(extendedAutomaton)) {
                    final BDDExtendedAutomaton bddAutomaton = bddExAutomata.automatonToBDDAutomatonMap.get(extendedAutomaton);
                    keep = keep.and(bddAutomaton.getSelfLoopsBDD());
                }
            }

            transWithoutActions.andWith(keep);

            eventForwardTransitionWithoutActions = eventForwardTransitionWithoutActions.or(transWithoutActions);

            eventToTransitionBDDwithoutActions.put(eventIndex, transWithoutActions);

            transWithoutActions.andWith(tmp);

            final BDD isolatedTransBDD = transWithoutActions;

            eventForwardCompleteTransitions.orWith(isolatedTransBDD);

            /* Determine whether or not to put this event as the initialComponentCandidate set or markedComponentCandidate set. */
            if(eventIsQualifiedForInitialComponent)
                initialComponentCandidates.add(eventIndex);

            if(eventIsQualifiedForMarkedComponent)
                markedComponentCandidates.add(eventIndex);

            /* If the event is uncontrollable, put it into the uncontrollableComponentCandidates */
            if(bddExAutomata.plantUncontrollableEventIndexList.contains(eventIndex) || bddExAutomata.specUncontrollableEventIndexList.contains(eventIndex))
                uncontrollableComponentCandidates.add(eventIndex);
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
            public AutIncludingTransLabeledByEvent(final HashMap<ExtendedAutomaton, ArrayList<EdgeProxy>> includingAutomata2Edges,
                    final int eventIndex, final ExtendedAutomaton anAutomaton) {

                this.includingAutomata2Edges = includingAutomata2Edges;
                this.eventIndex = eventIndex;
                this.anAutomaton = anAutomaton;
                /* Create these two BDD arrays */
                this.transitionBDDCorrespondingToUpdatedVariablesWithoutActions = new BDD[orgAutomata.getVars().size()];
                this.transitionBDDCorrespondingToUpdatedVariables = new BDD[orgAutomata.getVars().size()];

                initialize();
            }

            private void initialize() {

                /* First initialize the BDD and two BDD arrays*/
                transitionBDDWithoutActions = manager.getZeroBDD(); // It should be zero-BDD, because we want the union of BDD for all edges in one automaton
                for (final VariableComponentProxy var : orgAutomata.getVars()) {
                    transitionBDDCorrespondingToUpdatedVariablesWithoutActions[theIndexMap.getVariableIndexByName(var.getName())] = manager.getZeroBDD();
                    transitionBDDCorrespondingToUpdatedVariables[theIndexMap.getVariableIndexByName(var.getName())] = manager.getZeroBDD();
                }

                /* Go through each edge and build these two BDD arrays*/
                for (final Iterator<EdgeProxy> edgeIterator = includingAutomata2Edges.get(anAutomaton).iterator(); edgeIterator.hasNext();) {

                    final EdgeProxy anEdge = edgeIterator.next();

                    if(anAutomaton.isLocationInitial(anEdge.getSource()))
                        qualifiedForInitialComponent = true;

                    if(anAutomaton.isLocationAccepted(anEdge.getTarget()))
                        qualifiedForMarkedComponent = true;

                    /* Construct the BDD for the edge but exclude the actions. */
                    final BDD transitionWithoutActionsOnCurrEdge = getEdgeBDDWithoutActions(anAutomaton, anEdge);

                    /* Keep track of the updated variable index */
                    final TIntHashSet updatedVariableIndexSet = new TIntHashSet();

                    /* Try another idea */
                    final TIntObjectHashMap<BDD> varIndex2Action = new TIntObjectHashMap<BDD>(orgAutomata.getVars().size());

                    List<BinaryExpressionProxy> actions = null;
                    final BDD actionsBDD = manager.getOneBDD();

                    if (anEdge.getGuardActionBlock() != null && anEdge.getGuardActionBlock().getActions() != null && !anEdge.getGuardActionBlock().getActions().isEmpty()) {

                        actions = anEdge.getGuardActionBlock().getActions();
                        /* Iterate each action to extract variables and build BDD*/
                        for (final BinaryExpressionProxy anAction : actions) {
                            final String updatedVariableOfThisAction = ((SimpleIdentifierProxy) anAction.getLeft()).getName();
                            final int varIndex = theIndexMap.getVariableIndexByName(updatedVariableOfThisAction);
                            updatedVariableIndexSet.add(theIndexMap.getVariableIndexByName(updatedVariableOfThisAction));
                            /* Use it for heuristics*/
                            eventIndex2UpdatedVariables.get(eventIndex).add(updatedVariableOfThisAction);
                            /*BDD-AND each actionBDD */
                            varIndex2Action.put(varIndex, transitionWithoutActionsOnCurrEdge.and(manager.action2BDD(anAction)));
                            actionsBDD.andWith(manager.action2BDD(anAction));
                        }
                    }

                    /* Now we have, for each edge (1) forward BDD transition without actions (2) actionsBDD (3) the updated variable set
                    Next, we merge them together
                    (1) based on the indices of updated variables, OR with them on the BDD array and return this BDD array
                    (2) Or with transitionBDDWithoutActions with transitionWithoutActionsOnCurrEdge
                     */
                    for (final TIntIterator itr = updatedVariableIndexSet.iterator(); itr.hasNext();) {
                        final int variableIndex = itr.next();
                        transitionBDDCorrespondingToUpdatedVariablesWithoutActions[variableIndex]
                                = transitionBDDCorrespondingToUpdatedVariablesWithoutActions[variableIndex].or(transitionWithoutActionsOnCurrEdge);
                        //transitionBDDCorrespondingToUpdatedVariables[variableIndex]
                        //         =transitionBDDCorrespondingToUpdatedVariables[variableIndex].or(transitionWithoutActionsOnCurrEdge.and(actionsBDD));
                    }

                    varIndex2Action.forEachEntry(new TIntObjectProcedure<BDD>() {

                        @Override
                        public boolean execute(final int varIndex, final BDD action) {
                            transitionBDDCorrespondingToUpdatedVariables[varIndex]
                                    =  transitionBDDCorrespondingToUpdatedVariables[varIndex].or(action);
                            return true;
                        }
                    });

                    transitionBDDWithoutActions.orWith(transitionWithoutActionsOnCurrEdge);
                }
            }

            private BDD getEdgeBDDWithoutActions(final ExtendedAutomaton anAutomaton, final EdgeProxy anEdge) {

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
                if (anEdge.getGuardActionBlock() != null) {
                    guards = anEdge.getGuardActionBlock().getGuards();
                }
                if (guards != null && guards.size() > 0) {
                    /* Use it for heuristics*/
                    eventIndex2GuardVariables.get(eventIndex).addAll(extractVariablesFromTheEdge(anEdge));
                    guardBDD = manager.guard2BDD(guards.get(0));
                }

                sourceBDD.andWith(destBDD);
                sourceBDD.andWith(guardBDD);
                return sourceBDD;
            }

            /* Find which EFA variables are in this edge. */
            private HashSet<String> extractVariablesFromTheEdge(final EdgeProxy anEdge) {

                final HashSet<String> extractedVariables = new HashSet<String>();

                if (anEdge.getGuardActionBlock() != null && anEdge.getGuardActionBlock().getGuards() != null && !anEdge.getGuardActionBlock().getGuards().isEmpty()) {

                    final String guardString = anEdge.getGuardActionBlock().getGuards().get(0).toString();

                    for (final VariableComponentProxy var : orgAutomata.getVars()) {

                        if (guardString.contains(var.getName())) {
                            extractedVariables.add(var.getName());
                        }
                    }
                }
                return extractedVariables;
            }

            public BDD getTransitionBDDWithoutActions() {
                return transitionBDDWithoutActions;
            }

            public BDD[] getTransitionBDDCorrespondingToUpdatedVariablesWithoutActions() {
                return transitionBDDCorrespondingToUpdatedVariablesWithoutActions;
            }

            public BDD[] getTransitionBDDCorrespondingToUpdatedVariables() {
                return transitionBDDCorrespondingToUpdatedVariables;
            }

            public boolean isQualifiedForInitialComponent(){
                return qualifiedForInitialComponent;
            }

            public boolean isQualifiedForMarkedComponent(){
                return qualifiedForMarkedComponent;
            }
        }
    }

    /* Implement different workset algorithm */
//    @Override
//    public BDD forwardWorkSetAlgorithm(BDD initialStates) {
//
//        System.err.println("Execute event-based specific workset forward search: ");
//
//        /* Reset the workset */
//        workset.reset();
//
//        BDD currentReachableStatesBDD = initialStates;
//        BDD previousReachablestatesBDD = null;
//
//        boolean forward = true;
//
//        while (!workset.empty()) {
//
//            previousReachablestatesBDD = currentReachableStatesBDD.id();
//            int choice = workset.pickOne(forward);
//
//            BDD currentTransitionOnly = getComponentToComponentOnlyTransMap().get(choice);
//            BDD currentTransitionAction = getComponentToComponentActionMap().get(choice);
//
//            BDD caredTansitionOnly =null;
//            do {
//                caredTansitionOnly = currentReachableStatesBDD.and(currentTransitionOnly);
//                BDD nextStates = caredTansitionOnly.and(currentTransitionAction)
//                        .exist(bddExAutomata.sourceStateVariables)
//                        .replaceWith(bddExAutomata.destToSourceLocationPairing)
//                        .replaceWith(bddExAutomata.destToSourceVariablePairing);
//                currentReachableStatesBDD.orWith(nextStates);
//            } while (!currentReachableStatesBDD.and(currentTransitionOnly).equals(caredTansitionOnly));
//
//            workset.advance(choice, !currentReachableStatesBDD.equals(previousReachablestatesBDD));
//        }
//
//        /* Use the intuitive way to test */
//        /*do {
//
//            int[] eventIndexAsKeys = getComponentToComponentOnlyTransMap().keys();
//
//            previousReachablestatesBDD = currentReachableStatesBDD.id();
//
//            for (int i = 0; i < eventIndexAsKeys.length; i++) {
//
//                int currEventIndex = eventIndexAsKeys[i];
//                BDD currentTransitionOnly = getComponentToComponentOnlyTransMap().get(currEventIndex);
//                BDD currentTransitionAction = getComponentToComponentActionMap().get(currEventIndex);
//
//                BDD caredTansitionOnly = currentReachableStatesBDD.and(currentTransitionOnly);
//
//                BDD resultValuesOfVariables = caredTansitionOnly.and(currentTransitionAction)
//                        .exist(bddExAutomata.sourceStateVariables)
//                        .replaceWith(bddExAutomata.destToSourceLocationPairing)
//                        .replaceWith(bddExAutomata.destToSourceVariablePairing);
//
//                currentReachableStatesBDD.orWith(resultValuesOfVariables);
//
//            }
//        } while (!currentReachableStatesBDD.equals(previousReachablestatesBDD));*/
//        return currentReachableStatesBDD;
//    }
}
