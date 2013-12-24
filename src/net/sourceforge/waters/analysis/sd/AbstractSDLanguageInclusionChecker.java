//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters SD
//# PACKAGE: net.sourceforge.waters.analysis.sd
//# CLASS:   AbstractSDLanguageInclusionChecker
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.sd;

import java.util.Collection;

import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.AnalysisResult;
import net.sourceforge.waters.model.analysis.LanguageInclusionKindTranslator;
import net.sourceforge.waters.model.analysis.des.AbstractSafetyVerifier;
import net.sourceforge.waters.model.analysis.des.LanguageInclusionChecker;
import net.sourceforge.waters.model.analysis.des.LanguageInclusionDiagnostics;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;

import org.apache.log4j.Logger;


/**
 * <P>An abstract base class for model verifier to check sample-data (SD)
 * properties that are based on a language inclusion check.</P>
 *
 * <P>The abstract base class provides the common configuration option to
 * configure the underlying language inclusion checker needed for different
 * SD verification tasks.</P>
 *
 * @author Mahvash Baloch, Robi Malik
 */

abstract public class AbstractSDLanguageInclusionChecker
  extends AbstractSafetyVerifier
{

  //#########################################################################
  //# Constructors
  public AbstractSDLanguageInclusionChecker
    (final ProductDESProxyFactory factory)
  {
    this(null, factory);
  }

  public AbstractSDLanguageInclusionChecker
    (final LanguageInclusionChecker checker,
     final ProductDESProxyFactory factory)
  {
    this(checker, null, factory);
  }

  public AbstractSDLanguageInclusionChecker
    (final LanguageInclusionChecker checker,
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

  @Override
  public boolean supportsNondeterminism()
  {
    return false;
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.analysis.AbstractModelAnalyser
  @Override
  protected void setUp() throws AnalysisException
  {
    super.setUp();
    mFirstResult = true;
    mHibIndex = 0;
  }


  //#########################################################################
  //# Auxiliary Methods
  void recordStatistics(final AnalysisResult result)
  {
    if (mFirstResult) {
      setAnalysisResult(result);
      mFirstResult = false;
    } else {
      final AnalysisResult present = getAnalysisResult();
      final int numaut1 = present.getTotalNumberOfAutomata();
      final int numaut2 = result.getTotalNumberOfAutomata();
      final int numaut = Math.max(numaut1, numaut2);
      present.merge(result);
      present.setNumberOfAutomata(numaut);
    }
  }


  //#########################################################################
  //# Logging
  void logHibEvent(final EventProxy hib, final Collection<EventProxy> hibs)
  {
    final Logger logger = getLogger();
    if (logger.isDebugEnabled()) {
      mHibIndex++;
      logger.debug("Prohibitable event " + hib.getName() +
                   " (" + mHibIndex + "/" + hibs.size() + ")");
    }
  }


  //#########################################################################
  //# Data Members
  private LanguageInclusionChecker mChecker;
  private boolean mFirstResult;
  private int mHibIndex;

}
