//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.tr
//# CLASS:   CachingTransitionIterator
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.tr;

import gnu.trove.set.hash.TIntHashSet;

import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.base.WatersRuntimeException;


/**
 * A transition iterator that caches its results to prevent duplicates.
 * This class can be used as a wrapper around any {@link TransitionIterator}
 * to ensure that each result is returned only once. The iterator implements
 * the {@link #resume(int) resume()} method that enables users to retain the
 * cache while continuing to iterate from a different source state.
 *
 * This is an optimised version of {@link CachingTransitionIterator} for the
 * case that iteration is performed using only one event. It is an error to
 * reset this iterator to iterate over a range of multiple events.
 *
 * @author Robi Malik
 */

public class OneEventCachingTransitionIterator implements TransitionIterator
{

  //#########################################################################
  //# Constructor
  public OneEventCachingTransitionIterator(final TransitionIterator inner)
  {
    this(inner, -1);
  }

  public OneEventCachingTransitionIterator(final TransitionIterator inner,
                                           final int event)
  {
    mInnerIterator = inner;
    mVisited = new TIntHashSet();
    mEvent = event;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.tr.TransitionIterator
  @Override
  public OneEventCachingTransitionIterator clone()
  {
    try {
      final OneEventCachingTransitionIterator cloned =
        (OneEventCachingTransitionIterator) super.clone();
      cloned.mInnerIterator = mInnerIterator.clone();
      cloned.mVisited = new TIntHashSet(mVisited);
      return cloned;
    } catch (final CloneNotSupportedException exception) {
      throw new WatersRuntimeException(exception);
    }
  }

  @Override
  public void reset()
  {
    assert mEvent >= 0;
    mInnerIterator.reset();
    clear();
  }

  @Override
  public void resetEvent(final int event)
  {
    mInnerIterator.resetEvent(event);
    clear();
    mEvent = event;
  }

  @Override
  public void resetEvents(final int first, final int last)
  {
    if (first == last) {
      resetEvent(first);
    } else {
      throw new UnsupportedOperationException
        (ProxyTools.getShortClassName(this) +
         " does not support multiple events!");
    }
  }

  @Override
  public void resetState(final int from)
  {
    mInnerIterator.resetState(from);
    clear();
  }

  @Override
  public void reset(final int from, final int event)
  {
    mInnerIterator.reset(from, event);
    clear();
    mEvent = event;
  }

  @Override
  public void resume(final int from)
  {
    assert mEvent >= 0;
    mInnerIterator.resetState(from);
  }

  @Override
  public int getFirstEvent()
  {
    return mEvent;
  }

  @Override
  public int getLastEvent()
  {
    return mEvent;
  }

  @Override
  public boolean advance()
  {
    while (mInnerIterator.advance()) {
      final int state = mInnerIterator.getCurrentToState();
      if (mVisited.add(state)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public int getCurrentEvent()
  {
    return mEvent;
  }

  @Override
  public int getCurrentSourceState()
  {
    return mInnerIterator.getCurrentSourceState();
  }

  @Override
  public int getCurrentToState()
  {
    return mInnerIterator.getCurrentToState();
  }

  @Override
  public int getCurrentTargetState()
  {
    return mInnerIterator.getCurrentTargetState();
  }

  @Override
  public int getCurrentFromState()
  {
    return mInnerIterator.getCurrentFromState();
  }

  @Override
  public void setCurrentToState(final int state)
  {
    mInnerIterator.setCurrentToState(state);
  }

  @Override
  public void remove()
  {
    mInnerIterator.remove();
  }


  //#########################################################################
  //# Specific Access
  /**
   * Adds the given state to the set of states visited by this iterator,
   * preventing the same state from being produced by successive iterations.
   */
  public void addVisitedState(final int state)
  {
    mVisited.add(state);
  }


  //#########################################################################
  //# Auxiliary Methods
  private void clear()
  {
    final int size = mVisited.size();
    if (size > 100) {
      mVisited = new TIntHashSet();
    } else if (size > 0) {
      mVisited.clear();
    }
  }


  //#########################################################################
  //# Data Members
  private TransitionIterator mInnerIterator;
  private TIntHashSet mVisited;
  private int mEvent;

}

