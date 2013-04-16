//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:   HalfWaySynthesisTRSimplifierTest
//###########################################################################
//# $Id: 28b5d3e8a78c120597cf75fe48443bd1313c7cb6 $
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import gnu.trove.TIntHashSet;

import java.util.ArrayList;
import java.util.Collection;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.model.analysis.des.IdenticalKindTranslator;
import net.sourceforge.waters.model.analysis.des.KindTranslator;
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
    EventProxy marking = null;
    mLastSharedControllable = 0;
    for (final EventProxy event : events) {
      switch (event.getKind()) {
      case UNCONTROLLABLE:
        if (event.isObservable()) {
          sharedUncontrollable.add(event);
        } else {
          localUncontrollable.add(event);
        }
        mLastSharedControllable++;
        break;
      case CONTROLLABLE:
        if (event.isObservable()) {
          sharedControllable.add(event);
        } else {
          localControllable.add(event);
        }
        mLastSharedControllable++;
        break;
      case PROPOSITION:
        sharedControllable.add(event);
        if (event.getName().equals(EventDeclProxy.DEFAULT_MARKING_NAME)) {
          marking = event;
        }
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
    final EventEncoding encoding = new EventEncoding(all, translator);
    mDefaultMarkingID = encoding.getEventCode(marking);
    return encoding;
  }

  @Override
  protected void configureTransitionRelationSimplifier()
  {
    super.configureTransitionRelationSimplifier();
    final HalfWaySynthesisTRSimplifier simplifier =
      getTransitionRelationSimplifier();
    simplifier.setDefaultMarkingID(mDefaultMarkingID);
    simplifier.setLastLocalUncontrollableEvent(mLastLocalUncontrollable);
    simplifier.setLastLocalControllableEvent(mLastLocalControllable);
    simplifier.setLastSharedUncontrollableEvent(mLastSharedUncontrollable);
    if (mSettingRenamed) {
      final int numRenamed = mLastSharedControllable - mLastSharedUncontrollable;
      final TIntHashSet renamed = new TIntHashSet(numRenamed);
      for (int e = mLastSharedUncontrollable + 1;
           e <= mLastSharedControllable; e++) {
        renamed.add(e);
      }
      simplifier.setRenamedEvents(renamed);
    }
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
  private int mLastLocalUncontrollable;
  private int mLastLocalControllable;
  private int mLastSharedUncontrollable;
  private int mLastSharedControllable;
  private int mDefaultMarkingID;
  private final boolean mSettingRenamed = true;
}
