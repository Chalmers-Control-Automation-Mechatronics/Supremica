//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:   SynthesisAbstractionTRSimplifierTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import java.util.ArrayList;
import java.util.Collection;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.model.analysis.IdenticalKindTranslator;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * A test for the synthesis abstraction algorithm
 * ({@link SynthesisAbstractionTRSimplifier}).
 *
 * @author Robi Malik
 */

public class SynthesisAbstractionTRSimplifierTest
  extends AbstractTRSimplifierTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite testSuite =
        new TestSuite(SynthesisAbstractionTRSimplifierTest.class);
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
  protected SynthesisAbstractionTRSimplifier
    createTransitionRelationSimplifier()
  {
    return new SynthesisAbstractionTRSimplifier();
  }

  @Override
  protected EventEncoding createEventEncoding(final ProductDESProxy des,
                                              final AutomatonProxy aut)
  {
    final Collection<EventProxy> events = aut.getEvents();
    final int numEvents = events.size();
    final Collection<EventProxy> localUncontrollable =
      new ArrayList<EventProxy>(numEvents);
    final Collection<EventProxy> localControllable =
      new ArrayList<EventProxy>(numEvents);
    final Collection<EventProxy> sharedUncontrollable =
      new ArrayList<EventProxy>(numEvents);
    final Collection<EventProxy> sharedControllable =
      new ArrayList<EventProxy>(numEvents);
    for (final EventProxy event : events) {
      switch (event.getKind()) {
      case UNCONTROLLABLE:
        if (event.isObservable()) {
          sharedUncontrollable.add(event);
        } else {
          localUncontrollable.add(event);
        }
        break;
      case CONTROLLABLE:
        if (event.isObservable()) {
          sharedControllable.add(event);
        } else {
          localControllable.add(event);
        }
        break;
      case PROPOSITION:
        sharedControllable.add(event);
        break;
      default:
        break;
      }
    }
    final Collection<EventProxy> all = localUncontrollable;
    mLastLocalUncontrollable = all.size();
    all.addAll(localControllable);
    mLastLocalControllable = all.size();
    all.addAll(sharedUncontrollable);
    mLastSharedUncontrollable = all.size();
    all.addAll(sharedControllable);
    final KindTranslator translator = IdenticalKindTranslator.getInstance();
    return new EventEncoding(all, translator);
  }

  @Override
  protected void configureTransitionRelationSimplifier()
  {
    super.configureTransitionRelationSimplifier();
    final SynthesisAbstractionTRSimplifier simplifier =
      getTransitionRelationSimplifier();
    simplifier.setLastLocalUncontrollableEvent(mLastLocalUncontrollable);
    simplifier.setLastLocalControllableEvent(mLastLocalControllable);
    simplifier.setLastSharedUncontrollableEvent(mLastSharedUncontrollable);
  }

  @Override
  protected SynthesisAbstractionTRSimplifier getTransitionRelationSimplifier()
  {
    return (SynthesisAbstractionTRSimplifier)
      super.getTransitionRelationSimplifier();
  }


  //#########################################################################
  //# Test Cases
  public void test_synthesisAbstraction_1()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "synthesisAbstraction_1.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_synthesisAbstraction_2()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "synthesisAbstraction_2.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_synthesisAbstraction_3()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "synthesisAbstraction_3.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_synthesisAbstraction_4()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "synthesisAbstraction_4.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_synthesisAbstraction_5()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "synthesisAbstraction_5.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  /**
   * A test to see whether a single abstraction rule object can perform multiple
   * abstractions in sequence.
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
    // test_synthesisAbstraction_6();
    test_synthesisAbstraction_5();
    test_synthesisAbstraction_4();
    test_synthesisAbstraction_3();
    test_synthesisAbstraction_2();
    test_synthesisAbstraction_1();
  }


  //#########################################################################
  //# Data Members
  private int mLastLocalUncontrollable;
  private int mLastLocalControllable;
  private int mLastSharedUncontrollable;

}
