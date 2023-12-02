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
import net.sourceforge.waters.analysis.abstraction.OmegaRemovalTRSimplifier;
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
import net.sourceforge.waters.model.analysis.des.EventNotFoundException;
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
                          final AbstractTRCompositionalModelAnalyzer analyzer)
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
  /**
   * <P>A utility class to run the trace expansion process for a
   * partition-based abstraction step.</P>
   *
   * <P>Trace expansion performs a breadth-first search through the
   * original automaton (before abstraction) to find a concrete trace accepted
   * by the original automaton that uses the same non-local events as the
   * abstract trace, which ends in a state that belongs to the equivalence
   * class represented by the end state of the abstract trace.</P>
   *
   * <P>Special support is built in to support conflict trace expansion.</P>
   * <UL>
   * <LI>Trace expansion may end prematurely if a deadlock state is reached.
   * Here, a deadlock state is a state not marked with the accepting
   * proposition ({@link AbstractTRCompositionalModelAnalyzer#DEFAULT_MARKING})
   * and without outgoing transitions. In the case of generalised nonblocking,
   * the deadlock state must also be marked with the precondition marking
   * ({@link AbstractTRCompositionalModelAnalyzer#PRECONDITION_MARKING}).
   * If a deadlock state is encountered during the search, expansion stops
   * immediately, possibly returning a concrete trace that does not accept
   * all the events found in the abstract trace. This is to support selfloop
   * removal abstraction, where an event can be removed from an automaton
   * if it is selflooped in all states except for deadlock states.</LI>
   * <LI>For generalised nonblocking, it is ensured that the trace ends in
   * a precondition-marked state. That is, after all non-local events are
   * consumed and a state in the equivalence class of the end state of the
   * abstract trace is reached, the search continues using local events until
   * a precondition-marked state is reached. The set of precondition-marked
   * states considered for this purpose can restricted through the method
   * {@link TRAbstractionStepPartition#setRelevantPreconditionMarkings(BitSet)}
   * to support possible &omega;-Removal ({@link OmegaRemovalTRSimplifier})
   * abstraction steps.</LI>
   * </UL>
   * <P>These special conditions only apply when the invoking model verifier
   * uses a default marking ({@link
   * AbstractTRCompositionalModelAnalyzer#getUsedDefaultMarking()}),
   * i.e., not for language inclusion checks.</P>
   *
   * @author Robi Malik
   */
  private class TraceExpander
  {
    //#######################################################################
    //# Constructor
    private TraceExpander(final TRTraceProxy trace,
                          final ListBufferTransitionRelation rel,
                          final AbstractTRCompositionalModelAnalyzer analyzer)
       throws EventNotFoundException
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
      mHasDeadlockState = false;
      if (analyzer.getUsedDefaultMarking() != null &&
          rel.isPropositionUsed(AbstractTRCompositionalModelAnalyzer.DEFAULT_MARKING)) {
        for (int s = 0; s < rel.getNumberOfStates(); s++) {
          if (rel.isReachable(s) && isDeadlockState(s)) {
            mHasDeadlockState = true;
            break;
          }
        }
      }
      mSearchSpace = new BFSSearchSpace<>(rel.getNumberOfStates());
      mStartOfNextLevel = mNonDeadlockTarget = null;
    }

    //#######################################################################
    //# Trace Search
    private TRTraceSearchRecord findTrace()
      throws AnalysisAbortException
    {
      final int numStates = mInputTransitionRelation.getNumberOfStates();
      for (int s = 0; s < numStates; s++) {
        if (mInputTransitionRelation.isInitial(s)) {
          final SearchRecord record = new SearchRecord(this, s);
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
            final SearchRecord next = new SearchRecord(this, t, current, e);
            if (processSearchRecord(next)) {
              return next;
            }
          }
        }
        iterLocal.resetState(s);
        while (iterLocal.advance()) {
          final int l = iterLocal.getCurrentEvent();
          final int t = iterLocal.getCurrentTargetState();
          final SearchRecord next = new SearchRecord(this, t, current, l);
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
      final int state = record.getState();
      if (mHasDeadlockState && isDeadlockState(state)) {
        mNonDeadlockTarget = null;
        return true;
      } else if (record.isEndStateReached()) {
        mNonDeadlockTarget = record;
        if (!mHasDeadlockState) {
          return true;
        }
      }
      if (mSearchSpace.addIfUnvisited(record)) {
        if (mStartOfNextLevel == null) {
          mStartOfNextLevel = record;
        }
      }
      return false;
    }

    private boolean isTargetState(final int state, final int consumed)
    {
      return consumed == mEventSequence.size() && isTargetState(state);
    }

    private boolean isTargetState(final int state)
    {
      if (mTargetStateClass != null) {
        return mTargetStateClass.contains(state);
      } else if (mTargetState >= 0) {
        return state == mTargetState;
      } else {
        return true;
      }
    }

    private boolean isDeadlockState(final int state)
    {
      return
        mInputTransitionRelation.isDeadlockState
          (state, AbstractTRCompositionalModelAnalyzer.DEFAULT_MARKING) &&
        isRelevantPreconditionMarkedState(state);
    }

    private boolean isRelevantPreconditionMarkedState(final int state)
    {
      if (mRelevantPreconditionMarkings != null) {
        return mRelevantPreconditionMarkings.get(state);
      } else {
        return mInputTransitionRelation.isMarked
          (state, AbstractTRCompositionalModelAnalyzer.PRECONDITION_MARKING);
      }
    }

    private boolean isLocalEvent(final int event)
    {
      final byte status = mEventEncodingBefore.getProperEventStatus(event);
      return EventStatus.isLocalEvent(status);
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
        if (nextSearchRecord == null) {
          if (searchRecordIter.hasNext()) {
            nextSearchRecord = searchRecordIter.next();
          } else if (mNonDeadlockTarget == null) {
            break;
          }
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
    private final AbstractTRCompositionalModelAnalyzer mAnalyzer;
    /**
     * The automaton before abstraction.
     */
    private final ListBufferTransitionRelation mInputTransitionRelation;
    /**
     * The sequence of non-local event codes to be included in the
     * concrete trace.
     */
    private final TIntArrayList mEventSequence;
    /**
     * The target state of the concrete trace, or <CODE>-1</CODE>
     * if unspecified. If the {@link #mTargetStateClass} is <CODE>null</CODE>
     * and the {@link #mTargetState} is <CODE>-1</CODE>, then any state
     * can be used as end state.
     */
    private final int mTargetState;
    /**
     * A set of possible target states for the concrete trace, or
     * <CODE>null</CODE> if unspecified. If specified, this has
     * precedence of {@link #mTargetState}.
     */
    private final TIntHashSet mTargetStateClass;

    /**
     * Whether the original automaton includes a deadlock state. This is used
     * to avoid checking the deadlock conditions when not needed.
     */
    private boolean mHasDeadlockState;
    /**
     * Search space used by the breadth-first search.
     */
    private final BFSSearchSpace<SearchRecord> mSearchSpace;
    /**
     * Record that starts the next level of breadth-first search.
     */
    private SearchRecord mStartOfNextLevel;
    /**
     * A search record that qualifies to end the search which is not a deadlock
     * state. If a non-deadlock end state is encountered, the search continues
     * within the same depth level to find a deadlock state in the hope of
     * producing a better counterexample. After the search, a <CODE>null</CODE>
     * value of this variable indicates that a deadlock state has been found
     * and the concrete trace does not need to include all events in
     * {@link #mEventSequence}.
     */
    private SearchRecord mNonDeadlockTarget;
  }


  //#########################################################################
  //# Inner Class SearchRecord
  /**
   * <P>A search record to facilitate trace expansion for partition-based
   * abstraction. The search record contains two additional flags to support
   * the special behaviour needed for conflict trace expansion.</P>
   * <DL>
   * <DT>Target state reached</DT>
   * <DD>to record whether the trace associated with the search record has
   * passed through a state in the equivalence class of the end state of the
   * abstract trace after consuming all events.</DD>
   * <DT>End state reached</DT>
   * <DD>to record, when a target state is reached, whether the state
   * associated with the search record has the precondition marking.</DD>
   * </DL>
   *
   * @author Robi Malik
   */
  private static class SearchRecord extends TRTraceSearchRecord
  {
    //#######################################################################
    //# Constructors
    private SearchRecord(final TraceExpander expander,
                         final int state)
    {
      super(state);
      mTargetClassReached = expander.isTargetState(state, 0);
      mEndStateReached =
        mTargetClassReached && expander.isRelevantPreconditionMarkedState(state);
    }

    private SearchRecord(final TraceExpander expander,
                         final int state,
                         final SearchRecord pred,
                         final int event)
    {
      super(state, pred, event, expander.isLocalEvent(event) ? 0 : 1);
      final int consumed = getNumberOfConsumedEvents();
      mTargetClassReached =
        pred.mTargetClassReached || expander.isTargetState(state, consumed);
      mEndStateReached =
        mTargetClassReached && expander.isRelevantPreconditionMarkedState(state);
    }

    //#######################################################################
    //# Simple Access
    private boolean isEndStateReached()
    {
      return mEndStateReached;
    }

    //#########################################################################
    //# Overrides for java.lang.Object
    @Override
    public boolean equals(final Object other)
    {
      if (super.equals(other)) {
        final SearchRecord record = (SearchRecord) other;
        return mTargetClassReached == record.mTargetClassReached;
      } else {
        return false;
      }
    }

    @Override
    public int hashCode()
    {
      int result = 5 * super.hashCode();
      if (mTargetClassReached) {
        result++;
      }
      return result;
    }

    //#######################################################################
    //# Data Members
    /**
     * Whether the trace associated with the search record has passed through
     * a state in the equivalence class of the end state of the abstract trace
     * after consuming all events.
     */
    private final boolean mTargetClassReached;
    /**
     * Whether the state associated with the search record has the precondition
     * marking. This flag is <CODE>false</CODE> if {@link #mTargetClassReached}
     * is <CODE>false</CODE>, otherwise it is <CODE>true</CODE> if and only
     * if the state has the relevant precondition marking. This flag is
     * not included in {@link #equals(Object) equals()} or {@link #hashCode()}
     * because it is functionally dependent on other attributes.
     */
    private final boolean mEndStateReached;
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
