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

package net.sourceforge.waters.analysis.options;

import net.sourceforge.waters.analysis.abstraction.DefaultSupervisorReductionFactory;
import net.sourceforge.waters.analysis.abstraction.SupervisorReductionFactory;
import net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier;
import net.sourceforge.waters.analysis.bdd.BDDPackage;
import net.sourceforge.waters.analysis.bdd.TransitionPartitioningStrategy;
import net.sourceforge.waters.analysis.bdd.VariableOrdering;
import net.sourceforge.waters.analysis.compositional.AbstractCompositionalModelAnalyzer;
import net.sourceforge.waters.analysis.compositional.AbstractCompositionalModelAnalyzer.PreselectingMethod;
import net.sourceforge.waters.analysis.compositional.AbstractionProcedureCreator;
import net.sourceforge.waters.analysis.compositional.AutomataSynthesisAbstractionProcedureFactory;
import net.sourceforge.waters.analysis.compositional.CompositionalConflictChecker;
import net.sourceforge.waters.analysis.compositional.CompositionalSelectionHeuristicFactory;
import net.sourceforge.waters.analysis.compositional.ConflictAbstractionProcedureFactory;
import net.sourceforge.waters.analysis.compositional.ConflictSelectionHeuristicFactory;
import net.sourceforge.waters.analysis.compositional.SelectionHeuristic;
import net.sourceforge.waters.analysis.compositional.SelectionHeuristicCreator;
import net.sourceforge.waters.analysis.gnonblocking.PreselectingHeuristicFactory;
import net.sourceforge.waters.analysis.gnonblocking.SelectingHeuristicFactory;
import net.sourceforge.waters.analysis.modular.AutomataGroup;
import net.sourceforge.waters.analysis.trcomp.AbstractTRCompositionalModelAnalyzer;
import net.sourceforge.waters.analysis.trcomp.TRCandidate;
import net.sourceforge.waters.analysis.trcomp.TRCompositionalConflictChecker;
import net.sourceforge.waters.analysis.trcomp.TRPreselectionHeuristic;
import net.sourceforge.waters.analysis.trcomp.TRToolCreator;
import net.sourceforge.waters.cpp.analysis.ConflictCheckMode;
import net.sourceforge.waters.model.base.ComponentKind;

public class ParameterIDs
{

  public static final JListParameter<ComponentKind> testJListParam = new JListParameter<ComponentKind>(9000,
    "JListParam Test",
    "", ComponentKind.values(), ComponentKind.SPEC);

  //net.sourceforge.waters.model.analysis.ModelAnalyzer
  private static final int ModelAnalyzer_DetailedOutputEnabled_ID = 0;
  private static final int ModelAnalyzer_NodeLimit_ID = 2;
  private static final int ModelAnalyzer_TransitionLimit_ID = 3;

  public static final BoolParameter ModelAnalyzer_DetailedOutputEnabled =
    new BoolParameter
     (ModelAnalyzer_DetailedOutputEnabled_ID,
      "Detailed output",
      "Compute full output, e.g., synthesised supervisor automata or " +
      "counterexample.",
      true);

  public static final IntParameter ModelAnalyzer_NodeLimit =
    new IntParameter
     (ModelAnalyzer_NodeLimit_ID,
      "Node limit",
      "Maximum number of states allows before aborting.",
      0, Integer.MAX_VALUE, Integer.MAX_VALUE);

  public static final IntParameter ModelAnalyzer_TransitionLimit =
    new IntParameter
     (ModelAnalyzer_TransitionLimit_ID,
      "Transition limit",
      "Maximum number of transitions allowed before aborting.",
      0, Integer.MAX_VALUE, Integer.MAX_VALUE);


  //net.sourceforge.waters.model.analysis.SupervisorSynthesizer
  private static final int SupervisorSynthesizer_DetailedOutputEnabled_ID = 200;
  private static final int SupervisorSynthesizer_OutputName_ID = 201;
  private static final int SupervisorSynthesizer_ControllableSynthesis_ID = 202;
  private static final int SupervisorSynthesizer_NonblockingSynthesis_ID = 203;
  @SuppressWarnings("unused")
  private static final int SupervisorSynthesizer_NormalSynthesis_ID = 204;
  private static final int SupervisorSynthesizer_ConfiguredDefaultMarking_ID = 205;
  private static final int SupervisorSynthesizer_SupervisorLocalisationEnabled_ID = 206;
  private static final int SupervisorSynthesizer_SupervisorReductionFactory_ID = 207;

