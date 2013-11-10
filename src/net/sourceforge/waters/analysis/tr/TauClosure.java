//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.tr
//# CLASS:   TauClosure
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.tr;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.stack.TIntStack;
import gnu.trove.stack.array.TIntArrayStack;

import java.util.NoSuchElementException;

import net.sourceforge.waters.model.base.ProxyTools;


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
     this(buffer, EventEncoding.TAU, EventEncoding.TAU, limit);
  }

/**
   * Creates a tau-closure for the given transition list buffer,
   * which may or may not be precomputed. If precomputed, the tau-closure
   * does not update automatically when the transition relation changes.
   * @param  buffer     The transition list buffer containing the transitions
   *                    for which tau-closure is computed.
   * @param  firstLocal The code of the first event considered as local
   *                    (i.e., tau) by this tau-closure.
   * @param  lastLocal  The code of the last event considered as local
   *                    (i.e., tau) by this tau-closure.
   * @param  limit      The maximum number of transitions that can be stored
   *                    in the tau-closure. If the number of computed
   *                    tau-transitions exceeds the limit, precomputation is
   *                    aborted and transitions will be produced on the fly by
   *                    iterators. A limit of&nbsp;0 forces the tau closure
   *                    always to be computed on the fly.
   */
  public TauClosure(final TransitionListBuffer buffer,
                    final int firstLocal,
                    final int lastLocal,
                    int limit)
  {
    mTransitionBuffer = buffer;
    mFirstLocal = firstLocal;
    mLastLocal = lastLocal;
    if (limit > 0) {
      final int numStates = mTransitionBuffer.getNumberOfStates();
      final int[][] trans = new int[numStates][];
      final TransitionIterator iter =
        new OnTheFlyTauClosureIterator(mTransitionBuffer,
                                       firstLocal, lastLocal);
      final TIntArrayList list = new TIntArrayList();
      for (int state = 0; state < numStates; state++) {
        iter.resetState(state);
        iter.advance();
        while (iter.advance()) {
          if (--limit == 0) {
            mStoredTransitions = null;
            return;
          }
          final int succ = iter.getCurrentToState();
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
      return new OnTheFlyTauClosureIterator(mTransitionBuffer,
                                            mFirstLocal, mLastLocal);
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
      return new OnTheFlyTauClosureIterator(mTransitionBuffer, from,
                                            mFirstLocal, mLastLocal);
    } else {
      return new StoredTauClosureIterator(from);
    }
  }

  /**
   * Creates an iterator over this tau-closure that is guaranteed to
   * support caching. This method produces an iterator like {@link
   * #createIterator()} with the additional guarantee that it supports
   * caching and fully implements the {@link TransitionIterator#resume()
   * resume()} method.
   * @see #createIterator()
   */
  public TransitionIterator createCachingIterator()
  {
    if (mStoredTransitions == null) {
      return new OnTheFlyTauClosureIterator(mTransitionBuffer,
                                            mFirstLocal, mLastLocal);
    } else {
      final TransitionIterator inner = new StoredTauClosureIterator();
      return new OneEventCachingTransitionIterator(inner, EventEncoding.TAU);
    }
  }


  /**
   * Creates an iterator over the pre-event closure of the underlying transition
   * relation. The iterator returned by this method produces transitions with an
   * event e by following an arbitrary number of {@link EventEncoding#TAU}
   * event and then executing a transition with event e.
   *
   * The iterator returned is not initialised, so the method
   * {@link TransitionIterator#reset(int,int) reset()} must be used before it
   * can be used. The returned iterator is a read-only iterator and does not
   * implement the {@link TransitionIterator#remove()} method. It can also not
   * be used to iterate using the silent event {@link EventEncoding#TAU}.
   */
  public TransitionIterator createPreEventClosureIterator()
  {
    final TransitionIterator inner;
    if (mTransitionBuffer instanceof OutgoingTransitionListBuffer) {
      inner = new PreEventClosureTransitionIterator();
    } else {
      inner = new PostEventClosureTransitionIterator();
    }
    return new CachingTransitionIterator(mTransitionBuffer, inner);
  }

  /**
   * Creates an iterator over the pre-event closure of the underlying transition
   * relation. The iterator returned by this method produces transitions with an
   * event e by following an arbitrary number of {@link EventEncoding#TAU}
   * event and then executing a transition with event e.
   *
   * The iterator returned is not initialised, so the method
   * {@link TransitionIterator#resetState(int) resetState()} must be used before
   * it can be used. The returned iterator is a read-only iterator and does
   * not implement the {@link TransitionIterator#remove()} method.
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
      final TransitionIterator inner;
      if (mTransitionBuffer instanceof OutgoingTransitionListBuffer) {
        inner = new PreEventClosureTransitionIterator(event);
      } else {
        inner = new PostEventClosureTransitionIterator(event);
      }
      return new OneEventCachingTransitionIterator(inner, event);
    }
  }

  /**
   * Creates an iterator over the post-event closure of the underlying
   * transition relation. The iterator returned by this method produces
   * transitions with an event e by following an arbitrary number of
   * {@link EventEncoding#TAU} after executing a transition with event e.
   *
   * The iterator returned is not initialised, so the method
   * {@link TransitionIterator#reset(int,int) reset()} must be used before it
   * can be used. The returned iterator is a read-only iterator and does not
   * implement the {@link TransitionIterator#remove()} method. It can also not
   * be used to iterate using the silent event {@link EventEncoding#TAU}.
   */
  public TransitionIterator createPostEventClosureIterator()
  {
    final TransitionIterator inner;
    if (mTransitionBuffer instanceof OutgoingTransitionListBuffer) {
      inner = new PostEventClosureTransitionIterator();
    } else {
      inner = new PreEventClosureTransitionIterator();
    }
    return new CachingTransitionIterator(mTransitionBuffer, inner);
  }

  /**
   * Creates an iterator over the post-event closure of the underlying
   * transition relation. The iterator returned by this method produces
   * transitions with an event e by following an arbitrary number of
   * {@link EventEncoding#TAU} after executing a transition with event e.
   *
   * The iterator returned is not initialised, so the method
   * {@link TransitionIterator#resetState(int) resetState()} must be used before
   * it can be used. The returned iterator is a read-only iterator and does
   * not implement the {@link TransitionIterator#remove()} method.
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
      final TransitionIterator inner;
      if (mTransitionBuffer instanceof OutgoingTransitionListBuffer) {
        inner = new PostEventClosureTransitionIterator(event);
      } else {
        inner = new PreEventClosureTransitionIterator(event);
      }
      return new OneEventCachingTransitionIterator(inner, event);
    }
  }

  /**
   * Creates an iterator over the event-closure of the underlying transition
   * relation. The iterator returned by this method produces transitions with an
   * event e by first following an arbitrary number of {@link EventEncoding#TAU}
   * event, then executing a transition with event e, followed by another
   * arbitrary number of {@link EventEncoding#TAU} events.
   *
   * The iterator returned is not initialised, so the method
   * {@link TransitionIterator#reset(int,int) reset()} must be used before it
   * can be used. The returned iterator is a read-only iterator and does not
   * implement the {@link TransitionIterator#remove()} method. It can also not
   * be used to iterate using the silent event {@link EventEncoding#TAU}.
   */
  public TransitionIterator createFullEventClosureIterator()
  {
    final TransitionIterator inner = new FullEventClosureTransitionIterator();
    return new CachingTransitionIterator(mTransitionBuffer, inner);
  }

  /**
   * Creates an iterator over the event-closure of the underlying transition
   * relation. The iterator returned by this method produces transitions with an
   * event e by first following an arbitrary number of {@link EventEncoding#TAU}
   * event, then executing a transition with event e, followed by another
   * arbitrary number of {@link EventEncoding#TAU} events.
   *
   * The iterator returned is not initialised, so the method
   * {@link TransitionIterator#reset(int,int) reset()} must be used before it
   * can be used. The returned iterator is a read-only iterator and does not
   * implement the {@link TransitionIterator#remove()} method.
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
        new FullEventClosureTransitionIterator(event);
      return new OneEventCachingTransitionIterator(inner, event);
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
    public void resetEventsByStatus(final int... flags)
    {
      throwNonTauException();
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
                                       final int firstLocal,
                                       final int lastLocal)
    {
      this(buffer, -1, firstLocal, lastLocal);
    }

    private OnTheFlyTauClosureIterator(final TransitionListBuffer buffer,
                                       final int from,
                                       final int firstLocal,
                                       final int lastLocal)
    {
      super(from);
      mTransitionBuffer = buffer;
      mStack = new TIntArrayStack();
      mInner = mTransitionBuffer.createReadOnlyIterator();
      mInner.resetEvents(firstLocal, lastLocal);
      mVisited = new TIntHashSet();
      mCurrentState = -1;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.analysis.tr.TransitionIterator
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
    private final TIntStack mStack;
    private final TransitionIterator mInner;

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
    private PreEventClosureTransitionIterator()
    {
      mTauIterator = createIterator();
      mEventIterator = mTransitionBuffer.createReadOnlyIterator();
      mFromState = -1;
    }

    private PreEventClosureTransitionIterator(final int event)
    {
      mTauIterator = createIterator();
      mEventIterator = mTransitionBuffer.createReadOnlyIterator(event);
      mFromState = -1;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.analysis.tr.TransitionIterator
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
    public void resetEventsByStatus(final int... flags)
    {
      mEventIterator.resetEventsByStatus(flags);
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

    private final TransitionIterator mTauIterator;
    private final TransitionIterator mEventIterator;

  }


  //#########################################################################
  //# Inner Class PostEventClosureTransitionIterator
  private class PostEventClosureTransitionIterator
    implements TransitionIterator
  {

    //#######################################################################
    //# Constructor
    private PostEventClosureTransitionIterator()
    {
      mEventIterator = mTransitionBuffer.createReadOnlyIterator();
      mTauIterator = createIterator();
      mFromState = -1;
      mStart = true;
    }

    private PostEventClosureTransitionIterator(final int event)
    {
      mEventIterator = mTransitionBuffer.createReadOnlyIterator(event);
      mTauIterator = createIterator();
      mFromState = -1;
      mStart = true;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.analysis.tr.TransitionIterator
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
    public void resetEventsByStatus(final int... flags)
    {
      mEventIterator.resetEventsByStatus(flags);
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

    private final TransitionIterator mEventIterator;
    private final TransitionIterator mTauIterator;

  }


  //#########################################################################
  //# Inner Class FullEventClosureTransitionIterator
  private class FullEventClosureTransitionIterator
    implements TransitionIterator
  {

    //#######################################################################
    //# Constructor
    private FullEventClosureTransitionIterator()
    {
      mTauIterator1 = createIterator();
      mEventIterator = mTransitionBuffer.createReadOnlyIterator();
      mTauIterator2 = createIterator();
    }

    private FullEventClosureTransitionIterator(final int event)
    {
      mTauIterator1 = createIterator();
      mEventIterator = mTransitionBuffer.createReadOnlyIterator(event);
      mTauIterator2 = createIterator();
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.analysis.tr.TransitionIterator
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
    public void resetEventsByStatus(final int... flags)
    {
      mEventIterator.resetEventsByStatus(flags);
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

    private final TransitionIterator mTauIterator1;
    private final TransitionIterator mEventIterator;
    private final TransitionIterator mTauIterator2;

  }


  //#########################################################################
  //# Data Members
  /**
   * The transition list buffer this tau-closure refers to.
   */
  private final TransitionListBuffer mTransitionBuffer;
  /**
   * The code of the first event to be considered as local (i.e., tau) by this
   * closure.
   */
  private int mFirstLocal;
  /**
   * The code of the last event to be considered as local (i.e., tau) by this
   * closure.
   */
  private int mLastLocal;
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

