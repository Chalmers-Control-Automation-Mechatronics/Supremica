package org.supremica.automata.BDD.EFA;

import gnu.trove.TIntHashSet;
import java.util.Random;
import net.sf.javabdd.BDD;
import static org.supremica.automata.BDD.EFA.BDDPartitionReachabilityUti.restrictedPreImage;

/**
 * Random selection of partitions. Mainly used for testing.
 * 
 * @author  Zhennan
 * @version 2.0
 */

public class BDDPartitionAlgoWorkerRan extends  BDDPartitionAlgoWorker {

    public BDDPartitionAlgoWorkerRan(BDDPartitionSet partitions, BDDPartitionCoordinator coordinator) {
        super(partitions, coordinator);
    }
    
    @Override
    public BDD forwardWorkSetAlgorithm(BDD initialStates) {
        System.err.println("Using random forward workset [for testing only]");
        BDD noForbiddenStates = partitions.manager.getZeroBDD();
        return forwardRestrictedWorkSetAlgorithm(initialStates, noForbiddenStates);
    }

    @Override
    public BDD backwardWorkSetAlgorithm(BDD markedStates) {
        System.err.println("Using random backward workset [for testing only]");
        BDD noForbiddenStates = partitions.manager.getZeroBDD();
        BDD allReachableStates = partitions.manager.getOneBDD();
        return reachableBackwardRestrictedWorkSetAlgorithm(markedStates, noForbiddenStates, allReachableStates);
    }

    @Override
    public BDD reachableBackwardWorkSetAlgorithm(BDD markedStates, BDD reachableStates) {
        System.err.println("Using random reachable backward workset [for testing only]");
        BDD noForbiddenStates = partitions.manager.getZeroBDD();
        return reachableBackwardRestrictedWorkSetAlgorithm(markedStates, noForbiddenStates, reachableStates);
    }

    @Override
    public BDD forwardRestrictedWorkSetAlgorithm(BDD initialStates, BDD forbiddenStates) {
        System.err.println("Using random forward restricted workset [for testing only]");
        BDD[] relationSet = new BDD[partitions.getCompIndexToCompBDDMap().size()];
        int[] eventIndexAsKeys = partitions.getCompIndexToCompBDDMap().keys();
        
        for (int i = 0; i < eventIndexAsKeys.length; i++) {
            int eventIndex = eventIndexAsKeys[i];
            relationSet[eventIndex] = partitions.getCompIndexToCompBDDMap().get(eventIndex);
        }

        BDD currentReachableStatesBDD = initialStates;

        BDD previousReachablestatesBDD = null;

        do { //comment this do while and run the pure forward reachability. It should get the right number of coreachable states.
            int workCount = relationSet.length;
            boolean[] activeFlags = new boolean[relationSet.length];
            for (int i = 0; i < activeFlags.length; i++) {
                activeFlags[i] = true;
            }
            previousReachablestatesBDD = currentReachableStatesBDD.id();
            currentReachableStatesBDD = internalForwardRestrictedSubroutine(relationSet, activeFlags, workCount, 
                                                                            currentReachableStatesBDD, forbiddenStates);
            
        } while (!previousReachablestatesBDD.equals(currentReachableStatesBDD));
        
        return currentReachableStatesBDD;
    }
    
    private BDD internalForwardRestrictedSubroutine (BDD[] relationSet, boolean[] activeFlags, int workCount, BDD reachableStates, BDD forbiddenStates) {
                
        BDD previousReachablestatesBDD = null;
        BDD currentReachableStatesBDD = reachableStates.id();
        
        Random random = new Random();
        while (workCount != 0) {
            previousReachablestatesBDD = currentReachableStatesBDD.id();
            int comIndex = 0;
            do {
                comIndex = random.nextInt(relationSet.length);
            } while (!activeFlags[comIndex]);
            BDD currentTransitionRelation = (BDD) relationSet[comIndex];
            activeFlags[comIndex] = false;
            workCount--;
            currentReachableStatesBDD = BDDPartitionReachabilityUti
                    .restrictedImage(partitions.bddExAutomata, currentReachableStatesBDD, forbiddenStates, currentTransitionRelation);

            if (!previousReachablestatesBDD.equals(currentReachableStatesBDD)) {
                TIntHashSet dependentEventIndexSet = partitions.getForwardDependentComponentMap().get(comIndex);
                int[] dependentEventIndexArray = dependentEventIndexSet.toArray();
                for (int j = 0; j < dependentEventIndexArray.length; j++) {
                    if (!activeFlags[dependentEventIndexArray[j]]) {
                        activeFlags[dependentEventIndexArray[j]] = true;
                        workCount++;
                    }
                }
            }
        }
        
        return currentReachableStatesBDD;
    }

