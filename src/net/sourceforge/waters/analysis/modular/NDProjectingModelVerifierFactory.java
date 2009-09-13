//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.modular
//# CLASS:   ProjectingModelVerifierFactory
//###########################################################################
//# $Id: ProjectingModelVerifierFactory.java 4468 2008-11-01 21:54:58Z robi $
//###########################################################################

package net.sourceforge.waters.analysis.modular;

import java.util.List;

import net.sourceforge.waters.analysis.monolithic.MonolithicControllabilityChecker;
import net.sourceforge.waters.cpp.analysis.NativeControllabilityChecker;
import net.sourceforge.waters.model.analysis.AbstractModelVerifierFactory;
import net.sourceforge.waters.model.analysis.LanguageInclusionChecker;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.analysis.ProjectingNonBlockingChecker;


/**
 * A factory that produces projecting model verifiers.
 *
 * @author Robi Malik
 */

public class NDProjectingModelVerifierFactory
  extends AbstractModelVerifierFactory
{

  //#########################################################################
  //# Constructors
  private NDProjectingModelVerifierFactory()
  {
  }

  private NDProjectingModelVerifierFactory(final List<String> arglist)
  {
    super(arglist);
    addArgument(ModularHeuristicFactory.getMethodArgument());
    addArgument(ModularHeuristicFactory.getPreferenceArgument());
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelVerifierFactory
  public ProjectingNonBlockingChecker createConflictChecker
    (final ProductDESProxyFactory factory)
  {
    return new ProjectingNonBlockingChecker(null, factory);
  }
  
  public NDProjectingControllabilityChecker createControllabilityChecker
    (final ProductDESProxyFactory factory)
  {
    return new NDProjectingControllabilityChecker
      (null,
       factory,
       new NativeControllabilityChecker(factory),
       false);
  }

  public LanguageInclusionChecker createLanguageInclusionChecker
    (final ProductDESProxyFactory factory)
  {
    return new ModularLanguageInclusionChecker(
       null, factory,
       /*new OneUncontrollableChecker(null, factory,
                                    createControllabilityChecker(factory)),*/
       createControllabilityChecker(factory));
  }


  //#########################################################################
  //# Factory Instantiation
  public static NDProjectingModelVerifierFactory getInstance()
  {
    if (theInstance == null) {
      theInstance = new NDProjectingModelVerifierFactory();
    }
    return theInstance;
  }

  public static NDProjectingModelVerifierFactory
    getInstance(final List<String> cmdline)
  {
    return new NDProjectingModelVerifierFactory(cmdline);
  }


  //#########################################################################
  //# Class Variables
  private static NDProjectingModelVerifierFactory theInstance = null;

}
