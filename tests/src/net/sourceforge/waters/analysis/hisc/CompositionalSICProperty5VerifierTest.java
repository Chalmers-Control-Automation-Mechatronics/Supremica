//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.despot
//# CLASS:   CompositionalSICProperty5VerifierTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.hisc;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.analysis.gnonblocking.CompositionalGeneralisedConflictChecker;
import net.sourceforge.waters.analysis.hisc.SICProperty5Verifier;
import net.sourceforge.waters.model.analysis.ModelVerifier;
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
  protected ModelVerifier createModelVerifier
    (final ProductDESProxyFactory factory)
  {
    final CompositionalGeneralisedConflictChecker checker =
        new CompositionalGeneralisedConflictChecker(factory);
    checker.setInternalStepTransitionLimit(100000);
    return new SICProperty5Verifier(checker, factory);
  }

}
