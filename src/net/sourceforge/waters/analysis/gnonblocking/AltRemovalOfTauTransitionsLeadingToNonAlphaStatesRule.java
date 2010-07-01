//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.gnonblocking
//# CLASS:   AltRemovalOfTauTransitionsLeadingToNonAlphaStatesRule
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.gnonblocking;

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
 * For a given automaton applies an abstraction rule, which removes a transition
 * when a tau event links two states <I>x</I> and&nbsp;<I>y</I> where at most
 * the source state&nbsp;<I>x</I> contains the precondition
 * marking&nbsp;<I>alpha</I>. If the target state&nbsp;<I>y</I> becomes
 * unreachable, it is removed, too. All transitions originating from the target
 * state&nbsp;<I>y</I> are copied to the source state&nbsp;<I>x</I>.
 * </P>
 *
 * <P>
 * This is an alternative experimental implementation of this rule that uses
 * {@link ListBufferTransitionRelation}.
 * </P>
 *
 * @author Rachel Francis, Robi Malik
 */

class AltRemovalOfTauTransitionsLeadingToNonAlphaStatesRule extends
    AbstractionRule
{
  // #######################################################################
  // # Constructors
  AltRemovalOfTauTransitionsLeadingToNonAlphaStatesRule(
                                                        final ProductDESProxyFactory factory)
  {
    this(factory, null);
  }

  AltRemovalOfTauTransitionsLeadingToNonAlphaStatesRule(
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
    final int tauID = eventEnc.getEventCode(tau);
    mInputStateEnc = new StateEncoding(autToAbstract);
    final ListBufferTransitionRelation rel =
        new ListBufferTransitionRelation(autToAbstract, eventEnc,
            mInputStateEnc, ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    final TransitionIterator iter = rel.createSuccessorsModifyingIterator();
    final int numStates = rel.getNumberOfStates();
    int source = 0;
    boolean modified = false;
    main: while (source < numStates) {
      iter.reset(source, tauID);
      while (iter.advance()) {
        final int target = iter.getCurrentTargetState();
        if (!rel.isMarked(target, alphaID)) {
          iter.remove();
          rel.copyOutgoingTransitions(target, source);
          modified = true;
          // After copying outgoing transitions from target to source,
          // the source state may receive new tau-transitions. To make sure
          // these are processed, we start checking the source state again.
          continue main;
        }
      }
      source++;
    }
    if (modified) {
      mAutToAbstract = autToAbstract;
      mTau = tau;
      rel.checkReachability();
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

  CompositionalGeneralisedConflictChecker.Step createStep(
                                                          final CompositionalGeneralisedConflictChecker checker,
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
  }

  // #######################################################################
  // # Data Members
  private EventProxy mAlphaMarking;

  private AutomatonProxy mAutToAbstract;
  private EventProxy mTau;
  private StateEncoding mInputStateEnc;
  private StateEncoding mOutputStateEnc;

}
