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

package net.sourceforge.waters.cpp.analysis;

import gnu.trove.set.hash.TIntHashSet;
import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.junit.AbstractWatersTest;


/**
 * A JUnit test for the C++ class <CODE>BitSet</CODE> declared in
 * <CODE>waters/base/BitSet.h</CODE>. The C++ class is tested through the
 * Java encapsulation {@link NativeBitSet}.
 *
 * @author Robi Malik
 */

public class NativeBitSetTest extends AbstractWatersTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite() {
    final TestSuite testSuite =
      new TestSuite(NativeBitSetTest.class);
    return testSuite;
  }

  public static void main(final String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }


  //#########################################################################
  //# Test Cases
  public void testCreateEmptyBitSet()
  {
    for (int s = 0; s <= 128; s += 32) {
      testCreateEmptyBitSet(s);
      testCreateEmptyBitSet(s + 1);
      testCreateEmptyBitSet(s + 31);
    }
  }

  public void testCreateFullBitSet()
  {
    for (int s = 0; s <= 128; s += 32) {
      testCreateFullBitSet(s);
      testCreateFullBitSet(s + 1);
      testCreateFullBitSet(s + 31);
    }
  }

  public void testSetBit()
  {
    final int end = max(TEST_DATA) + 65;
    final NativeBitSet bitSet = new NativeBitSet(37);
    final TIntHashSet hashSet = new TIntHashSet(TEST_DATA.length);
    for (int i = 0; i < TEST_DATA.length; i++) {
      final int b = TEST_DATA[i];
      bitSet.setBit(b);
      hashSet.add(b);
      for (int t = 0; t < end; t++) {
        assertEquals("Unexpected bit value after setBit()!",
                     hashSet.contains(t), bitSet.get(t));
      }
    }
  }

  public void testClearBit()
  {
    final int end = max(TEST_DATA) + 65;
    final NativeBitSet bitSet = new NativeBitSet(end, true);
    final TIntHashSet hashSet = new TIntHashSet(TEST_DATA.length);
    for (int i = 0; i < TEST_DATA.length; i++) {
      final int b = TEST_DATA[i];
      bitSet.clearBit(b);
      hashSet.add(b);
      for (int t = 0; t < end; t++) {
        assertEquals("Unexpected bit value after clearBit()!",
                     !hashSet.contains(t), bitSet.get(t));
      }
    }
  }

  public void testEquals()
  {
    final NativeBitSet bitSet1 = new NativeBitSet(0);
    final NativeBitSet bitSet2 = new NativeBitSet(160);
    for (int i = 0; i < TEST_DATA.length; i++) {
      assertTrue("Equal bit sets not reported as equal!",
                 bitSet1.equals(bitSet2));
      assertTrue("Equal bit sets not reported as equal!",
                 bitSet2.equals(bitSet1));
      final int b = TEST_DATA[i];
      bitSet1.setBit(b);
      assertFalse("Different bit sets reported as equal!",
                  bitSet1.equals(bitSet2));
      assertFalse("Different bit sets reported as equal!",
                  bitSet2.equals(bitSet1));
      bitSet2.setBit(b);
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private void testCreateEmptyBitSet(final int size)
  {
    final NativeBitSet bitSet = new NativeBitSet(size);
    for (int b = 0; b <= size + 64; b++) {
      assertFalse("Unexpected 1-bit in empty bit set!", bitSet.get(b));
    }
  }

  private void testCreateFullBitSet(final int size)
  {
    final NativeBitSet bitSet = new NativeBitSet(size, true);
    for (int b = 0; b < size; b++) {
      assertTrue("Unexpected 0-bit in full bit set!", bitSet.get(b));
    }
    for (int b = size; b <= size + 64; b++) {
      assertFalse("Unexpected 1-bit after end of full bit set!", bitSet.get(b));
    }
  }

  private static int max(final int[] array)
  {
    int result = Integer.MIN_VALUE;
    for (int i = 0; i < array.length; i++) {
      if (array[i] > result) {
        result = array[i];
      }
    }
    return result;
  }


  //#########################################################################
  //# Class Constants
  private static int[] TEST_DATA = {
    0, 7, 32, 48, 63, 1, 64, 33, 96, 95, 100, 31, 127, 128, 77
  };

}








