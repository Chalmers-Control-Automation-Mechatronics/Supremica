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
import net.sourceforge.waters.analysis.CompNonBlockingChecker;


/**
 * A factory that produces projecting model verifiers.
 *
 * @author Robi Malik
 */

public class CompModelVerifierFactory
  extends AbstractModelVerifierFactory
{

  //#########################################################################
  //# Constructors
  private CompModelVerifierFactory()
  {
  }

  private CompModelVerifierFactory(final List<String> arglist)
  {
    super(arglist);
    addArgument(ModularHeuristicFactory.getMethodArgument());
    addArgument(ModularHeuristicFactory.getPreferenceArgument());
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelVerifierFactory
  public CompNonBlockingChecker createConflictChecker
    (final ProductDESProxyFactory factory)
  {
    return new CompNonBlockingChecker(null, factory);
  }
  
  //#########################################################################
  //# Factory Instantiation
  public static CompModelVerifierFactory getInstance()
  {
    if (theInstance == null) {
      theInstance = new CompModelVerifierFactory();
    }
    return theInstance;
  }

  public static CompModelVerifierFactory
    getInstance(final List<String> cmdline)
  {
    return new CompModelVerifierFactory(cmdline);
  }


  //#########################################################################
  //# Class Variables
  private static CompModelVerifierFactory theInstance = null;

}
