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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sourceforge.waters.analysis.op.ObserverProjectionTransitionRelation;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * For a given Automaton applies an abstraction rule which removes a transition
 * when a tau event links two states where at most one of them contains the
 * alpha marking proposition (if the unmarked state becomes unreachable it is
 * removed to). All transitions originating from the removed state (y) are
 * copied to state x.
 *
 * @author Rachel Francis
 */

class RemovalOfTauTransitionsLeadingToNonAlphaStatesRule extends
    AbstractionRule
{
  // #######################################################################
  // # Constructors
  RemovalOfTauTransitionsLeadingToNonAlphaStatesRule(
                                                     final ProductDESProxyFactory factory)
  {
    this(factory, null);
  }

  RemovalOfTauTransitionsLeadingToNonAlphaStatesRule(
                                                     final ProductDESProxyFactory factory,
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

  EventProxy getDefaultMarking()
  {
    return mDefaultMarking;
  }

  void setDefaultMarking(final EventProxy defaultMarking)
  {
    mDefaultMarking = defaultMarking;
  }

  // #######################################################################
  // # Rule Application
  AutomatonProxy applyRule(final AutomatonProxy autToAbstract,
                           final EventProxy tau)
  {
    mAutToAbstract = autToAbstract;
    /*
     * if (!autToAbstract.getEvents().contains(mAlphaMarking)) { return
     * autToAbstract; }
     */
    boolean modified = false;
    mTR =
        new ObserverProjectionTransitionRelation(autToAbstract,
            getPropositions());
    final int tauID = mTR.getEventInt(tau);
    if (tauID == -1) {
      return autToAbstract;
    }

    final int alphaID = mTR.getEventInt(mAlphaMarking);
    @SuppressWarnings("unused")
    final int defaultID = mTR.getEventInt(mDefaultMarking);
    final int numStates = mTR.getNumberOfStates();

    // creates a hash set of all states which are reachable from an alpha marked
    // state
    for (int sourceID = 0; sourceID < numStates; sourceID++) {
      final TIntHashSet successors = mTR.getSuccessors(sourceID, tauID);
      if (successors != null) {
        final TIntIterator iter = successors.iterator();
        final List<Integer> transToRemove =
            new ArrayList<Integer>(successors.size());
        while (iter.hasNext()) {
          final int targetID = iter.next();
          if (!mTR.isMarked(targetID, alphaID)) {
            if (targetID != sourceID) {
              transToRemove.add(targetID);
              mTR.moveAllSuccessors(targetID, sourceID);
              modified = true;
            }
          }
        }
        for (final int target : transToRemove) {
          mTR.removeTransition(sourceID, tauID, target);
        }
      }
    }
    if (modified) {
      final AutomatonProxy convertedAut = mTR.createAutomaton(getFactory());
      return convertedAut;
    } else {
      return autToAbstract;
    }
  }

  CompositionalGeneralisedConflictChecker.Step createStep(
                                                          final CompositionalGeneralisedConflictChecker checker,
                                                          final AutomatonProxy abstractedAut)
  {
    return checker.createRemovalOfMarkingsStep(abstractedAut, mAutToAbstract,
                                               mTR.getOriginalIntToStateMap(),
                                               mTR.getResultingStateToIntMap());
  }

  // #######################################################################
  // # Data Members
  private EventProxy mAlphaMarking;
  private EventProxy mDefaultMarking;
  private AutomatonProxy mAutToAbstract;
  private ObserverProjectionTransitionRelation mTR;
}
