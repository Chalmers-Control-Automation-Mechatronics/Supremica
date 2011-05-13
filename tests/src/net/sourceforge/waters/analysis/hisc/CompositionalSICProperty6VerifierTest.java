//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.hisc
//# CLASS:   CompositionalSICProperty6VerifierTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.hisc;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.analysis.gnonblocking.CompositionalGeneralisedConflictChecker;
import net.sourceforge.waters.analysis.hisc.SICProperty6Verifier;
import net.sourceforge.waters.model.analysis.ModelVerifier;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


public class CompositionalSICProperty6VerifierTest
  extends AbstractSICProperty6VerifierTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite suite =
      new TestSuite(CompositionalSICProperty6VerifierTest.class);
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
    checker.setInternalStepNodeLimit(5000);
    return new SICProperty6Verifier(checker, factory);
  }

}
