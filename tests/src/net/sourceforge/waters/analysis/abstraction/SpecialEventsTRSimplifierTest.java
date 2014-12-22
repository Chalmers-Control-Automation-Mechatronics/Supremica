//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:   SpecialEventsTRSimplifierTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.model.des.ProductDESProxy;


public class SpecialEventsTRSimplifierTest
  extends AbstractTRSimplifierTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite testSuite =
      new TestSuite(SpecialEventsTRSimplifierTest.class);
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
    final SpecialEventsTRSimplifier simplifier =
      new SpecialEventsTRSimplifier();
    return simplifier;
  }

  @Override
  protected void configureTransitionRelationSimplifier()
  {
    configureTransitionRelationSimplifierWithPropositions();
  }


  //#########################################################################
  //# Test Cases - Hiding of Local Events
  public void test_local_01()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "special_events_local_01.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_local_02()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "special_events_local_02.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_local_03()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "special_events_local_03.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_local_04()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "special_events_local_04.wmod");
    runTransitionRelationSimplifier(des);
  }


  //#########################################################################
  //# Test Cases - Failing Events
  public void test_failing_01()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "special_events_failing_01.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_failing_02()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "special_events_failing_02.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_failing_03()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "special_events_failing_03.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_failing_04()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "special_events_failing_04.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_failing_05()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "special_events_failing_05.wmod");
    runTransitionRelationSimplifier(des);
  }


  //#########################################################################
  //# Test Cases - Multiple types
  public void test_mixed_01()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "special_events_01.wmod");
    runTransitionRelationSimplifier(des);
  }

  /**
   * A test to see whether a single abstraction rule object can perform multiple
   * abstractions in sequence.
   */
  public void testReentrant()
  throws Exception
  {
    test_local_01();
    test_local_02();
    test_local_02();
    test_local_03();
    test_mixed_01();
    test_local_02();
    test_local_01();
  }

}
