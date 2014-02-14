//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   LimitedCertainConflictsStep
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.compositional;

import java.util.List;

import net.sourceforge.waters.analysis.abstraction.EnabledEventsLimitedCertainConflictsTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.LimitedCertainConflictsTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier;
import net.sourceforge.waters.analysis.certainconf.CertainConflictsTRSimplifier;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.StateEncoding;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;


/**
 * An abstraction step in which the result automaton is obtained by
 * certain conflicts simplification.
 *
 * @author Robi Malik
 */

class LimitedCertainConflictsStep extends AbstractionStep
{

  //#########################################################################
  //# Constructor
  /**
   * Creates a new abstraction step record.
   * @param  simplifier        The certain conflicts simplifier that
   *                           produced this abstraction.
   * @param  resultAut         The automaton resulting from abstraction.
   * @param  originalAut       The automaton before abstraction.
   * @param  tau               The event representing silent transitions,
   *                           or <CODE>null</CODE>.
   * @param  originalStateEnc  State encoding that relates states in the
   *                           original automaton to state numbers used in
   *                           the partition.
   * @param  resultStateEnc    State encoding that relates states in the
   *                           original automaton to state numbers used in
   *                           the partition.
   */
  LimitedCertainConflictsStep
    (final AbstractCompositionalModelAnalyzer analyzer,
     final TransitionRelationSimplifier simplifier,
     final AutomatonProxy resultAut,
     final AutomatonProxy originalAut,
     final EventProxy tau,
     final StateEncoding originalStateEnc,
     final StateEncoding resultStateEnc)
   {
    super(analyzer, resultAut, originalAut);
    mSimplifier = simplifier;
    mTau = tau;
    mOriginalStateEncoding = originalStateEnc;
    mResultStateEncoding = resultStateEnc;
   }


  LimitedCertainConflictsStep
  (final AbstractCompositionalModelAnalyzer analyzer,
   final TransitionRelationSimplifier simplifier,
   final AutomatonProxy resultAut,
   final AutomatonProxy originalAut,
   final EventProxy tau,
   final StateEncoding originalStateEnc,
   final StateEncoding resultStateEnc,
   final EventEncoding eventEncoding,
   final int numEnabledEvents)
 {
  super(analyzer, resultAut, originalAut);
  mSimplifier = simplifier;
  mTau = tau;
  mOriginalStateEncoding = originalStateEnc;
  mResultStateEncoding = resultStateEnc;

  mEventEncoding = eventEncoding;
  mNumEnabledEvents = numEnabledEvents;
 }


  //#########################################################################
  //# Trace Computation
  @Override
  protected List<TraceStepProxy> convertTraceSteps
    (final List<TraceStepProxy> traceSteps)
  throws AnalysisException
  {
    final CompositionalConflictChecker verifier = getVerifier();
    if (mSimplifier instanceof LimitedCertainConflictsTRSimplifier) {
      final LimitedCertainConflictsTRSimplifier simplifier =
        (LimitedCertainConflictsTRSimplifier) mSimplifier;
      final AutomatonProxy resultAut = getResultAutomaton();
      final AutomatonProxy originalAut = getOriginalAutomaton();
      final LimitedCertainConflictsTraceExpander expander =
        new LimitedCertainConflictsTraceExpander
          (verifier, simplifier, mTau, resultAut, mResultStateEncoding,
           originalAut, mOriginalStateEncoding);
      return expander.convertTraceSteps(traceSteps);
    } else if (mSimplifier instanceof EnabledEventsLimitedCertainConflictsTRSimplifier) {
      final EnabledEventsLimitedCertainConflictsTRSimplifier simplifier =
        (EnabledEventsLimitedCertainConflictsTRSimplifier) mSimplifier;
      final AutomatonProxy resultAut = getResultAutomaton();
      final AutomatonProxy originalAut = getOriginalAutomaton();
      final EnabledEventsLimitedCertainConflictsTraceExpander expander =
        new EnabledEventsLimitedCertainConflictsTraceExpander
          (verifier, simplifier, mTau, resultAut, mResultStateEncoding,
           originalAut, mOriginalStateEncoding,
           mEventEncoding, mNumEnabledEvents);
      return expander.convertTraceSteps(traceSteps);
      /*
    } else if (mSimplifier instanceof EnabledEventsSetLimitedCertainConflictsTRSimplifier) {
      final EnabledEventsSetLimitedCertainConflictsTRSimplifier simplifier =
        (EnabledEventsSetLimitedCertainConflictsTRSimplifier) mSimplifier;
      final AutomatonProxy resultAut = getResultAutomaton();
      final AutomatonProxy originalAut = getOriginalAutomaton();
      final EnabledEventsSetLimitedCertainConflictsTraceExpander expander =
        new EnabledEventsSetLimitedCertainConflictsTraceExpander(
          (verifier, simplifier, mTau, resultAut, mResultStateEncoding,
           originalAut, mOriginalStateEncoding, mEventEncoding);
      return expander.convertTraceSteps(traceSteps);
      */
    } else if (mSimplifier instanceof CertainConflictsTRSimplifier) {
      final CertainConflictsTRSimplifier simplifier =
        (CertainConflictsTRSimplifier) mSimplifier;
      final AutomatonProxy resultAut = getResultAutomaton();
      final AutomatonProxy originalAut = getOriginalAutomaton();
      final CertainConflictsTraceExpander expander =
        new CertainConflictsTraceExpander
          (verifier, simplifier, mTau, resultAut, mResultStateEncoding,
           originalAut, mOriginalStateEncoding);
      return expander.convertTraceSteps(traceSteps);
    } else {
      throw new UnsupportedOperationException
        ("Trace expansion for " + ProxyTools.getShortClassName(mSimplifier) +
         " not yet implemented!");
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private CompositionalConflictChecker getVerifier()
  {
    return (CompositionalConflictChecker) super.getAnalyzer();
  }


  //#########################################################################
  //# Data Members
  /**
   * The certain conflicts simplifier used to produce this abstraction.
   */
  private final TransitionRelationSimplifier mSimplifier;
  /**
   * The event that was hidden from the original automaton,
   * or <CODE>null</CODE>.
   */
  private final EventProxy mTau;
  /**
   * State encoding of original automaton. Maps state codes in the input
   * transition relation to state objects in the input automaton.
   */
  private final StateEncoding mOriginalStateEncoding;
  /**
   * Reverse encoding of output states. Maps states in output automaton
   * (simplified automaton) to state code in output transition relation.
   */
  private final StateEncoding mResultStateEncoding;

  private EventEncoding mEventEncoding;

  private int mNumEnabledEvents;

}