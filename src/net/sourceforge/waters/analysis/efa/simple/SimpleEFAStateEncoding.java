//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT:
//# PACKAGE: net.sourceforge.waters.analysis.efa
//# CLASS:   SimpleEFAStateEncoding
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.simple;

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
  }

  public SimpleEFAStateEncoding()
  {
    this(DEFAULT_SIZE);
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

  public int getNodeId(final SimpleNodeProxy node)
  {
    for (final SimpleEFAState state : mStateMap.keySet()) {
      if (state.equals(node)) {
        return mStateMap.get(state);
      }
    }
    return -1;
  }

  public SimpleEFAState getSimpleState(final int stateId)
  {
    return mStateList.get(stateId);
  }

  public SimpleNodeProxy getSimpleNode(final int stateId)
  {
    final SimpleEFAState state = mStateList.get(stateId);
    return state.getSimpleNode();
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
      return labelId;
    }
  }

  public int createSimpleNodeId(final SimpleNodeProxy node)
  {
    final int id = getNodeId(node);
    if (id >= 0) {
      return id;
    } else {
      final int labelId = mStateMap.size();
      final SimpleEFAState state = new SimpleEFAState(node);
      mStateMap.put(state, labelId);
      mStateList.add(state);
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

  @Override
  public Iterator<SimpleEFAState> iterator()
  {
    return mStateList.iterator();
  }

  private final TObjectIntHashMap<SimpleEFAState> mStateMap;
  private final ArrayList<SimpleEFAState> mStateList;
  private static final int DEFAULT_SIZE = 16;
}
