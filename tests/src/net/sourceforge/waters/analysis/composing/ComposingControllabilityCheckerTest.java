//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.modular
//# CLASS:   ComposingControllabilityCheckerTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.composing;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.model.analysis.AbstractControllabilityCheckerTest;
import net.sourceforge.waters.model.analysis.ControllabilityChecker;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;

public class ComposingControllabilityCheckerTest
  extends AbstractControllabilityCheckerTest {

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    TestSuite testSuite =
      new TestSuite(ComposingControllabilityCheckerTest.class);
    return testSuite;
  }

  public static void main(final String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }

  //#########################################################################
  //# Overrides for abstract base class
  //# net.sourceforge.waters.analysis.AbstractModelVerifierTest
  protected ControllabilityChecker createModelVerifier
    (final ProductDESProxyFactory factory)
  {
    return
      new ComposingControllabilityChecker(null, factory);
  }

}
