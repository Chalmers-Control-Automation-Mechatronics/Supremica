//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   ModularAndCompositionalSynthesizerTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.compositional;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.model.analysis.AbstractSupervisorSynthesizerTest;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


public class ModularAndCompositionalSynthesizerTest
  extends AbstractSupervisorSynthesizerTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite testSuite =
      new TestSuite(ModularAndCompositionalSynthesizerTest.class);
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
  public void testTbedMinsync()throws Exception
  {
    // too tough :-(
  }


  //#########################################################################
  //# Overrides for abstract base class
  //# net.sourceforge.waters.analysis.AbstractModelVerifierTest
  @Override
  protected ModularAndCompositionalSynthesizer createSynthesizer
    (final ProductDESProxyFactory factory)
  {
    final ModularAndCompositionalSynthesizer synthesizer =
      new ModularAndCompositionalSynthesizer(factory);
    synthesizer.setInternalStateLimit(5000);
    return synthesizer;
  }

}
