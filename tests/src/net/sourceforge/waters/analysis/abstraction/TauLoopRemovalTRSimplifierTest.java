//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:   TauLoopRemovalTRSimplifierTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import junit.framework.Test;
import junit.framework.TestSuite;


public class TauLoopRemovalTRSimplifierTest
  extends AbstractTRSimplifierTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite testSuite =
        new TestSuite(TauLoopRemovalTRSimplifierTest.class);
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
  protected TauLoopRemovalTRSimplifier createTransitionRelationSimplifier()
  {
    return new TauLoopRemovalTRSimplifier();
  }

  @Override
  protected TauLoopRemovalTRSimplifier getTransitionRelationSimplifier()
  {
    return (TauLoopRemovalTRSimplifier) super.getTransitionRelationSimplifier();
  }


  //#########################################################################
  //# Test Cases
  public void test_oeq1() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "oeq_1.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_oeq5() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "oeq_5.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_oeq7() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "oeq_7.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_tauLoop1() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "tauloop01.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_tauLoop2() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "tauloop02.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_tauLoop3() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "tauloop03.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_tauLoop4() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "tauloop04.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_tauLoop5() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "tauloop05.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  /**
   * A test to see whether a single abstraction rule object can perform
   * multiple abstractions in sequence.
   */
  public void testReentrant() throws Exception
  {
    test_oeq1();
    test_oeq5();
    test_tauLoop1();
    test_tauLoop2();
    test_oeq1();
    test_oeq5();
    test_tauLoop1();
    test_tauLoop2();
  }

}

