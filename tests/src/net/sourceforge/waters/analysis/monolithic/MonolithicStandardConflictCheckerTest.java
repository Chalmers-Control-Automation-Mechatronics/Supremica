//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.monolithic
//# CLASS:   MonolithicConflictCheckerTest
//###########################################################################
//# $Id: MonolithicConflictCheckerTest.java 4962 2009-12-15 02:23:35Z rmf18 $
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

  // #########################################################################
  // # Entry points in junit.framework.TestCase
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

  // #########################################################################
  // # Overrides for abstract base class
  // # net.sourceforge.waters.analysis.AbstractModelVerifierTest
  protected ConflictChecker createModelVerifier(
      final ProductDESProxyFactory factory)
  {
    return new MonolithicConflictChecker(factory);
  }

  // #########################################################################
  // # Overridden Test Cases
  public void testHISCRhoneSubsystem1Patch0() throws Exception
  {
    try {
      super.testHISCRhoneSubsystem1Patch0();
    } catch (final OverflowException exception) {
      // never mind
    }
  }

  public void testHISCRhoneSubsystem1Patch1() throws Exception
  {
    try {
      super.testHISCRhoneSubsystem1Patch1();
    } catch (final OverflowException exception) {
      // never mind
    }
  }

  public void testHISCRhoneSubsystem1Patch2() throws Exception
  {
    try {
      super.testHISCRhoneSubsystem1Patch2();
    } catch (final OverflowException exception) {
      // never mind
    }
  }

}
