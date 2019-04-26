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


/**
 * A test for the <I>Limited Certain Conflicts Rule</I>.
 *
 * @author Robi Malik
 */

public class LimitedCertainConflictsTRSimplifierTest
  extends AbstractTRSimplifierTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite testSuite =
      new TestSuite(LimitedCertainConflictsTRSimplifierTest.class);
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
    return new LimitedCertainConflictsTRSimplifier();
  }

  @Override
  protected void configureTransitionRelationSimplifier()
  {
    configureTransitionRelationSimplifierWithPropositions();
  }


  //#########################################################################
  //# Test Cases
  @Override
  public void test_basic_7() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "limitedCertainConflicts_basic_7.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_nonblocking()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "nonalphadet_10.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_limitedCertainConflicts_1()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "limitedCertainConflicts_1.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_limitedCertainConflicts_2()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "limitedCertainConflicts_2.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_limitedCertainConflicts_3()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "limitedCertainConflicts_3.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_limitedCertainConflicts_4()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "limitedCertainConflicts_4.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_limitedCertainConflicts_5()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "limitedCertainConflicts_5.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_limitedCertainConflicts_6()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "limitedCertainConflicts_6.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_limitedCertainConflicts_7()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "limitedCertainConflicts_7.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_limitedCertainConflicts_8()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "limitedCertainConflicts_8.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_limitedCertainConflicts_9()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "limitedCertainConflicts_9.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_limitedCertainConflicts_10()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "limitedCertainConflicts_10.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_limitedCertainConflicts_11()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "limitedCertainConflicts_11.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_limitedCertainConflicts_12()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "limitedCertainConflicts_12.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_limitedCertainConflicts_13()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "limitedCertainConflicts_13.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_limitedCertainConflicts_14()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "limitedCertainConflicts_14.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_limitedCertainConflicts_15()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "limitedCertainConflicts_15.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_limitedCertainConflicts_16()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "limitedCertainConflicts_16.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_certainConflicts_15()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "certainconflicts_15.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_certainconflicts_17()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "certainconflicts_17.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_certainconflicts_18()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "certainconflicts_18.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_alwaysEnabledLimitedCertainConflicts01()
  throws Exception
  {
    final ProductDESProxy des = getCompiledDES
      ("tests", "abstraction", "alwaysEnabledLimitedCertainConflicts01.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_alwaysEnabledLimitedCertainConflicts02()
  throws Exception
  {
    final ProductDESProxy des = getCompiledDES
      ("tests", "abstraction", "alwaysEnabledLimitedCertainConflicts02.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_alwaysEnabledLimitedCertainConflicts03()
  throws Exception
  {
    final ProductDESProxy des = getCompiledDES
      ("tests", "abstraction", "alwaysEnabledLimitedCertainConflicts03.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_alwaysEnabledLimitedCertainConflicts04()
  throws Exception
  {
    final ProductDESProxy des = getCompiledDES
      ("tests", "abstraction", "alwaysEnabledLimitedCertainConflicts04.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_alwaysEnabledLimitedCertainConflicts05()
  throws Exception
  {
    final ProductDESProxy des = getCompiledDES
      ("tests", "abstraction", "alwaysEnabledLimitedCertainConflicts05.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_alwaysEnabledLimitedCertainConflicts06()
  throws Exception
  {
    final ProductDESProxy des = getCompiledDES
      ("tests", "abstraction", "alwaysEnabledLimitedCertainConflicts06.wmod");
    runTransitionRelationSimplifier(des);
  }

  /**
   * A test to see whether a single abstraction rule object can perform multiple
   * abstractions in sequence.
   */
  public void testReentrant() throws Exception
  {
    test_nonblocking();
    test_limitedCertainConflicts_1();
    test_limitedCertainConflicts_2();
    test_limitedCertainConflicts_3();
    test_limitedCertainConflicts_4();
    test_limitedCertainConflicts_3();
    test_limitedCertainConflicts_2();
    test_limitedCertainConflicts_1();
    test_nonblocking();
  }

}
