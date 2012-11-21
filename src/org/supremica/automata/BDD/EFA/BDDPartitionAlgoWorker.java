package org.supremica.automata.BDD.EFA;

import gnu.trove.TIntHashSet;
import java.util.Random;
import net.sf.javabdd.BDD;
import static org.supremica.automata.BDD.EFA.BDDPartitionReachabilityUti.preImage;

/**
 * The interface for the set of algorithms used to carry out the verification and synthesis for the partitioning technique.
 * Each BDDPartitioningSet has a reference to one of the subclasses of this class to perform its own fix-point computation. 
 *   
 * @author  Zhennan
 * @version 2.0
 */

public abstract class BDDPartitionAlgoWorker {
    
    /**
     * The reference to partitions to get the BDDs.
     */
    protected BDDPartitionSet partitions;
    
    /**
     * The reference to coordinator. 
     */
    protected BDDPartitionCoordinator coordinator;
    
    /**
     * Constructs a BDDPartitionAlgoWorker.
     * 
     * @param partitions all the partitions
     * @param coordinator the coordinator to these partitions
     */
    public BDDPartitionAlgoWorker(BDDPartitionSet partitions, BDDPartitionCoordinator coordinator) {
        this.partitions = partitions;
        this.coordinator = coordinator;
    }
    
    /* Non abstract methods */
    
    /**
     * Returns a Boolean value indicating whether the algorithm should stop or not.
     * 
     *  @return {@code true} if the included coordinator does not have any active component
     * 	       {@code false} if the coordinator has some active components
     */
    public boolean shouldStop () {
        return coordinator.empty();
    }
    
    /**
     * Returns a component index for the forward reachability computation
     * 
     * @return the index of a component
     */
    public int pickCompFwd() {
        return coordinator.pickOne(true);
    }

    /**
     * Returns a component index for the backward reachability computation
     * 
     * @return the index of a component
     */
    public int pickCompBwd() {
        return coordinator.pickOne(false);
    }
    
    /**
     * Returns the BDD for the component.
     * 
     * @param compIndex
     * @return the partitioning BDD corresponding to the component index
     */
    public BDD getCompBDD(int compIndex) {
        return partitions.getCompIndexToCompBDDMap().get(compIndex);
    }
    
    public void  resetCoordinator(){
        coordinator.reset();
    }
    
    /**
     * Regarding the forward reachability, records changes based on the chosen component.
     * 
     * @param changed whether the chosen component changed the temporary BDD
     */
    public void record_forward(boolean changed) {
        coordinator.advance(true, changed);
    }
    
    /**
     * Regarding the forward reachability, records changes based on the chosen component.
     * 
     * @param changed whether the chosen component changed the temporary BDD
     */
    public void record_backward(boolean changed) {
        coordinator.advance(false, changed);
    }

    /** 
     * Given the initial states, the method returns a set of states that can be reached from the initial state. 
     * 
     * @param initialStates initial states, for deterministic automata, there is only one initial state
     * @return a set of states which can be reached from initialStates
     */
    public abstract BDD forwardWorkSetAlgorithm(BDD initialStates);
    
    /** 
     * The method returns a set of states which are reached from initial states but not forbidden.
     * 
     * @param initialStates initial states, for deterministic automata, there is only one initial state
     * @param forbiddenStates a set of states which must be forbidden
     * @return a set of states which are reached from initial states but not forbidden
     */
    public abstract BDD forwardRestrictedWorkSetAlgorithm(BDD initialStates, BDD forbiddenStates);
    
    /** 
     * Given a set of marked states, the method returns a set of states which can be co-reached
     * from any marked state. 
     * 
     * @param markedStates a set of marked states
     * @return a set of states which can be co-reached from any of the marked states
     */
    public abstract BDD backwardWorkSetAlgorithm(BDD markedStates);
    
    /** 
     * Given a et of marked states and all reachable states, the method returns the subset of reachable states
     * which are co-reached from any of the marked states.
     * 
     * @param markedStates a set of marked states
     * @param reachableStates all reachable states
     * @return a subset of reachable states which are also co-reachable 
     */
    public abstract BDD reachableBackwardWorkSetAlgorithm(BDD markedStates, BDD reachableStates);
    
    /** 
     * The method returns a set of states which are co-reached from the marked states but not forbidden.
     * 
     * @param markedStates a set of marked states
     * @param forbiddenStates a set of states which must be forbidden
     * @return a set of states which are co-reached from the marked states but not forbidden
     */
    public abstract BDD backwardRestrictedWorkSetAlgorithm(BDD markedStates, BDD forbiddenStates);
    
    /** 
     * The method returns a set of reachable states which are co-reached from the marked states but not forbidden.
     * 
     * @param markedStates a set of marked states
     * @param forbiddenStates a set of states which must be forbidden
     * @param reachableStates a set of reachable states
     * @return a set of states which are co-reached from the marked states but not forbidden
     */
    public abstract BDD reachableBackwardRestrictedWorkSetAlgorithm(BDD markedStates, BDD forbiddenStates, BDD reachableStates);
    
    /** 
     * Given a set of forbidden states (either uncontrollable states or explicitly forbidden states), 
     * the method does the backward reachability through any uncontrollable event to find more uncontrollable states.
     * 
     * @param forbiddenStates a set of forbidden states
     * @return a set of forbidden states which is a superset of forbiddenStates 
     */
    public BDD uncontrollableBackwardWorkSetAlgorithm(BDD forbiddenStates) {
        
        BDD uncontrollableTransitionRelationBDD = partitions.getUncontrollableTransitionRelationBDD();
        
        BDD forbiddenStatesAsTargetStates = forbiddenStates.id()
                .replaceWith(partitions.bddExAutomata.sourceToDestLocationPairing)
                .replaceWith(partitions.bddExAutomata.sourceToDestVariablePairing);
        
        return preImage(partitions.bddExAutomata, forbiddenStatesAsTargetStates, uncontrollableTransitionRelationBDD)
                                               .replaceWith(partitions.bddExAutomata.destToSourceLocationPairing)
                                               .replaceWith(partitions.bddExAutomata.destToSourceVariablePairing);
    }
}
