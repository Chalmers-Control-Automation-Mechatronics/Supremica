//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
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

import gnu.trove.set.hash.TIntHashSet;

import net.sourceforge.waters.model.base.WatersRuntimeException;
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
  public CachingTransitionIterator clone()
  {
    try {
      final CachingTransitionIterator cloned =
        (CachingTransitionIterator) super.clone();
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
  private TransitionIterator mInnerIterator;
  private TIntHashSet mVisited;
  private final int mEventShift;

}
