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

package net.sourceforge.waters.analysis.modular;

import net.sourceforge.waters.model.analysis.des.AbstractModelAnalyzerFactory;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzerFactoryLoader;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.options.AnalysisOptionPage;
import net.sourceforge.waters.model.options.ChainedAnalyzerOption;


/**
 * A factory that produces modular model verifiers based on event slicing.
 * This includes controllability and language inclusion check considering
 * one uncontrollable event at a time and coobservability check considering
 * one relevant site set at a time.
 *
 * @author Robi Malik
 */

public class SlicingModelVerifierFactory
  extends AbstractModelAnalyzerFactory
{

  //#########################################################################
  //# Constructors
  public SlicingModelVerifierFactory()
  {
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelVerifierFactory
  @Override
  public SlicingControllabilityChecker createControllabilityChecker
    (final ProductDESProxyFactory factory)
  {
    final ModularControllabilityChecker modular =
      ModularModelVerifierFactory.getInstance().
      createControllabilityChecker(factory);
    return new SlicingControllabilityChecker(factory, modular);
  }

  @Override
  public SlicingCoobservabilityChecker createCoobservabilityChecker
    (final ProductDESProxyFactory factory)
  {
    final ModularCoobservabilityChecker modular =
      ModularModelVerifierFactory.getInstance().
      createCoobservabilityChecker(factory);
    return new SlicingCoobservabilityChecker(factory, modular);
  }

  @Override
  public SlicingLanguageInclusionChecker createLanguageInclusionChecker
    (final ProductDESProxyFactory factory)
  {
    final ModularLanguageInclusionChecker modular =
      ModularModelVerifierFactory.getInstance().
      createLanguageInclusionChecker(factory);
    return new SlicingLanguageInclusionChecker(factory, modular);
  }

  @Override
  public void registerOptions(final AnalysisOptionPage db)
  {
    super.registerOptions(db);

    db.register(new ChainedAnalyzerOption
             (OPTION_SlicingControllabilityChecker_Chain,
              "Nested controllability checker",
              "Algorithm to perform the separate controllability check " +
              "for each uncontrollable event.",
              db, ModelAnalyzerFactoryLoader.Slicing,
              ModelAnalyzerFactoryLoader.Modular, CHAIN_SUPPRESSIONS));
    db.register(new ChainedAnalyzerOption
             (OPTION_SlicingCoobservabilityChecker_Chain,
              "Nested coobservability checker",
              "Algorithm to perform the separate language inclusion check " +
              "for each event.",
              db, ModelAnalyzerFactoryLoader.Slicing,
              ModelAnalyzerFactoryLoader.Modular, CHAIN_SUPPRESSIONS));
    db.register(new ChainedAnalyzerOption
             (OPTION_SlicingLanguageInclusionChecker_Chain,
              "Nested language inclusion checker",
              "Algorithm to perform the separate coobservability check " +
              "for each relevant site group.",
              db, ModelAnalyzerFactoryLoader.Slicing,
              ModelAnalyzerFactoryLoader.Modular, CHAIN_SUPPRESSIONS));
  }


  //#########################################################################
  //# Factory Instantiation
  public static SlicingModelVerifierFactory getInstance()
  {
    if (theInstance == null) {
      theInstance = new SlicingModelVerifierFactory();
    }
    return theInstance;
  }


  //#########################################################################
  //# Class Variables
  private static SlicingModelVerifierFactory theInstance = null;


  //#########################################################################
  //# Class Constants
  public static final String
    OPTION_SlicingControllabilityChecker_Chain =
    "SlicingControllabilityChecker.chain";
  public static final String
    OPTION_SlicingCoobservabilityChecker_Chain =
    "SlicingCoobservabilityChecker.chain";
  public static final String
    OPTION_SlicingLanguageInclusionChecker_Chain =
    "SlicingLanguageInclusionChecker.chain";

}
