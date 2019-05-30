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
  //Current Highest Number: 30
  //Freed Numbers:


  //net.sourceforge.waters.model.analysis.ModelAnalyser
  public static final int ModelAnalyzer_setDetailedOutputEnabled = 0;
  public static final int ModelAnalyzer_setKindTranslator = 1;
  public static final int ModelAnalyzer_setNodeLimit = 2;
  public static final int ModelAnalyzer_setTransitionLimit = 3;

  //net.sourceforge.waters.model.analysis.ModelBuilder

  public static final int ModelBuilder_setOutputName = 4;

  //net.sourceforge.waters.model.analysis.SupervisorSynthesizer

  public static final int SupervisorSynthesizer_setConfiguredDefaultMarking = 5;
  public static final int SupervisorSynthesizer_setNondeterminismEnabled = 6;
  public static final int SupervisorSynthesizer_setSupervisorLocalisationEnabled = 7;
  public static final int SupervisorSynthesizer_setSupervisorReductionFactory = 8;

  //net.sourceforge.waters.analysis.monolithic.MonolithicSynthesizer;

  public static final int MonolithicSynthesizer_setNonblockingSupported = 9;


  //net.sourceforge.waters.analysis.compositional.CompositionalAutomataSynthesizer
  public static final int CompositionalAutomataSynthesizer_setSupervisorNamePrefix = 10;

  //net.sourceforge.waters.analysis.compositional.AbstractCompositionalSynthesizer

  public static final int AbstractCompositionalSynthesizer_setAbstractionProcedureCreator = 11;
  public static final int AbstractCompositionalSynthesizer_setBlockedEventsEnabled = 12;
  public static final int AbstractCompositionalSynthesizer_setConfiguredDefaultMarking = 13;
  public static final int AbstractCompositionalSynthesizer_setConfiguredPreconditionMarking = 14;
  public static final int AbstractCompositionalSynthesizer_setFailingEventsEnabled = 15;
  public static final int AbstractCompositionalSynthesizer_setInternalStateLimit = 16;
  public static final int AbstractCompositionalSynthesizer_setInternalTransitionLimit = 17;
  public static final int AbstractCompositionalSynthesizer_setLowerInternalStateLimit = 18;
  public static final int AbstractCompositionalSynthesizer_setMonolithicAnalyzer = 19;
  public static final int AbstractCompositionalSynthesizer_setMonolithicDumpFileName = 20;
  public static final int AbstractCompositionalSynthesizer_setMonolithicStatelimit = 21;
  public static final int AbstractCompositionalSynthesizer_setMonolithicTransitionLimit = 22;
  public static final int AbstractCompositionalSynthesizer_setPreselectingMethod = 23;
  public static final int AbstractCompositionalSynthesizer_setPruningDeadlocks = 24;
  public static final int AbstractCompositionalSynthesizer_setSelectionHeurisitc = 25;
  public static final int AbstractCompositionalSynthesizer_setSelfLoopOnlyEventsEnabled = 26;
  public static final int AbstractCompositionalSynthesizer_setSubumptionEnabled = 27;
  public static final int AbstractCompositionalSynthesizer_setSynchronousProductBuilder = 28;
  public static final int AbstractCompositionalSynthesizer_setUpperInternalStateLimit = 29;
  public static final int AbstractCompositionalSynthesizer_setUsingSpecialEvents = 30;


}
