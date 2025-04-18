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

import java.util.Arrays;
import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.junit.AbstractWatersTest;


/**
 * @author Robi Malik
 */

public class IntListBufferTest extends AbstractWatersTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite testSuite = new TestSuite(IntListBufferTest.class);
    return testSuite;
  }

  public static void main(final String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }


  //#########################################################################
  //# Test Cases
  public void testCopyList1()
  {
    final IntListBuffer buffer = new IntListBuffer();
    copyList(buffer, DATA1);
  }

  public void testCopyList2()
  {
    final IntListBuffer buffer = new IntListBuffer();
    copyList(buffer, DATA2);
  }

  public void testCopyList3()
  {
    final IntListBuffer buffer = new IntListBuffer();
    copyList(buffer, DATA3);
  }

  public void testTransferList1()
  {
    final IntListBuffer buffer1 = new IntListBuffer();
    final IntListBuffer buffer2 = new IntListBuffer();
    copyList(buffer1, buffer2, DATA1);
  }

  public void testTransferList2()
  {
    final IntListBuffer buffer1 = new IntListBuffer();
    final IntListBuffer buffer2 = new IntListBuffer();
    copyList(buffer1, buffer2, DATA2);
  }

  public void testTransferList3()
  {
    final IntListBuffer buffer1 = new IntListBuffer();
    final IntListBuffer buffer2 = new IntListBuffer();
    copyList(buffer1, buffer2, DATA3);
  }


  //#########################################################################
  //# Auxiliary Methods
  private void copyList(final IntListBuffer buffer, final int[] data)
  {
    final int list = buffer.createList(data);
    final int copy = buffer.copy(list);
    final int[] copyData = buffer.toArray(copy);
    assertNotSame("Copy is the same as original!", list, copy);
    assertTrue("Unexpected data after copy!", Arrays.equals(data, copyData));
    buffer.dispose(list);
    final int[] copyData2 = buffer.toArray(copy);
    assertTrue("Unexpected data after dispose!",
               Arrays.equals(data, copyData2));
  }

  private void copyList(final IntListBuffer buffer1,
                        final IntListBuffer buffer2,
                        final int[] data)
  {
    final int list = buffer1.createList(data);
    final int copy = buffer2.copy(list, buffer1);
    final int[] copyData = buffer2.toArray(copy);
    assertTrue("Unexpected data after copy!", Arrays.equals(data, copyData));
  }


  //#########################################################################
  //# Class Constants
  private static final int[] DATA1 = {};
  private static final int[] DATA2 = {1};
  private static final int[] DATA3 =
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

}
