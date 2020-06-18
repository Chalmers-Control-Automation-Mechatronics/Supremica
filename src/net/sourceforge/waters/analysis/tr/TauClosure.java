//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.stack.TIntStack;
import gnu.trove.stack.array.TIntArrayStack;

import java.util.NoSuchElementException;

import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.base.WatersRuntimeException;


/**
 * A utility class to iterate over transitions in the tau-closure of
 * a transition relation. This class can be configured to compute the
 * tau closure on the fly for each iteration, or to compute it once and
 * use it for subsequent requests. By default, the tau-closure only
 * operates using the default tau event {@link EventEncoding#TAU}, but
 * it can be configured to use an arbitrary range of events.
 *
 * @author Robi Malik
 */
public class TauClosure
{

  //#########################################################################
  //# Constructor
  /**
   * Computes the tau-closure over the standard tau
   * ({@link EventEncoding#TAU}) transitions in the given transition list
   * buffer. All transitions in the tau closure are precomputed and stored by
   * this constructor. The precomputed tau-closure does not update
   * automatically when the transition relation changes.
   * @param  buffer     The transition list buffer containing the transitions
   *                    for which tau-closure is computed.
   */
  public TauClosure(final TransitionListBuffer buffer)
  {
    this(buffer, Integer.MAX_VALUE);
  }

  /**
   * Computes the tau-closure over the standard tau
   * ({@link EventEncoding#TAU}) transitions in the given transition list
   * buffer, which may or may not be precomputed. If precomputed, the
   * tau-closure does not update automatically when the transition relation
   * changes.
   * @param  buffer     The transition list buffer containing the transitions
   *                    for which tau-closure is computed.
   * @param  limit      The maximum number of transitions that can be stored
   *                    in the tau-closure. If the number of computed
   *                    tau-transitions exceeds the limit, precomputation is
   *                    aborted and transitions will be produced on the fly by
   *                    iterators. A limit of&nbsp;0 forces the tau closure
   *                    always to be computed on the fly.
   */
  public TauClosure(final TransitionListBuffer buffer, final int limit)
  {
     this(buffer, null, limit);
  }

/**
   * Creates a tau-closure for the given transition list buffer,
   * which may or may not be precomputed. If precomputed, the tau-closure
   * does not update automatically when the transition relation changes.
   * @param  buffer     The transition list buffer containing the transitions
   *                    for which tau-closure is computed.
   * @param  iter       A transition iterator that defines the transitions
   *                    to be considered in the tau closure. A value of
   *                    <CODE>null</CODE> requests a tau closure over the
   *                    standard silent transitions with code
   *                    {@link EventEncoding#TAU}.
   * @param  limit      The maximum number of transitions that can be stored
   *                    in the tau-closure. If the number of computed
   *                    tau-transitions exceeds the limit, precomputation is
   *                    aborted and transitions will be produced on the fly by
   *                    iterators. A limit of&nbsp;0 forces the tau closure
   *                    always to be computed on the fly.
   */
  public TauClosure(final TransitionListBuffer buffer,
                    final TransitionIterator iter,
                    int limit)
  {
    mTransitionBuffer = buffer;
    mIteratorTemplate = iter;
    if (limit > 0) {
      final int numStates = mTransitionBuffer.getNumberOfStates();
      final int[][] trans = new int[numStates][];
      final TransitionIterator inner = createInnerIterator();
      final TransitionIterator onTheFly =
        new OnTheFlyTauClosureIterator(mTransitionBuffer, inner);
      final TIntArrayList list = new TIntArrayList();
      for (int state = 0; state < numStates; state++) {
        onTheFly.resetState(state);
        onTheFly.advance();
        while (onTheFly.advance()) {
          if (--limit == 0) {
            mStoredTransitions = null;
            return;
          }
          final int succ = onTheFly.getCurrentToState();
          list.add(succ);
        }
        if (list.isEmpty()) {
          trans[state] = EMPTY_ARRAY;
        } else {
          trans[state] = list.toArray();
          list.clear();
        }
      }
      mStoredTransitions = trans;
    } else {
      mStoredTransitions = null;
    }
  }

  //#########################################################################
  //# Iterator Access
  /**
   * Creates an iterator over this tau-closure.
   * The iterator returned is not initialised, so the method
   * {@link TransitionIterator#resetState(int) resetState()} must be used before
   * it can be used. After initialisation, the first state returned by the
   * iterator is the start state of iteration, and it is followed by all states
   * reachable by sequences of {@link EventEncoding#TAU} events in depth-first
   * order. The tau-closure iterator is a read-only iterator and does not
   * implement the {@link TransitionIterator#remove()} method.
   */
  public TransitionIterator createIterator()
  {
    if (mStoredTransitions == null) {
      final TransitionIterator inner = createInnerIterator();
      return new OnTheFlyTauClosureIterator(mTransitionBuffer, inner);
    } else {
      return new StoredTauClosureIterator();
    }
  }

