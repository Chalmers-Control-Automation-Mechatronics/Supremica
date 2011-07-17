//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.tr
//# CLASS:   TauClosure
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.tr;

import gnu.trove.TIntArrayList;
import gnu.trove.TIntHashSet;
import java.util.NoSuchElementException;

import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.des.AutomatonTools;


/**
 * A utility class to iterate over transitions in the tau-closure of
 * a transition relation. This class can be configured to compute the
 * tau closure on the fly for each iteration, or to compute it once and
 * use it for subsequent requests.
 *
 * @author Robi Malik
 */
public class TauClosure
{

  //#########################################################################
  //# Constructor
  /**
   * Computes the tau-closure for the given transition list buffer.
   * All transitions in the tau closure are precomputed and stored by
   * this constructor. The precomputed tau-closure does not update
   * automatically when the transition relation changes.
   * @param  buffer   The transition list buffer containing the transitions
   *                  for which tau-closure is computed.
   */
  public TauClosure(final TransitionListBuffer buffer)
  {
    this(buffer, Integer.MAX_VALUE);
  }

  /**
   * Creates a tau-closure for the given transition list buffer,
   * which may or may not be precomputed. If precomputed, the tau-closure
   * does not update automatically when the transition relation changes.
   * @param  buffer   The transition list buffer containing the transitions
   *                  for which tau-closure is computed.
   * @param  limit    The maximum number of transitions that can be stored
   *                  in the tau-closure. If the number of computed
   *                  tau-transitions exceeds the limit, precomputation is
   *                  aborted and transitions will be produced on the fly by
   *                  iterators. A limit of&nbsp;0 forces the tau closure
   *                  always to be computed on the fly.
   */
  public TauClosure(final TransitionListBuffer buffer, int limit)
  {
    mTransitionBuffer = buffer;
    final int numStates = mTransitionBuffer.getNumberOfStates();
    mEventShift = AutomatonTools.log2(numStates);
    if (limit > 0) {
      final int[][] trans = new int[numStates][];
      final TransitionIterator iter =
        new OnTheFlyTauClosureIterator(mTransitionBuffer);
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
          trans[state] = list.toNativeArray();
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
      return new OnTheFlyTauClosureIterator(mTransitionBuffer);
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
      return new OnTheFlyTauClosureIterator(mTransitionBuffer, from);
    } else {
      return new StoredTauClosureIterator(from);
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
    return new PreEventClosureTransitionIterator();
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
   *          pre-event closure iterator is returned, which can be reset to
   *          iterate over other events except {@link EventEncoding#TAU}.
   */
  public TransitionIterator createPreEventClosureIterator(final int event)
  {
    if (event == EventEncoding.TAU) {
      return createIterator();
    } else {
      return new PreEventClosureTransitionIterator(event);
    }
  }

  /**
   * Creates an iterator over the pre-event closure of the underlying transition
   * relation. The iterator returned by this method produces transitions with an
   * event e by following an arbitrary number of {@link EventEncoding#TAU}
   * event and then executing a transition with event e.
   *
   * The returned iterator is a read-only iterator and does
   * not implement the {@link TransitionIterator#remove()} method.
   *
   * @param from
   *          The start state of the iteration. The iterator is set up to
   *          start iterating from this state with the given event. It can
   *          also be reset to start iterating from other states.
   * @param event
   *          The event used by the iterator. If this is
   *          {@link EventEncoding#TAU}, the method returns a plain tau-closure
   *          iterator that can only iterate with the silent event. Otherwise a
   *          pre-event closure iterator is returned, which can be reset to
   *          iterate over other events except {@link EventEncoding#TAU}.
   */
  public TransitionIterator createPreEventClosureIterator(final int from,
                                                          final int event)
  {
    if (event == EventEncoding.TAU) {
      return createIterator(from);
    } else {
      return new PreEventClosureTransitionIterator(from, event);
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
    return new PostEventClosureTransitionIterator();
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
   *          post-event closure iterator is returned, which can be reset to
   *          iterate over other events except {@link EventEncoding#TAU}.
   */
  public TransitionIterator createPostEventClosureIterator(final int event)
  {
    if (event == EventEncoding.TAU) {
      return createIterator();
    } else {
      return new PostEventClosureTransitionIterator(event);
    }
  }

  /**
   * Creates an iterator over the post-event closure of the underlying
   * transition relation. The iterator returned by this method produces
   * transitions with an event e by following an arbitrary number of
   * {@link EventEncoding#TAU} after executing a transition with event e.
   *
   * The returned iterator is a read-only iterator and does
   * not implement the {@link TransitionIterator#remove()} method.
   *
   * @param from
   *          The start state of the iteration. The iterator is set up to
   *          start iterating from this state with the given event. It can
   *          also be reset to start iterating from other states.
   * @param event
   *          The event used by the iterator. If this is
   *          {@link EventEncoding#TAU}, the method returns a plain tau-closure
   *          iterator that can only iterate with the silent event. Otherwise a
   *          pre-event closure iterator is returned, which can be reset to
   *          iterate over other events except {@link EventEncoding#TAU}.
   */
  public TransitionIterator createPostEventClosureIterator(final int from,
                                                           final int event)
  {
    if (event == EventEncoding.TAU) {
      return createIterator(from);
    } else {
      return new PostEventClosureTransitionIterator(from, event);
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
    return new FullEventClosureTransitionIterator();
  }

  /**
   * Creates an iterator over the event-closure of the underlying transition
   * relation. The iterator returned by this method produces transitions by
   * first following an arbitrary number of {@link EventEncoding#TAU} event,
   * then executing a transition with event the given event, followed by another
   * arbitrary number of {@link EventEncoding#TAU} events.
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
   *          proper event closure iterator is returned, which can be reset to
   *          iterate over other events except {@link EventEncoding#TAU}.
   */
  public TransitionIterator createFullEventClosureIterator(final int event)
  {
    if (event == EventEncoding.TAU) {
      return createIterator();
    } else {
      return new FullEventClosureTransitionIterator(event);
    }
  }

  /**
   * Creates an iterator over the event-closure of the underlying transition
   * relation. The iterator returned by this method produces transitions by
   * first following an arbitrary number of {@link EventEncoding#TAU} event,
   * then executing a transition with event the given event, followed by another
   * arbitrary number of {@link EventEncoding#TAU} events.
   *
   * The returned iterator is a read-only iterator and does
   * not implement the {@link TransitionIterator#remove()} method.
   *
   * @param from
   *          The start state of the iteration. The iterator is set up to
   *          start iterating from this state with the given event. It can
   *          also be reset to start iterating from other states.
   * @param event
   *          The event used by the iterator. If this is
   *          {@link EventEncoding#TAU}, the method returns a plain tau-closure
   *          iterator that can only iterate with the silent event. Otherwise a
   *          proper event closure iterator is returned, which can be reset to
   *          iterate over other events except {@link EventEncoding#TAU}.
   */
  public TransitionIterator createFullEventClosureIterator(final int from,
                                                           final int event)
  {
    if (event == EventEncoding.TAU) {
      return createIterator(from);
    } else {
      return new FullEventClosureTransitionIterator(from, event);
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
    //# Interface net.sourceforge.waters.analysis.op.TransitionIterator
    public void resetEvent(final int event)
    {
      if (event == EventEncoding.TAU) {
        reset();
      } else {
        throwNonTauException();
      }
    }

    public void resetEvents(final int first, final int last)
    {
      if (first == EventEncoding.TAU && last == EventEncoding.TAU) {
        reset();
      } else {
        throwNonTauException();
      }
    }

    public void resetState(final int from)
    {
      mFrom = from;
      reset();
    }

    public void reset(final int from, final int event)
    {
      mFrom = from;
      resetEvent(event);
    }

    public int getCurrentEvent()
    {
      return EventEncoding.TAU;
    }

    public int getCurrentFromState()
    {
      return mFrom;
    }

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
    private OnTheFlyTauClosureIterator(final TransitionListBuffer buffer)
    {
      this(buffer, -1);
    }

    private OnTheFlyTauClosureIterator(final TransitionListBuffer buffer,
                                       final int from)
    {
      super(from);
      mTransitionBuffer = buffer;
      mOpen = new TIntArrayList();
      mVisited = new TIntHashSet();
      mInner = mTransitionBuffer.createReadOnlyIterator();
      mInner.resetEvent(EventEncoding.TAU);
      mCurrentState = -1;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.analysis.op.TransitionIterator
    public void reset()
    {
      mOpen.clear();
      mVisited.clear();
      mInner.resetState(-1);
      mCurrentState = -1;
    }

    public boolean advance()
    {
      if (mVisited.isEmpty()) {
        final int from = getCurrentFromState();
        mCurrentState = from;
        mOpen.add(from);
        mVisited.add(from);
        mInner.resetState(-1);
        return true;
      }
      while (true) {
        while (mInner.advance()) {
          final int next = mInner.getCurrentToState();
          if (mVisited.add(next)) {
            mOpen.add(next);
            mCurrentState = next;
            return true;
          }
        }
        if (mOpen.isEmpty()) {
          mCurrentState = -1;
          return false;
        } else {
          final int end = mOpen.size() - 1;
          final int state = mOpen.remove(end);
          mInner.resetState(state);
        }
      }
    }

    public int getCurrentEvent()
    {
      return EventEncoding.TAU;
    }

    public int getCurrentSourceState()
    {
      return mTransitionBuffer.getIteratorSourceState(this);
    }

    public int getCurrentToState()
    {
      if (mCurrentState >= 0) {
        return mCurrentState;
      } else {
        throw new NoSuchElementException("Reading past end of list in " +
                                         ProxyTools.getShortClassName(this));
      }
    }

    public int getCurrentTargetState()
    {
      return mTransitionBuffer.getIteratorTargetState(this);
    }

    public void remove()
    {
      throw new UnsupportedOperationException
        (ProxyTools.getShortClassName(this) +
         " does not support transition removal!");
    }

    //#######################################################################
    //# Data Members
    private final TransitionListBuffer mTransitionBuffer;

    private final TIntArrayList mOpen;
    private final TIntHashSet mVisited;
    private final TransitionIterator mInner;
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
    //# Interface net.sourceforge.waters.analysis.op.TransitionIterator
    public void reset()
    {
      final int from = getCurrentFromState();
      mCurrentArray = mStoredTransitions[from];
      mIndex = -2;
    }

    public boolean advance()
    {
      return ++mIndex < mCurrentArray.length;
    }

    public int getCurrentSourceState()
    {
      return mTransitionBuffer.getIteratorSourceState(this);
    }

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
      mVisited = new TIntHashSet();
      mFromState = -1;
    }

    private PreEventClosureTransitionIterator(final int event)
    {
      mTauIterator = createIterator();
      mEventIterator = mTransitionBuffer.createReadOnlyIterator(event);
      mVisited = new TIntHashSet();
      mFromState = -1;
    }

    private PreEventClosureTransitionIterator(final int from, final int event)
    {
      mTauIterator = createIterator();
      mEventIterator = mTransitionBuffer.createReadOnlyIterator(event);
      mVisited = new TIntHashSet();
      reset(from, event);
      mFirstEvent = event;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.analysis.op.TransitionIterator
    public void reset()
    {
      mTauIterator.resetState(mFromState);
      mTauIterator.advance();
      mEventIterator.resetState(mFromState);
      mVisited.clear();
    }

    public void resetEvent(final int event)
    {
      mTauIterator.resetState(mFromState);
      mTauIterator.advance();
      mEventIterator.reset(mFromState, event);
      mVisited.clear();
      mFirstEvent = event;
    }

    public void resetEvents(final int first, final int last)
    {
      mFirstEvent = first;
      mEventIterator.resetEvents(first, last);
      reset();
    }

    public void resetState(final int from)
    {
      mFromState = from;
      reset();
    }

    public void reset(final int from, final int event)
    {
      mFromState = from;
      resetEvent(event);
    }

    public boolean advance()
    {
      while (seek()) {
        final int state = mEventIterator.getCurrentToState();
        final int event = mEventIterator.getCurrentEvent() - mFirstEvent;
        final int key = (event << mEventShift) | state;
        if (mVisited.add(key)) {
          return true;
        }
      }
      return false;
    }

    public int getCurrentEvent()
    {
      return mEventIterator.getCurrentEvent();
    }

    public int getCurrentSourceState()
    {
      return mTransitionBuffer.getIteratorSourceState(this);
    }

    public int getCurrentToState()
    {
      return mEventIterator.getCurrentToState();
    }

    public int getCurrentTargetState()
    {
      return mTransitionBuffer.getIteratorTargetState(this);
    }

    public int getCurrentFromState()
    {
      return mFromState;
    }

    public void remove()
    {
      throw new UnsupportedOperationException
        (ProxyTools.getShortClassName(this) +
         " does not support transition removal!");
    }

    //#######################################################################
    //# Auxiliary Methods
    private boolean seek()
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

    //#######################################################################
    //# Data Members
    private int mFromState;
    private int mFirstEvent;

    private final TransitionIterator mTauIterator;
    private final TransitionIterator mEventIterator;
    private final TIntHashSet mVisited;

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
      mVisited = new TIntHashSet();
      mFromState = -1;
      mStart = true;
    }

    private PostEventClosureTransitionIterator(final int event)
    {
      mEventIterator = mTransitionBuffer.createReadOnlyIterator(event);
      mTauIterator = createIterator();
      mVisited = new TIntHashSet();
      mFromState = -1;
      mStart = true;
    }

    private PostEventClosureTransitionIterator(final int from, final int event)
    {
      mEventIterator = mTransitionBuffer.createReadOnlyIterator(event);
      mTauIterator = createIterator();
      mVisited = new TIntHashSet();
      mFirstEvent = event;
      reset(from, event);
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.analysis.op.TransitionIterator
    public void reset()
    {
      mEventIterator.resetState(mFromState);
      mVisited.clear();
      mStart = true;
    }

    public void resetEvent(final int event)
    {
      mEventIterator.reset(mFromState, event);
      mVisited.clear();
      mFirstEvent = event;
      mStart = true;
    }

    public void resetEvents(final int first, final int last)
    {
      mFirstEvent = first;
      mEventIterator.resetEvents(first, last);
      reset();
    }

    public void resetState(final int from)
    {
      mFromState = from;
      reset();
    }

    public void reset(final int from, final int event)
    {
      mFromState = from;
      resetEvent(event);
    }

    public boolean advance()
    {
      while (seek()) {
        final int state = mTauIterator.getCurrentToState();
        final int event = mEventIterator.getCurrentEvent() - mFirstEvent;
        final int key = (event << mEventShift) | state;
        if (mVisited.add(key)) {
          return true;
        }
      }
      return false;
    }

    public int getCurrentEvent()
    {
      return mEventIterator.getCurrentEvent();
    }

    public int getCurrentSourceState()
    {
      return mTransitionBuffer.getIteratorSourceState(this);
    }

    public int getCurrentToState()
    {
      return mTauIterator.getCurrentToState();
    }

    public int getCurrentTargetState()
    {
      return mTransitionBuffer.getIteratorTargetState(this);
    }

    public int getCurrentFromState()
    {
      return mFromState;
    }

    public void remove()
    {
      throw new UnsupportedOperationException
        (ProxyTools.getShortClassName(this) +
         " does not support transition removal!");
    }

    //#######################################################################
    //# Auxiliary Methods
    private boolean seek()
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

    //#######################################################################
    //# Data Members
    private int mFromState;
    private int mFirstEvent;
    private boolean mStart = true;

    private final TransitionIterator mEventIterator;
    private final TransitionIterator mTauIterator;
    private final TIntHashSet mVisited;

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
      mVisited = new TIntHashSet(TransitionListBuffer.HASH_STRATEGY);
    }

    private FullEventClosureTransitionIterator(final int event)
    {
      mTauIterator1 = createIterator();
      mEventIterator = mTransitionBuffer.createReadOnlyIterator(event);
      mTauIterator2 = createIterator();
      mVisited = new TIntHashSet(TransitionListBuffer.HASH_STRATEGY);
      mFirstEvent = event;
    }

    private FullEventClosureTransitionIterator(final int from, final int event)
    {
      mTauIterator1 = createIterator();
      mEventIterator = mTransitionBuffer.createReadOnlyIterator();
      mTauIterator2 = createIterator();
      mVisited = new TIntHashSet(TransitionListBuffer.HASH_STRATEGY);
      reset(from, event);
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.analysis.op.TransitionIterator
    public void reset()
    {
      mTauIterator1.resetState(mCurrent);
      mEventIterator.resetState(mCurrent);
      mVisited.clear();
      mStart = true;
    }

    public void resetEvent(final int event)
    {
      mTauIterator1.resetState(mCurrent);
      mEventIterator.reset(mCurrent, event);
      mVisited.clear();
      mFirstEvent = event;
      mStart = true;
    }

    public void resetEvents(final int first, final int last)
    {
      mEventIterator.resetEvents(first, last);
      mFirstEvent = first;
      reset();
    }

    public void resetState(final int from)
    {
      mCurrent = from;
      reset();
    }

    public void reset(final int from, final int event)
    {
      mCurrent = from;
      resetEvent(event);
    }

    public boolean advance()
    {
      while (seek()) {
        final int state = mTauIterator2.getCurrentToState();
        final int event = mEventIterator.getCurrentEvent() - mFirstEvent;
        final int key = (event << mEventShift) | state;
        if (mVisited.add(key)) {
          return true;
        }
      }
      mStart = true;
      return false;
    }

    public int getCurrentEvent()
    {
      return mEventIterator.getCurrentEvent();
    }

    public int getCurrentSourceState()
    {
      return mTransitionBuffer.getIteratorSourceState(this);
    }

    public int getCurrentToState()
    {
      if (mStart) {
        throw new NoSuchElementException("Reading past end of list in " +
                                         ProxyTools.getShortClassName(this));
      } else {
        return mTauIterator2.getCurrentToState();
      }
    }

    public int getCurrentTargetState()
    {
      return mTransitionBuffer.getIteratorTargetState(this);
    }

    public int getCurrentFromState()
    {
      return mCurrent;
    }

    public void remove()
    {
      throw new UnsupportedOperationException
        (ProxyTools.getShortClassName(this) +
         " does not support transition removal!");
    }

    //#######################################################################
    //# Auxiliary Methods
    private boolean seek()
    {
      if (mStart) {
        mStart = false;
        mTauIterator1.advance(); // always succeeds and gives mCurrent
        if (mEventIterator.advance()) {
          final int succ = mEventIterator.getCurrentToState();
          mTauIterator2.resetState(succ);
          return mTauIterator2.advance(); // always true
        }
      } else if (mTauIterator2.advance()) {
        return true;
      } else if (mEventIterator.advance()) {
        final int tausucc = mEventIterator.getCurrentToState();
        mTauIterator2.resetState(tausucc);
        return mTauIterator2.advance(); // always true
      }
      while (mTauIterator1.advance()) {
        int succ = mTauIterator1.getCurrentToState();
        mEventIterator.resetState(succ);
        if (mEventIterator.advance()) {
          succ = mEventIterator.getCurrentToState();
          mTauIterator2.resetState(succ);
          return mTauIterator2.advance(); // always true
        }
      }
      return false;
    }

    //#######################################################################
    //# Data Members
    private int mFirstEvent;
    private int mCurrent;
    private boolean mStart;

    private final TransitionIterator mTauIterator1;
    private final TransitionIterator mEventIterator;
    private final TransitionIterator mTauIterator2;
    private final TIntHashSet mVisited;

  }


  //#########################################################################
  //# Data Members
  /**
   * The transition list buffer this tau-closure refers to.
   */
  private final TransitionListBuffer mTransitionBuffer;
  /**
   * Arrays of stored tau-successors for each state.
   * The state itself is never explicitly stored, although it is returned
   * as a tau-successor by all iterators. States with no tau-successors other
   * than themselves have an entry referencing to {@link #EMPTY_ARRAY}.
   */
  private final int[][] mStoredTransitions;
  /**
   * Shift amount for state/event pair encoding in iterators.
   */
  private final int mEventShift;


  //#########################################################################
  //# Class Constants
  private static final int[] EMPTY_ARRAY = new int[0];

}
