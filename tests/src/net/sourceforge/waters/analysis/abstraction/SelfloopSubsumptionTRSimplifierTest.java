//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:   SelfloopSubsumptionTRSimplifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.model.des.ProductDESProxy;


/**
 * A test for the selfloop subsumption simplifier
 * ({@link SelfloopSubsumptionTRSimplifier}).
 *
 * @author Robi Malik
 */

public class SelfloopSubsumptionTRSimplifierTest
  extends AbstractTRSimplifierTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite() {
    final TestSuite testSuite =
      new TestSuite(SelfloopSubsumptionTRSimplifierTest.class);
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
  protected SelfloopSubsumptionTRSimplifier createTransitionRelationSimplifier()
  {
    return new SelfloopSubsumptionTRSimplifier();
  }


  //#########################################################################
  //# Test Cases
  public void testSelfloopSubsumption1() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "selfloop_subsumption_01.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void testSelfloopSubsumption2() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "selfloop_subsumption_02.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void testSelfloopSubsumption3() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "selfloop_subsumption_03.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void testSelfloopSubsumption4() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "selfloop_subsumption_04.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void testSelfloopSubsumption5() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "selfloop_subsumption_05.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void testSelfloopSubsumption6() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "abstraction", "selfloop_subsumption_06.wmod");
    runTransitionRelationSimplifier(des);
  }

  /**
   * A test to see whether a single transition relation simplifier
   * object can perform multiple abstractions in sequence.
   */
  public void testReentrant() throws Exception
  {
    testSelfloopSubsumption1();
    testSelfloopSubsumption2();
    testSelfloopSubsumption3();
    testSelfloopSubsumption1();
    testSelfloopSubsumption2();
    testSelfloopSubsumption3();
  }

}
