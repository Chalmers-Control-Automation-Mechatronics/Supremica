//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.analysis.tr;

import java.util.NoSuchElementException;

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
  extends Cloneable
{

  /**
   * Creates a copy of this transition iterator. The cloned iterator
   * becomes an independent new iterator with the same capabilities as this
   * transition iterator, and starts off in the same state.
   */
  public TransitionIterator clone();

  /**
   * Restarts this iterator to iterate over the same set of transitions
   * it was used for previously.
   */
  public void reset();

  /**
   * Restarts this iterator to iterate over transitions associated with
   * only the given event.
   */
  public void resetEvent(int event);

  /**
   * Restarts this iterator to iterate over transitions associated with
   * the given range of events.
   * @param  first   The first event to be included in the iteration.
   * @param  last    The last event to be included in the iteration.
   */
  public void resetEvents(int first, int last);

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
   * Restarts this iterator to iterate over transitions associated with
   * the given from-state, but without clearing any stored history. This is
   * an optional operation implemented by some iterators that store previously
   * returned results to prevent duplicates, in which case this method makes
   * it possible to continue from a different start state without clearing the
   * previously returned results. Iterators that do not store results can
   * implement this method by calling {@link #resetState(int) resetState()}.
   */
  public void resume(int from);

  /**
   * Returns the number of the first event considered by this iterator,
   * as passed to the {@link #resetEvents(int, int)} method.
   */
  public int getFirstEvent();

  /**
   * Returns the number of the last event considered by this iterator,
   * as passed to the {@link #resetEvents(int, int)} method.
   */
  public int getLastEvent();

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
   * Returns whether the iterator has been advanced and can provide a
   * transition. This method returns the same result as a previous call to
   * {@link #advance()}.
   * @return <CODE>true</CODE> if there is another transition in the iteration,
   *         so the next call to {@link #getCurrentFromState()},
   *         {@link #getCurrentEvent()}, or {@link #getCurrentToState()}
   *         will succeed.
   */
  public boolean isValid();

  /**
   * Gets the ID of the source state of the current transition in the iteration.
   * A transition's <I>source</I> state differs from its <I>from-state</I> in
   * that it always represents the actual source state of the transition, no
   * matter how the transition buffer is organised.
   * @throws NoSuchElementException if there is no current transition in
   *         the iteration, or if the method is called without calling
   *         {@link #advance()} first.
   * @see #getCurrentFromState()
   */
  public int getCurrentSourceState();

  /**
   * Gets the ID of the from-state of the current transition in the iteration.
   * Unlike the <I>source</I> state, a transition's <I>from-state</I>
   * depends on the transition buffer's organisation and identifies the
   * state under which the transition is indexed.
   * @throws NoSuchElementException if there is no current transition in
   *         the iteration, or if the method is called without calling
   *         {@link #advance()} first.
   * @see #getCurrentSourceState()
   */
  public int getCurrentFromState();

  /**
   * Gets the ID of the event of the current transition in the iteration.
   * @throws NoSuchElementException if there is no current transition in
   *         the iteration, or if the method is called without calling
   *         {@link #advance()} first.
   */
  public int getCurrentEvent();

  /**
   * Gets the ID of the target state of the current transition in the iteration.
   * A transition's <I>target</I> state differs from its <I>to-state</I> in
   * that it always represents the actual target state of the transition, no
   * matter how the transition buffer is organised.
   * @throws NoSuchElementException if there is no current transition in
   *         the iteration, or if the method is called without calling
   *         {@link #advance()} first.
   * @see #getCurrentToState()
   */
  public int getCurrentTargetState();

  /**
   * Gets the ID of the to-state of the current transition in the iteration.
   * Unlike the <I>target</I> state, a transition's <I>to-state</I>
   * depends on the transition buffer's organisation and is the state
   * stored with each individual transition, not the one used for indexing.
   * @throws NoSuchElementException if there is no current transition in
   *         the iteration, or if the method is called without calling
   *         {@link #advance()} first.
   * @see #getCurrentTargetState()
   */
  public int getCurrentToState();

  /**
   * Sets the ID of the to-state of the current transition in the iteration.
   * This method directly changes the data in the transition buffer for the
   * current transition. This may violate the ordering of nondeterministic
   * successor states or introduce duplicate transitions. It is the caller's
   * responsibility to return the transition relation to an ordered
   * duplicate-free state.
   * @param  state    The state number of the new to-state, which must
   *                  be a valid state number in the transition buffer.
   * @throws NoSuchElementException if there is no current transition in
   *         the iteration, or if the method is called without calling
   *         {@link #advance()} first.
   * @see #getCurrentToState()
   * @see TransitionListBuffer
   */
  public void setCurrentToState(int state);

  /**
   * Removes the current transition. This method removes the current
   * transition in the iteration from the associated transition buffer.
   * After removing, the current transition is undefined, and {@link
   * #advance()} needs to be called to access the transition following the
   * removed transition.
   * @throws NoSuchElementException if there is no current transition in
   *         the iteration, or if the method is called without calling
   *         {@link #advance()} first.
   */
  public void remove();

}
