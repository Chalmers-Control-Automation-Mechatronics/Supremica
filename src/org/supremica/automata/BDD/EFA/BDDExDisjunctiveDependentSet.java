package org.supremica.automata.BDD.EFA;

import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;
import net.sf.javabdd.BDD;
import gnu.trove.TIntArrayList;
import gnu.trove.TIntIntHashMap;
import gnu.trove.TIntObjectHashMap;
import gnu.trove.TIntProcedure;
import gnu.trove.TObjectIntHashMap;
import java.util.Iterator;
import org.supremica.automata.BDD.EFA.EventDisParDepSets.EventDisParDepSet;
import org.supremica.automata.ExtendedAutomaton;

/**
 *
 * @author zhennan
 *
 */

public class BDDExDisjunctiveDependentSet {

    static Logger logger = LoggerFactory.createLogger(BDDExDisjunctiveDependentSet.class);
    private BDDExtendedAutomata bddAutomata;
    private BDDExtendedManager manager;
    private ExtendedAutomaton myExtendedAutomaton;
    private BDDExtendedAutomaton me;

    /* Indices of ExtendedAutomata which share events with me*/
    private TIntArrayList eventDependentAutomata;
    /* Used for Heuristics, the variables updated by enabling the event might lead to the
       guards in other extended automata evaluated to be true. Besides, it records how many*/
    private TIntObjectHashMap<TIntIntHashMap> eventIndex2VariableDepAutomata;
    /* For each event in the alpabet of me, use its event partial transition to constrcut
       my partial transition (forward)*/
    private TIntObjectHashMap<EventDisParDepSet> events2EventDisParDepSet;
    /* The partitioned BDD transitions for me. */
    private BDD partialForwardTransition;

    public BDDExDisjunctiveDependentSet(BDDExtendedManager bddExtendedManager, BDDExtendedAutomaton me) {
        this.manager = bddExtendedManager;
        this.bddAutomata = manager.bddExAutomata;
        this.me = me;
        this.eventDependentAutomata = new TIntArrayList();
        this.myExtendedAutomaton = me.getExAutomaton();
        initialize();
    }

    private void initialize() {
        partialForwardTransition = manager.getZeroBDD();
        TIntArrayList caredEventsIndex = me.caredEventsIndex;
        eventIndex2VariableDepAutomata = new TIntObjectHashMap(caredEventsIndex.size());
        events2EventDisParDepSet = bddAutomata.getEvents2EventDisParDepSet();

        caredEventsIndex.forEach(new TIntProcedure() {
            @Override
            public boolean execute(int currCaredEventIndex) {

                // Complete the eventBDD2indices in bddAutomata in order to realize the heuristics for variables.
                BDD eventBDD = manager.getFactory().buildCube(currCaredEventIndex, bddAutomata.getEventDomain().vars());
                if (!bddAutomata.eventBDD2eventIndices.containsKey(eventBDD)) {
                    bddAutomata.eventBDD2eventIndices.put(eventBDD, currCaredEventIndex);
                }
                
                EventDisParDepSet eventDisDepSet = events2EventDisParDepSet.get(currCaredEventIndex);

                boolean flag = false;
                // Iterate each automaton which includes the current event. 
                // If the automaton isn't me, add it (no duplicate) into the event dependent automaton list.
                for (Iterator<ExtendedAutomaton> autIterator = eventDisDepSet.getIncludingAutomata().iterator(); autIterator.hasNext();) {
                    ExtendedAutomaton aut = autIterator.next();
                    String autName = aut.getName();
                    flag = !eventDependentAutomata.contains(bddAutomata.theIndexMap.getExAutomatonIndex(autName))
                        && aut!=myExtendedAutomaton;
                    if (flag) {
                        eventDependentAutomata.add(bddAutomata.theIndexMap.getExAutomatonIndex(autName));
                    }
                }

                TIntIntHashMap autIndex2nbrOfInfluencedVariables = null;
                TObjectIntHashMap<ExtendedAutomaton> aut2nbrOfInfluencedVariables = eventDisDepSet.getAutomaton2nbrOfInfluencedVariables();
                Object [] keys = aut2nbrOfInfluencedVariables.keys();

                for(int i = 0; i < keys.length; i++){
                    autIndex2nbrOfInfluencedVariables = new TIntIntHashMap();
                    ExtendedAutomaton aut = (ExtendedAutomaton)keys[i];
                    if(aut2nbrOfInfluencedVariables.get(aut) > 0){
                        int autIndex = bddAutomata.theIndexMap.getExAutomatonIndex(aut.getName());
                        autIndex2nbrOfInfluencedVariables.put(autIndex, aut2nbrOfInfluencedVariables.get(aut));
                    }
                }

                if(autIndex2nbrOfInfluencedVariables!=null && autIndex2nbrOfInfluencedVariables.size() > 0)
                    eventIndex2VariableDepAutomata.put(currCaredEventIndex, autIndex2nbrOfInfluencedVariables);

                partialForwardTransition = partialForwardTransition.or(eventDisDepSet.getEventForwardPartialTransitions().and(eventBDD));
                return true;
            }
        });
    }

    public TIntArrayList getEventDependentAutomata() {
        return eventDependentAutomata;
    }

    public BDD getPartialForwardTransition() {
        return partialForwardTransition;
    }

    public BDD getPartialBackwardTransition() {
        return getPartialForwardTransition().id()
                .replaceWith(bddAutomata.sourceToTempLocationPairing)
                .replaceWith(bddAutomata.destToSourceLocationPairing)
                .replaceWith(bddAutomata.tempToDestLocationPairing)
                .replaceWith(bddAutomata.sourceToTempVariablePairing)
                .replaceWith(bddAutomata.destToSourceVariablePairing)
                .replaceWith(bddAutomata.tempToDestVariablePairing);
    }

    /*Used for reachability search, not considering events*/
    public BDD getPartialForwardTransitionRelation(){
        return getPartialForwardTransition().exist(bddAutomata.getEventVarSet());
    }

    /*Used for coreachability search, not considering events*/
    public BDD getPartialBackwardTransitionRelation(){
        return getPartialBackwardTransition().exist(bddAutomata.getEventVarSet());
    }

    /*Used for synthesis algorithm*/
    public BDD getUncontrollableBackwardTransitionRelation(){
        return (getPartialBackwardTransition().and(bddAutomata.uncontrollableEventsBDD))
                .exist(bddAutomata.getEventVarSet());
    }

    public TIntObjectHashMap<TIntIntHashMap> getEventIndex2VariableDepAutomata() {
        return eventIndex2VariableDepAutomata;
    }

}
