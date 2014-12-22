//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.trcomp
//# CLASS:   SelectionHeuristicMinS
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
 * <P>The <STRONG>MinS</STRONG> and <STRONG>MinS</STRONG><SUP>&alpha;</SUP>
 * candidate selection heuristics for compositional model analysers of type
 * {@link AbstractTRCompositionalAnalyzer}.</P>
 *
 * <P>The <STRONG>MinS</STRONG> heuristic estimates the number of states of the
 * abstracted synchronous composition of candidates and chooses the candidate
 * with the smallest estimate. The estimate is obtained by multiplying the
 * product of the state numbers of the candidate's automata with its
 * ratio of shared over total events (excluding {@link EventEncoding#TAU}).</P>
 *
 * <P>The <STRONG>MinS</STRONG><SUP>&alpha;</SUP> heuristic is of interest
 * when verifying the generalised nonblocking property. It estimates the
 * number of states of the abstracted synchronous composition of candidates
 * and chooses the candidate with the smallest estimate. The estimate is
 * obtained by multiplying the product of the numbers of precondition-marked
 * states of the candidate's automata with its ratio of shared over total
 * events (excluding {@link EventEncoding#TAU}).</P>
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
      case EventStatus.STATUS_LOCAL:
        numEvents++;
        // fall through ...
      case 0:
        numSharedEvents++;
        // fall through ...
      default:
        break;
      }
    }
    if (numEvents == 0) {
      return 1.0;
    } else {
      return numStates * numSharedEvents / numEvents;
    }
  }


  //#########################################################################
  //# Data Members
  private final NumericSelectionHeuristic<TRCandidate> mStateEstimator;

}
