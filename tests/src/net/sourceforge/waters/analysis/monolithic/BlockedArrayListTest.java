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

import java.math.BigInteger;
import java.util.Random;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


public class BlockedArrayListTest extends TestCase {

  private BlockedArrayList<BigInteger> ba;
  private BigInteger                   item;

  protected void setUp()
  {
    ba = new BlockedArrayList<BigInteger>(BigInteger.class);  
    item = BigInteger.valueOf(123456789);
  }

  public void testAdd()
  {    
    for (int i = 0; i<1000000; i++){
      assertEquals(true, ba.add(item));
    }
  }
  
  public void testGet()
  {
    for (int i = 0; i<1000000; i++){
      ba.add(item);
    }
    assertEquals(item, ba.get(0));
    assertEquals(item, ba.get(999999));
  }

  public void testSize()
  {
    assertEquals(0, ba.size());
    ba.add(item);
    assertEquals(1, ba.size());
    for (int i = 1; i < 1000000; i++) {
      ba.add(item);
    }
    assertEquals(1000000, ba.size());
  }

  public void testRandomAddGet()
  {
    final int COUNT = 5000;
    final int SEED = 20060703;
    Random source = new Random(SEED);
    for (int i = 0; i < COUNT; i++) {
      final int value = source.nextInt();
      final BigInteger item = BigInteger.valueOf(value);
      assertTrue(ba.add(item));
      assertEquals(ba.size(), i + 1);
      final BigInteger got = ba.get(i);
      assertEquals(item, got);
    }
    source = new Random(SEED);
    for (int i = 0; i < COUNT; i++) {
      final int value = source.nextInt();
      final BigInteger expected = BigInteger.valueOf(value);
      final BigInteger got = ba.get(i);
      assertEquals(expected, got);
    }
  }

  public static Test suite()
  { 
    TestSuite testSuite = new TestSuite(BlockedArrayListTest.class);     
    return testSuite;
  }

  public static void main(String[] args)
  {
    junit.textui.TestRunner.run(suite()); 
  }

}








