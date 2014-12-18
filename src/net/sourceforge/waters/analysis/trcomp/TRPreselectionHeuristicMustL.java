//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.trcomp
//# CLASS:   TRPreselectionHeuristicMustL
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

import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.model.analysis.AnalysisException;


/**
 * <P>The <STRONG>MustL</STRONG> preselection heuristic used by compositional
 * model analysers of type {@link AbstractTRCompositionalAnalyzer}.</P>
 *
 * <P>The <STRONG>MustL</STRONG> preselection heuristic forms one candidate
 * for each event, which contains all automata using that event.</P>

 * <P><I>Reference:</I><BR>
 * Hugo Flordal, Robi Malik. Compositional Verification in Supervisory Control.
 * SIAM Journal of Control and Optimization, <STRONG>48</STRONG>(3),
 * 1914-1938, 2009.</P>
 *
 * @author Robi Malik
 */

class TRPreselectionHeuristicMustL extends TRPreselectionHeuristic
{

  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.trcomp.TRPreselectingHeuristic
  @Override
  Collection<TRCandidate> collectCandidates(final TRSubsystemInfo subsys)
    throws AnalysisException
  {
    final Collection<TREventInfo> events = subsys.getEvents(); // unordered!
    final int numEvents = events.size();
    final Map<List<TRAutomatonProxy>,TRCandidate> candidates =
      new HashMap<>(numEvents);
    for (final TREventInfo info : events) {
      if (!info.isExternal()) {
        final Set<TRAutomatonProxy> set = info.getAutomata();
        assert set.size() > 1;
        final List<TRAutomatonProxy> list = new ArrayList<>(set);
        recordCandidate(list, subsys, candidates);
      }
    }
    return candidates.values();
  }

}