  public static final BoolParameter SupervisorSynthesizer_DetailedOutputEnabled =
    new BoolParameter
      (SupervisorSynthesizer_DetailedOutputEnabled_ID,
       "Create supervisor automata",
       "Disable this to suppress the creation of supervisor automata, and " +
       "only determine whether a supervisor exists.",
       true);

  public static final StringParameter SupervisorSynthesizer_OutputName =
    new StringParameter
      (SupervisorSynthesizer_OutputName_ID,
       "Supervisor name prefix",
       "Name or name prefix for synthesised supervisors.",
       "sup");

  public static final BoolParameter SupervisorSynthesizer_ControllableSynthesis =
    new BoolParameter
      (SupervisorSynthesizer_ControllableSynthesis_ID,
       "Controllable supervisor",
       "Synthesise a controllable supervisor.",
       true);

  public static final BoolParameter SupervisorSynthesizer_NonblockingSynthesis =
    new BoolParameter
      (SupervisorSynthesizer_NonblockingSynthesis_ID,
       "Nonblocking supervisor",
       "Synthesise a supervisor that is nonblocking supervisor with respect " +
       "to the configured marking proposition.",
       true);

  public static final EventParameter SupervisorSynthesizer_ConfiguredDefaultMarking =
    new EventParameter
      (SupervisorSynthesizer_ConfiguredDefaultMarking_ID,
       "Marking proposition",
       "If synthesising a nonblocking supervisor, it will be " +
       "nonblocking with respect to this proposition.",
       EventParameterType.ALLOW_NULL);

  public static final BoolParameter SupervisorSynthesizer_SupervisorLocalisationEnabled =
    new BoolParameter
      (SupervisorSynthesizer_SupervisorLocalisationEnabled_ID,
       "Localize supervisors",
       "If using supervisor reduction, create a separate supervisor " +
       "for each controllable event that needs to be disabled.",
       true);

  public static final EnumParameter<SupervisorReductionFactory> SupervisorSynthesizer_SupervisorReductionFactory =
    new EnumParameter<SupervisorReductionFactory>
     (SupervisorSynthesizer_SupervisorReductionFactory_ID,
      "Supervisor reduction",
      "Method of supervisor reduction to be used after synthesis.",
      DefaultSupervisorReductionFactory.values());


  //net.sourceforge.waters.analysis.compositional.AbstractCompositionalModelAnalyzer
  private static final int AbstractCompositionalModelAnalyzer_AbstractionProcedureCreator_ID = 500;
  private static final int AbstractCompositionalModelAnalyzer_BlockedEventsEnabled_ID = 501;
  private static final int AbstractCompositionalModelAnalyzer_FailingEventsEnabled_ID = 504;
  private static final int AbstractCompositionalModelAnalyzer_InternalStateLimit_ID = 505;
  private static final int AbstractCompositionalModelAnalyzer_InternalTransitionLimit_ID = 506;
  @SuppressWarnings("unused")
  private static final int AbstractCompositionalModelAnalyzer_LowerInternalStateLimit_ID = 507;             //unused
  @SuppressWarnings("unused")
  private static final int AbstractCompositionalModelAnalyzer_MonolithicAnalyzer_ID = 508;                    //unused
  private static final int AbstractCompositionalModelAnalyzer_MonolithicDumpFile_ID = 509;
  private static final int AbstractCompositionalModelAnalyzer_MonolithicStatelimit_ID = 510;
  private static final int AbstractCompositionalModelAnalyzer_MonolithicTransitionLimit_ID = 511;
  private static final int AbstractCompositionalModelAnalyzer_PreselectingMethod_ID = 512;
  private static final int AbstractCompositionalModelAnalyzer_PruningDeadlocks_ID = 513;
  private static final int AbstractCompositionalModelAnalyzer_SelectionHeuristic_ID = 514;
  private static final int AbstractCompositionalModelAnalyzer_SelfLoopOnlyEventsEnabled_ID = 515;
  private static final int AbstractCompositionalModelAnalyzer_SubumptionEnabled_ID = 516;
  @SuppressWarnings("unused")
  private static final int AbstractCompositionalModelAnalyzer_SynchronousProductBuilder_ID = 517;         //unused
  @SuppressWarnings("unused")
  private static final int AbstractCompositionalModelAnalyzer_UpperInternalStateLimit_ID = 518;           //unused
  @SuppressWarnings("unused")
  private static final int AbstractCompositionalModelAnalyzer_UsingSpecialEvents_ID = 519;                //unused

