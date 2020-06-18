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

import net.sourceforge.waters.model.des.ProductDESProxy;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * A test for the <I>Active Events Rule</I>.
 *
 * @author Robi Malik
 */

public class ActiveEventsTRSimplifierTest
  extends AbstractTRSimplifierTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite testSuite =
      new TestSuite(ActiveEventsTRSimplifierTest.class);
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
    return new ActiveEventsTRSimplifier();
  }

  @Override
  protected void configureTransitionRelationSimplifier()
  {
    configureTransitionRelationSimplifierWithPropositions();
  }


  //#########################################################################
  //# Test Cases
  public void test_activeEvents_1()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "activeEvents01.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_activeEvents_2()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "activeEvents02.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_activeEvents_3()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "activeEvents03.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_activeEvents_4()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "activeEvents04.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_activeEvents_5()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "activeEvents05.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_activeEvents_6()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "activeEvents06.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_activeEvents_7()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "activeEvents07.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_activeEvents_8()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "activeEvents08.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_activeEvents_9()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "activeEvents09.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_activeEvents_10()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "activeEvents10plus.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_activeEvents_11()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "activeEvents11.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_activeEvents_12()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "activeEvents12.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_activeEvents_13()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "activeEvents13.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_activeEvents_14()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "activeEvents14.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_activeEvents_15()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "activeEvents15.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_activeEvents_16()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "activeEvents16.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_activeEvents_17()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "activeEvents17.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_activeEvents_18()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "activeEvents18.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_activeEvents_19()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "activeEvents19.wmod");
    runTransitionRelationSimplifier(des);
  }


  /**
   * A test to see whether a single abstraction rule object can perform multiple
   * abstractions in sequence.
   */
  public void testReentrant() throws Exception
  {
    test_activeEvents_1();
    test_activeEvents_2();
    test_activeEvents_3();
    test_activeEvents_4();
    test_activeEvents_5();
    test_activeEvents_5();
    test_activeEvents_4();
    test_activeEvents_3();
    test_activeEvents_2();
    test_activeEvents_1();
  }

}
