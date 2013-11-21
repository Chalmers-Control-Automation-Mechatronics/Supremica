package org.supremica.automata.BDD.EFA;

import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntObjectProcedure;
import gnu.trove.procedure.TIntProcedure;
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
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;
import org.supremica.automata.ExtendedAutomaton;
import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;

/**
 * @author  Zhennan
 * @version 2.0
 */
public class BDDPartitionSetEve extends BDDPartitionSet {

    @SuppressWarnings("unused")
    private static Logger logger = LoggerFactory.createLogger(BDDPartitionSetEve.class);

    /**
     * A map where the key is the index of an event in the index map while the value is the BDD expression of partition.
     */
    private final TIntObjectHashMap<BDD> eventToCompleteTransitionBDD;

    /**
     * A field from BDDExtendedAutoma: an event can appear in any edges of any automaton. This map wants to trace them.
     */
    private final TIntObjectHashMap<HashMap<ExtendedAutomaton, ArrayList<EdgeProxy>>> event2AutomatonsEdges;

    /**
     * A map from an event index to all of the updated variable names during partitioning.
     */
    TIntObjectHashMap<HashSet<String>> eventIndex2UpdatedVariables;

    /**
     * A map from an event index to the variables in the guard associated with the transition
     */
    private final TIntObjectHashMap<HashSet<String>> eventIndex2GuardVariables;

    /**
     * Regarding the forward reachability computation, the map maps an event index to the union of
     * D^{e}(\sigma) and D^{v}(\sigma). Refer to the EFA partitioning paper.
     */
    private TIntObjectHashMap<TIntHashSet> forwardDependencyMap;

    /** forward D^{v}(\sigma) */
    private TIntObjectHashMap<TIntHashSet> forwardVarDependencyMap;

    /** forward D^{e}(\sigma) */
    private TIntObjectHashMap<TIntHashSet> forwardEveDependencyMap;

    /** backward D^{v}(\sigma) */
    private TIntObjectHashMap<TIntHashSet> backwardVarDependencyMap;

    /** backward D^{e}(\sigma) */
    private TIntObjectHashMap<TIntHashSet> backwardEveDependencyMap;

    /**
     * Regarding the backward reachability computation, the map maps an event index to the union of
     * D^{e}(\sigma) and D^{v}(\sigma). Refer to the EFA partitioning paper.
     */
    private TIntObjectHashMap<TIntHashSet> backwardDependencyMap;


    /**
     * An event index list in which the source states of the edges labeled by the event index are the initial locations.
     */
    private final TIntHashSet initialComponentCandidates;

    /**
     * An event index list in which the target states of the edges labeled by the event index are the marked locations
     */
    private final TIntHashSet markedComponentCandidates;

    /**
     * An event index list with which the edges labeled are uncontrollable events
     */
    private final TIntHashSet uncontrollableComponentCandidates;

    /**
     * Size of the used events.
     */
    private int size;

    public BDDPartitionSetEve(final BDDExtendedAutomata bddExAutomata) {
        super(bddExAutomata);

        this.event2AutomatonsEdges = bddExAutomata.event2AutomatonsEdges;

        /* Test event2AutomatonsEdges
        event2AutomatonsEdges.forEachEntry(new TIntObjectProcedure<HashMap<ExtendedAutomaton, ArrayList<EdgeProxy>>>() {

            @Override
            public boolean execute(int eventIndex, HashMap<ExtendedAutomaton, ArrayList<EdgeProxy>> aut2Edges) {
                System.err.println(theIndexMap.getEventAt(eventIndex).getName() + " :");
                for(Map.Entry<ExtendedAutomaton, ArrayList<EdgeProxy>> entry: aut2Edges.entrySet()) {
                    ExtendedAutomaton aut = entry.getKey();
                    System.err.println("automaton: " + aut.getName());
                    for(Iterator<EdgeProxy> edgeItr = entry.getValue().iterator(); edgeItr.hasNext();) {
                        EdgeProxy anEdge = edgeItr.next();
                        System.err.println(anEdge.getSource().getName() +
                                " --> " + anEdge.getTarget().getName()  + "(marked:" + aut.isLocationAccepted(anEdge.getTarget()) + ")");
                    }
                }
                System.err.println();
                return true;
            }
        });*/

        this.size = event2AutomatonsEdges.size();

        this.eventToCompleteTransitionBDD = new TIntObjectHashMap<BDD>(size);

        this.eventIndex2UpdatedVariables = new TIntObjectHashMap<HashSet<String>>(size);
        this.eventIndex2GuardVariables = new TIntObjectHashMap<HashSet<String>>(size);

        this.initialComponentCandidates = new TIntHashSet();
        this.markedComponentCandidates = new TIntHashSet();
        this.uncontrollableComponentCandidates = new TIntHashSet();

        event2AutomatonsEdges.forEach(new TIntProcedure() {

            @Override
            public boolean execute(final int eventIndex) {
                eventIndex2GuardVariables.put(eventIndex, new HashSet<String>());
                eventIndex2UpdatedVariables.put(eventIndex, new HashSet<String>());
                return true;
            }
        });

        event2AutomatonsEdges.forEachKey(new TIntProcedure() {

            @Override
            public boolean execute(final int eventIndex) {
                final HashMap<ExtendedAutomaton, ArrayList<EdgeProxy>> automatonsEdges = event2AutomatonsEdges.get(eventIndex);
                /* Use the event index and edges where it appears in to construct the instance of EventDisjParDepSet. */
                final EventDisjParDepSet eventDisParSet = new EventDisjParDepSet(eventIndex, automatonsEdges);
                eventToCompleteTransitionBDD.put(eventIndex, eventDisParSet.eventForwardTransitionBDD);
                return true;
            }
        });
    }

