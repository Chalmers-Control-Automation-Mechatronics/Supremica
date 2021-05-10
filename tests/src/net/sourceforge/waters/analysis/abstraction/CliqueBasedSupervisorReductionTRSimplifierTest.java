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

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;


/**
 * A test for the clique-based supervisor reduction simplifier (
 * {@link MaxCliqueSupervisorReductionTRSimplifier}).
 *
 * @author Robi Malik, Jordan Schroder
 */

public class CliqueBasedSupervisorReductionTRSimplifierTest
  extends AbstractTRSimplifierTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite() {
    final TestSuite testSuite =
      new TestSuite(CliqueBasedSupervisorReductionTRSimplifierTest.class);
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
    return new MaxCliqueSupervisorReductionTRSimplifier();
  }

  @Override
  protected MaxCliqueSupervisorReductionTRSimplifier getTransitionRelationSimplifier()
  {
    return (MaxCliqueSupervisorReductionTRSimplifier)
      super.getTransitionRelationSimplifier();
  }

  @Override
  protected EventEncoding createEventEncoding(final ProductDESProxy des,
                                              final AutomatonProxy aut)
    throws OverflowException
  {
    final EventEncoding encoding = super.createEventEncoding(des, aut);
    encoding.sortProperEvents((byte) ~EventStatus.STATUS_LOCAL,
                              (byte) ~EventStatus.STATUS_CONTROLLABLE);
    return encoding;
  }

  @Override
  protected void configureTransitionRelationSimplifier()
  {
    super.configureTransitionRelationSimplifier();
    final MaxCliqueSupervisorReductionTRSimplifier simplifier =
      getTransitionRelationSimplifier();
    final ListBufferTransitionRelation rel = simplifier.getTransitionRelation();
    if (rel.getNumberOfProperEvents() >= 2 &&
        EventStatus.isControllableEvent(rel.getProperEventStatus(1))) {
      simplifier.setSupervisedEvent(1);
    } else {
      simplifier.setSupervisedEvent(0);
    }
  }


  //#########################################################################
  //# Overridden Test Cases
  @Override
  public void test_basic_1() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "basic_supred_1.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  @Override
  public void test_basic_2() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "basic_supred_2.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  @Override
  public void test_basic_3() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "basic_supred_3.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  @Override
  public void test_basic_4() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "basic_supred_4.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  @Override
  public void test_basic_5() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "basic_supred_5.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  @Override
  public void test_basic_6() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "basic_supred_6.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }


  //#########################################################################
  //# Specific Test Cases
  public void test_supred_01() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "supred_01.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_supred_02() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "supred_02.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_supred_03() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "supred_03.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_supred_04() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "supred_04.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  /**
   * A test to see whether a single transition relation simplifier
   * object can perform multiple abstractions in sequence.
   */
  public void testReentrant() throws Exception
  {
    test_supred_01();
    test_supred_02();
    test_supred_01();
    test_supred_02();
    test_supred_01();
  }
}
