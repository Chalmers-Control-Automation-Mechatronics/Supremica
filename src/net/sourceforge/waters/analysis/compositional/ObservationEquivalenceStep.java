//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   ObservationEquivalenceStep
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
 * merging observation equivalent or weakly observation equivalent states.
 * This class provides more efficient trace computation than is possible
 * for a general merge.
 *
 * @author Robi Malik
 */

public class ObservationEquivalenceStep extends MergeStep
{

  //#######################################################################
  //# Constructors
  /**
   * Creates a new observation equivalence step record.
   * This constructor creates a step that assumes an unchanged set of
   * precondition markings.
   * @param  resultAut         The automaton resulting from abstraction.
   * @param  originalAut       The automaton before abstraction.
   * @param  tau               The event representing silent transitions,
   *                           or <CODE>null</CODE>.
   * @param  originalStateEnc  State encoding that relates states in the
   *                           original automaton to state numbers used in
   *                           the partition.
   * @param  partition         Partition that identifies classes of states
   *                           merged during abstraction.
   * @param  resultStateEnc    State encoding that relates states in the
   *                           original automaton to state numbers used in
   *                           the partition.
   */
  ObservationEquivalenceStep(final AbstractCompositionalModelAnalyzer analyzer,
                             final AutomatonProxy resultAut,
                             final AutomatonProxy originalAut,
                             final EventProxy tau,
                             final StateEncoding originalStateEnc,
                             final TRPartition partition,
                             final StateEncoding resultStateEnc)
  {
    this(analyzer, resultAut, originalAut, tau,
         originalStateEnc, partition, false, resultStateEnc);
  }

  /**
   * Creates a new observation equivalence step record.
   * @param  resultAut         The automaton resulting from abstraction.
   * @param  originalAut       The automaton before abstraction.
   * @param  tau               The event representing silent transitions,
   *                           or <CODE>null</CODE>.
   * @param  originalStateEnc  State encoding that relates states in the
   *                           original automaton to state numbers used in
   *                           the partition.
   * @param  partition         Partition that identifies classes of states
   *                           merged during abstraction.
   * @param  reduced           Whether or not the set of precondition
   *                           markings was reduced during abstraction.
   * @param  resultStateEnc    State encoding that relates states in the
   *                           original automaton to state numbers used in
   *                           the partition.
   */
  ObservationEquivalenceStep(final AbstractCompositionalModelAnalyzer analyzer,
                             final AutomatonProxy resultAut,
                             final AutomatonProxy originalAut,
                             final EventProxy tau,
                             final StateEncoding originalStateEnc,
                             final TRPartition partition,
                             final boolean reduced,
                             final StateEncoding resultStateEnc)
  {
    super(analyzer, resultAut, originalAut, tau,
          originalStateEnc, partition, reduced, resultStateEnc);
  }

  //#######################################################################
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
    final TRPartition partition = getPartition();
    final boolean reduced = hasReducedPreconditionMarking();
    final ObservationEquivalenceTraceExpander expander =
      new ObservationEquivalenceTraceExpander
        (verifier, tau, preconditionMarking,
         resultAut, resultStateEnc, originalAut, originalStateEnc,
         partition, reduced);
    return expander.convertTraceSteps(traceSteps);
  }

}
