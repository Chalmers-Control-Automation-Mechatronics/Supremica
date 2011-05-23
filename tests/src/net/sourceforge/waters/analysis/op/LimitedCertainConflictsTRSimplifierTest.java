//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.op
//# CLASS:   LimitedCertainConflictsTRSimplifierTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.op;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * A test for the <I>certain Conflicts Rule</I>.
 *
 * @author Robi Malik
 */

public class LimitedCertainConflictsTRSimplifierTest
  extends AbstractTRSimplifierTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite testSuite =
      new TestSuite(LimitedCertainConflictsTRSimplifierTest.class);
    return testSuite;
  }

  public static void main(final String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }


  //#########################################################################
  //# Overrides for abstract base class
  //# net.sourceforge.waters.analysis.op.AbstractTRSimplifierTest
  protected TransitionRelationSimplifier createTransitionRelationSimplifier()
  {
    return new LimitedCertainConflictsTRSimplifier();
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
  //# Test Cases
  public void test_nonblocking()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "determinisation_10.wmod";
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