//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2017 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
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
 * <P>The <STRONG>MustSp</STRONG> preselection heuristic forms two candidates
 * for each event: the first contains all automata where the event is not
 * selfloop-only, and the second contains all automata where the event is
 * not always enabled.</P>

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
      if (numAutomata <= 1) {
        continue;
      }
      checkAbort();
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
