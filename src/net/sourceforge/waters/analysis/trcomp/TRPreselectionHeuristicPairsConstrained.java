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

import gnu.trove.map.hash.TObjectDoubleHashMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.des.EventProxy;


/**
 * An abstract superclass to compute pairing heuristics for compositional
 * model analysers of type {@link AbstractTRCompositionalAnalyzer}.
 * This class supports heuristics such as <STRONG>MinT</STRONG>
 * ({@link TRPreselectionHeuristicMinT}) where one automaton is chosen and
 * all other automata are paired with it.
 *
 * @author Robi Malik
 */

abstract class TRPreselectionHeuristicPairsConstrained
  extends TRPreselectionHeuristic
  implements Comparator<TRAutomatonProxy>
{

  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.trcomp.TRPreselectingHeuristic
  @Override
  Collection<TRCandidate> collectCandidates(final TRSubsystemInfo subsys)
    throws AnalysisException
  {
    final List<TRAutomatonProxy> automata = subsys.getAutomata();
    final int numAutomata = automata.size();
    mCache = new TObjectDoubleHashMap<>(numAutomata, 0.5f, -1.0);
    final Map<List<TRAutomatonProxy>,TRCandidate> candidates =
      new HashMap<>(numAutomata);
    final Queue<TRAutomatonProxy> queue =
      new PriorityQueue<TRAutomatonProxy>(numAutomata, this);
    queue.addAll(automata);
    while (candidates.isEmpty() && !queue.isEmpty()) {
      final TRAutomatonProxy aut1 = queue.poll();
      final EventEncoding enc1 = aut1.getEventEncoding();
      final Set<EventProxy> used1 = enc1.getUsedEvents();
      final int numUsed1 = used1.size();
      for (final TRAutomatonProxy aut2 : automata) {
        if (aut1 != aut2) {
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
      }
    }
    mCache = null;
    return candidates.values();
  }


  //#########################################################################
  //# Interface java.util.Comparator<TRAutomatonProxy>
  @Override
  public int compare(final TRAutomatonProxy aut1, final TRAutomatonProxy aut2)
  {
    final double val1 = getHeuristicValue(aut1);
    final double val2 = getHeuristicValue(aut2);
    if (val1 < val2) {
      return -1;
    } else if (val2 < val1) {
      return 1;
    } else {
      return aut1.compareTo(aut2);
    }
  }


  //#########################################################################
  //# Hooks
  abstract double computeHeuristicValue(TRAutomatonProxy aut);


  //#########################################################################
  //# Auxiliary Methods
  private double getHeuristicValue(final TRAutomatonProxy aut)
  {
    double value = mCache.get(aut);
    if (value < 0.0) {
      value = computeHeuristicValue(aut);
      mCache.put(aut, value);
    }
    return value;
  }


  //#########################################################################
  //# Data Members
  private TObjectDoubleHashMap<TRAutomatonProxy> mCache;

}
