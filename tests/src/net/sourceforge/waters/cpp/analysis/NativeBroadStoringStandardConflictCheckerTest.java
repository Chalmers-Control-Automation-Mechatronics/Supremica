//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters C++
//# PACKAGE: net.sourceforge.waters.cpp.analysis
//# CLASS:   NativeBroadStoringStandardConflictCheckerTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.cpp.analysis;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.model.analysis.des.ConflictChecker;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


public class NativeBroadStoringStandardConflictCheckerTest
  extends AbstractNativeStandardConflictCheckerTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite suite =
      new TestSuite(NativeBroadStoringStandardConflictCheckerTest.class);
    return suite;
  }

  public static void main(final String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }


  //#########################################################################
  //# Overrides for abstract base class
  //# net.sourceforge.waters.analysis.AbstractModelVerifierTest
  @Override
  protected ConflictChecker
    createModelVerifier(final ProductDESProxyFactory factory)
  {
    final NativeConflictChecker checker = new NativeConflictChecker(factory);
    checker.setExplorerMode(ExplorerMode.BROAD);
    checker.setConflictCheckMode(ConflictCheckMode.STORED_BACKWARDS_TRANSITIONS);
    return checker;
  }

}
