//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.op
//# CLASS:   AbstractObservationEquivalenceTRSimplifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.op;

import java.util.Collection;

import net.sourceforge.waters.model.analysis.OverflowException;


/**
 * @author Robi Malik
 */

public abstract class AbstractObservationEquivalenceTRSimplifier
  extends AbstractTRSimplifier
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new bisimulation simplifier without a transition relation.
   */
  public AbstractObservationEquivalenceTRSimplifier()
  {
    this(null);
  }

  /**
   * Creates a new bisimulation simplifier for the given transition relation.
   */
  public AbstractObservationEquivalenceTRSimplifier
    (final ListBufferTransitionRelation rel)
  {
    super(rel);
    mEquivalence = Equivalence.OBSERVATION_EQUIVALENCE;
    mTransitionRemovalMode = TransitionRemoval.NONTAU;
    mMarkingMode = MarkingMode.UNCHANGED;
    mTransitionLimit = Integer.MAX_VALUE;
  }


  //#########################################################################
  //# Configuration
  /**
   * Sets the equivalence by which the transition relation is partitioned.
   * @see Equivalence
   */
  public void setEquivalence(final Equivalence mode)
  {
    mEquivalence = mode;
    if (mode == Equivalence.BISIMULATION) {
      mTransitionRemovalMode = TransitionRemoval.NONE;
    }
  }

  /**
   * Gets the equivalence by which the transition relation is partitioned.
   * @see Equivalence
   */
  public Equivalence getEquivalence()
  {
    return mEquivalence;
  }

  /**
   * Sets the mode which redundant transitions are to be removed.
   * @see TransitionRemoval
   */
  public void setTransitionRemovalMode(final TransitionRemoval mode)
  {
    mTransitionRemovalMode = mode;
  }

  /**
   * Gets the mode which redundant transitions are to be removed.
   * @see TransitionRemoval
   */
  public TransitionRemoval getTransitionRemovalMode()
  {
    return mTransitionRemovalMode;
  }

  /**
   * Sets the mode how implicit markings are handled.
   * @see MarkingMode
   */
  public void setMarkingMode(final MarkingMode mode)
  {
    mMarkingMode = mode;
  }

  /**
   * Gets the mode how implicit markings are handled.
   * @see MarkingMode
   */
  public MarkingMode getMarkingMode()
  {
    return mMarkingMode;
  }

  /**
   * Sets the transition limit. The transition limit specifies the maximum
   * number of transitions (including stored silent transitions of the
   * transitive closure) that will be stored.
   * @param limit
   *          The new transition limit, or {@link Integer#MAX_VALUE} to allow an
   *          unlimited number of transitions.
   */
  public void setTransitionLimit(final int limit)
  {
    mTransitionLimit = limit;
  }

  /**
   * Gets the transition limit.
   * @see #setTransitionLimit(int) setTransitionLimit()
   */
  public int getTransitionLimit()
  {
    return mTransitionLimit;
  }


  //#########################################################################
  //# Initial Partition
  /**
   * Sets an initial partition for the bisimulation algorithm.
   * The partition is copied into the simplifiers data structures, which will
   * be modified destructively when calling {@link #run()}, so it needs to be
   * set again for a second run.
   * @param partition
   *          A collection of classes constituting the initial partition. Each
   *          array in the collection represents a class of equivalent state
   *          codes.
   */
  public abstract void setUpInitialPartition(Collection<int[]> partition);

  /**
   * Sets up an initial partition for the bisimulation algorithm based on
   * the markings of states in the transition relation.
   * States with equal sets of markings are placed in the same class.
   * This method replaces any previously set initial partition.
   * This method is called by default during each {@link #run()} unless
   * the user provides an alternative initial partition.
   */
  public void setUpInitialPartitionBasedOnMarkings()
  throws OverflowException
  {
    final ListBufferTransitionRelation rel = getTransitionRelation();
    final int numProps = rel.getNumberOfPropositions();
    long mask = 0;
    for (int prop = 0; prop < numProps; prop++) {
      mask = rel.addMarking(mask, prop);
    }
    setUpInitialPartitionBasedOnMarkings(mask);
  }

  /**
   * Sets up an initial partition for the bisimulation algorithm based on
   * the markings of states in the transition relation.
   * States with equal sets of markings are placed in the same class.
   * This method replaces any previously set initial partition.
   * @param  mask   Marking pattern identifying the markings to be considered.
   *                Only markings in this pattern will be taken into account
   *                for the partition.
   */
  public abstract void setUpInitialPartitionBasedOnMarkings(long mask)
  throws OverflowException;

  /**
   * Refines the current partition using initial states.
   * This method splits all equivalence classes in the current partition
   * such that two states can only be equivalent if either both of them
   * are initial or none of them is initial. When using observation
   * equivalence, a state is also considered as initial if an initial
   * state is reachable via a sequence of tau transitions.
   *
   * (This method is intended to support non-alpha determinisation
   * and only makes sense when the input transition relation is reversed.)
   *
   * @see NonAlphaDeterminisationTRSimplifier
   */
  public abstract void refinePartitionBasedOnInitialStates()
  throws OverflowException;


  //#########################################################################
  //# Inner Enumeration Equivalence
  /**
   * Possible equivalences for partitioning a transition relation.
   */
  public enum Equivalence
  {
    /**
     * Bisimulation equivalence. Equivalent states must be able to reach
     * equivalent successors for all traces of events. There are no silent
     * events, and the tau-closure is not computed for this setting.
     */
    BISIMULATION,
    /**
     * Observation equivalence. Equivalent states must be able to reach
     * equivalent successors for all traces of observable events including the
     * empty trace. This setting is the default.
     */
    OBSERVATION_EQUIVALENCE,
    /**
     * Weak observation equivalence. Equivalent states must be able to reach
     * equivalent successors for all traces of observable events <I>not</I>
     * including the empty trace. This is implemented by not considering the
     * silent event for splitting equivalence classes.
     */
    WEAK_OBSERVATION_EQUIVALENCE
  }


  //#########################################################################
  //# Inner Enumeration TransitionRemoval
  /**
   * <P>Possible settings to control how an
   * {@link AbstractObservationEquivalenceTRSimplifier} handles the removal of redundant
   * transitions.</P>
   *
   * <P>A transition is redundant according to observation equivalence, if the
   * automaton contains other transitions such that the same target state can be
   * reached without the transition, using the same sequence of observable
   * events. For more details on redundant transitions see the following paper: <I>Jaana Eloranta: Minimizing
   * the number of transitions with respect to observation equivalence, BIT
   * <STRONG>31</STRONG>(4), 397-419, 1991</I>.</P>
   *
   * <P>This simplifier can perform two passes of redundant transition
   * removal.</P>
   *
   * <P>The first pass is performed before computing the state state partition.
   * This optional step may improve performance, but fails to remove all
   * redundant transitions if a non-trivial partition is found. Furthermore,
   * it cannot remove tau-transitions correctly if the input transition
   * relation contains tau-loops.</P>
   *
   * <P>The second pass is performed after computation of the partition, when
   * building the output transition relation. Only this pass can guarantee a
   * minimal result.</P>
   */
  public enum TransitionRemoval
  {
    /**
     * Disables removal of redundant transitions. This is the only option that
     * works when using bisimulation equivalence
     * ({@link Equivalence#BISIMULATION}), and it will be automatically selected
     * when bisimulation equivalence is configured.
     */
    NONE,
    /**
     * Enables the first pass to remove of redundant transitions for all events
     * except the silent event with code {@link EventEncoding#TAU}. This option
     * is safe for all automata, and is used as the default. The second pass is
     * performed in addition (even in case of trivial partition, to remove
     * tau-transitions).
     */
    NONTAU,
    /**
     * Enables the first pass to remove all redundant transitions, including
     * silent transitions. This option only is guaranteed to work correctly if
     * the input automaton does not contain any loops of silent events. If this
     * cannot be guaranteed, consider using {@link #NONTAU} instead. The second
     * pass is performed in addition, if a non-trivial partition has been found.
     */
    ALL,
    /**
     * Disables the first pass. Redundant transitions are removed only in the
     * second pass, which is performed even in case of a trivial partition.
     */
    AFTER,
    /**
     * Disables the first pass. Redundant transitions are removed only in the
     * second pass, which is performed only in case of a nontrivial partition.
     */
    AFTER_IF_CHANGED
  }


  //#########################################################################
  //# Inner Enumeration MarkingMode
  /**
   * <P>Possible settings to control how an
   * {@link AbstractObservationEquivalenceTRSimplifier} handles implicit markings.</P>
   *
   * <P>When minimising for observation equivalence, states that have a string
   * of silent events leading to a marked states can be considered as marked
   * themselves. The marking method controls whether or not such states such
   * receive a marking in the output automaton.</P>
   *
   * <P>This setting only takes effect if the initial partition is set up
   * based on markings, i.e., if method {@link #createInitialPartition()}
   * is used.</P>
   */
  public enum MarkingMode
  {
    /**
     * Leaves markings unchanged. A state in the output automaton will be
     * marked with a given proposition if and only if at least one state
     * in its equivalence class is marked with that proposition. This is
     * the default setting.
     */
    UNCHANGED,
    /**
     * Adds markings to all states that are implicitly marked. A state is
     * marked by a given proposition, if there is a trace of silent
     * transitions leading to a state marked by that proposition.
     */
    SATURATE,
    /**
     * Tries to minimise the number of markings by removing implicit markings.
     * If, for some state&nbsp;<I>s</I> marked by a given proposition, there is
     * another silently reachable state marked by that proposition, the marking
     * will be removed from&nbsp;<I>s</I>. This option only is guaranteed to
     * work correctly if the input automaton does not contain any loops of
     * silent events.
     */
    MINIMIZE
  }


  //#########################################################################
  //# Data Members
  private Equivalence mEquivalence;
  private TransitionRemoval mTransitionRemovalMode;
  private MarkingMode mMarkingMode;
  private int mTransitionLimit;

}
