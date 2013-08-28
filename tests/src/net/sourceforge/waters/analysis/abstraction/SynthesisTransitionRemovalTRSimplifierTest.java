//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:   ObservationEquivalenceTRSimplifierTest
//###########################################################################
//# $Id$
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


/**
 * A test for the observation equivalence simplifier (
 * {@link ObservationEquivalenceTRSimplifier}).
 *
 * This test is to be used with caution because the same bisimulation module
 * ({@link ObservationEquivalenceTRSimplifier}) is used by the abstraction rule
 * and the isomorphism checker that compares the test output with the expected
 * result. Nevertheless, it may be helpful to show the output of observation
 * equivalence and test how silent events are handled by various configurations
 * of the bisimulation algorithm.
 *
 * @author Robi Malik
 */

public class SynthesisTransitionRemovalTRSimplifierTest
  extends AbstractTRSimplifierTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite() {
    final TestSuite testSuite =
      new TestSuite(SynthesisTransitionRemovalTRSimplifierTest.class);
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
    return new SynthesisTransitionRemovalTRSimplifier();
  }

  @Override
  protected SynthesisTransitionRemovalTRSimplifier getTransitionRelationSimplifier()
  {
    return (SynthesisTransitionRemovalTRSimplifier)
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
    final SynthesisTransitionRemovalTRSimplifier simplifier =
      getTransitionRelationSimplifier();
    simplifier.setDefaultMarkingID(mDefaultMarkingID);
  }


  //#########################################################################
  //# Test Cases
  public void test_synthesis_transition_removal_01() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "synthesis_transition_removal_01.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_synthesis_transition_removal_02() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "synthesis_transition_removal_02.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }


  public void test_synthesis_transition_removal_03() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "synthesis_transition_removal_03.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_synthesis_transition_removal_04() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "synthesis_transition_removal_04.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_synthesis_transition_removal_05() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "synthesis_transition_removal_05.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_synthesis_transition_removal_06() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "synthesis_transition_removal_06.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_synthesis_transition_removal_07() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "synthesis_transition_removal_07.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_synthesis_transition_removal_08() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "synthesis_transition_removal_08.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_synthesis_transition_removal_09() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "synthesis_transition_removal_09.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  /**
   * A test to see whether a single transition relation simplifier
   * object can perform multiple abstractions in sequence.
   */
  public void testReentrant() throws Exception
  {
    test_synthesis_transition_removal_01();
    test_synthesis_transition_removal_02();
    test_synthesis_transition_removal_03();
    test_synthesis_transition_removal_01();
    test_synthesis_transition_removal_02();
    test_synthesis_transition_removal_03();
  }

  private int mDefaultMarkingID;
}