  public static final EnumParameter<AbstractionProcedureCreator> AbstractCompositionalModelAnalyzer_AbstractionProcedureCreator =
    new EnumParameter<AbstractionProcedureCreator>
      (AbstractCompositionalModelAnalyzer_AbstractionProcedureCreator_ID,
       "Abstraction procedure",
       "Abstraction procedure to simplify automata during compositional " +
       "minimisation.");

  public static final BoolParameter AbstractCompositionalModelAnalyzer_BlockedEventsEnabled =
    new BoolParameter
      (AbstractCompositionalModelAnalyzer_BlockedEventsEnabled_ID,
       "Use blocked events",
       "Detect and remove events known to be globablly disabled.",
       true);

  public static final BoolParameter AbstractCompositionalModelAnalyzer_FailingEventsEnabled =
    new BoolParameter
      (AbstractCompositionalModelAnalyzer_FailingEventsEnabled_ID,
       "Use failing events",
       "Detect events that only lead to blocking states and " +
       "simplify automata based on this information.",
       true);

  public static final IntParameter AbstractCompositionalModelAnalyzer_InternalStateLimit =
    new IntParameter
      (AbstractCompositionalModelAnalyzer_InternalStateLimit_ID,
       "Internal state limit",
       "The maximum number of states allowed for intermediate automata.",
       0, Integer.MAX_VALUE, Integer.MAX_VALUE);

  public static final IntParameter AbstractCompositionalModelAnalyzer_InternalTransitionLimit =
    new IntParameter
      (AbstractCompositionalModelAnalyzer_InternalTransitionLimit_ID,
       "Internal transition limit",
       "The maximum number of transitions allowed for intermediate automata.",
       0, Integer.MAX_VALUE, Integer.MAX_VALUE);

  public static final FileParameter AbstractCompositionalModelAnalyzer_MonolithicDumpFile =
    new FileParameter
      (AbstractCompositionalModelAnalyzer_MonolithicDumpFile_ID,
       "Dump file name",
       "If set, any abstracted model will be written to this file " +
       "before being sent for monolithic analysis.");

  public static final IntParameter AbstractCompositionalModelAnalyzer_MonolithicStatelimit =
    new IntParameter
      (AbstractCompositionalModelAnalyzer_MonolithicStatelimit_ID,
       "Monolithic state limit",
       "The maximum number of states allowed during monolithic analysis " +
       "attempts.",
       0, Integer.MAX_VALUE, Integer.MAX_VALUE);

  public static final IntParameter AbstractCompositionalModelAnalyzer_MonolithicTransitionLimit =
    new IntParameter
      (AbstractCompositionalModelAnalyzer_MonolithicTransitionLimit_ID,
       "Monolithic transition limit",
       "The maximum number of transitions allowed during monolithic " +
       "analysis attempts.",
       0, Integer.MAX_VALUE, Integer.MAX_VALUE);

  public static final EnumParameter<PreselectingMethod>
    AbstractCompositionalModelAnalyzer_PreselectingMethod =
    new EnumParameter<PreselectingMethod>
      (AbstractCompositionalModelAnalyzer_PreselectingMethod_ID,
       "Preselection method",
       "Preselection heuristic to choose groups of automata to consider " +
       "for composition.",
       AbstractCompositionalModelAnalyzer.getPreselectingMethodFactoryStatic());

  public static final BoolParameter AbstractCompositionalModelAnalyzer_PruningDeadlocks =
    new BoolParameter
      (AbstractCompositionalModelAnalyzer_PruningDeadlocks_ID,
       "Prune deadlocks",
       "Allow synchronous product construction to stop when encountering " +
       "states that are a deadlock in one of the components.",
       true);

  public static final EnumParameter<SelectionHeuristicCreator>
    AbstractCompositionalModelAnalyzer_SelectionHeuristic =
    new EnumParameter<SelectionHeuristicCreator>
      (AbstractCompositionalModelAnalyzer_SelectionHeuristic_ID,
       "Selection heuristic",
       "Heuristic to choose the group of automata to compose and simplify " +
       "from the options produced by the preselection method.",
       CompositionalSelectionHeuristicFactory.getInstance());

  public static final BoolParameter AbstractCompositionalModelAnalyzer_SelfLoopOnlyEventsEnabled =
    new BoolParameter
      (AbstractCompositionalModelAnalyzer_SelfLoopOnlyEventsEnabled_ID,
       "Use selfloop-only events",
       "Detect events that are appear only as selfloop outside of the " +
       "subsystem being abstracted, and use this information to help with " +
       "minimisation.",
       true);

