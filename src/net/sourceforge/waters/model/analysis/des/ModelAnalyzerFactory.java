//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   ModelVerifierFactory
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis.des;

import java.util.ListIterator;

import net.sourceforge.waters.model.analysis.AnalysisConfigurationException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * A factory interface for all types of model verifiers.
 *
 * @author Robi Malik
 */

public interface ModelAnalyzerFactory
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
   * Creates a language inclusion checker.
   */
  public LanguageInclusionChecker createLanguageInclusionChecker
    (ProductDESProxyFactory factory)
    throws AnalysisConfigurationException;

  /**
   * Creates a supervisor synthesiser.
   */
  public SupervisorSynthesizer createSupervisorSynthesizer
    (ProductDESProxyFactory factory)
    throws AnalysisConfigurationException;


  //#########################################################################
  //# Command Line Arguments
  public void parse(ListIterator<String> iter);

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

}
