//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.cpp.analysis
//# CLASS:   ProjectingModelVerifierFactory
//###########################################################################
//# $Id: ModularModelVerifierFactory.java,v 1.2 2008-06-29 22:49:20 robi Exp $
//###########################################################################

package net.sourceforge.waters.analysis.modular;

import java.util.List;

import net.sourceforge.waters.analysis.monolithic.MonolithicControllabilityChecker;
import net.sourceforge.waters.cpp.analysis.NativeControllabilityChecker;
import net.sourceforge.waters.model.analysis.AbstractModelVerifierFactory;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * A factory that produces modular/incremental model verifiers.
 *
 * @author Robi Malik
 */

public class ModularModelVerifierFactory
  extends AbstractModelVerifierFactory
{

  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelVerifierFactory
  public ModularControllabilityChecker createControllabilityChecker
    (final ProductDESProxyFactory factory)
  {
    return new ModularControllabilityChecker
      (null,
       factory,
       new NativeControllabilityChecker(factory),
       new RelMaxCommonEventsHeuristic(HeuristicType.NOPREF),
       false);
  }

  public ModularLanguageInclusionChecker createLanguageInclusionChecker
    (final ProductDESProxyFactory factory)
  {
    return new ModularLanguageInclusionChecker(
       null, factory,
       createControllabilityChecker(factory),
       new RelMaxCommonEventsHeuristic(HeuristicType.NOPREF)
       );
  }


  //#########################################################################
  //# Factory Instantiation
  public static ModularModelVerifierFactory getInstance()
  {
    if (theInstance == null) {
      theInstance = new ModularModelVerifierFactory();
    }
    return theInstance;
  }

  public static ModularModelVerifierFactory
    getInstance(final List<String> cmdline)
  {
    return getInstance();
  }


  //#########################################################################
  //# Class Variables
  private static ModularModelVerifierFactory theInstance = null;

}
