//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.bdd
//# CLASS:   BDDModelVerifierFactory
//###########################################################################
//# $Id: BDDModelVerifierFactory.java,v 1.1 2007-11-02 00:30:37 robi Exp $
//###########################################################################

package net.sourceforge.waters.analysis.bdd;

import java.util.List;

import net.sourceforge.waters.model.analysis.AbstractModelVerifierFactory;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * A model verifier factory to produce BDD-based model verifiers.
 *
 * @author Robi Malik
 */

public class BDDModelVerifierFactory
  extends AbstractModelVerifierFactory
{

  //#########################################################################
  //# Constructors
  private BDDModelVerifierFactory()
  {
  }

  private BDDModelVerifierFactory(final List<String> arglist)
  {
    super(arglist);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelVerifierFactory
  public BDDControllabilityChecker createControllabilityChecker
    (final ProductDESProxyFactory factory)
  {
    return new BDDControllabilityChecker(factory);
  }

  public BDDLanguageInclusionChecker createLanguageInclusionChecker
    (final ProductDESProxyFactory factory)
  {
    return new BDDLanguageInclusionChecker(factory);
  }


  //#########################################################################
  //# Factory Instantiation
  public static BDDModelVerifierFactory getInstance()
  {
    if (theInstance == null) {
      theInstance = new BDDModelVerifierFactory();
    }
    return theInstance;
  }

  public static BDDModelVerifierFactory
    getInstance(final List<String> cmdline)
  {
    return new BDDModelVerifierFactory(cmdline);
  }


  //#########################################################################
  //# Class Variables
  private static BDDModelVerifierFactory theInstance = null;

}
