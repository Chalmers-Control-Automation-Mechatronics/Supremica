//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.bdd
//# CLASS:   EventBDD
//###########################################################################
//# $Id: EventBDD.java,v 1.1 2007-11-02 00:30:37 robi Exp $
//###########################################################################

package net.sourceforge.waters.analysis.bdd;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BDDVarSet;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * @author Robi Malik
 */

abstract class EventBDD
  implements Comparable<EventBDD>
{

  //#########################################################################
  //# Constructor
  EventBDD(final EventProxy event,
           final int numautomata,
           final BDDFactory factory)
  {
    mEvent = event;
    mSynchronisedAutomata = new BitSet(numautomata);
    mTransitionsBDD = factory.one();
    mCurrentAutomaton = null;
    mIsOnlySelfloops = true;
    mCurrentAutomatonBDD = null;
  }


  //#########################################################################
  //# Interface java.util.Comparable
  public int compareTo(final EventBDD eventbdd)
  {
    final EventKind kind0 = getEventKind();
    final EventKind kind1 = eventbdd.getEventKind();
    if (kind0 != kind1) {
      return kind0 == EventKind.UNCONTROLLABLE ? -1 : 1;
    } else {
      return mEvent.compareTo(eventbdd.mEvent);
    }
  }


  //#########################################################################
  //# Simple Access
  EventProxy getEvent()
  {
    return mEvent;
  }

  BitSet getSynchronisedAutomata()
  {
    return mSynchronisedAutomata;
  }

  AutomatonBDD getCurrentAutomaton()
  {
    return mCurrentAutomaton;
  }

  BDD getTransitionsBDD()
  {
    if (mTransitionsBDD != null) {
      if (mIsOnlySelfloops || mTransitionsBDD.isZero()) {
        mTransitionsBDD.free();
        mTransitionsBDD = null;
      }
    }
    return mTransitionsBDD;
  }


  //#########################################################################
  //# Constructing BDDs
  void startAutomaton(final AutomatonBDD autbdd, final BDDFactory factory)
  {
    mCurrentAutomaton = autbdd;
    if (!mTransitionsBDD.isZero()) {
      mCurrentAutomatonBDD = factory.zero();
      final AutomatonProxy aut = autbdd.getAutomaton();
      final int numstates = aut.getStates().size();
      mCurrentAutomatonSelfloops = new HashSet<StateProxy>(numstates);
    }
  }

  void includeTransition(final TransitionProxy trans,
                         final BDDFactory factory)
  {
    if (mCurrentAutomatonBDD != null) {
      final BDD transbdd = mCurrentAutomaton.getTransitionBDD(trans, factory);
      mCurrentAutomatonBDD.orWith(transbdd);
      final StateProxy source = trans.getSource();
      final StateProxy target = trans.getTarget();
      if (source != target) {
        mIsOnlySelfloops = false;
        mCurrentAutomatonSelfloops = null;
      } else if (mCurrentAutomatonSelfloops != null) {
        mCurrentAutomatonSelfloops.add(source);
      }
    }
  }

  void finishAutomaton(final BDDFactory factory)
  {
    try {
      if (mCurrentAutomatonBDD != null) {
        if (mCurrentAutomatonSelfloops != null) {
          final AutomatonProxy aut = mCurrentAutomaton.getAutomaton();
          final int numstates = aut.getStates().size();
          final int numselfloops = mCurrentAutomatonSelfloops.size();
          mCurrentAutomatonSelfloops = null;
          if (numstates == numselfloops) {
            mCurrentAutomatonBDD.free();
            return;
          } else if (mCurrentAutomatonBDD.isZero()) {
            mTransitionsBDD.free();
            mTransitionsBDD = mCurrentAutomatonBDD;
            mSynchronisedAutomata.clear();
          } else {
            final BDDVarSet cube = mCurrentAutomaton.getNextStateCube(factory);
            final BDD projected = mCurrentAutomatonBDD.exist(cube);
            mCurrentAutomatonBDD.free();
            cube.free();
            mTransitionsBDD.andWith(projected);
          }          
        } else {
          final int index = mCurrentAutomaton.getBitIndex();
          mSynchronisedAutomata.set(index);
          mTransitionsBDD.andWith(mCurrentAutomatonBDD);
        }
      }
    } finally {
      mCurrentAutomaton = null;
      mCurrentAutomatonBDD = null;
    }
  }


  //#########################################################################
  //# Provided by Subclasses
  abstract EventKind getEventKind();

  BDD getControllabilityConditionBDD()
  {
    return null;
  }

  BitSet getControllabilityTestedAutomata()
  {
    final int size = mSynchronisedAutomata.size();
    final BitSet result = new BitSet(size);
    return result;
  }

  void disposeControllabilityConditionBDD()
  {
  }


  //#########################################################################
  //# Data Members
  private final EventProxy mEvent;
  private final BitSet mSynchronisedAutomata;

  private BDD mTransitionsBDD;
  private boolean mIsOnlySelfloops;
  private AutomatonBDD mCurrentAutomaton;
  private BDD mCurrentAutomatonBDD;
  private Set<StateProxy> mCurrentAutomatonSelfloops;

}
