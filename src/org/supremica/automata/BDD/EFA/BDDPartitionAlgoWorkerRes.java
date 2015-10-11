//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
package org.supremica.automata.BDD.EFA;

import java.util.Random;

import net.sf.javabdd.BDD;

import gnu.trove.set.hash.TIntHashSet;


/**
 * Random selection of partitions. Used for RAS Unsafe States Computation.
 *
 * @author zhennan
 */

public class BDDPartitionAlgoWorkerRes extends BDDPartitionAlgoWorker
{

  public BDDPartitionAlgoWorkerRes(final BDDPartitionSet partitions,
                                   final BDDPartitionCoordinator coordinator,
                                   final BDDPartitionImageOperator imageOperator)
  {
    super(partitions, coordinator, imageOperator);
  }

  @Override
  public BDD forwardWorkSetAlgorithm(final BDD initialStates)
  {
    System.err.println("Using random forward workset [for testing only]");
    final BDD noForbiddenStates = partitions.manager.getZeroBDD();
    return forwardRestrictedWorkSetAlgorithm(initialStates,
                                             noForbiddenStates);
  }

  @Override
  public BDD backwardWorkSetAlgorithm(final BDD markedStates)
  {
    System.err.println("Using random backward workset [for testing only]");
    final BDD noForbiddenStates = partitions.manager.getZeroBDD();
    final BDD allReachableStates = partitions.manager.getOneBDD();
    return reachableBackwardRestrictedWorkSetAlgorithm(markedStates,
                                                       noForbiddenStates,
                                                       allReachableStates);
  }

  @Override
  public BDD reachableBackwardWorkSetAlgorithm(final BDD markedStates,
                                               final BDD reachableStates)
  {
    System.err
      .println("Using random reachable backward workset [for testing only]");
    final BDD noForbiddenStates = partitions.manager.getZeroBDD();
    return reachableBackwardRestrictedWorkSetAlgorithm(markedStates,
                                                       noForbiddenStates,
                                                       reachableStates);
  }

  @Override
  public BDD forwardRestrictedWorkSetAlgorithm(final BDD initialStates,
                                               final BDD forbiddenStates)
  {
    System.err
      .println("Using random forward restricted workset [for testing only]");
    final BDD[] relationSet =
      new BDD[partitions.getCompIndexToCompBDDMap().size()];
    final int[] eventIndexAsKeys =
      partitions.getCompIndexToCompBDDMap().keys();

    for (int i = 0; i < eventIndexAsKeys.length; i++) {
      final int eventIndex = eventIndexAsKeys[i];
      relationSet[eventIndex] =
        partitions.getCompIndexToCompBDDMap().get(eventIndex);
    }

    BDD currentReachableStatesBDD = initialStates;

    BDD previousReachablestatesBDD = null;

    do { //comment this do while and run the pure forward reachability. It should get the right number of coreachable states.
      final int workCount = relationSet.length;
      final boolean[] activeFlags = new boolean[relationSet.length];
      for (int i = 0; i < activeFlags.length; i++) {
        activeFlags[i] = true;
      }
      previousReachablestatesBDD = currentReachableStatesBDD.id();
      currentReachableStatesBDD =
        internalForwardRestrictedSubroutine(relationSet, activeFlags,
                                            workCount,
                                            currentReachableStatesBDD,
                                            forbiddenStates);

    } while (!previousReachablestatesBDD.equals(currentReachableStatesBDD));

    return currentReachableStatesBDD;
  }

