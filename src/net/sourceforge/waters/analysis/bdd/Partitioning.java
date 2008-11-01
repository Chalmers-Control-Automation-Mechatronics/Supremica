//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.bdd
//# CLASS:   Partitioning
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.bdd;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import net.sf.javabdd.BDDFactory;


/**
 * @author Robi Malik
 */

class Partitioning<P extends PartitionBDD>
{

  //#########################################################################
  //# Constructor
  Partitioning(final Class<P> clazz)
  {
    mClass = clazz;
    mPartitions = new TreeSet<P>();
  }

  Partitioning(final Class<P> clazz,
               final Collection<? extends P> partitions)
  {
    mClass = clazz;
    mPartitions = new TreeSet<P>(partitions);
  }


  //#########################################################################
  //# Simple Access
  void add(final P part)
  {
    mPartitions.add(part);
  }

  SortedSet<P> getPartitions()
  {
    return mPartitions;
  }


  //#########################################################################
  //# Algorithm
  SortedSet<P> mergePartitions(final AutomatonBDD[] automatonBDDs,
                               final BDDFactory factory)
  {
    final int count = mPartitions.size();
    final Collection<P> completed = new ArrayList<P>(count);
    while (!mPartitions.isEmpty()) {
      final Iterator<P> iter = mPartitions.iterator();
      final P part = iter.next();
      iter.remove();
      final P merged = merge(part, automatonBDDs, factory);
      if (merged == null) {
        completed.add(part);
      }
    }
    mPartitions.clear();
    mPartitions.addAll(completed);
    return mPartitions;
  }


  //#########################################################################
  //# Auxiliary Methods
  private P merge(final P part,
                  final AutomatonBDD[] automatonBDDs,
                  final BDDFactory factory)
  {
    final BitSet automata0 = part.getAutomata();
    final int numautomata0 = automata0.cardinality();
    final int size0 = part.getNodeCount();
    PartitionBDD bestcomposition = null;
    P bestcandidate = null;
    int bestsize = Integer.MAX_VALUE;
    for (final P candidate : mPartitions) {
      final BitSet automata1 = candidate.getAutomata();
      if (!automata0.intersects(automata1)) {
        continue;
      }
      final BitSet intersection = (BitSet) automata0.clone();
      intersection.and(automata1);
      if (!intersection.equals(automata0) && !intersection.equals(automata1)) {
        final int numintersect = intersection.cardinality();
        if (numautomata0 - numintersect > 1 ||
            automata1.cardinality() - numintersect > 1) {
          continue;
        }
      }
      final int size1 = candidate.getNodeCount();
      final PartitionBDD composition =
        part.compose(candidate, automatonBDDs, factory);
      final int size = composition.getNodeCount();
      if (size >= bestsize || size > 2 * (size0 + size1)) {
        composition.dispose();
        continue;
      }
      if (bestcomposition != null) {
        bestcomposition.dispose();
      }
      bestcomposition = composition;
      bestcandidate = candidate;
      bestsize = size;
    }
    if (bestcomposition != null) {
      final P result = mClass.cast(bestcomposition);
      mPartitions.remove(bestcandidate);
      part.disposeComposedBDD();
      bestcandidate.disposeComposedBDD();
      mPartitions.add(result);
      return result;
    } else {
      return null;
    }
  }


  //#########################################################################
  //# Data Members
  private final Class<P> mClass;
  private final SortedSet<P> mPartitions;

}
