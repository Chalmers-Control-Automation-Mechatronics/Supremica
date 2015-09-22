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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BDDPairing;
import net.sf.javabdd.BDDVarSet;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.AutomatonTools;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;


/**
 * @author Robi Malik
 */

class AutomatonBDD
{

  //#########################################################################
  //# Constructor
  AutomatonBDD(final AutomatonProxy aut,
               final ComponentKind kind,
               final int autindex,
               final BDDFactory factory)
  {
    mAutomaton = aut;
    mKind = kind;
    mAutomatonIndex = autindex;
    final Collection<StateProxy> states = aut.getStates();
    final int numstates = states.size();
    mStateMap = new HashMap<StateProxy,StateCode>(numstates);
    final List<StateCode> reached = new ArrayList<StateCode>(numstates);
    for (final StateProxy state : states) {
      final StateCode code = new StateCode(state);
      mStateMap.put(state, code);
      if (state.isInitial()) {
        reached.add(code);
      }
    }
    for (final TransitionProxy trans : aut.getTransitions()) {
      final StateProxy source = trans.getSource();
      final StateCode code = mStateMap.get(source);
      code.addTransition(trans);
    }
    for (int index = 0; index < reached.size(); index++) {
      final StateCode code = reached.get(index);
      final Collection<StateCode> successors = code.removeSuccessorStates();
      for (final StateCode succ : successors) {
        if (succ.setReachable()) {
          reached.add(succ);
        }
      }
    }
    final int numreached = reached.size();
    if (numreached < numstates) {
      final Iterator<Map.Entry<StateProxy,StateCode>> iter =
        mStateMap.entrySet().iterator();
      while (iter.hasNext()) {
        final Map.Entry<StateProxy,StateCode> entry = iter.next();
        final StateCode code = entry.getValue();
        if (!code.isReachable()) {
          iter.remove();
        }
      }
    }
    mNumberOfBits = AutomatonTools.log2(numreached);
    mFirstVariableIndex = factory.varNum();
    mStateArray = new StateCode[1 << mNumberOfBits];
    if (mNumberOfBits > 0) {
      factory.setVarNum(mFirstVariableIndex + 2 * mNumberOfBits);
    }
    final int power2 = 1 << mNumberOfBits;
    mFirstUse2 = numreached + numreached - power2;
    int index = 0;
    for (final StateCode code : reached) {
      if (index >= mFirstUse2) {
        code.setStateCode(index, true);
        mStateArray[index++] = code;
        mStateArray[index++] = code;
      } else {
        code.setStateCode(index);
        mStateArray[index] = code;
        index++;
      }
    }
    mNondeterministicEvents = null;
  }


  //#########################################################################
  //# Simple Access
  AutomatonProxy getAutomaton()
  {
    return mAutomaton;
  }

  ComponentKind getKind()
  {
    return mKind;
  }

  int getFirstVariableIndex()
  {
    return mFirstVariableIndex;
  }

  int getLastVariableIndex()
  {
    return mFirstVariableIndex + 2 * mNumberOfBits - 1;
  }

  int getBitIndex(final int varindex)
  {
    return (varindex - mFirstVariableIndex) >> 1;
  }

  boolean isNextStateVariable(final int varindex)
  {
    return ((varindex - mFirstVariableIndex) & 1) != 0;
  }

  int getFirstUse2()
  {
    return mFirstUse2;
  }

  int getAutomatonIndex()
  {
    return mAutomatonIndex;
  }

  int getNumberOfEncodedStates()
  {
    return mStateMap.size();
  }

  int getNumberOfStateBits()
  {
    return mNumberOfBits;
  }

  int getNumberOfStateCodes()
  {
    return 1 << mNumberOfBits;
  }

  boolean isDeterministic()
  {
    return mNondeterministicEvents == null;
  }

  boolean isDeterministic(final EventProxy event)
  {
    if (mNondeterministicEvents == null) {
      return true;
    } else {
      return !mNondeterministicEvents.contains(event);
    }
  }

