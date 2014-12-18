//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.trcomp
//# CLASS:   PreselectionHeuristicMustL
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.trcomp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.des.EventProxy;


/**
 * <P>The <STRONG>Pairs</STRONG> preselection heuristic used by compositional
 * model analysers of type {@link AbstractTRCompositionalAnalyzer}.</P>
 *
 * <P>The <STRONG>Pairs</STRONG> preselection heuristic forms one candidate
 * for each pair of two automata that share at least one event.</P>
 *
 * @author Robi Malik
 */

class TRPreselectionHeuristicPairs extends TRPreselectionHeuristic
{

  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.trcomp.TRPreselectingHeuristic
  @Override
  Collection<TRCandidate> collectCandidates(final TRSubsystemInfo subsys)
    throws AnalysisException
  {
    final List<TRAutomatonProxy> automata = subsys.getAutomata();
    final int numAutomata = automata.size();
    final Map<List<TRAutomatonProxy>,TRCandidate> candidates =
      new HashMap<>(2 * numAutomata);
    int index1 = 0;
    for (final TRAutomatonProxy aut1 : automata) {
      final EventEncoding enc1 = aut1.getEventEncoding();
      final Set<EventProxy> used1 = enc1.getUsedEvents();
      final int numUsed1 = used1.size();
      int index2 = 0;
      for (final TRAutomatonProxy aut2 : automata) {
        if (index1 < index2) {
          final EventEncoding enc2 = aut2.getEventEncoding();
          final Set<EventProxy> used2 = enc2.getUsedEvents();
          final int numUsed2 = used2.size();
          final Set<EventProxy> used1a, used2a;
          if (numUsed1 < numUsed2) {
            used1a = used1;
            used2a = used2;
          } else {
            used1a = used2;
            used2a = used1;
          }
          for (final EventProxy event : used1a) {
            if (used2a.contains(event)) {
              final List<TRAutomatonProxy> pair = new ArrayList<>(2);
              pair.add(aut1);
              pair.add(aut2);
              recordCandidate(pair, subsys, candidates);
              break;
            }
          }
        }
        index2++;
      }
      index1++;
    }
    return candidates.values();
  }

}
