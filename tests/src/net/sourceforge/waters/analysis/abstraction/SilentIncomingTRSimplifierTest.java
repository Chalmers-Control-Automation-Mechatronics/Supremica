//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:   SilentContinuationTRSimplifierTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * A test for the standard <I>Silent Incoming Rule</I>.
 * This test the version of the rule for standard nonblocking,
 * possibly with always enabled events, but without precondition markings.
 *
 * @author Robi Malik
 */

public class SilentIncomingTRSimplifierTest
  extends AbstractTRSimplifierTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite testSuite =
      new TestSuite(SilentIncomingTRSimplifierTest.class);
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
  protected SilentIncomingTRSimplifier createTransitionRelationSimplifier()
  {
    return new SilentIncomingTRSimplifier();
  }

  @Override
  protected SilentIncomingTRSimplifier getTransitionRelationSimplifier()
  {
    return (SilentIncomingTRSimplifier) super.getTransitionRelationSimplifier();
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