  void setNondeterministic(final EventProxy event)
  {
    if (mNondeterministicEvents == null) {
      mNondeterministicEvents = new THashSet<EventProxy>();
    }
    mNondeterministicEvents.add(event);
  }


  //#########################################################################
  //# Constructing BDDs
  BDD createInitialStateBDD(final BDDFactory factory)
  {
    final BDD result = factory.zero();
    final Collection<StateProxy> states = mAutomaton.getStates();
    int count = 0;
    for (final StateProxy state : states) {
      if (state.isInitial()) {
        if (++count > 1) {
          setNondeterministic(null);
        }
        final BDD statebdd = getStateBDD(state, factory);
        result.orWith(statebdd);
      }
    }
    return result;
  }

  BDD getMarkedStateBDD(final EventProxy prop, final BDDFactory factory)
  {
    final Collection<EventProxy> alphabet = mAutomaton.getEvents();
    if (alphabet.contains(prop)) {
      final BDD result = factory.zero();
      final Collection<StateProxy> states = mAutomaton.getStates();
      for (final StateProxy state : states) {
        final Collection<EventProxy> props = state.getPropositions();
        if (props.contains(prop)) {
          final BDD statebdd = getStateBDD(state, factory);
          result.orWith(statebdd);
        }
      }
      return result;
    } else {
      return null;
    }
  }

  BDDVarSet getCurrentStateCube(final BDDFactory factory)
  {
    final BDDVarSet result = factory.emptySet();
    buildForwardCubes(result, null, factory);
    return result;
  }

  BDDVarSet getNextStateCube(final BDDFactory factory)
  {
    final BDDVarSet result = factory.emptySet();
    buildBackwardCubes(result, null, factory);
    return result;
  }

  void buildForwardCubes(final BDDVarSet currentStateCube,
                         final BDDPairing nextToCurrent,
                         final BDDFactory factory)
  {
    for (int bitno = mNumberOfBits - 1; bitno >= 0; bitno--) {
      final int index = getStateVariableIndex(bitno);
      if (currentStateCube != null) {
        currentStateCube.unionWith(index);
      }
      if (nextToCurrent != null) {
        final int nextindex = getNextStateVariableIndex(bitno);
        nextToCurrent.set(nextindex, index);
      }
    }
  }

  void buildBackwardCubes(final BDDVarSet nextStateCube,
                          final BDDPairing currentToNext,
                          final BDDFactory factory)
  {
    for (int bitno = mNumberOfBits - 1; bitno >= 0; bitno--) {
      final int nextindex = getNextStateVariableIndex(bitno);
      if (nextStateCube != null) {
        nextStateCube.unionWith(nextindex);
      }
      if (currentToNext != null) {
        final int index = getStateVariableIndex(bitno);
        currentToNext.set(index, nextindex);
      }
    }
  }

  BDD includeUnchangedBDD(final BDD bdd, final BDDFactory factory)
  {
    final int numstates = getNumberOfEncodedStates();
    final int power2 = 1 << mNumberOfBits;
    final int firstuse2 = numstates + numstates - power2;
    if (numstates == firstuse2) {
      for (int bitno = 0; bitno < mNumberOfBits; bitno++) {
        final BDD current = getStateVariable(factory, bitno);
        final BDD next = getNextStateVariable(factory, bitno);
        current.biimpWith(next);
        bdd.andWith(current);
      }
    } else {
      final BDD result = getStateVariable(factory, 0);
      final BDD next0 = getNextStateVariable(factory, 0);
      result.biimpWith(next0);
      final BDD geq = buildCurrentStateGreaterOrEqualBDD
        (firstuse2, mNumberOfBits - 1, factory);
      result.orWith(geq);
      final BDD equiv = factory.one();
      for (int bitno = 1; bitno < mNumberOfBits; bitno++) {
        final BDD current = getStateVariable(factory, bitno);
        final BDD next = getNextStateVariable(factory, bitno);
        current.biimpWith(next);
        equiv.andWith(current);
      }
      result.andWith(equiv);
      bdd.andWith(result);
    }
    return bdd;
  }

  boolean isReachable(final StateProxy state)
  {
    return mStateMap.containsKey(state);
  }

