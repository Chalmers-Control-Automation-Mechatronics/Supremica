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
    final int[] data3 = {2, 4};
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
    data1.add(1);
    data1.add(2);
    data1.add(3);
    final TIntArrayList data2 = new TIntArrayList(2);
    data2.add(7);
    final TIntArrayList data3 = new TIntArrayList(6);
    data3.add(1);
    data3.add(3);
    data3.add(4);
    data3.add(5);
    data3.add(6);
    data3.add(7);
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

  public void testSmallArray1()
  {
    final IntSetBuffer buffer = new IntSetBuffer(4);
    final int[] data = {1, 2, 3};
    addArray(buffer, data);
  }

  public void testSmallArrayList1()
  {
    final IntSetBuffer buffer = new IntSetBuffer(4);
    final int[] data = {1, 2, 3};
    addArrayList(buffer, data);
  }

  public void testSmallArray2()
  {
    final IntSetBuffer buffer = new IntSetBuffer(18);
    final int[] data = {0, 1, 2, 4, 9, 14, 15};
    addArray(buffer, data);
  }

  public void testSmallArrayList2()
  {
    final IntSetBuffer buffer = new IntSetBuffer(18);
    final int[] data = {0, 1, 2, 4, 9, 14, 15};
    addArrayList(buffer, data);
  }

  public void testSmallArray3()
  {
    final IntSetBuffer buffer = new IntSetBuffer(1513);
    final int[] data =
      {1, 3, 5, 6, 140, 142, 144, 145, 147, 149, 151, 153, 155, 157, 158,
       159, 161, 163, 165, 167, 169, 171, 172, 173, 175, 177, 179, 181, 183,
       185, 186, 187, 189, 191, 193, 195, 197, 199, 200, 201, 203, 205, 207,
       209, 211, 213, 214, 215, 217, 219, 221, 223, 225, 227, 228, 229, 231,
       233, 235, 237, 239, 241, 242, 244, 246, 248, 250, 252, 254, 256, 257,
       259, 261, 263, 265, 267, 269, 270, 271, 273, 275, 277, 279, 281, 283,
       284, 285, 287, 289, 291, 293, 295, 297, 298, 299, 301, 303, 305, 307,
       309, 311, 313, 314, 316, 318, 320, 322, 324, 326, 327, 328, 330, 332,
       334, 336, 338, 340, 341, 342, 344, 346, 348, 350, 352, 354, 355, 356,
       358, 360, 362, 364, 366, 374, 470, 472, 474, 476, 477, 478, 479, 480,
       481, 482, 483, 484, 485, 486, 487, 488, 489, 490, 491, 492, 493, 494,
       495, 496, 497, 498, 499, 500, 501, 502, 503, 504, 505, 506, 507, 508,
       509, 510, 511, 512, 513, 514, 515, 516, 517, 518, 519, 520, 521, 522,
       523, 524, 525, 526, 527, 528, 529, 530, 531, 532, 533, 534, 535, 536,
       537, 538, 539, 540, 541, 542, 543, 544, 545, 546, 547, 548, 549, 550,
       551, 552, 553, 554, 555, 556, 557, 558, 559, 560, 561, 562, 563, 564,
       565, 566, 567, 568, 569, 570, 571, 572, 573, 574, 575, 576, 577, 578,
       579, 580, 581, 582, 583, 584, 585, 586, 587, 588, 589, 590, 591, 592,
       593, 594, 595, 596, 597, 598, 599};
    addArray(buffer, data);
  }

  public void testBigIterator()
  {
    final int MAXCOUNT = 100;
    final IntSetBuffer buffer = new IntSetBuffer(MAXCOUNT + 1);
    final IntSetBuffer.IntSetIterator iter = buffer.iterator();
    final TIntArrayList list = new TIntArrayList(MAXCOUNT);
    for (int count = 0; count <= MAXCOUNT; count++) {
      if (count > 0) {
        list.add(count);
      }
      final int offset = buffer.add(list);
      iter.reset(offset);
      for (int i = 1; i <= count; i++) {
        assertTrue("Premature end of iteration!", iter.advance());
        assertEquals("Unexpected data in iteration!",
                     i, iter.getCurrentData());
      }
      assertFalse("Expected end of iteration not signalled!", iter.advance());
      assertEquals("Unexpected set size!", count, buffer.size(offset));
    }
  }


  //#########################################################################
  //# Auxiliary Method
  private void addArray(final IntSetBuffer buffer, final int[] data)
  {
    final int offset = buffer.add(data);
    final WatersIntIterator iter = buffer.iterator(offset);
    for (int i = 0; i < data.length; i++) {
      assertTrue("Premature end of iteration!", iter.advance());
      final int value = iter.getCurrentData();
      assertEquals("Unexpected data in iteration!", data[i], value);
    }
    assertFalse("Expected end of iteration not signalled!", iter.advance());
  }

  private void addArrayList(final IntSetBuffer buffer, final int[] data)
  {
    final TIntArrayList list = new TIntArrayList(data.length);
    for (final int d : data) {
      list.add(d);
    }
    final int offset = buffer.add(list);
    final WatersIntIterator iter = buffer.iterator(offset);
    for (int i = 0; i < data.length; i++) {
      assertTrue("Premature end of iteration!", iter.advance());
      final int value = iter.getCurrentData();
      assertEquals("Unexpected data in iteration!", data[i], value);
    }
    assertFalse("Expected end of iteration not signalled!", iter.advance());
  }

}
