//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.cpp.analysis
//# CLASS:   ProjectingModelVerifierFactory
//###########################################################################
//# $Id: ProjectingModelVerifierFactory.java,v 1.4 2007-07-06 00:25:12 siw4 Exp $
//###########################################################################

package net.sourceforge.waters.analysis.modular.supremica;

import net.sourceforge.waters.analysis.modular.ModularLanguageInclusionChecker;
import java.util.List;

import net.sourceforge.waters.analysis.modular.HeuristicType;
import net.sourceforge.waters.analysis.modular.MaxCommonEventsHeuristic;
import net.sourceforge.waters.cpp.analysis.NativeControllabilityChecker;
import net.sourceforge.waters.model.analysis.ModelVerifierFactory;
import net.sourceforge.waters.model.analysis.LanguageInclusionChecker;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.analysis.monolithic.MonolithicControllabilityChecker;


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
    return new ModularLanguageInclusionChecker(
       null, factory,
       createControllabilityChecker(factory),
       new MaxCommonEventsHeuristic(HeuristicType.PREFERREALPLANT)
       );
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
