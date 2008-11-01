//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.cpp.analysis
//# CLASS:   MonolithicModelVerifierFactory
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.monolithic;

import java.util.List;

import net.sourceforge.waters.model.analysis.AbstractModelVerifierFactory;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * A factory interface for all types of model verifiers.
 *
 * @author Robi Malik
 */

public class MonolithicModelVerifierFactory
  extends AbstractModelVerifierFactory
{

  //#########################################################################
  //# Constructors
  private MonolithicModelVerifierFactory()
  {
  }

  private MonolithicModelVerifierFactory(final List<String> arglist)
  {
    super(arglist);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelVerifierFactory
  public MonolithicControllabilityChecker createControllabilityChecker
    (final ProductDESProxyFactory factory)
  {
    return new MonolithicControllabilityChecker(factory);
  }

  public MonolithicLanguageInclusionChecker createLanguageInclusionChecker
    (final ProductDESProxyFactory factory)
  {
    return new MonolithicLanguageInclusionChecker(factory);
  }


  //#########################################################################
  //# Factory Instantiation
  public static MonolithicModelVerifierFactory getInstance()
  {
    if (theInstance == null) {
      theInstance = new MonolithicModelVerifierFactory();
    }
    return theInstance;
  }

  public static MonolithicModelVerifierFactory
    getInstance(final List<String> cmdline)
  {
    return new MonolithicModelVerifierFactory(cmdline);
  }


  //#########################################################################
  //# Class Variables
  private static MonolithicModelVerifierFactory theInstance = null;

}
