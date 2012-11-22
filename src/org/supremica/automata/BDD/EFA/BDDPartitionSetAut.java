package org.supremica.automata.BDD.EFA;

import gnu.trove.TIntHashSet;
import gnu.trove.TIntIterator;
import gnu.trove.TIntObjectHashMap;
import java.util.Iterator;
import net.sf.javabdd.BDD;
import net.sourceforge.waters.model.module.EventDeclProxy;
import org.supremica.automata.ExtendedAutomaton;
import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;

/**
 * The class implements the automaton-based partitioning approach for DFAs.
 *
 * @author  Zhennan
 * @version 2.0
 */
public class BDDPartitionSetAut extends BDDPartitionSet {

    @SuppressWarnings("unused")
    static Logger logger = LoggerFactory.createLogger(BDDPartitionSetAut.class);
    
    /**
     * A map where the key is the index of an event in the index map while the value is its BDD.
     */
    private final TIntObjectHashMap<BDD> event2BDD;
    
    /**
     * A map where the key is the index of an automaton in the index map while the value is the BDD expression of partition.
     */
    private final TIntObjectHashMap<BDD> automatonToCompleteTransitionBDD;
    
    /**
     * Regarding the forward or backward reachability computation, the map maps an automaton index to the union of D(A).
     * 
     * Refer to the DFA partitioning paper. 
     */
    private TIntObjectHashMap<TIntHashSet> dependencyMap;
    
    /**
     * Size of the automata.
     */
    private int size;
    
    /** 
     * An event index list in which the source states of the edges labeled by the event index are the initial locations. 
     */
    private final TIntHashSet initialComponentCandidates;
    
    /** 
     * An event index list in which the target states of the edges labeled by the event index are the marked locations
     */
    private final TIntHashSet markedComponentCandidates;
    
    /**
     * A map where the key is an uncontrollable event index and the key is the corresponding BDD.
     */
    private TIntObjectHashMap<BDD> uncontrollableEventIndex2BDDMap;
    
    /**
     * The uncontrollable BDD, used for the uncontrollable backward reachability.
     */
    private BDD uncontrollableBDD;

    public BDDPartitionSetAut(BDDExtendedAutomata bddExAutomata) {
        super(bddExAutomata);

        this.size = orgAutomata.size();
        this.automatonToCompleteTransitionBDD = new TIntObjectHashMap<BDD>(size);
        this.dependencyMap = new TIntObjectHashMap<TIntHashSet>(size);
        this.initialComponentCandidates = new TIntHashSet();
        this.markedComponentCandidates = new TIntHashSet();
        this.event2BDD = new TIntObjectHashMap<BDD>(orgAutomata.getUnionAlphabet().size());
        this.uncontrollableEventIndex2BDDMap = new TIntObjectHashMap<BDD>();
        this.uncontrollableBDD = manager.getZeroBDD();
        initialize();
    }
    
    private void initialize() {
        constructEventBDDs();
        
        for (Iterator<ExtendedAutomaton> autItr = orgAutomata.iterator(); autItr.hasNext();) {
            ExtendedAutomaton automaton = autItr.next();
            BDDExtendedAutomaton bddAutomaton = bddExAutomata.getBDDExAutomaton(automaton);
            int autIndex = theIndexMap.getExAutomatonIndex(automaton.getName());
            automatonToCompleteTransitionBDD.put(autIndex, new AutomatonDisjParDepSet(autIndex, bddAutomaton).automatonForwardTransitionBDD);
        }
        
        int[] autIndexArray = automatonToCompleteTransitionBDD.keys();
        initialComponentCandidates.addAll(autIndexArray);
        markedComponentCandidates.addAll(autIndexArray);
    }
    
    private void constructEventBDDs() {
        for (Iterator<EventDeclProxy> eventItr = orgAutomata.getUnionAlphabet().iterator(); eventItr.hasNext();) {
            EventDeclProxy event = eventItr.next();
            int eventIndex = theIndexMap.getEventIndex(event);
            BDD eventBDD = manager.getFactory().buildCube(eventIndex, bddExAutomata.getEventDomain().vars());
            event2BDD.put(eventIndex, eventBDD);
            if (orgAutomata.getUncontrollableAlphabet().contains(event)) {
                uncontrollableEventIndex2BDDMap.put(eventIndex, eventBDD);
            }
        }
    }

    //##############################################################################################################################
    //#######################################INTERNAL CLASS#########################################################################
    class AutomatonDisjParDepSet {

