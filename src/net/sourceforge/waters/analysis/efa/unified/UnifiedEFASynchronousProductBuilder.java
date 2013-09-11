//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFA Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efa.unified
//# CLASS:   UnifiedEFASynchronousProductBuilder
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efa.unified;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.analysis.efa.base.AbstractEFAAlgorithm;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.IntArrayBuffer;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.PreTransitionBuffer;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.xsd.base.ComponentKind;


/**
 * @author Robi Malik, Sahar Mohajerani
 */

public class UnifiedEFASynchronousProductBuilder extends AbstractEFAAlgorithm
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


  //#########################################################################
  //# Invocation
  @Override
  protected void setUp() throws AnalysisException
  {
    super.setUp();
  }

  public void run() throws AnalysisException, EvalException
  {
    try {
      setUp();
      createEventEncoding();
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
    final Map<AbstractEFAEvent, EventInfo> renamingMap = new HashMap<>();
    final List<AbstractEFAEvent> orderedEvents = new ArrayList<>();
    int trIndex = 0;
    for (final UnifiedEFATransitionRelation tr : mInputTransitionRelations) {
      final UnifiedEFAEventEncoding encoding = tr.getEventEncoding();
      for (int e=EventEncoding.NONTAU; e<encoding.size(); e++) {
        final AbstractEFAEvent event = encoding.getEvent(e);
        EventInfo eventInfo = renamingMap.get(event);
        if (eventInfo == null) {
          eventInfo = new EventInfo(event);
          renamingMap.put(event, eventInfo);
          orderedEvents.add(event);
        }
        final EventTRInfo trInfo = new EventTRInfo(trIndex, e);
        eventInfo.addEventTRInfo(trInfo);
      }
      trIndex++;
    }
    // Step 2: Find ancestors.
    for (final AbstractEFAEvent event : orderedEvents) {
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
    mEventEncoding = new UnifiedEFAEventEncoding("Sahar");
    mEventInfoList = new ArrayList<>();
    final AbstractEFAEvent tau = mEventEncoding.getEvent(EventEncoding.TAU);
    final EventInfo tauInfo = new EventInfo(tau);
    mEventInfoList.add(tauInfo);
    for (final AbstractEFAEvent event : orderedEvents) {
      final EventInfo info = renamingMap.get(event);
      if (!info.isRenamed()) {
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

  private void exploreSynchronousProduct() throws OverflowException
  {
    mUsesMarking = false;
    mUsesTau = false;
    mPreTransitionBuffer = new PreTransitionBuffer(mEventEncoding.size());
    mStateSpace = new IntArrayBuffer(mInputTransitionRelations.size());
    final int[] sourceTuple = new int[mInputTransitionRelations.size()];
    createInitialStates(sourceTuple, 0);
    mNumberOfInitialStates = mStateSpace.size();
    mTransitionIterators = new TransitionIterator[mInputTransitionRelations.size()];
    for (int trIndex = 0; trIndex < mInputTransitionRelations.size(); trIndex++) {
      final UnifiedEFATransitionRelation tr = mInputTransitionRelations.get(trIndex);
      final ListBufferTransitionRelation rel = tr.getTransitionRelation();
      mUsesMarking |= rel.isUsedProposition(UnifiedEFAEventEncoding.OMEGA);
      final byte tauStatus = rel.getProperEventStatus(EventEncoding.TAU);
      mUsesTau |= EventEncoding.isUsedEvent(tauStatus);
      mTransitionIterators[trIndex] = rel.createSuccessorsReadOnlyIterator();
    }
    final int[] targetTuple = new int[mInputTransitionRelations.size()];
    for (int stateNumber = 0; stateNumber < mStateSpace.size(); stateNumber++) {
      mStateSpace.getContents(stateNumber, sourceTuple);
      if (mUsesTau) {
        mStateSpace.getContents(stateNumber, targetTuple);
        for (int trIndex = 0; trIndex < mInputTransitionRelations.size(); trIndex++) {
          targetTuple[trIndex] = sourceTuple[trIndex];
          final TransitionIterator iter = mTransitionIterators[trIndex];
          iter.reset(sourceTuple[trIndex], EventEncoding.TAU);
          while (iter.advance()) {
            targetTuple[trIndex] = iter.getCurrentTargetState();
            final int target = mStateSpace.add(targetTuple);
            mPreTransitionBuffer.addTransition
              (stateNumber, EventEncoding.TAU, target);
          }
        }
      }
      for (int e = EventEncoding.NONTAU; e < mEventEncoding.size(); e++) {
        mStateSpace.getContents(stateNumber, targetTuple);
        final EventInfo eventInfo = mEventInfoList.get(e);
        final List<EventTRInfo> infoList = eventInfo.getEventTRInfo();
        createSuccessorStates(stateNumber, e, sourceTuple, targetTuple, infoList, 0);
      }
    }
  }

  private void createInitialStates(final int[] tuple, final int trIndex)
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

  private void createSuccessorStates(final int source, final int event,
                                     final int[] sourceTuple,
                                     final int[] targetTuple,
                                     final List<EventTRInfo> infoList,
                                     final int index)
  throws OverflowException
  {
    if (index < infoList.size()) {
      final EventTRInfo info = infoList.get(index);
      final int trIndex = info.getTRIndex();
      final int eventCode = info.getEventCode();
      final TransitionIterator iter = mTransitionIterators[trIndex];
      iter.reset(sourceTuple[trIndex], eventCode);
      while (iter.advance()) {
        targetTuple[trIndex] = iter.getCurrentTargetState();
        createSuccessorStates(source, event, sourceTuple, targetTuple,
                              infoList, index+1);
      }
    } else {
      final int target = mStateSpace.add(targetTuple);
      mPreTransitionBuffer.addTransition(source, event, target);
    }
  }

  private void createTransitionRelation() throws OverflowException
  {
    final int numPropositions = mUsesMarking ? 1 : 0;
    final ListBufferTransitionRelation resultRel =
      new ListBufferTransitionRelation("Robi", ComponentKind.PLANT,
                                       mEventEncoding.size(), numPropositions,
                                       mStateSpace.size(),
                                       ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    if (!mUsesTau) {
      resultRel.setProperEventStatus(EventEncoding.TAU,
                                     EventEncoding.STATUS_FULLY_LOCAL |
                                     EventEncoding.STATUS_UNUSED);
    }
    for (int i = 0; i < mNumberOfInitialStates; i++) {
      resultRel.setInitial(i, true);
    }
    if (mUsesMarking) {
      final int[] tuple = new int[mInputTransitionRelations.size()];
      for (int i=0; i<mStateSpace.size(); i++) {
        mStateSpace.getContents(i, tuple);
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
          resultRel.setMarked(i, UnifiedEFAEventEncoding.OMEGA, true);
        }
      }
    }
    mPreTransitionBuffer.addOutgoingTransitions(resultRel);
    mSynchronousProduct =
      new UnifiedEFATransitionRelation(resultRel, mEventEncoding);
  }

  //#########################################################################
  //# Inner Class
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
    @SuppressWarnings("unused")
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
    //# Data Members
    private final AbstractEFAEvent mEvent;
    private int mEventCode;
    private final List<EventTRInfo> mEventTRInfo;
    private boolean mRenamed;
  }


  //#########################################################################
  //# Inner Class
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
    //# Data Members
    private final int mTRIndex;
    private final int mEventCode;
  }


  //#########################################################################
  //# Data Members
  private UnifiedEFATransitionRelation mSynchronousProduct;
  private List<UnifiedEFATransitionRelation> mInputTransitionRelations;
  private UnifiedEFAEventEncoding mEventEncoding;
  private IntArrayBuffer mStateSpace;
  private int mNumberOfInitialStates;
  private List<EventInfo> mEventInfoList;
  private boolean mUsesMarking;
  private boolean mUsesTau;
  private TransitionIterator[] mTransitionIterators;
  private PreTransitionBuffer mPreTransitionBuffer;

}
