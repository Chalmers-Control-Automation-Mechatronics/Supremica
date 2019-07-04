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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.sourceforge.waters.analysis.abstraction.LimitedCertainConflictsTRSimplifier;
import net.sourceforge.waters.analysis.tr.AbstractStateBuffer;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.IntStateBuffer;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.des.AutomatonTools;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * An abstraction step representing certain conflicts simplification.
 *
 * @author Robi Malik
 */

class TRAbstractionStepCertainConflicts
  extends TRAbstractionStep
{

  //#########################################################################
  //# Constructor
  TRAbstractionStepCertainConflicts(final TRAbstractionStep pred,
                                    final EventEncoding eventEncoding,
                                    final LimitedCertainConflictsTRSimplifier simplifier)
  {
    super(pred.getName());
    mPredecessor = pred;
    mEventEncoding = eventEncoding;
    mSimplifier = simplifier;
    mLevels = simplifier.getLevels();
  }


  //#########################################################################
  //# Simple Access
  TRAbstractionStep getPredecessor()
  {
    return mPredecessor;
  }

  EventEncoding getEventEncoding()
  {
    return mEventEncoding;
  }

  int[] getLevels()
  {
    return mLevels;
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
    mSimplifier.setPreferredOutputConfiguration(preferredConfig);
    final int inputConfig = mSimplifier.getPreferredInputConfiguration();
    final EventEncoding inputEventEncoding = new EventEncoding(mEventEncoding);
    final TRAutomatonProxy inputAut =
      mPredecessor.getClonedOutputAutomaton(inputEventEncoding, inputConfig);
    final ListBufferTransitionRelation inputRel =
      inputAut.getTransitionRelation();
    final Logger logger = getLogger();
    reportRebuilding();
    inputRel.logSizes(logger);
    final ListBufferTransitionRelation outputRel =
      new ListBufferTransitionRelation(inputRel, inputEventEncoding, inputConfig);
    mSimplifier.setTransitionRelation(outputRel);
    mSimplifier.run();
    return new TRAutomatonProxy(inputEventEncoding, outputRel);
  }

  @Override
  public void expandTrace(final TRTraceProxy trace,
                          final AbstractTRCompositionalAnalyzer analyzer)
    throws AnalysisException
  {
    final long start = System.currentTimeMillis();
    final TRCompositionalConflictChecker conflictChecker =
      (TRCompositionalConflictChecker) analyzer;
    final TraceExpander expander = new TraceExpander(conflictChecker);
    expander.pruneTrace(trace);
    trace.replaceAutomaton(this, mPredecessor);
    int level = expander.getLevelOfEndState(trace);
    boolean created = false;
    if (level != 0 && level != 1) {
      int nextLevel = level < 0 ? Integer.MAX_VALUE : (level | 1) - 2;
      expander.createLanguageInclusionAutomata(trace, nextLevel, false);
      created = true;
      while (true) {
        final TRTraceProxy extension = expander.findTraceExtension();
        if (extension == null) {
          break;
        }
        final int numSteps = extension.getNumberOfSteps() - 2;
        trace.append(extension, numSteps);
        level = expander.getLevelOfEndState(trace);
        if (level < 2) {
          break;
        }
        nextLevel = (level | 1) - 2;
        expander.setNextLevel(trace, nextLevel);
      }
    }
    if (level >= 0 && (level & 1) != 0) {
      final int nextLevel = level - 1;
      if (created) {
        expander.setNextLevel(trace, nextLevel);
        expander.restrictToAlwaysEnabled();
      } else {
        expander.createLanguageInclusionAutomata(trace, nextLevel, true);
      }
      final TRTraceProxy extension = expander.findTraceExtension();
      if (extension != null) {
        final int numSteps = extension.getNumberOfSteps() - 2;
        trace.append(extension, numSteps);
      }
    }
    final long stop = System.currentTimeMillis();
    final int count = expander.getNumberOfLanguageInclusionChecks();
    conflictChecker.recordCCLanguageInclusionChecks(count, stop - start);
  }


  //#########################################################################
  //# Debugging
  @Override
  public void reportExpansion()
  {
    final Logger logger = getLogger();
    if (logger.isDebugEnabled()) {
      logger.debug("Expanding certain conflicts of " + getName() + " ...");
    }
  }


  //#########################################################################
  //# Inner Class TraceExpander
  private class TraceExpander
  {
    //#######################################################################
    //# Constructor
    private TraceExpander(final TRCompositionalConflictChecker checker)
      throws AnalysisException
    {
      mLanguageInclusionChecker = checker.getLanguageInclusionChecker();
      final int config =
        mLanguageInclusionChecker.getPreferredInputConfiguration();
      final ProductDESProxyFactory factory = checker.getFactory();
      mCertainConflictsEvent = factory.createEventProxy
        (CERTAIN_CONFLICT_EVENT_NAME, EventKind.UNCONTROLLABLE);
      final EventEncoding propertyEnc = new EventEncoding();
      propertyEnc.addProperEvent(mCertainConflictsEvent, EventStatus.STATUS_NONE);
      final ListBufferTransitionRelation rel = new ListBufferTransitionRelation
        (PROPERTY_NAME, ComponentKind.PROPERTY, propertyEnc, 1, config);
      rel.setInitial(0, true);
      mPropertyAutomaton = new TRAutomatonProxy(propertyEnc, rel);
      mNumberOfLanguageInclusionChecks = 0;
    }

    //#######################################################################
    //# Simple Access
    private int getNumberOfLanguageInclusionChecks()
    {
      return mNumberOfLanguageInclusionChecks;
    }

    //#######################################################################
    //# Trace Expansion
    private void pruneTrace(final TRTraceProxy trace)
      throws AnalysisException
    {
      // Find the last trace step outside of certain conflicts in the
      // output automaton.
      int index;
      for (index = trace.getNumberOfSteps() - 1; index >= 0; index--) {
        final int s =
          trace.getState(TRAbstractionStepCertainConflicts.this, index);
        if (mLevels[s] < 0) {
          break;
        }
      }
      if (index < 0) {
        // The entire trace is in certain conflicts. Find an initial state
        // at the lowest (= most conflicting) level.
        final int init = findMostConflictingInitialState();
        trace.setState(TRAbstractionStepCertainConflicts.this, 0, init);
        index = 0;
      }
      trace.prune(index + 1);
    }

    private void createLanguageInclusionAutomata(final TRTraceProxy trace,
                                                 final int level,
                                                 final boolean onlyAlwaysEnabled)
      throws AnalysisException
    {
      final Logger logger = LogManager.getLogger();
      mMaxLevel = level;
      final int config =
        mLanguageInclusionChecker.getPreferredInputConfiguration();
      mLanguageInclusionAutomata = new LinkedList<>();
      mCertainConflictsAutomaton = new LanguageInclusionAutomaton
        (trace, TRAbstractionStepCertainConflicts.this,
         config, mCertainConflictsEvent, mMaxLevel, onlyAlwaysEnabled, logger);
      mLanguageInclusionAutomata.add(mCertainConflictsAutomaton);
      final EventEncoding restriction;
      if (onlyAlwaysEnabled) {
        final TRAutomatonProxy aut = mCertainConflictsAutomaton.getAutomaton();
        restriction = aut.getEventEncoding();
      } else {
        restriction = null;
      }
      final Set<TRAbstractionStep> steps = trace.getCoveredAbstractionSteps();
      for (final TRAbstractionStep step : steps) {
        if (step != mPredecessor) {
          final LanguageInclusionAutomaton laut =
            new LanguageInclusionAutomaton(trace, step, config,
                                           restriction, logger);
          if (!laut.isTrivial()) {
            mLanguageInclusionAutomata.add(laut);
          }
        }
      }
    }

    private void restrictToAlwaysEnabled()
    {
      mCertainConflictsAutomaton.restrictToAlwaysEnabled(mCertainConflictsEvent);
      final TRAutomatonProxy aut = mCertainConflictsAutomaton.getAutomaton();
      final EventEncoding restriction = aut.getEventEncoding();
      final Iterator<LanguageInclusionAutomaton> iter =
        mLanguageInclusionAutomata.iterator();
      while (iter.hasNext()) {
        final LanguageInclusionAutomaton laut = iter.next();
        if (laut != mCertainConflictsAutomaton) {
          laut.restrict(restriction);
          if (laut.isTrivial()) {
            iter.remove();
          }
        }
      }
    }

    private void setNextLevel(final TRTraceProxy trace, final int level)
    {
      if (level > mMaxLevel) {
        mCertainConflictsAutomaton.addLevels
          (TRAbstractionStepCertainConflicts.this,
           mCertainConflictsEvent, mMaxLevel + 1, level);
      } else {
        mCertainConflictsAutomaton.removeLevels
          (TRAbstractionStepCertainConflicts.this,
           mCertainConflictsEvent, level + 1, mMaxLevel);
      }
      for (final LanguageInclusionAutomaton laut : mLanguageInclusionAutomata) {
        laut.setCurrentState(trace);
      }
      mMaxLevel = level;
    }

    private TRTraceProxy findTraceExtension()
      throws AnalysisException
    {
      final ProductDESProxy des = createLanguageInclusionModel();
      mLanguageInclusionChecker.setModel(des);
      mLanguageInclusionChecker.run();
      final VerificationResult result =
        mLanguageInclusionChecker.getAnalysisResult();
      mNumberOfLanguageInclusionChecks++;
      // TODO mConflictChecker.recordCCLanguageInclusionCheck(result);
      if (result.isSatisfied()) {
        return null;
      } else {
        final TRTraceProxy trace =
          (TRTraceProxy) mLanguageInclusionChecker.getCounterExample();
        for (final LanguageInclusionAutomaton laut : mLanguageInclusionAutomata) {
          final TRAutomatonProxy aut = laut.getAutomaton();
          final TRAbstractionStep step = laut.getAbstractionStep();
          trace.replaceInputAutomaton(aut, step);
        }
        return trace;
      }
    }

    //#######################################################################
    //# Auxiliary Methods
    private int findMostConflictingInitialState()
      throws AnalysisException
    {
      final int config =
        mLanguageInclusionChecker.getPreferredInputConfiguration();
      final TRAutomatonProxy inputAut =
        mPredecessor.getOutputAutomaton(config);
      final ListBufferTransitionRelation inputRel =
        inputAut.getTransitionRelation();
      final int numStates = inputRel.getNumberOfStates();
      int found = -1;
      int lowest = Integer.MAX_VALUE;
      for (int s = 0; s < numStates; s++) {
        if (inputRel.isInitial(s) && mLevels[s] >= 0 && mLevels[s] < lowest) {
          found = s;
          lowest = mLevels[s];
        }
      }
      assert found >= 0 : "No initial state in certain conflicts automaton!";
      return found;
    }

    private int getLevelOfEndState(final TRTraceProxy trace)
    {
      final int last = trace.getNumberOfSteps() - 1;
      final int end = trace.getState(mPredecessor, last);
      return mLevels[end];
    }

    private ProductDESProxy createLanguageInclusionModel()
    {
      final int numAutomata = mLanguageInclusionAutomata.size() + 1;
      final List<TRAutomatonProxy> automata = new ArrayList<>(numAutomata);
      for (final LanguageInclusionAutomaton laut : mLanguageInclusionAutomata) {
        automata.add(laut.getAutomaton());
      }
      automata.add(mPropertyAutomaton);
      final ProductDESProxyFactory factory =
        mLanguageInclusionChecker.getFactory();
      return
        AutomatonTools.createProductDESProxy(PROPERTY_NAME, automata, factory);
    }

    //#######################################################################
    //# Data Members
    private final TRLanguageInclusionChecker mLanguageInclusionChecker;
    private final EventProxy mCertainConflictsEvent;
    private final TRAutomatonProxy mPropertyAutomaton;
    private List<LanguageInclusionAutomaton> mLanguageInclusionAutomata;
    private LanguageInclusionAutomaton mCertainConflictsAutomaton;
    private int mMaxLevel;
    private int mNumberOfLanguageInclusionChecks;
  }


  //#########################################################################
  //# Inner Class LanguageInclusionAutomaton
  private static class LanguageInclusionAutomaton
  {
    //#######################################################################
    //# Constructor
    private LanguageInclusionAutomaton(final TRTraceProxy trace,
                                       final TRAbstractionStep step,
                                       final int config,
                                       final EventEncoding restriction,
                                       final Logger logger)
      throws AnalysisException
    {
      mAbstractionStep = step;
      final TRAutomatonProxy inputAut = step.getOutputAutomaton(config);
      if (restriction == null) {
        mLanguageInclusionAutomaton = new TRAutomatonProxy(inputAut, config);
      } else {
        final EventEncoding inputEnc = inputAut.getEventEncoding();
        final EventEncoding langEnc = new EventEncoding(inputEnc);
        final int numEvents = langEnc.getNumberOfProperEvents();
        boolean trivial = true;
        for (int e = EventEncoding.NONTAU; e < numEvents; e++) {
          final EventProxy event = langEnc.getProperEvent(e);
          final int r = restriction.getEventCode(event);
          if (r >= 0) {
            final byte status = restriction.getProperEventStatus(r);
            if (EventStatus.isUsedEvent(status)) {
              trivial = false;
              continue;
            }
          }
          langEnc.setProperEventStatus(e, EventStatus.STATUS_UNUSED);
        }
        if (trivial) {
          mLanguageInclusionAutomaton = null;
          return;
        }
        final ListBufferTransitionRelation inputRel =
          inputAut.getTransitionRelation();
        final ListBufferTransitionRelation langRel =
          new ListBufferTransitionRelation(inputRel, langEnc, config);
        mLanguageInclusionAutomaton = new TRAutomatonProxy(langEnc, langRel);
      }
      mCurrentState = getCurrentState(trace);
      final ListBufferTransitionRelation rel =
        mLanguageInclusionAutomaton.getTransitionRelation();
      final int numStates = rel.getNumberOfStates();
      for (int s = 0; s < numStates; s++) {
        rel.setInitial(s, s == mCurrentState);
      }
    }

    private LanguageInclusionAutomaton(final TRTraceProxy trace,
                                       final TRAbstractionStepCertainConflicts step,
                                       final int config,
                                       final EventProxy ccEvent,
                                       final int maxLevel,
                                       final boolean onlyAlwaysEnabled,
                                       final Logger logger)
      throws AnalysisException
    {
      mAbstractionStep = step.getPredecessor();
      final EventEncoding inputEnc = step.getEventEncoding();
      final TRAutomatonProxy inputAut =
        mAbstractionStep.getOutputAutomaton(config);
      final EventEncoding langEnc = inputEnc.clone();
      if (onlyAlwaysEnabled) {
        final int numEvents = langEnc.getNumberOfProperEvents();
        for (int e = EventEncoding.NONTAU; e < numEvents; e++) {
          final byte status = inputEnc.getProperEventStatus(e);
          if (!EventStatus.isAlwaysEnabledEvent(status)) {
            langEnc.setProperEventStatus(e, EventStatus.STATUS_UNUSED);
          }
        }
      }
      final int cc = langEnc.addProperEvent(ccEvent, EventStatus.STATUS_NONE);
      final ListBufferTransitionRelation inputRel =
        inputAut.getTransitionRelation();
      final int numStates = inputRel.getNumberOfStates();
      final AbstractStateBuffer langStateBuffer =
        new IntStateBuffer(numStates, langEnc); // Making new dump state!
      final ListBufferTransitionRelation langRel =
        new ListBufferTransitionRelation(inputRel, langEnc,
                                         langStateBuffer, config);
      mCurrentState = getCurrentState(trace);
      final int[] levels = step.getLevels();
      for (int s = 0; s < numStates; s++) {
        if (inputRel.isReachable(s)) {
          langRel.setInitial(s, s == mCurrentState);
          if (levels[s] >= 0 && levels[s] <= maxLevel) {
            langRel.addTransition(s, cc, s);
          }
        } else {
          langRel.setReachable(s, false);
        }
      }
      mLanguageInclusionAutomaton = new TRAutomatonProxy(langEnc, langRel);
    }

    //#######################################################################
    //# Simple Access
    private TRAbstractionStep getAbstractionStep()
    {
      return mAbstractionStep;
    }

    private TRAutomatonProxy getAutomaton()
    {
      return mLanguageInclusionAutomaton;
    }

    private boolean isTrivial()
    {
      return mLanguageInclusionAutomaton == null;
    }

    //#######################################################################
    //# Auxiliary Methods
    private int getCurrentState(final TRTraceProxy trace)
    {
      final int last = trace.getNumberOfSteps() - 1;
      return trace.getState(mAbstractionStep, last);
    }

    private void setCurrentState(final TRTraceProxy trace)
    {
      final ListBufferTransitionRelation rel =
        mLanguageInclusionAutomaton.getTransitionRelation();
      rel.setInitial(mCurrentState, false);
      mCurrentState = getCurrentState(trace);
      rel.setInitial(mCurrentState, true);
    }

    private void addLevels(final TRAbstractionStepCertainConflicts step,
                           final EventProxy ccEvent,
                           final int minAdded,
                           final int maxAdded)
    {
      final EventEncoding enc = mLanguageInclusionAutomaton.getEventEncoding();
      final int cc = enc.getEventCode(ccEvent);
      final ListBufferTransitionRelation rel =
        mLanguageInclusionAutomaton.getTransitionRelation();
      final int numStates = rel.getNumberOfStates();
      final int[] levels = step.getLevels();
      for (int s = 0; s < numStates; s++) {
        if (rel.isReachable(s) &&
            levels[s] >= minAdded && levels[s] <= maxAdded) {
          rel.addTransition(s, cc, s);
        }
      }
    }

    private void removeLevels(final TRAbstractionStepCertainConflicts step,
                              final EventProxy ccEvent,
                              final int minRemoved,
                              final int maxRemoved)
    {
      final EventEncoding enc = mLanguageInclusionAutomaton.getEventEncoding();
      final int cc = enc.getEventCode(ccEvent);
      final ListBufferTransitionRelation rel =
        mLanguageInclusionAutomaton.getTransitionRelation();
      final int numStates = rel.getNumberOfStates();
      final int[] levels = step.getLevels();
      for (int s = 0; s < numStates; s++) {
        if (rel.isReachable(s) &&
            levels[s] >= minRemoved && levels[s] <= maxRemoved) {
          rel.removeTransition(s, cc, s);
        }
      }
    }

    private void restrictToAlwaysEnabled(final EventProxy ccEvent)
    {
      final EventEncoding enc = mLanguageInclusionAutomaton.getEventEncoding();
      final int cc = enc.getEventCode(ccEvent);
      final int numEvents = enc.getNumberOfProperEvents();
      for (int e = EventEncoding.NONTAU; e < numEvents; e++) {
        if (e != cc) {
          final byte status = enc.getProperEventStatus(e);
          if (!EventStatus.isAlwaysEnabledEvent(status)) {
            enc.setProperEventStatus(e, EventStatus.STATUS_UNUSED);
          }
        }
      }
      removeUnusedEvents();
    }

    private void restrict(final EventEncoding restriction)
    {
      final EventEncoding enc = mLanguageInclusionAutomaton.getEventEncoding();
      final int numEvents = enc.getNumberOfProperEvents();
      boolean trivial = true;
      for (int e = EventEncoding.NONTAU; e < numEvents; e++) {
        final EventProxy event = enc.getProperEvent(e);
        final int r = restriction.getEventCode(event);
        if (r >= 0) {
          final byte status = restriction.getProperEventStatus(r);
          if (EventStatus.isUsedEvent(status)) {
            trivial = false;
            continue;
          }
        }
        enc.setProperEventStatus(e, EventStatus.STATUS_UNUSED);
      }
      if (trivial) {
        mLanguageInclusionAutomaton = null;
      } else {
        removeUnusedEvents();
      }
    }

    private void removeUnusedEvents()
    {
      final ListBufferTransitionRelation rel =
        mLanguageInclusionAutomaton.getTransitionRelation();
      final TransitionIterator iter = rel.createAllTransitionsModifyingIterator();
      while (iter.advance()) {
        final int e = iter.getCurrentEvent();
        final byte status = rel.getProperEventStatus(e);
        if (!EventStatus.isUsedEvent(status)) {
          iter.remove();
        }
      }
    }

    //#######################################################################
    //# Debugging
    @Override
    public String toString()
    {
      return mLanguageInclusionAutomaton.getName();
    }

    //#######################################################################
    //# Data Members
    private final TRAbstractionStep mAbstractionStep;
    private TRAutomatonProxy mLanguageInclusionAutomaton;
    private int mCurrentState;
  }


  //#########################################################################
  //# Data Members
  private final TRAbstractionStep mPredecessor;
  private final EventEncoding mEventEncoding;
  private final LimitedCertainConflictsTRSimplifier mSimplifier;
  private final int[] mLevels;


  //#########################################################################
  //# Class Constants
  private static final String CERTAIN_CONFLICT_EVENT_NAME = ":certainconf";
  private static final String PROPERTY_NAME = ":never";

}
