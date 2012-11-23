package org.supremica.automata.BDD.EFA;

import gnu.trove.TIntHashSet;
import gnu.trove.TIntObjectIterator;
import java.util.Random;
import net.sf.javabdd.BDD;
import static org.supremica.automata.BDD.EFA.BDDPartitionReachabilityUti.restrictedImage;
import static org.supremica.automata.BDD.EFA.BDDPartitionReachabilityUti.restrictedPreImage;

/**
 * The class implements these fix-point event-based algorithms for synthesis and verification.
 * 
 * @author  Zhennan
 * @version 2.0
 */

public class BDDPartitionAlgoWorkerEve extends BDDPartitionAlgoWorker {

    public BDDPartitionAlgoWorkerEve(BDDPartitionSet partitions, BDDPartitionCoordinator coordinator) {
        super(partitions, coordinator);
    }
    
    @Override
    public BDD forwardWorkSetAlgorithm(BDD initialStates) {
      
        BDD noForbiddenStates = partitions.bddExAutomata.getManager().getZeroBDD();
        return forwardRestrictedWorkSetAlgorithm(initialStates, noForbiddenStates);
    }
    
    @Override
    public BDD forwardRestrictedWorkSetAlgorithm(BDD initialStates, BDD forbiddenStates) {

        resetCoordinator();

        BDD currentReachableStatesBDD = initialStates;
        BDD previousReachableStatesBDD = null;
        
        while (!shouldStop()) {
            previousReachableStatesBDD = currentReachableStatesBDD.id();
            int choice = pickCompFwd();
            System.err.println(partitions.theIndexMap.getEventAt(choice));
            BDD currentTransitionRelation = getCompBDD(choice);
            currentReachableStatesBDD = restrictedImage(partitions.bddExAutomata, currentReachableStatesBDD,
                    forbiddenStates, currentTransitionRelation);
            record_forward(!previousReachableStatesBDD.equals(currentReachableStatesBDD));
        }
        
        // Temporary solution. 
        if (!forbiddenStates.isZero()) {
            BDD forbiddenTargetStates = forbiddenStates.id()
                                        .replaceWith(partitions.bddExAutomata.getSourceToDestLocationPairing())
                                        .replaceWith(partitions.bddExAutomata.getSourceToDestVariablePairing());
            boolean[] activeFlags = new boolean[partitions.getCompIndexToCompBDDMap().size()];
            int partitionCount = 0;
            for (TIntObjectIterator<BDD> itr = partitions.getCompIndexToCompBDDMap().iterator(); itr.hasNext();) {
                itr.advance();
                if (!itr.value().and(forbiddenTargetStates).isZero()) {
                    activeFlags[itr.key()] = true;
                    partitionCount++;
                }
            }
            do {
                boolean[] cpActiveFlags = activeFlags.clone();
                int cpCount = partitionCount;
                previousReachableStatesBDD = currentReachableStatesBDD.id();
                BDD tmpBDD = null;
                Random r = new Random();
                while (cpCount != 0) {
                    tmpBDD = currentReachableStatesBDD.id();
                    int comIndex = 0;
                    do {
                        comIndex = r.nextInt(cpActiveFlags.length);
                    } while (!cpActiveFlags[comIndex]);
                    BDD currentTansitionBDD = partitions.getCompIndexToCompBDDMap().get(comIndex);
                    cpActiveFlags[comIndex] = false;
                    cpCount --;
                    currentReachableStatesBDD = BDDPartitionReachabilityUti.restrictedImage(partitions.bddExAutomata, currentReachableStatesBDD, 
                            forbiddenStates, currentTansitionBDD);
                    if (!tmpBDD.equals(currentReachableStatesBDD)) {
                        TIntHashSet dependentEventIndexSet = partitions.getForwardDependentComponentMap().get(comIndex);
                        int[] dependentEventIndexArray = dependentEventIndexSet.toArray();
                        for (int j = 0; j < dependentEventIndexArray.length; j++) {
                            if (!cpActiveFlags[dependentEventIndexArray[j]]) {
                                cpActiveFlags[dependentEventIndexArray[j]] = true;
                                cpCount ++;
                            }
                        }
                    }
                }
            } while (!previousReachableStatesBDD.equals(currentReachableStatesBDD));          
        }
        
        return currentReachableStatesBDD;
    }
    
