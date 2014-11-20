//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.trcomp
//# CLASS:   TRAbstractionStepInput
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.trcomp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.analysis.monolithic.TRSynchronousProductBuilder;
import net.sourceforge.waters.analysis.monolithic.TRSynchronousProductResult;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.analysis.tr.TRSynchronousProductStateMap;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * An abstraction step representing synchronous composition.
 *
 * @author Robi Malik
 */

class TRAbstractionStepSync
  extends TRAbstractionStep
{

  //#########################################################################
  //# Constructor
  TRAbstractionStepSync(final List<TRAbstractionStep> preds,
                        final EventEncoding enc,
                        final EventProxy defaultMarking,
                        final EventProxy preconditionMarking,
                        final ProductDESProxyFactory factory,
                        final TRSynchronousProductBuilder builder)
  {
    mPredecessors = preds;
    mEventEncoding = enc;
    mDefaultMarking = defaultMarking;
    mPreconditionMarking = preconditionMarking;
    mFactory = factory;
    mSynchronousProductBuilder = builder;
    for (final TRAbstractionStep pred : preds) {
      pred.setSuccessor(this);
    }
  }


  //#########################################################################
  //# Simple Access
  EventEncoding getEventEncoding()
  {
    return mEventEncoding;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.analysis.trcomp.TRAbstractionStep
  @Override
  public Collection<TRAbstractionStep> getPredecessors()
  {
    return mPredecessors;
  }

  @Override
  public TRAutomatonProxy createOutputAutomaton(final int preferredConfig)
    throws AnalysisException
  {
    final List<TRAutomatonProxy> automata =
      new ArrayList<>(mPredecessors.size());
    for (final TRAbstractionStep pred : mPredecessors) {
      final TRAutomatonProxy aut = pred.createOutputAutomaton
        (ListBufferTransitionRelation.CONFIG_SUCCESSORS);
      automata.add(aut);
    }
    final TRCandidate candidate = new TRCandidate(automata, mEventEncoding);
    final ProductDESProxy des = candidate.createProductDESProxy(mFactory);
    final EventEncoding syncEncoding =
      candidate.createSyncEventEncoding(mDefaultMarking, mPreconditionMarking);
    mSynchronousProductBuilder.setModel(des);
    mSynchronousProductBuilder.setEventEncoding(syncEncoding);
    mSynchronousProductBuilder.run();
    final TRSynchronousProductResult result =
      mSynchronousProductBuilder.getAnalysisResult();
    final TRAutomatonProxy aut = result.getComputedAutomaton();
    final ListBufferTransitionRelation rel = aut.getTransitionRelation();
    mDumpStateIndex = rel.getDumpStateIndex();
    mStateMap = result.getStateMap();
    return aut;
  }

  @Override
  public void expandTrace(final TRTraceProxy trace)
    throws AnalysisException
  {
    // Ensure mDumpState and mStateMap are available ...
    getOutputAutomaton(ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    assert mStateMap != null;
    // Set up ...
    final List<EventProxy> events = trace.getEvents();
    final int numSteps = events.size() + 1;
    final int numAutomata = mPredecessors.size();
    final TransitionFinder[] finders = new TransitionFinder[numAutomata];
    final int defaultMarking = mEventEncoding.getEventCode(mDefaultMarking);
    for (int autIndex = 0; autIndex < numAutomata; autIndex++) {
      final TRAbstractionStep pred = mPredecessors.get(autIndex);
      final TRAutomatonProxy aut =
        pred.getOutputAutomaton(ListBufferTransitionRelation.CONFIG_SUCCESSORS);
      finders[autIndex] = new TransitionFinder(aut, numSteps, defaultMarking);
    }
    // Store initial states ...
    final int[] tuple = new int[numAutomata];
    final int init = trace.getState(this, 0);
    if (init == mDumpStateIndex) {
      boolean gotDump = false;
      for (int autIndex = 0; autIndex < numAutomata; autIndex++) {
        gotDump |= finders[autIndex].storeInitialDumpState();
      }
      assert gotDump : "Did not find dump state component!";
    } else {
      mStateMap.getOriginalState(init, tuple);
      for (int autIndex = 0; autIndex < numAutomata; autIndex++) {
        finders[autIndex].storeInitialState(tuple[autIndex]);
      }
    }
    if (numSteps > 1) {
      // Find local events ...
      final int numEvents = mEventEncoding.getNumberOfEvents();
      final List<EventProxy> localEvents = new ArrayList<>(numEvents);
      for (int autIndex = 0; autIndex < numAutomata; autIndex++) {
        final EventProxy event = finders[autIndex].getTauEvent();
        if (event != null) {
          localEvents.add(event);
        }
      }
      for (int e = EventEncoding.NONTAU; e < numEvents; e++) {
        final byte status = mEventEncoding.getProperEventStatus(e);
        if (EventStatus.isLocalEvent(status)) {
          final EventProxy event = mEventEncoding.getProperEvent(e);
          localEvents.add(event);
        }
      }
      // Store event steps ...
      int stepIndex = 1;
      for (final EventProxy event : events) {
        final int e = mEventEncoding.getEventCode(event);
        final List<EventProxy> alternatives =
          e == EventEncoding.TAU ? localEvents : Collections.singletonList(event);
        final int state = trace.getState(this, stepIndex);
        if (state != mDumpStateIndex) {
          mStateMap.getOriginalState(state, tuple);
        }
        boolean found = false;
        eventLoop:
        for (final EventProxy alt : alternatives) {
          found = state != mDumpStateIndex;
          for (int autIndex = 0; autIndex < numAutomata; autIndex++) {
            final TransitionFinder finder = finders[autIndex];
            if (state == mDumpStateIndex) {
              final int target = finder.storeDumpStateTransition(stepIndex, alt);
              if (target < 0) {
                continue eventLoop;
              }
              found |= finder.isDumpState(target);
            } else {
              if (!finder.storeProperTransition(stepIndex, alt, tuple[autIndex])) {
                continue eventLoop;
              }
            }
          }
          if (found) {
            events.set(stepIndex, alt);
            break;
          }
        }
        assert found : "Transition not found!";
        stepIndex++;
      }
    }
    // Update trace ...
    trace.removeAutomaton(this);
    for (int autIndex = 0; autIndex < numAutomata; autIndex++) {
      final TRAbstractionStep pred = mPredecessors.get(autIndex);
      final int[] states = finders[autIndex].getStateSequence();
      trace.addAutomaton(pred, states);
    }
  }


  //#########################################################################
  //# Inner Class TransitionFinder
  private static class TransitionFinder
  {
    //#######################################################################
    //# Constructor
    private TransitionFinder(final TRAutomatonProxy aut,
                             final int numSteps,
                             final int defaultMarking)
    {
      mEventEncoding = aut.getEventEncoding();
      mTransitionRelation = aut.getTransitionRelation();
      mDefaultMarking =
        mTransitionRelation.isPropositionUsed(defaultMarking) ? defaultMarking : -1;
      mTransitionIterator = mTransitionRelation.createSuccessorsReadOnlyIterator();
      mDumpIterator = null;
      mStateSequence = new int[numSteps];
    }

    //#######################################################################
    //# Simple Access
    private int[] getStateSequence()
    {
      return mStateSequence;
    }

    //#######################################################################
    //# Algorithm
    private void storeInitialState(final int init)
    {
      mStateSequence[0] = init;
    }

    private boolean storeInitialDumpState()
    {
      if (mDefaultMarking >= 0) {
        int foundInit = -1;
        for (int s = 0; s < mTransitionRelation.getNumberOfStates(); s++) {
          if (mTransitionRelation.isInitial(s)) {
            if (isDumpState(s)) {
              mStateSequence[0] = s;
              return true;
            } else if (foundInit < 0) {
              foundInit = s;
            }
          }
        }
        assert foundInit >= 0 : "No initial state found!";
        mStateSequence[0] = foundInit;
        return false;
      } else {
        for (int s = 0; s < mTransitionRelation.getNumberOfStates(); s++) {
          if (mTransitionRelation.isInitial(s)) {
            mStateSequence[0] = s;
            return false;
          }
        }
        assert false : "No initial state found!";
        return false;
      }
    }

    private boolean storeProperTransition(final int stepIndex,
                                          final EventProxy event,
                                          final int target)
    {
      mStateSequence[stepIndex] = target;
      final int source = mStateSequence[stepIndex - 1];
      final int e = mEventEncoding.getEventCode(event);
      if (e < 0) {
        return source == target;
      }
      final byte status = mEventEncoding.getProperEventStatus(e);
      if (!EventStatus.isUsedEvent(status)) {
        return source == target;
      }
      mTransitionIterator.reset(source, e);
      while (mTransitionIterator.advance()) {
        if (mTransitionIterator.getCurrentTargetState() == target) {
          return true;
        }
      }
      return false;
    }

    private int storeDumpStateTransition(final int stepIndex,
                                         final EventProxy event)
    {
      final int source = mStateSequence[stepIndex - 1];
      final int e = mEventEncoding.getEventCode(event);
      if (e < 0) {
        return mStateSequence[stepIndex] = source;
      }
      final byte status = mEventEncoding.getProperEventStatus(e);
      if (!EventStatus.isUsedEvent(status)) {
        return mStateSequence[stepIndex] = source;
      }
      mTransitionIterator.reset(source, e);
      int foundTarget = -1;
      while (mTransitionIterator.advance()) {
        final int target = mTransitionIterator.getCurrentTargetState();
        if (mDefaultMarking < 0 || isDumpState(target)) {
          return mStateSequence[stepIndex] = target;
        } else if (foundTarget < 0) {
          foundTarget = target;
        }
      }
      return mStateSequence[stepIndex] = foundTarget;
    }

    private boolean isDumpState(final int state)
    {
      if (mDefaultMarking < 0 ||
          mTransitionRelation.isMarked(state, mDefaultMarking)) {
        return false;
      }
      if (mDumpIterator == null) {
        mDumpIterator = mTransitionRelation.createSuccessorsReadOnlyIterator();
      }
      mDumpIterator.resetState(state);
      return !mDumpIterator.advance();
    }

    private EventProxy getTauEvent()
    {
      final byte status = mEventEncoding.getProperEventStatus(EventEncoding.TAU);
      if (EventStatus.isUsedEvent(status)) {
        return mEventEncoding.getProperEvent(EventEncoding.TAU);
      } else {
        return null;
      }
    }

    //#######################################################################
    //# Data Members
    private final EventEncoding mEventEncoding;
    private final ListBufferTransitionRelation mTransitionRelation;
    private final int mDefaultMarking;
    private final TransitionIterator mTransitionIterator;
    private TransitionIterator mDumpIterator;
    private final int[] mStateSequence;
  }


  //#########################################################################
  //# Data Members
  private final List<TRAbstractionStep> mPredecessors;
  private final EventEncoding mEventEncoding;
  private final EventProxy mDefaultMarking;
  private final EventProxy mPreconditionMarking;
  private final ProductDESProxyFactory mFactory;
  private final TRSynchronousProductBuilder mSynchronousProductBuilder;

  private int mDumpStateIndex = -1;
  private TRSynchronousProductStateMap mStateMap;

}
