//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.trcomp
//# CLASS:   SelectionHeuristicMaxL
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
 * <P>The <STRONG>MaxL</STRONG> candidate selection heuristic for
 * compositional model analysers of type {@link AbstractTRCompositionalAnalyzer}.</P>
 *
 * <P>The <STRONG>MaxL</STRONG> selection heuristic gives preference to
 * candidate with the highest proportion of local events over its total
 * number of events (excluding {@link EventEncoding#TAU}).</P>
 *
 * <P><I>Reference:</I><BR>
 * Hugo Flordal, Robi Malik. Compositional Verification in Supervisory Control.
 * SIAM Journal of Control and Optimization, 48(3), 1914-1938, 2009.</P>
 *
 * @author Robi Malik
 */

public class SelectionHeuristicMaxL
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
      AbstractTRCompositionalAnalyzer.SEL_MaxC,
      AbstractTRCompositionalAnalyzer.SEL_MinE,
      AbstractTRCompositionalAnalyzer.SEL_MinS
    };
    return new ChainSelectionHeuristic<TRCandidate>(chain);
  }

  @Override
  public double getHeuristicValue(final TRCandidate candidate)
  {
    final EventEncoding enc = candidate.getEventEncoding();
    int numLocal = 0;
    int numEvents = 0;
    final byte pattern = EventStatus.STATUS_LOCAL | EventStatus.STATUS_UNUSED;
    for (int e = EventEncoding.NONTAU; e < enc.getNumberOfProperEvents(); e++) {
      final byte status = enc.getProperEventStatus(e);
      switch (status & pattern) {
      case EventStatus.STATUS_LOCAL:
        numLocal++;
        // fall through ...
      case 0:
        numEvents++;
        // fall through ...
      default:
        break;
      }
    }
    if (numEvents == 0) {
      return -1.0;
    } else {
      return - (double) numLocal / numEvents;
    }
  }

}
