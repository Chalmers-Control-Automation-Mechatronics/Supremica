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


public class CompositionalSupervisorReductionMustLMinSTest
  extends AbstractSupervisorSynthesizerTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite testSuite =
      new TestSuite(CompositionalSupervisorReductionMustLMinSTest.class);
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
  protected CompositionalAutomataSynthesizer createSynthesizer
    (final ProductDESProxyFactory factory)
  {
    final CompositionalAutomataSynthesizer synthesizer =
      new CompositionalAutomataSynthesizer(factory,
                                   AutomataSynthesisAbstractionProcedureFactory.WSOE);
    synthesizer.setInternalStateLimit(5000);
    synthesizer.setMonolithicStateLimit(100000);
    synthesizer.setInternalTransitionLimit(500000);
    synthesizer.setPreselectingMethod(AbstractCompositionalModelAnalyzer.MustL);
    synthesizer.setSelectingMethod(AbstractCompositionalModelAnalyzer.MinS);
    synthesizer.setSupervisorReductionEnabled(true);
    return synthesizer;
  }

}
