//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.modular.supremica
//# CLASS:   ProjectingLanguageInclusionCheckerTest
//###########################################################################
//# $Id: ProjectingLanguageInclusionCheckerTest.java,v 1.3 2008-06-30 01:50:57 robi Exp $
//###########################################################################

package net.sourceforge.waters.analysis.modular.supremica;

import net.sourceforge.waters.analysis.modular.ModularLanguageInclusionChecker;
import net.sourceforge.waters.analysis.modular.HeuristicType;
import net.sourceforge.waters.analysis.modular.MaxCommonEventsHeuristic;
import net.sourceforge.waters.cpp.analysis.NativeControllabilityChecker;
import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.analysis.modular.supremica.
       ProjectingModelVerifierFactory;
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
    return createLanguageInclusionChecker(desfactory);
  }

  public ProjectingControllabilityChecker createControllabilityChecker
    (final ProductDESProxyFactory factory)
  {
    return new ProjectingControllabilityChecker
      (null,
       factory,
       new NativeControllabilityChecker(factory),
       new MaxCommonEventsHeuristic(HeuristicType.PREFERREALPLANT),
       false);
  }

  public LanguageInclusionChecker createLanguageInclusionChecker
    (final ProductDESProxyFactory factory)
  {
    return new ModularLanguageInclusionChecker(
       null, factory,
       createControllabilityChecker(factory),
       new MaxCommonEventsHeuristic(HeuristicType.PREFERREALPLANT));
  }
}
