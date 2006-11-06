//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.monolothic
//# CLASS:   MonolithicLanguageInclusionChecker
//###########################################################################
//# $Id: MonolithicLanguageInclusionChecker.java,v 1.1 2006-11-06 03:23:35 robi Exp $
//###########################################################################

package net.sourceforge.waters.analysis.monolithic;

import net.sourceforge.waters.model.analysis.LanguageInclusionChecker;
import net.sourceforge.waters.model.analysis.LanguageInclusionKindTranslator;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * @author Robi Malik
 */

public class MonolithicLanguageInclusionChecker
  extends MonolithicSafetyVerifier
  implements LanguageInclusionChecker
{

  //#########################################################################
  //# Constructors
  public MonolithicLanguageInclusionChecker
    (final ProductDESProxyFactory factory)
  {
    this(null, factory);
  }

  public MonolithicLanguageInclusionChecker
    (final ProductDESProxy model,
     final ProductDESProxyFactory factory)
  {
    super(model, LanguageInclusionKindTranslator.getInstance(), factory);
  }

}
