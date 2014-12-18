//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.trcomp
//# CLASS:   SelectionHeuristicMaxC
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.trcomp;

import net.sourceforge.waters.analysis.compositional.ChainSelectionHeuristic;
import net.sourceforge.waters.analysis.compositional.NumericSelectionHeuristic;
import net.sourceforge.waters.analysis.compositional.SelectionHeuristic;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;


/**
 * <P>The <STRONG>MaxC</STRONG> candidate selection heuristic for
 * compositional model analysers of type {@link AbstractTRCompositionalAnalyzer}.</P>
 *
 * <P>The <STRONG>MaxC</STRONG> selection heuristic gives preference to
 * candidate with the highest proportion of common events (i.e., events shared
 * by all automata of the candidate) over its total number of events
 * (excluding {@link EventEncoding#TAU}).</P>
 *
 * <P><I>Reference:</I><BR>
 * Hugo Flordal, Robi Malik. Compositional Verification in Supervisory Control.
 * SIAM Journal of Control and Optimization, 48(3), 1914-1938, 2009.</P>
 *
 * @author Robi Malik
 */

public class SelectionHeuristicMaxC
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
      AbstractTRCompositionalAnalyzer.SEL_MinE,
      AbstractTRCompositionalAnalyzer.SEL_MinS
    };
    return new ChainSelectionHeuristic<TRCandidate>(chain);
  }

  @Override
  protected double getHeuristicValue(final TRCandidate candidate)
  {
    final EventEncoding enc = candidate.getEventEncoding();
    int numCommon = 0;
    int numEvents = 0;
    for (final EventProxy event : enc.getUsedEvents()) {
      numEvents++;
      numCommon++;
      for (final TRAutomatonProxy aut : candidate.getAutomata()) {
        final EventEncoding autEnc = aut.getEventEncoding();
        final int e = autEnc.getEventCode(event);
        if (e < 0) {
          numCommon--;
        } else {
          final byte status = autEnc.getProperEventStatus(e);
          if (!EventStatus.isUsedEvent(status)) {
            numCommon--;
          }
        }
      }
    }
    if (numEvents == 0) {
      return -1.0;
    } else {
      return - (double) numCommon / numEvents;
    }
  }

}
