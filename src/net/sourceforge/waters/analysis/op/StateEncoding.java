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
 * @see {@link ListBufferTransitionRelation}
 * @see {@link StateBuffer}
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
  public int getNumberOfStates()
  {
    return mStates.length;
  }

  public int getStateCode(final StateProxy state)
  {
    if (mStateCodeMap.containsKey(state)) {
      return mStateCodeMap.get(state);
    } else {
      return -1;
    }
  }

  public StateProxy getState(final int code)
  {
    return mStates[code];
  }

  public StateProxy[] getStatesArray()
  {
    return mStates;
  }

  public List<StateProxy> getStates()
  {
    return Arrays.asList(mStates);
  }

  public TObjectIntHashMap<StateProxy> getStateCodeMap()
  {
    return mStateCodeMap;
  }


  //#########################################################################
  //# Data Members
  private StateProxy[] mStates;
  private TObjectIntHashMap<StateProxy> mStateCodeMap;

}
