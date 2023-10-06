//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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

package net.sourceforge.waters.analysis.tr;

import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.Arrays;
import java.util.Collection;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.StateProxy;


/**
 * <P>A state encoding for transition relations.</P>
 *
 * <P>The state encoding assigns to each state of an automaton an integer
 * used as its code for indexing or compact storage. The class provides
 * methods to find the state codes for given {@link StateProxy} objects
 * and versa.</P>
 *
 * <P>State codes start at&nbsp;0, and each state is assigned the next
 * available number, so the range of state codes always is from&nbsp;0
 * up to one less than the number of states in the encoding.</P>
 *
 * <P>State codes are assigned in a deterministic manner depending only on
 * the order in which they are found in the input. There is support to modify
 * an encoding after it has been created. However, transition relations are
 * not expected to store the state encoding after creation, so changes to the
 * state encoding will not affect any transition relation that has been
 * created.</P>
 *
 * <P>States in the encoding may be missing, represented by <CODE>null</CODE>.
 * This can be used to represent unreachable states.<P>
 *
 * @see ListBufferTransitionRelation
 * @see IntStateBuffer
 *
 * @author Robi Malik
 */

public class StateEncoding
{

  //#########################################################################
  //# Constructor
  /**
   * Creates an empty state encoding.
   */
  public StateEncoding()
  {
    mStates = null;
    mStateCodeMap = null;
  }

  /**
   * Creates a with the given number of states, but with no state objects
   * associated to any state.
   */
  public StateEncoding(final int size)
  {
    mStates = new StateProxy[size];
    mStateCodeMap = new TObjectIntHashMap<>(size, 0.5f, -1);;
  }

  /**
   * Creates a new state encoding for the given automaton.
   * State codes are assigned in the order they appear in the automaton's
   * state list.
   */
  public StateEncoding(final AutomatonProxy aut)
  {
    init(aut.getStates());
  }

  /**
   * Creates a new state encoding for the given states.
   * State codes are assigned in the order they appear in the given
   * collection. Any <CODE>null</CODE> entries are not associated with a state
   * object, but their code numbers are still used.
   */
  public StateEncoding(final Collection<? extends StateProxy> states)
  {
    init(states);
  }

  /**
   * Creates a new state encoding for the given states.
   * State codes are assigned according to their indexes in the given
   * array. Any <CODE>null</CODE> entries are not associated with a state
   * object, but their code numbers are still used.
   * @param  states  New states array. Will not be copied.
   */
  public StateEncoding(final StateProxy[] states)
  {
    init(states);
  }

  /**
   * Creates a new state encoding by copying another.
   * The new state encoding does not share the original encoding's state array.
   */
  public StateEncoding(final StateEncoding enc)
  {
    if (enc.mStates == null) {
      mStates = null;
      mStateCodeMap = null;
    } else {
      final StateProxy[] states =
        Arrays.copyOf(enc.mStates, enc.getNumberOfStates());
      init(states);
    }
  }


  //#########################################################################
  //# Overrides for java.lang.Object
  @Override
  public String toString()
  {
    final StringBuilder buffer = new StringBuilder("{");
    if (mStates != null) {
      int code = 0;
      for (final StateProxy state : mStates) {
        if (code > 0) {
          buffer.append(", ");
        }
        buffer.append(code);
        if (state != null) {
          buffer.append('=');
          buffer.append(state.getName());
        }
        code++;
      }
    }
    buffer.append('}');
    return buffer.toString();
  }


  //#########################################################################
  //# Initialisation
  /**
   * Resets this state encoding to use the given states and an optional
   * dump state.
   * @param  states     Collection of states, whose state codes are
   *                    assigned in the order they appear. Any <CODE>null</CODE>
   *                    entries are not associated with a state object, but their
   *                    code numbers are still used.
   * @param  dumpState  Dump state to be used, or <CODE>null</CODE>.
   *                    If the collection of states does not contain the dump
   *                    state, an additional state is allocated and associated
   *                    with the dump state as the last state number.
   */
  public void init(final Collection<? extends StateProxy> states,
                   final StateProxy dumpState)
  {
    int numStates = states.size();
    if (!states.contains(dumpState)) {
      numStates++;
    }
    mStates = new StateProxy[numStates];
    mStateCodeMap = new TObjectIntHashMap<>(numStates, 0.5f, -1);
    int code = 0;
    for (final StateProxy state : states) {
      if (state != null) {
        mStates[code] = state;
        mStateCodeMap.put(state, code);
      }
      code++;
    }
    if (numStates > states.size() && dumpState != null) {
      code = numStates - 1;
      mStates[code] = dumpState;
      mStateCodeMap.put(dumpState, code);
    }
  }

