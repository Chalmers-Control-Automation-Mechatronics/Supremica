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
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.model.analysis.IdenticalKindTranslator;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;


/**
 * A test for the observation equivalence simplifier
 * ({@link ObservationEquivalenceTRSimplifier}) with selfloop-only events
 * enabled. This test considers all controllable events in the input automaton
 * as selfloop-only.
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

public class SelfLoopsObservationEquivalenceTRSimplifierTest
  extends AbstractTRSimplifierTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite() {
    final TestSuite testSuite =
      new TestSuite(SelfLoopsObservationEquivalenceTRSimplifierTest.class);
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
    return new ObservationEquivalenceTRSimplifier();
  }

  @Override
  protected EventEncoding createEventEncoding(final ProductDESProxy des,
                                              final AutomatonProxy aut)
    throws OverflowException
  {
    final KindTranslator translator = IdenticalKindTranslator.getInstance();
    final EventProxy tau = getEvent(aut, TAU);
    final EventEncoding encoding = new EventEncoding(aut, translator, tau);
    // Mark uncontrollable events (except tau) as selfloop-only
    final int numEvents = encoding.getNumberOfProperEvents();
    for (int event = EventEncoding.NONTAU; event < numEvents; event++) {
      final byte status = encoding.getProperEventStatus(event);
      if (!EventStatus.isControllableEvent(status)) {
        encoding.setProperEventStatus
          (event, status | EventStatus.STATUS_SELFLOOP_ONLY);
      }
    }
    return encoding;

  }
  @Override
  protected ObservationEquivalenceTRSimplifier getTransitionRelationSimplifier()
  {
    return (ObservationEquivalenceTRSimplifier) super.getTransitionRelationSimplifier();
  }


  //#########################################################################
  //# Test Cases
  public void test_selfLoopObservationalEquivalence01() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "selfLoopObservationalEquivalence01.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_selfLoopObservationalEquivalence02() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "selfLoopObservationalEquivalence02.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_selfLoopObservationalEquivalence03() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "selfLoopObservationalEquivalence03.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_selfLoopObservationalEquivalence04() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "selfLoopObservationalEquivalence04.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_selfLoopObservationalEquivalence05() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "selfLoopObservationalEquivalence05.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_selfLoopObservationalEquivalence06() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "selfLoopObservationalEquivalence06.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }
  public void test_selfLoopObservationalEquivalence07() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "selfLoopObservationalEquivalence07.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_oeq_1() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "oeq_1.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_oeq_2() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "oeq_2.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_oeq_3() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "oeq_3.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_oeq_4() throws Exception
  {
    final ObservationEquivalenceTRSimplifier simplifier =
      getTransitionRelationSimplifier();
    simplifier.setTransitionRemovalMode
      (ObservationEquivalenceTRSimplifier.TransitionRemoval.ALL);
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "oeq_4.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_oeq_5() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "oeq_5.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_oeq_6() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "oeq_6.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_oeq_7() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "oeq_7.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_oeq_8() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "oeq_8.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_oeq_9() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "oeq_9.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_oeq_10() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "oeq_10.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_oeq_11() throws Exception
  {
    final ObservationEquivalenceTRSimplifier simplifier =
      getTransitionRelationSimplifier();
    simplifier.setTransitionRemovalMode
      (ObservationEquivalenceTRSimplifier.TransitionRemoval.ALL);
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "oeq_11.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_oeq_12() throws Exception
  {
    final ObservationEquivalenceTRSimplifier simplifier =
      getTransitionRelationSimplifier();
    simplifier.setTransitionRemovalMode
      (ObservationEquivalenceTRSimplifier.TransitionRemoval.ALL);
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "oeq_12.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_oeq_13() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "oeq_13.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_oeq_14() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "oeq_14.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_oeq_15() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "oeq_15.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_oeq_16() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "oeq_16.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_oeq_17() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "oeq_17.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_oeq_18() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "oeq_18.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  /**
   * A test to see whether a single transition relation simplifier
   * object can perform multiple abstractions in sequence.
   */
  public void testReentrant() throws Exception
  {
    test_oeq_1();
    test_oeq_2();
    test_oeq_3();
    test_oeq_4();
    test_oeq_1();
    test_oeq_2();
  }

}
