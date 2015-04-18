//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFA Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa.unified
//# CLASS:   CompositionSelectionHeuristicMinF2
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.unified;

import net.sourceforge.waters.analysis.compositional.NumericSelectionHeuristic;


/**
 * The &quot;minimum frontier&quot; composition selection heuristic for
 * unified EFSMs (MinF<sub>2</sub>). Here, the frontier of a candidate is
 * defined as the numbers of transition relations and variables of a candidate
 * plus the number of other transition relations and variables linked via
 * events to the candidate's transitions relations and variables. The
 * heuristic gives preference to the candidate with the smallest frontier.
 *
 * @author Robi Malik
 */

public class CompositionSelectionHeuristicMinF2
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
    return mConflictChecker.getCurrentSubSystem().getFrontierSize2(candidate);
  }


  //#########################################################################
  //# Data Members
  private UnifiedEFAConflictChecker mConflictChecker;

}