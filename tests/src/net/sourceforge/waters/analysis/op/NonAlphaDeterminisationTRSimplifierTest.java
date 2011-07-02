//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.op
//# CLASS:   NonAlphaDeterminisationTRSimplifierTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.op;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.analysis.op.ObservationEquivalenceTRSimplifier;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;


/**
 * A test for the determinisation of non alpha states rule
 * ({@link NonAlphaDeterminisationTRSimplifier}).
 *
 * This test is to be used with caution because the same bisimulation module (
 * {@link net.sourceforge.waters.analysis.op.ObservationEquivalenceTRSimplifier
 * ObservationEquivalenceTRSimplifier}) is used by the abstraction rule and the
 * isomorphism checker that compares the test output with the expected result.
 * Nevertheless, it may be helpful to show the output of observation equivalence
 * and test how silent events are handled by various configurations of the
 * bisimulation algorithm.
 *
 * @author Robi Malik, Rachel Francis
 */

public class NonAlphaDeterminisationTRSimplifierTest
  extends AbstractTRSimplifierTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite testSuite =
        new TestSuite(NonAlphaDeterminisationTRSimplifierTest.class);
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
    final AltObservationEquivalenceTRSimplifier oeq =
      new AltObservationEquivalenceTRSimplifier();
    return new NonAlphaDeterminisationTRSimplifier(oeq);
  }

  @Override
  protected EventEncoding createEventEncoding(final ProductDESProxy des,
                                              final AutomatonProxy aut)
  {
    return createEventEncodingWithPropositions(des, aut);
  }

  @Override
  protected void configureTransitionRelationSimplifier()
  {
    configureTransitionRelationSimplifierWithPropositions();
  }


  //#########################################################################
  //# Test Cases
  public void test_determinisation_1() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "determinisation_1.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_determinisation_2() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "determinisation_2.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_determinisation_3() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "determinisation_3.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_determinisation_4() throws Exception
  {
    final NonAlphaDeterminisationTRSimplifier simplifier =
      (NonAlphaDeterminisationTRSimplifier) getTransitionRelationSimplifier();
    simplifier.setTransitionRemovalMode
      (ObservationEquivalenceTRSimplifier.TransitionRemoval.ALL);
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "determinisation_4.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_determinisation_5() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "determinisation_5.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_determinisation_6() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "determinisation_6.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_determinisation_7() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "determinisation_7.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_determinisation_8() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "determinisation_8.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_determinisation_9() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "determinisation_9.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_determinisation_10() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "determinisation_10.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_determinisation_11() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "determinisation_11.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_determinisation_12() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "determinisation_12.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_determinisation_13() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "determinisation_13.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_determinisation_14() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "determinisation_14.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_determinisation_15() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "determinisation_15.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_determinisation_16() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "determinisation_16.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_determinisation_17() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "determinisation_17.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_determinisation_18() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "determinisation_18.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_determinisation_19() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "determinisation_19.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_determinisation_20() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "determinisation_20.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_determinisation_21() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "determinisation_21.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_determinisation_22() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "determinisation_22.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_determinisation_23() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "determinisation_23.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_determinisation_24() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "determinisation_24.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_determinisation_25() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "determinisation_25.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_determinisation_26() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "determinisation_26.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_determinisation_27() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "determinisation_27.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_determinisation_28() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "determinisation_28.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  /**
   * A test to see whether a single abstraction rule object can perform multiple
   * abstractions in sequence.
   */
  public void testReentrant() throws Exception
  {
    test_determinisation_12();
    test_determinisation_1();
    test_determinisation_2();
    test_determinisation_3();
    test_determinisation_11();
    test_determinisation_13();
    test_determinisation_4();
    test_determinisation_20();
    test_determinisation_8();
    test_determinisation_14();
    test_determinisation_1();
    test_determinisation_10();
    test_determinisation_2();
    test_determinisation_9();
  }

}
