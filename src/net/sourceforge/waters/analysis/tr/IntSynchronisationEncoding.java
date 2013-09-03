//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   LongSynchronisationEncoding
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.tr;

import gnu.trove.map.hash.TIntIntHashMap;

import net.sourceforge.waters.model.des.AutomatonTools;

/**
 * @author Robi Malik, Sahar Mohajerani
 */

public class IntSynchronisationEncoding
  extends AbstractSynchronisationEncoding
{
  //#######################################################################
  //# Constructor
  public IntSynchronisationEncoding(final int[] sizes, final int numStates)
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
    assert shift <= MAX_BITS;
    mMap = new TIntIntHashMap(numStates, 0.5f, -1, -1);
  }


  //#######################################################################
  //# Override AbstractSynchronisationEncoding
  @Override
  public int getStateCode(final int[] tuple)
  {
    final int encodedTuple = encode(tuple);
    return mMap.get(encodedTuple);
  }

  @Override
  public void addState(final int[] tuple, final int code)
  {
    final int encodedTuple = encode(tuple);
    mMap.put(encodedTuple, code);
  }

  @Override
  public int getMemoryEstimate()
  {
    return mMap.size()*8;
  }

  //#######################################################################
  //# Specific methods
  public int encode(final int[] tuple)
  {
    int result = 0;
    for(int i = 0; i < tuple.length; i++) {
      result = result | tuple[i]<<mShiftAmount[i];
    }
    return result;
  }

  public void decode(final int code, final int[] tuple)
  {
    for (int i = 0; i < tuple.length; i++) {
      tuple[i] = ((code >>> mShiftAmount[i]) & mMask[i]);
    }
  }


  //#######################################################################
  //# Data Members
  private final int[] mShiftAmount;
  private final int[] mMask;
  private final TIntIntHashMap mMap;


  //#######################################################################
  //# Data Members
  static final int MAX_BITS = 31;

}
