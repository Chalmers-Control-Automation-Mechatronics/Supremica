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

package net.sourceforge.waters.analysis.abstraction;

import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.StateEncoding;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.SafetyCounterExampleProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;


/**
 * A tool to determine whether a nondeterministic automaton accepts
 * a given trace.
 *
 * The TraceFinder can determine whether an event sequence is accepted,
 * and provide an appropriate sequence of states through the automaton.
 * It remembers an automaton for repeated processing of different traces,
 * and uses a {@link ListBufferTransitionRelation} for efficient searching.
 *
 * @author Robi Malik
 */

public class TraceFinder
{

  //#########################################################################
  //# Constructors
  public TraceFinder(final AutomatonProxy aut, final KindTranslator translator)
  {
    try {
      mAutomaton = aut;
      final Collection<EventProxy> empty = Collections.emptyList();
      mEventEncoding = new EventEncoding(aut, translator, empty,
                                         EventEncoding.FILTER_PROPOSITIONS);
      final StateEncoding enc = new StateEncoding(aut);
      mTransitionRelation = new ListBufferTransitionRelation
        (aut, mEventEncoding.clone(), enc,
         ListBufferTransitionRelation.CONFIG_SUCCESSORS);
      final ComponentKind kind = translator.getComponentKind(aut);
      mTransitionRelation.setKind(kind);
      mInitialStates = new ArrayList<SearchRecord>();
      final int numStates = mTransitionRelation.getNumberOfStates();
      for (int state = 0; state < numStates; state++) {
        if (mTransitionRelation.isInitial(state)) {
          final SearchRecord record = new SearchRecord(state);
          mInitialStates.add(record);
        }
      }
      final boolean det =
        mInitialStates.size() <= 1 && mTransitionRelation.isDeterministic();
      mStateEncoding = det ? null : enc;
      mNumberOfAcceptedSteps = -1;
      mPath = null;
    } catch (final OverflowException exception) {
      // Should not get overflow with 0 propositions ...
      throw new WatersRuntimeException(exception);
    }
  }


  //#########################################################################
  //# Simple Access
  public boolean isDeterministic()
  {
    return mStateEncoding == null;
  }


  //#########################################################################
  //# Invocation
  /**
   * Checks whether the automaton completely accepts the given trace.
   */
  public boolean accepts(final TraceProxy trace)
  {
    final int len = trace.getEvents().size();
    return computeNumberOfAcceptedSteps(trace) == len;
  }

  /**
   * Checks whether the automaton accepts the given trace.
   * @return The number of events in the trace accepted by the automaton.
   *         A return value of trace.{@link TraceProxy#getEvents()
   *         getEvents()}.size() indicates that the complete trace is
   *         accepted. A return value of -1 indicates that the automaton
   *         has no initial state, and 0 indicates that the first event of
   *         the trace is not eligible in any initial state.
   */
  public int computeNumberOfAcceptedSteps(final TraceProxy trace)
  {
    if (mInitialStates.isEmpty()) {
      // No initial state ...
      return -1;
    } else if (mStateEncoding == null) {
      // Deterministic automaton ...
      final TransitionIterator iter =
        mTransitionRelation.createSuccessorsReadOnlyIterator();
      final SearchRecord record = mInitialStates.get(0);
      int state = record.getState();
      int depth = 0;
      for (final EventProxy event : trace.getEvents()) {
        final int eventID = mEventEncoding.getEventCode(event);
        if (eventID >= 0) {
          iter.reset(state, eventID);
          if (!iter.advance()) {
            break;
          }
          state = iter.getCurrentTargetState();
        }
        depth++;
      }
      mNumberOfAcceptedSteps = depth;
      return depth;
    } else {
      // Nondeterministic automaton ...
      final List<EventProxy> events = trace.getEvents();
      List<SearchRecord> currentLevel =
        new ArrayList<SearchRecord>(mInitialStates);
      List<SearchRecord> nextLevel = new ArrayList<SearchRecord>();
      final TIntHashSet visited = new TIntHashSet();
      final TransitionIterator iter =
        mTransitionRelation.createSuccessorsReadOnlyIterator();
      int depth = 0;
      for (final EventProxy event : events) {
        final int eventID = mEventEncoding.getEventCode(event);
        if (eventID >= 0) {
          for (final SearchRecord current : currentLevel) {
            final int state = current.getState();
            iter.reset(state, eventID);
            while (iter.advance()) {
              final int target = iter.getCurrentTargetState();
              if (visited.add(target)) {
                final SearchRecord next = new SearchRecord(target, current);
                nextLevel.add(next);
              }
            }
          }
          if (nextLevel.isEmpty()) {
            break;
          }
          final List<SearchRecord> tmp = currentLevel;
          currentLevel = nextLevel;
          nextLevel = tmp;
          nextLevel.clear();
          visited.clear();
        }
        depth++;
      }
      mPath = new StateProxy[depth + 1];
      int index = depth;
      SearchRecord current = currentLevel.get(0);
      int nextStateID = current.getState();
      current = current.getPredecessor();
      while (current != null) {
        final int currentStateID = current.getState();
        final EventProxy event = events.get(index - 1);
        final int eventID = mEventEncoding.getEventCode(event);
        if (eventID >= 0) {
          iter.reset(currentStateID, eventID);
          if (iter.advance() && iter.advance()) {
            mPath[index] = mStateEncoding.getState(nextStateID);
          }
          nextStateID = currentStateID;
          current = current.getPredecessor();
        }
        index--;
      }
      if (mInitialStates.size() > 1) {
        mPath[0] = mStateEncoding.getState(nextStateID);
      }
      mNumberOfAcceptedSteps = depth;
      return depth;
    }
  }

