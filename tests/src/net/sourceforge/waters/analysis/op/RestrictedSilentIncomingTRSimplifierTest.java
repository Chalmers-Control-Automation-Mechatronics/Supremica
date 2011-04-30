//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.op
//# CLASS:   FreeSilentIncomingTRSimplifierTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.op;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * A test for the <I>Silent Incoming Rule</I>.
 * This tests the {@link SilentIncomingTRSimplifier} when it is configured
 * to remove only tau transitions leading to states that become unreachable.
 *
 * @see SilentIncomingTRSimplifier#setAppliesPartitionAutomatically(boolean)
 * @author Robi Malik
 */

public class RestrictedSilentIncomingTRSimplifierTest
  extends AbstractTRSimplifierTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite testSuite =
      new TestSuite(RestrictedSilentIncomingTRSimplifierTest.class);
    return testSuite;
  }

  public static void main(final String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }


  //#########################################################################
  //# Overrides for abstract base class
  //# net.sourceforge.waters.analysis.op.AbstractTRSimplifierTest
  protected TransitionRelationSimplifier createTransitionRelationSimplifier()
  {
    final SilentIncomingTRSimplifier simplifier =
      new SilentIncomingTRSimplifier();
    simplifier.setRestrictsToUnreachableStates(true);
    return simplifier;
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
  public void test_tauTransRemovalToNonAlpha_StateUnreachable()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "tauTransRemovalToNonAlpha_1.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_tauTransRemovalToNonAlpha_NoFurther() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "tauTransRemovalToNonAlpha_2.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_tauTransRemovalToNonAlpha_StateReachable() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "tauTransRemovalToNonAlpha_3a.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_allStatesImplicitlyMarkedAlpha() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "tauTransRemovalToNonAlpha_8.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_allStatesImplicitlyMarkedOmega() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "tauTransRemovalToNonAlpha_9.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_tauTransRemovalToNonAlpha_TwoConsecutiveTauUnreachable()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "tauTransRemovalToNonAlpha_7.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_tauTransRemovalToNonAlpha_TwoConsecutiveTauReachable()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "tauTransRemovalToNonAlpha_4a.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_tauTransRemovalToNonAlpha_TwoTau() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "tauTransRemovalToNonAlpha_5.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_tauTransRemovalToNonAlpha_tauLoopNoMarking()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "tauTransRemovalToNonAlpha_10.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_tauTransRemovalToNonAlpha_oneRemovaableTauAndOneNonremovable()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "tauTransRemovalToNonAlpha_11.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_tauTransRemovalToNonAlpha_12()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "tauTransRemovalToNonAlpha_12.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  /**
   * A test to see whether a single abstraction rule object can perform multiple
   * abstractions in sequence.
   */
  public void testReentrant() throws Exception
  {
    test_tauTransRemovalToNonAlpha_StateUnreachable();
    test_tauTransRemovalToNonAlpha_NoFurther();
    test_tauTransRemovalToNonAlpha_TwoConsecutiveTauReachable();
    test_tauTransRemovalToNonAlpha_StateReachable();
    test_allStatesImplicitlyMarkedOmega();
    test_tauTransRemovalToNonAlpha_StateUnreachable();
    test_tauTransRemovalToNonAlpha_TwoConsecutiveTauUnreachable();
    test_tauTransRemovalToNonAlpha_TwoTau();
    test_tauTransRemovalToNonAlpha_oneRemovaableTauAndOneNonremovable();
    test_allStatesImplicitlyMarkedAlpha();
    test_tauTransRemovalToNonAlpha_NoFurther();
  }

}
