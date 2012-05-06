
//###########################################################################

package net.sourceforge.waters.analysis.sd;

import net.sourceforge.waters.model.analysis.AbstractSafetyVerifier;
import net.sourceforge.waters.model.analysis.LanguageInclusionChecker;
import net.sourceforge.waters.model.analysis.LanguageInclusionDiagnostics;
import net.sourceforge.waters.model.analysis.LanguageInclusionKindTranslator;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * An abstract base class for model verifier to check SIC or LDIC Properties
 * of HISC models that are based on a conflict check.
 *
 * The abstract base class provides the common configuration option to
 * configure the underlying conflict checker needed for different SIC
 * verification tasks.
 *
 * @author Robi Malik
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
