//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.monolithic
//# CLASS:   NativeControllabilityCheckerTest
//###########################################################################
//# $Id: EarlyNotAcceptHeuristicTest.java,v 1.4 2007-01-03 00:49:08 robi Exp $
//###########################################################################

package net.sourceforge.waters.analysis.modular;

import net.sourceforge.waters.cpp.analysis.NativeControllabilityChecker;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.model.analysis.AbstractControllabilityCheckerTest;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


public class EarlyNotAcceptHeuristicTest
  extends AbstractControllabilityCheckerTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite() {
    TestSuite testSuite =
      new TestSuite(EarlyNotAcceptHeuristicTest.class);
    return testSuite;
  }

  public static void main(final String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }

  //#########################################################################
  //# Overrides for abstract base class
  //# net.sourceforge.waters.analysis.AbstractModelVerifierTest
  protected ModularControllabilityChecker createModelVerifier(final ProductDESProxyFactory factory)
  {
    return new ModularControllabilityChecker(null, factory,
                                             new NativeControllabilityChecker(null, factory),
                                             new EarlyNotAcceptHeuristic(HeuristicType.NOPREF), false);
  }
}
