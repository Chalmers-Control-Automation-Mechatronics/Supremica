//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
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
                          final TRPartition partition,
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
    final TRPartition partition = getPartition();
    final boolean reduced = hasReducedPreconditionMarking();
    final ConflictEquivalenceTraceExpander expander =
      new ConflictEquivalenceTraceExpander
        (verifier, tau, preconditionMarking, resultAut, resultStateEnc,
         originalAut, originalStateEnc, partition, reduced);
    return expander.convertTraceSteps(traceSteps);
  }

}
