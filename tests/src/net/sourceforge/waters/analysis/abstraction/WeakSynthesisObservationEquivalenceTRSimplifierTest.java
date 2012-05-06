//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:   WeakSynthesisObservationEquivalenceTRSimplifierTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import java.util.ArrayList;
import java.util.Collection;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.StateEncoding;
import net.sourceforge.waters.model.analysis.IdenticalKindTranslator;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * A test for the weak synthesis observation equivalence algorithm in
 * ({@link SynthesisObservationEquivalenceTRSimplifier}).
 *
 * @author Sahar Mohajerani, Robi Malik
 */

public class WeakSynthesisObservationEquivalenceTRSimplifierTest
  extends AbstractTRSimplifierTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite testSuite =
      new TestSuite(WeakSynthesisObservationEquivalenceTRSimplifierTest.class);
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
  {
    EventProxy marking = null;
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
  protected StateEncoding createStateEncoding(final AutomatonProxy aut)
  {
    final StateEncoding encoding = new StateEncoding(aut);
    encoding.setNumberOfExtraStates(1);
    return encoding;
  }

  @Override
  protected void configureTransitionRelationSimplifier()
  {
    super.configureTransitionRelationSimplifier();
    final SynthesisObservationEquivalenceTRSimplifier simplifier =
      getTransitionRelationSimplifier();
    simplifier.setUsesWeakSynthesisObservationEquivalence(true);
    simplifier.setLastLocalUncontrollableEvent(mLastLocalUncontrollable);
    simplifier.setLastLocalControllableEvent(mLastLocalControllable);
    simplifier.setLastSharedUncontrollableEvent(mLastSharedUncontrollable);
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
    final String name = "wsoe13.wmod";
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
    final String name = "wsoe18.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_synthesisAbstraction_19() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "wsoe19.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_synthesisAbstraction_20() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "wsoe20.wmod";
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
    final String name = "wsoe22.wmod";
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
    final String name = "wsoe28.wmod";
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
    final String name = "wsoe31.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_synthesisAbstraction_32() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "soe32.wmod";
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
    test_synthesisAbstraction_6();
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
  private int mDefaultMarkingID;

}