  /**
   * Creates a iterator for this tau-closure that is set up to
   * iterate over all states silently reachable from the given from-state.
   * The first state returned by the iterator is the given from-state,
   * and it is followed by all states reachable by sequences of
   * {@link EventEncoding#TAU} events in depth-first order. The tau-closure
   * iterator is a read-only iterator and does not implement the
   * {@link TransitionIterator#remove()} method.
   */
  public TransitionIterator createIterator(final int from)
  {
    if (mStoredTransitions == null) {
      final TransitionIterator inner = createInnerIterator();
      return new OnTheFlyTauClosureIterator(mTransitionBuffer, inner, from);
    } else {
      return new StoredTauClosureIterator(from);
    }
  }

  /**
   * Creates an iterator over this tau-closure that is guaranteed to
   * support caching. This method produces an iterator like {@link
   * #createIterator()} with the additional guarantee that it supports
   * caching and fully implements the {@link TransitionIterator#resume(int)
   * resume()} method.
   * @see #createIterator()
   */
  public TransitionIterator createCachingIterator()
  {
    if (mStoredTransitions == null) {
      final TransitionIterator inner = createInnerIterator();
      return new OnTheFlyTauClosureIterator(mTransitionBuffer, inner);
    } else {
      final TransitionIterator inner = new StoredTauClosureIterator();
      return new OneEventCachingTransitionIterator(inner, EventEncoding.TAU);
    }
  }


  /**
   * <P>Creates an iterator over the pre-event closure of the underlying
   * transition relation. The iterator returned by this method produces
   * transitions with an event&nbsp;<I>e</I> by following an arbitrary number
   * of {@link EventEncoding#TAU} events and then executing a transition with
   * event&nbsp;<I>e</I>.
   *
   * <P>The iterator returned is not initialised, so the method
   * {@link TransitionIterator#reset(int,int) reset()} must be used before it
   * can be used. The returned iterator is a read-only iterator and does not
   * implement the {@link TransitionIterator#remove()} method. It can also not
   * be used to iterate using the silent event {@link EventEncoding#TAU}.</P>
   */
  public TransitionIterator createPreEventClosureIterator()
  {
    final TransitionIterator inner = mTransitionBuffer.createReadOnlyIterator();
    final TransitionIterator outer;
    if (mTransitionBuffer instanceof OutgoingTransitionListBuffer) {
      outer = new PreEventClosureTransitionIterator(inner);
    } else {
      outer = new PostEventClosureTransitionIterator(inner);
    }
    return new CachingTransitionIterator(mTransitionBuffer, outer);
  }

  /**
   * <P>Creates an iterator over the pre-event closure of the underlying
   * transition relation. The iterator returned by this method produces
   * transitions with an event&nbsp;<I>e</I> by following an arbitrary number
   * of {@link EventEncoding#TAU} events and then executing a transition with
   * event&nbsp;<I>e</I>.
   *
   * <P>The iterator returned is not initialised, so the method
   * {@link TransitionIterator#resetState(int) resetState()} must be used before
   * it can be used. The returned iterator is a read-only iterator and does
   * not implement the {@link TransitionIterator#remove()} method.</P>
   *
   * @param event
   *          The event used by the iterator. If this is
   *          {@link EventEncoding#TAU}, the method returns a plain tau-closure
   *          iterator that can only iterate with the silent event. Otherwise a
   *          closure iterator is returned, which can be reset to iterate over
   *          any other single event except {@link EventEncoding#TAU}.
   */
  public TransitionIterator createPreEventClosureIterator(final int event)
  {
    if (event == EventEncoding.TAU) {
      return createIterator();
    } else {
      final TransitionIterator inner =
        mTransitionBuffer.createReadOnlyIterator(event);
      final TransitionIterator outer;
      if (mTransitionBuffer instanceof OutgoingTransitionListBuffer) {
        outer = new PreEventClosureTransitionIterator(inner);
      } else {
        outer = new PostEventClosureTransitionIterator(inner);
      }
      return new OneEventCachingTransitionIterator(outer, event);
    }
  }

