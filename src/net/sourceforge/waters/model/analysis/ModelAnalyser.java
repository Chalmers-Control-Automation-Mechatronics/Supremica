//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   ModelAnalyser
//###########################################################################
//# $Id: ModelAnalyser.java,v 1.5 2006-11-03 01:00:07 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.analysis;

import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * <P>The main model analyser interface.</P>
 *
 * <P>A model analyser takes a finite-state machine model as input and
 * performs a particular kind of analysis on it. This may be verification
 * ({@link ModelVerifier}) to determine whether a certain property is
 * satisfied, or a synthesis operation that computes some new automaton
 * from the input model.</P>
 *
 * @author Robi Malik
 */

public interface ModelAnalyser
{

  //#########################################################################
  //# Invocation
  /**
   * Runs the analysis operation associated with this model analyser.
   * @return <CODE>true</CODE> if analysis was successful,
   *         <CODE>false</CODE> otherwise.
   * @throws NullPointerException to indicate that no model has been
   *         specified.
   */
  public boolean run();


  //#########################################################################
  //# Simple Access Methods
  /**
   * Gets the factory used by this model analyser to create result
   * objects.
   */
  public ProductDESProxyFactory getFactory();

  /**
   * Gets the model under investigation by this model analyser.
   */
  public ProductDESProxy getModel();

  /**
   * Sets a new model to be investigated by this model analyser.
   */
  public void setModel(ProductDESProxy model);

  /**
   * Gets the result of the last analysis run.
   * @return An object containing all information associated with the
   *         analysis result, or <CODE>null</CODE> if this method is
   *         called before {@link #run() run()} has been called.
   */
  public AnalysisResult getAnalysisResult();

  /**
   * Resets the analysis result computed by this model analyser.
   * This method is used when the model or other parameters are changed
   * after a run, to indicate that the previous results are no longer
   * valid.
   */
  public void clearAnalysisResult();

}
