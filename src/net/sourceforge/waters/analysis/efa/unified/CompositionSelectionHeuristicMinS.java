//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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

package net.sourceforge.waters.analysis.efa.unified;

import gnu.trove.set.hash.THashSet;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import net.sourceforge.waters.analysis.compositional.NumericSelectionHeuristic;


/**
 * The &quot;minimum states&quot; composition selection heuristic for unified
 * EFA (MinS). This heuristic gives preference to candidates with the
 * minimum estimated number of states in the synchronous composition of
 * their EFAs and variables.
 *
 * @author Robi Malik, Sahar Mohajerani
 */

public class CompositionSelectionHeuristicMinS
  extends NumericSelectionHeuristic<UnifiedEFACandidate>
{

  //#########################################################################
  //# Overrides for
  //# net.sourceforge.waters.analysis.compositional.NumericSelectionHeuristic
  @Override
  public double getHeuristicValue(final UnifiedEFACandidate candidate)
  {
    Set<AbstractEFAEvent> eventSet = new THashSet<>();
    double numStates = 1.0;
    final Collection<UnifiedEFAConflictChecker.VariableInfo> vars =
      candidate.getVariableInfo();
    for (final UnifiedEFAConflictChecker.VariableInfo var : vars) {
      numStates *= var.getRangeSize();
      final List<AbstractEFAEvent> events = var.getLeaveEvents();
      eventSet.addAll(events);
    }
    final Collection<UnifiedEFATransitionRelation> trs =
      candidate.getTransitionRelations();
    for (final UnifiedEFATransitionRelation tr : trs) {
      numStates *= tr.getTransitionRelation().getNumberOfReachableStates();
      eventSet.addAll(tr.getUsedEventsExceptTau());
    }
    eventSet = removeNonLeaves(eventSet);
    int localEventSize = 0;
    final Collection<UnifiedEFAConflictChecker.EventInfo> localInfo =
      candidate.getLocalEvents();
    final Set<AbstractEFAEvent> localSet = new THashSet<>(localInfo.size());
    for (final UnifiedEFAConflictChecker.EventInfo info : localInfo) {
      final AbstractEFAEvent event = info.getEvent();
      localSet.add(event);
    }
    for (AbstractEFAEvent event : eventSet) {
      do {
        if (localSet.contains(event)) {
          localEventSize++;
          break;
        }
        event = event.getOriginalEvent();
      } while (event != null);
    }
    final double numEvents = eventSet.size();
    return (1.0 - localEventSize / numEvents) * numStates;
  }


  //#########################################################################
  //# Auxiliary Methods
  private static Set<AbstractEFAEvent> removeNonLeaves
    (final Collection<AbstractEFAEvent> eventSet)
  {
    final Set<AbstractEFAEvent> copy = new THashSet<>(eventSet);
    for (final AbstractEFAEvent event : eventSet) {
      AbstractEFAEvent original = event.getOriginalEvent();
      while (original != null) {
        copy.remove(original);
        original = original.getOriginalEvent();
      }
    }
    return copy;
  }

}
