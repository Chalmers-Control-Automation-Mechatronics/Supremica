//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.analysis.monolithic.TRAbstractSynchronousProductBuilder;
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

import org.apache.logging.log4j.Logger;


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
                        final ProductDESProxyFactory factory,
                        final TRAbstractSynchronousProductBuilder builder,
                        final TRSynchronousProductResult result)
  {
    super(result.getComputedAutomaton().getName());
    mPredecessors = preds;
    mEventEncoding = enc;
    mFactory = factory;
    mSynchronousProductBuilder = builder;
    final TRAutomatonProxy outputAut = result.getComputedAutomaton();
    final ListBufferTransitionRelation rel = outputAut.getTransitionRelation();
    mDumpStateIndex = rel.getDumpStateIndex();
  }


  //#########################################################################
  //# Simple Access
  EventEncoding getEventEncoding()
  {
    return mEventEncoding;
  }


  //#########################################################################
  //# Debugging
  @Override
  public void reportExpansion()
  {
    final Logger logger = getLogger();
    if (logger.isDebugEnabled()) {
      logger.debug("Expanding synchronous composition of " +
                   getName() + " ...");
    }
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
      final TRAutomatonProxy aut = pred.getOutputAutomaton
        (ListBufferTransitionRelation.CONFIG_SUCCESSORS);
      automata.add(aut);
    }
    reportRebuilding();
    final TRCandidate candidate = new TRCandidate(automata, mEventEncoding);
    final ProductDESProxy des = candidate.createProductDESProxy(mFactory);
    final EventEncoding syncEncoding =
      candidate.createSyncEventEncoding();
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
  void provideOutputAutomaton(final TRAutomatonProxy outputAut)
  {
    // Can't receive output automaton from outside because of missing state
    // map. Must use createOutputAutomaton() to rebuild if needed.
  }

  @Override
  void clearOutputAutomaton()
  {
    super.clearOutputAutomaton();
    mStateMap = null;
  }

  @Override
  public void expandTrace(final TRTraceProxy trace,
                          final AbstractTRCompositionalModelAnalyzer analyzer)
    throws AnalysisException
  {
    // Ensure mDumpStateIndex and mStateMap are available ...
    getOutputAutomaton(ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    assert mStateMap != null;
    // Set up ...
    final List<EventProxy> events = trace.getEvents();
    final int numSteps = events.size() + 1;
    final int numAutomata = mPredecessors.size();
    final TransitionFinder[] finders = new TransitionFinder[numAutomata];
    for (int autIndex = 0; autIndex < numAutomata; autIndex++) {
      final TRAbstractionStep pred = mPredecessors.get(autIndex);
      final TRAutomatonProxy aut =
        pred.getOutputAutomaton(ListBufferTransitionRelation.CONFIG_SUCCESSORS);
      finders[autIndex] = new TransitionFinder(aut, numSteps);
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
      final int numEvents = mEventEncoding.getNumberOfProperEvents();
      final List<EventProxy> localEvents = new ArrayList<>(numEvents);
      for (int autIndex = 0; autIndex < numAutomata; autIndex++) {
        final EventProxy event = finders[autIndex].getTauEvent();
        if (event != null) {
          localEvents.add(event);
        }
      }
      for (int e = EventEncoding.NONTAU; e < numEvents; e++) {
        final byte status = mEventEncoding.getProperEventStatus(e);
        if (EventStatus.isUsedEvent(status) &&
            EventStatus.isLocalEvent(status)) {
          final EventProxy event = mEventEncoding.getProperEvent(e);
          localEvents.add(event);
        }
      }
      // Store event steps ...
      int stepIndex = 1;
      for (final EventProxy event : events) {
        final int e = mEventEncoding.getEventCode(event);
        final boolean failingEvent;
        if (e >= 0) {
          final byte status = mEventEncoding.getProperEventStatus(e);
          failingEvent = EventStatus.isFailingEvent(status);
        } else {
          failingEvent = false;
        }
        final List<EventProxy> alternatives =
          e == EventEncoding.TAU ? localEvents : Collections.singletonList(event);
        final int state = trace.getState(this, stepIndex);
        if (state != mDumpStateIndex) {
          mStateMap.getOriginalState(state, tuple);
        }
        boolean found = false;
        eventLoop:
        for (final EventProxy alt : alternatives) {
          analyzer.checkAbort();
          found = state != mDumpStateIndex || failingEvent;
          for (int autIndex = 0; autIndex < numAutomata; autIndex++) {
            final TransitionFinder finder = finders[autIndex];
            if (state == mDumpStateIndex) {
              final int target = finder.storeDumpStateTransition(stepIndex, alt);
              if (target < 0) {
                found = false;
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
            events.set(stepIndex - 1, alt);
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
                             final int numSteps)
    {
      mEventEncoding = aut.getEventEncoding();
      mTransitionRelation = aut.getTransitionRelation();
      mHasDefaultMarking = mTransitionRelation.isPropositionUsed
        (TRCompositionalConflictChecker.DEFAULT_MARKING);
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
      if (mHasDefaultMarking) {
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
        if (!mHasDefaultMarking || isDumpState(target)) {
          return mStateSequence[stepIndex] = target;
        } else if (foundTarget < 0) {
          foundTarget = target;
        }
      }
      return mStateSequence[stepIndex] = foundTarget;
    }

    private boolean isDumpState(final int state)
    {
      if (!mHasDefaultMarking ||
          mTransitionRelation.isMarked
            (state, TRCompositionalConflictChecker.DEFAULT_MARKING)) {
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
    private final boolean mHasDefaultMarking;
    private final TransitionIterator mTransitionIterator;
    private TransitionIterator mDumpIterator;
    private final int[] mStateSequence;
  }


  //#########################################################################
  //# Data Members
  private final List<TRAbstractionStep> mPredecessors;
  private final EventEncoding mEventEncoding;
  private final ProductDESProxyFactory mFactory;
  private final TRAbstractSynchronousProductBuilder mSynchronousProductBuilder;

  private int mDumpStateIndex;
  private TRSynchronousProductStateMap mStateMap;

}
