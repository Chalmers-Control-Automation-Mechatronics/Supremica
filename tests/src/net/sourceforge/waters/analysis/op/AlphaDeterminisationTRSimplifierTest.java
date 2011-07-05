//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.op
//# CLASS:   AlphaDeterminisationTRSimplifierTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.op;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;


/**
 * A test for the determinisation of alpha states rule
 * ({@link AlphaDeterminisationTRSimplifier}).
 *
 * This test is to be used with caution because the same bisimulation module (
 * {@link net.sourceforge.waters.analysis.op.ObservationEquivalenceTRSimplifier
 * ObservationEquivalenceTRSimplifier}) is used by the abstraction rule and the
 * isomorphism checker that compares the test output with the expected result.
 * Nevertheless, it may be helpful to show the output of observation equivalence
 * and test how silent events are handled by various configurations of the
 * bisimulation algorithm.
 *
 * @author Robi Malik
 */

public class AlphaDeterminisationTRSimplifierTest
  extends AbstractTRSimplifierTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite testSuite =
        new TestSuite(AlphaDeterminisationTRSimplifierTest.class);
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
    final ObservationEquivalenceTRSimplifier oeq =
      new ObservationEquivalenceTRSimplifier();
    return new AlphaDeterminisationTRSimplifier(oeq);
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
    final String name = "alphadet_1.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_determinisation_2() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alphadet_2.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_determinisation_3() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alphadet_3.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_determinisation_4() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alphadet_4.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_determinisation_5() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alphadet_5.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
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
    test_determinisation_5();
    test_determinisation_1();
    test_determinisation_2();
    test_determinisation_3();
    test_determinisation_4();
    test_determinisation_5();
  }

}
