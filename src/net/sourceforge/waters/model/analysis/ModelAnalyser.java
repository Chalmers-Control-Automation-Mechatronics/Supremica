//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   ModelAnalyser
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import net.sourceforge.waters.model.des.AutomatonProxy;
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
 * <P>The input model may contain several automata, in which case the
 * system to be analysed is their synchronous product.</P>
 *
 * @author Robi Malik
 */

public interface ModelAnalyser
{

  //#########################################################################
  //# Invocation
  /**
   * Runs the analysis operation associated with this model analyser.
   * @return <CODE>true</CODE> if analysis was completed with successful
   *         result, <CODE>false</CODE> if analysis was completed with
   *         unsuccessful result,
   * @throws NullPointerException to indicate that no model has been
   *         specified.
   * @throws AnalysisException to indicate that analysis was aborted or
   *         could not be completed due to errors in the model or due to
   *         resource limitations.
   */
  public boolean run() throws AnalysisException;


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
   * Sets a new automaton to be investigated by this model analyser.
   * This method creates initialises the model analyser to check a model
   * consisting of a single automaton.
   */
  public void setModel(AutomatonProxy aut);


  //#########################################################################
  //# Parameters
  /**
   * Sets the node limit for this model verifier.
   * If set, the node limit is the maximum number of nodes the verifier
   * is allowed to keep in memory at any one time. If this number is
   * exceeded, an {@link OverflowException} is thrown.
   * A 'node' here represents a basic unit of memory such as a state
   * in a synchronous product or a BDD node.
   * @param  limit  The new node limit, or {@link Integer#MAX_VALUE} to
   *                indicate that no node limit is to be used.
   */
  public void setNodeLimit(final int limit);

  /**
   * Gets the node limit for this model verifier.
   * @return The current node limit, or {@link Integer#MAX_VALUE} to indicate
   *         that no node limit is used.
   * @see    #setNodeLimit(int)
   */
  public int getNodeLimit();

  /**
   * Sets the transition limit for this model verifier.
   * If set, the transition limit is the maximum number of transitions the
   * verifier is allowed to keep in memory at any one time. If this number
   * is exceeded, an {@link OverflowException} is thrown.
   * Many algorithms do not explicitly store any transitions and can ignore
   * this parameter.
   * @param  limit  The new transition limit, or {@link Integer#MAX_VALUE} to
   *                indicate that no transition limit is to be used. A value
   *                of&nbsp;0 can be used to request that transitions should
   *                not be stored explicitly.
   */
  public void setTransitionLimit(final int limit);

  /**
   * Gets the transition limit for this model verifier.
   * @return The current transition limit, or {@link Integer#MAX_VALUE} to
   *         indicate that no transition limit is used.
   * @see    #setTransitionLimit(int)
   */
  public int getTransitionLimit();


  //#########################################################################
  //# Accessing the Result
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


  //#########################################################################
  //# Aborting
  /**
   * Requests that a running model analyser aborts.
   * This does not necessarily cause an abort, it merely sets a flag to
   * request abort at a later time. It may take some time for the model
   * analyser to check this flag and react, or the request may not be
   * obeyed at all. If a model analyser aborts, it will throw an
   * {@link AbortException} from its {@link #run()} method.
   */
  public void requestAbort();

  /**
   * Returns whether the model analyser has been requested to abort.
   */
  public boolean isAborting();

}
