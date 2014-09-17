//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   LongSynchronisationEncoding
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.tr;

import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.custom_hash.TObjectIntCustomHashMap;

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
  //# Data Members
  private final int[] mWordIndex;
  private final int[] mShiftAmount;
  private final int[] mMask;
  private final int mNumberOfWords;
  private final TObjectIntCustomHashMap<int[]> mMap;


}
