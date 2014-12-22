//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.trcomp
//# CLASS:   SelectionHeuristicMinSSp
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.trcomp;

import net.sourceforge.waters.analysis.compositional.ChainSelectionHeuristic;
import net.sourceforge.waters.analysis.compositional.NumericSelectionHeuristic;
import net.sourceforge.waters.analysis.compositional.SelectionHeuristic;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;


/**
 * <P>The <STRONG>MinSSp</STRONG> candidate selection heuristic for
 * compositional model analysers of type {@link AbstractTRCompositionalAnalyzer}.</P>
 *
 * <P>The <STRONG>MinSSp</STRONG>  estimates the number of states of the
 * abstracted synchronous composition of candidates and chooses the candidate
 * with the smallest estimate. The estimate is obtained by multiplying the
 * product of the state numbers of the candidate's automata with its
 * ratio of the shared events minus half the numbers of <I>selfloop-only</I>
 * and <I>always enabled</I> events over total number of events (excluding
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
