//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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

package net.sourceforge.waters.analysis.monolithic;

import java.util.Arrays;


/**
 * <P>Encoded synchronized state tuple.</P>
 *
 * @author Peter Yunil Park
 */

public class StateTuple
{

  //#########################################################################
  //# Constructors
  /**
   * Creates an empty state tuple.
   * @param size number of integers used to store encoded state.
   */
  public StateTuple(final int size)
  {
    mStateCodes = new int[size];
  }

  /**
   * Creates a state tuple with given encoded state tuple (integer array).
   */
  public StateTuple(final int[] codes)
  {
    mStateCodes = codes;
  }


  //#########################################################################
  //# Simple Access
  /**
   * Returns current encoded state tuple codes.
   */
  public int[] getCodes()
  {
    return mStateCodes;
  }

  /**
   * Gets required state from state tuple
   * @param index index of state in the automata
   * @return index of state
   */
  public int get(final int index)
  {
    return mStateCodes[index];
  }


  //#########################################################################
  //# Overrides for Baseclass java.lang.Object
  public int hashCode()
  {
    int result = 0;
    for (int i = 0; i < mStateCodes.length; i++){
      result *= 5;
      result += mStateCodes[i];
    }
    return result;
  }

  public boolean equals(final Object other)
  {
    if (other != null && getClass() == other.getClass()) {
      final StateTuple tuple = (StateTuple) other;
      for(int i = 0; i < mStateCodes.length; i++){
        if (mStateCodes[i] != tuple.get(i)) {
          return false;
        }
      }
      return true;
    } else {
      return false;
    }
  }

  public String toString(){
    return Arrays.toString(mStateCodes);
  }


  //#########################################################################
  //# Data Members
  private final int mStateCodes[];

}
