//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:   CoreachabilityTRSimplifierTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import junit.framework.Test;
import junit.framework.TestSuite;


public class CoreachabilityTRSimplifierTest
  extends AbstractTRSimplifierTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite testSuite =
        new TestSuite(CoreachabilityTRSimplifierTest.class);
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
    return new CoreachabilityTRSimplifier();
  }

  @Override
  protected void configureTransitionRelationSimplifier()
  {
    configureTransitionRelationSimplifierWithPropositions();
  }


  //#########################################################################
  //# Test Cases
  /**
   * <P>
   * Tests the model in file {supremica}/examples/waters/tests/abstraction/
   * noncoreachableStatesRemoval_1.wmod.
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
   * {supremica }/logs/results/analysis/op/CoreachabilityTRSimplifierTest as
   * a .des file (for text viewing) and as a .wmod file (to load into the IDE).
   * </P>
   */
  public void test_noncoreachableStatesRemoval_1() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "noncoreachableremoval_1.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_noncoreachableStatesRemoval_2() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "noncoreachableremoval_2.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  // All states implicitly marked alpha.
  public void test_noncoreachableStatesRemoval_3() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "noncoreachableremoval_3.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_allStatesImplicitlyMarkedOmega() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "noncoreachableremoval_4.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_unreachableLoop() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "noncoreachableremoval_5.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_reachableLoop() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "noncoreachableremoval_6.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_selfLoops() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "noncoreachableremoval_7.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_noTransitions() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "noncoreachableremoval_8.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_multipleIncomingTransitions() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "noncoreachableremoval_9.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_multipleOutgoingTransitions() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "noncoreachableremoval_10.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_reachableBeforeNotAfter() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "noncoreachableremoval_11.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_stateWithReachableAndNonreachablePath() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "noncoreachableremoval_12.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  /**
   * A test to see whether a single abstraction rule object can perform multiple
   * abstractions in sequence.
   */
  public void testReentrant() throws Exception
  {
    test_noncoreachableStatesRemoval_1();
    test_reachableLoop();
    test_noncoreachableStatesRemoval_3();
    test_selfLoops();
    test_reachableBeforeNotAfter();
    test_multipleIncomingTransitions();
    test_multipleOutgoingTransitions();
    test_noncoreachableStatesRemoval_2();
    test_noTransitions();
    test_unreachableLoop();
    test_noncoreachableStatesRemoval_1();
    test_selfLoops();
    test_reachableBeforeNotAfter();
    test_noncoreachableStatesRemoval_2();
    test_stateWithReachableAndNonreachablePath();
    test_noTransitions();
    test_unreachableLoop();
    test_noncoreachableStatesRemoval_3();
    test_multipleOutgoingTransitions();
    test_stateWithReachableAndNonreachablePath();
    test_reachableLoop();
  }

}
