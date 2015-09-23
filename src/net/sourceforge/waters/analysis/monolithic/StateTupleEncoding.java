//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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

import gnu.trove.list.array.TIntArrayList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.AutomatonTools;


/**
 * <P>A utility class to encode state tuples into compact integer arrays.</P>
 *
 * <P>During synchronous product computation, states are represented
 * as tuples (x<sub>1</sub>,...,x<sub>n</sub>) where each&nbsp;x<sub>i</sub>
 * is a state component of an automaton&nbsp;A<sub>i</sub>, encoded as
 * an integer. However, as most automata only have a few states,
 * representing the tuple as an integer array consumes a lot more
 * memory than necessary, particularly for large state spaces resulting
 * from the composition of many small automata.</P>
 *
 * <P>To save memory, the state tuple can be bit-packed into smaller integer
 * arrays, where each automaton is allocated only the number of bits needed.
 * The state tuple encoding class facilitates the conversion between
 * <I>decoded</I> tuples, where each automaton has its own array index, and
 * bit-packed <I>encoded</I> tuples. The encoded tuples are typically stored
 * in an {@link net.sourceforge.waters.analysis.tr.IntArrayBuffer
 * IntArrayBuffer}.</P>
 *
 * @author Fangqian Qiu, Robi Malik
 */

public class StateTupleEncoding
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new encoding for the given state numbers.
   * @param sizes     State numbers defining the encoding. For each automaton
   *                  index, the created encoding assumes with possible values
   *                  from 0 to one less than the corresponding size. The
   *                  method {@link StateTupleEncoding#getAutomataSizes(Collection)
   *                  getAutomataSizes()} can be used to obtain this array.
   */
  public StateTupleEncoding(final int[] sizes)
  {
    mAutomatonInfo = new AutomatonInfo[sizes.length];
    mWordInfo = new ArrayList<>();
    final TIntArrayList used = new TIntArrayList();
    int autIndex = 0;
    for (final int numStates : sizes) {
      final int numBits = AutomatonTools.log2(numStates);
      int wordIndex;
      for (wordIndex = 0; wordIndex < used.size(); wordIndex++) {
        if (used.get(wordIndex) + numBits <= SIZE_INT) {
          break;
        }
      }
      final int bitOffset;
      final List<AutomatonInfo> list;
      if (wordIndex < used.size()) {
        bitOffset = used.get(wordIndex);
        used.set(wordIndex, bitOffset + numBits);
        list = mWordInfo.get(wordIndex);
      } else {
        bitOffset = 0;
        used.add(numBits);
        list = new ArrayList<>();
        mWordInfo.add(list);
      }
      final AutomatonInfo info =
        new AutomatonInfo(autIndex, numBits, wordIndex, bitOffset);
      mAutomatonInfo[autIndex++] = info;
      list.add(info);
    }
  }


  /**
   * Returns an array with the state numbers of the given automata.
   * Each entry in the array is assigned the number of states of the
   * corresponding automaton in the order it appears in the input.
   */
  public static int[] getAutomataSizes
    (final Collection<AutomatonProxy> automata)
  {
    final int numAutomata = automata.size();
    final int[] sizes = new int[numAutomata];
    int a = 0;
    for (final AutomatonProxy aut : automata) {
      sizes[a++] = aut.getStates().size();
    }
    return sizes;
  }


  //#########################################################################
  //# Simple Access
  /**
   * Gets the number of automata in this encoding.
   */
  public int getNumberOfAutomata()
  {
    return mAutomatonInfo.length;
  }

  /**
   * Gets the number of words used by the encoded state tuples.
   */
  public int getNumberOfWords()
  {
    return mWordInfo.size();
  }


  //#########################################################################
  //# Encoding and Decoding
  /**
   * Encodes a given state tuple.
   * @param  decoded  The state tuple to be encoded.
   * @param  encoded  The encoded state tuple will be stored in this array,
   *                  which must be allocated to the number of words in the
   *                  encoding.
   */
  public void encode(final int[] decoded, final int[] encoded)
  {
    int wordIndex = 0;
    for (final List<AutomatonInfo> list : mWordInfo) {
      int word = 0;
      for (final AutomatonInfo info : list) {
        final int autIndex = info.getAutomatonIndex();
        final int bitOffset = info.getBitOffset();
        word |= (decoded[autIndex] << bitOffset);
      }
      encoded[wordIndex++] = word;
    }
  }

  /**
   * Decodes a given encoded state tuple.
   * @param  encoded  The state tuple to be decoded.
   * @param  decoded  The decoded state tuple will be stored in this array,
   *                  which must be allocated to the number of automata.
   */
  public void decode(final int[] encoded, final int[] decoded)
  {
    int wordIndex = 0;
    for (final List<AutomatonInfo> list : mWordInfo) {
      final int word = encoded[wordIndex];
      for (final AutomatonInfo info : list) {
        final int autIndex = info.getAutomatonIndex();
        final int bitOffset = info.getBitOffset();
        final int mask = info.getMask();
        decoded[autIndex] = (word & mask) >>> bitOffset;
      }
      wordIndex++;
    }
  }

  /**
   * Retrieves a state component from an encoded state tuple.
   * @param  encoded  The encoded state tuple to be read.
   * @param  autIndex The number of the automaton to be accessed.
   * @return The state of the indicated automaton in the encoded state
   *         tuple.
   */
  public int get(final int[] encoded, final int autIndex)
  {
    final AutomatonInfo info = mAutomatonInfo[autIndex];
    final int wordIndex = info.getWordIndex();
    final int bitOffset = info.getBitOffset();
    final int mask = info.getMask();
    return (encoded[wordIndex] & mask) >>> bitOffset;
  }

  /**
   * Assigns a state component from an encoded state tuple.
   * @param  encoded  The encoded state tuple to be modified.
   * @param  autIndex The number of the automaton to be modified.
   * @param  value    The new state to be assigned for the indicated
   *                  automaton in the encoded state tuple.
   */
  public void set(final int[] encoded, final int autIndex, final int value)
  {
    final AutomatonInfo info = mAutomatonInfo[autIndex];
    final int wordIndex = info.getWordIndex();
    final int bitOffset = info.getBitOffset();
    final int mask = info.getMask();
    encoded[wordIndex] = (encoded[wordIndex] & ~mask) | (value << bitOffset);
  }


  //#########################################################################
  //# Inner Class AutomatonInfo
  private static class AutomatonInfo
  {
    //#######################################################################
    //# Constructor
    private AutomatonInfo(final int autIndex,
                          final int numBits,
                          final int wordIndex,
                          final int bitOffset)
    {
      mAutomatonIndex = autIndex;
      mMask = ((1 << numBits) - 1) << bitOffset;
      mWordIndex = wordIndex;
      mBitOffset = bitOffset;
    }

    //#######################################################################
    //# Simple Access
    private int getAutomatonIndex()
    {
      return mAutomatonIndex;
    }

    private int getMask()
    {
      return mMask;
    }

    private int getWordIndex()
    {
      return mWordIndex;
    }

    private int getBitOffset()
    {
      return mBitOffset;
    }

    //#######################################################################
    //# Data Members
    private final int mAutomatonIndex;
    private final int mMask;
    private final int mWordIndex;
    private final int mBitOffset;
  }


  //#########################################################################
  //# Data Members
  private final AutomatonInfo[] mAutomatonInfo;
  private final List<List<AutomatonInfo>> mWordInfo;


  //#########################################################################
  //# Class Constants
  private static final int SIZE_INT = 32;

}
