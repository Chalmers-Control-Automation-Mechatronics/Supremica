//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:   CompositionalSynthesizerMustLMinSyncTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.model.analysis.AbstractSynthesizerTest;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


public class CompositionalSynthesizerMaxSMinSyncTest
  extends AbstractSynthesizerTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite testSuite =
      new TestSuite(CompositionalSynthesizerMaxSMinSyncTest.class);
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
      new CompositionalSynthesizer(factory);
    synthesizer.setInternalStateLimit(5000);
    synthesizer.setMonolithicStateLimit(100000);
    synthesizer.setInternalTransitionLimit(500000);
    synthesizer.setPreselectingMethod(AbstractCompositionalModelAnalyzer.MaxS);
    synthesizer.setSelectingMethod(AbstractCompositionalModelAnalyzer.MinSync);
    return synthesizer;
  }

}
