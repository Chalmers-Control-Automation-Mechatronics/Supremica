//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.bdd
//# CLASS:   BDDLanguageInclusionChecker
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.bdd;

import net.sourceforge.waters.model.analysis.LanguageInclusionChecker;
import net.sourceforge.waters.model.analysis.LanguageInclusionKindTranslator;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * @author Robi Malik
 */

public class BDDLanguageInclusionChecker
  extends BDDSafetyVerifier
  implements LanguageInclusionChecker
{

  //#########################################################################
  //# Constructors
  public BDDLanguageInclusionChecker
    (final ProductDESProxyFactory factory)
  {
    this(null, factory);
  }

  public BDDLanguageInclusionChecker
    (final ProductDESProxy model,
     final ProductDESProxyFactory factory)
  {
    super(model, LanguageInclusionKindTranslator.getInstance(), factory);
  }

}
