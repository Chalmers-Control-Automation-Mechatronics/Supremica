//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.trcomp
//# CLASS:   GNBwGeneralisedConflictCheckerTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.trcomp;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.model.analysis.AbstractGeneralisedConflictCheckerTest;
import net.sourceforge.waters.model.analysis.des.ConflictChecker;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


public class GNBwGeneralisedConflictCheckerTest
  extends AbstractGeneralisedConflictCheckerTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite testSuite =
      new TestSuite(GNBwGeneralisedConflictCheckerTest.class);
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
      new TRCompositionalConflictChecker();
    checker.setSimplifierCreator(TRCompositionalConflictChecker.GNBw);
    checker.setPreselectionHeuristic(AbstractTRCompositionalAnalyzer.PRESEL_Pairs);
    checker.setSelectionHeuristic(AbstractTRCompositionalAnalyzer.SEL_MinSyncA);
    checker.setInternalStateLimit(5000);
    checker.setMonolithicStateLimit(100000);
    checker.setInternalTransitionLimit(500000);
    checker.setBlockedEventsEnabled(true);
    checker.setFailingEventsEnabled(true);
    checker.setSelfloopOnlyEventsEnabled(true);
    checker.setAlwaysEnabledEventsEnabled(true);
    checker.setPruningDeadlocks(true);
    checker.setCounterExampleEnabled(true);
    checker.setTraceCheckingEnabled(true);
    return checker;
  }

}
