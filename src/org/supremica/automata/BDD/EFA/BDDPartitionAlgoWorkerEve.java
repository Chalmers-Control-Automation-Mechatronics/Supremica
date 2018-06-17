//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
package org.supremica.automata.BDD.EFA;

import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.set.hash.TIntHashSet;

import net.sf.javabdd.BDD;


/**
 * The class implements these fix-point event-based algorithms for synthesis
 * and verification.
 *
 * @author zhennan
 */

public class BDDPartitionAlgoWorkerEve extends BDDPartitionAlgoWorker
{

  public BDDPartitionAlgoWorkerEve(final BDDPartitionSet partitions,
                                   final BDDPartitionCoordinator coordinator,
                                   final BDDPartitionImageOperator imageOperator)
  {
    super(partitions, coordinator, imageOperator);
  }

  @Override
  public BDD forwardWorkSetAlgorithm(final BDD initialStates)
  {

    final BDD noForbiddenStates =
      partitions.bddExAutomata.getManager().getZeroBDD();

    return forwardRestrictedWorkSetAlgorithm(initialStates,
                                             noForbiddenStates);
  }

  @Override
  public BDD forwardRestrictedWorkSetAlgorithm(final BDD initialStates,
                                               final BDD forbiddenStates)
  {

    resetCoordinator();

    BDD currentReachableStatesBDD = initialStates;
    BDD previousReachableStatesBDD = null;

    while (!shouldStop()) {
      previousReachableStatesBDD = currentReachableStatesBDD.id();
      final int choice = pickCompFwd();
      final BDD currentTransitionRelation = getCompBDD(choice);
      currentReachableStatesBDD = imageOperator
        .restrictedImage(partitions.bddExAutomata, currentReachableStatesBDD,
                         forbiddenStates, currentTransitionRelation);
      record_forward(!previousReachableStatesBDD
        .equals(currentReachableStatesBDD));
    }

    // Temporary solution.
    if (!forbiddenStates.isZero()) {
      final BDD forbiddenTargetStates = forbiddenStates.id()
        .replaceWith(partitions.bddExAutomata
          .getSourceToDestLocationPairing())
        .replaceWith(partitions.bddExAutomata
          .getSourceToDestVariablePairing());
      final boolean[] activeFlags =
        new boolean[partitions.getCompIndexToCompBDDMap().size()];
      int partitionCount = 0;
      for (final TIntObjectIterator<BDD> itr =
        partitions.getCompIndexToCompBDDMap().iterator(); itr.hasNext();) {
        itr.advance();
        if (!itr.value().and(forbiddenTargetStates).isZero()) {
          activeFlags[itr.key()] = true;
          partitionCount++;
        }
      }
      do {
        final boolean[] cpActiveFlags = activeFlags.clone();
        int cpCount = partitionCount;
        previousReachableStatesBDD = currentReachableStatesBDD.id();
        BDD tmpBDD = null;
        while (cpCount != 0) {
          tmpBDD = currentReachableStatesBDD.id();
          int comIndex = 0;
          while (!cpActiveFlags[comIndex] && comIndex < cpActiveFlags.length) {
              comIndex++;
          }
          assert cpActiveFlags[comIndex];
          final BDD currentTansitionBDD =
            partitions.getCompIndexToCompBDDMap().get(comIndex);
          cpActiveFlags[comIndex] = false;
          cpCount--;
          currentReachableStatesBDD = imageOperator
            .restrictedImage(partitions.bddExAutomata,
                             currentReachableStatesBDD, forbiddenStates,
                             currentTansitionBDD);
          if (!tmpBDD.equals(currentReachableStatesBDD)) {
            final TIntHashSet dependentEventIndexSet =
              partitions.getForwardDependentComponentMap().get(comIndex);
            final int[] dependentEventIndexArray =
              dependentEventIndexSet.toArray();
            for (int j = 0; j < dependentEventIndexArray.length; j++) {
              if (!cpActiveFlags[dependentEventIndexArray[j]]) {
                cpActiveFlags[dependentEventIndexArray[j]] = true;
                cpCount++;
              }
            }
          }
        }
      } while (!previousReachableStatesBDD.equals(currentReachableStatesBDD));
    }

    return currentReachableStatesBDD;
  }

  @Override
  public BDD backwardWorkSetAlgorithm(final BDD markedStates)
  {

    final BDD noForbiddenStates =
      partitions.bddExAutomata.getManager().getZeroBDD();
    final BDD allReachableStates =
      partitions.bddExAutomata.getManager().getOneBDD();
    return reachableBackwardRestrictedWorkSetAlgorithm(markedStates,
                                                       noForbiddenStates,
                                                       allReachableStates);

  }

  @Override
  public BDD reachableBackwardWorkSetAlgorithm(final BDD markedStates,
                                               final BDD reachableStates)
  {

    final BDD noForbiddenStates =
      partitions.bddExAutomata.getManager().getZeroBDD();
    return reachableBackwardRestrictedWorkSetAlgorithm(markedStates,
                                                       noForbiddenStates,
                                                       reachableStates);
  }

