//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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

package net.sourceforge.waters.analysis.efa.simple;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.ModuleProxyCloner;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.subject.base.AttributeMapSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.subject.module.SimpleNodeSubject;


/**
 * @author Mohammad Reza Shoaei
 */
public class SimpleEFAStateEncoding implements Iterable<SimpleNodeProxy>
{

  public SimpleEFAStateEncoding(final int size)
  {
    mStateMap = new TObjectIntHashMap<>(size, 0.5f, -1);
    mStateList = new ArrayList<>(size);
    mMarkedStates = new TIntArrayList(25, -1);
    mForbiddenStates = new TIntArrayList(25, -1);
    mCloner = ModuleSubjectFactory.getCloningInstance();
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

  public int getStateId(final SimpleNodeProxy state)
  {
    return mStateMap.get(state.getName());
  }

  public SimpleNodeProxy getSimpleState(final int stateId)
  {
    return mStateList.get(stateId);
  }

  public List<SimpleNodeProxy> getSimpleStates()
  {
    return mStateList;
  }

  public int createSimpleStateId(final SimpleNodeProxy state)
  {
    final int id = mStateMap.get(state.getName());
    if (id >= 0) {
      return id;
    } else {
      final int labelId = mStateMap.size();
      final SimpleNodeSubject snode = getSimpleNodeSubject(state);
      mStateMap.put(snode.getName(), labelId);
      mStateList.add(snode);
      if (state.isInitial()) {
        mInitialStateId = labelId;
      }
      try {
        if (state.getPropositions().toString().contains(EventDeclProxy.DEFAULT_MARKING_NAME)) {
          mMarkedStates.add(labelId);
        }
        if (state.getPropositions().toString().contains(EventDeclProxy.DEFAULT_FORBIDDEN_NAME)) {
          mForbiddenStates.add(labelId);
        }
      } catch (final Exception e) {
      }
      return labelId;
    }
  }

  public int put(final SimpleNodeProxy state, final int stateId)
  {
    final SimpleNodeSubject snode = getSimpleNodeSubject(state);
    final int put = mStateMap.put(snode.getName(), stateId);
    mStateList.add(snode);
    return put;
  }

  public void merge(final SimpleEFAStateEncoding enc)
  {
    for (final SimpleNodeProxy state : enc.getSimpleStates()) {
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

  public boolean isMarked(final int stateId)
  {
    return mMarkedStates.contains(stateId);
  }

  public boolean isForbidden(final int stateId)
  {
    return mForbiddenStates.contains(stateId);
  }

  public void addToAttribute(final int stateId, final String key, final String value)
  {
    getAttributes(stateId).put(value, value);
  }

  public AttributeMapSubject getAttributes(final int stateId)
  {
    return ((SimpleNodeSubject) getSimpleState(stateId)).getAttributesModifiable();
  }

  public String getAttribute(final int stateId, final String key)
  {
    final Map<String, String> att = getAttributes(stateId);
    if (att != null) {
      return att.get(key);
    }
    return null;
  }

  public void mergeToAttribute(final int stateId, final String key, final String value,
                               final String separator)
  {
    SimpleEFAHelper.mergeToAttribute(getAttributes(stateId), key, value, separator);
  }

  public void mergeToAttribute(final int stateId, final String key, final String value)
  {
    mergeToAttribute(stateId, key, value, SimpleEFAHelper.DEFAULT_VALUE_SEPARATOR);
  }

  public boolean isInitial(final int stateId)
  {
    return getInitialStateId() == stateId;
  }

  @Override
  public Iterator<SimpleNodeProxy> iterator()
  {
    return mStateList.iterator();
  }

  public SimpleNodeProxy getInitialState()
  {
    return getSimpleState(mInitialStateId);
  }

  private SimpleNodeSubject getSimpleNodeSubject(final SimpleNodeProxy node)
  {
    SimpleNodeSubject snode;
    if (node instanceof SimpleNodeSubject) {
      snode = (SimpleNodeSubject) node;
    } else {
      snode = (SimpleNodeSubject) mCloner.getClone(node);
    }
    return snode;
  }

  private final TObjectIntHashMap<String> mStateMap;
  private final ArrayList<SimpleNodeProxy> mStateList;
  private static final int DEFAULT_SIZE = 16;
  private int mInitialStateId;
  private final TIntArrayList mMarkedStates;
  private final TIntArrayList mForbiddenStates;
  private final ModuleProxyCloner mCloner;
}
