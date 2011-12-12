package org.supremica.automata.BDD.EFA;

import net.sf.javabdd.BDD;
import gnu.trove.TIntArrayList;
import gnu.trove.TIntHashSet;
import gnu.trove.TIntIntHashMap;
import gnu.trove.TIntObjectHashMap;
import gnu.trove.TIntProcedure;
import java.util.HashSet;
import java.util.Map;
import org.supremica.automata.ExtendedAutomaton;

/**
 * Automaton-based disjunctive partitions
 * 
 * @author zhennan
 * @version 1.0
 * 
 */
public class BDDExDisjAutmatonDepSets extends BDDExDisjDepSetsDecorator {

    /* Define a map where the key is the index of an automaton while the value is an instance of AutomatonDisjParDepSet*/
    private TIntObjectHashMap<AutomatonDisjParDepSet> aut2AutDisjParDepSet;
    
    /* A map from the index of an automaton to the partitioned BDD expression*/
    private TIntObjectHashMap<BDD> automatonToTransitionBDD;
    
    /* Used for heuristics: a map where the key is the index of an event while the value is the influenced automata indices*/
    private TIntObjectHashMap<TIntIntHashMap> automaton2ForwardInfluencedAutomata;
    
    private TIntObjectHashMap<TIntIntHashMap> automaton2BackwardInfluencedAutomata;
   
    private Map<ExtendedAutomaton, BDDExtendedAutomaton> automatonToBDDAutomatonMap;
    
    private TIntObjectHashMap<HashSet<String>> autIndex2UpdatedVariables;
    private TIntObjectHashMap<HashSet<String>> autIndex2GuardVariables;
    
    /** Automaton index to its alphabet map */
    private TIntObjectHashMap<TIntArrayList> autIndex2CaredEventIndices;
    
    /** An automaton index list in which each automaton can be initial component candidate. Whether an automaton index 
     *   is in this list depends on the cared events it has. If the automaton-based BDD partition has an event-based BDD partition
     *   which is qualified as the initial component in the event-based partitioning. Then the automaton is qualified.  
     */
    private TIntHashSet initialComponentCandidates;
    
    /** Similar as before but for markedComponentCandidates. */
    private TIntHashSet markedComponentCandidates;
    
    /** For uncontrollable backward search */
    private TIntHashSet uncontrollableComponentCandidates;
    
    public BDDExDisjAutmatonDepSets(BDDExtendedAutomata bddExAutomata, BDDExDisjEventDepSets eventParDepSets) {
        
        super(bddExAutomata, eventParDepSets);
        this.automatonToBDDAutomatonMap = bddExAutomata.automatonToBDDAutomatonMap;
        this.size = theExAutomata.size();
        
        this.aut2AutDisjParDepSet = new TIntObjectHashMap<AutomatonDisjParDepSet>(size);
        this.automatonToTransitionBDD = new TIntObjectHashMap<BDD>(size);
        
        this.autIndex2CaredEventIndices = new TIntObjectHashMap<TIntArrayList>(size);
        
        this.autIndex2UpdatedVariables = new TIntObjectHashMap<HashSet<String>>(size);
        this.autIndex2GuardVariables = new TIntObjectHashMap<HashSet<String>>(size);
        
        this.automaton2ForwardInfluencedAutomata = new TIntObjectHashMap<TIntIntHashMap>(size);
        this.automaton2BackwardInfluencedAutomata = new TIntObjectHashMap<TIntIntHashMap>(size);
        
        
        this.initialComponentCandidates = new TIntHashSet();
        this.markedComponentCandidates = new TIntHashSet();
        this.uncontrollableComponentCandidates = new TIntHashSet();
        
        initialize();
    }

    @Override
    protected final void initialize() {
       for(ExtendedAutomaton a: theExAutomata){
            aut2AutDisjParDepSet.put(theIndexMap.getExAutomatonIndex(a.getName()),
                                                                                 new AutomatonDisjParDepSet(automatonToBDDAutomatonMap.get(a)));
       }
    }

    @Override
    public TIntObjectHashMap<BDD> getComponentToComponentTransMap() {
        return automatonToTransitionBDD;
    }

    @Override
    protected TIntObjectHashMap<TIntIntHashMap> getForwardComponentToInfluencedComponentMap() {

        if (automaton2ForwardInfluencedAutomata.isEmpty()) {
            if (!orgAutomata.getVars().isEmpty()) {
                int[] autIndicesAsKeys = autIndex2CaredEventIndices.keys();
                for (int i = 0; i < autIndicesAsKeys.length; i++) {
                    int autIndex = autIndicesAsKeys[i];
                    TIntIntHashMap tmp = new TIntIntHashMap();
                    HashSet<String> updatedVariables = autIndex2UpdatedVariables.get(autIndex);
                    for (int j = 0; j < autIndicesAsKeys.length; j++) {
                        HashSet<String> guardVariables = null;
                        if (i != j) {
                            guardVariables = autIndex2GuardVariables.get(autIndicesAsKeys[j]);
                            guardVariables.retainAll(updatedVariables);
                            tmp.put(autIndicesAsKeys[j], guardVariables.size());
                        }
                    }
                    automaton2ForwardInfluencedAutomata.put(autIndex, tmp);
                }
            } else { // Pure DFAs
                int[] autIndicesAsKeys = autIndex2CaredEventIndices.keys();
                for (int i = 0; i < autIndicesAsKeys.length; i++) {
                    TIntArrayList caredEventIndices_i = autIndex2CaredEventIndices.get(autIndicesAsKeys[i]);
                    TIntIntHashMap tmp = new TIntIntHashMap(autIndicesAsKeys.length);
                    for (int j = 0; j < autIndicesAsKeys.length; j++) {
                        if (autIndicesAsKeys[i] != autIndicesAsKeys[j]) {
                            int sharedEventSize = 0;
                            TIntArrayList caredEventIndices_j = autIndex2CaredEventIndices.get(autIndicesAsKeys[j]);
                            for (int index_i = 0; index_i < caredEventIndices_i.size(); index_i++) {
                                for (int index_j = 0; index_j < caredEventIndices_j.size(); index_j++) {
                                    if (caredEventIndices_i.get(index_i) == caredEventIndices_j.get(index_j)) {
                                        sharedEventSize++;
                                    }
                                }
                            }
                            tmp.put(autIndicesAsKeys[j], sharedEventSize);
                        }
                    }
                    automaton2ForwardInfluencedAutomata.put(autIndicesAsKeys[i], tmp);
                }
            }
        }
        return automaton2ForwardInfluencedAutomata;
    }

