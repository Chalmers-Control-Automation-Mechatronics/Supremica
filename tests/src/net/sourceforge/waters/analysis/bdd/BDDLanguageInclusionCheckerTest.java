//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.monolithic
//# CLASS:   BDDLanguageInclusionCheckerTest
//###########################################################################
//# $Id: BDDLanguageInclusionCheckerTest.java,v 1.1 2007-11-02 00:30:37 robi Exp $
//###########################################################################

package net.sourceforge.waters.analysis.bdd;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.model.analysis.
  AbstractLanguageInclusionCheckerTest;
import net.sourceforge.waters.model.analysis.LanguageInclusionChecker;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


public class BDDLanguageInclusionCheckerTest
  extends AbstractLanguageInclusionCheckerTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite() {
    TestSuite testSuite =
      new TestSuite(BDDLanguageInclusionCheckerTest.class);
    return testSuite;
  }

  public static void main(final String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }


  //#########################################################################
  //# Overrides for abstract base class
  //# net.sourceforge.waters.analysis.AbstractModelVerifierTest
  protected LanguageInclusionChecker
    createModelVerifier(final ProductDESProxyFactory factory)
  {
    return new BDDLanguageInclusionChecker(factory);
  }

}
