//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.bdd
//# CLASS:   BDDStateCounter
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.bdd;

import gnu.trove.TObjectDoubleHashMap;
import java.util.ArrayList;
import java.util.List;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;


/**
 * A utility class to determine the number of synchronous product states
 * from a state set given as a {@link BDD}.
 *
 * @author Robi Malik
 */

class BDDStateCounter
{

  //#########################################################################
  //# Constructors
  BDDStateCounter(final BDDFactory factory,
                  final AutomatonBDD[] autBDDbyVarIndex)
  {
    mBDDFactory = factory;
    mAutomatonBDDbyVarIndex = autBDDbyVarIndex;
    final int numvars = mBDDFactory.varNum();
    mOrderedAutomatonBDDs = new ArrayList<AutomatonBDD>(numvars);
    AutomatonBDD prev = null;
    for (int l = 0; l < numvars; l++) {
      final int varindex = mBDDFactory.level2Var(l);
      final AutomatonBDD autBDD = mAutomatonBDDbyVarIndex[varindex];
      if (autBDD != prev) {
        mOrderedAutomatonBDDs.add(autBDD);
        prev = autBDD;
      }
    }
    mCache = new TObjectDoubleHashMap<BDD>();
  }


  //#########################################################################
  //# Invocation
  public double count(final BDD bdd)
  {
    if (mOrderedAutomatonBDDs.isEmpty()) {
      return 1.0;
    } else {
      return getOpenCount(bdd, 0);
    }
  }


  //#########################################################################
  //# Algorithm
  private double getOpenCount(final BDD bdd, int autindex)
  {
    if (bdd.isZero()) {
      return 0.0;
    } else if (bdd.isOne()) {
      return getStateProduct(autindex);
    }
    final int varindex = bdd.var();
    AutomatonBDD autBDD = mOrderedAutomatonBDDs.get(autindex);
    double factor = 1.0;
    while (mAutomatonBDDbyVarIndex[varindex] != autBDD) {
      factor *= autBDD.getNumberOfEncodedStates();
      autindex++;
      autBDD = mOrderedAutomatonBDDs.get(autindex);
    }
    final int numbits = autBDD.getNumberOfStateBits();
    return factor * getOpenCount(bdd, autindex, 0, 0, numbits);
  }

  private double getOpenCount(final BDD bdd,
                              final int autindex,
                              final int falsemask,
                              final int truemask,
                              int openbits)
  {
    if (bdd.isZero()) {
      return 0.0;
    }
    final AutomatonBDD autBDD = mOrderedAutomatonBDDs.get(autindex);
    final int firstuse2 = autBDD.getFirstUse2();
    if (truemask >= firstuse2) {
      if (((truemask | falsemask) & 1) == 0) {
        openbits--;
      }
      return getClosedCount(bdd, autindex, openbits);
    }
    final int numbits = autBDD.getNumberOfStateBits();
    final int mask = (1 << numbits) - 1;
    final int maxcode = mask & ~falsemask;
    if (maxcode < firstuse2) {
      return getClosedCount(bdd, autindex, openbits);
    } else if (bdd.isOne()) {
      final int bothmask = falsemask | truemask;
      int bit = numbits - 1;
      int bitmask = 1 << bit;
      while ((bitmask & bothmask) != 0) {
        bit--;
        bitmask = 1 << bit;
      }
      openbits--;
      return
        getOpenCount(bdd, autindex, falsemask | bitmask, truemask, openbits) +
        getOpenCount(bdd, autindex, falsemask, truemask | bitmask, openbits);
    }
    final BDD low = bdd.low();
    final BDD high = bdd.high();
    final int varindex = bdd.var();
    final int bitindex = autBDD.getBitIndex(varindex);
    final int bitmask = 1 << bitindex;
    openbits--;
    return
      getOpenCount(low, autindex, falsemask | bitmask, truemask, openbits) +
      getOpenCount(high, autindex, falsemask, truemask | bitmask, openbits);
  }

  private double getClosedCount(final BDD bdd,
                                final int autindex,
                                int openbits)
  {
    if (bdd.isZero()) {
      return 0.0;
    } else if (bdd.isOne()) {
      return (1 << openbits) * getStateProduct(autindex + 1);
    }
    final int varindex = bdd.var();
    final AutomatonBDD autBDD = mOrderedAutomatonBDDs.get(autindex);
    if (mAutomatonBDDbyVarIndex[varindex] != autBDD) {
      return (1 << openbits) * getOpenCount(bdd, autindex + 1);
    }
    final double cached = mCache.get(bdd);
    if (cached != 0.0) {
      return cached;
    }
    final BDD low = bdd.low();
    final BDD high = bdd.high();
    openbits--;
    final double result = getClosedCount(low, autindex, openbits) +
                          getClosedCount(high, autindex, openbits);
    mCache.put(bdd, result);
    return result;
  }

  private double getStateProduct(final int autindex)
  {
    double result = 1.0;
    for (int a = autindex; a < mOrderedAutomatonBDDs.size(); a++) {
      final AutomatonBDD autBDD = mOrderedAutomatonBDDs.get(a);
      result *= autBDD.getNumberOfEncodedStates();
    }
    return result;
  }


  //#########################################################################
  //# Data Members
  private final BDDFactory mBDDFactory;
  private final AutomatonBDD[] mAutomatonBDDbyVarIndex;
  private final List<AutomatonBDD> mOrderedAutomatonBDDs;
  private final TObjectDoubleHashMap<BDD> mCache;

}