    @Override
    public TIntObjectHashMap<BDD> getCompIndexToCompBDDMap() {
        return eventToCompleteTransitionBDD;
    }

    @Override
    public TIntObjectHashMap<TIntHashSet> getForwardDependentComponentMap() {
        if (forwardDependencyMap == null) {
            forwardDependencyMap = new TIntObjectHashMap<TIntHashSet>(size);
            buildForwardBackwardVarDependencyMap();
            buildForwardEveDependencyMap();
            // merge
            forwardEveDependencyMap.forEachEntry(new TIntObjectProcedure<TIntHashSet>() {

                @Override
                public boolean execute(final int eventIndex, final TIntHashSet eventDepEventIndexSet) {
                    if (!forwardDependencyMap.containsKey(eventIndex))
                        forwardDependencyMap.put(eventIndex, eventDepEventIndexSet);
                    else
                        forwardDependencyMap.get(eventIndex).addAll(eventDepEventIndexSet.toArray());
                    return true;
                }
            });

            forwardVarDependencyMap.forEachEntry(new TIntObjectProcedure<TIntHashSet>() {

                @Override
                public boolean execute(final int eventIndex, final TIntHashSet varDepEventIndexSet) {
                    if (!forwardDependencyMap.containsKey(eventIndex))
                        forwardDependencyMap.put(eventIndex, varDepEventIndexSet);
                    else
                        forwardDependencyMap.get(eventIndex).addAll(varDepEventIndexSet.toArray());
                    return true;
                }
            });
        }
        return forwardDependencyMap;
    }

    @Override
    public TIntObjectHashMap<TIntHashSet> getBackwardDependentComponentMap() {
        if (backwardDependencyMap == null) {
            backwardDependencyMap = new TIntObjectHashMap<TIntHashSet>(size);
            buildForwardBackwardVarDependencyMap();
            buildBackwardEveDependencyMap();
            // merge
            backwardEveDependencyMap.forEachEntry(new TIntObjectProcedure<TIntHashSet>() {

                @Override
                public boolean execute(final int eventIndex, final TIntHashSet eventDepEventIndexSet) {
                    if (!backwardDependencyMap.containsKey(eventIndex))
                        backwardDependencyMap.put(eventIndex, eventDepEventIndexSet);
                    else
                        backwardDependencyMap.get(eventIndex).addAll(eventDepEventIndexSet.toArray());
                    return true;
                }
            });

            backwardVarDependencyMap.forEachEntry(new TIntObjectProcedure<TIntHashSet>() {

                @Override
                public boolean execute(final int eventIndex, final TIntHashSet varDepEventIndexSet) {
                    if (!backwardDependencyMap.containsKey(eventIndex))
                        backwardDependencyMap.put(eventIndex, varDepEventIndexSet);
                    else
                        backwardDependencyMap.get(eventIndex).addAll(varDepEventIndexSet.toArray());
                    return true;
                }
            });
        }
        return backwardDependencyMap;
    }

