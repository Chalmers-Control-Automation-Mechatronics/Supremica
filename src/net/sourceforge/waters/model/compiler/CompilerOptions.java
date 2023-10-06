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

package net.sourceforge.waters.model.compiler;

import net.sourceforge.waters.model.options.BooleanOption;
import net.sourceforge.waters.model.options.ParameterBindingListOption;


/**
 * Static container of options used by the {@link ModuleCompiler}.
 *
 * @author Robi Malik
 */

public class CompilerOptions
{

  //#########################################################################
  //# Dummy Constructor
  private CompilerOptions()
  {
  }


  //#########################################################################
  //# Option Names
  public static final String OPTION_ModuleCompiler_ParameterBindings =
    "parameterBindings";
  public static final String OPTION_ModuleCompiler_IncludeInstantiation =
    "includeInstantiation";
  public static final String OPTION_ModuleCompiler_BackgroundCompiler =
    "backgroundCompiler";
  public static final String OPTION_ModuleCompiler_OptimizingCompiler =
    "optimizingCompiler";
  public static final String OPTION_ModuleCompiler_EFSMCompiler =
    "efsmCompiler";
  public static final String OPTION_ModuleCompiler_NormalizingCompiler =
    "normalizingCompiler";
  public static final String OPTION_ModuleCompiler_AutomatonVariablesCompiler =
    "automatonVariablesCompiler";


  //#########################################################################
  //# Options
  public static final ParameterBindingListOption PARAMETER_BINDINGS =
  new ParameterBindingListOption
    (OPTION_ModuleCompiler_ParameterBindings, "Parameter bindings",
     "Set binding for a module parameter", "-D");
  public static final BooleanOption INCLUDE_INSTANTIATION = new BooleanOption
    (OPTION_ModuleCompiler_IncludeInstantiation, "Include Instantiation",
     "Enable instantiation and other advanced features", "-inst", true);
  public static final BooleanOption BACKGROUND_COMPILER = new BooleanOption
    (OPTION_ModuleCompiler_BackgroundCompiler, "Background Compiler",
     "Compile automatically while editing", null, true);
  public static final BooleanOption OPTIMIZING_COMPILER = new BooleanOption
    (OPTION_ModuleCompiler_OptimizingCompiler, "Optimizing Compiler",
     "Remove redundant events, transitions, and components when compiling",
     "-opt", true);
  public static final BooleanOption EFSM_COMPILER = new BooleanOption
    (OPTION_ModuleCompiler_EFSMCompiler, "EFSM Compiler",
     "Expand extended finite-state machine (EFSM) variables and guards",
     "-efsm", true);
  public static final BooleanOption NORMALIZING_COMPILER = new BooleanOption
    (OPTION_ModuleCompiler_NormalizingCompiler, "Normalizing Compiler",
     "Use normalising EFSM compiler", "-norm", true);
  public static final BooleanOption AUTOMATON_VARIABLES_COMPILER =
  new BooleanOption
    (OPTION_ModuleCompiler_AutomatonVariablesCompiler,
     "Automaton Variables Compiler",
     "Allow automaton names in EFSM guards", "-autvars", false);

}
