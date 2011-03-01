package org.supremica.automata.BDD.EFA;

import net.sf.javabdd.BDD;

/**
 *
 * @author zhennan
 *
 * Image and PreImage of the disjunctive partitioning 
 *
 */
public class ReachabilityUtilities {

    private ReachabilityUtilities() {
        throw new AssertionError();
    }

    static BDD image(BDDExtendedAutomata bddAutomata, BDD reachableStatesBDD, BDD curTransitionRelation) {
        BDD nextStates;
        BDD previousReachableStates;

        do {
            previousReachableStates = reachableStatesBDD.id();
            nextStates = reachableStatesBDD.and(curTransitionRelation).exist(bddAutomata.sourceStateVariables);
            nextStates.replaceWith(bddAutomata.destToSourceLocationPairing).replaceWith(bddAutomata.destToSourceVariablePairing);
            reachableStatesBDD.orWith(nextStates);
        } while (!reachableStatesBDD.equals(previousReachableStates));

        previousReachableStates.free();
        return reachableStatesBDD;
    }

    static BDD preImage(BDDExtendedAutomata bddAutomata, BDD coreachableStatesBDD, BDD curTransitionRelation) {
        BDD nextStates;
        BDD previousCoreachableStates;

        do {
            previousCoreachableStates = coreachableStatesBDD.id();
            nextStates = coreachableStatesBDD.and(curTransitionRelation).exist(bddAutomata.sourceStateVariables);
            nextStates.replaceWith(bddAutomata.destToSourceLocationPairing).replaceWith(bddAutomata.destToSourceVariablePairing);
            coreachableStatesBDD = coreachableStatesBDD.orWith(nextStates);
        } while (!coreachableStatesBDD.equals(previousCoreachableStates));

        previousCoreachableStates.free();
        return coreachableStatesBDD;
    }

    static BDD restrictedImage(BDDExtendedAutomata bddAutomata, BDD reachableStatesBDD, BDD forbiddenStatesBDD, BDD curTransitionRelation){
        BDD nextStates;
        BDD previousReachableStates;
        BDD premittedStates = forbiddenStatesBDD.not();
        reachableStatesBDD = reachableStatesBDD.and(premittedStates);
        do {
            previousReachableStates = reachableStatesBDD.id();
            nextStates = reachableStatesBDD.and(curTransitionRelation).exist(bddAutomata.sourceStateVariables);
            nextStates.replaceWith(bddAutomata.destToSourceLocationPairing).replaceWith(bddAutomata.destToSourceVariablePairing);
            reachableStatesBDD.orWith(nextStates);
            reachableStatesBDD = reachableStatesBDD.and(premittedStates);
        } while (!reachableStatesBDD.equals(previousReachableStates));

        previousReachableStates.free();
        return reachableStatesBDD;
    }

    static BDD restrictedPreImage(BDDExtendedAutomata bddAutomata, BDD coreachableStatesBDD, BDD forbiddenStatesBDD, BDD curTransitionRelation) {
        BDD nextStates;
        BDD previousCoreachableStates;
        BDD premittedStates = forbiddenStatesBDD.not();
        coreachableStatesBDD = coreachableStatesBDD.and(premittedStates);

        do {
            previousCoreachableStates = coreachableStatesBDD.id();
            nextStates = coreachableStatesBDD.and(curTransitionRelation).exist( bddAutomata.sourceStateVariables);
            nextStates.replaceWith(bddAutomata.destToSourceLocationPairing).replaceWith(bddAutomata.destToSourceVariablePairing);
            coreachableStatesBDD.orWith(nextStates);
             coreachableStatesBDD = coreachableStatesBDD.and(premittedStates);
        } while (!coreachableStatesBDD.equals(previousCoreachableStates));

        previousCoreachableStates.free();
        return coreachableStatesBDD;
    }
}
