//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.trcomp
//# CLASS:   SelectionHeuristicMinF
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.trcomp;

import gnu.trove.set.hash.THashSet;

import java.util.Set;

import net.sourceforge.waters.analysis.compositional.ChainSelectionHeuristic;
import net.sourceforge.waters.analysis.compositional.NumericSelectionHeuristic;
import net.sourceforge.waters.analysis.compositional.SelectionHeuristic;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;


/**
 * <P>The <STRONG>MinF</STRONG> candidate selection heuristic for
 * compositional model analysers of type {@link AbstractTRCompositionalAnalyzer}.</P>
 *
 * <P>The <STRONG>MinF</STRONG> selection heuristic gives preference to
 * candidate with the smallest frontier, i.e., the smallest number
 * of other automata that share events with the candidate's automata.</P>
 *
 * @author Robi Malik
 */

public class SelectionHeuristicMinF
  extends NumericSelectionHeuristic<TRCandidate>
{

  //#########################################################################
  //# Interface
  //# net.sourceforge.waters.analysis.compositional.NumericSelectionHeuristic
  @Override
  public void setContext(final Object context)
  {
    mAnalyzer = (AbstractTRCompositionalAnalyzer) context;
  }

  @Override
  public SelectionHeuristic<TRCandidate> createDecisiveHeuristic()
  {
    @SuppressWarnings("unchecked")
    final SelectionHeuristic<TRCandidate>[] chain = new SelectionHeuristic[] {
      this,
      AbstractTRCompositionalAnalyzer.SEL_MinSync,
      AbstractTRCompositionalAnalyzer.SEL_MaxL,
      AbstractTRCompositionalAnalyzer.SEL_MaxC,
      AbstractTRCompositionalAnalyzer.SEL_MinE
    };
    return new ChainSelectionHeuristic<TRCandidate>(chain);
  }

  @Override
  public double getHeuristicValue(final TRCandidate candidate)
  {
    final Set<TRAutomatonProxy> connected = new THashSet<>();
    final TRSubsystemInfo subsys = mAnalyzer.getCurrentSubsystem();
    final EventEncoding enc = candidate.getEventEncoding();
    for (final EventProxy event : enc.getUsedEvents()) {
      final TREventInfo info = subsys.getEventInfo(event);
      if (info != null) {
        connected.addAll(info.getAutomata());
      }
    }
    return connected.size() - candidate.getAutomata().size();
  }


  //#########################################################################
  //# Data Members
  private AbstractTRCompositionalAnalyzer mAnalyzer;

}
