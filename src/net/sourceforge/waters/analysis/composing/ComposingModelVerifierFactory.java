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

public class ComposingModelVerifierFactory
  extends AbstractModelVerifierFactory
{

  //#########################################################################
  //# Constructors
  private ComposingModelVerifierFactory()
  {
  }

  private ComposingModelVerifierFactory(final List<String> arglist)
  {
    super(arglist);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelVerifierFactory
  public ComposingControllabilityChecker createControllabilityChecker
    (final ProductDESProxyFactory factory)
  {
    return new ComposingControllabilityChecker(factory);
  }

  public ComposingLanguageInclusionChecker createLanguageInclusionChecker
    (final ProductDESProxyFactory factory)
  {
    return new ComposingLanguageInclusionChecker(factory);
  }


  //#########################################################################
  //# Factory Instantiation
  public static ComposingModelVerifierFactory getInstance()
  {
    if (theInstance == null) {
      theInstance = new ComposingModelVerifierFactory();
    }
    return theInstance;
  }

  public static ComposingModelVerifierFactory
    getInstance(final List<String> cmdline)
  {
    return new ComposingModelVerifierFactory(cmdline);
  }


  //#########################################################################
  //# Class Variables
  private static ComposingModelVerifierFactory theInstance = null;

}
