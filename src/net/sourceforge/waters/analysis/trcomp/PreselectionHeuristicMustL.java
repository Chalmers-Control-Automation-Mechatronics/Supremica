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

import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.model.analysis.OverflowException;


/**
 * @author Robi Malik
 */

class PreselectionHeuristicMustL extends PreselectionHeuristic
{
  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.trcomp.PreselectingHeuristic
  @Override
  public Collection<TRCandidate> collectCandidates(final TRSubsystemInfo subsys)
    throws OverflowException
  {
    final Collection<TREventInfo> events = subsys.getEvents(); // unordered!
    final int numEvents = events.size();
    final Map<List<TRAutomatonProxy>,TRCandidate> candidates =
      new HashMap<>(numEvents);
    for (final TREventInfo info : events) {
      final Set<TRAutomatonProxy> set = info.getAutomata();
      assert set.size() > 1;
      final List<TRAutomatonProxy> list = new ArrayList<>(set);
      recordCandidate(list, subsys, candidates);
    }
    return candidates.values();
  }
}
