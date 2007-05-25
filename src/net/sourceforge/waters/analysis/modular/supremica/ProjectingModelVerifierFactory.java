//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.cpp.analysis
//# CLASS:   ProjectingModelVerifierFactory
//###########################################################################
//# $Id: ProjectingModelVerifierFactory.java,v 1.1 2007-05-25 07:53:02 robi Exp $
//###########################################################################

package net.sourceforge.waters.analysis.modular.supremica;

import java.util.List;

import net.sourceforge.waters.analysis.modular.HeuristicType;
import net.sourceforge.waters.analysis.modular.MaxCommonEventsHeuristic;
import net.sourceforge.waters.cpp.analysis.NativeControllabilityChecker;
import net.sourceforge.waters.model.analysis.ModelVerifierFactory;
import net.sourceforge.waters.model.analysis.LanguageInclusionChecker;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * A factory that produces projecting model verifiers.
 *
 * @author Robi Malik
 */

public class ProjectingModelVerifierFactory implements ModelVerifierFactory
{

  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelVerifierFactory
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
    throw new UnsupportedOperationException
      ("Language inclusion check not yet supported!");
  }


  //#########################################################################
  //# Factory Instantiation
  public static ProjectingModelVerifierFactory getInstance()
  {
    if (theInstance == null) {
      theInstance = new ProjectingModelVerifierFactory();
    }
    return theInstance;
  }

  public static ProjectingModelVerifierFactory
    getInstance(final List<String> cmdline)
  {
    return getInstance();
  }


  //#########################################################################
  //# Class Variables
  private static ProjectingModelVerifierFactory theInstance = null;

}
