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


public class RemovalOfTauTransitionsLeadingToNonAlphaStatesRuleTest extends
    AbstractAbstractionRuleTest
{

  // #########################################################################
  // # Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite testSuite =
        new TestSuite(
            RemovalOfTauTransitionsLeadingToNonAlphaStatesRuleTest.class);
    return testSuite;
  }

  public static void main(final String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }

  // #########################################################################
  // # Overrides for abstract base class
  // # net.sourceforge.waters.analysis.gnonblocking.AbstractAbstractionRuleTest
  protected RemovalOfTauTransitionsLeadingToNonAlphaStatesRule createAbstractionRule(
                                                                                     final ProductDESProxyFactory factory)
  {
    return new RemovalOfTauTransitionsLeadingToNonAlphaStatesRule(factory);
  }

  protected void configureAbstractionRule(final ProductDESProxy des)
  {
    super.configureAbstractionRule(des);
    final RemovalOfTauTransitionsLeadingToNonAlphaStatesRule rule =
        getAbstractionRule();
    final EventProxy alphaMarking = findEvent(des, ALPHA);
    rule.setAlphaMarking(alphaMarking);
  }

  protected RemovalOfTauTransitionsLeadingToNonAlphaStatesRule getAbstractionRule()
  {
    return (RemovalOfTauTransitionsLeadingToNonAlphaStatesRule) super
        .getAbstractionRule();
  }

  // #########################################################################
  // # Test Cases
  /**
   * <P>
   * Tests the model in file
   * {supremica}/examples/waters/tests/abstraction/tauTransRemovalToNonAlpha_1
   * .wmod.
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
   * RemovalOfTauTransitionsLeadingToNonAlphaStatesRule as a .des file (for text
   * viewing) and as a .wmod file (to load into the IDE).
   * </P>
   */
  public void test_tauTransRemovalToNonAlpha_StateUnreachable()
      throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "tauTransRemovalToNonAlpha_1.wmod";
    runAbstractionRule(group, subdir, name);
  }

  public void test_tauTransRemovalToNonAlpha_NoFurther() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "tauTransRemovalToNonAlpha_2.wmod";
    runAbstractionRule(group, subdir, name);
  }

  public void test_tauTransRemovalToNonAlpha_StateReachable() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "tauTransRemovalToNonAlpha_3.wmod";
    runAbstractionRule(group, subdir, name);
  }

  public void test_allStatesImplicitlyMarkedAlpha() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "tauTransRemovalToNonAlpha_8.wmod";
    runAbstractionRule(group, subdir, name);
  }

  public void test_allStatesImplicitlyMarkedOmega() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "tauTransRemovalToNonAlpha_9.wmod";
    runAbstractionRule(group, subdir, name);
  }

  public void test_tauTransRemovalToNonAlpha_TwoConsecutiveTauUnreachable()
      throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "tauTransRemovalToNonAlpha_7.wmod";
    runAbstractionRule(group, subdir, name);
  }

  public void test_tauTransRemovalToNonAlpha_TwoConsecutiveTauReachable()
      throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "tauTransRemovalToNonAlpha_4.wmod";
    runAbstractionRule(group, subdir, name);
  }

  public void test_tauTransRemovalToNonAlpha_TwoTau() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "tauTransRemovalToNonAlpha_5.wmod";
    runAbstractionRule(group, subdir, name);
  }

  public void test_tauTransRemovalToNonAlpha_tauLoopNoMarking() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "tauTransRemovalToNonAlpha_10.wmod";
    runAbstractionRule(group, subdir, name);
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
    test_allStatesImplicitlyMarkedAlpha();
    test_tauTransRemovalToNonAlpha_NoFurther();
  }

}
