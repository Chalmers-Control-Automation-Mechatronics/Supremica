//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.op
//# CLASS:   ObserverProjectionConflictCheckerTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.op;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.model.analysis.AbstractStandardConflictCheckerTest;
import net.sourceforge.waters.model.analysis.ConflictChecker;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


public class ObserverProjectionConflictCheckerTest extends
    AbstractStandardConflictCheckerTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite testSuite =
        new TestSuite(ObserverProjectionConflictCheckerTest.class);
    return testSuite;
  }

  public static void main(final String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }


  //#########################################################################
  //# Overrides for abstract base class
  //# net.sourceforge.waters.analysis.AbstractModelVerifierTest
  protected ConflictChecker createModelVerifier
    (final ProductDESProxyFactory factory)
  {
    final ObserverProjectionConflictChecker checker =
      new ObserverProjectionConflictChecker(factory);
    checker.setInternalStepNodeLimit(5000);
    checker.setFinalStepNodeLimit(100000);
    return checker;
  }

}
