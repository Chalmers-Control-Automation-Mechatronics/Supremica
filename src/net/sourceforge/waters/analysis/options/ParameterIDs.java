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

public class ParameterIDs
{
  //net.sourceforge.waters.model.analysis.ModelAnalyzer
  public static final int ModelAnalyzer_DetailedOutputEnabled = 0;
  public static final int ModelAnalyzer_KindTranslator = 1;
  public static final int ModelAnalyzer_NodeLimit = 2;
  public static final int ModelAnalyzer_TransitionLimit = 3;

  //net.sourceforge.waters.model.analysis.ModelBuilder
  public static final int ModelBuilder_OutputName = 100;

  //net.sourceforge.waters.model.analysis.SupervisorSynthesizer
  public static final int SupervisorSynthesizer_ControllableSynthesis = 200;
  public static final int SupervisorSynthesizer_NonblockingSynthesis = 201;
  public static final int SupervisorSynthesizer_NormalSynthesis = 202;
  public static final int SupervisorSynthesizer_ConfiguredDefaultMarking = 203;
  public static final int SupervisorSynthesizer_SupervisorLocalisationEnabled = 204;
  public static final int SupervisorSynthesizer_SupervisorReductionFactory = 205;
  public static final int SupervisorSynthesizer_SupervisorNamePrefix = 206;

  //net.sourceforge.waters.analysis.monolithic.MonolithicSynthesizer;

  //net.sourceforge.waters.analysis.compositional.CompositionalAutomataSynthesizer

  //net.sourceforge.waters.analysis.compositional.AbstractCompositionalModelAnalyzer
  public static final int AbstractCompositionalModelAnalyzer_AbstractionProcedureCreator = 500;
  public static final int AbstractCompositionalModelAnalyzer_BlockedEventsEnabled = 501;
  public static final int AbstractCompositionalModelAnalyzer_FailingEventsEnabled = 504;
  public static final int AbstractCompositionalModelAnalyzer_InternalStateLimit = 505;
  public static final int AbstractCompositionalModelAnalyzer_InternalTransitionLimit = 506;
  public static final int AbstractCompositionalModelAnalyzer_LowerInternalStateLimit = 507;
  public static final int AbstractCompositionalModelAnalyzer_MonolithicAnalyzer = 508;
  public static final int AbstractCompositionalModelAnalyzer_MonolithicDumpFile = 509;
  public static final int AbstractCompositionalModelAnalyzer_MonolithicStatelimit = 510;
  public static final int AbstractCompositionalModelAnalyzer_MonolithicTransitionLimit = 511;
  public static final int AbstractCompositionalModelAnalyzer_PreselectingMethod = 512;
  public static final int AbstractCompositionalModelAnalyzer_PruningDeadlocks = 513;
  public static final int AbstractCompositionalModelAnalyzer_SelectionHeurisitc = 514;
  public static final int AbstractCompositionalModelAnalyzer_SelfLoopOnlyEventsEnabled = 515;
  public static final int AbstractCompositionalModelAnalyzer_SubumptionEnabled = 516;
  public static final int AbstractCompositionalModelAnalyzer_SynchronousProductBuilder = 517;
  public static final int AbstractCompositionalModelAnalyzer_UpperInternalStateLimit = 518;
  public static final int AbstractCompositionalModelAnalyzer_UsingSpecialEvents = 519;

  //net.sourceforge.waters.analysis.modular.ModularControllabilitySynthesizer
  public static final int ModularControllabilitySynthesizer_IncludesAllAutomata = 600;
  public static final int ModularControllabilitySynthesizer_LocalNonblockingSupported = 601;
  public static final int ModularControllabilitySynthesizer_RemovesUnnecessarySupervisors = 602;

 //net.sourceforge.waters.model.analysis.ConflictChecker
  public static final int ConflictChecker_ConfiguredDefaultMarking = 700;
  public static final int ConflictChecker_ConfiguredPreconditionMarking = 701;

  //net.sourceforge.waters.model.analysis.des.ModelVerifier
  public static final int ModelVerifier_CounterExampleEnabled = 800;
  public static final int ModelVerifier_ShortCounterExampleRequested = 801;

  //net.sourceforge.waters.model.analysis.bdd.BDDModelVerifier
  public static final int BDDModelVerifier_BDDPackage = 900;
  public static final int BDDModelVerifier_InitialSize = 901;
  public static final int BDDModelVerifier_PartitionSizeLimit = 902;
  public static final int BDDModelVerifier_ReorderingEnabled = 903;
  public static final int BDDModelVerifier_TransactionPartitioningStrategy = 904;
  public static final int BDDModelVerifier_VariableOrdering = 905;

  //net.sourceforge.waters.analysis.compositional.AbstractCompositionalModelVerifier
  public static final int AbstractCompositionalModelVerifier_TraceCheckingEnabled = 1000;

  //net.sourceforge.waters.cpp.analysis.NativeConflictChecker
  public static final int NativeConflictChecker_ConflictCheckMode = 1100;

  //net.sourceforge.waters.analysis.tr.AbstractTRCompositionalModelAnalyzer
  public static final int AbstractTRCompositionalModelAnalyzer_PreselectionHeuristic = 1200;
  public static final int AbstractTRCompositionalModelAnalyzer_SelectionHeuristic = 1201;
  public static final int AbstractTRCompositionalModelAnalyzer_SimplifierCreator = 1202;
  public static final int AbstractTRCompositionalModelAnalyzer_AlwaysEnabledEventsEnabled = 1203;

}
