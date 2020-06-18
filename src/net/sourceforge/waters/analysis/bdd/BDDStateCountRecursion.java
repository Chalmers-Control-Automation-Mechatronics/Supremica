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

import java.util.ArrayList;
import java.util.List;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;

import gnu.trove.map.hash.TObjectDoubleHashMap;


/**
 * A utility class to determine the number of synchronous product states
 * from a state set given as a {@link BDD}.
 *
 * @author Robi Malik
 */

class BDDStateCountRecursion
{

  //#########################################################################
  //# Constructors
  BDDStateCountRecursion(final BDDFactory factory,
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
    double result = mCache.get(bdd);
    if (result == 0.0) {
      final int numbits = autBDD.getNumberOfStateBits();
      result = getOpenCount(bdd, autindex, 0, 0, numbits);
      mCache.put(bdd, result);
    }
    return factor * result;
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
    if (truemask >= firstuse2) { // truemask is the lowest possible value.
      if ((truemask & 1) != 0) {
        return 0.0;
      } else if ((falsemask & 1) == 0) {
        openbits--;
      }
      return getClosedCount(bdd, autindex, openbits);
    }
    final int numbits = autBDD.getNumberOfStateBits();
    final int mask = (1 << numbits) - 1;
    final int maxcode = mask & ~falsemask;
    if (maxcode < firstuse2) {
      return getClosedCount(bdd, autindex, openbits);
    }
    final int varindex;
    final boolean newvar;
    if (bdd.isOne()) {
      varindex = -1;
      newvar = true;
    } else {
      varindex = bdd.var();
      newvar = (mAutomatonBDDbyVarIndex[varindex] != autBDD);
    }
    if (newvar) {
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
    final BDD low = bdd.low();
    final BDD high = bdd.high();
    openbits--;
    return getClosedCount(low, autindex, openbits) +
           getClosedCount(high, autindex, openbits);
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
