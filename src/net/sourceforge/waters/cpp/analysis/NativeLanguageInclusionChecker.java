//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.cpp.analysis
//# CLASS:   NativeLanguageInclusionChecker
//###########################################################################
//# $Id: NativeLanguageInclusionChecker.java,v 1.2 2006-11-03 05:18:28 robi Exp $
//###########################################################################

package net.sourceforge.waters.cpp.analysis;

import net.sourceforge.waters.model.analysis.LanguageInclusionChecker;
import net.sourceforge.waters.model.analysis.LanguageInclusionKindTranslator;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * @author Robi Malik
 */

public class NativeLanguageInclusionChecker
  extends NativeSafetyVerifier
  implements LanguageInclusionChecker
{

  //#########################################################################
  //# Constructors
  public NativeLanguageInclusionChecker(final ProductDESProxyFactory factory)
  {
    this(null, factory);
  }

  public NativeLanguageInclusionChecker(final ProductDESProxy model,
                                        final ProductDESProxyFactory factory)
  {
    super(model, LanguageInclusionKindTranslator.getInstance(), factory);
  }

}
