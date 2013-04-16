//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.cpp.compositional
//# CLASS:   NativeLanguageInclusionChecker
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.compositional;

import net.sourceforge.waters.model.analysis.LanguageInclusionKindTranslator;
import net.sourceforge.waters.model.analysis.des.LanguageInclusionChecker;
import net.sourceforge.waters.model.analysis.des.LanguageInclusionDiagnostics;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * <P>A compositional language inclusion checker implementation using
 * natural projection.</P>
 *
 * @author Robi Malik
 */

public class CompositionalLanguageInclusionChecker
  extends CompositionalSafetyVerifier
  implements LanguageInclusionChecker
{

  //#########################################################################
  //# Constructors
  public CompositionalLanguageInclusionChecker
    (final ProductDESProxyFactory factory)
  {
    this(null, factory);
  }

  public CompositionalLanguageInclusionChecker
    (final ProductDESProxy model,
     final ProductDESProxyFactory factory)
  {
    super(model,
          factory,
          LanguageInclusionKindTranslator.getInstance(),
          LanguageInclusionDiagnostics.getInstance());
  }

}
