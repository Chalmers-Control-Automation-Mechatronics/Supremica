//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:   TRSimplificationListener
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

/**
 * Callback interface for transition relation simplifiers.
 * This interface contains two methods to execute custom code
 * when a transition relation simplifier starts or finishes
 * execution.
 *
 * @see TransitionRelationSimplifier
 * @author Robi Malik
 */

public interface TRSimplificationListener
{

  /**
   * Callback executed before a transition relation simplifier starts
   * execution.
   * @param   simplifier  The simplifier that just starts execution.
   * @return  Whether simplification is allowed to start.
   *          The simplifier will only execute if this method returns
   *          <CODE>true</CODE>. If <CODE>false</CODE> is returned,
   *          simplification will be skipped, and the result reported
   *          as unchanged. The callback {@link
   *          #onSimplificationFinish(TransitionRelationSimplifier, boolean)
   *          onSimplificationFinish()} will not be called in this case.
   */
  public boolean onSimplificationStart(TransitionRelationSimplifier simplifier);

  /**
   * Callback executed after a transition relation simplifier has finished
   * execution. This method is only called on successful completion, not
   * in case of an exception, and not when simplification was cancelled
   * by the {@link #onSimplificationStart(TransitionRelationSimplifier)
   * onSimplificationStart()} handler.
   * @param   simplifier  The simplifier that has just finished.
   *                      Additional information can be retrieved from
   *                      this object.
   * @param   result      The result returned by the simplifier's {@link
   *                      TransitionRelationSimplifier#run() run()} method.
   *                      A value of <CODE>true</CODE> indicates that the
   *                      input transition relation has been modified.
   */
  public void onSimplificationFinish(TransitionRelationSimplifier simplifier,
                                     boolean result);

}
