//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters SD
//# PACKAGE: net.sourceforge.waters.analysis.sd
//# CLASS:   AbstractSDLanguageInclusionChecker
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.sd;

import net.sourceforge.waters.model.analysis.LanguageInclusionKindTranslator;
import net.sourceforge.waters.model.analysis.des.AbstractSafetyVerifier;
import net.sourceforge.waters.model.analysis.des.LanguageInclusionChecker;
import net.sourceforge.waters.model.analysis.des.LanguageInclusionDiagnostics;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * An abstract base class for model verifier to check sample-data (SD)
 * properties that are based on a language inclusion check.
 *
 * The abstract base class provides the common configuration option to
 * configure the underlying language inclusion checker needed for different
 * SD verification tasks.
 *
 * @author Mahvash Baloch, Robi Malik
 */

abstract public class AbstractSDLanguageInclusionChecker
  extends AbstractSafetyVerifier
{

  //#########################################################################
  //# Constructors
  public AbstractSDLanguageInclusionChecker(final ProductDESProxyFactory factory)
  {
    this(null, factory);
  }

  public AbstractSDLanguageInclusionChecker(final LanguageInclusionChecker checker,
                                            final ProductDESProxyFactory factory)
  {
    this(checker, null, factory);
  }

  public AbstractSDLanguageInclusionChecker(final LanguageInclusionChecker checker,
                                            final ProductDESProxy model,
                                            final ProductDESProxyFactory factory)
  {
    super(model,
          LanguageInclusionKindTranslator.getInstance(),
          LanguageInclusionDiagnostics.getInstance(),
          factory);
    mChecker = checker;
  }


  //#########################################################################
  //# Configuration
  public LanguageInclusionChecker getLanguageInclusionChecker()
  {
    return mChecker;
  }

  public void setLanguageInclusionChecker(final LanguageInclusionChecker checker)
  {
    mChecker = checker;
  }


  //#########################################################################
  //# Data Members
  private LanguageInclusionChecker mChecker;

}
