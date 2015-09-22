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
 * <P>The <STRONG>MinS</STRONG> and <STRONG>MinS</STRONG><SUP>&alpha;</SUP>
 * candidate selection heuristics for compositional model analysers of type
 * {@link AbstractTRCompositionalAnalyzer}.</P>
 *
 * <P>The <STRONG>MinS</STRONG> heuristic estimates the number of states of the
 * abstracted synchronous composition of candidates and chooses the candidate
 * with the smallest estimate. The estimate is obtained by multiplying the
 * product of the state numbers of the candidate's automata with its
 * ratio of shared over total events (including {@link EventEncoding#TAU}).</P>
 *
 * <P>The <STRONG>MinS</STRONG><SUP>&alpha;</SUP> heuristic is of interest
 * when verifying the generalised nonblocking property. It estimates the
 * number of states of the abstracted synchronous composition of candidates
 * and chooses the candidate with the smallest estimate. The estimate is
 * obtained by multiplying the product of the numbers of precondition-marked
 * states of the candidate's automata with its ratio of shared over total
 * events (including {@link EventEncoding#TAU}).</P>
 *
 * <P>An argument to the constructor determines which of the above heuristics
 * is implemented by an instance of this class.</P>
 *
 * <P><I>References:</I><BR>
 * Hugo Flordal, Robi Malik. Compositional Verification in Supervisory Control.
 * SIAM Journal of Control and Optimization, 48(3), 1914-1938, 2009.<BR>
 * Robi Malik, Ryan Leduc. Compositional Nonblocking Verification Using
 * Generalised Nonblocking Abstractions, IEEE Transactions on Automatic
 * Control <STRONG>58</STRONG>(8), 1-13, 2013.</P>
 *
 * @author Robi Malik
 */

public class SelectionHeuristicMinS
  extends NumericSelectionHeuristic<TRCandidate>
{

  //#########################################################################
  //# Constructor
  /**
   * Creates a new instance of this heuristic.
   * @param stateEstimator The heuristic used to estimate the state number of
   *                       the synchronous product. The <STRONG>MinS</STRONG>
   *                       heuristic is obtained by passing an instance of
   *                       {@link SelectionHeuristicMinS0}, and the
   *                       <STRONG>MinS</STRONG><SUP>&alpha;</SUP> heuristic
   *                       is obtained by passing an instance of {@link
   *                       SelectionHeuristicMinS0a} to this argument.
   */
  public SelectionHeuristicMinS
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
    final byte pattern = EventStatus.STATUS_LOCAL | EventStatus.STATUS_UNUSED;
    int numEvents = 0;
    int numSharedEvents = 0;
    final EventEncoding enc = candidate.getEventEncoding();
    for (int e = EventEncoding.NONTAU; e < enc.getNumberOfProperEvents(); e++) {
      final byte status = enc.getProperEventStatus(e);
      switch (status & pattern) {
      case 0:
        numSharedEvents++;
        // fall through ...
      case EventStatus.STATUS_LOCAL:
        numEvents++;
        // fall through ...
      default:
        break;
      }
    }
    for (final TRAutomatonProxy aut : candidate.getAutomata()) {
      final EventEncoding autEnc = aut.getEventEncoding();
      final byte tauStatus = autEnc.getProperEventStatus(EventEncoding.TAU);
      if (EventStatus.isUsedEvent(tauStatus)) {
        numEvents++;
      }
    }
    final double value =
      numEvents == 0 ? 1.0 : numStates * numSharedEvents / numEvents;
    //getLogger().debug(getName() + "(" + candidate.getName() + ") = " + value);
    return value;
  }



  //#########################################################################
  //# Data Members
  private final NumericSelectionHeuristic<TRCandidate> mStateEstimator;

}