    @Override
    public BDD backwardRestrictedWorkSetAlgorithm(BDD markedStates, BDD forbiddenStates) {
        System.err.println("Using random backward restricted workset [for testing only]");
        BDD allReachableStates = partitions.manager.getOneBDD();
        return reachableBackwardRestrictedWorkSetAlgorithm(markedStates, forbiddenStates, allReachableStates);
    }
    
    @Override
    public BDD reachableBackwardRestrictedWorkSetAlgorithm(BDD markedStates, BDD forbiddenStates, BDD reachableStates) {
        
        System.err.println("Using random reachable backward restricted workset [for testing only]");
        
        if (partitions.bddExAutomata.isAllMarked() && forbiddenStates.isZero()) {
            return reachableStates;
        }
        
        BDD[] relationSet = new BDD[partitions.getCompIndexToCompBDDMap().size()];
        int[] compIndexAsKeys = partitions.getCompIndexToCompBDDMap().keys();

        for (int i = 0; i < compIndexAsKeys.length; i++) {
            int compIndex = compIndexAsKeys[i];
            relationSet[compIndex] = partitions.getCompIndexToCompBDDMap().get(compIndex);
        }
     
        BDD targetMarkedStates = markedStates.id()
                .replaceWith(partitions.bddExAutomata.getSourceToDestLocationPairing())
                .replaceWith(partitions.bddExAutomata.getSourceToDestVariablePairing());
        
        BDD targetForbiddenStates = forbiddenStates.id()
                .replaceWith(partitions.bddExAutomata.getSourceToDestLocationPairing())
                .replaceWith(partitions.bddExAutomata.getSourceToDestVariablePairing());
        
        BDD targetReachableStates = reachableStates.id()
                .replaceWith(partitions.bddExAutomata.getSourceToDestLocationPairing())
                .replaceWith(partitions.bddExAutomata.getSourceToDestVariablePairing());

        BDD currentCoreachableTargetStatesBDD = targetMarkedStates;

        BDD previousCoreachableTargetStatesBDD = null;
        
        do { //comment this do while and run the pure backward reachability. It should get the right number of coreachable states.
            boolean[] activeFlags = new boolean[relationSet.length];
            for (int i = 0; i < activeFlags.length; i++) {
                activeFlags[i] = true;
            }

            int workCount = relationSet.length;
            previousCoreachableTargetStatesBDD = currentCoreachableTargetStatesBDD.id();
            currentCoreachableTargetStatesBDD = internalBackwardRestrictedSubroutine(relationSet, activeFlags, workCount, 
                                                targetReachableStates, currentCoreachableTargetStatesBDD, targetForbiddenStates);
        } while(!previousCoreachableTargetStatesBDD.equals(currentCoreachableTargetStatesBDD));

        return currentCoreachableTargetStatesBDD
                .replaceWith(partitions.bddExAutomata.getDestToSourceLocationPairing())
                .replaceWith(partitions.bddExAutomata.getDestToSourceVariablePairing());
    }
    
    private BDD internalBackwardRestrictedSubroutine (BDD[] relationSet, boolean[] activeFlags, int workCount, 
                                            BDD targetReachableStates, BDD targetCoreachableStates, BDD targetForbiddenStates) {
        
        BDD previousCoreachableTargetStatesBDD = null;
        BDD currentCoreachableTargetStatesBDD = targetCoreachableStates.id();
        
        Random random = new Random();
        while (workCount != 0) {
            previousCoreachableTargetStatesBDD = currentCoreachableTargetStatesBDD.id();
            int comIndex = 0;
            do {
                comIndex = random.nextInt(relationSet.length);
            } while (!activeFlags[comIndex]);
            BDD currentTransitionRelation = (BDD) relationSet[comIndex];
            activeFlags[comIndex] = false;
            workCount--;

            currentCoreachableTargetStatesBDD = restrictedPreImage(partitions.bddExAutomata,
                    currentCoreachableTargetStatesBDD, targetForbiddenStates, currentTransitionRelation);
            currentCoreachableTargetStatesBDD = currentCoreachableTargetStatesBDD.and(targetReachableStates);

            if (!previousCoreachableTargetStatesBDD.equals(currentCoreachableTargetStatesBDD)) {
                TIntHashSet dependentEventIndexSet = partitions.getBackwardDependentComponentMap().get(comIndex);
                int[] dependentEventIndexArray = dependentEventIndexSet.toArray();
                for (int j = 0; j < dependentEventIndexArray.length; j++) {
                    if (!activeFlags[dependentEventIndexArray[j]]) {
                        activeFlags[dependentEventIndexArray[j]] = true;
                        workCount++;
                    }
                }
            }
        }
        
        return currentCoreachableTargetStatesBDD;
    }
    
}
