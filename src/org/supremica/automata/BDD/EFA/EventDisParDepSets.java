package org.supremica.automata.BDD.EFA;

import gnu.trove.TIntArrayList;
import gnu.trove.TIntObjectHashMap;
import gnu.trove.TIntProcedure;
import gnu.trove.TObjectIntHashMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDDomain;
import org.supremica.automata.ExtendedAutomaton;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;

/**
 *
 * @author zhennan
 *
 *  Make use of  the event-base disjunctive partitioning technique to construct the automaton and variable partial transition relation
 */
public class EventDisParDepSets {

    static Logger logger = LoggerFactory.createLogger(EventDisParDepSets.class);
    private final BDDExtendedAutomata bddExAutomata;
    private final BDDExtendedManager manager;
    private final List<ExtendedAutomaton> theExAutomata;
    private final Map<ExtendedAutomaton, BDDExtendedAutomaton> automatonToBDDAutomatonMap;
    private TIntObjectHashMap <EventDisParDepSet> events2EventDisParDepSet;
    private final TIntObjectHashMap<HashMap<ExtendedAutomaton, ArrayList<EdgeProxy>>> event2AutomatonsEdges;

    public EventDisParDepSets(final BDDExtendedAutomata bddAutomata){
        this.bddExAutomata = bddAutomata;
        this.manager = bddAutomata.manager;
        this.theExAutomata = bddAutomata.theExAutomata;
        this.automatonToBDDAutomatonMap = bddAutomata.automatonToBDDAutomatonMap;
        this.event2AutomatonsEdges = bddAutomata.event2AutomatonsEdges;
        initialize();
    }

    private void initialize() {
        events2EventDisParDepSet = new TIntObjectHashMap<EventDisParDepSet>();
        event2AutomatonsEdges.forEachKey(new TIntProcedure() {
            public boolean execute(final int eventIndex) {
                final HashMap<ExtendedAutomaton, ArrayList<EdgeProxy>> automatonsEdges = event2AutomatonsEdges.get(eventIndex);
                final EventDisParDepSet eventDisParSet = new EventDisParDepSet(eventIndex, automatonsEdges);
                events2EventDisParDepSet.put(eventIndex, eventDisParSet);
                return true;
            }
        });
    }

    public TIntObjectHashMap <EventDisParDepSet> getEvents2EventDisParDepSet(){
        return events2EventDisParDepSet;
    }


    class EventDisParDepSet{
        private final int eventIndex;
        private final HashMap<ExtendedAutomaton, ArrayList<EdgeProxy>> includingAutomata2Edges;
        private final BDD eventForwardPartialTransitions;
        private final TObjectIntHashMap<ExtendedAutomaton> automaton2nbrOfInfluencedVariables;

        public EventDisParDepSet(final int eventIndex, final HashMap<ExtendedAutomaton, ArrayList<EdgeProxy>> includingAutomata2Edges){
            this.eventIndex = eventIndex;
            this.includingAutomata2Edges = includingAutomata2Edges;
            this.eventForwardPartialTransitions = manager.getZeroBDD();
            this.automaton2nbrOfInfluencedVariables = new TObjectIntHashMap<ExtendedAutomaton> (bddExAutomata.theExAutomata.size());
            initialize();
        }

