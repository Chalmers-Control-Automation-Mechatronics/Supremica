//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:   SubsetConstructionTRSimplifierTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;


/**
 * A test for the subset construction algorithm
 * ({@link SubsetConstructionTRSimplifier}).
 *
 * @author Robi Malik
 */

public class SubsetConstructionTRSimplifierTest
  extends AbstractTRSimplifierTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite testSuite =
        new TestSuite(SubsetConstructionTRSimplifierTest.class);
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
    final SubsetConstructionTRSimplifier simplifier =
      new SubsetConstructionTRSimplifier();
    simplifier.setFailingEventsAsSelfloops(true);
    return simplifier;
  }

  @Override
  protected EventEncoding createEventEncoding(final ProductDESProxy des,
                                              final AutomatonProxy aut)
    throws OverflowException
  {
    final EventEncoding enc = super.createEventEncoding(des, aut);
    final int numEvents = enc.getNumberOfProperEvents();
    for (int e = EventEncoding.NONTAU; e < numEvents; e++) {
      final EventProxy event = enc.getProperEvent(e);
      final String name = event.getName();
      if (name.startsWith(EventDeclProxy.DEFAULT_FORBIDDEN_NAME)) {
        byte status = enc.getProperEventStatus(e);
        status |= EventStatus.STATUS_FAILING;
        status |= EventStatus.STATUS_ALWAYS_ENABLED;
        enc.setProperEventStatus(e, status);
      }
    }
    return enc;
  }

  @Override
  protected SubsetConstructionTRSimplifier getTransitionRelationSimplifier()
  {
    return (SubsetConstructionTRSimplifier)
      super.getTransitionRelationSimplifier();
  }


  //#########################################################################
  //# Test Cases
  public void test_determinisation_1()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "determinisation_1.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_determinisation_2()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "determinisation_2.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_determinisation_3()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "determinisation_3.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_determinisation_4()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "determinisation_4.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_determinisation_5()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "determinisation_5.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_determinisation_6()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "determinisation_6.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_determinisation_7()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "determinisation_7.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  /**
   * A test to see whether a single abstraction rule object can perform multiple
   * abstractions in sequence.
   */
  public void testReentrant() throws Exception
  {
    test_determinisation_4();
    test_determinisation_3();
    test_determinisation_2();
    test_determinisation_1();
    test_determinisation_2();
    test_determinisation_3();
    test_determinisation_4();
    test_determinisation_5();
    test_determinisation_6();
    test_determinisation_5();
    test_determinisation_4();
    test_determinisation_3();
    test_determinisation_2();
    test_determinisation_1();
  }

}