  private BDD internalForwardRestrictedSubroutine(final BDD[] relationSet,
                                                  final boolean[] activeFlags,
                                                  int workCount,
                                                  final BDD reachableStates,
                                                  final BDD forbiddenStates)
  {

    BDD previousReachablestatesBDD = null;
    BDD currentReachableStatesBDD = reachableStates.id();

    final Random random = new Random();
    while (workCount != 0) {
      previousReachablestatesBDD = currentReachableStatesBDD.id();
      int comIndex = 0;
      do {
        comIndex = random.nextInt(relationSet.length);
      } while (!activeFlags[comIndex]);
      final BDD currentTransitionRelation = relationSet[comIndex];
      activeFlags[comIndex] = false;
      workCount--;
      currentReachableStatesBDD = BDDPartitionReachabilityUti
        .restrictedImage(partitions.bddExAutomata, currentReachableStatesBDD,
                         forbiddenStates, currentTransitionRelation);

      if (!previousReachablestatesBDD.equals(currentReachableStatesBDD)) {
        final TIntHashSet dependentEventIndexSet =
          partitions.getForwardDependentComponentMap().get(comIndex);
        final int[] dependentEventIndexArray =
          dependentEventIndexSet.toArray();
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
  public BDD backwardRestrictedWorkSetAlgorithm(final BDD markedStates,
                                                final BDD forbiddenStates)
  {
    System.err
      .println("Using random backward restricted workset [for testing only]");
    final BDD allReachableStates = partitions.manager.getOneBDD();
    return reachableBackwardRestrictedWorkSetAlgorithm(markedStates,
                                                       forbiddenStates,
                                                       allReachableStates);
  }

  @Override
  public BDD reachableBackwardRestrictedWorkSetAlgorithm(final BDD markedStates,
                                                         final BDD forbiddenStates,
                                                         final BDD reachableStates)
  {

    System.err
      .println("Using random reachable backward restricted workset [for testing only]");

    if (partitions.bddExAutomata.isAllMarked() && forbiddenStates.isZero()) {
      return reachableStates;
    }

    final BDD[] relationSet =
      new BDD[partitions.getCompIndexToCompBDDMap().size()];
    final int[] compIndexAsKeys =
      partitions.getCompIndexToCompBDDMap().keys();

    for (int i = 0; i < compIndexAsKeys.length; i++) {
      final int compIndex = compIndexAsKeys[i];
      relationSet[compIndex] =
        partitions.getCompIndexToCompBDDMap().get(compIndex);
    }

    final BDD targetMarkedStates = markedStates.id()
      .replaceWith(partitions.bddExAutomata.getSourceToDestLocationPairing())
      .replaceWith(partitions.bddExAutomata.getSourceToDestVariablePairing());

    final BDD targetForbiddenStates = forbiddenStates.id()
      .replaceWith(partitions.bddExAutomata.getSourceToDestLocationPairing())
      .replaceWith(partitions.bddExAutomata.getSourceToDestVariablePairing());

    final BDD targetReachableStates = reachableStates.id()
      .replaceWith(partitions.bddExAutomata.getSourceToDestLocationPairing())
      .replaceWith(partitions.bddExAutomata.getSourceToDestVariablePairing());

    BDD currentCoreachableTargetStatesBDD = targetMarkedStates;

    BDD previousCoreachableTargetStatesBDD = null;

    do {
      final boolean[] activeFlags = new boolean[relationSet.length];
      for (int i = 0; i < activeFlags.length; i++) {
        activeFlags[i] = true;
      }

      final int workCount = relationSet.length;
      previousCoreachableTargetStatesBDD =
        currentCoreachableTargetStatesBDD.id();
      currentCoreachableTargetStatesBDD =
        internalBackwardRestrictedSubroutine(relationSet, activeFlags,
                                             workCount, targetReachableStates,
                                             currentCoreachableTargetStatesBDD,
                                             targetForbiddenStates);
    } while (!previousCoreachableTargetStatesBDD
      .equals(currentCoreachableTargetStatesBDD));

    return currentCoreachableTargetStatesBDD
      .replaceWith(partitions.bddExAutomata.getDestToSourceLocationPairing())
      .replaceWith(partitions.bddExAutomata.getDestToSourceVariablePairing());
  }

  private BDD internalBackwardRestrictedSubroutine(final BDD[] relationSet,
                                                   final boolean[] activeFlags,
                                                   int workCount,
                                                   final BDD targetReachableStates,
                                                   final BDD targetCoreachableStates,
                                                   final BDD targetForbiddenStates)
  {

    BDD previousCoreachableTargetStatesBDD = null;
    BDD currentCoreachableTargetStatesBDD = targetCoreachableStates.id();

    final Random random = new Random();
    while (workCount != 0) {
      previousCoreachableTargetStatesBDD =
        currentCoreachableTargetStatesBDD.id();
      int comIndex = 0;
      do {
        comIndex = random.nextInt(relationSet.length);
      } while (!activeFlags[comIndex]);
      final BDD currentTransitionRelation = relationSet[comIndex];
      activeFlags[comIndex] = false;
      workCount--;

      currentCoreachableTargetStatesBDD = imageOperator
        .restrictedPreImage(partitions.bddExAutomata,
                            currentCoreachableTargetStatesBDD,
                            targetForbiddenStates, currentTransitionRelation);
      currentCoreachableTargetStatesBDD =
        currentCoreachableTargetStatesBDD.and(targetReachableStates);

      if (!previousCoreachableTargetStatesBDD
        .equals(currentCoreachableTargetStatesBDD)) {
        final TIntHashSet dependentEventIndexSet =
          partitions.getBackwardDependentComponentMap().get(comIndex);
        final int[] dependentEventIndexArray =
          dependentEventIndexSet.toArray();
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