        private void initialize(){

            final List<BDD> forwardTransitionRelationsWithoutActions = new ArrayList<BDD>(); // Maintain one BDD edge list [Locations and Guards]
            final List<List<BinaryExpressionProxy>> actionList = new ArrayList<List<BinaryExpressionProxy>>(); // Maintain one updated actions list
            final TIntArrayList conflictingIndexList = new TIntArrayList(); // Keep track the conflicting action indices to finally remove the conflicting path

            final Set<ExtendedAutomaton> includedAutomata = includingAutomata2Edges.keySet();
            final Iterator<ExtendedAutomaton> automatonIterator = includedAutomata.iterator();

            while (automatonIterator.hasNext()) {
                final ExtendedAutomaton anAutomaton = automatonIterator.next();
                final List<List<BinaryExpressionProxy>> tempActionList = cloneAndClearBinaryExpressionList(actionList);
                final List<BDD> tempForwardBDDList = cloneAndClearBDDList(forwardTransitionRelationsWithoutActions);

                for (final Iterator<EdgeProxy> edgeIterator = includingAutomata2Edges.get(anAutomaton).iterator(); edgeIterator.hasNext();) {
                    final EdgeProxy anEdge = edgeIterator.next();
                    List<BinaryExpressionProxy> actionOfTheEdge = null;

                    if (anEdge.getGuardActionBlock() != null && anEdge.getGuardActionBlock().getActions() != null && !anEdge.getGuardActionBlock().getActions().isEmpty()) {
                        actionOfTheEdge = anEdge.getGuardActionBlock().getActions();
                        if (tempActionList.isEmpty()) {
                            actionList.add(actionOfTheEdge);
                        } else {
                            for (int index = 0; index < tempActionList.size(); index++) {
                                if (conflicting(tempActionList.get(index), actionOfTheEdge)) {
                                    conflictingIndexList.add(index);
                                }
                            }
                            updateActionList(actionList, tempActionList, conflictingIndexList, actionOfTheEdge);
                        }
                    } else {
                        if (!bddExAutomata.BDDBitVecSourceVarsMap.isEmpty()) {
                            actionOfTheEdge = new ArrayList<BinaryExpressionProxy>();
                            if (tempActionList.isEmpty()) {
                                actionList.add(actionOfTheEdge);
                            } else {
                                updateActionList(actionList, tempActionList, conflictingIndexList, actionOfTheEdge);
                            }
                        }
                    }

                    final BDD aForwardTransition = getEdgeBDDWithoutActions(anAutomaton, anEdge);
                    if (tempForwardBDDList.isEmpty()) {
                        forwardTransitionRelationsWithoutActions.add(aForwardTransition);
                    } else {
                        for(int index = 0; index < tempForwardBDDList.size(); index++){
                            if(!conflictingIndexList.contains(index)){
                                forwardTransitionRelationsWithoutActions.add(aForwardTransition.and(tempForwardBDDList.get(index)));
                            }
                        }
                    }
                    conflictingIndexList.clear();
                }
            }

            BDD isolatedEventForwardPartialTransitions = manager.getZeroBDD();
            for(int index = 0; index < forwardTransitionRelationsWithoutActions.size(); index++){
                BDD actionForwardBDD = manager.getOneBDD();
                if(actionList.size() > 0)
                    actionForwardBDD = manager.action2BDDDisjunctiveVersion(this, actionList.get(index));
                else {
                    for (final Iterator<String> varIterator = bddExAutomata.BDDBitVecTargetVarsMap.keySet().iterator(); varIterator.hasNext();) {
                        final String varName = varIterator.next();
                        final SupremicaBDDBitVector leftSide = bddExAutomata.getBDDBitVecTarget(varName);
                        final SupremicaBDDBitVector rightSide = bddExAutomata.getBDDBitVecSource(varName);
                        BDD compensateBDD = leftSide.equ(rightSide);
                        compensateBDD = compensateBDD
                                .and(bddExAutomata.BDDBitVecSourceVarsMap.get(varName).lte(bddExAutomata.getMaxBDDBitVecOf(varName)))
                                .and(bddExAutomata.BDDBitVecTargetVarsMap.get(varName).lte(bddExAutomata.getMaxBDDBitVecOf(varName)))
                                .and(bddExAutomata.BDDBitVecSourceVarsMap.get(varName).gte(bddExAutomata.getMinBDDBitVecOf(varName)))
                                .and(bddExAutomata.BDDBitVecTargetVarsMap.get(varName).gte(bddExAutomata.getMinBDDBitVecOf(varName)));
                        actionForwardBDD = actionForwardBDD.and(compensateBDD);
                    }
                }
                isolatedEventForwardPartialTransitions.orWith(forwardTransitionRelationsWithoutActions.get(index).andWith(actionForwardBDD));
            }

            BDD keep = manager.getOneBDD();
            for (final ExtendedAutomaton extendedAutomaton : theExAutomata) {
                if (!includedAutomata.contains(extendedAutomaton)) {
                    final BDDExtendedAutomaton bddAutomaton = automatonToBDDAutomatonMap.get(extendedAutomaton);
                    keep = keep.and(bddAutomaton.getSelfLoopsBDD());
                }
            }

            isolatedEventForwardPartialTransitions = isolatedEventForwardPartialTransitions.and(keep);
            keep.free();
            eventForwardPartialTransitions.orWith(isolatedEventForwardPartialTransitions);
        }

