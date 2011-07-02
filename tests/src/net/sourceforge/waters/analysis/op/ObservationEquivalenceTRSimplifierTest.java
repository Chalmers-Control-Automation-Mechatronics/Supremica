//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.op
//# CLASS:   ObservationEquivalenceTRSimplifierTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.op;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.analysis.op.AltObservationEquivalenceTRSimplifier;


/**
 * A test for the observation equivalence simplifier
 * ({@link AltObservationEquivalenceTRSimplifier}).
 *
 * This test is to be used with caution because the same bisimulation module
 * ({@link net.sourceforge.waters.analysis.op.AltObservationEquivalenceTRSimplifier
 * AltObservationEquivalenceTRSimplifier}) is used by the abstraction rule and the
 * isomorphism checker that compares the test output with the expected result.
 * Nevertheless, it may be helpful to show the output of observation equivalence
 * and test how silent events are handled by various configurations of the
 * bisimulation algorithm.
 *
 * @author Robi Malik
 */

public class ObservationEquivalenceTRSimplifierTest
  extends AbstractTRSimplifierTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite() {
    final TestSuite testSuite =
      new TestSuite(ObservationEquivalenceTRSimplifierTest.class);
    return testSuite;
  }

  public static void main(final String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }


  //#########################################################################
  //# Overrides for abstract base class
  //# net.sourceforge.waters.analysis.op.AbstractTRSimplifierTest
  @Override
  protected TransitionRelationSimplifier createTransitionRelationSimplifier()
  {
    return new AltObservationEquivalenceTRSimplifier();
  }


  //#########################################################################
  //# Test Cases
  public void test_oeq_1() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "oeq_1.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_oeq_2() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "oeq_2.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_oeq_3() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "oeq_3.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_oeq_4() throws Exception
  {
    final AltObservationEquivalenceTRSimplifier simplifier =
      (AltObservationEquivalenceTRSimplifier) getTransitionRelationSimplifier();
    simplifier.setTransitionRemovalMode
      (AltObservationEquivalenceTRSimplifier.TransitionRemoval.ALL);
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "oeq_4.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_oeq_5() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "oeq_5.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_oeq_6() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "oeq_6.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_oeq_7() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "oeq_7.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_oeq_8() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "oeq_8.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_oeq_9() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "oeq_9.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_oeq_10() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "oeq_10.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_oeq_11() throws Exception
  {
    final AltObservationEquivalenceTRSimplifier simplifier =
      (AltObservationEquivalenceTRSimplifier) getTransitionRelationSimplifier();
    simplifier.setTransitionRemovalMode
      (AltObservationEquivalenceTRSimplifier.TransitionRemoval.ALL);
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "oeq_11.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_oeq_12() throws Exception
  {
    final AltObservationEquivalenceTRSimplifier simplifier =
      (AltObservationEquivalenceTRSimplifier) getTransitionRelationSimplifier();
    simplifier.setTransitionRemovalMode
      (AltObservationEquivalenceTRSimplifier.TransitionRemoval.ALL);
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "oeq_12.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_oeq_13() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "oeq_13.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_oeq_14() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "oeq_14.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_oeq_15() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "oeq_15.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_oeq_16() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "oeq_16.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_oeq_17() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "oeq_17.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  /**
   * A test to see whether a single transition relation simplifier
   * object can perform multiple abstractions in sequence.
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
