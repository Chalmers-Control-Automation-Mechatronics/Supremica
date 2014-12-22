//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.trcomp
//# CLASS:   SelectionHeuristicMinE
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
 * <P>The <STRONG>MinE</STRONG> candidate selection heuristic for
 * compositional model analysers of type {@link AbstractTRCompositionalAnalyzer}.</P>
 *
 * <P>The <STRONG>MinE</STRONG> selection heuristic gives preference to
 * candidate with the smallest number of events after composition and
 * abstraction, i.e., the smallest number events shared with the remainder
 * of the system.</P>
 *
 * @author Robi Malik
 */

public class SelectionHeuristicMinE
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
      AbstractTRCompositionalAnalyzer.SEL_MaxL,
      AbstractTRCompositionalAnalyzer.SEL_MaxC,
      AbstractTRCompositionalAnalyzer.SEL_MinS
    };
    return new ChainSelectionHeuristic<TRCandidate>(chain);
  }

  @Override
  public double getHeuristicValue(final TRCandidate candidate)
  {
    final byte pattern = EventStatus.STATUS_LOCAL | EventStatus.STATUS_UNUSED;
    final EventEncoding enc = candidate.getEventEncoding();
    int numEvents = 0;
    for (int e = EventEncoding.NONTAU; e < enc.getNumberOfProperEvents(); e++) {
      final byte status = enc.getProperEventStatus(e);
      if ((status & pattern) == 0) {
        numEvents++;
      }
    }
    return numEvents;
  }

}
