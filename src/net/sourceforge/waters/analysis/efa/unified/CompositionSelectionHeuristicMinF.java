//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFA Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa.unified
//# CLASS:   MinFrontierCompositionSelectionHeuristic
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.unified;

import gnu.trove.set.hash.THashSet;

import java.util.Set;

import net.sourceforge.waters.analysis.compositional.NumericSelectionHeuristic;
import net.sourceforge.waters.analysis.efa.unified.UnifiedEFAConflictChecker.EventInfo;
import net.sourceforge.waters.analysis.efa.unified.UnifiedEFAConflictChecker.VariableInfo;


/**
 * The &quot;minimum frontier&quot; composition selection heuristic for unified
 * EFA (MinF). This heuristic gives preference to candidates with the
 * smallest number of other transition relations and variables linked via
 * events to the candidate's transitions relations and variables.
 *
 * @author Robi Malik, Sahar Mohajerani
 */

public class CompositionSelectionHeuristicMinF
  extends NumericSelectionHeuristic<UnifiedEFACandidate>
{

  //#########################################################################
  //# Overrides for
  //# net.sourceforge.waters.analysis.compositional.NumericSelectionHeuristic
  @Override
  public void setContext(final Object context)
  {
    mConflictChecker = (UnifiedEFAConflictChecker) context;
  }

  @Override
  protected double getHeuristicValue(final UnifiedEFACandidate candidate)
  {
    // Set to contain variables and transition relations of the candidate
    // plus its frontier
    final Set<Object> frontierCount = new THashSet<>();
    // Set to contain events used by the candidate
    final Set<AbstractEFAEvent> events = new THashSet<>();
    // Collect transition relations, variables, events of candidate
    for (final UnifiedEFATransitionRelation tr :
         candidate.getTransitionRelations()) {
      frontierCount.add(tr);
      events.addAll(tr.getUsedEventsExceptTau());
    }
    for (final VariableInfo var : candidate.getVariableInfo()) {
      frontierCount.add(var);
      for (final EventInfo info : var.getEvents()) {
        events.add(info.getEvent());
      }
    }
    // Remember the number of transition relations and variables of the candidate
    final int ownSize = frontierCount.size();
    // Add variables and transition relations of events to frontier
    for (final AbstractEFAEvent event : events) {
      final UnifiedEFAConflictChecker.EventInfo info =
        mConflictChecker.getEventInfo(event);
      frontierCount.addAll(info.getTransitionRelations());
      frontierCount.addAll(info.getVariables());
    }
    // Frontier size is number of all variables and transition relations
    // minus number of variables and transition relations of the candidate
    return frontierCount.size() - ownSize;
  }


  //#########################################################################
  //# Data Members
  private UnifiedEFAConflictChecker mConflictChecker;

}