//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.trcomp
//# CLASS:   TRAbstractionStepDrop
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.trcomp;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayList;
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
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.analysis.tr.TRPartition;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.des.EventProxy;


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
                             final EventEncoding eventEncoding,
                             final int defaultMarking,
                             final int preconditionMarking,
                             final TransitionRelationSimplifier simplifier)
  {
    mPredecessor = pred;
    mEventEncoding = eventEncoding;
    mDefaultMarking = defaultMarking;
    mPreconditionMarking = preconditionMarking;
    mSimplificationSteps = new LinkedList<>();
    mSimplificationSteps.add(simplifier);
    mPartition = simplifier.getResultPartition();
    pred.setSuccessor(this);
  }


  //#########################################################################
  //# Simple Access
  TRPartition getPartition()
  {
    return mPartition;
  }

  void merge(final TransitionRelationSimplifier simplifier)
  {
    mSimplificationSteps.add(simplifier);
    final TRPartition partition = simplifier.getResultPartition();
    mPartition = TRPartition.combine(mPartition, partition);
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
    final ChainTRSimplifier chain = new ChainTRSimplifier(mSimplificationSteps);
    chain.setPreferredOutputConfiguration(preferredConfig);
    final int inputConfig = chain.getPreferredInputConfiguration();
    final TRAutomatonProxy inputAut = mPredecessor.getOutputAutomaton(inputConfig);
    final ListBufferTransitionRelation inputRel =
      inputAut.getTransitionRelation();
    final ListBufferTransitionRelation outputRel =
      new ListBufferTransitionRelation(inputRel, mEventEncoding, inputConfig);
    chain.setTransitionRelation(outputRel);
    chain.run();
    return new TRAutomatonProxy(mEventEncoding, outputRel);
  }

  @Override
  public void expandTrace(final TRTraceProxy trace)
    throws AnalysisException
  {
    final TRAutomatonProxy aut = mPredecessor.createOutputAutomaton
      (ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    final ListBufferTransitionRelation rel = aut.getTransitionRelation();
    rel.reconfigure(ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    final TraceExpander expander = new TraceExpander(trace, rel);
    final SearchRecord found = expander.findTrace();
    final List<SearchRecord> searchRecordTrace = found.getSearchRecordTrace();
    final int maxLength = trace.getNumberOfSteps() + searchRecordTrace.size();
    final List<EventProxy> eventTrace = new ArrayList<>(maxLength);
    final TIntArrayList states = new TIntArrayList(maxLength);
    expander.mergeEventTrace(trace, searchRecordTrace, eventTrace, states);
    final int[] stepStates = states.toArray();
    final EventProxy[] newEvents = new EventProxy[eventTrace.size()];
    eventTrace.toArray(newEvents);
    trace.expandPartitionStep(this, stepStates, newEvents);
  }


  //#########################################################################
  //# Inner Class TraceExpander
  private class TraceExpander
  {
    //#######################################################################
    //# Constructor
    private TraceExpander(final TRTraceProxy trace,
                          final ListBufferTransitionRelation rel)
    {
      mInputTransitionRelation = rel;
      final List<EventProxy> events = trace.getEvents();
      final int numTraceEvents = events.size();
      mEventSequence = new TIntArrayList(numTraceEvents);
      boolean lastFailing = false;
      for (final EventProxy event : events) {
        final int e = mEventEncoding.getEventCode(event);
        if (e >= 0) {
          final byte status = mEventEncoding.getProperEventStatus(e);
          if (!EventStatus.isLocalEvent(status)) {
            mEventSequence.add(e);
            lastFailing = EventStatus.isFailingEvent(status);
          }
        } else {
          lastFailing = false;
        }
      }
      if (lastFailing) {
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
        mTargetStateClass = new TIntHashSet(clazz.length);
        final boolean usesPreconditionMarking =
          mPreconditionMarking >= 0 && rel.isPropositionUsed(mPreconditionMarking);
        for (final int s : clazz) {
          if (!usesPreconditionMarking || rel.isMarked(s, mPreconditionMarking)) {
            mTargetStateClass.add(s);
          }
        }
      }
      if (rel.isPropositionUsed(mDefaultMarking)) {
        boolean hasDeadlock = false;
        for (int s = 0; s < rel.getNumberOfStates(); s++) {
          if (rel.isReachable(s) && rel.isDeadlockState(s, mDefaultMarking)) {
            hasDeadlock = true;
            break;
          }
        }
        mHasDeadlockState = hasDeadlock;
      } else {
        mHasDeadlockState = false;
      }
      final int numTREvents = mEventEncoding.getNumberOfProperEvents();
      mLocalEventRanges = new TIntArrayList(numTREvents);
      final byte pattern = EventStatus.STATUS_LOCAL | EventStatus.STATUS_UNUSED;
      int i = -1;
      for (int e = EventEncoding.TAU; e < numTREvents; e++) {
        final byte status = rel.getProperEventStatus(e);
        if ((status & pattern) != EventStatus.STATUS_LOCAL) {
          // not a local event
          if (EventStatus.isUsedEvent(status)) {
            i = -1;
          }
        } else if (i < 0) {
          // local event starts new range
          mLocalEventRanges.add(e);
          mLocalEventRanges.add(e);
          i = mLocalEventRanges.size() - 1;
        } else {
          // local event added to existing range
          mLocalEventRanges.set(i, e);
        }
      }
      mSearchSpace = new BFSSearchSpace<>(mPartition.getNumberOfStates());
      mStartOfNextLevel = mNonDeadlockTarget = null;
    }

    //#######################################################################
    //# Trace Search
    private SearchRecord findTrace()
    {
      final int numStates = mInputTransitionRelation.getNumberOfStates();
      for (int s = 0; s < numStates; s++) {
        if (mInputTransitionRelation.isInitial(s)) {
          final SearchRecord record = new SearchRecord(s);
          if (processSearchRecord(record)) {
            return record;
          }
        }
      }
      final TransitionIterator iter =
        mInputTransitionRelation.createSuccessorsReadOnlyIterator();
      while (true) {
        final SearchRecord current = mSearchSpace.poll();
        assert current != null : "Failed to expand trace for input automaton " +
                                 mInputTransitionRelation.getName() + "!";
        if (current == mStartOfNextLevel) {
          if (mNonDeadlockTarget != null) {
            return mNonDeadlockTarget;
          }
          mStartOfNextLevel = null;
        }
        final int s = current.getState();
        final int depth = current.getNumberOfConsumedEvents();
        final int e = mEventSequence.get(depth);
        iter.reset(s, e);
        while (iter.advance()) {
          final int t = iter.getCurrentTargetState();
          final SearchRecord next = new SearchRecord(t, current, e, false);
          if (processSearchRecord(next)) {
            return next;
          }
        }
        for (int i = 0; i < mLocalEventRanges.size(); i += 2) {
          final int first = mLocalEventRanges.get(i);
          final int last = mLocalEventRanges.get(i + 1);
          iter.resetEvents(first, last);
          while (iter.advance()) {
            final int l = iter.getCurrentEvent();
            final int t = iter.getCurrentTargetState();
            final SearchRecord next = new SearchRecord(t, current, l, true);
            if (processSearchRecord(next)) {
              return next;
            }
          }
        }
      }
    }

    private boolean processSearchRecord(final SearchRecord record)
    {
      if (isTargetState(record)) {
        if (!mHasDeadlockState) {
          return true;
        }
        final int state = record.getState();
        if (mInputTransitionRelation.isDeadlockState(state, mDefaultMarking)) {
          return true;
        }
        if (mNonDeadlockTarget != null) {
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

    private boolean isTargetState(final SearchRecord record)
    {
      if (record.getNumberOfConsumedEvents() < mEventSequence.size()) {
        return false;
      }
      final int state = record.getState();
      if (mTargetStateClass != null) {
        return mTargetStateClass.contains(state);
      } else if (mTargetState >= 0) {
        return state == mTargetState;
      } else {
        return true;
      }
    }

    //#######################################################################
    //# Trace Merging
    private void mergeEventTrace(final TRTraceProxy trace,
                                 final List<SearchRecord> searchRecordTrace,
                                 final List<EventProxy> outputEvents,
                                 final TIntArrayList outputStates)
    {
      final Iterator<SearchRecord> searchRecordIter = searchRecordTrace.iterator();
      SearchRecord nextSearchRecord = searchRecordIter.next();
      int currentState = nextSearchRecord.getState();
      outputStates.add(currentState);
      nextSearchRecord = null;
      final List<EventProxy> inputEvents = trace.getEvents();
      final Iterator<EventProxy> inputEventIter = inputEvents.iterator();
      EventProxy nextInputEvent = null;
      while (true) {
        if (nextSearchRecord == null && searchRecordIter.hasNext()) {
          nextSearchRecord = searchRecordIter.next();
        }
        if (nextInputEvent == null && inputEventIter.hasNext()) {
          nextInputEvent = inputEventIter.next();
        }
        if (nextSearchRecord == null && nextInputEvent == null) {
          break;
        }
        if (nextInputEvent != null) {
          final int e = mEventEncoding.getEventCode(nextInputEvent);
          if (e < 0) {
            outputEvents.add(nextInputEvent);
            outputStates.add(currentState);
            nextInputEvent = null;
            continue;
          }
        }
        assert nextSearchRecord != null;
        final int e = nextSearchRecord.getEvent();
        final EventProxy searchRecordEvent = mEventEncoding.getProperEvent(e);
        outputEvents.add(searchRecordEvent);
        currentState = nextSearchRecord.getState();
        outputStates.add(currentState);
        nextSearchRecord = null;
        if (searchRecordEvent == nextInputEvent) {
          nextInputEvent = null;
        }
      }
    }


    //#######################################################################
    //# Data Members
    private final ListBufferTransitionRelation mInputTransitionRelation;
    private final TIntArrayList mEventSequence;
    private final int mTargetState;
    private final TIntHashSet mTargetStateClass;
    private boolean mHasDeadlockState;
    private final TIntArrayList mLocalEventRanges;
    private final BFSSearchSpace<SearchRecord> mSearchSpace;
    private SearchRecord mStartOfNextLevel;
    private SearchRecord mNonDeadlockTarget;
  }


  //#########################################################################
  //# Inner Class SearchRecord
  private static class SearchRecord
  {
    //#######################################################################
    //# Constructor
    private SearchRecord(final int state)
    {
      this(state, 0, -1, null);
    }

    private SearchRecord(final int state,
                         final SearchRecord pred,
                         final int event,
                         final boolean local)
    {
      this(state, pred.mNumConsumedEvents + (local ? 0 : 1), event, pred);
    }

    private SearchRecord(final int state,
                         final int numConsumedEvents,
                         final int event,
                         final SearchRecord pred)
    {
      mState = state;
      mNumConsumedEvents = numConsumedEvents;
      mEvent = event;
      mPredecessor = pred;
    }

    //#######################################################################
    //# Simple Access
    private int getState()
    {
      return mState;
    }

    private int getNumberOfConsumedEvents()
    {
      return mNumConsumedEvents;
    }

    public int getEvent()
    {
      return mEvent;
    }

    //#######################################################################
    //# Trace Construction
    private List<SearchRecord> getSearchRecordTrace()
    {
      final List<SearchRecord> list = new LinkedList<>();
      SearchRecord record = this;
      while (record != null) {
        list.add(0, record);
        record = record.mPredecessor;
      }
      return list;
    }

    //#######################################################################
    //# Overrides for java.lang.Object
    @Override
    public boolean equals(final Object other)
    {
      if (other != null && getClass() == other.getClass()) {
        final SearchRecord record = (SearchRecord) other;
        return mState == record.mState &&
               mNumConsumedEvents == record.mNumConsumedEvents;
      } else {
        return false;
      }
    }

    @Override
    public int hashCode()
    {
      return mState + 5 * mNumConsumedEvents;
    }

    //#######################################################################
    //# Data Members
    private final int mState;
    private final int mNumConsumedEvents;
    private final int mEvent;
    private final SearchRecord mPredecessor;
  }


  //#########################################################################
  //# Data Members
  private final TRAbstractionStep mPredecessor;
  private final EventEncoding mEventEncoding;
  private final int mDefaultMarking;
  private final int mPreconditionMarking;
  private final List<TransitionRelationSimplifier> mSimplificationSteps;
  private TRPartition mPartition;

}
