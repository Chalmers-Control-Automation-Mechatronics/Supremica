//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.gnonblocking
//# CLASS:   AltObservationEquivalenceRuleTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.gnonblocking;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.analysis.op.ObservationEquivalenceTRSimplifier;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * A test for the observation equivalence rule (
 * {@link ObservationEquivalenceRule}).
 *
 * This test is to be used with caution because the same bisimulation module
 * ({@link net.sourceforge.waters.analysis.op.ObservationEquivalenceTRSimplifier
 * ObservationEquivalenceTRSimplifier}) is used by the abstraction rule and the
 * isomorphism checker that compares the test output with the expected result.
 * Nevertheless, it may be helpful to show the output of observation equivalence
 * and test how silent events are handled by various configurations of the
 * bisimulation algorithm.
 *
 * @author Robi Malik
 */

public class ObservationEquivalenceRuleTest
  extends AbstractAbstractionRuleTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite() {
    final TestSuite testSuite =
      new TestSuite(ObservationEquivalenceRuleTest.class);
    return testSuite;
  }

  public static void main(final String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }


  //#########################################################################
  //# Overrides for abstract base class
  //# net.sourceforge.waters.analysis.gnonblocking.AbstractAbstractionRuleTest
  protected ObservationEquivalenceRule createAbstractionRule
    (final ProductDESProxyFactory factory)
  {
    return new ObservationEquivalenceRule(factory);
  }

  protected ObservationEquivalenceRule getAbstractionRule()
  {
    return (ObservationEquivalenceRule) super.getAbstractionRule();
  }


  //#########################################################################
  //# Test Cases
  public void test_oeq_1() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "oeq_1.wmod";
    runAbstractionRule(group, subdir, name);
  }

  public void test_oeq_2() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "oeq_2.wmod";
    runAbstractionRule(group, subdir, name);
  }

  public void test_oeq_3() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "oeq_3.wmod";
    runAbstractionRule(group, subdir, name);
  }

  public void test_oeq_4() throws Exception
  {
    final ObservationEquivalenceRule rule = getAbstractionRule();
    rule.setTransitionRemovalMode
      (ObservationEquivalenceTRSimplifier.TransitionRemoval.ALL);
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "oeq_4.wmod";
    runAbstractionRule(group, subdir, name);
  }

  public void test_oeq_5() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "oeq_5.wmod";
    runAbstractionRule(group, subdir, name);
  }

  public void test_oeq_6() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "oeq_6.wmod";
    runAbstractionRule(group, subdir, name);
  }

  public void test_oeq_7() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "oeq_7.wmod";
    runAbstractionRule(group, subdir, name);
  }

  public void test_oeq_8() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "oeq_8.wmod";
    runAbstractionRule(group, subdir, name);
  }

  public void test_oeq_9() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "oeq_9.wmod";
    runAbstractionRule(group, subdir, name);
  }

  public void test_oeq_10() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "oeq_10.wmod";
    runAbstractionRule(group, subdir, name);
  }

  public void test_oeq_11() throws Exception
  {
    final ObservationEquivalenceRule rule = getAbstractionRule();
    rule.setTransitionRemovalMode
      (ObservationEquivalenceTRSimplifier.TransitionRemoval.ALL);
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "oeq_11.wmod";
    runAbstractionRule(group, subdir, name);
  }

  public void test_oeq_12() throws Exception
  {
    final ObservationEquivalenceRule rule = getAbstractionRule();
    rule.setTransitionRemovalMode
      (ObservationEquivalenceTRSimplifier.TransitionRemoval.ALL);
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "oeq_12.wmod";
    runAbstractionRule(group, subdir, name);
  }

  public void test_oeq_13() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "oeq_13.wmod";
    runAbstractionRule(group, subdir, name);
  }

  public void test_oeq_14() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "oeq_14.wmod";
    runAbstractionRule(group, subdir, name);
  }

  /**
   * A test to see whether a single abstraction rule object can perform
   * multiple abstractions in sequence.
   */
  public void testReentrant() throws Exception
  {
    test_oeq_1();
    test_oeq_2();
    test_oeq_3();
    test_oeq_4();
    test_oeq_1();
    test_oeq_2();
  }

}
