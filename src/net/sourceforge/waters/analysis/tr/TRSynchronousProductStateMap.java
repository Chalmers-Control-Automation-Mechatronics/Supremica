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

package net.sourceforge.waters.analysis.tr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sourceforge.waters.analysis.monolithic.StateTupleEncoding;
import net.sourceforge.waters.model.analysis.des.SynchronousProductStateMap;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.StateProxy;

import gnu.trove.map.hash.TObjectIntHashMap;


/**
 * @author Robi Malik
 */
public class TRSynchronousProductStateMap
  implements SynchronousProductStateMap
{

  //#########################################################################
  //# Constructor
  public TRSynchronousProductStateMap(final Collection<AutomatonProxy> inputAutomata,
                                      final StateTupleEncoding stateTupleEncoding,
                                      final IntArrayBuffer stateSpace)
  {
    this(new ArrayList<AutomatonProxy>(inputAutomata),
         stateTupleEncoding, stateSpace);
  }

  public TRSynchronousProductStateMap(final List<AutomatonProxy> inputAutomata,
                                      final StateTupleEncoding stateTupleEncoding,
                                      final IntArrayBuffer stateSpace)
  {
    mInputAutomata = inputAutomata;
    mStateTupleEncoding = stateTupleEncoding;
    mStateSpace = stateSpace;
    mEncodedTuple = new int[stateTupleEncoding.getNumberOfWords()];
  }


  //#########################################################################
  //# Interface
  //# net.sourceforge.waters.analysis.monolithic.SynchronousProductStateMap
  @Override
  public Collection<AutomatonProxy> getInputAutomata()
  {
    return mInputAutomata;
  }

  @Override
  public StateProxy getOriginalState(final StateProxy syncState,
                                     final AutomatonProxy aut)
  {
    final TRAutomatonProxy.TRState trState =
      (TRAutomatonProxy.TRState) syncState;
    final int syncStateIndex = trState.getStateIndex();
    final int autIndex = getInputAutomatonIndex(aut);
    final int autStateIndex = getOriginalState(syncStateIndex, autIndex);
    return getInputAutomatonState(autIndex, autStateIndex);
  }


  //#########################################################################
  //# Specific Access
  public int getOriginalState(final int syncState, final int autIndex)
  {
    mStateSpace.getContents(syncState, mEncodedTuple);
    return mStateTupleEncoding.get(mEncodedTuple, autIndex);
  }

  public void getOriginalState(final int syncState, final int[] decodedTuple)
  {
    mStateSpace.getContents(syncState, mEncodedTuple);
    mStateTupleEncoding.decode(mEncodedTuple, decodedTuple);
  }

  public int getComposedState(final int[] decodedTuple)
  {
    mStateTupleEncoding.encode(decodedTuple, mEncodedTuple);
    return mStateSpace.getIndex(mEncodedTuple);
  }

  //#########################################################################
  //# Auxiliary Methods
  private int getInputAutomatonIndex(final AutomatonProxy aut)
  {
    if (mAutomataMap == null) {
      mAutomataMap =
        new TObjectIntHashMap<AutomatonProxy>(mInputAutomata.size());
      int a = 0;
      for (final AutomatonProxy in : mInputAutomata) {
        mAutomataMap.put(in, a++);
      }
    }
    return mAutomataMap.get(aut);
  }

  private StateProxy getInputAutomatonState(final int autIndex,
                                            final int stateIndex)
  {
    final AutomatonProxy aut = mInputAutomata.get(autIndex);
    if (aut instanceof TRAutomatonProxy) {
      final TRAutomatonProxy tr = (TRAutomatonProxy) aut;
      return tr.getState(stateIndex);
    }
    if (mStateArrays == null) {
      mStateArrays = new StateProxy[mInputAutomata.size()][];
    }
    if (mStateArrays[autIndex] == null) {
      int s = 0;
      for (final StateProxy state : aut.getStates()) {
        mStateArrays[autIndex][s++] = state;
      }
    }
    return mStateArrays[autIndex][stateIndex];
  }


  //#########################################################################
  //# Data Members
  private final List<AutomatonProxy> mInputAutomata;
  private final StateTupleEncoding mStateTupleEncoding;
  private final IntArrayBuffer mStateSpace;

  private final int[] mEncodedTuple;
  private TObjectIntHashMap<AutomatonProxy> mAutomataMap;
  private StateProxy[][] mStateArrays;

}
