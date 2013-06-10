//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.monolithic
//# CLASS:   MonolithicConflictCheckerTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.gnonblocking;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.model.analysis.AbstractStandardConflictCheckerTest;
import net.sourceforge.waters.model.analysis.des.ConflictChecker;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


public class CompositionalStandardConflictCheckerTest extends
    AbstractStandardConflictCheckerTest
{

  // #########################################################################
  // # Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite testSuite =
        new TestSuite(CompositionalStandardConflictCheckerTest.class);
    return testSuite;
  }

  public static void main(final String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }

  // #########################################################################
  // # Overrides for abstract base class
  // # net.sourceforge.waters.analysis.AbstractModelVerifierTest
  @Override
  protected ConflictChecker createModelVerifier
    (final ProductDESProxyFactory factory)
  {
    final CompositionalGeneralisedConflictChecker checker =
      new CompositionalGeneralisedConflictChecker(factory);
    checker.setInternalStepNodeLimit(10000);
    checker.setFinalStepNodeLimit(1000000);
    checker.setTransitionLimit(1000000);
    return checker;
  }

  @Override
  protected CompositionalGeneralisedConflictChecker getModelVerifier()
  {
    return (CompositionalGeneralisedConflictChecker) super.getModelVerifier();
  }

  // #########################################################################
  // # Specific Tests
  public void testInvoke_DefaultRemoval() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "defaultremoval_invoke.wmod";
    runModelVerifier(group, subdir, name, false);
    checkRuleApplicationCounts(RemovalOfDefaultMarkingsRule.class);
  }

  public void testInvoke_TauTransRemovalToNonAlpha() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "tauTransRemovalToNonAlpha_invoke.wmod";
    runModelVerifier(group, subdir, name, false);
    checkRuleApplicationCounts
      (RemovalOfTauTransitionsLeadingToNonAlphaStatesRule.class);
  }

  public void testInvoke_TauTransRemovalFromNonAlpha() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "tauTransRemovalFromNonAlpha_invoke.wmod";
    runModelVerifier(group, subdir, name, false);
    checkRuleApplicationCounts
      (RemovalOfTauTransitionsOriginatingFromNonAlphaStatesRule.class);
  }

  public void testInvoke_NonAlphaDet() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "nonalphadet_invoke.wmod";
    runModelVerifier(group, subdir, name, false);
    checkRuleApplicationCounts(DeterminisationOfNonAlphaStatesRule.class);
  }

  // #########################################################################
  // # Auxiliary Methods
  private void checkRuleApplicationCounts
    (final Class<? extends AbstractionRule> expected)
  {
    final CompositionalGeneralisedConflictChecker checker = getModelVerifier();
    final CompositionalGeneralisedConflictCheckerVerificationResult result =
      checker.getAnalysisResult();
    final List<AbstractionRuleStatistics> list =
      result.getAbstractionRuleStatistics();
    for (final AbstractionRuleStatistics stats : list) {
      final Class<? extends AbstractionRule> current = stats.getRuleClass();
      final int count = stats.getReductionCount();
      if (current == RemovalOfAlphaMarkingsRule.class || current == expected) {
        assertEquals(ProxyTools.getShortClassName(current) +
                     " was not successful exactly once as expected!", 1, count);
      } else {
        assertEquals("Unexpected application of " +
                     ProxyTools.getShortClassName(current) + "!", 0, count);
      }
    }

  }

}
