//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.bdd
//# CLASS:   PartitionBDD
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.bdd;

import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;

import net.sourceforge.waters.model.des.EventProxy;


/**
 * @author Robi Malik
 */

abstract class PartitionBDD
  implements Comparable<PartitionBDD>
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
   * Free the BDD representing this partition, if it is an atomic parition
   * member.
   */
  void disposeComposedBDD()
  {
    if (!mIsAtomic) {
      dispose();
    }
  }


  //#########################################################################
  //# Interface java.util.Comparable
  public int compareTo(final PartitionBDD part)
  {
    if (mNumAutomata != part.mNumAutomata) {
      return mNumAutomata - part.mNumAutomata;
    } else if (getNodeCount() != part.getNodeCount()) {
      return getNodeCount() - part.getNodeCount();
    } else if (mComponents.size() != part.mComponents.size()) {
      return mComponents.size() - part.mComponents.size();
    } else {
      final Set<EventProxy> events1 = mComponents.keySet();
      final EventProxy event1 = Collections.min(events1);
      final Set<EventProxy> events2 = part.mComponents.keySet();
      final EventProxy event2 = Collections.min(events2);
      return event1.compareTo(event2); // event sets should be disjoint ...
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
  //# Data Members
  private BDD mBDD;
  private int mNodeCount;
  private final Map<EventProxy,PartitionBDD> mComponents;
  private final BitSet mAutomata;
  private final int mNumAutomata;
  private final boolean mIsAtomic;

}
