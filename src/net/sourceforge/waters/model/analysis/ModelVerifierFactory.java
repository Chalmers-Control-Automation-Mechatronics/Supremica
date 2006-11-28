//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   ModelVerifierFactory
//###########################################################################
//# $Id: ModelVerifierFactory.java,v 1.1 2006-11-28 04:28:33 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.analysis;

import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * A factory interface for all types of model verifiers.
 *
 * @author Robi Malik
 */

public interface ModelVerifierFactory
{

  //#########################################################################
  //# Object Construction
  /**
   * Creates a controllability checker.
   */
  public ControllabilityChecker createControllabilityChecker
    (ProductDESProxyFactory factory);

  /**
   * Creates a language inclusion checker.
   */
  public LanguageInclusionChecker createLanguageInclusionChecker
    (ProductDESProxyFactory factory);

}
