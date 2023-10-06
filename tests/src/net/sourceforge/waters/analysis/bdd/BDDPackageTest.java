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
  public void testBDDLibrary_buddy()
  {
    System.loadLibrary("buddy");
  }

  public void testBDDLibrary_cudd()
  {
    System.loadLibrary("cudd");
  }

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

  public void testBDDPackage_buddy_after_jdd()
    throws SecurityException, NoSuchMethodException
  {
    try {
      System.loadLibrary("jdd");
      testBDDPackage(BDDPackage.BUDDY);
    } catch (final UnsatisfiedLinkError error) {
      // No jdd.dll --- no problem.
    }
  }

  public void testBDDPackage_buddy_before_jdd()
    throws SecurityException, NoSuchMethodException
  {
    try {
      testBDDPackage(BDDPackage.BUDDY);
      System.loadLibrary("jdd");
    } catch (final UnsatisfiedLinkError error) {
      // No jdd.dll --- no problem.
    }
  }

  public void testBDDPackage_cudd()
    throws SecurityException, NoSuchMethodException
  {
    testBDDPackage(BDDPackage.CUDD);
  }

  /*
  public void testBDDPackage_cal()
    throws SecurityException, NoSuchMethodException
  {
    testBDDPackage(BDDPackage.CAL);
  }
  */

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
