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

import net.sourceforge.waters.analysis.abstraction.SupervisorReductionFactory;
import net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier;
import net.sourceforge.waters.analysis.bdd.BDDPackage;
import net.sourceforge.waters.analysis.bdd.TransitionPartitioningStrategy;
import net.sourceforge.waters.analysis.bdd.VariableOrdering;
import net.sourceforge.waters.analysis.compositional.AbstractCompositionalModelAnalyzer.PreselectingMethod;
import net.sourceforge.waters.analysis.compositional.AbstractionProcedureCreator;
import net.sourceforge.waters.analysis.compositional.SelectionHeuristic;
import net.sourceforge.waters.analysis.compositional.SelectionHeuristicCreator;
import net.sourceforge.waters.analysis.trcomp.TRCandidate;
import net.sourceforge.waters.analysis.trcomp.TRPreselectionHeuristic;
import net.sourceforge.waters.analysis.trcomp.TRToolCreator;
import net.sourceforge.waters.cpp.analysis.ConflictCheckMode;
import net.sourceforge.waters.model.base.ComponentKind;

public class ParameterIDs
{
  //net.sourceforge.waters.model.analysis.ModelAnalyzer
  public static final int ModelAnalyzer_DetailedOutputEnabled = 0;
  public static final int ModelAnalyzer_KindTranslator = 1;                     //Unused
  public static final int ModelAnalyzer_NodeLimit = 2;
  public static final int ModelAnalyzer_TransitionLimit = 3;


  public static final BoolParameter ModelAnalyzer_BoolParameter_DetailedOutputEnabled = new BoolParameter
    (ModelAnalyzer_DetailedOutputEnabled,
     "Detailed output",
     "Compute full output, e.g., synthesised supervisor automata or " +
     "counterexample.",
     true);

  public static final IntParameter ModelAnalyzer_IntParameter_NodeLimit = new IntParameter
    (ModelAnalyzer_NodeLimit,
     "Node limit",
     "The maximum number of nodes the analyser is allowed to keep " +
     "in memory at any one time.",
     0, Integer.MAX_VALUE, Integer.MAX_VALUE);

   public static final IntParameter ModelAnalyzer_IntParameter_TransitionLimit =   new IntParameter
     (ModelAnalyzer_TransitionLimit,
      "Transition limit",
      "The maximum number of transitions the analyser is allowed to " +
      "keep in memory at any one time.",
      0, Integer.MAX_VALUE, Integer.MAX_VALUE);

  //net.sourceforge.waters.model.analysis.ModelBuilder
  public static final int ModelBuilder_OutputName = 100;

  public static final StringParameter ModelBuilder_StringParameter_OutputName =   new StringParameter
    (ModelBuilder_OutputName,
     "Supervisor name prefix",
     "Name or name prefix for synthesised supervisors.",
     "Supervisor");

  //net.sourceforge.waters.model.analysis.SupervisorSynthesizer
  public static final int SupervisorSynthesizer_ControllableSynthesis = 200;
  public static final int SupervisorSynthesizer_NonblockingSynthesis = 201;
  public static final int SupervisorSynthesizer_NormalSynthesis = 202;                          //Unused
  public static final int SupervisorSynthesizer_ConfiguredDefaultMarking = 203;
  public static final int SupervisorSynthesizer_SupervisorLocalisationEnabled = 204;
  public static final int SupervisorSynthesizer_SupervisorReductionFactory = 205;
  public static final int SupervisorSynthesizer_Supremica_SupervisorReductionFactory = 206;
  public static final int SupervisorSynthesizer_SupervisorNamePrefix = 207;                       //Unused

  public static final BoolParameter SupervisorSynthesizer_BoolParameter_ControllableSynthesis = new BoolParameter
    (SupervisorSynthesizer_ControllableSynthesis,
     "Controllable supervisor",
     "Synthesise a controllable supervisor.",
     true);

  public static final BoolParameter SupervisorSynthesizer_BoolParameter_NonblockingSynthesis = new BoolParameter
    (SupervisorSynthesizer_NonblockingSynthesis,
     "Nonblocking supervisor",
     "Synthesise a supervisor that is nonblocking supervisor with respect " +
     "to the configured marking proposition.",
       true );//                          isNonblockingSynthesis());

  public static final EventParameter SupervisorSynthesizer_EventParameter_ConfiguredDefaultMarking =
    new EventParameter(SupervisorSynthesizer_ConfiguredDefaultMarking,
                       "Marking proposition",
                       "If synthesising a nonblocking supervisor, it will be nonblocking "
                                              + "with respect to this proposition.",
                       EventParameterType.DEFAULT_NULL);


