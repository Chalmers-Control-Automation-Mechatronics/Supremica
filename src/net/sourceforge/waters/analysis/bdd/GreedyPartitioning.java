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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import net.sf.javabdd.BDDFactory;


/**
 * @author Robi Malik
 */

class GreedyPartitioning<P extends PartitionBDD>
  extends Partitioning<P>
{

  //#########################################################################
  //# Constructor
  GreedyPartitioning(final BDDFactory factory,
                     final Class<P> clazz,
                     final int partitioningSizeLimit)
  {
    super(factory, clazz, partitioningSizeLimit);
  }


  //#########################################################################
  //# Algorithm
  @Override
  void setUpAndMerge(final AutomatonBDD[] automatonBDDs)
  {
    final BDDFactory factory = getBDDFactory();
    final List<P> partitions = getFullPartition();
    final int limit = getPartitioningSizeLimit();
    if (partitions.isEmpty()) {
      // nothing ...
    } else if (limit == Integer.MAX_VALUE) {
      PartitionBDD composition = null;
      for (final P part : partitions) {
        if (composition == null) {
          composition = part.clone();
        } else {
          final PartitionBDD next =
            composition.compose(part, automatonBDDs, factory);
          composition.dispose();
          composition = next;
        }
      }
      partitions.clear();
      final P result = castBDD(composition);
      partitions.add(result);
    } else if (limit > 0) {
      final int count = partitions.size();
      final Collection<P> completed = new ArrayList<P>(count);
      while (!partitions.isEmpty()) {
        final Iterator<P> iter = partitions.iterator();
        final P part = iter.next();
        iter.remove();
        final P merged = merge(part, automatonBDDs);
        if (merged == null) {
          completed.add(part);
        }
      }
      partitions.clear();
      partitions.addAll(completed);
      Collections.sort(partitions);
    }
  }

  @Override
  List<P> nextGroup(final boolean stable)
  {
    if (stable) {
      return null;
    } else {
      return getFullPartition();
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private P merge(final P part, final AutomatonBDD[] automatonBDDs)
  {
    final BDDFactory factory = getBDDFactory();
    final List<P> partitions = getFullPartition();
    final int limit = getPartitioningSizeLimit();
    final BitSet automata0 = part.getAutomata();
    PartitionBDD bestcomposition = null;
    P bestcandidate = null;
    int bestsize = Integer.MAX_VALUE;
    for (final P candidate : partitions) {
      final BitSet automata1 = candidate.getAutomata();
      if (!automata0.intersects(automata1)) {
        continue;
      }
      final PartitionBDD composition =
        part.compose(candidate, automatonBDDs, factory);
      final int size = composition.getNodeCount();
      if (size >= bestsize || size > limit) {
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
      final P result = castBDD(bestcomposition);
      partitions.remove(bestcandidate);
      part.disposeComposedBDD();
      bestcandidate.disposeComposedBDD();
      partitions.add(result);
      return result;
    } else {
      return null;
    }
  }

}