        private List<BDD> cloneAndClearBDDList(final List<BDD> aBDDList){
            // Shallow copy of aBDDList and clear this obsolete list
            final List<BDD> temp = new ArrayList<BDD>(aBDDList);
            aBDDList.clear();
            return temp;
        }

        private List<List<BinaryExpressionProxy>> cloneAndClearBinaryExpressionList(final List<List<BinaryExpressionProxy>> actionList){
            // Shallow copy of the actionList and clear it.
            final List<List<BinaryExpressionProxy>> tempActionList = new ArrayList<List<BinaryExpressionProxy>>(actionList);
            actionList.clear();
            return tempActionList;
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

            BDD forwardGuardBDD = manager.getOneBDD();
            List<SimpleExpressionProxy> guards = null;
            if(anEdge.getGuardActionBlock() != null)
                guards = anEdge.getGuardActionBlock().getGuards();
            if (guards != null && guards.size() > 0) {
                manager.localOverflows = manager.getZeroBDD();
                forwardGuardBDD = manager.guard2BDD(guards.get(0));
            }
            sourceBDD.andWith(destBDD);
            sourceBDD.andWith(forwardGuardBDD);
            return sourceBDD;
        }

        private boolean conflicting(final List<BinaryExpressionProxy> actions, final List<BinaryExpressionProxy> others) {
            boolean whetherConflicting = false;
            for (final BinaryExpressionProxy aStatement : actions) {
                manager.resetLocalOverflows();
                final BDD aStatementBDD = manager.action2BDD(aStatement);
                for (final BinaryExpressionProxy anotherStatement : others) {
                    manager.resetLocalOverflows();
                    final BDD anotherStatementBDD = manager.action2BDD(anotherStatement);
                    if (aStatement.getLeft().equals(anotherStatement.getLeft())&&!aStatementBDD.equals(anotherStatementBDD)) {
                        whetherConflicting = true;
                        break;
                    }
                }
                if (whetherConflicting) {
                    break;
                }
            }
            return whetherConflicting;
        }

        private void updateActionList(final List<List<BinaryExpressionProxy>> actionList, final List<List<BinaryExpressionProxy>> tempBinaryExpressionList,
                                                               final TIntArrayList conflictingIndexList, final List<BinaryExpressionProxy> theAction) {
            for(int index = 0; index < tempBinaryExpressionList.size(); index++){
                if(!conflictingIndexList.contains(new Integer(index))){
                    final List<BinaryExpressionProxy> temp = new ArrayList<BinaryExpressionProxy>(tempBinaryExpressionList.get(index));
                    for(final BinaryExpressionProxy e: theAction){
                        if(!temp.contains(e))
                            temp.add(e);
                    }
                    actionList.add(temp);
                }
            }
        }

        public BDD getEventForwardPartialTransitions(){
            return eventForwardPartialTransitions;
        }

        public int getEventIndex(){
            return eventIndex;
        }

        public Set<ExtendedAutomaton> getIncludingAutomata(){
            return includingAutomata2Edges.keySet();
        }

        public TObjectIntHashMap<ExtendedAutomaton> getAutomaton2nbrOfInfluencedVariables(){
            return automaton2nbrOfInfluencedVariables;
        }
    }
}

