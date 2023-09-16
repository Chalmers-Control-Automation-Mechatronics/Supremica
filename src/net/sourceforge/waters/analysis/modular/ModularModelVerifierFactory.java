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

import net.sourceforge.waters.analysis.monolithic.TRMonolithicCoobservabilityChecker;
import net.sourceforge.waters.cpp.analysis.NativeControllabilityChecker;
import net.sourceforge.waters.cpp.analysis.NativeLanguageInclusionChecker;
import net.sourceforge.waters.model.analysis.des.AbstractModelAnalyzerFactory;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzerFactoryLoader;
import net.sourceforge.waters.model.analysis.des.SupervisorSynthesizer;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.options.AnalysisOptionPage;
import net.sourceforge.waters.model.options.BooleanOption;
import net.sourceforge.waters.model.options.ChainedAnalyzerOption;
import net.sourceforge.waters.model.options.EnumOption;


/**
 * A factory that produces modular/incremental model verifiers.
 *
 * @author Robi Malik, Andrew Holland
 */

public class ModularModelVerifierFactory
  extends AbstractModelAnalyzerFactory
{

  //#########################################################################
  //# Constructors
  public ModularModelVerifierFactory()
  {
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelVerifierFactory
  @Override
  public ModularControllabilityChecker createControllabilityChecker
    (final ProductDESProxyFactory factory)
  {
    final NativeControllabilityChecker mono =
      new NativeControllabilityChecker(factory);
    return new ModularControllabilityChecker(factory, mono);
  }

  @Override
  public ModularControlLoopChecker createControlLoopChecker
    (final ProductDESProxyFactory factory)
  {
    return new ModularControlLoopChecker(factory);
  }

  @Override
  public ModularCoobservabilityChecker createCoobservabilityChecker
    (final ProductDESProxyFactory factory)
  {
    final TRMonolithicCoobservabilityChecker mono =
      new TRMonolithicCoobservabilityChecker();
    return new ModularCoobservabilityChecker(factory, mono);
  }

  @Override
  public ModularLanguageInclusionChecker createLanguageInclusionChecker
    (final ProductDESProxyFactory factory)
  {
    final NativeLanguageInclusionChecker mono =
      new NativeLanguageInclusionChecker(factory);
    return new ModularLanguageInclusionChecker(factory, mono);
  }

  @Override
  public SupervisorSynthesizer createSupervisorSynthesizer
    (final ProductDESProxyFactory factory)
  {
    return new ModularControllabilitySynthesizer(factory);
  }


  @Override
  public void registerOptions(final AnalysisOptionPage db)
  {
    super.registerOptions(db);
    db.register(new EnumOption<HeuristicFactory.Method>
             (OPTION_AbstractModularVerifier_HeuristicMethod,
              "Heuristic Method",
              "Strategy to select additional components when a subsystem " +
              "fails the check during modular verification.",
              "-heuristic",
              HeuristicFactory.Method.values(),
              HeuristicFactory.Method.MaxCommonEvents));
    db.register(new EnumOption<HeuristicFactory.Preference>
             (OPTION_AbstractModularVerifier_HeuristicPreference,
              "Heuristic Preference",
              "What kind of plants are selected preferentially by the heuristic.",
              "-preference",
              HeuristicFactory.Preference.values(),
              HeuristicFactory.Preference.NOPREF));
    db.register(new BooleanOption
             (OPTION_AbstractModularVerifier_CollectsFailedSpecs,
              "Collect failed specifications",
              "Continue checking if a specification is found to fail verification.",
              "-collect",
              false));
    db.register(new BooleanOption
             (OPTION_AbstractModularVerifier_StartsWithSmallestSpec,
              "Start with smallest spefication",
              "Enable to process specifications in order of increasing " +
              "number of states, disable to process them in order of " +
              "decreasing number of states.",
              "-so",
              true));

    db.register(new ChainedAnalyzerOption
             (OPTION_ModularControllabilityChecker_Chain,
              "Monolithic controllability checker",
              "Algorithm used to analyze the subsystems during modular " +
              "or incremental processing.",
              db, ModelAnalyzerFactoryLoader.Modular, CHAIN_SUPPRESSIONS));

    db.register(new ChainedAnalyzerOption
             (OPTION_ModularCoobservabilityChecker_Chain,
              "Monolithic coobservability checker",
              "Algorithm used to analyze the subsystems during modular " +
              "or incremental processing.",
              db, ModelAnalyzerFactoryLoader.Modular,
              ModelAnalyzerFactoryLoader.TRMonolithic, CHAIN_SUPPRESSIONS));

    db.register(new BooleanOption
             (OPTION_ModularControllabilitySynthesizer_NonblockingSynthesis,
              "Locally nonblocking supervisors",
              "Attempt to synthesise nonblocking supervisors each time a subsystem " +
              "is sent for monolithic synthesis. While this may help to remove some " +
              "blocking states, it does not ensure a globally nonblocking supervisor.",
              "-nb",
              false));
    db.register(new BooleanOption
             (OPTION_ModularControllabilitySynthesizer_RemovingUnnecessarySupervisors,
              "Remove unnecessary supervisors",
              "Check whether new superivsors impose additional constraints over " +
              "those previously computed, and remove those that do not.",
              "-remove",
              true));

    db.register(new EnumOption<AutomataGroup.MergeVersion>
             (OPTION_ModularControlLoopChecker_MergeVersion,
              "Selection heuristic",
              "The heuristic to determine which components to include in " +
              "subsequent verification attempts based on the counterexample " +
              "from the previous attempt.",
              "-merge",
              AutomataGroup.MergeVersion.values(),
              AutomataGroup.MergeVersion.MaxCommonEvents));
    db.register(new EnumOption<AutomataGroup.SelectVersion>
             (OPTION_ModularControlLoopChecker_SelectVersion,
              "Select Version",
              "Method used to select the primary automaton for merging",
              "-select",
              AutomataGroup.SelectVersion.values(),
              AutomataGroup.SelectVersion.Naive));

    db.register(new ChainedAnalyzerOption
             (OPTION_ModularLanguageInclusionChecker_Chain,
              "Monolithic language inclusion checker",
              "Algorithm used to analyze the subsystems during modular " +
              "or incremental processing.",
              db, ModelAnalyzerFactoryLoader.Modular, CHAIN_SUPPRESSIONS));
  }


  //#########################################################################
  //# Factory Instantiation
  public static ModularModelVerifierFactory getInstance()
  {
    if (theInstance == null) {
      theInstance = new ModularModelVerifierFactory();
    }
    return theInstance;
  }


  //#########################################################################
  //# Class Variables
  private static ModularModelVerifierFactory theInstance = null;


  //#########################################################################
  //# Class Constants
  public static final String
    OPTION_AbstractModularVerifier_HeuristicMethod =
    "AbstractModularSafetyVerifier.HeuristicMethod"; // legacy name
  public static final String
    OPTION_AbstractModularVerifier_HeuristicPreference =
    "AbstractModularSafetyVerifier.HeuristicPreference"; // legacy name
  public static final String
    OPTION_AbstractModularVerifier_StartsWithSmallestSpec =
    "ModularControllabilityChecker.StartsWithSmallestSpec"; // legacy name
  public static final String
    OPTION_AbstractModularVerifier_CollectsFailedSpecs =
    "ModularControllabilityChecker.CollectsFailedSpecs"; // legacy name

  public static final String
    OPTION_ModularControllabilityChecker_Chain =
    "ModularControllabilityChecker.chain";

  public static final String
    OPTION_ModularCoobservabilityChecker_Chain =
    "ModularCoobservabilityChecker.chain";

  public static final String
    OPTION_ModularControllabilitySynthesizer_NonblockingSynthesis =
    "ModularControllabilitySynthesizer.NonblockingSynthesis";
  public static final String
    OPTION_ModularControllabilitySynthesizer_RemovingUnnecessarySupervisors =
    "ModularControllabilitySynthesizer.RemovingUnnecessarySupervisors";

  public static final String
    OPTION_ModularControlLoopChecker_MergeVersion =
    "ModularControlLoopChecker.MergeVersion";
  public static final String
    OPTION_ModularControlLoopChecker_SelectVersion =
    "ModularControlLoopChecker.SelectVersion";

  public static final String
    OPTION_ModularLanguageInclusionChecker_Chain =
    "ModularLanguageInclusionChecker.chain";

}
