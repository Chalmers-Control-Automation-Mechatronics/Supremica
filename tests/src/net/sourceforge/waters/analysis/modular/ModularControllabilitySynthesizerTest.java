//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.modular
//# CLASS:   ModularControllabilitySynthesizerTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.modular;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.model.analysis.AbstractSupervisorSynthesizerTest;
import net.sourceforge.waters.model.analysis.des.SupervisorSynthesizer;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


public class ModularControllabilitySynthesizerTest
  extends AbstractSupervisorSynthesizerTest
{

  //#########################################################################
  //# To be Provided by Subclasses
  @Override
  protected SupervisorSynthesizer createSynthesizer
    (final ProductDESProxyFactory factory)
  {
    final ModularControllabilitySynthesizer synthesizer =
      new ModularControllabilitySynthesizer(factory);
    synthesizer.setSupervisorReductionEnabled(false);
    synthesizer.setSupervisorLocalizationEnabled(false);
    return synthesizer;
  }


  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite testSuite =
      new TestSuite(ModularControllabilitySynthesizerTest.class);
    return testSuite;
  }

  public static void main(final String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }


  //#########################################################################
  //# Specific Test Cases
  @Override
  public void testAip0Sub1P0() throws Exception
  {
    // too tough :-(
  }

  @Override
  public void testTbedMinsync() throws Exception
  {
    // too tough :-(
  }

  @Override
  public void testZeroSup() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "test_zero_sup.wmod");
    runSynthesizer(des, true);
  }


  //#########################################################################
  //# Overrides for AbstractSupervisorSynthesizerTest
  @Override
  protected void verifySupervisorNonblocking(final ProductDESProxy des)
  {
  }

}
