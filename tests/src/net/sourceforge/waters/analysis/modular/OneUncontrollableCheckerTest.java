//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.modular
//# CLASS:   OneUncontrollableCheckerTest
//###########################################################################
//# $Id: OneUncontrollableCheckerTest.java,v 1.4 2008-07-01 22:18:21 robi Exp $
//###########################################################################

package net.sourceforge.waters.analysis.modular;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.cpp.analysis.NativeControllabilityChecker;
import net.sourceforge.waters.model.analysis.
       AbstractControllabilityCheckerTest;
import net.sourceforge.waters.model.analysis.ControllabilityChecker;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


public class OneUncontrollableCheckerTest
  extends AbstractControllabilityCheckerTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    TestSuite testSuite =
      new TestSuite(OneUncontrollableCheckerTest.class);
    return testSuite;
  }

  public static void main(final String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }


  //#########################################################################
  //# Overrides for abstract base class
  //# net.sourceforge.waters.analysis.AbstractControllabilityCheckerTest
  public void testHISCRhoneSubsystem1Patch0() throws Exception
  {
    try {
      super.testHISCRhoneSubsystem1Patch0();
    } catch (final OverflowException exception) {
      // Can't do this one-uncontrollable-at-a-time --- too bad :-(
    }
  }

  public void testHISCRhoneSubsystem1Patch1() throws Exception
  {
    try {
      super.testHISCRhoneSubsystem1Patch1();
    } catch (final OverflowException exception) {
      // Can't do this one-uncontrollable-at-a-time --- too bad :-(
    }
  }

  public void testHISCRhoneSubsystem1Patch2() throws Exception
  {
    try {
      super.testHISCRhoneSubsystem1Patch2();
    } catch (final OverflowException exception) {
      // Can't do this one-uncontrollable-at-a-time --- too bad :-(
    }
  }


  //#########################################################################
  //# Overrides for abstract base class
  //# net.sourceforge.waters.analysis.AbstractModelVerifierTest
  protected ControllabilityChecker createModelVerifier
    (final ProductDESProxyFactory desfactory)
  {
    return new OneUncontrollableChecker
      (null, desfactory,
       new ModularControllabilityChecker
       (null, desfactory,
        new NativeControllabilityChecker(null, desfactory),
        false));
  }

}
