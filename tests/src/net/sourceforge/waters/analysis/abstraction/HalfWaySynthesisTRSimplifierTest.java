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

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.kindtranslator.IdenticalKindTranslator;
import net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;


public class HalfWaySynthesisTRSimplifierTest
  extends AbstractTRSimplifierTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite testSuite =
        new TestSuite(HalfWaySynthesisTRSimplifierTest.class);
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
  protected HalfWaySynthesisTRSimplifier createTransitionRelationSimplifier()
  {
    return new HalfWaySynthesisTRSimplifier();
  }

  @Override
  protected HalfWaySynthesisTRSimplifier getTransitionRelationSimplifier()
  {
    return (HalfWaySynthesisTRSimplifier)
      super.getTransitionRelationSimplifier();
  }

  @Override
  protected EventEncoding createEventEncoding(final ProductDESProxy des,
                                              final AutomatonProxy aut)
   throws OverflowException
  {
    final KindTranslator translator = IdenticalKindTranslator.getInstance();
    final EventEncoding encoding = new EventEncoding(aut, translator);
    final int numEvents = encoding.getNumberOfProperEvents();
    for (int e = EventEncoding.NONTAU; e < numEvents; e++) {
      final EventProxy event = encoding.getProperEvent(e);
      if (!event.isObservable()) {
        final byte status = encoding.getProperEventStatus(e);
        encoding.setProperEventStatus
          (e, status | EventStatus.STATUS_LOCAL);
      }
    }
    mDefaultMarkingID = -1;
    for (int p = 0; p < encoding.getNumberOfPropositions(); p++) {
      final EventProxy prop = encoding.getProposition(p);
      final String name = prop.getName();
      if (name.equals(EventDeclProxy.DEFAULT_MARKING_NAME)) {
        mDefaultMarkingID = p;
        break;
      }
    }
    encoding.sortProperEvents((byte) ~EventStatus.STATUS_LOCAL,
                              EventStatus.STATUS_CONTROLLABLE);
    return encoding;
  }

  @Override
  protected void configureTransitionRelationSimplifier()
  {
    super.configureTransitionRelationSimplifier();
    final HalfWaySynthesisTRSimplifier simplifier =
      getTransitionRelationSimplifier();
    simplifier.setDefaultMarkingID(mDefaultMarkingID);
  }


  //#########################################################################
  //# Test Cases
  /**
   * <P>
   * Tests the model in file {supremica}/examples/waters/tests/abstraction/
   * HalfwaySynthesis_1.wmod.
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
   * Unobservable events are considered as local.
   * </P>
   *
   * <P>
   * After running the test, any automaton created by the rule is saved in
   * {supremica }/logs/results/analysis/op/HalfWaySynthesisTRSimplifierTest as
   * a .des file (for text viewing) and as a .wmod file (to load into the IDE).
   * </P>
   */
  public void test_HalfwaySynthesis_1() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "HalfwaySynthesis_1.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_HalfwaySynthesis_2() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "HalfwaySynthesis_2.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_HalfwaySynthesis_3() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "HalfwaySynthesis_3.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_HalfwaySynthesis_4() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "HalfwaySynthesis_4.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_HalfwaySynthesis_5() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "HalfwaySynthesis_5.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_HalfwaySynthesis_6() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "HalfwaySynthesis_6.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_HalfwaySynthesis_7() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "HalfwaySynthesis_7.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_HalfwaySynthesis_8() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "HalfwaySynthesis_8.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_HalfwaySynthesis_9() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "HalfwaySynthesis_9.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_HalfwaySynthesis_10() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "HalfwaySynthesis_10.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_HalfwaySynthesis_11() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "HalfwaySynthesis_11.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_HalfwaySynthesis_12() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "HalfwaySynthesis_12.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_HalfwaySynthesis_13() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "HalfwaySynthesis_13.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_HalfwaySynthesis_14() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "HalfwaySynthesis_14.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  @Override
  public void test_basic_7() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "HalfwaySynthesisBasic_7.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  /**
   * A test to see whether a single abstraction rule object can perform
   * multiple abstractions in sequence.
   */
  public void testReentrant() throws Exception
  {
    test_HalfwaySynthesis_1();
    test_HalfwaySynthesis_2();
    test_HalfwaySynthesis_3();
    test_HalfwaySynthesis_4();
    test_HalfwaySynthesis_5();
    test_HalfwaySynthesis_4();
    test_HalfwaySynthesis_3();
    test_HalfwaySynthesis_2();
    test_HalfwaySynthesis_1();
  }


  //#########################################################################
  //# Data Members
  private int mDefaultMarkingID;

}
