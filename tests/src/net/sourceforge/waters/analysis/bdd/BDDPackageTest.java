//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.bdd
//# CLASS:   BDDPackageTest
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.analysis.bdd;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sf.javabdd.BDDFactory;
import net.sourceforge.waters.junit.AbstractWatersTest;


public class BDDPackageTest extends AbstractWatersTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite testSuite = new TestSuite(BDDPackageTest.class);
    return testSuite;
  }

  public static void main(final String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }


  //#########################################################################
  //# Test Cases
  public void testBDDPackage_java()
  {
    testBDDPackage("java");
  }

  public void testBDDPackage_buddy()
  {
    testBDDPackage("buddy");
  }

  public void testBDDPackage_cudd()
  {
    testBDDPackage("cudd");
  }

  public void testBDDPackage_cal()
  {
    testBDDPackage("cal");
  }

  public void testEmptyReorder_java()
  {
    testEmptyReorder("java");
  }

  /*
  public void testEmptyReorder_buddy()
  {
    // crashes :-(
    testEmptyReorder("buddy");
  }
  */

  public void testEmptyReorder_cudd()
  {
    testEmptyReorder("cudd");
  }


  //#########################################################################
  //# Auxiliary Methods
  private void testBDDPackage(final String name)
  {
    final BDDFactory factory = loadBDDPackage(name);
    factory.done();
  }

  private void testEmptyReorder(final String packname)
  {
    final BDDFactory factory = loadBDDPackage(packname);
    factory.reorder(BDDFactory.REORDER_SIFT);
    factory.done();
  }

  private BDDFactory loadBDDPackage(final String name)
  {
    final int initnodes = 10000; // breaks BuDDy at 57600 ???
    return BDDFactory.init(name, initnodes, initnodes >> 1);
  }

}
