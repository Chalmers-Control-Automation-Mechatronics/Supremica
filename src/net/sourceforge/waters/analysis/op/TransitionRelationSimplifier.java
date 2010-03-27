//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.analysis.op
//# CLASS:   TransitionRelationSimplifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.op;

import net.sourceforge.waters.model.analysis.AnalysisException;

import gnu.trove.TIntObjectHashMap;


/**
 * An interface defining common functionality of algorithms that
 * simplify a transition relation based on observation equivalence.
 *
 * @author Robi Malik
 */

public interface TransitionRelationSimplifier
{

  /**
   * Gets the transition relation modified by this simplifier.
   */
  public ObserverProjectionTransitionRelation getTransitionRelation();

  /**
   * Sets a new transition relation to be modified by this simplifier.
   */
  public void setTransitionRelation
    (final ObserverProjectionTransitionRelation rel);

  /**
   * Runs this simplifier.
   * When run, the simplifier will destructively modify its transition
   * relation.
   * @return <CODE>true</CODE> if the transition relation was changed,
   *         <CODE>false</CODE> if no simplification was possible.
   */
  public boolean run() throws AnalysisException;

  /**
   * Gets the partition produced by the last {@link #run()}.
   * @return A map that assigns to each state number in the modified
   *         transition relation after the {@link #run()} an array of
   *         state numbers in the original transition relation.
   * @throws IllegalStateException if this method is called before a
   *         {@link #run()}, or if the {@link #run()} resulted in no
   *         change.
   */
  public TIntObjectHashMap<int[]> getStateClasses();

}
