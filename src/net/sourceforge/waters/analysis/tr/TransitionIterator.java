//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.tr
//# CLASS:   TransitionIterator
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.tr;

/**
 * An iterator to visit transitions in a {@link TransitionListBuffer}.
 * This interface provides a basis for iteration over all or subsets of
 * transitions in a transition relation.
 *
 * @see ListBufferTransitionRelation
 * @see TransitionListBuffer
 *
 * @author Robi Malik
 */

public interface TransitionIterator
{

  /**
   * Restarts this iterator to iterate over the same set of transitions
   * it was used for previously.
   */
  public void reset();

  /**
   * Restarts this iterator to iterate over transitions associated with
   * the given event.
   */
  public void resetEvent(int event);

  /**
   * Restarts this iterator to iterate over transitions associated with
   * the given from-state.
   */
  public void resetState(int from);

  /**
   * Restarts this iterator to iterate over transitions associated with
   * the given from-state and event.
   */
  public void reset(int from, int event);

  /**
   * Advances iteration. This method advances the iterator to next transition.
   * It needs to be called before trying to access the first transition in the
   * iteration.
   * @return <CODE>true</CODE> if there is another transition in the iteration,
   *         so the next call to {@link #getCurrentFromState()},
   *         {@link #getCurrentEvent()}, or {@link #getCurrentToState()}
   *         will succeed.
   */
  public boolean advance();

  /**
   * Gets the ID of the source state of the current transition in the iteration.
   * A transition's <I>source</I> state differs from its <I>from-state</I> in
   * that it always represents the actual source state of the transition, no
   * matter how the transition buffer is organised.
   * @throws {@link java.util.NoSuchElementException NoSuchElementException}
   *         if there is no more transition in the iteration, or if the method
   *         is called without calling {@link #advance()} first.
   * @see #getCurrentFromState()
   */
  public int getCurrentSourceState();

  /**
   * Gets the ID of the from-state of the current transition in the iteration.
   * Unlike the <I>source</I> state, a transition's <I>from-state</I>
   * depends on the transition buffer's organisation and identifies the
   * state under which the transition is indexed.
   * @throws {@link java.util.NoSuchElementException NoSuchElementException}
   *         if there is no more transition in the iteration, or if the method
   *         is called without calling {@link #advance()} first.
   * @see #getCurrentSourceState()
   */
  public int getCurrentFromState();

  /**
   * Gets the ID of the event of the current transition in the iteration.
   * @throws {@link java.util.NoSuchElementException NoSuchElementException}
   *         if there is no more transition in the iteration, or if the method
   *         is called without calling {@link #advance()} first.
   */
  public int getCurrentEvent();

  /**
   * Gets the ID of the target state of the current transition in the iteration.
   * A transition's <I>target</I> state differs from its <I>to-state</I> in
   * that it always represents the actual target state of the transition, no
   * matter how the transition buffer is organised.
   * @throws {@link java.util.NoSuchElementException NoSuchElementException}
   *         if there is no more transition in the iteration, or if the method
   *         is called without calling {@link #advance()} first.
   * @see #getCurrentToState()
   */
  public int getCurrentTargetState();

  /**
   * Gets the ID of the to-state of the current transition in the iteration.
   * Unlike the <I>target</I> state, a transition's <I>to-state</I>
   * depends on the transition buffer's organisation and is the state
   * stored with each individual transition, not the one used for indexing.
   * @throws {@link java.util.NoSuchElementException NoSuchElementException}
   *         if there is no more transition in the iteration, or if the method
   *         is called without calling {@link #advance()} first.
   * @see #getCurrentTargetState()
   */
  public int getCurrentToState();

  /**
   * Removes the current transition. This method removes the current
   * transition in the iteration from the associated transition buffer.
   * After removing, the current transition is undefined, and {@link
   * #advance()} needs to be called to access the transition following the
   * removed transition.
   * @throws {@link java.util.NoSuchElementException NoSuchElementException}
   *         if there is no current transition in the iteration, or if the
   *         method is called without calling {@link #advance()} first.
   */
  public void remove();

}