  /**
   * <P>Creates an iterator over the pre-event closure of the underlying
   * transition relation. The iterator returned by this method produces
   * transitions with an event&nbsp;<I>e</I> by following an arbitrary number
   * of {@link EventEncoding#TAU} events and then executing a transition with
   * event&nbsp;<I>e</I>.
   *
   * <P>The iterator returned is not initialised, so the method
   * {@link TransitionIterator#reset(int,int) reset()} must be used before it
   * can be used. The returned iterator is a read-only iterator and does not
   * implement the {@link TransitionIterator#remove()} method. It can also not
   * be used to iterate using the silent event {@link EventEncoding#TAU}.</P>
   *
   * @param flags
   *          Event status flags to specify the type of proper events,
   *          as passed to the
   *          {@link StatusGroupTransitionIterator#StatusGroupTransitionIterator(TransitionIterator, EventStatusProvider, int...)
   *          StatusGroupTransitionIterator} constructor.
   * @see StatusGroupTransitionIterator
   */
  public TransitionIterator createPreEventClosureIteratorByStatus
    (final int... flags)
  {
    final TransitionIterator inner =
      mTransitionBuffer.createReadOnlyIteratorByStatus(flags);
    final TransitionIterator outer;
    if (mTransitionBuffer instanceof OutgoingTransitionListBuffer) {
      outer = new PreEventClosureTransitionIterator(inner);
    } else {
      outer = new PostEventClosureTransitionIterator(inner);
    }
    return new CachingTransitionIterator(mTransitionBuffer, outer);
  }

  /**
   * <P>Creates an iterator over the post-event closure of the underlying
   * transition relation. The iterator returned by this method produces
   * transitions with an event&nbsp;<I>e</I> by following an arbitrary number
   * of {@link EventEncoding#TAU} after executing a transition with
   * event&nbsp;<I>e</I>.</P>
   *
   * <P>The iterator returned is not initialised, so the method
   * {@link TransitionIterator#reset(int,int) reset()} must be used before it
   * can be used. The returned iterator is a read-only iterator and does not
   * implement the {@link TransitionIterator#remove()} method. It can also not
   * be used to iterate using the silent event {@link EventEncoding#TAU}.</P>
   */
  public TransitionIterator createPostEventClosureIterator()
  {
    final TransitionIterator inner = mTransitionBuffer.createReadOnlyIterator();
    final TransitionIterator outer;
    if (mTransitionBuffer instanceof OutgoingTransitionListBuffer) {
      outer = new PostEventClosureTransitionIterator(inner);
    } else {
      outer = new PreEventClosureTransitionIterator(inner);
    }
    return new CachingTransitionIterator(mTransitionBuffer, outer);
  }

  /**
   * <P>Creates an iterator over the post-event closure of the underlying
   * transition relation. The iterator returned by this method produces
   * transitions with an event&nbsp;<I>e</I> by following an arbitrary number
   * of {@link EventEncoding#TAU} after executing a transition with
   * event&nbsp;<I>e</I>.</P>
   *
   * <P>The iterator returned is not initialised, so the method
   * {@link TransitionIterator#resetState(int) resetState()} must be used before
   * it can be used. The returned iterator is a read-only iterator and does
   * not implement the {@link TransitionIterator#remove()} method.</P>
   *
   * @param event
   *          The event used by the iterator. If this is
   *          {@link EventEncoding#TAU}, the method returns a plain tau-closure
   *          iterator that can only iterate with the silent event. Otherwise a
   *          closure iterator is returned, which can be reset to iterate over
   *          any other single event except {@link EventEncoding#TAU}.
   */
  public TransitionIterator createPostEventClosureIterator(final int event)
  {
    if (event == EventEncoding.TAU) {
      return createIterator();
    } else {
      final TransitionIterator inner =
        mTransitionBuffer.createReadOnlyIterator(event);
      final TransitionIterator outer;
      if (mTransitionBuffer instanceof OutgoingTransitionListBuffer) {
        outer = new PostEventClosureTransitionIterator(inner);
      } else {
        outer = new PreEventClosureTransitionIterator(inner);
      }
      return new OneEventCachingTransitionIterator(outer, event);
    }
  }

  /**
   * <P>Creates an iterator over the post-event closure of the underlying
   * transition relation. The iterator returned by this method produces
   * transitions with an event&nbsp;<I>e</I> by following an arbitrary number
   * of {@link EventEncoding#TAU} after executing a transition with
   * event&nbsp;<I>e</I>.</P>
   *
   * <P>The iterator returned is not initialised, so the method
   * {@link TransitionIterator#reset(int,int) reset()} must be used before it
   * can be used. The returned iterator is a read-only iterator and does not
   * implement the {@link TransitionIterator#remove()} method. It can also not
   * be used to iterate using the silent event {@link EventEncoding#TAU}.</P>
   *
   * @param flags
   *          Event status flags to specify the type of proper events,
   *          as passed to the
   *          {@link StatusGroupTransitionIterator#StatusGroupTransitionIterator(TransitionIterator, EventStatusProvider, int...)
   *          StatusGroupTransitionIterator} constructor.
   * @see StatusGroupTransitionIterator
   */
  public TransitionIterator createPostEventClosureIteratorByStatus
    (final int... flags)
  {
    final TransitionIterator inner =
      mTransitionBuffer.createReadOnlyIteratorByStatus(flags);
    final TransitionIterator outer;
    if (mTransitionBuffer instanceof OutgoingTransitionListBuffer) {
      outer = new PostEventClosureTransitionIterator(inner);
    } else {
      outer = new PreEventClosureTransitionIterator(inner);
    }
    return new CachingTransitionIterator(mTransitionBuffer, outer);
  }

