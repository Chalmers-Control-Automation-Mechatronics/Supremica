//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
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

package net.sourceforge.waters.analysis.compositional;

import net.sourceforge.waters.analysis.compositional.AbstractCompositionalModelAnalyzer.PreselectingMethod;
import net.sourceforge.waters.analysis.options.BooleanOption;
import net.sourceforge.waters.analysis.options.EnumOption;
import net.sourceforge.waters.analysis.options.FileOption;
import net.sourceforge.waters.analysis.options.OptionPage;
import net.sourceforge.waters.analysis.options.PositiveIntOption;
import net.sourceforge.waters.model.analysis.des.AbstractModelAnalyzerFactory;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * A factory that produces compositional model verifiers.
 *
 * @author Robi Malik
 */

public class CompositionalModelAnalyzerFactory
  extends AbstractModelAnalyzerFactory
{

  //#########################################################################
  //# Singleton Pattern
  public static CompositionalModelAnalyzerFactory getInstance()
  {
    return SingletonHolder.INSTANCE;
  }

  private static class SingletonHolder {
    private static final CompositionalModelAnalyzerFactory INSTANCE =
      new CompositionalModelAnalyzerFactory();
  }


  //#########################################################################
  //# Constructors
  CompositionalModelAnalyzerFactory()
  {
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelAnalyzerFactory
  @Override
  public CompositionalConflictChecker createConflictChecker
    (final ProductDESProxyFactory factory)
  {
    return new CompositionalConflictChecker(factory);
  }

  @Override
  public CompositionalLanguageInclusionChecker createLanguageInclusionChecker
    (final ProductDESProxyFactory factory)
  {
    return new CompositionalLanguageInclusionChecker(factory);
  }

  @Override
  public CompositionalAutomataSynthesizer createSupervisorSynthesizer
    (final ProductDESProxyFactory factory)
  {
    return new CompositionalAutomataSynthesizer(factory);
  }


  @Override
  public void registerOptions(final OptionPage db)
  {
    super.registerOptions(db);
    db.add(new BooleanOption
             (OPTION_AbstractCompositionalModelAnalyzer_BlockedEventsEnabled,
              "Use blocked events",
              "Detect and remove events known to be globablly disabled.",
              "-be",
              true));
    db.add(new BooleanOption
             (OPTION_AbstractCompositionalModelAnalyzer_FailingEventsEnabled,
              "Use failing events",
              "Detect events that only lead to blocking states and " +
              "simplify automata based on this information.",
              "-fe",
              true));
    db.add(new FileOption
             (OPTION_AbstractCompositionalModelAnalyzer_MonolithicDumpFile,
              "Dump file name",
              "If set, any abstracted model will be written to this file " +
              "before being sent for monolithic analysis.",
              "-dump"));
    db.add(new EnumOption<PreselectingMethod>
             (OPTION_AbstractCompositionalModelAnalyzer_PreselectingMethod,
              "Preselection method",
              "Preselection heuristic to generate groups of automata to consider " +
              "for composition.",
              "-presel",
              AbstractCompositionalModelAnalyzer.getPreselectingMethodFactoryStatic()));
    db.add(new BooleanOption
             (OPTION_AbstractCompositionalModelAnalyzer_SelfloopOnlyEventsEnabled,
              "Use selfloop-only events",
              "Detect events that are appear only as selfloop outside of the " +
              "subsystem being abstracted, and use this information to help " +
              "with minimisation.",
              "-se",
              true));
    db.add(new EnumOption<SelectionHeuristicCreator>
             (OPTION_AbstractCompositionalModelAnalyzer_SelectingMethod,
              "Selection method",
              "Heuristic to choose the group of automata to compose and simplify " +
              "from the options produced by the preselection method.",
              "-sel",
              CompositionalSelectionHeuristicFactory.getInstance()));
    db.add(new BooleanOption
             (OPTION_AbstractCompositionalModelAnalyzer_SubumptionEnabled,
              "Use subumption test",
              "Suppress candidate groups of automata that are supersets of " +
              "other candidates.",
              "-sub",
              true));

    db.add(new PositiveIntOption
           (OPTION_AbstractCompositionalModelVerifier_LowerInternalStateLimit,
            null,
            "Initial maximum number of states for abstraction attempts",
            "-lslimit",
            10000));
    db.add(new PositiveIntOption
           (OPTION_AbstractCompositionalModelVerifier_UpperInternalStateLimit,
            "Use blocked events",
            "Detect and remove events known to be globablly disabled.",
            "-uslimit",
            10000));
    db.add(new BooleanOption
           (OPTION_AbstractCompositionalModelVerifier_SpecialEvents,
            null,
            "Enable or disable blocked, failing, and selfloop-only events",
            "-sp",
            true));


    db.add(new BooleanOption
             (OPTION_AbstractCompositionalModelVerifier_TraceCheckingEnabled,
              "Counterexample debugging",
              "When computing counterexamples, perform debug checks to ensure " +
              "that the counterexample is accepted after every abstraction step.",
              "-tc",
              false));

    db.add(new EnumOption<AbstractionProcedureCreator>
             (OPTION_CompositionalAutomataSynthesizer_AbstractionProcedureCreator,
              "Abstraction procedure",
              "Abstraction procedure to simplify automata during compositional " +
              "minimisation.",
              "-method",
              AutomataSynthesisAbstractionProcedureFactory.getInstance()));

    db.add(new EnumOption<AbstractionProcedureCreator>
             (OPTION_CompositionalConflictChecker_AbstractionProcedureCreator,
              "Abstraction procedure",
              "Abstraction procedure to simplify automata during compositional " +
              "minimisation.",
              "-method",
              ConflictAbstractionProcedureFactory.getInstance()));
    db.add(new EnumOption<PreselectingMethod>
             (OPTION_CompositionalConflictChecker_PreselectingMethod,
              "Preselection method",
              "Preselection heuristic to generate groups of automata to consider " +
              "for composition.",
              "-presel",
              CompositionalConflictChecker.getPreselectingMethodFactoryStatic()));
    db.add(new EnumOption<SelectionHeuristicCreator>
             (OPTION_CompositionalConflictChecker_SelectingMethod,
              "Selection method",
              "Heuristic to choose the group of automata to compose and simplify " +
              "from the options produced by the preselection method.",
              "-sel",
              ConflictSelectionHeuristicFactory.getInstance()));
  }


  //#########################################################################
  //# Class Constants
  public static final String
    OPTION_AbstractCompositionalModelAnalyzer_BlockedEventsEnabled =
    "AbstractCompositionalModelAnalyzer.BlockedEventsEnabled";
  public static final String
    OPTION_AbstractCompositionalModelAnalyzer_FailingEventsEnabled =
    "AbstractCompositionalModelAnalyzer.FailingEventsEnabled";
  public static final String
    OPTION_AbstractCompositionalModelAnalyzer_MonolithicDumpFile =
    "AbstractCompositionalModelAnalyzer.MonolithicDumpFile";
  public static final String
    OPTION_AbstractCompositionalModelAnalyzer_PreselectingMethod =
    "AbstractCompositionalModelAnalyzer.PreselectingMethod";
  public static final String
    OPTION_AbstractCompositionalModelAnalyzer_SelfloopOnlyEventsEnabled =
    "AbstractCompositionalModelAnalyzer.SelfloopOnlyEventsEnabled";
  public static final String
    OPTION_AbstractCompositionalModelAnalyzer_SelectingMethod =
    "AbstractCompositionalModelAnalyzer.SelectingMethod";
  public static final String
    OPTION_AbstractCompositionalModelAnalyzer_SubumptionEnabled =
    "AbstractCompositionalModelAnalyzer.SubumptionEnabled";

  public static final String
    OPTION_AbstractCompositionalModelVerifier_TraceCheckingEnabled =
    "AbstractCompositionalModelVerifier.TraceCheckingEnabled";
  public static final String
    OPTION_AbstractCompositionalModelVerifier_LowerInternalStateLimit =
    "AbstractCompositionalModelAnalyzer.LowerInternalStateLimit";
  public static final String
    OPTION_AbstractCompositionalModelVerifier_UpperInternalStateLimit =
    "AbstractCompositionalModelAnalyzer.UpperInternalStateLimit";
  public static final String
    OPTION_AbstractCompositionalModelVerifier_SpecialEvents =
    "AbstractCompositionalModelAnalyzer.SpecialEvents";

  public static final String
    OPTION_CompositionalAutomataSynthesizer_AbstractionProcedureCreator =
    "CompositionalAutomataSynthesizer.AbstractionProcedureCreator";

  public static final String
    OPTION_CompositionalConflictChecker_AbstractionProcedureCreator =
    "CompositionalConflictChecker.AbstractionProcedureCreator";
  public static final String
    OPTION_CompositionalConflictChecker_PreselectingMethod =
    "CompositionalConflictChecker.PreselectingMethod";
  public static final String
    OPTION_CompositionalConflictChecker_SelectingMethod =
    "CompositionalConflictChecker.SelectionHeuristic";


}
