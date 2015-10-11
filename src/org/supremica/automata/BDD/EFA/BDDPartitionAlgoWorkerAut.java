//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
package org.supremica.automata.BDD.EFA;

import net.sf.javabdd.BDD;


/**
 * The class implements these fix-point automaton-based algorithms for
 * synthesis and verification.
 *
 * @author zhennan
 */

public class BDDPartitionAlgoWorkerAut extends BDDPartitionAlgoWorker
{
  public BDDPartitionAlgoWorkerAut(final BDDPartitionSet partitions,
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
    return internalForwardRestrictedWorkSetAlgorithm(initialStates,
                                                     noForbiddenStates);
  }

  @Override
  public BDD forwardRestrictedWorkSetAlgorithm(final BDD initialStates,
                                               final BDD forbiddenStates)
  {

    BDD currentReachableStatesBDD = initialStates;
    BDD previousReachableStatesBDD = null;

    do {
      previousReachableStatesBDD = currentReachableStatesBDD.id();
      currentReachableStatesBDD =
        internalForwardRestrictedWorkSetAlgorithm(currentReachableStatesBDD,
                                                  forbiddenStates);
    } while (!previousReachableStatesBDD.equals(currentReachableStatesBDD));

    return currentReachableStatesBDD;
  }

  private BDD internalForwardRestrictedWorkSetAlgorithm(final BDD initialStates,
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

    return currentReachableStatesBDD;
  }

  @Override
  public BDD backwardWorkSetAlgorithm(final BDD markedStates)
  {

    if (partitions.bddExAutomata.isAllMarked()) {
      return partitions.manager.getOneBDD();
    }

    final BDD noTargetForbiddenStates =
      partitions.bddExAutomata.getManager().getZeroBDD();
    final BDD allTargetReachableStates =
      partitions.bddExAutomata.getManager().getOneBDD();

    final BDD targetMarkedStates = markedStates.id()
      .replaceWith(partitions.bddExAutomata.getSourceToDestLocationPairing())
      .replaceWith(partitions.bddExAutomata.getSourceToDestVariablePairing());

    return interReachBwdRestrictedWorkSetAlgm(targetMarkedStates,
                                              noTargetForbiddenStates,
                                              allTargetReachableStates)
      .replaceWith(partitions.bddExAutomata.getDestToSourceLocationPairing())
      .replaceWith(partitions.bddExAutomata.getDestToSourceVariablePairing());
  }

  @Override
  public BDD reachableBackwardWorkSetAlgorithm(final BDD markedStates,
                                               final BDD reachableStates)
  {

    if (partitions.bddExAutomata.isAllMarked()) {
      return reachableStates;
    }

    final BDD noTargetForbiddenStates =
      partitions.bddExAutomata.getManager().getZeroBDD();
    final BDD targetMarkedStates = markedStates.id()
      .replaceWith(partitions.bddExAutomata.getSourceToDestLocationPairing())
      .replaceWith(partitions.bddExAutomata.getSourceToDestVariablePairing());
    final BDD targetReachableStates = reachableStates.id()
      .replaceWith(partitions.bddExAutomata.getSourceToDestLocationPairing())
      .replaceWith(partitions.bddExAutomata.getSourceToDestVariablePairing());
    return interReachBwdRestrictedWorkSetAlgm(targetMarkedStates,
                                              noTargetForbiddenStates,
                                              targetReachableStates)
      .replaceWith(partitions.bddExAutomata.getDestToSourceLocationPairing())
      .replaceWith(partitions.bddExAutomata.getDestToSourceVariablePairing());
  }

  @Override
  public BDD backwardRestrictedWorkSetAlgorithm(final BDD markedStates,
                                                final BDD forbiddenStates)
  {

    if (partitions.bddExAutomata.isAllMarked() && forbiddenStates.isZero()) {
      return partitions.manager.getOneBDD();
    }

    final BDD targetMarkedStates = markedStates.id()
      .replaceWith(partitions.bddExAutomata.getSourceToDestLocationPairing())
      .replaceWith(partitions.bddExAutomata.getSourceToDestVariablePairing());
    final BDD targetForbiddenStates = forbiddenStates.id()
      .replaceWith(partitions.bddExAutomata.getSourceToDestLocationPairing())
      .replaceWith(partitions.bddExAutomata.getSourceToDestVariablePairing());
    final BDD targetReachableStates = partitions.manager.getOneBDD();

    BDD currentTargetCoreachableStatesBDD = targetMarkedStates;
    BDD previousTargetCoreachableStatesBDD = null;

    do {
      previousTargetCoreachableStatesBDD =
        currentTargetCoreachableStatesBDD.id();
      currentTargetCoreachableStatesBDD =
        interReachBwdRestrictedWorkSetAlgm(targetMarkedStates,
                                           targetForbiddenStates,
                                           targetReachableStates);
    } while (!previousTargetCoreachableStatesBDD
      .equals(currentTargetCoreachableStatesBDD));

    return currentTargetCoreachableStatesBDD
      .replaceWith(partitions.bddExAutomata.getDestToSourceLocationPairing())
      .replaceWith(partitions.bddExAutomata.getDestToSourceVariablePairing());
  }

  @Override
  public BDD reachableBackwardRestrictedWorkSetAlgorithm(final BDD markedStates,
                                                         final BDD forbiddenStates,
                                                         final BDD reachableStates)
  {

    if (partitions.bddExAutomata.isAllMarked() && forbiddenStates.isZero())
      return reachableStates;

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

    do {
      previousTargetCoreachableStatesBDD =
        currentTargetCoreachableStatesBDD.id();
      currentTargetCoreachableStatesBDD =
        interReachBwdRestrictedWorkSetAlgm(targetMarkedStates,
                                           targetForbiddenStates,
                                           targetReachableStates);
    } while (!previousTargetCoreachableStatesBDD
      .equals(currentTargetCoreachableStatesBDD));

    return currentTargetCoreachableStatesBDD
      .replaceWith(partitions.bddExAutomata.getDestToSourceLocationPairing())
      .replaceWith(partitions.bddExAutomata.getDestToSourceVariablePairing());
  }

  private BDD interReachBwdRestrictedWorkSetAlgm(final BDD targetMarkedStates,
                                                 final BDD targetForbiddenStates,
                                                 final BDD targetReachableStates)
  {
    resetCoordinator();

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

    return currentTargetCoreachableStatesBDD;
  }
}
