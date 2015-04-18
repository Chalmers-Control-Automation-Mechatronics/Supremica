//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFA Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa.unified
//# CLASS:   CompositionSelectionHeuristicMinF1
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.unified;

import net.sourceforge.waters.analysis.compositional.NumericSelectionHeuristic;


/**
 * The &quot;minimum frontier&quot; composition selection heuristic for unified
 * EFA (MinF<sub>1</sub>). This heuristic gives preference to candidates with
 * the smallest number of other transition relations and variables linked via
 * events to the candidate's transitions relations and variables.
 *
 * @author Robi Malik, Sahar Mohajerani
 */

public class CompositionSelectionHeuristicMinF1
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
  public double getHeuristicValue(final UnifiedEFACandidate candidate)
  {
    final int ownSize = candidate.getTransitionRelations().size() +
                  candidate.getVariables().size();
    final int frontierSize =
      mConflictChecker.getCurrentSubSystem().getFrontierSize2(candidate);
    return frontierSize - ownSize;
  }


  //#########################################################################
  //# Data Members
  private UnifiedEFAConflictChecker mConflictChecker;

}