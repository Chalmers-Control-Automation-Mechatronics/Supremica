//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.tr
//# CLASS:   WatersIntHeapTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.tr;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.junit.AbstractWatersTest;

/**
 * @author Robi Malik
 */

public class WatersIntHeapTest extends AbstractWatersTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite testSuite = new TestSuite(WatersIntHeapTest.class);
    return testSuite;
  }

  public static void main(final String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }


  //#########################################################################
  //# Test Cases
  public void testIndividualAddition()
  {
    final int numItems = 10000;
    final WatersIntHeap heap = new WatersIntHeap();
    int i = 37;
    for (i = 37; i != 0; i = (i + 37) % numItems) {
      heap.add(i);
    }
    for (i = 1; i < numItems; i++) {
      final int removed = heap.removeFirst();
      assertEquals("Unexpected value from heap!", i, removed);
    }
  }

  public void testFromArray()
  {
    final int numItems = 10000;
    final int[] items = new int[numItems - 1];
    int i = 37;
    int j;
    for (i = 37, j = 0; i != 0; i = (i + 37) % numItems, j++) {
      items[j] = i;
    }
    final WatersIntHeap heap = new WatersIntHeap(items);
    for (i = 1; i < numItems; i++) {
      final int removed = heap.removeFirst();
      assertEquals("Unexpected value from heap!", i, removed);
    }
  }

}