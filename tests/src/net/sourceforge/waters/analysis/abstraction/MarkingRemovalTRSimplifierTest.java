//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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

package net.sourceforge.waters.analysis.abstraction;

import junit.framework.Test;
import junit.framework.TestSuite;


public class MarkingRemovalTRSimplifierTest
  extends AbstractTRSimplifierTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite testSuite =
        new TestSuite(MarkingRemovalTRSimplifierTest.class);
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
    return new MarkingRemovalTRSimplifier();
  }


  //#########################################################################
  //# Test Cases
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
   * {supremica}/logs/results/analysis/op/MarkingRemovalTRSimplifierTest as a
   * .des file (for text viewing) and as a .wmod file (to load into the IDE).
   * </P>
   */
  public void test_alpharemoval_1() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_1.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_alpharemoval_2() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_2.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_alphaRemovalFromStateWithMultiMarkings() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_3.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_allStatesImplicitlyMarkedAlpha() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_4.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_nonTauLoop() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_5.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_selfLoops() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_7a.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_noTransitions() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_8.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_multipleTauTransitionsBetween() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_10.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_multipleOutgoingTransitions() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_11.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_noRemovalWithNoTau() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_12.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_twoTransitionsBetweenTauStates() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_13.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_manyTauInSequence() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_14.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_manyTauInSequenceNotAllMarked() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_15.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_selfLoopsRemovalNeeded() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_16a.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_transitionWithMultiEvents() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "markingremoval_17.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_tauLoopWithOutgoingTau() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alpharemoval_18.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  /**
   * A test to see whether a single transition relation simplifier
   * object can perform multiple abstractions in sequence.
   */
  public void testReentrant() throws Exception
  {
    test_alpharemoval_1();
    test_allStatesImplicitlyMarkedAlpha();
    test_selfLoops();
    test_noRemovalWithNoTau();
    test_selfLoopsRemovalNeeded();
    test_multipleOutgoingTransitions();
    test_alpharemoval_2();
    test_alphaRemovalFromStateWithMultiMarkings();
    test_noTransitions();
    test_manyTauInSequenceNotAllMarked();
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
    test_manyTauInSequenceNotAllMarked();
    test_alphaRemovalFromStateWithMultiMarkings();
  }

}
