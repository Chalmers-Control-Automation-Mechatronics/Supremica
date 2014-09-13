//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFA Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa.unified
//# CLASS:   MaxSelfloopsVariableSelectionHeuristic
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.unified;

import net.sourceforge.waters.analysis.compositional.NumericSelectionHeuristic;


/**
 * The &quot;maximum selfloops&quot; variable selection heuristic for unified
 * EFA (MaxS). This heuristic gives preference to variables that appears in
 * the updates of the most selfloop-only events, where an event is
 * selfloop-only if neither the event nor any of its ancestors appears
 * in any transition relation.
 *
 * @author Robi Malik, Sahar Mohajerani
 */

public class VariableSelectionHeuristicMaxS
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
  protected double getHeuristicValue(final UnifiedEFAVariable var)
  {
    final UnifiedEFAConflictChecker.VariableInfo info =
      mConflictChecker.getVariableInfo(var);
    return -info.getNumberOfSelfloops();
  }


  //#########################################################################
  //# Data Members
  private UnifiedEFAConflictChecker mConflictChecker;

}