//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   MergeStep
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.compositional;

import java.util.List;

import net.sourceforge.waters.analysis.tr.StateEncoding;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;


/**
 * An abstraction step in which the result automaton is obtained by
 * merging states of the original automaton (automaton quotient).
 *
 * @author Robi Malik
 */

class MergeStep extends TRAbstractionStep
{

  //#########################################################################
  //# Constructor
  /**
   * Creates a new abstraction step record.
   * @param  resultAut         The automaton resulting from abstraction.
   * @param  originalAut       The automaton before abstraction.
   * @param  tau               The event represent silent transitions,
   *                           or <CODE>null</CODE>.
   * @param  originalStateEnc  State encoding that relates states in the
   *                           original automaton to state numbers used in
   *                           the partition.
   * @param  partition         Partition that identifies classes of states
   *                           merged during abstraction.
   * @param  reduced           Whether or not the set of precondition markings
   *                           was reduced during abstraction.
   * @param  resultStateEnc    State encoding that relates states in the
   *                           original automaton to state numbers used in
   *                           the partition.
   */
  MergeStep(final AbstractCompositionalModelAnalyzer analyzer,
            final AutomatonProxy resultAut,
            final AutomatonProxy originalAut,
            final EventProxy tau,
            final StateEncoding originalStateEnc,
            final List<int[]> partition,
            final boolean reduced,
            final StateEncoding resultStateEnc)
  {
    super(analyzer, resultAut, originalAut, tau, originalStateEnc);
    mPartition = partition;
    mHasReducedPreconditionMarking = reduced;
    mResultStateEncoding = resultStateEnc;
  }


  //#########################################################################
  //# Simple Access
  StateEncoding getResultStateEncoding()
  {
    return mResultStateEncoding;
  }

  List<int[]> getPartition()
  {
    return mPartition;
  }

  boolean hasReducedPreconditionMarking()
  {
    return mHasReducedPreconditionMarking;
  }


  //#########################################################################
  //# Data Members
  /**
   * Partition applied to original automaton.
   * Each entry lists states of the input encoding that have been merged.
   */
  private final List<int[]> mPartition;
  /**
   * A flag, indicating that the precondition markings have been reduced
   * during abstraction and need to be recovered for trace expansion.
   * @see #mRecoveredPreconditionMarking
   */
  private final boolean mHasReducedPreconditionMarking;

  private final StateEncoding mResultStateEncoding;

}
