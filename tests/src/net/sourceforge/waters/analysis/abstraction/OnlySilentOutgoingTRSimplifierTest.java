//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:   OnlySilentOutgoingTRSimplifierTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import junit.framework.Test;
import junit.framework.TestSuite;


public class OnlySilentOutgoingTRSimplifierTest
  extends AbstractTRSimplifierTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite testSuite =
      new TestSuite(OnlySilentOutgoingTRSimplifierTest.class);
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
    final OnlySilentOutgoingTRSimplifier simplifier =
      new OnlySilentOutgoingTRSimplifier();
    return simplifier;
  }

  @Override
  protected void configureTransitionRelationSimplifier()
  {
    configureTransitionRelationSimplifierWithPropositions();
  }


  //#########################################################################
  //# Test Cases
  public void test_tauTransRemovalFromNonAlpha_StateUnreachable()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "tauTransRemovalFromNonAlpha_1.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_tauTransRemovalFromNonAlpha_NoFurther()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "tauTransRemovalFromNonAlpha_2.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_tauTransRemovalFromNonAlpha_tauAndNonTauOutgoingTransitions()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "tauTransRemovalFromNonAlpha_3.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_allStatesImplicitlyMarkedAlpha()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "tauTransRemovalFromNonAlpha_8.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_allStatesImplicitlyMarkedOmega()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "tauTransRemovalFromNonAlpha_9.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_tauTransRemovalFromNonAlpha_TwoConsecutiveTauUnreachable()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "tauTransRemovalFromNonAlpha_7.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_tauTransRemovalFromNonAlpha_TwoTauOutgoing()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "tauTransRemovalFromNonAlpha_4.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_tauTransRemovalFromNonAlpha_multipleIncomingTransitions()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "tauTransRemovalFromNonAlpha_5.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_tauTransRemovalFromNonAlpha_tauLoopNoMarking()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "tauTransRemovalFromNonAlpha_10.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_tauTransRemovalFromNonAlpha_removalOfInitialState()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "tauTransRemovalFromNonAlpha_11.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_tauTransRemovalFromNonAlpha_nonRemovalOfStateWithNoOutgoingTransitions()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "tauTransRemovalFromNonAlpha_12.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  /**
   * A test to see whether a single abstraction rule object can perform multiple
   * abstractions in sequence.
   */
  public void testReentrant()
  throws Exception
  {
    test_tauTransRemovalFromNonAlpha_StateUnreachable();
    test_tauTransRemovalFromNonAlpha_NoFurther();
    test_tauTransRemovalFromNonAlpha_TwoTauOutgoing();
    test_tauTransRemovalFromNonAlpha_tauAndNonTauOutgoingTransitions();
    test_allStatesImplicitlyMarkedOmega();
    test_tauTransRemovalFromNonAlpha_StateUnreachable();
    test_tauTransRemovalFromNonAlpha_TwoConsecutiveTauUnreachable();
    test_tauTransRemovalFromNonAlpha_multipleIncomingTransitions();
    test_tauTransRemovalFromNonAlpha_nonRemovalOfStateWithNoOutgoingTransitions();
    test_allStatesImplicitlyMarkedAlpha();
    test_tauTransRemovalFromNonAlpha_NoFurther();
  }

}
