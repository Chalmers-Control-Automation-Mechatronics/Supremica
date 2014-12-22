//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFA Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa.unified
//# CLASS:   MaxEventsVariableSelectionHeuristic
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.unified;

import net.sourceforge.waters.analysis.compositional.NumericSelectionHeuristic;


/**
 * The &quot;maximum events&quot; variable selection heuristic for unified
 * EFA (MaxE). This heuristic gives preference to variables that appears in
 * the updates of the most events.
 *
 * @author Robi Malik, Sahar Mohajerani
 */

public class VariableSelectionHeuristicMaxE
  extends NumericSelectionHeuristic<UnifiedEFAVariable>
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
  public double getHeuristicValue(final UnifiedEFAVariable var)
  {
    final UnifiedEFAConflictChecker.VariableInfo info =
      mConflictChecker.getVariableInfo(var);
    return -info.getNumberOfEvents();
  }


  //#########################################################################
  //# Data Members
  private UnifiedEFAConflictChecker mConflictChecker;

}