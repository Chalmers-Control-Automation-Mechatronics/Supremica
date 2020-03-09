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

package net.sourceforge.waters.analysis.trcomp;

import net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier;
import net.sourceforge.waters.analysis.compositional.SelectionHeuristic;
import net.sourceforge.waters.analysis.options.BooleanOption;
import net.sourceforge.waters.analysis.options.ChainOption;
import net.sourceforge.waters.analysis.options.EnumOption;
import net.sourceforge.waters.analysis.options.FileOption;
import net.sourceforge.waters.analysis.options.OptionPage;
import net.sourceforge.waters.model.analysis.des.AbstractModelAnalyzerFactory;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * A factory that produces compositional model verifiers.
 *
 * @author Robi Malik
 */

public class TRCompositionalModelAnalyzerFactory
  extends AbstractModelAnalyzerFactory
{

  //#########################################################################
  //# Singleton Pattern
  public static TRCompositionalModelAnalyzerFactory getInstance()
  {
    return SingletonHolder.INSTANCE;
  }

  private static class SingletonHolder {
    private static final TRCompositionalModelAnalyzerFactory INSTANCE =
      new TRCompositionalModelAnalyzerFactory();
  }


  //#########################################################################
  //# Constructors
  TRCompositionalModelAnalyzerFactory()
  {
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelAnalyzerFactory
  @Override
  public TRControllabilityChecker createControllabilityChecker
    (final ProductDESProxyFactory factory)
  {
    return new TRControllabilityChecker();
  }

  @Override
  public TRCompositionalConflictChecker createConflictChecker
    (final ProductDESProxyFactory factory)
  {
    return new TRCompositionalConflictChecker();
  }

  @Override
  public TRLanguageInclusionChecker createLanguageInclusionChecker
    (final ProductDESProxyFactory factory)
  {
    return new TRLanguageInclusionChecker();
  }

  @Override
  public TRCompositionalStateCounter createStateCounter
    (final ProductDESProxyFactory factory)
  {
    return new TRCompositionalStateCounter();
  }


  @Override
  public void registerOptions(final OptionPage db)
  {
    super.registerOptions(db);
    db.add(new BooleanOption
             (OPTION_AbstractTRCompositionalModelAnalyzer_AlwaysEnabledEventsEnabled,
              "Use always enabled events",
              "Detect events that are enabled in all states outside of the " +
              "subsystem being abstracted, and use this information to help " +
              "with minimisation.",
              "-ae",
              true));
    db.add(new BooleanOption
             (OPTION_AbstractTRCompositionalModelAnalyzer_BlockedEventsEnabled,
              "Use blocked events",
              "Detect and remove events known to be globablly disabled.",
              "-be",
              true));
    db.add(new BooleanOption
             (OPTION_AbstractTRCompositionalModelAnalyzer_FailingEventsEnabled,
              "Use failing events",
              "Detect events that only lead to blocking states and " +
              "simplify automata based on this information.",
              "-fe",
              true));
    db.add(new FileOption
             (OPTION_AbstractTRCompositionalModelAnalyzer_MonolithicDumpFile,
              "Dump file name",
              "If set, any abstracted model will be written to this file " +
              "before being sent for monolithic analysis.",
              "-dump"));
    db.add(new EnumOption<TRPreselectionHeuristic>
             (OPTION_AbstractTRCompositionalModelAnalyzer_PreselectionHeuristic,
              "Preselection method",
              "Preselection heuristic to generate groups of automata to consider " +
              "for composition.",
              "-presel",
              AbstractTRCompositionalModelAnalyzer.getPreselectionHeuristicFactoryStatic()));
    db.add(new BooleanOption
             (OPTION_AbstractTRCompositionalModelAnalyzer_SelfloopOnlyEventsEnabled,
              "Use selfloop-only events",
              "Detect events that are appear only as selfloop outside of the " +
              "subsystem being abstracted, and use this information to help " +
              "with minimisation.",
              "-se",
              true));
    db.add(new EnumOption<SelectionHeuristic<TRCandidate>>
             (OPTION_AbstractTRCompositionalModelAnalyzer_SelectionHeuristic,
              "Selection method",
              "Heuristic to choose the group of automata to compose and simplify " +
              "from the options produced by the preselection method.",
              "-sel",
              AbstractTRCompositionalModelAnalyzer.getSelectionHeuristicFactoryStatic()));
    db.add(new BooleanOption
             (OPTION_AbstractTRCompositionalModelAnalyzer_WeakObservationEquivalence,
              "Use weak observation equivalence",
              "Use weak observation equivalence rather than ordinary " +
              "observation equivalence in the abstraction procedure.",
              "-woeq",
              false));

    db.add(new BooleanOption
             (OPTION_AbstractTRCompositionalModelVerifier_OutputCheckingEnabled,
              "Counterexample debugging",
              "When computing counterexamples, perform debug checks to ensure " +
              "that the counterexample is accepted after every abstraction step.",
              "-tc",
              false));

    db.add(new EnumOption<TRPreselectionHeuristic>
             (OPTION_TRCompositionalConflictChecker_PreselectionHeuristic,
              "Preselection method",
              "Preselection heuristic to generate groups of automata to consider " +
              "for composition.",
              "-presel",
              TRCompositionalConflictChecker.getPreselectionHeuristicFactoryStatic()));
    db.add(new EnumOption<SelectionHeuristic<TRCandidate>>
             (OPTION_TRCompositionalConflictChecker_SelectionHeuristic,
              "Selection method",
              "Heuristic to choose the group of automata to compose and simplify " +
              "from the options produced by the preselection method.",
              "-sel",
              TRCompositionalConflictChecker.getSelectionHeuristicFactoryStatic()));
    db.add(new EnumOption<TRToolCreator<TransitionRelationSimplifier>>
             (OPTION_TRCompositionalConflictChecker_SimplifierCreator,
              "Abstraction procedure",
              "Abstraction procedure to simplify automata during compositional " +
              "minimisation.",
              "-method",
              TRCompositionalConflictChecker.getTRSimplifierFactoryStatic()));
    db.add(new BooleanOption
             (OPTION_TRCompositionalConflictChecker_LimitedCertainConflicts,
              "Use limited certain conflicts",
              "Include the Limited Certain Conflicts Rule in the " +
              "abstraction procedure.",
              "-lcc",
              true));
    db.add(new ChainOption
             (OPTION_ModelAnalyzer_SecondaryFactory,
              "Monolithic model analyzer",
              "Algorithm used to analyze the results of abstraction.",
              "-chain"));
  }


  //#########################################################################
  //# Class Constants
  public static final String
    OPTION_AbstractTRCompositionalModelAnalyzer_AlwaysEnabledEventsEnabled =
    "AbstractTRCompositionalModelAnalyzer.AlwaysEnabledEventsEnabled";
  public static final String
    OPTION_AbstractTRCompositionalModelAnalyzer_BlockedEventsEnabled =
    "AbstractTRCompositionalModelAnalyzer.BlockedEventsEnabled";
  public static final String
    OPTION_AbstractTRCompositionalModelAnalyzer_FailingEventsEnabled =
    "AbstractTRCompositionalModelAnalyzer.FailingEventsEnabled";
  public static final String
    OPTION_AbstractTRCompositionalModelAnalyzer_MonolithicDumpFile =
    "AbstractTRCompositionalModelAnalyzer.MonolithicDumpFile";
  public static final String
    OPTION_AbstractTRCompositionalModelAnalyzer_PreselectionHeuristic =
    "AbstractTRCompositionalModelAnalyzer.PreselectionHeuristic";
  public static final String
    OPTION_AbstractTRCompositionalModelAnalyzer_SelfloopOnlyEventsEnabled =
    "AbstractTRCompositionalModelAnalyzer.SelfloopOnlyEventsEnabled";
  public static final String
    OPTION_AbstractTRCompositionalModelAnalyzer_SelectionHeuristic =
    "AbstractTRCompositionalModelAnalyzer.SelectionHeuristic";
  public static final String
    OPTION_AbstractTRCompositionalModelAnalyzer_WeakObservationEquivalence =
    "AbstractTRCompositionalModelAnalyzer.WeakObservationEquivalence";

  public static final String
    OPTION_AbstractTRCompositionalModelAnalyzer_SpecialEvents =
    "AbstractTRCompositionalModelAnalyzer.SpecialEvents";

  public static final String
    OPTION_AbstractTRCompositionalModelVerifier_OutputCheckingEnabled =
    "AbstractTRCompositionalModelVerifier.TraceCheckingEnabled";

  public static final String
    OPTION_TRCompositionalConflictChecker_SimplifierCreator =
    "TRCompositionalConflictChecker.SimplifierCreator";
  public static final String
    OPTION_TRCompositionalConflictChecker_PreselectionHeuristic =
    "TRCompositionalConflictChecker.PreselectionHeuristic";
  public static final String
    OPTION_TRCompositionalConflictChecker_SelectionHeuristic =
    "TRCompositionalConflictChecker.SelectionHeuristic";
  public static final String
    OPTION_TRCompositionalConflictChecker_LimitedCertainConflicts =
    "TRCompositionalConflictChecker.LimtedCertainConflicts";

}
