//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFSM Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa.efsm
//# CLASS:   CompositionSelectionHeuristic
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.efsm;

import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import net.sourceforge.waters.model.base.ProxyTools;


/**
 * The abstract superclass of all EFSM composition selection heuristics.
 * A composition selection heuristics chooses the best candidate among
 * different pairs of transition relations to be composed.
 *
 * @author Sahar Mohajerani, Robi Malik
 */

abstract class CompositionSelectionHeuristic
  implements Comparator<List<EFSMTransitionRelation>>
{

  //#########################################################################
  //# Constructors
  CompositionSelectionHeuristic()
  {
    mBestCandidate = null;
    mBestValue = Double.POSITIVE_INFINITY;
  }


  //#########################################################################
  //# Invocation
  List<EFSMTransitionRelation> selectComposition(final EFSMSystem system)
  {
    final List<EFSMVariable> variablesList = system.getVariables();
    final Collection<List<EFSMTransitionRelation>> visitedCandidates =
      new THashSet<List<EFSMTransitionRelation>>();
    List<EFSMTransitionRelation> bestCandidate = null;
    for (final EFSMVariable var : variablesList) {
      final Collection<EFSMTransitionRelation> efsmTRSet =
        var.getTransitionRelations();
      final List<EFSMTransitionRelation> efsmTRList =
        new ArrayList<EFSMTransitionRelation>(efsmTRSet);
      for (int i = 0; i < efsmTRList.size(); i++) {
        for (int j = i + 1; j < efsmTRList.size(); j++) {
          final EFSMTransitionRelation efsmTR1 = efsmTRList.get(i);
          final EFSMTransitionRelation efsmTR2 = efsmTRList.get(j);
          final List<EFSMTransitionRelation> candidate =
            new ArrayList<EFSMTransitionRelation>(2);
          if (efsmTR1.compareTo(efsmTR2) <= 0) {
            candidate.add(efsmTR1);
            candidate.add(efsmTR2);
          } else {
            candidate.add(efsmTR2);
            candidate.add(efsmTR1);
          }
          if (visitedCandidates.add(candidate)) {
            if (bestCandidate == null ||
                compare(candidate, bestCandidate) < 0) {
              bestCandidate = candidate;
            }
          }
        }
      }
    }
    reset();
    return bestCandidate;
  }

  void reset()
  {
    mBestCandidate = null;
    mBestValue = Double.POSITIVE_INFINITY;
  }

  abstract double getHeuristicValue(List<EFSMTransitionRelation> candidate);


  //#########################################################################
  //# Interface java.util.Comparator<List>
  @Override
  public int compare(final List<EFSMTransitionRelation> candidate1,
                     final List<EFSMTransitionRelation> candidate2)
  {
    final double value1;
    if (candidate1 == mBestCandidate) {
      value1 = mBestValue;
    } else {
      value1 = getHeuristicValue(candidate1);
    }
    final double value2;
    if (candidate2 == mBestCandidate) {
      value2 = mBestValue;
    } else {
      value2 = getHeuristicValue(candidate2);
    }
    if (value1 < value2) {
      mBestCandidate = candidate1;
      mBestValue = value1;
      return -1;
    } else if (value2 < value1) {
      mBestCandidate = candidate2;
      mBestValue = value2;
      return 1;
    } else {
      if (mBestValue != value1) {
        mBestCandidate = candidate1;
        mBestValue = value1;
      }
      return 0;
    }
  }


  //#########################################################################
  //# Overrides for java.lang.Object
  @Override
  public String toString()
  {
    final String className = ProxyTools.getShortClassName(this);
    if (className.endsWith("CompositionSelectionHeuristic")) {
      final int len = "CompositionSelectionHeuristic".length();
      return className.substring(0, className.length()-len);
    } else {
      return className;
    }
  }


  //#########################################################################
  //# Data Members
  private List<EFSMTransitionRelation> mBestCandidate;
  private double mBestValue;

}
