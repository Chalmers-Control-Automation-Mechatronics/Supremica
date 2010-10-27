//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.bdd
//# CLASS:   BDDPackageTest
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.analysis.bdd;

import java.lang.reflect.Method;

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
    throws SecurityException, NoSuchMethodException
  {
    testBDDPackage(BDDPackage.JAVA);
  }

  public void testBDDPackage_buddy()
    throws SecurityException, NoSuchMethodException
  {
    testBDDPackage(BDDPackage.BUDDY);
  }

  public void testBDDPackage_cudd()
    throws SecurityException, NoSuchMethodException
  {
    testBDDPackage(BDDPackage.CUDD);
  }

  public void testBDDPackage_cal()
    throws SecurityException, NoSuchMethodException
  {
    testBDDPackage(BDDPackage.CAL);
  }

  public void testEmptyReorder_java()
    throws SecurityException, NoSuchMethodException
  {
    testEmptyReorder(BDDPackage.JAVA);
  }

  public void testEmptyReorder_buddy()
    throws SecurityException, NoSuchMethodException
  {
    testEmptyReorder(BDDPackage.BUDDY);
  }

  public void testEmptyReorder_cudd()
    throws SecurityException, NoSuchMethodException
  {
    testEmptyReorder(BDDPackage.CUDD);
  }


  //#########################################################################
  //# Debug Output
  public void silentBDDHandler(final Object dummy1, final Object dummy2)
  {
  }


  //#########################################################################
  //# Auxiliary Methods
  private void testBDDPackage(final BDDPackage pack)
    throws SecurityException, NoSuchMethodException
  {
    final BDDFactory factory = loadBDDPackage(pack);
    factory.done();
  }

  private void testEmptyReorder(final BDDPackage pack)
    throws SecurityException, NoSuchMethodException
  {
    final BDDFactory factory = loadBDDPackage(pack);
    factory.reorder(BDDFactory.REORDER_SIFT);
    factory.done();
  }

  private BDDFactory loadBDDPackage(final BDDPackage pack)
    throws SecurityException, NoSuchMethodException
  {
    final String name = pack.getBDDPackageName();
    final int initnodes = 10000; // breaks BuDDy at 57600 ???
    final BDDFactory factory = BDDFactory.init(name, initnodes, initnodes >> 1);
    final Class<?>[] parameterTypes =
      new Class<?>[] {Object.class, Object.class};
    final Method method =
      getClass().getMethod("silentBDDHandler", parameterTypes);
    factory.registerGCCallback(this, method);
    factory.registerReorderCallback(this, method);
    factory.registerResizeCallback(this, method);
    return factory;
  }

}
