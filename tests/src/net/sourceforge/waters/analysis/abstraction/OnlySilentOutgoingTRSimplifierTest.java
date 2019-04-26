//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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

import net.sourceforge.waters.model.des.ProductDESProxy;


public class OnlySilentOutgoingTRSimplifierTest
  extends AbstractTRSimplifierTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite testSuite =
      new TestSuite(OnlySilentOutgoingTRSimplifierTest.class);
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
    final OnlySilentOutgoingTRSimplifier simplifier =
      new OnlySilentOutgoingTRSimplifier();
    return simplifier;
  }

  @Override
  protected void configureTransitionRelationSimplifier()
  {
    configureTransitionRelationSimplifierWithPropositions();
  }


  //#########################################################################
  //# Test Cases
  public void test_tauTransRemovalFromNonAlpha_StateUnreachable()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "tauTransRemovalFromNonAlpha_1.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_tauTransRemovalFromNonAlpha_NoFurther()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "tauTransRemovalFromNonAlpha_2.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_tauTransRemovalFromNonAlpha_tauAndNonTauOutgoingTransitions()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "tauTransRemovalFromNonAlpha_3.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_tauTransRemovalFromNonAlpha_TwoTauOutgoing()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "tauTransRemovalFromNonAlpha_4.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_tauTransRemovalFromNonAlpha_multipleIncomingTransitions()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "tauTransRemovalFromNonAlpha_5.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_tauTransRemovalFromNonAlpha_TwoConsecutiveTauUnreachable()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "tauTransRemovalFromNonAlpha_7.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_allStatesImplicitlyMarkedAlpha()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "tauTransRemovalFromNonAlpha_8.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_allStatesImplicitlyMarkedOmega()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "tauTransRemovalFromNonAlpha_9.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_tauTransRemovalFromNonAlpha_tauLoopNoMarking()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "tauTransRemovalFromNonAlpha_10.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_tauTransRemovalFromNonAlpha_removalOfInitialState()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "tauTransRemovalFromNonAlpha_11.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_tauTransRemovalFromNonAlpha_nonRemovalOfStateWithNoOutgoingTransitions()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "tauTransRemovalFromNonAlpha_12.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_tauTransRemovalFromNonAlpha_transitionOrder()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "tauTransRemovalFromNonAlpha_13.wmod");
    runTransitionRelationSimplifier(des);
  }

  /**
   * A test to see whether a single abstraction rule object can perform multiple
   * abstractions in sequence.
   */
  public void testReentrant()
  throws Exception
  {
    test_tauTransRemovalFromNonAlpha_StateUnreachable();
    test_tauTransRemovalFromNonAlpha_NoFurther();
    test_tauTransRemovalFromNonAlpha_TwoTauOutgoing();
    test_tauTransRemovalFromNonAlpha_tauAndNonTauOutgoingTransitions();
    test_allStatesImplicitlyMarkedOmega();
    test_tauTransRemovalFromNonAlpha_StateUnreachable();
    test_tauTransRemovalFromNonAlpha_TwoConsecutiveTauUnreachable();
    test_tauTransRemovalFromNonAlpha_multipleIncomingTransitions();
    test_tauTransRemovalFromNonAlpha_nonRemovalOfStateWithNoOutgoingTransitions();
    test_allStatesImplicitlyMarkedAlpha();
    test_tauTransRemovalFromNonAlpha_NoFurther();
  }

}
