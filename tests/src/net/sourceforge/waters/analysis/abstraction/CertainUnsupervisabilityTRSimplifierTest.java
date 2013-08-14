//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:   HalfWaySynthesisTRSimplifierTest
//###########################################################################
//# $Id: 28b5d3e8a78c120597cf75fe48443bd1313c7cb6 $
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.model.analysis.IdenticalKindTranslator;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;


public class CertainUnsupervisabilityTRSimplifierTest
  extends AbstractTRSimplifierTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite testSuite =
        new TestSuite(CertainUnsupervisabilityTRSimplifierTest.class);
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
  protected CertainUnsupervisabilityTRSimplifier createTransitionRelationSimplifier()
  {
    return new CertainUnsupervisabilityTRSimplifier();
  }

  @Override
  protected CertainUnsupervisabilityTRSimplifier getTransitionRelationSimplifier()
  {
    return (CertainUnsupervisabilityTRSimplifier)
      super.getTransitionRelationSimplifier();
  }

  @Override
  protected EventEncoding createEventEncoding(final ProductDESProxy des,
                                              final AutomatonProxy aut)
  {
    final KindTranslator translator = IdenticalKindTranslator.getInstance();
    final EventEncoding encoding = new EventEncoding(aut, translator);
    final int numEvents = encoding.getNumberOfProperEvents();
    for (int e = EventEncoding.NONTAU; e < numEvents; e++) {
      final EventProxy event = encoding.getProperEvent(e);
      if (!event.isObservable()) {
        final byte status = encoding.getProperEventStatus(e);
        encoding.setProperEventStatus
          (e, status | EventEncoding.STATUS_LOCAL);
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
    encoding.sortProperEvents((byte) ~EventEncoding.STATUS_LOCAL,
                              EventEncoding.STATUS_CONTROLLABLE);
    return encoding;
  }

  @Override
  protected void configureTransitionRelationSimplifier()
  {
    super.configureTransitionRelationSimplifier();
    final CertainUnsupervisabilityTRSimplifier simplifier =
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
  public void test_CertainUnsup_1() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "CertainUnsup_01.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_CertainUnsup_2() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "CertainUnsup_02.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_CertainUnsup_3() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "CertainUnsup_03.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_CertainUnsup_4() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "CertainUnsup_04.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

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

