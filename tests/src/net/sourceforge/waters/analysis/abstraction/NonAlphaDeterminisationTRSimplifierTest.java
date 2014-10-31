//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:   NonAlphaDeterminisationTRSimplifierTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;


/**
 * A test for the nonalphadet of non alpha states rule
 * ({@link NonAlphaDeterminisationTRSimplifier}).
 *
 * This test is to be used with caution because the same bisimulation module (
 * {@link net.sourceforge.waters.analysis.abstraction.ObservationEquivalenceTRSimplifier
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
  //# net.sourceforge.waters.analysis.abstraction.AbstractTRSimplifierTest
  @Override
  protected TransitionRelationSimplifier createTransitionRelationSimplifier()
  {
    return new NonAlphaDeterminisationTRSimplifier();
  }

  @Override
  protected EventEncoding createEventEncoding(final ProductDESProxy des,
                                              final AutomatonProxy aut)
    throws OverflowException
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
  public void test_nonalphadet_1() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "nonalphadet_1.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_nonalphadet_2() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "nonalphadet_2.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_nonalphadet_3() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "nonalphadet_3.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_nonalphadet_4() throws Exception
  {
    final NonAlphaDeterminisationTRSimplifier simplifier =
      (NonAlphaDeterminisationTRSimplifier) getTransitionRelationSimplifier();
    simplifier.setTransitionRemovalMode
      (ObservationEquivalenceTRSimplifier.TransitionRemoval.ALL);
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "nonalphadet_4.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_nonalphadet_5() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "nonalphadet_5.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_nonalphadet_6() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "nonalphadet_6.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_nonalphadet_7() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "nonalphadet_7.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_nonalphadet_8() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "nonalphadet_8.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_nonalphadet_9() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "nonalphadet_9.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_nonalphadet_10() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "nonalphadet_10.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_nonalphadet_11() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "nonalphadet_11.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_nonalphadet_12() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "nonalphadet_12.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_nonalphadet_13() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "nonalphadet_13.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_nonalphadet_14() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "nonalphadet_14.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_nonalphadet_15() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "nonalphadet_15.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_nonalphadet_16() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "nonalphadet_16.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_nonalphadet_17() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "nonalphadet_17.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_nonalphadet_18() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "nonalphadet_18.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_nonalphadet_19() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "nonalphadet_19.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_nonalphadet_20() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "nonalphadet_20.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_nonalphadet_21() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "nonalphadet_21.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_nonalphadet_22() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "nonalphadet_22.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_nonalphadet_23() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "nonalphadet_23.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_nonalphadet_24() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "nonalphadet_24.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_nonalphadet_25() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "nonalphadet_25.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_nonalphadet_26() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "nonalphadet_26.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_nonalphadet_27() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "nonalphadet_27.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_nonalphadet_28() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "nonalphadet_28.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  /**
   * A test to see whether a single abstraction rule object can perform multiple
   * abstractions in sequence.
   */
  public void testReentrant() throws Exception
  {
    test_nonalphadet_12();
    test_nonalphadet_1();
    test_nonalphadet_2();
    test_nonalphadet_3();
    test_nonalphadet_11();
    test_nonalphadet_13();
    test_nonalphadet_4();
    test_nonalphadet_20();
    test_nonalphadet_8();
    test_nonalphadet_14();
    test_nonalphadet_1();
    test_nonalphadet_10();
    test_nonalphadet_2();
    test_nonalphadet_9();
  }

}
