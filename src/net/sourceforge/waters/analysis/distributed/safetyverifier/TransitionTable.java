//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
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

import java.util.Arrays;

import net.sourceforge.waters.analysis.distributed.schemata.AutomatonSchema;
import net.sourceforge.waters.analysis.distributed.schemata.ProductDESSchema;
import net.sourceforge.waters.analysis.distributed.schemata.TransitionSchema;


/**
 * Represents transition information for an automaton. This uses a
 * technique similar to the one uesd by Jinjian Shi in the Waters
 * monolithic controllability checker.
 *
 * This is a two dimensional array [events][source state] -&gt; target
 * state
 *
 * This implementation can be used to check if an event is in the
 * alphabet for an automaton; if the event is not in the alphabet then
 * the second dimension of the array can be omitted (array[event] ==
 * null).
 *
 * This allows fast lookups to check if an event is allowed from a
 * given state.
 */
public class TransitionTable
{
  /**
   * Build a transition table for a given automaton from a model schematic.
   * @param des The model to use
   * @param autId The id of the automaton to use.
   */
  public TransitionTable(final ProductDESSchema des, final int autId)
  {
    final AutomatonSchema aut = des.getAutomaton(autId);
    mAutomatonIndex = autId;
    mModel = des;

    //Set some convenient member variables for the array bounds.
    mStates = aut.getStateCount();
    mEvents = des.getEventCount();

    //Only events in the alphabet will have a state dimension created
    //for them. All state transitions will be set to -1 initially, to
    //indicate a transition on that event from that state is not possible.
    mTransitions = new int[mEvents][];
    for (int i = 0; i < aut.getEventIdCount(); i++)
      {
	final int event = aut.getEventId(i);
	mTransitions[event] = new int[mStates];
	for (int j = 0; j < mStates; j++)
	  {
	    mTransitions[event][j] = -1;
	  }
      }

    for (int i = 0; i < aut.getTransitionCount(); i++)
      {
	final TransitionSchema t = aut.getTransition(i);
	mTransitions[t.getEventId()][t.getSource()] = t.getTarget();
      }
  }

  /**
   * Check if an event is in the alphabet for this automaton.
   */
  public boolean isInAlphabet(final int event)
  {
    return mTransitions[event] != null;
  }


  /**
   * Get the successor state, given the current state for the
   * automaton and an event.
   *
   * If the event is not in the alphabet for the automaton, then the
   * current state will be returned (implicit self-loop). If there is
   * no successor (the event is disabled from the current state), then
   * -1 will be returned.
   * @param state The current state index
   * @param event Event that occurred.
   * @return The target state if the event is enabled, or -1 if disabled.
   */
  public int getSuccessorState(final int state, final int event)
  {
    try
      {
	if (!isInAlphabet(event))
	  return state;
	else
	  return mTransitions[event][state];
      }
    catch (final IndexOutOfBoundsException e)
      {
	System.err.println(e);
	System.err.format("automaton %d state %d event %d\n", mAutomatonIndex, state, event);
	System.err.println(mModel.getAutomaton(mAutomatonIndex));
	System.err.println(this);
	throw e;
      }
  }

  /**
   * Gets the predecessors to a given state for a specified event. If
   * there are no predecessors, or the event is not in the alphabet
   * for this automaton, an empty array will be returned.
   * @param target state to search for predecessors
   * @param event event to return predecessors for
   * @return a possibly empty array of predecessor states
   */
  public int[] getPredecessorStates(final int target, final int event)
  {
    if (!isInAlphabet(event)) {
      return new int[0];
    } else {
      final TIntArrayList list = new TIntArrayList();
      for (int i = 0; i < mTransitions[event].length; i++) {
        if (mTransitions[event][i] == target)
          list.add(i);
      }
      return list.toArray();
    }
  }

  /**
   * Get the index of the automaton this transition relation
   * corresponds to.
   */
  public int getAutomatonIndex()
  {
    return mAutomatonIndex;
  }

  @Override
  public String toString()
  {
    final StringBuilder sb = new StringBuilder();

    for (int i = 0; i < mEvents; i++)
      {
	sb.append(i);
	sb.append(":");
	sb.append(Arrays.toString(mTransitions[i]));
	sb.append("\n");
      }

    return sb.toString();
  }

  private final ProductDESSchema mModel;
  private final int mAutomatonIndex;
  private final int mStates;
  private final int mEvents;
  private final int[][] mTransitions;
}