  /**
   * <P>Creates an iterator over the event-closure of the underlying
   * transition relation. The iterator returned by this method produces
   * transitions with an event&nbsp;<I>e</I> by first following an arbitrary
   * number of {@link EventEncoding#TAU} event, then executing a transition
   * with event&nbsp;<I>e</I>, followed by another arbitrary number of
   * {@link EventEncoding#TAU} events.</P>
   *
   * <P>The iterator returned is not initialised, so the method
   * {@link TransitionIterator#reset(int,int) reset()} must be used before it
   * can be used. The returned iterator is a read-only iterator and does not
   * implement the {@link TransitionIterator#remove()} method. It can also not
   * be used to iterate using the silent event {@link EventEncoding#TAU}.</P>
   */
  public TransitionIterator createFullEventClosureIterator()
  {
    final TransitionIterator inner =
      mTransitionBuffer.createReadOnlyIterator();
    final TransitionIterator outer =
      new FullEventClosureTransitionIterator(inner);
    return new CachingTransitionIterator(mTransitionBuffer, outer);
  }

  /**
   * <P>Creates an iterator over the event-closure of the underlying
   * transition relation. The iterator returned by this method produces
   * transitions with an event&nbsp;<I>e</I> by first following an arbitrary
   * number of {@link EventEncoding#TAU} event, then executing a transition
   * with event&nbsp;<I>e</I>, followed by another arbitrary number of
   * {@link EventEncoding#TAU} events.</P>
   *
   * <P>The iterator returned is not initialised, so the method
   * {@link TransitionIterator#reset(int,int) reset()} must be used before it
   * can be used. The returned iterator is a read-only iterator and does not
   * implement the {@link TransitionIterator#remove()} method.</P>
   *
   * @param event
   *          The event used by the iterator. If this is
   *          {@link EventEncoding#TAU}, the method returns a plain tau-closure
   *          iterator that can only iterate with the silent event. Otherwise a
   *          closure iterator is returned, which can be reset to iterate over
   *          any other single event except {@link EventEncoding#TAU}.
   */
  public TransitionIterator createFullEventClosureIterator(final int event)
  {
    if (event == EventEncoding.TAU) {
      return createIterator();
    } else {
      final TransitionIterator inner =
        mTransitionBuffer.createReadOnlyIterator(event);
      final TransitionIterator outer =
        new FullEventClosureTransitionIterator(inner);
      return new OneEventCachingTransitionIterator(outer, event);
    }
  }


  /**
   * <P>Creates an iterator over the event-closure of the underlying
   * transition relation. The iterator returned by this method produces
   * transitions with an event&nbsp;<I>e</I> by first following an arbitrary
   * number of {@link EventEncoding#TAU} event, then executing a transition
   * with event&nbsp;<I>e</I>, followed by another arbitrary number of
   * {@link EventEncoding#TAU} events.</P>
   *
   * <P>The iterator returned is not initialised, so the method
   * {@link TransitionIterator#reset(int,int) reset()} must be used before it
   * can be used. The returned iterator is a read-only iterator and does not
   * implement the {@link TransitionIterator#remove()} method. It can also not
   * be used to iterate using the silent event {@link EventEncoding#TAU}.</P>
   *
   * @param flags
   *          Event status flags to specify the type of proper events,
   *          as passed to the
   *          {@link StatusGroupTransitionIterator#StatusGroupTransitionIterator(TransitionIterator, EventStatusProvider, int...)
   *          StatusGroupTransitionIterator} constructor.
   * @see StatusGroupTransitionIterator
   */
  public TransitionIterator createFullEventClosureIteratorByStatus
    (final int... flags)
  {
    final TransitionIterator inner =
      mTransitionBuffer.createReadOnlyIteratorByStatus(flags);
    final TransitionIterator outer =
      new FullEventClosureTransitionIterator(inner);
    return new CachingTransitionIterator(mTransitionBuffer, outer);
  }