  public static final BoolParameter AbstractCompositionalModelAnalyzer_SubumptionEnabled =
    new BoolParameter
      (AbstractCompositionalModelAnalyzer_SubumptionEnabled_ID,
       "Use subumption test",
       "Suppress candidate groups of automata that are supersets of " +
       "other candidates.",
       true);


  //net.sourceforge.waters.analysis.compositional.CompositionalAutomataSynthesizer
  private static final int CompositionalAutomataSynthesizer_AbstractionProcedureCreator_ID = 550;

  public static final EnumParameter<AbstractionProcedureCreator>
    CompositionalAutomataSynthesizer_AbstractionProcedureCreator =
    new EnumParameter<AbstractionProcedureCreator>
      (CompositionalAutomataSynthesizer_AbstractionProcedureCreator_ID,
       AbstractCompositionalModelAnalyzer_AbstractionProcedureCreator,
       AutomataSynthesisAbstractionProcedureFactory.getInstance());


  //net.sourceforge.waters.analysis.compositional.CompositionalConflictChecker
  private static final int CompositionalConflictChecker_PreselectingMethod_ID = 560;
  private static final int CompositionalConflictChecker_SelectionHeuristic_ID = 561;
  private static final int CompositionalConflictChecker_AbstractionProcedureCreator_ID = 562;

  public static final EnumParameter<PreselectingMethod>
    CompositionalConflictChecker_PreselectingMethod =
    new EnumParameter<PreselectingMethod>
      (CompositionalConflictChecker_PreselectingMethod_ID,
       AbstractCompositionalModelAnalyzer_PreselectingMethod,
       CompositionalConflictChecker.getPreselectingMethodFactoryStatic());

  public static final EnumParameter<SelectionHeuristicCreator>
    CompositionalConflictChecker_SelectionHeuristic =
    new EnumParameter<SelectionHeuristicCreator>
      (CompositionalConflictChecker_SelectionHeuristic_ID,
       AbstractCompositionalModelAnalyzer_SelectionHeuristic,
       ConflictSelectionHeuristicFactory.getInstance());

  public static final EnumParameter<AbstractionProcedureCreator>
    CompositionalConflictChecker_AbstractionProcedureCreator =
    new EnumParameter<AbstractionProcedureCreator>
      (CompositionalConflictChecker_AbstractionProcedureCreator_ID,
       AbstractCompositionalModelAnalyzer_AbstractionProcedureCreator,
       ConflictAbstractionProcedureFactory.getInstance());


  //net.sourceforge.waters.analysis.modular.ModularControllabilitySynthesizer
  private static final int ModularControllabilitySynthesizer_NonblockingSynthesis_ID = 600;
  private static final int ModularControllabilitySynthesizer_RemovingUnnecessarySupervisors_ID = 601;

  public static final BoolParameter ModularControllabilitySynthesizer_NonblockingSynthesis =
    new BoolParameter
      (ModularControllabilitySynthesizer_NonblockingSynthesis_ID,
       "Locally nonblocking supervisors",
       "Attempt to synthesise nonblocking supervisors each time a subsystem " +
       "is sent for monolithic synthesis. While this may help to remove some " +
       "blocking states, it does not ensure a globally nonblocking supervisor.",
       false);

  public static BoolParameter ModularControllabilitySynthesizer_RemovingUnnecessarySupervisors =
    new BoolParameter
      (ModularControllabilitySynthesizer_RemovingUnnecessarySupervisors_ID,
       "Remove unnecessary supervisors",
       "Check whether new superivsors impose additional constraints over " +
       "those previously computed, and remove those that do not.",
       true);


 //net.sourceforge.waters.model.analysis.ConflictChecker
  private static final int ConflictChecker_ConfiguredDefaultMarking_ID = 700;
  private static final int ConflictChecker_ConfiguredPreconditionMarking_ID = 701;

  public static final EventParameter ConflictChecker_ConfiguredDefaultMarking =
    new EventParameter
      (ConflictChecker_ConfiguredDefaultMarking_ID,
       "Marking proposition",
       "The model is considered nonblocking, if it possible to reach a state " +
       "marked by this proposition from every reachable state.",
       EventParameterType.PREVENT_NULL);

  public static final EventParameter ConflictChecker_ConfiguredPreconditionMarking =
    new EventParameter
      (ConflictChecker_ConfiguredPreconditionMarking_ID,
       "Precondition marking",
       "Precondition marking used for generalised conflict check.",
       EventParameterType.DEFAULT_NULL);


