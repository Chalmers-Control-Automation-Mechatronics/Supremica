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


/**
 * A test for the observation equivalence simplifier (
 * {@link ObservationEquivalenceTRSimplifier}).
 *
 * This test is to be used with caution because the same bisimulation module
 * ({@link ObservationEquivalenceTRSimplifier}) is used by the abstraction rule
 * and the isomorphism checker that compares the test output with the expected
 * result. Nevertheless, it may be helpful to show the output of observation
 * equivalence and test how silent events are handled by various configurations
 * of the bisimulation algorithm.
 *
 * @author Robi Malik
 */

public class ObservationEquivalenceTRSimplifierTest
  extends AbstractTRSimplifierTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite() {
    final TestSuite testSuite =
      new TestSuite(ObservationEquivalenceTRSimplifierTest.class);
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
    return new ObservationEquivalenceTRSimplifier();
  }


  //#########################################################################
  //# Test Cases
  public void test_oeq_1() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "oeq_1.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_oeq_2() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "oeq_2.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_oeq_3() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "oeq_3.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_oeq_4() throws Exception
  {
    final ObservationEquivalenceTRSimplifier simplifier =
      (ObservationEquivalenceTRSimplifier) getTransitionRelationSimplifier();
    simplifier.setTransitionRemovalMode
      (ObservationEquivalenceTRSimplifier.TransitionRemoval.ALL);
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "oeq_4.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_oeq_5() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "oeq_5.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_oeq_6() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "oeq_6.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_oeq_7() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "oeq_7.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_oeq_8() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "oeq_8.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_oeq_9() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "oeq_9.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_oeq_10() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "oeq_10.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_oeq_11() throws Exception
  {
    final ObservationEquivalenceTRSimplifier simplifier =
      (ObservationEquivalenceTRSimplifier) getTransitionRelationSimplifier();
    simplifier.setTransitionRemovalMode
      (ObservationEquivalenceTRSimplifier.TransitionRemoval.ALL);
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "oeq_11.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_oeq_12() throws Exception
  {
    final ObservationEquivalenceTRSimplifier simplifier =
      (ObservationEquivalenceTRSimplifier) getTransitionRelationSimplifier();
    simplifier.setTransitionRemovalMode
      (ObservationEquivalenceTRSimplifier.TransitionRemoval.ALL);
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "oeq_12.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_oeq_13() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "oeq_13.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_oeq_14() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "oeq_14.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_oeq_15() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "oeq_15.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_oeq_16() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "oeq_16.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_oeq_17() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "oeq_17.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_oeq_18() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "oeq_18.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_selfLoopObservationalEquivalence01() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "selfLoopObservationalEquivalence01.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_selfLoopObservationalEquivalence02() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "selfLoopObservationalEquivalence02.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_selfLoopObservationalEquivalence03() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "selfLoopObservationalEquivalence03.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_selfLoopObservationalEquivalence04() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "selfLoopObservationalEquivalence04.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_selfLoopObservationalEquivalence05() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "selfLoopObservationalEquivalence05.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_selfLoopObservationalEquivalence06() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "selfLoopObservationalEquivalence06.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }
  public void test_selfLoopObservationalEquivalence07() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "selfLoopObservationalEquivalence07.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  /**
   * A test to see whether a single transition relation simplifier
   * object can perform multiple abstractions in sequence.
   */
  public void testReentrant() throws Exception
  {
    test_oeq_1();
    test_oeq_2();
    test_oeq_3();
    test_oeq_4();
    test_oeq_1();
    test_oeq_2();
  }

}
