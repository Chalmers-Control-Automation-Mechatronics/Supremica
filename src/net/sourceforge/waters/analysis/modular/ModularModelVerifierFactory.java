//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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

import net.sourceforge.waters.analysis.options.BooleanOption;
import net.sourceforge.waters.analysis.options.EnumOption;
import net.sourceforge.waters.analysis.options.OptionMap;
import net.sourceforge.waters.cpp.analysis.NativeControllabilityChecker;
import net.sourceforge.waters.cpp.analysis.NativeLanguageInclusionChecker;
import net.sourceforge.waters.model.analysis.AnalysisConfigurationException;
import net.sourceforge.waters.model.analysis.CommandLineArgumentChain;
import net.sourceforge.waters.model.analysis.CommandLineArgumentEnum;
import net.sourceforge.waters.model.analysis.des.AbstractModelAnalyzerFactory;
import net.sourceforge.waters.model.analysis.des.ModelVerifier;
import net.sourceforge.waters.model.analysis.des.SafetyVerifier;
import net.sourceforge.waters.model.analysis.des.SupervisorSynthesizer;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


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
  //# Overrides for
  //# net.sourceforge.waters.model.analysis.AbstractModelVerifierFactory
  @Override
  protected void addArguments()
  {
    super.addArguments();
    addArgument(new SecondaryFactoryArgument());
    addArgument(ModularHeuristicFactory.getMethodArgument());
    addArgument(ModularHeuristicFactory.getPreferenceArgument());
    addArgument(new MergeVersionAgument());
    addArgument(new SelectVersionArgument());
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelVerifierFactory
  @Override
  public ModularControllabilityChecker createControllabilityChecker
    (final ProductDESProxyFactory factory)
  {
    return new ModularControllabilityChecker
      (null, factory, new NativeControllabilityChecker(factory));
  }

  @Override
  public ModularControlLoopChecker createControlLoopChecker
    (final ProductDESProxyFactory factory)
  {
    return new ModularControlLoopChecker(factory);
  }

  @Override
  public ModularLanguageInclusionChecker createLanguageInclusionChecker
    (final ProductDESProxyFactory factory)
  {
    final SafetyVerifier mono =
      new NativeLanguageInclusionChecker(factory);
    return new ModularLanguageInclusionChecker(null, factory, mono);
  }

  @Override
  public SupervisorSynthesizer createSupervisorSynthesizer
    (final ProductDESProxyFactory factory)
  {
    return new ModularControllabilitySynthesizer(factory);
  }


  @Override
  public void registerOptions(final OptionMap db)
  {
    super.registerOptions(db);
    db.add(new EnumOption<ModularHeuristicFactory.Method>
             (OPTION_AbstractModularSafetyVerifier_HeuristicMethod,
              "Heuristic Method",
              "Strategy to select additional components when a subsystem " +
              "fails the check during modular verification.",
              "-heuristic",
              ModularHeuristicFactory.Method.values(),
              ModularHeuristicFactory.Method.MaxCommonEvents));
    db.add(new EnumOption<ModularHeuristicFactory.Preference>
             (OPTION_AbstractModularSafetyVerifier_HeuristicPreference,
              "Heuristic Preference",
              "What kind of plants are selected preferentially by the heuristic.",
              "-preference",
              ModularHeuristicFactory.Preference.values(),
              ModularHeuristicFactory.Preference.NOPREF));

    db.add(new BooleanOption
             (OPTION_ModularControllabilityChecker_CollectsFailedSpecs,
              "Collect failed specifications",
              "Continue checking if a specification is found not controllable.",
              "-collect",
              false));
    db.add(new BooleanOption
             (OPTION_ModularControllabilityChecker_StartsWithSmallestSpec,
              "Start with smallest spefication",
              "Sort the specifications by number of states, and start " +
              "with the smallest.",
              "-so",
              true));

    db.add(new BooleanOption
             (OPTION_ModularControllabilitySynthesizer_NonblockingSynthesis,
              "Locally nonblocking supervisors",
              "Attempt to synthesise nonblocking supervisors each time a subsystem " +
              "is sent for monolithic synthesis. While this may help to remove some " +
              "blocking states, it does not ensure a globally nonblocking supervisor.",
              "-nb",
              false));
    db.add(new BooleanOption
             (OPTION_ModularControllabilitySynthesizer_RemovingUnnecessarySupervisors,
              "Remove unnecessary supervisors",
              "Check whether new superivsors impose additional constraints over " +
              "those previously computed, and remove those that do not.",
              "-remove",
              true));

    db.add(new EnumOption<AutomataGroup.MergeVersion>
             (OPTION_ModularControlLoopChecker_MergeVersion,
              "Selection heuristic",
              "The heuristic to determine which components to include in " +
              "subsequent verification attempts based on the counterexample " +
              "from the previous attempt.",
              "-heuristic",
              AutomataGroup.MergeVersion.values(),
              AutomataGroup.MergeVersion.MaxCommonEvents));
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
  //# Inner Class SecondaryFactoryArgyment
  private static class SecondaryFactoryArgument
    extends CommandLineArgumentChain
  {
    //#######################################################################
    //# Constructors
    private SecondaryFactoryArgument()
    {
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    @Override
    public void configureAnalyzer(final Object analyzer)
      throws AnalysisConfigurationException
    {
      if (analyzer instanceof AbstractModularSafetyVerifier) {
        final ModelVerifier verifier = (ModelVerifier) analyzer;
        final AbstractModularSafetyVerifier modular =
          (AbstractModularSafetyVerifier) verifier;
        final SafetyVerifier secondaryVerifier =
          (SafetyVerifier) createSecondaryAnalyzer(verifier);
        modular.setMonolithicVerifier(secondaryVerifier);
      } else {
        failUnsupportedAnalyzerClass(analyzer);
      }
    }
  }


  //#########################################################################
  //# Inner Class MergeVersionAgument
  private static class MergeVersionAgument
    extends CommandLineArgumentEnum<AutomataGroup.MergeVersion>
  {
    //#######################################################################
    //# Constructors
    private MergeVersionAgument()
    {
      super("-merge", "Method used to select the secondary automaton for merging",
            AutomataGroup.MergeVersion.class);
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    @Override
    public void configureAnalyzer(final Object verifier)
    {
      final AutomataGroup.MergeVersion method = getValue();
      if (verifier instanceof ModularControlLoopChecker) {
        final ModularControlLoopChecker checker =
          (ModularControlLoopChecker) verifier;
        checker.setMergeVersion(method);
      } else {
        fail(getName() + " option only supported for modular control loop checker!");
      }
    }
  }


  //#########################################################################
  //# Inner Class SelectVersionArgument
  private static class SelectVersionArgument
  extends CommandLineArgumentEnum<AutomataGroup.SelectVersion>
  {
    //#######################################################################
    //# Constructors
    private SelectVersionArgument()
    {
      super("-select", "Method used to select the primary automaton for merging",
            AutomataGroup.SelectVersion.class);
    }

    //#######################################################################
    //# Overrides for Abstract Base Class
    //# net.sourceforge.waters.model.analysis.CommandLineArgument
    @Override
    public void configureAnalyzer(final Object verifier)
    {
      final AutomataGroup.SelectVersion method = getValue();
      if (verifier instanceof ModularControlLoopChecker) {
        final ModularControlLoopChecker checker =
          (ModularControlLoopChecker) verifier;
        checker.setSelectVersion(method);
      } else {
        fail(getName() + " option only supported for modular control loop checker!");
      }
    }
  }


  //#########################################################################
  //# Class Variables
  private static ModularModelVerifierFactory theInstance = null;


  //#########################################################################
  //# Class Constants
  public static final String
    OPTION_AbstractModularSafetyVerifier_HeuristicMethod =
    "AbstractModularSafetyVerifier.HeuristicMethod";
  public static final String
    OPTION_AbstractModularSafetyVerifier_HeuristicPreference =
    "AbstractModularSafetyVerifier.HeuristicPreference";

  public static final String
    OPTION_ModularControllabilityChecker_CollectsFailedSpecs =
    "ModularControllabilityChecker.CollectsFailedSpecs";
  public static final String
    OPTION_ModularControllabilityChecker_StartsWithSmallestSpec =
    "ModularControllabilityChecker.StartsWithSmallestSpec";

  public static final String
    OPTION_ModularControllabilitySynthesizer_NonblockingSynthesis =
    "ModularControllabilitySynthesizer.NonblockingSynthesis";
  public static final String
    OPTION_ModularControllabilitySynthesizer_RemovingUnnecessarySupervisors =
    "ModularControllabilitySynthesizer.RemovingUnnecessarySupervisors";

  public static final String
    OPTION_ModularControlLoopChecker_MergeVersion =
    "ModularControlLoopChecker.MergeVersion";

}