  public static final BoolParameter SupervisorSynthesizer_BoolParameter_SupervisorLocalisationEnabled =   new BoolParameter
    (SupervisorSynthesizer_SupervisorLocalisationEnabled,
     "Localize supervisors",
     "If using supervisor reduction, create a separate supervisor " +
     "for each controllable event that needs to be disabled.",
     true);

  public static final EnumParameter<SupervisorReductionFactory> SupervisorSynthesizer_EnumParameter_SupervisorReductionFactory =
    new EnumParameter<SupervisorReductionFactory>(SupervisorSynthesizer_SupervisorReductionFactory,
                                                  "Supervisor reduction",
                                                  "Method of supervisor reduction to be used after synthesis");

  public static final EnumParameter<SupervisorReductionFactory> SupervisorSynthesizer_Supremica_EnumParameter_SupervisorReductionFactory =
    new EnumParameter<SupervisorReductionFactory>(SupervisorSynthesizer_Supremica_SupervisorReductionFactory,
                                                  "Supervisor reduction",
                                                  "Method of supervisor reduction to be used after synthesis");

  //net.sourceforge.waters.analysis.monolithic.MonolithicSynthesizer;

  //net.sourceforge.waters.analysis.compositional.CompositionalAutomataSynthesizer

  //net.sourceforge.waters.analysis.compositional.AbstractCompositionalModelAnalyzer
  public static final int AbstractCompositionalModelAnalyzer_AbstractionProcedureCreator = 500;
  public static final int AbstractCompositionalModelAnalyzer_BlockedEventsEnabled = 501;
  public static final int AbstractCompositionalModelAnalyzer_FailingEventsEnabled = 504;
  public static final int AbstractCompositionalModelAnalyzer_InternalStateLimit = 505;
  public static final int AbstractCompositionalModelAnalyzer_InternalTransitionLimit = 506;
  public static final int AbstractCompositionalModelAnalyzer_LowerInternalStateLimit = 507;             //unused
  public static final int AbstractCompositionalModelAnalyzer_MonolithicAnalyzer = 508;                    //unused
  public static final int AbstractCompositionalModelAnalyzer_MonolithicDumpFile = 509;
  public static final int AbstractCompositionalModelAnalyzer_MonolithicStatelimit = 510;
  public static final int AbstractCompositionalModelAnalyzer_MonolithicTransitionLimit = 511;
  public static final int AbstractCompositionalModelAnalyzer_PreselectingMethod = 512;
  public static final int AbstractCompositionalModelAnalyzer_PruningDeadlocks = 513;
  public static final int AbstractCompositionalModelAnalyzer_SelectionHeurisitc = 514;
  public static final int AbstractCompositionalModelAnalyzer_SelfLoopOnlyEventsEnabled = 515;
  public static final int AbstractCompositionalModelAnalyzer_SubumptionEnabled = 516;
  public static final int AbstractCompositionalModelAnalyzer_SynchronousProductBuilder = 517;         //unused
  public static final int AbstractCompositionalModelAnalyzer_UpperInternalStateLimit = 518;           //unused
  public static final int AbstractCompositionalModelAnalyzer_UsingSpecialEvents = 519;                //unused

  public static final EnumParameter<AbstractionProcedureCreator> AbstractCompositionalModelAnalyzer_EnumParameter_AbstractionProcedureCreator =
    new EnumParameter<AbstractionProcedureCreator>(AbstractCompositionalModelAnalyzer_AbstractionProcedureCreator,
                                                   "Abstraction procedure",
                                                   "Abstraction procedure to simplify automata during compositional "
                                                                            + "minimisation.");

  public static final BoolParameter AbstractCompositionalModelAnalyzer_BoolParameter_BlockedEventsEnabled = new BoolParameter
    (AbstractCompositionalModelAnalyzer_BlockedEventsEnabled,
     "Use blocked events",
     "Detect and remove events known to be globablly disabled.",
     true);

  public static final BoolParameter AbstractCompositionalModelAnalyzer_BoolParameter_FailingEventsEnabled = new BoolParameter
    (AbstractCompositionalModelAnalyzer_FailingEventsEnabled,
     "Use failing events",
     "Detect events that only lead to blocking states and " +
     "simplify automata based on this information.",
     true);

  public static final IntParameter AbstractCompositionalModelAnalyzer_IntParameter_InternalStateLimit = new IntParameter
    (AbstractCompositionalModelAnalyzer_InternalStateLimit,
     "Internal state limit",
     "The maximum number of states allowed for intermediate automata.",
     0, Integer.MAX_VALUE, Integer.MAX_VALUE);

