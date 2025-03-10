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

import net.sourceforge.waters.analysis.diagnosis.MonolithicDiagnosabilityVerifier;
import net.sourceforge.waters.model.analysis.AnalysisConfigurationException;
import net.sourceforge.waters.model.analysis.des.AbstractModelAnalyzerFactory;
import net.sourceforge.waters.model.analysis.des.DiagnosabilityChecker;
import net.sourceforge.waters.model.analysis.des.StateCounter;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.options.AnalysisOptionPage;
import net.sourceforge.waters.model.options.BooleanOption;


/**
 * A model analyser factory that produces basic monolithic implementations
 * of all analysis algorithms.
 *
 * @author Robi Malik
 */

public class MonolithicModelAnalyzerFactory
  extends AbstractModelAnalyzerFactory
{

  //#########################################################################
  //# Singleton Pattern
  public static MonolithicModelAnalyzerFactory getInstance()
  {
    return SingletonHolder.INSTANCE;
  }

  private static class SingletonHolder {
    private static final MonolithicModelAnalyzerFactory INSTANCE =
      new MonolithicModelAnalyzerFactory();
  }


  //#########################################################################
  //# Constructors
  private MonolithicModelAnalyzerFactory()
  {
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelVerifierFactory
  @Override
  public MonolithicConflictChecker createConflictChecker
    (final ProductDESProxyFactory factory)
  {
    return new MonolithicConflictChecker(factory);
  }

  @Override
  public MonolithicControllabilityChecker createControllabilityChecker
    (final ProductDESProxyFactory factory)
  {
    return new MonolithicControllabilityChecker(factory);
  }

  @Override
  public MonolithicControlLoopChecker createControlLoopChecker
    (final ProductDESProxyFactory factory)
  {
    return new MonolithicControlLoopChecker(factory);
  }

  @Override
  public TRMonolithicDeadlockChecker createDeadlockChecker
    (final ProductDESProxyFactory factory)
  {
    return new TRMonolithicDeadlockChecker();
  }

  @Override
  public DiagnosabilityChecker createDiagnosabilityChecker
    (final ProductDESProxyFactory factory)
    throws AnalysisConfigurationException
  {
    return new MonolithicDiagnosabilityVerifier(factory);
  }

  @Override
  public MonolithicLanguageInclusionChecker createLanguageInclusionChecker
    (final ProductDESProxyFactory factory)
  {
    return new MonolithicLanguageInclusionChecker(factory);
  }

  @Override
  public StateCounter createStateCounter
    (final ProductDESProxyFactory factory)
  {
    return new TRStateCounter();
  }

  @Override
  public MonolithicSynchronousProductBuilder createSynchronousProductBuilder
    (final ProductDESProxyFactory factory)
  {
    return new MonolithicSynchronousProductBuilder(factory);
  }

  @Override
  public MonolithicSynthesizer createSupervisorSynthesizer
    (final ProductDESProxyFactory factory)
  {
    return new MonolithicSynthesizer(factory);
  }


  @Override
  public void registerOptions(final AnalysisOptionPage db)
  {
    super.registerOptions(db);
    db.register(new BooleanOption
             (OPTION_MonolithicSynchronousProductBuilder_PruningDeadlocks,
              "Prune deadlocks",
              "Stop synchronous product construction when encountering " +
              "states that are a deadlock in one of the components.",
              "-prune",
              false));
  }


  //#########################################################################
  //# Class Constants
  public static final String OPTION_MonolithicSynchronousProductBuilder_PruningDeadlocks =
    "MonolithicSynchronousProductBuilder.PruningDeadlocks";

}
