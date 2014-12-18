//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.trcomp
//# CLASS:   TRPreselectionHeuristicMustSp
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

import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.model.analysis.AnalysisException;


/**
 * <P>The <STRONG>MustSp</STRONG> preselection heuristic used by compositional
 * model analysers of type {@link AbstractTRCompositionalAnalyzer}.</P>
 *
 * <P>The <STRONG>MustSp</STRONG> preselection heuristic forms two candidate
 * for each event: the first contains all automata where the event is not
 * selfloop-only, and the second contains all automata where the event is
 * not always enabled..</P>

 * <P><I>Reference:</I><BR>
 * Colin Pilbrow, Robi Malik. Compositional Nonblocking Verification with
 * Always Enabled Events and Selfloop-only Events. Proc. 2nd International
 * Workshop on Formal Techniques for Safety-Critical Systems, FTSCS 2013,
 * 147-162, Queenstown, New Zealand, 2013.</P>
 *
 * @author Robi Malik
 */

class TRPreselectionHeuristicMustSp extends TRPreselectionHeuristic
{

  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.trcomp.TRPreselectingHeuristic
  @Override
  Collection<TRCandidate> collectCandidates(final TRSubsystemInfo subsys)
    throws AnalysisException
  {
    final Collection<TREventInfo> events = subsys.getEvents();
    final int numEvents = events.size();
    final Map<List<TRAutomatonProxy>,TRCandidate> candidates =
      new HashMap<>(numEvents);
    for (final TREventInfo info : events) {
      final Set<TRAutomatonProxy> automata = info.getAutomata();
      final int numAutomata = automata.size();
      List<TRAutomatonProxy> notAlwaysEnabled = new ArrayList<>(numAutomata);
      List<TRAutomatonProxy> notSelfloopOnly = notAlwaysEnabled;
      boolean useNotAlwaysEnabled = false;
      boolean useNotSelfloopOnly = false;
      for (final TRAutomatonProxy aut : automata) {
        final byte status = info.getAutomatonStatus(aut);
        final boolean alwaysEnabled =
          EventStatus.isAlwaysEnabledEvent(status);
        final boolean selfloopOnly =
          EventStatus.isSelfloopOnlyEvent(status);
        if (notAlwaysEnabled == notSelfloopOnly) {
          if (!alwaysEnabled && !selfloopOnly) {
            notAlwaysEnabled.add(aut);
          } else if (!selfloopOnly) {
            notSelfloopOnly = new ArrayList<>(notAlwaysEnabled);
            notSelfloopOnly.add(aut);
            useNotAlwaysEnabled = true;
          } else {
            notAlwaysEnabled = new ArrayList<>(notSelfloopOnly);
            notAlwaysEnabled.add(aut);
            useNotSelfloopOnly = true;
          }
        } else {
          if (!selfloopOnly) {
            notSelfloopOnly.add(aut);
            useNotAlwaysEnabled = true;
          }
          if (!alwaysEnabled) {
            notAlwaysEnabled.add(aut);
            useNotSelfloopOnly = true;
          }
        }
      }
      if (notAlwaysEnabled == notSelfloopOnly) {
        recordCandidate(notAlwaysEnabled, subsys, candidates);
      } else {
        boolean recorded = false;
        if (useNotAlwaysEnabled && notAlwaysEnabled.size() > 1) {
          recordCandidate(notAlwaysEnabled, subsys, candidates);
          recorded = true;
        }
        if (useNotSelfloopOnly && notSelfloopOnly.size() > 1) {
          recordCandidate(notSelfloopOnly, subsys, candidates);
          recorded = true;
        }
        if (!recorded) {
          final List<TRAutomatonProxy> list = new ArrayList<>(automata);
          recordCandidate(list, subsys, candidates);
        }
      }
    }
    return candidates.values();
  }

}
