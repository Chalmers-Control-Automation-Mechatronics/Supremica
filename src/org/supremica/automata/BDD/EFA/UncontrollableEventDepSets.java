package org.supremica.automata.BDD.EFA;

import gnu.trove.TIntArrayList;
import gnu.trove.TIntObjectHashMap;
import gnu.trove.TIntProcedure;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDDomain;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import org.supremica.automata.ExtendedAutomaton;
import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;

/**
 *
 * @author zhennan
 */

public class UncontrollableEventDepSets {
    static Logger logger = LoggerFactory.createLogger(UncontrollableEventDepSets.class);
    private final BDDExtendedAutomata bddExAutomata;
    private final BDDExtendedManager manager;
    private final List<ExtendedAutomaton> members;
    private final Map<ExtendedAutomaton, BDDExtendedAutomaton> automatonToBDDAutomatonMap;
    private final TIntObjectHashMap <BDD> uncontrollableEvents2EnabledStates;
    private final TIntObjectHashMap<HashMap<ExtendedAutomaton, ArrayList<EdgeProxy>>> uncontrollableEvent2MembersEdges;
    private final TIntArrayList uncontrollableEventIndexList;

    public UncontrollableEventDepSets(final BDDExtendedAutomata bddAutomata,  final List<ExtendedAutomaton> members, final TIntArrayList caredUncontrollableEventIndexList){
        this.bddExAutomata = bddAutomata;
        this.manager = bddAutomata.manager;
        this.members = members;
        this.automatonToBDDAutomatonMap = bddAutomata.automatonToBDDAutomatonMap;
        this.uncontrollableEventIndexList =caredUncontrollableEventIndexList;
        this.uncontrollableEvent2MembersEdges = new TIntObjectHashMap<HashMap<ExtendedAutomaton, ArrayList<EdgeProxy>>>(uncontrollableEventIndexList.size());
        this.uncontrollableEvents2EnabledStates = new TIntObjectHashMap<BDD>(uncontrollableEventIndexList.size());
        initialize();
    }

    private void initialize() {
        uncontrollableEventIndexList.forEach(new TIntProcedure() {
            //@Override
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
            //@Override
            public boolean execute(final int anUncontrollableEvent) {
                final List<BDD> forwardTransitionRelationsWithoutActions = new ArrayList<BDD>();
                final HashMap<ExtendedAutomaton, ArrayList<EdgeProxy>> includedMemebersEdges = uncontrollableEvent2MembersEdges.get(anUncontrollableEvent);
                final Set<ExtendedAutomaton> includedMembers = includedMemebersEdges.keySet();
                final Iterator<ExtendedAutomaton> automatonIterator = includedMembers.iterator();
                final BDD eventForwardPartialTransitions = manager.getZeroBDD();
                while (automatonIterator.hasNext()) {
                    final ExtendedAutomaton anMember = automatonIterator.next();
                    final List<BDD> tempForwardBDDList = cloneAndClearBDDList(forwardTransitionRelationsWithoutActions);
                    for (final Iterator<EdgeProxy> edgeIterator = includedMemebersEdges.get(anMember).iterator(); edgeIterator.hasNext();) {
                        final EdgeProxy anEdge = edgeIterator.next();
                        final BDD aForwardTransition = getEdgeBDDWithoutActions(anMember, anEdge);
                        if (tempForwardBDDList.isEmpty()) {
                            forwardTransitionRelationsWithoutActions.add(aForwardTransition);
                        } else {
                            for (int index = 0; index < tempForwardBDDList.size(); index++)
                                    forwardTransitionRelationsWithoutActions.add(aForwardTransition.and(tempForwardBDDList.get(index)));
                        }
                    }
                }
                BDD isolatedEventForwardPartialTransitions = manager.getZeroBDD();
                for (int index = 0; index < forwardTransitionRelationsWithoutActions.size(); index++) {
                    isolatedEventForwardPartialTransitions.orWith(forwardTransitionRelationsWithoutActions.get(index));
                }

                BDD keep = manager.getOneBDD();
                for (final ExtendedAutomaton extendedAutomaton : members) {
                    if (!includedMembers.contains(extendedAutomaton)) {
                        final BDDExtendedAutomaton bddAutomaton = automatonToBDDAutomatonMap.get(extendedAutomaton);
                        keep = keep.and(bddAutomaton.getSelfLoopsBDD());
                    }
                }
                isolatedEventForwardPartialTransitions = isolatedEventForwardPartialTransitions.andWith(keep);
                eventForwardPartialTransitions.orWith(isolatedEventForwardPartialTransitions);
                uncontrollableEvents2EnabledStates.put(anUncontrollableEvent, eventForwardPartialTransitions.exist(bddExAutomata.destStateVariables));
                return true;
            }
        });
    }

    private List<BDD> cloneAndClearBDDList(final List<BDD> aBDDList) {
        final List<BDD> temp = new ArrayList<BDD>(aBDDList);
        aBDDList.clear();
        return temp;
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
        if (anEdge.getGuardActionBlock() != null) {
            guards = anEdge.getGuardActionBlock().getGuards();
        }
        if (guards != null && guards.size() > 0) {
            forwardGuardBDD = manager.guard2BDD(guards.get(0));
        }
        sourceBDD.andWith(destBDD);
        sourceBDD.andWith(forwardGuardBDD);
        return sourceBDD;
    }

    public TIntObjectHashMap <BDD> getUncontrollableEvents2EnabledStates(){
        return uncontrollableEvents2EnabledStates;
    }
}