  int getStateCode(final StateProxy state)
  {
    final StateCode entry = mStateMap.get(state);
    if (entry == null) {
      return -1;
    } else {
      return entry.getStateCode();
    }
  }

  StateProxy getState(final int code)
  {
    final StateCode record = mStateArray[code];
    return record.getState();
  }

  BDD getStateBDD(final StateProxy state, final BDDFactory factory)
  {
    final StateCode entry = mStateMap.get(state);
    final int code = entry.getStateCode();
    final int last = entry.usesTwoCodes() ? 1 : 0;
    final BDD result = factory.one();
    for (int bitno = last; bitno < mNumberOfBits; bitno++) {
      final int mask = 1 << bitno;
      final BDD var = getStateVariable(factory, bitno);
      if ((code & mask) == 0) {
        result.andWith(var.not());
        var.free();
      } else {
        result.andWith(var);
      }
    }
    return result;
  }

  BDD getCurrentStateBitBDD(final StateProxy state,
                            final int bitindex,
                            final BDDFactory factory)
  {
    final StateCode entry = mStateMap.get(state);
    final int code = entry.getStateCode();
    if (entry.usesTwoCodes() && bitindex == mNumberOfBits - 1) {
      return null;
    } else if ((code & (1 << bitindex)) != 0) {
      final int varindex = getStateVariableIndex(bitindex);
      return factory.ithVar(varindex);
    } else {
      final int varindex = getStateVariableIndex(bitindex);
      return factory.nithVar(varindex);
    }
  }

  BDD getNextStateBitBDD(final StateProxy state,
                         final int bitindex,
                         final BDDFactory factory)
  {
    final StateCode entry = mStateMap.get(state);
    final int code = entry.getStateCode();
    if (entry.usesTwoCodes() && bitindex == mNumberOfBits - 1) {
      return null;
    } else if ((code & (1 << bitindex)) != 0) {
      final int varindex = getNextStateVariableIndex(bitindex);
      return factory.ithVar(varindex);
    } else {
      final int varindex = getNextStateVariableIndex(bitindex);
      return factory.nithVar(varindex);
    }
  }

  int getStateVariableIndex(final int bitno)
  {
    return mFirstVariableIndex + 2 * bitno;
  }

  int getNextStateVariableIndex(final int bitno)
  {
    return mFirstVariableIndex + 2 * bitno + 1;
  }

  BDD getTransitionBDD(final TransitionProxy trans, final BDDFactory factory)
  {
    final StateProxy source = trans.getSource();
    final StateProxy target = trans.getTarget();
    return getTransitionBDD(source, target, factory);
  }

  BDD getTransitionBDD(final StateProxy source,
                       final StateProxy target,
                       final BDDFactory factory)
  {
    final StateCode sourceentry = mStateMap.get(source);
    final int sourcecode = sourceentry.getStateCode();
    final int sourcelast = sourceentry.usesTwoCodes() ? 1 : 0;
    final StateCode targetentry = mStateMap.get(target);
    final int targetcode = targetentry.getStateCode();
    final int targetlast = targetentry.usesTwoCodes() ? 1 : 0;
    final BDD result = factory.one();
    for (int bitno = 0; bitno < mNumberOfBits; bitno++) {
      final int mask = 1 << bitno;
      if (bitno >= targetlast) {
        final BDD nextvar = getNextStateVariable(factory, bitno);
        if ((targetcode & mask) == 0) {
          result.andWith(nextvar.not());
          nextvar.free();
        } else {
          result.andWith(nextvar);
        }
      }
      if (bitno >= sourcelast) {
        final BDD curvar = getStateVariable(factory, bitno);
        if ((sourcecode & mask) == 0) {
          result.andWith(curvar.not());
        } else {
          result.andWith(curvar);
        }
      }
    }
    return result;
  }

  void createVarBlocks(final BDDFactory factory)
  {
    if (mNumberOfBits > 0) {
      final int first = mFirstVariableIndex;
      final int last = first + 2 * mNumberOfBits - 1;
      factory.addVarBlock(first, last, false);
      if (mNumberOfBits > 1) {
        for (int i = first; i < last; i += 2) {
          factory.addVarBlock(i, i + 1, false);
        }
      }
    }
  }


