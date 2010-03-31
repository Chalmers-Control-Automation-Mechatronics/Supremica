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

import java.util.Collection;

import net.sourceforge.waters.analysis.op.ObserverProjectionTransitionRelation;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


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
    if (!autToAbstract.getEvents().contains(mAlphaMarking)) {
      // TODO Is this correct?
      return autToAbstract;
    }
    boolean modified = false;
    mTR =
        new ObserverProjectionTransitionRelation(autToAbstract,
            getPropositions());
    final int alphaID = mTR.getEventInt(mAlphaMarking);
    final int tauID = mTR.getEventInt(tau);
    final int numStates = mTR.getNumberOfStates();
    for (int sourceID = 0; sourceID < numStates; sourceID++) {
      if (mTR.hasPredecessors(sourceID) && mTR.isMarked(sourceID, alphaID)) {
        final TIntHashSet successors = mTR.getSuccessors(sourceID, tauID);
        if (successors != null) {
          final TIntIterator iter = successors.iterator();
          while (iter.hasNext()) {
            final int targetID = iter.next();
            if (mTR.isMarked(targetID, alphaID)) {
              if (targetID != sourceID) {
                mTR.markState(sourceID, alphaID, false);
                modified = true;
                break;
              }
            }
          }
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
  private AutomatonProxy mAutToAbstract;
  private ObserverProjectionTransitionRelation mTR;
}
