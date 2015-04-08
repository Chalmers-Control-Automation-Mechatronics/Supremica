//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.hisc
//# CLASS:   CompositionalSICProperty5VerifierTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.hisc;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.analysis.trcomp.AbstractTRCompositionalAnalyzer;
import net.sourceforge.waters.analysis.trcomp.TRCompositionalConflictChecker;
import net.sourceforge.waters.model.analysis.des.ModelVerifier;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


public class CompositionalSICProperty5VerifierTest
  extends AbstractSICProperty5VerifierTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite suite =
      new TestSuite(CompositionalSICProperty5VerifierTest.class);
    return suite;
  }

  public static void main(final String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }


  //#########################################################################
  //# Overrides for abstract base class
  //# net.sourceforge.waters.analysis.AbstractModelVerifierTest
  @Override
  protected ModelVerifier createModelVerifier
    (final ProductDESProxyFactory factory)
  {
    final TRCompositionalConflictChecker checker =
      new TRCompositionalConflictChecker();
    checker.setSimplifierCreator(TRCompositionalConflictChecker.GNBw);
    checker.setPreselectionHeuristic(AbstractTRCompositionalAnalyzer.PRESEL_MinT);
    checker.setSelectionHeuristic(AbstractTRCompositionalAnalyzer.SEL_MaxL);
    checker.setInternalStateLimit(5000);
    checker.setInternalTransitionLimit(100000);
    checker.setBlockedEventsEnabled(true);
    checker.setFailingEventsEnabled(true);
    checker.setSelfloopOnlyEventsEnabled(true);
    checker.setAlwaysEnabledEventsEnabled(true);
    checker.setCounterExampleEnabled(true);
    checker.setTraceCheckingEnabled(true);
    return new SICProperty5Verifier(checker, factory);
  }

}
