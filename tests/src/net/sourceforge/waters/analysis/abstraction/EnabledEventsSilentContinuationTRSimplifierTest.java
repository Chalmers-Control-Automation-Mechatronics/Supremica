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

public class EnabledEventsSilentContinuationTRSimplifierTest
  extends AbstractTRSimplifierTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite testSuite =
      new TestSuite(EnabledEventsSilentContinuationTRSimplifierTest.class);
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
  protected EnabledEventsSilentContinuationTRSimplifier createTransitionRelationSimplifier()
  {
    return new EnabledEventsSilentContinuationTRSimplifier();
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
    final EnabledEventsSilentContinuationTRSimplifier simplifier =
      getTransitionRelationSimplifier();
    simplifier.setNumberOfEnabledEvents(uncontrollableCount);
    return new EventEncoding(events, translator, tau);
  }

  @Override
  protected EnabledEventsSilentContinuationTRSimplifier getTransitionRelationSimplifier()
  {
    return (EnabledEventsSilentContinuationTRSimplifier)
      super.getTransitionRelationSimplifier();
  }


  //#########################################################################
  //# Test Cases
  public void test_silentContinuation_1()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "silentContinuation_1.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_silentContinuation_2()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "silentContinuation_2.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_alwaysEnabledSilentContinuation01()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alwaysEnabledSilentContinuation01.wmod";
    runTransitionRelationSimplifier(group,subdir,name);
  }

  public void test_alwaysEnabledSilentContinuation02()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alwaysEnabledSilentContinuation02.wmod";
    runTransitionRelationSimplifier(group,subdir,name);
  }

  public void test_alwaysEnabledSilentContinuation03()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alwaysEnabledSilentContinuation03.wmod";
    runTransitionRelationSimplifier(group,subdir,name);
  }

  public void test_alwaysEnabledSilentContinuation04()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alwaysEnabledSilentContinuation04.wmod";
    runTransitionRelationSimplifier(group,subdir,name);
  }

  public void test_alwaysEnabledSilentContinuation05()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alwaysEnabledSilentContinuation05.wmod";
    runTransitionRelationSimplifier(group,subdir,name);
  }

  public void test_alwaysEnabledSilentContinuation06()
    throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alwaysEnabledSilentContinuation06.wmod";
    runTransitionRelationSimplifier(group,subdir,name);
  }

  public void test_alwaysEnabledSilentContinuation07()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alwaysEnabledSilentContinuation07.wmod";
    runTransitionRelationSimplifier(group,subdir,name);
  }

  public void test_alwaysEnabledSilentContinuation08()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "alwaysEnabledSilentContinuation08.wmod";
    runTransitionRelationSimplifier(group,subdir,name);
  }

  /**
   * A test to see whether a single abstraction rule object can perform multiple
   * abstractions in sequence.
   */
  public void testReentrant() throws Exception
  {
    test_silentContinuation_1();
    test_silentContinuation_2();
    test_silentContinuation_1();
    test_silentContinuation_2();
  }

}
