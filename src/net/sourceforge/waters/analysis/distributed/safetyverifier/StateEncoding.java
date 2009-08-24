package net.sourceforge.waters.analysis.distributed.safetyverifier;

import java.io.Serializable;

public abstract class StateEncoding implements Serializable
{
  /**
   * From an array of state indices, create an encoded
   * state.
   */
  public abstract StateTuple encodeState (int[] unpacked, int depth);

  /**
   * From a state tuple, decode it into an array of state
   * indices.
   */
  public abstract int[] decodeState(StateTuple packed);

  /**
   * Decode the state for a specified automaton from the 
   * packed state tuple. The default implementation of 
   * this decodes the full state tuple and returns the 
   * appropriate element. State encodings should override
   * this method if a more efficient technique exists.
   * @param packed tuple to extract state from
   * @param automaton to get state for
   * @return state of the given automaton
   */
  public int decodeAutomatonState(StateTuple packed, int automaton)
  {
    int[] unpacked = decodeState(packed);
    return unpacked[automaton];
  }

  /**
   * Interpret the state tuple according to this encoding
   * and return a human readable string representation.
   */
  public abstract String interpret(StateTuple state);
  public abstract String interpret(int[] unpacked);
}