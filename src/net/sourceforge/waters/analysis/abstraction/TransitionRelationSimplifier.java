//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.analysis.abstraction
//# CLASS:   TransitionRelationSimplifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.abstraction;

import java.util.List;

import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TRPartition;
import net.sourceforge.waters.model.analysis.Abortable;
import net.sourceforge.waters.model.analysis.AnalysisException;


/**
 * An interface defining common functionality of algorithms that
 * simplify a transition relation based on state merging.
 *
 * @author Robi Malik
 */

public interface TransitionRelationSimplifier
  extends Abortable
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
   * Sets the IDs of the default marking with respect to which
   * transition relations are simplified.
   * @param defaultID       The default (omega) marking used,
   *                        or <CODE>-1</CODE>.
   */
  public void setDefaultMarkingID(final int defaultID);
  /**
   * Sets the IDs of the marking propositions with respect to which
   * transition relations are simplified.
   * @param preconditionID  The precondition (alpha) marking used,
   *                        or <CODE>-1</CODE>.
   * @param defaultID       The default (omega) marking used,
   *                        or <CODE>-1</CODE>.
   */
  public void setPropositions(final int preconditionID, final int defaultID);

  /**
   * Sets whether this simplifier applies the computed partition automatically.
   * If set to <CODE>true</CODE> (the default), then any partition computed
   * by a call to {@link #run()} will immediately be applied to the
   * transition relation. Otherwise, states have to be merged by the
   * user.
   */
  public void setAppliesPartitionAutomatically(boolean apply);

  /**
   * Gets whether this simplifier applies the computed partition automatically.
   * @see #setAppliesPartitionAutomatically(boolean)
   * setAppliesPartitionAutomatically()
   */
  public boolean getAppliesPartitionAutomatically();

  /**
   * Sets a listener to be called before this simplifier starts executing
   * on a transition relation and after it finishes. This can be used
   * to execute custom code before and after simplification, and to
   * cancel simplification on user-specific conditions.
   * @see TRSimplificationListener
   */
  public void setSimplificationListener(TRSimplificationListener listener);

  /**
   * Runs this simplifier.
   * When run, the simplifier may destructively modify its transition
   * relation by modifying markings or transitions.
   * @return <CODE>true</CODE> if the transition relation was changed,
   *         <CODE>false</CODE> if no simplification was possible.
   * @see #getResultPartition()
   */
  public boolean run() throws AnalysisException;

  /**
   * Returns whether this transition relation simplifier can produce
   * a result partition to relate states in the input automaton to states
   * in the output automaton. Many standard simplifiers, such as bisimulation,
   * provide a partition, but some simplifiers, such as subset construction,
   * do not.
   * @see #getResultPartition()
   */
  public boolean isPartitioning();

  /**
   * Gets the partition produced by the last {@link #run()}.
   * @return The calculated partition, or <CODE>null</CODE> to indicate
   *         a partition that does not change the automaton.
   * @see #isPartitioning()
   */
  public TRPartition getResultPartition();

  /**
   * Returns whether the abstraction produced by the last {@link #run()}
   * is weak observation equivalent to the input automaton. This information
   * is used to enable more efficient trace computation.
   */
  public boolean isObservationEquivalentAbstraction();

  /**
   * Returns whether this simplifier handles always enabled events
   * ({@link EventStatus#STATUS_OUTSIDE_ALWAYS_ENABLED} specially.
   * This information can be used to avoid the effort to find always
   * enabled events when they are not needed.
   */
  public boolean isSupportingAlwaysEnabledEvents();

  /**
   * Returns whether the last call to {@link #run()} has removed any
   * markings of the given proposition. If precondition markings have
   * been changed, the caller may have to take special precautions to
   * find a suitable counterexample end state.
   */
  public boolean isReducedMarking(int propID);

  /**
   * Creates a new statistics record.
   * This method resets the gathering of statistics by this simplifier
   * by creating a new record. The previously used record remains unchanged
   * by this operation.
   * @return The new statistics record.
   */
  public abstract TRSimplifierStatistics createStatistics();

  /**
   * Gets a record with details of the performance of this simplifier.
   * The returned record contains accumulative information over all
   * invocations since the simplifier was created, or since
   * {@link #createStatistics()} was called.
   */
  public TRSimplifierStatistics getStatistics();

  /**
   * Stores statistics in the given list. This method is used to
   * collect detailed statistics for each individual simplifier invoked by
   * this simplifier.
   * @param  list           Statistics records are added to the end of this
   *                        list, in order of invocation of the simplifiers.
   */
  public void collectStatistics(List<TRSimplifierStatistics> list);

  /**
   * Cleans up. This method removes all temporary data and results
   * associated with any previous call to {@link #run()}.
   */
  public void reset();

}
