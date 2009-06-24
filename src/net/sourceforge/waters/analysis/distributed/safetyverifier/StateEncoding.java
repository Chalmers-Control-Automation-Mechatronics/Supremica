package net.sourceforge.waters.analysis.distributed.safetyverifier;

public abstract class StateEncoding
{
  /**
   * From an array of state indices, create an encoded
   * state.
   */
  public abstract StateTuple encodeState (int[] unpacked);

  /**
   * From a state tuple, decode it into an array of state
   * indices.
   */
  public abstract int[] decodeState(StateTuple packed);

  /**
   * Interpret the state tuple according to this encoding
   * and return a human readable string representation.
   */
  public abstract String interpret(StateTuple state);
  public abstract String interpret(int[] unpacked);
}