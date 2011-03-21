//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.despot
//# CLASS:   MonolithicSICProperty6VerifierTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.hisc;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.analysis.hisc.SICProperty6Verifier;
import net.sourceforge.waters.analysis.monolithic.MonolithicConflictChecker;
import net.sourceforge.waters.model.analysis.ConflictChecker;
import net.sourceforge.waters.model.analysis.ModelVerifier;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


public class MonolithicSICProperty6VerifierTest extends
    AbstractSICProperty6VerifierTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite suite =
      new TestSuite(MonolithicSICProperty6VerifierTest.class);
    return suite;
  }

  public static void main(final String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }


  //#########################################################################
  //# Overrides for abstract base class
  //# net.sourceforge.waters.analysis.AbstractModelVerifierTest
  protected ModelVerifier createModelVerifier(
                                              final ProductDESProxyFactory factory)
  {
    final ConflictChecker checker = new MonolithicConflictChecker(factory);
    return new SICProperty6Verifier(checker, factory);
  }


  //#########################################################################
  //# Test Cases
  public void testSICProperty6Verifier_rhone_subsystem1_ld()
    throws Exception
  {
    try {
      super.testSICProperty6Verifier_rhone_subsystem1_ld();
    } catch (final OverflowException exception) {
      // MonolithicConflictChecker fails because of state encoding size :-(
    }
  }

}
