//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.monolithic
//# CLASS:   BlockedArrayListTest
//###########################################################################
//# $Id: BlockedArrayListTest.java,v 1.1 2006-11-03 05:18:29 robi Exp $
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
