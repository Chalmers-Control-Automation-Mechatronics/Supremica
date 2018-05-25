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

package net.sourceforge.waters.analysis.distributed.schemata;

import java.util.Arrays;
import java.util.Formatter;
import java.io.Serializable;

public class AutomatonSchema implements Serializable
{
  AutomatonSchema(final String name,
		  final int[] eventIds,
		  final StateSchema[] states,
		  final int kind,
		  final TransitionSchema[] transitions,
		  final int id)
  {
    mName = name;
    mEventIds = eventIds;
    mStates = states;
    mKind = kind;
    mTransitions = transitions;
    mAutomatonId = id;
  }

  public String getName()
  {
    return mName;
  }

  public int getAutomatonId()
  {
    return mAutomatonId;
  }

  public int getEventId(final int index)
  {
    return mEventIds[index];
  }

  public int getEventIdCount()
  {
    return mEventIds.length;
  }

  public StateSchema getState(final int index)
  {
    return mStates[index];
  }

  public int getStateCount()
  {
    return mStates.length;
  }

  public int getKind()
  {
    return mKind;
  }

  public TransitionSchema getTransition(final int index)
  {
    return mTransitions[index];
  }

  public int getTransitionCount()
  {
    return mTransitions.length;
  }

  @Override
  public String toString()
  {
    final Formatter fmt = new Formatter();
    try {
      fmt.format("Name: %s\n", mName);
      fmt.format("Id: %d\n", mAutomatonId);
      fmt.format("Events: %s\n", Arrays.toString(mEventIds));
      fmt.format("Transitions: %s\n", Arrays.deepToString(mTransitions));
      fmt.format("States: %s\n", Arrays.deepToString(mStates));
      fmt.format("Kind: %d\n", mKind);
      return fmt.toString();
    } finally {
      fmt.close();
    }
  }

  /**
   * A simple predicate to check if an event is in the alphabet for
   * this automaton.
   * @param event to check for
   * @return true if event is in the alphabet for this automaton.
   */
  public boolean hasEvent(final int event)
  {
    for (final int eventid : mEventIds)
      {
	if (event == eventid)
	  return true;
      }

    return false;
  }

  private final String mName;
  private final int[] mEventIds;
  private final StateSchema[] mStates;
  private final int mKind;
  private final TransitionSchema[] mTransitions;
  private final int mAutomatonId;

  private static final long serialVersionUID = 1L;

  public static final int PLANT = 0;
  public static final int SPECIFICATION = 1;
  public static final int PROPERTY = 2;
  public static final int SUPERVISOR = 3;
}
