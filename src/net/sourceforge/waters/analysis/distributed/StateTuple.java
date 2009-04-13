package net.sourceforge.waters.analysis.distributed;

import java.io.Serializable;

/**
 * Represents a packed state tuple 
 */
public class StateTuple implements Serializable
{
  public StateTuple(int[] data)
  {
    mState = data;
  }

  /**
   * Get the state data associated with this tuple. This
   * will probably be encoded, and really only useful to
   * a decoder.
   */
  public int[] getStateArray()
  {
    return mState;
  }
  
  /**
   * Without an encoding for interpretation, a string
   * representation of this state will not be very useful.
   */
  public String toString()
  {
    return "Packed state: " + mState;
  }

  public String toString(StateEncoding encoding)
  {
    return encoding.interpret(this);
  }

  public int hashCode()
  {
    return java.util.Arrays.hashCode(mState);
  }

  public boolean equals(Object o)
  {
    if (o == null) return false;
    if (!(o instanceof StateTuple)) return false;

    StateTuple t = (StateTuple)o;

    return java.util.Arrays.equals(mState, t.mState);
  }


  private final int[] mState;
}