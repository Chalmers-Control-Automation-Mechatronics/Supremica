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

import gnu.trove.set.hash.THashSet;

import java.util.BitSet;
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
    mSynchronisedAutomataBitSet = new BitSet(numautomata);
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
    return mSynchronisedAutomataBitSet;
  }

  AutomatonBDD getCurrentAutomaton()
  {
    return mCurrentAutomaton;
  }

  BDD getTransitionsBDD()
  {
    if (mTransitionsBDD != null &&
        (mIsOnlySelfloops || mTransitionsBDD.isZero())) {
      mTransitionsBDD.free();
      mTransitionsBDD = null;
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
      mCurrentAutomatonSelfloops = new THashSet<StateProxy>(numstates);
      final int numcodes = autbdd.getNumberOfStateCodes();
      mCurrentAutomatonDeterministicSuccessors = new StateProxy[numcodes];
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
      if (mCurrentAutomatonDeterministicSuccessors != null) {
        final int code = mCurrentAutomaton.getStateCode(source);
        final StateProxy old = mCurrentAutomatonDeterministicSuccessors[code];
        if (old == null) {
          mCurrentAutomatonDeterministicSuccessors[code] = target;
        } else if (old == target) {
          // skip ...
        } else {
          mCurrentAutomaton.setNondeterministic(mEvent);
          mCurrentAutomatonDeterministicSuccessors = null;
        }
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
            mSynchronisedAutomataBitSet.clear();
          } else {
            final BDDVarSet cube = mCurrentAutomaton.getNextStateCube(factory);
            final BDD projected = mCurrentAutomatonBDD.exist(cube);
            mCurrentAutomatonBDD.free();
            cube.free();
            mTransitionsBDD.andWith(projected);
          }
        } else {
          final int index = mCurrentAutomaton.getAutomatonIndex();
          mSynchronisedAutomataBitSet.set(index);
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
    final int size = mSynchronisedAutomataBitSet.size();
    final BitSet result = new BitSet(size);
    return result;
  }

  void disposeControllabilityConditionBDD()
  {
  }


  //#########################################################################
  //# Data Members
  private final EventProxy mEvent;
  private final BitSet mSynchronisedAutomataBitSet;

  private BDD mTransitionsBDD;
  private boolean mIsOnlySelfloops;
  private AutomatonBDD mCurrentAutomaton;
  private BDD mCurrentAutomatonBDD;
  private Set<StateProxy> mCurrentAutomatonSelfloops;
  private StateProxy[] mCurrentAutomatonDeterministicSuccessors;

}
