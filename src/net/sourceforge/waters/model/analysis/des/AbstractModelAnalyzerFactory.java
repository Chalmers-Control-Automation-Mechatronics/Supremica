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

package net.sourceforge.waters.model.analysis.des;

import net.sourceforge.waters.analysis.abstraction.StateReorderingTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.SupervisorReductionMainMethod;
import net.sourceforge.waters.analysis.abstraction.SupervisorReductionProjectionMethod;
import net.sourceforge.waters.analysis.coobs.CoobservabilityAttributeFactory;
import net.sourceforge.waters.model.analysis.AnalysisConfigurationException;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.options.AnalysisOptionPage;
import net.sourceforge.waters.model.options.BooleanOption;
import net.sourceforge.waters.model.options.ComponentKindOption;
import net.sourceforge.waters.model.options.DoubleOption;
import net.sourceforge.waters.model.options.EnumOption;
import net.sourceforge.waters.model.options.EventSetOption;
import net.sourceforge.waters.model.options.PositiveIntOption;
import net.sourceforge.waters.model.options.PropositionOption;
import net.sourceforge.waters.model.options.StringListOption;
import net.sourceforge.waters.model.options.StringOption;


/**
 * A default implementation of the {@link ModelAnalyzerFactory} interface.
 * This class is extended for different flavours of model checking
 * algorithms.
 *
 * @author Robi Malik
 */

