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

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.kindtranslator.IdenticalKindTranslator;
import net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;


/**
 * A test for the synthesis observation equivalence algorithm in
 * ({@link SynthesisObservationEquivalenceTRSimplifier}).
 *
 * @author Sahar Mohajerani, Robi Malik
 */

public class SynthesisObservationEquivalenceTRSimplifierTest
  extends AbstractTRSimplifierTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite testSuite =
      new TestSuite(SynthesisObservationEquivalenceTRSimplifierTest.class);
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
  protected SynthesisObservationEquivalenceTRSimplifier
    createTransitionRelationSimplifier()
  {
    return new SynthesisObservationEquivalenceTRSimplifier();
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
    final SynthesisObservationEquivalenceTRSimplifier simplifier =
      getTransitionRelationSimplifier();
    simplifier.setUsesWeakSynthesisObservationEquivalence(false);
    simplifier.setDefaultMarkingID(mDefaultMarkingID);
  }

  @Override
  protected SynthesisObservationEquivalenceTRSimplifier getTransitionRelationSimplifier()
  {
    return (SynthesisObservationEquivalenceTRSimplifier)
      super.getTransitionRelationSimplifier();
  }


  //#########################################################################
  //# Test Cases
  public void test_synthesisAbstraction_1()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "soe01.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_synthesisAbstraction_2()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "soe02.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_synthesisAbstraction_3()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "soe03.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_synthesisAbstraction_4()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "soe04.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_synthesisAbstraction_5()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "soe05.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_synthesisAbstraction_6()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "soe06.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_synthesisAbstraction_7()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "soe07.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_synthesisAbstraction_8() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "soe08.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_synthesisAbstraction_9() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "soe09.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_synthesisAbstraction_10() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "soe10.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_synthesisAbstraction_11() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "soe11.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_synthesisAbstraction_12() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "soe12.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_synthesisAbstraction_13() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "soe13.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_synthesisAbstraction_14() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "soe14.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_synthesisAbstraction_15() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "soe15.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_synthesisAbstraction_16() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "soe16.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_synthesisAbstraction_17() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "soe17.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_synthesisAbstraction_18() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "soe18.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_synthesisAbstraction_19() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "soe19.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_synthesisAbstraction_20() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "soe20.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_synthesisAbstraction_21() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "soe21.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_synthesisAbstraction_22() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "soe22.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_synthesisAbstraction_23() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "soe23.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_synthesisAbstraction_24() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "soe24.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_synthesisAbstraction_25() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "soe25.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_synthesisAbstraction_26() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "soe26.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_synthesisAbstraction_27() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "soe27.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_synthesisAbstraction_28() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "soe28.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_synthesisAbstraction_29() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "soe29.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_synthesisAbstraction_30() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "soe30.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_synthesisAbstraction_31() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "soe31.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_synthesisAbstraction_32() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "soe32.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_synthesisAbstraction_33() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "soe33.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_synthesisAbstraction_36() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "soe36.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_synthesisAbstraction_37() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "soe37.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  /**
   * A test to see whether a single abstraction rule object can perform
   * multiple abstractions in sequence.
   */
  public void testReentrant() throws Exception
  {
    test_synthesisAbstraction_4();
    test_synthesisAbstraction_3();
    test_synthesisAbstraction_2();
    test_synthesisAbstraction_1();
    test_synthesisAbstraction_2();
    test_synthesisAbstraction_3();
    test_synthesisAbstraction_4();
    test_synthesisAbstraction_5();
    test_synthesisAbstraction_6();
    test_synthesisAbstraction_5();
    test_synthesisAbstraction_4();
    test_synthesisAbstraction_3();
    test_synthesisAbstraction_2();
    test_synthesisAbstraction_1();
  }


  //#########################################################################
  //# Data Members
  private int mDefaultMarkingID;

}
