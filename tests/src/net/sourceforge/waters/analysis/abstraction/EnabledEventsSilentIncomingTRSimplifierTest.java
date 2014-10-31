//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:   SilentContinuationTRSimplifierTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import java.util.ArrayList;
import java.util.Collection;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.model.analysis.IdenticalKindTranslator;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * A test for the <I>Silent Continuation Rule</I>.
 *
 * @author Robi Malik
 */

public class EnabledEventsSilentIncomingTRSimplifierTest
  extends AbstractTRSimplifierTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite testSuite =
      new TestSuite(EnabledEventsSilentIncomingTRSimplifierTest.class);
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
  protected EnabledEventsSilentIncomingTRSimplifier createTransitionRelationSimplifier()
  {
    return new EnabledEventsSilentIncomingTRSimplifier();
  }

  @Override
  protected EventEncoding createEventEncoding(final ProductDESProxy des,
                                              final AutomatonProxy aut)
    throws OverflowException
  {
    final KindTranslator translator = IdenticalKindTranslator.getInstance();
    final EventProxy tau = getEvent(aut, TAU);
    final int numEvents = aut.getEvents().size();
    final Collection<EventProxy> events = new ArrayList<>(numEvents);
    int uncontrollableCount = 0;
    for (final EventProxy event : aut.getEvents()) {
      //see if controllable
      if (event.getKind() == EventKind.UNCONTROLLABLE) {
        //put in the order you want to encode
        //don't need to worry about tau because of the constructor
        events.add(event);
        if (event != tau) {
          uncontrollableCount++;
        }
      }
    }
    for (final EventProxy event : aut.getEvents()) {
      //see if controllable
      if (event.getKind() != EventKind.UNCONTROLLABLE) {
        //put in the order you want to encode
        events.add(event);
      }
    }
    //returns a list where all uncontrollable events are first
    final EnabledEventsSilentIncomingTRSimplifier simplifier =
      getTransitionRelationSimplifier();
    simplifier.setNumberOfEnabledEvents(uncontrollableCount);
    return new EventEncoding(events, translator, tau);

  }

  @Override
  protected EnabledEventsSilentIncomingTRSimplifier getTransitionRelationSimplifier()
  {
    return (EnabledEventsSilentIncomingTRSimplifier)
      super.getTransitionRelationSimplifier();
  }


  //#########################################################################
  //# Test Cases
  public void test_silentIncoming01()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "silentIncoming01.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_silentIncoming02()
    throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "silentIncoming02.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_silentIncoming03()
    throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "silentIncoming03.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_silentIncoming04()
    throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "silentIncoming04.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_silentIncoming05()
    throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "silentIncoming05.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_alwaysEnabledSilentIncoming01()
    throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alwaysEnabledSilentIncoming01.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_alwaysEnabledSilentIncoming02()
    throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alwaysEnabledSilentIncoming02.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_alwaysEnabledSilentIncoming03()
    throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alwaysEnabledSilentIncoming03.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_alwaysEnabledSilentIncoming04()
    throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alwaysEnabledSilentIncoming04.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_alwaysEnabledSilentIncoming05()
    throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alwaysEnabledSilentIncoming05.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  /**
   * A test to see whether a single abstraction rule object can perform multiple
   * abstractions in sequence.
   */
  public void testReentrant() throws Exception
  {
    test_silentIncoming01();
    test_silentIncoming02();
    test_silentIncoming03();
    test_silentIncoming04();
    test_silentIncoming05();
    test_alwaysEnabledSilentIncoming04();
  }

}
