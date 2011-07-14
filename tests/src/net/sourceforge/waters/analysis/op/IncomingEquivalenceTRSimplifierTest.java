//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.op
//# CLASS:   IncomingEquivalenceTRSimplifierTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.op;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * A test for the combined implementation of the <I>Silent Continuation Rule</I> and
 * <I>Active Events Rule</I>.
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
  //# net.sourceforge.waters.analysis.op.AbstractTRSimplifierTest
  @Override
  protected TransitionRelationSimplifier createTransitionRelationSimplifier()
  {
    return new IncomingEquivalenceTRSimplifier();
  }

  @Override
  protected EventEncoding createEventEncoding(final ProductDESProxy des,
                                              final AutomatonProxy aut)
  {
    return createEventEncodingWithPropositions(des, aut);
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
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "activeEvents_1.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_activeEvents_2()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "activeEvents_2.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_activeEvents_3()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "activeEvents_3.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_activeEvents_4()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "activeEvents_4.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_activeEvents_5()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "activeEvents_5.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_activeEvents_6()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "activeEvents_6.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_activeEvents_7()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "activeEvents_7.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_activeEvents_8()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "activeEvents_8.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_activeEvents_9()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "activeEvents_9.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_activeEvents_10()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "activeEvents_10.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }


  //#########################################################################
  //# Test Cases - Silent Continuation Rule
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


  //#########################################################################
  //# Test Cases - Active Events and Silent Continuation Rule Combined
  public void test_incomingEquivalence_1()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "incomingEquivalence_1.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_incomingEquivalence_2()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "incomingEquivalence_2.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_incomingEquivalence_3()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "incomingEquivalence_3.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
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
    test_activeEvents_3();
    test_activeEvents_2();
    test_activeEvents_1();
  }

}
