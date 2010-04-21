//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.analysis.gnonblocking
//# CLASS:   RemovalOfAlphaMarkingsRuleTest
//###########################################################################
//# $Id: RemovalOfAlphaMarkingsRuleTest.java 5431 2010-03-29 10:26:57Z robi $
//###########################################################################

package net.sourceforge.waters.analysis.gnonblocking;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


public class RemovalOfTauTransitionsOriginatingFromNonAlphaStatesRuleTest
    extends AbstractAbstractionRuleTest
{

  // #########################################################################
  // # Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite testSuite =
        new TestSuite(
            RemovalOfTauTransitionsOriginatingFromNonAlphaStatesRuleTest.class);
    return testSuite;
  }

  public static void main(final String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }

  // #########################################################################
  // # Overrides for abstract base class
  // # net.sourceforge.waters.analysis.gnonblocking.AbstractAbstractionRuleTest
  protected RemovalOfTauTransitionsOriginatingFromNonAlphaStatesRule createAbstractionRule(
                                                                                           final ProductDESProxyFactory factory)
  {
    return new RemovalOfTauTransitionsOriginatingFromNonAlphaStatesRule(factory);
  }

  protected void configureAbstractionRule(final ProductDESProxy des)
  {
    super.configureAbstractionRule(des);
    final RemovalOfTauTransitionsOriginatingFromNonAlphaStatesRule rule =
        getAbstractionRule();
    final EventProxy alphaMarking = findEvent(des, ALPHA);
    rule.setAlphaMarking(alphaMarking);
    final EventProxy defaultMarking = findEvent(des, OMEGA);
    rule.setDefaultMarking(defaultMarking);
  }

  protected RemovalOfTauTransitionsOriginatingFromNonAlphaStatesRule getAbstractionRule()
  {
    return (RemovalOfTauTransitionsOriginatingFromNonAlphaStatesRule) super
        .getAbstractionRule();
  }

  // #########################################################################
  // # Test Cases
  /**
   * <P>
   * Tests the model in file {supremica}/examples/waters/tests/abstraction/
   * tauTransRemovalFromNonAlpha_1 .wmod.
   * </P>
   *
   * <P>
   * All test modules contain up to two automata, named "before" and "after".
   * The automaton named "before" is required to be present, and defines the
   * input automaton for the abstraction rule. The automaton "after" defines the
   * expected result of abstraction. It may be missing, in which case the
   * abstraction should have no effect and return the unchanged input automaton
   * (the test expects the same object, not an identical copy).
   * </P>
   *
   * <P>
   * The names of critical events are expected to be "tau", ":alpha", and
   * ":accepting", respectively.
   * </P>
   *
   * <P>
   * After running the test, any automaton created by the rule is saved in
   * {supremica }/logs/results/analysis/gnonblocking/
   * RemovalOfTauTransitionsOriginatingFromNonAlphaStatesRule as a .des file
   * (for text viewing) and as a .wmod file (to load into the IDE).
   * </P>
   */
  public void test_tauTransRemovalFromNonAlpha_StateUnreachable()
      throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "tauTransRemovalFromNonAlpha_1.wmod";
    runAbstractionRule(group, subdir, name);
  }

  public void test_tauTransRemovalFromNonAlpha_NoFurther() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "tauTransRemovalFromNonAlpha_2.wmod";
    runAbstractionRule(group, subdir, name);
  }

  public void test_tauTransRemovalFromNonAlpha_tauAndNonTauOutgoingTransitions()
      throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "tauTransRemovalFromNonAlpha_3.wmod";
    runAbstractionRule(group, subdir, name);
  }

  public void test_allStatesImplicitlyMarkedAlpha() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "tauTransRemovalFromNonAlpha_8.wmod";
    runAbstractionRule(group, subdir, name);
  }

  public void test_allStatesImplicitlyMarkedOmega() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "tauTransRemovalFromNonAlpha_9.wmod";
    runAbstractionRule(group, subdir, name);
  }

  public void test_tauTransRemovalFromNonAlpha_TwoConsecutiveTauUnreachable()
      throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "tauTransRemovalFromNonAlpha_7.wmod";
    runAbstractionRule(group, subdir, name);
  }

  public void test_tauTransRemovalFromNonAlpha_TwoTauOutgoing()
      throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "tauTransRemovalFromNonAlpha_4.wmod";
    runAbstractionRule(group, subdir, name);
  }

  public void test_tauTransRemovalFromNonAlpha_multipleIncomingTransitions()
      throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "tauTransRemovalFromNonAlpha_5.wmod";
    runAbstractionRule(group, subdir, name);
  }

  public void test_tauTransRemovalFromNonAlpha_tauLoopNoMarking()
      throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "tauTransRemovalFromNonAlpha_10.wmod";
    // TODO:wondering if there should be a tau self loop on state s5 in after...
    runAbstractionRule(group, subdir, name);
  }

  /**
   * A test to see whether a single abstraction rule object can perform multiple
   * abstractions in sequence.
   */
  public void testReentrant() throws Exception
  {
    test_tauTransRemovalFromNonAlpha_StateUnreachable();
    test_tauTransRemovalFromNonAlpha_NoFurther();
    test_tauTransRemovalFromNonAlpha_TwoTauOutgoing();
    test_tauTransRemovalFromNonAlpha_tauAndNonTauOutgoingTransitions();
    test_allStatesImplicitlyMarkedOmega();
    test_tauTransRemovalFromNonAlpha_StateUnreachable();
    test_tauTransRemovalFromNonAlpha_TwoConsecutiveTauUnreachable();
    test_tauTransRemovalFromNonAlpha_multipleIncomingTransitions();
    test_allStatesImplicitlyMarkedAlpha();
    test_tauTransRemovalFromNonAlpha_NoFurther();
  }

}
