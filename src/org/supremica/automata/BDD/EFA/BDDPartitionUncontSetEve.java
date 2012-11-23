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
 * The class is used to compute the initial uncontrollable states given plants and specifications. 
 * The class is similar to the event based partitioning with the difference that the destination 
 * variables (EFA also include updated values of variables).
 * 
 * @author  Zhennan
 * @version 2.0
 * @since   1.0
 */

public class BDDPartitionUncontSetEve {

    static Logger logger = LoggerFactory.createLogger(BDDPartitionUncontSetEve.class);
    
    /**
     * The reference to the bddExAutomata.
     */
    private BDDExtendedAutomata bddExAutomata;
    
    /**
     * The reference to the manager.
     */
    private BDDExtendedManager manager;
    
    /**
     * Either a group of plants or specifications.
     */
    private List<ExtendedAutomaton> members;
    
    /**
     * A map where the key is an extended automaton and the value is its BDD version. 
     */
    private Map<ExtendedAutomaton, BDDExtendedAutomaton> automatonToBDDAutomatonMap;
    
    /** 
     * For plants, the fields contains the set of states which enable uncontrollable events,
     * For specifications, it contains the set of states which cannot enable uncontrollable events.
     */
    private TIntObjectHashMap<BDD> uncontrollableEvents2EnabledStates;
    
    /** 
     * Each uncontrollable event can appear on several edges on different automata. 
     * This field keeps track of them. 
     */
    private TIntObjectHashMap<HashMap<ExtendedAutomaton, ArrayList<EdgeProxy>>> uncontrollableEvent2MembersEdges;
    
    /** 
     * Uncontrollable event indices 
     */
    private TIntArrayList uncontrollableEventIndexList;

    public BDDPartitionUncontSetEve(BDDExtendedAutomata bddAutomata, List<ExtendedAutomaton> members, 
                                                                     TIntArrayList caredUncontrollableEventIndexList) {
        
        this.bddExAutomata = bddAutomata;
        this.manager = bddAutomata.getManager();
        this.members = members;
        this.automatonToBDDAutomatonMap = bddAutomata.automatonToBDDAutomatonMap;
        
        this.uncontrollableEventIndexList = caredUncontrollableEventIndexList;
        
        this.uncontrollableEvent2MembersEdges 
        = new TIntObjectHashMap<HashMap<ExtendedAutomaton, ArrayList<EdgeProxy>>> (uncontrollableEventIndexList.size());
        this.uncontrollableEvents2EnabledStates = new TIntObjectHashMap<BDD>(uncontrollableEventIndexList.size());
        
        initialize();
    }

    private void initialize() {
        uncontrollableEventIndexList.forEach(new TIntProcedure() {

            @Override
            public boolean execute(int eventIndex) {
                Set<ExtendedAutomaton> allAut = bddExAutomata.event2AutomatonsEdges.get(eventIndex).keySet();
                HashMap<ExtendedAutomaton, ArrayList<EdgeProxy>> tmp 
                        = new HashMap<ExtendedAutomaton, ArrayList<EdgeProxy>>();
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
                
                BDD currUnconEventTransitions = manager.getOneBDD();
                
                HashMap<ExtendedAutomaton, ArrayList<EdgeProxy>> includedMemebersEdges 
                        = uncontrollableEvent2MembersEdges.get(anUncontrollableEvent);
                Set<ExtendedAutomaton> includedMembers = includedMemebersEdges.keySet();
                Iterator<ExtendedAutomaton> automatonIterator = includedMembers.iterator();
                
                while (automatonIterator.hasNext()) {

                    ExtendedAutomaton anMember = automatonIterator.next();
                    BDD currUnconEventTransInCurrAut = manager.getZeroBDD();
                    
                    /* Iterate each */
                    for (Iterator<EdgeProxy> edgeIterator = includedMemebersEdges.get(anMember).iterator(); 
                                                                                              edgeIterator.hasNext();) {                        
                        EdgeProxy anEdge = edgeIterator.next();
                        BDD aBDDTransition = getEdgeBDDWithoutActions(anMember, anEdge);
                        currUnconEventTransInCurrAut.orWith(aBDDTransition);
                    }
                    
                    currUnconEventTransitions.andWith(currUnconEventTransInCurrAut);
                }
                
                BDD keep = manager.getOneBDD();
                for (ExtendedAutomaton extendedAutomaton : members) {
                    if (!includedMembers.contains(extendedAutomaton)) {
                        BDDExtendedAutomaton bddAutomaton = automatonToBDDAutomatonMap.get(extendedAutomaton);
                        keep = keep.and(bddAutomaton.getSelfLoopsBDD());
                    }
                }
                
                currUnconEventTransitions.andWith(keep);
                uncontrollableEvents2EnabledStates.put(anUncontrollableEvent, 
                        currUnconEventTransitions.exist(bddExAutomata.getDestStatesVarSet()));
                return true;
            }
        });
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

    public TIntObjectHashMap<BDD> getUncontrollableEvents2EnabledStates() {
        return uncontrollableEvents2EnabledStates;
    }
}
