//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.modular
//# CLASS:   ComposeLanguageInclusionCheckerTest
//###########################################################################
//# $Id: ComposeLanguageInclusionCheckerTest.java,v 1.6 2008-06-30 01:50:57 robi Exp $
//###########################################################################

package net.sourceforge.waters.analysis.composing;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.cpp.analysis.NativeControllabilityChecker;
import net.sourceforge.waters.model.analysis.
       AbstractLanguageInclusionCheckerTest;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


public class ComposeLanguageInclusionCheckerTest
  extends AbstractLanguageInclusionCheckerTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite() {
    TestSuite testSuite =
      new TestSuite(ComposeLanguageInclusionCheckerTest.class);
    return testSuite;
  }

  public static void main(final String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }

  //#########################################################################
  //# Overrides for abstract base class
  //# net.sourceforge.waters.analysis.AbstractModelVerifierTest
  protected ComposeLanguageInclusionChecker createModelVerifier
    (final ProductDESProxyFactory factory)
  {
    final ComposeModelVerifierFactory checkerfactory =
      ComposeModelVerifierFactory.getInstance();
    return checkerfactory.createLanguageInclusionChecker(factory);
  }

}
