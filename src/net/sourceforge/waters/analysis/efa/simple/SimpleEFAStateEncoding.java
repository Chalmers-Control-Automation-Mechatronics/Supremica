//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT:
//# PACKAGE: net.sourceforge.waters.analysis.efa
//# CLASS:   SimpleEFAStateEncoding
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.simple;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.waters.model.module.SimpleNodeProxy;

/**
 *
 * @author Mohammad Reza Shoaei
 */
public class SimpleEFAStateEncoding implements Iterable<SimpleEFAState>
{

  public SimpleEFAStateEncoding(final int size)
  {
    mStateMap = new TObjectIntHashMap<>(size, 0.5f, -1);
    mStateList = new ArrayList<>(size);
    mMarkedStates = new TIntArrayList();
    mForbiddenStates = new TIntArrayList();
    mInitialStateId = -1;
  }

  public SimpleEFAStateEncoding()
  {
    this(SimpleEFAStateEncoding.DEFAULT_SIZE);
  }

  public int size()
  {
    return mStateMap.size();
  }

  public boolean isEmpty()
  {
    return mStateMap.isEmpty();
  }

  public int getStateId(final SimpleEFAState state)
  {
    return mStateMap.get(state);
  }

  public int getStateId(final SimpleNodeProxy node)
  {
    for (final SimpleEFAState state : getSimpleStates()) {
      if (state.equals(node)) {
        return getStateId(state);
      }
    }
    return -1;
  }

  public SimpleEFAState getSimpleState(final int stateId)
  {
    return mStateList.get(stateId);
  }

  public List<SimpleEFAState> getSimpleStates()
  {
    return mStateList;
  }

  public int createSimpleStateId(final SimpleEFAState state)
  {
    final int id = mStateMap.get(state);
    if (id >= 0) {
      return id;
    } else {
      final int labelId = mStateMap.size();
      mStateMap.put(state, labelId);
      mStateList.add(state);
      if (state.isInitial()) {
        mInitialStateId = labelId;
      }
      if (state.isMarked()) {
        mMarkedStates.add(labelId);
      }
      if (state.isForbidden()) {
        mForbiddenStates.add(labelId);
      }
      return labelId;
    }
  }

  public int put(final SimpleEFAState state, final int stateId)
  {
    final int put = mStateMap.put(state, stateId);
    mStateList.add(state);
    return put;
  }

  public void merge(final SimpleEFAStateEncoding enc)
  {
    for (final SimpleEFAState state : enc.getSimpleStates()) {
      createSimpleStateId(state);
    }
  }

  public int getInitialStateId()
  {
    return mInitialStateId;
  }

  public TIntArrayList getMarkedStateIds()
  {
    return mMarkedStates;
  }

  public TIntArrayList getForbbidenStateIds()
  {
    return mForbiddenStates;
  }

  public boolean hasMarkedState()
  {
    return !mMarkedStates.isEmpty();
  }

  public boolean hasForbbidenState()
  {
    return !mForbiddenStates.isEmpty();
  }

  @Override
  public Iterator<SimpleEFAState> iterator()
  {
    return mStateList.iterator();
  }

  public SimpleEFAState getInitialState()
  {
    return getSimpleState(mInitialStateId);
  }

  private final TObjectIntHashMap<SimpleEFAState> mStateMap;
  private final ArrayList<SimpleEFAState> mStateList;
  private static final int DEFAULT_SIZE = 16;
  private int mInitialStateId;
  private final TIntArrayList mMarkedStates;
  private final TIntArrayList mForbiddenStates;
}