  //net.sourceforge.waters.model.analysis.des.ModelVerifier
  private static final int ModelVerifier_DetailedOutputEnabled_ID = 800;
  private static final int ModelVerifier_ShortCounterExampleRequested_ID = 801;

  public static final BoolParameter ModelVerifier_DetailedOutputEnabled =
    new BoolParameter
      (ModelVerifier_DetailedOutputEnabled_ID,
       "Compute counterexample",
       "Computate a counterexample if model checking gives a failed result.",
       true);

  public static final BoolParameter ModelVerifier_ShortCounterExampleRequested =
    new BoolParameter
      (ModelVerifier_ShortCounterExampleRequested_ID,
       "Short counterexample",
       "Try to compute a counterexample that is as short as possible.",
       true);

  //net.sourceforge.waters.model.analysis.bdd.BDDModelVerifier
  private static final int BDDModelVerifier_BDDPackage_ID = 900;
  private static final int BDDModelVerifier_InitialSize_ID = 901;
  private static final int BDDModelVerifier_PartitionSizeLimit_ID = 902;
  private static final int BDDModelVerifier_ReorderingEnabled_ID = 903;
  private static final int BDDModelVerifier_TransactionPartitioningStrategy_ID = 904;
  private static final int BDDModelVerifier_VariableOrdering_ID = 905;
  private static final int BDDModelVerifier_NodeLimit_ID = 906;

  public static final EnumParameter<BDDPackage> BDDModelVerifier_BDDPackage =
    new EnumParameter<BDDPackage>
      (BDDModelVerifier_BDDPackage_ID,
       "BDD package",
       "The BDD package used when running the algorithm.",
       BDDPackage.values());

  public static final IntParameter BDDModelVerifier_InitialSize =
    new IntParameter
      (BDDModelVerifier_InitialSize_ID,
       "Initial BDD table size",
       "The initial number of BDD nodes to be supported by the BDD package.",
       0, Integer.MAX_VALUE, 50000);

  public static final IntParameter BDDModelVerifier_PartitionSizeLimit =
    new IntParameter
      (BDDModelVerifier_PartitionSizeLimit_ID,
       "Partition size limit",
       "The maximum number of BDD nodes allowed in a transition relation BDD " +
       "before it is split when partitioning.",
       0, Integer.MAX_VALUE, 10000);

  public static final BoolParameter BDDModelVerifier_ReorderingEnabled =
    new BoolParameter
      (BDDModelVerifier_ReorderingEnabled_ID,
       "Dynamic variable reordering",
       "Try to improve the BDD variable ordering between iterations.",
       false);

  public static final EnumParameter<TransitionPartitioningStrategy> BDDModelVerifier_TransactionPartitioningStrategy =
    new EnumParameter<TransitionPartitioningStrategy>
      (BDDModelVerifier_TransactionPartitioningStrategy_ID,
       "Transition partitioning strategy",
       "The method used to split the transition relation BDD into " +
       "disjunctive components.",
       TransitionPartitioningStrategy.values());

  public static final EnumParameter<VariableOrdering> BDDModelVerifier_VariableOrdering =
    new EnumParameter<VariableOrdering>
      (BDDModelVerifier_VariableOrdering_ID,
       "Initial variable ordering",
       "The strategy to determine the initial ordering of the BDD variables.",
       VariableOrdering.values());

  public static final IntParameter BDDModelVerifier_NodeLimit =
    new IntParameter
     (BDDModelVerifier_NodeLimit_ID,
      "BDD Node limit",
      "Maximum number of BDD nodes allowed before aborting.",
      0, Integer.MAX_VALUE, Integer.MAX_VALUE);


  //net.sourceforge.waters.analysis.compositional.AbstractCompositionalModelVerifier
  private static final int AbstractCompositionalModelVerifier_TraceCheckingEnabled_ID = 1000;

  public static final BoolParameter AbstractCompositionalModelVerifier_TraceCheckingEnabled =
    new BoolParameter
      (AbstractCompositionalModelVerifier_TraceCheckingEnabled_ID,
       "Counterexample debugging",
       "When computing counterexamples, perform debug checks to ensure " +
       "that the counterexample is accepted after every abstraction step",
       false);


  //net.sourceforge.waters.cpp.analysis.NativeModelAnalyzer
  private static final int NativeModelAnalyzer_EventTreeEnabled_ID = 1100;

  public static final BoolParameter NativeModelAnalyzer_EventTreeEnabled =
    new BoolParameter
      (NativeModelAnalyzer_EventTreeEnabled_ID,
       "Use branching program",
       "Compile the event enablement condition into a branching program " +
       "to speed up synchronous product computation.",
       true);


