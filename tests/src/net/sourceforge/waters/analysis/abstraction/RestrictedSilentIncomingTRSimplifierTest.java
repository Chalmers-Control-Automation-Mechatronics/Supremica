//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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
 * A test for the <I>Silent Incoming Rule</I>.
 * This tests the {@link SilentIncomingTRSimplifier} when it is configured
 * to remove only tau transitions leading to states that become unreachable.
 *
 * @see SilentIncomingTRSimplifier#setRestrictsToUnreachableStates(boolean)
 * @author Robi Malik
 */

public class RestrictedSilentIncomingTRSimplifierTest
  extends AbstractTRSimplifierTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite testSuite =
      new TestSuite(RestrictedSilentIncomingTRSimplifierTest.class);
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
    final SilentIncomingTRSimplifier simplifier =
      new SilentIncomingTRSimplifier();
    simplifier.setRestrictsToUnreachableStates(true);
    return simplifier;
  }

  @Override
  protected void configureTransitionRelationSimplifier()
  {
    configureTransitionRelationSimplifierWithPropositions();
  }


  //#########################################################################
  //# Test Cases
  public void test_tauTransRemovalToNonAlpha_StateUnreachable()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "tauTransRemovalToNonAlpha_1.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_tauTransRemovalToNonAlpha_NoFurther() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "tauTransRemovalToNonAlpha_2.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_tauTransRemovalToNonAlpha_StateReachable() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "tauTransRemovalToNonAlpha_3a.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_allStatesImplicitlyMarkedAlpha() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "tauTransRemovalToNonAlpha_8.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_allStatesImplicitlyMarkedOmega() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "tauTransRemovalToNonAlpha_9.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_tauTransRemovalToNonAlpha_TwoConsecutiveTauUnreachable()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "tauTransRemovalToNonAlpha_7.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_tauTransRemovalToNonAlpha_TwoConsecutiveTauReachable()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "tauTransRemovalToNonAlpha_4a.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_tauTransRemovalToNonAlpha_TwoTau() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "tauTransRemovalToNonAlpha_5.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_tauTransRemovalToNonAlpha_tauLoopNoMarking()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "tauTransRemovalToNonAlpha_10.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_tauTransRemovalToNonAlpha_oneRemovaableTauAndOneNonremovable()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "tauTransRemovalToNonAlpha_11.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_tauTransRemovalToNonAlpha_12()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "tauTransRemovalToNonAlpha_12.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_tauTransRemovalToNonAlpha_13()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "tauTransRemovalToNonAlpha_13.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_silentIncoming01()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "silentIncoming01.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
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
    test_tauTransRemovalToNonAlpha_oneRemovaableTauAndOneNonremovable();
    test_allStatesImplicitlyMarkedAlpha();
    test_tauTransRemovalToNonAlpha_NoFurther();
  }

}
