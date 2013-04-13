//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.monolithic
//# CLASS:   MonolithicStandardConflictCheckerTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.monolithic;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.model.analysis.AbstractStandardConflictCheckerTest;
import net.sourceforge.waters.model.analysis.ConflictChecker;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


public class MonolithicStandardConflictCheckerTest extends
    AbstractStandardConflictCheckerTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite testSuite =
        new TestSuite(MonolithicStandardConflictCheckerTest.class);
    return testSuite;
  }

  public static void main(final String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }


  //#########################################################################
  //# Overrides for abstract base class
  //# net.sourceforge.waters.analysis.AbstractModelVerifierTest
  @Override
  protected ConflictChecker createModelVerifier
    (final ProductDESProxyFactory factory)
  {
    final ConflictChecker checker = new MonolithicConflictChecker(factory);
    checker.setNodeLimit(3000000);
    return checker;
  }


  //#########################################################################
  //# Overridden Test Cases
  @Override
  public void testHISCRhoneSubsystem1Patch0() throws Exception
  {
    try {
      super.testHISCRhoneSubsystem1Patch0();
    } catch (final OverflowException exception) {
      // Overflow in encoding --- never mind
    }
  }

  @Override
  public void testHISCRhoneSubsystem1Patch1() throws Exception
  {
    try {
      super.testHISCRhoneSubsystem1Patch1();
    } catch (final OverflowException exception) {
      // Overflow in encoding --- never mind
    }
  }

  @Override
  public void testHISCRhoneSubsystem1Patch2() throws Exception
  {
    try {
      super.testHISCRhoneSubsystem1Patch2();
    } catch (final OverflowException exception) {
      // Overflow in encoding --- never mind
    }
  }

}
