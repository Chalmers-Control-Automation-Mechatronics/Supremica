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

import net.sourceforge.waters.model.des.AutomatonTools;


/**
 * A transition iterator that caches its results to prevent duplicates.
 * This class can be used as a wrapper around any {@link TransitionIterator}
 * to ensure that each result is returned only once. The iterator implements
 * the {@link #resume(int) resume()} method that enables users to retain the
 * cache while continuing to iterate from a different source state.
 *
 * @author Robi Malik
 */

public class CachingTransitionIterator implements TransitionIterator
{

  //#########################################################################
  //# Constructors
  public CachingTransitionIterator(final ListBufferTransitionRelation rel,
                                   final TransitionIterator inner)
  {
    this(inner, rel.getNumberOfStates());
  }

  public CachingTransitionIterator(final TransitionListBuffer buffer,
                                   final TransitionIterator inner)
  {
    this(inner, buffer.getNumberOfStates());
  }

  public CachingTransitionIterator(final TransitionIterator inner,
                                   final int numStates)
  {
    mInnerIterator = inner;
    mVisited = new TIntHashSet();
    mEventShift = AutomatonTools.log2(numStates);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.tr.TransitionIterator
  @Override
  public void reset()
  {
    mInnerIterator.reset();
    clear();
  }

  @Override
  public void resetEvent(final int event)
  {
    mInnerIterator.resetEvent(event);
    clear();
  }

  @Override
  public void resetEvents(final int first, final int last)
  {
    mInnerIterator.resetEvents(first, last);
    clear();
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
  }

  @Override
  public void resume(final int from)
  {
    mInnerIterator.resetState(from);
  }

  @Override
  public int getFirstEvent()
  {
    return mInnerIterator.getFirstEvent();
  }

  @Override
  public int getLastEvent()
  {
    return mInnerIterator.getLastEvent();
  }

  @Override
  public boolean advance()
  {
    while (mInnerIterator.advance()) {
      final int state = mInnerIterator.getCurrentToState();
      final int event = mInnerIterator.getCurrentEvent();
      final int key = (event << mEventShift) | state;
      if (mVisited.add(key)) {
        return true;
      }
    }
    return false;
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
  private final TransitionIterator mInnerIterator;
  private TIntHashSet mVisited;
  private final int mEventShift;

}

