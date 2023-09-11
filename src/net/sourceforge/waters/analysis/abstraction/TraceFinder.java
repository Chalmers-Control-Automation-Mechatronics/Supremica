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

package net.sourceforge.waters.analysis.abstraction;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.CounterExampleProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.SafetyCounterExampleProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;


/**
 * <P>A tool to determine whether a (possibly nondeterministic) automaton accepts
 * a trace.</P>
 *
 * <P>The trace finder can determine whether an event sequence is accepted,
 * and provide an appropriate sequence of states through the automaton.
 * It remembers an automaton for repeated processing of different traces,
 * constructing a {@link TRAutomatonProxy} if needed for efficient searching.</P>
 *
 * @author Robi Malik
 */

public class TraceFinder
{

  //#########################################################################
  //# Constructors
  public TraceFinder(final AutomatonProxy aut, final KindTranslator translator)
  {
    if (aut instanceof TRAutomatonProxy) {
      mTRAutomaton = (TRAutomatonProxy) aut;
      final ListBufferTransitionRelation rel = mTRAutomaton.getTransitionRelation();
      rel.reconfigure(ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    } else {
      try {
        final Collection<EventProxy> empty = Collections.emptyList();
        final EventEncoding eventEnc =
          new EventEncoding(aut, translator, empty,
                            EventEncoding.FILTER_PROPOSITIONS);
        mTRAutomaton =
          new TRAutomatonProxy(aut, eventEnc,
                               ListBufferTransitionRelation.CONFIG_SUCCESSORS);
      } catch (final OverflowException exception) {
        // Should not get overflow with 0 propositions ...
        throw new WatersRuntimeException(exception);
      }
    }
    mSpec = translator.getComponentKind(aut) != ComponentKind.PLANT;
    final ListBufferTransitionRelation rel = mTRAutomaton.getTransitionRelation();
    final int numStates = rel.getNumberOfStates();
    mStateMap = new TObjectIntHashMap<>(numStates, 0.5f, -1);
    mInitialStates = new ArrayList<SearchRecord>();
    for (int s = 0; s < numStates; s++) {
      final StateProxy state = mTRAutomaton.getOriginalState(s);
      if (state != null) {
        mStateMap.put(state, s);
      }
      if (rel.isInitial(s)) {
        final SearchRecord record = new SearchRecord(s);
        mInitialStates.add(record);
      }
    }
    mDeterministic = mInitialStates.size() <= 1 && rel.isDeterministic();
  }


  //#########################################################################
  //# Simple Access
  public boolean isDeterministic()
  {
    return mDeterministic;
  }

  public void setComponentKind(final ComponentKind kind)
  {
    final boolean spec = kind != ComponentKind.PLANT;
    if (spec != mSpec) {
      mSpec = spec;
      mLastInput = null;
      mLastResult = null;
    }
  }

  //#########################################################################
  //# Invocation
  /**
   * Checks whether the automaton accepts all traces of the given counterexample.
   */
  public Result examine(final CounterExampleProxy counter)
  {
    if (mLastInput != counter) {
      final List<TraceProxy> traces = counter.getTraces();
      final int numPaths = mDeterministic ? 0 : traces.size();
      final Result result = new Result(numPaths);
      int index = 0;
      for (final TraceProxy trace : counter.getTraces()) {
        final boolean spec = index == 0 && mSpec;
        final Result subResult = examine(trace, spec);
        result.merge(subResult, index);
        index++;
      }
      mLastInput = counter;
      mLastResult = result;
    }
    return mLastResult;
  }

  /**
   * Checks whether the automaton accepts the traces of the given safety
   * counterexample.
   */
  public Result examine(final SafetyCounterExampleProxy counter)
  {
    final TraceProxy trace = counter.getTrace();
    return examine(trace);
  }

  /**
   * Checks whether the automaton accepts the given trace.
   */
  public Result examine(final TraceProxy trace)
  {
    if (mLastInput != trace) {
      mLastInput = trace;
      mLastResult = examine(trace, mSpec);
    }
    return mLastResult;
  }

  private Result examine(final TraceProxy trace, final boolean spec)
  {
    if (mInitialStates.isEmpty()) {
      // No initial state ...
      return new Result(-1, false, false);
    } else if (mDeterministic) {
      final EventEncoding enc = mTRAutomaton.getEventEncoding();
      final ListBufferTransitionRelation rel =
        mTRAutomaton.getTransitionRelation();
      final TransitionIterator iter = rel.createSuccessorsReadOnlyIterator();
      final SearchRecord record = mInitialStates.get(0);
      final List<EventProxy> events = trace.getEvents();
      int state = record.getState();
      int depth = 0;
      for (final EventProxy event : events) {
        final int eventID = enc.getEventCode(event);
        if (eventID >= 0) {
          iter.reset(state, eventID);
          if (!iter.advance()) {
            break;
          }
          state = iter.getCurrentTargetState();
        }
        depth++;
      }
      final int numSteps = events.size();
      if (spec) {
        return new Result(depth, depth >= numSteps - 1,
                             depth == numSteps - 1);
      } else {
        return new Result(depth, depth == numSteps, false);
      }
    } else {
      // Nondeterministic automaton ...
      final EventEncoding eventEnc = mTRAutomaton.getEventEncoding();
      final ListBufferTransitionRelation rel =
        mTRAutomaton.getTransitionRelation();
      final List<EventProxy> events = trace.getEvents();
      List<SearchRecord> currentLevel =
        new ArrayList<SearchRecord>(mInitialStates);
      List<SearchRecord> nextLevel = new ArrayList<SearchRecord>();
      final TIntHashSet visited = new TIntHashSet();
      final TransitionIterator iter = rel.createSuccessorsReadOnlyIterator();
      final int numSteps = events.size();
      int depth = 0;
      outer:
      for (final EventProxy event : events) {
        final int eventID = eventEnc.getEventCode(event);
        if (eventID >= 0) {
          for (final SearchRecord current : currentLevel) {
            final int state = current.getState();
            iter.reset(state, eventID);
            boolean gotSuccessor = false;
            while (iter.advance()) {
              gotSuccessor = true;
              final int target = iter.getCurrentTargetState();
              if (visited.add(target)) {
                final SearchRecord next = new SearchRecord(target, current);
                nextLevel.add(next);
              }
            }
            if (!gotSuccessor && depth == numSteps - 1) {
              break outer;
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
      final StateProxy[] path = new StateProxy[depth + 1];
      int index = depth;
      SearchRecord current = currentLevel.get(0);
      int nextStateID = current.getState();
      current = current.getPredecessor();
      while (current != null) {
        final int currentStateID = current.getState();
        final EventProxy event = events.get(index - 1);
        final int eventID = eventEnc.getEventCode(event);
        if (eventID >= 0) {
          iter.reset(currentStateID, eventID);
          if (iter.advance() && iter.advance()) {
            path[index] = mTRAutomaton.getOriginalState(nextStateID);
          }
          nextStateID = currentStateID;
          current = current.getPredecessor();
        }
        index--;
      }
      if (mInitialStates.size() > 1) {
        path[0] = mTRAutomaton.getOriginalState(nextStateID);
      }
      if (spec) {
        return new Result(depth, depth >= numSteps - 1,
                             depth == numSteps - 1, path);
      } else {
        return new Result(depth, depth == numSteps, false, path);
      }
    }
  }


  //#########################################################################
  //# Inner Class TraceInfo
  public static class Result
  {
    //#######################################################################
    //# Constructor
    private Result(final int numPaths)
    {
      mTotalAcceptedSteps = 0;
      mFirstAcceptedSteps = Integer.MIN_VALUE;
      mAccepted = true;
      mRejectedBySpec = false;
      mPaths = numPaths > 0 ? new StateProxy[numPaths][] : null;
    }

    private Result(final int numSteps,
                   final boolean accepted,
                   final boolean rejectedBySpec)
    {
      mTotalAcceptedSteps = mFirstAcceptedSteps = numSteps;
      mAccepted = accepted;
      mRejectedBySpec = rejectedBySpec;
      mPaths = null;
    }

    private Result(final int numSteps,
                   final boolean accepted,
                   final boolean rejectedBySpec,
                   final StateProxy[] path)
    {
      mTotalAcceptedSteps = mFirstAcceptedSteps = numSteps;
      mAccepted = accepted;
      mRejectedBySpec = rejectedBySpec;
      mPaths = new StateProxy[1][];
      mPaths[0] = path;
    }

    //#######################################################################
    //# Simple Access
    /**
     * Gets the number of trace events accepted by the automaton.
     * A return value of -1 indicates that the automaton has no initial state,
     * and 0 indicates that the first event of the trace is not eligible in any
     * initial state.
     */
    public int getTotalAcceptedSteps()
    {
      return mTotalAcceptedSteps;
    }

    public int mFirstAcceptedSteps()
    {
      return mFirstAcceptedSteps;
    }

    public boolean isAccepted()
    {
      return mAccepted;
    }

    public boolean isRejectedBySpec()
    {
      return mRejectedBySpec;
    }

    public StateProxy getStateAt(final int pathIndex, final int stateIndex)
    {
      if (mPaths == null) {
        return null;
      } else if (stateIndex < mPaths[pathIndex].length) {
        return mPaths[pathIndex][stateIndex];
      } else {
        return null;
      }
    }

    //#######################################################################
    //# Auxiliary Methods
    private void merge(final Result subResult, final int traceIndex)
    {
      if (mTotalAcceptedSteps >= 0) {
        mTotalAcceptedSteps += subResult.mTotalAcceptedSteps;
      }
      if (mFirstAcceptedSteps < -1) {
        mFirstAcceptedSteps = subResult.mFirstAcceptedSteps;
        mRejectedBySpec = subResult.mRejectedBySpec;
      }
      mAccepted &= subResult.mAccepted;
      if (mPaths != null) {
        mPaths[traceIndex] = subResult.mPaths[0];
      }
    }

    //#######################################################################
    //# Data Members
    private int mTotalAcceptedSteps;
    private int mFirstAcceptedSteps;
    private boolean mAccepted;
    private boolean mRejectedBySpec;
    private final StateProxy[][] mPaths;
  }


  //#########################################################################
  //# Inner Class SearchRecord
  private static class SearchRecord
  {
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
  private final TRAutomatonProxy mTRAutomaton;
  private boolean mSpec;
  private final TObjectIntMap<StateProxy> mStateMap;
  private final List<SearchRecord> mInitialStates;
  private final boolean mDeterministic;

  private Proxy mLastInput;
  private Result mLastResult;

}
