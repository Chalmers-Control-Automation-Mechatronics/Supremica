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

package net.sourceforge.waters.analysis.tr;

import gnu.trove.iterator.TIntIntIterator;
import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.map.hash.TIntIntHashMap;

import java.util.Arrays;
import java.util.List;

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
    super(sizes, numStates);
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
  public int getMapSize()
  {
    return mMap.size();
  }

  @Override
  public boolean compose(final TRPartition partition)
  {
    boolean containsBadState = false;
    final TIntIntIterator iter = mMap.iterator();
    while (iter.hasNext()) {
      iter.advance();
      final int value = iter.value();
      final int clazz = partition.getClassCode(value);
      if (clazz < 0) {
        iter.remove();
        containsBadState = true;
      } else {
        iter.setValue(clazz);
      }
    }
    return containsBadState;
  }

  @Override
  public List<int[]> getInverseMap()
  {
    final int numOfAutomata = getNumberOfAutomata();
    final TIntIntIterator iter = mMap.iterator();
    final int[][] inverseMap = new int[getNumberOfStates()][];
    while (iter.hasNext()) {
      iter.advance();
      final int value = iter.value();
      final int key = iter.key();
      final int[] array = new int[numOfAutomata];
      decode(key, array);
      inverseMap[value] = array;
    }
     return Arrays.asList(inverseMap);
  }

  @Override
  public TObjectIntIterator<int[]> iterator()
  {
    return new IntSynchronisationIterator();
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
  //# Auxiliary methods
  private int getNumberOfStates()
  {
    final TIntIntIterator iter = mMap.iterator();
    int max = 0;
    while (iter.hasNext()) {
      iter.advance();
      final int value = iter.value();
      if (value > max) {
        max = value;
      }
    }
    return max + 1;
  }


  //#######################################################################
  //# Inner Class IntSynchronisationIterator
  private class IntSynchronisationIterator implements TObjectIntIterator<int[]>
  {

    private IntSynchronisationIterator()
    {
      mInnerIterator = mMap.iterator();
    }

    @Override
    public void advance()
    {
      mInnerIterator.advance();
    }

    @Override
    public boolean hasNext()
    {
      return mInnerIterator.hasNext();
    }

    @Override
    public void remove()
    {
      mInnerIterator.remove();
    }

    @Override
    public int[] key()
    {
      final int key = mInnerIterator.key();
      final int[] keys = new int[getNumberOfAutomata()];
      decode(key, keys);
      return keys;
    }

    @Override
    public int setValue(final int arg0)
    {
      return mInnerIterator.setValue(arg0);
    }

    @Override
    public int value()
    {
      return mInnerIterator.value();
    }

    private final TIntIntIterator mInnerIterator;
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
