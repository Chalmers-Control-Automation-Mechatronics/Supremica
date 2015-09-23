//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.analysis.sd;

import java.util.Collection;

import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.AnalysisResult;
import net.sourceforge.waters.model.analysis.LanguageInclusionKindTranslator;
import net.sourceforge.waters.model.analysis.VerificationResult;
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
  boolean runChecker(final ProductDESProxy des)
    throws AnalysisException
  {
    mChecker.setModel(des);
    try {
      return mChecker.run();
    } catch (final AnalysisException exception) {
      final VerificationResult result = getAnalysisResult();
      result.setException(exception);
      throw exception;
    } finally {
      final AnalysisResult result = mChecker.getAnalysisResult();
      recordStatistics(result);
    }
  }

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
