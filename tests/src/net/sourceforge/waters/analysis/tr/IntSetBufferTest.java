//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.tr
//# CLASS:   IntSetBufferTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.tr;

import java.util.Random;

import gnu.trove.TIntArrayList;
import gnu.trove.TIntHashSet;
import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.junit.AbstractWatersTest;


/**
 * @author Robi Malik
 */

public class IntSetBufferTest extends AbstractWatersTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite testSuite = new TestSuite(IntSetBufferTest.class);
    return testSuite;
  }

  public static void main(final String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }


  //#########################################################################
  //# Test Cases
  public void testAddArrays()
  {
    final IntSetBuffer buffer = new IntSetBuffer(4);
    final int[] data1 = {1, 2, 3};
    final int[] data2 = {1, 2};
    final int[] data3 = {4, 2};
    final int offset1 = buffer.add(data1);
    final int offset2 = buffer.add(data2);
    assertFalse("Got the same offset for different sets!", offset1 == offset2);
    final int offset3 = buffer.add(data3);
    assertFalse("Got the same offset for different sets!", offset1 == offset3);
    assertFalse("Got the same offset for different sets!", offset2 == offset3);
    final int alt1 = buffer.add(data1);
    assertEquals("Unexpected offset for existing set!", offset1, alt1);
    final int alt2 = buffer.add(data2);
    assertEquals("Unexpected offset for existing set!", offset2, alt2);
    final int alt3 = buffer.add(data3);
    assertEquals("Unexpected offset for existing set!", offset3, alt3);
  }

  public void testAddArrayLists()
  {
    final IntSetBuffer buffer = new IntSetBuffer(7);
    final TIntArrayList data1 = new TIntArrayList(3);
    data1.add(3);
    data1.add(2);
    data1.add(1);
    final TIntArrayList data2 = new TIntArrayList(2);
    data2.add(7);
    final TIntArrayList data3 = new TIntArrayList(6);
    data3.add(1);
    data3.add(7);
    data3.add(4);
    data3.add(3);
    data3.add(5);
    data3.add(6);
    final int offset1 = buffer.add(data1);
    final int offset2 = buffer.add(data2);
    assertFalse("Got the same offset for different sets!", offset1 == offset2);
    final int offset3 = buffer.add(data3);
    assertFalse("Got the same offset for different sets!", offset1 == offset3);
    assertFalse("Got the same offset for different sets!", offset2 == offset3);
    final int alt1 = buffer.add(data1);
    assertEquals("Unexpected offset for existing set!", offset1, alt1);
    final int alt2 = buffer.add(data2);
    assertEquals("Unexpected offset for existing set!", offset2, alt2);
    final int alt3 = buffer.add(data3);
    assertEquals("Unexpected offset for existing set!", offset3, alt3);
    final TIntArrayList data3a = new TIntArrayList(6);
    data3a.add(7);
    data3a.add(6);
    data3a.add(5);
    data3a.add(4);
    data3a.add(3);
    data3a.add(1);
    final int alt3a = buffer.add(data3a);
    assertEquals("Unexpected offset for existing set!", offset3, alt3a);
  }

  public void testAddHashSets()
  {
    final int MAXVAL = 255;
    final int COUNT = 100;
    final Random rand = new Random(0xabababab);
    final IntSetBuffer buffer = new IntSetBuffer(MAXVAL);
    final TIntHashSet[] sets = new TIntHashSet[COUNT];
    final int[] offsets = new int[COUNT];
    for (int i = 0; i < COUNT; i++) {
      final TIntHashSet set = new TIntHashSet(COUNT);
      final int size = rand.nextInt(MAXVAL + 1);
      for (int j = 0; j < size; j++) {
        final int val = rand.nextInt(MAXVAL + 1);
        set.add(val);
      }
      sets[i] = set;
      offsets[i] = buffer.add(set);
    }
    for (int i = 0; i < COUNT; i++) {
      final int alt = buffer.add(sets[i]);
      assertEquals("Unexpected offset for existing set!", offsets[i], alt);
    }
  }

  public void testSmallIterator()
  {
    final IntSetBuffer buffer = new IntSetBuffer(4);
    final int[] data = {3, 2, 1};
    final int offset = buffer.add(data);
    final WatersIntIterator iter = buffer.iterator(offset);
    assertTrue("Premature end of iteration!", iter.advance());
    assertEquals("Unexpected data in iteration!", 1, iter.getCurrentData());
    assertTrue("Premature end of iteration!", iter.advance());
    assertEquals("Unexpected data in iteration!", 2, iter.getCurrentData());
    assertTrue("Premature end of iteration!", iter.advance());
    assertEquals("Unexpected data in iteration!", 3, iter.getCurrentData());
    assertFalse("Expected end of iteration not signalled!", iter.advance());
  }

  public void testBigIterator()
  {
    final int MAXCOUNT = 100;
    final IntSetBuffer buffer = new IntSetBuffer(MAXCOUNT - 1);
    final IntSetBuffer.IntSetIterator iter = buffer.iterator();
    final TIntArrayList list = new TIntArrayList(MAXCOUNT);
    for (int count = 0; count <= MAXCOUNT; count++) {
      for (int i = count - 1; i >= 0; i--) {
        list.add(i);
      }
      final int offset = buffer.add(list);
      list.clear();
      iter.reset(offset);
      for (int i = 0; i < count; i++) {
        assertTrue("Premature end of iteration!", iter.advance());
        assertEquals("Unexpected data in iteration!", i, iter.getCurrentData());
      }
      assertFalse("Expected end of iteration not signalled!", iter.advance());
      assertEquals("Unexpected set size!", count, buffer.size(offset));
    }
  }

}
