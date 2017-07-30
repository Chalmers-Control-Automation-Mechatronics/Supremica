//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2017 Robi Malik
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

package net.sourceforge.waters.analysis.certainconf;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.analysis.abstraction.AbstractTRSimplifierTest;
import net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier;
import net.sourceforge.waters.model.des.ProductDESProxy;

public class CertainConflictsTRSimplifierTest extends AbstractTRSimplifierTest {

  public static Test suite() {
    final TestSuite testSuite =
      new TestSuite(CertainConflictsTRSimplifierTest.class);
    return testSuite;
  }

  public static void main(final String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }


  @Override
  protected TransitionRelationSimplifier createTransitionRelationSimplifier()
  {
    return new CertainConflictsTRSimplifier();
  }

  @Override
  protected void configureTransitionRelationSimplifier()
  {
    configureTransitionRelationSimplifierWithPropositions();
  }

  public void test_certainconflicts_1() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "certainconflicts_1.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_certainconflicts_2() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "certainconflicts_2.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_certainconflicts_3() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "certainconflicts_3.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_certainconflicts_4() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "certainconflicts_4.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_certainconflicts_5() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "certainconflicts_5.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_certainconflicts_6() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "certainconflicts_6.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_certainconflicts_7() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "certainconflicts_7.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_certainconflicts_8() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "certainconflicts_8.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_certainconflicts_9() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "certainconflicts_9.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_certainconflicts_10() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "certainconflicts_10.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_certainconflicts_11() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "certainconflicts_11.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_certainconflicts_12() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "certainconflicts_12.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_certainconflicts_13() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "certainconflicts_13.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_certainconflicts_14() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "certainconflicts_14.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_certainconflicts_15() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "certainconflicts_15.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_certainconflicts_16() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "certainconflicts_16.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_certainconflicts_17() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "certainconflicts_17.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_certainconflicts_18() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "certainconflicts_18.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_certainconflicts_tau1() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "certainconflicts_tau1.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_certainconflicts_tau2() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "certainconflicts_tau2.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_certainconflicts_tau3() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "certainconflicts_tau3.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_certainconflicts_tau4() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "certainconflicts_tau4.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_certainconflicts_tau5() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "certainconflicts_tau5.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_certainconflicts_tau6() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "certainconflicts_tau6.wmod");
    runTransitionRelationSimplifier(des);
  }

  @Override
  public void test_basic_7() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "certainconflicts_basic7.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }
}
