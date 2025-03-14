//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
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
