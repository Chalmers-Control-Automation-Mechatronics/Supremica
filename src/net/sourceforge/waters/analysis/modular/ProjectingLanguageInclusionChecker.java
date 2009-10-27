//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.monolothic
//# CLASS:   ProjectingLanguageInclusionChecker
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.modular;

import net.sourceforge.waters.model.analysis.LanguageInclusionChecker;
import net.sourceforge.waters.model.analysis.LanguageInclusionKindTranslator;
import net.sourceforge.waters.model.analysis.SafetyVerifier;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * @author Robi Malik
 */

public class ProjectingLanguageInclusionChecker
  extends ProjectingSafetyVerifier
  implements LanguageInclusionChecker
{

  //#########################################################################
  //# Constructors
  public ProjectingLanguageInclusionChecker
    (final ProductDESProxyFactory factory,
     final SafetyVerifier checker)
  {
    this(null, factory, checker);
  }

  public ProjectingLanguageInclusionChecker
    (final ProductDESProxy model,
     final ProductDESProxyFactory factory,
     final SafetyVerifier checker)
  {
    super(model, LanguageInclusionKindTranslator.getInstance(),
          factory, checker);
  }

  public ProjectingLanguageInclusionChecker
    (final ProductDESProxyFactory factory,
     final SafetyVerifier checker,
     final int projsize)
  {
    this(null, factory, checker, projsize);
  }

  public ProjectingLanguageInclusionChecker
    (final ProductDESProxy model,
     final ProductDESProxyFactory factory,
     final SafetyVerifier checker,
     final int projsize)
  {
    super(model, LanguageInclusionKindTranslator.getInstance(),
          factory, checker, projsize);
  }

}