  //net.sourceforge.waters.cpp.analysis.NativeConflictChecker
  private static final int NativeConflictChecker_ConflictCheckMode_ID = 1150;

  public static final EnumParameter<ConflictCheckMode> NativeConflictChecker_ConflictCheckMode =
    new EnumParameter<ConflictCheckMode>
      (NativeConflictChecker_ConflictCheckMode_ID,
       "Conflict check mode",
       "The algorithm used to store or explore the reverse transition relation",
       ConflictCheckMode.values(),
       ConflictCheckMode.NO_BACKWARDS_TRANSITIONS);


  //net.sourceforge.waters.analysis.tr.AbstractTRCompositionalModelAnalyzer
  private static final int AbstractTRCompositionalModelAnalyzer_PreselectionHeuristic_ID = 1200;
  private static final int AbstractTRCompositionalModelAnalyzer_SelectionHeuristic_ID = 1201;
  private static final int AbstractTRCompositionalModelAnalyzer_SimplifierCreator_ID = 1202;
  private static final int AbstractTRCompositionalModelAnalyzer_AlwaysEnabledEventsEnabled_ID = 1203;

  public static final EnumParameter<TRPreselectionHeuristic> AbstractTRCompositionalModelAnalyzer_PreselectionHeuristic =
    new EnumParameter<TRPreselectionHeuristic>
      (AbstractTRCompositionalModelAnalyzer_PreselectionHeuristic_ID,
       "Preselection method",
       "Preselection heuristic to generate groups of automata to consider " +
       "for composition.",
       AbstractTRCompositionalModelAnalyzer.getPreselectionHeuristicFactoryStatic());

  public static final EnumParameter<SelectionHeuristic<TRCandidate>> AbstractTRCompositionalModelAnalyzer_SelectionHeuristic =
    new EnumParameter<SelectionHeuristic<TRCandidate>>
      (AbstractTRCompositionalModelAnalyzer_SelectionHeuristic_ID,
       "Selection heuristic",
       "Heuristic to choose the group of automata to compose and simplify " +
       "from the options produced by the preselection method.",
       AbstractTRCompositionalModelAnalyzer.getSelectionHeuristicFactoryStatic());

  public static final EnumParameter<TRToolCreator<TransitionRelationSimplifier>> AbstractTRCompositionalModelAnalyzer_SimplifierCreator =
    new EnumParameter<TRToolCreator<TransitionRelationSimplifier>>
      (AbstractTRCompositionalModelAnalyzer_SimplifierCreator_ID,
       "Abstraction procedure",
       "Abstraction procedure to simplify automata during compositional " +
       "minimisation.");

  public static final BoolParameter AbstractTRCompositionalModelAnalyzer_AlwaysEnabledEventsEnabled =
    new BoolParameter
      (AbstractTRCompositionalModelAnalyzer_AlwaysEnabledEventsEnabled_ID,
       "Use always enabled events",
       "Detect events that are enabled in all states outside of the " +
       "subsystem being abstracted, and use this information to help with " +
       "minimisation.",
       true);


  //net.sourceforge.waters.analysis.gnonblocking.TRCompositionalConflictChecker
  private static final int TRCompositionalConflictChecker_SimplifierCreator_ID = 1210;

  public static final EnumParameter<TRToolCreator<TransitionRelationSimplifier>>
    TRCompositionalConflictChecker_SimplifierCreator =
    new EnumParameter<TRToolCreator<TransitionRelationSimplifier>>
      (TRCompositionalConflictChecker_SimplifierCreator_ID,
       AbstractTRCompositionalModelAnalyzer_SimplifierCreator,
       TRCompositionalConflictChecker.getTRSimplifierFactoryStatic());


  //net.sourceforge.waters.analysis.gnonblocking.CompositionalGeneralisedConflictChecker
  private static final int CompositionalGeneralisedConflictChecker_PreselectingHeuristic_ID = 1300;
  private static final int CompositionalGeneralisedConflictChecker_SelectingHeuristic_ID = 1301;

  public static final EnumParameter<PreselectingHeuristicFactory>
    CompositionalGeneralisedConflictChecker_PreselectingHeuristic =
    new EnumParameter<>
      (CompositionalGeneralisedConflictChecker_PreselectingHeuristic_ID,
       "Preselection method",
       "Preselection heuristic to generate groups of automata to consider " +
       "for composition.",
       PreselectingHeuristicFactory.getInstance());