  @Override
  public BDD backwardRestrictedWorkSetAlgorithm(final BDD markedStates,
                                                final BDD forbiddenStates)
  {

    final BDD reachableStates = partitions.manager.getOneBDD();
    return reachableBackwardRestrictedWorkSetAlgorithm(markedStates,
                                                       forbiddenStates,
                                                       reachableStates);

  }

  @Override
  public BDD reachableBackwardRestrictedWorkSetAlgorithm(final BDD markedStates,
                                                         final BDD forbiddenStates,
                                                         final BDD reachableStates)
  {

    if (partitions.bddExAutomata.isAllMarked() && forbiddenStates.isZero()) {
      return reachableStates;
    }

    resetCoordinator();

    final BDD targetMarkedStates = markedStates.id()
      .replaceWith(partitions.bddExAutomata.getSourceToDestLocationPairing())
      .replaceWith(partitions.bddExAutomata.getSourceToDestVariablePairing());
    final BDD targetForbiddenStates = forbiddenStates.id()
      .replaceWith(partitions.bddExAutomata.getSourceToDestLocationPairing())
      .replaceWith(partitions.bddExAutomata.getSourceToDestVariablePairing());
    final BDD targetReachableStates = reachableStates.id()
      .replaceWith(partitions.bddExAutomata.getSourceToDestLocationPairing())
      .replaceWith(partitions.bddExAutomata.getSourceToDestVariablePairing());

    BDD currentTargetCoreachableStatesBDD = targetMarkedStates;
    BDD previousTargetCoreachableStatesBDD = null;

    while (!shouldStop()) {
      previousTargetCoreachableStatesBDD =
        currentTargetCoreachableStatesBDD.id();
      final int choice = pickCompBwd();
      final BDD currentTransitionRelation = getCompBDD(choice);
      currentTargetCoreachableStatesBDD = imageOperator
        .restrictedPreImage(partitions.bddExAutomata,
                            currentTargetCoreachableStatesBDD,
                            targetForbiddenStates, currentTransitionRelation);
      currentTargetCoreachableStatesBDD =
        currentTargetCoreachableStatesBDD.and(targetReachableStates);
      record_backward(!previousTargetCoreachableStatesBDD
        .equals(currentTargetCoreachableStatesBDD));
    }

    // temporary solution
    if (!targetForbiddenStates.isZero()) {
      final boolean[] activeFlags =
        new boolean[partitions.getCompIndexToCompBDDMap().size()];
      int partitionCount = 0;
      for (final TIntObjectIterator<BDD> itr =
        partitions.getCompIndexToCompBDDMap().iterator(); itr.hasNext();) {
        itr.advance();
        if (!itr.value().and(forbiddenStates).isZero()) {
          activeFlags[itr.key()] = true;
          partitionCount++;
        }
      }
      do {
        final boolean[] cpActiveFlags = activeFlags.clone();
        int cpCount = partitionCount;
        previousTargetCoreachableStatesBDD =
          currentTargetCoreachableStatesBDD.id();
        BDD tmpBDD = null;
        while (cpCount != 0) {
          tmpBDD = currentTargetCoreachableStatesBDD.id();
          int comIndex = 0;
          while(!cpActiveFlags[comIndex] && comIndex < cpActiveFlags.length) {
            comIndex++;
          }
          assert cpActiveFlags[comIndex];
          final BDD currentTansitionBDD =
            partitions.getCompIndexToCompBDDMap().get(comIndex);
          cpActiveFlags[comIndex] = false;
          cpCount--;
          currentTargetCoreachableStatesBDD = imageOperator
            .restrictedPreImage(partitions.bddExAutomata,
                                currentTargetCoreachableStatesBDD,
                                targetForbiddenStates, currentTansitionBDD)
            .and(targetReachableStates);
          if (!tmpBDD.equals(currentTargetCoreachableStatesBDD)) {
            final TIntHashSet dependentEventIndexSet =
              partitions.getBackwardDependentComponentMap().get(comIndex);
            final int[] dependentEventIndexArray =
              dependentEventIndexSet.toArray();
            for (int j = 0; j < dependentEventIndexArray.length; j++) {
              if (!cpActiveFlags[dependentEventIndexArray[j]]) {
                cpActiveFlags[dependentEventIndexArray[j]] = true;
                cpCount++;
              }
            }
          }
        }
      } while (!previousTargetCoreachableStatesBDD
        .equals(currentTargetCoreachableStatesBDD));
    }

    return currentTargetCoreachableStatesBDD
      .replaceWith(partitions.bddExAutomata.getDestToSourceLocationPairing())
      .replaceWith(partitions.bddExAutomata.getDestToSourceVariablePairing());
  }
}
