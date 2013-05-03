//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   ConflictEquivalenceStep
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.compositional;

import java.util.List;

import net.sourceforge.waters.analysis.tr.StateEncoding;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;


/**
 * An abstraction step in which the result automaton is obtained by
 * merging states in such a way that generalised conflict equivalence
 * is preserved. This class supports all conflict preserving merge
 * operations. Trace computation is achieved by breadth-first search,
 * with complexity O(|<I>s</I>||<I>Q</I>|) where |<I>s</I>| is the
 * length of the trace of the abstracted automaton and |<I>Q</I>| is the
 * number of states of the original automaton.
 *
 * @author Robi Malik
 */

class ConflictEquivalenceStep extends MergeStep
{

  //#########################################################################
  //# Constructor
  /**
   * Creates a new conflict equivalence step record.
   * @param  resultAut         The automaton resulting from abstraction.
   * @param  originalAut       The automaton before abstraction.
   * @param  tau               The event representing silent transitions,
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
  ConflictEquivalenceStep(final AbstractCompositionalModelAnalyzer analyzer,
                          final AutomatonProxy resultAut,
                          final AutomatonProxy originalAut,
                          final EventProxy tau,
                          final StateEncoding originalStateEnc,
                          final List<int[]> partition,
                          final boolean reduced,
                          final StateEncoding resultStateEnc)
  {
    super(analyzer, resultAut, originalAut, tau,
          originalStateEnc, partition, reduced, resultStateEnc);
  }


  //#########################################################################
  //# Trace Computation
  @Override
  protected List<TraceStepProxy> convertTraceSteps
    (final List<TraceStepProxy> traceSteps)
    throws AnalysisException
  {
    final AbstractCompositionalModelVerifier verifier =
      (AbstractCompositionalModelVerifier) getAnalyzer();
    final EventProxy tau = getTau();
    final EventProxy preconditionMarking = getUsedPreconditionMarking();
    final AutomatonProxy resultAut = getResultAutomaton();
    final StateEncoding resultStateEnc = getResultStateEncoding();
    final AutomatonProxy originalAut = getOriginalAutomaton();
    final StateEncoding originalStateEnc = getOriginalStateEncoding();
    final List<int[]> partition = getPartition();
    final boolean reduced = hasReducedPreconditionMarking();
    final ConflictEquivalenceTraceExpander expander =
      new ConflictEquivalenceTraceExpander
        (verifier, tau, preconditionMarking, resultAut, resultStateEnc,
         originalAut, originalStateEnc, partition, reduced);
    return expander.convertTraceSteps(traceSteps);
  }

}
