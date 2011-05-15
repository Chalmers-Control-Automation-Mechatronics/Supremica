//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.op
//# CLASS:   SilentContinuationTRSimplifierTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.op;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * A test for the <I>Silent Continuation Rule</I>.
 *
 * @author Robi Malik
 */

public class SilentContinuationTRSimplifierTest
  extends AbstractTRSimplifierTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite testSuite =
      new TestSuite(SilentContinuationTRSimplifierTest.class);
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
    return new SilentContinuationTRSimplifier();
  }


  //#########################################################################
  //# Test Cases
  public void test_silentContinuation_1()
  throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "silentContinuation_1.wmod";
    runTransitionRelationSimplifier(group, subdir, name);
  }

  /**
   * A test to see whether a single abstraction rule object can perform multiple
   * abstractions in sequence.
  public void testReentrant() throws Exception
  {
    test_silentContinuation_1();
  }
   */

}
