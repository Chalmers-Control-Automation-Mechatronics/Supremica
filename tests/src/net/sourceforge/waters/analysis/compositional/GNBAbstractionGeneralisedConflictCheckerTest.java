//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   GNBAbstractionGeneralisedConflictCheckerTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.compositional;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.model.analysis.AbstractGeneralisedConflictCheckerTest;
import net.sourceforge.waters.model.analysis.ConflictChecker;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


public class GNBAbstractionGeneralisedConflictCheckerTest
  extends AbstractGeneralisedConflictCheckerTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite testSuite =
      new TestSuite(GNBAbstractionGeneralisedConflictCheckerTest.class);
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
    final CompositionalConflictChecker checker =
      new CompositionalConflictChecker(factory,
                                       ConflictAbstractionProcedureFactory.GNB);
    checker.setSelectingMethod(CompositionalConflictChecker.MinSyncA);
    checker.setInternalStateLimit(5000);
    checker.setMonolithicStateLimit(100000);
    checker.setInternalTransitionLimit(500000);
    return checker;
  }

}
