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


public class RemovalOfNoncoreachableStatesRuleTest extends
    AbstractAbstractionRuleTest
{

  // #########################################################################
  // # Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite testSuite =
        new TestSuite(RemovalOfNoncoreachableStatesRuleTest.class);
    return testSuite;
  }

  public static void main(final String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }

  // #########################################################################
  // # Overrides for abstract base class
  // # net.sourceforge.waters.analysis.gnonblocking.AbstractAbstractionRuleTest
  protected RemovalOfNoncoreachableStatesRule createAbstractionRule(
                                                                    final ProductDESProxyFactory factory)
  {
    return new RemovalOfNoncoreachableStatesRule(factory);
  }

  protected void configureAbstractionRule(final ProductDESProxy des)
  {
    super.configureAbstractionRule(des);
    final RemovalOfNoncoreachableStatesRule rule = getAbstractionRule();
    final EventProxy alphaMarking = findEvent(des, ALPHA);
    rule.setAlphaMarking(alphaMarking);
    final EventProxy defaultMarking = findEvent(des, OMEGA);
    rule.setDefaultMarking(defaultMarking);
  }

  protected RemovalOfNoncoreachableStatesRule getAbstractionRule()
  {
    return (RemovalOfNoncoreachableStatesRule) super.getAbstractionRule();
  }

  // #########################################################################
  // # Test Cases
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
   * {supremica }/logs/results/analysis/gnonblocking/
   * RemovalOfNoncoreachableStatesRuleTest as a .des file (for text viewing) and
   * as a .wmod file (to load into the IDE).
   * </P>
   */
  public void test_noncoreachableStatesRemoval_1() throws Exception
  {
    // TODO Please check in those .wmod files.
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "noncoreachableremoval_1.wmod";
    runAbstractionRule(group, subdir, name);
  }

  public void test_noncoreachableStatesRemoval_2() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "noncoreachableremoval_2.wmod";
    runAbstractionRule(group, subdir, name);
  }

  // TODO More tests needed ...

  /**
   * A test to see whether a single abstraction rule object can perform multiple
   * abstractions in sequence.
   */
  public void testReentrant() throws Exception
  {
    // TODO As soon as there are more tests, try some more variation here.
    test_noncoreachableStatesRemoval_1();
    test_noncoreachableStatesRemoval_2();
    test_noncoreachableStatesRemoval_1();
    test_noncoreachableStatesRemoval_2();
  }

}
