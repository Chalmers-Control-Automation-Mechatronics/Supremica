//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.op
//# CLASS:   NBAbstractionStandardConflictCheckerTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.op;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.model.analysis.AbstractStandardConflictCheckerTest;
import net.sourceforge.waters.model.analysis.ConflictChecker;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


public class NBAbstractionStandardConflictCheckerTest
  extends AbstractStandardConflictCheckerTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite testSuite =
      new TestSuite(NBAbstractionStandardConflictCheckerTest.class);
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
    checker.setAbstractionMethod(OPConflictChecker.AbstractionMethod.NB);
    checker.setInternalStepNodeLimit(5000);
    checker.setFinalStepNodeLimit(100000);
    checker.setInternalStepTransitionLimit(500000);
    //checker.setPreselectingMethod(OPConflictChecker.PreselectingMethod.MaxS);
    //checker.setSelectingMethod(OPConflictChecker.SelectingMethod.MaxL);
    return checker;
  }

}
