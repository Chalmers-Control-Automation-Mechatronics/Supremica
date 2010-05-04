//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.op
//# CLASS:   StateEncoding
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.op;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import gnu.trove.TObjectIntHashMap;

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
 * state encoding will not affect any transition relation that have been
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
   * Creates a new state encoding for the given automaton.
   * State codes are assigned in the order they appear in the automaton's
   * state list.
   */
  public StateEncoding(final AutomatonProxy aut)
  {
    this(aut.getStates());
  }

  /**
   * Creates a new state encoding for the given states.
   * State codes are assigned in the order they appear in the given
   * collection. Any <CODE>null</CODE> entries cause code numbers to be
   * skipped.
   */
  public StateEncoding(final Collection<? extends StateProxy> states)
  {
    init(states);
  }

  /**
   * Creates a new state encoding for the given states.
   * State codes are assigned according to their indexes in the given
   * array. Any <CODE>null</CODE> entries cause code numbers to be skipped.
   * @param  states  New states array. Will not be copied.
   */
  public StateEncoding(final StateProxy[] states)
  {
    init(states);
  }


  //#########################################################################
  //# Overrides for java.lang.Object
  public String toString()
  {
    final StringBuffer buffer = new StringBuffer("{");
    if (mStates != null) {
      int code = 0;
      for (final StateProxy state : mStates) {
        if (state != null) {
          if (code > 0) {
            buffer.append(", ");
          }
          buffer.append(code);
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
   * Creates a new state encoding for the given states.
   * State codes are assigned in the order they appear in the given
   * collection. Any <CODE>null</CODE> entries cause code numbers to be
   * skipped.
   */
  public void init(final Collection<? extends StateProxy> states)
  {
    final int numStates = states.size();
    mStates = new StateProxy[numStates];
    mStateCodeMap = new TObjectIntHashMap<StateProxy>(numStates);
    int code = 0;
    for (final StateProxy state : states) {
      mStates[code] = state;
      if (state != null) {
        mStateCodeMap.put(state, code);
      }
      code++;
    }
  }

  /**
   * Resets this state encoding to use the given states.
   * State codes are assigned according to their indexes in the given
   * array. Any <CODE>null</CODE> entries cause code numbers to be skipped.
   * @param  states  New states array. Will not be copied.
   */
  public void init(final StateProxy[] states)
  {
    final int numStates = states.length;
    mStates = states;
    mStateCodeMap = new TObjectIntHashMap<StateProxy>(numStates);
    for (int code = 0; code < numStates; code++) {
      final StateProxy state = states[code];
      if (state != null) {
        mStateCodeMap.put(state, code);
      }
    }
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
    return mStates.length;
  }

  /**
   * Gets the code for the given state.
   * @param  state    State object to be looked up.
   * @return Code of state, or <CODE>-1</CODE> if the state does not appear
   *         in the encoding.
   */
  public int getStateCode(final StateProxy state)
  {
    if (mStateCodeMap.containsKey(state)) {
      return mStateCodeMap.get(state);
    } else {
      return -1;
    }
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
   * Gets a list containing all states in the encoding, indexed by their
   * codes.
   */
  public List<StateProxy> getStates()
  {
    return Arrays.asList(mStates);
  }

  /**
   * Gets an encoding map that maps all all states in the encoding
   * to their integer codes.
   */
  public TObjectIntHashMap<StateProxy> getStateCodeMap()
  {
    return mStateCodeMap;
  }


  //#########################################################################
  //# Data Members
  private StateProxy[] mStates;
  private TObjectIntHashMap<StateProxy> mStateCodeMap;

}
