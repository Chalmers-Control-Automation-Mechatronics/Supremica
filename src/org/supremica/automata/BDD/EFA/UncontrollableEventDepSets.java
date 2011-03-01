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
    private BDDExtendedAutomata bddExAutomata;
    private BDDExtendedManager manager;
    private List<ExtendedAutomaton> members;
    private Map<ExtendedAutomaton, BDDExtendedAutomaton> automatonToBDDAutomatonMap;
    private TIntObjectHashMap <BDD> uncontrollableEvents2EnabledStates;
    private TIntObjectHashMap<HashMap<ExtendedAutomaton, ArrayList<EdgeProxy>>> uncontrollableEvent2MembersEdges;
    private TIntArrayList uncontrollableEventIndexList;

    public UncontrollableEventDepSets(BDDExtendedAutomata bddAutomata,  List<ExtendedAutomaton> members, TIntArrayList caredUncontrollableEventIndexList){
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
            @Override
            public boolean execute(int eventIndex) {
                Set<ExtendedAutomaton> allAut = bddExAutomata.event2AutomatonsEdges.get(eventIndex).keySet();
                HashMap<ExtendedAutomaton, ArrayList<EdgeProxy>> tmp = new HashMap<ExtendedAutomaton, ArrayList<EdgeProxy>>();
                for (ExtendedAutomaton aut : allAut) {
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
            public boolean execute(int anUncontrollableEvent) {
                List<BDD> forwardTransitionRelationsWithoutActions = new ArrayList<BDD>();
                HashMap<ExtendedAutomaton, ArrayList<EdgeProxy>> includedMemebersEdges = uncontrollableEvent2MembersEdges.get(anUncontrollableEvent);
                Set<ExtendedAutomaton> includedMembers = includedMemebersEdges.keySet();
                Iterator<ExtendedAutomaton> automatonIterator = includedMembers.iterator();
                BDD eventForwardPartialTransitions = manager.getZeroBDD();
                while (automatonIterator.hasNext()) {
                    ExtendedAutomaton anMember = automatonIterator.next();
                    List<BDD> tempForwardBDDList = cloneAndClearBDDList(forwardTransitionRelationsWithoutActions);
                    for (Iterator<EdgeProxy> edgeIterator = includedMemebersEdges.get(anMember).iterator(); edgeIterator.hasNext();) {
                        EdgeProxy anEdge = edgeIterator.next();
                        BDD aForwardTransition = getEdgeBDDWithoutActions(anMember, anEdge);
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
                for (ExtendedAutomaton extendedAutomaton : members) {
                    if (!includedMembers.contains(extendedAutomaton)) {
                        BDDExtendedAutomaton bddAutomaton = automatonToBDDAutomatonMap.get(extendedAutomaton);
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

    private List<BDD> cloneAndClearBDDList(List<BDD> aBDDList) {
        List<BDD> temp = new ArrayList<BDD>(aBDDList);
        aBDDList.clear();
        return temp;
    }

    private BDD getEdgeBDDWithoutActions(ExtendedAutomaton anAutomaton, EdgeProxy anEdge) {

        BDDDomain sourceLocationDomain = bddExAutomata.getSourceLocationDomain(anAutomaton.getName());
        BDDDomain destLocationDomain = bddExAutomata.getDestLocationDomain(anAutomaton.getName());

        NodeProxy sourceLocation = anEdge.getSource();
        NodeProxy destLocation = anEdge.getTarget();
        int sourceLocationIndex = bddExAutomata.getLocationIndex(anAutomaton, sourceLocation);
        int destLocationIndex = bddExAutomata.getLocationIndex(anAutomaton, destLocation);
        BDD sourceBDD = manager.getFactory().buildCube(sourceLocationIndex, sourceLocationDomain.vars());
        BDD destBDD = manager.getFactory().buildCube(destLocationIndex, destLocationDomain.vars());

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
