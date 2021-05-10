//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
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

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.des.EventProxy;


/**
 * <P>The <STRONG>Pairs</STRONG> preselection heuristic used by compositional
 * model analysers of type {@link AbstractTRCompositionalModelAnalyzer}.</P>
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
          checkAbort();
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
