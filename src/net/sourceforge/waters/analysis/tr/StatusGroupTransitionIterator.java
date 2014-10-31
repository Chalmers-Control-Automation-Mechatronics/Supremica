//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.tr
//# CLASS:   CachingTransitionIterator
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.tr;

import net.sourceforge.waters.model.base.ProxyTools;


/**
 * A {@link TransitionIterator} that can iterate over groups of events
 * determined by their status.
 *
 * @author Robi Malik
 */

public class StatusGroupTransitionIterator implements TransitionIterator
{

  //#########################################################################
  //# Constructors
  public StatusGroupTransitionIterator(final TransitionIterator inner,
                                       final OrderingInfo info,
                                       final int... flags)
  {
    mInnerIterator = inner;
    mOrderingInfo = info;
    mBoundaries = info.getBoundaries(flags);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.tr.TransitionIterator
  @Override
  public void reset()
  {
    mIndex = 0;
  }

  @Override
  public void resetEvent(final int event)
  {
    throw new UnsupportedOperationException
      (ProxyTools.getShortClassName(this) +
       "does not iterate over individual events!");
  }

  @Override
  public void resetEvents(final int first, final int last)
  {
    throw new UnsupportedOperationException
      (ProxyTools.getShortClassName(this) +
       "does not iterate over a set range of events!");
  }

  @Override
  public void resetEventsByStatus(final int... flags)
  {
    mBoundaries = mOrderingInfo.getBoundaries(flags);
    reset();
  }

  @Override
  public void resetState(final int from)
  {
    mInnerIterator.resetState(from);
    reset();
  }

  @Override
  public void reset(final int from, final int event)
  {
    throw new UnsupportedOperationException
      (ProxyTools.getShortClassName(this) +
       "does not iterate over individual events!");
  }

  @Override
  public void resume(final int from)
  {
    mInnerIterator.resetState(from);
  }

  @Override
  public boolean advance()
  {
    while (mIndex == 0 || !mInnerIterator.advance()) {
      if (mIndex < mBoundaries.length) {
        final int first = mBoundaries[mIndex++];
        final int last = mBoundaries[mIndex++];
        mInnerIterator.resetEvents(first, last);
      } else {
        return false;
      }
    }
    return true;
  }

  @Override
  public int getCurrentEvent()
  {
    return mInnerIterator.getCurrentEvent();
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
  public void setCurrentToState(final int state)
  {
    mInnerIterator.setCurrentToState(state);
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
  public void remove()
  {
    mInnerIterator.remove();
  }


  //#########################################################################
  //# Data Members
  private final TransitionIterator mInnerIterator;
  private final OrderingInfo mOrderingInfo;
  private int mIndex;
  private int[] mBoundaries;

}

