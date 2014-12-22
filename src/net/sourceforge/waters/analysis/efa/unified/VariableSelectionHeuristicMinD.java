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
 * The &quot;minimum domain&quot; variable selection heuristic for unified
 * EFA (MinD). This heuristic gives preference to variables with the smallest
 * domain, i.e., the smallest range of possible values.
 *
 * @author Robi Malik, Sahar Mohajerani
 */

public class VariableSelectionHeuristicMinD
  extends NumericSelectionHeuristic<UnifiedEFAVariable>
{

  //#########################################################################
  //# Overrides for
  //# net.sourceforge.waters.analysis.compositional.NumericSelectionHeuristic
  @Override
  public double getHeuristicValue(final UnifiedEFAVariable var)
  {
    return var.getRange().size();
  }

}