  public static final IntParameter AbstractCompositionalModelAnalyzer_IntParameter_InternalTransitionLimit = new IntParameter
    (AbstractCompositionalModelAnalyzer_InternalTransitionLimit,
     "Internal transition limit",
     "The maximum number of transitions allowed for intermediate automata.",
     0, Integer.MAX_VALUE, Integer.MAX_VALUE);

  public static FileParameter AbstractCompositionalModelAnalyzer_FileParameter_MonolithicDumpFile =  new FileParameter
  (AbstractCompositionalModelAnalyzer_MonolithicDumpFile,
   "Dump file name",
   "If set, any abstracted model will be written to this file " +
   "before being sent for monolithic verification.");

  public static IntParameter AbstractCompositionalModelAnalyzer_IntParameter_MonolithicStatelimit =  new IntParameter
  (AbstractCompositionalModelAnalyzer_MonolithicStatelimit,
   "Monolithic state limit",
   "The maximum number of states allowed during monolithic analysis " +
   "attempts.",
   0, Integer.MAX_VALUE, Integer.MAX_VALUE);

  public static IntParameter AbstractCompositionalModelAnalyzer_IntParameter_MonolithicTransitionLimit = new IntParameter
  (AbstractCompositionalModelAnalyzer_MonolithicTransitionLimit,
   "Monolithic transition limit",
   "The maximum number of transitions allowed during monolithic " +
   "analysis attempts.",
   0, Integer.MAX_VALUE, Integer.MAX_VALUE);


  public static EnumParameter<PreselectingMethod> AbstractCompositionalModelAnalyzer_EnumParameter_PreselectingMethod = new EnumParameter<PreselectingMethod>
  (AbstractCompositionalModelAnalyzer_PreselectingMethod,
    "Preselection method",
    "Preselection heuristic to choose groups of automata to consider " +
    "for composition.");

 public static BoolParameter AbstractCompositionalModelAnalyzer_BoolParameter_PruningDeadlocks =  new BoolParameter
 (AbstractCompositionalModelAnalyzer_PruningDeadlocks,
  "Prune deadlocks",
  "Allow synchronous product construction to stop when encountering " +
  "states that are a deadlock in one of the components.",
  true);

  public static EnumParameter<SelectionHeuristicCreator> AbstractCompositionalModelAnalyzer_EnumParameter_SelectionHeurisitc =
    new EnumParameter<SelectionHeuristicCreator>(AbstractCompositionalModelAnalyzer_SelectionHeurisitc,
                                                 "Selection heuristic",
                                                 "Heuristic to choose the group of automata to compose and simplify "
                                                                        + "from the options produced by the preselection method.");


public static BoolParameter AbstractCompositionalModelAnalyzer_BoolParameter_SelfLoopOnlyEventsEnabled = new BoolParameter
(AbstractCompositionalModelAnalyzer_SelfLoopOnlyEventsEnabled,
 "Use selfloop-only events",
 "Detect events that are appear only as selfloop outside of the " +
 "subsystem being abstracted, and use this information to help with " +
 "minimisation.",
 true);

public static BoolParameter AbstractCompositionalModelAnalyzer_BoolParameter_SubumptionEnabled = new BoolParameter
(AbstractCompositionalModelAnalyzer_SubumptionEnabled,
 "Use subumption test",
 "Suppress candidate groups of automata that are supersets of " +
 "other candidates.",
 true);

  //net.sourceforge.waters.analysis.modular.ModularControllabilitySynthesizer
  public static final int ModularControllabilitySynthesizer_IncludesAllAutomata = 600;                  //unused
  public static final int ModularControllabilitySynthesizer_LocalNonblockingSupported = 601;            //unused
  public static final int ModularControllabilitySynthesizer_RemovesUnnecessarySupervisors = 602;

  public static BoolParameter ModularControllabilitySynthesizer_BoolParameter_RemovesUnnecessarySupervisors = new BoolParameter
  (ModularControllabilitySynthesizer_RemovesUnnecessarySupervisors,
   "Remove unnecessary supervisors",
   "Check whether new superivsors impose additional constraints over " +
   "those previously computed, and remove those that do not.", true);

 //net.sourceforge.waters.model.analysis.ConflictChecker
  public static final int ConflictChecker_ConfiguredDefaultMarking = 700;
  public static final int ConflictChecker_ConfiguredPreconditionMarking = 701;

  //net.sourceforge.waters.model.analysis.des.ModelVerifier
  public static final int ModelVerifier_CounterExampleEnabled = 800;                //unused
  public static final int ModelVerifier_ShortCounterExampleRequested = 801;


