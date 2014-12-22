//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFA Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa.unified
//# CLASS:   MinStatesCompositionSelectionHeuristic
//###########################################################################
//# $Id$
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