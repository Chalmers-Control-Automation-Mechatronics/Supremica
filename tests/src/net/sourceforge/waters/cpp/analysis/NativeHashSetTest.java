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

import java.util.Arrays;
import java.util.Set;
import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.junit.AbstractWatersTest;


public class NativeHashSetTest extends AbstractWatersTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite() { 
    TestSuite testSuite =
      new TestSuite(NativeHashSetTest.class);     
    return testSuite;
  }

  public static void main(final String[] args)
  {
    junit.textui.TestRunner.run(suite()); 
  }


  //#########################################################################
  //# Overrides for base class junit.framework.TestCase
  public void setUp() throws Exception
  {
    super.setUp();
    mTestData = new Integer[TESTSIZE];
    for (int i = 0; i < TESTSIZE; i++) {
      mTestData[i] = i * i;
    }
  } 

  public void tearDown() throws Exception
  {
    mTestData = null;
    super.tearDown();
  } 


  //#########################################################################
  //# Test Cases
  public void testCreateEmptySet()
  {
    final Set<Integer> set = new NativeHashSet<Integer>();
    assertEquals(set.size(), 0);
  }

  public void testLoadAndLookup()
  {
    final Set<Integer> set = new NativeHashSet<Integer>();
    for (int i = 0; i < TESTSIZE; i++) {
      assertEquals(i, set.size());
      final boolean added = set.add(mTestData[i]);
      assertTrue("Unexpected result from add(" + i + ")!", added);
    }
    for (int i = 0; i < TESTSIZE; i++) {
      final int item = mTestData[i];
      final boolean found = set.contains(item);
      assertTrue("Added item " + item + " not found!", found);
    }
  }

  public void testLoadAndIterate()
  {
    final Set<Integer> set = new NativeHashSet<Integer>();
    for (int i = 0; i < TESTSIZE; i++) {
      assertEquals(i, set.size());
      final boolean added = set.add(mTestData[TESTSIZE - i - 1]);
      assertTrue("Unexpected result from add(" + i + ")!", added);
    }
    boolean found[] = new boolean[TESTSIZE];
    for (int i = 0; i < TESTSIZE; i++) {
      found[i] = false;
    }
    int count = 0;
    for (final Integer item : set) {
      final int pos = Arrays.binarySearch(mTestData, item);
      assertTrue("Unexpected item " + item + " in iteratiom!", pos >= 0);
      assertFalse("Duplicate item " + item + " in iteratiom!", found[pos]);
      found[pos] = true;
      count++;
    }
    assertEquals(TESTSIZE, count);
  }

  public void testNotContained()
  {
    final Set<Integer> set = new NativeHashSet<Integer>();
    final int THISSIZE = TESTSIZE / 4;
    for (int i = 0; i < THISSIZE; i++) {
      assertEquals(i, set.size());
      final boolean added = set.add(mTestData[i]);
      assertTrue("Unexpected result from add()!", added);
    }
    int current = -5;
    for (int i = 0; i < THISSIZE; i++) {
      final int next = mTestData[i];
      for (; current < next; current++) {
        final boolean found = set.contains(current);
        assertFalse("Found item " + current + " that was not added!", found);
      }
      final boolean found = set.contains(next);
      assertTrue("Added item " + next + " not found!", found);
      current = next + 1;
    }
  }

  public void testMultipleAdd()
  {
    final Set<Integer> set = new NativeHashSet<Integer>();
    for (int i = 0; i < TESTSIZE; i++) {
      assertEquals(i, set.size());
      final boolean added = set.add(mTestData[i]);
      assertTrue("Unexpected result from add()!", added);
    }
    for (int i = 0; i < TESTSIZE; i++) {
      assertEquals(TESTSIZE, set.size());
      final boolean added = set.add(mTestData[i]);
      assertFalse("Unexpected result from repeated add()!", added);
    }
  }

  public void testClear()
  {
    final int[] COUNTS = {10, 100, 1000, 500, 100};
    final Set<Integer> set = new NativeHashSet<Integer>();
    for (int run = 0; run < COUNTS.length; run++) {
      final int maxcount = COUNTS[run];
      set.clear();
      for (int i = 0; i < maxcount; i++) {
        assertEquals(i, set.size());
        final boolean added = set.add(mTestData[i]);
        assertTrue("Unexpected result from add()!", added);
      }
    }
  }

  public void testNull()
  {
    final Set<Integer> set = new NativeHashSet<Integer>();
    assertFalse(set.contains(0));
    assertTrue(set.add(0));
    assertTrue(set.contains(0));
    set.clear();
    assertFalse(set.contains(0));
  }

  public void testEquality()
  {
    final Set<Integer> set = new NativeHashSet<Integer>();
    assertTrue(set.add(0));
    assertTrue(set.contains(0));
    for (int i = 1; i < TESTSIZE; i++) {
      assertFalse(set.contains(i));
    }
  }


  //#########################################################################
  //# Data Members
  private Integer[] mTestData;


  //#########################################################################
  //# Constants
  private final int TESTSIZE = 1000;

}