  public static BoolParameter ModelVerifier_BoolParameter_ShortCounterExampleRequested = new BoolParameter
    (ModelVerifier_ShortCounterExampleRequested,
     "Short counterexample",
     "Try to compute a counterexample that is as short as possible.",
     true);

  //net.sourceforge.waters.model.analysis.bdd.BDDModelVerifier
  public static final int BDDModelVerifier_BDDPackage = 900;
  public static final int BDDModelVerifier_InitialSize = 901;
  public static final int BDDModelVerifier_PartitionSizeLimit = 902;
  public static final int BDDModelVerifier_ReorderingEnabled = 903;
  public static final int BDDModelVerifier_TransactionPartitioningStrategy = 904;
  public static final int BDDModelVerifier_VariableOrdering = 905;

  public static EnumParameter<BDDPackage> BDDModelVerifier_EnumParameter_BDDPackage = new EnumParameter<BDDPackage>
  (BDDModelVerifier_BDDPackage,
    "BDD package",
    "The BDD package used when running the algorithm",
    BDDPackage.values());

  public static IntParameter BDDModelVerifier_IntParameter_InitialSize = new IntParameter
    (BDDModelVerifier_InitialSize,
     "Initial BDD table size",
     "The initial number of BDD nodes to be supported by the BDD package",
     0, Integer.MAX_VALUE, 50000);

  public static IntParameter BDDModelVerifier_IntParameter_PartitionSizeLimit = new IntParameter
    (BDDModelVerifier_PartitionSizeLimit,
     "Partition size limit",
     "The maximum number of BDD nodes allowed in a transition relation BDD " +
     "before it is split when partitioning",
     0, Integer.MAX_VALUE, 10000);

  public static BoolParameter BDDModelVerifier_BoolParameter_ReorderingEnabled = new BoolParameter
    (BDDModelVerifier_ReorderingEnabled,
     "Dynamic variable reordering",
     "Try to improve the BDD variable ordering between iterations",
     false);

  public static EnumParameter<TransitionPartitioningStrategy> BDDModelVerifier_EnumParameter_TransactionPartitioningStrategy =
    new EnumParameter<TransitionPartitioningStrategy>(BDDModelVerifier_TransactionPartitioningStrategy,
                                                      "Transition partitioning strategy",
                                                      "The method used to split the transition relation BDD into "
                                                                                          + "disjunctive components",
                                                      TransitionPartitioningStrategy.values());

  public static EnumParameter<VariableOrdering> BDDModelVerifier_EnumParameter_VariableOrdering =
    new EnumParameter<VariableOrdering>(BDDModelVerifier_VariableOrdering,
                                        "Initial variable ordering",
                                        "The strategy to determine the initial ordering of the BDD variables",
                                        VariableOrdering.values());

  //net.sourceforge.waters.analysis.compositional.AbstractCompositionalModelVerifier
  public static final int AbstractCompositionalModelVerifier_TraceCheckingEnabled = 1000;

  public static BoolParameter AbstractCompositionalModelVerifier_BoolParameter_TraceCheckingEnabled = new BoolParameter
    (AbstractCompositionalModelVerifier_TraceCheckingEnabled,
     "Counterexample debugging",
     "When computing counterexamples, perform debug checks to ensure " +
     "that the counterexample is accepted after every abstraction step",
     false);

  //net.sourceforge.waters.cpp.analysis.NativeConflictChecker
  public static final int NativeConflictChecker_ConflictCheckMode = 1100;

  public static EnumParameter<ConflictCheckMode> NativeConflictChecker_EnumParameter_ConflictCheckMode =
    new EnumParameter<ConflictCheckMode>(NativeConflictChecker_ConflictCheckMode,
                                         "Conflict check mode",
                                         "The algorithm used to store or explore the reverse transition relation",
                                         ConflictCheckMode.values(),
                                         ConflictCheckMode.COMPUTED_BACKWARDS_TRANSITIONS);

  //net.sourceforge.waters.analysis.tr.AbstractTRCompositionalModelAnalyzer
  public static final int AbstractTRCompositionalModelAnalyzer_PreselectionHeuristic = 1200;
  public static final int AbstractTRCompositionalModelAnalyzer_SelectionHeuristic = 1201;
  public static final int AbstractTRCompositionalModelAnalyzer_SimplifierCreator = 1202;
  public static final int AbstractTRCompositionalModelAnalyzer_AlwaysEnabledEventsEnabled = 1203;

