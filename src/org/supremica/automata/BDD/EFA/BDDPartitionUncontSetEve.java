package org.supremica.automata.BDD.EFA;

import gnu.trove.TIntHashSet;
import gnu.trove.TIntIterator;
import gnu.trove.TIntObjectHashMap;
import gnu.trove.TIntProcedure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDDomain;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;

import org.supremica.automata.ExtendedAutomaton;
import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;

/**
 * The class is used to compute the initial uncontrollable states given plants
 * and specifications. The class is similar to the event based partitioning with
 * the difference that the destination variables (EFA also include updated
 * values of variables).
 *
 * @author Zhennan
 * @version 2.0
 * @since 1.0
 */
public class BDDPartitionUncontSetEve {

    static Logger logger = LoggerFactory.createLogger(BDDPartitionUncontSetEve.class);
    /**
     * The reference to the bddExAutomata.
     */
    private final BDDExtendedAutomata bddExAutomata;
    /**
     * The reference to the manager.
     */
    private final BDDExtendedManager manager;
    /**
     * Either a group of plants or specifications.
     */
    private final List<ExtendedAutomaton> members;

    /**
     * For plants, the fields contains the set of states which enable
     * uncontrollable events, For specifications, it contains the set of states
     * which cannot enable uncontrollable events.
     */
    private final TIntObjectHashMap<BDD> uncontrollableEvents2EnabledStates;
    /**
     * Each uncontrollable event can appear on several edges on different
     * automata. This field keeps track of them.
     */
    private final TIntObjectHashMap<HashMap<ExtendedAutomaton, ArrayList<EdgeProxy>>> uncontrollableEvent2MembersEdges;
    /**
     * Uncontrollable event indices
     */
    private final TIntHashSet uncontrollableEventIndexList;

    public BDDPartitionUncontSetEve(final BDDExtendedAutomata bddAutomata, final List<ExtendedAutomaton> members,
            final TIntHashSet caredUncontrollableEventIndexList) {

        this.bddExAutomata = bddAutomata;
        this.manager = bddAutomata.getManager();
        this.members = members;

        this.uncontrollableEventIndexList = caredUncontrollableEventIndexList;

        this.uncontrollableEvent2MembersEdges = new TIntObjectHashMap<HashMap<ExtendedAutomaton, ArrayList<EdgeProxy>>>(uncontrollableEventIndexList.size());
        this.uncontrollableEvents2EnabledStates = new TIntObjectHashMap<BDD>(uncontrollableEventIndexList.size());

        initialize();
    }

    private void initialize() {
        uncontrollableEventIndexList.forEach(new TIntProcedure() {
            @Override
            public boolean execute(final int eventIndex) {
                final Set<ExtendedAutomaton> allAut = bddExAutomata.event2AutomatonsEdges.get(eventIndex).keySet();
                final HashMap<ExtendedAutomaton, ArrayList<EdgeProxy>> tmp = new HashMap<ExtendedAutomaton, ArrayList<EdgeProxy>>();
                for (final ExtendedAutomaton aut : allAut) {
                    if (members.contains(aut)) {
                        tmp.put(aut, bddExAutomata.event2AutomatonsEdges.get(eventIndex).get(aut));
                    }
                }
                uncontrollableEvent2MembersEdges.put(eventIndex, tmp);
                return true;
            }
        });

        uncontrollableEventIndexList.forEach(new TIntProcedure() {
            @Override
            public boolean execute(final int anUncontrollableEvent) {
                final HashMap<ExtendedAutomaton, ArrayList<EdgeProxy>> automatonsEdges = uncontrollableEvent2MembersEdges.get(anUncontrollableEvent);
                final BDDPartitionUnEve eventDisParSet = new BDDPartitionUnEve(automatonsEdges);
                uncontrollableEvents2EnabledStates.put(anUncontrollableEvent, eventDisParSet.eventForwardTransitionBDD.exist(bddExAutomata.getDestStatesVarSet()));
                return true;
            }
        });
    }

    public TIntObjectHashMap<BDD> getUncontrollableEvents2EnabledStates() {
        return uncontrollableEvents2EnabledStates;
    }

    class BDDPartitionUnEve {

