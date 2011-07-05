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
   * @param  limit    The maximum number of transitions that can be stored.
   *                  If the number of transitions already in the transition
   *                  list buffer plus the number of computed tau transitions
   *                  exceeds the limit, precomputation is aborted and
   *                  transitions will be produced on the fly by iterators.
   *                  It limit of&nbsp;0 forces the tau closure always to
   *                  be computed on the fly.
   */
  public TauClosure(final TransitionListBuffer buffer, final int limit)
  {
    mTransitionBuffer = buffer;
    int numtrans = mTransitionBuffer.getNumberOfTransitions();
    if (numtrans <= limit) {
      final int numStates = mTransitionBuffer.getNumberOfStates();
      final int[][] trans = new int[numStates][];
      final TransitionIterator iter =
        new OnTheFlyTauClosureIterator(mTransitionBuffer);
      final TIntArrayList list = new TIntArrayList();
      for (int state = 0; state < numStates; state++) {
        iter.resetState(state);
        iter.advance();
        while (iter.advance()) {
          if (++numtrans > limit) {
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
  //# Inner Class OnTheFlyTauClosureIterator
  private static class OnTheFlyTauClosureIterator
  implements TransitionIterator
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
      mTransitionBuffer = buffer;
      mFrom = from;
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

    public void resetEvent(final int event)
    {
      if (event == EventEncoding.TAU) {
        reset();
      } else {
        throw new UnsupportedOperationException
          (ProxyTools.getShortClassName(this) +
           " only iterates with tau event!");
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

    public boolean advance()
    {
      if (mVisited.isEmpty()) {
        mCurrentState = mFrom;
        mOpen.add(mFrom);
        mVisited.add(mFrom);
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
    //# Data Members
    private final TransitionListBuffer mTransitionBuffer;

    private int mFrom;
    private final TIntArrayList mOpen;
    private final TIntHashSet mVisited;
    private final TransitionIterator mInner;
    private int mCurrentState;

  }


  //#########################################################################
  //# Inner Class StoredTauClosureIterator
  private class StoredTauClosureIterator implements TransitionIterator
  {

    //#######################################################################
    //# Constructor
    private StoredTauClosureIterator()
    {
      mCurrent = -1;
      mCurrentArray = null;
      mIndex = -2;
    }

    private StoredTauClosureIterator(final int target)
    {
      resetState(target);
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.analysis.op.TransitionIterator
    public void reset()
    {
      mCurrentArray = mStoredTransitions[mCurrent];
      mIndex = -2;
    }

    public void resetEvent(final int event)
    {
      if (event == EventEncoding.TAU) {
        reset();
      } else {
        throw new UnsupportedOperationException
          (ProxyTools.getShortClassName(this) +
           " only iterates with tau event!");
      }
    }

    public void resetState(final int from)
    {
      mCurrent = from;
      reset();
    }

    public void reset(final int from, final int event)
    {
      if (event == EventEncoding.TAU) {
        resetState(from);
      } else {
        throw new UnsupportedOperationException
          (ProxyTools.getShortClassName(this) +
           " only iterates with tau event!");
      }
    }

    public boolean advance()
    {
      return ++mIndex < mCurrentArray.length;
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
      if (mIndex == -1) {
        return mCurrent;  // Current state is never explicitly in array.
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
    //# Data Members
    private int mCurrent;
    private int[] mCurrentArray;
    private int mIndex;

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
      mInnerIterator = mTransitionBuffer.createReadOnlyIterator();
      mTauIterator2 = createIterator();
      mVisited = new TIntHashSet();
      mEvent = -1;
    }

    private FullEventClosureTransitionIterator(final int event)
    {
      mTauIterator1 = createIterator();
      mInnerIterator = mTransitionBuffer.createReadOnlyIterator(event);
      mTauIterator2 = createIterator();
      mVisited = new TIntHashSet();
      mEvent = event;
    }

    private FullEventClosureTransitionIterator(final int from, final int event)
    {
      mTauIterator1 = createIterator();
      mInnerIterator = mTransitionBuffer.createReadOnlyIterator(event);
      mTauIterator2 = createIterator();
      mVisited = new TIntHashSet();
      reset(from, event);
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.analysis.op.TransitionIterator
    public void reset()
    {
      mTauIterator1.resetState(mCurrent);
      mInnerIterator.reset(mCurrent, mEvent);
      mVisited.clear();
      mStart = true;
    }

    public void resetEvent(final int event)
    {
      mEvent = event;
      reset();
    }

    public void resetState(final int from)
    {
      mCurrent = from;
      reset();
    }

    public void reset(final int from, final int event)
    {
      mEvent = event;
      resetState(from);
    }

    public boolean advance()
    {
      while (seek()) {
        final int state = mTauIterator2.getCurrentToState();
        if (mVisited.add(state)) {
          return true;
        }
      }
      mStart = true;
      return false;
    }

    public int getCurrentEvent()
    {
      return mEvent;
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
        if (mInnerIterator.advance()) {
          final int pred = mInnerIterator.getCurrentSourceState();
          mTauIterator2.resetState(pred);
          return mTauIterator2.advance(); // always true
        } else {
          return false;
        }
      } else if (mTauIterator2.advance()) {
        return true;
      } else if (mInnerIterator.advance()) {
        final int pred = mInnerIterator.getCurrentSourceState();
        mTauIterator2.resetState(pred);
        return mTauIterator2.advance(); // always true
      } else {
        while (mTauIterator1.advance()) {
          int pred = mTauIterator1.getCurrentSourceState();
          mInnerIterator.resetState(pred);
          if (mInnerIterator.advance()) {
            pred = mInnerIterator.getCurrentSourceState();
            mTauIterator2.resetState(pred);
            return mTauIterator2.advance(); // always true
          }
        }
        return false;
      }
    }

    // #########################################################################
    // # Data Members
    private int mCurrent;
    private int mEvent;
    private boolean mStart;

    private final TransitionIterator mTauIterator1;
    private final TransitionIterator mInnerIterator;
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


  //#########################################################################
  //# Class Constants
  private static final int[] EMPTY_ARRAY = new int[0];

}
