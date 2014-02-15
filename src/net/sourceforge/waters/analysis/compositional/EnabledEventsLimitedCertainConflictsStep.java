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

import net.sourceforge.waters.analysis.tr.StateEncoding;
import net.sourceforge.waters.analysis.tr.TRPartition;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;


/**
 * An abstraction step in which the result automaton is obtained by
 * certain conflicts simplification with enabled events or enabled event
 * sets.
 *
 * @author Robi Malik
 */

class EnabledEventsLimitedCertainConflictsStep extends AbstractionStep
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
  EnabledEventsLimitedCertainConflictsStep
    (final AbstractCompositionalModelAnalyzer analyzer,
     final AutomatonProxy resultAut,
     final AutomatonProxy originalAut,
     final EventProxy tau,
     final StateEncoding originalStateEnc,
     final StateEncoding resultStateEnc,
     final TRPartition partition,
     final int[] levels)
  {
    super(analyzer, resultAut, originalAut);
    mTau = tau;
    mOriginalStateEncoding = originalStateEnc;
    mResultStateEncoding = resultStateEnc;
    mPartition = partition;
    mLevels = levels;
  }


  //#########################################################################
  //# Trace Computation
  @Override
  protected List<TraceStepProxy> convertTraceSteps
    (final List<TraceStepProxy> traceSteps)
  throws AnalysisException
  {
    final CompositionalConflictChecker verifier = getVerifier();
    final AutomatonProxy resultAut = getResultAutomaton();
    final AutomatonProxy originalAut = getOriginalAutomaton();
    final EnabledEventsLimitedCertainConflictsTraceExpander expander =
      new EnabledEventsLimitedCertainConflictsTraceExpander
        (verifier, mTau, resultAut, mResultStateEncoding,
         originalAut, mOriginalStateEncoding, mPartition, mLevels);
    return expander.convertTraceSteps(traceSteps);
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
  /**
   * The partition computed by the simplifier.
   */
  private final TRPartition mPartition;
  /**
   * The levels of certain conflicts computed during simplification.
   * Indicates the level of each state or -1 for states that are not
   * certain conflicts.
   */
  private final int[] mLevels;

}