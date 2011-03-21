//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.analysis.gnonblocking
//# CLASS:   RemovalOfAlphaMarkingsRuleTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.gnonblocking;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


public class AltRemovalOfTauTransitionsOriginatingFromNonAlphaStatesRuleTest
    extends AbstractAbstractionRuleTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite testSuite =
        new TestSuite(
            AltRemovalOfTauTransitionsOriginatingFromNonAlphaStatesRuleTest.class);
    return testSuite;
  }

  public static void main(final String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }


  //#########################################################################
  //# Overrides for abstract base class
  //# net.sourceforge.waters.analysis.gnonblocking.AbstractAbstractionRuleTest
  protected AltRemovalOfTauTransitionsOriginatingFromNonAlphaStatesRule
    createAbstractionRule(final ProductDESProxyFactory factory)
  {
    return
      new AltRemovalOfTauTransitionsOriginatingFromNonAlphaStatesRule(factory);
  }

  protected void configureAbstractionRule(final ProductDESProxy des)
  {
    super.configureAbstractionRule(des);
    final AltRemovalOfTauTransitionsOriginatingFromNonAlphaStatesRule rule =
        getAbstractionRule();
    final EventProxy alphaMarking = findEvent(des, ALPHA);
    rule.setAlphaMarking(alphaMarking);
    final EventProxy defaultMarking = findEvent(des, OMEGA);
    rule.setDefaultMarking(defaultMarking);
  }

  protected AltRemovalOfTauTransitionsOriginatingFromNonAlphaStatesRule
    getAbstractionRule()
  {
    return (AltRemovalOfTauTransitionsOriginatingFromNonAlphaStatesRule)
      super.getAbstractionRule();
  }


  //#########################################################################
  //# Test Cases
  public void test_tauTransRemovalFromNonAlpha_StateUnreachable()
      throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "tauTransRemovalFromNonAlpha_1.wmod";
    runAbstractionRule(group, subdir, name);
  }

  public void test_tauTransRemovalFromNonAlpha_NoFurther() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "tauTransRemovalFromNonAlpha_2.wmod";
    runAbstractionRule(group, subdir, name);
  }

  public void test_tauTransRemovalFromNonAlpha_tauAndNonTauOutgoingTransitions()
      throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "tauTransRemovalFromNonAlpha_3.wmod";
    runAbstractionRule(group, subdir, name);
  }

  public void test_allStatesImplicitlyMarkedAlpha() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "tauTransRemovalFromNonAlpha_8.wmod";
    runAbstractionRule(group, subdir, name);
  }

  public void test_allStatesImplicitlyMarkedOmega() throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "tauTransRemovalFromNonAlpha_9.wmod";
    runAbstractionRule(group, subdir, name);
  }

  public void test_tauTransRemovalFromNonAlpha_TwoConsecutiveTauUnreachable()
      throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "tauTransRemovalFromNonAlpha_7.wmod";
    runAbstractionRule(group, subdir, name);
  }

  public void test_tauTransRemovalFromNonAlpha_TwoTauOutgoing()
      throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "tauTransRemovalFromNonAlpha_4.wmod";
    runAbstractionRule(group, subdir, name);
  }

  public void test_tauTransRemovalFromNonAlpha_multipleIncomingTransitions()
      throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "tauTransRemovalFromNonAlpha_5.wmod";
    runAbstractionRule(group, subdir, name);
  }

  public void test_tauTransRemovalFromNonAlpha_tauLoopNoMarking()
      throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "tauTransRemovalFromNonAlpha_10.wmod";
    runAbstractionRule(group, subdir, name);
  }

  public void test_tauTransRemovalFromNonAlpha_removalOfInitialState()
      throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "tauTransRemovalFromNonAlpha_11.wmod";
    runAbstractionRule(group, subdir, name);
  }

  public void test_tauTransRemovalFromNonAlpha_nonRemovalOfStateWithNoOutgoingTransitions()
      throws Exception
  {
    final String group = "tests";
    final String subdir = "abstraction";
    final String name = "tauTransRemovalFromNonAlpha_12.wmod";
    runAbstractionRule(group, subdir, name);
  }

  /**
   * A test to see whether a single abstraction rule object can perform multiple
   * abstractions in sequence.
   */
  public void testReentrant() throws Exception
  {
    test_tauTransRemovalFromNonAlpha_StateUnreachable();
    test_tauTransRemovalFromNonAlpha_NoFurther();
    test_tauTransRemovalFromNonAlpha_TwoTauOutgoing();
    test_tauTransRemovalFromNonAlpha_tauAndNonTauOutgoingTransitions();
    test_allStatesImplicitlyMarkedOmega();
    test_tauTransRemovalFromNonAlpha_StateUnreachable();
    test_tauTransRemovalFromNonAlpha_TwoConsecutiveTauUnreachable();
    test_tauTransRemovalFromNonAlpha_multipleIncomingTransitions();
    test_tauTransRemovalFromNonAlpha_nonRemovalOfStateWithNoOutgoingTransitions();
    test_allStatesImplicitlyMarkedAlpha();
    test_tauTransRemovalFromNonAlpha_NoFurther();
  }

}
