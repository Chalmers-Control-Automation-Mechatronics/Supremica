//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
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
 * A test for the standard <I>Silent Incoming Rule</I>.
 * This test the version of the rule for standard nonblocking,
 * possibly with always enabled events, but without precondition markings.
 *
 * @author Robi Malik
 */

public class TauEliminationTRSimplifierTest
  extends AbstractTRSimplifierTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite testSuite =
      new TestSuite(TauEliminationTRSimplifierTest.class);
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
  protected TauEliminationTRSimplifier createTransitionRelationSimplifier()
  {
    return new TauEliminationTRSimplifier();
  }

  @Override
  protected TauEliminationTRSimplifier getTransitionRelationSimplifier()
  {
    return (TauEliminationTRSimplifier) super.getTransitionRelationSimplifier();
  }


  //#########################################################################
  //# Test Cases
  public void test_tauElimination01()
    throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "tauElimination01.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_tauElimination02()
    throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "tauElimination02.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_tauElimination03()
    throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "tauElimination03.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_tauElimination04()
    throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "tauElimination04.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_tauElimination05()
    throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "tauElimination05.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_tauElimination06()
    throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "tauElimination06.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_tauElimination07()
    throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "tauElimination07.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_tauLoop01()
    throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "tauloop01.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }


  /**
   * A test to see whether a single abstraction rule object can perform multiple
   * abstractions in sequence.
   */
  public void testReentrant() throws Exception
  {
    test_tauElimination01();
    test_tauElimination02();
    test_tauElimination03();
    test_tauElimination04();
    test_tauElimination05();
  }

}
