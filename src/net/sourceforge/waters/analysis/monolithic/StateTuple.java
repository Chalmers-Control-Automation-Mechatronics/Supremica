//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis
//# CLASS:   StateTuple
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.monolithic;


/**
 * <P>Encoded synchronized state tuple.</P>
 *
 * @author Peter Yunil Park
 */

class StateTuple
{

  //#########################################################################
  //# Constructors
  /**
   * Creates an empty state tuple.
   * @param size number of integers used to store encoded state.
   */
  StateTuple(final int size)
  {
    mStateCodes = new int[size];
  }

  /**
   * Creates a state tuple with given encoded state tuple (integer array).
   */
  StateTuple(final int[] codes)
  {
    mStateCodes = codes;
  }


  //#########################################################################
  //# Simple Access
  /**
   * Returns current encoded state tuple codes.
   */
  int[] getCodes()
  {
    return mStateCodes;
  }

  /**
   * Gets required state from state tuple
   * @param index index of state in the automata
   * @return index of state
   */
  int get(int index)
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


  //#########################################################################
  //# Data Members
  private final int mStateCodes[];

}