  /**
   * Determines whether the automaton rejects the given safety counterexample.
   * This method determines whether the automaton can reach a state where
   * the next event from the trace is not enabled. The trace must include
   * information about the automaton, which is used to track the state in
   * case of nondeterminism.
   * @param  counter  The counterexample to be checked.
   * @return <CODE>true</CODE> if the remainder of the counterexample is
   *         accepted starting from the last recorded state for the automaton
   *         in the trace;
   *         <CODE>false</CODE> otherwise.
   */
  public boolean isRejectingSpec(final SafetyCounterExampleProxy counter)
  {
    assert counter.getAutomata().contains(mAutomaton);
    assert mTransitionRelation.getKind() == ComponentKind.SPEC;

    final TraceProxy trace = counter.getTrace();
    final List<TraceStepProxy> steps = trace.getTraceSteps();
    final int numSteps = steps.size();
    final int numEvents = numSteps - 1;
    if (isDeterministic()) {
      return computeNumberOfAcceptedSteps(trace) < numEvents;
    }

    final ListIterator<TraceStepProxy> listIter = steps.listIterator(numSteps);
    StateProxy state = null;
    while (state == null && listIter.hasPrevious()) {
      final TraceStepProxy step = listIter.previous();
      state = step.getStateMap().get(mAutomaton);
    }
    if (state == null) {
      return computeNumberOfAcceptedSteps(trace) < numEvents;
    }

    listIter.next();
    final TransitionIterator transIter =
      mTransitionRelation.createSuccessorsReadOnlyIterator();
    int s = mStateEncoding.getStateCode(state);
    while (listIter.hasNext()) {
      final TraceStepProxy step = listIter.next();
      final EventProxy event = step.getEvent();
      final int e = mEventEncoding.getEventCode(event);
      if (e >= 0) {
        transIter.reset(s, e);
        if (!transIter.advance()) {
          return true;
        }
        s = transIter.getCurrentTargetState();
      }
    }
    return false;
  }


  /**
   * Gets the number of steps computed from the last call to
   * {@link #computeNumberOfAcceptedSteps(TraceProxy)
   * computeNumberOfAcceptedSteps()}.
   */
  public int getNumberOfAcceptedSteps()
  {
    return mNumberOfAcceptedSteps;
  }

  /**
   * Gets a trace state on the path found by a previous call to
   * {@link #computeNumberOfAcceptedSteps(TraceProxy) accepts()}
   * @param  depth   The index position of the state to be looked up,
   *                 where 0 indicates the initial state of the trace.
   * @return The state on the path, or <CODE>null</CODE> to indicate a
   *         the this state can be recomputed deterministically from its
   *         predecessor.
   */
  public StateProxy getState(final int depth)
  {
    if (mPath != null) {
      return mPath[depth];
    } else {
      return null;
    }
  }


  //#########################################################################
  //# Inner Class SearchRecord
  private static class SearchRecord {

    //#######################################################################
    //# Constructor
    private SearchRecord(final int state)
    {
      this(state, null);
    }

    private SearchRecord(final int state, final SearchRecord pred)
    {
      mState = state;
      mPredecessor = pred;
    }

    //#######################################################################
    //# Simple Access
    private int getState()
    {
      return mState;
    }

    private SearchRecord getPredecessor()
    {
      return mPredecessor;
    }

    //#######################################################################
    //# Data Members
    private final int mState;
    private final SearchRecord mPredecessor;

  }


  //#########################################################################
  //# Data Members
  private AutomatonProxy mAutomaton;
  private final EventEncoding mEventEncoding;
  private final StateEncoding mStateEncoding;
  private final ListBufferTransitionRelation mTransitionRelation;
  private final List<SearchRecord> mInitialStates;

  private int mNumberOfAcceptedSteps;
  private StateProxy[] mPath;

}
