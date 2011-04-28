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

import net.sourceforge.waters.model.analysis.IdenticalKindTranslator;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


public class RemovalOfDefaultMarkingsRuleTest extends
    AbstractAbstractionRuleTest
{

  // #########################################################################
  // # Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite testSuite =
        new TestSuite(RemovalOfDefaultMarkingsRuleTest.class);
    return testSuite;
  }

  public static void main(final String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }

  // #########################################################################
  // # Overrides for abstract base class
  // # net.sourceforge.waters.analysis.gnonblocking.AbstractAbstractionRuleTest
  protected RemovalOfDefaultMarkingsRule createAbstractionRule
    (final ProductDESProxyFactory factory)
  {
    final KindTranslator translator = IdenticalKindTranslator.getInstance();
    return new RemovalOfDefaultMarkingsRule(factory, translator);
  }

  protected void configureAbstractionRule(final ProductDESProxy des)
  {
    super.configureAbstractionRule(des);
    final RemovalOfDefaultMarkingsRule rule = getAbstractionRule();
    final EventProxy alphaMarking = findEvent(des, ALPHA);
    rule.setAlphaMarking(alphaMarking);
    final EventProxy defaultMarking = findEvent(des, OMEGA);
    rule.setDefaultMarking(defaultMarking);
  }

  protected RemovalOfDefaultMarkingsRule getAbstractionRule()
  {
    return (RemovalOfDefaultMarkingsRule) super.getAbstractionRule();
  }

  // #########################################################################
  // # Test Cases
  /**
   * <P>
   * Tests the model in file
   * {supremica}/examples/waters/tests/abstraction/defaultremoval_1.wmod.
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
   * {supremica}/logs/results/analysis/gnonblocking/RemovalOfDefaultMarkingsRuleTest
   * as a .des file (for text viewing) and as a .wmod file (to load into the
   * IDE).
   * </P>
   */
  public void test_defaultremoval_1() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "defaultremoval_1.wmod";
    runAbstractionRule(group, subdir, name);
  }

  public void test_defaultremoval_2() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "defaultremoval_2.wmod";
    runAbstractionRule(group, subdir, name);
  }

  public void test_nonDefaultRemovalWithAlphaOnSameState() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "defaultremoval_3.wmod";
    runAbstractionRule(group, subdir, name);
  }

  public void test_allStatesImplicitlyMarkedAlpha() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "defaultremoval_4.wmod";
    runAbstractionRule(group, subdir, name);
  }

  public void test_allStatesImplicitlyMarkedOmega() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "defaultremoval_5.wmod";
    runAbstractionRule(group, subdir, name);
  }

  public void test_nonTauLoop() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "defaultremoval_6.wmod";
    runAbstractionRule(group, subdir, name);
  }

  public void test_tauLoop() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "defaultremoval_7.wmod";
    runAbstractionRule(group, subdir, name);
  }

  public void test_selfLoops() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "defaultremoval_8.wmod";
    runAbstractionRule(group, subdir, name);
  }

  public void test_noTransitions() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "defaultremoval_9.wmod";
    runAbstractionRule(group, subdir, name);
  }

  public void test_multipleIncomingTransitions() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "defaultremoval_10.wmod";
    runAbstractionRule(group, subdir, name);
  }

  public void test_multipleOutgoingTransitions() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "defaultremoval_11.wmod";
    runAbstractionRule(group, subdir, name);
  }

  public void test_noncoreachableUnmarkedStates() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "defaultremoval_12.wmod";
    runAbstractionRule(group, subdir, name);
  }

  /**
   * A test to see whether a single abstraction rule object can perform multiple
   * abstractions in sequence.
   */
  public void testReentrant() throws Exception
  {
    test_defaultremoval_1();
    test_tauLoop();
    test_allStatesImplicitlyMarkedAlpha();
    test_allStatesImplicitlyMarkedOmega();
    test_selfLoops();
    test_multipleIncomingTransitions();
    test_multipleOutgoingTransitions();
    test_defaultremoval_2();
    test_nonDefaultRemovalWithAlphaOnSameState();
    test_noTransitions();
    test_nonTauLoop();
    test_defaultremoval_1();
    test_allStatesImplicitlyMarkedOmega();
    test_selfLoops();
    test_defaultremoval_2();
    test_noTransitions();
    test_nonTauLoop();
    test_allStatesImplicitlyMarkedAlpha();
    test_multipleOutgoingTransitions();
    test_nonDefaultRemovalWithAlphaOnSameState();
    test_tauLoop();
  }

}
