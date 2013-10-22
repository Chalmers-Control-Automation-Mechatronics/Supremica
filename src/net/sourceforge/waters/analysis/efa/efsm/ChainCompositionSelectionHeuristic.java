//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFSM Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa.efsm
//# CLASS:   ChainCompositionSelectionHeuristic
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.efsm;

import java.util.Iterator;
import java.util.List;


/**
 * A composition selection heuristic that applies several other heuristics
 * in sequence. The chain composition selection heuristic is initialised
 * with a list of composition selection heuristics. When comparing two
 * candidates, these heuristics are applied in the given order until
 * some heuristic can make a decision. If all heuristics are indifferent,
 * a default comparison based on names is used as a final resort.
 *
 * @author Robi Malik
 */

public class ChainCompositionSelectionHeuristic
  extends CompositionSelectionHeuristic
{

  //#########################################################################
  //# Constructors
  ChainCompositionSelectionHeuristic
    (final CompositionSelectionHeuristic... heuristics)
  {
    mHeuristics = heuristics;
  }


  //#########################################################################
  //# Interface java.util.Comparator<List>
  @Override
  public int compare(final List<EFSMTransitionRelation> candidate1,
                     final List<EFSMTransitionRelation> candidate2)
  {
    // Apply comparator chain ...
    for (final CompositionSelectionHeuristic heuristic : mHeuristics) {
      final int result = heuristic.compare(candidate1, candidate2);
      if (result != 0) {
        return result;
      }
    }
    // Default comparison ...
    final int len1 = candidate1.size();
    final int len2 = candidate2.size();
    if (len1 != len2) {
      return len1 - len2;
    }
    final Iterator<EFSMTransitionRelation> iter1 = candidate1.iterator();
    final Iterator<EFSMTransitionRelation> iter2 = candidate2.iterator();
    while (iter1.hasNext()) {
      final EFSMTransitionRelation efsmTR1 = iter1.next();
      final EFSMTransitionRelation efsmTR2 = iter2.next();
      final int result = efsmTR1.compareTo(efsmTR2);
      if (result != 0) {
        return result;
      }
    }
    return 0;
  }


  //#########################################################################
  //# Overrides for CompositionSelectionHeuristic
  @Override
  double getHeuristicValue(final List<EFSMTransitionRelation> candidate)
  {
    return 0.0;
  }

  @Override
  void reset()
  {
    for (final CompositionSelectionHeuristic heuristic : mHeuristics) {
      heuristic.reset();
    }
  }


  //#########################################################################
  //# Data Members
  private final CompositionSelectionHeuristic[] mHeuristics;

}