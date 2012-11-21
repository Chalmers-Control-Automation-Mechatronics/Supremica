package org.supremica.automata.BDD.EFA;

import net.sf.javabdd.BDD;
import static org.supremica.automata.BDD.EFA.BDDPartitionReachabilityUti.restrictedImage;
import static org.supremica.automata.BDD.EFA.BDDPartitionReachabilityUti.restrictedPreImage;

/**
 * The class implements these fix-point automaton-based algorithms for synthesis and verification.
 * 
 * @author zhennanfei
 */

public class BDDPartitionAlgoWorkerAut extends BDDPartitionAlgoWorker{
    public BDDPartitionAlgoWorkerAut(BDDPartitionSet partitions, BDDPartitionCoordinator coordinator) {
        super(partitions, coordinator);
    }
    
    @Override
    public BDD forwardWorkSetAlgorithm(BDD initialStates) {
      
        BDD noForbiddenStates = partitions.bddExAutomata.manager.getZeroBDD();
        return internalForwardRestrictedWorkSetAlgorithm(initialStates, noForbiddenStates);
    }
    
    @Override
    public BDD forwardRestrictedWorkSetAlgorithm(BDD initialStates, BDD forbiddenStates) {
        
        BDD currentReachableStatesBDD = initialStates;
        BDD previousReachableStatesBDD = null;
        
        do {
            previousReachableStatesBDD = currentReachableStatesBDD.id();
            currentReachableStatesBDD = internalForwardRestrictedWorkSetAlgorithm(currentReachableStatesBDD, forbiddenStates);
        } while (!previousReachableStatesBDD.equals(currentReachableStatesBDD));
        
        return currentReachableStatesBDD;
    }
    
    private BDD internalForwardRestrictedWorkSetAlgorithm(BDD initialStates, BDD forbiddenStates) {
        
        resetCoordinator();
        
        BDD currentReachableStatesBDD = initialStates;
        BDD previousReachableStatesBDD = null;
        
        while (!shouldStop()) {
            previousReachableStatesBDD = currentReachableStatesBDD.id();
            int choice = pickCompFwd();
            BDD currentTransitionRelation = getCompBDD(choice);
            currentReachableStatesBDD = restrictedImage(partitions.bddExAutomata, currentReachableStatesBDD, 
                                                                forbiddenStates, currentTransitionRelation);
            record_forward(!previousReachableStatesBDD.equals(currentReachableStatesBDD));
        }
        
        return currentReachableStatesBDD;
    }
    
    @Override
    public BDD backwardWorkSetAlgorithm(BDD markedStates) {
        
        if (partitions.bddExAutomata.isAllMarked()) {
            return partitions.manager.getOneBDD();
        }
        
        BDD noTargetForbiddenStates = partitions.bddExAutomata.manager.getZeroBDD();
        BDD allTargetReachableStates = partitions.bddExAutomata.manager.getOneBDD();
        
        BDD targetMarkedStates = markedStates.id()
                .replaceWith(partitions.bddExAutomata.sourceToDestLocationPairing)
                .replaceWith(partitions.bddExAutomata.sourceToDestVariablePairing);
        
        return internalReachableBackwardRestrictedWorkSetAlgorithm(targetMarkedStates, 
                noTargetForbiddenStates, allTargetReachableStates)
                .replaceWith(partitions.bddExAutomata.destToSourceLocationPairing)
                .replaceWith(partitions.bddExAutomata.destToSourceVariablePairing);
    }

    @Override
    public BDD reachableBackwardWorkSetAlgorithm(BDD markedStates, BDD reachableStates) {
        
        if (partitions.bddExAutomata.isAllMarked()) {
            return reachableStates;
        }
        
        BDD noTargetForbiddenStates = partitions.bddExAutomata.manager.getZeroBDD();
        BDD targetMarkedStates = markedStates.id()
                .replaceWith(partitions.bddExAutomata.sourceToDestLocationPairing)
                .replaceWith(partitions.bddExAutomata.sourceToDestVariablePairing);
        BDD targetReachableStates = reachableStates.id()
                .replaceWith(partitions.bddExAutomata.sourceToDestLocationPairing)
                .replaceWith(partitions.bddExAutomata.sourceToDestVariablePairing);
        return internalReachableBackwardRestrictedWorkSetAlgorithm(targetMarkedStates, 
                noTargetForbiddenStates, targetReachableStates)
                .replaceWith(partitions.bddExAutomata.destToSourceLocationPairing)
                .replaceWith(partitions.bddExAutomata.destToSourceVariablePairing);
    }

