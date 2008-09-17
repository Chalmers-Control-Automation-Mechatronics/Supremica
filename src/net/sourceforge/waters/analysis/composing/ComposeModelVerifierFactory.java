//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.composing
//# CLASS:   ComposeModelVerifierFactory
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.composing;

import java.util.List;

import net.sourceforge.waters.model.analysis.AbstractModelVerifierFactory;
import net.sourceforge.waters.model.analysis.ModelVerifierFactory;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * A factory that produces compositional model verifiers.
 *
 * @author Jinjian Shi, Robi Malik
 */

public class ComposeModelVerifierFactory
  extends AbstractModelVerifierFactory
{

  //#########################################################################
  //# Constructors
  private ComposeModelVerifierFactory()
  {
  }

  private ComposeModelVerifierFactory(final List<String> arglist)
  {
    super(arglist);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelVerifierFactory
  public ComposeControllabilityChecker createControllabilityChecker
    (final ProductDESProxyFactory factory)
  {
    return new ComposeControllabilityChecker(factory);
  }

  public ComposeLanguageInclusionChecker createLanguageInclusionChecker
    (final ProductDESProxyFactory factory)
  {
    return new ComposeLanguageInclusionChecker(factory);
  }


  //#########################################################################
  //# Factory Instantiation
  public static ComposeModelVerifierFactory getInstance()
  {
    if (theInstance == null) {
      theInstance = new ComposeModelVerifierFactory();
    }
    return theInstance;
  }

  public static ComposeModelVerifierFactory
    getInstance(final List<String> cmdline)
  {
    return new ComposeModelVerifierFactory(cmdline);
  }


  //#########################################################################
  //# Class Variables
  private static ComposeModelVerifierFactory theInstance = null;

}
