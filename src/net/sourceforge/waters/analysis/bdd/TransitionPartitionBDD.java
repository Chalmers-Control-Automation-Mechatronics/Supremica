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

import java.util.BitSet;
import java.util.Map;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BDDPairing;
import net.sf.javabdd.BDDVarSet;

import net.sourceforge.waters.model.des.EventProxy;


/**
 * @author Robi Malik
 */

class TransitionPartitionBDD
  extends PartitionBDD
{

  //#########################################################################
  //# Constructor
  TransitionPartitionBDD(final EventBDD eventBDD)
  {
    super(eventBDD.getEvent(),
          eventBDD.getTransitionsBDD(),
          eventBDD.getSynchronisedAutomata());
  }

  TransitionPartitionBDD(final TransitionPartitionBDD part1,
                         final TransitionPartitionBDD part2,
                         final BDD bdd)
  {
    super(part1, part2, bdd);
  }


  //#########################################################################
  //# Overrides for Abstract Base Class PartitionBDD
  /**
   * Composes this partition BDD with another.
   * @param  part          The BDD this BDD is composed with.
   * @param  automatonBDDs Array of automaton BDDs defining the model.
   * @param  factory       The BDD factory used to construct BDDs.
   * @return The new partition BDD.
   */
  @Override
  TransitionPartitionBDD compose(final PartitionBDD part,
                                 final AutomatonBDD[] automatonBDDs,
                                 final BDDFactory factory)
  {
    return compose((TransitionPartitionBDD) part, automatonBDDs, factory);
  }

  /**
   * Composes this partition BDD with another transition partition BDD.
   * @param part          The BDD this BDD is composed with.
   * @param automatonBDDs Array of automaton BDDs defining the model.
   * @param factory       The BDD factory used to construct BDDs.
   * @return The new partition BDD.
   */
  TransitionPartitionBDD compose(final TransitionPartitionBDD part,
                                 final AutomatonBDD[] automatonBDDs,
                                 final BDDFactory factory)
  {
    final BDD bdd1 = getBDD().id();
    final BDD bdd2 = part.getBDD().id();
    final BitSet automata1 = getAutomata();
    final BitSet automata2 = part.getAutomata();
    if (!automata1.equals(automata2)) {
      final BitSet tester = (BitSet) automata2.clone();
      tester.andNot(automata1);
      includeUnchanged(bdd1, tester, automatonBDDs, factory);
      tester.clear();
      tester.or(automata1);
      tester.andNot(automata2);
      includeUnchanged(bdd2, tester, automatonBDDs, factory);
    }
    final BDD bdd = bdd1.orWith(bdd2);
    return new TransitionPartitionBDD(this, part, bdd);
  }


  //#########################################################################
  //# Specific Methods
  @SuppressWarnings("unchecked")
  Map<EventProxy,TransitionPartitionBDD> getTransitionComponents()
  {
    final Map<?,?> precast = super.getComponents();
    return (Map<EventProxy,TransitionPartitionBDD>) precast;
  }

  BDDVarSet getCurrentStateCube()
  {
    return mCurrentStateCube;
  }

  BDDVarSet getNextStateCube()
  {
    return mNextStateCube;
  }

  BDDPairing getCurrentToNext()
  {
    return mCurrentToNext;
  }

  BDDPairing getNextToCurrent()
  {
    return mNextToCurrent;
  }

  void buildForwardCubes(final AutomatonBDD[] automatonBDDs,
                         final BDDFactory factory)
  {
    clearBackwardCubes();
    if (mCurrentStateCube == null) {
      mCurrentStateCube = factory.emptySet();
      mNextToCurrent = factory.makePair();
      final BitSet automata = getAutomata();
      for (int index = automata.nextSetBit(0);
           index >= 0;
           index = automata.nextSetBit(index + 1)) {
        final AutomatonBDD autbdd = automatonBDDs[index];
        autbdd.buildForwardCubes(mCurrentStateCube, mNextToCurrent, factory);
      }
    }
  }

  void buildBackwardCubes(final AutomatonBDD[] automatonBDDs,
                          final BDDFactory factory)
  {
    clearForwardCubes();
    if (mNextStateCube == null) {
      mNextStateCube = factory.emptySet();
      mCurrentToNext = factory.makePair();
      final BitSet automata = getAutomata();
      for (int index = automata.nextSetBit(0);
           index >= 0;
           index = automata.nextSetBit(index + 1)) {
        final AutomatonBDD autbdd = automatonBDDs[index];
        autbdd.buildBackwardCubes(mNextStateCube, mCurrentToNext, factory);
      }
      for (final TransitionPartitionBDD part :
             getTransitionComponents().values()) {
        part.buildBackwardCubes(automatonBDDs, factory);
      }
    }
  }

  void clearForwardCubes()
  {
    if (mCurrentStateCube != null) {
      mCurrentStateCube.free();
      mCurrentStateCube = null;
      mNextToCurrent = null;
    }
  }

  void clearBackwardCubes()
  {
    if (mNextStateCube != null) {
      mNextStateCube.free();
      mNextStateCube = null;
      mCurrentToNext = null;
    }
  }

  BDD getNonDeadlockBDD(final AutomatonBDD[] automatonBDDs,
                        final BDDFactory factory)
  {
    BDD bdd = getBDD();
    if (bdd.isZero()) {
      return factory.one();
    }
    final BitSet automata = getAutomata();
    BDD unchanged = factory.one();
    for (int a = automatonBDDs.length - 1; a >= 0; a--) {
      if (automata.get(a)) {
        final AutomatonBDD autBDD = automatonBDDs[a];
        unchanged = autBDD.includeUnchangedBDD(unchanged, factory);
      }
    }
    final BDD altBDD = bdd.apply(unchanged, BDDFactory.diff);
    unchanged.free();
    bdd = installSmallerBDD(altBDD);
    buildBackwardCubes(automatonBDDs, factory);
    final BDD nondeadlock = bdd.exist(mNextStateCube);
    return nondeadlock;
  }


  //#########################################################################
  //# Auxiliary Methods
  private void includeUnchanged(final BDD bdd,
                                final BitSet tester,
                                final AutomatonBDD[] automatonBDDs,
                                final BDDFactory factory)
  {
    final BDD unchanged = factory.one();
    for (int index = tester.nextSetBit(0);
         index >= 0;
         index = tester.nextSetBit(index + 1)) {
      final AutomatonBDD autbdd = automatonBDDs[index];
      autbdd.includeUnchangedBDD(unchanged, factory);
    }
    bdd.andWith(unchanged);
  }


  //#########################################################################
  //# Data Members
  private BDDVarSet mCurrentStateCube;
  private BDDVarSet mNextStateCube;
  private BDDPairing mCurrentToNext;
  private BDDPairing mNextToCurrent;

}
