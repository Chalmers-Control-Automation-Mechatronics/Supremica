//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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

package net.sourceforge.waters.analysis.monolithic;

import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.model.analysis.des.AbstractModelAnalyzerFactory;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.options.AnalysisOptionPage;
import net.sourceforge.waters.model.options.EnumOption;


/**
 * A model analyser factory that produces monolithic implementations
 * of analysis algorithms that are based on {@link TRAutomatonProxy}.
 *
 * @author Robi Malik
 */

public class TRMonolithicModelAnalyzerFactory
  extends AbstractModelAnalyzerFactory
{

  //#########################################################################
  //# Singleton Pattern
  public static TRMonolithicModelAnalyzerFactory getInstance()
  {
    return SingletonHolder.INSTANCE;
  }

  private static class SingletonHolder {
    private static final TRMonolithicModelAnalyzerFactory INSTANCE =
      new TRMonolithicModelAnalyzerFactory();
  }


  //#########################################################################
  //# Constructors
  private TRMonolithicModelAnalyzerFactory()
  {
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelAnalyzerFactory
  @Override
  public TRMonolithicControllabilityChecker createControllabilityChecker
    (final ProductDESProxyFactory factory)
  {
    return new TRMonolithicControllabilityChecker();
  }

  @Override
  public TRMonolithicCoobservabilityChecker createCoobservabilityChecker
    (final ProductDESProxyFactory factory)
  {
    return new TRMonolithicCoobservabilityChecker();
  }

  @Override
  public TRMonolithicDeadlockChecker createDeadlockChecker
    (final ProductDESProxyFactory factory)
  {
    return new TRMonolithicDeadlockChecker();
  }

  @Override
  public TRMonolithicLanguageInclusionChecker createLanguageInclusionChecker
    (final ProductDESProxyFactory factory)
  {
    return new TRMonolithicLanguageInclusionChecker();
  }

  @Override
  public TRSynchronousProductBuilder createSynchronousProductBuilder
    (final ProductDESProxyFactory factory)
  {
    return new TRSynchronousProductBuilder();
  }


  @Override
  public void registerOptions(final AnalysisOptionPage db)
  {
    super.registerOptions(db);
    db.register(new EnumOption<AbstractTRMonolithicModelVerifier.TraceMode>
             (OPTION_AbstractTRMonolithicModelVerifier_TraceMode,
              "Counterexample computation mode",
              AbstractTRMonolithicModelVerifier.TraceMode.STORED +
              " is fast but requires 4 additional bytes per state; " +
              AbstractTRMonolithicModelVerifier.TraceMode.DEPTHMAP +
              " uses little extra memory but has linear runtime overhead; " +
              AbstractTRMonolithicModelVerifier.TraceMode.REVERSE +
              " uses no extra memory with fast runtime on average, " +
              "but has a possible exponential worst case.",
              "-tm",
              AbstractTRMonolithicModelVerifier.TraceMode.values(),
              AbstractTRMonolithicModelVerifier.TraceMode.STORED));
  }


  //#########################################################################
  //# Class Constants
  public static final String
    OPTION_AbstractTRMonolithicModelVerifier_TraceMode =
    "AbstractTRMonolithicModelVerifier.TraceMode";
}
