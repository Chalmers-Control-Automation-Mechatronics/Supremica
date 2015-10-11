//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
package org.supremica.automata.BDD.EFA;

import net.sf.javabdd.BDD;


/**
 * Image and PreImage operations
 *
 * @author zhennan
 *
 *         <p>
 *         All the transition BDDs fed into each utility are forward. For
 *         preImage and restrictedPreImage, exchange of BDD domains should be
 *         performed. The returned values from image and restrictedImage
 *         methods are <em>source states</em>, while the return values from
 *         preImage and restrictedPreImage methods are <em>target states</em>.
 */
public class BDDPartitionImageOperator
{
  private boolean trackPeakBDD = false;
  private int maxNbrNodes = 0;

  public void setTrackPeak()
  {
    trackPeakBDD = true;
  }

  public int getMaxNbrNodes()
  {
    return maxNbrNodes;
  }

  /**
   * Returns a set of reachable states.
   * <p>
   * The initial and returned reachableStatesBDD are "source states".
   *
   * @param bddAutomata
   *          the BDDExtendedAutomata object
   * @param reachableStatesBDD
   *          a set of reachable states
   * @param curTransitionRelation
   *          a partition
   * @return more states from <code>reachableStatesBDD</code> through
   *         <code>curTransitionRelation</code>
   */
  public BDD image(final BDDExtendedAutomata bddAutomata,
                   final BDD reachableStatesBDD,
                   final BDD curTransitionRelation)
  {
    final BDD noForbiddenStates = bddAutomata.getManager().getZeroBDD();
    return restrictedImage(bddAutomata, reachableStatesBDD, noForbiddenStates,
                           curTransitionRelation);
  }

  /**
   * Returns a set of co-reachable states
   * <p>
   * The initial and returned coreachableStatesBDD are "target states".
   *
   * @param bddAutomata
   *          the BDDExtendedAutomata object
   * @param coreachableStatesBDD
   *          a set of co-reachable states
   * @param curTransitionRelation
   *          a partition
   * @return more states from <code>coreachableStatesBDD</code> through
   *         <code>curTransitionRelation</code>
   */
  public BDD preImage(final BDDExtendedAutomata bddAutomata,
                      final BDD coreachableStatesBDD,
                      final BDD curTransitionRelation)
  {
    final BDD noForbiddenStates = bddAutomata.getManager().getZeroBDD();
    return restrictedPreImage(bddAutomata, coreachableStatesBDD,
                              noForbiddenStates, curTransitionRelation);
  }

  /**
   * Returns a set of reachable states excluding a set of forbidden states.
   * <p>
   * The initial and returned reachableStatesBDD are "source states".
   *
   * @param bddAutomata
   *          the BDDExtendedAutomata object
   * @param reachableStatesBDD
   *          a set of reachable states
   * @param forbiddenStatesBDD
   *          a set of forbidden states
   * @param curTransitionRelation
   *          a partition
   * @return more states from <code>reachableStatesBDD</code> through
   *         <code>curTransitionRelation</code>
   */
  public BDD restrictedImage(final BDDExtendedAutomata bddAutomata,
                             BDD reachableStatesBDD,
                             final BDD forbiddenStatesBDD,
                             final BDD curTransitionRelation)
  {

    BDD nextStates;
    BDD previousReachableStates;
    final BDD premittedStates = forbiddenStatesBDD.not();
    reachableStatesBDD = reachableStatesBDD.and(premittedStates);

    if (trackPeakBDD) {
      maxNbrNodes = reachableStatesBDD.nodeCount() > maxNbrNodes
        ? reachableStatesBDD.nodeCount() : maxNbrNodes;
      maxNbrNodes = forbiddenStatesBDD.nodeCount() > maxNbrNodes
        ? forbiddenStatesBDD.nodeCount() : maxNbrNodes;
      maxNbrNodes = curTransitionRelation.nodeCount() > maxNbrNodes
        ? curTransitionRelation.nodeCount() : maxNbrNodes;
    }

    do {
      previousReachableStates = reachableStatesBDD.id();
      nextStates = reachableStatesBDD
        .relprod(curTransitionRelation, bddAutomata.getSourceStatesVarSet());
      nextStates.replaceWith(bddAutomata.getDestToSourceLocationPairing())
        .replaceWith(bddAutomata.getDestToSourceVariablePairing());

      if (trackPeakBDD) {
        maxNbrNodes = nextStates.nodeCount() > maxNbrNodes
        ? nextStates.nodeCount() : maxNbrNodes;
      }

      reachableStatesBDD.orWith(nextStates);
      reachableStatesBDD = reachableStatesBDD.and(premittedStates);

      if (trackPeakBDD) {
        maxNbrNodes = reachableStatesBDD.nodeCount() > maxNbrNodes
          ? reachableStatesBDD.nodeCount() : maxNbrNodes;
      }

    } while (!reachableStatesBDD.equals(previousReachableStates));

    previousReachableStates.free();
    return reachableStatesBDD;
  }

  /**
   * Returns a set of co-reachable states excluding a set of forbidden states
   * <p>
   * The initial and returned coreachableStatesBDD are "target states".
   *
   * @param bddAutomata
   *          the BDDExtendedAutomata object
   * @param coreachableStatesBDD
   *          a set of coreachable states
   * @param forbiddenStatesBDD
   *          a set of forbidden states
   * @param curTransitionRelation
   *          a partition
   * @return more states from <code>coreachableStatesBDD</code> through
   *         <code>curTransitionRelation</code>
   */
  public BDD restrictedPreImage(final BDDExtendedAutomata bddAutomata,
                                BDD coreachableStatesBDD,
                                final BDD forbiddenStatesBDD,
                                final BDD curTransitionRelation)
  {

    BDD nextStates;
    BDD previousCoreachableStates;
    final BDD premittedStates = forbiddenStatesBDD.not();
    coreachableStatesBDD = coreachableStatesBDD.and(premittedStates);

    if (trackPeakBDD) {
      maxNbrNodes = coreachableStatesBDD.nodeCount() > maxNbrNodes
        ? coreachableStatesBDD.nodeCount() : maxNbrNodes;
      maxNbrNodes = forbiddenStatesBDD.nodeCount() > maxNbrNodes
        ? forbiddenStatesBDD.nodeCount() : maxNbrNodes;
      maxNbrNodes = curTransitionRelation.nodeCount() > maxNbrNodes
        ? curTransitionRelation.nodeCount() : maxNbrNodes;
    }

    do {
      previousCoreachableStates = coreachableStatesBDD.id();
      nextStates = coreachableStatesBDD
        .relprod(curTransitionRelation, bddAutomata.getDestStatesVarSet());
      nextStates.replaceWith(bddAutomata.getSourceToDestLocationPairing())
        .replaceWith(bddAutomata.getSourceToDestVariablePairing());

      if (trackPeakBDD) {
        maxNbrNodes = nextStates.nodeCount() > maxNbrNodes
          ? nextStates.nodeCount() : maxNbrNodes;
      }

      coreachableStatesBDD.orWith(nextStates);
      coreachableStatesBDD = coreachableStatesBDD.and(premittedStates);

      if (trackPeakBDD) {
        maxNbrNodes = coreachableStatesBDD.nodeCount() > maxNbrNodes
          ? coreachableStatesBDD.nodeCount() : maxNbrNodes;
      }

    } while (!coreachableStatesBDD.equals(previousCoreachableStates));

    previousCoreachableStates.free();
    return coreachableStatesBDD;
  }
}
