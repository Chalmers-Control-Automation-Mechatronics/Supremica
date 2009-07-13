package net.sourceforge.waters.analysis.distributed.safetyverifier;

import java.io.Serializable;

/**
 * Represents a packed state tuple. All state tuples contain a depth
 * attribute which can be used as a heuristic to find better counter
 * examples. If no depth is specified, it will default to the maximum
 * positive value for an integer (which should make the path
 * unpreferable). States that are actually generated should set a sane
 * depth. The depth value does not affect the equality/hashcode
 * relation for state tuples, it is purely a hint -- that is to say,
 * two state tuples could be considerd `equal' by the equals method
 * and yet have different depth values. This is because they represent
 * the same state, they were just found by different paths.
 * @author Sam Douglas
 */
public class StateTuple implements Serializable
{
  /**
   * Creates a state tuple from data. This is intended to 
   */ 
  public StateTuple(int[] data)
  {
    this(data, Integer.MAX_VALUE);
  }

  public StateTuple(int[] data, int depth)
  {
    mState = data;
    mDepth = depth;
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

  /**
   * Gets the depth value for the state.
   */
  public int getDepthHint()
  {
    return mDepth;
  }


  private final int[] mState;
  private final int mDepth;
}