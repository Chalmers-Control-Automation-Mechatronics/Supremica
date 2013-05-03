//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:   OPVerifierTRChainTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import java.util.Collection;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.StateEncoding;
import net.sourceforge.waters.model.analysis.AbstractAnalysisTest;
import net.sourceforge.waters.model.analysis.IdenticalKindTranslator;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;


/**
 * @author Robi Malik
 */

public class OPVerifierTRChainTest
  extends AbstractAnalysisTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite testSuite = new TestSuite(OPVerifierTRChainTest.class);
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
    mVerifier = new OPVerifierTRChain();
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
    final AutomatonProxy aut = findAutomaton(des, BEFORE);
    final KindTranslator translator = IdenticalKindTranslator.getInstance();
    final Collection<EventProxy> hidden = getUnobservableEvents(des);
    final EventEncoding eventEnc = new EventEncoding();
    for (final EventProxy event : aut.getEvents()) {
      if (hidden.contains(event)) {
        eventEnc.addSilentEvent(event);
      } else {
        eventEnc.addEvent(event, translator, (byte)0);
      }
    }
    final StateEncoding stateEnc = new StateEncoding(aut);
    final int config = mVerifier.getPreferredInputConfiguration();
    final ListBufferTransitionRelation rel =
      new ListBufferTransitionRelation(aut, eventEnc, stateEnc, config);
    rel.checkReachability();
    mVerifier.setTransitionRelation(rel);
    final EventProxy omega = getEvent(des, OMEGA);
    final int omegaID = eventEnc.getEventCode(omega);
    mVerifier.setDefaultMarkingID(omegaID);
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
  private OPVerifierTRChain mVerifier;


  //#########################################################################
  //# Class Constants
  private final String BEFORE = "before";
  private final String OMEGA = EventDeclProxy.DEFAULT_MARKING_NAME;

}
