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
 * simplify a transition relation based on state merging.
 *
 * @author Robi Malik
 */

public interface TransitionRelationSimplifier
{

  /**
   * Gets the preferred configuration of a transition relation to
   * be modified by this simplifier. If a transition relation with
   * another configuration is passed into the simplifier, it may
   * be reconfigured before simplification starts.
   * @return Combination of flags defined in
   *         {@link ListBufferTransitionRelation}.
   *         A return value of&nbsp;0 indicates no preference.
   * @see ListBufferTransitionRelation#CONFIG_PREDECESSORS
   * @see ListBufferTransitionRelation#CONFIG_SUCCESSORS
   * @see ListBufferTransitionRelation#CONFIG_ALL
   */
  public int getPreferredInputConfiguration();

  /**
   * Sets a preferred configuration for the transition relation when this
   * simplifier exits. Simplifiers may or may not honour thus setting, but
   * they should respect it when reconfiguring the transition relation
   * prior to producing the output partition.
   * @param config Combination of flags defined in
   *               {@link ListBufferTransitionRelation}.
   *               A value of&nbsp;0 indicates no preference.
   * @see ListBufferTransitionRelation#CONFIG_PREDECESSORS
   * @see ListBufferTransitionRelation#CONFIG_SUCCESSORS
   * @see ListBufferTransitionRelation#CONFIG_ALL
   */
  public void setPreferredOutputConfiguration(int config);

  /**
   * Gets the transition relation modified by this simplifier.
   */
  public ListBufferTransitionRelation getTransitionRelation();

  /**
   * Sets a new transition relation to be modified by this simplifier.
   */
  public void setTransitionRelation(ListBufferTransitionRelation rel);


  /**
   * Sets whether this simplifier applies the computed partition automatically.
   * If set to <CODE>true</CODE> (the default), then any partition computed
   * by a call to {@link #run()} will immediately be applied to the
   * transition relation. Otherwise, states have to be merged manually
   * by calling {@link #applyResultPartition()}.
   */
  public void setAppliesPartitionAutomatically(boolean apply);

  /**
   * Gets whether this simplifier applies the computed partition automatically.
   * @see #setAppliesPartitionAutomatically(boolean) setAppliesPartitionAutomatically()
   */
  public boolean getAppliesPartitionAutomatically();

  /**
   * Runs this simplifier.
   * When run, the simplifier may destructively modify its transition
   * relation by modifying markings or transitions.
   * @return <CODE>true</CODE> if the transition relation was changed,
   *         <CODE>false</CODE> if no simplification was possible.
   * @see #applyResultPartition()
   * @see #getResultPartition()
   */
  public boolean run() throws AnalysisException;

  /**
   * Gets the partition produced by the last {@link #run()}.
   * @return The calculated partitioning. Each array in the collection
   *         defines the state codes comprising an equivalence class
   *         to be merged into a single state. A partition that does not
   *         change the automaton is indicated by a return value of
   *         <CODE>null</CODE>.
   */
  public List<int[]> getResultPartition();

  /**
   * Returns whether the abstraction produced by the last {@link #run()}
   * is weak observation equivalent to the input automaton. This information
   * is used to enabled more efficient trace computation.
   */
  public boolean isObservationEquivalentAbstraction();

  /**
   * Returns whether the last call to {@link #run()} has removed any
   * markings of the given proposition. If precondition markings have
   * been changed, the caller may have to take special precautions to
   * find a suitable counterexample end state.
   */
  public boolean isReducedMarking(int propID);

  /**
   * Cleans up. This method removes all temporary data and results
   * associated with any previous call to {@link #run()}.
   */
  public void reset();

}
