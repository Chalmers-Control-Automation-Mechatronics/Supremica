//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   CompositionalSynthesizerMustLMinSTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.compositional;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.model.analysis.AbstractSupervisorSynthesizerTest;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


public class CompositionalSynthesizerMaxSMaxLUnSupTest
  extends AbstractSupervisorSynthesizerTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite testSuite =
      new TestSuite(CompositionalSynthesizerMaxSMaxLUnSupTest.class);
    return testSuite;
  }

  public static void main(final String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }


  //#########################################################################
  //# Overrides for abstract base class
  //# net.sourceforge.waters.analysis.AbstractModelVerifierTest
  @Override
  protected CompositionalSynthesizer createSynthesizer
    (final ProductDESProxyFactory factory)
  {
    final CompositionalSynthesizer synthesizer =
      new CompositionalSynthesizer(factory,
                                   SynthesisAbstractionProcedureFactory.WSOE_UNSUP);
    synthesizer.setInternalStateLimit(5000);
    synthesizer.setMonolithicStateLimit(2000000);
    synthesizer.setInternalTransitionLimit(1000000);
    synthesizer.setPreselectingMethod(AbstractCompositionalModelAnalyzer.MaxS);
    synthesizer.setSelectingMethod(AbstractCompositionalModelAnalyzer.MaxL);
    return synthesizer;
  }

}
