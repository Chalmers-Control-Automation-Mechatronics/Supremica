//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.monolithic
//# CLASS:   OneHeuristicTest
//###########################################################################
//# $Id: OneHeuristicTest.java,v 1.3 2006-12-01 02:16:43 siw4 Exp $
//###########################################################################

package net.sourceforge.waters.analysis.modular;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.cpp.analysis.NativeControllabilityChecker;
import net.sourceforge.waters.model.analysis.
       AbstractControllabilityCheckerTest;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


public class OneHeuristicTest
  extends AbstractControllabilityCheckerTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite() {
    TestSuite testSuite = new TestSuite(OneHeuristicTest.class);
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
    checker.setHeuristicMethod(ModularHeuristicFactory.Method.One);
    return checker;
  }

}