  //#########################################################################
  //# Bit Counting
  static int getNumberOfBits(final AutomatonProxy aut)
  {
    final Collection<StateProxy> states = aut.getStates();
    final int numstates = states.size();
    return 2 * AutomatonTools.log2(numstates);
  }


  //#########################################################################
  //# Auxiliary Methods
  private BDD buildCurrentStateGreaterOrEqualBDD(final int code,
                                                 final int bitno,
                                                 final BDDFactory factory)
  {
    if (code == 0) {
      return factory.one();
    } else {
      final BDD var = getStateVariable(factory, bitno);
      final int pivot = 1 << bitno;
      if (code < pivot) {
        final BDD result = buildCurrentStateGreaterOrEqualBDD
          (code, bitno - 1, factory);
        result.andWith(var.not());
        var.free();
        return result;
      } else {
        final BDD result = buildCurrentStateGreaterOrEqualBDD
          (code & ~pivot, bitno - 1, factory);
        result.andWith(var);
        return result;
      }
    }
  }

  private BDD getStateVariable(final BDDFactory factory, final int bitno)
  {
    final int index = getStateVariableIndex(bitno);
    return factory.ithVar(index);
  }

  private BDD getNextStateVariable(final BDDFactory factory, final int bitno)
  {
    final int index = getNextStateVariableIndex(bitno);
    return factory.ithVar(index);
  }


  //#########################################################################
  //# Inner Class StateCode
  private class StateCode
    implements Comparable<StateCode>
  {

    //#######################################################################
    //# Constructors
    private StateCode(final StateProxy state)
    {
      mState = state;
      mSuccessorStates = new TreeSet<StateCode>();
      mReachable = state.isInitial();
      mStateCode = -1;
      mUsesTwoCodes = false;
    }

    //#######################################################################
    //# Comparing
    public int compareTo(final StateCode code)
    {
      return mState.compareTo(code.mState);
    }

    //#######################################################################
    //# Simple Access
    private StateProxy getState()
    {
      return mState;
    }

    private int getStateCode()
    {
      return mStateCode;
    }

    private boolean isReachable()
    {
      return mReachable;
    }

    private boolean setReachable()
    {
      if (mReachable) {
        return false;
      } else {
        mReachable = true;
        return true;
      }
    }

    private void setStateCode(final int code)
    {
      setStateCode(code, false);
    }

    private void setStateCode(final int code, final boolean use2)
    {
      if (mStateCode >= 0) {
        throw new IllegalStateException
          ("Attempt to set state code a second time!");
      } else if (use2 && (code & 1) != 0) {
        throw new IllegalArgumentException
          ("Must use even value when occupying two codes!");
      } else {
        mStateCode = code;
        mUsesTwoCodes = use2;
      }
    }

    private boolean usesTwoCodes()
    {
      return mUsesTwoCodes;
    }

    //#######################################################################
    //# BFS Encoding
    private void addTransition(final TransitionProxy trans)
    {
      final StateProxy target = trans.getTarget();
      final StateCode code = mStateMap.get(target);
      mSuccessorStates.add(code);
    }

    private Set<StateCode> removeSuccessorStates()
    {
      final Set<StateCode> result = mSuccessorStates;
      mSuccessorStates = null;
      return result;
    }

    //#######################################################################
    //# Data Members
    private final StateProxy mState;
    private Set<StateCode> mSuccessorStates;
    private boolean mReachable;
    private int mStateCode;
    private boolean mUsesTwoCodes;

  }


  //#########################################################################
  //# Data Members
  private final AutomatonProxy mAutomaton;
  private final ComponentKind mKind;
  private final int mAutomatonIndex;
  private final int mNumberOfBits;
  private final int mFirstVariableIndex;
  private final int mFirstUse2;
  private final StateCode[] mStateArray;
  private final Map<StateProxy,StateCode> mStateMap;
  private Set<EventProxy> mNondeterministicEvents;

}









