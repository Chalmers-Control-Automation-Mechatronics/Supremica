//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.analysis.gnonblocking
//# CLASS:   RemovalOfAlphaMarkingsRuleTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.gnonblocking;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


public class RemovalOfAlphaMarkingsRuleTest extends AbstractAbstractionRuleTest
{

  // #########################################################################
  // # Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite testSuite =
        new TestSuite(RemovalOfAlphaMarkingsRuleTest.class);
    return testSuite;
  }

  public static void main(final String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }

  // #########################################################################
  // # Overrides for abstract base class
  // # net.sourceforge.waters.analysis.gnonblocking.AbstractAbstractionRuleTest
  protected RemovalOfAlphaMarkingsRule createAbstractionRule(
                                                             final ProductDESProxyFactory factory)
  {
    return new RemovalOfAlphaMarkingsRule(factory);
  }

  protected void configureAbstractionRule(final ProductDESProxy des)
  {
    super.configureAbstractionRule(des);
    final RemovalOfAlphaMarkingsRule rule = getAbstractionRule();
    final EventProxy alphaMarking = findEvent(des, ALPHA);
    rule.setAlphaMarking(alphaMarking);
  }

  protected RemovalOfAlphaMarkingsRule getAbstractionRule()
  {
    return (RemovalOfAlphaMarkingsRule) super.getAbstractionRule();
  }

  // #########################################################################
  // # Test Cases
  /**
   * <P>
   * Tests the model in file
   * {supremica}/examples/waters/tests/abstraction/alpharemoval_1.wmod.
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
   * {supremica
   * }/logs/results/analysis/gnonblocking/RemovalOfAlphaMarkingsRuleTest as a
   * .des file (for text viewing) and as a .wmod file (to load into the IDE).
   * </P>
   */
  public void test_alpharemoval_1() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_1.wmod";
    runAbstractionRule(group, subdir, name);
  }

  public void test_alpharemoval_2() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_2.wmod";
    runAbstractionRule(group, subdir, name);
  }

  public void test_alphaRemovalFromStateWithMultiMarkings() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_3.wmod";
    runAbstractionRule(group, subdir, name);
  }

  public void test_allStatesImplicitlyMarkedAlpha() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_4.wmod";
    runAbstractionRule(group, subdir, name);
  }

  public void test_nonTauLoop() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_5.wmod";
    runAbstractionRule(group, subdir, name);
  }

  public void test_tauLoop() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_6.wmod";
    runAbstractionRule(group, subdir, name);
  }

  public void test_selfLoops() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_7.wmod";
    runAbstractionRule(group, subdir, name);
  }

  public void test_noTransitions() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_8.wmod";
    runAbstractionRule(group, subdir, name);
  }

  public void test_multipleIncomingTransitions() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_9.wmod";
    runAbstractionRule(group, subdir, name);
  }

  public void test_multipleTauTransitionsBetween() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_10.wmod";
    runAbstractionRule(group, subdir, name);
  }

  public void test_multipleOutgoingTransitions() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_11.wmod";
    runAbstractionRule(group, subdir, name);
  }

  public void test_noRemovalWithNoTau() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_12.wmod";
    runAbstractionRule(group, subdir, name);
  }

  public void test_twoTransitionsBetweenTauStates() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_13.wmod";
    runAbstractionRule(group, subdir, name);
  }

  public void test_manyTauInSequence() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_14.wmod";
    runAbstractionRule(group, subdir, name);
  }

  /**
   * A test to see whether a single abstraction rule object can perform multiple
   * abstractions in sequence.
   */
  public void testReentrant() throws Exception
  {
    test_alpharemoval_1();
    test_tauLoop();
    test_allStatesImplicitlyMarkedAlpha();
    test_selfLoops();
    test_noRemovalWithNoTau();
    test_multipleIncomingTransitions();
    test_multipleOutgoingTransitions();
    test_alpharemoval_2();
    test_alphaRemovalFromStateWithMultiMarkings();
    test_noTransitions();
    test_multipleTauTransitionsBetween();
    test_manyTauInSequence();
    test_twoTransitionsBetweenTauStates();
    test_nonTauLoop();
    test_alpharemoval_1();
    test_selfLoops();
    test_alpharemoval_2();
    test_noTransitions();
    test_nonTauLoop();
    test_allStatesImplicitlyMarkedAlpha();
    test_noRemovalWithNoTau();
    test_multipleOutgoingTransitions();
    test_alphaRemovalFromStateWithMultiMarkings();
    test_tauLoop();
  }
}