        private final HashMap<ExtendedAutomaton, ArrayList<EdgeProxy>> includingAutomata2UnconEdges;
        private final BDD eventForwardTransitionBDD;

        private BDDPartitionUnEve(final HashMap<ExtendedAutomaton, ArrayList<EdgeProxy>> includingAutomata2UnconEdges) {
            this.includingAutomata2UnconEdges = includingAutomata2UnconEdges;
            this.eventForwardTransitionBDD = manager.getZeroBDD();
            if (!includingAutomata2UnconEdges.isEmpty()) {
                buildUnconTansitionBDD();
            }
        }

        private void buildUnconTansitionBDD() {

            final Set<ExtendedAutomaton> includedAutomata = includingAutomata2UnconEdges.keySet();
            final Iterator<ExtendedAutomaton> autIterator = includedAutomata.iterator();

            BDD transWithoutActions = null;
            BDD[] transCorrespondingToUpdatedVariablesWithoutActions = null;
            BDD[] transCorrespondingToUpdatedVariables = null;

            if (autIterator.hasNext()) {

                final ExtendedAutomaton firstAutomaton = autIterator.next();
                final AutIncludingTransLabeledByUnEvent firstAutTransWithEvent
                        = new AutIncludingTransLabeledByUnEvent(includingAutomata2UnconEdges.get(firstAutomaton), firstAutomaton);

                transWithoutActions = firstAutTransWithEvent.transitionBDDWithoutActions;
                transCorrespondingToUpdatedVariablesWithoutActions = firstAutTransWithEvent.transitionBDDCorrespondingToUpdatedVariablesWithoutActions;
                transCorrespondingToUpdatedVariables = firstAutTransWithEvent.transitionBDDCorrespondingToUpdatedVariables;
            }

            if (includedAutomata.size() == 1) {
                for (final VariableComponentProxy var : bddExAutomata.orgExAutomata.getVars()) {
                    final int varIndex = bddExAutomata.theIndexMap.getVariableIndex(var);
                    final BDD noneUpdateVar = bddExAutomata.getBDDBitVecTarget(varIndex).equ(bddExAutomata.getBDDBitVecSource(varIndex));
                    transCorrespondingToUpdatedVariables[varIndex] = transCorrespondingToUpdatedVariablesWithoutActions[varIndex]
                            .ite(transCorrespondingToUpdatedVariables[varIndex], noneUpdateVar);
                }
            }

            while (autIterator.hasNext()) {

                final ExtendedAutomaton currAutomaton = autIterator.next();
                final AutIncludingTransLabeledByUnEvent currAutIncTrans
                        = new AutIncludingTransLabeledByUnEvent(includingAutomata2UnconEdges.get(currAutomaton), currAutomaton);

                final BDD currTransWithoutActions = currAutIncTrans.transitionBDDWithoutActions;
                final BDD[] currTransCorrespondingToUpdatedVariables = currAutIncTrans.transitionBDDCorrespondingToUpdatedVariables;
                final BDD[] currTransCorrespondingToUpdatedVariablesWithoutActions = currAutIncTrans.transitionBDDCorrespondingToUpdatedVariablesWithoutActions;

                for (final VariableComponentProxy var : bddExAutomata.orgExAutomata.getVars()) {

                    final int varIndex = bddExAutomata.theIndexMap.getVariableIndex(var);

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

                for (final VariableComponentProxy var : bddExAutomata.orgExAutomata.getVars()) {
                    final int varIndex = bddExAutomata.theIndexMap.getVariableIndex(var);
                    final BDD compensate = bddExAutomata.getBDDBitVecTarget(varIndex).equ(bddExAutomata.getBDDBitVecSource(varIndex));
                    transCorrespondingToUpdatedVariables[varIndex] = transCorrespondingToUpdatedVariablesWithoutActions[varIndex]
                            .ite(transCorrespondingToUpdatedVariables[varIndex], compensate);
                }

                transWithoutActions.andWith(currTransWithoutActions);
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
            final Set<ExtendedAutomaton> includedAutomataSet = includingAutomata2UnconEdges.keySet();
            BDD keep = manager.getOneBDD();
            for (final ExtendedAutomaton extendedAutomaton : bddExAutomata.theExAutomata) {
                if (!includedAutomataSet.contains(extendedAutomaton)) {
                    final BDDExtendedAutomaton bddAutomaton = bddExAutomata.automatonToBDDAutomatonMap.get(extendedAutomaton);
                    keep = keep.and(bddAutomaton.getSelfLoopsBDD());
                }
            }

            final BDD isolatedTransWithActions = transWithoutActions.andWith(keep);


            final BDD transWithActions = isolatedTransWithActions.andWith(tmp);
            eventForwardTransitionBDD.orWith(transWithActions);
        }

        class AutIncludingTransLabeledByUnEvent {

            private final ArrayList<EdgeProxy> includedEdges;
            private final ExtendedAutomaton automaton;

            private final BDD transitionBDDWithoutActions;
            private final BDD[] transitionBDDCorrespondingToUpdatedVariables;
            private final BDD[] transitionBDDCorrespondingToUpdatedVariablesWithoutActions;


            private AutIncludingTransLabeledByUnEvent(final ArrayList<EdgeProxy> includedEdges, final ExtendedAutomaton anAutomaton) {
                this.includedEdges = includedEdges;
                this.automaton = anAutomaton;
                // Create these two BDD arrays
                this.transitionBDDCorrespondingToUpdatedVariablesWithoutActions = new BDD[bddExAutomata.orgExAutomata.getVars().size()];
                this.transitionBDDCorrespondingToUpdatedVariables = new BDD[bddExAutomata.orgExAutomata.getVars().size()];

                /* First initialize the BDD and two BDD arrays*/
                transitionBDDWithoutActions = manager.getZeroBDD();
                for (final VariableComponentProxy var : bddExAutomata.orgExAutomata.getVars()) {
                    final int varIndex = bddExAutomata.theIndexMap.getVariableIndexByName(var.getName());
                    transitionBDDCorrespondingToUpdatedVariablesWithoutActions[varIndex] = manager.getZeroBDD();
                    transitionBDDCorrespondingToUpdatedVariables[varIndex] = manager.getZeroBDD();
                }

                /* Go through each edge and build these two BDD arrays*/
                for (final Iterator<EdgeProxy> edgeIterator = this.includedEdges.iterator(); edgeIterator.hasNext();) {

                    final EdgeProxy anEdge = edgeIterator.next();

                    /* Construct the BDD for the edge but exclude the actions. */
                    final BDD transitionWithoutActionsOnCurrEdge = getEdgeBDDWithoutActions(automaton, anEdge);

                    final TIntHashSet updatedVariableIndexSet = new TIntHashSet();

                    List<BinaryExpressionProxy> actions = null;
                    final BDD actionsBDD = manager.getOneBDD();

                    if (anEdge.getGuardActionBlock() != null && anEdge.getGuardActionBlock().getActions() != null
                            && !anEdge.getGuardActionBlock().getActions().isEmpty()) {

                        actions = anEdge.getGuardActionBlock().getActions();
                        /* Iterate each action to extract variables and build BDD*/
                        for (final BinaryExpressionProxy anAction : actions) {
                            final String updatedVariableOfThisAction = ((SimpleIdentifierProxy) anAction.getLeft()).getName();
                            updatedVariableIndexSet.add(bddExAutomata.theIndexMap.getVariableIndexByName(updatedVariableOfThisAction));
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
                        transitionBDDCorrespondingToUpdatedVariables[variableIndex] = transitionBDDCorrespondingToUpdatedVariables[variableIndex].or(transitionWithoutActionsOnCurrEdge.and(actionsBDD));
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
                    guardBDD = manager.guard2BDD(guards.get(0));
                }

                sourceBDD.andWith(destBDD);
                sourceBDD.andWith(guardBDD);
                return sourceBDD;
            }

            /* Find which EFA variables are in this edge. */
            @SuppressWarnings("unused")
            private Set<String> extractVariablesFromTheEdge(final SimpleExpressionProxy guard) {

                final HashSet<String> extractedVariables = new HashSet<String>();

                for (final VariableComponentProxy var : bddExAutomata.orgExAutomata.extractVariablesFromExpr(guard)) {
                    extractedVariables.add(var.getName());
                }

                return extractedVariables;
            }
        }
    }
}
