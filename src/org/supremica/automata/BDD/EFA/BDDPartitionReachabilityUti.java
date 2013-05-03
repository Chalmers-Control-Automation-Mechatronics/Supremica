package org.supremica.automata.BDD.EFA;

import net.sf.javabdd.BDD;

/**
 * Image and PreImage operations 
 * 
 * @author  Zhennan
 * @version 2.0
 * @since   1.0
 * 
 * <p>
 * All the transition BDDs fed into each utility are forward. For preImage and restrictedPreImage,
 * exchange of BDD domains should be performed. The returned values from image and restrictedImage 
 * methods are <em>source states</em>, while the return values from preImage and restrictedPreImage methods 
 * are <em>target states</em>.
 */
public class BDDPartitionReachabilityUti {

    /**
     * Private constructor prevents from instantiating of this class.
     */
    private BDDPartitionReachabilityUti() {
    }

    /**
     * Returns a set of reachable states.   
     * <p>
     * The initial and returned reachableStatesBDD are "source states". 
     * 
     * @param bddAutomata the BDDExtendedAutomata object
     * @param reachableStatesBDD a set of reachable states
     * @param curTransitionRelation a partition
     * @return more states from <code>reachableStatesBDD</code> through <code>curTransitionRelation</code>
     */
    static BDD image(BDDExtendedAutomata bddAutomata, BDD reachableStatesBDD, BDD curTransitionRelation) {
        BDD noForbiddenStates = bddAutomata.getManager().getZeroBDD();
        return restrictedImage(bddAutomata, reachableStatesBDD, noForbiddenStates, curTransitionRelation);
    }

    /**
     * Returns a set of co-reachable states
     * <p>
     * The initial and returned coreachableStatesBDD are "target states". 
     * 
     * @param bddAutomata the BDDExtendedAutomata object
     * @param coreachableStatesBDD a set of co-reachable states
     * @param curTransitionRelation a partition
     * @return more states from <code>coreachableStatesBDD</code> through <code>curTransitionRelation</code>
     */
    static BDD preImage(BDDExtendedAutomata bddAutomata, BDD coreachableStatesBDD, BDD curTransitionRelation) {
        BDD noForbiddenStates = bddAutomata.getManager().getZeroBDD();
        return restrictedPreImage(bddAutomata, coreachableStatesBDD, noForbiddenStates, curTransitionRelation);
    }

    /**
     * Returns a set of reachable states excluding a set of forbidden states.   
     * <p>
     * The initial and returned reachableStatesBDD are "source states". 
     * 
     * @param bddAutomata the BDDExtendedAutomata object
     * @param reachableStatesBDD a set of reachable states
     * @param forbiddenStatesBDD a set of forbidden states
     * @param curTransitionRelation a partition
     * @return more states from <code>reachableStatesBDD</code> through <code>curTransitionRelation</code>
     */
    static BDD restrictedImage(BDDExtendedAutomata bddAutomata, BDD reachableStatesBDD, BDD forbiddenStatesBDD, BDD curTransitionRelation){
        
        BDD nextStates;
        BDD previousReachableStates;
        BDD premittedStates = forbiddenStatesBDD.not();
        reachableStatesBDD = reachableStatesBDD.and(premittedStates);
        
        do {
            previousReachableStates = reachableStatesBDD.id();
            nextStates = reachableStatesBDD.relprod(curTransitionRelation, bddAutomata.getSourceStatesVarSet());
            nextStates.replaceWith(bddAutomata.getDestToSourceLocationPairing()).replaceWith(bddAutomata.getDestToSourceVariablePairing());
            reachableStatesBDD.orWith(nextStates);
            reachableStatesBDD = reachableStatesBDD.and(premittedStates);
        } while (!reachableStatesBDD.equals(previousReachableStates));

        previousReachableStates.free();
        return reachableStatesBDD;
    }

    /**
     * Returns a set of co-reachable states excluding a set of forbidden states
     * <p>
     * The initial and returned coreachableStatesBDD are "target states". 
     * 
     * @param bddAutomata the BDDExtendedAutomata object
     * @param coreachableStatesBDD a set of co-reachable states
     * @param forbiddenStatesBDD  a set of forbidden states
     * @param curTransitionRelation a partition
     * @return more states from <code>coreachableStatesBDD</code> through <code>curTransitionRelation</code>
     */
    static BDD restrictedPreImage(BDDExtendedAutomata bddAutomata, BDD coreachableStatesBDD, BDD forbiddenStatesBDD, BDD curTransitionRelation) {
        
        BDD nextStates;
        BDD previousCoreachableStates;
        BDD premittedStates = forbiddenStatesBDD.not();
        coreachableStatesBDD = coreachableStatesBDD.and(premittedStates);

        do {
            previousCoreachableStates = coreachableStatesBDD.id();
            nextStates = coreachableStatesBDD.relprod(curTransitionRelation, bddAutomata.getDestStatesVarSet());
            nextStates.replaceWith(bddAutomata.getSourceToDestLocationPairing()).replaceWith(bddAutomata.getSourceToDestVariablePairing());
            coreachableStatesBDD.orWith(nextStates);
            coreachableStatesBDD = coreachableStatesBDD.and(premittedStates);
        } while (!coreachableStatesBDD.equals(previousCoreachableStates));

        previousCoreachableStates.free();
        return coreachableStatesBDD;
    }
}
