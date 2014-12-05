//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.trcomp
//# CLASS:   TRAbstractionStepPartition
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
import net.sourceforge.waters.analysis.tr.StatusGroupTransitionIterator;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.analysis.tr.TRPartition;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisAbortException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.des.EventProxy;

import org.apache.log4j.Logger;


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
    super(pred.getName());
    mPredecessor = pred;
    mEventEncoding = eventEncoding;
    mDefaultMarking = defaultMarking;
    mPreconditionMarking = preconditionMarking;
    mUsedSimplifiers = new LinkedList<>();
    mUsedSimplifiers.add(simplifier);
    mIsPartitioning = simplifier.isPartitioning();
    mPartition = mIsPartitioning ? simplifier.getResultPartition() : null;
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
    chain.setPropositions(mPreconditionMarking, mDefaultMarking);
    chain.setPreferredOutputConfiguration(preferredConfig);
    final int inputConfig = chain.getPreferredInputConfiguration();
    final TRAutomatonProxy inputAut =
      mPredecessor.getOutputAutomaton(inputConfig);
    // We are going to destructively change this automaton,
    // so we need to clear the copy cached on the predecessor.
    mPredecessor.clearOutputAutomaton();
    final ListBufferTransitionRelation inputRel =
      inputAut.getTransitionRelation();
    final EventEncoding inputEventEncoding = new EventEncoding(mEventEncoding);
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
    final TRAutomatonProxy aut = mPredecessor.createOutputAutomaton
      (ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    final ListBufferTransitionRelation rel = aut.getTransitionRelation();
    rel.reconfigure(ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    final TraceExpander expander = new TraceExpander(trace, rel, analyzer);
    final SearchRecord found = expander.findTrace();
    final List<SearchRecord> searchRecordTrace = found.getSearchRecordTrace();
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
  public void report(final Logger logger)
  {
    if (logger.isDebugEnabled()) {
      logger.debug("Expanding partition of " + getName() + " ...");
    }
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
      mSearchSpace = new BFSSearchSpace<>(rel.getNumberOfStates());
      mStartOfNextLevel = mNonDeadlockTarget = null;
    }

    //#######################################################################
    //# Trace Search
    private SearchRecord findTrace()
      throws AnalysisAbortException, OverflowException
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
      final TransitionIterator iterEvent =
        mInputTransitionRelation.createSuccessorsReadOnlyIterator();
      final TransitionIterator inner =
        mInputTransitionRelation.createSuccessorsReadOnlyIterator();
      final TransitionIterator iterLocal = new StatusGroupTransitionIterator
        (inner, mEventEncoding, EventStatus.STATUS_LOCAL);
      while (!mSearchSpace.isEmpty()) {
        mAnalyzer.checkAbort();
        final SearchRecord current = mSearchSpace.poll();
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
            final SearchRecord next = new SearchRecord(t, current, e, false);
            if (processSearchRecord(next)) {
              return next;
            }
          }
        }
        iterLocal.resetState(s);
        while (iterLocal.advance()) {
          final int l = iterLocal.getCurrentEvent();
          final int t = iterLocal.getCurrentTargetState();
          final SearchRecord next = new SearchRecord(t, current, l, true);
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

    private boolean isTargetState(final SearchRecord record)
    {
      if (record.getNumberOfConsumedEvents() < mEventSequence.size()) {
        return false;
      } else if (!mIsPartitioning) {
        return true;
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
                                 final List<EventProxy> nonStutterEvents,
                                 final TIntArrayList outputStates)
    {
      final Iterator<SearchRecord> searchRecordIter =
        searchRecordTrace.iterator();
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
        while (nextInputEvent == null && inputEventIter.hasNext()) {
          final EventProxy event = inputEventIter.next();
          final int e = mEventEncoding.getEventCode(event);
          if (e < 0) {
            nextInputEvent = event;
          } else {
            final byte status = mEventEncoding.getProperEventStatus(e);
            if (!EventStatus.isLocalEvent(status)) {
              nextInputEvent = event;
            }
          }
        }
        if (nextSearchRecord == null && nextInputEvent == null) {
          break;
        }
        if (nextInputEvent != null) {
          final int e = mEventEncoding.getEventCode(nextInputEvent);
          if (e < 0) {
            outputEvents.add(nextInputEvent);
            nonStutterEvents.add(nextInputEvent);
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
    //# Debugging
    @Override
    public String toString()
    {
      return mState + "@" + mNumConsumedEvents;
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
  private final List<TransitionRelationSimplifier> mUsedSimplifiers;
  private boolean mIsPartitioning;
  private TRPartition mPartition;

}
