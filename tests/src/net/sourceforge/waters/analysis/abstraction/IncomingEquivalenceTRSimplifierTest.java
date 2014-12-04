//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:   IncomingEquivalenceTRSimplifierTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.model.des.ProductDESProxy;


/**
 * A test for the combined implementation of the <I>Silent Continuation Rule</I>
 * and <I>Active Events Rule</I>.
 *
 * @author Robi Malik
 */

public class IncomingEquivalenceTRSimplifierTest
  extends AbstractTRSimplifierTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite testSuite =
      new TestSuite(IncomingEquivalenceTRSimplifierTest.class);
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
    return new IncomingEquivalenceTRSimplifier();
  }

  @Override
  protected void configureTransitionRelationSimplifier()
  {
    configureTransitionRelationSimplifierWithPropositions();
  }


  //#########################################################################
  //# Test Cases - Active Events Rule
  public void test_activeEvents_1()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "activeEvents01.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_activeEvents_2()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "activeEvents02.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_activeEvents_3()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "activeEvents03.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_activeEvents_4()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "activeEvents04.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_activeEvents_5()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "activeEvents05.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_activeEvents_6()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "activeEvents06.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_activeEvents_7()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "activeEvents07.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_activeEvents_8()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "activeEvents08.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_activeEvents_9()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "activeEvents09.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_activeEvents_10()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "activeEvents10.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_activeEvents_11()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "activeEvents11.wmod");
    runTransitionRelationSimplifier(des);
  }


  //#########################################################################
  //# Test Cases - Silent Continuation Rule
  public void test_silentContinuation_1()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "silentContinuation_1.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_silentContinuation_2()
  throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "silentContinuation_2.wmod");
    runTransitionRelationSimplifier(des);
  }


  //#########################################################################
  //# Test Cases - Active Events and Silent Continuation Rule Combined
  public void test_incomingEquivalence01()
    throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "incomingEquivalence01.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_incomingEquivalence02()
    throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "incomingEquivalence02.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_incomingEquivalence03()
    throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "incomingEquivalence03.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_incomingEquivalence04()
    throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "incomingEquivalence04.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_incomingEquivalence05()
    throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "incomingEquivalence05.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_incomingEquivalence06()
    throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "incomingEquivalence06.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test_incomingEquivalence07()
    throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "incomingEquivalence07.wmod");
    runTransitionRelationSimplifier(des);
  }


  //#########################################################################
  //# Test Cases - Other
  /**
   * A test to see whether a single abstraction rule object can perform multiple
   * abstractions in sequence.
   */
  public void testReentrant() throws Exception
  {
    test_activeEvents_1();
    test_activeEvents_2();
    test_activeEvents_3();
    test_activeEvents_4();
    test_activeEvents_5();
    test_activeEvents_5();
    test_activeEvents_4();
    test_activeEvents_8();
    test_activeEvents_8();
    test_activeEvents_1();
  }

}
