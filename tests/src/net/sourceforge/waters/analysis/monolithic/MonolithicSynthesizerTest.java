//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.monolithic
//# CLASS:   MonolithicSynchronousProductBuilderTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.monolithic;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.model.analysis.AbstractSupervisorSynthesizerTest;
import net.sourceforge.waters.model.analysis.SupervisorSynthesizer;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


public class MonolithicSynthesizerTest
  extends AbstractSupervisorSynthesizerTest
{
  //#########################################################################
  //# To be Provided by Subclasses
  /**
   * Creates an instance of the synthesiser under test. This method
   * instantiates the class of the synthesiser tested by the particular
   * subclass of this test, and configures it as needed.
   * @param factory
   *          The factory used by the synthesiser to create its output.
   * @return An instance of the synthesiser.
   */
  protected SupervisorSynthesizer createSynthesizer
    (final ProductDESProxyFactory factory){
      final MonolithicSynthesizer synthesizer =  new MonolithicSynthesizer(factory);
      synthesizer.setSupervisorReductionEnabled(true);
      return synthesizer;
  }

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite() {
    final TestSuite testSuite =
      new TestSuite(MonolithicSynthesizerTest.class);
    return testSuite;
  }

  public static void main(final String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }


  //#########################################################################
  //# Test Cases --- BIG
  @Override
  public void testKoordWspSynth() throws Exception
  {
  }

}
