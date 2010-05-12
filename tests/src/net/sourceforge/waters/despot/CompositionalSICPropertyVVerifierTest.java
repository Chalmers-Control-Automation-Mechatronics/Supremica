//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.despot
//# CLASS:   CompositionalSICPropertyVVerifierTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.despot;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.analysis.gnonblocking.CompositionalGeneralisedConflictChecker;
import net.sourceforge.waters.model.analysis.ConflictChecker;
import net.sourceforge.waters.model.analysis.ModelVerifier;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


public class CompositionalSICPropertyVVerifierTest
  extends AbstractSICPropertyVVerifierTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite suite =
      new TestSuite(CompositionalSICPropertyVVerifierTest.class);
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
    final ConflictChecker checker =
        new CompositionalGeneralisedConflictChecker(factory);
    return new SICPropertyVVerifier(checker, factory);
  }

}
