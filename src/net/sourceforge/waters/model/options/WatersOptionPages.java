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

package net.sourceforge.waters.model.options;

import net.sourceforge.waters.analysis.abstraction.StepSimplifierFactory;
import net.sourceforge.waters.analysis.trcomp.ChainSimplifierFactory;
import net.sourceforge.waters.model.analysis.des.AnalysisOperation;
import net.sourceforge.waters.model.compiler.CompilerOptions;


/**
 * Container class for option pages used within Waters only.
 * This class contains option pages that can be used by ode that does not
 * link to Supremica licensed code.
 *
 * @author Robi Malik
 */

public class WatersOptionPages
{

  //#########################################################################
  //# Class Constants
  public static final SimpleLeafOptionPage COMPILER =
    new SimpleLeafOptionPage("gui.compiler", "Compiler",
                             CompilerOptions.PARAMETER_BINDINGS,
                             CompilerOptions.INCLUDE_INSTANTIATION,
                             CompilerOptions.BACKGROUND_COMPILER,
                             CompilerOptions.OPTIMIZING_COMPILER,
                             CompilerOptions.EFSM_COMPILER,
                             CompilerOptions.NORMALIZING_COMPILER,
                             CompilerOptions.AUTOMATON_VARIABLES_COMPILER);

  public static final AnalysisOptionPage CONFLICT =
    new AnalysisOptionPage(AnalysisOperation.CONFLICT_CHECK);
  public static final AnalysisOptionPage CONTROLLABILITY =
    new AnalysisOptionPage(AnalysisOperation.CONTROLLABILITY_CHECK);
  public static final AnalysisOptionPage CONTROL_LOOP =
    new AnalysisOptionPage(AnalysisOperation.CONTROL_LOOP_CHECK);
  public static final AnalysisOptionPage DEADLOCK =
    new AnalysisOptionPage(AnalysisOperation.DEADLOCK_CHECK);
  public static final AnalysisOptionPage DIAGNOSABILITY =
    new AnalysisOptionPage(AnalysisOperation.DIAGNOSABILITY_CHECK, true);
  public static final AnalysisOptionPage LANGUAGE_INCLUSION =
    new AnalysisOptionPage(AnalysisOperation.LANGUAGE_INCLUSION_CHECK);
  public static final AnalysisOptionPage STATE_COUNT =
    new AnalysisOptionPage(AnalysisOperation.STATE_COUNT);
  public static final AnalysisOptionPage SYNCHRONOUS_PRODUCT =
    new AnalysisOptionPage(AnalysisOperation.SYNCHRONOUS_PRODUCT);
  public static final AnalysisOptionPage SYNTHESIS =
    new AnalysisOptionPage(AnalysisOperation.SYNTHESIS);

  public static final SimplifierOptionPage SIMPLIFICATION =
    new SimplifierOptionPage
      ("waters.analysis.simplification",
       "Simplifiers",
       StepSimplifierFactory.class.getName(),
       "org.supremica.automata.waters.SupremicaSimplifierFactory",
       ChainSimplifierFactory.class.getName());

  public static final AggregatorOptionPage ANALYSIS =
    new AggregatorOptionPage("Analysis", CONFLICT,
                             CONTROLLABILITY, CONTROL_LOOP,
                             DEADLOCK, DIAGNOSABILITY,
                             LANGUAGE_INCLUSION, STATE_COUNT,
                             SYNCHRONOUS_PRODUCT, SYNTHESIS,
                             SIMPLIFICATION);

  public static final AggregatorOptionPage WATERS_ROOT =
    new AggregatorOptionPage("Waters", COMPILER, ANALYSIS);

}