  public static final EnumParameter<SelectingHeuristicFactory>
    CompositionalGeneralisedConflictChecker_SelectingHeuristic =
    new EnumParameter<>
      (CompositionalGeneralisedConflictChecker_SelectingHeuristic_ID,
       "Selection heuristic",
       "Heuristic to choose the group of automata to compose and simplify " +
       "from the options produced by the preselection method.",
       SelectingHeuristicFactory.getInstance());


  //net.sourceforge.waters.model.analysis.SynchronousProductBuilder
  private static final int SynchronousProductBuilder_DetailedOutputEnabled_ID = 1500;
  private static final int SynchronousProductBuilder_OutputName_ID = 1501;
  private static final int SynchrnousProductBuilder_OutputKind_ID = 1502;
  private static final int SynchronousProductBuilder_RemovingSelfloops_ID = 1503;

  public static final BoolParameter SynchronousProductBuilder_DetailedOutputEnabled =
    new BoolParameter
      (SynchronousProductBuilder_DetailedOutputEnabled_ID,
       "Build automaton model",
       "Disable this to suppress the creation of a synchronous product " +
       "automaton, and only run for statistics.",
       true);

  public static final StringParameter SynchronousProductBuilder_OutputName =
    new StringParameter
      (SynchronousProductBuilder_OutputName_ID,
       "Output name",
       "Name for the generated synchronous product automaton",
       "sync");

  public static final ComponentKindParameter SynchronousProductBuilder_OutputKind =
    new ComponentKindParameter
      (SynchrnousProductBuilder_OutputKind_ID,
       "Output kind",
       "Type of the generated synchronous product automaton.");

  public static final BoolParameter SynchronousProductBuilder_RemovingSelfloops =
    new BoolParameter
      (SynchronousProductBuilder_RemovingSelfloops_ID,
       "Remove Selfloops",
       "Remove events that appear only as selfloop on every state," +
       "as well as propositions that appear on all states, from the result.",
       true);   //isRemovingSelfloops())


  //net.sourceforge.waters.analysis.monolithic.MonolithicSynchronousProductBuilder
  private static final int MonolithicSynchronousProductBuilder_PruningDeadlocks_ID = 1600;

  public static final BoolParameter MonolithicSynchronousProductBuilder_PruningDeadlocks =
    new BoolParameter
      (MonolithicSynchronousProductBuilder_PruningDeadlocks_ID,
       "Prune deadlocks",
       "Stop synchronous product construction when encountering " +
       "states that are a deadlock in one of the components.",
       false);


  //org.supremica.automata.waters.SupremicaModelAnalyzer
  private static final int SupremicaModelAnalyzer_EnsuringUncontrollablesInPlant_ID = 5000;

  public static final BoolParameter SupremicaModelAnalyzer_EnsuringUncontrollablesInPlant =
    new BoolParameter
      (SupremicaModelAnalyzer_EnsuringUncontrollablesInPlant_ID,
       "Add uncontrollables to plant",
       "Treat uncontrollable events that appear in specifications " +
       "but not in plants as always enabled.",
       true);


  //org.supremica.automata.waters.SupremicaSynchronousProductBuilder
  private static final int SupremicaSynchronousProductBuilder_ShortStateNames_ID = 5100;
  private static final int SupremicaSynchronousProductBuilder_StateNameSeparator_ID = 5101;
  private static final int SupremicaSynchronousProductBuilder_MarkingUncontrollableStatesAsForbidden_ID = 5102;
  private static final int SupremicaSynchronousProductBuilder_ExpandingForbiddenStates_ID = 5103;
  private static final int SupremicaSynchronousProductBuilder_RememberingDisabledEvents_ID = 5104;
  private static final int SupremicaSynchronousProductBuilder_SynchronisingOnUnobservableEvents_ID = 5105;

  public static final BoolParameter SupremicaSynchronousProductBuilder_ShortStateNames =
    new BoolParameter
      (SupremicaSynchronousProductBuilder_ShortStateNames_ID,
       "Short state names",
       "Use short state instead of detailed state tuple information.",
       false);

  public static final StringParameter SupremicaSynchronousProductBuilder_StateNameSeparator =
    new StringParameter
      (SupremicaSynchronousProductBuilder_StateNameSeparator_ID,
       "State name separator",
       "Separator for state tuple components when using long state names.",
       ".");

  public static final BoolParameter SupremicaSynchronousProductBuilder_MarkingUncontrollableStatesAsForbidden =
    new BoolParameter
      (SupremicaSynchronousProductBuilder_MarkingUncontrollableStatesAsForbidden_ID,
       "Forbid uncontrollable states",
       "Mark uncontrollable states as forbidden in the synchronous composition.",
       true);

