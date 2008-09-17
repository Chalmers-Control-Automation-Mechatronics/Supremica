//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.cpp.analysis
//# CLASS:   ProjectingModelVerifierFactory
//###########################################################################
//# $Id: ProjectingModelVerifierFactory.java,v 1.6 2008-06-29 22:49:20 robi Exp $
//###########################################################################

package net.sourceforge.waters.analysis.modular.supremica;

import java.util.List;

import net.sourceforge.waters.analysis.modular.HeuristicType;
import net.sourceforge.waters.analysis.modular.MaxCommonEventsHeuristic;
import net.sourceforge.waters.analysis.modular.MaxCommonUncontrollableEventsHeuristic;
import net.sourceforge.waters.analysis.modular.MinNewEventsHeuristic;
import net.sourceforge.waters.analysis.modular.ModularLanguageInclusionChecker;
import net.sourceforge.waters.analysis.modular.OneUncontrollableChecker;
import net.sourceforge.waters.analysis.modular.RelMaxCommonEventsHeuristic;
import net.sourceforge.waters.analysis.monolithic.MonolithicControllabilityChecker;
import net.sourceforge.waters.cpp.analysis.NativeControllabilityChecker;
import net.sourceforge.waters.model.analysis.AbstractModelVerifierFactory;
import net.sourceforge.waters.model.analysis.LanguageInclusionChecker;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * A factory that produces projecting model verifiers.
 *
 * @author Robi Malik
 */

public class ProjectingModelVerifierFactory
  extends AbstractModelVerifierFactory
{

  //#########################################################################
  //# Constructors
  private ProjectingModelVerifierFactory()
  {
  }

  private ProjectingModelVerifierFactory(final List<String> arglist)
  {
    super(arglist);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelVerifierFactory
  public ProjectingControllabilityChecker createControllabilityChecker
    (final ProductDESProxyFactory factory)
  {
    return new ProjectingControllabilityChecker
      (null,
       factory,
       new NativeControllabilityChecker(factory),
       new RelMaxCommonEventsHeuristic(HeuristicType.NOPREF),
       false);
  }

  public LanguageInclusionChecker createLanguageInclusionChecker
    (final ProductDESProxyFactory factory)
  {
    return new ModularLanguageInclusionChecker(
       null, factory,
       /*new OneUncontrollableChecker(null, factory,
                                    createControllabilityChecker(factory)),*/
       createControllabilityChecker(factory),
       new RelMaxCommonEventsHeuristic(HeuristicType.NOPREF)
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
    return new ProjectingModelVerifierFactory(cmdline);
  }


  //#########################################################################
  //# Class Variables
  private static ProjectingModelVerifierFactory theInstance = null;

}
