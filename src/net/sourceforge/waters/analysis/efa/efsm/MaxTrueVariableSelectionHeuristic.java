//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFSM Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa.efsm
//# CLASS:   MaxTrueVariableSelectionHeuristic
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.efsm;

import net.sourceforge.waters.analysis.compositional.NumericSelectionHeuristic;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TransitionIterator;


/**
 * @author Robi Malik, Sahar Mohajerani
 */

public class MaxTrueVariableSelectionHeuristic
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
  public double getHeuristicValue(final EFSMVariable var)
  {
    final EFSMTransitionRelation unfoldTR = mCache.unfold(var);
    if (unfoldTR == null) {
      return Double.POSITIVE_INFINITY;
    }
    final ListBufferTransitionRelation rel = unfoldTR.getTransitionRelation();
    final double transSize = rel.getNumberOfTransitions();
    final TransitionIterator iter = rel.createAllTransitionsReadOnlyIterator();
    int trueCount = 0;
    while (iter.advance()) {
      if (iter.getCurrentEvent() == EventEncoding.TAU) {
        trueCount++;
      }
    }
    return - trueCount / transSize;
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
