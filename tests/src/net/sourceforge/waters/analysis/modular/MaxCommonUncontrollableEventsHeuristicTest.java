//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.monolithic
//# CLASS:   NativeControllabilityCheckerTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.modular;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.model.analysis.
       AbstractControllabilityCheckerTest;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


public class MaxCommonUncontrollableEventsHeuristicTest
  extends AbstractControllabilityCheckerTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite() {
    TestSuite testSuite =
      new TestSuite(MaxCommonUncontrollableEventsHeuristicTest.class);
    return testSuite;
  }

  public static void main(final String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }

  //#########################################################################
  //# Overrides for abstract base class
  //# net.sourceforge.waters.analysis.AbstractModelVerifierTest
  protected ModularControllabilityChecker createModelVerifier
    (final ProductDESProxyFactory desfactory)
  {
    final ModularModelVerifierFactory checkerfactory =
      ModularModelVerifierFactory.getInstance();
    final ModularControllabilityChecker checker =
      checkerfactory.createControllabilityChecker(desfactory);
    checker.setHeuristicMethod
      (ModularHeuristicFactory.Method.MaxCommonUncontrollableEvents);
    return checker;
  }

}
