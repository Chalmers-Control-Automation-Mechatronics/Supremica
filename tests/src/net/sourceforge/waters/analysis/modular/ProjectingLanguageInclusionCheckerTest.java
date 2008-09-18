//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.modular
//# CLASS:   ProjectingLanguageInclusionCheckerTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.modular;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.cpp.analysis.NativeControllabilityChecker;
import net.sourceforge.waters.model.analysis.
       AbstractLanguageInclusionCheckerTest;
import net.sourceforge.waters.model.analysis.LanguageInclusionChecker;
import net.sourceforge.waters.model.analysis.ModelVerifierFactory;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


public class ProjectingLanguageInclusionCheckerTest
  extends AbstractLanguageInclusionCheckerTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite testSuite =
      new TestSuite(ProjectingLanguageInclusionCheckerTest.class);
    return testSuite;
  }

  public static void main(final String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }


  //#########################################################################
  //# Overrides for abstract base class
  //# net.sourceforge.waters.analysis.AbstractModelVerifierTest
  protected LanguageInclusionChecker createModelVerifier
    (final ProductDESProxyFactory desfactory)
  {
    final ModelVerifierFactory checkerfactory =
      ProjectingModelVerifierFactory.getInstance();
    return checkerfactory.createLanguageInclusionChecker(desfactory);
  }

}