        private final int automatonIndex;
        private BDDExtendedAutomaton bddAutomaton;
        private BDD automatonForwardTransitionBDD;
        
        private AutomatonDisjParDepSet (int automatonIndex, BDDExtendedAutomaton bddAutomaton) {
            this.automatonIndex = automatonIndex;
            this.bddAutomaton = bddAutomaton;
            this.automatonForwardTransitionBDD = manager.getZeroBDD();
            buildAutPartialTransitionBDD();
        }
        
        private void buildAutPartialTransitionBDD () {
            Iterator<BDDExtendedAutomaton> bddAutItr = bddExAutomata.iterator(); 
            BDD bddKeepAll = manager.getOneBDD();
            
            // get forward transition relation without self loops
            BDD isolatedForwardTransition = bddAutomaton.getEdgeForwardWithoutSelfLoops().id();
            TIntHashSet caredEventIndexSet = bddAutomaton.getCaredEventsIndex();
            
            while (bddAutItr.hasNext()) {
                BDDExtendedAutomaton other = bddAutItr.next();
                int otherIndex = theIndexMap.getExAutomatonIndex(other.theExAutomaton.getName());
                if (automatonIndex != otherIndex) {
                    TIntHashSet tmp = new TIntHashSet(caredEventIndexSet.toArray());
                    tmp.retainAll(other.getCaredEventsIndex().toArray());
                    if (!tmp.isEmpty()) {
                        if (dependencyMap.get(automatonIndex) == null)
                            dependencyMap.put(automatonIndex, new TIntHashSet());
                        dependencyMap.get(automatonIndex).add(otherIndex); // add otherIndex into the dependency set
                        
                        // build common and uncommon event BDDs
                        BDD commonEventsBDD = manager.getZeroBDD();
                        BDD uncommonEventsBDD = manager.getZeroBDD();
                        
                        for (TIntIterator eventItr = caredEventIndexSet.iterator(); eventItr.hasNext();) {
                            int eventIndex = eventItr.next();
                            if (tmp.contains(eventIndex)) // common events
                                commonEventsBDD = commonEventsBDD.or(event2BDD.get(eventIndex));
                            else
                                uncommonEventsBDD = uncommonEventsBDD.or(event2BDD.get(eventIndex));
                        }
                        
                        BDD otherCaredTransitionBDD = other.getEdgeForwardWithoutSelfLoops().and(commonEventsBDD);
                        BDD otherCaredKeepBDD = other.getSelfLoopsBDD().and(uncommonEventsBDD);
                        isolatedForwardTransition.andWith(otherCaredTransitionBDD.orWith(otherCaredKeepBDD));
                        
                    } else { // no shared events 
                        bddKeepAll = bddKeepAll.and(other.getSelfLoopsBDD());
                    }
                }
            }
            automatonForwardTransitionBDD.orWith(isolatedForwardTransition.andWith(bddKeepAll));
            for (TIntIterator unconEventItr = bddAutomaton.getCaredUncontrollableEventsIndex().iterator(); unconEventItr.hasNext();) {
                int anUnconEvent = unconEventItr.next();
                if (uncontrollableEventIndex2BDDMap.containsKey(anUnconEvent)) {
                    uncontrollableBDD = uncontrollableBDD.or(uncontrollableEventIndex2BDDMap.get(anUnconEvent).and(automatonForwardTransitionBDD)
                            .exist(bddExAutomata.getEventVarSet()));
                    uncontrollableEventIndex2BDDMap.remove(anUnconEvent);
                }
            }                           
            // Exist the event variables from the automaton forward transition BDD
            automatonForwardTransitionBDD = automatonForwardTransitionBDD.exist(bddExAutomata.getEventVarSet());
        }
    }

    @Override
    public TIntObjectHashMap<BDD> getCompIndexToCompBDDMap() {
        return automatonToCompleteTransitionBDD;
    }

    @Override
    protected TIntHashSet getInitialComponentCandidates() {
        return initialComponentCandidates; // return all of automata 
    }

    @Override
    protected TIntHashSet getMarkedComponentCandidates() {
        return markedComponentCandidates; // return all of automata
    }

    @Override
    protected BDD getUncontrollableTransitionRelationBDD() {
        return uncontrollableBDD;
    }

    @Override
    protected TIntObjectHashMap<TIntHashSet> getForwardDependentComponentMap() {
        return dependencyMap;
    }

    @Override
    protected TIntObjectHashMap<TIntHashSet> getBackwardDependentComponentMap() {
        return dependencyMap; // dependency is for forward and backward
    }
}
