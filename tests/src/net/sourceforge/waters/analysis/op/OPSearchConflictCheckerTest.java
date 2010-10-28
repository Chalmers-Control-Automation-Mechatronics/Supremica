//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.op
//# CLASS:   OPSearchConflictCheckerTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.op;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.model.analysis.AbstractStandardConflictCheckerTest;
import net.sourceforge.waters.model.analysis.ConflictChecker;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


public class OPSearchConflictCheckerTest extends
    AbstractStandardConflictCheckerTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite testSuite = new TestSuite(OPSearchConflictCheckerTest.class);
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
    final OPConflictChecker checker = new OPConflictChecker(factory);
    checker.setMethod(OPConflictChecker.Method.OPSEARCH);
    checker.setInternalStepNodeLimit(50000);
    checker.setFinalStepNodeLimit(100000);
    checker.setInternalStepTransitionLimit(500000);
    return checker;
  }

}
