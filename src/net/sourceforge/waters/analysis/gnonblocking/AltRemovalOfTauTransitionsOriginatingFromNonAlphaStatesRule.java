//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.gnonblocking
//# CLASS:   AltRemovalOfTauTransitionsOriginatingFromNonAlphaStatesRule
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.gnonblocking;

import gnu.trove.TIntArrayList;
import java.util.Collection;

import net.sourceforge.waters.analysis.op.EventEncoding;
import net.sourceforge.waters.analysis.op.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.op.StateEncoding;
import net.sourceforge.waters.analysis.op.TransitionIterator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * <P>
 * For a given automaton, applies an abstraction rule, which removes
 * state&nbsp;<I>x</I> that does not have the alpha or default marking, if it
 * only has outgoing tau transitions. The incoming transitions to&nbsp;<I>x</I>
 * are redirected to all the (tau) successor states of&nbsp;<I>x</I>.
 * </P>
 *
 * <P>
 * This is an alternative experimental implementation of this rule that uses
 * {@link ListBufferTransitionRelation}.
 * </P>
 *
 * @author Rachel Francis, Robi Malik
 */

class AltRemovalOfTauTransitionsOriginatingFromNonAlphaStatesRule extends
    AbstractionRule
{

  //#########################################################################
  //# Constructors
  AltRemovalOfTauTransitionsOriginatingFromNonAlphaStatesRule
    (final ProductDESProxyFactory factory)
  {
    this(factory, null);
  }

  AltRemovalOfTauTransitionsOriginatingFromNonAlphaStatesRule
    (final ProductDESProxyFactory factory,
     final Collection<EventProxy> propositions)
  {
    super(factory, propositions);
  }

  //#########################################################################
  //# Configuration
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


  //#########################################################################
  //# Rule Application
  AutomatonProxy applyRuleToAutomaton(final AutomatonProxy autToAbstract,
                                      final EventProxy tau)
      throws OverflowException
  {
    if (tau == null) {
      return autToAbstract;
    }
    final EventEncoding eventEnc = new EventEncoding(autToAbstract, tau);
    final int alphaID = eventEnc.getEventCode(mAlphaMarking);
    if (alphaID < 0) {
      return autToAbstract;
    }
    final int omegaID = eventEnc.getEventCode(mDefaultMarking);
    if (omegaID < 0) {
      return autToAbstract;
    }
    final int tauID = eventEnc.getEventCode(tau);
    mInputStateEnc = new StateEncoding(autToAbstract);
    final ListBufferTransitionRelation rel =
        new ListBufferTransitionRelation
          (autToAbstract, eventEnc, mInputStateEnc,
           ListBufferTransitionRelation.CONFIG_ALL);
    final TransitionIterator iter = rel.createSuccessorsReadOnlyIterator();
    final TIntArrayList targets = new TIntArrayList();
    final int numStates = rel.getNumberOfStates();
    boolean modified = false;

    main:
    for (int source = 0; source < numStates; source++) {
      if (!rel.isMarked(source, alphaID) && !rel.isMarked(source, omegaID)) {
        iter.resetState(source);
        while (iter.advance()) {
          final int eventID = iter.getCurrentEvent();
          if (eventID == tauID) {
            final int target = iter.getCurrentTargetState();
            targets.add(target);
          } else {
            targets.clear();
            continue main;
          }
        }
        if (!targets.isEmpty()) {
          rel.removeOutgoingTransitions(source, tauID);
          for (int i = 0; i < targets.size(); i++) {
            final int target = targets.get(i);
            rel.copyIncomingTransitions(source, target);
          }
          rel.removeIncomingTransitions(source);
          rel.setInitial(source, false);
          rel.setReachable(source, false);
          modified = true;
          targets.clear();
        }
      }
    }

    if (modified) {
      mAutToAbstract = autToAbstract;
      mTau = tau;
      rel.removeTauSelfLoops();
      rel.removeProperSelfLoopEvents();
      rel.removeRedundantPropositions();
      mOutputStateEnc = new StateEncoding();
      final ProductDESProxyFactory factory = getFactory();
      return rel.createAutomaton(factory, eventEnc, mOutputStateEnc);
    } else {
      mAutToAbstract = null;
      return autToAbstract;
    }
  }

  CompositionalGeneralisedConflictChecker.Step createStep
    (final CompositionalGeneralisedConflictChecker checker,
     final AutomatonProxy abstractedAut)
  {
    return checker.createObservationEquivalenceStep(abstractedAut,
                                                    mAutToAbstract, mTau,
                                                    mInputStateEnc, null,
                                                    mOutputStateEnc);
  }

  public void cleanup()
  {
    mInputStateEnc = null;
    mOutputStateEnc = null;
    mAutToAbstract = null;
  }


  //#########################################################################
  //# Data Members
  private EventProxy mAlphaMarking;
  private EventProxy mDefaultMarking;

  private AutomatonProxy mAutToAbstract;
  private EventProxy mTau;
  private StateEncoding mInputStateEnc;
  private StateEncoding mOutputStateEnc;

}
