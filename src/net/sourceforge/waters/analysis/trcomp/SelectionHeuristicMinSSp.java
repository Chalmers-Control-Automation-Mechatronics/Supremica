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

package net.sourceforge.waters.analysis.trcomp;

import net.sourceforge.waters.analysis.compositional.ChainSelectionHeuristic;
import net.sourceforge.waters.analysis.compositional.NumericSelectionHeuristic;
import net.sourceforge.waters.analysis.compositional.SelectionHeuristic;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;


/**
 * <P>The <STRONG>MinSSp</STRONG> candidate selection heuristic for
 * compositional model analysers of type {@link AbstractTRCompositionalAnalyzer}.</P>
 *
 * <P>The <STRONG>MinSSp</STRONG>  estimates the number of states of the
 * abstracted synchronous composition of candidates and chooses the candidate
 * with the smallest estimate. The estimate is obtained by multiplying the
 * product of the state numbers of the candidate's automata with its
 * ratio of the shared events minus half the numbers of <I>selfloop-only</I>
 * and <I>always enabled</I> events over total number of events (including
 * {@link EventEncoding#TAU}).</P>
 *
 * @author Robi Malik
 */

public class SelectionHeuristicMinSSp
  extends NumericSelectionHeuristic<TRCandidate>
{

  //#########################################################################
  //# Constructor
  /**
   * Creates a new instance of this heuristic.
   * @param stateEstimator The heuristic used to estimate the state number of
   *                       the synchronous product.
   */
  public SelectionHeuristicMinSSp
    (final NumericSelectionHeuristic<TRCandidate> stateEstimator)
  {
    mStateEstimator = stateEstimator;
  }

  //#########################################################################
  //# Interface
  //# net.sourceforge.waters.analysis.compositional.NumericSelectionHeuristic
  @Override
  public SelectionHeuristic<TRCandidate> createDecisiveHeuristic()
  {
    @SuppressWarnings("unchecked")
    final SelectionHeuristic<TRCandidate>[] chain = new SelectionHeuristic[] {
      this,
      AbstractTRCompositionalAnalyzer.SEL_MinS,
      AbstractTRCompositionalAnalyzer.SEL_MaxL,
      AbstractTRCompositionalAnalyzer.SEL_MaxC,
      AbstractTRCompositionalAnalyzer.SEL_MinE
    };
    return new ChainSelectionHeuristic<TRCandidate>(chain);
  }

  @Override
  public double getHeuristicValue(final TRCandidate candidate)
  {
    final double numStates = mStateEstimator.getHeuristicValue(candidate);
    final byte pattern =
      EventStatus.STATUS_FULLY_LOCAL | EventStatus.STATUS_UNUSED;
    int numEvents = 0;
    int weightOfLocalEvents = 0;
    final EventEncoding enc = candidate.getEventEncoding();
    for (int e = EventEncoding.NONTAU; e < enc.getNumberOfProperEvents(); e++) {
      final byte status = enc.getProperEventStatus(e);
      switch (status & pattern) {
      case EventStatus.STATUS_FULLY_LOCAL:
        numEvents++;
        weightOfLocalEvents += 2;
        break;
      case EventStatus.STATUS_SELFLOOP_ONLY:
      case EventStatus.STATUS_ALWAYS_ENABLED:
        numEvents++;
        weightOfLocalEvents++;
        break;
      case 0:
        numEvents++;
        break;
      default:
        break;
      }
    }
    for (final TRAutomatonProxy aut : candidate.getAutomata()) {
      final EventEncoding autEnc = aut.getEventEncoding();
      final byte tauStatus = autEnc.getProperEventStatus(EventEncoding.TAU);
      if (EventStatus.isUsedEvent(tauStatus)) {
        numEvents++;
        weightOfLocalEvents += 2;
      }
    }
    if (numEvents == 0) {
      return 0.0;
    } else {
      return 0.5 * numStates *
        (2 * numEvents - weightOfLocalEvents) / numEvents;
    }
  }


  //#########################################################################
  //# Data Members
  private final NumericSelectionHeuristic<TRCandidate> mStateEstimator;

}








