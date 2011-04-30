//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.gnonblocking
//# CLASS:   RemovalOfTauTransitionsLeadingToNonAlphaStatesRule
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.gnonblocking;

import java.util.Collection;
import net.sourceforge.waters.analysis.op.EventEncoding;
import net.sourceforge.waters.analysis.op.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.op.SilentIncomingTRSimplifier;
import net.sourceforge.waters.analysis.op.StateEncoding;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.KindTranslator;
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

class RemovalOfTauTransitionsLeadingToNonAlphaStatesRule
  extends AbstractionRule
{

  //#########################################################################
  //# Constructors
  RemovalOfTauTransitionsLeadingToNonAlphaStatesRule
    (final ProductDESProxyFactory factory,
     final KindTranslator translator)
  {
    this(factory, translator, null);
  }

  RemovalOfTauTransitionsLeadingToNonAlphaStatesRule
    (final ProductDESProxyFactory factory,
     final KindTranslator translator,
     final Collection<EventProxy> propositions)
  {
    super(factory, translator, propositions);
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

  /**
   * Sets whether abstraction is applied to all states or only to states
   * that become unreachable. When this option is set to <CODE>true</CODE>
   * (the default), then the <I>Silent Incoming Rule</I> is only applied
   * to tau-transitions that lead to a state that becomes unreachable
   * by application of the rule. When set to <CODE>false</CODE>, the rule
   * is applied to all tau transitions leading to a state not marked by
   * the precondition, regardless of whether these states become unreachable
   * or not.
   */
  public void setRestrictsToUnreachableStates(final boolean restrict)
  {
    mRestrictsToUnreachableStates = restrict;
  }

  /**
   * Gets whether abstraction is applied to all states or only to states
   * that become unreachable.
   * @see #setRestrictsToUnreachableStates(boolean) setRestrictsToUnreachableStates()
   */
  public boolean getRestrictsToUnreachableStates()
  {
    return mRestrictsToUnreachableStates;
  }


  //#########################################################################
  //# Rule Application
  AutomatonProxy applyRuleToAutomaton(final AutomatonProxy autToAbstract,
                                      final EventProxy tau)
  throws AnalysisException
  {
    if (!autToAbstract.getEvents().contains(tau)) {
      return autToAbstract;
    }
    final KindTranslator translator = getKindTranslator();
    final EventEncoding eventEnc =
        new EventEncoding(autToAbstract, translator, tau, getPropositions(),
                          EventEncoding.FILTER_PROPOSITIONS);
    final int alphaID = eventEnc.getEventCode(mAlphaMarking);
    if (alphaID < 0) {
      return autToAbstract;
    }
    mTau = tau;
    mAutToAbstract = autToAbstract;
    mInputEncoding = new StateEncoding(autToAbstract);
    final ListBufferTransitionRelation rel =
      new ListBufferTransitionRelation
                 (autToAbstract, eventEnc, mInputEncoding,
                  ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    final SilentIncomingTRSimplifier simplifier =
      new SilentIncomingTRSimplifier(rel);
    simplifier.setPropositions(alphaID, -1);
    simplifier.setRestrictsToUnreachableStates(mRestrictsToUnreachableStates);
    final boolean modified = simplifier.run();
    if (modified) {
      rel.removeRedundantPropositions();
      final ProductDESProxyFactory factory = getFactory();
      mOutputEncoding = new StateEncoding();
      return rel.createAutomaton(factory, eventEnc, mOutputEncoding);
    } else {
      return autToAbstract;
    }
  }

  CompositionalGeneralisedConflictChecker.Step createStep
    (final CompositionalGeneralisedConflictChecker checker,
     final AutomatonProxy abstractedAut)
  {
    return checker.createRemovalOfTauTransitionsStep
      (abstractedAut, mAutToAbstract, mTau, mInputEncoding, mOutputEncoding);
  }

  public void cleanup()
  {
    mAutToAbstract = null;
    mInputEncoding = null;
    mOutputEncoding = null;
  }


  //#######################################################################
  //# Data Members
  private EventProxy mAlphaMarking;
  private EventProxy mTau;
  private boolean mRestrictsToUnreachableStates = true;

  private AutomatonProxy mAutToAbstract;
  private StateEncoding mInputEncoding;
  private StateEncoding mOutputEncoding;

}
