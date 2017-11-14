//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2017 Robi Malik
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

package net.sourceforge.waters.analysis.trcomp;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.sourceforge.waters.analysis.abstraction.ChainTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.TransitionRelationSimplifier;
import net.sourceforge.waters.analysis.tr.BFSSearchSpace;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.StatusGroupTransitionIterator;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.analysis.tr.TRPartition;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisAbortException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.des.EventProxy;

import org.apache.logging.log4j.Logger;


/**
 * An abstraction step representing a partitioning abstraction step.
 *
 * @author Robi Malik
 */

class TRAbstractionStepPartition
  extends TRAbstractionStep
{

  //#########################################################################
  //# Constructor
  TRAbstractionStepPartition(final TRAbstractionStep pred,
                             final EventEncoding encBefore,
                             final TransitionRelationSimplifier simplifier)
  {
    super(pred.getName());
    mPredecessor = pred;
    mEventEncodingBefore = encBefore;
    mUsedSimplifiers = new LinkedList<>();
    mUsedSimplifiers.add(simplifier);
    mIsPartitioning = simplifier.isPartitioning();
    mPartition = mIsPartitioning ? simplifier.getResultPartition() : null;
    mRelevantPreconditionMarkings = null;
  }


  //#########################################################################
  //# Simple Access
  TRAbstractionStep getPredecessor()
  {
    return mPredecessor;
  }

  TRPartition getPartition()
  {
    return mPartition;
  }

  boolean isEmpty()
  {
    return mUsedSimplifiers.isEmpty();
  }

  TransitionRelationSimplifier getLastSimplifier()
  {
    final int end = mUsedSimplifiers.size() - 1;
    return mUsedSimplifiers.get(end);
  }

  TransitionRelationSimplifier removeLastSimplifier()
  {
    final int end = mUsedSimplifiers.size() - 1;
    return mUsedSimplifiers.remove(end);
  }

  void merge(final TransitionRelationSimplifier simplifier)
  {
    mUsedSimplifiers.add(simplifier);
    mIsPartitioning &= simplifier.isPartitioning();
    if (mIsPartitioning) {
      final TRPartition partition = simplifier.getResultPartition();
      mPartition = TRPartition.combine(mPartition, partition);
    } else {
      mPartition = null;
    }
  }

  void setRelevantPreconditionMarkings(final BitSet markings)
  {
    mRelevantPreconditionMarkings = markings;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.trcomp.TRAbstractionStep
  @Override
  public Collection<TRAbstractionStep> getPredecessors()
  {
    return Collections.singletonList(mPredecessor);
  }

  @Override
  public TRAutomatonProxy createOutputAutomaton(final int preferredConfig)
    throws AnalysisException
  {
    final ChainTRSimplifier chain = new ChainTRSimplifier(mUsedSimplifiers);
    chain.setPreferredOutputConfiguration(preferredConfig);
    final int inputConfig = chain.getPreferredInputConfiguration();
    final EventEncoding inputEventEncoding =
      new EventEncoding(mEventEncodingBefore);
    final TRAutomatonProxy inputAut =
      mPredecessor.getClonedOutputAutomaton(inputEventEncoding, inputConfig);
    final ListBufferTransitionRelation inputRel =
      inputAut.getTransitionRelation();
    final Logger logger = getLogger();
    reportRebuilding();
    inputRel.logSizes(logger);
    final ListBufferTransitionRelation outputRel =
      new ListBufferTransitionRelation(inputRel, inputEventEncoding, inputConfig);
    chain.setTransitionRelation(outputRel);
    chain.run();
    return new TRAutomatonProxy(inputEventEncoding, outputRel);
  }

  @Override
  public void expandTrace(final TRTraceProxy trace,
                          final AbstractTRCompositionalAnalyzer analyzer)
    throws AnalysisException
  {
    final TRAutomatonProxy aut = mPredecessor.getOutputAutomaton
      (ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    final ListBufferTransitionRelation rel = aut.getTransitionRelation();
    rel.reconfigure(ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    final TraceExpander expander = new TraceExpander(trace, rel, analyzer);
    final TRTraceSearchRecord found = expander.findTrace();
    final List<TRTraceSearchRecord> searchRecordTrace = found.getSearchRecordTrace();
    final int maxLength = trace.getNumberOfSteps() + searchRecordTrace.size();
    final List<EventProxy> newEvents = new ArrayList<>(maxLength);
    final List<EventProxy> nonStutterEvents =
      new ArrayList<>(trace.getNumberOfSteps());
    final TIntArrayList states = new TIntArrayList(maxLength);
    expander.mergeEventTrace
      (trace, searchRecordTrace, newEvents, nonStutterEvents, states);
    trace.removeAutomaton(this);
    trace.addStutteringSteps(newEvents, nonStutterEvents);
    final int[] statesArray = states.toArray();
    trace.addAutomaton(mPredecessor, statesArray);
  }


  //#########################################################################
  //# Debugging
  @Override
  public void reportExpansion()
  {
    final Logger logger = getLogger();
    logger.debug("Expanding partition of {} ...", getName());
  }


  //#########################################################################
  //# Inner Class TraceExpander
  private class TraceExpander
  {
    //#######################################################################
    //# Constructor
    private TraceExpander(final TRTraceProxy trace,
                          final ListBufferTransitionRelation rel,
                          final AbstractTRCompositionalAnalyzer analyzer)
    {
      mAnalyzer = analyzer;
      mInputTransitionRelation = rel;
      final byte mask = EventStatus.STATUS_LOCAL | EventStatus.STATUS_UNUSED;
      final List<EventProxy> events = trace.getEvents();
      final int numTraceEvents = events.size();
      mEventSequence = new TIntArrayList(numTraceEvents);
      boolean lastFailing = false;
      for (final EventProxy event : events) {
        final int e = mEventEncodingBefore.getEventCode(event);
        if (e >= 0) {
          final byte status = mEventEncodingBefore.getProperEventStatus(e);
          if ((status & mask) == 0) {
            mEventSequence.add(e);
            lastFailing = EventStatus.isFailingEvent(status);
          }
        }
      }
      if (!mIsPartitioning || lastFailing) {
        mTargetState = -1;
        mTargetStateClass = null;
      } else if (mPartition == null) {
        mTargetState =
          trace.getState(TRAbstractionStepPartition.this, numTraceEvents);
        mTargetStateClass = null;
      } else {
        mTargetState =
          trace.getState(TRAbstractionStepPartition.this, numTraceEvents);
        final int[] clazz = mPartition.getStates(mTargetState);
        mTargetStateClass = new TIntHashSet(clazz);
      }
      if (rel.isPropositionUsed(TRCompositionalConflictChecker.DEFAULT_MARKING)) {
        boolean hasDeadlock = false;
        for (int s = 0; s < rel.getNumberOfStates(); s++) {
          if (rel.isReachable(s) &&
              rel.isDeadlockState(s, TRCompositionalConflictChecker.DEFAULT_MARKING)) {
            hasDeadlock = true;
            break;
          }
        }
        mHasDeadlockState = hasDeadlock;
      } else {
        mHasDeadlockState = false;
      }
      mSearchSpace = new BFSSearchSpace<>(rel.getNumberOfStates());
      mStartOfNextLevel = mNonDeadlockTarget = null;
    }

    //#######################################################################
    //# Trace Search
    private TRTraceSearchRecord findTrace()
      throws AnalysisAbortException, OverflowException
    {
      final int numStates = mInputTransitionRelation.getNumberOfStates();
      for (int s = 0; s < numStates; s++) {
        if (mInputTransitionRelation.isInitial(s)) {
          final TRTraceSearchRecord record = createInitialRecord(s);
          if (processSearchRecord(record)) {
            return record;
          }
        }
      }
      final TransitionIterator iterEvent =
        mInputTransitionRelation.createSuccessorsReadOnlyIterator();
      final TransitionIterator inner =
        mInputTransitionRelation.createSuccessorsReadOnlyIterator();
      final TransitionIterator iterLocal = new StatusGroupTransitionIterator
        (inner, mEventEncodingBefore, EventStatus.STATUS_LOCAL);
      while (!mSearchSpace.isEmpty()) {
        mAnalyzer.checkAbort();
        final TRTraceSearchRecord current = mSearchSpace.poll();
        if (current == mStartOfNextLevel) {
          if (mNonDeadlockTarget != null) {
            return mNonDeadlockTarget;
          }
          mStartOfNextLevel = null;
        }
        final int s = current.getState();
        final int depth = current.getNumberOfConsumedEvents();
        if (depth < mEventSequence.size()) {
          final int e = mEventSequence.get(depth);
          iterEvent.reset(s, e);
          while (iterEvent.advance()) {
            final int t = iterEvent.getCurrentTargetState();
            final TRTraceSearchRecord next = createNextRecord(t, e, current);
            if (processSearchRecord(next)) {
              return next;
            }
          }
        }
        iterLocal.resetState(s);
        while (iterLocal.advance()) {
          final int l = iterLocal.getCurrentEvent();
          final int t = iterLocal.getCurrentTargetState();
          final TRTraceSearchRecord next = createNextRecord(t, l, current);
          if (processSearchRecord(next)) {
            return next;
          }
        }
      }
      assert mNonDeadlockTarget != null :
        "Failed to expand trace for input automaton " +
        mInputTransitionRelation.getName() + "!";
      return mNonDeadlockTarget;
    }

    private TRTraceSearchRecord createInitialRecord(final int state)
    {
      return createNextRecord(state, -1, null);
    }

    private TRTraceSearchRecord createNextRecord(final int state,
                                                 final int event,
                                                 final TRTraceSearchRecord pred)
    {
      // Trick! Search records contain the number of consumed events,
      // or the number of consumed events plus one, if all events
      // in the input trace have been consumed and a state equivalent
      // to the end state of the input trace has been reached.
      // For generalised nonblocking, the search has to continue towards
      // precondition-marked state not equivalent to the trace end state.
      int consumed = 0;
      if (pred != null) {
        consumed = pred.getNumberOfConsumedEvents();
        final byte status = mEventEncodingBefore.getProperEventStatus(event);
        if (!EventStatus.isLocalEvent(status)) {
          consumed++;
        }
      }
      if (consumed == mEventSequence.size() && isTraceEndState(state)) {
        consumed++;
      }
      return new TRTraceSearchRecord(state, consumed, event, pred);
    }

    private boolean processSearchRecord(final TRTraceSearchRecord record)
    {
      if (isTargetState(record)) {
        if (!mHasDeadlockState) {
          return true;
        }
        final int state = record.getState();
        if (mInputTransitionRelation.isDeadlockState
              (state, TRCompositionalConflictChecker.DEFAULT_MARKING)) {
          return true;
        }
        if (mNonDeadlockTarget == null) {
          mNonDeadlockTarget = record;
        }
      }
      if (mSearchSpace.addIfUnvisited(record)) {
        if (mStartOfNextLevel == null) {
          mStartOfNextLevel = record;
        }
      }
      return false;
    }

    private boolean isTraceEndState(final int state)
    {
      if (mTargetStateClass != null) {
        return mTargetStateClass.contains(state);
      } else if (mTargetState >= 0) {
        return state == mTargetState;
      } else {
        return true;
      }
    }

    private boolean isTargetState(final TRTraceSearchRecord record)
    {
      if (record.getNumberOfConsumedEvents() <= mEventSequence.size()) {
        return false;
      } else if (mRelevantPreconditionMarkings != null) {
        final int state = record.getState();
        return mRelevantPreconditionMarkings.get(state);
      } else {
        final int state = record.getState();
        return mInputTransitionRelation.isMarked
          (state, AbstractTRCompositionalAnalyzer.PRECONDITION_MARKING);
      }
    }


    //#######################################################################
    //# Trace Merging
    private void mergeEventTrace(final TRTraceProxy trace,
                                 final List<TRTraceSearchRecord> searchRecordTrace,
                                 final List<EventProxy> outputEvents,
                                 final List<EventProxy> nonStutterEvents,
                                 final TIntArrayList outputStates)
    {
      final Iterator<TRTraceSearchRecord> searchRecordIter =
        searchRecordTrace.iterator();
      TRTraceSearchRecord nextSearchRecord = searchRecordIter.next();
      int currentState = nextSearchRecord.getState();
      outputStates.add(currentState);
      nextSearchRecord = null;
      final List<EventProxy> inputEvents = trace.getEvents();
      final Iterator<EventProxy> inputEventIter = inputEvents.iterator();
      EventProxy nextInputEvent = null;
      byte nextInputStatus = EventStatus.STATUS_UNUSED;
      while (true) {
        if (nextSearchRecord == null && searchRecordIter.hasNext()) {
          nextSearchRecord = searchRecordIter.next();
        }
        while (nextInputEvent == null && inputEventIter.hasNext()) {
          final EventProxy event = inputEventIter.next();
          final int e = mEventEncodingBefore.getEventCode(event);
          if (e < 0) {
            nextInputEvent = event;
            nextInputStatus = EventStatus.STATUS_UNUSED;
          } else {
            final byte status = mEventEncodingBefore.getProperEventStatus(e);
            if (!EventStatus.isLocalEvent(status)) {
              nextInputEvent = event;
              nextInputStatus = status;
            }
          }
        }
        if (nextSearchRecord == null && nextInputEvent == null) {
          break;
        }
        if (nextInputEvent != null &&
            !EventStatus.isUsedEvent(nextInputStatus)) {
          outputEvents.add(nextInputEvent);
          nonStutterEvents.add(nextInputEvent);
          outputStates.add(currentState);
          nextInputEvent = null;
          continue;
        }
        assert nextSearchRecord != null;
        final int e = nextSearchRecord.getEvent();
        final EventProxy searchRecordEvent =
          mEventEncodingBefore.getProperEvent(e);
        outputEvents.add(searchRecordEvent);
        currentState = nextSearchRecord.getState();
        outputStates.add(currentState);
        nextSearchRecord = null;
        if (searchRecordEvent == nextInputEvent) {
          nonStutterEvents.add(nextInputEvent);
          nextInputEvent = null;
        }
      }
    }


    //#######################################################################
    //# Data Members
    private final AbstractTRCompositionalAnalyzer mAnalyzer;
    private final ListBufferTransitionRelation mInputTransitionRelation;
    private final TIntArrayList mEventSequence;
    private final int mTargetState;
    private final TIntHashSet mTargetStateClass;
    private boolean mHasDeadlockState;
    private final BFSSearchSpace<TRTraceSearchRecord> mSearchSpace;
    private TRTraceSearchRecord mStartOfNextLevel;
    private TRTraceSearchRecord mNonDeadlockTarget;
  }


  //#########################################################################
  //# Data Members
  private final TRAbstractionStep mPredecessor;
  private final EventEncoding mEventEncodingBefore;
  private final List<TransitionRelationSimplifier> mUsedSimplifiers;
  private boolean mIsPartitioning;
  private TRPartition mPartition;
  private BitSet mRelevantPreconditionMarkings;

}