  //#########################################################################
  //# Auxiliary Methods
  private TransitionIterator createInnerIterator()
  {
    if (mIteratorTemplate == null) {
      final TransitionIterator inner = mTransitionBuffer.createReadOnlyIterator();
      inner.resetEvent(EventEncoding.TAU);
      return inner;
    } else {
      return mIteratorTemplate.clone();
    }
  }


  //#########################################################################
  //# Inner Class AbstractTauClosureIterator
  private static abstract class AbstractTauClosureIterator
    implements TransitionIterator
  {

    //#######################################################################
    //# Constructor
    private AbstractTauClosureIterator(final int from)
    {
      mFrom = from;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.analysis.tr.TransitionIterator
    @Override
    public AbstractTauClosureIterator clone()
    {
      try {
        return (AbstractTauClosureIterator) super.clone();
      } catch (final CloneNotSupportedException exception) {
        throw new WatersRuntimeException(exception);
      }
    }

    @Override
    public void resetEvent(final int event)
    {
      if (event == EventEncoding.TAU) {
        reset();
      } else {
        throwNonTauException();
      }
    }

    @Override
    public void resetEvents(final int first, final int last)
    {
      if (first == EventEncoding.TAU && last == EventEncoding.TAU) {
        reset();
      } else {
        throwNonTauException();
      }
    }

    @Override
    public void resetState(final int from)
    {
      mFrom = from;
      reset();
    }

    @Override
    public void reset(final int from, final int event)
    {
      mFrom = from;
      resetEvent(event);
    }

    @Override
    public void resume(final int state)
    {
      resetState(state);
    }

    @Override
    public int getFirstEvent()
    {
      return EventEncoding.TAU;
    }

    @Override
    public int getLastEvent()
    {
      return EventEncoding.TAU;
    }

    @Override
    public int getCurrentEvent()
    {
      return EventEncoding.TAU;
    }

    @Override
    public int getCurrentFromState()
    {
      return mFrom;
    }

    @Override
    public void setCurrentToState(final int state)
    {
      throw new UnsupportedOperationException
        (ProxyTools.getShortClassName(this) +
         " does not support transition modification!");
    }

    @Override
    public void remove()
    {
      throw new UnsupportedOperationException
        (ProxyTools.getShortClassName(this) +
         " does not support transition removal!");
    }

    //#######################################################################
    //# Auxiliary Methods
    private void throwNonTauException()
    {
      throw new UnsupportedOperationException
        (ProxyTools.getShortClassName(this) +
         " only iterates with tau event!");
    }

    //#######################################################################
    //# Data Members
    private int mFrom;

  }


  //#########################################################################
  //# Inner Class OnTheFlyTauClosureIterator
  private static class OnTheFlyTauClosureIterator
    extends AbstractTauClosureIterator
  {

    //#######################################################################
    //# Constructor
    private OnTheFlyTauClosureIterator(final TransitionListBuffer buffer,
                                       final TransitionIterator inner)
    {
      this(buffer, inner, -1);
    }

    private OnTheFlyTauClosureIterator(final TransitionListBuffer buffer,
                                       final TransitionIterator inner,
                                       final int from)
    {
      super(from);
      mTransitionBuffer = buffer;
      mStack = new TIntArrayStack();
      mInner = inner;
      mVisited = new TIntHashSet();
      mCurrentState = -1;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.analysis.tr.TransitionIterator
    @Override
    public OnTheFlyTauClosureIterator clone()
    {
      final OnTheFlyTauClosureIterator cloned =
        (OnTheFlyTauClosureIterator) super.clone();
      cloned.mStack = new TIntArrayStack(mStack);
      cloned.mInner = mInner.clone();
      cloned.mVisited = new TIntHashSet(mVisited);
      return cloned;
    }

    @Override
    public void reset()
    {
      mStack.clear();
      final int size = mVisited.size();
      if (size > 100) {
        mVisited = new TIntHashSet();
      } else if (size > 0) {
        mVisited.clear();
      }
      mCurrentState = -1;
    }

    @Override
    public int getFirstEvent()
    {
      return mInner.getFirstEvent();
    }

    @Override
    public int getLastEvent()
    {
      return mInner.getLastEvent();
    }

    @Override
    public boolean advance()
    {
      if (mCurrentState < 0) {
        mCurrentState = getCurrentFromState();
        mVisited.add(mCurrentState);
        mInner.resetState(mCurrentState);
        return true;
      }
      while (true) {
        while (mInner.advance()) {
          final int next = mInner.getCurrentToState();
          if (mVisited.add(next)) {
            mStack.push(next);
            mCurrentState = next;
            return true;
          }
        }
        if (mStack.size() == 0) {
          mCurrentState = -1;
          return false;
        } else {
          final int state = mStack.pop();
          mInner.resetState(state);
        }
      }
    }

    @Override
    public boolean isValid()
    {
      return mCurrentState >= 0;
    }

    @Override
    public int getCurrentSourceState()
    {
      return mTransitionBuffer.getIteratorSourceState(this);
    }

    @Override
    public int getCurrentToState()
    {
      if (mCurrentState >= 0) {
        return mCurrentState;
      } else {
        throw new NoSuchElementException("Reading past end of list in " +
                                         ProxyTools.getShortClassName(this));
      }
    }

    @Override
    public int getCurrentTargetState()
    {
      return mTransitionBuffer.getIteratorTargetState(this);
    }

    @Override
    public void remove()
    {
      throw new UnsupportedOperationException
        (ProxyTools.getShortClassName(this) +
         " does not support transition removal!");
    }

    //#######################################################################
    //# Data Members
    private final TransitionListBuffer mTransitionBuffer;
    private TIntStack mStack;
    private TransitionIterator mInner;

    private TIntHashSet mVisited;
    private int mCurrentState;

  }


  //#########################################################################
  //# Inner Class StoredTauClosureIterator
  private class StoredTauClosureIterator
    extends AbstractTauClosureIterator
  {

    //#######################################################################
    //# Constructor
    private StoredTauClosureIterator()
    {
      super(-1);
      mCurrentArray = null;
      mIndex = -2;
    }

    private StoredTauClosureIterator(final int from)
    {
      super(from);
      reset();
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.analysis.tr.TransitionIterator
    @Override
    public void reset()
    {
      final int from = getCurrentFromState();
      if (from >= 0) {
        mCurrentArray = mStoredTransitions[from];
        mIndex = -2;
      }
    }

    @Override
    public boolean advance()
    {
      return ++mIndex < mCurrentArray.length;
    }

    @Override
    public boolean isValid()
    {
      return mIndex >= -1 && mIndex < mCurrentArray.length;
    }

    @Override
    public int getCurrentSourceState()
    {
      return mTransitionBuffer.getIteratorSourceState(this);
    }

    @Override
    public int getCurrentToState()
    {
      if (mIndex == -1) {
        // Current state is never explicitly in array.
        return getCurrentFromState();
      } else if (mIndex < mCurrentArray.length) {
        return mCurrentArray[mIndex];
      } else {
        throw new NoSuchElementException("Reading past end of list in " +
                                         ProxyTools.getShortClassName(this));
      }
    }

    @Override
    public int getCurrentTargetState()
    {
      return mTransitionBuffer.getIteratorTargetState(this);
    }

    //#######################################################################
    //# Data Members
    private int[] mCurrentArray;
    private int mIndex;

  }


  //#########################################################################
  //# Inner Class PreEventClosureTransitionIterator
  private class PreEventClosureTransitionIterator
    implements TransitionIterator
  {

    //#######################################################################
    //# Constructor
    private PreEventClosureTransitionIterator(final TransitionIterator inner)
    {
      mTauIterator = createIterator();
      mEventIterator = inner;
      mFromState = -1;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.analysis.tr.TransitionIterator
    @Override
    public PreEventClosureTransitionIterator clone()
    {
      try {
        final PreEventClosureTransitionIterator cloned =
          (PreEventClosureTransitionIterator) super.clone();
        cloned.mTauIterator = mTauIterator.clone();
        cloned.mEventIterator = mEventIterator.clone();
        return cloned;
      } catch (final CloneNotSupportedException exception) {
        throw new WatersRuntimeException(exception);
      }
    }

    @Override
    public void reset()
    {
      if (mFromState >= 0) {
        mTauIterator.resetState(mFromState);
        mTauIterator.advance();
        mEventIterator.resetState(mFromState);
      }
    }

    @Override
    public void resetEvent(final int event)
    {
      if (mFromState >= 0) {
        mTauIterator.resetState(mFromState);
        mTauIterator.advance();
        mEventIterator.reset(mFromState, event);
      } else {
        mEventIterator.resetEvent(event);
      }
    }

    @Override
    public void resetEvents(final int first, final int last)
    {
      mEventIterator.resetEvents(first, last);
      reset();
    }

    @Override
    public void resetState(final int from)
    {
      mFromState = from;
      reset();
    }

    @Override
    public void reset(final int from, final int event)
    {
      mFromState = from;
      resetEvent(event);
    }

    @Override
    public void resume(final int state)
    {
      resetState(state);
    }

    @Override
    public int getFirstEvent()
    {
      return mEventIterator.getFirstEvent();
    }

    @Override
    public int getLastEvent()
    {
      return mEventIterator.getLastEvent();
    }

    @Override
    public boolean advance()
    {
      if (mEventIterator.advance()) {
        return true;
      } else {
        while (mTauIterator.advance()) {
          final int state = mTauIterator.getCurrentToState();
          mEventIterator.resetState(state);
          if (mEventIterator.advance()) {
            return true;
          }
        }
        return false;
      }
    }

    @Override
    public boolean isValid()
    {
      return mEventIterator.isValid();
    }

    @Override
    public int getCurrentEvent()
    {
      return mEventIterator.getCurrentEvent();
    }

    @Override
    public int getCurrentSourceState()
    {
      return mTransitionBuffer.getIteratorSourceState(this);
    }

    @Override
    public int getCurrentToState()
    {
      return mEventIterator.getCurrentToState();
    }

    @Override
    public int getCurrentTargetState()
    {
      return mTransitionBuffer.getIteratorTargetState(this);
    }

    @Override
    public int getCurrentFromState()
    {
      return mFromState;
    }

    @Override
    public void setCurrentToState(final int state)
    {
      throw new UnsupportedOperationException
        (ProxyTools.getShortClassName(this) +
         " does not support transition modification!");
    }

    @Override
    public void remove()
    {
      throw new UnsupportedOperationException
        (ProxyTools.getShortClassName(this) +
         " does not support transition removal!");
    }

    //#######################################################################
    //# Data Members
    private int mFromState;

    private TransitionIterator mTauIterator;
    private TransitionIterator mEventIterator;

  }


  //#########################################################################
  //# Inner Class PostEventClosureTransitionIterator
  private class PostEventClosureTransitionIterator
    implements TransitionIterator
  {

    //#######################################################################
    //# Constructor
    private PostEventClosureTransitionIterator(final TransitionIterator inner)
    {
      mEventIterator = inner;
      mTauIterator = createIterator();
      mFromState = -1;
      mStart = true;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.analysis.tr.TransitionIterator
    @Override
    public PostEventClosureTransitionIterator clone()
    {
      try {
        final PostEventClosureTransitionIterator cloned =
          (PostEventClosureTransitionIterator) super.clone();
        cloned.mTauIterator = mTauIterator.clone();
        cloned.mEventIterator = mEventIterator.clone();
        return cloned;
      } catch (final CloneNotSupportedException exception) {
        throw new WatersRuntimeException(exception);
      }
    }

    @Override
    public void reset()
    {
      mEventIterator.resetState(mFromState);
      mStart = true;
    }

    @Override
    public void resetEvent(final int event)
    {
      mEventIterator.reset(mFromState, event);
      mStart = true;
    }

    @Override
    public void resetEvents(final int first, final int last)
    {
      mEventIterator.resetEvents(first, last);
      reset();
    }

    @Override
    public void resetState(final int from)
    {
      mFromState = from;
      reset();
    }

    @Override
    public void reset(final int from, final int event)
    {
      mFromState = from;
      resetEvent(event);
    }

    @Override
    public void resume(final int state)
    {
      resetState(state);
    }

    @Override
    public int getFirstEvent()
    {
      return mEventIterator.getFirstEvent();
    }

    @Override
    public int getLastEvent()
    {
      return mEventIterator.getLastEvent();
    }

    @Override
    public boolean advance()
    {
      if (mStart) {
        mStart = false;
      } else if (mTauIterator.advance()) {
        return true;
      }
      if (mEventIterator.advance()) {
        final int state = mEventIterator.getCurrentToState();
        mTauIterator.resetState(state);
        return mTauIterator.advance();
      } else {
        return false;
      }
    }

    @Override
    public boolean isValid()
    {
      return mTauIterator.isValid();
    }

    @Override
    public int getCurrentEvent()
    {
      return mEventIterator.getCurrentEvent();
    }

    @Override
    public int getCurrentSourceState()
    {
      return mTransitionBuffer.getIteratorSourceState(this);
    }

    @Override
    public int getCurrentToState()
    {
      return mTauIterator.getCurrentToState();
    }

    @Override
    public int getCurrentTargetState()
    {
      return mTransitionBuffer.getIteratorTargetState(this);
    }

    @Override
    public int getCurrentFromState()
    {
      return mFromState;
    }

    @Override
    public void setCurrentToState(final int state)
    {
      throw new UnsupportedOperationException
        (ProxyTools.getShortClassName(this) +
         " does not support transition modification!");
    }

    @Override
    public void remove()
    {
      throw new UnsupportedOperationException
        (ProxyTools.getShortClassName(this) +
         " does not support transition removal!");
    }

    //#######################################################################
    //# Data Members
    private int mFromState;
    private boolean mStart = true;

    private TransitionIterator mEventIterator;
    private TransitionIterator mTauIterator;

  }


  //#########################################################################
  //# Inner Class FullEventClosureTransitionIterator
  private class FullEventClosureTransitionIterator
    implements TransitionIterator
  {

    //#######################################################################
    //# Constructor
    private FullEventClosureTransitionIterator(final TransitionIterator inner)
    {
      mTauIterator1 = createIterator();
      mEventIterator = inner;
      mTauIterator2 = createIterator();
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.analysis.tr.TransitionIterator
    @Override
    public FullEventClosureTransitionIterator clone()
    {
      try {
        final FullEventClosureTransitionIterator cloned =
          (FullEventClosureTransitionIterator) super.clone();
        cloned.mTauIterator1 = mTauIterator1.clone();
        cloned.mEventIterator = mEventIterator.clone();
        cloned.mTauIterator2 = mTauIterator2.clone();
        return cloned;
      } catch (final CloneNotSupportedException exception) {
        throw new WatersRuntimeException(exception);
      }
    }

    @Override
    public void reset()
    {
      mTauIterator1.resetState(mFromState);
      mStart = true;
    }

    @Override
    public void resetEvent(final int event)
    {
      mEventIterator.resetEvent(event);
      reset();
    }

    @Override
    public void resetEvents(final int first, final int last)
    {
      mEventIterator.resetEvents(first, last);
      reset();
    }

    @Override
    public void resetState(final int from)
    {
      mFromState = from;
      reset();
    }

    @Override
    public void reset(final int from, final int event)
    {
      mFromState = from;
      resetEvent(event);
    }

    @Override
    public void resume(final int state)
    {
      resetState(state);
    }

    @Override
    public int getFirstEvent()
    {
      return mEventIterator.getFirstEvent();
    }

    @Override
    public int getLastEvent()
    {
      return mEventIterator.getLastEvent();
    }

    @Override
    public boolean advance()
    {
      if (mStart) {
        mStart = false;
      } else if (mTauIterator2.advance()) {
        return true;
      } else if (mEventIterator.advance()) {
        final int tausucc = mEventIterator.getCurrentToState();
        mTauIterator2.resetState(tausucc);
        return mTauIterator2.advance(); // always true
      }
      while (mTauIterator1.advance()) {
        final int succ = mTauIterator1.getCurrentToState();
        mEventIterator.resetState(succ);
        if (mEventIterator.advance()) {
          final int tausucc = mEventIterator.getCurrentToState();
          mTauIterator2.resetState(tausucc);
          return mTauIterator2.advance(); // always true
        }
      }
      return false;
    }

    @Override
    public boolean isValid()
    {
      return !mStart && mTauIterator2.isValid();
    }

    @Override
    public int getCurrentEvent()
    {
      return mEventIterator.getCurrentEvent();
    }

    @Override
    public int getCurrentSourceState()
    {
      return mTransitionBuffer.getIteratorSourceState(this);
    }

    @Override
    public int getCurrentToState()
    {
      if (mStart) {
        throw new NoSuchElementException("Reading past end of list in " +
                                         ProxyTools.getShortClassName(this));
      } else {
        return mTauIterator2.getCurrentToState();
      }
    }

    @Override
    public void setCurrentToState(final int state)
    {
      throw new UnsupportedOperationException
        (ProxyTools.getShortClassName(this) +
         " does not support transition modification!");
    }

    @Override
    public int getCurrentTargetState()
    {
      return mTransitionBuffer.getIteratorTargetState(this);
    }

    @Override
    public int getCurrentFromState()
    {
      return mFromState;
    }

    @Override
    public void remove()
    {
      throw new UnsupportedOperationException
        (ProxyTools.getShortClassName(this) +
         " does not support transition removal!");
    }

    //#######################################################################
    //# Data Members
    private int mFromState;
    private boolean mStart;

    private TransitionIterator mTauIterator1;
    private TransitionIterator mEventIterator;
    private TransitionIterator mTauIterator2;

  }


  //#########################################################################
  //# Data Members
  /**
   * The transition list buffer this tau-closure refers to.
   */
  private final TransitionListBuffer mTransitionBuffer;
  /**
   * A template iterator that defines what transitions are covered in the
   * tau closure. This iterator is not used directly, it is copied using
   * {@link TransitionIterator#clone()} when creating iterators over the
   * tau closure.
   */
  private TransitionIterator mIteratorTemplate;
  /**
   * Arrays of stored tau-successors for each state.
   * The state itself is never explicitly stored, although it is returned
   * as a tau-successor by all iterators. States with no tau-successors other
   * than themselves have an entry referencing to {@link #EMPTY_ARRAY}.
   */
  private final int[][] mStoredTransitions;


  //#########################################################################
  //# Class Constants
  private static final int[] EMPTY_ARRAY = new int[0];

}