    @Override
    public BDD backwardRestrictedWorkSetAlgorithm(BDD markedStates, BDD forbiddenStates) {
        
        if(partitions.bddExAutomata.isAllMarked() && forbiddenStates.isZero()) {
            return partitions.manager.getOneBDD();
        }
        
        BDD targetMarkedStates = markedStates.id()
                .replaceWith(partitions.bddExAutomata.sourceToDestLocationPairing)
                .replaceWith(partitions.bddExAutomata.sourceToDestVariablePairing);
        BDD targetForbiddenStates = forbiddenStates.id()
                .replaceWith(partitions.bddExAutomata.sourceToDestLocationPairing)
                .replaceWith(partitions.bddExAutomata.sourceToDestVariablePairing);
        BDD targetReachableStates = partitions.manager.getOneBDD();
        
        BDD currentTargetCoreachableStatesBDD = targetMarkedStates;
        BDD previousTargetCoreachableStatesBDD = null;
        
        do {
            previousTargetCoreachableStatesBDD = currentTargetCoreachableStatesBDD.id();
            currentTargetCoreachableStatesBDD 
                = internalReachableBackwardRestrictedWorkSetAlgorithm(targetMarkedStates, 
                  targetForbiddenStates, targetReachableStates);
        } while (!previousTargetCoreachableStatesBDD.equals(currentTargetCoreachableStatesBDD));
        
        return currentTargetCoreachableStatesBDD
                .replaceWith(partitions.bddExAutomata.destToSourceLocationPairing)
                .replaceWith(partitions.bddExAutomata.destToSourceVariablePairing);
    }
    
    @Override
    public BDD reachableBackwardRestrictedWorkSetAlgorithm(BDD markedStates, BDD forbiddenStates, BDD reachableStates) {
        
        if(partitions.bddExAutomata.isAllMarked() && forbiddenStates.isZero())
            return reachableStates;
        
        BDD targetMarkedStates = markedStates.id()
                .replaceWith(partitions.bddExAutomata.sourceToDestLocationPairing)
                .replaceWith(partitions.bddExAutomata.sourceToDestVariablePairing);
        BDD targetForbiddenStates = forbiddenStates.id()
                .replaceWith(partitions.bddExAutomata.sourceToDestLocationPairing)
                .replaceWith(partitions.bddExAutomata.sourceToDestVariablePairing);
        BDD targetReachableStates = reachableStates.id()
                .replaceWith(partitions.bddExAutomata.sourceToDestLocationPairing)
                .replaceWith(partitions.bddExAutomata.sourceToDestVariablePairing);
        
        BDD currentTargetCoreachableStatesBDD = targetMarkedStates;
        BDD previousTargetCoreachableStatesBDD = null;
        
        do {
            previousTargetCoreachableStatesBDD = currentTargetCoreachableStatesBDD.id();
            currentTargetCoreachableStatesBDD 
                = internalReachableBackwardRestrictedWorkSetAlgorithm(targetMarkedStates, 
                  targetForbiddenStates, targetReachableStates);
        } while (!previousTargetCoreachableStatesBDD.equals(currentTargetCoreachableStatesBDD));
        
        return currentTargetCoreachableStatesBDD
                .replaceWith(partitions.bddExAutomata.destToSourceLocationPairing)
                .replaceWith(partitions.bddExAutomata.destToSourceVariablePairing);
    }
    
    private BDD internalReachableBackwardRestrictedWorkSetAlgorithm(BDD targetMarkedStates, BDD targetForbiddenStates, 
                                                                                            BDD targetReachableStates) {
        resetCoordinator();

        BDD currentTargetCoreachableStatesBDD = targetMarkedStates;
        BDD previousTargetCoreachableStatesBDD = null;

        while (!shouldStop()) {
            previousTargetCoreachableStatesBDD = currentTargetCoreachableStatesBDD.id();
            int choice = pickCompBwd();
            BDD currentTransitionRelation = getCompBDD(choice);
            currentTargetCoreachableStatesBDD = restrictedPreImage(partitions.bddExAutomata, 
                    currentTargetCoreachableStatesBDD, targetForbiddenStates, currentTransitionRelation);
            currentTargetCoreachableStatesBDD = currentTargetCoreachableStatesBDD.and(targetReachableStates);
            record_backward(!previousTargetCoreachableStatesBDD.equals(currentTargetCoreachableStatesBDD));
        }
        return currentTargetCoreachableStatesBDD;
    }
}
