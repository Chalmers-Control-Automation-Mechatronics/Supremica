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
 * For a given Automaton applies an abstraction rule which removes state x which
 * does not have the alpha or default marking if it only has outgoing tau
 * transitions. The incoming transitions to x are redirected to all the (tau)
 * successor states of x.
 *
 * @author Rachel Francis
 */
class RemovalOfTauTransitionsOriginatingFromNonAlphaStatesRule extends
    AbstractionRule
{
  // #######################################################################
  // # Constructors
  RemovalOfTauTransitionsOriginatingFromNonAlphaStatesRule(
                                                           final ProductDESProxyFactory factory)
  {
    this(factory, null);
  }

  RemovalOfTauTransitionsOriginatingFromNonAlphaStatesRule(
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
    boolean modified = false;
    mTR =
        new ObserverProjectionTransitionRelation(autToAbstract,
            getPropositions());
    final int tauID = mTR.getEventInt(tau);

    final int alphaID = mTR.getEventInt(mAlphaMarking);
    final int defaultID = mTR.getEventInt(mDefaultMarking);
    if (tauID == -1 || alphaID == -1 || defaultID == -1) {
      return autToAbstract;
    }
    mTau = tau;

    final int numStates = mTR.getNumberOfStates();

    final Queue<Integer> visitStates = new LinkedList<Integer>();
    for (int sourceID = 0; sourceID < numStates; sourceID++) {
      if (!mTR.isMarked(sourceID, alphaID)
          && !mTR.isMarked(sourceID, defaultID)) {
        visitStates.offer(sourceID);
      }
    }
    while (visitStates.size() > 0) {
      final int sourceID = visitStates.remove();
      final TIntHashSet[] allSuccessors = mTR.getAllSuccessors(sourceID);
      TIntHashSet tauSuccessors = null;
      boolean nonTauSuccessors = false;
      if (allSuccessors != null) {
        for (int eventID = 0; eventID < allSuccessors.length; eventID++) {
          if (eventID != tauID) {
            final TIntHashSet succ = allSuccessors[eventID];
            if (succ != null) {
              nonTauSuccessors = true;
              break;
            }
          } else {
            tauSuccessors = allSuccessors[eventID];
          }
        }
        if (!nonTauSuccessors && tauSuccessors != null) {
          final TIntArrayList transToRemove =
              new TIntArrayList(tauSuccessors.size());
          final TIntIterator iter = tauSuccessors.iterator();
          while (iter.hasNext()) {
            final int targetID = iter.next();
            if (targetID != sourceID) {
              transToRemove.add(targetID);
            }
          }
          for (int i = 0; i < transToRemove.size(); i++) {
            final int targetID = transToRemove.get(i);
            mTR.addAllPredeccessors(sourceID, targetID);
            mTR.removeTransition(sourceID, tauID, targetID);
            // visitStates.offer(sourceID);
            modified = true;
          }
          mTR.removeAllIncoming(sourceID);
        }
      }
    }
    if (modified) {
      final AutomatonProxy convertedAut = mTR.createAutomaton(getFactory());
      System.out.println(autToAbstract);
      System.out.println("CONVERTED--------------" + convertedAut);
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
  private EventProxy mDefaultMarking;
  private AutomatonProxy mAutToAbstract;
  private ObserverProjectionTransitionRelation mTR;
}
