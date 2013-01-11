//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis
//# CLASS:   StateTuple
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.po;

import java.util.Arrays;


/**
 * <P>Encoded synchronized state tuple.</P>
 *
 * @author Adrian Shaw
 */

public class PartialOrderStateTuple
{

  //#########################################################################
  //# Constructors
  /**
   * Creates an empty state tuple.
   * @param size number of integers used to store encoded state.
   */
  public PartialOrderStateTuple(final int size)
  {
    mStateCodes = new int[size];
    mMayNeedExpansion = false;
    mFullyExpand = false;
    mVisited = false;
  }

  /**
   * Creates a state tuple with given encoded state tuple (integer array).
   */
  public PartialOrderStateTuple(final int[] codes)
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

  public void setMayNeedExpansion(final boolean expand){
    mMayNeedExpansion = expand;
  }

  public boolean mayNeedExpansion(){
    return mMayNeedExpansion;
  }

  public void setFullyExpand(final boolean expand){
    mFullyExpand = expand;
  }

  public boolean FullyExpand(){
    return mFullyExpand;
  }

  public void setPred(final PartialOrderStateTuple pred){
    mPred = pred;
  }

  public PartialOrderStateTuple getPred(){
    return mPred;
  }

  public void setVisited(final boolean visited){
    mVisited = visited;
  }

  public boolean getVisited(){
    return mVisited;
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
      final PartialOrderStateTuple tuple = (PartialOrderStateTuple) other;
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
  private boolean mMayNeedExpansion;
  private boolean mFullyExpand;
  private boolean mVisited;
  private PartialOrderStateTuple mPred;

}
