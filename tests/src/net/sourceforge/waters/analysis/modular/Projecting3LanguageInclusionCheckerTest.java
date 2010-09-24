//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.modular
//# CLASS:   ProjectingLanguageInclusionCheckerTest
//###########################################################################
//# $Id: ProjectingLanguageInclusionCheckerTest.java 5900 2010-08-19 05:45:16Z cmjr1 $
//###########################################################################

package net.sourceforge.waters.analysis.modular;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.analysis.modular.Projection3.Method;
import net.sourceforge.waters.cpp.analysis.NativeLanguageInclusionChecker;
import net.sourceforge.waters.model.analysis.
       AbstractLanguageInclusionCheckerTest;
import net.sourceforge.waters.model.analysis.SafetyVerifier;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


public class Projecting3LanguageInclusionCheckerTest
  extends AbstractLanguageInclusionCheckerTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public static Test suite()
  {
    final TestSuite testSuite =
      new TestSuite(Projecting3LanguageInclusionCheckerTest.class);
    return testSuite;
  }

  public static void main(final String[] args)
  {
    junit.textui.TestRunner.run(suite());
  }


  //#########################################################################
  //# Overrides for abstract base class
  //# net.sourceforge.waters.analysis.AbstractModelVerifierTest
  protected ProjectingLanguageInclusionChecker createModelVerifier
    (final ProductDESProxyFactory factory)
  {
    final SafetyVerifier subchecker =
      new NativeLanguageInclusionChecker(factory);
    final Projection3 projector = new Projection3(factory);
    projector.setMethod(Method.STATE);
    projector.setOutputStream(true);
    final ProjectingLanguageInclusionChecker checker =
      new ProjectingLanguageInclusionChecker(factory, subchecker, projector);
    checker.setMaxProjStates(2000);
    return checker;
  }

}
