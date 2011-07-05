//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.op
//# CLASS:   TraceFinder
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.op;

import gnu.trove.TIntHashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.StateEncoding;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;


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
      final Collection<EventProxy> empty = Collections.emptyList();
      mEventEncoding = new EventEncoding(aut, translator, empty,
                                         EventEncoding.FILTER_PROPOSITIONS);
      final StateEncoding enc = new StateEncoding(aut);
      mTransitionRelation = new ListBufferTransitionRelation
      (aut, mEventEncoding, enc,
       ListBufferTransitionRelation.CONFIG_SUCCESSORS);
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
    return getNumberOfAcceptedSteps(trace) == len;
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
  public int getNumberOfAcceptedSteps(final TraceProxy trace)
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
      return depth;
    }
  }

  /**
   * Gets a trace state on the path found by a previous call to
   * {@link #getNumberOfAcceptedSteps(TraceProxy) accepts()}
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
  private final EventEncoding mEventEncoding;
  private final StateEncoding mStateEncoding;
  private final ListBufferTransitionRelation mTransitionRelation;
  private final List<SearchRecord> mInitialStates;

  private StateProxy[] mPath;

}
