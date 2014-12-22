//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.trcomp
//# CLASS:   SelectionHeuristicMinSa
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.trcomp;

import net.sourceforge.waters.analysis.compositional.ChainSelectionHeuristic;
import net.sourceforge.waters.analysis.compositional.NumericSelectionHeuristic;
import net.sourceforge.waters.analysis.compositional.SelectionHeuristic;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;


/**
 * <P>The <STRONG>MinS</STRONG><SUP>&alpha;</SUP> candidate selection
 * heuristic for compositional model analysers of type {@link
 * AbstractTRCompositionalAnalyzer}.</P>
 *
 * <P>The <STRONG>MinS</STRONG><SUP>&alpha;</SUP> heuristic is of interest
 * when verifying the generalised nonblocking property. It estimates the
 * number of states of the abstracted synchronous composition of candidates
 * and chooses the candidate with the smallest estimate. The estimate is
 * obtained by multiplying the product of the numbers of precondition-marked
 * states of the candidate's automata with its ratio of shared over total
 * events (excluding {@link EventEncoding#TAU}).</P>
 *
 * <P><I>Reference:</I><BR>
 * Robi Malik, Ryan Leduc. Compositional Nonblocking Verification Using
 * Generalised Nonblocking Abstractions, IEEE Transactions on Automatic
 * Control <STRONG>58</STRONG>(8), 1-13, 2013.</P>
 *
 * @author Robi Malik
 */

public class SelectionHeuristicMinSa
  extends NumericSelectionHeuristic<TRCandidate>
{

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
  protected double getHeuristicValue(final TRCandidate candidate)
  {
    final int alpha = AbstractTRCompositionalAnalyzer.PRECONDITION_MARKING;
    double numStates = 1.0;
    for (final TRAutomatonProxy aut : candidate.getAutomata()) {
      final ListBufferTransitionRelation rel = aut.getTransitionRelation();
      if (rel.isPropositionUsed(alpha)) {
        numStates *= rel.getNumberOfMarkings(alpha, false);
      } else {
        numStates *= rel.getNumberOfReachableStates();
      }
    }
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

}