  public static EnumParameter<TRPreselectionHeuristic> AbstractTRCompositionalModelAnalyzer_EnumParameter_PreselectionHeuristic = new EnumParameter<TRPreselectionHeuristic>
  (AbstractTRCompositionalModelAnalyzer_PreselectionHeuristic,
    "Preselection method",
    "Preselection heuristic to generate groups of automata to consider " +
    "for composition.");

  public static EnumParameter<SelectionHeuristic<TRCandidate>> AbstractTRCompositionalModelAnalyzer_EnumParameter_SelectionHeuristic =
  new EnumParameter<SelectionHeuristic<TRCandidate>>
  (AbstractTRCompositionalModelAnalyzer_SelectionHeuristic,
   "Selection heuristic",
   "Heuristic to choose the group of automata to compose and simplify " +
   "from the options produced by the preselection method.");

  public static EnumParameter<TRToolCreator<TransitionRelationSimplifier>> AbstractTRCompositionalModelAnalyzer_EnumParameter_SimplifierCreator =
  new EnumParameter<TRToolCreator<TransitionRelationSimplifier>>
  (AbstractTRCompositionalModelAnalyzer_SimplifierCreator,
   "Abstraction procedure",
   "Abstraction procedure to simplify automata during compositional " +
   "minimisation.");

  public static BoolParameter AbstractTRCompositionalModelAnalyzer_BoolParameter_AlwaysEnabledEventsEnabled = new BoolParameter
    (AbstractTRCompositionalModelAnalyzer_AlwaysEnabledEventsEnabled,
     "Use always enabled events",
     "Detect events that are enabled in all states outside of the " +
     "subsystem being abstracted, and use this information to help with " +
     "minimisation.",
     true);

  //net.sourceforge.waters.analysis.gnonblocking.CompositionalGeneralisedConflictChecker
  public static final int CompositionalGeneralisedConflictChecker_PreselectingHeuristic = 1300;
  public static final int CompositionalGeneralisedConflictChecker_SelectingHeuristic = 1301;
  /*
  public static EnumParameter<PreselectingHeuristic> CompositionalGeneralisedConflictChecker_EnumParameter_PreselectingHeuristic = new EnumParameter<PreselectingHeuristic>
  (CompositionalGeneralisedConflictChecker_PreselectingHeuristic,
    "Preselection method",
    "Preselection heuristic to generate groups of automata to consider " +
    "for composition.");


  public static EnumParameter<SelectingHeuristic> CompositionalGeneralisedConflictChecker_EnumParameter_SelectingHeuristic = new EnumParameter<SelectingHeuristic>
  (CompositionalGeneralisedConflictChecker_SelectingHeuristic,
   "Selection heuristic",
   "Heuristic to choose the group of automata to compose and simplify " +
   "from the options produced by the preselection method.");

   */

  //net.sourceforge.waters.model.analysis.des.AutomatonBuilder
  public static final int AutomatonBuilder_OutputKind = 1400;

  public static final ComponentKindParameter AutomatonBuilder_ComponentKindParameter_OutputKind = new ComponentKindParameter
    (AutomatonBuilder_OutputKind,
     "Output kind",
     "Type of the generated automaton.",
     ComponentKind.values());

  //net.sourceforge.waters.model.analysis.des.SynchronousProductBuilder
  public static final int SynchronousProductBuilder_RemovingSelfloops = 1500;

  //net.sourceforge.waters.analysis.monolithic.MonolithicSynchronousProductBuilder
  public static final int MonolithicSynchronousProductBuilder_PruningDeadlocks = 1600;

  //net.sourceforge.waters.analysis.monolithic.TRAbstractModelAnalyzer
  public static final int TRAbstractModelAnalyzer_PruningDeadLocks = 1700;

  //org.supremica.automata.waters.SupremicaModelAnalyzer
  public static final int SupremicaModelAnalyzer_EnsuringUncontrollablesInPlant = 5000;

  //org.supremica.automata.waters.SupremicaSynchronousProductBuilder
  public static final int SupremicaSynchronousProductBuilder_ShortStateNames = 5100;
  public static final int SupremicaSynchronousProductBuilder_StateNameSeparator = 5101;
  public static final int SupremicaSynchronousProductBuilder_MarkingUncontrollableStatesAsForbidden = 5102;
  public static final int SupremicaSynchronousProductBuilder_ExpandingForbiddenStates = 5103;
  public static final int SupremicaSynchronousProductBuilder_RememberingDisabledEvents = 5104;
  public static final int SupremicaSynchronousProductBuilder_SynchronisingOnUnobservableEvents = 5105;

  //org.supremica.automata.waters.SupremicaSynthesizer
  public static final int SupremicaSynthesizer_Purging = 5200;
}
