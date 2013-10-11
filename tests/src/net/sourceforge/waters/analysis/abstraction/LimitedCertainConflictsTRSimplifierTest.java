//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:   LimitedCertainConflictsTRSimplifierTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;


/**
 * A test for the <I>Limited Certain Conflicts Rule</I>.
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
  //# net.sourceforge.waters.analysis.abstraction.AbstractTRSimplifierTest
  @Override
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

  public void test_limitedCertainConflicts_14()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "limitedCertainConflicts_14.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  public void test_limitedCertainConflicts_15()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "limitedCertainConflicts_15.wmod";
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

  public void test_certainconflicts_18()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "certainconflicts_18.wmod";
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