    // build both forward and backward variable dependency set together.
    private void buildForwardBackwardVarDependencyMap() {

        if (forwardVarDependencyMap == null || backwardVarDependencyMap == null) {
            forwardVarDependencyMap = new TIntObjectHashMap<TIntHashSet>(size);
            backwardVarDependencyMap = new TIntObjectHashMap<TIntHashSet>(size);

            final int[] eventIndexArray = event2AutomatonsEdges.keys();
            for (int i = 0; i < eventIndexArray.length; i++) {
                final int iEventIndex = eventIndexArray[i];
                final HashSet<String> updatedVars = eventIndex2UpdatedVariables.get(iEventIndex);
                for (int j = 0; j < eventIndexArray.length; j++) {
                    if (j != i) {
                        final int jEventIndex = eventIndexArray[j];
                        final Set<String> tmpGuardVars = new HashSet<String>(eventIndex2GuardVariables.get(jEventIndex));
                        tmpGuardVars.retainAll(updatedVars);
                        		
                        if (!tmpGuardVars.isEmpty() || eventIndex2GuardVariables.get(jEventIndex).isEmpty() 
                                || !eventIndex2UpdatedVariables.get(jEventIndex).isEmpty()) {
                            if (!forwardVarDependencyMap.contains(iEventIndex)) {
                                forwardVarDependencyMap.put(iEventIndex, new TIntHashSet());
                            }
                            forwardVarDependencyMap.get(iEventIndex).add(jEventIndex);

                            if (!backwardVarDependencyMap.contains(jEventIndex)) {
                                backwardVarDependencyMap.put(jEventIndex, new TIntHashSet());
                            }
                            backwardVarDependencyMap.get(jEventIndex).add(iEventIndex);
                        }
                    }
                }
            }
        }
    }

    /* get all of the successor-events for each event. */
    private void buildForwardEveDependencyMap() {

        if (forwardEveDependencyMap == null) {
            forwardEveDependencyMap = new TIntObjectHashMap<TIntHashSet>(size);
            event2AutomatonsEdges.forEachEntry(new TIntObjectProcedure<HashMap<ExtendedAutomaton, ArrayList<EdgeProxy>>>() {

                @Override
                public boolean execute(final int eventIndex, final HashMap<ExtendedAutomaton, ArrayList<EdgeProxy>> autEdges) {
                    for (final Map.Entry<ExtendedAutomaton, ArrayList<EdgeProxy>> entry: autEdges.entrySet()) {
                       final ExtendedAutomaton currAut = entry.getKey();
                       for(final EdgeProxy anEdge: entry.getValue()) {
                           final NodeProxy targetLocation = anEdge.getTarget();
                           for (final EdgeProxy anOutgoingEdge: currAut.getLocationToOutgoingEdgesMap().get(targetLocation)) {
                               for (final Proxy anEventProxy: anOutgoingEdge.getLabelBlock().getEventIdentifierList()) {
                                   final String eventName = ((SimpleIdentifierProxy) anEventProxy).getName();
                                   final EventDeclProxy anDeclEvent = bddExAutomata.getExtendedAutomata().eventIdToProxy(eventName);
                                   if (!forwardEveDependencyMap.containsKey(eventIndex)) {
                                       forwardEveDependencyMap.put(eventIndex, new TIntHashSet());
                                   }
                                   forwardEveDependencyMap.get(eventIndex).add(theIndexMap.getEventIndex(anDeclEvent));
                               }
                           }
                       }
                    }
                    return true;
                }
            });
        }

        // debug the successors of each event
//        forwardEveDependencyMap.forEachEntry(new TIntObjectProcedure<TIntHashSet>() {
//            @Override
//            public boolean execute(int eventIndex, TIntHashSet successors) {
//                System.err.println(theIndexMap.getEventAt(eventIndex).getName() + " successor events are: ");
//                successors.forEach(new TIntProcedure() {
//
//                    @Override
//                    public boolean execute(int successor) {
//                        System.err.print(" " + theIndexMap.getEventAt(successor).getName() + " ;");
//                        return true;
//                    }
//                });
//                System.err.println();
//                return true;
//            }
//        });
    }

