//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   ModelVerifierFactory
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import java.util.List;

import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * A factory interface for all types of model verifiers.
 *
 * @author Robi Malik
 */

public interface ModelVerifierFactory
{

  //#########################################################################
  //# Object Construction
  /**
   * Creates a conflict checker.
   */
  public ConflictChecker createConflictChecker
    (ProductDESProxyFactory factory);

  /**
   * Creates a controllability checker.
   */
  public ControllabilityChecker createControllabilityChecker
    (ProductDESProxyFactory factory);

  /**
   * Creates a control-loop checker.
   */
  public ControlLoopChecker createControlLoopChecker
    (ProductDESProxyFactory factory);

  /**
   * Creates a language inclusion checker.
   */
  public LanguageInclusionChecker createLanguageInclusionChecker
    (ProductDESProxyFactory factory);


  //#########################################################################
  //# Command Line Arguments
  /**
   * Configures the given model verifier according to any command line
   * arguments passed to this factory. This method is called while
   * parsing command line arguments, before loading of any models.
   * Hence, the model verifier does not yet have its input model when
   * this method is called.
   * @return A string array containing all arguments that could not
   *         be processed. These arguments are to be considered as
   *         file names by the command line tool.
   */
  public List<String> configure(ModelVerifier verifier);

  /**
   * Configures the given model verifier after command line arguments
   * parsing and compiling of models. This method is called just before
   * running the model verifier to provide a second pass of configuration.
   * When it is called, the model verifier's input model is available.
   */
  public void postConfigure(ModelVerifier verifier) throws AnalysisException;

  /**
   * Configures the given compiler according to any command line
   * arguments passed to this factory.
   */
  public void configure(ModuleCompiler compiler);

}
