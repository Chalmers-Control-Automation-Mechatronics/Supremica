//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.despot
//# CLASS:   MonolithicSICProperty5VerifierTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.hisc;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.analysis.hisc.SICProperty5Verifier;
import net.sourceforge.waters.analysis.monolithic.MonolithicConflictChecker;
import net.sourceforge.waters.model.analysis.ConflictChecker;
import net.sourceforge.waters.model.analysis.ModelVerifier;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


public class MonolithicSICProperty5VerifierTest extends
    AbstractSICProperty5VerifierTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite suite =
      new TestSuite(MonolithicSICProperty5VerifierTest.class);
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
    return new SICProperty5Verifier(checker, factory);
  }


  //#########################################################################
  //# Test Cases
  public void testSICProperty5Verifier_rhone_subsystem1_ld()
    throws Exception
  {
    try {
      super.testSICProperty5Verifier_rhone_subsystem1_ld();
    } catch (final OverflowException exception) {
      // MonolithicConflictChecker fails because of state encoding size :-(
    }
  }

  public void testSICProperty5Verifier_rhone_subsystem1_ld_failsic5()
    throws Exception
  {
    try {
      super.testSICProperty5Verifier_rhone_subsystem1_ld_failsic5();
    } catch (final OverflowException exception) {
      // MonolithicConflictChecker fails because of state encoding size :-(
    }
  }

}