    /* get all of the predecessor-events for each event. */
    private void buildBackwardEveDependencyMap() {

        if (backwardEveDependencyMap == null) {
            backwardEveDependencyMap = new TIntObjectHashMap<TIntHashSet>(size);
            event2AutomatonsEdges.forEachEntry(new TIntObjectProcedure<HashMap<ExtendedAutomaton, ArrayList<EdgeProxy>>>() {

                @Override
                public boolean execute(final int eventIndex, final HashMap<ExtendedAutomaton, ArrayList<EdgeProxy>> autEdges) {
                    for (final Map.Entry<ExtendedAutomaton, ArrayList<EdgeProxy>> entry: autEdges.entrySet()) {
                        final ExtendedAutomaton currAut = entry.getKey();
                        for (final EdgeProxy anEdge: entry.getValue()) {
                            final NodeProxy sourceLocation = anEdge.getSource();
                            for (final EdgeProxy anIngoingEdge: currAut.getLocationToIngoingEdgesMap().get(sourceLocation)) {
                                for (final Proxy anEventProxy: anIngoingEdge.getLabelBlock().getEventIdentifierList()) {
                                    final String eventName = ((SimpleIdentifierProxy) anEventProxy).getName();
                                    final EventDeclProxy anDeclEvent = bddExAutomata.getExtendedAutomata().eventIdToProxy(eventName);
                                    if (!backwardEveDependencyMap.containsKey(eventIndex)) {
                                        backwardEveDependencyMap.put(eventIndex, new TIntHashSet());
                                    }
                                    backwardEveDependencyMap.get(eventIndex).add(theIndexMap.getEventIndex(anDeclEvent));
                                }
                            }
                        }
                    }
                    return true;
                }
            });
        }
    }

    @Override
    public TIntHashSet getInitialComponentCandidates() {
        return new TIntHashSet(initialComponentCandidates.toArray());
    }

    @Override
    public TIntHashSet getMarkedComponentCandidates() {
        return new TIntHashSet(markedComponentCandidates.toArray());
    }

    private TIntHashSet getUncontrollableComponentCandidates() {
         return uncontrollableComponentCandidates;
    }

    @Override
    public BDD getUncontrollableTransitionRelationBDD() {
        BDD uncontrollableTransitionRelationBDD = manager.getZeroBDD();
        for(final TIntIterator componentItr = getUncontrollableComponentCandidates().iterator(); componentItr.hasNext();){
            uncontrollableTransitionRelationBDD = uncontrollableTransitionRelationBDD
                                                            .or(getCompIndexToCompBDDMap().get(componentItr.next()));
        }
        return uncontrollableTransitionRelationBDD;
    }

    //#################################################################
    //########## INTERNAL CLASS############################################
    /* For each event, initiate an instance of EventDisjParDepSet. */
    class EventDisjParDepSet {

        private final int eventIndex;
        private final HashMap<ExtendedAutomaton, ArrayList<EdgeProxy>> includingAutomata2Edges;
        private final BDD eventForwardTransitionBDD;

        private EventDisjParDepSet(final int eventIndex, final HashMap<ExtendedAutomaton, ArrayList<EdgeProxy>> includingAutomata2Edges) {
            this.eventIndex = eventIndex;
            this.includingAutomata2Edges = includingAutomata2Edges;
            this.eventForwardTransitionBDD = manager.getZeroBDD();
            if (!includingAutomata2Edges.isEmpty()) {
                buildTansitionBDD();
            }
        }

