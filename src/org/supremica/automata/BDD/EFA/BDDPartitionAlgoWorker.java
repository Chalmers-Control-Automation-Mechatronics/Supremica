//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
package org.supremica.automata.BDD.EFA;

import net.sf.javabdd.BDD;


/**
 * Interface for the set of algorithms used to carry out the verification
 * and synthesis for the partitioning technique. Each BDDPartitioningSet has a
 * reference to one of the subclasses of this class to perform its own
 * fix-point computation.
 *
 * @author zhennan
 */

public abstract class BDDPartitionAlgoWorker
{

  /**
   * The reference to partitions to get the BDDs.
   */
  protected BDDPartitionSet partitions;

  /**
   * The reference to coordinator.
   */
  protected BDDPartitionCoordinator coordinator;

  /**
   * The reference to imageOperator
   */
  protected BDDPartitionImageOperator imageOperator;

  /**
   * Constructs a BDDPartitionAlgoWorker.
   *
   * @param partitions
   *          the set of partial partitions
   * @param coordinator
   *          the coordinator for choosing these partitions
   * @param imageOperator
   *          image and preimage operator
   */
  public BDDPartitionAlgoWorker(final BDDPartitionSet partitions,
                                final BDDPartitionCoordinator coordinator,
                                final BDDPartitionImageOperator imageOperator)
  {
    this.partitions = partitions;
    this.coordinator = coordinator;
    this.imageOperator = imageOperator;
  }

  /* Non abstract methods */
  /**
   * Returns a Boolean value indicating whether the algorithm should stop.
   *
   * @return {@code true} if the included coordinator does not have any active
   *         component {@code false} if the coordinator has some active
   *         components
   */
  public boolean shouldStop()
  {
    return coordinator.empty();
  }

  /**
   * Returns a component index for the forward reachability computation.
   *
   * @return the index of a component
   */
  public int pickCompFwd()
  {
    return coordinator.pickOne(true);
  }

  /**
   * Returns a component index for the backward reachability computation
   *
   * @return the index of a component
   */
  public int pickCompBwd()
  {
    return coordinator.pickOne(false);
  }

  /**
   * Returns the BDD for the component.
   *
   * @param compIndex
   * @return the partitioning BDD corresponding to the component index
   */
  public BDD getCompBDD(final int compIndex)
  {
    return partitions.getCompIndexToCompBDDMap().get(compIndex);
  }

  public void resetCoordinator()
  {
    coordinator.reset();
  }

  public BDDPartitionSet getPartitions()
  {
    return partitions;
  }

  /**
   * Records changes based on the chosen component for the forward search.
   *
   * @param changed
   *          whether the chosen component changed the temporary BDD
   */
  public void record_forward(final boolean changed)
  {
    coordinator.advance(true, changed);
  }

  /**
   * Records changes based on the chosen component for the backward search.
   *
   * @param changed
   *          whether the chosen component changed the temporary BDD
   */
  public void record_backward(final boolean changed)
  {
    coordinator.advance(false, changed);
  }

  /**
   * Given the initial states, the method returns a set of states that can be
   * reached from the initial state.
   *
   * @param initialStates
   *          initial states, for deterministic automata, there is only one
   *          initial state
   * @return a set of states which can be reached from initialStates
   */
  public abstract BDD forwardWorkSetAlgorithm(BDD initialStates);

  /**
   * The method returns a set of states which are reached from initial states
   * but not forbidden.
   *
   * @param initialStates
   *          initial states, for deterministic automata, there is only one
   *          initial state
   * @param forbiddenStates
   *          a set of states which must be forbidden
   * @return states that are reached from initial states but not forbidden
   */
  public abstract BDD forwardRestrictedWorkSetAlgorithm(BDD initialStates,
                                                        BDD forbiddenStates);

  /**
   * Given a set of marked states, the method returns a set of states which
   * can be co-reached from any marked state.
   *
   * @param markedStates
   *          a set of marked states
   * @return states which can be co-reached from any of the marked states
   */
  public abstract BDD backwardWorkSetAlgorithm(BDD markedStates);

  /**
   * Given a set of marked states and reachable states, the method returns the
   * subset of reachable states which are co-reached.
   *
   * @param markedStates
   *          a set of marked states
   * @param reachableStates
   *          all reachable states
   * @return a subset of reachable states which are also co-reachable
   */
  public abstract BDD reachableBackwardWorkSetAlgorithm(BDD markedStates,
                                                        BDD reachableStates);

  /**
   * The method returns a set of states co-reached from the marked states.
   *
   * @param markedStates
   *          a set of marked states
   * @param forbiddenStates
   *          a set of states which must be forbidden
   * @return states co-reached from the marked states but not forbidden
   */
  public abstract BDD backwardRestrictedWorkSetAlgorithm(BDD markedStates,
                                                         BDD forbiddenStates);

  /**
   * The method returns a set of reachable states co-reached from the marked
   * states.
   *
   * @param markedStates
   *          a set of marked states
   * @param forbiddenStates
   *          a set of states which must be forbidden
   * @param reachableStates
   *          a set of reachable states
   * @return states co-reached from the marked states but not forbidden
   */
  public abstract BDD reachableBackwardRestrictedWorkSetAlgorithm(BDD markedStates,
                                                                  BDD forbiddenStates,
                                                                  BDD reachableStates);

  /**
   * Given a set of forbidden states, which are either uncontrollable states
   * or explicitly forbidden states, the method does the backward reachability
   * through any uncontrollable event to find more uncontrollable states.
   *
   * @param forbiddenStates
   *          a set of forbidden states
   * @return a set of forbidden states which is a superset of forbiddenStates
   */
  public BDD uncontrollableBackwardWorkSetAlgorithm(final BDD forbiddenStates)
  {

    final BDD uncontrollableTransitionRelationBDD =
      partitions.getUncontrollableTransitionRelationBDD();

    final BDD forbiddenStatesAsTargetStates = forbiddenStates.id()
      .replaceWith(partitions.bddExAutomata.getSourceToDestLocationPairing())
      .replaceWith(partitions.bddExAutomata.getSourceToDestVariablePairing());

    final BDD results = imageOperator.preImage(partitions.bddExAutomata,
                                         forbiddenStatesAsTargetStates,
                                         uncontrollableTransitionRelationBDD)
      .replaceWith(partitions.bddExAutomata.getDestToSourceLocationPairing())
      .replaceWith(partitions.bddExAutomata.getDestToSourceVariablePairing());

    return results;
  }

  /* Getters and setters */
  public void setTrackPeakBDD()
  {
    imageOperator.setTrackPeak();
  }

  public int getMaxNbrNodes()
  {
    return imageOperator.getMaxNbrNodes();
  }
}
