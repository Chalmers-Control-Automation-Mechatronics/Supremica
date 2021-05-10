//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
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

package net.sourceforge.waters.analysis.distributed.safetyverifier;

import java.io.IOException;
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
 *
 * This class uses a custom serialisation protocol. The data fields
 * are marked as transient and not final. This class is still
 * immutable.
 *
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

  public void setDepthHint(int depth)
  {
    mDepth = depth;
  }

  /*
   * Serialisation protocol for state tuple:
   *   [ depth ][N][state][data0]...[dataN] 
   *      int  short        ints
   *
   * The length is sent as a short. This means that there can be at
   * most 65536 words of data per tuple. This is more than enough and
   * will hopefully save a small amount of space.
   *
   * After the length, it reads the appropriate number of data
   * values as integer words.
   */


  private void writeObject(java.io.ObjectOutputStream out) 
    throws IOException
  {
    //This should write the object header, but nothing else because
    //the other fields are marked as transient
    out.defaultWriteObject();

    //Length of the state as a short.
    int len = mState.length & 0xFFFF;

    out.writeInt(mDepth);
    out.writeShort(len);
    for (int i = 0; i < len; i++)
      {
	out.writeInt(mState[i]);
      }
  }

  private void readObject(java.io.ObjectInputStream in)
    throws IOException, ClassNotFoundException
  {
    in.defaultReadObject();

    mDepth = in.readInt();
    int len = in.readShort();

    mState = new int[len];

    for (int i = 0; i < len; i++)
      {
	mState[i] = in.readInt();
      }
  }

  //These fields should be final, but to avoid further reflection when
  //deserialising, they are marked as transient-volatile to keep them 
  //threadsafe
  private transient int[] mState;
  private volatile transient int mDepth;

  private static final long serialVersionUID = 1L;
}
