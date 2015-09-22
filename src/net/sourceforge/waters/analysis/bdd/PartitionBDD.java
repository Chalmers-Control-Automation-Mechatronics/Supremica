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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;

import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.des.EventProxy;


/**
 * @author Robi Malik
 */

abstract class PartitionBDD
  implements Cloneable, Comparable<PartitionBDD>
{

  //#########################################################################
  //# Constructors
  /**
   * Creates an atomic partition member, representing the BDD for a single
   * event.
   */
  PartitionBDD(final EventProxy event, final BDD bdd, final BitSet automata)
  {
    mBDD = bdd;
    mNodeCount = -1;
    mAveragePosition = Double.NaN;
    mComponents = Collections.singletonMap(event, this);
    mAutomata = automata;
    mNumAutomata = automata.cardinality();
    mIsAtomic = true;
  }

  /**
   * Creates a new composed partition member by combining two others.
   */
  PartitionBDD(final PartitionBDD part1,
               final PartitionBDD part2,
               final BDD bdd)
  {
    mBDD = bdd;
    mNodeCount = -1;
    mAveragePosition = Double.NaN;
    final Map<EventProxy,PartitionBDD> comp1 = part1.mComponents;
    final Map<EventProxy,PartitionBDD> comp2 = part2.mComponents;
    final int size = comp1.size() + comp2.size();
    mComponents = new HashMap<EventProxy,PartitionBDD>(size);
    mComponents.putAll(comp1);
    mComponents.putAll(comp2);
    mAutomata = (BitSet) part1.getAutomata().clone();
    mAutomata.or(part2.getAutomata());
    mNumAutomata = mAutomata.cardinality();
    mIsAtomic = false;
  }


  //#########################################################################
  //# Cleaning up
  /**
   * Free all the BDDs associated with this partition.
   */
  void dispose()
  {
    mBDD.free();
    mBDD = null;
  }

  /**
   * Free the BDD representing this partition, if it is an atomic partition
   * member.
   */
  void disposeComposedBDD()
  {
    if (!mIsAtomic) {
      dispose();
    }
  }


  //#########################################################################
  //# Interface java.lang.Cloneable
  @Override
  public PartitionBDD clone()
  {
    try {
      final PartitionBDD result = (PartitionBDD) super.clone();
      result.mBDD = mBDD.id();
      return result;
    } catch (final CloneNotSupportedException exception) {
      throw new WatersRuntimeException(exception);
    }
  }


  //#########################################################################
  //# Interface java.util.Comparable
  public int compareTo(final PartitionBDD part)
  {
    final double pos1 = computeAveragePosition();
    final double pos2 = part.computeAveragePosition();
    if (pos1 != pos2) {
      // Start iteration at lower end in variable ordering.
      return pos1 < pos2 ? 1 : -1;
    } else if (mNumAutomata != part.mNumAutomata) {
      return mNumAutomata - part.mNumAutomata;
    } else if (mComponents.size() != part.mComponents.size()) {
      return mComponents.size() - part.mComponents.size();
    } else if (getNodeCount() != part.getNodeCount()) {
      return getNodeCount() - part.getNodeCount();
    } else {
      final Queue<EventProxy> events1 =
        new PriorityQueue<EventProxy>(mComponents.keySet());
      final Queue<EventProxy> events2 =
        new PriorityQueue<EventProxy>(part.mComponents.keySet());
      EventProxy event1 = null;
      EventProxy event2 = null;
      while (event1 == event2 && !events1.isEmpty()) {
        event1 = events1.remove();
        event2 = events2.remove();
      }
      return event1.compareTo(event2);
    }
  }


  //#########################################################################
  //# Simple Access
  BDD getBDD()
  {
    return mBDD;
  }

  int getNodeCount()
  {
    if (mNodeCount < 0) {
      mNodeCount = mBDD.nodeCount();
    }
    return mNodeCount;
  }

  Map<EventProxy,PartitionBDD> getComponents()
  {
    return mComponents;
  }

  BitSet getAutomata()
  {
    return mAutomata;
  }

  boolean dependsOn(final AutomatonBDD autBDD)
  {
    final int a = autBDD.getAutomatonIndex();
    return mAutomata.get(a);
  }

  boolean isAtomic()
  {
    return mIsAtomic;
  }


  //#########################################################################
  //# Specific Methods Provided by Subclasses
  abstract PartitionBDD compose(final PartitionBDD part,
                                final AutomatonBDD[] automatonBDDs,
                                final BDDFactory factory);


  //#########################################################################
  //# Simplification
  BDD installSmallerBDD(final BDD altBDD)
  {
    if (mBDD.equals(altBDD)) {
      altBDD.free();
      return mBDD;
    }
    final int nodecount = getNodeCount();
    final int altnodecount = altBDD.nodeCount();
    if (nodecount <= altnodecount) {
      altBDD.free();
      return mBDD;
    }
    mBDD.free();
    mBDD = altBDD;
    mNodeCount = altnodecount;
    return mBDD;
  }


  //#########################################################################
  //# Debugging
  @Override
  public String toString()
  {
    final List<EventProxy> events =
      new ArrayList<EventProxy>(mComponents.keySet());
    Collections.sort(events);
    final StringBuilder buffer = new StringBuilder();
    buffer.append(ProxyTools.getShortClassName(this));
    buffer.append('{');
    boolean first = true;
    for (final EventProxy event : events) {
      if (first) {
        first = false;
      } else {
        buffer.append(',');
      }
      buffer.append(event.getName());
    }
    buffer.append('}');
    return buffer.toString();
  }


  //#########################################################################
  //# Auxiliary Methods
  private double computeAveragePosition()
  {
    if (mAveragePosition == Double.NaN) {
      int sum = 0;
      int count = 0;
      for (final PartitionBDD part : mComponents.values()) {
        final BitSet automata = part.getAutomata();
        for (int bit = automata.nextSetBit(0);
             bit >= 0;
             bit = automata.nextSetBit(bit + 1)) {
          sum += bit;
          count++;
        }
      }
      if (count == 0) {
        mAveragePosition = -1.0;
      } else {
        mAveragePosition = (double) sum / (double) count;
      }
    }
    return mAveragePosition;
  }


  //#########################################################################
  //# Data Members
  private BDD mBDD;
  private int mNodeCount;
  private double mAveragePosition;
  private final Map<EventProxy,PartitionBDD> mComponents;
  private final BitSet mAutomata;
  private final int mNumAutomata;
  private final boolean mIsAtomic;

}