    @Override
    public BDD backwardWorkSetAlgorithm(BDD markedStates) {
        
        BDD noForbiddenStates = partitions.bddExAutomata.getManager().getZeroBDD();
        BDD allReachableStates = partitions.bddExAutomata.getManager().getOneBDD();
        return reachableBackwardRestrictedWorkSetAlgorithm(markedStates, noForbiddenStates, allReachableStates);
       
    }

    @Override
    public BDD reachableBackwardWorkSetAlgorithm(BDD markedStates, BDD reachableStates) {
        
        BDD noForbiddenStates = partitions.bddExAutomata.getManager().getZeroBDD();
        return reachableBackwardRestrictedWorkSetAlgorithm(markedStates, noForbiddenStates, reachableStates);
    }

    @Override
    public BDD backwardRestrictedWorkSetAlgorithm(BDD markedStates, BDD forbiddenStates) {
        
        BDD reachableStates = partitions.manager.getOneBDD();
        return reachableBackwardRestrictedWorkSetAlgorithm(markedStates, forbiddenStates, reachableStates);
        
    }
    
    // This function is buggy.
    @Override
    public BDD reachableBackwardRestrictedWorkSetAlgorithm(BDD markedStates, BDD forbiddenStates, BDD reachableStates) {

        if (partitions.bddExAutomata.isAllMarked() && forbiddenStates.isZero()) {
            return reachableStates;
        }

        resetCoordinator();

        BDD targetMarkedStates = markedStates.id()
                .replaceWith(partitions.bddExAutomata.getSourceToDestLocationPairing())
                .replaceWith(partitions.bddExAutomata.getSourceToDestVariablePairing());
        BDD targetForbiddenStates = forbiddenStates.id()
                .replaceWith(partitions.bddExAutomata.getSourceToDestLocationPairing())
                .replaceWith(partitions.bddExAutomata.getSourceToDestVariablePairing());
        BDD targetReachableStates = reachableStates.id()
                .replaceWith(partitions.bddExAutomata.getSourceToDestLocationPairing())
                .replaceWith(partitions.bddExAutomata.getSourceToDestVariablePairing());

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
        
        // temporary solution
        if (!targetForbiddenStates.isZero()) {
            boolean[] activeFlags = new boolean[partitions.getCompIndexToCompBDDMap().size()];
            int partitionCount = 0;
            for (TIntObjectIterator<BDD> itr = partitions.getCompIndexToCompBDDMap().iterator(); itr.hasNext();) {
                itr.advance();
                if (!itr.value().and(forbiddenStates).isZero()) {
                    activeFlags[itr.key()] = true;
                    partitionCount++;
                }
            }
            do {
                boolean[] cpActiveFlags = activeFlags.clone();
                int cpCount = partitionCount;
                previousTargetCoreachableStatesBDD = currentTargetCoreachableStatesBDD.id();
                BDD tmpBDD = null;
                Random r = new Random();
                while (cpCount != 0) {
                    tmpBDD = currentTargetCoreachableStatesBDD.id();
                    int comIndex = 0;
                    do {
                        comIndex = r.nextInt(cpActiveFlags.length);
                    } while (!cpActiveFlags[comIndex]);
                    BDD currentTansitionBDD = partitions.getCompIndexToCompBDDMap().get(comIndex);
                    cpActiveFlags[comIndex] = false;
                    cpCount --;
                    currentTargetCoreachableStatesBDD = restrictedPreImage(partitions.bddExAutomata,
                    currentTargetCoreachableStatesBDD, targetForbiddenStates, currentTansitionBDD).and(targetReachableStates);
                    if (!tmpBDD.equals(currentTargetCoreachableStatesBDD)) {
                        TIntHashSet dependentEventIndexSet = partitions.getBackwardDependentComponentMap().get(comIndex);
                        int[] dependentEventIndexArray = dependentEventIndexSet.toArray();
                        for (int j = 0; j < dependentEventIndexArray.length; j++) {
                            if (!cpActiveFlags[dependentEventIndexArray[j]]) {
                                cpActiveFlags[dependentEventIndexArray[j]] = true;
                                cpCount ++;
                            }
                        }
                    }
                }
            } while (!previousTargetCoreachableStatesBDD.equals(currentTargetCoreachableStatesBDD));          
        }

        return currentTargetCoreachableStatesBDD
                .replaceWith(partitions.bddExAutomata.getDestToSourceLocationPairing())
                .replaceWith(partitions.bddExAutomata.getDestToSourceVariablePairing());
    }
}