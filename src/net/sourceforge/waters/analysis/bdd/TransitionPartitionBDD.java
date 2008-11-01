//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.bdd
//# CLASS:   TransitionPartitionBDD
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.bdd;

import java.util.BitSet;
import java.util.Map;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BDDPairing;
import net.sf.javabdd.BDDVarSet;

import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.unchecked.Casting;


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
  TransitionPartitionBDD compose(final PartitionBDD part,
                                 final AutomatonBDD[] automatonBDDs,
                                 final BDDFactory factory)
  {
    return compose((TransitionPartitionBDD) part, automatonBDDs, factory);
  }

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
  Map<EventProxy,TransitionPartitionBDD> getTransitionComponents()
  {
    return Casting.toMap(super.getComponents());
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
      mCurrentStateCube = null;
      mCurrentToNext = null;
    }
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
