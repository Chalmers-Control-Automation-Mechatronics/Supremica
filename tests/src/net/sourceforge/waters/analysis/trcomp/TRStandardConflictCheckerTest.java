//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   NBAAbstractionStandardConflictCheckerTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.trcomp;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.model.analysis.AbstractStandardConflictCheckerTest;
import net.sourceforge.waters.model.analysis.des.ConflictChecker;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


public class TRStandardConflictCheckerTest
  extends AbstractStandardConflictCheckerTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite testSuite =
      new TestSuite(TRStandardConflictCheckerTest.class);
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
    final TRCompositionalConflictChecker checker =
      new TRCompositionalConflictChecker(factory);
    //checker.setInternalStateLimit(5000);
    //checker.setMonolithicStateLimit(100000);
    //checker.setInternalTransitionLimit(500000);
    checker.setBlockedEventsSupported(true);
    checker.setFailingEventsSupported(true);
    checker.setSelfloopOnlyEventsSupported(true);
    checker.setAlwaysEnabledEventsSupported(true);
    checker.setCounterExampleEnabled(false);
    //checker.setTraceCheckingEnabled(true);
    return checker;
  }


  //#########################################################################
  //# Test Cases
  public void testBigComponent() throws Exception
  {
    @SuppressWarnings("unused")
    final TRCompositionalConflictChecker checker =
      (TRCompositionalConflictChecker) getModelVerifier();
    // checker.setInternalStateLimit(1000);
    final String group = "tests";
    final String subdir = "nasty";
    final String name = "big_component.wmod";
    runModelVerifier(group, subdir, name, false);
  }

}
