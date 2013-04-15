//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:   LimitedCertainConflictsTRSimplifierTest
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
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * A test for the <I>certain Conflicts Rule</I>.
 *
 * @author Robi Malik
 */

public class EnabledEventsLimitedCertainConflictsTRSimplifierTest
  extends AbstractTRSimplifierTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite testSuite =
      new TestSuite(EnabledEventsLimitedCertainConflictsTRSimplifierTest.class);
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
  protected EnabledEventsLimitedCertainConflictsTRSimplifier createTransitionRelationSimplifier()
  {
    return new EnabledEventsLimitedCertainConflictsTRSimplifier();
  }

  @Override
  protected EventEncoding createEventEncoding(final ProductDESProxy des,
                                              final AutomatonProxy aut)
  {
    final KindTranslator translator = IdenticalKindTranslator.getInstance();
    final EventProxy tau = getEvent(aut, TAU);
    final Collection<EventProxy> events = new ArrayList<EventProxy>();
    final EnabledEventsLimitedCertainConflictsTRSimplifier simplifier = getTransitionRelationSimplifier();
    int uncontrollableCount = 0;
    //go through automaton
    for(final EventProxy event : aut.getEvents())
    {
    //find events


    //see if controllable
    if(event.getKind() == EventKind.UNCONTROLLABLE)
    {
    //put in the order you want to encode
    //don't need to worry about tau because of the construtor I'm using
      events.add(event);
    //Collection<EventProxy> create list of events in right order

      if(event != tau)
      uncontrollableCount++;
    }


    }
    for(final EventProxy event : aut.getEvents())
      {
      switch(event.getKind()){
    //find events
      case CONTROLLABLE :
        events.add(event);
        break;
      case PROPOSITION :
        if(event.getName().equals(OMEGA))
        {
          events.add(event);
          simplifier.setDefaultMarkingID(0);
         }
        /*
        if(event.getName().equals(ALPHA))
        {
          //don't just use the number 0

        }
        */
        break;
      default :
        break;
    }
      }
    //returns a list where all uncontrollable events are first

    simplifier.setNumberOfEnabledEvents(uncontrollableCount);

    return new EventEncoding(events, translator, tau);

  }

  @Override
  protected EnabledEventsLimitedCertainConflictsTRSimplifier getTransitionRelationSimplifier()
  {

    return (EnabledEventsLimitedCertainConflictsTRSimplifier) super.getTransitionRelationSimplifier();

  }



  //#########################################################################
  //# Test Cases
  @Override
  public void test_basic_7() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "limitedCertainConflicts_basic_7.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_nonblocking()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "nonalphadet_10.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_limitedCertainConflicts_1()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "limitedCertainConflicts_1.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_limitedCertainConflicts_2()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "limitedCertainConflicts_2.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_limitedCertainConflicts_3()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "limitedCertainConflicts_3.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_limitedCertainConflicts_4()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "limitedCertainConflicts_4.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_limitedCertainConflicts_5()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "limitedCertainConflicts_5.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_limitedCertainConflicts_6()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "limitedCertainConflicts_6.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_limitedCertainConflicts_7()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "limitedCertainConflicts_7.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_limitedCertainConflicts_8()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "limitedCertainConflicts_8.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_limitedCertainConflicts_9()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "limitedCertainConflicts_9.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_limitedCertainConflicts_10()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "limitedCertainConflicts_10.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_limitedCertainConflicts_11()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "limitedCertainConflicts_11.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_limitedCertainConflicts_12()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "limitedCertainConflicts_12.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_limitedCertainConflicts_13()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "limitedCertainConflicts_13.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }


  public void test_alwaysEnabledLimitedCertainConflicts01()
    throws Exception
    {
      final String group = "tests";
      final String subdir = "abstraction";
      final String name = "alwaysEnabledLimitedCertainConflicts01.wmod";
      runTransitionRelationSimplifier(group, subdir, name);
    }
  public void test_alwaysEnabledLimitedCertainConflicts02()
    throws Exception
    {
      final String group = "tests";
      final String subdir = "abstraction";
      final String name = "alwaysEnabledLimitedCertainConflicts02.wmod";
      runTransitionRelationSimplifier(group, subdir, name);
    }
  public void test_alwaysEnabledLimitedCertainConflicts03()
    throws Exception
    {
      final String group = "tests";
      final String subdir = "abstraction";
      final String name = "alwaysEnabledLimitedCertainConflicts03.wmod";
      runTransitionRelationSimplifier(group, subdir, name);
    }
  public void test_alwaysEnabledLimitedCertainConflicts04()
    throws Exception
    {
      final String group = "tests";
      final String subdir = "abstraction";
      final String name = "alwaysEnabledLimitedCertainConflicts04.wmod";
      runTransitionRelationSimplifier(group, subdir, name);
    }
  public void test_alwaysEnabledLimitedCertainConflicts05()
    throws Exception
    {
      final String group = "tests";
      final String subdir = "abstraction";
      final String name = "alwaysEnabledLimitedCertainConflicts05.wmod";
      runTransitionRelationSimplifier(group, subdir, name);
    }
  public void test_alwaysEnabledLimitedCertainConflicts06()
    throws Exception
    {
      final String group = "tests";
      final String subdir = "abstraction";
      final String name = "alwaysEnabledLimitedCertainConflicts06.wmod";
      runTransitionRelationSimplifier(group, subdir, name);
    }
  public void test_alwaysEnabledLimitedCertainConflicts07()
    throws Exception
    {
      final String group = "tests";
      final String subdir = "abstraction";
      final String name = "alwaysEnabledLimitedCertainConflicts07.wmod";
      runTransitionRelationSimplifier(group, subdir, name);
    }

  public void test_certainConflicts_15()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "certainconflicts_15.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_certainconflicts_17()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "certainconflicts_17.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  /**
   * A test to see whether a single abstraction rule object can perform multiple
   * abstractions in sequence.
   */
  public void testReentrant() throws Exception
  {
    test_nonblocking();
    test_limitedCertainConflicts_1();
    test_limitedCertainConflicts_2();
    test_limitedCertainConflicts_3();
    test_limitedCertainConflicts_4();
    test_limitedCertainConflicts_3();
    test_limitedCertainConflicts_2();
    test_limitedCertainConflicts_1();
    test_nonblocking();
  }

}