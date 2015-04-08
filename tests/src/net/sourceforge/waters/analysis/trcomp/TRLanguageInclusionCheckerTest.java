//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.trcomp
//# CLASS:   TRLanguageInclusionCheckerTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.trcomp;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.model.analysis.AbstractLanguageInclusionCheckerTest;
import net.sourceforge.waters.model.analysis.des.LanguageInclusionChecker;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


public class TRLanguageInclusionCheckerTest
  extends AbstractLanguageInclusionCheckerTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite testSuite =
      new TestSuite(TRLanguageInclusionCheckerTest.class);
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
  protected LanguageInclusionChecker createModelVerifier
    (final ProductDESProxyFactory factory)
  {
    final TRCompositionalLanguageInclusionChecker checker =
      new TRCompositionalLanguageInclusionChecker();
    checker.setPreselectionHeuristic(AbstractTRCompositionalAnalyzer.PRESEL_MustL);
    checker.setSelectionHeuristic(AbstractTRCompositionalAnalyzer.SEL_MinS);
    checker.setInternalStateLimit(5000);
    checker.setMonolithicStateLimit(100000);
    checker.setInternalTransitionLimit(500000);
    checker.setBlockedEventsEnabled(true);
    checker.setFailingEventsEnabled(true);
    checker.setSelfloopOnlyEventsEnabled(true);
    checker.setAlwaysEnabledEventsEnabled(true);
    checker.setCounterExampleEnabled(true);
    checker.setTraceCheckingEnabled(true);
    return checker;
  }

}
