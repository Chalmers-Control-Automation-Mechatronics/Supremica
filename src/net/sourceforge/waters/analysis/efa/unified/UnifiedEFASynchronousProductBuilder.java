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

package net.sourceforge.waters.analysis.efa.unified;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.analysis.efa.base.AbstractEFAAlgorithm;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.IntArrayBuffer;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.PreTransitionBuffer;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.base.ComponentKind;


/**
 * @author Robi Malik, Sahar Mohajerani
 */

public class UnifiedEFASynchronousProductBuilder
  extends AbstractEFAAlgorithm
{

  //#########################################################################
  //# Constructors
  public UnifiedEFASynchronousProductBuilder()
  {
  }


  //#########################################################################
  //# Configuration
  public void setInputTransitionRelations
    (final List<UnifiedEFATransitionRelation> inputs)
  {
    mInputTransitionRelations = inputs;
  }

  public List<UnifiedEFATransitionRelation> getInputTransitionRelations()
  {
    return mInputTransitionRelations;
  }

  public void setStateLimit(final int limit)
  {
    mStateLimit = limit;
  }

  public int getStateLimit()
  {
    return mStateLimit;
  }

  public void setTransitionLimit(final int limit)
  {
    mTransitionLimit = limit;
  }

  public int getTransitionLimit()
  {
    return mTransitionLimit;
  }


  //#########################################################################
  //# Invocation
  @Override
  protected void setUp()
    throws AnalysisException
  {
    super.setUp();
    configureTransitionRelations();
    createEventEncoding();
    createTransitionIterators();
  }

  public void run()
    throws AnalysisException
  {
    try {
      setUp();
      exploreSynchronousProduct();
      createTransitionRelation();
    } finally {
      tearDown();
    }
  }

  @Override
  protected void tearDown()
  {
    super.tearDown();
    mEventEncoding = null;
    mEventInfoList = null;
    mTransitionIterators = null;
    mStateSpace = null;
    mPreTransitionBuffer = null;
    mDeadlockInfo = null;
  }

  public UnifiedEFATransitionRelation getSynchronousProduct()
  {
    return mSynchronousProduct;
  }


  //#########################################################################
  //# Auxiliary Methods
  private void createEventEncoding()
  {
    // Step 1: Collect event information.
    final Map<AbstractEFAEvent,EventInfo> renamingMap = new HashMap<>();
    final List<EventInfo> orderedEvents = new ArrayList<>();
    int trIndex = 0;
    for (final UnifiedEFATransitionRelation tr : mInputTransitionRelations) {
      final UnifiedEFAEventEncoding encoding = tr.getEventEncoding();
      for (int e = EventEncoding.NONTAU; e < encoding.size(); e++) {
        if (tr.isUsedEvent(e)) {
          final AbstractEFAEvent event = encoding.getEvent(e);
          EventInfo eventInfo = renamingMap.get(event);
          if (eventInfo == null) {
            eventInfo = new EventInfo(event);
            renamingMap.put(event, eventInfo);
            orderedEvents.add(eventInfo);
          }
          final EventTRInfo trInfo = new EventTRInfo(trIndex, e);
          eventInfo.addEventTRInfo(trInfo);
        }
      }
      trIndex++;
    }
    // Step 2: Find ancestors.
    for (final EventInfo info : orderedEvents) {
      final AbstractEFAEvent event = info.getEvent();
      AbstractEFAEvent original = event.getOriginalEvent();
      while (original != null) {
        final EventInfo originalInfo = renamingMap.get(original);
        if (originalInfo != null) {
          originalInfo.setRenamed();
        }
        original = original.getOriginalEvent();
      }
    }
    // Step 3: Create event encoding.
    final String name = getOutputName();
    mEventEncoding = new UnifiedEFAEventEncoding(name);
    mEventInfoList = new ArrayList<>();
    final AbstractEFAEvent tau = mEventEncoding.getEvent(EventEncoding.TAU);
    final EventInfo tauInfo = new EventInfo(tau);
    mEventInfoList.add(tauInfo);
    for (final EventInfo info : orderedEvents) {
      if (!info.isRenamed()) {
        final AbstractEFAEvent event = info.getEvent();
        final int code = mEventEncoding.createEventId(event);
        mEventInfoList.add(info);
        info.setEventCode(code);
        AbstractEFAEvent original = event.getOriginalEvent();
        while (original != null) {
          final EventInfo originalInfo = renamingMap.get(original);
          if (originalInfo != null) {
            info.merge(originalInfo);
          }
          original = original.getOriginalEvent();
        }
      }
    }
  }

  private void createTransitionIterators()
  {
    final int numTR = mInputTransitionRelations.size();
    mTransitionIterators = new TransitionIterator[numTR];
    mDeadlockInfo = new boolean[numTR][];
    mUsesMarking = false;
    mUsesTau = false;
    for (int trIndex = 0; trIndex < numTR; trIndex++) {
      final UnifiedEFATransitionRelation tr =
        mInputTransitionRelations.get(trIndex);
      final ListBufferTransitionRelation rel = tr.getTransitionRelation();
      if (rel.isPropositionUsed(UnifiedEFAEventEncoding.OMEGA)) {
        mUsesMarking = true;
        final int numStates = rel.getNumberOfStates();
        final boolean[] info = new boolean[numStates];
        for (int s = 0; s < numStates; s++) {
          info[s] = rel.isDeadlockState(s, UnifiedEFAEventEncoding.OMEGA);
        }
        mDeadlockInfo[trIndex] = info;
      }
      final byte tauStatus = rel.getProperEventStatus(EventEncoding.TAU);
      mUsesTau |= EventStatus.isUsedEvent(tauStatus);
      mTransitionIterators[trIndex] = rel.createSuccessorsReadOnlyIterator();
    }
    mPreTransitionBuffer =
      new PreTransitionBuffer(mEventEncoding.size(), mTransitionLimit);
    mDumpStateIndex = -1;
  }

  private void configureTransitionRelations()
  {
    for (final UnifiedEFATransitionRelation tr : mInputTransitionRelations) {
      final ListBufferTransitionRelation rel = tr.getTransitionRelation();
      rel.reconfigure(ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    }
  }

  private void exploreSynchronousProduct() throws AnalysisException
  {
    final int numTR = mInputTransitionRelations.size();
    mStateSpace = new IntArrayBuffer(numTR, mStateLimit);
    final int[] sourceTuple = new int[numTR];
    createInitialStates(sourceTuple, 0);
    mNumberOfInitialStates = mStateSpace.size();
    final int[] targetTuple = new int[numTR];
    for (int stateNumber = 0; stateNumber < mStateSpace.size(); stateNumber++) {
      if (stateNumber == mDumpStateIndex) {
        continue;
      }
      mStateSpace.getContents(stateNumber, sourceTuple);
      if (mUsesTau) {
        System.arraycopy(sourceTuple, 0, targetTuple, 0, numTR);
        for (int trIndex = 0; trIndex < numTR; trIndex++) {
          targetTuple[trIndex] = sourceTuple[trIndex];
          final TransitionIterator iter = mTransitionIterators[trIndex];
          iter.reset(sourceTuple[trIndex], EventEncoding.TAU);
          while (iter.advance()) {
            final int t = iter.getCurrentTargetState();
            final int target;
            // TODO Make deadlock pruning optional?
            if (mDeadlockInfo[trIndex] != null && mDeadlockInfo[trIndex][t]) {
              target = createDumpState();
            } else {
              targetTuple[trIndex] = t;
              target = mStateSpace.add(targetTuple);
            }
            mPreTransitionBuffer.addTransition
              (stateNumber, EventEncoding.TAU, target);
          }
        }
      }
      for (int e = EventEncoding.NONTAU; e < mEventEncoding.size(); e++) {
        mStateSpace.getContents(stateNumber, targetTuple);
        final EventInfo eventInfo = mEventInfoList.get(e);
        final List<EventTRInfo> infoList = eventInfo.getEventTRInfo();
        createSuccessorStates(stateNumber, sourceTuple, e,
                              targetTuple, infoList, 0);
      }
    }
  }

  private void createInitialStates(final int[] tuple, final int trIndex)
    throws OverflowException
  {
    if (trIndex < tuple.length) {
      final ListBufferTransitionRelation rel =
        mInputTransitionRelations.get(trIndex).getTransitionRelation();
      final int numStates = rel.getNumberOfStates();
      for (int s = 0; s < numStates; s++) {
        if (rel.isInitial(s)) {
          tuple[trIndex] = s;
          createInitialStates(tuple, trIndex + 1);
        }
      }
    } else {
      mStateSpace.add(tuple);
    }
  }

  private int createDumpState()
    throws OverflowException
  {
    if (mDumpStateIndex < 0) {
      final int numTR = mInputTransitionRelations.size();
      final int[] tuple = new int[numTR];
      for (int trIndex = 0; trIndex < numTR; trIndex++) {
        final ListBufferTransitionRelation rel =
          mInputTransitionRelations.get(trIndex).getTransitionRelation();
        tuple[trIndex] = rel.getDumpStateIndex();
      }
      mDumpStateIndex = mStateSpace.add(tuple);
    }
    return mDumpStateIndex;
  }

  private void createSuccessorStates(final int source,
                                     final int[] sourceTuple,
                                     final int event,
                                     final int[] targetTuple,
                                     final List<EventTRInfo> infoList,
                                     final int index)
    throws AnalysisException
  {
    if (index < infoList.size()) {
      final EventTRInfo info = infoList.get(index);
      final int trIndex = info.getTRIndex();
      final int eventCode = info.getEventCode();
      final TransitionIterator iter = mTransitionIterators[trIndex];
      iter.reset(sourceTuple[trIndex], eventCode);
      while (iter.advance()) {
        if (targetTuple == null) {
          createSuccessorStates(source, sourceTuple, event,
                                null, infoList, index + 1);
          continue;
        }
        final int target = iter.getCurrentTargetState();
        // TODO Make deadlock pruning optional?
        if (mDeadlockInfo[trIndex] != null && mDeadlockInfo[trIndex][target]) {
          createSuccessorStates(source, sourceTuple, event,
                                null, infoList, index + 1);
        } else {
          targetTuple[trIndex] = target;
          createSuccessorStates(source, sourceTuple, event,
                                targetTuple, infoList, index + 1);
        }
      }
    } else {
      checkAbort();
      final int target =
        targetTuple == null ? createDumpState() : mStateSpace.add(targetTuple);
      mPreTransitionBuffer.addTransition(source, event, target);
    }
  }

  private void createTransitionRelation() throws OverflowException
  {
    final String name = getOutputName();
    final ComponentKind kind = getOutputKind();
    final int numPropositions = mUsesMarking ? 1 : 0;
    final int config = ListBufferTransitionRelation.CONFIG_SUCCESSORS;
    final ListBufferTransitionRelation resultRel;
    if (mDumpStateIndex < 0) {
      resultRel = new ListBufferTransitionRelation
        (name, kind, mEventEncoding.size(), numPropositions,
         mStateSpace.size(), config);
    } else {
      resultRel = new ListBufferTransitionRelation
        (name, kind, mEventEncoding.size(), numPropositions,
         mStateSpace.size(), mDumpStateIndex, config);
    }
    if (!mUsesTau) {
      resultRel.setProperEventStatus(EventEncoding.TAU,
                                     EventStatus.STATUS_FULLY_LOCAL |
                                     EventStatus.STATUS_UNUSED);
    }
    for (int e = EventEncoding.NONTAU; e < mEventEncoding.size(); e++) {
      final EventInfo eventInfo = mEventInfoList.get(e);
      final EventTRInfo trInfo = eventInfo.getEventTRInfo().get(0);
      final int trIndex = trInfo.getTRIndex();
      final UnifiedEFATransitionRelation tr =
        mInputTransitionRelations.get(trIndex);
      final ListBufferTransitionRelation rel = tr.getTransitionRelation();
      final int eventCode = trInfo.getEventCode();
      final byte status = rel.getProperEventStatus(eventCode);
      resultRel.setProperEventStatus(e, status);
    }
    for (int i = 0; i < mNumberOfInitialStates; i++) {
      resultRel.setInitial(i, true);
    }
    if (mUsesMarking) {
      final int[] tuple = new int[mInputTransitionRelations.size()];
      for (int s = 0; s < mStateSpace.size(); s++) {
        mStateSpace.getContents(s, tuple);
        boolean marked = true;
        for (int trIndex = 0; trIndex < tuple.length; trIndex++) {
          final UnifiedEFATransitionRelation tr =
            mInputTransitionRelations.get(trIndex);
          final ListBufferTransitionRelation rel = tr.getTransitionRelation();
          if (!rel.isMarked(tuple[trIndex], UnifiedEFAEventEncoding.OMEGA)) {
            marked= false;
            break;
          }
        }
        if (marked) {
          resultRel.setMarked(s, UnifiedEFAEventEncoding.OMEGA, true);
        }
      }
    }
    mPreTransitionBuffer.addOutgoingTransitions(resultRel);
    mSynchronousProduct =
      new UnifiedEFATransitionRelation(resultRel, mEventEncoding);
  }

  private String getOutputName()
  {
    final int numTR = mInputTransitionRelations.size();
    final StringBuilder buffer = new StringBuilder("{");
    boolean first = true;
    for (int trIndex = 0; trIndex < numTR; trIndex++) {
      if (first) {
        first = false;
      } else {
        buffer.append(',');
      }
      final UnifiedEFATransitionRelation tr =
        mInputTransitionRelations.get(trIndex);
      final String name = tr.getName();
      buffer.append(name);
    }
    buffer.append('}');
    return buffer.toString();
  }

  private ComponentKind getOutputKind()
  {
    final int numTR = mInputTransitionRelations.size();
    if (numTR == 0) {
      return ComponentKind.PLANT;
    } else {
      ComponentKind result = null;
      for (int trIndex = 0; trIndex < numTR; trIndex++) {
        final UnifiedEFATransitionRelation tr =
          mInputTransitionRelations.get(trIndex);
        final ListBufferTransitionRelation rel = tr.getTransitionRelation();
        final ComponentKind kind = rel.getKind();
        if (result == null || kind.compareTo(result) < 0) {
          result = kind;
        }
      }
      return result;
    }
  }


  //#########################################################################
  //# Inner Class EventInfo
  private class EventInfo
  {
    //#######################################################################
    //# Constructor
    private EventInfo(final AbstractEFAEvent event)
    {
      mEvent = event;
      mEventCode = -1;
      mEventTRInfo = new ArrayList<>(mInputTransitionRelations.size());
    }

    //#######################################################################
    //# Simple Access
    private AbstractEFAEvent getEvent()
    {
      return mEvent;
    }

    @SuppressWarnings("unused")
    private int getEventCode()
    {
      return mEventCode;
    }

    private void setEventCode(final int code)
    {
      mEventCode = code;
    }

    private List<EventTRInfo> getEventTRInfo()
    {
      return mEventTRInfo;
    }

    private void addEventTRInfo(final EventTRInfo info)
    {
      mEventTRInfo.add(info);
    }

    private boolean isRenamed()
    {
      return mRenamed;
    }

    private void setRenamed()
    {
      mRenamed = true;
    }

    private void merge(final EventInfo info)
    {
      mEventTRInfo.addAll(info.mEventTRInfo);
    }

    //#######################################################################
    //# Debugging
    @Override
    public String toString()
    {
      final StringWriter writer = new StringWriter();
      dump(writer);
      return writer.toString();
    }

    private void dump(final StringWriter writer)
    {
      writer.write(mEvent.getName());
      writer.write("@");
      writer.write(Integer.toString(mEventCode));
      if (mRenamed) {
        writer.write(" (renamed)");
      }
      writer.write("\n");
      for (final EventTRInfo info : mEventTRInfo) {
        info.dump(writer);
        writer.write("\n");
      }
    }

    //#######################################################################
    //# Data Members
    private final AbstractEFAEvent mEvent;
    private int mEventCode;
    private final List<EventTRInfo> mEventTRInfo;
    private boolean mRenamed;
  }


  //#########################################################################
  //# Inner Class EventTRInfo
  private class EventTRInfo
  {
    //#######################################################################
    //# Constructor
    private EventTRInfo(final int trIndex, final int eventCode)
    {
      mTRIndex = trIndex;
      mEventCode = eventCode;
    }

    //#######################################################################
    //# Simple Access
    private int getTRIndex()
    {
      return mTRIndex;
    }

    private int getEventCode()
    {
      return mEventCode;
    }

    //#######################################################################
    //# Debugging
    @Override
    public String toString()
    {
      final StringWriter writer = new StringWriter();
      dump(writer);
      return writer.toString();
    }

    private void dump(final StringWriter writer)
    {
      final UnifiedEFATransitionRelation tr =
        mInputTransitionRelations.get(mTRIndex);
      writer.write(tr.getName());
      writer.write("@");
      writer.write(Integer.toString(mTRIndex));
      writer.write(" : ");
      final UnifiedEFAEventEncoding enc = tr.getEventEncoding();
      final AbstractEFAEvent event = enc.getEvent(mEventCode);
      writer.write(event.getName());
      writer.write("@");
      writer.write(Integer.toString(mEventCode));
    }

    //#######################################################################
    //# Data Members
    private final int mTRIndex;
    private final int mEventCode;
  }


  //#########################################################################
  //# Data Members
  private List<UnifiedEFATransitionRelation> mInputTransitionRelations;
  private int mStateLimit = Integer.MAX_VALUE;
  private int mTransitionLimit = Integer.MAX_VALUE;

  private UnifiedEFATransitionRelation mSynchronousProduct;

  private UnifiedEFAEventEncoding mEventEncoding;
  private List<EventInfo> mEventInfoList;
  private int mNumberOfInitialStates;
  private boolean mUsesMarking;
  private boolean mUsesTau;
  private TransitionIterator[] mTransitionIterators;
  private boolean[][] mDeadlockInfo;
  private IntArrayBuffer mStateSpace;
  private PreTransitionBuffer mPreTransitionBuffer;
  private int mDumpStateIndex;

}
