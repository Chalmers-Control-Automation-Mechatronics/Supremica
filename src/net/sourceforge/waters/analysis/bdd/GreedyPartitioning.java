//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.analysis.bdd;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.sf.javabdd.BDDFactory;
import net.sourceforge.waters.model.analysis.AnalysisAbortException;


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
  void merge(final AutomatonBDD[] automatonBDDs)
    throws AnalysisAbortException
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
          checkAbort();
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
        checkAbort();
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
  boolean isStrictBFS()
  {
    return true;
  }

  @Override
  List<P> startIteration()
  {
    return getFullPartition();
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