public abstract class AbstractModelAnalyzerFactory
  implements ModelAnalyzerFactory
{

  //#########################################################################
  //# Constructors
  protected AbstractModelAnalyzerFactory()
  {
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelVerifierFactory
  @Override
  public ConflictChecker createConflictChecker
    (final ProductDESProxyFactory factory)
    throws AnalysisConfigurationException
  {
    throw createUnsupportedOperationException("conflict check");
  }

  @Override
  public ControllabilityChecker createControllabilityChecker
    (final ProductDESProxyFactory factory)
    throws AnalysisConfigurationException
  {
    throw createUnsupportedOperationException("controllability check");
  }

  @Override
  public ControlLoopChecker createControlLoopChecker
    (final ProductDESProxyFactory factory)
    throws AnalysisConfigurationException
  {
    throw createUnsupportedOperationException("control-loop check");
  }

  @Override
  public CoobservabilityChecker createCoobservabilityChecker
    (final ProductDESProxyFactory factory)
    throws AnalysisConfigurationException
  {
    throw createUnsupportedOperationException("coobservability check");
  }

  @Override
  public DeadlockChecker createDeadlockChecker
    (final ProductDESProxyFactory factory)
    throws AnalysisConfigurationException
  {
    throw createUnsupportedOperationException("deadlock check");
  }

  @Override
  public DiagnosabilityChecker createDiagnosabilityChecker
    (final ProductDESProxyFactory factory)
    throws AnalysisConfigurationException
  {
    throw createUnsupportedOperationException("diagnosability check");
  }

  @Override
  public LanguageInclusionChecker createLanguageInclusionChecker
    (final ProductDESProxyFactory factory)
    throws AnalysisConfigurationException
  {
    throw createUnsupportedOperationException("language inclusion check");
  }

  @Override
  public SynchronousProductBuilder createSynchronousProductBuilder
    (final ProductDESProxyFactory factory)
    throws AnalysisConfigurationException
  {
    throw createUnsupportedOperationException("synchronous product");
  }

  @Override
  public SupervisorSynthesizer createSupervisorSynthesizer
    (final ProductDESProxyFactory factory)
    throws AnalysisConfigurationException
  {
    throw createUnsupportedOperationException("synthesis");
  }

  @Override
  public StateCounter createStateCounter
    (final ProductDESProxyFactory factory)
    throws AnalysisConfigurationException
  {
    throw createUnsupportedOperationException("state counting");
  }


  //#########################################################################
  //# Configuration
  @Override
  public void registerOptions(final AnalysisOptionPage db)
  {
    db.register(new PositiveIntOption
             (OPTION_ModelAnalyzer_FinalStateLimit,
              "Monolithic state limit",
              "Maximum number of states in final synchronous product before aborting.",
              "-fslimit"));
    db.register(new PositiveIntOption
             (OPTION_ModelAnalyzer_FinalTransitionLimit,
              "Monolithic transition limit",
              "Maximum number of transitions in final synchronous product before aborting.",
              "-ftlimit"));
    db.register(new PositiveIntOption
             (OPTION_ModelAnalyzer_InternalStateLimit,
              "Internal state limit",
              "Maximum number of states in intermediate abstraction steps.",
              "-islimit"));
    db.register(new PositiveIntOption
             (OPTION_ModelAnalyzer_InternalTransitionLimit,
              "Internal transition limit",
              "Maximum number of transitions in intermediate abstraction steps.",
              "-itlimit"));

    db.register(new BooleanOption
             (OPTION_ModelVerifier_DetailedOutputEnabled,
              "Compute counterexample",
              "Compute a counterexample if model checking gives a failed result.",
              "-out",
              true));
    db.register(new BooleanOption
             (OPTION_ModelVerifier_ShortCounterExampleRequested,
              "Short counterexample",
              "Try to compute a counterexample that is as short as possible.",
              "-mince",
              false));

    db.register(new PropositionOption
             (OPTION_ConflictChecker_ConfiguredDefaultMarking,
              "Marking proposition",
              "The model is considered nonblocking, if it possible to reach a state " +
              "marked by this proposition from every reachable state.",
              "-marking",
              PropositionOption.DefaultKind.PREVENT_NULL));
    db.register(new PropositionOption
             (OPTION_ConflictChecker_ConfiguredPreconditionMarking,
              "Precondition marking",
              "Precondition marking used for generalised conflict check.",
              "-premarking",
              PropositionOption.DefaultKind.DEFAULT_NULL));

    db.register(new EventSetOption
             (OPTION_ControlLoopChecker_LoopEvents,
              "Loop events",
              "Check whether the system permits a loop using the selected events.",
              "-events",
              EventSetOption.DefaultKind.CONTROLLABLE,
              "Loop Events",
              "Non-Loop Events"));

    db.register(new StringOption
             (OPTION_CoobservabilityChecker_DefaultSite,
              "Default supervisor site",
              "Name of supervisor site assigned to controllable or " +
              "observable events without a site given as an attribute. " +
              "Leave blank to assign no controlling or observing site.",
              "-defaultSite",
              CoobservabilityAttributeFactory.DEFAULT_SITE_NAME));

    db.register(new StringListOption
      (OPTION_DiagnosabilityChecker_FaultClasses,
       "Fault class", "Fault class to be checked for diagnosability",
       "-fault"));

    db.register(new BooleanOption
             (OPTION_SupervisorSynthesizer_DetailedOutputEnabled,
              "Create supervisor automata",
              "Disable this to suppress the creation of supervisor automata, " +
              "and only determine whether a supervisor exists.",
              "-out",
              true));
    db.register(new PropositionOption
             (OPTION_SupervisorSynthesizer_ConfiguredDefaultMarking,
              "Marking proposition",
              "If synthesising a nonblocking supervisor, it will be " +
              "nonblocking with respect to this proposition.",
              "-marking",
              PropositionOption.DefaultKind.ALLOW_NULL));
    db.register(new BooleanOption
             (OPTION_SupervisorSynthesizer_ControllableSynthesis,
              "Controllable supervisor",
              "Synthesise a controllable supervisor.",
              "-cont",
              true));
    db.register(new BooleanOption
             (OPTION_SupervisorSynthesizer_NonblockingSynthesis,
              "Nonblocking supervisor",
              "Synthesise a nonblocking supervisor.",
              "-nbl",
              true));
    db.register(new BooleanOption
             (OPTION_SupervisorSynthesizer_NormalSynthesis,
              "Normal supervisor",
              "Synthesise a normal supervisor.",
              "-norm",
              false));
    db.register(new StringOption
             (OPTION_SupervisorSynthesizer_OutputName,
              "Supervisor name prefix",
              "Name or name prefix for synthesised supervisors.",
              "-name",
              "sup"));
    db.register(new BooleanOption
             (OPTION_SupervisorSynthesizer_SupervisorLocalisationEnabled,
              "Localize supervisors",
              "If using supervisor reduction, create separate supervisors " +
              "for each controllable event that needs to be disabled.",
              "-loc",
              false));
    db.register(new EnumOption<SupervisorReductionMainMethod>
             (OPTION_SupervisorSynthesizer_SupervisorReductionMainMethod,
              "Supervisor reduction method",
              "Core algorithm to reduce the size of computed supervisors.",
              "-red",
              SupervisorReductionMainMethod.values()));
    db.register(new DoubleOption
             (OPTION_SupervisorSynthesizer_SupervisorReductionMaxIncrease,
              "Growth limit for supervisor reduction",
              "If using supervisor reduction projection, limit the growth " +
              "of intermediate results by this factor.",
              "-maxinc",
              10.0, 1.0, Double.POSITIVE_INFINITY));
    db.register(new EnumOption<SupervisorReductionProjectionMethod>
             (OPTION_SupervisorSynthesizer_SupervisorReductionProjectionMethod,
              "Supervisor reduction projection",
              "Method to reduce the number of events before supervisor reduction.",
              "-redproj",
              SupervisorReductionProjectionMethod.values()));
    db.register(new EnumOption<StateReorderingTRSimplifier.StateOrdering>
             (OPTION_SupervisorSynthesizer_SupervisorReductionStateOrdering,
              "Supervisor reduction state ordering",
              "State ordering for supervisor reduction.",
              "-redso",
              StateReorderingTRSimplifier.getStateOrderingEnumFactory()));

    db.register(new BooleanOption
             (OPTION_SynchronousProductBuilder_DetailedOutputEnabled,
              "Build automaton model",
              "Disable this to suppress the creation of a synchronous product " +
              "automaton, and only run for statistics.",
              "-out",
              true));
    db.register(new StringOption
             (OPTION_SynchronousProductBuilder_OutputName,
              "Output name",
              "Name for the generated synchronous product automaton",
              "-name",
              "sync"));
    db.register(new ComponentKindOption
             (OPTION_SynchronousProductBuilder_OutputKind,
              "Output kind",
              "Type of the generated synchronous product automaton.",
              "-kind"));
    db.register(new BooleanOption
             (OPTION_SynchronousProductBuilder_PruningDeadlocks,
              "Prune deadlocks",
              "Stop synchronous product construction when encountering " +
              "states that are a deadlock in one of the components.",
              "-prune",
              false));
    db.register(new BooleanOption
             (OPTION_SynchronousProductBuilder_RemovingSelfloops,
              "Remove Selfloops",
              "Remove events that appear only as selfloop on every state," +
              "as well as propositions that appear on all states, from the result.",
              "-out",
              true));

    db.register(new StringListOption
             (OPTION_LanguageInclusionChecker_Property, null,
              "Name of a property to be checked",
              "-property"));
  }


  //#########################################################################
  //# Auxiliary Methods
  private AnalysisConfigurationException createUnsupportedOperationException
    (final String opname)
  {
    final String clsname = getClass().getName();
    final int dotpos = clsname.lastIndexOf('.');
    final String msg =
      clsname.substring(dotpos + 1) + " does not support " + opname + "!";
    return new AnalysisConfigurationException(msg);
  }


  //#########################################################################
  //# Class Constants
  public static final String OPTION_ModelAnalyzer_FinalStateLimit =
    "ModelAnalyzer.FinalStateLimit";
  public static final String OPTION_ModelAnalyzer_FinalTransitionLimit =
    "ModelAnalyzer.FinalTransitionLimit";
  public static final String OPTION_ModelAnalyzer_InternalStateLimit =
    "ModelAnalyzer.InternalStateLimit";
  public static final String OPTION_ModelAnalyzer_InternalTransitionLimit =
    "ModelAnalyzer.InternalTransitionLimit";

  public static final String OPTION_ModelVerifier_DetailedOutputEnabled =
    "ModelVerifier.DetailedOutputEnabled";
  public static final String OPTION_ModelVerifier_ShortCounterExampleRequested =
    "ModelVerifier.ShortCounterExampleRequested";

  public static final String OPTION_ConflictChecker_ConfiguredDefaultMarking =
    "ConflictChecker.ConfiguredDefaultMarking";
  public static final String OPTION_ConflictChecker_ConfiguredPreconditionMarking =
    "ConflictChecker.ConfiguredPreconditionMarking";

  public static final String OPTION_ControlLoopChecker_LoopEvents =
    "ControlLoopChecker.LoopEvents";

  public static final String OPTION_CoobservabilityChecker_DefaultSite =
    "CoobservabilityChecker.DefaultSite";

  public static final String OPTION_DiagnosabilityChecker_FaultClasses =
    "DiagnosabilityChecker.FaultClasses";

  public static final String OPTION_LanguageInclusionChecker_Property =
    "LanguageInclusionChecker.Property";

  public static final String[] CHAIN_SUPPRESSIONS = {
    OPTION_ModelAnalyzer_FinalStateLimit,
    OPTION_ModelAnalyzer_FinalTransitionLimit,
    OPTION_ModelVerifier_DetailedOutputEnabled,
    OPTION_ModelVerifier_ShortCounterExampleRequested,
    OPTION_ConflictChecker_ConfiguredDefaultMarking,
    OPTION_ConflictChecker_ConfiguredPreconditionMarking,
    OPTION_ControlLoopChecker_LoopEvents,
    OPTION_LanguageInclusionChecker_Property
  };

  public static final String OPTION_SupervisorSynthesizer_ConfiguredDefaultMarking =
    "SupervisorSynthesizer.ConfiguredDefaultMarking";
  public static final String OPTION_SupervisorSynthesizer_ControllableSynthesis =
    "SupervisorSynthesizer.ControllableSynthesis";
  public static final String OPTION_SupervisorSynthesizer_DetailedOutputEnabled =
    "SupervisorSynthesizer.DetailedOutputEnabled";
  public static final String OPTION_SupervisorSynthesizer_NonblockingSynthesis =
    "SupervisorSynthesizer.NonblockingSynthesis";
  public static final String OPTION_SupervisorSynthesizer_NormalSynthesis =
    "SupervisorSynthesizer.NormalSynthesis";
  public static final String OPTION_SupervisorSynthesizer_OutputName =
    "SupervisorSynthesizer.OutputName";
  public static final String OPTION_SupervisorSynthesizer_SupervisorLocalisationEnabled =
    "SupervisorSynthesizer.SupervisorLocalisationEnabled";
  public static final String OPTION_SupervisorSynthesizer_SupervisorReductionMaxIncrease =
    "SupervisorSynthesizer.SupervisorReductionMaxIncrease";
  public static final String OPTION_SupervisorSynthesizer_SupervisorReductionMainMethod =
    "SupervisorSynthesizer.SupervisorReductionMainMethod";
  public static final String OPTION_SupervisorSynthesizer_SupervisorReductionProjectionMethod =
    "SupervisorSynthesizer.SupervisorReductionProjectionMethod";
  public static final String OPTION_SupervisorSynthesizer_SupervisorReductionStateOrdering =
    "SupervisorSynthesizer.SupervisorReductionStateOrdering";

  public static final String OPTION_SynchronousProductBuilder_DetailedOutputEnabled =
    "SynchronousProductBuilder.DetailedOutputEnabled";
  public static final String OPTION_SynchronousProductBuilder_OutputName =
    "SynchronousProductBuilder.OutputName";
  public static final String OPTION_SynchronousProductBuilder_OutputKind =
    "SynchronousProductBuilder.OutputKind";
  public static final String OPTION_SynchronousProductBuilder_PruningDeadlocks =
    "SynchronousProductBuilder.PruningDeadlocks";
  public static final String OPTION_SynchronousProductBuilder_RemovingSelfloops =
    "SynchronousProductBuilder.RemovingSelfloops";

}
