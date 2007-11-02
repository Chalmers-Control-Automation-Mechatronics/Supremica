//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.bdd
//# CLASS:   AutomatonBDD
//###########################################################################
//# $Id: AutomatonBDD.java,v 1.1 2007-11-02 00:30:37 robi Exp $
//###########################################################################

package net.sourceforge.waters.analysis.bdd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BDDPairing;
import net.sf.javabdd.BDDVarSet;

import net.sourceforge.waters.model.des.AutomatonProxy;
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
    mBitIndex = autindex;
    final Collection<StateProxy> states = aut.getStates();
    final int numstates = states.size();
    mNumberOfBits = log2(numstates);
    mFirstVariableIndex = factory.varNum();
    mStateMap = new HashMap<StateProxy,StateCode>(numstates);
    int index = 0;
    switch (numstates) {
    case 0:
      break;
    case 1:
      final StateProxy state0 = states.iterator().next();
      final StateCode code0 = new StateCode(state0, 0);
      mStateMap.put(state0, code0);
      break;
    case 2:
      factory.setVarNum(mFirstVariableIndex + 2);
      final List<StateProxy> sorted = new ArrayList<StateProxy>(states);
      Collections.sort(sorted, INIT_COMPARATOR);
      for (final StateProxy state : sorted) {
        final StateCode code = new StateCode(state, index++);
        mStateMap.put(state, code);
      }
      break;
    default:
      factory.setVarNum(mFirstVariableIndex + 2 * mNumberOfBits);
      final List<StateCode> open = new LinkedList<StateCode>();
      for (final StateProxy state : states) {
        final StateCode code = new StateCode(state);
        mStateMap.put(state, code);
        if (state.isInitial()) {
          open.add(code);
        }
      }
      for (final TransitionProxy trans : aut.getTransitions()) {
        final StateProxy source = trans.getSource();
        final StateCode code = mStateMap.get(source);
        code.addTransition(trans);
      }
      final int power2 = 1 << mNumberOfBits;
      final int firstuse2 = numstates + numstates - power2;
      Collections.sort(open);
      while (!open.isEmpty()) {
        final StateCode code = open.remove(0);
        if (index >= firstuse2) {
          code.setStateCode(index, true);
          index += 2;
        } else {
          code.setStateCode(index++);
        }
        final Collection<StateCode> successors = code.removeSuccessorStates();
        for (final StateCode succ : successors) {
          if (succ.getStateCode() < 0 && !open.contains(succ)) {
            open.add(succ);
          }
        }
      }
      break;
    }
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

  int getBitIndex()
  {
    return mBitIndex;
  }


  //#########################################################################
  //# Constructing BDDs
  BDD getInitialStateBDD(final BDDFactory factory)
  {
    final BDD result = factory.zero();
    final Collection<StateProxy> states = mAutomaton.getStates();
    for (final StateProxy state : states) {
      if (state.isInitial()) {
        final BDD statebdd = getStateBDD(state, factory);
        result.orWith(statebdd);
      }
    }
    return result;
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
    final Collection<StateProxy> states = mAutomaton.getStates();
    final int numstates = states.size();
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
      } else {
	result.andWith(var);
      }
    }
    return result;
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
    return 2 * log2(numstates);
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
        return result;
      } else {
        final BDD result = buildCurrentStateGreaterOrEqualBDD
          (code & ~pivot, bitno - 1, factory);
        result.andWith(var);
        return result;
      }
    }
  }

  private int getStateVariableIndex(final int bitno)
  {
    return mFirstVariableIndex + 2 * bitno;
  }

  private int getNextStateVariableIndex(final int bitno)
  {
    return mFirstVariableIndex + 2 * bitno + 1;
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
  
  private static int log2(int x)
  {
    int result = 0;
    if (x > 1) {
      x--;
      do {
	x >>= 1;
	result++;
      } while (x > 0);
    }
    return result;
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
      mNumIncomingTransitions = state.isInitial() ? 1 : 0;
      mStateCode = -1;
      mUsesTwoCodes = false;
    }

    private StateCode(final StateProxy state, final int code)
    {
      mState = state;
      mSuccessorStates = null;
      mNumIncomingTransitions = -1;
      mStateCode = code;
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
    private int getStateCode()
    {
      return mStateCode;
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
      code.mNumIncomingTransitions++;
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
    private int mNumIncomingTransitions;
    private int mStateCode;
    private boolean mUsesTwoCodes;
 
  }


  //#########################################################################
  //# Inner Class InitialStateComparator
  private static class InitialStateComparator
    implements Comparator<StateProxy>
  {

    //#######################################################################
    //# Interface java.util.Comparator
    public int compare(final StateProxy state1, final StateProxy state2)
    {
      final boolean init1 = state1.isInitial();
      final boolean init2 = state2.isInitial();
      if (init1 && !init2) {
        return -1;
      } else if (init2 && !init1) {
        return 1;
      } else {
        return state1.compareTo(state2);
      }
    }

  }


  //#########################################################################
  //# Data Members
  private final AutomatonProxy mAutomaton;
  private final ComponentKind mKind;
  private final int mBitIndex;
  private final int mNumberOfBits;
  private final int mFirstVariableIndex;
  private final Map<StateProxy,StateCode> mStateMap;

  private static final InitialStateComparator INIT_COMPARATOR =
    new InitialStateComparator();

}
