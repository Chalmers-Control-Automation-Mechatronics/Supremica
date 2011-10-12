package org.supremica.automata.BDD.EFA;

import net.sf.javabdd.BDD;

/**
 * The interface for the set of algorithms used to realize the verification and synthesis for the disjunctive partitioning technique.
 *   
 * @author Zhennan
 * @version  1.0
 */

public interface BDDExDisjAlgorithms {
    
    /** Given the initial state (deterministic EFAs), the method returns a set of states (expressed in terms of BDD) which can
     *   can be reached from the initial state. The method is typically used to find all the reachable from the initial state while
     *   disregards the forbidden states during the search. 
     */
    public BDD forwardWorkSetAlgorithm(BDD initialStates);
    
    /** Given a set of marked states, the method returns a set of states (expressed in terms of BDD) which can be co-reached
     *   from any marked state. The method is typically used to find all the co-reachable states from the marked states while
     *   disregard the forbidden states. 
     */
    public BDD backwardWorkSetAlgorithm(BDD markedStates);
    
    /** Here is a convenient method to get the both reachable and co-reachable states, aka, the nonblocking states.  */
    public BDD reachableBackwardWorkSetAlgorithm(BDD markedStates, BDD reachableStates);
    
    /** The difference between this method and forwardWorSetAlgorithm is that during the synthesis, it is required to exclude
     *   all the forbidden states when finding all the reachable states from the initial state. 
     */
    public BDD forwardRestrictedWorkSetAlgorithm(BDD initialStates, BDD forbiddenStates);
    
    /** The difference between this method and backwardWorSetAlgorithm is that during the synthesis, it is required to exclude
     *   all the forbidden states when finding all the co-reachable states from the initial state. 
     */
    public BDD backwardRestrictedWorkSetAlgorithm(BDD markedStates, BDD forbiddenStates);
    
    /** Given a set of forbidden states (uncontrollable states or explicit forbidden states), the method is used to do the backward reachability through 
     *   any uncontrollable event to find more uncontrollable states which will append to the forbidden states
     */
    public BDD uncontrollableBackwardWorkSetAlgorithm(BDD forbiddenStates);
}
