//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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

import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

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
    final List<P> partitions = getFullPartition();
    final int limit = getPartitioningSizeLimit();
    if (partitions.size() <= 1) {
      // nothing ...
    } else if (limit == Integer.MAX_VALUE) {
      final P result = mergeAll(automatonBDDs);
      partitions.clear();
      partitions.add(result);
    } else if (limit > 0) {
      Collection<PartitionBDD> input = new TreeSet<>(partitions);
      Collection<PartitionBDD> output = new TreeSet<>();
      while (true) {
        final boolean canDoMore = mergeOnce(input, output, automatonBDDs);
        if (!canDoMore) {
          break;
        }
        final Collection<PartitionBDD> tmp = input;
        input = output;
        output = tmp;
      }
      partitions.clear();
      for (final PartitionBDD part : output) {
        final P castPart = castBDD(part);
        partitions.add(castPart);
      }
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
  private P mergeAll(final AutomatonBDD[] automatonBDDs)
    throws AnalysisAbortException
  {
    final BDDFactory factory = getBDDFactory();
    final List<P> partitions = getFullPartition();
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
    return castBDD(composition);
  }

  private boolean mergeOnce(final Collection<PartitionBDD> input,
                            final Collection<PartitionBDD> output,
                            final AutomatonBDD[] automatonBDDs)
    throws AnalysisAbortException
  {
    final BDDFactory factory = getBDDFactory();
    final int limit = getPartitioningSizeLimit();
    boolean composedSome = false;
    while (!input.isEmpty()) {
      checkAbort();
      final Iterator<PartitionBDD> iter = input.iterator();
      final PartitionBDD first = iter.next();
      final BitSet firstAutomata = first.getAutomata();
      iter.remove();
      PartitionBDD best = null;
      int bestSize = Integer.MAX_VALUE;
      PartitionBDD bestComposition = null;
      while (iter.hasNext()) {
        final PartitionBDD second = iter.next();
        final BitSet secondAutomata = second.getAutomata();
        if (!firstAutomata.intersects(secondAutomata)) {
          continue;
        }
        final PartitionBDD composition =
          first.compose(second, automatonBDDs, factory);
        final int size = composition.getNodeCount();
        if (size >= bestSize || size > limit) {
          composition.dispose();
          continue;
        } else if (composition.isDominant()) {
          disposAll(input);
          disposAll(output);
          output.clear();
          output.add(composition);
          return false;
        }
        bestComposition = composition;
        best = second;
        bestSize = size;
      }
      if (bestComposition == null) {
        output.add(first);
      } else {
        input.remove(best);
        first.disposeComposedBDD();
        best.disposeComposedBDD();
        input.add(bestComposition);
        composedSome = true;
      }
    }
    return composedSome && output.size() > 1;
  }

  private void disposAll(final Collection<? extends PartitionBDD> parts)
  {
    for (final PartitionBDD part : parts) {
      part.disposeComposedBDD();
    }
  }

}
