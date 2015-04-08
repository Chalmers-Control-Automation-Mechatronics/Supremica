//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   NBAbstractionStandardConflictCheckerTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.compositional;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.model.analysis.AbstractStandardConflictCheckerTest;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.analysis.des.ConflictChecker;
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
  @Override
  protected ConflictChecker createModelVerifier
    (final ProductDESProxyFactory factory)
  {
    final CompositionalConflictChecker checker =
      new CompositionalConflictChecker(factory,
                                       ConflictAbstractionProcedureFactory.NB);
    checker.setPreselectingMethod(AbstractCompositionalModelAnalyzer.MustL);
    checker.setSelectionHeuristic(CompositionalSelectionHeuristicFactory.MinS);
    checker.setInternalStateLimit(5000);
    checker.setMonolithicStateLimit(100000);
    checker.setInternalTransitionLimit(500000);
    checker.setBlockedEventsEnabled(true);
    checker.setFailingEventsEnabled(true);
    checker.setSelfloopOnlyEventsEnabled(false);
    checker.setTraceCheckingEnabled(true);
    return checker;
  }

  @Override
  protected CompositionalConflictChecker getModelVerifier()
  {
    return (CompositionalConflictChecker) super.getModelVerifier();
  }

  @Override
  protected void checkStatistics(final VerificationResult stats)
  {
    final CompositionalConflictChecker checker = getModelVerifier();
    if (!checker.isSelfloopOnlyEventsEnabled()) {
      final CompositionalVerificationResult compositionalStats =
        (CompositionalVerificationResult) stats;
      assertEquals("Compositional model verifier reports selfloop-only events " +
                   "although they are disabled!",
                   -1, compositionalStats.getSelfloopOnlyEventsCount());
    }
  }


  //#########################################################################
  //# Test Cases
  public void testBigComponent() throws Exception
  {
    final CompositionalConflictChecker checker =
      getModelVerifier();
    checker.setInternalStateLimit(1000);
    final String group = "tests";
    final String subdir = "nasty";
    final String name = "big_component.wmod";
    runModelVerifier(group, subdir, name, false);
  }

}
