//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.cpp.analysis
//# CLASS:   NativeBroadStoringConflictCheckerTest
//###########################################################################
//# $Id: NativeBroadStoringConflictCheckerTest.java 4825 2009-11-16 10:30:43Z robi $
//###########################################################################

package net.sourceforge.waters.cpp.analysis;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.cpp.analysis.ExplorerMode;
import net.sourceforge.waters.model.analysis.AbstractStandardConflictCheckerTest;
import net.sourceforge.waters.model.analysis.ConflictChecker;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


public class NativeStandardBroadStoringConflictCheckerTest
  extends AbstractStandardConflictCheckerTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite suite =
      new TestSuite(NativeStandardBroadStoringConflictCheckerTest.class);
    return suite;
  }

  public static void main(final String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }


  //#########################################################################
  //# Overrides for abstract base class
  //# net.sourceforge.waters.analysis.AbstractModelVerifierTest
  protected ConflictChecker
    createModelVerifier(final ProductDESProxyFactory factory)
  {
    final NativeConflictChecker checker = new NativeConflictChecker(factory);
    checker.setExplorerMode(ExplorerMode.BROAD);
    return checker;
  }

}
