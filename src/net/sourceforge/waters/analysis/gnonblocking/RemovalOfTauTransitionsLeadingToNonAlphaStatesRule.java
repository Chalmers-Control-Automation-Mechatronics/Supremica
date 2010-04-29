//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.analysis.gnonblocking
//# CLASS:   RemovalOfAlphaMarkingsRule
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.gnonblocking;

import gnu.trove.TIntArrayList;
import gnu.trove.TIntHashSet;
import gnu.trove.TIntIterator;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

import net.sourceforge.waters.analysis.op.ObserverProjectionTransitionRelation;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * For a given Automaton applies an abstraction rule which removes a transition
 * when a tau event links two states where at most the source contains the alpha
 * marking proposition (if the unmarked state becomes unreachable it is removed,
 * too). All transitions originating from the removed state (y) are copied to
 * state x.
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

  // #######################################################################
  // # Rule Application
  AutomatonProxy applyRule(final AutomatonProxy autToAbstract,
                           final EventProxy tau)
  {
    mAutToAbstract = autToAbstract;
    boolean modified = false;
    mTR =
        new ObserverProjectionTransitionRelation(autToAbstract,
            getPropositions());
    final int tauID = mTR.getEventInt(tau);
    final int alphaID = mTR.getEventInt(mAlphaMarking);
    if (tauID == -1 || alphaID == -1) {
      return autToAbstract;
    }
    mTau = tau;

    final int numStates = mTR.getNumberOfStates();
    final Queue<Integer> visitStates = new LinkedList<Integer>();

    for (int sourceID = 0; sourceID < numStates; sourceID++) {
      visitStates.offer(sourceID);
    }
    while (visitStates.size() > 0) {
      final int sourceID = visitStates.remove();
      final TIntHashSet successors = mTR.getSuccessors(sourceID, tauID);
      if (successors != null) {
        final TIntArrayList transToRemove =
            new TIntArrayList(successors.size());
        final TIntIterator iter = successors.iterator();
        while (iter.hasNext()) {
          final int targetID = iter.next();
          if (!mTR.isMarked(targetID, alphaID)) {
            if (targetID != sourceID) {
              transToRemove.add(targetID);
            }
          }
        }
        for (int i = 0; i < transToRemove.size(); i++) {
          final int targetID = transToRemove.get(i);
          mTR.addAllSuccessors(targetID, sourceID);
          mTR.removeTransition(sourceID, tauID, targetID);
          visitStates.offer(sourceID);
          modified = true;
        }
      }
    }
    if (modified) {
      mTR.removeTauSelfLoops(tauID);
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
    return checker.createRemovalOfTauTransitionsStep(abstractedAut,
                                                     mAutToAbstract, mTau, mTR);
  }

  // #######################################################################
  // # Data Members
  private EventProxy mAlphaMarking;
  private EventProxy mTau;
  private AutomatonProxy mAutToAbstract;
  private ObserverProjectionTransitionRelation mTR;
}
