package org.supremica.automata.BDD.EFA;

import net.sf.javabdd.BDD;

/**
 * Image and PreImage of the disjunctive partitioning 
 * 
 * @author zhennan
 * @version  1.0
 * 
 * Note: all the transition BDDs fed into each utility are forward. For preImage and  restrictedPreImage,
 * exchange of BDD domains should be performed. The returned values from image and restrictedImage 
 * methods are "source states", while the return values from preImage and restrictedPreImage methods 
 * are "target states".
 */
public class ReachabilityUtilities {

    // FOR TEST: the largest number of BDD nodes during the image computation
    // public static int maxNbrBDDnodes = 0;
    
    private ReachabilityUtilities() {
        throw new AssertionError();
    }

    /* The initial and returned reachableStatesBDD are "source states". */
    static BDD image(BDDExtendedAutomata bddAutomata, BDD reachableStatesBDD, BDD curTransitionRelation) {
        
        BDD nextStates;
        BDD previousReachableStates;
        
        do {
            previousReachableStates = reachableStatesBDD.id();
            nextStates = reachableStatesBDD.and(curTransitionRelation).exist(bddAutomata.getSourceStatesVarSet());
            nextStates.replaceWith(bddAutomata.getDestToSourceLocationPairing()).replaceWith(bddAutomata.getDestToSourceVariablePairing());
            reachableStatesBDD.orWith(nextStates);
        } while (!reachableStatesBDD.equals(previousReachableStates));

        previousReachableStates.free();
        return reachableStatesBDD;
    }

    /* The initial and returned coreachableStatesBDD are "target states". */
    static BDD preImage(BDDExtendedAutomata bddAutomata, BDD coreachableStatesBDD, BDD curTransitionRelation) {
        
        BDD nextStates;
        BDD previousCoreachableStates;

        do {
            previousCoreachableStates = coreachableStatesBDD.id();
            nextStates = coreachableStatesBDD.and(curTransitionRelation).exist(bddAutomata.getDestStatesVarSet());
            nextStates.replaceWith(bddAutomata.getSourceToDestLocationPairing()).replaceWith(bddAutomata.getSourceToDestVariablePairing());
            coreachableStatesBDD = coreachableStatesBDD.orWith(nextStates);
        } while (!coreachableStatesBDD.equals(previousCoreachableStates));

        previousCoreachableStates.free();
        return coreachableStatesBDD;
    }
    
        /* The initial and returned coreachableStatesBDD are "target states". The reachable states are the "source" states. */
    static BDD preImage2(BDDExtendedAutomata bddAutomata, BDD coreachableStatesBDD, BDD curTransitionRelation, BDD reachableStates) {
        
        BDD nextStates;
        BDD previousCoreachableStates;

        do {
            previousCoreachableStates = coreachableStatesBDD.id();
            nextStates = coreachableStatesBDD.and(curTransitionRelation).exist(bddAutomata.getDestStatesVarSet());
            nextStates = nextStates.and(reachableStates)
                    .replaceWith(bddAutomata.getSourceToDestLocationPairing()).replaceWith(bddAutomata.getSourceToDestVariablePairing());
            coreachableStatesBDD = coreachableStatesBDD.orWith(nextStates);
        } while (!coreachableStatesBDD.equals(previousCoreachableStates));

        previousCoreachableStates.free();
        return coreachableStatesBDD;
    }

    /* Both of reachableStatesBDD and forbiddenStatesBDD are "source states". */
    static BDD restrictedImage(BDDExtendedAutomata bddAutomata, BDD reachableStatesBDD, BDD forbiddenStatesBDD, BDD curTransitionRelation){
        
        BDD nextStates;
        BDD previousReachableStates;
        BDD premittedStates = forbiddenStatesBDD.not();
        reachableStatesBDD = reachableStatesBDD.and(premittedStates);
        
        do {
            previousReachableStates = reachableStatesBDD.id();
            nextStates = reachableStatesBDD.and(curTransitionRelation).exist(bddAutomata.getSourceStatesVarSet());
            nextStates.replaceWith(bddAutomata.getDestToSourceLocationPairing()).replaceWith(bddAutomata.getDestToSourceVariablePairing());
            reachableStatesBDD.orWith(nextStates);
            reachableStatesBDD = reachableStatesBDD.and(premittedStates);
        } while (!reachableStatesBDD.equals(previousReachableStates));

        previousReachableStates.free();
        return reachableStatesBDD;
    }

    /* Both of coreachableStatesBDD and forbiddenStatesBDD ought to be "target states". */
    static BDD restrictedPreImage(BDDExtendedAutomata bddAutomata, BDD coreachableStatesBDD, BDD forbiddenStatesBDD, BDD curTransitionRelation) {
        
        BDD nextStates;
        BDD previousCoreachableStates;
        BDD permittedStates = forbiddenStatesBDD.not();
        coreachableStatesBDD = coreachableStatesBDD.and(permittedStates);

        do {
            previousCoreachableStates = coreachableStatesBDD.id();
            nextStates = coreachableStatesBDD.and(curTransitionRelation).exist(bddAutomata.getDestStatesVarSet());
            nextStates.replaceWith(bddAutomata.getSourceToDestLocationPairing()).replaceWith(bddAutomata.getSourceToDestVariablePairing());
            coreachableStatesBDD.orWith(nextStates);
            coreachableStatesBDD = coreachableStatesBDD.and(permittedStates);
        } while (!coreachableStatesBDD.equals(previousCoreachableStates));

        previousCoreachableStates.free();
        return coreachableStatesBDD;
    }
    
    /* Both of coreachableStatesBDD and forbiddenStatesBDD ought to be "target states". 
       The reachable states are the "source states".
     */
    static BDD restrictedPreImage2(BDDExtendedAutomata bddAutomata, BDD coreachableStatesBDD, BDD forbiddenStatesBDD, 
                                   BDD curTransitionRelation, BDD reachableStates) {
        
        BDD nextStates;
        BDD previousCoreachableStates;
        BDD permittedStates = forbiddenStatesBDD.not();
        coreachableStatesBDD = coreachableStatesBDD.and(permittedStates);

        do {
            previousCoreachableStates = coreachableStatesBDD.id();
            nextStates = coreachableStatesBDD.and(curTransitionRelation).exist(bddAutomata.getDestStatesVarSet());
            nextStates = nextStates.and(reachableStates)
                    .replaceWith(bddAutomata.getSourceToDestLocationPairing()).replaceWith(bddAutomata.getSourceToDestVariablePairing())
                    .and(permittedStates);
            coreachableStatesBDD.orWith(nextStates);
        } while (!coreachableStatesBDD.equals(previousCoreachableStates));

        previousCoreachableStates.free();
        return coreachableStatesBDD;
    }
}
