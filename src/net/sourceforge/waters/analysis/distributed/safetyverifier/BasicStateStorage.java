//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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

package net.sourceforge.waters.analysis.distributed.safetyverifier;

import gnu.trove.map.hash.THashMap;
import java.util.ArrayList;
import java.util.List;

public class BasicStateStorage implements StateStorage
{
  public BasicStateStorage()
  {
    mStateList = new ArrayList<StateTuple>();
    mObservedSet = new THashMap<StateTuple,StateTuple>();
  }

  public synchronized void addState(StateTuple state)
  {
    if (!mObservedSet.containsKey(state))
      {
	mObservedSet.put(state, state);
	mStateList.add(state);
      }
  }

  public synchronized StateTuple getNextState()
  {
    return mStateList.get(mCurrentStateIndex++);
  }

  public synchronized int getUnprocessedStateCount()
  {
    return mStateList.size() - mCurrentStateIndex;
  }

  public synchronized int getStateDepth(StateTuple state)
  {
    StateTuple t = mObservedSet.get(state);
    if (t == null)
      return -1;
    else
      return t.getDepthHint();
  }

  public synchronized int getStateCount()
  {
    return mObservedSet.size();
  }

  public synchronized int getProcessedStateCount()
  {
    return mCurrentStateIndex;
  }

  public boolean containsState(StateTuple state)
  {
    return mObservedSet.containsKey(state);
  }

  private final THashMap<StateTuple,StateTuple> mObservedSet;
  private final List<StateTuple> mStateList;
  private volatile int mCurrentStateIndex = 0;
}