        private void buildTansitionBDD() {

            final Set<ExtendedAutomaton> includedAutomata = includingAutomata2Edges.keySet();
            final Iterator<ExtendedAutomaton> autIterator = includedAutomata.iterator();

            BDD transWithoutActions = null;
            BDD[] transCorrespondingToUpdatedVariablesWithoutActions = null;
            BDD[] transCorrespondingToUpdatedVariables = null;

            /*
             * For some automata, there is at least edge labeled by the event index comes from an initial location, but it doesn't mean
             * this event is qualified for the initial component during reachability search. We have to make sure that the event is qualified
             * for all of the automata. Here is the boolean variable which can help us do that.
             */
            boolean eventIsQualifiedForInitialComponent = true;
            boolean eventIsQualifiedForMarkedComponent = true;
            boolean allVarPossiblyUpdated = true;

            if (autIterator.hasNext()) {

                final ExtendedAutomaton firstAutomaton = autIterator.next();
                final AutIncludingTransLabeledByEvent firstAutTransWithEvent
                        = new AutIncludingTransLabeledByEvent(includingAutomata2Edges.get(firstAutomaton), eventIndex, firstAutomaton);

                transWithoutActions = firstAutTransWithEvent.transitionBDDWithoutActions;
                transCorrespondingToUpdatedVariablesWithoutActions
                                                    = firstAutTransWithEvent.transitionBDDCorrespondingToUpdatedVariablesWithoutActions;
                transCorrespondingToUpdatedVariables = firstAutTransWithEvent.transitionBDDCorrespondingToUpdatedVariables;

                eventIsQualifiedForInitialComponent = eventIsQualifiedForInitialComponent && firstAutTransWithEvent.qualifiedForInitialComponent;
                eventIsQualifiedForMarkedComponent = eventIsQualifiedForMarkedComponent && firstAutTransWithEvent.qualifiedForMarkedComponent;
                allVarPossiblyUpdated = allVarPossiblyUpdated && firstAutTransWithEvent.allVarPossiblyUpdated;
            }

            if (includedAutomata.size() == 1) {
                for (final VariableComponentProxy var : orgAutomata.getVars()) {
                    final int varIndex = theIndexMap.getVariableIndex(var);
                    final BDD noneUpdateVar = bddExAutomata.getBDDBitVecTarget(varIndex).equ(bddExAutomata.getBDDBitVecSource(varIndex));
                    transCorrespondingToUpdatedVariables[varIndex] = transCorrespondingToUpdatedVariablesWithoutActions[varIndex]
                                                                    .ite(transCorrespondingToUpdatedVariables[varIndex], noneUpdateVar);
                }
            }

            while (autIterator.hasNext()) {

                final ExtendedAutomaton currAutomaton = autIterator.next();
                final AutIncludingTransLabeledByEvent currAutIncTrans
                        = new AutIncludingTransLabeledByEvent(includingAutomata2Edges.get(currAutomaton), eventIndex, currAutomaton);

                final BDD currTransWithoutActions = currAutIncTrans.transitionBDDWithoutActions;
                final BDD[] currTransCorrespondingToUpdatedVariables = currAutIncTrans.transitionBDDCorrespondingToUpdatedVariables;
                final BDD[] currTransCorrespondingToUpdatedVariablesWithoutActions
                                                        = currAutIncTrans.transitionBDDCorrespondingToUpdatedVariablesWithoutActions;

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

                for (final VariableComponentProxy var : orgAutomata.getVars()) {
                    final int varIndex = theIndexMap.getVariableIndex(var);
                    final BDD compensate = bddExAutomata.getBDDBitVecTarget(varIndex).equ(bddExAutomata.getBDDBitVecSource(varIndex));
                    transCorrespondingToUpdatedVariables[varIndex] = transCorrespondingToUpdatedVariablesWithoutActions[varIndex]
                                                                    .ite(transCorrespondingToUpdatedVariables[varIndex], compensate);
                }

                transWithoutActions.andWith(currTransWithoutActions);

                /* Update eventIsQualifiedForInitialComponent and eventIsQualifiedForMarkedComponent */
                eventIsQualifiedForInitialComponent = eventIsQualifiedForInitialComponent && currAutIncTrans.qualifiedForInitialComponent;
                eventIsQualifiedForMarkedComponent = eventIsQualifiedForMarkedComponent && currAutIncTrans.qualifiedForMarkedComponent;
                
                if(allVarPossiblyUpdated) {
                    allVarPossiblyUpdated = allVarPossiblyUpdated && currAutIncTrans.allVarPossiblyUpdated;
                }
            }

            final BDD tmp = manager.getOneBDD();
            for (final VariableComponentProxy var : bddExAutomata.orgExAutomata.getVars()) {
                final int varIndex = bddExAutomata.theIndexMap.getVariableIndex(var);
                transCorrespondingToUpdatedVariables[varIndex] = transCorrespondingToUpdatedVariables[varIndex]
                        .and(bddExAutomata.getBDDBitVecSource(varIndex).lte(bddExAutomata.getMaxBDDBitVecOf(varIndex)));

                transCorrespondingToUpdatedVariables[varIndex] = transCorrespondingToUpdatedVariables[varIndex]
                        .and(bddExAutomata.getBDDBitVecTarget(varIndex).lte(bddExAutomata.getMaxBDDBitVecOf(varIndex)));

                transCorrespondingToUpdatedVariables[varIndex] = transCorrespondingToUpdatedVariables[varIndex]
                        .and(bddExAutomata.getBDDBitVecSource(varIndex).gte(bddExAutomata.getMinBDDBitVecOf(varIndex)));

                transCorrespondingToUpdatedVariables[varIndex] = transCorrespondingToUpdatedVariables[varIndex]
                        .and(bddExAutomata.getBDDBitVecTarget(varIndex).gte(bddExAutomata.getMinBDDBitVecOf(varIndex)));

                tmp.andWith(transCorrespondingToUpdatedVariables[varIndex]);
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

            final BDD isolatedTransWithActions = transWithoutActions.andWith(keep);


            final BDD transWithActions = isolatedTransWithActions.andWith(tmp);
            eventForwardTransitionBDD.orWith(transWithActions);
            //eventToCompleteTransitionBDD.put(eventIndex, eventForwardTransitionBDD);

            /* Determine whether or not to put this event as the initialComponentCandidate set or markedComponentCandidate set. */
            if (eventIsQualifiedForInitialComponent) {
                initialComponentCandidates.add(eventIndex);
            }

            if (eventIsQualifiedForMarkedComponent) {
                markedComponentCandidates.add(eventIndex);
            }
            
            if (allVarPossiblyUpdated) {
                eventIndex2GuardVariables.remove(eventIndex);
                eventIndex2GuardVariables.put(eventIndex, new HashSet<String>());
            }

            /* If the event is uncontrollable, put it into the uncontrollableComponentCandidates */
            if (bddExAutomata.plantUncontrollableEventIndexList.contains(eventIndex)
                    || bddExAutomata.specUncontrollableEventIndexList.contains(eventIndex)) {
                uncontrollableComponentCandidates.add(eventIndex);
            }
        }

        /*   A help class, which is used for constructing event-based BDD partitioning on the BDD level
         *   The whole idea of constructing event-based BDD partition is that:
         *   1. For each event, we iterate each automaton to get all the edges labeled with the event
         *   2. From those edges on the current automaton, we create
         *       a. A single BDD of the transitions without actions;
         *       b. A BDD array which holds the variable cared transition for each variable (used as conditions in If-Then-Else operator)
         *       c. A BDD array which holds the variable cared transition and actions for each variable (used as statement in If-Then-Else operator)
         *   3. After we get an instance for each automaton, we merge them together on the BDD level.
         *   This class focuses on part 2
         */
        class AutIncludingTransLabeledByEvent {

            private final ArrayList<EdgeProxy> includedEdges;
            private final ExtendedAutomaton anAutomaton;
            private final int eventIndex;
            private final BDD transitionBDDWithoutActions;
            private final BDD[] transitionBDDCorrespondingToUpdatedVariables;
            private final BDD[] transitionBDDCorrespondingToUpdatedVariablesWithoutActions;
            private boolean allVarPossiblyUpdated;

            /* When iterate each edge, for one edge, if the source location of it is an initial location, qualifiedForInitialComponent is set true. */
            private boolean qualifiedForInitialComponent = false;

            /* When iterate each edge, for one edge, if the target location of it is an marked location, qualifiedForMarkedComponent is set true. */
            private boolean qualifiedForMarkedComponent = false;

            private AutIncludingTransLabeledByEvent(final ArrayList<EdgeProxy> includedEdges, final int eventIndex,
                    final ExtendedAutomaton anAutomaton) {

                this.includedEdges = includedEdges;
                this.eventIndex = eventIndex;
                this.anAutomaton = anAutomaton;
                // Create these two BDD arrays
                this.transitionBDDCorrespondingToUpdatedVariablesWithoutActions = new BDD[orgAutomata.getVars().size()];
                this.transitionBDDCorrespondingToUpdatedVariables = new BDD[orgAutomata.getVars().size()];
                
                allVarPossiblyUpdated = false;

                /* First initialize the BDD and two BDD arrays*/
                transitionBDDWithoutActions = manager.getZeroBDD();
                for (final VariableComponentProxy var : orgAutomata.getVars()) {
                    final int varIndex = theIndexMap.getVariableIndexByName(var.getName());
                    transitionBDDCorrespondingToUpdatedVariablesWithoutActions[varIndex] = manager.getZeroBDD();
                    transitionBDDCorrespondingToUpdatedVariables[varIndex] = manager.getZeroBDD();
                }

                /* Go through each edge and build these two BDD arrays*/
                for (final Iterator<EdgeProxy> edgeIterator = this.includedEdges.iterator(); edgeIterator.hasNext();) {

                    final EdgeProxy anEdge = edgeIterator.next();

                    if (anAutomaton.isLocationInitial(anEdge.getSource())) {
                        qualifiedForInitialComponent = true;
                    }

                    if (anAutomaton.isLocationAccepted(anEdge.getTarget()) || anAutomaton.isAllMarked()) {
                        qualifiedForMarkedComponent = true;
                    }

                    /* Construct the BDD for the edge but exclude the actions. */
                    final BDD transitionWithoutActionsOnCurrEdge = getEdgeBDDWithoutActions(anAutomaton, anEdge);

                    /* Keep track of the updated variable index */
                    final TIntHashSet updatedVariableIndexSet = new TIntHashSet();

                    List<BinaryExpressionProxy> actions = null;
                    final BDD actionsBDD = manager.getOneBDD();

                    if (anEdge.getGuardActionBlock() != null && anEdge.getGuardActionBlock().getActions() != null
                                                                    && !anEdge.getGuardActionBlock().getActions().isEmpty()) {

                        actions = anEdge.getGuardActionBlock().getActions();
                        /* Iterate each action to extract variables and build BDD*/
                        for (final BinaryExpressionProxy anAction : actions) {
                            final String updatedVariableOfThisAction = ((SimpleIdentifierProxy) anAction.getLeft()).getName();
                            updatedVariableIndexSet.add(theIndexMap.getVariableIndexByName(updatedVariableOfThisAction));
                            /* Use it for heuristics*/
                            eventIndex2UpdatedVariables.get(eventIndex).add(updatedVariableOfThisAction);
                            /*BDD-AND each actionBDD */
                            actionsBDD.andWith(manager.action2BDD(anAction));
                        }
                    }

                    /*
                     * Now we have, for each edge (1) forward BDD transition without actions (2) actionsBDD (3) the updated variable set
                     * Next, we merge them together
                     * (1) based on the indices of updated variables, OR with them on the BDD array and return this BDD array
                     * (2) Or with transitionBDDWithoutActions with transitionWithoutActionsOnCurrEdge
                     */
                    for (final TIntIterator itr = updatedVariableIndexSet.iterator(); itr.hasNext();) {
                        final int variableIndex = itr.next();
                        transitionBDDCorrespondingToUpdatedVariablesWithoutActions[variableIndex] =
                                transitionBDDCorrespondingToUpdatedVariablesWithoutActions[variableIndex].or(transitionWithoutActionsOnCurrEdge);
                        transitionBDDCorrespondingToUpdatedVariables[variableIndex]
                         = transitionBDDCorrespondingToUpdatedVariables[variableIndex].or(transitionWithoutActionsOnCurrEdge.and(actionsBDD));
                    }

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
                    eventIndex2GuardVariables.get(eventIndex).addAll(extractVariablesFromTheEdge(guards.get(0)));
                    guardBDD = manager.guard2BDD(guards.get(0));
                }

                /*
                 * We still need to track another thing.  
                 * First of all, let's see an example: assuming that EFA E_1 has two edges labeled by e. One edge has guards and one doesn't. 
                 * What should be included in the guard variables if we only consider this EFA? Apprently, all, right? since one edge doesn't 
                 * have any guard. Now, let's consider another EFA E_2 which also has two edges labeled by e. But at this time both edges have 
                 * guards. What should be included in the guard variables if they are synchronized? The union of all variables in these guards. 
                 * The non-guard edge in E_1 is synchronized with either edge in E_2 which does have guard. 
                 * What if one edge in E_2 deosn't have guard. OK, here is the point: the guard variables should be all!
                 */
                if (guards == null || (guards != null && guards.isEmpty())) {
                    allVarPossiblyUpdated = true;
                }
                
                sourceBDD.andWith(destBDD);
                sourceBDD.andWith(guardBDD);
                return sourceBDD;
            }

            /* Find which EFA variables are in this edge. */
            private HashSet<String> extractVariablesFromTheEdge(final SimpleExpressionProxy guard) {

                final HashSet<String> extractedVariables = new HashSet<String>();

                for (final VariableComponentProxy var : bddExAutomata.orgExAutomata.extractVariablesFromExpr(guard)) {
                    extractedVariables.add(var.getName());
                }

                return extractedVariables;
            }
        }
    }
}

