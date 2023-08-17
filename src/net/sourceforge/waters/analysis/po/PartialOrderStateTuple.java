//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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
    mComponentVisited = false;
    mRootChanged = false;
    mComponentNumber = 0;
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

  public boolean getFullyExpand(){
    return mFullyExpand;
  }

  public void setFullyExpanded(final boolean expand){
    mFullyExpanded = expand;
  }

  public boolean getFullyExpanded(){
    return mFullyExpanded;
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

  public void setComponentVisited(final boolean visited){
    mComponentVisited = visited;
  }

  public boolean getComponentVisited(){
    return mComponentVisited;
  }

  public void setComponent(final int value){
    mComponentNumber = value;
  }

  public int getComponent(){
    return mComponentNumber;
  }

  public boolean isInComponent(){
    return mComponentNumber != 0;
  }

  public void setRootChanged(final boolean value){
    mRootChanged = value;
  }

  public boolean getRootChanged(){
    return mRootChanged;
  }

  public void setRootIndex(final int value){
    mRootIndex = value;
  }

  public int getRootIndex(){
    return mRootIndex;
  }

  public void setTotalSuccessors(final int value){
    mTotalSuccessors = value;
  }

  public int getTotalSuccessors(){
    return mTotalSuccessors;
  }

  public void setAmpleSuccessors(final int value){
    mAmpleSuccessors = value;
  }

  public int getAmpleSuccessors(){
    return mAmpleSuccessors;
  }



  //#########################################################################
  //# Overrides for Baseclass java.lang.Object
  @Override
  public int hashCode()
  {
    int result = 0;
    for (int i = 0; i < mStateCodes.length; i++){
      result *= 5;
      result += mStateCodes[i];
    }
    return result;
  }

  @Override
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

  @Override
  public String toString(){
    return Arrays.toString(mStateCodes);
  }

  //#########################################################################
  //# Data Members
  protected final int mStateCodes[];
  protected boolean mMayNeedExpansion; //
  protected boolean mFullyExpand; //
  protected boolean mVisited;
  protected PartialOrderStateTuple mPred; //
  protected int mComponentNumber;
  protected boolean mComponentVisited;
  protected boolean mRootChanged;
  protected boolean mFullyExpanded;
  protected int mRootIndex;
  protected int mTotalSuccessors; //
  protected int mAmpleSuccessors; //
}
