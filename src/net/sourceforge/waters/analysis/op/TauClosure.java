//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.op
//# CLASS:   TauClosure
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.op;

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
   * {@link TransitionIterator#resetState(int)} must be used before it can be
   * used. After initialisation, the first state returned by the iterator is
   * the start state of iteration, and it is followed by all states reachable
   * by sequences of {@link EventEncoding#TAU} events in depth-first order.
   * The tau-closure iterator is a read-only iterator and does not
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
  public TransitionIterator createIterator(final int state)
  {
    if (mStoredTransitions == null) {
      return new OnTheFlyTauClosureIterator(mTransitionBuffer, state);
    } else {
      return new StoredTauClosureIterator(state);
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
