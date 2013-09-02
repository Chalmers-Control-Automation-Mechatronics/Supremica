//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.analysis.tr
//# CLASS:   SynchronisationEncoding
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.tr;

import net.sourceforge.waters.model.des.AutomatonTools;

/**
 * @author Robi Malik, Sahar Mohajerani
 */
public class LongSynchronisationEncoding
{
  public LongSynchronisationEncoding(final int[] sizes)
  {
    mShiftAmount = new int[sizes.length+1];
    mMask = new int[sizes.length];
    int shift = 0;
    for (int i=0; i < sizes.length; i++) {
      final int numberOfBits = AutomatonTools.log2(sizes[i]);
      shift = shift + numberOfBits;
      mShiftAmount[i+1]= shift;
      mMask[i] = (1<<numberOfBits)-1;
    }
    assert shift <= 63;
  }

  public int size()
  {
    return mShiftAmount[mShiftAmount.length-1];
  }

  public long encode(final int[] tuple)
  {
    long result = 0;
    for(int i = 0; i < tuple.length; i++) {
      result = result | (long)tuple[i]<<mShiftAmount[i];
    }
    return result;
  }

  public void decode(final long code, final int[] tuple)
  {
    for (int i = 0; i < tuple.length; i++) {
      tuple[i] = (int) ((code >>> mShiftAmount[i]) & mMask[i]);
    }
  }



  //#######################################################################
  //# Data Members
  private final int[] mShiftAmount;
  private final int[] mMask;
}
