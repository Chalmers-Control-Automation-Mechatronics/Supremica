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

package net.sourceforge.waters.model.analysis.des;

import net.sourceforge.waters.analysis.options.OptionPage;
import net.sourceforge.waters.model.analysis.AnalysisConfigurationException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.cli.ArgumentSource;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * A factory interface for all types of model verifiers.
 *
 * @author Robi Malik
 */

public interface ModelAnalyzerFactory extends ArgumentSource
{

  //#########################################################################
  //# Object Construction
  /**
   * Creates a conflict checker.
   */
  public ConflictChecker createConflictChecker
    (ProductDESProxyFactory factory)
    throws AnalysisConfigurationException;

  /**
   * Creates a controllability checker.
   */
  public ControllabilityChecker createControllabilityChecker
    (ProductDESProxyFactory factory)
    throws AnalysisConfigurationException;

  /**
   * Creates a control-loop checker.
   */
  public ControlLoopChecker createControlLoopChecker
    (ProductDESProxyFactory factory)
    throws AnalysisConfigurationException;

  /**
   * Creates a deadlock checker.
   */
  public DeadlockChecker createDeadlockChecker
    (ProductDESProxyFactory factory)
    throws AnalysisConfigurationException;

  /**
   * Creates a diagnosability checker.
   */
  public DiagnosabilityChecker createDiagnosabilityChecker
    (ProductDESProxyFactory desFactory)
    throws AnalysisConfigurationException;

  /**
   * Creates a language inclusion checker.
   */
  public LanguageInclusionChecker createLanguageInclusionChecker
    (ProductDESProxyFactory factory)
    throws AnalysisConfigurationException;

  /**
   * Creates a synchronous product builder.
   */
  public SynchronousProductBuilder createSynchronousProductBuilder
    (ProductDESProxyFactory factory)
    throws AnalysisConfigurationException;

  /**
   * Creates a supervisor synthesiser.
   */
  public SupervisorSynthesizer createSupervisorSynthesizer
    (ProductDESProxyFactory factory)
    throws AnalysisConfigurationException;

  /**
   * Creates a state counter.
   */
  public StateCounter createStateCounter
    (ProductDESProxyFactory factory)
    throws AnalysisConfigurationException;


  //#########################################################################
  //# Options
  public void registerOptions(OptionPage db);


//  //#########################################################################
//  //# Command Line Arguments
//  public void parse(CommandLineOptionContext context,
//                    ModelAnalyzer analyzer,
//                    ListIterator<String> iter);

  /**
   * Configures the given model analyser according to any command line
   * arguments passed to this factory. This method is called while parsing
   * command line arguments, before loading of any models. Hence, the model
   * analyser does not yet have its input model when this method is called.
   */
  public void configure(ModelAnalyzer analyzer)
    throws AnalysisConfigurationException;

  /**
   * Configures the given compiler according to any command line arguments
   * passed to this factory.
   */
  public void configure(ModuleCompiler compiler);

  /**
   * Configures the given model analyser after command line arguments parsing
   * and compiling of models. This method is called just before running the
   * model verifier to provide a second pass of configuration. When it is
   * called, the model analyser's input model is available.
   */
  public void postConfigure(ModelAnalyzer analyzer) throws AnalysisException;


  //#########################################################################
  //# Supremica Options
  /**
   * Configures a BDD model verifier from Supremica options, if these
   * are available.
   */
  public void configureFromOptions(ModelAnalyzer analyzer);

}
