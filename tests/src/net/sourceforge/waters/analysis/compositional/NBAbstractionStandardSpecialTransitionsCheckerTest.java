//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   NBAbstractionStandardConflictCheckerTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.compositional;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.model.analysis.AbstractStandardConflictCheckerTest;
import net.sourceforge.waters.model.analysis.des.ConflictChecker;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


public class NBAbstractionStandardSpecialTransitionsCheckerTest
  extends AbstractStandardConflictCheckerTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite testSuite =
      new TestSuite(NBAbstractionStandardSpecialTransitionsCheckerTest.class);
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
  protected ConflictChecker createModelVerifier
    (final ProductDESProxyFactory factory)
  {
    final CompositionalSpecialTransitionsChecker checker =
      new CompositionalSpecialTransitionsChecker(factory,
                                       ConflictAbstractionProcedureFactory.NB);
    // checker.setSelectingMethod(AbstractCompositionalModelAnalyzer.MinF);
    checker.setInternalStateLimit(5000);
    checker.setMonolithicStateLimit(100000);
    checker.setInternalTransitionLimit(500000);
    checker.setTraceCheckingEnabled(false);
    return checker;
  }


  //#########################################################################
  //# Test Cases
  public void testBigComponent() throws Exception
  {
    final CompositionalSpecialTransitionsChecker checker =
      (CompositionalSpecialTransitionsChecker) getModelVerifier();
    checker.setInternalStateLimit(1000);
    final String group = "tests";
    final String subdir = "nasty";
    final String name = "big_component.wmod";
    runModelVerifier(group, subdir, name, false);
  }

}
