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
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


public class CompositionalStateRepresentationSynthesizerMustLMinSTest
  extends AbstractSupervisorSynthesizerTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite testSuite =
      new TestSuite(CompositionalStateRepresentationSynthesizerMustLMinSTest.class);
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
  protected AbstractCompositionalSynthesizer createSynthesizer
    (final ProductDESProxyFactory factory)
  {
    @SuppressWarnings("unused")
    final CompositionalStateRepresentationSynthesizer synthesizerSR =
      new CompositionalStateRepresentationSynthesizer(factory,
          StateRepresentationSynthesisAbstractionProcedureFactory.NO_TRANSITIONREMOVAL);
    final CompositionalAutomataSynthesizer synthesizerAut =
      new CompositionalAutomataSynthesizer(factory,
          AutomataSynthesisAbstractionProcedureFactory.WSOE);
    final AbstractCompositionalSynthesizer synthesizer = synthesizerAut;
    synthesizer.setInternalStateLimit(20000);
    synthesizer.setMonolithicStateLimit(1000000);
    synthesizer.setInternalTransitionLimit(1000000);
    synthesizer.setMonolithicTransitionLimit(5000000);
    synthesizer.setPreselectingMethod(AbstractCompositionalModelAnalyzer.Pairs);
    synthesizer.setSelectionHeuristic
      (CompositionalSelectionHeuristicFactory.MinSync);
    synthesizer.setDetailedOutputEnabled(false);
    synthesizer.setFailingEventsEnabled(false);
    return synthesizer;
  }


  //#########################################################################
  //# Test Cases --- just debugging
  public void testTbedHisc1() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("despot", "tbed_hisc", "tbed_hisc1.wmod");
    runSynthesizer(des, true);
  }

}
