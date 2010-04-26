//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.analysis.gnonblocking
//# CLASS:   RemovalOfAlphaMarkingsRule
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.gnonblocking;

import gnu.trove.TIntHashSet;
import gnu.trove.TIntIterator;
import gnu.trove.TIntStack;
import gnu.trove.TObjectIntHashMap;

import java.util.Collection;
import net.sourceforge.waters.analysis.op.ObserverProjectionTransitionRelation;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;


/**
 * @author Rachel Francis
 */

class RemovalOfAlphaMarkingsRule extends AbstractionRule
{
  // #######################################################################
  // # Constructors
  RemovalOfAlphaMarkingsRule(final ProductDESProxyFactory factory)
  {
    this(factory, null);
  }

  RemovalOfAlphaMarkingsRule(final ProductDESProxyFactory factory,
                             final Collection<EventProxy> propositions)
  {
    super(factory, propositions);
  }

  // #######################################################################
  // # Configuration
  EventProxy getAlphaMarking()
  {
    return mAlphaMarking;
  }

  void setAlphaMarking(final EventProxy alphaMarking)
  {
    mAlphaMarking = alphaMarking;
  }

  // #######################################################################
  // # Rule Application
  AutomatonProxy applyRule(final AutomatonProxy autToAbstract,
                           final EventProxy tau)
  {
    mAutToAbstract = autToAbstract;
    mTR =
        new ObserverProjectionTransitionRelation(autToAbstract,
            getPropositions());
    final int tauID = mTR.getEventInt(tau);
    if (tauID == -1) {
      return autToAbstract;
    }
    final int numStates = mTR.getNumberOfStates();
    boolean modified = false;
    int alphaID = mTR.getEventInt(mAlphaMarking);
    if (alphaID == -1) {
      alphaID = mTR.addProposition(mAlphaMarking, true);
    }

    final TIntHashSet reachableStates = new TIntHashSet();
    final TIntStack unvisitedStates = new TIntStack();

    // performs a backwards search to remove alpha markings from states which
    // satisfy the rule conditions
    for (int stateID = 0; stateID < numStates; stateID++) {
      if (mTR.isMarked(stateID, alphaID) && !reachableStates.contains(stateID)) {
        unvisitedStates.push(stateID);
        while (unvisitedStates.size() > 0) {
          final int newState = unvisitedStates.pop();
          final TIntHashSet predeccessors =
              mTR.getPredecessors(newState, tauID);
          if (predeccessors != null) {
            final TIntIterator iter = predeccessors.iterator();
            while (iter.hasNext()) {
              final int predID = iter.next();
              if (predID != stateID) {
                if (predID != newState) {
                  if (!reachableStates.contains(predID)) {
                    reachableStates.add(predID);
                    unvisitedStates.push(predID);
                    if (mTR.isMarked(predID, alphaID)) {
                      mTR.markState(predID, alphaID, false);
                      modified = true;
                    }
                  }
                }
              } else {
                final boolean outgoing = expandTauLoop(predID, tauID, alphaID);
                if (!outgoing) {
                  mTR.markState(predID, alphaID, true);
                  modified = true;
                }
              }
            }
          }
        }
      }
    }
    if (modified) {
      final AutomatonProxy convertedAut = mTR.createAutomaton(getFactory());
      mOriginalIntToStateMap = mTR.getOriginalIntToStateMap();
      mResultingStateToIntMap = mTR.getResultingStateToIntMap();
      return convertedAut;
    } else {
      return autToAbstract;
    }
  }

  /**
   * Expands a loop of tau transitions to determine whether any of the states
   * within the loop have outgoing tau transitions which lead to alpha marked
   * states.
   *
   * @param stateIDs
   *          The ID of a state within the tau loop.
   * @return True if there is an outgoing tau transition from the loop which
   *         leads towards an alpha marked state, otherwise false.
   */
  private boolean expandTauLoop(final int stateID, final int tauID,
                                final int alphaID)
  {
    final TIntHashSet reachableStates = new TIntHashSet();
    final TIntStack unvisitedStates = new TIntStack();
    unvisitedStates.push(stateID);
    while (unvisitedStates.size() > 0) {
      final int newState = unvisitedStates.pop();
      final TIntHashSet tauSuccessors = mTR.getSuccessors(newState, tauID);
      // if there is not more than one successor then the single successor is
      // only the one as part of the tau loop (rather than an outgoing
      // transition we need to find)
      assert tauSuccessors != null;
      if (tauSuccessors.size() > 1) {
        final TIntIterator iter = tauSuccessors.iterator();
        while (iter.hasNext()) {
          final int succID = iter.next();
          if (!reachableStates.contains(succID)) {
            if (mTR.isMarked(succID, alphaID)) {
              return true;
            }
            reachableStates.add(succID);
            unvisitedStates.push(succID);
            // TODO: need a way to determine which states are part of the tau
            // loop and which is the outgoing transition
          }
        }
      } else {
        final TIntIterator iter = tauSuccessors.iterator();
        final int tauSucc = iter.next();
        if (tauSucc != stateID) {
          unvisitedStates.push(tauSucc);
        }
      }
    }
    return false;
  }

  CompositionalGeneralisedConflictChecker.Step createStep(
                                                          final CompositionalGeneralisedConflictChecker checker,
                                                          final AutomatonProxy abstractedAut)
  {
    return checker.createRemovalOfMarkingsStep(abstractedAut, mAutToAbstract,
                                               mOriginalIntToStateMap,
                                               mResultingStateToIntMap);
  }

  // #######################################################################
  // # Data Members
  private EventProxy mAlphaMarking;
  private AutomatonProxy mAutToAbstract;
  private StateProxy[] mOriginalIntToStateMap;
  private TObjectIntHashMap<StateProxy> mResultingStateToIntMap;
  ObserverProjectionTransitionRelation mTR = null;

}
