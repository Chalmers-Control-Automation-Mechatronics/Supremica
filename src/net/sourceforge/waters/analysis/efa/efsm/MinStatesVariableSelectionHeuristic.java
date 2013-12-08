//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFSM Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa.efsm
//# CLASS:   MinStatesVariableSelectionHeuristic
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.efsm;

import net.sourceforge.waters.analysis.compositional.NumericSelectionHeuristic;


/**
 * @author Robi Malik, Sahar Mohajerani
 */
public class MinStatesVariableSelectionHeuristic
  extends NumericSelectionHeuristic<EFSMVariable>
{

  //#########################################################################
  //# Overrides for
  //# net.sourceforge.waters.analysis.compositional.NumericSelectionHeuristic
  @Override
  public void setContext(final Object context)
  {
    mCache = (EFSMUnfoldingCache) context;
    mCache.register(this);
  }

  @Override
  protected double getHeuristicValue(final EFSMVariable var)
  {
    final EFSMTransitionRelation unfoldTR =  mCache.unfold(var);
    if (unfoldTR == null) {
      return Double.POSITIVE_INFINITY;
    } else {
      return unfoldTR.getTransitionRelation().getNumberOfStates();
    }
  }

  @Override
  protected void setBestCandidate(final EFSMVariable var)
  {
    mCache.reset(this, var);
  }

  @Override
  protected void reset()
  {
    super.reset();
    mCache.reset(this);
  }


  //#########################################################################
  //# Data Members
  private EFSMUnfoldingCache mCache;

}
