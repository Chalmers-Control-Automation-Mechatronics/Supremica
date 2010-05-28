//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.analysis.op
//# CLASS:   TransitionRelationSimplifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.op;

import java.util.List;

import net.sourceforge.waters.model.analysis.AnalysisException;


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
  public ListBufferTransitionRelation getTransitionRelation();

  /**
   * Sets a new transition relation to be modified by this simplifier.
   */
  public void setTransitionRelation
    (final ListBufferTransitionRelation rel);

  /**
   * Runs this simplifier.
   * When run, the simplifier may destructively modify its transition
   * relation.
   * @return <CODE>true</CODE> if the transition relation was changed,
   *         <CODE>false</CODE> if no simplification was possible.
   */
  public boolean run() throws AnalysisException;

  /**
   * Destructively applies the computed partitioning to the simplifier's
   * transition relation.
   * @return <CODE>true</CODE> if the transition relation has been modified in
   *         any way.
   */
  public boolean applyResultPartition() throws AnalysisException;

  /**
   * Gets the partition produced by the last {@link #run()}.
   * @return The calculated partitioning. Each array in the collection
   *         defines the state codes comprising an equivalence class
   *         to be merged into a single state.
   * @throws IllegalStateException if this method is called before a
   *         {@link #run()}, or if the {@link #run()} resulted in no
   *         change.
   */
  public List<int[]> getResultPartition();

}