  public static final BoolParameter SupremicaSynchronousProductBuilder_ExpandingForbiddenStates =
    new BoolParameter
      (SupremicaSynchronousProductBuilder_ExpandingForbiddenStates_ID,
       "Expand forbidden states",
       "If checked, transitions from forbidden states are examined. " +
       "Otherwise forbidden states are considered terminal.",
       true);

  public static final BoolParameter SupremicaSynchronousProductBuilder_RememberingDisabledEvents =
    new BoolParameter
      (SupremicaSynchronousProductBuilder_RememberingDisabledEvents_ID,
       "Remember disabled events",
       "Add transitions to a 'dump' state for all events enabled in the plant " +
       "but disabled by a specification.",
       false);

  public static final BoolParameter SupremicaSynchronousProductBuilder_SynchronisingOnUnobservableEvents =
    new BoolParameter
      (SupremicaSynchronousProductBuilder_SynchronisingOnUnobservableEvents_ID,
       "Synchronise on unobservable events",
       "If checked, treat unoberservable as shared events in synchronisation, " +
       "otherwise allow them to be executed independently by each component.",
       true);


  //org.supremica.automata.waters.SupremicaSynthesizer
  private static final int SupremicaSynthesizer_Purging_ID = 5200;
  private static final int SupremicaSynthesizer_SupervisorReductionFactory_ID = 5201;

  public static final BoolParameter SupremicaSynthesizer_Purging =
    new BoolParameter
      (SupremicaSynthesizer_Purging_ID,
       "Purge result",
       "Remove unreachable states from the synthesised supervisor.",
       true);

  public static final EnumParameter<SupervisorReductionFactory>
    SupremicaSynthesizer_SupervisorReductionFactory =
    new EnumParameter<SupervisorReductionFactory>
     (SupremicaSynthesizer_SupervisorReductionFactory_ID,
      "Supervisor reduction",
      "Method of supervisor reduction to be used after synthesis.",
      SupremicaSupervisorReductionFactory.values());


  //org.supremica.automata.waters.SupremicaMonolithicVerifier
  private static final int SupremicaMonolithicVerifier_DetailedOutputEnabled_ID = 5300;

  public static final BoolParameter SupremicaMonolithicVerifier_DetailedOutputEnabled =
    new BoolParameter
      (SupremicaMonolithicVerifier_DetailedOutputEnabled_ID,
       "Print counterexample",
       "Show trace to bad state as info in log.",
       false);

  //net.sourceforge.waters.analysis.monolithic.MonolithicControlLoopChecker


  //net.sourceforge.waters.analysis.modular.ModularControlLoopChecker
  private static final int ModularControlLoopChecker_NodeLimit_ID = 5500;
  private static final int ModularControlLoopChecker_TransitionLimit_ID = 5501;
  private static final int ModularControlLoopChecker_MergeVersion_ID = 5502;
  private static final int ModularControlLoopChecker_SelectVersion_ID = 5503;

  public static final IntParameter ModularControlLoopChecker_NodeLimit =
    new IntParameter
      (ModularControlLoopChecker_NodeLimit_ID,
       "State limit",
       "The maximum number of states allowed for intermediate verification " +
       "attempts.",
       0, Integer.MAX_VALUE, Integer.MAX_VALUE);

  public static final IntParameter ModularControlLoopChecker_TransitionLimit =
    new IntParameter
      (ModularControlLoopChecker_TransitionLimit_ID,
       "Transition limit",
       "The maximum number of transitions allowed for intermediate " +
       "verification attempts.",
       0, Integer.MAX_VALUE, Integer.MAX_VALUE);

  public static final EnumParameter<AutomataGroup.MergeVersion>
    ModularControlLoopChecker_MergeVersion =
    new EnumParameter<AutomataGroup.MergeVersion>
     (ModularControlLoopChecker_MergeVersion_ID,
      "Selection heuristic",
      "The heuristic to determine which components to include in " +
      "subsequent verification attempts based on the counterexample from " +
      "the previous attempt.",
      AutomataGroup.MergeVersion.values(),
      AutomataGroup.MergeVersion.MaxCommonEvents);

  // Not used:
  public static final EnumParameter<AutomataGroup.SelectVersion>
    ModularControlLoopChecker_SelectVersion =
    new EnumParameter<AutomataGroup.SelectVersion>
     (ModularControlLoopChecker_SelectVersion_ID,
      "Select Version",
      "",
      AutomataGroup.SelectVersion.values());

}
