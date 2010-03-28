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


public class RemovalOfAlphaMarkingsRuleTest
  extends AbstractAbstractionRuleTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite() {
    final TestSuite testSuite =
      new TestSuite(RemovalOfAlphaMarkingsRuleTest.class);
    return testSuite;
  }

  public static void main(final String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }


  //#########################################################################
  //# Overrides for abstract base class
  //# net.sourceforge.waters.analysis.gnonblocking.AbstractAbstractionRuleTest
  protected RemovalOfAlphaMarkingsRule createAbstractionRule
    (final ProductDESProxyFactory factory)
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


  //#########################################################################
  //# Test Cases
  /**
   * Tests the model in file
   * {supremica}/examples/waters/tests/abstraction/alpharemoval_1.wmod.
   * All test modules contain two automata, named "before" and "after",
   * defining an automaton to be simplified and the expected result.
   * The names of critical events are expected to be "tau", ":alpha",
   * and ":accepting", respectively.
   * After running the test, any automaton created by the rule is saved in
   * {supremica}/logs/results/analysis/gnonblocking/RemovalOfAlphaMarkingsRuleTest
   * as a .des file (for text viewing) and as a .wmod file (to load into the
   * IDE).
   */
  public void test_alpharemoval_1() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_1.wmod";
    runAbstractionRule(group, subdir, name);
  }

  // TODO More tests needed ...

  /**
   * A test to see whether a single abstraction rule object can perform
   * multiple abstractions in sequence.
   */
  public void testReentrant() throws Exception
  {
    // TODO As soon as there are more tests, try some variation here.
    test_alpharemoval_1();
    test_alpharemoval_1();
    test_alpharemoval_1();
  }

}