    @Override
    protected TIntObjectHashMap<TIntIntHashMap> getBackwardComponentToInfluencedComponentMap() {

        if (automaton2BackwardInfluencedAutomata.isEmpty()) {
            if (orgAutomata.getVars().isEmpty()) {
                automaton2BackwardInfluencedAutomata = getForwardComponentToInfluencedComponentMap();
            } else {
                int[] autIndicesAsKeys = autIndex2CaredEventIndices.keys();
                for (int i = 0; i < autIndicesAsKeys.length; i++) {
                    int autIndex = autIndicesAsKeys[i];
                    TIntIntHashMap tmp = new TIntIntHashMap();
                    HashSet<String> guardVariables = autIndex2GuardVariables.get(autIndex);
                    for (int j = 0; j < autIndicesAsKeys.length; j++) {
                        HashSet<String> updatedVariables = null;
                        if (i != j) {
                            updatedVariables = autIndex2UpdatedVariables.get(autIndicesAsKeys[j]);
                            updatedVariables.retainAll(guardVariables);
                            tmp.put(autIndicesAsKeys[j], updatedVariables.size());
                        }
                    }
                    automaton2BackwardInfluencedAutomata.put(autIndex, tmp);
                }
            }
        }
        return automaton2BackwardInfluencedAutomata;
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
        return eventParDepSets.getUncontrollableTransitionRelationBDD();
    }
    
    /* For each automaton, initiate an instance of AutomatonDisjParDepSet. */
    class AutomatonDisjParDepSet {

        private ExtendedAutomaton myExtendedAutomaton;
        private BDDExtendedAutomaton me;
        private BDD partialForwardTransition;
        
        private int myIndex;
        private HashSet<String> updatedVariables;
        private HashSet<String> guardVariables;

        public AutomatonDisjParDepSet(BDDExtendedAutomaton me) {
            this.me = me;
            this.myExtendedAutomaton = me.getExAutomaton();
            this.partialForwardTransition = manager.getZeroBDD();
            
            this.myIndex = theIndexMap.getExAutomatonIndex(myExtendedAutomaton.getName());
            this.updatedVariables = new HashSet<String>();
            this.guardVariables = new HashSet<String>();
            initialize();
        }

        /* 1. Get the alphabet of the automaton "me". 
            2. For each event, find the event-based BDD expression of it, if it is not taken by others
            3. Merge them together.   That's it. */
        private void initialize() {
            
            TIntArrayList caredEventsIndex = me.caredEventsIndex;
            caredEventsIndex.forEach(new TIntProcedure() {          
               
                @Override
                public boolean execute(int currCaredEventIndex) {  
                        
                        TIntObjectHashMap<BDD> event2BDD = eventParDepSets.getComponentToComponentTransMap();
                            
                        partialForwardTransition = partialForwardTransition.or(event2BDD.get(currCaredEventIndex));
                        
                        if(eventParDepSets.getInitialComponentCandidates().contains(currCaredEventIndex))
                            initialComponentCandidates.add(myIndex);
                        
                        if(eventParDepSets.getMarkedComponentCandidates().contains(currCaredEventIndex))
                            markedComponentCandidates.add(myIndex);
                        
                        if(eventParDepSets.getUncontrollableComponentCandidates().contains(currCaredEventIndex))
                            uncontrollableComponentCandidates.add(myIndex);
                        
                        if(eventParDepSets.getEventIndex2UpdatedVariables().get(currCaredEventIndex) != null)
                            updatedVariables.addAll(eventParDepSets.getEventIndex2UpdatedVariables().get(currCaredEventIndex));
                        
                        if(eventParDepSets.getEventIndex2GuardVariables().get(currCaredEventIndex) != null)
                            guardVariables.addAll(eventParDepSets.getEventIndex2GuardVariables().get(currCaredEventIndex));   
                        
                    return true;
                }
            });
            
            autIndex2CaredEventIndices.put(myIndex, me.caredEventsIndex);
            autIndex2UpdatedVariables.put(myIndex, updatedVariables);
            autIndex2GuardVariables.put(myIndex, guardVariables);
            automatonToTransitionBDD.put(myIndex, partialForwardTransition);
        }
    }
}
