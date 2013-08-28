//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.analysis.tr
//# CLASS:   TRPartition
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.tr;

import java.util.Arrays;
import java.util.List;

/**
 * @author Robi Malik, Sahar Mohajerani
 */
public class TRPartition
{
  public TRPartition(final List<int[]> partition, final int size)
  {
    mPartition = partition;
    mSize = size;
    mStateToClass = null;
  }

  public List<int[]> getPartition()
  {
    return mPartition;
  }

  public int getSize()
  {
    return mSize;
  }

  public int[] getStateToClass()
  {
    if (mStateToClass == null) {
      mStateToClass = new int[mSize];
      Arrays.fill(mStateToClass, -1);
      for (int i = 0; i < mPartition.size(); i++)
      {
        final int[] clazz = mPartition.get(i);
        if (clazz != null) {
          for (final int s : clazz) {
            mStateToClass[s] = i;
          }
        }
      }
    }
    return mStateToClass;
  }

  //#########################################################################
  //# Data Members

  private final List<int[]> mPartition;
  private final int mSize;
  private int[] mStateToClass;
}
