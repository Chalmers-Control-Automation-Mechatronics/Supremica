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

import net.sourceforge.waters.model.des.ProductDESProxy;


/**
 * A test for the selfloop subsumption simplifier
 * ({@link SelfloopSubsumptionTRSimplifier}).
 *
 * @author Robi Malik
 */

public class SelfloopSubsumptionTRSimplifierTest
  extends AbstractTRSimplifierTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite() {
    final TestSuite testSuite =
      new TestSuite(SelfloopSubsumptionTRSimplifierTest.class);
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
  protected SelfloopSubsumptionTRSimplifier createTransitionRelationSimplifier()
  {
    return new SelfloopSubsumptionTRSimplifier();
  }

  @Override
  protected void configureTransitionRelationSimplifier()
  {
    configureTransitionRelationSimplifierWithPropositions();
  }


  //#########################################################################
  //# Test Cases
  public void testSelfloopSubsumption1() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "selfloop_subsumption_01.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void testSelfloopSubsumption2() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "selfloop_subsumption_02.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void testSelfloopSubsumption3() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "selfloop_subsumption_03.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void testSelfloopSubsumption4() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "selfloop_subsumption_04.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void testSelfloopSubsumption5() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "selfloop_subsumption_05.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void testSelfloopSubsumption6() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "selfloop_subsumption_06.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void testSelfloopSubsumption7() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "selfloop_subsumption_07.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void testSelfloopSubsumption8() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "selfloop_subsumption_08.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void testSelfloopSubsumption9() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "selfloop_subsumption_09.wmod");
    runTransitionRelationSimplifier(des);
  }

  /**
   * A test to see whether a single transition relation simplifier
   * object can perform multiple abstractions in sequence.
   */
  public void testReentrant() throws Exception
  {
    testSelfloopSubsumption1();
    testSelfloopSubsumption2();
    testSelfloopSubsumption3();
    testSelfloopSubsumption1();
    testSelfloopSubsumption2();
    testSelfloopSubsumption3();
  }

}
