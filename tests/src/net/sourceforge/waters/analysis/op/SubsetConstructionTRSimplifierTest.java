//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.op
//# CLASS:   SubsetConstructionTRSimplifierTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.op;

import junit.framework.Test;
import junit.framework.TestSuite;


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
  //# net.sourceforge.waters.analysis.op.AbstractTRSimplifierTest
  @Override
  protected TransitionRelationSimplifier createTransitionRelationSimplifier()
  {
    return new SubsetConstructionTRSimplifier();
  }


  //#########################################################################
  //# Test Cases
  /**
   * A test to see whether a single abstraction rule object can perform multiple
   * abstractions in sequence.
  public void testReentrant() throws Exception
  {
    test_determinisation_12();
    test_determinisation_1();
    test_determinisation_2();
    test_determinisation_3();
    test_determinisation_11();
    test_determinisation_13();
    test_determinisation_4();
    test_determinisation_20();
    test_determinisation_8();
    test_determinisation_14();
    test_determinisation_1();
    test_determinisation_10();
    test_determinisation_2();
    test_determinisation_9();
  }
   */

}
