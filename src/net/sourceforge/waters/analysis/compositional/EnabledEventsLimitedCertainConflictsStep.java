//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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
