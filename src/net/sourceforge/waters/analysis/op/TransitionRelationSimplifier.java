//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.analysis.op
//# CLASS:   TransitionRelationSimplifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.op;

import java.util.Collection;

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
  public ObserverProjectionTransitionRelation getTransitionRelation();

  /**
   * Sets a new transition relation to be modified by this simplifier.
   */
  public void setTransitionRelation
    (final ObserverProjectionTransitionRelation rel);

  /**
   * Gets the ID of the silent (tau) event used for simplification.
   */
  public int getHiddenEventID();

  /**
   * Sets the ID of the silent (tau) event used for simplification.
   */
  public void setHiddenEventID(final int event);

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
   * @return The calculated partitioning. Each array in the collection
   *         defines the state codes comprising an equivalence class
   *         to be merged into a single state.
   * @throws IllegalStateException if this method is called before a
   *         {@link #run()}, or if the {@link #run()} resulted in no
   *         change.
   */
  public Collection<int[]> getResultPartition();

}