  /**
   * Resets this state encoding to use the given states.
   * State codes are assigned in the order they appear in the given
   * collection. Any <CODE>null</CODE> entries are not associated with a state
   * object, but their code numbers are still used.
   */
  public void init(final Collection<? extends StateProxy> states)
  {
    final int numStates = states.size();
    mStates = new StateProxy[numStates];
    mStateCodeMap = new TObjectIntHashMap<>(numStates, 0.5f, -1);
    int code = 0;
    for (final StateProxy state : states) {
      if (state != null) {
        mStates[code] = state;
        mStateCodeMap.put(state, code);
      }
      code++;
    }
  }

  /**
   * Resets this state encoding to use the given states.
   * State codes are assigned according to their indexes in the given
   * array. Any <CODE>null</CODE> entries are not associated with a state
   * object, but their code numbers are still used.
   * @param  states  New states array. Will not be copied.
   */
  public void init(final StateProxy[] states)
  {
    final int numStates = states.length;
    mStates = states;
    mStateCodeMap = new TObjectIntHashMap<>(numStates, 0.5f, -1);
    for (int code = 0; code < numStates; code++) {
      final StateProxy state = states[code];
      if (state != null) {
        mStateCodeMap.put(state, code);
      }
    }
  }

  /**
   * Clears this state encoding.
   * This method deletes all states from this encoding and resets it
   * to be an empty encoding.
   */
  public void clear()
  {
    mStates = null;
    mStateCodeMap = null;
  }


  //#########################################################################
  //# Simple Access
  /**
   * Gets the number of states in this encoding.
   * State codes start at zero and run up to one less than the number of
   * states, so this method can also be used to determine the range of valid
   * state codes.
   * Unreachable states (marked with a <CODE>null</CODE> state object) are
   * included in the state count.
   */
  public int getNumberOfStates()
  {
    return mStates == null ? 0 : mStates.length;
  }

  /**
   * Gets the code for the given state.
   * @param  state    State object to be looked up.
   * @return Code of state, or <CODE>-1</CODE> if the state does not appear
   *         in the encoding.
   */
  public int getStateCode(final StateProxy state)
  {
    return mStateCodeMap.get(state);
  }

  /**
   * Gets the state object encoded by the given code.
   * @param  code    State code to be looked up. Should be in the range
   *                 from 0 to {@link #getNumberOfStates()}-1.
   * @return State object encoded by the given code, or <CODE>null</CODE>
   *         if the state is missing (marked as unreachable) in the
   *         encoding.
   * @throws IndexOutOfBoundsException to indicate that the given code
   *         is not within the range of valid state codes for the encoding.
   */
  public StateProxy getState(final int code)
  {
    return mStates[code];
  }

  /**
   * Gets an array containing all states in the encoding, indexed by their
   * codes.
   */
  public StateProxy[] getStatesArray()
  {
    return mStates;
  }

  /**
   * Gets an encoding map that maps all all states in the encoding
   * to their integer codes.
   */
  public TObjectIntHashMap<StateProxy> getStateCodeMap()
  {
    return mStateCodeMap;
  }

  /**
   * Returns the smallest state code associated with a <CODE>null</CODE>
   * state object in the encoding, or <CODE>-1</CODE>.
   */
  public int findUnusedCode()
  {
    for (int c = 0; c < mStates.length; c++) {
      if (mStates[c] == null) {
        return c;
      }
    }
    return -1;
  }


  //#########################################################################
  //# Data Members
  private StateProxy[] mStates;
  private TObjectIntHashMap<StateProxy> mStateCodeMap;

}
