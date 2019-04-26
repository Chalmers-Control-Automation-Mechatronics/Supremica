//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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

import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.custom_hash.TObjectIntCustomHashMap;

import java.util.Arrays;
import java.util.List;

import net.sourceforge.waters.model.des.AutomatonTools;


/**
 * @author Robi Malik, Sahar Mohajerani
 */

public class ArraySynchronisationEncoding extends
  AbstractSynchronisationEncoding
{
  //#######################################################################
  //# Constructor
  public ArraySynchronisationEncoding(final int[] sizes, final int numStates)
  {
    super(sizes, numStates);
    mWordIndex = new int[sizes.length];
    mShiftAmount = new int[sizes.length];
    mMask = new int[sizes.length];
    final TIntArrayList occupied = new TIntArrayList();
    for (int i = 0; i < sizes.length; i++) {
      final int numberOfBits = AutomatonTools.log2(sizes[i]);
      int j;
      for (j = 0; j < occupied.size(); j++) {
        if (occupied.get(j) + numberOfBits <= 32) {
          break;
        }
      }
      if (j == occupied.size()) {
        occupied.add(0);
      }
      final int shiftAmount = occupied.get(j);
      occupied.set(j, shiftAmount + numberOfBits);
      mWordIndex[i] = j;
      mShiftAmount[i] = shiftAmount;
      mMask[i] = (1 << numberOfBits) - 1;
    }
    mNumberOfWords = occupied.size();
    final IntArrayHashingStrategy strategy = new IntArrayHashingStrategy();
    mMap = new TObjectIntCustomHashMap<int[]>(strategy, numStates, 0.5f, -1);
  }

  //#######################################################################
  //# Override AbstractSynchronisationEncoding
  @Override
  public int getStateCode(final int[] tuple)
  {
    final int[] encoded = new int[mNumberOfWords];
    encode(tuple, encoded);
    return mMap.get(encoded);
  }

  @Override
  public void addState(final int[] tuple, final int code)
  {
    final int[] encoded = new int[mNumberOfWords];
    encode(tuple, encoded);
    mMap.put(encoded, code);
  }

  @Override
  public int getMapSize()
  {
    return mMap.size();
  }

  @Override
  public List<int[]> getInverseMap()
  {
    final int numOfAutomata = getNumberOfAutomata();
    final TObjectIntIterator<int[]> iter = mMap.iterator();
    final int[][] inverseMap = new int[getNumberOfStates()][];
    while (iter.hasNext()) {
      iter.advance();
      final int value = iter.value();
      final int[] key = iter.key();
      final int[] array = new int[numOfAutomata];
      decode(key, array);
      inverseMap[value] = array;
    }
     return Arrays.asList(inverseMap);
  }

  @Override
  public boolean compose(final TRPartition partition)
  {
    boolean containsBadState = false;
    final TObjectIntIterator<int[]> iter = mMap.iterator();
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
  public TObjectIntIterator<int[]> iterator()
  {
    return new ArraySynchronisationIterator();
  }

  //#######################################################################
  //# Specific methods
  public int getNumberOfWords()
  {
    return mNumberOfWords;
  }

  public void encode(final int[] tuple, final int[] encoded)
  {
    for (int i = 0; i < tuple.length; i++) {
      final int position = mWordIndex[i];
      encoded[position] |= tuple[i] << mShiftAmount[i];
    }
  }

  public void decode(final int[] encoded, final int[] tuple)
  {
    for (int i = 0; i < tuple.length; i++) {
      final int position = mWordIndex[i];
      tuple[i] = ((encoded[position] >>> mShiftAmount[i]) & mMask[i]);
    }
  }

  //#######################################################################
  //# Auxiliary methods
  private int getNumberOfStates()
  {
    final TObjectIntIterator<int[]> iter = mMap.iterator();
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
  //# Inner Class ArraySynchronisationIterator
  private class ArraySynchronisationIterator implements TObjectIntIterator<int[]>
  {

    private ArraySynchronisationIterator()
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
      final int[] key = mInnerIterator.key();
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

    private final TObjectIntIterator<int[]> mInnerIterator;
  }


  //#######################################################################
  //# Data Members
  private final int[] mWordIndex;
  private final int[] mShiftAmount;
  private final int[] mMask;
  private final int mNumberOfWords;
  private final TObjectIntCustomHashMap<int[]> mMap;


}
