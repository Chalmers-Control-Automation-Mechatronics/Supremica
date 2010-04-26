//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.analysis.gnonblocking
//# CLASS:   ObservationEquivalenceRuleTest
//###########################################################################
//# $Id: ObservationEquivalenceRuleTest.java 5455 2010-03-31 02:20:16Z robi $
//###########################################################################

package net.sourceforge.waters.analysis.gnonblocking;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * A test for the determinsation of non alpha states rule (
 * {@link DeterminisationOfNonAlphaStatesRule}).
 *
 * This test is to be used with caution because the same bisimulation module (
 * {@link net.sourceforge.waters.analysis.op.ObservationEquivalenceTRSimplifier
 * ObservationEquivalenceTRSimplifier}) is used by the abstraction rule and the
 * isomorphism checker that compares the test output with the expected result.
 * Nevertheless, it may be helpful to show the output of observation equivalence
 * and test how silent events are handled by various configurations of the
 * bisimulation algorithm.
 *
 * @author Robi Malik and Rachel Francis
 */

public class DeterminisationOfNonAlphaStatesRuleTest extends
    AbstractAbstractionRuleTest
{

  // #########################################################################
  // # Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite testSuite =
        new TestSuite(DeterminisationOfNonAlphaStatesRuleTest.class);
    return testSuite;
  }

  public static void main(final String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }

  // #########################################################################
  // # Overrides for abstract base class
  // # net.sourceforge.waters.analysis.gnonblocking.AbstractAbstractionRuleTest
  protected DeterminisationOfNonAlphaStatesRule createAbstractionRule(
                                                                      final ProductDESProxyFactory factory)
  {
    return new DeterminisationOfNonAlphaStatesRule(factory);
  }

  protected DeterminisationOfNonAlphaStatesRule getAbstractionRule()
  {
    return (DeterminisationOfNonAlphaStatesRule) super.getAbstractionRule();
  }

  // #########################################################################
  // # Test Cases
  public void test_determinisation_1() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "determinisation_1.wmod";
    runAbstractionRule(group, subdir, name);
  }

  public void test_determinisation_2() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "determinisation_2.wmod";
    runAbstractionRule(group, subdir, name);
  }

  public void test_determinisation_3() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "determinisation_3.wmod";
    runAbstractionRule(group, subdir, name);
    //TODO:unsure about this test case.
  }

  public void test_determinisation_4() throws Exception
  {
    final DeterminisationOfNonAlphaStatesRule rule = getAbstractionRule();
    rule.setSuppressRedundantHiddenTransitions(true);
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "determinisation_4.wmod";
    runAbstractionRule(group, subdir, name);
  }

  public void test_determinisation_5() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "determinisation_5.wmod";
    runAbstractionRule(group, subdir, name);
  }

  public void test_determinisation_6() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "determinisation_6.wmod";
    runAbstractionRule(group, subdir, name);
  }

  public void test_determinisation_7() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "determinisation_7.wmod";
    runAbstractionRule(group, subdir, name);
  }

  public void test_determinisation_8() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "determinisation_8.wmod";
    runAbstractionRule(group, subdir, name);
  }

  /**
   * A test to see whether a single abstraction rule object can perform multiple
   * abstractions in sequence.
   */
  public void testReentrant() throws Exception
  {
    test_determinisation_1();
    test_determinisation_2();
    test_determinisation_3();
    test_determinisation_4();
    test_determinisation_8();
    test_determinisation_1();
    test_determinisation_2();
  }

}
