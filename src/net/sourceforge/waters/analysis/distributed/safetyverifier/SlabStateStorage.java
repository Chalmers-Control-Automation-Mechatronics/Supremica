//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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

import gnu.trove.list.array.TIntArrayList;

public class SlabStateStorage implements StateStorage
{
  /**
   * Creates a state storage. The memory allocator used needs
   * to know how long each state is.
   * @param stateLength Number of 32 bit words each state tuple
   * encodes into.
   */
  public SlabStateStorage(int stateLength)
  {
    mStateLength = stateLength;
    mStateSet = new StateTupleSet(mStateLength, INITIAL_CAPACITY);
    mStateList = new TIntArrayList(INITIAL_CAPACITY);
  }

  public synchronized void addState(StateTuple state)
  {
    int p = mStateSet.add(state);
    if (p >= 0)
      {
	//The state was newly added to the set. Also add
	//it to the state queue.
	mStateList.add(p);
      }
  }

  public synchronized StateTuple getNextState()
  {
    int p = mStateList.get(mCurrentStateIndex++);
    return mStateSet.get(p);
  }

  public synchronized int getUnprocessedStateCount()
  {
    return mStateList.size() - mCurrentStateIndex;
  }

  public synchronized int getStateDepth(StateTuple state)
  {
    int p = mStateSet.lookup(state);
    if (p < 0)
      return -1;
    else
      {
	return mStateSet.getDepth(p);
      }
  }

  public synchronized int getStateCount()
  {
    return mStateSet.size();
  }

  public synchronized int getProcessedStateCount()
  {
    return mCurrentStateIndex;
  }

  public boolean containsState(StateTuple state)
  {
    return mStateSet.contains(state);
  }

  private final int mStateLength;
  private final StateTupleSet mStateSet;
  private final TIntArrayList mStateList;
  private volatile int mCurrentStateIndex = 0;

  private static final int INITIAL_CAPACITY = 100000;
}
