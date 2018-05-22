//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
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
