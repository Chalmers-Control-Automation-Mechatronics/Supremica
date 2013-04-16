//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:   OPVerifierTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.model.analysis.AbstractAnalysisTest;
import net.sourceforge.waters.model.analysis.des.IdenticalKindTranslator;
import net.sourceforge.waters.model.analysis.des.KindTranslator;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * @author Robi Malik
 */

public class OPVerifierTest
  extends AbstractAnalysisTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite testSuite = new TestSuite(OPVerifierTest.class);
    return testSuite;
  }

  public static void main(final String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }


  //#########################################################################
  //# Overrides for base class junit.framework.TestCase
  @Override
  protected void setUp() throws Exception
  {
    super.setUp();
    final ProductDESProxyFactory factory = getProductDESProxyFactory();
    final KindTranslator translator = IdenticalKindTranslator.getInstance();
    mVerifier = new OPSearchAutomatonSimplifier(factory, translator);
    mVerifier.setOperationMode(OPSearchAutomatonSimplifier.Mode.VERIFY);
  }

  @Override
  protected void tearDown() throws Exception
  {
    mVerifier = null;
    super.tearDown();
  }


  //#########################################################################
  //# Test Cases
  public void testOPempty() throws Exception
  {
    runOPVerifier("alpharemoval_8.wmod", true);
  }

  public void testOP1() throws Exception
  {
    runOPVerifier("op01.wmod", false);
  }

  public void testOP2() throws Exception
  {
    runOPVerifier("op02.wmod", true);
  }

  public void testOP3() throws Exception
  {
    runOPVerifier("op03.wmod", false);
  }

  public void testOP3a() throws Exception
  {
    runOPVerifier("tauTransRemovalFromNonAlpha_3.wmod", false);
  }

  public void testOP4() throws Exception
  {
    runOPVerifier("op04.wmod", true);
  }

  public void testOP5() throws Exception
  {
    runOPVerifier("op05.wmod", false);
  }

  public void testOP6() throws Exception
  {
    runOPVerifier("op06.wmod", true);
  }

  public void testOP7() throws Exception
  {
    runOPVerifier("op07.wmod", false);
  }

  public void testOP8() throws Exception
  {
    runOPVerifier("op08.wmod", true);
  }

  public void testOP9() throws Exception
  {
    runOPVerifier("op09.wmod", true);
  }

  public void testOP10() throws Exception
  {
    runOPVerifier("op10.wmod", true);
  }

  public void testOP11() throws Exception
  {
    runOPVerifier("op11.wmod", false);
  }

  public void testOP12() throws Exception
  {
    runOPVerifier("op12.wmod", true);
  }

  public void testOP13() throws Exception
  {
    runOPVerifier("op13.wmod", false);
  }

  public void testOP14() throws Exception
  {
    runOPVerifier("op14.wmod", false);
  }

  public void testOP15() throws Exception
  {
    runOPVerifier("op15.wmod", false);
  }

  public void testOP15a() throws Exception
  {
    runOPVerifier("op15a.wmod", false);
  }

  public void testOP16() throws Exception
  {
    runOPVerifier("op16.wmod", false);
  }

  public void testOP17() throws Exception
  {
    runOPVerifier("op17.wmod", false);
  }

  public void testOP18() throws Exception
  {
    runOPVerifier("op18.wmod", false);
  }

  public void testOP19() throws Exception
  {
    runOPVerifier("op19.wmod", false);
  }

  public void testOP20() throws Exception
  {
    runOPVerifier("op20.wmod", true);
  }

  public void testOP21() throws Exception
  {
    runOPVerifier("op21.wmod", true);
  }

  public void testOP22() throws Exception
  {
    runOPVerifier("op22.wmod", true);
  }


  //#########################################################################
  //# Auxiliary Methods
  private void runOPVerifier(final String name, final boolean expect)
    throws Exception
  {
    final ProductDESProxy des = getCompiledDES("tests", "abstraction", name);
    final String desname = des.getName();
    getLogger().info("Checking " + desname + " ...");
    final AutomatonProxy before = findAutomaton(des, BEFORE);
    final List<EventProxy> hidden = getUnobservableEvents(des);
    mVerifier.setModel(before);
    mVerifier.setHiddenEvents(hidden);
    final boolean result = mVerifier.run();
    assertEquals("Unexpected result from OP-Verifier!", expect, result);
    getLogger().info("Done " + desname);
  }


  //#########################################################################
  //# Overrides for Abstract Base Class
  //# net.sourceforge.waters.model.analysis.AbstractAnalysisTest
  @Override
  protected void configure(final ModuleCompiler compiler)
  {
    compiler.setOptimizationEnabled(false);
  }


  //#########################################################################
  //# Data Members
  private OPSearchAutomatonSimplifier mVerifier;


  //#########################################################################
  //# Class Constants
  private final String BEFORE = "before";

}
